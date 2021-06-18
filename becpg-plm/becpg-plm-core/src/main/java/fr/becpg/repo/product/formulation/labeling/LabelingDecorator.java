package fr.becpg.repo.product.formulation.labeling;

import java.util.Locale;

public interface LabelingDecorator {

	boolean matchLocale(Locale locale);

	String decorate(String input);

}
