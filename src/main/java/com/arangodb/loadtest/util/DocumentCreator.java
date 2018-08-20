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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.RandomStringUtils;

import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.BaseEdgeDocument;
import com.arangodb.loadtest.cli.CliOptions;

/**
 * @author Mark Vollmary
 *
 */
public class DocumentCreator {

	public static final String FIELD_SIMPLE = "simple";
	public static final String FIELD_LARGE = "large";
	public static final String FIELD_ARRAY = "array";
	public static final String FIELD_OBJECT = "object";

	private final List<BaseDocument> cache;
	private final List<BaseEdgeDocument> edgeCache;
	private final CliOptions options;

	public DocumentCreator(final CliOptions options) {
		super();
		this.options = options;
		cache = Stream.generate(() -> createObject(0)).limit(options.getBatchSize()).map(obj -> new BaseDocument(obj))
				.collect(Collectors.toList());
		edgeCache = Stream.generate(() -> createEdge()).limit(options.getBatchSize())
				.map(obj -> new BaseEdgeDocument(obj)).collect(Collectors.toList());
	}

	private void createSimple(final Map<String, Object> doc) {
		final AtomicInteger i = new AtomicInteger(0);
		Stream.generate(() -> createString(options.getDocSimpleSize())).limit(options.getDocNumSimple())
				.forEach(s -> doc.put(FIELD_SIMPLE + i.getAndIncrement(), s));
	}

	private void createLargeSimple(final Map<String, Object> doc) {
		final AtomicInteger i = new AtomicInteger(0);
		Stream.generate(() -> createString(options.getDocLargeSimpleSize())).limit(options.getDocNumLargeSimple())
				.forEach(s -> doc.put(FIELD_LARGE + i.getAndIncrement(), s));
	}

	private void createArrays(final Map<String, Object> doc) {
		final AtomicInteger i = new AtomicInteger(0);
		Stream.generate(() -> createArray(options.getDocArraysSize())).limit(options.getDocNumArrays())
				.forEach(a -> doc.put(FIELD_ARRAY + i.getAndIncrement(), a));
	}

	private void createObjects(final Map<String, Object> doc, final int depth) {
		final AtomicInteger i = new AtomicInteger(0);
		Stream.generate(() -> createObject(depth)).limit(options.getDocNumObjects())
				.forEach(o -> doc.put(FIELD_OBJECT + i.getAndIncrement(), o));
	}

	private Map<String, Object> createObject(final int depth) {
		final Map<String, Object> doc = new LinkedHashMap<>();
		createSimple(doc);
		createLargeSimple(doc);
		createArrays(doc);
		if (depth < options.getDocNestingDepth()) {
			createObjects(doc, depth + 1);
		}
		return doc;
	}

	private Map<String, Object> createEdge() {
		final Map<String, Object> edge = createObject(0);
		edge.put("_from", options.getVertexCollection() + "/dummy");
		edge.put("_to", options.getVertexCollection() + "/dummy");
		return edge;
	}

	private static String createString(final int size) {
		return RandomStringUtils.random(size, false, true);
	}

	private static Collection<String> createArray(final int size) {
		return Stream.generate(() -> createString(5)).limit(size).collect(Collectors.toList());
	}

	public List<BaseDocument> create(final Collection<String> keys) {
		final Iterator<String> iterator = keys.iterator();
		cache.forEach(e -> e.setKey(iterator.next()));
		return cache;
	}

	public List<BaseEdgeDocument> createEdge(final Collection<String> keys) {
		final Iterator<String> iterator = keys.iterator();
		edgeCache.forEach(e -> e.setKey(iterator.next()));
		return edgeCache;
	}

}
