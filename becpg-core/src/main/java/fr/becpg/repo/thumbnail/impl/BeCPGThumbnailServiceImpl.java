package fr.becpg.repo.thumbnail.impl;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.site.SiteModel;
import org.alfresco.repo.thumbnail.ThumbnailServiceImpl;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.search.ResultSet;
import org.alfresco.service.cmr.search.SearchService;
import org.alfresco.service.cmr.thumbnail.ThumbnailService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import fr.becpg.repo.helper.TranslateHelper;
import fr.becpg.common.RepoConsts;
import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.entity.EntityService;
import fr.becpg.repo.thumbnail.BeCPGThumbnailService;

/**
 * Provide specific thumbnail for custom becpg types
 * 
 * @author "Matthieu Laborie <matthieu.laborie@becpg.fr>"
 * 
 */
public class BeCPGThumbnailServiceImpl extends ThumbnailServiceImpl implements
		BeCPGThumbnailService, ThumbnailService {

	public static String DOC_LIB_THUMBNAIL = "doclib";

	private static Log logger = LogFactory
			.getLog(BeCPGThumbnailServiceImpl.class);

	private NodeService nodeService;

	private DictionaryService dictionaryService;

	private EntityService entityService;

	private SearchService searchService;

	private Map<String, NodeRef> cachedThumbs = new HashMap<String, NodeRef>();

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

	public void setSearchService(SearchService searchService) {
		this.searchService = searchService;
	}

	@Override
	public NodeRef render(NodeRef sourceNodeRef) {
		QName type = nodeService.getType(sourceNodeRef);
		// Try to find a logo for the specific type
		String imgName;

		if (!ContentModel.TYPE_CONTENT.equals(type)
				&& !ContentModel.TYPE_FOLDER.equals(type)
				&& !ContentModel.TYPE_CATEGORY.equals(type)
				&& !ContentModel.TYPE_AUTHORITY.equals(type)
				&& !ContentModel.TYPE_PERSON.equals(type)
				&& !ContentModel.TYPE_USER.equals(type)
				&& !SiteModel.TYPE_SITE.equals(type)
				&& !SiteModel.TYPE_SITES.equals(type)) {

			if (dictionaryService.isSubClass(type, BeCPGModel.TYPE_CLIENT)
					|| dictionaryService.isSubClass(type,
							BeCPGModel.TYPE_SUPPLIER)
					|| dictionaryService.isSubClass(type,
							BeCPGModel.TYPE_PRODUCT)) {
				imgName = TranslateHelper.getTranslatedPath(
						RepoConsts.PATH_LOGO_IMAGE).toLowerCase();
				//boolean isContaintReport = false;
				if (dictionaryService.isSubClass(type, BeCPGModel.TYPE_PRODUCT)) {
					imgName = TranslateHelper.getTranslatedPath(
							RepoConsts.PATH_PRODUCT_IMAGE).toLowerCase();
					//isContaintReport = true;
				}

				logger.debug("Look for product thumbnail: " + imgName);
				NodeRef img = entityService.getImage(sourceNodeRef, imgName);
				if (img != null) {
					return super.getThumbnailByName(img,
							ContentModel.PROP_CONTENT, DOC_LIB_THUMBNAIL);
				}
//				if (isContaintReport) {
//					return null;
//				}
				
			}
			imgName = "generic-" + type.getLocalName() + "-thumb.png";
			return getImage(imgName);
		}
		return null;

	}

	private NodeRef getImage(String imgName) {
		NodeRef ret = null;
		if (cachedThumbs.containsKey(imgName)) {
			ret = cachedThumbs.get(imgName);
			if (ret == null || !nodeService.exists(ret)) {
				cachedThumbs.remove(imgName);
			} else {
				return ret;
			}
		}

		String query = String.format(RepoConsts.PATH_QUERY_THUMBNAIL, imgName);

		ResultSet resultSet = searchService.query(RepoConsts.SPACES_STORE,
				SearchService.LANGUAGE_LUCENE, query);

		if (logger.isDebugEnabled()) {
			logger.debug("Look for thumbnail : " + query);
			logger.debug("Found  : " + resultSet.getNodeRefs().size()
					+ " results");
		}

		if (resultSet.getNodeRefs().size() != 1) {
			logger.debug("image not found. imgName: " + imgName);
			return null;
		}

		ret = resultSet.getNodeRef(0);

		cachedThumbs.put(imgName, ret);
		return ret;

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
