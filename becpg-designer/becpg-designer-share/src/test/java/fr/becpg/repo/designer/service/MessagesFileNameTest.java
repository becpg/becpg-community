package fr.becpg.repo.designer.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Unit tests for {@link MessagesFileName}.
 *
 * @author matthieu
 */
public class MessagesFileNameTest {

	private static final String BASE_NAME = "pasquier-custom";

	@Test
	public void testRootBundleHasNoLocale() {
		MessagesFileName messagesFileName = MessagesFileName.parse(BASE_NAME + ".properties");

		assertEquals(BASE_NAME, messagesFileName.baseName());
		assertEquals("", messagesFileName.locale());
		assertFalse(messagesFileName.hasLocale());
	}

	@Test
	public void testLanguageLocale() {
		MessagesFileName messagesFileName = MessagesFileName.parse(BASE_NAME + "_fr.properties");

		assertEquals(BASE_NAME, messagesFileName.baseName());
		assertEquals("fr", messagesFileName.locale());
	}

	@Test
	public void testCountryVariantIsKeptWhole() {
		MessagesFileName messagesFileName = MessagesFileName.parse(BASE_NAME + "_en_US.properties");

		assertEquals(BASE_NAME, messagesFileName.baseName());
		assertEquals("en_US", messagesFileName.locale());
	}

	@Test
	public void testRootBundleAppliesToAllUiLocales() {
		assertTrue(MessagesFileName.parse(BASE_NAME + ".properties").appliesToAllUiLocales("FR"));
	}

	@Test
	public void testDefaultCountryBundleAppliesToAllUiLocales() {
		assertTrue(MessagesFileName.parse(BASE_NAME + "_FR.properties").appliesToAllUiLocales("FR"));
		assertTrue(MessagesFileName.parse(BASE_NAME + "_fr.properties").appliesToAllUiLocales("FR"));
	}

	@Test
	public void testCountryVariantDoesNotApplyToAllUiLocales() {
		assertFalse(MessagesFileName.parse(BASE_NAME + "_en_US.properties").appliesToAllUiLocales("US"));
	}

	@Test
	public void testRootFileName() {
		assertEquals(BASE_NAME + ".properties", MessagesFileName.parse(BASE_NAME + "_en_US.properties").rootFileName());
	}
}
