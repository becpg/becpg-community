package fr.becpg.repo.report.entity;

import org.alfresco.service.cmr.repository.NodeRef;

public interface EntityReportService {

	
	/**
	 * Generate Entity Report
	 * @param entityNodeRef
	 */
	public void generateReport(NodeRef entityNodeRef);

	/**
	 * Check if the report is up to date
	 * @param sfNodeRef
	 * @return
	 */
	public boolean isReportUpToDate(NodeRef sfNodeRef);

	/**
	 * Register new extractor
	 * @param typeName
	 * @param extractor
	 */
	public void registerExtractor(String typeName, EntityReportExtractor extractor);
		
}
