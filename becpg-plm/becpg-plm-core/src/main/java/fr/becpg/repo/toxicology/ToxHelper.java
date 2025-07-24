package fr.becpg.repo.toxicology;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import fr.becpg.repo.product.data.productList.IngListDataItem;

public class ToxHelper {

	private static final String INDENTATION_SYMBOL = "- ";
	private static final String COLON = ": ";
	private static final String LINE_SEPARATOR = "\n";

	private ToxHelper() {
		
	}
	
	public static Double extractIngMaxQuantity(IngListDataItem ingListDataItem) {
		return ingListDataItem.getMaxi() != null ? ingListDataItem.getMaxi() : ingListDataItem.getQtyPerc();
	}
	
	public static String formatValue(Double value) {
		MathContext mc = new MathContext(6, RoundingMode.HALF_UP);
		BigDecimal bdValue = new BigDecimal(value, mc).stripTrailingZeros();
		return bdValue.toPlainString();
	}
	
	public static String removeToxValue(String values, String toxName) {
		if (values != null) {
			StringBuilder newValuesBuilder = new StringBuilder();
			for (String value : values.split(LINE_SEPARATOR)) {
				if (!value.isBlank() && !value.startsWith(INDENTATION_SYMBOL + toxName + COLON)) {
					appendNewLine(newValuesBuilder, value);
				}
			}
			return newValuesBuilder.toString();
		}
		return null;
	}
	
	public static String appendToxValue(String values, String toxName, Double newValue) {
		if (newValue == null) {
			return values;
		}
		StringBuilder newValuesBuilder = new StringBuilder();
		boolean valueFound = false;
		if (values != null) {
			for (String value : values.split(LINE_SEPARATOR)) {
				if (value.startsWith(INDENTATION_SYMBOL + toxName + COLON)) {
					appendNewLine(newValuesBuilder, INDENTATION_SYMBOL, toxName, COLON, formatValue(newValue));
					valueFound = true;
				} else if (!value.isBlank()) {
					appendNewLine(newValuesBuilder, value);
				}
			}
		}
		if (!valueFound) {
			appendNewLine(newValuesBuilder, INDENTATION_SYMBOL, toxName, COLON, formatValue(newValue));
		}
		return newValuesBuilder.toString();
	}
	
	private static void appendNewLine(StringBuilder builder, String... line) {
		if (!builder.isEmpty()) {
			builder.append(LINE_SEPARATOR);
		}
		for (String string : line) {
			builder.append(string);
		}
	}
	
	public static Double extractToxValue(String values, String toxName) {
		if (values != null) {
			for (String value : values.split(LINE_SEPARATOR)) {
				String prefix = INDENTATION_SYMBOL + toxName + COLON;
				if (value.startsWith(prefix)) {
					return Double.parseDouble(value.replace(prefix, ""));
				}
			}
		}
		return null;
	}
}
