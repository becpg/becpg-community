package fr.becpg.repo.helper;

import java.nio.charset.StandardCharsets;
import java.util.Iterator;
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

	private LargeTextHelper() {
		//Do Nothing
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
	 * @param mlText a {@link org.alfresco.service.cmr.repository.MLText} object
	 */
	public static void elipse(MLText mlText) {
		
		if (mlText.toString().length() > TEXT_SIZE_LIMIT) {
			int localesNumber = mlText.keySet().size();
			
			int newTextLength = TEXT_SIZE_LIMIT / localesNumber - 20;
			
			Iterator<Entry<Locale, String>> it = mlText.entrySet().iterator();

			while (it.hasNext()) {
				Locale locale = it.next().getKey();
				mlText.put(locale, elipse(mlText.get(locale),newTextLength));
			}
		}
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
