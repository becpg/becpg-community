package fr.becpg.repo.designer;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * 
 * @author "Matthieu Laborie <matthieu.laborie@becpg.fr>"
 * 
 */
public interface DesignerInitService {
	

	public static final String PATH_CONFIGS = "configs";

	
	/**
	 * Designer common folders 
	 */
	public NodeRef getWorkflowsNodeRef();

	public NodeRef getModelsNodeRef();

	public NodeRef getConfigsNodeRef();

	/**
	 * Add a Readonly model to designer 
	 * @param pattern
	 */
	public void addReadOnlyDesignerFiles(String pattern);
	


}
