package fr.becpg.repo.entity.version;

import java.util.Date;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.VersionType;

/**
 * <p>EntityVersionPlugin interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface EntityVersionPlugin {

	/** Constant <code>POST_UPDATE_HISTORY_NODEREF="postUpdateHistoryNodeRef"</code> */
	public final String POST_UPDATE_HISTORY_NODEREF = "postUpdateHistoryNodeRef";

	/**
	 * <p>doAfterCheckout.</p>
	 *
	 * @param origNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param workingCopyNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	void doAfterCheckout(NodeRef origNodeRef, NodeRef workingCopyNodeRef);

	/**
	 * <p>doBeforeCheckin.</p>
	 *
	 * @param origNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param workingCopyNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	void doBeforeCheckin(NodeRef origNodeRef, NodeRef workingCopyNodeRef);

	/**
	 * <p>cancelCheckout.</p>
	 *
	 * @param origNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param workingCopyNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	void cancelCheckout(NodeRef origNodeRef, NodeRef workingCopyNodeRef);

	/**
	 * <p>impactWUsed.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param versionType a {@link org.alfresco.service.cmr.version.VersionType} object.
	 * @param description a {@link java.lang.String} object.
	 * @param effectiveDate a {@link java.util.Date} object
	 */
	void impactWUsed(NodeRef entityNodeRef, VersionType versionType, String description, Date effectiveDate);
}
