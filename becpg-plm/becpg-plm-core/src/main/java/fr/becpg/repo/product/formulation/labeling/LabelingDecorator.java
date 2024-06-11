package fr.becpg.repo.product.formulation.labeling;

import java.util.Locale;

/**
 * <p>LabelingDecorator interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface LabelingDecorator {

	/**
	 * <p>matchLocale.</p>
	 *
	 * @param locale a {@link java.util.Locale} object
	 * @return a boolean
	 */
	boolean matchLocale(Locale locale);

	/**
	 * <p>decorate.</p>
	 *
	 * @param input a {@link java.lang.String} object
	 * @return a {@link java.lang.String} object
	 */
	String decorate(String input);

}
