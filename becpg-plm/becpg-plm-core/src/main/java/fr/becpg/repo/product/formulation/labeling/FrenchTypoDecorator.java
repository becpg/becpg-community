package fr.becpg.repo.product.formulation.labeling;

import java.util.Locale;
import java.util.regex.Pattern;

public class FrenchTypoDecorator extends RegexpLabelingDecorator {

	private static Pattern pattern = Pattern.compile("([:;\\?\\!])");
	
	@Override
	protected String transform(String group) {
		return " "+group;
	}

	@Override
	protected Pattern getPattern() {
		return pattern;
	}

	@Override
	public boolean matchLocale(Locale locale) {
		return Locale.FRENCH.getLanguage().equals(locale.getLanguage());
	}
	
	
}
