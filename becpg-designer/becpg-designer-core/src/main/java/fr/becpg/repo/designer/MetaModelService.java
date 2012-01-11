package fr.becpg.repo.designer;

import org.alfresco.repo.dictionary.M2Model;

/**
 * This service is used to read metaModel and write it from xml
 * @author "Matthieu Laborie <matthieu.laborie@becpg.fr>"
 *
 */
public interface MetaModelService {
	
	
	/**
	 * extract M2Model from m2:model node
	 * @param nodeRef
	 * @return
	 */
	M2Model extractM2Model(String nodeRef);
	
	/**
	 * create nodeRef form M2Model
	 * @param model
	 * @return
	 */
	String createModelNodeRef(M2Model model);
	
	

}
