package fr.becpg.repo.entity.datalist.impl;

import java.io.Serializable;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.dictionary.AssociationDefinition;
import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.StoreRef;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.extensions.surf.util.I18NUtil;
import org.springframework.util.StopWatch;

import fr.becpg.config.format.FormatMode;
import fr.becpg.model.BeCPGModel;
import fr.becpg.model.BeCPGModel.EntityFormat;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.entity.EntityFormatService;
import fr.becpg.repo.entity.datalist.PaginatedExtractedItems;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.entity.remote.RemoteEntityService;
import fr.becpg.repo.helper.SiteHelper;
import fr.becpg.repo.helper.impl.AttributeExtractorField;
import fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl.AttributeExtractorStructure;

/**
 * <p>JSONVersionExtractor class.</p>
 *
 * @author matthieu
 */
public class JSONVersionExtractor extends SimpleExtractor {

	/** Constant <code>logger</code> */
	private static final Log logger = LogFactory.getLog(JSONVersionExtractor.class);
	
	/** Constant <code>PROP_ACCESSRIGHT="accessRight"</code> */
	public static final String PROP_ACCESSRIGHT = "accessRight";

	/** Constant <code>PROP_NODE="nodeRef"</code> */
	private static final String PROP_NODE = "nodeRef";

	/** Constant <code>PROP_TYPE="itemType"</code> */
	private static final String PROP_TYPE = "itemType";

	/** Constant <code>PROP_CREATED="createdOn"</code> */
	private static final String PROP_CREATED = "createdOn";

	/** Constant <code>PROP_CREATOR_DISPLAY="createdBy"</code> */
	private static final String PROP_CREATOR_DISPLAY = "createdBy";

	/** Constant <code>PROP_MODIFIED="modifiedOn"</code> */
	private static final String PROP_MODIFIED = "modifiedOn";

	/** Constant <code>PROP_MODIFIER_DISPLAY="modifiedBy"</code> */
	private static final String PROP_MODIFIER_DISPLAY = "modifiedBy";

	/** Constant <code>PROP_COLOR="color"</code> */
	private static final String PROP_COLOR = "color";

	/** Constant <code>PROP_PERMISSIONS="permissions"</code> */
	private static final String PROP_PERMISSIONS = "permissions";

	/** Constant <code>PROP_NODEDATA="itemData"</code> */
	private static final String PROP_NODEDATA = "itemData";

	/** Constant <code>VALUE="value"</code> */
	private static final String VALUE = "value";
	
	/** Constant <code>DISPLAY_VALUE="displayValue"</code> */
	private static final String DISPLAY_VALUE = "displayValue";
	
	/** Constant <code>METADATA="metadata"</code> */
	private static final String METADATA = "metadata";
	
	/** Constant <code>ATTRIBUTES="attributes"</code> */
	private static final String ATTRIBUTES = "attributes";
	
	/** Constant <code>TYPE="type"</code> */
	private static final String TYPE = "type";
	
	/** Constant <code>VERSION="version"</code> */
	private static final String VERSION = "version";
	
	/** Constant <code>CM_NAME="cm:name"</code> */
	private static final String CM_NAME = "cm:name";

	/** Constant <code>AL_DATA_PROPS</code> */
	private static final String[] AL_DATA_PROPS = {"datalistType", "charactType", "entityType", "datalistNodeRef", "entityNodeRef", "className", "title", "charactNodeRef" };
	
	/** Constant <code>BCPG_PREFIX="bcpg:"</code> */
	private static final String BCPG_PREFIX = "bcpg:";
	
	/**
	 * Regular expression to match any Universally Unique Identifier (UUID), in a case-insensitive
	 * fashion.
	 */
	public static final String UUID_STRING = "[a-fA-F0-9]{8}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{4}-[a-fA-F0-9]{12}";

			
	@Autowired
	protected NamespaceService namespaceService;
	
	@Autowired
	protected EntityFormatService entityFormatService;
	
	private boolean isDefaultExtractor = false;
	
	/** {@inheritDoc} */
	@Override
	public boolean isDefaultExtractor() {
		return isDefaultExtractor;
	}
	
	/**
	 * <p>Setter for the field <code>entityFormatService</code>.</p>
	 *
	 * @param entityJsonService a {@link fr.becpg.repo.entity.EntityFormatService} object
	 */
	public void setEntityFormatService(EntityFormatService entityJsonService) {
		this.entityFormatService = entityJsonService;
	}
	
	/**
	 * <p>filterList.</p>
	 *
	 * @param list a {@link org.json.JSONArray} object
	 * @param dataListFilter a {@link fr.becpg.repo.entity.datalist.data.DataListFilter} object
	 * @return a {@link org.json.JSONArray} object
	 * @throws org.json.JSONException if any.
	 */
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
	
	/**
	 * <p>canAddObject.</p>
	 *
	 * @param object a {@link org.json.JSONObject} object
	 * @param dataListFilter a {@link fr.becpg.repo.entity.datalist.data.DataListFilter} object
	 * @return a boolean
	 * @throws org.json.JSONException if any.
	 */
	private boolean canAddObject(JSONObject object, DataListFilter dataListFilter) throws JSONException {
		
		if (!object.getString(TYPE).equals(dataListFilter.getDataType().toPrefixString())) {
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

	/**
	 * <p>canAddObject.</p>
	 *
	 * @param object a {@link org.json.JSONObject} object
	 * @param filterData a {@link java.lang.String} object
	 * @return a boolean
	 * @throws org.json.JSONException if any.
	 */
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

	/**
	 * <p>canAddAssoc.</p>
	 *
	 * @param object a {@link org.json.JSONObject} object
	 * @param assoc a {@link java.lang.String} object
	 * @param value a {@link java.lang.String} object
	 * @return a boolean
	 * @throws org.json.JSONException if any.
	 */
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

	/**
	 * <p>sortList.</p>
	 *
	 * @param list a {@link org.json.JSONArray} object
	 * @param dataListFilter a {@link fr.becpg.repo.entity.datalist.data.DataListFilter} object
	 * @return a {@link org.json.JSONArray} object
	 * @throws org.json.JSONException if any.
	 */
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
	
	/**
	 * <p>extractAssociationNodeData.</p>
	 *
	 * @param object a {@link org.json.JSONObject} object
	 * @return a {@link java.util.Map} object
	 * @throws org.json.JSONException if any.
	 */
	private Map<String, Object> extractAssociationNodeData(JSONObject object) throws JSONException {
		Map<String, Object> ret = new HashMap<>();
		
		NodeRef nodeRef = new NodeRef("workspace://SpacesStore/" + object.getString("id"));
		String displayValue = null;
		QName type = null;
		if (nodeService.exists(nodeRef)) {
			ret.put(VALUE, nodeRef.toString());
			type = nodeService.getType(nodeRef);
			displayValue = attributeExtractorService.extractPropName(type, nodeRef);
		}
		
		if (displayValue == null) {
			if (object.has(BeCPGModel.PROP_CHARACT_NAME.toPrefixString(namespaceService))) {
				if (object.get(BeCPGModel.PROP_CHARACT_NAME.toPrefixString(namespaceService)) instanceof JSONObject json) {
					if (json.has(I18NUtil.getLocale().toLanguageTag())) {
						displayValue = json.getString(I18NUtil.getLocale().toLanguageTag());
					} else if (json.has(Locale.getDefault().toLanguageTag())) {
						displayValue = json.getString(Locale.getDefault().toLanguageTag());
					} else if (json.has(Locale.ENGLISH.toLanguageTag())) {
						displayValue = json.getString(Locale.ENGLISH.toLanguageTag());
					}
				}
				if (displayValue == null) {
					displayValue = object.getString(BeCPGModel.PROP_CHARACT_NAME.toPrefixString(namespaceService));
				}
			} else if (object.has(ContentModel.PROP_NAME.toPrefixString(namespaceService))) {
				displayValue = object.getString(ContentModel.PROP_NAME.toPrefixString(namespaceService));
			}
		}
		
		if (displayValue == null) {
			displayValue = "";
		}
		
		if (object.has(RemoteEntityService.ATTR_VERSION)) {
			String versionLabel = (String) object.get(RemoteEntityService.ATTR_VERSION);
			ret.put(VERSION, versionLabel);
			displayValue += displayValue.endsWith(RepoConsts.VERSION_NAME_DELIMITER + versionLabel) ? "" : RepoConsts.VERSION_NAME_DELIMITER + versionLabel;
		}
		
		ret.put(DISPLAY_VALUE, displayValue);
		ret.put("siteId", SiteHelper.extractSiteId(object.getString("path")));
		ret.put(METADATA, object.getString(METADATA));
		
		return ret;
	}
	
	/**
	 * <p>extractNodeData.</p>
	 *
	 * @param properties a {@link org.json.JSONObject} object
	 * @param attribute a {@link org.alfresco.service.cmr.dictionary.ClassAttributeDefinition} object
	 * @param mode a {@link fr.becpg.config.format.FormatMode} object
	 * @param itemType a {@link org.alfresco.service.namespace.QName} object
	 * @return a {@link java.lang.Object} object
	 * @throws org.json.JSONException if any.
	 */
	private Object extractNodeData(JSONObject properties, ClassAttributeDefinition attribute, FormatMode mode, QName itemType) throws JSONException {
		if (attribute instanceof PropertyDefinition) {
			if (attribute.getName().toPrefixString(namespaceService).contains("alData")) {
				return extractAlData((JSONObject) properties.get(attribute.getName().toPrefixString(namespaceService)));
			}
			return extractPropertyNodeData(properties, (PropertyDefinition) attribute, mode, itemType);
		} else if (attribute instanceof AssociationDefinition) {
			return extractAssociationDefinitionNodeData(properties, (AssociationDefinition) attribute);
		}
		
		return null;
	}
	
	/**
	 * <p>extractAssociationDefinitionNodeData.</p>
	 *
	 * @param properties a {@link org.json.JSONObject} object
	 * @param attribute a {@link org.alfresco.service.cmr.dictionary.AssociationDefinition} object
	 * @return a {@link java.lang.Object} object
	 * @throws org.json.JSONException if any.
	 */
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

	/**
	 * <p>extractPropertyNodeData.</p>
	 *
	 * @param properties a {@link org.json.JSONObject} object
	 * @param attribute a {@link org.alfresco.service.cmr.dictionary.PropertyDefinition} object
	 * @param mode a {@link fr.becpg.config.format.FormatMode} object
	 * @param itemType a {@link org.alfresco.service.namespace.QName} object
	 * @return a {@link java.util.HashMap} object
	 * @throws org.json.JSONException if any.
	 */
	private HashMap<String, Object> extractPropertyNodeData(JSONObject properties, PropertyDefinition attribute, FormatMode mode, QName itemType) throws JSONException {
		String displayName = null;
		Object value = null;
		String metadata = attribute.getDataType().getName().getLocalName();
		
		HashMap<String, Object> tmp = HashMap.newHashMap(6);

		if (!properties.has(attribute.getName().toPrefixString(namespaceService))) {
			
			if (itemType != null && properties.has(itemType.toPrefixString(namespaceService))) {
				String id = ((JSONObject) properties.get(itemType.toPrefixString(namespaceService))).getString("id");
				NodeRef node = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, id);
				if (nodeService.exists(node)) {
					value = nodeService.getProperty(node, attribute.getName());
					displayName = attributeExtractorService.getStringValue(attribute, (Serializable) value, attributeExtractorService.getPropertyFormats(mode, false));
				}
			}
			
			tmp.put(DISPLAY_VALUE, displayName);
			tmp.put(VALUE, value);
			tmp.put(METADATA, metadata);
			return tmp;
		}
		
		value = properties.get(attribute.getName().toPrefixString(namespaceService)).toString();
		
		Serializable seri = (Serializable) value;

		if (metadata.equals("double")) {
			seri = Double.parseDouble((String) value);
			displayName = attributeExtractorService.getStringValue(attribute, seri, attributeExtractorService.getPropertyFormats(mode, false));
			value = seri;
		} else if (metadata.equals("float")) {
			seri = Float.parseFloat((String) value);
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

		} else if (metadata.equals("date")) {

			String dateString = (String) value;

			String yearMonthDay = dateString.split("T")[0];

			int year = Integer.parseInt(yearMonthDay.split("-")[0]);
			int month = Integer.parseInt(yearMonthDay.split("-")[1]);
			int day = Integer.parseInt(yearMonthDay.split("-")[2]);

			Calendar cal = Calendar.getInstance();

			cal.set(year, month - 1, day, 0, 0, 0);

			displayName = attributeExtractorService.getStringValue(attribute, cal.getTime(), attributeExtractorService.getPropertyFormats(mode, false));

		} else if (metadata.equals("mltext")) {
			if (properties.has(attribute.getName().toPrefixString(namespaceService) + "_" + I18NUtil.getLocale().toLanguageTag())) {
				displayName = properties.getString(attribute.getName().toPrefixString(namespaceService) + "_" + I18NUtil.getLocale().toLanguageTag());
			} else {
				displayName = properties.getString(attribute.getName().toPrefixString(namespaceService));
			}
		} else if (metadata.equals("noderef")) {
			
			displayName = extractJsonNodeRefProp(seri);
			
		} else if (metadata.equals("text") && attribute.isMultiValued()) {
			seri = (Serializable) convertStringToList(seri.toString());
			displayName = attributeExtractorService.getStringValue(attribute, seri, attributeExtractorService.getPropertyFormats(mode, false));
		} else if (metadata.equals("qname")) {
			seri = QName.createQName(seri.toString());
			displayName = attributeExtractorService.getStringValue(attribute, seri, attributeExtractorService.getPropertyFormats(mode, false));
		} else {
			displayName = attributeExtractorService.getStringValue(attribute, seri, attributeExtractorService.getPropertyFormats(mode, false));
		}

		tmp.put(DISPLAY_VALUE, displayName);
		tmp.put(VALUE, value);
		tmp.put(METADATA, metadata);
		return tmp;

	}
	
    /**
     * <p>convertStringToList.</p>
     *
     * @param inputString a {@link java.lang.String} object
     * @return a {@link java.util.List} object
     */
    private static List<String> convertStringToList(String inputString) {
        String cleanString = inputString.replaceAll("[\\[\\]\"]", "");
        String[] stringArray = cleanString.split(",");
        return Arrays.asList(stringArray);
    }

	/**
	 * <p>extractJsonNodeRefProp.</p>
	 *
	 * @param seri a {@link java.io.Serializable} object
	 * @return a {@link java.lang.String} object
	 */
	private String extractJsonNodeRefProp(Serializable seri) {
		
		StringBuilder sb = null;
		try {
			JSONArray jsonArray = new JSONArray(seri.toString());
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject json = jsonArray.getJSONObject(i);
				if (sb == null) {
					sb = new StringBuilder();
				} else {
					sb.append(RepoConsts.LABEL_SEPARATOR);
				}
				
				if (json.has(CM_NAME)) {
					sb.append(json.get(CM_NAME));
				} else {
					sb.append(seri);
				}
			}
		} catch (JSONException e) {
			sb = new StringBuilder();
			try {
				JSONObject json = new JSONObject(seri.toString());
				if (json.has(CM_NAME)) {
					sb.append(json.get(CM_NAME));
				} else {
					sb.append(seri);
				}
			} catch (JSONException e2) {
				sb.append(seri);
			}
		}
		return sb == null ? seri.toString() : sb.toString();
	}

	/**
	 * <p>extractAlData.</p>
	 *
	 * @param value a {@link org.json.JSONObject} object
	 * @return a {@link java.util.HashMap} object
	 * @throws org.json.JSONException if any.
	 */
	private HashMap<String, Object> extractAlData(JSONObject value) throws JSONException {
		
		HashMap<String, Object> ret = new HashMap<>();
		
		for (String prop : AL_DATA_PROPS) {
			if (value.has(prop)) {
				ret.put(prop, value.getString(prop));
			}
		}
		
		return ret;
	}

	/**
	 * <p>getFieldDef.</p>
	 *
	 * @param itemType a {@link org.alfresco.service.namespace.QName} object
	 * @param field a {@link fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl.AttributeExtractorStructure} object
	 * @return a {@link org.alfresco.service.cmr.dictionary.ClassAttributeDefinition} object
	 */
	private ClassAttributeDefinition getFieldDef(QName itemType, AttributeExtractorStructure field) {

		if (itemType != null && !field.getItemType().equals(itemType)) {
			return entityDictionaryService.findMatchingPropDef(field.getItemType(), itemType, field.getFieldQname());
		}
		return field.getFieldDef();
	}
	
	/**
	 * <p>doExtract.</p>
	 *
	 * @param itemType a {@link org.alfresco.service.namespace.QName} object
	 * @param metadataFields a {@link java.util.List} object
	 * @param mode a {@link fr.becpg.config.format.FormatMode} object
	 * @param properties a {@link org.json.JSONObject} object
	 * @return a {@link java.util.Map} object
	 * @throws org.json.JSONException if any.
	 */
	private Map<String, Object> doExtract(QName itemType, List<AttributeExtractorStructure> metadataFields,
			FormatMode mode, JSONObject properties) throws JSONException {
		Map<String, Object> ret = new HashMap<>();

		for (AttributeExtractorStructure field : metadataFields) {
			if (field.isNested()) {
				List<Map<String, Object>> extracted = extractNestedField(properties, field);
				
				ret.put(field.getFieldName(), extracted);
			} else {
				ret.put(field.getFieldName(), extractNodeData(properties, getFieldDef(itemType, field), mode, itemType));
			}
		}
		
		return ret;
	}

	/**
	 * <p>extractNestedField.</p>
	 *
	 * @param properties a {@link org.json.JSONObject} object
	 * @param field a {@link fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl.AttributeExtractorStructure} object
	 * @return a {@link java.util.List} object
	 * @throws org.json.JSONException if any.
	 */
	private List<Map<String, Object>> extractNestedField(JSONObject properties, AttributeExtractorStructure field) throws JSONException {
		List<Map<String, Object>> ret = new ArrayList<>();
		if (field.getFieldDef() instanceof AssociationDefinition) {
			ret.add(extractJSON(null, properties, field.getChildrens(), field));
		}
		
		return ret;
	}

	/**
	 * <p>extractJSON.</p>
	 *
	 * @param dataListFilter a {@link fr.becpg.repo.entity.datalist.data.DataListFilter} object
	 * @param object a {@link org.json.JSONObject} object
	 * @param metadataFields a {@link java.util.List} object
	 * @param field a {@link fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl.AttributeExtractorStructure} object
	 * @return a {@link java.util.Map} object
	 * @throws org.json.JSONException if any.
	 */
	private Map<String, Object> extractJSON(DataListFilter dataListFilter, JSONObject object, List<AttributeExtractorStructure> metadataFields, AttributeExtractorStructure field) throws JSONException {
		StopWatch watch = null;
		if (logger.isDebugEnabled()) {
			watch = new StopWatch();
			watch.start();
		}
		try {
			
			Map<String, Object> ret = HashMap.newHashMap(20);
			
			QName fieldType = null;
			
			if (field == null && object.has(TYPE)) {
				fieldType = QName.createQName(object.getString(TYPE), namespaceService);
				ret.put(PROP_TYPE, fieldType.toPrefixString(namespaceService));
			} else if (field != null && field.getFieldDef() instanceof AssociationDefinition) {
				ret.put(PROP_TYPE, ((AssociationDefinition) field.getFieldDef()).getTargetClass().getName().toPrefixString(namespaceService));
			}
			
			JSONObject properties = object;
			
			if (object.has(ATTRIBUTES)) {
				properties = object.getJSONObject(ATTRIBUTES);
			}
			
			if (dataListFilter != null && dataListFilter.getFilterData() != null) {
				ret.put(PROP_NODE, dataListFilter.getFilterData() + "|" + object.getString(TYPE) + "|" + object.getString("id"));
			}
			
			// Skipping condition
			
			
			// Date parsing test
			Instant instantCreated = Instant.parse(properties.getString(ContentModel.PROP_CREATED.toPrefixString(namespaceService)));
			ret.put(PROP_CREATED, convertDateValue(Date.from(instantCreated), FormatMode.JSON));
			ret.put(PROP_CREATOR_DISPLAY, extractPerson(properties.getString(ContentModel.PROP_CREATOR.toPrefixString(namespaceService))));
			Instant instantModified = Instant.parse(properties.getString(ContentModel.PROP_MODIFIED.toPrefixString(namespaceService)));
			ret.put(PROP_MODIFIED, convertDateValue(Date.from(instantModified), FormatMode.JSON));
			ret.put(PROP_MODIFIER_DISPLAY, extractPerson(properties.getString(ContentModel.PROP_MODIFIER.toPrefixString(namespaceService))));
			
			if (field != null && properties.has(field.getFieldQname().toPrefixString(namespaceService))) {
				String id = ((JSONObject) properties.get(field.getFieldQname().toPrefixString(namespaceService))).getString("id");
				NodeRef node = new NodeRef(StoreRef.STORE_REF_WORKSPACE_SPACESSTORE, id);
				if (nodeService.exists(node)) {
					String color = (String) nodeService.getProperty(node, BeCPGModel.PROP_COLOR);
					if (color != null) {
						ret.put(PROP_COLOR, color);
					}
				}
			}
			
			Map<String, Map<String, Boolean>> permissions = HashMap.newHashMap(1);
			Map<String, Boolean> userAccess = HashMap.newHashMap(5);

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

			ret.put(PROP_NODEDATA, doExtract(field == null ? null : field.getFieldQname(), metadataFields, FormatMode.JSON, properties));
			
			if (dataListFilter != null) {
				Map<QName, Serializable> propertiesMap = new HashMap<>();
				
				Iterator<?> it = properties.keys();
				
				while (it.hasNext()) {
					String name = (String) it.next();
					if (name.startsWith("cm:")) {
						QName qname = QName.createQName(NamespaceService.CONTENT_MODEL_1_0_URI, name.split("cm:")[1]);
						propertiesMap.put(qname, properties.get(name).toString());
					} else if (name.startsWith(BCPG_PREFIX)) {
						QName qname = QName.createQName(BeCPGModel.BECPG_URI, name.split(BCPG_PREFIX)[1]);
						propertiesMap.put(qname, properties.get(name).toString());
					}
				}
			}
			
			return ret;

		} finally {
			if (logger.isDebugEnabled() && watch != null) {
				watch.stop();
				logger.debug(getClass().getSimpleName() + " extract metadata in  " + watch.getTotalTimeSeconds() + " s");
			}
		}
	}

	/**
	 * <p>extractData.</p>
	 *
	 * @param dataListFilter a {@link fr.becpg.repo.entity.datalist.data.DataListFilter} object
	 * @return a {@link java.lang.String} object
	 */
	private String extractData(DataListFilter dataListFilter) {
		
		if (dataListFilter.getFilterData() != null) {
			String[] filterDataArray = dataListFilter.getFilterData().replace("{", "").replace("}", "").split(",");
			
			for (String filterData : filterDataArray) {
				
				if (filterData.split(":").length < 2) {
					continue;
				}
				
				if (filterData.startsWith(StoreRef.PROTOCOL_WORKSPACE)) {
					NodeRef versionNodeRef = new NodeRef(filterData);
					return entityFormatService.getEntityData(versionNodeRef);
				}
			}
		}
		
		return entityFormatService.getEntityData(dataListFilter.getEntityNodeRef());
	}
	
	/** {@inheritDoc} */
	@Override
	public PaginatedExtractedItems extract(DataListFilter dataListFilter, List<AttributeExtractorField> metadataFields) {

		PaginatedExtractedItems ret = new PaginatedExtractedItems(dataListFilter.getPagination().getPageSize());

		try {
			
			String data = extractData(dataListFilter);
			
			JSONObject entityJson = new JSONObject(data);
			
			JSONObject entity = (JSONObject) entityJson.get("entity");
			
			JSONObject datalists = (JSONObject) entity.get("datalists");
			
			QName dataListFilterQName = QName.createQName(BeCPGModel.BECPG_URI, dataListFilter.getDataListName());
			
			if (BeCPGModel.TYPE_ACTIVITY_LIST.equals(dataListFilterQName)) {
				return ret;
			}
			
			JSONArray dataListJsonArray = extractDataList(datalists, dataListFilter);
			
			if (dataListJsonArray != null) {
				JSONArray filteredList = filterList(dataListJsonArray, dataListFilter);
				
				JSONArray results = sortList(filteredList, dataListFilter);
	
				if (results.length() == 0) {
					logger.warn("List is empty");
				}
				
				for (int i = 0; i < results.length(); i++) {
					JSONObject object = results.getJSONObject(i);
					
					if (ret.getComputedFields() == null) {
						ret.setComputedFields(attributeExtractorService.readExtractStructure(QName.createQName(object.getString(TYPE), namespaceService), metadataFields));
					}
					
					if (RepoConsts.FORMAT_CSV.equals(dataListFilter.getFormat()) || RepoConsts.FORMAT_XLSX.equals(dataListFilter.getFormat())) {
						logger.warn("CSV and XLSX unimplemented!");
					} else {
						
						Map<String, Object> item = extractJSON(dataListFilter, object, ret.getComputedFields(), null);
						ret.addItem(item);
					}
				}
				
				ret.setFullListSize(dataListFilter.getPagination().getFullListSize());
			}

		} catch (JSONException e) {
			logger.error("Failed to extract", e);
		}

		if(logger.isDebugEnabled() && !ret.getPageItems().isEmpty()) {
			logger.debug("First itemData is " + ret.getPageItems().get(0).get(PROP_NODEDATA));
		}
		return ret;
	}
	
	/**
	 * <p>extractDataList.</p>
	 *
	 * @param datalists a {@link org.json.JSONObject} object
	 * @param dataListFilter a {@link fr.becpg.repo.entity.datalist.data.DataListFilter} object
	 * @return a {@link org.json.JSONArray} object
	 */
	private JSONArray extractDataList(JSONObject datalists, DataListFilter dataListFilter) {
		
		String dataListType = dataListFilter.getDataType().getPrefixedQName(namespaceService).getPrefixString();
		
		if (dataListFilter.getDataListName().contains("@")) {
			dataListType += "|" + dataListFilter.getDataListName();
		}
		
		if (datalists.has(dataListType)) {
			return datalists.getJSONArray(dataListType);
		}
		
		String dataListNestedType = dataListFilter.getDataListName() + "@" + dataListType;
		
		for (String key : datalists.keySet()) {
			if (key.endsWith(dataListNestedType) && datalists.has(key)) {
				return datalists.getJSONArray(key);
			}
		}
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public boolean applyTo(DataListFilter dataListFilter) {
		
		NodeRef targetNodeRef = dataListFilter.getEntityNodeRef();
		
		if (dataListFilter.getFilterData() != null) {
			String[] filterDataArray = dataListFilter.getFilterData().replace("{", "").replace("}", "").split(",");
			
			for (String filterData : filterDataArray) {
				if (filterData.split(":").length < 2) {
					continue;
				}
				
				if (filterData.startsWith(StoreRef.PROTOCOL_WORKSPACE)) {
					targetNodeRef = new NodeRef(filterData);
				}
			}
		}
		
		return EntityFormat.JSON.toString().equals(entityFormatService.getEntityFormat(targetNodeRef));
	}

	/** {@inheritDoc} */
	@Override
	public Date computeLastModified(DataListFilter dataListFilter) {
		return null;
	}

	/** {@inheritDoc} */
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

			try {
				Object sortA = 0;
				Object sortB = 0;
				if (a.getJSONObject(ATTRIBUTES).has(sortString)) {
					sortA = a.getJSONObject(ATTRIBUTES).get(sortString);
				} else {
					return -order;
				}
				if (b.getJSONObject(ATTRIBUTES).has(sortString)) {
					sortB = b.getJSONObject(ATTRIBUTES).get(sortString);
				} else {
					return order;
				}
				
				if (sortA instanceof Double compA && sortB instanceof Double compB) {
					return order * compA.compareTo(compB);
				}
				
				if (sortA instanceof Integer compA && sortB instanceof Integer compB) {
					return order * compA.compareTo(compB);
				}
				
				return order * sortA.toString().compareTo(sortB.toString());
				
			} catch (JSONException e) {
				logger.warn("comparison error", e);
			}

			return order * a.toString().compareTo(b.toString());
		}
		
	}

}
