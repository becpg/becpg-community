/*******************************************************************************
 * Copyright (C) 2010-2018 beCPG. 
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
package fr.becpg.repo.report.engine;

import java.io.OutputStream;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;

import fr.becpg.repo.report.entity.EntityReportData;
import fr.becpg.report.client.ReportException;
import fr.becpg.report.client.ReportFormat;

public interface BeCPGReportEngine {
	
	public static final String PARAM_DOCUMENT_NODEREF = "documentNodeRef";
	public static final String PARAM_ENTITY_NODEREF = "entityNodeRef";


	void createReport(NodeRef tplNodeRef,EntityReportData reportData, OutputStream out, Map<String, Object> params) throws ReportException;

	boolean isApplicable(NodeRef templateNodeRef, ReportFormat reportFormat);
	
	boolean isXmlEngine();

}
