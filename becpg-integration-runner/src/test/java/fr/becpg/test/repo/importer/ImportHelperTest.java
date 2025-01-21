package fr.becpg.test.repo.importer;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.Locale;

import org.junit.Assert;
import org.junit.Test;

import fr.becpg.repo.importer.ImportContext;
import fr.becpg.repo.importer.impl.ImportHelper;

public class ImportHelperTest {

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

}
