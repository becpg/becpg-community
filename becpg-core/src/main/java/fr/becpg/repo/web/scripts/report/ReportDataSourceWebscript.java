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
package fr.becpg.repo.web.scripts.report;

import java.io.IOException;
import java.util.Locale;

import org.alfresco.service.cmr.repository.NodeRef;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.report.entity.EntityReportParameters;
import fr.becpg.repo.report.entity.EntityReportService;

/**
 * <p>ReportDataSourceWebscript class.</p>
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class ReportDataSourceWebscript extends AbstractWebScript {

	private static final String PARAM_NODEREF = "nodeRef";
	private static final String PARAM_LOCALE = "locale";

	EntityReportService entityReportService;

	/**
	 * <p>Setter for the field <code>entityReportService</code>.</p>
	 *
	 * @param entityReportService
	 *            the entityReportService to set
	 */
	public void setEntityReportService(EntityReportService entityReportService) {
		this.entityReportService = entityReportService;
	}

	/** {@inheritDoc} */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

		NodeRef entityNodeRef = new NodeRef(req.getParameter(PARAM_NODEREF));
		
		Locale locale;
		String paramLocale = req.getParameter(PARAM_LOCALE);
		if (paramLocale == null) {
			locale = I18NUtil.getLocale();
		} else {
			locale = MLTextHelper.parseLocale(paramLocale);
		}


		EntityReportParameters reportParameters = null;
		JSONObject json = (JSONObject) req.parseContent();
		if (json != null) {
			reportParameters = EntityReportParameters.createFromJSON(json.toString());
		}
		
		
		
		res.setContentType("application/xml");
		res.setContentEncoding("UTF-8");
		res.getWriter().write(entityReportService.getXmlReportDataSource(entityNodeRef, locale, reportParameters));

	}

}
