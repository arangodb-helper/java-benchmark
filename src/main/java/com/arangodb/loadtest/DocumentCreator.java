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

import org.apache.commons.lang3.RandomStringUtils;

import com.arangodb.entity.BaseDocument;

/**
 * @author Mark Vollmary
 *
 */
public class DocumentCreator {

	private final int numberOfFields;
	private final int fieldSize;

	public DocumentCreator(final int numberOfFields, final int fieldSize) {
		super();
		this.numberOfFields = numberOfFields;
		this.fieldSize = fieldSize;
	}

	public BaseDocument create(final String key) {
		final BaseDocument doc = new BaseDocument(key);
		for (int i = 0; i < numberOfFields; i++) {
			doc.addAttribute("field" + i, RandomStringUtils.random(fieldSize, false, true));
		}
		return doc;
	}

}
