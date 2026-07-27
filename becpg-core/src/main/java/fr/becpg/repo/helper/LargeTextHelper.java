package fr.becpg.repo.helper;

import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

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

		int totalLength = 0;
		for (String value : mlText.values()) {
			if (value != null) {
				totalLength += value.length();
			}
		}

		for (Entry<Locale, String> entry : mlText.entrySet()) {
			String value = entry.getValue();
			if ((value == null) || value.isEmpty() || (totalLength <= TEXT_SIZE_LIMIT)) {
				result.put(entry.getKey(), value);
			} else {
				// Share the global budget proportionally to each locale's actual size
				int allowed = (int) ((long) value.length() * TEXT_SIZE_LIMIT / totalLength) - ELLIPSIS_OVERHEAD;
				result.put(entry.getKey(), elipse(value, Math.max(allowed, 0)));
			}
		}
		return result;
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
