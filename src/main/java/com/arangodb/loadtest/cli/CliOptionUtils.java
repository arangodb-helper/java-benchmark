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

package com.arangodb.loadtest.cli;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 * @author Mark Vollmary
 *
 */
public class CliOptionUtils {

	private CliOptionUtils() {
		super();
	}

	public static Options createOptions() {
		final Options options = new Options();
		Stream.of(CliOptions.class.getDeclaredFields()).map(field -> {
			final Option option;
			final CliOptionInfo annotation = field.getAnnotation(CliOptionInfo.class);
			if (annotation != null) {
				option = new Option(annotation.opt().isEmpty() ? null : annotation.opt(), field.getName(), true,
						annotation.description());
				option.setRequired(annotation.required());
				option.setArgName(field.getType().getSimpleName());
				if (field.getType().isEnum()) {
					option.setDescription(String.format("%s. possible values: \"%s\"", option.getDescription(),
						enumOptions((Enum<?>[]) field.getType().getEnumConstants())));
				}
				if (Collection.class.isAssignableFrom(field.getType()) && annotation.componentType().isEnum()) {
					option.setDescription(String.format("%s. possible values: \"%s\"", option.getDescription(),
						enumOptions((Enum<?>[]) annotation.componentType().getEnumConstants())));
				}
				if (!annotation.defaultValue().isEmpty()) {
					option.setDescription(
						String.format("%s (default: %s)", option.getDescription(), annotation.defaultValue()));
				}
			} else {
				option = new Option(field.getName(), "");
			}
			return option;
		}).forEach(option -> options.addOption(option));
		return options;
	}

	private static String enumOptions(final Enum<?>[] values) {
		return Arrays.asList(values).stream().map(e -> e.name().toLowerCase()).reduce((a, b) -> a + "\", \"" + b).get();
	}

	public static CliOptions readOptions(final CommandLine cmd) {
		final CliOptions options = new CliOptions();
		Stream.of(CliOptions.class.getDeclaredFields()).forEach(field -> {
			try {
				final CliOptionInfo annotation = field.getAnnotation(CliOptionInfo.class);
				final String defaultValue = annotation != null ? annotation.defaultValue() : null;
				final String value = cmd.getOptionValue(field.getName(), defaultValue);
				field.setAccessible(true);
				if (!value.toString().isEmpty()) {
					field.set(options, valueOf(value, field.getType(), annotation));
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				e.printStackTrace();
			}
		});
		return options;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static <T> T valueOf(final Object value, final Class<T> type, final CliOptionInfo annotation) {
		final Object t;
		if (type == Integer.class) {
			t = Integer.valueOf(value.toString());
		} else if (type == Double.class) {
			t = Double.valueOf(value.toString());
		} else if (type == Boolean.class) {
			t = Boolean.valueOf(value.toString());
		} else if (type.isEnum()) {
			t = Enum.valueOf((Class<? extends Enum>) type, value.toString().toUpperCase());
		} else if (Collection.class.isAssignableFrom(type)) {
			t = Stream.of(value.toString().split(",")).map(e -> valueOf(e, annotation.componentType(), annotation))
					.collect(Collectors.toList());
		} else {
			t = type.cast(value);
		}
		return (T) t;
	}

}
