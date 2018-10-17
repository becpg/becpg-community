package fr.becpg.repo.product.formulation.labeling;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.product.data.meat.MeatType;

public class MeatContentRule {

	NodeRef replacement;
	MeatType meatType;
	NodeRef component;
	
	Set<Locale> locales = new HashSet<>();

	public MeatContentRule(MeatType meatType, List<String> locales) {
		super();
		this.meatType = meatType;
		
		if (locales != null) {
			for (String tmp : locales) {
				this.locales.add(MLTextHelper.parseLocale(tmp));
			}
		}

	}

	public MeatType getMeatType() {
		return meatType;
	}

	public void setMeatType(MeatType meatType) {
		this.meatType = meatType;
	}

	public NodeRef getReplacement() {
		return replacement;
	}

	public void setReplacement(NodeRef replacement) {
		this.replacement = replacement;
	}

	public boolean matchLocale(Locale locale) {
		return locales.isEmpty() || locales.contains(locale);
	}

	public NodeRef getComponent() {
		return component;
	}

	public void setComponent(NodeRef component) {
		this.component = component;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((component == null) ? 0 : component.hashCode());
		result = prime * result + ((locales == null) ? 0 : locales.hashCode());
		result = prime * result + ((meatType == null) ? 0 : meatType.hashCode());
		result = prime * result + ((replacement == null) ? 0 : replacement.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MeatContentRule other = (MeatContentRule) obj;
		if (component == null) {
			if (other.component != null)
				return false;
		} else if (!component.equals(other.component))
			return false;
		if (locales == null) {
			if (other.locales != null)
				return false;
		} else if (!locales.equals(other.locales))
			return false;
		if (meatType != other.meatType)
			return false;
		if (replacement == null) {
			if (other.replacement != null)
				return false;
		} else if (!replacement.equals(other.replacement))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "MeatContentRule [replacement=" + replacement + ", meatType=" + meatType + ", component=" + component + ", locales=" + locales + "]";
	}

	
}
