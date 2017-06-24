/*******************************************************************************
 * Copyright (C) 2010-2017 beCPG. 
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

public interface ReportTplService {

	String PARAM_VALUE_DESIGN_EXTENSION = ".rptdesign";
	String PARAM_VALUE_XLSXREPORT_EXTENSION = ".xlsx";
	
	
	/**
	 * Gets the system report templates.
	 *
	 * @param nodeType
	 * @return the system report templates
	 */
	List<NodeRef> getSystemReportTemplates(ReportType reportType, QName nodeType);
	
	/**
	 * Gets the system report template.
	 * @param reportType
	 * @param nodeType
	 * @param tplName
	 * @return
	 */
	NodeRef getSystemReportTemplate(ReportType reportType, QName nodeType, String tplName);
	
	/**
	 * Get the user template by name
	 * @param reportType
	 * @param nodeType
	 * @param tplName
	 * @return
	 */
	NodeRef getUserReportTemplate(ReportType reportType, QName nodeType, String tplName);
	
	/**
	 * Gets the user report templates.
	 *
	 * @param nodeType the node type
	 * @param tplName the tpl name
	 * @return the user report templates
	 */
	List<NodeRef> getUserReportTemplates(ReportType reportType, QName nodeType, String tplName);
	
	/**
	 * Create the rptdesign node for the report
	 * @param parentNodeRef
	 * @param tplName
	 * @param tplFilePath
	 * @param reportType
	 * @param reportFormat
	 * @param nodeType
	 * @param isSystemTpl
	 * @param isDefaultTpl
	 * @param overrideTpl
	 * @return
	 * @throws IOException
	 */
	NodeRef createTplRptDesign(NodeRef parentNodeRef, String tplName, String tplFilePath, ReportType reportType, ReportFormat reportFormat, QName nodeType, boolean isSystemTpl, boolean isDefaultTpl, boolean overrideTpl) throws IOException;
	
	/**
	 * Create a resource for the report
	 * @param parentNodeRef
	 * @param xmlFilePath
	 * @param overrideRessource
	 * @throws IOException
	 * @return the created resource
	 */
	NodeRef createTplRessource(NodeRef parentNodeRef, String xmlFilePath, boolean overrideRessource) throws IOException;
	
	/**
	 * Check the default reports (return one default tpl)
	 * if there is a user default tpl, remove system default tpl and keep user one
	 * @param tplsNodeRef
	 * @return
	 */
	List<NodeRef> cleanDefaultTpls(List<NodeRef> tplsNodeRef);
	
	/**
	 * Get the report format
	 * @param tplNodeRef
	 * @return
	 */
	ReportFormat getReportFormat(NodeRef tplNodeRef);

	/**
	 * Get the template associated to the report
	 * @param nodeRef
	 * @return
	 */
	NodeRef getAssociatedReportTemplate(NodeRef nodeRef);
}
