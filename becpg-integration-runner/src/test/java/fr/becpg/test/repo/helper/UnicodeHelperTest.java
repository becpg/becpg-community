package fr.becpg.test.repo.helper;

import org.junit.Assert;
import org.junit.Test;

import fr.becpg.repo.helper.UnicodeHelper;

/**
 * Unit tests for {@link UnicodeHelper}.
 */
public class UnicodeHelperTest {

	@Test
	public void testSanitizeBmpNullAndEmpty() {
		Assert.assertNull(UnicodeHelper.sanitizeBmp(null));
		Assert.assertEquals("", UnicodeHelper.sanitizeBmp(""));
	}

	@Test
	public void testSanitizeBmpStandardText() {
		String standardText = "Commentaire standard avec accents : é, è, à, ç, €, £, 中文";
		Assert.assertSame(standardText, UnicodeHelper.sanitizeBmp(standardText));
		Assert.assertFalse(UnicodeHelper.containsNonBmp(standardText));
	}

	@Test
	public void testSanitizeBmpWithEmojis() {
		String input = "Poudre de licorne 🦄 - Vaadata 👍";
		String expected = "Poudre de licorne  - Vaadata ";

		Assert.assertTrue(UnicodeHelper.containsNonBmp(input));
		Assert.assertEquals(expected, UnicodeHelper.sanitizeBmp(input));
	}

	@Test
	public void testSanitizeBmpWithUnpairedHighSurrogate() {
		String input = "Poudre de licorne \uD83Dsans paire";

		Assert.assertTrue(UnicodeHelper.containsNonBmp(input));
		Assert.assertEquals("Poudre de licorne sans paire", UnicodeHelper.sanitizeBmp(input));
	}

	@Test
	public void testSanitizeBmpWithUnpairedLowSurrogate() {
		String input = "Vaadata \uDE00 sans paire";

		Assert.assertTrue(UnicodeHelper.containsNonBmp(input));
		Assert.assertEquals("Vaadata  sans paire", UnicodeHelper.sanitizeBmp(input));
	}
}
