package fr.becpg.repo.product.formulation.labeling;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.alfresco.service.cmr.repository.MLText;

import fr.becpg.repo.helper.MLTextHelper;

class RenameRule {
	MLText mlText;
	MLText pluralMlText;

	Set<Locale> locales = new HashSet<>();

	/**
	 * <p>Constructor for RenameRule.</p>
	 *
	 * @param mlText a {@link org.alfresco.service.cmr.repository.MLText} object.
	 * @param pluralMlText a {@link org.alfresco.service.cmr.repository.MLText} object.
	 * @param locales a {@link java.util.List} object.
	 */
	public RenameRule(MLText mlText, MLText pluralMlText, List<String> locales) {
		this.mlText = mlText;
		this.pluralMlText = pluralMlText;

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
	 * <p>getClosestValue.</p>
	 *
	 * @param locale a {@link java.util.Locale} object.
	 * @param plural a boolean.
	 * @return a {@link java.lang.String} object.
	 */
	public String getClosestValue(Locale locale, boolean plural) {
		String ret = null;

		if (plural && (pluralMlText != null) && !pluralMlText.isEmpty()) {
			ret = MLTextHelper.getClosestValue(pluralMlText, locale);
		}

		if ((ret == null) || ret.isEmpty()) {
			ret = MLTextHelper.getClosestValue(mlText, locale);
		}

		return ret;
	}

}
