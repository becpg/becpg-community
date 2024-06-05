package fr.becpg.repo.product.formulation.labeling;

import java.util.regex.Pattern;

/**
 * <p>LowerCaseDecorator class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class LowerCaseDecorator extends RegexpLabelingDecorator {

	private static final Pattern LOWERCASE_PATTERN = Pattern.compile("(<\\s*lo[^>]*>.*?<\\s*/\\s*lo>)");

	/** {@inheritDoc} */
	@Override
	protected String transform(String group) {
		return group.replace("<lo>", "").replace("</lo>", "").toLowerCase();
	}

	/** {@inheritDoc} */
	@Override
	protected Pattern getPattern() {
		return LOWERCASE_PATTERN;
	}

}
