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
package fr.becpg.repo.report.template;

import java.io.IOException;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;

import fr.becpg.report.client.ReportFormat;

/**
 * <p>ReportTplService interface.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public interface ReportTplService {

	/** Constant <code>PARAM_VALUE_DESIGN_EXTENSION=".rptdesign"</code> */
	String PARAM_VALUE_DESIGN_EXTENSION = ".rptdesign";
	/** Constant <code>PARAM_VALUE_XLSXREPORT_EXTENSION=".xlsx"</code> */
	String PARAM_VALUE_XLSXREPORT_EXTENSION = ".xlsx";
	/** Constant <code>PARAM_VALUE_XMLREPORT_EXTENSION=".xml"</code> */
	String PARAM_VALUE_XMLREPORT_EXTENSION = ".xml";
	/** Constant <code>PARAM_VALUE_XLSMREPORT_EXTENSION=".xlsm"</code> */
	String PARAM_VALUE_XLSMREPORT_EXTENSION =  ".xlsm";
	
	
	/**
	 * Gets the system report templates.
	 *
	 * @param nodeType
	 * the system report templates
	 * @param reportType a {@link fr.becpg.repo.report.template.ReportType} object.
	 * @return a {@link java.util.List} object.
	 */
	List<NodeRef> getSystemReportTemplates(ReportType reportType, QName nodeType);
	
	/**
	 * Gets the system report template.
	 *
	 * @param reportType a {@link fr.becpg.repo.report.template.ReportType} object.
	 * @param nodeType a {@link org.alfresco.service.namespace.QName} object.
	 * @param tplName a {@link java.lang.String} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef getSystemReportTemplate(ReportType reportType, QName nodeType, String tplName);
	
	/**
	 * Get the user template by name
	 *
	 * @param reportType a {@link fr.becpg.repo.report.template.ReportType} object.
	 * @param nodeType a {@link org.alfresco.service.namespace.QName} object.
	 * @param tplName a {@link java.lang.String} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef getUserReportTemplate(ReportType reportType, QName nodeType, String tplName);
	
	/**
	 * Gets the user report templates.
	 *
	 * @param nodeType the node type
	 * @param tplName the tpl name
	 * the user report templates
	 * @param reportType a {@link fr.becpg.repo.report.template.ReportType} object.
	 * @return a {@link java.util.List} object.
	 */
	List<NodeRef> getUserReportTemplates(ReportType reportType, QName nodeType, String tplName);
	
	/**
	 * Create the rptdesign node for the report
	 *
	 * @param parentNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param tplName a {@link java.lang.String} object.
	 * @param tplFilePath a {@link java.lang.String} object.
	 * @param overrideTpl a boolean.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @throws java.io.IOException if any.
	 * @param tplInformation a {@link fr.becpg.repo.report.template.ReportTplInformation} object
	 */
	NodeRef createTplRptDesign(NodeRef parentNodeRef, String tplName, String tplFilePath, ReportTplInformation tplInformation, boolean overrideTpl) throws IOException;
	
	/**
	 * Create a resource for the report
	 *
	 * @param parentNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @param xmlFilePath a {@link java.lang.String} object.
	 * @param overrideRessource a boolean.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @throws java.io.IOException if any.
	 */
	NodeRef createTplRessource(NodeRef parentNodeRef, String xmlFilePath, boolean overrideRessource) throws IOException;
	
	/**
	 * Check the default reports (return one default tpl)
	 * if there is a user default tpl, remove system default tpl and keep user one
	 *
	 * @param tplsNodeRef a {@link java.util.List} object.
	 * @return a {@link java.util.List} object.
	 */
	List<NodeRef> cleanDefaultTpls(List<NodeRef> tplsNodeRef);
	
	/**
	 * Get the report format
	 *
	 * @param tplNodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link fr.becpg.report.client.ReportFormat} object.
	 */
	ReportFormat getReportFormat(NodeRef tplNodeRef);

	/**
	 * Get the template associated to the report
	 *
	 * @param nodeRef a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef getAssociatedReportTemplate(NodeRef nodeRef);

	/**
	 * <p>getDefaultReportTemplate.</p>
	 *
	 * @param reportType a {@link fr.becpg.repo.report.template.ReportType} object.
	 * @param nodeType a {@link org.alfresco.service.namespace.QName} object.
	 * @return a {@link org.alfresco.service.cmr.repository.NodeRef} object.
	 */
	NodeRef getDefaultReportTemplate(ReportType reportType, QName nodeType);
}
