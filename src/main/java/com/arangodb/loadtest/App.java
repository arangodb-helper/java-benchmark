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
import java.util.stream.Collectors;
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
import com.arangodb.model.CollectionCreateOptions;

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
	private static final String USAGE_INFO = "java -jar arangodb-load-test.jar";
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
	private static final String OPTION_NUMBER_OF_SHARDS = "numberOfShards";
	private static final String OPTION_REPLICATION_FACTOR = "replicationFactor";
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
			new HelpFormatter().printHelp(USAGE_INFO, options);
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
				new HelpFormatter().printHelp(USAGE_INFO, options);
				System.exit(1);
			}

			final List<String[]> cases = Stream.of(caseString.split(",")).map(e -> e.split(":"))
					.collect(Collectors.toList());
			for (final String[] caze : cases) {
				final Integer operations = caze.length > 1 ? Integer.valueOf(caze[1]) : null;
				run(app, cmd, batchSize, numThreads, builder, detailLog, Case.valueOf(caze[0].toUpperCase()),
					operations);
			}
		} catch (final InterruptedException e) {
			LOGGER.error("Failed", e);
		}
	}

	private static void run(
		final App app,
		final CommandLine cmd,
		final int batchSize,
		final int numThreads,
		final ArangoDB.Builder builder,
		final boolean detailLog,
		final Case caze,
		final Integer operations) throws InterruptedException {
		final String keyPrefix = cmd.getOptionValue(OPTION_KEY_PREFIX, DEFAULT_KEY_PREFIX);
		if (caze == Case.READ) {
			app.read(builder, batchSize, numThreads, detailLog, keyPrefix, operations);
		} else if (caze == Case.WRITE) {
			final String numberOfShards = cmd.getOptionValue(OPTION_NUMBER_OF_SHARDS);
			final String replicationFactor = cmd.getOptionValue(OPTION_REPLICATION_FACTOR);
			app.setup(builder, Boolean.valueOf(cmd.getOptionValue(OPTION_DROP_DB, DEFAULT_DROP_DB.toString())),
				numberOfShards != null ? Integer.valueOf(numberOfShards) : null,
				replicationFactor != null ? Integer.valueOf(replicationFactor) : null);
			final DocumentCreator.Builder documentCreator = new DocumentCreator.Builder(
					Integer.valueOf(cmd.getOptionValue(OPTION_DOCUMENT_SIZE, DEFAULT_DOCUMENT_SIZE.toString())),
					Integer.valueOf(
						cmd.getOptionValue(OPTION_DOCUMENT_FIELD_SIZE, DEFAULT_DOCUMENT_FIELD_SIZE.toString())),
					batchSize);
			app.write(builder, documentCreator, batchSize, numThreads, detailLog, keyPrefix, operations);
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
				.withDescription(String.format("Connections per thread (default for vst: 1, http: 20)"))
				.create(OPTION_MAX_CONNECTIONS));
		options.addOption(OptionBuilder.withArgName(OPTION_DROP_DB).hasArg()
				.withDescription(String.format("Drop DB before run (default: %s)", DEFAULT_DROP_DB))
				.create(OPTION_DROP_DB));
		options.addOption(OptionBuilder.withArgName(OPTION_NUMBER_OF_SHARDS).hasArg()
				.withDescription(String.format("Collection number of shards (default: 1)"))
				.create(OPTION_NUMBER_OF_SHARDS));
		options.addOption(OptionBuilder.withArgName(OPTION_REPLICATION_FACTOR).hasArg()
				.withDescription(String.format("Collection replication factor (default: 1)"))
				.create(OPTION_REPLICATION_FACTOR));
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

	private void setup(
		final Builder builder,
		final boolean dropDB,
		final Integer numberOfShards,
		final Integer replicationFactor) {
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
			arangoDB.db(DB_NAME).createCollection(COLLECTION_NAME,
				new CollectionCreateOptions().numberOfShards(numberOfShards).replicationFactor(replicationFactor));
		} catch (final Exception e) {
			if (!arangoDB.db(DB_NAME).collection(COLLECTION_NAME).exists()) {
				LOGGER.error(String.format("Failed to create collection %s", COLLECTION_NAME));
			}
		}
	}

	private void collectData(
		final int batchSize,
		final int numThreads,
		final Map<String, Collection<Long>> times,
		final String type,
		final Integer operations) {
		final Stopwatch sw = new Stopwatch();
		int currentOp = 0;
		while (operations == null || currentOp < operations) {
			final long elapsedTime = sw.getElapsedTime();
			if (elapsedTime >= 10000) {
				final List<Long> requests = new ArrayList<>();
				times.values().forEach(requests::addAll);
				times.values().forEach(Collection::clear);
				currentOp += requests.size();
				Collections.sort(requests);
				final int numRequests = requests.size();
				final Long average, min, max, p95th, p99th;
				if (numRequests > 0) {
					average = requests.stream().reduce((a, b) -> a + b).map(e -> e / numRequests).orElse(0L);
					min = requests.get(0);
					max = requests.get(numRequests - 1);
					p95th = requests.get((int) ((numRequests * 0.95) - 1));
					p99th = requests.get((int) ((numRequests * 0.99) - 1));
				} else {
					average = min = max = p95th = p99th = 0L;
				}
				LOGGER.info(String.format(
					"Within the last %s sec: Threads %s, %s requests %s, Documents %s, Latency[Average: %s ms, Min: %s ms, Max: %s ms, 95th: %s ms, 99th: %s ms]",
					(int) (elapsedTime / 1000), numThreads, type, numRequests, numRequests * batchSize, average, min,
					max, p95th, p99th));
				sw.start();
			}
		}
	}

	private void write(
		final ArangoDB.Builder builder,
		final DocumentCreator.Builder documentCreator,
		final int batchSize,
		final int numThreads,
		final boolean detailLog,
		final String keyPrefix,
		final Integer operations) throws InterruptedException {
		LOGGER.info(String.format("starting writes with %s threads", numThreads));

		final Map<String, Collection<Long>> times = new ConcurrentHashMap<>();
		final WriterWorkerThread[] workers = new WriterWorkerThread[numThreads];
		for (int i = 0; i < workers.length; i++) {
			workers[i] = new WriterWorkerThread(builder, documentCreator, i, batchSize, detailLog, times, keyPrefix,
					operations);
		}
		for (int i = 0; i < workers.length; i++) {
			workers[i].start();
		}
		collectData(batchSize, numThreads, times, "Write", operations);
		for (int i = 0; i < workers.length; i++) {
			workers[i].join();
		}
	}

	private void read(
		final ArangoDB.Builder builder,
		final int batchSize,
		final int numThreads,
		final boolean detailLog,
		final String keyPrefix,
		final Integer operations) throws InterruptedException {
		LOGGER.info(String.format("starting reads with %s threads", numThreads));

		final Map<String, Collection<Long>> times = new ConcurrentHashMap<>();
		final ReaderWorkerThread[] workers = new ReaderWorkerThread[numThreads];
		for (int i = 0; i < workers.length; i++) {
			workers[i] = new ReaderWorkerThread(builder, i, batchSize, detailLog, times, keyPrefix, operations);
		}
		for (int i = 0; i < workers.length; i++) {
			workers[i].start();
		}
		collectData(batchSize, numThreads, times, "Read", operations);
		for (int i = 0; i < workers.length; i++) {
			workers[i].join();
		}
	}

	class WriterWorkerThread extends Thread {

		private final DocumentWriter writer;
		private final int batchSize;
		private final Integer operations;

		public WriterWorkerThread(final ArangoDB.Builder builder, final DocumentCreator.Builder documentCreator,
			final int num, final int batchSize, final boolean log, final Map<String, Collection<Long>> times,
			final String keyPrefix, final Integer operations) {
			super();
			this.operations = operations;
			final ArrayList<Long> l = new ArrayList<>();
			times.put("thread" + num, l);
			this.writer = new DocumentWriter(builder, DB_NAME, COLLECTION_NAME, documentCreator.build(), num, log, l,
					keyPrefix);
			this.batchSize = batchSize;
		}

		@Override
		public void run() {
			try {
				if (operations == null) {
					for (;;) {
						writer.write(batchSize);
					}
				} else {
					for (int i = 0; i < operations; i++) {
						writer.write(batchSize);
					}
				}
			} catch (final Exception e) {
				LOGGER.error("Failed to upload documents", e);
			}
		}

	};

	class ReaderWorkerThread extends Thread {

		private final DocumentReader reader;
		private final int batchSize;
		private final Integer operations;

		public ReaderWorkerThread(final ArangoDB.Builder builder, final int num, final int batchSize, final boolean log,
			final Map<String, Collection<Long>> times, final String keyPrefix, final Integer operations) {
			super();
			this.operations = operations;
			final ArrayList<Long> l = new ArrayList<>();
			times.put("thread" + num, l);
			this.reader = new DocumentReader(builder, DB_NAME, COLLECTION_NAME, num, log, l, keyPrefix);
			this.batchSize = batchSize;
		}

		@Override
		public void run() {
			try {
				if (operations == null) {
					for (;;) {
						reader.read(batchSize);
					}
				} else {
					for (int i = 0; i < operations; i++) {
						reader.read(batchSize);
					}
				}
			} catch (final Exception e) {
				LOGGER.error("Failed to download documents", e);
			}
		}

	};
}
