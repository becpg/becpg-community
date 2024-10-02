package fr.becpg.repo.toxicology;

import org.alfresco.service.cmr.repository.NodeRef;

public interface ToxicologyService {

	NodeRef createOrUpdateToxIngNodeRef(NodeRef ingNodeRef, NodeRef toxNodeRef);

	Double computeMaxValue(NodeRef ingNodeRef, NodeRef toxNodeRef);

}
