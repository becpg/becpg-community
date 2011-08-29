package fr.becpg.repo.quality;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.quality.data.WorkItemAnalysisData;

public interface WorkItemAnalysisDAO {

	public NodeRef create(NodeRef parentNodeRef, WorkItemAnalysisData wiaData);	
	public void update(NodeRef wiaNodeRef, WorkItemAnalysisData wiaData);		
	public WorkItemAnalysisData find(NodeRef wiaNodeRef);
	public void delete(NodeRef wiaNodeRef);
}
