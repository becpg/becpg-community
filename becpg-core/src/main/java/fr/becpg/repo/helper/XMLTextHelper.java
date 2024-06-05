package fr.becpg.repo.helper;

/**
 * <p>XMLTextHelper class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class XMLTextHelper {

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
		StringBuilder out = new StringBuilder(); // Used to hold the output.
		char current; // Used to reference the current character.

		if (in == null || ("".equals(in))) {
			return ""; // vacancy test.
		}
		
		for (int i = 0; i < in.length(); i++) {
			current = in.charAt(i); // NOTE: No IndexOutOfBoundsException caught here; it should not happen.
			if ((current == 0x9) ||
					(current == 0xA) ||
					(current == 0xD) ||
					((current >= 0x20) && (current <= 0xD7FF)) ||
					((current >= 0xE000) && (current <= 0xFFFD)) ||
					((current >= 0x10000) && (current <= 0x10FFFF))) {
				out.append(current);
			}
		}
		
	     return out.toString();
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
