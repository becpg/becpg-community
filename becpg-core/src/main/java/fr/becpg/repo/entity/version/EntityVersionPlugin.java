package fr.becpg.repo.entity.version;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.VersionType;

public interface EntityVersionPlugin {

	void doAfterCheckout(NodeRef origNodeRef, NodeRef workingCopyNodeRef);

	void doBeforeCheckin(NodeRef origNodeRef, NodeRef workingCopyNodeRef);

	void cancelCheckout(NodeRef origNodeRef, NodeRef workingCopyNodeRef);

	void impactWUsed(NodeRef entityNodeRef, VersionType versionType, String description);
}
