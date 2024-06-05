package fr.becpg.repo.product.formulation.labeling;

import java.util.regex.Pattern;

/**
 * <p>UppercaseDecorator class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class UppercaseDecorator extends RegexpLabelingDecorator {

	private static final Pattern UPPERCASE_PATTERN = Pattern.compile("(<\\s*up[^>]*>.*?<\\s*/\\s*up>)");

	/** {@inheritDoc} */
	@Override
	protected String transform(String group) {
		return group.replace("<up>", "").replace("</up>", "").toUpperCase();
	}

	/** {@inheritDoc} */
	@Override
	protected Pattern getPattern() {
		return UPPERCASE_PATTERN;
	}

}
