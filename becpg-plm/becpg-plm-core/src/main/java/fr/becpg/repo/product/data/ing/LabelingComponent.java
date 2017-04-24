/*
 * 
 */
package fr.becpg.repo.product.data.ing;

import java.util.Locale;
import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.repository.RepositoryEntity;


public interface LabelingComponent extends RepositoryEntity {
	
	String getLegalName(Locale locale);
	
	Double getQty();
	
	Double getVolume();
	
	Set<NodeRef> getAllergens();
	
	Set<NodeRef> getGeoOrigins();

}
