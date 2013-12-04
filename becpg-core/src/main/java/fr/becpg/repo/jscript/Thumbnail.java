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
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.cache.BeCPGCacheDataProviderCallBack;
import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.report.entity.EntityReportService;
import fr.becpg.repo.search.BeCPGSearchService;

public final class Thumbnail extends BaseScopableProcessorExtension {

	private static final String THUMB_CACHE_KEY_PREFIX = "thumbCache_";
	private static String ICON_THUMBNAIL_NAME = "generic-%s-thumb*";

	private static Log logger = LogFactory.getLog(Thumbnail.class);

	private NodeService nodeService;

	private EntityService entityService;

	private EntityReportService entityReportService;

	private BeCPGSearchService beCPGSearchService;

	private BeCPGCacheService beCPGCacheService;

	private ServiceRegistry serviceRegistry;

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setEntityService(EntityService entityService) {
		this.entityService = entityService;
	}

	public void setBeCPGSearchService(BeCPGSearchService beCPGSearchService) {
		this.beCPGSearchService = beCPGSearchService;
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
		}

		if (img == null) {
			img = getImage(String.format(ICON_THUMBNAIL_NAME, type.getLocalName()));
		}

		return img != null ? new ScriptNode(img, serviceRegistry, getScope()) : sourceNode;

	}

	public ScriptNode getReportNode(ScriptNode sourceNode) {
		NodeRef reportNodeRef = null;
		if (entityReportService.shouldGenerateReport(sourceNode.getNodeRef())) {
			logger.debug("Entity report is not up to date for " + sourceNode.getNodeRef());
			reportNodeRef = entityReportService.getSelectedReport(sourceNode.getNodeRef());
			if (reportNodeRef != null) {
				NodeRef thumbNodeRef = serviceRegistry.getThumbnailService().getThumbnailByName(reportNodeRef, ContentModel.PROP_CONTENT, "webpreview");
				if (thumbNodeRef != null) {
					// Ensure thumbnail is regenerated before preview
					nodeService.deleteNode(thumbNodeRef);
				}
			}
			entityReportService.generateReport(sourceNode.getNodeRef());

		}
		
		reportNodeRef = entityReportService.getSelectedReport(sourceNode.getNodeRef());
		

		return reportNodeRef != null ? new ScriptNode(reportNodeRef, serviceRegistry, getScope()) : sourceNode;

	}

	private NodeRef getImage(final String imgName) {

		return beCPGCacheService.getFromCache(Thumbnail.class.getName(), THUMB_CACHE_KEY_PREFIX + imgName, new BeCPGCacheDataProviderCallBack<NodeRef>() {

			@Override
			public NodeRef getData() {
				String query = String.format(RepoConsts.PATH_QUERY_THUMBNAIL, imgName);

				List<NodeRef> listItems = beCPGSearchService.luceneSearch(query, RepoConsts.MAX_RESULTS_SINGLE_VALUE);

				if (logger.isDebugEnabled()) {
					logger.debug("Look for thumbnail : " + query);
					logger.debug("Found  : " + listItems.size() + " results");
				}

				if (listItems.isEmpty()) {
					logger.debug("image not found. imgName: " + imgName);
					return null;
				}

				return listItems.get(0);
			}

		});

	}

}
