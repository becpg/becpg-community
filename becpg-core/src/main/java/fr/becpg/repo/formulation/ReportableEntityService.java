package fr.becpg.repo.formulation;

import java.util.Set;

import org.alfresco.service.cmr.repository.NodeRef;

public interface ReportableEntityService {

	void postEntityErrors(NodeRef entityNodeRef, String formulationChainId, Set<ReportableError> errors);
	
}
