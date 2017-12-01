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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.MultiDocumentEntity;

/**
 * 
 * @author Mark Vollmary
 *
 */
public class DocumentReader {

	private static Logger LOGGER = LoggerFactory.getLogger(DocumentReader.class);

	private final ArangoDB arango;
	private final String collectionName;
	private final String id;
	private final ArangoDatabase db;
	private final String keyPrefix;
	private int keySuffix = 0;
	private final boolean log;
	private final ArrayList<Long> times;

	public DocumentReader(final ArangoDB.Builder builder, final String databaseName, final String collectionName,
		final int num, final boolean log, final ArrayList<Long> times, final String keyPrefix) {
		this.log = log;
		this.times = times;
		this.keyPrefix = keyPrefix;
		arango = builder.build();
		db = arango.db(databaseName);
		this.collectionName = collectionName;
		this.id = (new Integer(num)).toString();
	}

	public void read(final int batchSize) throws ArangoDBException {
		final List<String> keys = new ArrayList<>(batchSize);
		for (int i = 0; i < batchSize; i++) {
			keys.add(keyPrefix + id + "-" + keySuffix++);
		}
		final Stopwatch sw = new Stopwatch();
		if (batchSize == 1) {
			final BaseDocument doc = db.collection(collectionName).getDocument(keys.get(0), BaseDocument.class);
			if (doc == null) {
				throw new ArangoDBException(String.format("Failed to read document with key: %s", keys.get(0)));
			}
		} else {
			final MultiDocumentEntity<BaseDocument> documents = db.collection(collectionName).getDocuments(keys,
				BaseDocument.class);
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
