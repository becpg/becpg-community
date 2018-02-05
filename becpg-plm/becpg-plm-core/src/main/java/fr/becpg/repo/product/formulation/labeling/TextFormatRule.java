package fr.becpg.repo.product.formulation.labeling;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import fr.becpg.repo.helper.MLTextHelper;

public class TextFormatRule {
	String textFormat;
	Set<Locale> locales = new HashSet<>();

	public TextFormatRule(String textFormat, List<String> locales) {
		this.textFormat = textFormat;

		if (locales != null) {
			for (String tmp : locales) {
				this.locales.add(MLTextHelper.parseLocale(tmp));
			}
		}
	}

	public boolean matchLocale(Locale locale) {
		return locales.isEmpty() || locales.contains(locale);
	}

	public String getTextFormat() {
		return textFormat;
	}
}

