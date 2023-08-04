package fr.becpg.repo.entity.catalog;

import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.springframework.stereotype.Service;

@Service
public class DoNothingEntityCatalogObserver implements EntityCatalogObserver {

	@Override
	public void notifyAuditedFieldChange(String string, NodeRef entityNodeRef) {
		//Do Nothing

	}

	@Override
	public boolean acceptCatalogEvents(QName type, NodeRef entityNodeRef, Set<NodeRef> listNodeRefs) {
		return false;
	}

}
