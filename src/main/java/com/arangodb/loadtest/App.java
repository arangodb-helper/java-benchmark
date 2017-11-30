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

import java.util.Arrays;

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

	private static final String DEFAULT_IP = "127.0.0.1";
	private static final Integer DEFAULT_PORT = 8529;
	private static final String DEFAULT_USER = "root";
	private static final String DEFAULT_PW = "";
	private static final Integer DEFAULT_BATCH_SIZE = 1000;
	private static final Integer DEFAULT_THREADS = 1;
	private static final Integer DEFAULT_DOCUMENT_SIZE = 20;
	private static final Integer DEFAULT_DOCUMENT_FIELD_SIZE = 30;
	private static final Protocol DEFAULT_PROTOCOL = Protocol.VST;

	private static final String OPTION_CASE = "case";
	private static final String OPTION_IP = "ip";
	private static final String OPTION_PORT = "port";
	private static final String OPTION_USER = "user";
	private static final String OPTION_PW = "password";
	private static final String OPTION_BATCH_SIZE = "batchSize";
	private static final String OPTION_THREADS = "threads";
	private static final String OPTION_DOCUMENT_SIZE = "docSize";
	private static final String OPTION_DOCUMENT_FIELD_SIZE = "docFieldSize";
	private static final String OPTION_PROTOCOL = "protocol";

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
				.host(cmd.getOptionValue(OPTION_IP, DEFAULT_IP),
					Integer.valueOf(cmd.getOptionValue(OPTION_PORT, DEFAULT_PORT.toString())))
				.useProtocol(
					Protocol.valueOf(cmd.getOptionValue(OPTION_PROTOCOL, DEFAULT_PROTOCOL.toString()).toUpperCase()))
				.user(cmd.getOptionValue(OPTION_USER, DEFAULT_USER))
				.password(cmd.getOptionValue(OPTION_PW, DEFAULT_PW));
		try {
			final String caseString = cmd.getOptionValue(OPTION_CASE);
			if (caseString == null) {
				new HelpFormatter().printHelp("java -jar arangodb-load-test.jar", options);
				System.exit(1);
			}
			final Case caze = Case.valueOf(caseString.toUpperCase());
			if (caze == Case.READ) {
				app.read(builder, batchSize, numThreads);
			} else if (caze == Case.WRITE) {
				app.setup(builder);
				final DocumentCreator documentCreator = new DocumentCreator(
						Integer.valueOf(cmd.getOptionValue(OPTION_DOCUMENT_SIZE, DEFAULT_DOCUMENT_SIZE.toString())),
						Integer.valueOf(
							cmd.getOptionValue(OPTION_DOCUMENT_FIELD_SIZE, DEFAULT_DOCUMENT_FIELD_SIZE.toString())));
				app.write(builder, documentCreator, batchSize, numThreads);
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
		options.addOption(OptionBuilder.withArgName(OPTION_IP).hasArg()
				.withDescription(String.format("Server address (default: %s)", DEFAULT_IP)).create(OPTION_IP));
		options.addOption(OptionBuilder.withArgName(OPTION_PORT).hasArg()
				.withDescription(String.format("Server port (default: %s)", DEFAULT_PORT)).create(OPTION_PORT));
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
		return options;
	}

	private static String enumOptions(final Enum<?>[] values) {
		return Arrays.asList(values).stream().map(e -> e.name().toLowerCase()).reduce((a, b) -> a + "," + b).get();
	}

	private void setup(final Builder builder) {
		final ArangoDB arangoDB = builder.build();
		try {
			arangoDB.db(DB_NAME).drop();
		} catch (final ArangoDBException e) {
		}
		try {
			arangoDB.createDatabase(DB_NAME);
		} catch (final ArangoDBException e) {
			LOGGER.error(String.format("Failed to create database: %s", DB_NAME), e);
		}
		try {
			arangoDB.db(DB_NAME).createCollection(COLLECTION_NAME);
		} catch (final Exception e) {
			LOGGER.error(String.format("Failed to create collection %s", COLLECTION_NAME), e);
		}
	}

	private void write(
		final ArangoDB.Builder builder,
		final DocumentCreator documentCreator,
		final int batchSize,
		final int numThreads) throws InterruptedException {
		LOGGER.info(String.format("starting writes with %s threads", numThreads));

		final WriterWorkerThread[] workers = new WriterWorkerThread[numThreads];
		for (int i = 0; i < workers.length; i++) {
			workers[i] = new WriterWorkerThread(builder, documentCreator, i, batchSize);
		}
		for (int i = 0; i < workers.length; i++) {
			workers[i].start();
		}
		for (int i = 0; i < workers.length; i++) {
			workers[i].join();
		}
	}

	private void read(final ArangoDB.Builder builder, final int batchSize, final int numThreads)
			throws InterruptedException {
		LOGGER.info(String.format("starting reads with %s threads", numThreads));

		final ReaderWorkerThread[] workers = new ReaderWorkerThread[numThreads];
		for (int i = 0; i < workers.length; i++) {
			workers[i] = new ReaderWorkerThread(builder, i, batchSize);
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
			final int batchSize) {
			super();
			this.writer = new DocumentWriter(builder, DB_NAME, COLLECTION_NAME, documentCreator, num);
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

		public ReaderWorkerThread(final ArangoDB.Builder builder, final int num, final int batchSize) {
			super();
			this.reader = new DocumentReader(builder, DB_NAME, COLLECTION_NAME, num);
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
