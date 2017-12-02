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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.MultiDocumentEntity;
import com.arangodb.loadtest.cli.CliOptions;
import com.arangodb.loadtest.util.KeyGen;
import com.arangodb.loadtest.util.Stopwatch;

/**
 * 
 * @author Mark Vollmary
 *
 */
public class DocumentReadTestCase implements ArangoTestCase {

	private static Logger LOGGER = LoggerFactory.getLogger(DocumentReadTestCase.class);

	private final ArangoDB arango;
	private final ArangoCollection collection;
	private final String id;
	private final boolean log;
	private final ArrayList<Long> times;

	private final CliOptions options;
	private final KeyGen keyGen;

	public DocumentReadTestCase(final ArangoDB.Builder builder, final CliOptions options, final KeyGen keyGen,
		final int num, final ArrayList<Long> times) {
		this.options = options;
		this.keyGen = keyGen;
		this.log = false;
		this.times = times;
		arango = builder.build();
		collection = arango.db(options.getDatabase()).collection(options.getCollection());
		this.id = (new Integer(num)).toString();
	}

	@Override
	public void close() throws IOException {
		arango.shutdown();
	}

	@Override
	public void run() throws ArangoDBException {
		final Integer batchSize = options.getBatchSize();
		final List<String> keys = keyGen.generateKeys(batchSize);
		final Stopwatch sw = new Stopwatch();
		if (batchSize == 1) {
			final BaseDocument doc = collection.getDocument(keys.get(0), BaseDocument.class);
			if (doc == null) {
				throw new ArangoDBException(String.format("Failed to read document with key: %s", keys.get(0)));
			}
		} else {
			final MultiDocumentEntity<BaseDocument> documents = collection.getDocuments(keys, BaseDocument.class);
			final int numDocs = documents.getDocuments().size();
			if (numDocs != batchSize) {
				throw new ArangoDBException(String.format("Failed to read all documents. %s / %s documents successful",
					numDocs, batchSize));
			}
		}
		final long elapsedTime = sw.getElapsedTime();
		times.add(elapsedTime);
		if (log) {
			LOGGER.info(
				String.format("thread [%s] finished reading of %s documents in %s ms", id, batchSize, elapsedTime));
		}
	}

}
