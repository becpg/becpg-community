package fr.becpg.repo.decernis.helper;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

public class DecernisHelper {
	
	private static final DecimalFormat decimalFormat = new DecimalFormat("#.########################", new DecimalFormatSymbols(Locale.ENGLISH)); // 24 decimal places

	private DecernisHelper() {
		
	}
	
	public static Double truncateDoubleValue(Double ingQtyPerc) {
		if (ingQtyPerc == null) {
			return null;
		}
		String formattedValue = decimalFormat.format(ingQtyPerc);
		return Double.parseDouble(formattedValue);
	}
}
