/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG.
 *
 * This file is part of beCPG
 *
 * beCPG is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * beCPG is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.config.format;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Format of properties (used for import and reports)
 *
 * @author querephi, matthieu
 * @version $Id: $Id
 */
public class PropertyFormats {

	private boolean useDefaultLocale = true;
	private Integer maxDecimalPrecision = null;
	protected String dateFormat;
	protected String datetimeFormat;
	protected String decimalFormat;

	/**
	 * <p>Constructor for PropertyFormats.</p>
	 *
	 * @param useDefaultLocal a boolean.
	 */
	public PropertyFormats(boolean useDefaultLocal) {
		this(useDefaultLocal, "EEE d MMM yyyy", "EEE d MMM yyyy HH:mm:ss", "###,###.####");
	}

	/**
	* <p>Constructor for PropertyFormats.</p>
	*
	* @param useDefaultLocal a boolean.
	* @param dateFormat a {@link java.lang.String} object.
	* @param datetimeFormat a {@link java.lang.String} object.
	* @param decimalFormat a {@link java.lang.String} object.
	*/
	public PropertyFormats(boolean useDefaultLocal, String dateFormat, String datetimeFormat, String decimalFormat) {
		this.useDefaultLocale = useDefaultLocal;
		this.dateFormat = dateFormat;
		this.datetimeFormat = datetimeFormat;
		this.decimalFormat = decimalFormat;
	}

	/**
	 * <p>Setter for the field <code>maxDecimalPrecision</code>.</p>
	 *
	 * @param maxDecimalPrecision a {@link java.lang.Integer} object.
	 */
	public void setMaxDecimalPrecision(Integer maxDecimalPrecision) {
		this.maxDecimalPrecision = maxDecimalPrecision;
	}

	/**
	 * <p>isUseDefaultLocale.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isUseDefaultLocale() {
		return useDefaultLocale;
	}

	/**
	 * <p>Setter for the field <code>useDefaultLocale</code>.</p>
	 *
	 * @param useDefaultLocale a boolean.
	 */
	public void setUseDefaultLocale(boolean useDefaultLocale) {
		localDateFormat.remove();
		localDateTimeFormat.remove();
		this.useDefaultLocale = useDefaultLocale;
	}

	/**
	 * <p>Setter for the field <code>dateFormat</code>.</p>
	 *
	 * @param dateFormat a {@link java.lang.String} object.
	 */
	public void setDateFormat(String dateFormat) {
		localDateFormat.remove();
		this.dateFormat = dateFormat;
	}

	/**
	 * <p>Setter for the field <code>datetimeFormat</code>.</p>
	 *
	 * @param datetimeFormat a {@link java.lang.String} object.
	 */
	public void setDatetimeFormat(String datetimeFormat) {
		localDateTimeFormat.remove();
		this.datetimeFormat = datetimeFormat;
	}

	/**
	 * <p>Setter for the field <code>decimalFormat</code>.</p>
	 *
	 * @param decimalFormat a {@link java.lang.String} object.
	 */
	public void setDecimalFormat(String decimalFormat) {
		localDecimalFormat.remove();
		this.decimalFormat = decimalFormat;
	}

	/**
	 * <p>Constructor for PropertyFormats.</p>
	 *
	 * @param useDefaultLocal a boolean.
	 * @param maxDecimalPrecision a int.
	 */
	public PropertyFormats(boolean useDefaultLocal, int maxDecimalPrecision) {
		this(useDefaultLocal);
		this.maxDecimalPrecision = maxDecimalPrecision;
	}

	/**
	 * <p>formatDate.</p>
	 *
	 * @param o a {@link java.lang.Object} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String formatDate(Object o) {
		return localDateFormat.get().format(o);
	}

	/**
	 * <p>formatDecimal.</p>
	 *
	 * @param o a {@link java.lang.Object} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String formatDecimal(Object o) {
		String ret = null;

		if ((maxDecimalPrecision != null) && (o != null) && (o instanceof Double)) {
			Double qty = (Double) o;

			int previousMaxDigit = localDecimalFormat.get().getMaximumFractionDigits();
			RoundingMode previousRoundingMode = localDecimalFormat.get().getRoundingMode();
			try {

				if ((qty > -1) && (qty != 0d)) {
					int maxNum = localDecimalFormat.get().getMaximumFractionDigits();

					while (((Math.pow(10, maxNum) * qty) < 1000)) {
						if (maxNum >= maxDecimalPrecision) {
							break;
						}
						maxNum++;
					}
					if (maxNum > previousMaxDigit) {
						localDecimalFormat.get().setMaximumFractionDigits(maxNum);
						if (maxNum >= maxDecimalPrecision) {
							if ((Math.pow(10, maxNum) * qty) < 1) {
								localDecimalFormat.get().setMinimumFractionDigits(maxNum);
								localDecimalFormat.get().setRoundingMode(RoundingMode.FLOOR);
							}
						} else {
							localDecimalFormat.get().setRoundingMode(RoundingMode.HALF_UP);
						}
					}
				}
				ret = localDecimalFormat.get().format(o);
			} finally {
				localDecimalFormat.get().setMaximumFractionDigits(previousMaxDigit);
				localDecimalFormat.get().setRoundingMode(previousRoundingMode);
			}
		} else {
			ret = localDecimalFormat.get().format(o);
		}

		return ret;
	}

	/**
	 * <p>formatDateTime.</p>
	 *
	 * @param o a {@link java.lang.Object} object.
	 * @return a {@link java.lang.String} object.
	 */
	public String formatDateTime(Object o) {
		return localDateTimeFormat.get().format(o);
	}

	/**
	 * <p>parseDate.</p>
	 *
	 * @param dateString a {@link java.lang.String} object.
	 * @return a {@link java.util.Date} object.
	 * @throws java.text.ParseException if any.
	 */
	public Date parseDate(String dateString) throws ParseException {
		return localDateFormat.get().parse(dateString);
	}
	
	public Date parseDateTime(String dateString) throws ParseException {
		return localDateTimeFormat.get().parse(dateString);
	}

	/**
	 * <p>parseDecimal.</p>
	 *
	 * @param decimalString a {@link java.lang.String} object.
	 * @return a {@link java.lang.Number} object.
	 * @throws java.text.ParseException if any.
	 */
	public Number parseDecimal(String decimalString) throws ParseException {
		return localDecimalFormat.get().parse(decimalString);
	}

	/**
	 * <p>Getter for the field <code>decimalFormat</code>.</p>
	 *
	 * @return a {@link java.text.DecimalFormat} object.
	 */
	public DecimalFormat getDecimalFormat() {
		return localDecimalFormat.get();
	}

	private final ThreadLocal<SimpleDateFormat> localDateFormat = ThreadLocal.withInitial(() -> {
		if (useDefaultLocale) {
			return new SimpleDateFormat(dateFormat, Locale.getDefault());
		} else {
			return new SimpleDateFormat(dateFormat, I18NUtil.getLocale());
		}
	});

	private final ThreadLocal<SimpleDateFormat> localDateTimeFormat = ThreadLocal.withInitial(() -> {
		if (useDefaultLocale) {
			return new SimpleDateFormat(datetimeFormat, Locale.getDefault());
		} else {
			return new SimpleDateFormat(datetimeFormat, I18NUtil.getLocale());
		}
	});

	private final ThreadLocal<DecimalFormat> localDecimalFormat = ThreadLocal.withInitial(() -> {
		if (useDefaultLocale) {
			return new DecimalFormat(decimalFormat);
		} else {
			DecimalFormat ret = (DecimalFormat) NumberFormat.getInstance(I18NUtil.getLocale());
			ret.applyPattern(decimalFormat);
			return ret;
		}
	});

}
