/*
 *
 */
package fr.becpg.repo.web.scripts.entity.datalist;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.util.StopWatch;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.DataListModel;
import fr.becpg.model.SystemGroup;
import fr.becpg.model.SystemState;
import fr.becpg.repo.entity.datalist.AsyncPaginatedExtractorWrapper;
import fr.becpg.repo.entity.datalist.DataListExtractor;
import fr.becpg.repo.entity.datalist.DataListExtractorFactory;
import fr.becpg.repo.entity.datalist.PaginatedExtractedItems;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.entity.datalist.impl.DataListOutputWriterFactory;
import fr.becpg.repo.helper.JsonHelper;
import fr.becpg.repo.helper.MLTextHelper;
import fr.becpg.repo.security.SecurityService;
import fr.becpg.repo.web.scripts.BrowserCacheHelper;
import fr.becpg.repo.web.scripts.WebscriptHelper;

/**
 * Webscript that send the result of a datalist
 *
 * @author matthieu
 * @version $Id: $Id
 */
public class EntityDataListWebScript extends AbstractWebScript {

	
	private static final Log logger = LogFactory.getLog(EntityDataListWebScript.class);

	/** The Constant PARAM_FILTER. */

	protected static final String PARAM_FILTER_ID = "filterId";

	/** Constant <code>PARAM_FILTER="filter"</code> */
	protected static final String PARAM_FILTER = "filter";

	/** Constant <code>PARAM_FILTER_DATA="filterData"</code> */
	protected static final String PARAM_FILTER_DATA = "filterData";

	/** Constant <code>PARAM_FILTER_PARAMS="filterParams"</code> */
	protected static final String PARAM_FILTER_PARAMS = "filterParams";

	/** Constant <code>PARAM_DATA_LIST_NAME="dataListName"</code> */
	protected static final String PARAM_DATA_LIST_NAME = "dataListName";
	
	protected static final String PARAM_ASYNC = "async";

	/**
	 * METADATA
	 */
	protected static final String PARAM_METADATA = "metadata";

	/**
	 * Sites search params
	 */
	protected static final String PARAM_CONTAINER = "container";

	/** Constant <code>PARAM_SITE="site"</code> */
	protected static final String PARAM_SITE = "site";

	/** Constant <code>PARAM_REPOSITORY="repo"</code> */
	protected static final String PARAM_REPOSITORY = "repo";

	// request parameter names
	/** The Constant PARAM_STORE_TYPE. */
	protected static final String PARAM_STORE_TYPE = "store_type";

	/** The Constant PARAM_STORE_ID. */
	protected static final String PARAM_STORE_ID = "store_id";

	/** The Constant PARAM_ID. */
	protected static final String PARAM_ID = "id";

	/** Constant <code>PARAM_LOCALE="locale"</code> */
	protected static final String PARAM_LOCALE = "locale";

	/** Constant <code>PARAM_FIELDS="fields"</code> */
	protected static final String PARAM_FIELDS = "fields";

	/** The Constant PARAM_NODEREF. */
	protected static final String PARAM_ENTITY_NODEREF = "entityNodeRef";

	/** Constant <code>PARAM_ITEMTYPE="itemType"</code> */
	protected static final String PARAM_ITEMTYPE = "itemType";

	/** Constant <code>PARAM_EXTRA_PARAMS="extraParams"</code> */
	protected static final String PARAM_EXTRA_PARAMS = "extraParams";

	/** Pagination **/

	protected static final String PARAM_SORT = "sort";

	/** Constant <code>PARAM_SORT_ID="sortId"</code> */
	protected static final String PARAM_SORT_ID = "sortId";

	/** Constant <code>PARAM_PAGE="page"</code> */
	protected static final String PARAM_PAGE = "page";

	/** Constant <code>PARAM_PAGE_SIZE="pageSize"</code> */
	protected static final String PARAM_PAGE_SIZE = "pageSize";

	/** Constant <code>PARAM_MAX_RESULTS="maxResults"</code> */
	protected static final String PARAM_MAX_RESULTS = "maxResults";

	/** Constant <code>PARAM_QUERY_EXECUTION_ID="queryExecutionId"</code> */
	protected static final String PARAM_QUERY_EXECUTION_ID = "queryExecutionId";

	/** Constant <code>PARAM_GUESS_CONTAINER="guessContainer"</code> */
	protected static final String PARAM_GUESS_CONTAINER = "guessContainer";

	/** Constant <code>PARAM_EFFECTIVE_FILTER_ON="effectiveFilterOn"</code> */
	protected static final String PARAM_EFFECTIVE_FILTER_ON = "effectiveFilterOn";

	private NodeService nodeService;

	private SecurityService securityService;

	private LockService lockService;

	private NamespaceService namespaceService;

	private DataListExtractorFactory dataListExtractorFactory;

	private AuthorityService authorityService;

	private DataListOutputWriterFactory datalistOutputWriterFactory;

	private boolean effectiveFilterEnabled = false;

	/**
	 * <p>Setter for the field <code>effectiveFilterEnabled</code>.</p>
	 *
	 * @param effectiveFilterEnabled a boolean.
	 */
	public void setEffectiveFilterEnabled(boolean effectiveFilterEnabled) {
		this.effectiveFilterEnabled = effectiveFilterEnabled;
	}

	/**
	 * <p>Setter for the field <code>datalistOutputWriterFactory</code>.</p>
	 *
	 * @param datalistOutputWriterFactory a {@link fr.becpg.repo.entity.datalist.impl.DataListOutputWriterFactory} object.
	 */
	public void setDatalistOutputWriterFactory(DataListOutputWriterFactory datalistOutputWriterFactory) {
		this.datalistOutputWriterFactory = datalistOutputWriterFactory;
	}

	/**
	 * <p>Setter for the field <code>nodeService</code>.</p>
	 *
	 * @param nodeService a {@link org.alfresco.service.cmr.repository.NodeService} object.
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	/**
	 * <p>Setter for the field <code>securityService</code>.</p>
	 *
	 * @param securityService a {@link fr.becpg.repo.security.SecurityService} object.
	 */
	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	/**
	 * <p>Setter for the field <code>namespaceService</code>.</p>
	 *
	 * @param namespaceService a {@link org.alfresco.service.namespace.NamespaceService} object.
	 */
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	/**
	 * <p>Setter for the field <code>dataListExtractorFactory</code>.</p>
	 *
	 * @param dataListExtractorFactory a {@link fr.becpg.repo.entity.datalist.DataListExtractorFactory} object.
	 */
	public void setDataListExtractorFactory(DataListExtractorFactory dataListExtractorFactory) {
		this.dataListExtractorFactory = dataListExtractorFactory;
	}

	/**
	 * <p>Setter for the field <code>authorityService</code>.</p>
	 *
	 * @param authorityService a {@link org.alfresco.service.cmr.security.AuthorityService} object.
	 */
	public void setAuthorityService(AuthorityService authorityService) {
		this.authorityService = authorityService;
	}

	/**
	 * <p>Setter for the field <code>lockService</code>.</p>
	 *
	 * @param lockService a {@link org.alfresco.service.cmr.lock.LockService} object.
	 */
	public void setLockService(LockService lockService) {
		this.lockService = lockService;
	}

	/** {@inheritDoc} */
	@Override
	public void execute(WebScriptRequest req, WebScriptResponse res) throws IOException {

		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
			logger.debug("EntityDataListWebScript executeImpl()");
		}

		DataListFilter dataListFilter = new DataListFilter();
		dataListFilter.getPagination().setMaxResults(getNumParameter(req, PARAM_MAX_RESULTS));
		dataListFilter.getPagination().setPageSize(getNumParameter(req, PARAM_PAGE_SIZE));
		dataListFilter.getPagination().setQueryExecutionId(req.getParameter(PARAM_QUERY_EXECUTION_ID));

		String itemType = req.getParameter(PARAM_ITEMTYPE);
		dataListFilter.setDataListName(req.getParameter(PARAM_DATA_LIST_NAME));
		// String argDays = req.getParameter(PARAM_DAYS);
		QName dataType = QName.createQName(itemType, namespaceService);
		dataListFilter.setDataType(dataType);

		// Sort param
		dataListFilter.setSortMap(WebscriptHelper.extractSortMap(req.getParameter(PARAM_SORT), namespaceService));
		dataListFilter.setSortId(req.getParameter(PARAM_SORT_ID));

		// Format
		dataListFilter.setFormat(req.getFormat());

		// Site filter
		dataListFilter.setSiteId(req.getParameter(PARAM_SITE));
		dataListFilter.setContainerId(req.getParameter(PARAM_CONTAINER));
		String repo = req.getParameter(PARAM_REPOSITORY);

		boolean isRepo = true;
		if ("false".equals(repo)) {
			isRepo = false;
		}
		dataListFilter.setRepo(isRepo);

		Locale currentLocal = null;
		if (req.getParameter(PARAM_LOCALE) != null) {
			currentLocal = I18NUtil.getContentLocale();
			I18NUtil.setContentLocale(MLTextHelper.parseLocale(req.getParameter(PARAM_LOCALE)));
		}

		String guessContainer = req.getParameter(PARAM_GUESS_CONTAINER);

		if ("true".equals(guessContainer)) {
			dataListFilter.setGuessContainer(true);
		}

		Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
		if (templateArgs != null) {
			String storeType = templateArgs.get(PARAM_STORE_TYPE);
			String storeId = templateArgs.get(PARAM_STORE_ID);
			String nodeId = templateArgs.get(PARAM_ID);
			if ((storeType != null) && (storeId != null) && (nodeId != null)) {
				NodeRef nodeRef = new NodeRef(storeType, storeId, nodeId);
				if (req.getServiceMatch().getPath().contains("/item/node")) {
					dataListFilter.setNodeRef(nodeRef);
					dataListFilter.setParentNodeRef(nodeService.getPrimaryParent(nodeRef).getParentRef());
				} else {
					dataListFilter.setParentNodeRef(nodeRef);
				}
			}
		}

		String entityNodeRefs = req.getParameter(PARAM_ENTITY_NODEREF);
		List<NodeRef> entityNodeRefsList = new ArrayList<>();
		if ((entityNodeRefs != null) && !entityNodeRefs.isEmpty()) {
			for (String entityNodeRef : entityNodeRefs.split(",")) {
				entityNodeRefsList.add(new NodeRef(entityNodeRef));
			}
			dataListFilter.setEntityNodeRefs(entityNodeRefsList);
		}

		String filterId = req.getParameter(PARAM_FILTER);
		String filterData = req.getParameter(PARAM_FILTER_DATA);
		String filterParams = req.getParameter(PARAM_FILTER_PARAMS);
		String extraParams = req.getParameter(PARAM_EXTRA_PARAMS);
		String async = req.getParameter(PARAM_ASYNC);

		try {

			JSONObject json;

			if (req.getParameter(PARAM_METADATA) != null) {
				json = new JSONObject(req.getParameter(PARAM_METADATA));
			} else {
				json = (JSONObject) req.parseContent();
			}

			if (filterId == null) {
				if ((json != null) && json.has(PARAM_FILTER)) {
					JSONObject filterJSON = (JSONObject) json.get(PARAM_FILTER);
					if (filterJSON != null) {
						filterId = (String) filterJSON.get(PARAM_FILTER_ID);
						if (filterJSON.has(PARAM_FILTER_DATA)) {
							filterData = (String) filterJSON.get(PARAM_FILTER_DATA);
						}
						if (filterJSON.has(PARAM_FILTER_PARAMS) && !filterJSON.isNull(PARAM_FILTER_PARAMS)) {
							filterParams = (String) filterJSON.get(PARAM_FILTER_PARAMS);
						}
						filterId = (String) filterJSON.get(PARAM_FILTER_ID);
					}
				} else {
					filterId = DataListFilter.ALL_FILTER;
				}
			}

			if (extraParams == null) {
				if ((json != null) && json.has(PARAM_EXTRA_PARAMS) && !json.isNull(PARAM_EXTRA_PARAMS)) {
					extraParams = (String) json.get(PARAM_EXTRA_PARAMS);
				}
			}

			if (dataListFilter.isSimpleItem()) {
				filterId = DataListFilter.NODE_FILTER;
			} else {
				Integer page = getNumParameter(req, PARAM_PAGE);

				if ((page == null) && (json != null) && json.has(PARAM_PAGE)) {
					page = (Integer) json.get(PARAM_PAGE);
				}
				dataListFilter.getPagination().setPage(page);
			}

			if ((json != null) && json.has(PARAM_SORT)) {

				dataListFilter.setSortMap(WebscriptHelper.extractSortMap((String) json.get(PARAM_SORT), namespaceService));
			}

			if ((json != null) && json.has(PARAM_QUERY_EXECUTION_ID) && !json.isNull(PARAM_QUERY_EXECUTION_ID)) {
				dataListFilter.getPagination().setQueryExecutionId((String) json.get(PARAM_QUERY_EXECUTION_ID));
			}

			if (filterId.equals(DataListFilter.FORM_FILTER) && (filterData != null)) {
				JSONObject jsonObject = new JSONObject(filterData);
				dataListFilter.setCriteriaMap(JsonHelper.extractCriteria(jsonObject));
			}

			List<String> metadataFields = new LinkedList<>();

			if ((json != null) && json.has(PARAM_FIELDS)) {
				JSONArray jsonFields = (JSONArray) json.get(PARAM_FIELDS);

				for (int i = 0; i < jsonFields.length(); i++) {
					metadataFields.add(((String) jsonFields.get(i)).replace("_", ":"));
				}
			}

			dataListFilter.setFilterId(filterId);
			dataListFilter.setFilterData(filterData);
			dataListFilter.setFilterParams(filterParams);
			dataListFilter.setExtraParams(extraParams);
			dataListFilter.setEffectiveFilterOn(effectiveFilterEnabled && "true".equals(req.getParameter(PARAM_EFFECTIVE_FILTER_ON)));

			if (logger.isDebugEnabled()) {
				logger.debug("Filter:" + dataListFilter.toString());
				logger.debug("Pagination:" + dataListFilter.getPagination().toString());
				logger.debug("MetadataFields:" + metadataFields.toString());
				logger.debug("SearchQuery:" + dataListFilter.getSearchQuery());
			}

			DataListExtractor extractor = dataListExtractorFactory.getExtractor(dataListFilter);

			boolean hasWriteAccess = !dataListFilter.isVersionFilter();
			boolean hasReadAccess = true;
			if(!entityNodeRefsList.isEmpty()) {
				NodeRef entityNodeRef = entityNodeRefsList.get(0);
				QName entityNodeRefType = nodeService.getType(entityNodeRef);
				
				int accessMode = securityService.computeAccessMode(entityNodeRef, entityNodeRefType, itemType);
				hasReadAccess = accessMode != SecurityService.NONE_ACCESS;
				
				if (hasReadAccess && hasWriteAccess) {

					hasWriteAccess = extractor.hasWriteAccess() && !nodeService.hasAspect(entityNodeRef, ContentModel.ASPECT_CHECKED_OUT)
							&& !nodeService.hasAspect(entityNodeRef, BeCPGModel.ASPECT_COMPOSITE_VERSION)
							&& (lockService.getLockStatus(entityNodeRef) == LockStatus.NO_LOCK)
							&& (accessMode == SecurityService.WRITE_ACCESS)
							&& isExternalUserAllowed(dataListFilter);
	
					if (hasWriteAccess && (dataListFilter.getParentNodeRef() != null) && dataType!=null && !dataType.getLocalName().equals(dataListFilter.getDataListName())) {
						String dataListType = (String) nodeService.getProperty(dataListFilter.getParentNodeRef(), DataListModel.PROP_DATALISTITEMTYPE);
	
						if ((dataListType != null) && !dataListType.isEmpty()) {
							QName dataListTypeQName = QName.createQName(dataListType, namespaceService);
							hasWriteAccess = securityService.computeAccessMode(entityNodeRef, entityNodeRefType, dataListTypeQName) == SecurityService.WRITE_ACCESS;
						}
					}
				}
			}
			PaginatedExtractedItems extractedItems;
			if(hasReadAccess) {
				dataListFilter.setHasWriteAccess(hasWriteAccess);
	
				Date lastModified = extractor.computeLastModified(dataListFilter);
	
				if (BrowserCacheHelper.shouldReturnNotModified(req, lastModified)) {
					res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
					if (logger.isDebugEnabled()) {
						logger.debug("Send Not_MODIFIED status");
					}
					return;
				}
	
				Cache cache = new Cache(getDescription().getRequiredCache());
				cache.setIsPublic(false);
				cache.setMustRevalidate(true);
				cache.setNeverCache(false);
				cache.setMaxAge(0L);
				cache.setLastModified(lastModified);
				res.setCache(cache);
				
				if("true".equals(async)) {
					extractedItems =  new AsyncPaginatedExtractorWrapper(extractor,dataListFilter, metadataFields);
				} else {
					extractedItems = extractor.extract(dataListFilter, metadataFields);
				}
			} else {
				extractedItems = new PaginatedExtractedItems(dataListFilter.getPagination().getPageSize());
			}
			
			datalistOutputWriterFactory.write(req, res, dataListFilter, extractedItems);

		} catch (JSONException e) {
			throw new WebScriptException("Unable to parse JSON", e);
		} finally {
			if (currentLocal != null) {
				I18NUtil.setContentLocale(currentLocal);
			}

			if (logger.isDebugEnabled()) {
				watch.stop();
				logger.debug("EntityDataListWebScript execute in " + watch.getTotalTimeSeconds() + "s");
			}
		}

	}

	private boolean isExternalUserAllowed(DataListFilter dataListFilter) {
		if ((dataListFilter.getParentNodeRef() != null) && nodeService.exists(dataListFilter.getParentNodeRef())
				&& nodeService.hasAspect(dataListFilter.getParentNodeRef(), BeCPGModel.ASPECT_ENTITYLIST_STATE)
				&& SystemState.Valid.toString().equals(nodeService.getProperty(dataListFilter.getParentNodeRef(), BeCPGModel.PROP_ENTITYLIST_STATE))
				&& isCurrentUserExternal()) {
			return false;

		}
		return true;
	}

	private boolean isCurrentUserExternal() {
		for (String currAuth : authorityService.getAuthorities()) {
			if ((PermissionService.GROUP_PREFIX + SystemGroup.ExternalUser.toString()).equals(currAuth)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * <p>getNumParameter.</p>
	 *
	 * @param req a {@link org.springframework.extensions.webscripts.WebScriptRequest} object.
	 * @param paramName a {@link java.lang.String} object.
	 * @return a {@link java.lang.Integer} object.
	 */
	protected Integer getNumParameter(WebScriptRequest req, String paramName) {
		String param = req.getParameter(paramName);

		Integer ret = null;
		if (param != null) {
			try {
				ret = Integer.parseInt(param);
			} catch (NumberFormatException e) {
				logger.error("Cannot parse page argument", e);
			}
		}
		return ret;
	}

}
