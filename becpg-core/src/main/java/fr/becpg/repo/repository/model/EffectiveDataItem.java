package fr.becpg.repo.repository.model;

import java.util.Date;

public interface EffectiveDataItem {


	public Date getStartEffectivity();

	public void setStartEffectivity(Date startEffectivity);
	
	public Date getEndEffectivity();

	public void setEndEffectivity(Date endEffectivity);
	
}
