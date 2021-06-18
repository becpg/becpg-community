package fr.becpg.repo.product.formulation.labeling;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class RegexpLabelingDecorator implements LabelingDecorator {

	private List<Locale> locales = new ArrayList<>();

	@Override
	public boolean matchLocale(Locale locale) {
		return locales.isEmpty() || locales.contains(locale);
	}

	@Override
	public String decorate(String input) {

		Matcher m = getPattern().matcher(input);

		StringBuilder sb = new StringBuilder();
		while (m.find()) {
			m.appendReplacement(sb, transform(m.group()));
		}
		m.appendTail(sb);

		return sb.toString();
	}

	protected abstract String transform(String group);

	protected abstract Pattern getPattern();

}
