package fr.becpg.repo.entity.datalist.impl;

import java.io.InputStreamReader;
import java.io.Serializable;
import java.time.Instant;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.InvalidQNameException;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.springframework.core.io.FileUrlResource;
import org.springframework.core.io.Resource;
import org.springframework.util.StopWatch;

import fr.becpg.config.format.FormatMode;
import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.datalist.DataListExtractor;
import fr.becpg.repo.entity.datalist.DataListExtractorFactory;
import fr.becpg.repo.entity.datalist.PaginatedExtractedItems;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.helper.SiteHelper;
import fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl.AttributeExtractorStructure;
import fr.becpg.repo.search.PaginatedSearchCache;

public class JsonVersionExtractor implements DataListExtractor {

	private static final Log logger = LogFactory.getLog(JsonVersionExtractor.class);
	
	public static final String PROP_ACCESSRIGHT = "accessRight";

	private static final String PROP_NODE = "nodeRef";

	private static final String PROP_TYPE = "itemType";

	private static final String PROP_CREATED = "createdOn";

	private static final String PROP_CREATOR_DISPLAY = "createdBy";

	private static final String PROP_MODIFIED = "modifiedOn";

	private static final String PROP_MODIFIER_DISPLAY = "modifiedBy";

	private static final String PROP_COLOR = "color";

	private static final String PROP_PERMISSIONS = "permissions";

	private static final String PROP_NODEDATA = "itemData";

	private static final String PROP_LEAF = "isLeaf";
	
	private static final String PROP_OPEN = "open";
	
	private Pattern subTypePattern;
	
	protected PaginatedSearchCache paginatedSearchCache;
	
	protected AttributeExtractorService attributeExtractorService;
	
	protected NamespaceService namespaceService;
	
	protected DataListExtractorFactory dataListExtractorFactory;
	
	protected EntityDictionaryService entityDictionaryService;
	
	private boolean isDefaultExtractor = false;
	
	public JsonVersionExtractor() {
		subTypePattern = Pattern.compile(":([a-z]+)List");
	}
	
	@Override
	public boolean isDefaultExtractor() {
		return isDefaultExtractor;
	}
	
	public void setDefaultExtractor(boolean isDefaultExtractor) {
		this.isDefaultExtractor = isDefaultExtractor;
	}
	
	public void setPaginatedSearchCache(PaginatedSearchCache paginatedSearchCache) {
		this.paginatedSearchCache = paginatedSearchCache;
	}

	public void setAttributeExtractorService(AttributeExtractorService attributeExtractorService) {
		this.attributeExtractorService = attributeExtractorService;
	}
	
	public void setDataListExtractorFactory(DataListExtractorFactory dataListExtractorFactory) {
		this.dataListExtractorFactory = dataListExtractorFactory;
	}
	
	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}
	
	public void setEntityDictionaryService(EntityDictionaryService entityDictionaryService) {
		this.entityDictionaryService = entityDictionaryService;
	}

	public void init() {
		dataListExtractorFactory.registerExtractor(this);
	}
	
	private static JSONObject getListContainer(JSONObject entity) throws JSONException {
		return entity.getJSONObject("datalists");
	}
	
	private static JSONArray filterList(JSONArray list, String name) throws JSONException {
		JSONArray ret = new JSONArray();

		for (int i = 0; i < list.length(); i++) {
			JSONObject object = list.getJSONObject(i);

			if (object.getString("type").equals(name)) {
				ret.put(object);
			}
		}
		
		return ret;
	}
	
	private JSONObject extractListProperties(JSONObject entity) throws JSONException {
		String type = entity.getString("type");
		Matcher subTypeMatcher = subTypePattern.matcher(type);
		if (!subTypeMatcher.find()) {
			return null;
		}
		String subType = subTypeMatcher.group(1);
		JSONObject attributes = entity.getJSONObject("attributes");
		if (subType.equals("compo")) {
			return attributes.getJSONObject(type + "Product");
		} else {
			String field = type + subType.substring(0, 1).toUpperCase() + subType.substring(1);
			return attributes.getJSONObject(field);
		}
	}
	
	private static JSONArray getList(JSONObject entity, DataListFilter dataListFilter) throws JSONException {
		
		JSONObject listsContainer = getListContainer(entity);
		if (listsContainer == null) {
			logger.warn("listContainer was null");
			return null;
		}
		
		QName name = dataListFilter.getDataType();
		if (name == null) {
			logger.warn("dataListFilter has no type");
			return null;
		}
		
		JSONArray list = listsContainer.getJSONArray("bcpg:" + dataListFilter.getDataListName());
		if (list == null) {
			logger.warn("listsContainer has no list");
			return null;
		}
		
		return filterList(list, name.toPrefixString());
		
	}
	
	protected String convertDateValue(Serializable value, FormatMode mode) {
		if (value instanceof Date) {
			return formatDate((Date) value, mode);
		}
		return null;
	}

	protected String formatDate(Date date, FormatMode mode) {
		if (date != null) {
			return attributeExtractorService.getPropertyFormats(mode,false).formatDate(date);
		}
		return null;
	}

	protected Map<String, String> extractPerson(String person) {
		Map<String, String> ret = new HashMap<>(2);
		ret.put("value", person);
		ret.put("displayValue", attributeExtractorService.getPersonDisplayName(person));
		return ret;
	}
	
	private Map<String, Object> extractCommonNodeData(JSONObject object) throws JSONException {
		Map<String, Object> ret = new HashMap<>();
		ret.put("displayValue", object.getString(ContentModel.PROP_NAME.toPrefixString(namespaceService)));
		ret.put("value", "workspace://SpacesStore/" + object.getString("id"));
		ret.put("siteId", SiteHelper.extractSiteId(object.getString("path")));
		ret.put("metadata", "rawMaterial-Simulation");
		return ret;
	}

	private Object extractNodeData(JSONObject object, JSONObject properties, Locale locale,
			ClassAttributeDefinition attribute, FormatMode mode, Integer order) throws JSONException {
		if (attribute instanceof PropertyDefinition) {
			String displayName = null;
			if (locale != null) {
				logger.warn("Locale " + locale.getCountry() + " detected. ML support has not been tested yet.");
				displayName = properties.getString("displayName" + locale.toLanguageTag());
			} else {
				try {
					displayName = properties.getString(attribute.getName().toPrefixString(namespaceService));
				} catch (JSONException e) {
					// Ignore and leave displayName null
				}
			}

			HashMap<String, Object> tmp = new HashMap<>(6);
			tmp.put("displayValue", displayName);
			tmp.put("value", displayName);
			tmp.put("metadata", extractListProperties(object).getString("metadata"));
			return tmp;
		} else if (attribute instanceof AssociationDefinition) {
			if (((AssociationDefinition) attribute).isChild()) {
				logger.warn(attribute.getName() + " is a child association. Child associations haven't been tested yet.");
			}
			
			List<Map<String, Object>> ret = new ArrayList<>();
			
			Object child = properties.get(attribute.getName().toPrefixString(namespaceService));
			if (child instanceof JSONObject) {
				ret.add(extractCommonNodeData((JSONObject) child));
			}
			return ret;
		}
		return null;
	}
	
	private ClassAttributeDefinition getFieldDef(QName itemType, AttributeExtractorStructure field) {

		if (!field.getItemType().equals(itemType)) {
			return entityDictionaryService.findMatchingPropDef(field.getItemType(), itemType, field.getFieldQname());
		}
		return field.getFieldDef();
	}
	
	private Map<String, Object> doExtract(JSONObject object, QName itemType, List<AttributeExtractorStructure> metadataFields,
			FormatMode mode, JSONObject properties, Map<String, Object> props) throws JSONException {
		Map<String, Object> ret = new HashMap<>();

		Integer order = 0;
		
		for (AttributeExtractorStructure field : metadataFields) {
			ret.put(field.getFieldName(), extractNodeData(object, properties, field.getLocale(), getFieldDef(itemType, field), mode, order++));
		}
		
		Object insertProp = props.get(PROP_LEAF);
		if (insertProp != null) {
			ret.put(PROP_LEAF, insertProp);
		} else {
			logger.warn("Had no " + PROP_LEAF);
		}
		
		insertProp = props.get(PROP_OPEN);
		if (insertProp != null) {
			ret.put(PROP_OPEN, insertProp);
		} else {
			logger.warn("Had no " + PROP_OPEN);
		}

		return ret;
	}

	private Map<String, Object> extractJSON(JSONObject object, List<AttributeExtractorStructure> metadataFields, Map<String, Object> props) throws InvalidQNameException, JSONException {
		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}
		try {

			QName itemType = QName.createQName(object.getString("type"), namespaceService);
			JSONObject properties = object.getJSONObject("attributes");
			
			Map<String, Object> ret = new HashMap<>(20);
			
			ret.put(PROP_NODE, new NodeRef("workspace://SpacesStore/" + object.getString("id")));
			
			// Skipping condition
			
			ret.put(PROP_TYPE, itemType.toPrefixString(namespaceService));
			
			// Date parsing test
			Instant instantCreated = Instant.parse(properties.getString(ContentModel.PROP_CREATED.toPrefixString(namespaceService)));
			ret.put(PROP_CREATED, convertDateValue(Date.from(instantCreated), FormatMode.JSON));
			ret.put(PROP_CREATOR_DISPLAY, extractPerson(properties.getString(ContentModel.PROP_CREATOR.toPrefixString(namespaceService))));
			Instant instantModified = Instant.parse(properties.getString(ContentModel.PROP_MODIFIED.toPrefixString(namespaceService)));
			ret.put(PROP_MODIFIED, convertDateValue(Date.from(instantModified), FormatMode.JSON));
			ret.put(PROP_MODIFIER_DISPLAY, extractPerson(properties.getString(ContentModel.PROP_MODIFIER.toPrefixString(namespaceService))));
			
			try {
				ret.put(PROP_COLOR, properties.getString(BeCPGModel.PROP_COLOR.toPrefixString(namespaceService)));
			} catch (JSONException e) {
				// Ignore, this just means there is no string or that it isn't a string
			}
			
			Map<String, Map<String, Boolean>> permissions = new HashMap<>(1);
			Map<String, Boolean> userAccess = new HashMap<>(5);

			// Read-only access
			userAccess.put("delete", false);
			userAccess.put("create", false);
			userAccess.put("edit", false);
			userAccess.put("sort", false);
			userAccess.put("details", false);
			userAccess.put("wused", false);
			userAccess.put("content", false);
			permissions.put("userAccess", userAccess);

			ret.put(PROP_PERMISSIONS, permissions);

			ret.put(PROP_NODEDATA, doExtract(object, itemType, metadataFields, FormatMode.JSON, properties, props));
			
			return ret;

		} finally {
			if (logger.isDebugEnabled()) {
				watch.stop();
				logger.debug(getClass().getSimpleName() + " extract metadata in  " + watch.getTotalTimeSeconds() + " s");
			}
		}
	}

	@Override
	public PaginatedExtractedItems extract(DataListFilter dataListFilter, List<String> metadataFields) {

		PaginatedExtractedItems ret = new PaginatedExtractedItems(dataListFilter.getPagination().getPageSize());

		Resource resource;
		try {
			/*
			 * To test, use remote (format=json_all), download the result and use docker cp to copy it
			 * into target_becpg_1:/root/entity_test.json
			 * 
			 * TODO: remove this and run it another way
			 */
			resource = new FileUrlResource("entity_test.json");
			
			JSONTokener tokener;
			tokener = new JSONTokener(new InputStreamReader(resource.getInputStream()));
			JSONObject data = new JSONObject(tokener);
			
			JSONArray results = getList(data.getJSONObject("entity"), dataListFilter);
			
			if (results.length() == 0) {
				logger.warn("List is empty");
			}
			
			Map<String, Object> props = new HashMap<>();
			props.put(PROP_ACCESSRIGHT, dataListFilter.hasWriteAccess());

			for (int i = 0; i < results.length(); i++) {
				JSONObject object = results.getJSONObject(i);
				
				if (ret.getComputedFields() == null) {
					ret.setComputedFields(attributeExtractorService.readExtractStructure(QName.createQName(object.getString("type"), namespaceService), metadataFields));
				}
				
				if (RepoConsts.FORMAT_CSV.equals(dataListFilter.getFormat()) || RepoConsts.FORMAT_XLSX.equals(dataListFilter.getFormat())) {
					logger.warn("CSV and XLSX unimplemented!");
				} else {
					ret.addItem(extractJSON(object, ret.getComputedFields(), props));
				}
			}

			ret.setFullListSize(dataListFilter.getPagination().getFullListSize());

		} catch (IOException|JSONException e) {
			logger.error("Failed to extract", e);
		}

		if (ret.getPageItems().size() > 0) {
			logger.debug("First itemData is " + ret.getPageItems().get(0).get("itemData"));
		}
		return ret;
	}

	@Override
	public boolean applyTo(DataListFilter dataListFilter) {
		return dataListFilter.isVersionFilter();
	}

	@Override
	public Date computeLastModified(DataListFilter dataListFilter) {
		return null;
	}

	@Override
	public boolean hasWriteAccess() {
		return false;
	}

}
