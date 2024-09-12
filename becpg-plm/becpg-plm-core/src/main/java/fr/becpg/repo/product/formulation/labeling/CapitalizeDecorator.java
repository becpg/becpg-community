package fr.becpg.repo.product.formulation.labeling;

import java.util.regex.Pattern;

import org.springframework.util.StringUtils;

/**
 * <p>CapitalizeDecorator class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class CapitalizeDecorator extends RegexpLabelingDecorator {

	private static final Pattern CAPITALIZE_PATTERN = Pattern.compile("(<\\s*ca[^>]*>.*?<\\s*/\\s*ca>)");

	/** {@inheritDoc} */
	@Override
	protected String transform(String group) {
		return StringUtils.capitalize(group.replace("<ca>", "").replace("</ca>", ""));
	}

	/** {@inheritDoc} */
	@Override
	protected Pattern getPattern() {
		return CAPITALIZE_PATTERN;
	}

}
