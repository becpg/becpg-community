package fr.becpg.repo.repository.model;

import fr.becpg.repo.repository.annotation.AlfProp;
import fr.becpg.repo.repository.annotation.AlfQname;

public abstract class AbstractManualDataItem extends BeCPGDataObject implements IManualDataItem {

	
	protected Boolean isManual;

	@AlfProp
	@AlfQname(qname="bcpg:isManual")
	public Boolean getIsManual() {
		return isManual;
	}

	public void setIsManual(Boolean isManual) {
		this.isManual = isManual;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((isManual == null) ? 0 : isManual.hashCode());
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
		AbstractManualDataItem other = (AbstractManualDataItem) obj;
		if (isManual == null) {
			if (other.isManual != null)
				return false;
		} else if (!isManual.equals(other.isManual))
			return false;
		return true;
	}

	
	
	
}
