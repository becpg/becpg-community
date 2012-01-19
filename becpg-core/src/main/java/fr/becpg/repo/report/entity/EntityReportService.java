package fr.becpg.repo.report.entity;

import org.alfresco.service.cmr.repository.NodeRef;

public interface EntityReportService {

	
	/**
	 * Generate Entity Report
	 * @param entityNodeRef
	 */
	public void generateReport(NodeRef entityNodeRef);

	/**
	 * 
	 * @param tempNodeRef
	 * @return
	 */
	public byte[] getImage(NodeRef tempNodeRef);

	
	/**
	 * Check if the report is up to date
	 * @param sfNodeRef
	 * @return
	 */
	public boolean isReportUpToDate(NodeRef sfNodeRef);
		
}
