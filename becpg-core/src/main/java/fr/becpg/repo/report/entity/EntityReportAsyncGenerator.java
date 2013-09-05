package fr.becpg.repo.report.entity;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

public interface EntityReportAsyncGenerator {

	void queueNodes(List<NodeRef> pendingNodes);

}
