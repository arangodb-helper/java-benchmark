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
public class AqlCustomTestCase extends ArangoTestCase {

	private final DocumentCreator documentCreator;
	private final AqlQueryOptions queryOptions;
	private final ArangoDatabase db;
	private final KeyGen keyGen;
	private List<BaseDocument> documents;
	private List<String> keys;

	public AqlCustomTestCase(final ArangoDB.Builder builder, final CliOptions options, final int num,
		final Collection<Long> times, final KeyGen keyGen, final DocumentCreator documentCreator) {
		super(builder, options, num, times);
		this.keyGen = keyGen;
		db = arango.db(options.getDatabase());
		queryOptions = new AqlQueryOptions().batchSize(options.getCursorBatchSize());
		this.documentCreator = documentCreator;
	}

	@Override
	protected void _prepare() {
		final Integer batchSize = options.getBatchSize();
		keys = keyGen.generateKeys(batchSize);
		documents = documentCreator.create(keys);
	}

	@Override
	protected void _run() throws ArangoDBException {
		final String query = options.getQuery();
		if (query.isEmpty()) {
			throw new IllegalArgumentException("AQL query is empty!");
		}
		final MapBuilder bindVars = new MapBuilder();
		if (query.contains("@graph")) {
			bindVars.put("graph", options.getGraph());
		}
		if (query.contains("@@collection")) {
			bindVars.put("@collection", options.getCollection());
		}
		if (query.contains("@@edge")) {
			bindVars.put("@edge", options.getEdgeCollection());
		}
		if (query.contains("@@vertex")) {
			bindVars.put("@vertex", options.getVertexCollection());
		}
		if (query.contains("@docs")) {
			bindVars.put("docs", documents);
		} else if (query.contains("@doc")) {
			bindVars.put("doc", documents.get(0));
		}
		if (query.contains("@keys")) {
			bindVars.put("keys", keys);
		} else if (query.contains("@key")) {
			bindVars.put("key", keys.get(0));
		}
		db.query(query, bindVars.get(), queryOptions, VPackSlice.class);
	}

}
