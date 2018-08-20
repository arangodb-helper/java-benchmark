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

import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.ArangoDatabase;
import com.arangodb.entity.BaseDocument;
import com.arangodb.loadtest.cli.CliOptions;
import com.arangodb.loadtest.util.KeyGen;
import com.arangodb.model.AqlQueryOptions;
import com.arangodb.util.MapBuilder;

/**
 * 
 * @author Mark Vollmary
 *
 */
public class AqlReadTestCase extends ArangoTestCase {

	private final AqlQueryOptions queryOptions;
	private final ArangoDatabase db;
	private final String collection;
	private final KeyGen keyGen;
	private List<String> keys;

	public AqlReadTestCase(final ArangoDB.Builder builder, final CliOptions options, final int num,
		final Collection<Long> times, final KeyGen keyGen) {
		super(builder, options, num, times);
		this.keyGen = keyGen;
		db = arango.db(options.getDatabase());
		collection = options.getCollection();
		queryOptions = new AqlQueryOptions().batchSize(options.getCursorBatchSize()).stream(options.getCursorStream());
	}

	@Override
	protected void _prepare() {
		final Integer batchSize = options.getBatchSize();
		keys = keyGen.generateKeys(batchSize);
	}

	@Override
	protected void _run() throws ArangoDBException {
		final Integer batchSize = options.getBatchSize();

		final String query;
		final MapBuilder bindVars = new MapBuilder();
		bindVars.put("@collection", collection);
		if (batchSize == 1) {
			query = "FOR i IN @@collection FILTER i._key == @key LIMIT 1 RETURN i";
			bindVars.put("key", keys.get(0));
		} else {
			query = "FOR i IN @@collection FILTER i._key IN @keys RETURN i";
			bindVars.put("keys", keys);
		}
		final List<BaseDocument> result = db.query(query, bindVars.get(), queryOptions, BaseDocument.class)
				.asListRemaining();
		final int numDocs = result.size();
		if (batchSize == 1 && numDocs < 1) {
			throw new ArangoDBException(String.format("Failed to read document with key: %s", keys.get(0)));
		} else if (numDocs != batchSize) {
			throw new ArangoDBException(
					String.format("Failed to read all documents. %s / %s documents successful", numDocs, batchSize));
		}
	}

}
