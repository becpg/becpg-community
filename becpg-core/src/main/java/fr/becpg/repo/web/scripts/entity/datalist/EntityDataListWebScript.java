/*
 * 
 */
package fr.becpg.repo.web.scripts.entity.datalist;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.lock.LockService;
import org.alfresco.service.cmr.lock.LockStatus;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.AuthorityService;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.util.StopWatch;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.SystemGroup;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.datalist.DataListExtractor;
import fr.becpg.repo.entity.datalist.DataListExtractorFactory;
import fr.becpg.repo.entity.datalist.DataListOutputWriter;
import fr.becpg.repo.entity.datalist.DataListSortService;
import fr.becpg.repo.entity.datalist.PaginatedExtractedItems;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.entity.datalist.data.DataListPagination;
import fr.becpg.repo.entity.datalist.impl.AbstractDataListExtractor;
import fr.becpg.repo.entity.datalist.impl.CSVDataListOutputWriter;
import fr.becpg.repo.entity.datalist.impl.DataListOutputWriterFactory;
import fr.becpg.repo.entity.datalist.impl.ExcelDataListOutputWriter;
import fr.becpg.repo.helper.JSONHelper;
import fr.becpg.repo.security.SecurityService;
import fr.becpg.repo.web.scripts.BrowserCacheHelper;
import fr.becpg.repo.web.scripts.WebscriptHelper;

/**
 * Webscript that send the result of a datalist
 * 
 * @author matthieu
 */
public class EntityDataListWebScript extends AbstractWebScript {

	/** The logger. */
	private static final Log logger = LogFactory.getLog(EntityDataListWebScript.class);

	/** The Constant PARAM_FILTER. */

	protected static final String PARAM_FILTER_ID = "filterId";

	protected static final String PARAM_FILTER = "filter";

	protected static final String PARAM_FILTER_DATA = "filterData";

	protected static final String PARAM_FILTER_PARAMS = "filterParams";

	protected static final String PARAM_DATA_LIST_NAME = "dataListName";

	/**
	 * METADATA
	 */
	protected static final String PARAM_METADATA = "metadata";

	/**
	 * Sites search params
	 */
	protected static final String PARAM_CONTAINER = "container";

	protected static final String PARAM_SITE = "site";

	protected static final String PARAM_REPOSITORY = "repo";

	// request parameter names
	/** The Constant PARAM_STORE_TYPE. */
	protected static final String PARAM_STORE_TYPE = "store_type";

	/** The Constant PARAM_STORE_ID. */
	protected static final String PARAM_STORE_ID = "store_id";

	/** The Constant PARAM_ID. */
	protected static final String PARAM_ID = "id";

	protected static final String PARAM_FIELDS = "fields";

	/** The Constant PARAM_NODEREF. */
	protected static final String PARAM_ENTITY_NODEREF = "entityNodeRef";

	protected static final String PARAM_ITEMTYPE = "itemType";
	
	protected static final String PARAM_EXTRA_PARAMS =  "extraParams";

	/** Pagination **/

	protected static final String PARAM_SORT = "sort";

	protected static final String PARAM_SORT_ID = "sortId";

	protected static final String PARAM_PAGE = "page";

	protected static final String PARAM_PAGE_SIZE = "pageSize";

	protected static final String PARAM_MAX_RESULTS = "maxResults";

	protected static final String PARAM_QUERY_EXECUTION_ID = "queryExecutionId";

	private NodeService nodeService;

	private SecurityService securityService;

	private LockService lockService;
	
	private NamespaceService namespaceService;

	private DataListExtractorFactory dataListExtractorFactory;

	private AuthorityService authorityService;
	
	private DataListOutputWriterFactory datalistOutputWriterFactory; 
	
	

	public void setDatalistOutputWriterFactory(DataListOutputWriterFactory datalistOutputWriterFactory) {
		this.datalistOutputWriterFactory = datalistOutputWriterFactory;
	}

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	public void setDataListExtractorFactory(DataListExtractorFactory dataListExtractorFactory) {
		this.dataListExtractorFactory = dataListExtractorFactory;
	}


	public void setAuthorityService(AuthorityService authorityService) {
		this.authorityService = authorityService;
	}
	
	public void setLockService(LockService lockService) {
		this.lockService = lockService;
	}

	/**
	 * @param req
	 *            the req
	 * @param res
	 *            the res
	 * @throws IOException
	 *             Signals that an I/O exception has occurred.
	 */
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

		Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
		if (templateArgs != null) {
			String storeType = templateArgs.get(PARAM_STORE_TYPE);
			String storeId = templateArgs.get(PARAM_STORE_ID);
			String nodeId = templateArgs.get(PARAM_ID);
			if (storeType != null && storeId != null && nodeId != null) {
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
		if (entityNodeRefs != null && !entityNodeRefs.isEmpty()) {
			for (String entityNodeRef : entityNodeRefs.split(",")) {
				entityNodeRefsList.add(new NodeRef(entityNodeRef));
			}
			dataListFilter.setEntityNodeRefs(entityNodeRefsList);
		}

		String filterId = req.getParameter(PARAM_FILTER);
		String filterData = req.getParameter(PARAM_FILTER_DATA);
		String filterParams = req.getParameter(PARAM_FILTER_PARAMS);
		String extraParams = req.getParameter(PARAM_EXTRA_PARAMS);
		

		try {

			JSONObject json;

			if (req.getParameter(PARAM_METADATA) != null) {
				json = new JSONObject(req.getParameter(PARAM_METADATA));
			} else {
				json = (JSONObject) req.parseContent();
			}

			if (filterId == null) {
				if (json != null && json.has(PARAM_FILTER)) {
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
			
			if(extraParams == null){
				if (json != null && json.has(PARAM_EXTRA_PARAMS) && !json.isNull(PARAM_EXTRA_PARAMS)) {
					extraParams = (String) json.get(PARAM_EXTRA_PARAMS);
				}
			}

			if (dataListFilter.isSimpleItem()) {
				filterId = DataListFilter.NODE_FILTER;
			} else {
				Integer page = getNumParameter(req, PARAM_PAGE);

				if (page == null && json != null && json.has(PARAM_PAGE)) {
					page = (Integer) json.get(PARAM_PAGE);
				}
				dataListFilter.getPagination().setPage(page);
			}

			if (json != null && json.has(PARAM_SORT)) {

				dataListFilter.setSortMap(WebscriptHelper.extractSortMap((String) json.get(PARAM_SORT), namespaceService));
			}

			if (filterId.equals(DataListFilter.FORM_FILTER) && filterData != null) {
				JSONObject jsonObject = new JSONObject(filterData);
				dataListFilter.setCriteriaMap(JSONHelper.extractCriteria(jsonObject));
			}

			List<String> metadataFields = new LinkedList<>();

			if (json != null && json.has(PARAM_FIELDS)) {
				JSONArray jsonFields = (JSONArray) json.get(PARAM_FIELDS);

				for (int i = 0; i < jsonFields.length(); i++) {
					metadataFields.add(((String) jsonFields.get(i)).replace("_", ":"));
				}
			}

			dataListFilter.setFilterId(filterId);
			dataListFilter.setFilterData(filterData);
			dataListFilter.setFilterParams(filterParams);
			dataListFilter.setExtraParams(extraParams);

			if (logger.isDebugEnabled()) {
				logger.debug("Filter:" + dataListFilter.toString());
				logger.debug("Pagination:" + dataListFilter.getPagination().toString());
				logger.debug("MetadataFields:" + metadataFields.toString());
				logger.debug("SearchQuery:" + dataListFilter.getSearchQuery());
			}

			DataListExtractor extractor = dataListExtractorFactory.getExtractor(dataListFilter);

			boolean hasWriteAccess = !dataListFilter.isVersionFilter();
			if (hasWriteAccess && !entityNodeRefsList.isEmpty()) {
				hasWriteAccess = 
						extractor.hasWriteAccess()
						&& !nodeService.hasAspect(entityNodeRefsList.get(0), ContentModel.ASPECT_CHECKED_OUT)
						&& lockService.getLockStatus(entityNodeRefsList.get(0)) == LockStatus.NO_LOCK 
						&& securityService.computeAccessMode(nodeService.getType(entityNodeRefsList.get(0)), itemType) == SecurityService.WRITE_ACCESS
						&& isExternalUserAllowed(dataListFilter);
				
			}
			
			dataListFilter.setHasWriteAccess(hasWriteAccess);

			// TODO : #546
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

			PaginatedExtractedItems extractedItems = extractor.extract(dataListFilter, metadataFields);
			
			datalistOutputWriterFactory.write(res,dataListFilter, extractedItems);
			

		} catch (JSONException e) {
			throw new WebScriptException("Unable to parse JSON", e);
		} finally {
			if (logger.isDebugEnabled()) {
				watch.stop();
				logger.debug("EntityDataListWebScript execute in " + watch.getTotalTimeSeconds() + "s");
			}
		}
			

	}

	

	private boolean isExternalUserAllowed(DataListFilter dataListFilter) {
		if(dataListFilter.getParentNodeRef() !=null 
				&& nodeService.hasAspect(dataListFilter.getParentNodeRef(), BeCPGModel.ASPECT_ENTITYLIST_STATE)
				&& "Valid".equals(nodeService.getProperty(dataListFilter.getParentNodeRef(), BeCPGModel.PROP_ENTITYLIST_STATE))
				&& isCurrentUserExternal()
				){
			return false;
			
		}
		return true;
	}

	private boolean isCurrentUserExternal() {
		for (String currAuth : authorityService.getAuthorities()) {
			if((PermissionService.GROUP_PREFIX+SystemGroup.ExternalUser.toString()).equals(currAuth)){
				return true;
			}
		}
		return false;
	}



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
