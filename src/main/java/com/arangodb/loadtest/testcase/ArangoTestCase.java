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

package com.arangodb.loadtest.testcase;

import java.io.Closeable;
import java.io.IOException;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.loadtest.cli.CliOptions;
import com.arangodb.loadtest.util.DocumentCreator;
import com.arangodb.loadtest.util.KeyGen;
import com.arangodb.loadtest.util.Stopwatch;

/**
 * @author Mark Vollmary
 *
 */
public abstract class ArangoTestCase implements Closeable {

	private static final Logger LOGGER = LoggerFactory.getLogger(ArangoTestCase.class);

	public static interface InstanceCreator {
		ArangoTestCase create(
			ArangoDB.Builder builder,
			CliOptions options,
			int num,
			Collection<Long> times,
			KeyGen keyGen,
			DocumentCreator documentCreator);
	}

	protected final CliOptions options;
	protected final ArangoDB arango;
	protected final int num;
	protected final Collection<Long> times;

	public ArangoTestCase(final ArangoDB.Builder builder, final CliOptions options, final int num,
		final Collection<Long> times) {
		super();
		arango = builder.build();
		this.options = options;
		this.num = num;
		this.times = times;
	}

	protected void _prepare() {
	};

	protected abstract void _run() throws ArangoDBException;

	public void run() throws ArangoDBException {
		_prepare();
		final Stopwatch sw = new Stopwatch();
		try {
			_run();
		} catch (final ArangoDBException e) {
			LOGGER.error("Error during test run", e);
		}
		final long elapsedTime = sw.getElapsedTime();
		times.add(elapsedTime);
	};

	@Override
	public void close() throws IOException {
		arango.shutdown();
	}
}
