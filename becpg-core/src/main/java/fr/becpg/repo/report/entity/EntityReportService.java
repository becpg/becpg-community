package fr.becpg.repo.report.entity;

import org.alfresco.service.cmr.repository.NodeRef;

public interface EntityReportService {

	void registerExtractor(String typeName, EntityReportExtractor extractor);

	void generateReport(NodeRef entityNodeRef);

	String getXmlReportDataSource(NodeRef entityNodeRef);

	void setPermissions(NodeRef tplNodeRef, NodeRef documentNodeRef);

	boolean shouldGenerateReport(NodeRef entityNodeRef);

	NodeRef getSelectedReport(NodeRef entityNodeRef);

	String getSelectedReportName(NodeRef entityNodeRef);
}
