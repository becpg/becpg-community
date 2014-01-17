/*
 * 
 */
package fr.becpg.repo.product.data.ing;

import java.util.Locale;

import fr.becpg.repo.repository.RepositoryEntity;


public interface LabelingComponent extends RepositoryEntity{
	
	public String getLegalName(Locale locale);
	
	public Double getQty();	
	
}
