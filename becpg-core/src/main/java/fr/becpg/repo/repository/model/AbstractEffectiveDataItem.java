package fr.becpg.repo.repository.model;

import java.util.Date;

import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;

public abstract class AbstractEffectiveDataItem extends BeCPGDataObject implements EffectiveDataItem {

	protected Date startEffectivity;
	
	protected Date endEffectivity;
	
	
	@AlfProp
	@AlfQname(qname="bcpg:startEffectivity")
	public Date getStartEffectivity() {
		return startEffectivity;
	}

	public void setStartEffectivity(Date startEffectivity) {
		this.startEffectivity = startEffectivity;
	}

	@AlfProp
	@AlfQname(qname="bcpg:endEffectivity")
	public Date getEndEffectivity() {
		return endEffectivity;
	}

	public void setEndEffectivity(Date endEffectivity) {
		this.endEffectivity = endEffectivity;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((endEffectivity == null) ? 0 : endEffectivity.hashCode());
		result = prime * result + ((startEffectivity == null) ? 0 : startEffectivity.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		AbstractEffectiveDataItem other = (AbstractEffectiveDataItem) obj;
		if (endEffectivity == null) {
			if (other.endEffectivity != null)
				return false;
		} else if (!endEffectivity.equals(other.endEffectivity))
			return false;
		if (startEffectivity == null) {
			if (other.startEffectivity != null)
				return false;
		} else if (!startEffectivity.equals(other.startEffectivity))
			return false;
		return true;
	}
	

}
