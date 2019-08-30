/*
 *
 */
package fr.becpg.test.repo.helper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

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

}
