package fr.becpg.repo.entity.catalog;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

public interface EntityCatalogObserver {

	void notifyAuditedFieldChange(String string, NodeRef entityNodeRef);

	boolean accept(QName type, NodeRef entityNodeRef);

}
