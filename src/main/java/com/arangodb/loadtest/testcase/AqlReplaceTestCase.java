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
import com.arangodb.loadtest.util.DocumentCreator;
import com.arangodb.loadtest.util.KeyGen;
import com.arangodb.model.AqlQueryOptions;
import com.arangodb.util.MapBuilder;
import com.arangodb.velocypack.VPackSlice;

/**
 * 
 * @author Mark Vollmary
 *
 */
public class AqlReplaceTestCase extends ArangoTestCase {

	private final DocumentCreator documentCreator;
	private final AqlQueryOptions queryOptions;
	private final ArangoDatabase db;
	private final String collection;
	private final KeyGen keyGen;
	private List<BaseDocument> documents;

	public AqlReplaceTestCase(final ArangoDB.Builder builder, final CliOptions options, final int num,
		final Collection<Long> times, final KeyGen keyGen, final DocumentCreator documentCreator) {
		super(builder, options, num, times);
		this.keyGen = keyGen;
		db = arango.db(options.getDatabase());
		collection = options.getCollection();
		queryOptions = new AqlQueryOptions().batchSize(options.getCursorBatchSize());
		this.documentCreator = documentCreator;
	}

	@Override
	protected void _prepare() {
		final Integer batchSize = options.getBatchSize();
		documents = documentCreator.create(keyGen.generateKeys(batchSize));
	}

	@Override
	protected void _run() throws ArangoDBException {
		final Integer batchSize = options.getBatchSize();

		final String query;
		final MapBuilder bindVars = new MapBuilder();
		bindVars.put("@collection", collection);
		if (batchSize == 1) {
			query = "REPLACE @doc IN @@collection";
			bindVars.put("doc", documents.get(0));
		} else {
			query = "FOR i IN @docs REPLACE i IN @@collection";
			bindVars.put("docs", documents);
		}
		db.query(query, bindVars.get(), queryOptions, VPackSlice.class);
	}

}
