package fr.becpg.test.repo.helper;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.alfresco.service.cmr.repository.MLText;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.extensions.surf.util.I18NUtil;

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
		MLTextHelper.setSupportedLocales(
				"ar, ar_DZ, ar_EG, ar_MA, ar_SA, ar_JO, bg, bn_BD, cs, en, en_US, en_PH, en_AU, en_ZA, da_DK, de, de_AT, el, el_CY, es, fi, fr, fr_CA, hi_IN, hr_HR, hu, it, iw_IL, lt, lv, ja_JP, ko_KR, ms_MY, nl, no, ro, ru, pl, pt, pt_BR, sk, sl_SI, sr_RS, sv_SE, th, tr, ur_PK, vi_VN, zh_CN, mt_MT, et_EE, in_ID, ne_NP)");

	
		Locale.setDefault(MLTextHelper.parseLocale("en_GB"));
		I18NUtil.setLocale(Locale.getDefault());
		I18NUtil.setContentLocale(null);

		

		Assert.assertTrue(MLTextHelper.isSupportedLocale(Locale.US));
		Assert.assertFalse(MLTextHelper.isSupportedLocale(MLTextHelper.parseLocale("en_GB")));


		MLText mlTextILL = new MLText();
		mlTextILL.addValue(MLTextHelper.parseLocale("ar_EG"), "يحفظ في العبوة الأصلية غير المفتوحة المخزنة جيدًا في مكان بارد وجاف.");
		mlTextILL.addValue(MLTextHelper.parseLocale("en"), "keep in original unopened packaging stored well closed in a cool dry place.");
		mlTextILL.addValue(MLTextHelper.parseLocale("en_US"), "keep in original u./ru	nopened packaging stored well closed in a cool dry place.");
		
		
		
		Assert.assertEquals(MLTextHelper.getClosestValue(mlTextILL,I18NUtil.getContentLocale()), "keep in original unopened packaging stored well closed in a cool dry place.");
		
		MLTextHelper.replaceTextForLanguage(I18NUtil.getContentLocale(), MLTextHelper.getClosestValue(mlTextILL,I18NUtil.getContentLocale()), mlTextILL);

		Assert.assertEquals(MLTextHelper.getClosestValue(mlTextILL, MLTextHelper.parseLocale("en")), "keep in original unopened packaging stored well closed in a cool dry place.");
		Assert.assertEquals(MLTextHelper.getClosestValue(mlTextILL, MLTextHelper.parseLocale("ii")), "keep in original unopened packaging stored well closed in a cool dry place.");
		Assert.assertEquals(MLTextHelper.getClosestValue(mlTextILL, MLTextHelper.parseLocale("")), "keep in original unopened packaging stored well closed in a cool dry place.");
		Assert.assertEquals(MLTextHelper.getClosestValue(mlTextILL, MLTextHelper.parseLocale("ar_EG")), "يحفظ في العبوة الأصلية غير المفتوحة المخزنة جيدًا في مكان بارد وجاف.");
		//Assert.assertEquals(MLTextHelper.getClosestValue(mlTextILL, MLTextHelper.parseLocale("en_GB")),"en GB");
		
		MLTextHelper.flushCache();
	}

	@Test
	public void testDecimalFormat() {
		System.out.printf(Locale.UK, "%.2f", 10.5d);
		System.out.printf(MLTextHelper.parseLocale("en_ZA"), "%.2f", 10.5d);
	}
	
}
