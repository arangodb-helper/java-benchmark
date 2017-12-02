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

import java.util.Collection;
import java.util.List;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.MultiDocumentEntity;
import com.arangodb.loadtest.cli.CliOptions;
import com.arangodb.loadtest.util.KeyGen;

/**
 * 
 * @author Mark Vollmary
 *
 */
public class DocumentReadTestCase extends ArangoTestCase {

	private final ArangoCollection collection;
	private final KeyGen keyGen;
	private List<String> keys;

	public DocumentReadTestCase(final ArangoDB.Builder builder, final CliOptions options, final KeyGen keyGen,
		final int num, final Collection<Long> times) {
		super(builder, options, num, times);
		this.keyGen = keyGen;
		collection = arango.db(options.getDatabase()).collection(options.getCollection());
	}

	@Override
	protected void _prepare() {
		final Integer batchSize = options.getBatchSize();
		keys = keyGen.generateKeys(batchSize);
	}

	@Override
	protected void _run() throws ArangoDBException {
		final Integer batchSize = options.getBatchSize();
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
	}

}
