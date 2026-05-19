package fr.becpg.test.repo.helper;

import org.junit.Assert;
import org.junit.Test;

import fr.becpg.repo.helper.CheckSumHelper;

public class CheckSumHelperTest {

	@Test
	public void testHashChecksum() {
		Assert.assertEquals("e3069283", CheckSumHelper.hashChecksum("123456789"));
		Assert.assertNull(CheckSumHelper.hashChecksum(null));
	}

	@Test
	public void testUpdateChecksum() {
		String checksumValue = CheckSumHelper.hashChecksum("regulatory-value");
		String updatedChecksum = CheckSumHelper.updateChecksum("regulatory", null, checksumValue);

		Assert.assertEquals("{\"regulatory\":\"" + checksumValue + "\"}", updatedChecksum);
	}

	@Test
	public void testUpdateChecksumPreservesExistingEntries() {
		String checksumValue = CheckSumHelper.hashChecksum("regulatory-value");
		String updatedChecksum = CheckSumHelper.updateChecksum("regulatory", "{\"other\":\"value\"}", checksumValue);

		Assert.assertTrue(updatedChecksum.contains("\"other\":\"value\""));
		Assert.assertTrue(updatedChecksum.contains("\"regulatory\":\"" + checksumValue + "\""));
	}

	@Test
	public void testIsSameChecksum() {
		String checksumValue = CheckSumHelper.hashChecksum("regulatory-value");
		String updatedChecksum = CheckSumHelper.updateChecksum("regulatory", null, checksumValue);

		Assert.assertTrue(CheckSumHelper.isSameChecksum("regulatory", updatedChecksum, checksumValue));
		Assert.assertFalse(CheckSumHelper.isSameChecksum("regulatory", updatedChecksum, CheckSumHelper.hashChecksum("other-value")));
	}

	@Test
	public void testUpdateChecksumRemovesEntryWhenChecksumIsNull() {
		String updatedChecksum = CheckSumHelper.updateChecksum("regulatory", "{\"regulatory\":\"abc123\",\"other\":\"value\"}", null);

		Assert.assertEquals("{\"other\":\"value\"}", updatedChecksum);
		Assert.assertFalse(CheckSumHelper.isSameChecksum("regulatory", updatedChecksum, "abc123"));
	}

	@Test
	public void testHashChecksumWithVeryLargeValue() {
		String largeValue = createLargeValue();
		String checksumValue = CheckSumHelper.hashChecksum(largeValue);
		String updatedLargeValue = largeValue.substring(0, largeValue.length() - 1) + "Y";
		String updatedChecksumValue = CheckSumHelper.hashChecksum(updatedLargeValue);

		Assert.assertNotNull(checksumValue);
		Assert.assertEquals(8, checksumValue.length());
		Assert.assertNotEquals(checksumValue, updatedChecksumValue);
	}

	private String createLargeValue() {
		StringBuilder builder = new StringBuilder();
		for (int i = 0; i < 500000; i++) {
			builder.append("regulatory-country-").append(i).append("-usage-").append(i % 10).append(';');
		}
		builder.append('X');
		return builder.toString();
	}
}
