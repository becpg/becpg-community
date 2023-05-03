package fr.becpg.repo.helper;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.checkdigit.CheckDigitException;
import org.apache.commons.validator.routines.checkdigit.EAN13CheckDigit;

/**
 * <p>GTINHelper class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class GTINHelper {
	
	/**
	 * SPEL T(fr.becpg.repo.helper.GTINHelper).generateEAN13Code("455121")
	 *
	 * @param prefix a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 * @throws org.apache.commons.validator.routines.checkdigit.CheckDigitException if any.
	 */
	public static String generateEAN13Code(String prefix) throws CheckDigitException {
		return GTINHelper.createEAN13Code(prefix,AutoNumHelper.getAutoNumValue("bcpg:eanCode", "bcpg:ean13Pref"+prefix));
	}
	
	

	/**
	 * <p>createEAN13Code.</p>
	 *
	 * @param prefix a {@link java.lang.String} object.
	 * @param serialNumber a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 * @throws org.apache.commons.validator.routines.checkdigit.CheckDigitException if any.
	 */
	public static String createEAN13Code(String prefix, String serialNumber) throws CheckDigitException {
		String eanCode = prefix + StringUtils.leftPad( serialNumber, 12-prefix.length() , "0");
		EAN13CheckDigit validator = new EAN13CheckDigit();
		return eanCode + validator.calculate(eanCode);
	}
	
	
}
