package fr.becpg.repo.helper;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

public interface AssociationService {

	void update(NodeRef nodeRef, QName qName, List<NodeRef> assocNodeRefs);
	void update(NodeRef nodeRef, QName qName, NodeRef assocNodeRef);
}
