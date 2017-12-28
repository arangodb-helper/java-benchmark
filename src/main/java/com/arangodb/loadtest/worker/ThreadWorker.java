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

package com.arangodb.loadtest.worker;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arangodb.ArangoDB;
import com.arangodb.loadtest.cli.CliOptions;
import com.arangodb.loadtest.testcase.ArangoTestCase;
import com.arangodb.loadtest.util.DocumentCreator;
import com.arangodb.loadtest.util.KeyGen;
import com.arangodb.loadtest.util.Stopwatch;

/**
 * @author Mark Vollmary
 *
 */
public class ThreadWorker extends Thread implements Closeable {

	public static interface InstanceCreator {
		ThreadWorker create(int num, Map<String, Collection<Long>> times);
	}

	private static final Logger LOGGER = LoggerFactory.getLogger(ThreadWorker.class);
	private final CliOptions options;
	private final ArangoTestCase test;

	public ThreadWorker(final ArangoDB.Builder builder, final CliOptions options, final int num,
		final Map<String, Collection<Long>> times, final ArangoTestCase.InstanceCreator instanceCreator,
		final DocumentCreator documentCreator, final int run) {
		super();
		this.options = options;
		final ArrayList<Long> l = new ArrayList<>();
		times.put("thread" + num, l);
		test = instanceCreator.create(builder, options, num, l, new KeyGen(options, num, run), documentCreator);
	}

	@Override
	public void run() {
		try {
			if (options.getDuration() > 0) {
				final Integer duration = options.getDuration();
				final Stopwatch sw = new Stopwatch();
				while ((sw.getElapsedTime() / 1000 / 1000 / 1000) < duration) {
					test.run();
				}
			} else {
				for (int i = 0; i < options.getRequests(); i++) {
					test.run();
				}
			}
		} catch (final Exception e) {
			LOGGER.error("Failed to execute request", e);
		}
	}

	@Override
	public void close() throws IOException {
		test.close();
	}

}
