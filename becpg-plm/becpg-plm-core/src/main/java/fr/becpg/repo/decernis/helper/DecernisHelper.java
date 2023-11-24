package fr.becpg.repo.decernis.helper;

import java.text.DecimalFormat;

public class DecernisHelper {
	
	private static final DecimalFormat decimalFormat = new DecimalFormat("#.########################"); // 24 decimal places

	public static Double truncateDoubleValue(Double ingQtyPerc) {
		if (ingQtyPerc == null) {
			return null;
		}
		String formattedValue = decimalFormat.format(ingQtyPerc);
		return Double.parseDouble(formattedValue);
	}
}
