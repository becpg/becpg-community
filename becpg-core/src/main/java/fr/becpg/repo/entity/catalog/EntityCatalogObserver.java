package fr.becpg.repo.entity.catalog;

import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

public interface EntityCatalogObserver {

	void notifyAuditedFieldChange(String string, NodeRef entityNodeRef);

	boolean acceptCatalogEvents(QName type, NodeRef entityNodeRef, Set<NodeRef> listNodeRefs);
	
}
