package fr.becpg.repo.entity.datalist.impl;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StopWatch;

import fr.becpg.config.format.FormatMode;
import fr.becpg.model.BeCPGModel;
import fr.becpg.model.BeCPGModel.EntityFormat;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.activity.extractor.ActivityListExtractor;
import fr.becpg.repo.entity.EntityFormatService;
import fr.becpg.repo.entity.datalist.PaginatedExtractedItems;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.helper.SiteHelper;
import fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl.AttributeExtractorStructure;

public class JsonVersionExtractor extends ActivityListExtractor {

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

	private static final String VALUE = "value";
	
	private static final String DISPLAY_VALUE = "displayValue";
	
	private static final String METADATA = "metadata";
	
	private static final String ATTRIBUTES = "attributes";
	
	
	private static final String[] AL_DATA_PROPS = {"datalistType", "charactType", "entityType", "datalistNodeRef", "entityNodeRef", "className", "title", "charactNodeRef" };
	
	private static final String BCPG_PREFIX = "bcpg:";
			
	@Autowired
	protected NamespaceService namespaceService;
	
	@Autowired
	protected EntityFormatService entityFormatService;
	
	private boolean isDefaultExtractor = false;
	
	@Override
	public boolean isDefaultExtractor() {
		return isDefaultExtractor;
	}
	
	public void setEntityFormatService(EntityFormatService entityJsonService) {
		this.entityFormatService = entityJsonService;
	}

	private JSONArray filterList(JSONArray list, DataListFilter dataListFilter) throws JSONException {
		JSONArray ret = new JSONArray();

		for (int i = 0; i < list.length(); i++) {
			JSONObject object = list.getJSONObject(i);

			if (canAddObject(object, dataListFilter)) {
				ret.put(object);
			}
		}

		return ret;
	}
	
	private boolean canAddObject(JSONObject object, DataListFilter dataListFilter) throws JSONException {
		
		if (!object.getString("type").equals(dataListFilter.getDataType().toPrefixString())) {
			return false;
		}
		
		if (dataListFilter.getFilterData() == null) {
			return true;
		}
		
		String[] filterDataArray = dataListFilter.getFilterData().replace("{", "").replace("}", "").split(",");
		
		for (String filterData : filterDataArray) {
			
			if (filterData.split(":").length < 2) {
				continue;
			}
			
			if (!canAddObject(object, filterData)) {
				return false;
			}
		}

		return true;
	}

	private boolean canAddObject(JSONObject object, String filterData) throws JSONException {
		String filterName = filterData.split(":")[0].replace("\"", "");
		
		if (filterName.startsWith("prop_bcpg_")) {
			String prop = BCPG_PREFIX + filterName.replace("prop_bcpg_", "");
			String value = filterData.split(":")[1].replace("\"", "");
			
			if (!value.equals("") && (!object.getJSONObject(ATTRIBUTES).has(prop) || !object.getJSONObject(ATTRIBUTES).getString(prop).equals(value))) {
				return false;
			}
		} else if (filterName.startsWith("assoc_bcpg_")) {
			String assoc = BCPG_PREFIX + filterName.replace("assoc_bcpg_", "").replace("_added", "");
			String value = filterData.split("\":\"")[1].replace("\"", "");

			if (!canAddAssoc(object, assoc, value)) {
				return false;
			}
		}
		return true;
	}

	private boolean canAddAssoc(JSONObject object, String assoc, String value) throws JSONException {
		
		NodeRef nodeRef = new NodeRef(value);

		if (!object.getJSONObject(ATTRIBUTES).has(assoc)) {
			return false;
		}
		
		if (object.getJSONObject(ATTRIBUTES).get(assoc) instanceof JSONArray) {
			JSONArray associationArray = (JSONArray) object.getJSONObject(ATTRIBUTES).get(assoc);
			
			boolean hasObject = false;
			
			for (int j = 0; j < associationArray.length(); j++) {
				
				if (associationArray.getJSONObject(j).get("id").equals(nodeRef.getId())) {
					hasObject = true;
					break;
				}
			}
			
			if (!hasObject) {
				return false;
			}
		} else if (object.getJSONObject(ATTRIBUTES).get(assoc) instanceof JSONObject && !((JSONObject) object.getJSONObject(ATTRIBUTES).get(assoc)).get("id").equals(nodeRef.getId())) {
			return false;
		}
		
		return true;
	}

	private JSONArray sortList(JSONArray list, DataListFilter dataListFilter) throws JSONException {

		List<JSONObject> jsonValues = new ArrayList<>();
		for (int i = 0; i < list.length(); i++) {
			jsonValues.add(list.getJSONObject(i));
		}
		
		String sortString = null;
		
		int order = 1;
		
		if (dataListFilter.isDefaultSort()) {
			sortString = "bcpg:sort";
		} else {
			for (String key : dataListFilter.getSortMap().keySet()) {
				sortString = BCPG_PREFIX + key.split("}")[1];
				if (!Boolean.TRUE.equals(dataListFilter.getSortMap().get(key))) {
					order = -1;
				}
			}
		}
		
		JsonComparator jsonComparator = new JsonComparator(sortString, order);
		
		jsonValues.sort(jsonComparator);

		jsonValues = dataListFilter.getPagination().paginate(jsonValues);
		
		JSONArray sortedJsonArray = new JSONArray();

		for (int i = 0; i < jsonValues.size(); i++) {
			sortedJsonArray.put(jsonValues.get(i));
		}

		return sortedJsonArray;
	}
	
	private Map<String, Object> extractAssociationNodeData(JSONObject object) throws JSONException {
		Map<String, Object> ret = new HashMap<>();
		
		NodeRef nodeRef = new NodeRef("workspace://SpacesStore/" + object.getString("id"));
		
		if (nodeService.exists(nodeRef)) {
			QName type = nodeService.getType(nodeRef);
			ret.put(DISPLAY_VALUE, attributeExtractorService.extractPropName(type, nodeRef));
			ret.put(VALUE, nodeRef.toString());
		} else {
			if (object.has(BeCPGModel.PROP_CHARACT_NAME.toPrefixString(namespaceService))) {
				ret.put(DISPLAY_VALUE, object.getString(BeCPGModel.PROP_CHARACT_NAME.toPrefixString(namespaceService)));
			} else {
				ret.put(DISPLAY_VALUE, object.getString(ContentModel.PROP_NAME.toPrefixString(namespaceService)));
			}
		}
		
		ret.put("siteId", SiteHelper.extractSiteId(object.getString("path")));
		ret.put(METADATA, object.getString(METADATA));
		
		return ret;
	}

	private Object extractNodeData(JSONObject properties, ClassAttributeDefinition attribute, FormatMode mode) throws JSONException {
		if (attribute instanceof PropertyDefinition) {
			if (attribute.getName().toPrefixString(namespaceService).contains("alData")) {
				return extractAlData((JSONObject) properties.get(attribute.getName().toPrefixString(namespaceService)));
			}
			return extractPropertyNodeData(properties, (PropertyDefinition) attribute, mode);
		} else if (attribute instanceof AssociationDefinition) {
			return extractAssociationDefinitionNodeData(properties, (AssociationDefinition) attribute);
		}
		
		return null;
	}
	
	private Object extractAssociationDefinitionNodeData(JSONObject properties, AssociationDefinition attribute) throws JSONException {
		
		if (attribute.isChild()) {
			logger.warn(attribute.getName() + " is a child association. Child associations haven't been tested yet.");
		}
		
		List<Map<String, Object>> ret = new ArrayList<>();
		
		if (properties.has(attribute.getName().toPrefixString(namespaceService))) {
			Object child = properties.get(attribute.getName().toPrefixString(namespaceService));
			if (child instanceof JSONObject) {
				ret.add(extractAssociationNodeData((JSONObject) child));
			} else if (child instanceof JSONArray) {
				JSONArray array = (JSONArray) child;
				
				for (int i = 0; i < array.length(); i++) {
					
					if (array.get(i) instanceof JSONObject) {
						ret.add(extractAssociationNodeData((JSONObject) array.get(i)));
					}
				}
			}
		}
		return ret;
	}

	private HashMap<String, Object> extractPropertyNodeData(JSONObject properties, PropertyDefinition attribute, FormatMode mode) throws JSONException {
		String displayName = null;
		Object value = null;
		String metadata = null;
		
		HashMap<String, Object> tmp = new HashMap<>(6);

		if (!properties.has(attribute.getName().toPrefixString(namespaceService))) {
			return tmp;
		}
		
		value = properties.getString(attribute.getName().toPrefixString(namespaceService));
		metadata = attribute.getDataType().getName().getLocalName();
		Serializable seri = (Serializable) value;

		if (metadata.equals("double")) {
			seri = Double.parseDouble((String) value);
			displayName = attributeExtractorService.getStringValue(attribute, seri, attributeExtractorService.getPropertyFormats(mode, false));
			value = seri;
		} else if (metadata.equals("int")) {
			seri = Integer.parseInt((String) value);
			displayName = attributeExtractorService.getStringValue(attribute, seri, attributeExtractorService.getPropertyFormats(mode, false));
			value = seri;
		} else if (metadata.equals("boolean")) {
			seri = Boolean.parseBoolean((String) value);
			displayName = attributeExtractorService.getStringValue(attribute, seri, attributeExtractorService.getPropertyFormats(mode, false));
			value = seri;
		} else if (metadata.equals("datetime")) {

			String dateString = (String) value;

			String yearMonthDay = dateString.split("T")[0];

			int year = Integer.parseInt(yearMonthDay.split("-")[0]);
			int month = Integer.parseInt(yearMonthDay.split("-")[1]);
			int day = Integer.parseInt(yearMonthDay.split("-")[2]);

			String hourMinSec = dateString.split("T")[1].split("\\.")[0];

			int hour = Integer.parseInt(hourMinSec.split(":")[0], 10);
			int min = Integer.parseInt(hourMinSec.split(":")[1], 10);
			int sec = Integer.parseInt(hourMinSec.split(":")[2], 10);

			Calendar cal = Calendar.getInstance();

			cal.set(year, month - 1, day, hour, min, sec);

			displayName = attributeExtractorService.getStringValue(attribute, cal.getTime(), attributeExtractorService.getPropertyFormats(mode, false));

		} else {
			displayName = attributeExtractorService.getStringValue(attribute, seri, attributeExtractorService.getPropertyFormats(mode, false));
		}

		tmp.put(DISPLAY_VALUE, displayName);
		tmp.put(VALUE, value);
		tmp.put(METADATA, metadata);
		return tmp;

	}

	private HashMap<String, Object> extractAlData(JSONObject value) throws JSONException {
		
		HashMap<String, Object> ret = new HashMap<>();
		
		for (String prop : AL_DATA_PROPS) {
			if (value.has(prop)) {
				ret.put(prop, value.getString(prop));
			}
		}
		
		return ret;
	}

	private ClassAttributeDefinition getFieldDef(QName itemType, AttributeExtractorStructure field) {

		if (!field.getItemType().equals(itemType)) {
			return entityDictionaryService.findMatchingPropDef(field.getItemType(), itemType, field.getFieldQname());
		}
		return field.getFieldDef();
	}
	
	private Map<String, Object> doExtract(QName itemType, List<AttributeExtractorStructure> metadataFields,
			FormatMode mode, JSONObject properties) throws JSONException {
		Map<String, Object> ret = new HashMap<>();

		for (AttributeExtractorStructure field : metadataFields) {
			ret.put(field.getFieldName(), extractNodeData(properties, getFieldDef(itemType, field), mode));
		}
		
		return ret;
	}

	private Map<String, Object> extractJSON(DataListFilter dataListFilter, JSONObject object, List<AttributeExtractorStructure> metadataFields) throws JSONException {
		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}
		try {

			QName itemType = QName.createQName(object.getString("type"), namespaceService);
			JSONObject properties = object.getJSONObject(ATTRIBUTES);
			
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

			ret.put(PROP_NODEDATA, doExtract(itemType, metadataFields, FormatMode.JSON, properties));
			
			QName dataListFilterQName = QName.createQName(BeCPGModel.BECPG_URI, dataListFilter.getDataListName());
			
			Map<QName, Serializable> propertiesMap = new HashMap<>();
			
			Iterator<?> it = properties.keys();
			
			while (it.hasNext()) {
			    String name = (String) it.next();
			    if (name.startsWith("cm:")) {
			    	QName qname = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name.split("cm:")[1]);
			    	propertiesMap.put(qname, (Serializable) properties.get(name).toString());
			    } else if (name.startsWith(BCPG_PREFIX)) {
			    	QName qname = QName.createQName(BeCPGModel.BECPG_URI, name.split(BCPG_PREFIX)[1]);
			    	propertiesMap.put(qname, (Serializable) properties.get(name).toString());
			    }
			}
			
			if (BeCPGModel.TYPE_ACTIVITY_LIST.equals(dataListFilterQName)) {
				postLookupActivity(dataListFilter.getEntityNodeRef(), ret, propertiesMap, FormatMode.JSON);
			}
			
			return ret;

		} finally {
			if (logger.isDebugEnabled() && watch != null) {
				watch.stop();
				logger.debug(getClass().getSimpleName() + " extract metadata in  " + watch.getTotalTimeSeconds() + " s");
			}
		}
	}

	private String extractData(DataListFilter dataListFilter) {
		
		if (dataListFilter.getFilterData() != null) {
			String[] filterDataArray = dataListFilter.getFilterData().replace("{", "").replace("}", "").split(",");
			
			for (String filterData : filterDataArray) {
				
				if (filterData.split(":").length < 2) {
					continue;
				}
				
				if (filterData.startsWith("workspace")) {
					NodeRef versionNodeRef = new NodeRef(filterData);
					
					return entityFormatService.getEntityData(versionNodeRef);
				}
			}
		}
		
		return entityFormatService.getEntityData(dataListFilter.getEntityNodeRef());
	}
	
	@Override
	public PaginatedExtractedItems extract(DataListFilter dataListFilter, List<String> metadataFields) {

		PaginatedExtractedItems ret = new PaginatedExtractedItems(dataListFilter.getPagination().getPageSize());

		try {
			
			String data = extractData(dataListFilter);
			
			JSONObject entityJson = new JSONObject(data);
			
			JSONObject entity = (JSONObject) entityJson.get("entity");
			
			JSONObject datalists = (JSONObject) entity.get("datalists");
			
			JSONArray dataListJsonArray = (JSONArray) datalists.get(BCPG_PREFIX + dataListFilter.getDataListName());

			JSONArray filteredList = filterList(dataListJsonArray, dataListFilter);
			
			JSONArray results = sortList(filteredList, dataListFilter);

			if (results.length() == 0) {
				logger.warn("List is empty");
			}
			
			for (int i = 0; i < results.length(); i++) {
				JSONObject object = results.getJSONObject(i);
				
				if (ret.getComputedFields() == null) {
					ret.setComputedFields(attributeExtractorService.readExtractStructure(QName.createQName(object.getString("type"), namespaceService), metadataFields));
				}
				
				if (RepoConsts.FORMAT_CSV.equals(dataListFilter.getFormat()) || RepoConsts.FORMAT_XLSX.equals(dataListFilter.getFormat())) {
					logger.warn("CSV and XLSX unimplemented!");
				} else {
					
					Map<String, Object> item = extractJSON(dataListFilter, object, ret.getComputedFields());
					ret.addItem(item);
				}
				
			}

			ret.setFullListSize(dataListFilter.getPagination().getFullListSize());

		} catch (JSONException e) {
			logger.error("Failed to extract", e);
		}

		if (!ret.getPageItems().isEmpty()) {
			logger.debug("First itemData is " + ret.getPageItems().get(0).get(PROP_NODEDATA));
		}
		return ret;
	}

	@Override
	public boolean applyTo(DataListFilter dataListFilter) {
		
		NodeRef targetNodeRef = dataListFilter.getEntityNodeRef();
		
		if (dataListFilter.getFilterData() != null) {
			String[] filterDataArray = dataListFilter.getFilterData().replace("{", "").replace("}", "").split(",");
			
			for (String filterData : filterDataArray) {
				if (filterData.split(":").length < 2) {
					continue;
				}
				
				if (filterData.startsWith("workspace")) {
					targetNodeRef = new NodeRef(filterData);
				}
			}
		}
		
		return EntityFormat.JSON.toString().equals(entityFormatService.getEntityFormat(targetNodeRef));
	}

	@Override
	public Date computeLastModified(DataListFilter dataListFilter) {
		return null;
	}

	@Override
	public boolean hasWriteAccess() {
		return false;
	}
	
	public class JsonComparator implements Comparator<JSONObject> {

		private String sortString;
		private int order;
		
		public JsonComparator(String sortString, int order) {
			this.sortString = sortString;
			this.order = order;
		}
		
		@Override
		public int compare(JSONObject a, JSONObject b) {
			String sortA = "";
			String sortB = "";

			try {
				if (a.getJSONObject(ATTRIBUTES).has(sortString)) {
					sortA = a.getJSONObject(ATTRIBUTES).getString(sortString);
				} else {
					return -order;
				}
				if (b.getJSONObject(ATTRIBUTES).has(sortString)) {
					sortB = b.getJSONObject(ATTRIBUTES).getString(sortString);
				} else {
					return order;
				}
			} catch (JSONException e) {
				logger.warn("comparison error", e);
			}
			
			try {
				Double doubleA = Double.parseDouble(sortA);
				Double doubleB = Double.parseDouble(sortB);
				
				return order * doubleA.compareTo(doubleB);
			} catch (NumberFormatException e) {
				// do nothing : let the String comparator do the work
			}

			return order * sortA.compareTo(sortB);
		}
		
	}

}
