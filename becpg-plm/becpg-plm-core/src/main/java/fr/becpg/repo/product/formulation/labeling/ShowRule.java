package fr.becpg.repo.product.formulation.labeling;

import java.math.RoundingMode;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import fr.becpg.repo.helper.MLTextHelper;

class ShowRule {
	String format;
	RoundingMode roundingMode = null;
	Set<Locale> locales = new HashSet<>();

	public ShowRule(String format, List<String> locales) {
		if(format.contains("|")) {
			this.format = format.split("\\|")[0];
			roundingMode = RoundingMode.valueOf(format.split("\\|")[1]);
		} else {
			this.format = format;
		}
		

		if (locales != null) {
			for (String tmp : locales) {
				this.locales.add(MLTextHelper.parseLocale(tmp));
			}
		}
	}

	public boolean matchLocale(Locale locale) {
		return locales.isEmpty() || locales.contains(locale);
	}

}
