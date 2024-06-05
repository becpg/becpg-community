package fr.becpg.repo.signature;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

/**
 * <p>SignatureProjectService interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface SignatureProjectService {
	
	/**
	 * <p>prepareSignatureProject.</p>
	 *
	 * @param projectNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param documents a {@link java.util.List} object
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	NodeRef prepareSignatureProject(NodeRef projectNodeRef, List<NodeRef> documents);

	/**
	 * <p>createEntitySignatureTasks.</p>
	 *
	 * @param projectNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param previousTask a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param projectType a {@link java.lang.String} object
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	NodeRef createEntitySignatureTasks(NodeRef projectNodeRef, NodeRef previousTask, String projectType);

	/**
	 * <p>extractRecipients.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a {@link java.util.List} object
	 */
	List<NodeRef> extractRecipients(NodeRef nodeRef);
		
}
