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

import java.util.ArrayList;
import java.util.List;

import com.arangodb.loadtest.cli.CliOptions;

/**
 * @author Mark Vollmary
 *
 */
public class KeyGen {

	private final int num;
	private final String keyPrefix;
	private final int run;
	private int index;

	public KeyGen(final CliOptions options, final int num, final int run) {
		super();
		this.num = num;
		this.run = run;
		keyPrefix = options.getKeyPrefix();
		index = 0;
	}

	public List<String> generateKeys(final int quantity) {
		final List<String> keys = new ArrayList<>(quantity);
		for (int i = 0; i < quantity; i++) {
			final String key;
			if (keyPrefix != null) {
				key = String.format("%s-%s-%s-%s", keyPrefix, run, num, index++);
			} else {
				key = String.format("%s-%s-%s", run, num, index++);
			}
			keys.add(key);
		}
		return keys;
	}

}
