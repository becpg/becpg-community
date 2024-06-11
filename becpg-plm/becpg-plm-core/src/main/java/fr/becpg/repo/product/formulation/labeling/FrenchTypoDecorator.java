package fr.becpg.repo.product.formulation.labeling;

import java.util.Locale;
import java.util.regex.Pattern;

/**
 * <p>FrenchTypoDecorator class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class FrenchTypoDecorator extends RegexpLabelingDecorator {

	private static Pattern pattern = Pattern.compile("([:;\\?\\!])");
	
	/** {@inheritDoc} */
	@Override
	protected String transform(String group) {
		return " "+group;
	}

	/** {@inheritDoc} */
	@Override
	protected Pattern getPattern() {
		return pattern;
	}

	/** {@inheritDoc} */
	@Override
	public boolean matchLocale(Locale locale) {
		return Locale.FRENCH.getLanguage().equals(locale.getLanguage());
	}
	
	
}
