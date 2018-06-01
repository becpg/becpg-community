/*******************************************************************************
 * Copyright (C) 2010-2017 beCPG.
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
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.repo.RepoConsts;

/**
 * Format of properties (used for import and reports)
 * 
 * @author querephi
 *
 */
public class PropertyFormats {

	private static final String FORMAT_DECIMAL_VALUE = "###,###.####";

	private final ThreadLocal<SimpleDateFormat> s_localDateFormat = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			if (useDefaultLocale) {
				return new SimpleDateFormat(dateFormat, Locale.getDefault());
			} else {
				return new SimpleDateFormat(dateFormat, I18NUtil.getLocale());
			}
		}

	};

	private final ThreadLocal<SimpleDateFormat> s_localDateTimeFormat = new ThreadLocal<SimpleDateFormat>() {
		@Override
		protected SimpleDateFormat initialValue() {
			if (useDefaultLocale) {
				return new SimpleDateFormat(datetimeFormat, Locale.getDefault());
			} else {
				return new SimpleDateFormat(datetimeFormat, I18NUtil.getLocale());
			}
		}

	};

	private final ThreadLocal<DecimalFormat> s_localDecimalFormat = new ThreadLocal<DecimalFormat>() {
		@Override
		protected DecimalFormat initialValue() {
			if (useDefaultLocale) {
				return new DecimalFormat(decimalFormat);
			} else {
				DecimalFormat ret = (DecimalFormat) DecimalFormat.getInstance(I18NUtil.getLocale());
				ret.applyPattern(decimalFormat);
				return ret;
			}
		}
	};

	private boolean useDefaultLocale = true;

	private Integer maxDecimalPrecision = null;

	protected String dateFormat;

	protected String datetimeFormat;

	protected String decimalFormat;

	public void setMaxDecimalPrecision(Integer maxDecimalPrecision) {
		this.maxDecimalPrecision = maxDecimalPrecision;
	}

	public boolean isUseDefaultLocale() {
		return useDefaultLocale;
	}

	public void setUseDefaultLocale(boolean useDefaultLocale) {
		s_localDateFormat.remove();
		s_localDateTimeFormat.remove();
		this.useDefaultLocale = useDefaultLocale;
	}

	public void setDateFormat(String dateFormat) {
		s_localDateFormat.remove();
		this.dateFormat = dateFormat;
	}

	public void setDatetimeFormat(String datetimeFormat) {
		s_localDateTimeFormat.remove();
		this.datetimeFormat = datetimeFormat;
	}

	public void setDecimalFormat(String decimalFormat) {
		s_localDecimalFormat.remove();
		this.decimalFormat = decimalFormat;
	}

	public PropertyFormats(boolean useDefaultLocal) {

		this.useDefaultLocale = useDefaultLocal;

		dateFormat = RepoConsts.FORMAT_DATE;
		datetimeFormat = RepoConsts.FORMAT_DATETIME;
		decimalFormat = FORMAT_DECIMAL_VALUE;

	}


	public PropertyFormats(boolean useDefaultLocal, int maxDecimalPrecision) {
		this(useDefaultLocal);
		this.maxDecimalPrecision = maxDecimalPrecision;
	}
	

	public String formatDate(Object o) {
		return s_localDateFormat.get().format(o);
	}

	public String formatDecimal(Object o) {

		String ret = null;

		if ((maxDecimalPrecision != null) && (o != null) && (o instanceof Double)) {
			Double qty = (Double) o;

			int previousMaxDigit = s_localDecimalFormat.get().getMaximumFractionDigits();
			RoundingMode previousRoundingMode = s_localDecimalFormat.get().getRoundingMode();
			try {
			
				if ((qty != null) && (qty > -1) && (qty != 0d)) {
					int maxNum = s_localDecimalFormat.get().getMaximumFractionDigits();
					
					while (((Math.pow(10, maxNum ) * qty) < 1000)) {
						if (maxNum >= maxDecimalPrecision) {
							break;
						}
						maxNum++;
					}
					if(maxNum > previousMaxDigit) {	
						s_localDecimalFormat.get().setMaximumFractionDigits(maxNum);
						if(maxNum >= maxDecimalPrecision) {
							if((Math.pow(10, maxNum ) * qty)<1) {
								s_localDecimalFormat.get().setMinimumFractionDigits(maxNum);
								s_localDecimalFormat.get().setRoundingMode(RoundingMode.FLOOR);
							}
						} else {
							s_localDecimalFormat.get().setRoundingMode(RoundingMode.HALF_UP);
						}
					}
				}
				ret = s_localDecimalFormat.get().format(o);
			} finally {
				s_localDecimalFormat.get().setMaximumFractionDigits(previousMaxDigit);
				s_localDecimalFormat.get().setRoundingMode(previousRoundingMode);
			}
		} else {
			ret = s_localDecimalFormat.get().format(o);
		}

		return ret;
	}

	public String formatDateTime(Object o) {
		return s_localDateTimeFormat.get().format(o);
	}

	public Date parseDate(String dateString) throws ParseException {
		return s_localDateFormat.get().parse(dateString);
	}

	public Number parseDecimal(String decimalString) throws ParseException {
		return s_localDecimalFormat.get().parse(decimalString);
	}

	public DecimalFormat getDecimalFormat() {
		return s_localDecimalFormat.get();
	}

}
