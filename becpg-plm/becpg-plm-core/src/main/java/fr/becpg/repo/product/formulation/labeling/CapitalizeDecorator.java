package fr.becpg.repo.product.formulation.labeling;

import java.util.regex.Pattern;

import org.springframework.util.StringUtils;

public class CapitalizeDecorator extends RegexpLabelingDecorator {

	private static final Pattern CAPITALIZE_PATTERN = Pattern.compile("(<\\s*ca[^>]*>.*?<\\s*/\\s*ca>)");

	@Override
	protected String transform(String group) {
		return StringUtils.capitalize(group.replace("<ca>", "").replace("</ca>", ""));
	}

	@Override
	protected Pattern getPattern() {
		return CAPITALIZE_PATTERN;
	}

}
