package fr.becpg.repo.activity.extractor;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.ServiceRegistry;
import org.json.JSONObject;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.activity.EntityActivityExtractorService;
import fr.becpg.repo.audit.model.AuditQuery;
import fr.becpg.repo.audit.model.AuditType;
import fr.becpg.repo.audit.service.BeCPGAuditService;
import fr.becpg.repo.entity.datalist.DataListExtractor;
import fr.becpg.repo.entity.datalist.DataListExtractorFactory;
import fr.becpg.repo.entity.datalist.PaginatedExtractedItems;
import fr.becpg.repo.entity.datalist.data.DataListFilter;
import fr.becpg.repo.entity.datalist.impl.AbstractDataListExtractor;
import fr.becpg.repo.helper.AttributeExtractorService;

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
	public PaginatedExtractedItems extract(DataListFilter dataListFilter, List<String> metadataFields) {

		PaginatedExtractedItems ret = new PaginatedExtractedItems(dataListFilter.getPagination().getPageSize());

		if (ret.getComputedFields() == null) {
			ret.setComputedFields(attributeExtractorService.readExtractStructure(BeCPGModel.TYPE_ACTIVITY_LIST, metadataFields));
		}
		
		AuditQuery auditQuery = AuditQuery.createQuery().order(false).sortBy("startedAt").filter("entityNodeRef", dataListFilter.getEntityNodeRef().toString());
		
		List<JSONObject> results = dataListFilter.getPagination().paginate(beCPGAuditService.listAuditEntries(AuditType.ACTIVITY, auditQuery));

		for (JSONObject result : results) {

			Map<String, Object> item = new HashMap<>();

			item.put(AbstractDataListExtractor.PROP_TYPE, BeCPGModel.TYPE_ACTIVITY_LIST.toPrefixString(serviceRegistry.getNamespaceService()));

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

			item.put(AbstractDataListExtractor.PROP_NODEDATA, entityActivityExtractorService.extractAuditActivityData(result, ret.getComputedFields()));

			ret.addItem(item);
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
