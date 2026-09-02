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
	 * Strips non-BMP Unicode characters (code points > 0xFFFF, e.g. 4-byte UTF-8 emojis)
	 * to prevent SQLException on MySQL utf8/utf8mb3 tables.
	 *
	 * @param text input text
	 * @return sanitized text without non-BMP code points, or original text if unchanged
	 */
	public static String sanitizeBmp(String text) {
		if ((text == null) || text.isEmpty() || !containsNonBmp(text)) {
			return text;
		}

		return text.codePoints()
				.filter(Character::isBmpCodePoint)
				.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
				.toString();
	}

	/**
	 * Checks if a string contains any non-BMP (supplementary) Unicode characters.
	 *
	 * @param text text to check
	 * @return true if text contains at least one non-BMP character
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
