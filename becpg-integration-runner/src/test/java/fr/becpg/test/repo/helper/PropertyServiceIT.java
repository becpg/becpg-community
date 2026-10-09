/*
 *
 */
package fr.becpg.test.repo.helper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.config.format.FormatMode;
import fr.becpg.config.format.PropertyFormats;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.test.PLMBaseTestCase;

// TODO: Auto-generated Javadoc
/**
 * The Class ProductDAOTest.
 *
 * @author querephi
 */
public class PropertyServiceIT extends PLMBaseTestCase {

	@Autowired
	private AttributeExtractorService attributeExtractorService;

	@Autowired
	private DictionaryService dictionaryService;

	/**
	 * Test date property
	 */
	@Test
	public void testGetDate() throws ParseException {

		PropertyDefinition propertyDef = dictionaryService.getProperty(QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, "modified"));
		SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
		Date date = dateFormat.parse("07/10/2009");

		assertNotNull(propertyDef);
		assertNotNull(date);

		PropertyFormats propertyFormats = new PropertyFormats(true);
		String stringDate = attributeExtractorService.getStringValue(propertyDef, date, propertyFormats);
		assertEquals("check date", "mer. 7 oct. 2009 00:00:00", stringDate);
	}

	/**
	 * Instances returned by forMode are shared between requests, so they must follow the
	 * locale of the current request and not keep the one of the request that created them.
	 */
	@Test
	public void testSharedFormatFollowsCurrentLocale() throws ParseException {

		Date date = new SimpleDateFormat("dd/MM/yyyy").parse("07/10/2009");

		String englishDate = formatDateIn(Locale.ENGLISH, date);
		String frenchDate = formatDateIn(Locale.FRENCH, date);

		assertEquals("check english date", "Wed 7 Oct 2009", englishDate);
		assertEquals("check french date", "mer. 7 oct. 2009", frenchDate);
	}

	private String formatDateIn(Locale locale, Date date) {

		Locale previousLocale = I18NUtil.getLocale();

		try {
			I18NUtil.setLocale(locale);
			return PropertyFormats.forMode(FormatMode.JSON, false).formatDate(date);
		} finally {
			I18NUtil.setLocale(previousLocale);
		}
	}

}
