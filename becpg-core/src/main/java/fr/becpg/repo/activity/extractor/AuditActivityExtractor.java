package fr.becpg.repo.activity.extractor;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.service.cmr.dictionary.ClassAttributeDefinition;
import org.alfresco.service.cmr.dictionary.DataTypeDefinition;
import org.alfresco.service.cmr.dictionary.PropertyDefinition;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;
import org.alfresco.util.ISO8601DateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONException;
import org.json.JSONObject;

import fr.becpg.config.format.FormatMode;
import fr.becpg.config.format.PropertyFormatService;
import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.activity.EntityActivityExtractorService;
import fr.becpg.repo.activity.EntityActivityService;
import fr.becpg.repo.activity.data.ActivityType;
import fr.becpg.repo.audit.model.AuditFilter;
import fr.becpg.repo.audit.model.AuditType;
import fr.becpg.repo.audit.service.BeCPGAuditService;
import fr.becpg.repo.entity.EntityDictionaryService;
import fr.becpg.repo.entity.datalist.DataListExtractor;
import fr.becpg.repo.entity.datalist.DataListExtractorFactory;
import fr.becpg.repo.entity.datalist.PaginatedExtractedItems;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.entity.datalist.impl.AbstractDataListExtractor;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.helper.JsonHelper;
import fr.becpg.repo.helper.impl.AttributeExtractorServiceImpl.AttributeExtractorStructure;

public class AuditActivityExtractor implements DataListExtractor {

	private BeCPGAuditService beCPGAuditService;

	private DataListExtractorFactory dataListExtractorFactory;

	private ServiceRegistry serviceRegistry;

	private EntityDictionaryService entityDictionaryService;

	private AttributeExtractorService attributeExtractorService;

	private PropertyFormatService propertyFormatService;

	private NamespaceService namespaceService;

	private EntityActivityService entityActivityService;

	private EntityActivityExtractorService entityActivityExtractorService;

	public void setEntityActivityExtractorService(EntityActivityExtractorService entityActivityExtractorService) {
		this.entityActivityExtractorService = entityActivityExtractorService;
	}

	public void setEntityActivityService(EntityActivityService entityActivityService) {
		this.entityActivityService = entityActivityService;
	}

	public void setNamespaceService(NamespaceService namespaceService) {
		this.namespaceService = namespaceService;
	}

	public void setPropertyFormatService(PropertyFormatService propertyFormatService) {
		this.propertyFormatService = propertyFormatService;
	}

	public void setAttributeExtractorService(AttributeExtractorService attributeExtractorService) {
		this.attributeExtractorService = attributeExtractorService;
	}

	public void setEntityDictionaryService(EntityDictionaryService entityDictionaryService) {
		this.entityDictionaryService = entityDictionaryService;
	}

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

	public void setDataListExtractorFactory(DataListExtractorFactory dataListExtractorFactory) {
		this.dataListExtractorFactory = dataListExtractorFactory;
	}

	public void setBeCPGAuditService(BeCPGAuditService beCPGAuditService) {
		this.beCPGAuditService = beCPGAuditService;
	}

	public void init() {
		dataListExtractorFactory.registerExtractor(this);
	}

	private static final Log logger = LogFactory.getLog(AuditActivityExtractor.class);

	@Override
	public PaginatedExtractedItems extract(DataListFilter dataListFilter, List<String> metadataFields) {

		PaginatedExtractedItems ret = new PaginatedExtractedItems(dataListFilter.getPagination().getPageSize());

		if (ret.getComputedFields() == null) {
			ret.setComputedFields(attributeExtractorService.readExtractStructure(BeCPGModel.TYPE_ACTIVITY_LIST, metadataFields));
		}
		
		AuditFilter auditFilter = new AuditFilter();
		
		auditFilter.setAscendingOrder(false);
		
		auditFilter.setSortBy("startedAt");
		
		auditFilter.setFilter("entityNodeRef=" + dataListFilter.getEntityNodeRef());

		List<JSONObject> results = beCPGAuditService.getAuditStatistics(AuditType.ACTIVITY, RepoConsts.MAX_RESULTS_256, auditFilter);

		for (JSONObject result : results) {

			Map<String, Object> item = new HashMap<>();

			item.put(AbstractDataListExtractor.PROP_TYPE,
					BeCPGModel.TYPE_ACTIVITY_LIST.toPrefixString(serviceRegistry.getNamespaceService()));

			Map<String, Map<String, Boolean>> permissions = new HashMap<>();
			Map<String, Boolean> userAccess = new HashMap<>();

			userAccess.put("delete", false);
			userAccess.put("create", false);
			userAccess.put("edit", false);
			userAccess.put("sort", false);
			userAccess.put("details", false);
			userAccess.put("wused", false);
			userAccess.put("content", false);

			permissions.put(AbstractDataListExtractor.PROP_USERACCESS, userAccess);

			item.put(AbstractDataListExtractor.PROP_PERMISSIONS, permissions);

			item.put(AbstractDataListExtractor.PROP_NODEDATA, extractNodeData(result, ret.getComputedFields()));

			ret.addItem(item);
		}

		ret.setFullListSize(dataListFilter.getPagination().getFullListSize());

		return ret;
	}

	private Object extractNodeData(JSONObject result, List<AttributeExtractorStructure> metadataFields) {

		Map<String, Object> ret = new HashMap<>(metadataFields.size());

		for (AttributeExtractorStructure metadataField : metadataFields) {
			ClassAttributeDefinition attributeDef = getFieldDef(BeCPGModel.TYPE_ACTIVITY_LIST, metadataField);

			Object value = result.get(metadataField.getFieldName());

			HashMap<String, Object> tmp = new HashMap<>();

			if (attributeDef instanceof PropertyDefinition) {

				if (DataTypeDefinition.DATETIME.equals(((PropertyDefinition) attributeDef).getDataType().getName())) {
					
					Date date = ISO8601DateFormat.parse(value.toString());
					String displayName = attributeExtractorService.getStringValue((PropertyDefinition) attributeDef, date, propertyFormatService.getPropertyFormats(FormatMode.JSON, false));
					tmp.put("displayValue", displayName);
					value = date;

				} else {
					String displayName = attributeExtractorService.getStringValue((PropertyDefinition) attributeDef, value.toString(), propertyFormatService.getPropertyFormats(FormatMode.JSON, false));
					tmp.put("displayValue", displayName);
				}

				QName type = ((PropertyDefinition) attributeDef).getDataType().getName().getPrefixedQName(namespaceService);

				String metadata = entityDictionaryService.toPrefixString(type).split(":")[1];

				tmp.put("metadata", metadata);
				tmp.put("value", JsonHelper.formatValue(value));

				ret.put(metadataField.getFieldName(), tmp);

			}
		}
		
		ret.put("prop_bcpg_alUserId", extractPerson((String) result.get("prop_bcpg_alUserId")));

		JSONObject postLookup = entityActivityService.postActivityLookUp(ActivityType.valueOf((String) result.get("prop_bcpg_alType")), (String) result.get("prop_bcpg_alData"));
		
		if (postLookup != null) {
			try {
				entityActivityExtractorService.formatPostLookup(postLookup);
			} catch (JSONException e) {
				logger.error(e, e);
			}
			ret.put("prop_bcpg_alData", postLookup);
		}

		return ret;
	}
	
	private Map<String, String> extractPerson(String person) {
		Map<String, String> ret = new HashMap<>(2);
		ret.put("value", person);
		ret.put("displayValue", attributeExtractorService.getPersonDisplayName(person));
		return ret;
	}

	private ClassAttributeDefinition getFieldDef(QName itemType, AttributeExtractorStructure field) {

		if (!field.getItemType().equals(itemType)) {
			return entityDictionaryService.findMatchingPropDef(field.getItemType(), itemType, field.getFieldQname());
		}
		return field.getFieldDef();
	}

	@Override
	public boolean applyTo(DataListFilter dataListFilter) {
		return BeCPGModel.TYPE_ACTIVITY_LIST.equals(dataListFilter.getDataType());
	}

	@Override
	public boolean isDefaultExtractor() {
		return false;
	}

	@Override
	public Date computeLastModified(DataListFilter dataListFilter) {
		return null;
	}

	@Override
	public boolean hasWriteAccess() {
		return false;
	}

	@Override
	public int getPriority() {
		return 0;
	}

}
