package fr.becpg.repo.template.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.util.Locale;

import org.junit.Test;

/**
 * Unit tests for {@link TemplateLookupLocale}.
 *
 * @author matthieu
 */
public class TemplateLookupLocaleTest {

	private static final String TRAVERSAL = "/../../../alfresco";

	@Test
	public void testKeepsLanguageAndCountry() {
		assertEquals(Locale.CANADA_FRENCH, TemplateLookupLocale.sanitize(Locale.CANADA_FRENCH));
	}

	@Test
	public void testKeepsLanguageAlone() {
		assertEquals(Locale.FRENCH, TemplateLookupLocale.sanitize(Locale.FRENCH));
	}

	@Test
	public void testKeepsNumericAreaCode() {
		assertEquals(Locale.of("es", "419"), TemplateLookupLocale.sanitize(Locale.of("es", "419")));
	}

	@Test
	public void testDropsTraversalVariant() {
		Locale sanitized = TemplateLookupLocale.sanitize(Locale.of("fr", "FR", TRAVERSAL));

		assertEquals(Locale.FRANCE, sanitized);
		assertFalse(sanitized.toString().contains(".."));
	}

	@Test
	public void testDropsMalformedCountry() {
		assertEquals(Locale.FRENCH, TemplateLookupLocale.sanitize(Locale.of("fr", TRAVERSAL)));
	}

	@Test
	public void testFallsBackToRootOnMalformedLanguage() {
		assertEquals(Locale.ROOT, TemplateLookupLocale.sanitize(Locale.of(TRAVERSAL)));
	}

	@Test
	public void testFallsBackToRootOnNull() {
		assertEquals(Locale.ROOT, TemplateLookupLocale.sanitize(null));
	}

}
