/*
 * 
 */
package fr.becpg.repo.web.scripts.entity.datalist;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.InvalidNodeRefException;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.AbstractWebScript;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.stereotype.Service;
import org.springframework.util.StopWatch;

import fr.becpg.repo.entity.datalist.DataListExtractor;
import fr.becpg.repo.entity.datalist.DataListExtractorFactory;
import fr.becpg.repo.entity.datalist.DataListSortService;
import fr.becpg.repo.entity.datalist.PaginatedExtractedItems;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.entity.datalist.data.DataListPagination;
import fr.becpg.repo.entity.datalist.impl.AbstractDataListExtractor;
import fr.becpg.repo.security.SecurityService;
import fr.becpg.repo.web.scripts.WebscriptHelper;

/**
 * Webscript that send the result of a datalist
 * 
 * @author matthieu
 */
@Service
public class EntityDataListWebScript extends AbstractWebScript {

	/** The logger. */
	private static Log logger = LogFactory.getLog(EntityDataListWebScript.class);

	/** The Constant PARAM_FILTER. */
	
	protected static final String PARAM_FILTER_ID = "filterId";
	
	protected static final String PARAM_FILTER = "filter";

	protected static final String PARAM_FILTER_DATA = "filterData";
	
	protected static final String PARAM_FILTER_PARAMS = "filterParams";

	protected static final String PARAM_DATA_LIST_NAME = "dataListName";
	
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

	/** Pagination **/

	protected static final String PARAM_SORT = "sort";
	
	protected static final String PARAM_PAGE = "page";

	protected static final String PARAM_PAGE_SIZE = "pageSize";

	protected static final String PARAM_MAX_RESULTS = "maxResults";
	
	protected static final String PARAM_QUERY_EXECUTION_ID =  "queryExecutionId";

	/** Services **/

	private NodeService nodeService;

	private SecurityService securityService;

	private NamespaceService namespaceService;

	private PermissionService permissionService;

	private DataListExtractorFactory dataListExtractorFactory;
	
	private DataListSortService dataListSortService;

	

	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	public void setSecurityService(SecurityService securityService) {
		this.securityService = securityService;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}


	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
	}

	public void setDataListExtractorFactory(DataListExtractorFactory dataListExtractorFactory) {
		this.dataListExtractorFactory = dataListExtractorFactory;
	}

	public void setDataListSortService(DataListSortService dataListSortService) {
		this.dataListSortService = dataListSortService;
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

		DataListPagination pagination = new DataListPagination();
		DataListFilter dataListFilter = new DataListFilter();
		pagination.setMaxResults(getNumParameter(req, PARAM_MAX_RESULTS));
		pagination.setPageSize(getNumParameter(req, PARAM_PAGE_SIZE));
		pagination.setQueryExecutionId(req.getParameter(PARAM_QUERY_EXECUTION_ID));
		
		
		String itemType = req.getParameter(PARAM_ITEMTYPE);
		String dataListName = req.getParameter(PARAM_DATA_LIST_NAME);
		//String argDays = req.getParameter(PARAM_DAYS);
		QName dataType = QName.createQName(itemType, namespaceService);
		dataListFilter.setDataType(dataType);

		String sort = req.getParameter(PARAM_SORT);
		Map<String, Boolean> sortMap = WebscriptHelper.extractSortMap(sort,namespaceService);
		
		dataListFilter.setSortMap(sortMap);
		
		//Site filter 
		dataListFilter.setSiteId(req.getParameter(PARAM_SITE));
		dataListFilter.setContainerId(req.getParameter(PARAM_CONTAINER));		
		String repo = req.getParameter(PARAM_REPOSITORY);

		boolean isRepo = true;
		if (repo != null && repo.equals("false")) {
			isRepo = false;
		}
		dataListFilter.setRepo(isRepo);
		
		
		Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
		if(templateArgs!=null){
			String storeType = templateArgs.get(PARAM_STORE_TYPE);
			String storeId = templateArgs.get(PARAM_STORE_ID);
			String nodeId = templateArgs.get(PARAM_ID);
			if(storeType!=null && storeId!=null && nodeId!=null){
				NodeRef nodeRef = new NodeRef(storeType, storeId, nodeId);
				if(req.getServiceMatch().getPath().contains("/item/node")){
					dataListFilter.setNodeRef(nodeRef);
					dataListFilter.setParentNodeRef(nodeService.getPrimaryParent(nodeRef).getParentRef());
				} else {
					dataListFilter.setParentNodeRef(nodeRef);
				}
			}
		}
		
		String entityNodeRef = req.getParameter(PARAM_ENTITY_NODEREF);
		if(entityNodeRef!=null){
			dataListFilter.setEntityNodeRef(new NodeRef(entityNodeRef));
		}
		
		String filterId = req.getParameter(PARAM_FILTER);
		String filterData = req.getParameter(PARAM_FILTER_DATA);
		String filterParams = req.getParameter(PARAM_FILTER_PARAMS);

		try {

			JSONObject json = (JSONObject) req.parseContent();

			if (filterId == null) {
				if (json != null && json.has(PARAM_FILTER)) {
					JSONObject filterJSON = (JSONObject) json.get(PARAM_FILTER);
					if (filterJSON != null) {
						filterId = (String) filterJSON.get(PARAM_FILTER_ID);
						if(filterJSON.has(PARAM_FILTER_DATA)){
							filterData = (String) filterJSON.get(PARAM_FILTER_DATA);
						}
						if(filterJSON.has(PARAM_FILTER_PARAMS) && !filterJSON.isNull(PARAM_FILTER_PARAMS)){
							filterParams = (String) filterJSON.get(PARAM_FILTER_PARAMS);
						}
					}
				} else {
					filterId = DataListFilter.ALL_FILTER;
				}
			}

			
			if(dataListFilter.isSimpleItem()){
				filterId = DataListFilter.NODE_FILTER;
			} else {
				Integer page = getNumParameter(req, PARAM_PAGE);
			
				if(page==null && json != null && json.has("page")) {
					page = (Integer) json.get("page");
				}
				pagination.setPage(page);
			}


			if (filterId.equals(DataListFilter.FORM_FILTER) && filterData != null) {
				JSONObject jsonObject = new JSONObject(filterData);
				dataListFilter.setCriteriaMap(extractCriteria(jsonObject));
			}

			List<String> metadataFields = new LinkedList<String>();
			if (json!=null && json.has(PARAM_FIELDS)) {
				JSONArray jsonFields = (JSONArray) json.get(PARAM_FIELDS);

				for (int i = 0; i < jsonFields.length(); i++) {
					metadataFields.add(((String) jsonFields.get(i)).replace("_", ":"));
				}
			}

			boolean hasWriteAccess = true;
			if (entityNodeRef != null) {
				hasWriteAccess = securityService.computeAccessMode(nodeService.getType(new NodeRef(entityNodeRef)), itemType) == SecurityService.WRITE_ACCESS;
			}
			
			if(dataListName.equals("WUsed")){
				hasWriteAccess = false;
			}
			
			dataListFilter.buildQueryFilter(filterId, filterData, filterParams);

			if (logger.isDebugEnabled()) {
				logger.debug("Filter:" + dataListFilter.toString());
				logger.debug("Pagination:" + pagination.toString());
				logger.debug("MetadataFields:" + metadataFields.toString());
				logger.debug("SearchQuery:"+dataListFilter.getSearchQuery());
			}


			DataListExtractor extractor = dataListExtractorFactory.getExtractor(dataListFilter, dataListName);


			PaginatedExtractedItems extractedItems = extractor.extract(dataListFilter, metadataFields, pagination, hasWriteAccess);

			JSONObject ret = new JSONObject();
			if(!dataListFilter.isSimpleItem()){
				ret.put("startIndex", pagination.getPage());
				ret.put("pageSize", pagination.getPageSize());
				ret.put("totalRecords", extractedItems.getFullListSize());
				if(pagination.getQueryExecutionId()!=null){
					ret.put(PARAM_QUERY_EXECUTION_ID, pagination.getQueryExecutionId());
				}
			}
			
			JSONObject metadata = new JSONObject();

			JSONObject parent = new JSONObject();

			parent.put("nodeRef", dataListFilter.getParentNodeRef());
	
			
			JSONObject permissions = new JSONObject();
			JSONObject userAccess = new JSONObject();
			
			
			userAccess.put("create", (hasWriteAccess 
					&& permissionService.hasPermission(dataListFilter.getParentNodeRef(), "CreateChildren") == AccessStatus.ALLOWED));
			
			
			permissions.put("userAccess",userAccess);
			
			parent.put("permissions", permissions);
			
			metadata.put("parent", parent);
				

			ret.put("metadata", metadata);
			if(dataListFilter.isSimpleItem()){
				Map<String,Object> item = extractedItems.getItems().get(0);
				ret.put("item", new JSONObject(item));
				ret.put("lastSiblingNodeRef", dataListSortService.getLastChild((NodeRef)item.get(AbstractDataListExtractor.PROP_NODE)));
			} else {
				ret.put("items", processResults(extractedItems));
			}

			res.setContentType("application/json");
			res.setContentEncoding("UTF-8");
			res.getWriter().write(ret.toString(3));

		} catch (JSONException e) {
			throw new WebScriptException("Unable to serialize JSON",e);
		} finally {
			if (logger.isDebugEnabled()) {
				watch.stop();
				logger.debug("EntityDataListWebScript execute in " + watch.getTotalTimeSeconds() + "s");
			}
		}

	}


	private JSONArray processResults(PaginatedExtractedItems extractedItems) throws InvalidNodeRefException, JSONException {

		JSONArray items = new JSONArray();

		for (Map<String, Object> item : extractedItems.getItems()) {
			items.put(new JSONObject(item));
		}

		return items;

	}

	@SuppressWarnings("unchecked")
	protected Map<String, String> extractCriteria(JSONObject jsonObject) throws JSONException {

		Map<String, String> criteriaMap = new HashMap<String, String>();

		Iterator<String> iterator = jsonObject.keys();

		while (iterator.hasNext()) {

			String key = (String) iterator.next();
			String value = jsonObject.getString(key);
			criteriaMap.put(key, value);
		}

		return criteriaMap;

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
