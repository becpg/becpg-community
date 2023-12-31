package fr.becpg.repo.helper;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Map.Entry;

import org.alfresco.service.cmr.repository.MLText;
import org.alfresco.util.Pair;

import fr.becpg.common.diff.Diff;
import fr.becpg.common.diff.DiffMatchPatch;
import fr.becpg.common.diff.Operation;

public class LargeTextHelper {

	public static final int TEXT_SIZE_LIMIT = 50000;

	private LargeTextHelper() {
		//Do Nothing
	}
	
	public static final String elipse(String textBefore) {
		return elipse(textBefore,TEXT_SIZE_LIMIT);
	}

	public static final String elipse(String textBefore, int textLength) {
		if(textBefore!=null && textBefore.length()> textLength) {
			return textBefore.substring(0, textLength) + "...";
		}
		return textBefore;
	}

	public static Pair<String, String> createTextDiffs(String string1, String string2) {

		DiffMatchPatch dmp = new DiffMatchPatch();
		LinkedList<Diff> diffs = dmp.diff_main(string1, string2);

		StringBuilder beforeBuilder = new StringBuilder();
		StringBuilder afterBuilder = new StringBuilder();

		for (Diff diff : diffs) {
			if (diff.operation == Operation.INSERT) {
				afterBuilder.append(diff.text);
			} else if (diff.operation == Operation.DELETE) {
				beforeBuilder.append(diff.text);
			} else if ((diff.operation == Operation.EQUAL) && (diff.text.length() < 20)) {
				beforeBuilder.append(diff.text);
				afterBuilder.append(diff.text);
			}
		}

		return new Pair<>(beforeBuilder.toString(), afterBuilder.toString());
	}

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

}
