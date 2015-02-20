/*
 * 
 */
package fr.becpg.repo.entity.version;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;

/**
 * The Interface EntityVersionService.
 *
 * @author querephi
 */
public interface EntityVersionService {

	NodeRef createVersionAndCheckin(NodeRef origNodeRef, NodeRef workingCopyNodeRef, Map<String, Serializable> versionProperties);

	NodeRef doCheckOut(NodeRef origNodeRef, NodeRef workingCopyNodeRef);

	void cancelCheckOut(NodeRef origNodeRef, NodeRef workingCopyNodeRef);

	NodeRef getEntityVersion(Version version);

	void deleteVersionHistory(NodeRef entityNodeRef);

	NodeRef getEntitiesHistoryFolder();

	List<EntityVersion> getAllVersions(NodeRef entityNodeRef);

	NodeRef getVersionHistoryNodeRef(NodeRef entityNodeRef);

	List<NodeRef> buildVersionHistory(NodeRef versionHistoryRef, NodeRef nodeRef);

	List<EntityVersion> getAllVersionAndBranches(NodeRef entityNodeRef);

	List<NodeRef> getAllVersionBranches(NodeRef entityNodeRef);

	void prepareBranchBeforeMerge(NodeRef nodeRef, NodeRef branchToNodeRef);

	NodeRef createVersion(NodeRef entityNodeRef, Map<String, Serializable> versionProperties);

	void createInitialVersion(NodeRef entityNodeRef);

	void afterCancelCheckOut(NodeRef entityNodeRef);

}