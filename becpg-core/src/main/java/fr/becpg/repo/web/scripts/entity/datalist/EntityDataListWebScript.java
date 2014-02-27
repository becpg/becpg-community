/*
 * 
 */
package fr.becpg.repo.web.scripts.entity.datalist;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletResponse;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.DictionaryService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.cmr.security.AccessStatus;
import org.alfresco.service.cmr.security.PermissionService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.csv.writer.CSVConfig;
import org.apache.commons.csv.writer.CSVField;
import org.apache.commons.csv.writer.CSVWriter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.extensions.webscripts.Cache;
import org.springframework.extensions.webscripts.WebScriptException;
import org.springframework.extensions.webscripts.WebScriptRequest;
import org.springframework.extensions.webscripts.WebScriptResponse;
import org.springframework.util.StopWatch;

import fr.becpg.repo.entity.datalist.DataListExtractor;
import fr.becpg.repo.entity.datalist.DataListExtractorFactory;
import fr.becpg.repo.entity.datalist.DataListSortService;
import fr.becpg.repo.entity.datalist.PaginatedExtractedItems;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.entity.datalist.data.DataListPagination;
import fr.becpg.repo.entity.datalist.impl.AbstractDataListExtractor;
import fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl.AttributeExtractorStructure;
import fr.becpg.repo.security.SecurityService;
import fr.becpg.repo.web.scripts.AbstractCachingWebscript;
import fr.becpg.repo.web.scripts.WebscriptHelper;

/**
 * Webscript that send the result of a datalist
 * 
 * @author matthieu
 */
public class EntityDataListWebScript extends AbstractCachingWebscript {

	/** The logger. */
	private static Log logger = LogFactory.getLog(EntityDataListWebScript.class);

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

	/** Pagination **/

	protected static final String PARAM_SORT = "sort";

	protected static final String PARAM_SORT_ID = "sortId";

	protected static final String PARAM_PAGE = "page";

	protected static final String PARAM_PAGE_SIZE = "pageSize";

	protected static final String PARAM_MAX_RESULTS = "maxResults";

	protected static final String PARAM_QUERY_EXECUTION_ID = "queryExecutionId";

	/** Services **/

	private NodeService nodeService;

	private SecurityService securityService;

	private NamespaceService namespaceService;

	private PermissionService permissionService;

	private DataListExtractorFactory dataListExtractorFactory;

	private DataListSortService dataListSortService;
	
	private DictionaryService dictionaryService;

	public void setDictionaryService(DictionaryService dictionaryService) {
		this.dictionaryService = dictionaryService;
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
		if (repo != null && repo.equals("false")) {
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

		try {

			JSONObject json = null;

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

			if (dataListFilter.isSimpleItem()) {
				filterId = DataListFilter.NODE_FILTER;
			} else {
				Integer page = getNumParameter(req, PARAM_PAGE);

				if (page == null && json != null && json.has(PARAM_PAGE)) {
					page = (Integer) json.get(PARAM_PAGE);
				}
				pagination.setPage(page);
			}

			if (json != null && json.has(PARAM_SORT)) {

				dataListFilter.setSortMap(WebscriptHelper.extractSortMap((String) json.get(PARAM_SORT), namespaceService));
			}

			if (filterId.equals(DataListFilter.FORM_FILTER) && filterData != null) {
				JSONObject jsonObject = new JSONObject(filterData);
				dataListFilter.setCriteriaMap(extractCriteria(jsonObject));
			}

			List<String> metadataFields = new LinkedList<String>();

			if (json != null && json.has(PARAM_FIELDS)) {
				JSONArray jsonFields = (JSONArray) json.get(PARAM_FIELDS);

				for (int i = 0; i < jsonFields.length(); i++) {
					metadataFields.add(((String) jsonFields.get(i)).replace("_", ":"));
				}
			}

			dataListFilter.buildQueryFilter(filterId, filterData, filterParams);

			if (logger.isDebugEnabled()) {
				logger.debug("Filter:" + dataListFilter.toString());
				logger.debug("Pagination:" + pagination.toString());
				logger.debug("MetadataFields:" + metadataFields.toString());
				logger.debug("SearchQuery:" + dataListFilter.getSearchQuery());
			}

			DataListExtractor extractor = dataListExtractorFactory.getExtractor(dataListFilter);

			boolean hasWriteAccess = !dataListFilter.isVersionFilter();
			if (hasWriteAccess && !entityNodeRefsList.isEmpty()) {
				hasWriteAccess = !nodeService.hasAspect(entityNodeRefsList.get(0), ContentModel.ASPECT_CHECKED_OUT)
						&& securityService.computeAccessMode(nodeService.getType(entityNodeRefsList.get(0)), itemType) == SecurityService.WRITE_ACCESS;
			}

			// TODO : #546
			Date lastModified = extractor.computeLastModified(dataListFilter);

			if (shouldReturnNotModified(req, lastModified)) {
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

			PaginatedExtractedItems extractedItems = extractor.extract(dataListFilter, metadataFields, pagination, hasWriteAccess);

			if ("csv".equals(dataListFilter.getFormat())) {
				res.setContentType("application/vnd.ms-excel");
				res.setContentEncoding("ISO-8859-1");

				CSVConfig csvConfig = new CSVConfig();

				csvConfig.setDelimiter(';');
				csvConfig.setValueDelimiter('"');
				csvConfig.setIgnoreValueDelimiter(false);

				appendCSVField(csvConfig, extractedItems.getComputedFields(), null);

				CSVWriter csvWriter = new CSVWriter(csvConfig);

				csvWriter.setWriter(res.getWriter());

				Map<String, String> headers = new HashMap<>();
				appendCSVHeader(headers, extractedItems.getComputedFields(), null, null);
				csvWriter.writeRecord(headers);

				writeToCSV(extractedItems, csvWriter);

				res.setHeader("Content-disposition", "attachment; filename=export.csv");
			} else {

				JSONObject ret = new JSONObject();
				if (!dataListFilter.isSimpleItem()) {
					ret.put("startIndex", pagination.getPage());
					ret.put("pageSize", pagination.getPageSize());
					ret.put("totalRecords", extractedItems.getFullListSize());
					if (pagination.getQueryExecutionId() != null) {
						ret.put(PARAM_QUERY_EXECUTION_ID, pagination.getQueryExecutionId());
					}

				}

				JSONObject metadata = new JSONObject();

				JSONObject parent = new JSONObject();

				parent.put("nodeRef", dataListFilter.getParentNodeRef());

				JSONObject permissions = new JSONObject();
				JSONObject userAccess = new JSONObject();

				logger.info("###dataListFilter.getParentNodeRef(): " + dataListFilter.getParentNodeRef());
				userAccess
						.put("create",
								((dataListFilter.getSiteId() != "" || dataListFilter.getParentNodeRef() != null) && extractor.hasWriteAccess() && hasWriteAccess && permissionService.hasPermission(dataListFilter.getParentNodeRef(), "CreateChildren") == AccessStatus.ALLOWED));

				permissions.put("userAccess", userAccess);

				parent.put("permissions", permissions);

				metadata.put("parent", parent);
				
				ret.put("metadata", metadata);
				if (dataListFilter.isSimpleItem()) {
					Map<String, Object> item = extractedItems.getPageItems().get(0);
					ret.put("item", new JSONObject(item));
					ret.put("lastSiblingNodeRef", dataListSortService.getLastChild((NodeRef) item.get(AbstractDataListExtractor.PROP_NODE)));
				} else {
					ret.put("items", processResults(extractedItems));
				}

				res.setContentType("application/json");
				res.setContentEncoding("UTF-8");
				ret.write(res.getWriter());
			}

		} catch (JSONException e) {
			throw new WebScriptException("Unable to serialize JSON", e);
		} finally {
			if (logger.isDebugEnabled()) {
				watch.stop();
				logger.debug("EntityDataListWebScript execute in " + watch.getTotalTimeSeconds() + "s");
			}
		}

	}

	private void appendCSVField(CSVConfig csvConfig, List<AttributeExtractorStructure> fields, String prefix) {
		if (fields != null) {
			for (AttributeExtractorStructure field : fields) {
				if (field.isNested()) {
					appendCSVField(csvConfig, field.getChildrens(), field.getFieldName());
				} else {
					if (prefix != null) {
						csvConfig.addField(new CSVField(prefix + "_" + field.getFieldName()));
					} else {
						csvConfig.addField(new CSVField(field.getFieldName()));
					}
				}
			}
		}
	}

	private void appendCSVHeader(Map<String, String> headers, List<AttributeExtractorStructure> fields, String fieldNamePrefix, String titlePrefix) {
		if (fields != null) {
			for (AttributeExtractorStructure field : fields) {
				if (field.isNested()) {
					appendCSVHeader(headers, field.getChildrens(), field.getFieldName(), field.getFieldDef() != null ? field.getFieldDef().getTitle(dictionaryService) : null);
				} else {
					String fieldName = fieldNamePrefix != null ? fieldNamePrefix + "_" + field.getFieldName() : field.getFieldName();
					String fullTitle = titlePrefix != null ? titlePrefix + " - " + field.getFieldDef().getTitle(dictionaryService) : field.getFieldDef().getTitle(dictionaryService);
					headers.put(fieldName, fullTitle);
				}
			}
		}
	}

	private JSONArray processResults(PaginatedExtractedItems extractedItems) {

		JSONArray items = new JSONArray();

		for (Map<String, Object> item : extractedItems.getPageItems()) {
			items.put(new JSONObject(item));
		}

		return items;

	}

	private void writeToCSV(PaginatedExtractedItems extractedItems, CSVWriter csvWriter) {
		for (Map<String, Object> item : extractedItems.getPageItems()) {
			csvWriter.writeRecord(item);
		}
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
