package fr.becpg.repo.helper;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.checkdigit.CheckDigitException;
import org.apache.commons.validator.routines.checkdigit.EAN13CheckDigit;

public class GTINHelper {
	
	/**
	 * SPEL T(fr.becpg.repo.helper.GTINHelper).generateEAN13Code("455121")
	 * @param prefix
	 * @return
	 * @throws CheckDigitException
	 */
	
	public static String generateEAN13Code(String prefix) throws CheckDigitException {
		return GTINHelper.createEAN13Code(prefix,AutoNumHelper.getAutoNumValue("bcpg:eanCode", "bcpg:ean13Pref"+prefix));
	}
	
	

	public static String createEAN13Code(String prefix, String serialNumber) throws CheckDigitException {
		String eanCode = prefix + StringUtils.leftPad( serialNumber, 6 , "0");
		EAN13CheckDigit validator = new EAN13CheckDigit();
		return eanCode + validator.calculate(eanCode);
	}
	
	
}
