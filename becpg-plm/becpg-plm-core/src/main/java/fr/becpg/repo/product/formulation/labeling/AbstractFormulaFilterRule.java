package fr.becpg.repo.product.formulation.labeling;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import fr.becpg.repo.helper.MLTextHelper;

public abstract class AbstractFormulaFilterRule implements Serializable {

	private static final long serialVersionUID = -3030472014517241276L;

	private final String formula;

	private final Set<Locale> locales = new HashSet<>();

	private final String ruleName;
	
	protected AbstractFormulaFilterRule(String ruleName, String formula, List<String> locales) {

		this.ruleName = ruleName;
		this.formula = formula;
		if (locales != null) {
			for (String tmp : locales) {
				this.locales.add(MLTextHelper.parseLocale(tmp));
			}
		}
	}

	/**
	 * <p>Getter for the field <code>ruleName</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getRuleName() {
		return ruleName;
	}

	/**
	 * <p>Getter for the field <code>formula</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getFormula() {
		return formula;
	}

	/**
	 * <p>matchLocale.</p>
	 *
	 * @param locale a {@link java.util.Locale} object.
	 * @return a boolean.
	 */
	public boolean matchLocale(Locale locale) {
		return locales.isEmpty() || locales.contains(locale);
	}

	@Override
	public int hashCode() {
		return Objects.hash(formula, locales, ruleName);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if ((obj == null) || (getClass() != obj.getClass())) {
			return false;
		}
		AbstractFormulaFilterRule other = (AbstractFormulaFilterRule) obj;
		return Objects.equals(formula, other.formula) && Objects.equals(locales, other.locales) && Objects.equals(ruleName, other.ruleName);
	}

}
