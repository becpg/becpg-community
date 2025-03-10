package fr.becpg.repo.decernis.helper;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.system.SystemConfigurationRegistry;

/**
 * <p>DecernisHelper class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class DecernisHelper {
	
	private static final Pattern THRESHOLD_PATTERN_1 = Pattern.compile("\\(?<=([0-9.]+)\\s*(mg/l|mg/kg)\\)?");
	private static final Pattern THRESHOLD_PATTERN_2 = Pattern.compile("\\(?<=([0-9.]+)\\s*%\\)?");
	
	private static final DecimalFormat decimalFormat = new DecimalFormat("#.########################", new DecimalFormatSymbols(Locale.ENGLISH)); // 24 decimal places

	private static final Log logger = LogFactory.getLog(DecernisHelper.class);

	private DecernisHelper() {
		
	}
	
	/**
	 * <p>truncateDoubleValue.</p>
	 *
	 * @param ingQtyPerc a {@link java.lang.Double} object
	 * @return a {@link java.lang.Double} object
	 */
	public static Double truncateDoubleValue(Double ingQtyPerc) {
		if (ingQtyPerc == null) {
			return null;
		}
		String formattedValue = decimalFormat.format(ingQtyPerc);
		return Double.parseDouble(formattedValue);
	}
	
	/**
	 * <p>extractThresholdValue.</p>
	 *
	 * @param threshold a {@link java.lang.String} object
	 * @return a {@link java.lang.Double} object
	 */
	public static Double extractThresholdValue(String threshold) {
		try {
			Matcher matcher = THRESHOLD_PATTERN_1.matcher(threshold);
			if (matcher.find()) {
				String extracted = matcher.group(1);
				return Double.parseDouble(extracted.trim()) / 10000;
			}
			matcher = THRESHOLD_PATTERN_2.matcher(threshold);
			if (matcher.find()) {
				String extracted = matcher.group(1);
				return Double.parseDouble(extracted.trim());
			}
		} catch (NumberFormatException e) {
			logger.error("Cannot parse threshold number: " + threshold);
		}
		
		return null;
	}
	
	/**
	 * <p>cleanError.</p>
	 *
	 * @param error a {@link java.lang.String} object
	 * @return a {@link java.lang.String} object
	 */
	public static String cleanError(String error) {
		if (error != null) {
			String token = SystemConfigurationRegistry.instance().confValue("beCPG.decernis.token");
			if (token != null && !token.isBlank()) {
				return error.replace(token, "XXX");
			}
		}
		return error;
	}
	
}
