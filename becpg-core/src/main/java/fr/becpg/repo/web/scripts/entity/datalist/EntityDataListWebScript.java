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
import org.springframework.util.StopWatch;

import fr.becpg.repo.entity.datalist.DataListExtractor;
import fr.becpg.repo.entity.datalist.DataListExtractorFactory;
import fr.becpg.repo.entity.datalist.PaginatedExtractedItems;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.entity.datalist.data.DataListPagination;
import fr.becpg.repo.security.SecurityService;

/**
 * Webscript that send the result of a datalist
 * 
 * @author matthieu
 */
public class EntityDataListWebScript extends AbstractWebScript {

	/** The logger. */
	private static Log logger = LogFactory.getLog(EntityDataListWebScript.class);

	/** The Constant PARAM_FILTER. */
	protected static final String PARAM_FILTER = "filter";

	protected static final String PARAM_FILTER_DATA = "filterData";

	protected static final String PARAM_DATA_LIST_NAME = "dataListName";
	
	// request parameter names
	/** The Constant PARAM_STORE_TYPE. */
	private static final String PARAM_STORE_TYPE = "store_type";

	/** The Constant PARAM_STORE_ID. */
	private static final String PARAM_STORE_ID = "store_id";

	/** The Constant PARAM_ID. */
	protected static final String PARAM_ID = "id";

	protected static final String PARAM_DAYS = "days";

	protected static final String PARAM_FIELDS = "fields";

	/** The Constant PARAM_NODEREF. */
	protected static final String PARAM_ENTITY_NODEREF = "entityNodeRef";

	protected static final String PARAM_ITEMTYPE = "itemType";

	/** Pagination **/

	protected static final String PARAM_PAGE = "page";

	protected static final String PARAM_PAGE_SIZE = "pageSize";

	protected static final String PARAM_MAX_RESULTS = "maxResults";

	/** Services **/

	private NodeService nodeService;

	private SecurityService securityService;

	private NamespaceService namespaceService;


	private PermissionService permissionService;

	private DataListExtractorFactory dataListExtractorFactory;

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
		pagination.setPage(getNumParameter(req, PARAM_PAGE));
		pagination.setPageSize(getNumParameter(req, PARAM_PAGE_SIZE));
	
		String itemType = req.getParameter(PARAM_ITEMTYPE);
		String dataListName = req.getParameter(PARAM_DATA_LIST_NAME);
		String argDays = req.getParameter(PARAM_DAYS);
		QName dataType = QName.createQName(itemType, namespaceService);
		dataListFilter.setDataType(dataType);

		Map<String, String> templateArgs = req.getServiceMatch().getTemplateVars();
		if(templateArgs!=null){
			String storeType = templateArgs.get(PARAM_STORE_TYPE);
			String storeId = templateArgs.get(PARAM_STORE_ID);
			String nodeId = templateArgs.get(PARAM_ID);
			if(storeType!=null && storeId!=null && nodeId!=null){
				dataListFilter.setDataListNodeRef(new NodeRef(storeType, storeId, nodeId));
			}
		}
		
		String entityNodeRef = req.getParameter(PARAM_ENTITY_NODEREF);
		if(entityNodeRef!=null){
			dataListFilter.setEntityNodeRef(new NodeRef(entityNodeRef));
		}
		
		String filterId = req.getParameter(PARAM_FILTER);
		String filterData = req.getParameter(PARAM_FILTER_DATA);

		try {

			JSONObject json = (JSONObject) req.parseContent();

			if (filterId == null) {
				if (json != null && json.has("filter")) {
					JSONObject filterJSON = (JSONObject) json.get("filter");
					if (filterJSON != null) {
						filterId = (String) filterJSON.get("filterId");
						filterData = (String) filterJSON.get("filterData");
					}
				} else {
					filterId = "all";
				}
			}


			if (filterId.equals("filterform") && filterData != null) {
				JSONObject jsonObject = new JSONObject(filterData);
				dataListFilter.setCriteriaMap(extractCriteria(jsonObject));
			}

			List<String> metadataFields = new LinkedList<String>();
			if (json.has(PARAM_FIELDS)) {
				JSONArray jsonFields = (JSONArray) json.get(PARAM_FIELDS);

				for (int i = 0; i < jsonFields.length(); i++) {
					metadataFields.add(((String) jsonFields.get(i)).replace("_", ":"));
				}
			}

			boolean hasWriteAccess = true;
			if (entityNodeRef != null) {
				hasWriteAccess = securityService.computeAccessMode(nodeService.getType(new NodeRef(entityNodeRef)), itemType) == SecurityService.WRITE_ACCESS;
			}

			dataListFilter.buildQueryFilter(filterId, filterData, argDays);

			if (logger.isDebugEnabled()) {
				logger.debug("Filter:" + dataListFilter.toString());
				logger.debug("Pagination:" + pagination.toString());
				logger.debug("MetadataFields:" + metadataFields.toString());
			}


			DataListExtractor extractor = dataListExtractorFactory.getExtractor(dataListName, dataType);


			PaginatedExtractedItems extractedItems = extractor.extract(dataListFilter, metadataFields, pagination, hasWriteAccess);

			JSONObject ret = new JSONObject();
			ret.put("startIndex", pagination.getPage());
			ret.put("pageSize", pagination.getPageSize());
			ret.put("totalRecords", extractedItems.getFullListSize());

			JSONObject metadata = new JSONObject();

			JSONObject parent = new JSONObject();

			parent.put("nodeRef", dataListFilter.getDataListNodeRef());

			
			JSONObject permissions = new JSONObject();
			JSONObject userAccess = new JSONObject();
			
			
			userAccess.put("create", (hasWriteAccess && permissionService.hasPermission(dataListFilter.getDataListNodeRef(), "CreateChildren") == AccessStatus.ALLOWED));
			permissions.put("userAccess",userAccess);
			
			parent.put("permissions", permissions);
			
			metadata.put("parent", parent);

			ret.put("metadata", metadata);

			ret.put("items", processResults(extractedItems));

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
