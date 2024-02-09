/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG.
 *
 * This file is part of beCPG
 *
 * beCPG is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * beCPG is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.report.entity;

import java.io.OutputStream;
import java.util.List;
import java.util.Locale;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.report.client.ReportFormat;

/**
 * <p>EntityReportService interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface EntityReportService {

	public static final String REPORT_FORMULATION_CHAIN_ID = "ReportFormulationChainId";

	void generateReports(NodeRef nodeRefFrom, NodeRef nodeRefTo);
	
	/**
	 * <p>generateReports.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	void generateReports(NodeRef entityNodeRef);
	
	/**
	 * <p>generateReport.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param documentNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	void generateReport(NodeRef entityNodeRef, NodeRef documentNodeRef);
	
	/**
	 * <p>generateReport.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param documentNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param reportFormat a {@link fr.becpg.report.client.ReportFormat} object.
	 * @param outputStream a {@link java.io.OutputStream} object.
	 */
	void generateReport(NodeRef entityNodeRef, NodeRef documentNodeRef, ReportFormat reportFormat, OutputStream outputStream);
	
	/**
	 * <p>generateReport.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param templateNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param reportParameters a {@link fr.becpg.repo.report.entity.EntityReportParameters} object.
	 * @param locale a {@link java.util.Locale} object.
	 * @param reportFormat a {@link fr.becpg.report.client.ReportFormat} object.
	 * @param outputStream a {@link java.io.OutputStream} object.
	 */
	void generateReport(NodeRef entityNodeRef, NodeRef templateNodeRef, EntityReportParameters reportParameters, Locale locale,
			ReportFormat reportFormat, OutputStream outputStream);

	
	/**
	 * <p>getOrRefreshReport.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param documentNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef getOrRefreshReport(NodeRef entityNodeRef, NodeRef documentNodeRef);
	
	List<NodeRef> getOrRefreshReportsOfKind(NodeRef entityNodeRef, String reportType);
	
	/**
	 * <p>getXmlReportDataSource.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param locale a {@link java.util.Locale} object.
	 * @param reportParameters a {@link fr.becpg.repo.report.entity.EntityReportParameters} object.
	 * @return a {@link java.lang.String} object.
	 */
	String getXmlReportDataSource(NodeRef entityNodeRef, Locale locale, EntityReportParameters reportParameters);

	/**
	 * <p>setPermissions.</p>
	 *
	 * @param tplNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param documentNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	void setPermissions(NodeRef tplNodeRef, NodeRef documentNodeRef);

	/**
	 * <p>shouldGenerateReport.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param documentNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a boolean.
	 */
	boolean shouldGenerateReport(NodeRef entityNodeRef, NodeRef documentNodeRef);

	/**
	 * <p>getSelectedReport.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef getSelectedReport(NodeRef entityNodeRef);
	
	List<NodeRef> getReportsOfKind(NodeRef entityNodeRef, String reportType);

	/**
	 * <p>getSelectedReportName.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link java.lang.String} object.
	 */
	String getSelectedReportName(NodeRef entityNodeRef);

	/**
	 * <p>retrieveExtractor.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link fr.becpg.repo.report.entity.EntityReportExtractorPlugin} object.
	 */
	EntityReportExtractorPlugin retrieveExtractor(NodeRef entityNodeRef);

	/**
	 * <p>getEntityNodeRef.</p>
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef getEntityNodeRef(NodeRef nodeRef);

	/**
	 * <p>getAssociatedDocumentNodeRef.</p>
	 *
	 * @param entityNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param tplNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param reportParameters a {@link fr.becpg.repo.report.entity.EntityReportParameters} object.
	 * @param locale a {@link java.util.Locale} object.
	 * @param reportFormat a {@link fr.becpg.report.client.ReportFormat} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef getAssociatedDocumentNodeRef(NodeRef entityNodeRef, NodeRef tplNodeRef, EntityReportParameters reportParameters, Locale locale, ReportFormat reportFormat);

	void generateReports(NodeRef nodeRefFrom, boolean generateAllReports);


	
}
