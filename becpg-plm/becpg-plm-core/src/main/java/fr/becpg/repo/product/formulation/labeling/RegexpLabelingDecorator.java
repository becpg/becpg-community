package fr.becpg.repo.product.formulation.labeling;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * <p>Abstract RegexpLabelingDecorator class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public abstract class RegexpLabelingDecorator implements LabelingDecorator {

	private List<Locale> locales = new ArrayList<>();

	/** {@inheritDoc} */
	@Override
	public boolean matchLocale(Locale locale) {
		return locales.isEmpty() || locales.contains(locale);
	}

	/** {@inheritDoc} */
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

	/**
	 * <p>transform.</p>
	 *
	 * @param group a {@link java.lang.String} object
	 * @return a {@link java.lang.String} object
	 */
	protected abstract String transform(String group);

	/**
	 * <p>getPattern.</p>
	 *
	 * @return a {@link java.util.regex.Pattern} object
	 */
	protected abstract Pattern getPattern();

}
