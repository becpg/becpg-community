/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG. 
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
 * You should have received a copy of the GNU Lesser General Public License along with beCPG.
 *  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.web.scripts.report;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.DeclarativeWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptRequest;

import fr.becpg.repo.report.template.ReportTplService;
import fr.becpg.repo.report.template.ReportType;

/**
 * The Class ExportSearchTplsWebScript.
 *
 * @author querephi, matthieu
 */
public class ReportTplsWebScript extends DeclarativeWebScript {

	private static final String PARAM_DATATYPE = "datatype";
	private static final String PARAM_REPORT_TYPE = "type";
	private static final String MODEL_KEY_NAME_REPORT_TEMPLATES = "reportTpls";

	private NamespaceService namespaceService;

	private ReportTplService reportTplService;

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	public void setReportTplService(ReportTplService reportTplService) {
		this.reportTplService = reportTplService;
	}

	@Override
	protected Map<String, Object> executeImpl(WebScriptRequest req, Status status, Cache cache) {

		// get datatype
		Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
		String datatype = templateArgs.get(PARAM_DATATYPE);
		String reportType = templateArgs.get(PARAM_REPORT_TYPE);
		ReportType type = ReportType.ExportSearch;

		if (reportType != null && !reportType.isEmpty()) {
			for (ReportType tmp : ReportType.values()) {
				if (reportType.equalsIgnoreCase(tmp.toString())) {
					type = tmp;
					break;
				}
			}
		}

		QName datatypeQName = null;
		if (datatype != null && datatype.length() > 0) {
			datatypeQName = QName.createQName(datatype, namespaceService);
		}

		List<NodeRef> reportTpls = reportTplService.getUserReportTemplates(type, datatypeQName, null);
		Map<String, Object> model = new HashMap<String, Object>();
		model.put(MODEL_KEY_NAME_REPORT_TEMPLATES, reportTpls);

		return model;
	}
}
