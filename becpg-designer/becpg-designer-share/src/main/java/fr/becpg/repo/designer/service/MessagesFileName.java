package fr.becpg.repo.designer.service;

/**
 * Base name and locale of a published messages file such as <code>name_en_US.properties</code>.
 * Everything after the first underscore is the locale, so a country variant is kept whole.
 *
 * @param baseName the bundle base name, without locale nor extension
 * @param locale the locale suffix, empty for the root bundle
 * @author matthieu
 */
record MessagesFileName(String baseName, String locale) {

	/** Constant <code>PROPERTIES=".properties"</code> */
	static final String PROPERTIES = ".properties";

	private static final String LOCALE_SEPARATOR = "_";

	/**
	 * Parses a messages file name.
	 *
	 * @param fileName the file name, with or without the <code>.properties</code> extension
	 * @return the parsed base name and locale
	 */
	static MessagesFileName parse(String fileName) {
		String name = fileName.endsWith(PROPERTIES) ? fileName.substring(0, fileName.length() - PROPERTIES.length()) : fileName;
		int separatorIndex = name.indexOf(LOCALE_SEPARATOR);
		if (separatorIndex < 0) {
			return new MessagesFileName(name, "");
		}
		return new MessagesFileName(name.substring(0, separatorIndex), name.substring(separatorIndex + 1));
	}

	/**
	 * Tells whether the file carries a locale suffix.
	 *
	 * @return true unless the file is the root bundle
	 */
	boolean hasLocale() {
		return !locale.isEmpty();
	}

	/**
	 * Tells whether the file feeds every UI language: the root bundle, or the bundle of the default country.
	 *
	 * @param defaultCountry the country of the JVM default locale
	 * @return true for the root bundle or the default country bundle
	 */
	boolean appliesToAllUiLocales(String defaultCountry) {
		return locale.isEmpty() || locale.equalsIgnoreCase(defaultCountry);
	}

	/**
	 * Returns the root bundle file name sharing this base name.
	 *
	 * @return the base name followed by <code>.properties</code>
	 */
	String rootFileName() {
		return baseName + PROPERTIES;
	}
}
