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
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.web.scripts.content.ContentGet;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.helper.AttachmentHelper;
import fr.becpg.repo.report.entity.EntityReportService;
import fr.becpg.report.client.ReportFormat;

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
	protected static final String PARAM_STORE_TYPE = "store_type";
	protected static final String PARAM_STORE_ID = "store_id";
	protected static final String PARAM_ID = "id";
	protected static final String PARAM_PROPERTY = "property";

	private EntityReportService entityReportService;

	private NamespaceService namespaceService;

	@Override
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	public void setEntityReportService(EntityReportService entityReportService) {
		this.entityReportService = entityReportService;
	}

	@Override
	public void execute(final WebScriptRequest req, final WebScriptResponse res) throws IOException {
		

		NodeRef nodeRef = null;

		// create map of template vars
		Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
		if (templateArgs != null) {
			String storeType = templateArgs.get(PARAM_STORE_TYPE);
			String storeId = templateArgs.get(PARAM_STORE_ID);
			String nodeId = templateArgs.get(PARAM_ID);
			if ((storeType != null) && (storeId != null) && (nodeId != null)) {
				nodeRef = new NodeRef(storeType, storeId, nodeId);
			}
		}
		if (nodeRef == null) {
			throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "No report provided");
		}
		
		String entityNodeRefParam = req.getParameter(PARAM_ENTITY_NODEREF);
		NodeRef entityNodeRef = null;
		if ((entityNodeRefParam != null) && !entityNodeRefParam.isEmpty()) {
			entityNodeRef = new NodeRef(entityNodeRefParam);
		} else {
			entityNodeRef = entityReportService.getEntityNodeRef(nodeRef);
		}
		

		if (entityNodeRef == null) {
			throw new WebScriptException(HttpServletResponse.SC_NOT_FOUND, "No entity provided");
		}

		nodeRef = entityReportService.getOrRefreshReport(entityNodeRef, nodeRef);
	

		// determine attachment
		boolean attach = Boolean.valueOf(req.getParameter("a"));

		String format = req.getParameter("format");

		if ((format != null) && attach) {

			ReportFormat reportFormat = ReportFormat.valueOf(format.toUpperCase());

			String name = (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
		
			String mimeType = mimetypeService.getMimetype(format);

			name = FilenameUtils.removeExtension(name) + FilenameUtils.EXTENSION_SEPARATOR_STR + mimetypeService.getExtension(mimeType);
			
			logger.debug("Rendering report at format :" + reportFormat.toString() + " mimetype: " + mimeType + " name " + name);

			entityReportService.generateReport(entityNodeRef, nodeRef, reportFormat, res.getOutputStream());

			res.setContentType(mimeType);
			AttachmentHelper.setAttachment(req, res, name);

			return;
		} else {

			// render content
			QName propertyQName = ContentModel.PROP_CONTENT;
			String contentPart = templateArgs.get(PARAM_PROPERTY);
			if ((contentPart.length() > 0) && (contentPart.charAt(0) == ';')) {
				if (contentPart.length() < 2) {
					throw new WebScriptException(HttpServletResponse.SC_BAD_REQUEST, "Content property malformed");
				}
				String propertyName = contentPart.substring(1);
				if (propertyName.length() > 0) {
					propertyQName = QName.createQName(propertyName, namespaceService);
				}
			}

			// Stream the content
			streamContentLocal(req, res, nodeRef, attach, propertyQName, null);
		}

	}

}