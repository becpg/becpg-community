package fr.becpg.repo.quality;

import org.alfresco.service.cmr.repository.NodeRef;

public interface QualityControlService {

	void createSamplingList(NodeRef qcNodeRef, NodeRef controlPlanNodeRef);
}
