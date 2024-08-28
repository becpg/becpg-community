package fr.becpg.repo.activity.extractor;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.alfresco.service.ServiceRegistry;
import org.alfresco.util.ISO8601DateFormat;
import org.json.JSONObject;

import fr.becpg.config.format.FormatMode;
import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.activity.EntityActivityExtractorService;
import fr.becpg.repo.audit.model.AuditQuery;
import fr.becpg.repo.audit.model.AuditType;
import fr.becpg.repo.audit.plugin.impl.ActivityAuditPlugin;
import fr.becpg.repo.audit.service.BeCPGAuditService;
import fr.becpg.repo.entity.datalist.DataListExtractor;
import fr.becpg.repo.entity.datalist.DataListExtractorFactory;
import fr.becpg.repo.entity.datalist.PaginatedExtractedItems;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.entity.datalist.impl.AbstractDataListExtractor;
import fr.becpg.repo.helper.AttributeExtractorService;
import fr.becpg.repo.helper.impl.AttributeExtractorField;

public class AuditActivityExtractor implements DataListExtractor {

	private BeCPGAuditService beCPGAuditService;

	private DataListExtractorFactory dataListExtractorFactory;

	private ServiceRegistry serviceRegistry;

	private AttributeExtractorService attributeExtractorService;

	private EntityActivityExtractorService entityActivityExtractorService;

	public void setEntityActivityExtractorService(EntityActivityExtractorService entityActivityExtractorService) {
		this.entityActivityExtractorService = entityActivityExtractorService;
	}

	public void setAttributeExtractorService(AttributeExtractorService attributeExtractorService) {
		this.attributeExtractorService = attributeExtractorService;
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

	@Override
	public PaginatedExtractedItems extract(DataListFilter dataListFilter, List<AttributeExtractorField> metadataFields) {

		PaginatedExtractedItems ret = new PaginatedExtractedItems(dataListFilter.getPagination().getPageSize());

		if (ret.getComputedFields() == null) {
			ret.setComputedFields(attributeExtractorService.readExtractStructure(BeCPGModel.TYPE_ACTIVITY_LIST, metadataFields));
		}
		
		AuditQuery auditQuery = AuditQuery.createQuery().dbAsc(false).asc(false)
				.sortBy(ActivityAuditPlugin.PROP_CM_CREATED)
				.filter(ActivityAuditPlugin.ENTITY_NODEREF, dataListFilter.getEntityNodeRef().toString());
		
		List<JSONObject> listAuditEntries = beCPGAuditService.listAuditEntries(AuditType.ACTIVITY, auditQuery);
		
		if (dataListFilter.getCriteriaMap() != null && !dataListFilter.getCriteriaMap().isEmpty()) {
			for (Entry<String, String> entry : dataListFilter.getCriteriaMap().entrySet()) {
				String key = entry.getKey();
				String value = entry.getValue();
				if ("prop_bcpg_alType".equals(key)) {
					listAuditEntries = listAuditEntries.stream().filter(e -> value.equals("="+e.getString("prop_bcpg_alType"))).toList();
				} else if ("prop_bcpg_alUserId".equals(key)) {
					listAuditEntries = listAuditEntries.stream().filter(e -> value.equals(e.getString("prop_bcpg_alUserId"))).toList();
				} else if ("prop_cm_created-date-range".equals(key)) {
					String[] split = value.split("\\|");
					Date from = !split[0].isBlank() ? ISO8601DateFormat.parse(split[0]) : null;
					Date to = split.length > 1 && !split[1].isBlank() ? ISO8601DateFormat.parse(split[1]) : null;
					listAuditEntries = listAuditEntries.stream().filter(e -> {
						Date date = ISO8601DateFormat.parse(e.getString("completedAt"));
						return (from == null || date.after(from)) && (to == null || date.before(to));
					}).toList();
				}
			}
		}
		
		List<JSONObject> results = dataListFilter.getPagination().paginate(listAuditEntries);
		
		
		for (JSONObject result : results) {
			if (RepoConsts.FORMAT_XLSX.equals(dataListFilter.getFormat())) {
				Map<String, Object> extractAuditActivityData = entityActivityExtractorService.extractAuditActivityData(result, ret.getComputedFields(), FormatMode.XLSX);
				ret.addItem(extractAuditActivityData);
			} else if (RepoConsts.FORMAT_CSV.equals(dataListFilter.getFormat())) {
				Map<String, Object> extractAuditActivityData = entityActivityExtractorService.extractAuditActivityData(result, ret.getComputedFields(), FormatMode.CSV);
				ret.addItem(extractAuditActivityData);
			} else {
				Map<String, Object> item = new HashMap<>();
				Map<String, Map<String, Boolean>> permissions = new HashMap<>();
				Map<String, Boolean> userAccess = new HashMap<>();
				userAccess.put("delete", false);
				userAccess.put("create", false);
				userAccess.put("edit", false);
				userAccess.put("sort", false);
				userAccess.put("details", false);
				userAccess.put("wused", false);
				userAccess.put("content", false);
				item.put(AbstractDataListExtractor.PROP_TYPE, BeCPGModel.TYPE_ACTIVITY_LIST.toPrefixString(serviceRegistry.getNamespaceService()));
				permissions.put(AbstractDataListExtractor.PROP_USERACCESS, userAccess);
				item.put(AbstractDataListExtractor.PROP_PERMISSIONS, permissions);
				Map<String, Object> extractAuditActivityData = entityActivityExtractorService.extractAuditActivityData(result, ret.getComputedFields(), FormatMode.JSON);
				item.put(AbstractDataListExtractor.PROP_NODEDATA, extractAuditActivityData);
				ret.addItem(item);
			}
		}

		ret.setFullListSize(dataListFilter.getPagination().getFullListSize());

		return ret;
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
