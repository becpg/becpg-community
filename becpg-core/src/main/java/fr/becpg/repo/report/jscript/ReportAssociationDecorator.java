/*******************************************************************************
 * Copyright (C) 2010-2014 beCPG. 
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
package fr.becpg.repo.report.jscript;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.MessageFormat;
import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONAware;
import org.json.simple.JSONObject;

import fr.becpg.model.ReportModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.report.entity.EntityReportService;
import fr.becpg.repo.report.template.ReportTplService;

public class ReportAssociationDecorator extends fr.becpg.repo.jscript.app.BaseAssociationDecorator {
	private static Log logger = LogFactory.getLog(ReportAssociationDecorator.class);

	private ReportTplService reportTplService;

	private EntityReportService entityReportService;

	private final static String CONTENT_DOWNLOAD_API_URL = "becpg/report/node/content/{0}/{1}/{2}/{3}?entityNodeRef={4}";

	public void setReportTplService(ReportTplService reportTplService) {
		this.reportTplService = reportTplService;
	}

	public void setEntityReportService(EntityReportService entityReportService) {
		this.entityReportService = entityReportService;
	}

	@SuppressWarnings("unchecked")
	public JSONAware decorate(QName propertyName, NodeRef nodeRef, List<NodeRef> assocs) {
		JSONArray array = new JSONArray();

		if (assocs != null && !assocs.isEmpty()) {

			String reportName = entityReportService.getSelectedReportName(nodeRef);

			for (NodeRef obj : assocs) {
				if (permissionService.hasPermission(obj, "Read") == AccessStatus.ALLOWED) {
					try {
						JSONObject jsonObj = new JSONObject();

						String name = (String) this.nodeService.getProperty(obj, ContentModel.PROP_NAME);

						jsonObj.put("name", name);
						NodeRef reportTemplateNodeRef = reportTplService.getAssociatedReportTemplate(obj);
						if (reportTemplateNodeRef != null) {
							String templateName = (String) this.nodeService.getProperty(reportTemplateNodeRef, ContentModel.PROP_NAME);
							if (templateName.endsWith(RepoConsts.REPORT_EXTENSION_BIRT)) {
								templateName = templateName.replace("." + RepoConsts.REPORT_EXTENSION_BIRT, "");
							}
							jsonObj.put("templateName", templateName);
							jsonObj.put("isDefault", this.nodeService.getProperty(reportTemplateNodeRef, ReportModel.PROP_REPORT_TPL_IS_DEFAULT));
							if (templateName.equalsIgnoreCase(reportName)) {
								jsonObj.put("isSelected", true);
							} else {
								jsonObj.put("isSelected", false);
							}
						}

						jsonObj.put("nodeRef", obj.toString());

						try {
							String contentURL = MessageFormat.format(CONTENT_DOWNLOAD_API_URL, new Object[] { obj.getStoreRef().getProtocol(), obj.getStoreRef().getIdentifier(),
									obj.getId(), URLEncoder.encode(name, "UTF-8"), nodeRef.toString() });

							jsonObj.put("contentURL", contentURL);
						} catch (UnsupportedEncodingException e) {
							logger.error(e, e);
						}

						array.add(jsonObj);
					} catch (InvalidNodeRefException e) {
						logger.warn("Report with nodeRef " + obj.toString() + " does not exist.");
					}
				}
			}
		}

		return array;
	}

	@Override
	public QName getAspect() {
		return ReportModel.ASPECT_REPORT_ENTITY;
	}

}
