package fr.becpg.test.repo.helper;

import org.apache.commons.validator.routines.checkdigit.CheckDigitException;
import org.apache.commons.validator.routines.checkdigit.EAN13CheckDigit;
import org.junit.Assert;
import org.junit.Test;

import fr.becpg.repo.helper.GTINHelper;

public class GTINHelperTest {

	@Test
	public void testEAN13() throws CheckDigitException {

		String ean13 = GTINHelper.createEAN13Code("455632", "1258");

		Assert.assertEquals(ean13, "4556320012583");
		EAN13CheckDigit validator = new EAN13CheckDigit();
		Assert.assertEquals("3",validator.calculate("455632001258"));
		Assert.assertTrue(validator.isValid(ean13));

		ean13 = GTINHelper.createEAN13Code("353152003", "1");
		Assert.assertEquals(ean13, "3531520030019");
		

		ean13 = GTINHelper.createEAN13Code("360438", "25112");
		Assert.assertEquals(ean13, "3604380251129");
		
		ean13 = GTINHelper.addDigitToEANPrefix("360438025112");
		Assert.assertEquals(ean13, "3604380251129");
		
	}
	
	@Test
	public void testEAN14() throws CheckDigitException {

		String ean14 = GTINHelper.createEAN13Code("1503034", "000903");

		Assert.assertEquals(ean14, "15030340009038");
		EAN13CheckDigit validator = new EAN13CheckDigit();
		Assert.assertEquals("8",validator.calculate("1503034000903"));
		Assert.assertTrue(validator.isValid(ean14));
		
	}

	
	
}
