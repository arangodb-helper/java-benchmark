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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.lang3.RandomStringUtils;

import com.arangodb.entity.BaseDocument;

/**
 * @author Mark Vollmary
 *
 */
public class DocumentCreator {

	public static class Builder {

		private final int numberOfFields;
		private final int fieldSize;
		private final int batchSize;

		public Builder(final int numberOfFields, final int fieldSize, final int batchSize) {
			super();
			this.numberOfFields = numberOfFields;
			this.fieldSize = fieldSize;
			this.batchSize = batchSize;
		}

		public DocumentCreator build() {
			return new DocumentCreator(numberOfFields, fieldSize, batchSize);
		}
	}

	private final List<BaseDocument> cache;

	private DocumentCreator(final int numberOfFields, final int fieldSize, final int batchSize) {
		super();
		cache = new ArrayList<>();
		for (int j = 0; j < batchSize; j++) {
			final BaseDocument doc = new BaseDocument();
			for (int i = 0; i < numberOfFields; i++) {
				doc.addAttribute("field" + i, RandomStringUtils.random(fieldSize, false, true));
			}
			cache.add(doc);
		}
	}

	public List<BaseDocument> create(final Collection<String> keys) {
		final Iterator<String> iterator = keys.iterator();
		cache.forEach(e -> e.setKey(iterator.next()));
		return cache;
	}

}
