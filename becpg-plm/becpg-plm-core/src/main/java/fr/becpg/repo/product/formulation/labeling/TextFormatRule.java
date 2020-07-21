package fr.becpg.repo.product.formulation.labeling;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import fr.becpg.repo.helper.MLTextHelper;

/**
 * <p>TextFormatRule class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class TextFormatRule {
	String textFormat;
	Set<Locale> locales = new HashSet<>();

	/**
	 * <p>Constructor for TextFormatRule.</p>
	 *
	 * @param textFormat a {@link java.lang.String} object.
	 * @param locales a {@link java.util.List} object.
	 */
	public TextFormatRule(String textFormat, List<String> locales) {
		this.textFormat = textFormat;

		if (locales != null) {
			for (String tmp : locales) {
				this.locales.add(MLTextHelper.parseLocale(tmp));
			}
		}
	}

	/**
	 * <p>matchLocale.</p>
	 *
	 * @param locale a {@link java.util.Locale} object.
	 * @return a boolean.
	 */
	public boolean matchLocale(Locale locale) {
		return locales.isEmpty() || locales.contains(locale);
	}

	/**
	 * <p>Getter for the field <code>textFormat</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getTextFormat() {
		return textFormat;
	}
}

