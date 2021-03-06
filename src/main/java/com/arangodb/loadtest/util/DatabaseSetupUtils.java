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

package com.arangodb.loadtest.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDB.Builder;
import com.arangodb.ArangoDBException;
import com.arangodb.ArangoDatabase;
import com.arangodb.ArangoGraph;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.EdgeDefinition;
import com.arangodb.loadtest.cli.CliOptions;
import com.arangodb.loadtest.cli.Index;
import com.arangodb.loadtest.testcase.TestCase;
import com.arangodb.model.CollectionCreateOptions;
import com.arangodb.model.FulltextIndexOptions;
import com.arangodb.model.GeoIndexOptions;
import com.arangodb.model.GraphCreateOptions;
import com.arangodb.model.HashIndexOptions;
import com.arangodb.model.PersistentIndexOptions;
import com.arangodb.model.SkiplistIndexOptions;
import com.arangodb.model.arangosearch.ArangoSearchCreateOptions;
import com.arangodb.entity.arangosearch.FieldLink;
import com.arangodb.entity.arangosearch.CollectionLink;

/**
 * @author Mark Vollmary
 *
 */
public class DatabaseSetupUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseSetupUtils.class);

	private DatabaseSetupUtils() {
		super();
	}

	public static void setup(
		final Builder builder,
		final CliOptions options,
		final boolean dropDB,
		final Collection<TestCase> tests) throws ArangoDBException {
		final ArangoDB arangoDB = builder.build();
		final ArangoDatabase db = arangoDB.db(options.getDatabase());
		if (dropDB) {
			try {
				db.drop();
			} catch (final ArangoDBException e) {
			}
		}
		try {
			if (!db.exists()) {
				db.create();
			}
		} catch (final ArangoDBException e) {
			if (!db.exists()) {
				LOGGER.error(String.format("Failed to create database: %s", db.name()));
			}
		}
		final ArangoCollection collection = db.collection(options.getCollection());
		try {
			if (!collection.exists()) {
				collection.create(new CollectionCreateOptions().numberOfShards(options.getNumberOfShards())
						.replicationFactor(options.getReplicationFactor()).waitForSync(options.getWaitForSync()));
			}
		} catch (final Exception e) {
			if (!collection.exists()) {
				LOGGER.error(String.format("Failed to create collection %s", collection.name()));
			}
		}

		final Collection<TestCase> testCaseRequireGraph = Arrays.asList(TestCase.VERTEX_GET, TestCase.VERTEX_INSERT,
			TestCase.VERTEX_REPLACE, TestCase.VERTEX_UPDATE, TestCase.EDGE_GET, TestCase.EDGE_INSERT,
			TestCase.EDGE_REPLACE, TestCase.EDGE_UPDATE, TestCase.AQL_CUSTOM);
		final boolean requireGraph = tests.stream().anyMatch(test -> testCaseRequireGraph.contains(test));

		if (requireGraph) {
			final ArangoGraph graph = db.graph(options.getGraph());
			try {
				if (!graph.exists()) {
					graph.create(
						Arrays.asList(new EdgeDefinition().collection(options.getEdgeCollection())
								.from(options.getVertexCollection()).to(options.getVertexCollection())),
						new GraphCreateOptions().numberOfShards(options.getNumberOfShards())
								.replicationFactor(options.getReplicationFactor()));
				}
			} catch (final Exception e) {
				if (!graph.exists()) {
					LOGGER.error(String.format("Failed to create graph %s", graph.name()));
				}
			}
            if (db.collection(options.getVertexCollection()).exists()) {
                // create dummy vertex for edge tests
                try {
                    db.collection(options.getVertexCollection()).insertDocument(new BaseDocument("dummy"));
                } catch (final Exception e) {
                    // This is not an error if the vertex exists already.
                    LOGGER.error(String.format("Cannot insert dummy vertex."));
                }
            }
		}

    try {
      Stream<String> s = requireGraph ? Stream.of(options.getCollection(), options.getVertexCollection(), options.getEdgeCollection()) : Stream.of(options.getCollection()); 
      s.map(name -> db.collection(name))
       .forEach(colHandle -> {
         createIndex(options, colHandle, options.getDocIndexSimple(), options.getDocNumIndexSimple(),
           DocumentCreator.FIELD_SIMPLE);
         createIndex(options, colHandle, options.getDocIndexLargeSimple(),
           options.getDocNumIndexLargeSimple(), DocumentCreator.FIELD_LARGE);
         createIndex(options, colHandle, options.getDocIndexArrays(), options.getDocNumIndexArrays(),
           DocumentCreator.FIELD_ARRAY);
         createIndex(options, colHandle, options.getDocIndexObjects(), options.getDocNumIndexObjects(),
           DocumentCreator.FIELD_OBJECT);
         createView(options, db, colHandle, options.getDocView(), DocumentCreator.FIELD_LARGE);
       });
    } catch (Exception e) {
      LOGGER.error("There was an error in index creation:");
      LOGGER.error(e.toString());
    }

		arangoDB.shutdown();
	}

  private static void createView(
		final CliOptions options,
    final ArangoDatabase db,
		final ArangoCollection colHandle,
    final boolean viewForField,
		final String field) {
    if (viewForField) {
      ArangoSearchCreateOptions opts = new ArangoSearchCreateOptions();
      opts.link(CollectionLink.on(colHandle.name()).fields(FieldLink.on(field)));
      db.createArangoSearch("MySearch", opts);
    }
  }

	private static void createIndex(
		final CliOptions options,
		final ArangoCollection colHandle,
		final Collection<Index> indexForField,
		final Integer numFields,
		final String field) {
		if (indexForField != null) {
			final Iterable<String> fields = IntStream.rangeClosed(0, numFields - 1).mapToObj(i -> field + i)
					.collect(Collectors.toList());
			indexForField.forEach(index -> {
				switch (index) {
				case HASH:
					fields.forEach(f -> colHandle.ensureHashIndex(Collections.singleton(f), new HashIndexOptions()));
					break;
				case SKIPLIST:
					fields.forEach(
						f -> colHandle.ensureSkiplistIndex(Collections.singleton(f), new SkiplistIndexOptions()));
					break;
				case PERSISTENT:
					fields.forEach(
						f -> colHandle.ensurePersistentIndex(Collections.singleton(f), new PersistentIndexOptions()));
					break;
				case GEO:
					fields.forEach(f -> colHandle.ensureGeoIndex(Collections.singleton(f), new GeoIndexOptions()));
					break;
				case FULLTEXT:
					fields.forEach(
						f -> colHandle.ensureFulltextIndex(Collections.singleton(f), new FulltextIndexOptions()));
					break;
				default:
					break;
				}
			});
		}
	}

}
