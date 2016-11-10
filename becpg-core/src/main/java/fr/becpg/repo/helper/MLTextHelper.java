package fr.becpg.repo.helper;

import java.util.Locale;

import org.alfresco.service.cmr.repository.MLText;
import org.springframework.extensions.surf.util.I18NUtil;

/**
 * 
 * @author matthieu
 *
 */
public class MLTextHelper {

	/**
	 * Try to find the best match for locale or try with default server local 
	 * @param mltext
	 * @param locale
	 * @return
	 */
	public static String getClosestValue(MLText mltext, Locale locale) {
		String ret = null;

		if (mltext != null) {
			if (mltext.containsKey(locale)) {
				ret = mltext.get(locale);
			} else {

				Locale match = I18NUtil.getNearestLocale(locale, mltext.getLocales());
				if (match == null) {
					// No close matches for the locale - go for the default
					// locale
					locale = Locale.getDefault();
					match = I18NUtil.getNearestLocale(locale, mltext.getLocales());

				}
				// Did we get a match
				if (match == null) {
					// We could find no locale matches
					return null;
				} else {
					return mltext.get(match);
				}

			}
		}

		return ret;

	}

}
