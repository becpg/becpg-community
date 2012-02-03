package fr.becpg.repo.report.entity;

import org.alfresco.service.cmr.repository.NodeRef;

public interface EntityReportService {

	

	/**
	 * Register new extractor
	 * @param typeName
	 * @param extractor
	 */
	public void registerExtractor(String typeName, EntityReportExtractor extractor);
		
	
	/**
	 * Generate Entity Report
	 * @param entityNodeRef
	 */
	public void generateReport(NodeRef entityNodeRef);

	
	/**
	 * Retrieve XML report dataSource 
	 * @param entityNodeRef
	 * @return
	 */
	public String getXmlReportDataSource(NodeRef entityNodeRef);

}
