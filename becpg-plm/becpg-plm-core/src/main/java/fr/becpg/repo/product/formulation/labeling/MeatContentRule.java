package fr.becpg.repo.product.formulation.labeling;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.helper.MLTextHelper;

/**
 * <p>MeatContentRule class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class MeatContentRule {

	NodeRef fatReplacement;
	NodeRef ctReplacement;
	String meatType;
	NodeRef component;
	
	Set<Locale> locales = new HashSet<>();

	/**
	 * <p>Constructor for MeatContentRule.</p>
	 *
	 * @param meatType a {@link java.lang.String} object.
	 * @param locales a {@link java.util.List} object.
	 */
	public MeatContentRule(String meatType, List<String> locales) {
		super();
		this.meatType = meatType;
		
		if (locales != null) {
			for (String tmp : locales) {
				this.locales.add(MLTextHelper.parseLocale(tmp));
			}
		}

	}

	/**
	 * <p>Getter for the field <code>meatType</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getMeatType() {
		return meatType;
	}

	/**
	 * <p>Setter for the field <code>meatType</code>.</p>
	 *
	 * @param meatType a {@link java.lang.String} object.
	 */
	public void setMeatType(String meatType) {
		this.meatType = meatType;
	}

	/**
	 * <p>Getter for the field <code>fatReplacement</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public NodeRef getFatReplacement() {
		return fatReplacement;
	}

	/**
	 * <p>Setter for the field <code>fatReplacement</code>.</p>
	 *
	 * @param fatReplacement a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public void setFatReplacement(NodeRef fatReplacement) {
		this.fatReplacement = fatReplacement;
	}



	/**
	 * <p>Getter for the field <code>ctReplacement</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public NodeRef getCtReplacement() {
		return ctReplacement;
	}

	/**
	 * <p>Setter for the field <code>ctReplacement</code>.</p>
	 *
	 * @param ctReplacement a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	public void setCtReplacement(NodeRef ctReplacement) {
		this.ctReplacement = ctReplacement;
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

	/**
	 * <p>Getter for the field <code>component</code>.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public NodeRef getComponent() {
		return component;
	}

	/**
	 * <p>Setter for the field <code>component</code>.</p>
	 *
	 * @param component a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	public void setComponent(NodeRef component) {
		this.component = component;
	}

	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		return Objects.hash(component, ctReplacement, fatReplacement, locales, meatType);
	}

	/** {@inheritDoc} */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MeatContentRule other = (MeatContentRule) obj;
		return Objects.equals(component, other.component) && Objects.equals(ctReplacement, other.ctReplacement)
				&& Objects.equals(fatReplacement, other.fatReplacement) && Objects.equals(locales, other.locales)
				&& Objects.equals(meatType, other.meatType);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "MeatContentRule [fatReplacement=" + fatReplacement + ", ctReplacement=" + ctReplacement + ", meatType=" + meatType + ", component="
				+ component + ", locales=" + locales + "]";
	}

	
}
