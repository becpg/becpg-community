package fr.becpg.repo.copy;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

public interface CopyRestrictionPlugin {

	boolean shouldCopy(QName sourceClassQName, NodeRef sourceNodeRef, NodeRef targetNodeRef, String typeToReset);

}
