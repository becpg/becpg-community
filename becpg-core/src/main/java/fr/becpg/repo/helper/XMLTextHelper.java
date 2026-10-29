package fr.becpg.repo.helper;

/**
 * <p>XMLTextHelper class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class XMLTextHelper {

	/**
	 * <p>Constructor for XMLTextHelper.</p>
	 */
	private XMLTextHelper() {
		//Singleton
	}
	
	/**
	 * <p>writeCData.</p>
	 *
	 * @param in a {@link java.lang.String} object
	 * @return a {@link java.lang.String} object
	 */
	public static String writeCData(String in) {
		if (in == null || in.isEmpty()) {
			return "";
		}

		int firstInvalid = indexOfInvalidChar(in, 0);
		if (firstInvalid < 0) {
			return in;
		}

		StringBuilder out = new StringBuilder(in.length());
		int start = 0;
		while (firstInvalid >= 0) {
			out.append(in, start, firstInvalid);
			start = firstInvalid + Character.charCount(in.codePointAt(firstInvalid));
			firstInvalid = indexOfInvalidChar(in, start);
		}
		out.append(in, start, in.length());

		return out.toString();
	}

	/**
	 * Tells whether a code point may appear in an XML 1.0 document. Control characters such as the
	 * optional and non breaking hyphens Word puts in copied text (U+001F, U+001E) may not, even escaped.
	 *
	 * @param codePoint the code point to test
	 * @return true if the code point is allowed
	 */
	public static boolean isXmlChar(int codePoint) {
		return (codePoint == 0x9) || (codePoint == 0xA) || (codePoint == 0xD) || ((codePoint >= 0x20) && (codePoint <= 0xD7FF))
				|| ((codePoint >= 0xE000) && (codePoint <= 0xFFFD)) || ((codePoint >= 0x10000) && (codePoint <= 0x10FFFF));
	}

	private static int indexOfInvalidChar(String in, int from) {
		int i = from;
		while (i < in.length()) {
			int codePoint = in.codePointAt(i);
			if (!isXmlChar(codePoint)) {
				return i;
			}
			i += Character.charCount(codePoint);
		}
		return -1;
	}


	/**
	 * <p>writeAttribute.</p>
	 *
	 * @param in a {@link java.lang.String} object
	 * @return a {@link java.lang.String} object
	 */
	public static String writeAttribute(String in) {
		return writeCData(in);
	}
	
	
	/**
	 * <p>writeContent.</p>
	 *
	 * @param in a {@link java.lang.String} object
	 * @return a {@link java.lang.String} object
	 */
	public static String writeContent(String in) {
		return writeCData(in);
	}
}
