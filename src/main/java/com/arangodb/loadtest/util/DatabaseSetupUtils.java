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

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDB.Builder;
import com.arangodb.ArangoDBException;
import com.arangodb.loadtest.cli.CliOptions;
import com.arangodb.loadtest.cli.Index;
import com.arangodb.model.CollectionCreateOptions;
import com.arangodb.model.FulltextIndexOptions;
import com.arangodb.model.GeoIndexOptions;
import com.arangodb.model.HashIndexOptions;
import com.arangodb.model.PersistentIndexOptions;
import com.arangodb.model.SkiplistIndexOptions;

/**
 * @author Mark Vollmary
 *
 */
public class DatabaseSetupUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(DatabaseSetupUtils.class);

	private DatabaseSetupUtils() {
		super();
	}

	public static void setup(final Builder builder, final CliOptions options, final boolean dropDB)
			throws ArangoDBException {
		final ArangoDB arangoDB = builder.build();
		final String database = options.getDatabase();
		if (dropDB) {
			try {
				arangoDB.db(database).drop();
			} catch (final ArangoDBException e) {
			}
		} else {
                  return;
                }
		try {
			arangoDB.createDatabase(database);
		} catch (final ArangoDBException e) {
			if (!arangoDB.db(database).exists()) {
				LOGGER.error(String.format("Failed to create database: %s", database));
			}
		}
		final String collection = options.getCollection();
		try {
			arangoDB.db(database).createCollection(collection,
				new CollectionCreateOptions().numberOfShards(options.getNumberOfShards())
						.replicationFactor(options.getReplicationFactor()).waitForSync(options.getWaitForSync()));
		} catch (final Exception e) {
			if (!arangoDB.db(database).collection(collection).exists()) {
				LOGGER.error(String.format("Failed to create collection %s", collection));
			}
		}
		final ArangoCollection colHandle = arangoDB.db(database).collection(collection);

		createIndex(options, colHandle, options.getDocIndexSimple(), options.getDocNumIndexSimple(),
			DocumentCreator.FIELD_SIMPLE);
		createIndex(options, colHandle, options.getDocIndexLargeSimple(), options.getDocNumIndexLargeSimple(),
			DocumentCreator.FIELD_LARGE);
		createIndex(options, colHandle, options.getDocIndexArrays(), options.getDocNumIndexArrays(),
			DocumentCreator.FIELD_ARRAY);
		createIndex(options, colHandle, options.getDocIndexObjects(), options.getDocNumIndexObjects(),
			DocumentCreator.FIELD_OBJECT);
		arangoDB.shutdown();
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
