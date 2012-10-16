package fr.becpg.repo.product.data;

import java.util.Date;

public interface EffectiveDataItem {


	public Date getStartEffectivity();

	public void setStartEffectivity(Date startEffectivity);
	
	public Date getEndEffectivity();

	public void setEndEffectivity(Date endEffectivity);
	
}
