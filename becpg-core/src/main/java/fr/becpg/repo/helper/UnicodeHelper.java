package fr.becpg.repo.helper;

/**
 * Utility methods for Unicode text sanitization.
 *
 * @author beCPG
 */
public final class UnicodeHelper {

	private UnicodeHelper() {
		// Utility class
	}

	/**
	 * Strips the characters that MySQL utf8/utf8mb3 tables cannot store, to prevent SQLException:
	 * non-BMP characters (code points > 0xFFFF, e.g. 4-byte UTF-8 emojis) and unpaired surrogates.
	 *
	 * @param text input text
	 * @return sanitized text holding only storable code points, or original text if unchanged
	 */
	public static String sanitizeBmp(String text) {
		if ((text == null) || text.isEmpty() || !containsNonBmp(text)) {
			return text;
		}

		return text.codePoints()
				.filter(UnicodeHelper::isStorableCodePoint)
				.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
				.toString();
	}

	/**
	 * Checks whether a code point can be stored in a MySQL utf8/utf8mb3 column. A supplementary code
	 * point needs 4 UTF-8 bytes, and an unpaired surrogate, which is a BMP code point on its own, has
	 * no valid UTF-8 encoding at all.
	 *
	 * @param codePoint code point to check
	 * @return true if the code point is a BMP code point other than a surrogate
	 */
	private static boolean isStorableCodePoint(int codePoint) {
		return Character.isBmpCodePoint(codePoint) && !Character.isSurrogate((char) codePoint);
	}

	/**
	 * Checks if a string holds any character that {@link #sanitizeBmp(String)} strips, that is a
	 * supplementary character or an unpaired surrogate.
	 *
	 * @param text text to check
	 * @return true if text contains at least one surrogate char
	 */
	public static boolean containsNonBmp(String text) {
		if ((text == null) || text.isEmpty()) {
			return false;
		}

		for (int i = 0; i < text.length(); i++) {
			if (Character.isSurrogate(text.charAt(i))) {
				return true;
			}
		}
		return false;
	}
}
