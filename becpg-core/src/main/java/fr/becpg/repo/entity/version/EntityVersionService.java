/*
 * 
 */
package fr.becpg.repo.entity.version;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionType;

/**
 * The Interface EntityVersionService.
 *
 * @author querephi
 * @version $Id: $Id
 */
public interface EntityVersionService {

	/**
	 * <p>createVersionAndCheckin.</p>
	 *
	 * @param origNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param workingCopyNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param versionProperties a {@link java.util.Map} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef createVersionAndCheckin(NodeRef origNodeRef, NodeRef workingCopyNodeRef, Map<String, Serializable> versionProperties);

	/**
	 * <p>doCheckOut.</p>
	 *
	 * @param origNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param workingCopyNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef doCheckOut(NodeRef origNodeRef, NodeRef workingCopyNodeRef);

	/**
	 * <p>cancelCheckOut.</p>
	 *
	 * @param origNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param workingCopyNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	void cancelCheckOut(NodeRef origNodeRef, NodeRef workingCopyNodeRef);

	/**
	 * <p>getEntityVersion.</p>
	 *
	 * @param version a {@link org.alfresco.service.cmr.version.Version} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef getEntityVersion(Version version);

	/**
	 * <p>deleteVersionHistory.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	void deleteVersionHistory(NodeRef entityNodeRef);

	/**
	 * <p>getEntitiesHistoryFolder.</p>
	 *
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef getEntitiesHistoryFolder();

	/**
	 * <p>getAllVersions.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link java.util.List} object.
	 */
	List<EntityVersion> getAllVersions(NodeRef entityNodeRef);

	/**
	 * <p>getVersionHistoryNodeRef.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef getVersionHistoryNodeRef(NodeRef entityNodeRef);

	/**
	 * <p>buildVersionHistory.</p>
	 *
	 * @param versionHistoryRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link java.util.List} object.
	 */
	List<NodeRef> buildVersionHistory(NodeRef versionHistoryRef, NodeRef nodeRef);

	/**
	 * <p>getAllVersionAndBranches.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link java.util.List} object.
	 */
	List<EntityVersion> getAllVersionAndBranches(NodeRef entityNodeRef);

	/**
	 * <p>getAllVersionBranches.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link java.util.List} object.
	 */
	List<NodeRef> getAllVersionBranches(NodeRef entityNodeRef);

	/**
	 * <p>createVersion.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param versionProperties a {@link java.util.Map} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef createVersion(NodeRef entityNodeRef, Map<String, Serializable> versionProperties);

	/**
	 * <p>createInitialVersion.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	void createInitialVersion(NodeRef entityNodeRef);

	/**
	 * <p>afterCancelCheckOut.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	void afterCancelCheckOut(NodeRef entityNodeRef);

	/**
	 * <p>createBranch.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param parentRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef createBranch(NodeRef entityNodeRef, NodeRef parentRef);

	/**
	 * <p>deleteEntityVersion.</p>
	 *
	 * @param version a {@link org.alfresco.service.cmr.version.Version} object.
	 */
	void deleteEntityVersion(Version version);

	/**
	 * <p>impactWUsed.</p>
	 *
	 * @param newEntityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param versionType a {@link org.alfresco.service.cmr.version.VersionType} object.
	 * @param description a {@link java.lang.String} object.
	 */
	void impactWUsed(NodeRef newEntityNodeRef, VersionType versionType, String description);


	/**
	 * <p>mergeBranch.</p>
	 *
	 * @param branchNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param branchToNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param versionType a {@link org.alfresco.service.cmr.version.VersionType} object.
	 * @param description a {@link java.lang.String} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef mergeBranch(NodeRef branchNodeRef, NodeRef branchToNodeRef, VersionType versionType, String description);
	
	/**
	 * <p>mergeBranch.</p>
	 *
	 * @param branchNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param branchToNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param versionType a {@link org.alfresco.service.cmr.version.VersionType} object.
	 * @param description a {@link java.lang.String} object.
	 * @param impactWused a boolean.
	 * @param rename a boolean.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef mergeBranch(NodeRef branchNodeRef, NodeRef branchToNodeRef, VersionType versionType, String description, boolean impactWused, boolean rename);

	/**
	 * <p>mergeBranch.</p>
	 *
	 * @param branchNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef mergeBranch(NodeRef branchNodeRef);


	/**
	 * <p>updateLastVersionLabel.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param versionLabel a {@link java.lang.String} object.
	 */
	void updateLastVersionLabel(NodeRef entityNodeRef, String versionLabel);

	boolean isVersion(NodeRef entity1);

	NodeRef extractVersion(NodeRef entity1);
	
	boolean isV2Service();

	NodeRef getVersionedNodeRef(String id);

}
