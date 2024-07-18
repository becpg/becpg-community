package fr.becpg.test.repo.helper;

import java.util.Locale;

import org.alfresco.service.cmr.repository.MLText;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.helper.TranslateHelper;

public class TranslateHelperTest {

	@Test
	public void test() {
		
		I18NUtil.registerResourceBundle( "beCPG.translations.paths" );

		Assert.assertNotNull(I18NUtil.getMessage("path.icons"));
		MLText key  = TranslateHelper.getTranslatedKey(RepoConsts.PATH_ICON);
		Assert.assertEquals( 0, key.size());
		
		key  = TranslateHelper.getTranslatedPathMLText(RepoConsts.PATH_ICON);
		Assert.assertEquals( RepoConsts.SUPPORTED_UI_LOCALES.split(",").length, key.getLocales().size());
		Assert.assertEquals(key.get(Locale.ENGLISH),key.get(MLTextHelper.parseLocale("en_US")));
		Assert.assertNotEquals(key.get(Locale.FRENCH),key.get(MLTextHelper.parseLocale("sv_SE")));
		Assert.assertNotEquals(key.get(Locale.ENGLISH),key.get(MLTextHelper.parseLocale("sv_SE")));
		Assert.assertEquals("Ikoner",key.get(MLTextHelper.parseLocale("sv_SE")));
	}
	
	@Test
	public void testApostrophe() {
		I18NUtil.registerResourceBundle( "beCPG.translations.plm" );
		
		MLTextHelper.setSupportedLocales(RepoConsts.SUPPORTED_UI_LOCALES);
		
		MLText key = MLTextHelper.getI18NMessage("message.formulate.wrong.unit");
		Assert.assertEquals( RepoConsts.SUPPORTED_UI_LOCALES.split(",").length, key.getLocales().size());
		Assert.assertEquals(key.get(Locale.FRENCH),"L'unité utilisée n'est pas la bonne.");
		
		MLTextHelper.flushCache();

	}

}
