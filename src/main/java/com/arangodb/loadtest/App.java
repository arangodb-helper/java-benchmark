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

import java.util.ArrayList;
import java.util.Arrays;
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
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDB.Builder;
import com.arangodb.ArangoDBException;
import com.arangodb.Protocol;
import com.arangodb.entity.LoadBalancingStrategy;

/**
 * 
 * @author Mark Vollmary
 *
 */
public class App {

	enum Case {
		READ, WRITE
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(App.class);
	private static final String DB_NAME = "load_test_db";
	private static final String COLLECTION_NAME = "load_test_collection";

	private static final String DEFAULT_HOSTS = "127.0.0.1:8529";
	private static final String DEFAULT_USER = "root";
	private static final String DEFAULT_PW = "";
	private static final Integer DEFAULT_BATCH_SIZE = 1000;
	private static final Integer DEFAULT_THREADS = 1;
	private static final Integer DEFAULT_DOCUMENT_SIZE = 20;
	private static final Integer DEFAULT_DOCUMENT_FIELD_SIZE = 30;
	private static final Protocol DEFAULT_PROTOCOL = Protocol.VST;
	private static final LoadBalancingStrategy DEFAULT_LOAD_BALANCING_STRATEGY = LoadBalancingStrategy.NONE;
	private static final Boolean DEFAULT_DROP_DB = false;
	private static final String DEFAULT_KEY_PREFIX = "";
	private static final Boolean DEFAULT_PRINT_REQUEST = false;
	private static final Boolean DEFAULT_ACQUIRE_HOST_LIST = false;

	private static final String OPTION_CASE = "case";
	private static final String OPTION_HOSTS = "hosts";
	private static final String OPTION_USER = "user";
	private static final String OPTION_PW = "password";
	private static final String OPTION_BATCH_SIZE = "batchSize";
	private static final String OPTION_THREADS = "threads";
	private static final String OPTION_DOCUMENT_SIZE = "docSize";
	private static final String OPTION_DOCUMENT_FIELD_SIZE = "docFieldSize";
	private static final String OPTION_PROTOCOL = "protocol";
	private static final String OPTION_LOAD_BALANCING_STRATEGY = "loadBalancing";
	private static final String OPTION_MAX_CONNECTIONS = "connections";
	private static final String OPTION_DROP_DB = "dropDB";
	private static final String OPTION_KEY_PREFIX = "keyPrefix";
	private static final String OPTION_PRINT_REQUEST = "printRequestTime";
	private static final String OPTION_ACQUIRE_HOST_LIST = "acquireHostList";

	public static void main(final String[] args) {
		final App app = new App();
		final CommandLineParser parser = new BasicParser();
		final Options options = app.createOptions();
		CommandLine cmd;
		try {
			cmd = parser.parse(options, args);
		} catch (final ParseException e) {
			System.err.println(e);
			new HelpFormatter().printHelp("java -jar arangodb-load-test.jar", options);
			System.exit(1);
			return;
		}
		final int batchSize = Integer.valueOf(cmd.getOptionValue(OPTION_BATCH_SIZE, DEFAULT_BATCH_SIZE.toString()));
		final int numThreads = Integer.valueOf(cmd.getOptionValue(OPTION_THREADS, DEFAULT_THREADS.toString()));
		final ArangoDB.Builder builder = new ArangoDB.Builder()
				.useProtocol(
					Protocol.valueOf(cmd.getOptionValue(OPTION_PROTOCOL, DEFAULT_PROTOCOL.toString()).toUpperCase()))
				.user(cmd.getOptionValue(OPTION_USER, DEFAULT_USER)).password(cmd.getOptionValue(OPTION_PW, DEFAULT_PW))
				.loadBalancingStrategy(LoadBalancingStrategy.valueOf(
					cmd.getOptionValue(OPTION_LOAD_BALANCING_STRATEGY, DEFAULT_LOAD_BALANCING_STRATEGY.toString())
							.toUpperCase()))
				.acquireHostList(Boolean
						.valueOf(cmd.getOptionValue(OPTION_ACQUIRE_HOST_LIST, DEFAULT_ACQUIRE_HOST_LIST.toString())));
		final String maxConnections = cmd.getOptionValue(OPTION_MAX_CONNECTIONS, null);
		if (maxConnections != null) {
			builder.maxConnections(Integer.valueOf(maxConnections));
		}

		final String[] hosts = cmd.getOptionValue(OPTION_HOSTS, DEFAULT_HOSTS).split(",");
		Stream.of(hosts).map(e -> e.split(":")).filter(e -> e.length == 2)
				.forEach(e -> builder.host(e[0], Integer.valueOf(e[1])));
		try {
			final boolean detailLog = Boolean
					.valueOf(cmd.getOptionValue(OPTION_PRINT_REQUEST, DEFAULT_PRINT_REQUEST.toString()));
			final String caseString = cmd.getOptionValue(OPTION_CASE);
			if (caseString == null) {
				new HelpFormatter().printHelp("java -jar arangodb-load-test.jar", options);
				System.exit(1);
			}
			final Case caze = Case.valueOf(caseString.toUpperCase());
			if (caze == Case.READ) {
				app.read(builder, batchSize, numThreads, detailLog);
			} else if (caze == Case.WRITE) {
				app.setup(builder, Boolean.valueOf(cmd.getOptionValue(OPTION_DROP_DB, DEFAULT_DROP_DB.toString())));
				final DocumentCreator documentCreator = new DocumentCreator(
						Integer.valueOf(cmd.getOptionValue(OPTION_DOCUMENT_SIZE, DEFAULT_DOCUMENT_SIZE.toString())),
						Integer.valueOf(
							cmd.getOptionValue(OPTION_DOCUMENT_FIELD_SIZE, DEFAULT_DOCUMENT_FIELD_SIZE.toString())),
						cmd.getOptionValue(OPTION_KEY_PREFIX, DEFAULT_KEY_PREFIX));
				app.write(builder, documentCreator, batchSize, numThreads, detailLog);
			}
		} catch (final InterruptedException e) {
			LOGGER.error("Failed", e);
		}
	}

	@SuppressWarnings("static-access")
	private Options createOptions() {
		final Options options = new Options();
		options.addOption(OptionBuilder.withArgName(OPTION_CASE).hasArg()
				.withDescription(String.format("Use-case (%s)", enumOptions(Case.values()))).isRequired()
				.create(OPTION_CASE));
		options.addOption(OptionBuilder.withArgName(OPTION_HOSTS).hasArg()
				.withDescription(String.format("Comma separated host addresses (default: %s)", DEFAULT_HOSTS))
				.create(OPTION_HOSTS));
		options.addOption(OptionBuilder.withArgName(OPTION_USER).hasArg()
				.withDescription(String.format("User (default: %s)", DEFAULT_USER)).create(OPTION_USER));
		options.addOption(OptionBuilder.withArgName(OPTION_PW).hasArg()
				.withDescription(String.format("Password (default: %s)", DEFAULT_PW)).create(OPTION_PW));
		options.addOption(OptionBuilder.withArgName(OPTION_BATCH_SIZE).hasArg()
				.withDescription(
					String.format("Number of documents processed in one batch (default: %s)", DEFAULT_BATCH_SIZE))
				.create(OPTION_BATCH_SIZE));
		options.addOption(OptionBuilder.withArgName(OPTION_THREADS).hasArg()
				.withDescription(String.format("Number of client threads (default: %s)", DEFAULT_THREADS))
				.create(OPTION_THREADS));
		options.addOption(OptionBuilder.withArgName(OPTION_DOCUMENT_SIZE).hasArg()
				.withDescription(String.format("Number of field in the documents (default: %s)", DEFAULT_DOCUMENT_SIZE))
				.create(OPTION_DOCUMENT_SIZE));
		options.addOption(OptionBuilder.withArgName(OPTION_DOCUMENT_FIELD_SIZE).hasArg()
				.withDescription(
					String.format("The field size in the documents (default: %s)", DEFAULT_DOCUMENT_FIELD_SIZE))
				.create(OPTION_DOCUMENT_FIELD_SIZE));
		options.addOption(OptionBuilder.withArgName(OPTION_PROTOCOL).hasArg().withDescription(
			String.format("Network protocol (%s) (default: %s)", enumOptions(Protocol.values()), DEFAULT_PROTOCOL))
				.create(OPTION_PROTOCOL));
		options.addOption(OptionBuilder.withArgName(OPTION_LOAD_BALANCING_STRATEGY).hasArg()
				.withDescription(String.format("Load balancing strategy (%s) (default: %s)",
					enumOptions(LoadBalancingStrategy.values()), DEFAULT_LOAD_BALANCING_STRATEGY))
				.create(OPTION_LOAD_BALANCING_STRATEGY));
		options.addOption(OptionBuilder.withArgName(OPTION_ACQUIRE_HOST_LIST).hasArg()
				.withDescription(String.format("Acquire list of hosts (default %s)", DEFAULT_ACQUIRE_HOST_LIST))
				.create(OPTION_ACQUIRE_HOST_LIST));
		options.addOption(OptionBuilder.withArgName(OPTION_MAX_CONNECTIONS).hasArg()
				.withDescription(String.format("Connections per thread (default for vst: 0, http: 20)"))
				.create(OPTION_MAX_CONNECTIONS));
		options.addOption(OptionBuilder.withArgName(OPTION_DROP_DB).hasArg()
				.withDescription(String.format("Drop DB before run (default: %s)", DEFAULT_DROP_DB))
				.create(OPTION_DROP_DB));
		options.addOption(
			OptionBuilder.withArgName(OPTION_KEY_PREFIX).hasArg()
					.withDescription(String.format(
						"Document key prefix (when running on multiple clients) (default: %s)", DEFAULT_KEY_PREFIX))
					.create(OPTION_KEY_PREFIX));
		options.addOption(OptionBuilder.withArgName(OPTION_PRINT_REQUEST).hasArg()
				.withDescription(String.format("Print time for every request (default: %s)", DEFAULT_PRINT_REQUEST))
				.create(OPTION_PRINT_REQUEST));
		return options;
	}

	private static String enumOptions(final Enum<?>[] values) {
		return Arrays.asList(values).stream().map(e -> e.name().toLowerCase()).reduce((a, b) -> a + "," + b).get();
	}

	private void setup(final Builder builder, final boolean dropDB) {
		final ArangoDB arangoDB = builder.build();
		if (dropDB) {
			try {
				arangoDB.db(DB_NAME).drop();
			} catch (final ArangoDBException e) {
			}
		}
		try {
			arangoDB.createDatabase(DB_NAME);
		} catch (final ArangoDBException e) {
			if (!arangoDB.db(DB_NAME).exists()) {
				LOGGER.error(String.format("Failed to create database: %s", DB_NAME));
			}
		}
		try {
			arangoDB.db(DB_NAME).createCollection(COLLECTION_NAME);
		} catch (final Exception e) {
			if (!arangoDB.db(DB_NAME).collection(COLLECTION_NAME).exists()) {
				LOGGER.error(String.format("Failed to create collection %s", COLLECTION_NAME));
			}
		}
	}

	private void write(
		final ArangoDB.Builder builder,
		final DocumentCreator documentCreator,
		final int batchSize,
		final int numThreads,
		final boolean detailLog) throws InterruptedException {
		LOGGER.info(String.format("starting writes with %s threads", numThreads));

		final Map<String, Collection<Long>> times = new ConcurrentHashMap<>();
		final WriterWorkerThread[] workers = new WriterWorkerThread[numThreads];
		for (int i = 0; i < workers.length; i++) {
			workers[i] = new WriterWorkerThread(builder, documentCreator, i, batchSize, detailLog, times);
		}
		for (int i = 0; i < workers.length; i++) {
			workers[i].start();
		}
		final Stopwatch sw = new Stopwatch();
		for (;;) {
			final long elapsedTime = sw.getElapsedTime();
			if (elapsedTime >= 10000) {
				final List<Long> collect = new ArrayList<>();
				times.values().forEach(collect::addAll);
				times.values().forEach(Collection::clear);
				Collections.sort(collect);
				final int calls = collect.size();
				LOGGER.info(
					String.format("All %s threads perform %s write calls (%s documents) within the last %s seconds",
						numThreads, calls, calls * batchSize, (int) (elapsedTime / 1000)));
				sw.start();
			}
		}
	}

	private void read(
		final ArangoDB.Builder builder,
		final int batchSize,
		final int numThreads,
		final boolean detailLog) throws InterruptedException {
		LOGGER.info(String.format("starting reads with %s threads", numThreads));

		final ReaderWorkerThread[] workers = new ReaderWorkerThread[numThreads];
		for (int i = 0; i < workers.length; i++) {
			workers[i] = new ReaderWorkerThread(builder, i, batchSize, detailLog);
		}
		for (int i = 0; i < workers.length; i++) {
			workers[i].start();
		}
		for (int i = 0; i < workers.length; i++) {
			workers[i].join();
		}
	}

	class WriterWorkerThread extends Thread {

		private final DocumentWriter writer;
		private final int batchSize;

		public WriterWorkerThread(final ArangoDB.Builder builder, final DocumentCreator documentCreator, final int num,
			final int batchSize, final boolean log, final Map<String, Collection<Long>> times) {
			super();
			final ArrayList<Long> l = new ArrayList<>();
			times.put("thread" + num, l);
			this.writer = new DocumentWriter(builder, DB_NAME, COLLECTION_NAME, documentCreator, num, log, l);
			this.batchSize = batchSize;
		}

		@Override
		public void run() {
			try {
				for (;;) {
					writer.write(batchSize);
				}
			} catch (final Exception e) {
				LOGGER.error("Failed to upload documents", e);
			}
		}

	};

	class ReaderWorkerThread extends Thread {

		private final DocumentReader reader;
		private final int batchSize;

		public ReaderWorkerThread(final ArangoDB.Builder builder, final int num, final int batchSize,
			final boolean log) {
			super();
			this.reader = new DocumentReader(builder, DB_NAME, COLLECTION_NAME, num, log);
			this.batchSize = batchSize;
		}

		@Override
		public void run() {
			try {
				for (;;) {
					reader.read(batchSize);
				}
			} catch (final Exception e) {
				LOGGER.error("Failed to download documents", e);
			}
		}

	};
}
