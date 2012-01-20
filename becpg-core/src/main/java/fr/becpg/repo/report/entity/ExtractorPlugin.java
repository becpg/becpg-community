package fr.becpg.repo.report.entity;

public class ExtractorPlugin {
	
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
		entityReportService.registerExtractor(typeName, extractor);
	}
	
	

}
