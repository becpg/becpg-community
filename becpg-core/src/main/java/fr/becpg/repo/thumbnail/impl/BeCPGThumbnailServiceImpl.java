package fr.becpg.repo.thumbnail.impl;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.thumbnail.ThumbnailServiceImpl;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.thumbnail.ThumbnailService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.common.BeCPGException;
import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.cache.BeCPGCacheDataProviderCallBack;
import fr.becpg.repo.cache.BeCPGCacheService;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.search.BeCPGSearchService;
import fr.becpg.repo.thumbnail.BeCPGThumbnailService;

/**
 * Provide specific thumbnail for custom becpg types
 * 
 * @author "Matthieu Laborie <matthieu.laborie@becpg.fr>"
 * 
 */
public class BeCPGThumbnailServiceImpl extends ThumbnailServiceImpl implements
		BeCPGThumbnailService, ThumbnailService {

	private static final String THUMB_CACHE_KEY_PREFIX = "thumbCache_";
	private static String DOC_LIB_THUMBNAIL = "doclib";
	private static String ICON_THUMBNAIL_NAME = "generic-%s-thumb*png";

	private static Log logger = LogFactory
			.getLog(BeCPGThumbnailServiceImpl.class);

	private NodeService nodeService;

	private DictionaryService dictionaryService;

	private EntityService entityService;

	private BeCPGSearchService beCPGSearchService;
	
	private BeCPGCacheService beCPGCacheService;

	// private Map<String, NodeRef> cachedThumbs = new HashMap<String, NodeRef>();

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
		super.setNodeService(nodeService);
	}

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
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

	@Override
	public NodeRef render(NodeRef sourceNodeRef) {
		QName type = nodeService.getType(sourceNodeRef);
		// Try to find a logo for the specific type

		if (!ContentModel.TYPE_CONTENT.equals(type)
				&& !ContentModel.TYPE_FOLDER.equals(type)
				&& !ContentModel.TYPE_CATEGORY.equals(type)
				&& !ContentModel.TYPE_AUTHORITY.equals(type)
				&& !ContentModel.TYPE_PERSON.equals(type)
				&& !ContentModel.TYPE_USER.equals(type)
				&& !SiteModel.TYPE_SITE.equals(type)
				&& !SiteModel.TYPE_SITES.equals(type)) {

			if (BeCPGModel.TYPE_CLIENT.isMatch(type) || 
					BeCPGModel.TYPE_SUPPLIER.isMatch(type) || 
					dictionaryService.isSubClass(type, BeCPGModel.TYPE_PRODUCT)) {		
				
				NodeRef img;
				try {
					img = entityService.getEntityDefaultImage(sourceNodeRef);
					if (img != null) {
					return super.getThumbnailByName(img,
								ContentModel.PROP_CONTENT, DOC_LIB_THUMBNAIL);
					}
				} catch (BeCPGException e) {
					logger.debug(e,e);
				}
			} 
			return getImage(String.format(ICON_THUMBNAIL_NAME, type.getLocalName()));
		}
		return null;

	}

	private NodeRef getImage(final String imgName) {
		
		return beCPGCacheService.getFromCache(BeCPGThumbnailService.class.getName(), THUMB_CACHE_KEY_PREFIX+imgName, new BeCPGCacheDataProviderCallBack<NodeRef>() {

			@Override
			public NodeRef getData() {
				String query = String.format(RepoConsts.PATH_QUERY_THUMBNAIL, imgName);

				List<NodeRef> listItems = beCPGSearchService.luceneSearch(query, RepoConsts.MAX_RESULTS_SINGLE_VALUE);

				if (logger.isDebugEnabled()) {
					logger.debug("Look for thumbnail : " + query);
					logger.debug("Found  : " + listItems.size()
							+ " results");
				}

				if (listItems.size() == 0) {
					logger.debug("image not found. imgName: " + imgName);
					return null;
				}

				return listItems.get(0);
			}
			
			
		});
		

	}

	@Override
	public NodeRef getThumbnailByName(NodeRef node, QName contentProperty,
			String thumbnailName) {
		NodeRef ret = null;
		if (DOC_LIB_THUMBNAIL.equals(thumbnailName)) {
			ret = render(node);
		}

		if (ret == null) {
			ret = super
					.getThumbnailByName(node, contentProperty, thumbnailName);
		}

		return ret;
	}

}
