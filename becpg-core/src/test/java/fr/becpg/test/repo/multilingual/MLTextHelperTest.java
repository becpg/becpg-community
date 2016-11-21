package fr.becpg.test.repo.multilingual;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import fr.becpg.repo.helper.MLTextHelper;

public class MLTextHelperTest {

	@Test
	public void test() {
		
		Set<Locale> locales = new HashSet<>();
		
		locales.add(Locale.FRENCH);
		locales.add(Locale.CANADA_FRENCH);
		Assert.assertTrue(Locale.FRENCH.equals(MLTextHelper.getNearestLocale(Locale.FRANCE, locales)));
		Assert.assertTrue(Locale.CANADA_FRENCH.equals(MLTextHelper.getNearestLocale(Locale.CANADA_FRENCH, locales)));
		Assert.assertTrue(Locale.FRENCH.equals(MLTextHelper.getNearestLocale(Locale.FRENCH, locales)));
		
		
		locales.add(Locale.FRANCE);
	
		
		Assert.assertTrue(Locale.FRANCE.equals(MLTextHelper.getNearestLocale(Locale.FRANCE, locales)));
		Assert.assertTrue(Locale.FRANCE.equals(MLTextHelper.getNearestLocale(new Locale("fr","FR","BZH"), locales)));
		
		Assert.assertNull(MLTextHelper.getNearestLocale(Locale.ENGLISH, locales));
		
		
		
		locales = new HashSet<>();
		
		locales.add(Locale.CANADA_FRENCH);
		Assert.assertTrue(Locale.CANADA_FRENCH.equals(MLTextHelper.getNearestLocale(Locale.FRANCE, locales)));
	}

}
