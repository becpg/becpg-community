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
 * You should have received a copy of the GNU Lesser General Public License along with beCPG.
 *  If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.web.scripts.entity;

import java.io.IOException;
import java.net.SocketException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.repo.webdav.WebDAVHelper;
import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.MimetypeService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.comparison.CompareEntityReportService;
import fr.becpg.repo.entity.version.EntityVersionService;
import fr.becpg.repo.helper.AttachmentHelper;
import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.repo.report.template.ReportTplService;
import fr.becpg.repo.report.template.ReportType;

/**
 * The Class CompareEntityVersionReportWebScript.
 * 
 * @author querephi, matthieu
 */
public class CompareEntityReportWebScript extends AbstractWebScript {

	private static final int MAX_ENTITIES = 10;
	private static final String PARAM_ENTITY = "entity";

	private static final String PARAM_STORE_TYPE = "store_type";
	private static final String PARAM_STORE_ID = "store_id";
	private static final String PARAM_ID = "id";

	private static final String PARAM_ENTITIES = "entities";

	private static final String PARAM_VERSION_LABEL = "versionLabel";

	private static final String PARAM_FILE_NAME = "fileName";

	private static final String PARAM_TPL_NODEREF = "tplNodeRef";

	private static final Log logger = LogFactory.getLog(CompareEntityReportWebScript.class);

	private CompareEntityReportService compareEntityReportService;

	private MimetypeService mimetypeService;

	private VersionService versionService;

	private EntityVersionService entityVersionService;

	private ReportTplService reportTplService;

	public void setCompareEntityReportService(CompareEntityReportService compareEntityReportService) {
		this.compareEntityReportService = compareEntityReportService;
	}


	public void setMimetypeService(MimetypeService mimetypeService) {
		this.mimetypeService = mimetypeService;
	}

	public void setVersionService(VersionService versionService) {
		this.versionService = versionService;
	}

	public void setEntityVersionService(EntityVersionService entityVersionService) {
		this.entityVersionService = entityVersionService;
	}

	public void setReportTplService(ReportTplService reportTplService) {
		this.reportTplService = reportTplService;
	}

	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
		List<NodeRef> entityNodeRefs = new LinkedList<>();

		NodeRef entityNodeRef = null;
		NodeRef templateNodeRef;

		Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
		if (templateArgs != null) {
			String storeType = templateArgs.get(PARAM_STORE_TYPE);
			String storeId = templateArgs.get(PARAM_STORE_ID);
			String nodeId = templateArgs.get(PARAM_ID);
			if (storeType != null && storeId != null && nodeId != null) {
				entityNodeRef = new NodeRef(storeType, storeId, nodeId);
			}
		}

		NodeRef entity1NodeRef = null;


		String versionLabel = templateArgs.get(PARAM_VERSION_LABEL);
		if (versionLabel != null) {
			VersionHistory versionHistory = versionService.getVersionHistory(entityNodeRef);
			if(versionHistory!=null){
				Version version = versionHistory.getVersion(versionLabel);
				entity1NodeRef = entityVersionService.getEntityVersion(version);
			} else {
				entity1NodeRef = entityNodeRef;
			}
			
			if (logger.isDebugEnabled()) {
				logger.debug("entityNodeRef: " + entityNodeRef + " - versionLabel: " + versionLabel + " - entityVersionNodeRef: " + entityNodeRef);
			}

			entityNodeRefs.add(entityNodeRef);

		}
		else{
			entity1NodeRef = entityNodeRef;
			String entities = req.getParameter(PARAM_ENTITIES);
			if (entities != null && !entities.isEmpty()) {
				for (String entity : entities.split(",")) {
					entityNodeRefs.add(new NodeRef(entity));
				}
			}

			if (entityNodeRefs.isEmpty()) {
				for (int i = 1; i <= MAX_ENTITIES; i++) {
					String entity = req.getParameter(PARAM_ENTITY + i);
					if (entity != null) {
						if (entity1NodeRef == null) {
							entity1NodeRef = new NodeRef(entity);
						} else {
							entityNodeRefs.add(new NodeRef(entity));
						}
					}
				}
			}
		}

		if (entity1NodeRef == null) {
			throw new WebScriptException(Status.STATUS_BAD_REQUEST, "missing parameters. entity");
		}

		// get the content and stream directly to the response output stream
		// assuming the repository is capable of streaming in chunks, this
		// should allow large files
		// to be streamed directly to the browser response stream.
		try {
			
			 //Ensure not comparing itselfs
             entityNodeRefs.remove(entity1NodeRef);

			if (req.getParameter(PARAM_TPL_NODEREF) != null) {
				templateNodeRef = new NodeRef(req.getParameter(PARAM_TPL_NODEREF));
			} else {
				templateNodeRef = reportTplService.getDefaultReportTemplate(ReportType.Compare, null);
			}
			

			String fileName = compareEntityReportService.getReportFileName(templateNodeRef, templateArgs.get(PARAM_FILE_NAME));
			
			
			if(logger.isDebugEnabled()){
				logger.debug("entity1NodeRef : " + entity1NodeRef);
				logger.debug("entityNodeRefs : " + entityNodeRefs);
			}			
			

			// set mimetype for the content and the character encoding + length
			// for the stream
			
			res.setContentType(mimetypeService.guessMimetype(fileName));
			AttachmentHelper.setAttachment(req, res, fileName);
			
			
			compareEntityReportService.getComparisonReport(entity1NodeRef, entityNodeRefs, templateNodeRef, res.getOutputStream());


		} catch (SocketException | ContentIOException e1) {
			// the client cut the connection - our mission was accomplished
			// apart from a little error message
			if (logger.isInfoEnabled()){
				logger.info("Client aborted stream read:\n\tcontent", e1);
			}
		}

	}

}
