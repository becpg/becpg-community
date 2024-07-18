package fr.becpg.test.repo.helper;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import fr.becpg.repo.helper.MLTextHelper;

public class MLTextHelperTest {

	@Test
	public void testGetNearestLocale() {
		Set<Locale> locales = new HashSet<>();

		locales.add(Locale.FRENCH);
		locales.add(Locale.CANADA_FRENCH);
		Assert.assertEquals(Locale.FRENCH, MLTextHelper.getNearestLocale(Locale.FRANCE, locales));
		Assert.assertEquals(Locale.CANADA_FRENCH, MLTextHelper.getNearestLocale(Locale.CANADA_FRENCH, locales));
		Assert.assertEquals(Locale.FRENCH, MLTextHelper.getNearestLocale(Locale.FRENCH, locales));

		locales.add(Locale.FRANCE);

		Assert.assertEquals(Locale.FRANCE, MLTextHelper.getNearestLocale(Locale.FRANCE, locales));
		Assert.assertEquals(Locale.FRANCE, MLTextHelper.getNearestLocale(new Locale("fr", "FR", "BZH"), locales));

		Assert.assertNull(MLTextHelper.getNearestLocale(Locale.ENGLISH, locales));

		locales = new HashSet<>();

		locales.add(Locale.CANADA_FRENCH);
		Assert.assertEquals(Locale.CANADA_FRENCH, MLTextHelper.getNearestLocale(Locale.FRANCE, locales));
	}

	@Test
	public void testIsSupportedLocale() {
		MLTextHelper.setSupportedLocales("fr,en_US,en");

		Assert.assertTrue(MLTextHelper.isSupportedLocale(Locale.US));
		Assert.assertFalse(MLTextHelper.isSupportedLocale(Locale.UK));

		MLTextHelper.flushCache();
	}
}
