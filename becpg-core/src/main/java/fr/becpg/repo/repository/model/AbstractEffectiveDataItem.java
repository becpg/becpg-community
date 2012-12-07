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

	
}
