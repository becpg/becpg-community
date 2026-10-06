package fr.becpg.test.repo.importer;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.MLText;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.config.mapping.AbstractAttributeMapping;
import fr.becpg.config.mapping.AttributeMapping;
import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.importer.ImportContext;
import fr.becpg.repo.importer.impl.ImportHelper;

public class ImportHelperTest {

	private static final String CHARACT_NAME = "bcpg:charactName";

	private static final String CAS_NUMBER = "bcpg:casNumber";

	@Test
	public void testParseNumber() throws ParseException  {
		Locale.setDefault(Locale.FRENCH);

		Assert.assertEquals(1195l, ImportHelper.parseNumber(new ImportContext(), "1 195"));

		Assert.assertEquals(1.2d, ImportHelper.parseNumber(new ImportContext(), "1,2"));

		String input = "1. Allergen management has separate ";
		DecimalFormat decimalFormat = new DecimalFormat();
		try {
			decimalFormat.parse(input);

		} catch (ParseException e) {
			Assert.fail();
		}

		try {
			ImportHelper.parseNumber(new ImportContext(),input);
			Assert.fail();
		} catch (ParseException e) {

		}
	}

	/**
	 * Checks which columns are read as the value of a multilingual property: the property column or one of its
	 * translations, followed only by the other translations of the same property (#36417).
	 *
	 * @throws ParseException if a value cannot be parsed
	 */
	@Test
	public void testLoadMLTextValue() throws ParseException {
		try {
			MLTextHelper.flushCache();
			MLTextHelper.setSupportedLocales("fr, en, en_US, de");
			I18NUtil.setContentLocale(Locale.FRENCH);

			MLText mlText = loadMLText(List.of("bcpg:regulatoryCode", CHARACT_NAME + "_en_US", CHARACT_NAME + "_de", CAS_NUMBER),
					List.of("K", "Apricot", "Aprikose", "7732-18-5"), 1);
			Assert.assertEquals("A column following the translations is not read as the default value", "", mlText.get(Locale.FRENCH));
			Assert.assertEquals("Apricot", mlText.get(Locale.US));
			Assert.assertEquals("Aprikose", mlText.get(Locale.GERMAN));

			mlText = loadMLText(List.of("bcpg:regulatoryCode", CHARACT_NAME + "_fr"), List.of("K", "Abricot"), 1);
			Assert.assertEquals("A translation in the content locale is kept as the default value", "Abricot", mlText.get(Locale.FRENCH));

			mlText = loadMLText(List.of(CHARACT_NAME + "_fr", CAS_NUMBER), List.of("Abricot", "7732-18-5"), 0);
			Assert.assertEquals("A translation in the content locale is not replaced by the following column", "Abricot",
					mlText.get(Locale.FRENCH));

			mlText = loadMLText(List.of(CHARACT_NAME, CHARACT_NAME + "_en_US", CAS_NUMBER), List.of("Abricot", "Apricot", "7732-18-5"), 0);
			Assert.assertEquals("The property column is read as the default value", "Abricot", mlText.get(Locale.FRENCH));
			Assert.assertEquals("Apricot", mlText.get(Locale.US));
			Assert.assertEquals("The following column is not a translation", 2, mlText.size());
		} finally {
			I18NUtil.setContentLocale(null);
			MLTextHelper.flushCache();
		}
	}

	private MLText loadMLText(List<String> columnIds, List<String> values, int pos) throws ParseException {
		DataTypeDefinition dataType = Mockito.mock(DataTypeDefinition.class);
		Mockito.when(dataType.getName()).thenReturn(DataTypeDefinition.MLTEXT);
		PropertyDefinition charactName = Mockito.mock(PropertyDefinition.class);
		Mockito.when(charactName.getName()).thenReturn(BeCPGModel.PROP_CHARACT_NAME);
		Mockito.when(charactName.getDataType()).thenReturn(dataType);

		List<AbstractAttributeMapping> columns = new ArrayList<>();
		for (String columnId : columnIds) {
			columns.add(new AttributeMapping(columnId, columnId.startsWith(CHARACT_NAME) ? charactName : null));
		}

		ImportContext importContext = new ImportContext();
		importContext.setColumns(columns);

		return (MLText) ImportHelper.loadPropertyValue(importContext, values, pos);
	}

}
