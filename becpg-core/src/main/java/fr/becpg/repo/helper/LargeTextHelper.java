package fr.becpg.repo.helper;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.function.Function;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.util.Pair;

import fr.becpg.common.diff.Diff;
import fr.becpg.common.diff.DiffMatchPatch;
import fr.becpg.common.diff.Operation;

/**
 * <p>LargeTextHelper class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class LargeTextHelper {

	/** Constant <code>TEXT_SIZE_LIMIT=50000</code> */
	public static final int TEXT_SIZE_LIMIT = 50000;

	/**
	 * Maximum size of a single locale value, in UTF-8 bytes.
	 *
	 * Alfresco keys {@code alf_node_properties} on the locale, so every language of a d:mltext
	 * property lands in its own {@code string_value} row. On MySQL that column is a {@code TEXT},
	 * capped at 65535 bytes, and the InnoDB dialect never spills strings to {@code serializable_value}
	 * (SchemaBootstrap sets its threshold to Integer.MAX_VALUE), so the column is the hard wall.
	 * The margin absorbs the encoding of the notice replacing an oversized value.
	 */
	public static final int MAX_LOCALE_SIZE_BYTES = 60000;

	/** Overhead kept when shortening a value to leave room for the ellipsis suffix. */
	private static final int ELLIPSIS_OVERHEAD = 20;

	/**
	 * <p>Constructor for LargeTextHelper.</p>
	 */
	private LargeTextHelper() {
		//Do Nothing
	}

	/**
	 * <p>elipse.</p>
	 *
	 * @param textBefore a {@link java.lang.String} object
	 * @return a {@link java.lang.String} object
	 */
	public static final String elipse(String textBefore) {
		return elipse(textBefore,TEXT_SIZE_LIMIT);
	}

	/**
	 * <p>elipse.</p>
	 *
	 * @param textBefore a {@link java.lang.String} object
	 * @param textLength a int
	 * @return a {@link java.lang.String} object
	 */
	public static final String elipse(String textBefore, int textLength) {
		if(textBefore!=null && textBefore.length()> textLength) {
			return textBefore.substring(0, textLength) + "...";
		}
		return textBefore;
	}

	/**
	 * <p>HTML-aware truncation. When the value is an HTML table, the cut is done after the last
	 * complete row (&lt;/tr&gt;) and the table is closed, so the rendered output stays valid instead
	 * of being broken mid-tag. Otherwise falls back to {@link #elipse(String, int)}.</p>
	 *
	 * @param value a {@link java.lang.String} object
	 * @param textLength a int
	 * @return a {@link java.lang.String} object
	 */
	public static final String elipseHtml(String value, int textLength) {
		if ((value == null) || (value.length() <= textLength)) {
			return value;
		}
		int tableStart = value.indexOf("<table");
		if (tableStart > -1) {
			String head = value.substring(0, textLength);
			int lastRow = head.lastIndexOf("</tr>");
			if (lastRow > tableStart) {
				return value.substring(0, lastRow + "</tr>".length()) + "</table>";
			}
		}
		return elipse(value, textLength);
	}

	/**
	 * <p>createTextDiffs.</p>
	 *
	 * @param string1 a {@link java.lang.String} object
	 * @param string2 a {@link java.lang.String} object
	 * @return a {@link org.alfresco.util.Pair} object
	 */
	public static Pair<String, String> createTextDiffs(String string1, String string2) {


		DiffMatchPatch dmp = new DiffMatchPatch();
		List<Diff> diffs = dmp.diffMain(string1, string2);

		StringBuilder beforeBuilder = new StringBuilder();
		StringBuilder afterBuilder = new StringBuilder();

		for (Diff diff : diffs) {
			if (diff.getOperation() == Operation.INSERT) {
				afterBuilder.append(diff.getText());
			} else if (diff.getOperation() == Operation.DELETE) {
				beforeBuilder.append(diff.getText());
			} else if ((diff.getOperation() == Operation.EQUAL) && (diff.getText().length() < 20)) {
				beforeBuilder.append(diff.getText());
				afterBuilder.append(diff.getText());
			}
		}

		return new Pair<>(beforeBuilder.toString(), afterBuilder.toString());
	}


	/**
	 * <p>elipse.</p>
	 *
	 * Returns a copy of the given {@link org.alfresco.service.cmr.repository.MLText} whose values are
	 * shortened so that the total length (all locales combined) stays under {@link #TEXT_SIZE_LIMIT}.
	 * The budget is shared between locales proportionally to their actual content, empty locales are
	 * ignored, and the source {@link org.alfresco.service.cmr.repository.MLText} is left untouched.
	 *
	 * @param mlText a {@link org.alfresco.service.cmr.repository.MLText} object
	 * @return a new {@link org.alfresco.service.cmr.repository.MLText} object
	 */
	public static MLText elipse(MLText mlText) {

		MLText result = new MLText();
		if (mlText == null) {
			return result;
		}

		int totalLength = totalLength(mlText);
		if (totalLength <= TEXT_SIZE_LIMIT) {
			result.putAll(mlText);
			return result;
		}

		for (Entry<Locale, String> entry : mlText.entrySet()) {
			String value = entry.getValue();
			if ((value == null) || value.isEmpty()) {
				result.put(entry.getKey(), value);
			} else {
				// Share the global budget proportionally to each locale's actual size
				int allowed = (int) ((long) value.length() * TEXT_SIZE_LIMIT / totalLength) - ELLIPSIS_OVERHEAD;
				result.put(entry.getKey(), elipseHtml(value, Math.max(allowed, 0)));
			}
		}
		return result;
	}

	/**
	 * <p>Replaces the locale values that cannot be persisted by the notice supplied for that locale,
	 * without ever cutting a value in the middle. A partially cut value carries wrong data and is
	 * never usable, whereas an explicit notice tells the user what happened.</p>
	 *
	 * <p>Each locale is weighed on its own: Alfresco stores every locale of a d:mltext property in
	 * its own {@code alf_node_properties} row, so one oversized language never penalises the
	 * others.</p>
	 *
	 * @param mlText a {@link org.alfresco.service.cmr.repository.MLText} object
	 * @param noticeProvider supplies the replacement notice for a dropped locale
	 * @return a new {@link org.alfresco.service.cmr.repository.MLText} object, the source is left untouched
	 */
	public static MLText dropOversizedLocales(MLText mlText, Function<Locale, String> noticeProvider) {

		MLText result = new MLText();
		if (mlText == null) {
			return result;
		}

		for (Entry<Locale, String> entry : mlText.entrySet()) {
			String value = entry.getValue();
			result.put(entry.getKey(), fitsInProperty(value) ? value : noticeProvider.apply(entry.getKey()));
		}
		return result;
	}

	/**
	 * <p>Tells whether a single locale value can be persisted, measured in UTF-8 bytes rather than in
	 * characters: the underlying column is capped in bytes, and a character weighs from one byte
	 * (markup, digits) to four, so a limit counted in characters would be either too lax for Chinese
	 * or needlessly strict for Latin scripts.</p>
	 *
	 * @param value a {@link java.lang.String} object
	 * @return true when the value fits {@link #MAX_LOCALE_SIZE_BYTES}
	 */
	public static boolean fitsInProperty(String value) {
		if (value == null) {
			return true;
		}
		// A character never exceeds 4 UTF-8 bytes, so below that ratio the value fits without encoding it
		if (value.length() <= (MAX_LOCALE_SIZE_BYTES / 4)) {
			return true;
		}
		return value.getBytes(StandardCharsets.UTF_8).length <= MAX_LOCALE_SIZE_BYTES;
	}

	private static int valueLength(String value) {
		return value != null ? value.length() : 0;
	}

	private static int totalLength(MLText mlText) {
		int totalLength = 0;
		for (String value : mlText.values()) {
			totalLength += valueLength(value);
		}
		return totalLength;
	}

	/**
	 * <p>htmlDiff.</p>
	 *
	 * @param text1 a {@link java.lang.String} object
	 * @param text2 a {@link java.lang.String} object
	 * @return a {@link java.lang.String} object
	 * @since 23.2.1.26
	 */
	public static String htmlDiff(String text1, String text2) {
		DiffMatchPatch dmp = new DiffMatchPatch();
		return dmp.diffPrettyHtml( dmp.diffMain(text1,text2 ));
	}

}
