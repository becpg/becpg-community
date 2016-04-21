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
 * You should have received a copy of the GNU Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package fr.becpg.repo.jscript;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.jscript.BaseScopableProcessorExtension;
import org.alfresco.repo.jscript.ScriptNode;
import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.common.BeCPGException;
import fr.becpg.model.ReportModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.helper.AssociationService;
import fr.becpg.repo.report.entity.EntityReportService;
import fr.becpg.repo.search.BeCPGQueryBuilder;

public final class Thumbnail extends BaseScopableProcessorExtension {

	private static final String THUMB_CACHE_KEY_PREFIX = "thumbCache_";
	private static final String ICON_THUMBNAIL_NAME = "generic-%s-thumb.png";

	private static final Log logger = LogFactory.getLog(Thumbnail.class);

	private NodeService nodeService;

	private EntityService entityService;

	private EntityReportService entityReportService;

	private BeCPGCacheService beCPGCacheService;

	private ServiceRegistry serviceRegistry;

	private AssociationService associationService;

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setEntityService(EntityService entityService) {
		this.entityService = entityService;
	}

	public void setBeCPGCacheService(BeCPGCacheService beCPGCacheService) {
		this.beCPGCacheService = beCPGCacheService;
	}

	public void setEntityReportService(EntityReportService entityReportService) {
		this.entityReportService = entityReportService;
	}

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

	public void setAssociationService(AssociationService associationService) {
		this.associationService = associationService;
	}

	public ScriptNode getThumbnailNode(ScriptNode sourceNode) {

		QName type = nodeService.getType(sourceNode.getNodeRef());
		// Try to find a logo for the specific type

		NodeRef img = null;

		if (entityService.hasAssociatedImages(type)) {

			try {
				img = entityService.getEntityDefaultImage(sourceNode.getNodeRef());

			} catch (BeCPGException e) {
				logger.debug(e, e);
			}

			if (img == null) {
				img = getImage(String.format(ICON_THUMBNAIL_NAME, type.getLocalName()));
			}

			if (img == null) {
				img = getImage(String.format(ICON_THUMBNAIL_NAME, "entity"));
			}
		}

		return img != null ? new ScriptNode(img, serviceRegistry, getScope()) : sourceNode;

	}

	public ScriptNode getOrCreateImageNode(ScriptNode sourceNode) {
		QName type = nodeService.getType(sourceNode.getNodeRef());
		// Try to find a logo for the specific type

		NodeRef img = null;
		try {
			if (entityService.hasAssociatedImages(type)) {
				img = entityService.getEntityDefaultImage(sourceNode.getNodeRef());
			}
		} catch (BeCPGException e) {
			logger.debug(e, e);
		}

		if (img == null) {
			img = entityService.createDefaultImage(sourceNode.getNodeRef());
		}

		return img != null ? new ScriptNode(img, serviceRegistry, getScope()) : sourceNode;

	}

	public ScriptNode getReportNode(ScriptNode sourceNode) {
		NodeRef reportNodeRef;
		if (entityReportService.shouldGenerateReport(sourceNode.getNodeRef())) {
			logger.debug("getReportNode: Entity report is not up to date for " + sourceNode.getNodeRef());
			reportNodeRef = entityReportService.getSelectedReport(sourceNode.getNodeRef());
			if (reportNodeRef != null) {
				cleanThumbnails(reportNodeRef);
			}
			entityReportService.generateReport(sourceNode.getNodeRef());
		}

		reportNodeRef = entityReportService.getSelectedReport(sourceNode.getNodeRef());

		return reportNodeRef != null ? new ScriptNode(reportNodeRef, serviceRegistry, getScope()) : sourceNode;

	}

	public ScriptNode refreshReport(ScriptNode reportNode) {
		NodeRef reportNodeRef = reportNode.getNodeRef();

		List<NodeRef> entityNodeRefs = associationService.getSourcesAssocs(reportNodeRef, ReportModel.ASSOC_REPORTS);
		if (entityNodeRefs != null) {
			for (NodeRef entityNodeRef : entityNodeRefs) {
				if (entityReportService.shouldGenerateReport(entityNodeRef)) {
					logger.debug("refreshReport: Entity report is not up to date for " + entityNodeRef);
					cleanThumbnails(reportNodeRef);
					entityReportService.generateReport(entityNodeRef);
				}
				reportNodeRef = entityReportService.getSelectedReport(entityNodeRef);
			}
		}
		
		return reportNodeRef != null ? new ScriptNode(reportNodeRef, serviceRegistry, getScope()) :reportNode;

	}

	private void cleanThumbnails(NodeRef reportNodeRef) {

		// Ensure thumbnail is regenerated before preview
		NodeRef thumbNodeRef = serviceRegistry.getThumbnailService().getThumbnailByName(reportNodeRef, ContentModel.PROP_CONTENT, "webpreview");
		if (thumbNodeRef != null) {
			nodeService.deleteNode(thumbNodeRef);
		}

		thumbNodeRef = serviceRegistry.getThumbnailService().getThumbnailByName(reportNodeRef, ContentModel.PROP_CONTENT, "pdf");
		if (thumbNodeRef != null) {
			nodeService.deleteNode(thumbNodeRef);
		}

	}

	private NodeRef getImage(final String imgName) {

		return beCPGCacheService.getFromCache(Thumbnail.class.getName(), THUMB_CACHE_KEY_PREFIX + imgName, () -> {

			NodeRef imageNodeRef = BeCPGQueryBuilder.createQuery().selectNodeByPath(nodeService.getRootNode(RepoConsts.SPACES_STORE),
					"/app:company_home" + RepoConsts.FULL_PATH_THUMBNAIL + "/cm:" + imgName);

			if (imageNodeRef == null) {
				logger.debug("image not found. imgName: " + imgName);
			}

			return imageNodeRef;
		});

	}

}
