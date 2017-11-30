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
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;

/**
 * 
 * @author Mark Vollmary
 *
 */
public class DocumentWriter {

	private static final Logger LOGGER = LoggerFactory.getLogger(DocumentWriter.class);

	private final ArangoDB arango;
	private final String collectionName;
	private final String id;
	private final ArangoDatabase db;
	private final DocumentCreator documentCreator;
	private int keySuffix = 0;

	public DocumentWriter(final ArangoDB.Builder builder, final String databaseName, final String collectionName,
		final DocumentCreator documentCreator, final int num) {
		arango = builder.build();
		db = arango.db(databaseName);
		this.collectionName = collectionName;
		this.documentCreator = documentCreator;
		this.id = (new Integer(num)).toString();
	}

	public void write(final int batchSize) throws ArangoDBException {
		final List<BaseDocument> documents = new ArrayList<>(batchSize);
		for (int i = 0; i < batchSize; i++) {
			final String key = id + "-" + keySuffix++;
			documents.add(documentCreator.create(key));
		}
		Collections.shuffle(documents, new Random(System.nanoTime()));

		final Stopwatch sw = new Stopwatch();
		if (batchSize == 1) {
			db.collection(collectionName).insertDocument(documents.get(0));
		} else {
			db.collection(collectionName).insertDocuments(documents);
		}
		LOGGER.info(String.format("thread [%s] finished writing of %s documents in %s ms", id, documents.size(),
			sw.getElapsedTime()));
	}

}