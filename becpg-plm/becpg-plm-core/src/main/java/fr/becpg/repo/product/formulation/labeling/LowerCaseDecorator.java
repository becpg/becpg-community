package fr.becpg.repo.product.formulation.labeling;

import java.util.regex.Pattern;

public class LowerCaseDecorator extends RegexpLabelingDecorator {

	private static final Pattern LOWERCASE_PATTERN = Pattern.compile("(<\\s*lo[^>]*>.*?<\\s*/\\s*lo>)");

	@Override
	protected String transform(String group) {
		return group.replace("<lo>", "").replace("</lo>", "").toLowerCase();
	}

	@Override
	protected Pattern getPattern() {
		return LOWERCASE_PATTERN;
	}

}
