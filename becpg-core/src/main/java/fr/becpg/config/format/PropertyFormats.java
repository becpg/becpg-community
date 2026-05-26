package fr.becpg.config.format;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.extensions.surf.util.I18NUtil;

/**
 * Modern Java 17 implementation using DateTimeFormatter and Records
 * Provides better performance, thread safety, and immutability
 *
 * @author querephi, matthieu
 */
public class PropertyFormats {

	// Cache for thread-safe, immutable formatters
	/** Constant <code>DATE_FORMATTER_CACHE</code> */
	private static final ConcurrentHashMap<FormatConfig, DateTimeFormatter> DATE_FORMATTER_CACHE = new ConcurrentHashMap<>();
	/** Constant <code>NUMBER_FORMATTER_CACHE</code> */
	private static final ConcurrentHashMap<FormatConfig, NumberFormat> NUMBER_FORMATTER_CACHE = new ConcurrentHashMap<>();
	/** Constant <code>MODE_CACHE</code> */
	private static final ConcurrentHashMap<String, PropertyFormats> MODE_CACHE = new ConcurrentHashMap<>();

	// Default patterns as constants
	/** Constant <code>DEFAULT_DATE_PATTERN="EEE d MMM yyyy"</code> */
	private static final String DEFAULT_DATE_PATTERN = "EEE d MMM yyyy";
	/** Constant <code>DEFAULT_DATETIME_PATTERN="EEE d MMM yyyy HH:mm:ss"</code> */
	private static final String DEFAULT_DATETIME_PATTERN = "EEE d MMM yyyy HH:mm:ss";
	/** Constant <code>DEFAULT_DECIMAL_PATTERN="###,###.####"</code> */
	public static final String DEFAULT_DECIMAL_PATTERN = "###,###.####";

	/** Constant <code>PROCESS_DATE_FORMAT="dd MMMM, yyyy"</code> */
	public static final String PROCESS_DATE_FORMAT = "dd MMMM, yyyy";
	/** Constant <code>FRENCH_CSV_DATETIME_FORMAT="dd/MM/yyyy HH:mm:ss"</code> */
	private static final String FRENCH_CSV_DATETIME_FORMAT = "dd/MM/yyyy HH:mm:ss";
	/** Constant <code>CSV_DATETIME_FORMAT="MM/dd/yyyy HH:mm:ss"</code> */
	private static final String CSV_DATETIME_FORMAT = "MM/dd/yyyy HH:mm:ss";
	/** Constant <code>FRENCH_CSV_DATE_FORMAT="dd/MM/yyyy"</code> */
	private static final String FRENCH_CSV_DATE_FORMAT = "dd/MM/yyyy";
	/** Constant <code>CSV_DATE_FORMAT="MM/dd/yyyy"</code> */
	private static final String CSV_DATE_FORMAT = "MM/dd/yyyy";

	/** Constant <code>COMPARE_MAX_PRECISION=9</code> */
	private static final int COMPARE_MAX_PRECISION = 9;

	// Immutable configuration record
	public record FormatConfig(String pattern, Locale locale, boolean useDefaultLocale) {
		public FormatConfig {
			// Compact constructor for validation if needed
			if ((pattern == null) || pattern.isBlank()) {
				throw new IllegalArgumentException("Pattern cannot be null or blank");
			}
		}
	}

	private final boolean useDefaultLocale;
	private final Integer maxDecimalPrecision;
	private final String datePattern;
	private final String datetimePattern;
	private final String decimalPattern;

	// Cached config instances (immutable)
	private final FormatConfig dateConfig;
	private final FormatConfig datetimeConfig;
	private final FormatConfig decimalConfig;

	/**
	 * <p>Constructor for PropertyFormats.</p>
	 *
	 * @param useDefaultLocale a boolean
	 */
	public PropertyFormats(boolean useDefaultLocale) {
		this(useDefaultLocale, DEFAULT_DATE_PATTERN, DEFAULT_DATETIME_PATTERN, DEFAULT_DECIMAL_PATTERN, null);
	}

	/**
	 * <p>Constructor for PropertyFormats.</p>
	 *
	 * @param useDefaultLocale a boolean
	 * @param datePattern a {@link java.lang.String} object
	 * @param datetimePattern a {@link java.lang.String} object
	 * @param decimalPattern a {@link java.lang.String} object
	 */
	public PropertyFormats(boolean useDefaultLocale, String datePattern, String datetimePattern, String decimalPattern) {
		this(useDefaultLocale, datePattern, datetimePattern, decimalPattern, null);
	}

	/**
	 * <p>Constructor for PropertyFormats.</p>
	 *
	 * @param useDefaultLocale a boolean
	 * @param datePattern a {@link java.lang.String} object
	 * @param datetimePattern a {@link java.lang.String} object
	 * @param decimalPattern a {@link java.lang.String} object
	 * @param maxDecimalPrecision a {@link java.lang.Integer} object
	 */
	public PropertyFormats(boolean useDefaultLocale, String datePattern, String datetimePattern, String decimalPattern, Integer maxDecimalPrecision) {
		this.useDefaultLocale = useDefaultLocale;
		this.maxDecimalPrecision = maxDecimalPrecision;
		this.datePattern = datePattern;
		this.datetimePattern = datetimePattern;
		this.decimalPattern = decimalPattern;

		// Create immutable config objects
		Locale locale = useDefaultLocale ? Locale.getDefault() : I18NUtil.getLocale();
		this.dateConfig = new FormatConfig(datePattern, locale, useDefaultLocale);
		this.datetimeConfig = new FormatConfig(datetimePattern, locale, useDefaultLocale);
		this.decimalConfig = new FormatConfig(decimalPattern, locale, useDefaultLocale);
	}

	/**
	 * <p>withDateFormat.</p>
	 *
	 * @param dateFormat a {@link java.lang.String} object
	 * @return a {@link fr.becpg.config.format.PropertyFormats} object
	 */
	public PropertyFormats withDateFormat(String dateFormat) {
		return new PropertyFormats(this.useDefaultLocale, dateFormat, this.datetimePattern, this.decimalPattern, this.maxDecimalPrecision);
	}

	/**
	 * <p>withDateTimeFormat.</p>
	 *
	 * @param datetimeFormat a {@link java.lang.String} object
	 * @return a {@link fr.becpg.config.format.PropertyFormats} object
	 */
	public PropertyFormats withDateTimeFormat(String datetimeFormat) {
		return new PropertyFormats(this.useDefaultLocale, this.datePattern, datetimeFormat, this.decimalPattern, this.maxDecimalPrecision);
	}

	/**
	 * <p>withDecimalFormat.</p>
	 *
	 * @param decimalFormat a {@link java.lang.String} object
	 * @return a {@link fr.becpg.config.format.PropertyFormats} object
	 */
	public PropertyFormats withDecimalFormat(String decimalFormat) {
		return new PropertyFormats(this.useDefaultLocale, this.datePattern, this.datetimePattern, decimalFormat, this.maxDecimalPrecision);
	}

	/**
	 * <p>forMode.</p>
	 *
	 * @param mode a {@link fr.becpg.config.format.FormatMode} object
	 * @param useServerLocale a boolean
	 * @return a {@link fr.becpg.config.format.PropertyFormats} object
	 */
	public static PropertyFormats forMode(final FormatMode mode, boolean useServerLocale) {
		return MODE_CACHE.computeIfAbsent((mode != null ? mode.toString() + "-" : "") + useServerLocale, k -> {
			PropertyFormats ret;
			if (FormatMode.PROCESS.equals(mode)) {
				ret = new PropertyFormats(useServerLocale, PROCESS_DATE_FORMAT, DEFAULT_DATETIME_PATTERN, DEFAULT_DECIMAL_PATTERN);
			} else if (useServerLocale) {
				ret = new PropertyFormats(useServerLocale);
			} else if (FormatMode.CSV.equals(mode)) {
				if (Locale.FRENCH.equals(I18NUtil.getContentLocaleLang())) {
					ret = new PropertyFormats(false, FRENCH_CSV_DATE_FORMAT, FRENCH_CSV_DATETIME_FORMAT, DEFAULT_DECIMAL_PATTERN);
				} else {
					ret = new PropertyFormats(false, CSV_DATE_FORMAT, CSV_DATETIME_FORMAT, DEFAULT_DECIMAL_PATTERN);
				}
			} else if (FormatMode.COMP.equals(mode)) {
				ret = new PropertyFormats(false, DEFAULT_DATE_PATTERN, DEFAULT_DATETIME_PATTERN, DEFAULT_DECIMAL_PATTERN, COMPARE_MAX_PRECISION);
			} else {
				ret = new PropertyFormats(false);
			}
			return ret;
		});
	}

	/**
	 * Factory method to create a new instance with a different datetime pattern
	 *
	 * @param pattern the new datetime pattern to use
	 * @return a new PropertyFormats instance with the updated datetime pattern
	 */
	public PropertyFormats withDatetimePattern(String pattern) {
		return new PropertyFormats(useDefaultLocale, datePattern, pattern, decimalPattern, maxDecimalPrecision);
	}

	// Modern formatting methods using DateTimeFormatter (thread-safe and immutable)
	/**
	 * <p>formatDate.</p>
	 *
	 * @param o a {@link java.lang.Object} object
	 * @return a {@link java.lang.String} object
	 */
	public String formatDate(Object o) {
		if (o == null) {
			return null;
		} else if (o instanceof Date date) {
			return formatDate(convertToLocalDate(date));
		} else if (o instanceof LocalDate localDate) {
			return getDateFormatter().format(localDate);
		} else if (o instanceof LocalDateTime localDateTime) {
			return getDateFormatter().format(localDateTime.toLocalDate());
		} else {
			throw new IllegalArgumentException("Unsupported date type: " + o.getClass());
		}
	}

	/**
	 * <p>formatDateTime.</p>
	 *
	 * @param o a {@link java.lang.Object} object
	 * @return a {@link java.lang.String} object
	 */
	public String formatDateTime(Object o) {
		if (o == null) {
			return null;
		} else if (o instanceof Date date) {
			return formatDateTime(convertToLocalDateTime(date));
		} else if (o instanceof LocalDateTime localDateTime) {
			return getDateTimeFormatter().format(localDateTime);
		} else if (o instanceof LocalDate localDate) {
			return getDateTimeFormatter().format(localDate.atStartOfDay());
		} else {
			throw new IllegalArgumentException("Unsupported datetime type: " + o.getClass());
		}
	}

	/**
	 * <p>formatDecimal.</p>
	 *
	 * @param o a {@link java.lang.Object} object
	 * @return a {@link java.lang.String} object
	 */
	public String formatDecimal(Object o) {
		if (o == null) {
			return "";
		}

		var formatter = getNumberFormatter();

		// Enhanced precision handling for small numbers
		if ((maxDecimalPrecision != null) && o instanceof Double qty && (qty > -1) && (qty != 0d)) {
			return formatWithPrecision(qty, formatter);
		}

		return formatter.format(o);
	}

	/**
	 * <p>formatWithPrecision.</p>
	 *
	 * @param qty a {@link java.lang.Double} object
	 * @param baseFormatter a {@link java.text.NumberFormat} object
	 * @return a {@link java.lang.String} object
	 */
	private String formatWithPrecision(Double qty, NumberFormat baseFormatter) {
		// Create a copy for thread safety
		var formatter = (DecimalFormat) ((DecimalFormat) baseFormatter).clone();

		int maxNum = formatter.getMaximumFractionDigits();

		// Calculate required precision for small numbers
		while (((Math.pow(10, maxNum) * qty) < 1000) && (maxNum < maxDecimalPrecision)) {
			maxNum++;
		}

		if (maxNum > formatter.getMaximumFractionDigits()) {
			formatter.setMaximumFractionDigits(maxNum);

			if ((maxNum >= maxDecimalPrecision) && ((Math.pow(10, maxNum) * qty) < 1)) {
				formatter.setMinimumFractionDigits(maxNum);
				formatter.setRoundingMode(RoundingMode.FLOOR);
			} else {
				formatter.setRoundingMode(RoundingMode.HALF_UP);
			}
		}

		return formatter.format(qty);
	}

	// Modern parsing methods with better error handling
	/**
	 * <p>parseDate.</p>
	 *
	 * @param dateString a {@link java.lang.String} object
	 * @return a {@link java.util.Date} object
	 * @throws java.text.ParseException if any.
	 */
	public Date parseDate(String dateString) throws ParseException {
		if ((dateString == null) || dateString.isBlank()) {
			throw new ParseException("Date string cannot be null or blank", 0);
		}

		try {
			LocalDate localDate = LocalDate.parse(dateString, getDateFormatter());
			return convertToDate(localDate.atStartOfDay());
		} catch (DateTimeParseException e) {
			throw new ParseException("Unable to parse date: " + dateString, e.getErrorIndex());
		}
	}

	/**
	 * <p>parseDateTime.</p>
	 *
	 * @param dateString a {@link java.lang.String} object
	 * @return a {@link java.util.Date} object
	 * @throws java.text.ParseException if any.
	 */
	public Date parseDateTime(String dateString) throws ParseException {
		if ((dateString == null) || dateString.isBlank()) {
			throw new ParseException("DateTime string cannot be null or blank", 0);
		}

		try {
			LocalDateTime localDateTime = LocalDateTime.parse(dateString, getDateTimeFormatter());
			return convertToDate(localDateTime);
		} catch (DateTimeParseException e) {
			throw new ParseException("Unable to parse datetime: " + dateString, e.getErrorIndex());
		}
	}

	/**
	 * <p>parseDecimal.</p>
	 *
	 * @param decimalString a {@link java.lang.String} object
	 * @return a {@link java.lang.Number} object
	 * @throws java.text.ParseException if any.
	 */
	public Number parseDecimal(String decimalString) throws ParseException {
		if ((decimalString == null) || decimalString.isBlank()) {
			throw new ParseException("Decimal string cannot be null or blank", 0);
		}

		return getNumberFormatter().parse(decimalString);
	}

	// Cached formatter retrieval methods
	/**
	 * <p>getDateFormatter.</p>
	 *
	 * @return a {@link java.time.format.DateTimeFormatter} object
	 */
	private DateTimeFormatter getDateFormatter() {
		return DATE_FORMATTER_CACHE.computeIfAbsent(dateConfig, this::createDateTimeFormatter);
	}

	/**
	 * <p>getDateTimeFormatter.</p>
	 *
	 * @return a {@link java.time.format.DateTimeFormatter} object
	 */
	private DateTimeFormatter getDateTimeFormatter() {
		return DATE_FORMATTER_CACHE.computeIfAbsent(datetimeConfig, this::createDateTimeFormatter);
	}

	/**
	 * <p>getNumberFormatter.</p>
	 *
	 * @return a {@link java.text.NumberFormat} object
	 */
	private NumberFormat getNumberFormatter() {
		return NUMBER_FORMATTER_CACHE.computeIfAbsent(decimalConfig, this::createNumberFormat);
	}

	// Factory methods for creating formatters
	/**
	 * <p>createDateTimeFormatter.</p>
	 *
	 * @param config a {@link fr.becpg.config.format.PropertyFormats.FormatConfig} object
	 * @return a {@link java.time.format.DateTimeFormatter} object
	 */
	private DateTimeFormatter createDateTimeFormatter(FormatConfig config) {
		return DateTimeFormatter.ofPattern(config.pattern(), config.locale());
	}

	/**
	 * <p>createNumberFormat.</p>
	 *
	 * @param config a {@link fr.becpg.config.format.PropertyFormats.FormatConfig} object
	 * @return a {@link java.text.NumberFormat} object
	 */
	private NumberFormat createNumberFormat(FormatConfig config) {
		if (config.useDefaultLocale()) {
			return new DecimalFormat(config.pattern());
		} else {
			var formatter = (DecimalFormat) NumberFormat.getInstance(config.locale());
			formatter.applyPattern(config.pattern());
			return formatter;
		}
	}

	// Utility methods for Date/LocalDateTime conversion
	/**
	 * <p>convertToLocalDate.</p>
	 *
	 * @param date a {@link java.util.Date} object
	 * @return a {@link java.time.LocalDate} object
	 */
	private LocalDate convertToLocalDate(Date date) {
		return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	}

	/**
	 * <p>convertToLocalDateTime.</p>
	 *
	 * @param date a {@link java.util.Date} object
	 * @return a {@link java.time.LocalDateTime} object
	 */
	private LocalDateTime convertToLocalDateTime(Date date) {
		return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
	}

	/**
	 * <p>convertToDate.</p>
	 *
	 * @param localDateTime a {@link java.time.LocalDateTime} object
	 * @return a {@link java.util.Date} object
	 */
	private Date convertToDate(LocalDateTime localDateTime) {
		return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
	}

	// Getters for configuration
	/**
	 * <p>isUseDefaultLocale.</p>
	 *
	 * @return a boolean
	 */
	public boolean isUseDefaultLocale() {
		return useDefaultLocale;
	}

	/**
	 * <p>Getter for the field <code>maxDecimalPrecision</code>.</p>
	 *
	 * @return a {@link java.lang.Integer} object
	 */
	public Integer getMaxDecimalPrecision() {
		return maxDecimalPrecision;
	}

	/**
	 * <p>Getter for the field <code>datePattern</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getDatePattern() {
		return datePattern;
	}

	/**
	 * <p>Getter for the field <code>datetimePattern</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getDatetimePattern() {
		return datetimePattern;
	}

	/**
	 * <p>Getter for the field <code>decimalPattern</code>.</p>
	 *
	 * @return a {@link java.lang.String} object
	 */
	public String getDecimalPattern() {
		return decimalPattern;
	}

	// Backward compatibility
	/**
	 * <p>getDecimalFormat.</p>
	 *
	 * @return a {@link java.text.DecimalFormat} object
	 */
	public DecimalFormat getDecimalFormat() {
		return (DecimalFormat) getNumberFormatter();
	}

	// Cache management
	/**
	 * <p>clearFormatCache.</p>
	 */
	public static void clearFormatCache() {
		DATE_FORMATTER_CACHE.clear();
		NUMBER_FORMATTER_CACHE.clear();
		MODE_CACHE.clear();
	}

}
