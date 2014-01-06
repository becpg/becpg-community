/*
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */
package fr.becpg.repo.web.scripts.report;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.repo.web.scripts.content.ContentGet;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.report.entity.EntityReportService;

/**
 * 
 * GET web script to get stream report content from the repository
 * 
 * @author matthieu
 * @since 1.5.c
 */
public class ReportContentGet extends ContentGet {
	private static final Log logger = LogFactory.getLog(ReportContentGet.class);

	private static final String PARAM_ENTITY_NODEREF = "entityNodeRef";

	private EntityReportService entityReportService;

	public void setEntityReportService(EntityReportService entityReportService) {
		this.entityReportService = entityReportService;
	}

	@Override
	public void execute(final WebScriptRequest req, final WebScriptResponse res) throws IOException {
		String entityNodeRefParam = req.getParameter(PARAM_ENTITY_NODEREF);
		NodeRef entityNodeRef = null;
		if (entityNodeRefParam != null && !entityNodeRefParam.isEmpty()) {
			entityNodeRef = new NodeRef(entityNodeRefParam);
		}

		if (entityNodeRef == null) {
			throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "No entity provided");
		}

		if (entityReportService.shouldGenerateReport(entityNodeRef)) {
			logger.debug("Entity report is not up to date for " + entityNodeRef);
			entityReportService.generateReport(entityNodeRef);
		}

		super.execute(req, res);

	}

}