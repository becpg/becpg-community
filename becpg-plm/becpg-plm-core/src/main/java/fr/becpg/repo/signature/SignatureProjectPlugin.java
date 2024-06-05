package fr.becpg.repo.signature;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.project.data.ProjectData;

/**
 * <p>SignatureProjectPlugin interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface SignatureProjectPlugin {
	
	/**
	 * <p>prepareEntitySignatureFolder.</p>
	 *
	 * @param projectNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	NodeRef prepareEntitySignatureFolder(NodeRef projectNodeRef, NodeRef entityNodeRef);
	
	/**
	 * <p>getExternalSignatureFolder.</p>
	 *
	 * @param projectNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @param documents a {@link java.util.List} object
	 * @param viewRecipients a {@link java.util.List} object
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 */
	NodeRef getExternalSignatureFolder(NodeRef projectNodeRef, List<NodeRef> documents, List<NodeRef> viewRecipients);

	/**
	 * <p>createOrUpdateClosingTask.</p>
	 *
	 * @param project a {@link fr.becpg.repo.project.data.ProjectData} object
	 * @param lastsTasks a {@link java.util.List} object
	 */
	void createOrUpdateClosingTask(ProjectData project, List<NodeRef> lastsTasks);

	/**
	 * <p>applyTo.</p>
	 *
	 * @param projectType a {@link java.lang.String} object
	 * @return a boolean
	 */
	boolean applyTo(String projectType);

	/**
	 * <p>extractRecipients.</p>
	 *
	 * @param document a {@link org.alfresco.service.cmr.repository.NodeRef} object
	 * @return a {@link java.util.List} object
	 */
	List<NodeRef> extractRecipients(NodeRef document);

}
