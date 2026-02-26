/*
 *  Copyright (C) 2010-2026 beCPG. All rights reserved.
 */
package fr.becpg.test.utils;

import java.util.Set;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

/**
 * Unit tests for {@link JSONCompareHelper}.
 *
 * @author Valentin
 */
public class JSONCompareHelperTest {

	@Test
	public void testIdenticalObjects() {
		JSONObject expected = new JSONObject("{\"name\":\"Alice\",\"age\":30}");
		JSONObject actual = new JSONObject("{\"name\":\"Alice\",\"age\":30}");

		JSONCompareHelper comparator = new JSONCompareHelper.Builder().build();
		JSONCompareHelper.Result result = comparator.compare(expected, actual);

		Assert.assertTrue(result.getMessage(), result.passed());
	}

	@Test
	public void testValueMismatch() {
		JSONObject expected = new JSONObject("{\"name\":\"Alice\"}");
		JSONObject actual = new JSONObject("{\"name\":\"Bob\"}");

		JSONCompareHelper comparator = new JSONCompareHelper.Builder().build();
		JSONCompareHelper.Result result = comparator.compare(expected, actual);

		Assert.assertFalse(result.passed());
		Assert.assertEquals(1, result.getDifferences().size());
		Assert.assertTrue(result.getDifferences().get(0).contains("name"));
	}

	@Test
	public void testMissingKey() {
		JSONObject expected = new JSONObject("{\"name\":\"Alice\",\"age\":30}");
		JSONObject actual = new JSONObject("{\"name\":\"Alice\"}");

		JSONCompareHelper comparator = new JSONCompareHelper.Builder().build();
		JSONCompareHelper.Result result = comparator.compare(expected, actual);

		Assert.assertFalse(result.passed());
		Assert.assertTrue(result.getDifferences().get(0).contains("Missing key"));
		Assert.assertTrue(result.getDifferences().get(0).contains("age"));
	}

	@Test
	public void testNewEntriesNotAllowedByDefault() {
		JSONObject expected = new JSONObject("{\"name\":\"Alice\"}");
		JSONObject actual = new JSONObject("{\"name\":\"Alice\",\"extra\":\"value\"}");

		JSONCompareHelper comparator = new JSONCompareHelper.Builder().build();
		JSONCompareHelper.Result result = comparator.compare(expected, actual);

		Assert.assertFalse("New entries should not be allowed by default", result.passed());
	}

	@Test
	public void testNewEntriesRejectedInStrictMode() {
		JSONObject expected = new JSONObject("{\"name\":\"Alice\"}");
		JSONObject actual = new JSONObject("{\"name\":\"Alice\",\"extra\":\"value\"}");

		JSONCompareHelper comparator = new JSONCompareHelper.Builder()
				.withAllowNewEntries(false)
				.build();
		JSONCompareHelper.Result result = comparator.compare(expected, actual);

		Assert.assertFalse(result.passed());
		Assert.assertTrue(result.getDifferences().get(0).contains("Unexpected key"));
		Assert.assertTrue(result.getDifferences().get(0).contains("extra"));
	}

	@Test
	public void testIgnoredFields() {
		JSONObject expected = new JSONObject("{\"name\":\"Alice\",\"timestamp\":\"2024-01-01\"}");
		JSONObject actual = new JSONObject("{\"name\":\"Alice\",\"timestamp\":\"2025-12-31\"}");

		JSONCompareHelper comparator = new JSONCompareHelper.Builder()
				.withIgnoredField("timestamp")
				.build();
		JSONCompareHelper.Result result = comparator.compare(expected, actual);

		Assert.assertTrue("Ignored fields should not cause failures", result.passed());
	}

	@Test
	public void testIgnoredFieldsAtNestedLevel() {
		JSONObject expected = new JSONObject("{\"user\":{\"name\":\"Alice\",\"id\":\"123\"}}");
		JSONObject actual = new JSONObject("{\"user\":{\"name\":\"Alice\",\"id\":\"999\"}}");

		JSONCompareHelper comparator = new JSONCompareHelper.Builder()
				.withIgnoredField("id")
				.build();
		JSONCompareHelper.Result result = comparator.compare(expected, actual);

		Assert.assertTrue("Ignored field 'id' at nested level should not cause failures", result.passed());
	}

	@Test
	public void testIgnoredPaths() {
		JSONObject expected = new JSONObject("{\"entity\":{\"attributes\":{\"cm:contains\":\"old\",\"cm:name\":\"test\"}}}");
		JSONObject actual = new JSONObject("{\"entity\":{\"attributes\":{\"cm:contains\":\"new\",\"cm:name\":\"test\"}}}");

		JSONCompareHelper comparator = new JSONCompareHelper.Builder()
				.withIgnoredPath("entity.attributes.cm:contains")
				.build();
		JSONCompareHelper.Result result = comparator.compare(expected, actual);

		Assert.assertTrue("Ignored path should not cause failures", result.passed());
	}

	@Test
	public void testIgnoredPathDoesNotAffectOtherPaths() {
		JSONObject expected = new JSONObject("{\"entity\":{\"attributes\":{\"cm:contains\":\"old\",\"cm:name\":\"test\"}}}");
		JSONObject actual = new JSONObject("{\"entity\":{\"attributes\":{\"cm:contains\":\"new\",\"cm:name\":\"changed\"}}}");

		JSONCompareHelper comparator = new JSONCompareHelper.Builder()
				.withIgnoredPath("entity.attributes.cm:contains")
				.build();
		JSONCompareHelper.Result result = comparator.compare(expected, actual);

		Assert.assertFalse("Non-ignored path changes should still be detected", result.passed());
		Assert.assertTrue(result.getDifferences().get(0).contains("cm:name"));
	}

	@Test
	public void testDoubleEpsilonComparison() {
		JSONObject expected = new JSONObject("{\"value\":1.0000001}");
		JSONObject actual = new JSONObject("{\"value\":1.0000002}");

		JSONCompareHelper comparator = new JSONCompareHelper.Builder()
				.withEpsilon(1e-6)
				.build();
		JSONCompareHelper.Result result = comparator.compare(expected, actual);

		Assert.assertTrue("Values within epsilon should be considered equal", result.passed());
	}

	@Test
	public void testDoubleEpsilonComparisonFails() {
		JSONObject expected = new JSONObject("{\"value\":1.0}");
		JSONObject actual = new JSONObject("{\"value\":2.0}");

		JSONCompareHelper comparator = new JSONCompareHelper.Builder()
				.withEpsilon(1e-6)
				.build();
		JSONCompareHelper.Result result = comparator.compare(expected, actual);

		Assert.assertFalse("Values outside epsilon should cause failure", result.passed());
	}

	@Test
	public void testIntegerComparison() {
		JSONObject expected = new JSONObject("{\"count\":42}");
		JSONObject actual = new JSONObject("{\"count\":42}");

		JSONCompareHelper comparator = new JSONCompareHelper.Builder().build();
		JSONCompareHelper.Result result = comparator.compare(expected, actual);

		Assert.assertTrue(result.passed());
	}

	@Test
	public void testArrayReorderingNotAllowedByDefault() {
		JSONObject expected = new JSONObject("{\"items\":[1,2,3]}");
		JSONObject actual = new JSONObject("{\"items\":[3,1,2]}");

		JSONCompareHelper comparator = new JSONCompareHelper.Builder().build();
		JSONCompareHelper.Result result = comparator.compare(expected, actual);

		Assert.assertFalse("Array reordering should not be allowed by default", result.passed());
	}

	@Test
	public void testArrayReorderingRejectedWhenDisabled() {
		JSONObject expected = new JSONObject("{\"items\":[1,2,3]}");
		JSONObject actual = new JSONObject("{\"items\":[3,1,2]}");

		JSONCompareHelper comparator = new JSONCompareHelper.Builder()
				.withAllowReordering(false)
				.build();
		JSONCompareHelper.Result result = comparator.compare(expected, actual);

		Assert.assertFalse("Array reordering should fail when disabled", result.passed());
	}

	@Test
	public void testArrayOrderedComparison() {
		JSONObject expected = new JSONObject("{\"items\":[1,2,3]}");
		JSONObject actual = new JSONObject("{\"items\":[1,2,3]}");

		JSONCompareHelper comparator = new JSONCompareHelper.Builder()
				.withAllowReordering(false)
				.build();
		JSONCompareHelper.Result result = comparator.compare(expected, actual);

		Assert.assertTrue("Identically ordered arrays should pass", result.passed());
	}

	@Test
	public void testArrayWithNewEntriesAllowed() {
		JSONObject expected = new JSONObject("{\"items\":[1,2]}");
		JSONObject actual = new JSONObject("{\"items\":[1,2,3]}");

		JSONCompareHelper comparator = new JSONCompareHelper.Builder().withAllowNewEntries(true).build();
		JSONCompareHelper.Result result = comparator.compare(expected, actual);

		Assert.assertTrue("Extra array elements should be allowed by default", result.passed());
	}

	@Test
	public void testArrayWithNewEntriesRejected() {
		JSONObject expected = new JSONObject("{\"items\":[1,2]}");
		JSONObject actual = new JSONObject("{\"items\":[1,2,3]}");

		JSONCompareHelper comparator = new JSONCompareHelper.Builder()
				.withAllowNewEntries(false)
				.build();
		JSONCompareHelper.Result result = comparator.compare(expected, actual);

		Assert.assertFalse("Extra array elements should fail in strict mode", result.passed());
	}

	@Test
	public void testArrayMissingElement() {
		JSONObject expected = new JSONObject("{\"items\":[1,2,3]}");
		JSONObject actual = new JSONObject("{\"items\":[1,2]}");

		JSONCompareHelper comparator = new JSONCompareHelper.Builder().build();
		JSONCompareHelper.Result result = comparator.compare(expected, actual);

		Assert.assertFalse("Missing array elements should always fail", result.passed());
	}

	@Test
	public void testNestedObjectComparison() {
		JSONObject expected = new JSONObject("{\"user\":{\"address\":{\"city\":\"Paris\",\"zip\":\"75001\"}}}");
		JSONObject actual = new JSONObject("{\"user\":{\"address\":{\"city\":\"Paris\",\"zip\":\"75001\"}}}");

		JSONCompareHelper comparator = new JSONCompareHelper.Builder().build();
		JSONCompareHelper.Result result = comparator.compare(expected, actual);

		Assert.assertTrue(result.passed());
	}

	@Test
	public void testNestedObjectMismatch() {
		JSONObject expected = new JSONObject("{\"user\":{\"address\":{\"city\":\"Paris\"}}}");
		JSONObject actual = new JSONObject("{\"user\":{\"address\":{\"city\":\"Lyon\"}}}");

		JSONCompareHelper comparator = new JSONCompareHelper.Builder().build();
		JSONCompareHelper.Result result = comparator.compare(expected, actual);

		Assert.assertFalse(result.passed());
		Assert.assertTrue(result.getDifferences().get(0).contains("user.address.city"));
	}

	@Test
	public void testTypeMismatch() {
		JSONObject expected = new JSONObject("{\"value\":{\"nested\":true}}");
		JSONObject actual = new JSONObject("{\"value\":\"string\"}");

		JSONCompareHelper comparator = new JSONCompareHelper.Builder().build();
		JSONCompareHelper.Result result = comparator.compare(expected, actual);

		Assert.assertFalse(result.passed());
		Assert.assertTrue(result.getDifferences().get(0).contains("Type mismatch"));
	}

	@Test
	public void testNullValues() {
		JSONObject expected = new JSONObject();
		expected.put("value", JSONObject.NULL);
		JSONObject actual = new JSONObject();
		actual.put("value", JSONObject.NULL);

		JSONCompareHelper comparator = new JSONCompareHelper.Builder().build();
		JSONCompareHelper.Result result = comparator.compare(expected, actual);

		Assert.assertTrue(result.passed());
	}

	@Test
	public void testNullVsNonNull() {
		JSONObject expected = new JSONObject();
		expected.put("value", JSONObject.NULL);
		JSONObject actual = new JSONObject("{\"value\":\"hello\"}");

		JSONCompareHelper comparator = new JSONCompareHelper.Builder().build();
		JSONCompareHelper.Result result = comparator.compare(expected, actual);

		Assert.assertFalse(result.passed());
		Assert.assertTrue(result.getDifferences().get(0).contains("expected null"));
	}

	@Test
	public void testNonNullVsNull() {
		JSONObject expected = new JSONObject("{\"value\":\"hello\"}");
		JSONObject actual = new JSONObject();
		actual.put("value", JSONObject.NULL);

		JSONCompareHelper comparator = new JSONCompareHelper.Builder().build();
		JSONCompareHelper.Result result = comparator.compare(expected, actual);

		Assert.assertFalse(result.passed());
		Assert.assertTrue(result.getDifferences().get(0).contains("got null"));
	}

	@Test
	public void testBooleanComparison() {
		JSONObject expected = new JSONObject("{\"active\":true}");
		JSONObject actual = new JSONObject("{\"active\":true}");

		JSONCompareHelper comparator = new JSONCompareHelper.Builder().build();
		JSONCompareHelper.Result result = comparator.compare(expected, actual);

		Assert.assertTrue(result.passed());
	}

	@Test
	public void testBooleanMismatch() {
		JSONObject expected = new JSONObject("{\"active\":true}");
		JSONObject actual = new JSONObject("{\"active\":false}");

		JSONCompareHelper comparator = new JSONCompareHelper.Builder().build();
		JSONCompareHelper.Result result = comparator.compare(expected, actual);

		Assert.assertFalse(result.passed());
	}

	@Test
	public void testArrayOfObjectsReordered() {
		JSONObject expected = new JSONObject(
				"{\"users\":[{\"name\":\"Alice\",\"age\":30},{\"name\":\"Bob\",\"age\":25}]}");
		JSONObject actual = new JSONObject(
				"{\"users\":[{\"name\":\"Bob\",\"age\":25},{\"name\":\"Alice\",\"age\":30}]}");

		JSONCompareHelper comparator = new JSONCompareHelper.Builder().withAllowReordering(true).build();
		JSONCompareHelper.Result result = comparator.compare(expected, actual);

		Assert.assertTrue("Reordered array of objects should pass", result.passed());
	}

	@Test
	public void testArrayOfObjectsNoMatch() {
		JSONObject expected = new JSONObject(
				"{\"users\":[{\"name\":\"Alice\",\"age\":30}]}");
		JSONObject actual = new JSONObject(
				"{\"users\":[{\"name\":\"Alice\",\"age\":99}]}");

		JSONCompareHelper comparator = new JSONCompareHelper.Builder().build();
		JSONCompareHelper.Result result = comparator.compare(expected, actual);

		Assert.assertFalse(result.passed());
	}

	@Test
	public void testAssertEqualsThrows() {
		JSONObject expected = new JSONObject("{\"name\":\"Alice\"}");
		JSONObject actual = new JSONObject("{\"name\":\"Bob\"}");

		JSONCompareHelper comparator = new JSONCompareHelper.Builder().build();
		try {
			comparator.assertEquals(expected, actual);
			Assert.fail("assertEquals should have thrown AssertionError");
		} catch (AssertionError e) {
			Assert.assertTrue(e.getMessage().contains("JSON comparison failed"));
			Assert.assertTrue(e.getMessage().contains("name"));
		}
	}

	@Test
	public void testAssertEqualsPassesOnMatch() {
		JSONObject expected = new JSONObject("{\"name\":\"Alice\"}");
		JSONObject actual = new JSONObject("{\"name\":\"Alice\"}");

		JSONCompareHelper comparator = new JSONCompareHelper.Builder().build();
		comparator.assertEquals(expected, actual);
	}

	@Test
	public void testMultipleIgnoredFields() {
		JSONObject expected = new JSONObject("{\"name\":\"Alice\",\"id\":\"1\",\"modified\":\"date1\"}");
		JSONObject actual = new JSONObject("{\"name\":\"Alice\",\"id\":\"99\",\"modified\":\"date2\"}");

		JSONCompareHelper comparator = new JSONCompareHelper.Builder()
				.withIgnoredFields(Set.of("id", "modified"))
				.build();
		JSONCompareHelper.Result result = comparator.compare(expected, actual);

		Assert.assertTrue(result.passed());
	}

	@Test
	public void testMultipleIgnoredPaths() {
		JSONObject expected = new JSONObject("{\"a\":{\"x\":1,\"y\":2},\"b\":{\"x\":3}}");
		JSONObject actual = new JSONObject("{\"a\":{\"x\":99,\"y\":2},\"b\":{\"x\":88}}");

		JSONCompareHelper comparator = new JSONCompareHelper.Builder()
				.withIgnoredPaths(Set.of("a.x", "b.x"))
				.build();
		JSONCompareHelper.Result result = comparator.compare(expected, actual);

		Assert.assertTrue(result.passed());
	}

	@Test
	public void testEmptyObjects() {
		JSONObject expected = new JSONObject("{}");
		JSONObject actual = new JSONObject("{}");

		JSONCompareHelper comparator = new JSONCompareHelper.Builder().build();
		JSONCompareHelper.Result result = comparator.compare(expected, actual);

		Assert.assertTrue(result.passed());
	}

	@Test
	public void testEmptyArrays() {
		JSONObject expected = new JSONObject("{\"items\":[]}");
		JSONObject actual = new JSONObject("{\"items\":[]}");

		JSONCompareHelper comparator = new JSONCompareHelper.Builder().build();
		JSONCompareHelper.Result result = comparator.compare(expected, actual);

		Assert.assertTrue(result.passed());
	}

	@Test
	public void testCustomEpsilon() {
		JSONObject expected = new JSONObject("{\"value\":1.0}");
		JSONObject actual = new JSONObject("{\"value\":1.5}");

		JSONCompareHelper strict = new JSONCompareHelper.Builder()
				.withEpsilon(0.1)
				.build();
		Assert.assertFalse("0.5 difference exceeds 0.1 epsilon", strict.compare(expected, actual).passed());

		JSONCompareHelper lenient = new JSONCompareHelper.Builder()
				.withEpsilon(1.0)
				.build();
		Assert.assertTrue("0.5 difference within 1.0 epsilon", lenient.compare(expected, actual).passed());
	}

	@Test
	public void testArrayWithNewEntriesOrderedMode() {
		JSONObject expected = new JSONObject("{\"items\":[1,2]}");
		JSONObject actual = new JSONObject("{\"items\":[1,2,3]}");

		JSONCompareHelper comparator = new JSONCompareHelper.Builder()
				.withAllowReordering(false)
				.withAllowNewEntries(true)
				.build();
		JSONCompareHelper.Result result = comparator.compare(expected, actual);

		Assert.assertTrue("Extra trailing elements in ordered mode should be allowed", result.passed());
	}

	@Test
	public void testArrayMissingElementOrderedMode() {
		JSONObject expected = new JSONObject("{\"items\":[1,2,3]}");
		JSONObject actual = new JSONObject("{\"items\":[1,2]}");

		JSONCompareHelper comparator = new JSONCompareHelper.Builder()
				.withAllowReordering(false)
				.build();
		JSONCompareHelper.Result result = comparator.compare(expected, actual);

		Assert.assertFalse("Missing elements in ordered mode should fail", result.passed());
	}

	@Test
	public void testDeeplyNestedStructure() {
		JSONObject expected = new JSONObject(
				"{\"a\":{\"b\":{\"c\":{\"d\":{\"value\":42}}}}}");
		JSONObject actual = new JSONObject(
				"{\"a\":{\"b\":{\"c\":{\"d\":{\"value\":42}}}}}");

		JSONCompareHelper comparator = new JSONCompareHelper.Builder().build();
		Assert.assertTrue(comparator.compare(expected, actual).passed());
	}

	@Test
	public void testMixedArrayTypes() {
		JSONObject expected = new JSONObject("{\"mixed\":[1,\"hello\",true,null]}");
		JSONObject actual = new JSONObject("{\"mixed\":[1,\"hello\",true,null]}");

		JSONCompareHelper comparator = new JSONCompareHelper.Builder()
				.withAllowReordering(false)
				.build();
		JSONCompareHelper.Result result = comparator.compare(expected, actual);

		Assert.assertTrue(result.passed());
	}

	@Test
	public void testMultipleDifferencesReported() {
		JSONObject expected = new JSONObject("{\"a\":1,\"b\":2,\"c\":3}");
		JSONObject actual = new JSONObject("{\"a\":99,\"b\":88,\"c\":3}");

		JSONCompareHelper comparator = new JSONCompareHelper.Builder().build();
		JSONCompareHelper.Result result = comparator.compare(expected, actual);

		Assert.assertFalse(result.passed());
		Assert.assertEquals(2, result.getDifferences().size());
	}

	@Test
	public void testNewEntriesStrictModeWithIgnoredFields() {
		JSONObject expected = new JSONObject("{\"name\":\"Alice\"}");
		JSONObject actual = new JSONObject("{\"name\":\"Alice\",\"id\":\"123\",\"extra\":\"val\"}");

		JSONCompareHelper comparator = new JSONCompareHelper.Builder()
				.withAllowNewEntries(false)
				.withIgnoredField("id")
				.build();
		JSONCompareHelper.Result result = comparator.compare(expected, actual);

		Assert.assertFalse("Non-ignored extra field 'extra' should fail in strict mode", result.passed());
		Assert.assertEquals(1, result.getDifferences().size());
		Assert.assertTrue(result.getDifferences().get(0).contains("extra"));
	}
}
