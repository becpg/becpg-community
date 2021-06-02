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
 * You should have received a copy of the GNU Lesser General Public License along with beCPG.
 *  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.report.engine.impl;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.ContentReader;
import org.alfresco.service.cmr.repository.ContentService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.util.StopWatch;

import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.report.engine.BeCPGReportEngine;
import fr.becpg.repo.report.entity.EntityImageInfo;
import fr.becpg.repo.report.entity.EntityReportData;
import fr.becpg.repo.report.template.ReportTplService;
import fr.becpg.report.client.AbstractBeCPGReportClient;
import fr.becpg.report.client.ReportException;
import fr.becpg.report.client.ReportFormat;
import fr.becpg.report.client.ReportParams;
import io.opencensus.common.Scope;
import io.opencensus.trace.Tracer;
import io.opencensus.trace.Tracing;

/**
 * beCPGReportServerClient used to interact with reporting server
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class ReportServerEngine extends AbstractBeCPGReportClient implements BeCPGReportEngine {

	private NodeService nodeService;

	private ContentService contentService;

	private EntityService entityService;
	
	private String instanceName;

	private static final Tracer tracer = Tracing.getTracer();

	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * <p>Setter for the field <code>contentService</code>.</p>
	 *
	 * @param contentService a {@link org.alfresco.service.cmr.repository.ContentService} object.
	 */
	public void setContentService(ContentService contentService) {
		this.contentService = contentService;
	}

	public void setEntityService(EntityService entityService) {
		this.entityService = entityService;
	}
	
	

	public void setInstanceName(String instanceName) {
		this.instanceName = instanceName;
	}

	/** {@inheritDoc} */
	@Override
	public boolean isApplicable(NodeRef templateNodeRef, ReportFormat reportFormat) {
		return ((String) nodeService.getProperty(templateNodeRef, ContentModel.PROP_NAME)).endsWith(ReportTplService.PARAM_VALUE_DESIGN_EXTENSION);
	}

	/** {@inheritDoc} */
	@Override
	public void createReport(NodeRef tplNodeRef, EntityReportData reportData, OutputStream out, Map<String, Object> params) throws ReportException {
		
		try (Scope scope = tracer.spanBuilder("reportEngine.Create").startScopedSpan()) {
			
			StopWatch watch = null;
			if (logger.isDebugEnabled()) {
				watch = new StopWatch();
				watch.start();
			}
			
			final ReportFormat format = (ReportFormat) params.get(ReportParams.PARAM_FORMAT);
			
			if (format == null) {
				throw new IllegalArgumentException("Format is a mandatory param");
			}
			
			executeInSession(reportSession -> {
				
				String templateId = (instanceName!=null? instanceName:"") + tplNodeRef.toString();
				
				tracer.getCurrentSpan().addAnnotation("sendReportTpl");
				
				sendTplFile(reportSession, templateId, tplNodeRef);
				
				@SuppressWarnings("unchecked")
				List<NodeRef> associatedTplFiles = (List<NodeRef>) params.get(ReportParams.PARAM_ASSOCIATED_TPL_FILES);
				
				if (associatedTplFiles != null) {
					for (NodeRef nodeRef : associatedTplFiles) {
						String assocFileId = getAssociatedTplFileId(templateId, (String) nodeService.getProperty(nodeRef, ContentModel.PROP_NAME));
						sendTplFile(reportSession, assocFileId, nodeRef);
					}
				}
				
				reportSession.setTemplateId(templateId);
				
				tracer.getCurrentSpan().addAnnotation("sendReportImages");
				
				for (EntityImageInfo entry : reportData.getImages()) {
					byte[] imageBytes = entityService.getImage(entry.getImageNodeRef());
					if (imageBytes != null) {
						try (InputStream in = new ByteArrayInputStream(imageBytes)) {
							sendImage(reportSession, entry.getId(), in);
						}
					}
				}
				
				tracer.getCurrentSpan().addAnnotation("sendReportData");
				reportSession.setFormat(format.toString());
				reportSession.setLang((String) params.get(ReportParams.PARAM_LANG));
				try (InputStream in = new ByteArrayInputStream(reportData.getXmlDataSource().asXML().getBytes())) {
					generateReport(reportSession, in, out);
				}
			});
			
			if (logger.isDebugEnabled() && (watch != null)) {
				watch.stop();
				logger.debug(" Report generated by server in  " + watch.getTotalTimeSeconds() + " seconds ");
			}
		}


	}

	private String getAssociatedTplFileId(String templateId, String name) {
		return templateId + "-" + name;
	}

	private void sendTplFile(ReportSession reportSession, String templateId, NodeRef tplNodeRef) throws ReportException, IOException {

		Date dateModified = (Date) nodeService.getProperty(tplNodeRef, ContentModel.PROP_MODIFIED);
		// Timestamp or -1
		Long timeStamp = getTemplateTimeStamp(reportSession, templateId);

		if (timeStamp == null) {
			logger.error("Error accessing report server timeStamp is null");
			return;
		}

		logger.debug("Received timeStamp :" + timeStamp);

		if ((timeStamp < 0) || (timeStamp < dateModified.getTime())) {
			ContentReader reader = contentService.getReader(tplNodeRef, ContentModel.PROP_CONTENT);
			if (reader != null) {
				saveTemplate(reportSession, reader.getContentInputStream());
			}
		}

	}

	/** {@inheritDoc} */
	@Override
	public boolean isXmlEngine() {
		return true;
	}

}
