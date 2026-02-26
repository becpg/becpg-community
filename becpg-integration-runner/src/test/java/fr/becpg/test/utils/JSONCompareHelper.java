/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.test.utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * A JSON comparison utility that compares two {@link JSONObject} instances
 * and reports differences.
 *
 * <p>By default, array reordering is allowed and new entries in the actual JSON
 * are accepted (lenient mode). Both behaviors can be configured via the {@link Builder}.</p>
 *
 * <p>Usage example:</p>
 * <pre>
 * JSONCompareHelper comparator = new JSONCompareHelper.Builder()
 *     .withIgnoredPath("entity.attributes.cm:contains")
 *     .withEpsilon(1e-6)
 *     .build();
 * comparator.assertEquals(expectedJson, actualJson);
 * </pre>
 *
 * @author valentin
 */
public class JSONCompareHelper {

	private final Set<String> ignoredFields;
	private final Set<String> ignoredPaths;
	private final boolean allowNewEntries;
	private final boolean allowReordering;
	private final double epsilon;

	private JSONCompareHelper(Builder builder) {
		this.ignoredFields = Set.copyOf(builder.ignoredFields);
		this.ignoredPaths = Set.copyOf(builder.ignoredPaths);
		this.allowNewEntries = builder.allowNewEntries;
		this.allowReordering = builder.allowReordering;
		this.epsilon = builder.epsilon;
	}

	/**
	 * Result of a JSON comparison containing all detected differences.
	 */
	public static class Result {

		private final List<String> differences = new ArrayList<>();

		void addDifference(String message) {
			differences.add(message);
		}

		/**
		 * Returns {@code true} if no differences were found.
		 *
		 * @return {@code true} when comparison passed
		 */
		public boolean passed() {
			return differences.isEmpty();
		}

		/**
		 * Returns an unmodifiable list of difference descriptions.
		 *
		 * @return the list of differences
		 */
		public List<String> getDifferences() {
			return List.copyOf(differences);
		}

		/**
		 * Returns all differences joined as a single newline-separated string.
		 *
		 * @return a human-readable summary of differences
		 */
		public String getMessage() {
			return String.join("\n", differences);
		}
	}

	/**
	 * Compares two {@link JSONObject} instances and returns a {@link Result}
	 * containing all detected differences.
	 *
	 * @param expected the expected JSON object
	 * @param actual   the actual JSON object
	 * @return a {@link Result} with all differences
	 */
	public Result compare(JSONObject expected, JSONObject actual) {
		Result result = new Result();
		compareObjects("", expected, actual, result);
		return result;
	}

	/**
	 * Asserts that two {@link JSONObject} instances are equal according
	 * to this comparator's rules. Throws {@link AssertionError} on failure.
	 *
	 * @param expected the expected JSON object
	 * @param actual   the actual JSON object
	 * @throws AssertionError if the objects differ
	 */
	public void assertEquals(JSONObject expected, JSONObject actual) {
		Result result = compare(expected, actual);
		if (!result.passed()) {
			throw new AssertionError("JSON comparison failed with " + result.getDifferences().size()
					+ " difference(s):\n" + result.getMessage());
		}
	}

	private void compareObjects(String path, JSONObject expected, JSONObject actual, Result result) {
		for (String key : expected.keySet()) {
			if (ignoredFields.contains(key)) {
				continue;
			}
			String currentPath = path.isEmpty() ? key : path + "." + key;
			if (ignoredPaths.contains(currentPath)) {
				continue;
			}
			if (!actual.has(key)) {
				result.addDifference("Missing key at '" + currentPath + "': expected to be present");
				continue;
			}
			compareValues(currentPath, expected.get(key), actual.get(key), result);
		}
		if (!allowNewEntries) {
			for (String key : actual.keySet()) {
				if (ignoredFields.contains(key)) {
					continue;
				}
				String currentPath = path.isEmpty() ? key : path + "." + key;
				if (ignoredPaths.contains(currentPath)) {
					continue;
				}
				if (!expected.has(key)) {
					result.addDifference("Unexpected key at '" + currentPath + "': not expected");
				}
			}
		}
	}

	private void compareValues(String path, Object expected, Object actual, Result result) {
		if (expected instanceof JSONObject expectedObj) {
			if (actual instanceof JSONObject actualObj) {
				compareObjects(path, expectedObj, actualObj, result);
			} else {
				result.addDifference("Type mismatch at '" + path
						+ "': expected JSONObject but got " + actual.getClass().getSimpleName());
			}
		} else if (expected instanceof JSONArray expectedArr) {
			if (actual instanceof JSONArray actualArr) {
				compareArrays(path, expectedArr, actualArr, result);
			} else {
				result.addDifference("Type mismatch at '" + path
						+ "': expected JSONArray but got " + actual.getClass().getSimpleName());
			}
		} else if (expected instanceof Number && actual instanceof Number) {
			double expectedDouble = ((Number) expected).doubleValue();
			double actualDouble = ((Number) actual).doubleValue();
			if (Math.abs(expectedDouble - actualDouble) > epsilon) {
				result.addDifference("Value mismatch at '" + path
						+ "': expected <" + expected + "> but got <" + actual + ">");
			}
		} else if (JSONObject.NULL.equals(expected)) {
			if (!JSONObject.NULL.equals(actual)) {
				result.addDifference("Value mismatch at '" + path
						+ "': expected null but got <" + actual + ">");
			}
		} else if (JSONObject.NULL.equals(actual)) {
			result.addDifference("Value mismatch at '" + path
					+ "': expected <" + expected + "> but got null");
		} else {
			if (!expected.toString().equals(actual.toString())) {
				result.addDifference("Value mismatch at '" + path
						+ "': expected <" + expected + "> but got <" + actual + ">");
			}
		}
	}

	private void compareArrays(String path, JSONArray expected, JSONArray actual, Result result) {
		if (allowReordering) {
			compareArraysUnordered(path, expected, actual, result);
		} else {
			compareArraysOrdered(path, expected, actual, result);
		}
	}

	private void compareArraysOrdered(String path, JSONArray expected, JSONArray actual, Result result) {
		int maxLen = Math.max(expected.length(), actual.length());
		for (int i = 0; i < maxLen; i++) {
			String elementPath = path + "[" + i + "]";
			if (i >= expected.length()) {
				if (!allowNewEntries) {
					result.addDifference("Unexpected array element at '" + elementPath + "'");
				}
			} else if (i >= actual.length()) {
				result.addDifference("Missing array element at '" + elementPath + "'");
			} else {
				compareValues(elementPath, expected.get(i), actual.get(i), result);
			}
		}
	}

	private void compareArraysUnordered(String path, JSONArray expected, JSONArray actual, Result result) {
		Set<Integer> matchedIndices = new HashSet<>();
		for (int i = 0; i < expected.length(); i++) {
			Object expectedElement = expected.get(i);
			boolean found = false;
			for (int j = 0; j < actual.length(); j++) {
				if (matchedIndices.contains(j)) {
					continue;
				}
				Result tempResult = new Result();
				compareValues("", expectedElement, actual.get(j), tempResult);
				if (tempResult.passed()) {
					matchedIndices.add(j);
					found = true;
					break;
				}
			}
			if (!found) {
				result.addDifference("No matching element found at '" + path + "[" + i + "]' for expected: "
						+ truncate(expectedElement.toString(), 120));
			}
		}
		if (!allowNewEntries) {
			for (int j = 0; j < actual.length(); j++) {
				if (!matchedIndices.contains(j)) {
					result.addDifference("Unexpected array element at '" + path + "[" + j + "]': "
							+ truncate(actual.get(j).toString(), 120));
				}
			}
		}
	}

	private static String truncate(String value, int maxLength) {
		if (value.length() <= maxLength) {
			return value;
		}
		return value.substring(0, maxLength) + "...";
	}

	/**
	 * Builder for configuring {@link JSONCompareHelper} instances.
	 */
	public static class Builder {

		private final Set<String> ignoredFields = new HashSet<>();
		private final Set<String> ignoredPaths = new HashSet<>();
		private boolean allowNewEntries = false;
		private boolean allowReordering = false;
		private double epsilon = 1e-6;

		/**
		 * Adds field names to ignore during comparison (matched at any level).
		 *
		 * @param fields the field names to ignore
		 * @return this builder
		 */
		public Builder withIgnoredFields(Set<String> fields) {
			this.ignoredFields.addAll(fields);
			return this;
		}

		/**
		 * Adds a single field name to ignore during comparison.
		 *
		 * @param field the field name to ignore
		 * @return this builder
		 */
		public Builder withIgnoredField(String field) {
			this.ignoredFields.add(field);
			return this;
		}

		/**
		 * Adds full dot-notation paths to ignore during comparison.
		 *
		 * @param paths the paths to ignore
		 * @return this builder
		 */
		public Builder withIgnoredPaths(Set<String> paths) {
			this.ignoredPaths.addAll(paths);
			return this;
		}

		/**
		 * Adds a single dot-notation path to ignore during comparison.
		 *
		 * @param path the path to ignore
		 * @return this builder
		 */
		public Builder withIgnoredPath(String path) {
			this.ignoredPaths.add(path);
			return this;
		}

		/**
		 * Sets whether new (unexpected) entries in the actual JSON are allowed.
		 * Defaults to {@code true}.
		 *
		 * @param allow {@code true} to allow new entries, {@code false} for strict mode
		 * @return this builder
		 */
		public Builder withAllowNewEntries(boolean allow) {
			this.allowNewEntries = allow;
			return this;
		}

		/**
		 * Sets whether array element reordering is allowed.
		 * Defaults to {@code true}.
		 *
		 * @param allow {@code true} to allow reordering
		 * @return this builder
		 */
		public Builder withAllowReordering(boolean allow) {
			this.allowReordering = allow;
			return this;
		}

		/**
		 * Sets the epsilon tolerance for floating-point comparisons.
		 * Defaults to {@code 1e-6}.
		 *
		 * @param epsilon the tolerance value
		 * @return this builder
		 */
		public Builder withEpsilon(double epsilon) {
			this.epsilon = epsilon;
			return this;
		}

		/**
		 * Builds a new {@link JSONCompareHelper} with the configured settings.
		 *
		 * @return a new comparator instance
		 */
		public JSONCompareHelper build() {
			return new JSONCompareHelper(this);
		}
	}
}
