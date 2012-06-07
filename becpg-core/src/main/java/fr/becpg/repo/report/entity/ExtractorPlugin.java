package fr.becpg.repo.report.entity;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class ExtractorPlugin {
	
	private static Log logger = LogFactory.getLog(ExtractorPlugin.class);
	
	EntityReportExtractor extractor;
	
	EntityReportService entityReportService;
	
	String typeName;

	/**
	 * @param extractor the extractor to set
	 */
	public void setExtractor(EntityReportExtractor extractor) {
		this.extractor = extractor;
	}

	/**
	 * @param typeName the typeName to set
	 */
	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	/**
	 * @param entityReportService the entityReportService to set
	 */
	public void setEntityReportService(EntityReportService entityReportService) {
		this.entityReportService = entityReportService;
	}
	
	
	public void init(){
		logger.info("registerExtractor, typeName: " + typeName + " - extractor: " + extractor);
		entityReportService.registerExtractor(typeName, extractor);
	}
	
	

}
