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
package fr.becpg.repo.web.scripts.entity;

import java.io.IOException;
import java.net.SocketException;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.ContentIOException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.version.Version;
import org.alfresco.service.cmr.version.VersionHistory;
import org.alfresco.service.cmr.version.VersionService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Status;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;

import fr.becpg.repo.entity.comparison.CompareEntityReportService;
import fr.becpg.repo.entity.version.EntityVersionService;

/**
 * The Class CompareEntityDataSourceWebscript.
 *
 * @author querephi, matthieu, kevin
 * @version $Id: $Id
 */
public class CompareEntityDataSourceWebScript extends AbstractWebScript {

	private static final int MAX_ENTITIES = 10;
	private static final String PARAM_ENTITY = "entity";

	private static final String PARAM_STORE_TYPE = "store_type";
	private static final String PARAM_STORE_ID = "store_id";
	private static final String PARAM_ID = "id";

	private static final String PARAM_ENTITIES = "entities";

	private static final String PARAM_VERSION_LABEL = "versionLabel";

	private static final Log logger = LogFactory.getLog(CompareEntityDataSourceWebScript.class);

	@Autowired
	private CompareEntityReportService compareEntityReportService;

	@Autowired
	private VersionService versionService;

	@Autowired
	private EntityVersionService entityVersionService;

	@Autowired
	private NodeService nodeService;

	/**
	 * <p>Setter for the field <code>compareEntityReportService</code>.</p>
	 *
	 * @param compareEntityReportService a {@link fr.becpg.repo.entity.comparison.CompareEntityReportService} object.
	 */
	public void setCompareEntityReportService(CompareEntityReportService compareEntityReportService) {
		this.compareEntityReportService = compareEntityReportService;
	}
	
	/**
	 * <p>Setter for the field <code>versionService</code>.</p>
	 *
	 * @param versionService a {@link org.alfresco.service.cmr.version.VersionService} object.
	 */
	public void setVersionService(VersionService versionService) {
		this.versionService = versionService;
	}

	/**
	 * <p>Setter for the field <code>entityVersionService</code>.</p>
	 *
	 * @param entityVersionService a {@link fr.becpg.repo.entity.version.EntityVersionService} object.
	 */
	public void setEntityVersionService(EntityVersionService entityVersionService) {
		this.entityVersionService = entityVersionService;
	}

	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/** {@inheritDoc} */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {
		List<NodeRef> entityNodeRefs = new LinkedList<>();

		NodeRef entityNodeRef = null;
		
		String entityNodeRefParam = req.getParameter(PARAM_ENTITY);
		if (entityNodeRefParam != null) {
			entityNodeRef = new NodeRef(entityNodeRefParam);
		}

		Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
		if (templateArgs != null) {
			String storeType = templateArgs.get(PARAM_STORE_TYPE);
			String storeId = templateArgs.get(PARAM_STORE_ID);
			String nodeId = templateArgs.get(PARAM_ID);
			if ((storeType != null) && (storeId != null) && (nodeId != null)) {
				entityNodeRef = new NodeRef(storeType, storeId, nodeId);
			}
		}

		NodeRef entity1NodeRef = null;

		String versionLabel = templateArgs.get(PARAM_VERSION_LABEL);
		if (versionLabel != null) {
			VersionHistory versionHistory = versionService.getVersionHistory(entityNodeRef);
			if (versionHistory != null) {
				Version version = versionHistory.getVersion(versionLabel);
				entity1NodeRef = entityVersionService.getEntityVersion(version);
			} else {
				entity1NodeRef = entityNodeRef;
			}

			if (logger.isDebugEnabled()) {
				logger.debug("entityNodeRef: " + entityNodeRef + " - versionLabel: " + versionLabel + " - entityVersionNodeRef: " + entityNodeRef);
			}

			entityNodeRefs.add(entityNodeRef);

		} else {
			entity1NodeRef = entityNodeRef;
			String entities = req.getParameter(PARAM_ENTITIES);
			if ((entities != null) && !entities.isEmpty()) {
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

			if (entityNodeRefs.isEmpty()) {
				entityNodeRefs.addAll(entityVersionService.getAllVersionBranches(entity1NodeRef));
			} else {
				entityNodeRefs.sort((e1, e2) -> {
					Date d1 = (Date) nodeService.getProperty(e1, org.alfresco.model.ContentModel.PROP_CREATED);
					Date d2 = (Date) nodeService.getProperty(e2, org.alfresco.model.ContentModel.PROP_CREATED);
					return (d1 == d2) ? 0 : d1 == null ? -1 : d1.compareTo(d2);
				});
			}
		}

		if (entity1NodeRef == null) {
			throw new WebScriptException(Status.STATUS_BAD_REQUEST, "missing parameters. entity");
		}

		try {
			//Ensure not comparing itselfs
			entityNodeRefs.remove(entity1NodeRef);

			if (logger.isDebugEnabled()) {
				logger.debug("entity1NodeRef : " + entity1NodeRef);
				logger.debug("entityNodeRefs : " + entityNodeRefs);
			}
			
			res.setContentType("application/xml");
			res.setContentEncoding("UTF-8");
			res.getWriter().write(compareEntityReportService.getXmlReportDataSource(entityNodeRef, entityNodeRefs));
		} catch (SocketException | ContentIOException e1) {
			// the client cut the connection - our mission was accomplished
			// apart from a little error message
			if (logger.isInfoEnabled()) {
				logger.info("Client aborted stream read:\n\tcontent", e1);
			}
		}

	}
}
