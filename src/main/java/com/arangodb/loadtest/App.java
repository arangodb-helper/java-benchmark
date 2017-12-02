/*
 * DISCLAIMER
 *
 * Copyright 2017 ArangoDB GmbH, Cologne, Germany
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Copyright holder is ArangoDB GmbH, Cologne, Germany
 */

package com.arangodb.loadtest;

import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arangodb.ArangoDB;
import com.arangodb.loadtest.cli.CliOptionUtils;
import com.arangodb.loadtest.cli.CliOptions;
import com.arangodb.loadtest.testcase.DocumentImportTestCase;
import com.arangodb.loadtest.testcase.DocumentInsertTestCase;
import com.arangodb.loadtest.testcase.DocumentReadTestCase;
import com.arangodb.loadtest.testcase.DocumentReplaceTestCase;
import com.arangodb.loadtest.testcase.DocumentUpdateTestCase;
import com.arangodb.loadtest.testcase.GetVersionTestCase;
import com.arangodb.loadtest.testcase.TestCase;
import com.arangodb.loadtest.util.DatabaseSetupUtils;
import com.arangodb.loadtest.util.DocumentCreator;
import com.arangodb.loadtest.worker.ThreadWorker;
import com.arangodb.loadtest.worker.ThreadWorker.InstanceCreator;

/**
 * 
 * @author Mark Vollmary
 *
 */
public class App {

	private static final Logger LOGGER = LoggerFactory.getLogger(App.class);
	private static final String USAGE_INFO = "java -jar arangodb-load-test.jar";

	public static void main(final String[] args) {
		final App app = new App();
		final CommandLineParser parser = new BasicParser();
		final Options opts = CliOptionUtils.createOptions();
		CommandLine cmd;
		CliOptions options;
		try {
			cmd = parser.parse(opts, args);
			options = CliOptionUtils.readOptions(cmd);
		} catch (final ParseException e) {
			System.err.println(e);
			new HelpFormatter().printHelp(USAGE_INFO, opts);
			System.exit(1);
			return;
		}
		final ArangoDB.Builder builder = new ArangoDB.Builder().useProtocol(options.getProtocol())
				.user(options.getUser()).password(options.getPassword())
				.loadBalancingStrategy(options.getLoadBalancing()).acquireHostList(options.getAcquireHostList())
				.maxConnections(options.getConnections());

		Stream.of(options.getEndpoints().split(",")).map(e -> e.split(":")).filter(e -> e.length == 2)
				.forEach(e -> builder.host(e[0], Integer.valueOf(e[1])));
		try {
			final Collection<TestCase> tests = options.getTest();
			if (tests == null) {
				new HelpFormatter().printHelp(USAGE_INFO, opts);
				System.exit(1);
			}
			run(app, options, builder, tests, System.out);
		} catch (final Exception e) {
			LOGGER.error("Failed", e);
		}
	}

	private static void run(
		final App app,
		final CliOptions options,
		final ArangoDB.Builder builder,
		final Collection<TestCase> tests,
		final PrintStream out) throws InterruptedException, IOException {
		for (int i = 0; i < options.getRuns(); i++) {
			final Integer delay = options.getDelay();
			if (i > 0 && delay > 0) {
				out.println(String.format("## SLEEP %s seconds till next run", delay));
				try {
					Thread.sleep(delay * 1000);
				} catch (final InterruptedException e) {
				}
			}
			out.println("# RUN " + (i + 1));
			final boolean dropDB = (options.getDropDB() != null && options.getDropDB().booleanValue()) || i > 0;
			DatabaseSetupUtils.setup(builder, options, dropDB);
			for (final TestCase test : tests) {
				final InstanceCreator creator;
				switch (test) {
				case VERSION:
					creator = (num, times) -> new ThreadWorker(builder, options, num, times,
							(b, o, n, t, k, d) -> new GetVersionTestCase(b, o, n, t), null);
					break;
				case DOCUMENT_GET:
					creator = (num, times) -> new ThreadWorker(builder, options, num, times,
							(b, o, n, t, k, d) -> new DocumentReadTestCase(b, o, n, t, k), null);
					break;
				case DOCUMENT_INSERT:
					creator = (num, times) -> new ThreadWorker(builder, options, num, times,
							(b, o, n, t, k, d) -> new DocumentInsertTestCase(b, o, n, t, k, d),
							new DocumentCreator(options));
					break;
				case DOCUMENT_IMPORT:
					creator = (num, times) -> new ThreadWorker(builder, options, num, times,
							(b, o, n, t, k, d) -> new DocumentImportTestCase(b, o, n, t, k, d),
							new DocumentCreator(options));
					break;
				case DOCUMENT_UPDATE:
					creator = (num, times) -> new ThreadWorker(builder, options, num, times,
							(b, o, n, t, k, d) -> new DocumentUpdateTestCase(b, o, n, t, k, d),
							new DocumentCreator(options));
					break;
				case DOCUMENT_REPLACE:
					creator = (num, times) -> new ThreadWorker(builder, options, num, times,
							(b, o, n, t, k, d) -> new DocumentReplaceTestCase(b, o, n, t, k, d),
							new DocumentCreator(options));
					break;
				default:
					continue;
				}
				app.run(options, test, creator, out);
			}
		}
	}

	private void run(
		final CliOptions options,
		final TestCase testCase,
		final InstanceCreator creator,
		final PrintStream out) throws InterruptedException, IOException {
		out.println(String.format("## TEST CASE \"%s\". %s threads, %s connections/thread, %s protocol",
			testCase.toString().toLowerCase(), options.getThreads(), options.getConnections(),
			options.getProtocol().toString().toLowerCase()));

		final Map<String, Collection<Long>> times = new ConcurrentHashMap<>();
		final ThreadWorker[] workers = new ThreadWorker[options.getThreads()];
		for (int i = 0; i < workers.length; i++) {
			workers[i] = creator.create(i, times);
		}
		for (int i = 0; i < workers.length; i++) {
			workers[i].start();
		}
		collectData(options, times, testCase.toString().toLowerCase(), out);
		for (int i = 0; i < workers.length; i++) {
			workers[i].join();
		}
		for (int i = 0; i < workers.length; i++) {
			workers[i].close();
		}
	}

	private void collectData(
		final CliOptions options,
		final Map<String, Collection<Long>> times,
		final String type,
		final PrintStream out) {
		final Integer numThreads = options.getThreads();
		final int batchSize = options.getBatchSize();
		int currentOp = 0;
		final int sleep = options.getOutputInterval() * 1000;
		out.println(
			"elapsed time (sec), threads, requests, batch, latency average (ms), latency min (ms), latency max (ms), latency 50th (ms), latency 95th (ms), latency 99th (ms)");
		while (currentOp < options.getRequests()) {
			try {
				Thread.sleep(sleep);
			} catch (final InterruptedException e) {
			}
			final List<Long> requests = new ArrayList<>();
			times.values().forEach(requests::addAll);
			times.values().forEach(Collection::clear);
			currentOp += requests.size();
			Collections.sort(requests);
			final int numRequests = requests.size();
			final Double average, min, max, p50th, p95th, p99th;
			if (numRequests > 0) {
				average = toMs(requests.stream().reduce((a, b) -> a + b).map(e -> e / numRequests).orElse(0L));
				min = toMs(requests.get(0));
				max = toMs(requests.get(numRequests - 1));
				p50th = toMs(requests.get((int) ((numRequests * 0.5) - 1)));
				p95th = toMs(requests.get((int) ((numRequests * 0.95) - 1)));
				p99th = toMs(requests.get((int) ((numRequests * 0.99) - 1)));
			} else {
				average = min = max = p50th = p95th = p99th = 0.;
			}
			final Number[] d = new Number[] { sleep / 1000, numThreads, numRequests, numRequests * batchSize, average,
					min, max, p50th, p95th, p99th };
			out.println(Stream.of(d).map(n -> n.toString()).reduce((a, b) -> a + ", " + b).get());
		}
	}

	private static Double toMs(final Long nanoSec) {
		final Double microSec = (double) (nanoSec / 1000);
		final Double milliSec = microSec / 1000;
		return milliSec;
	}

}
