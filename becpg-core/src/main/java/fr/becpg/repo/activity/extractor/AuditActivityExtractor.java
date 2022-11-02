package fr.becpg.repo.activity.extractor;

import java.util.Date;
import java.util.List;

import org.json.JSONObject;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.audit.model.AuditType;
import fr.becpg.repo.audit.service.BeCPGAuditService;
import fr.becpg.repo.entity.datalist.DataListExtractor;
import fr.becpg.repo.entity.datalist.DataListExtractorFactory;
import fr.becpg.repo.entity.datalist.PaginatedExtractedItems;
import fr.becpg.repo.entity.datalist.data.DataListFilter;

public class AuditActivityExtractor implements DataListExtractor {

	private BeCPGAuditService beCPGAuditService;
	
	private DataListExtractorFactory dataListExtractorFactory;
	
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
		
		List<JSONObject> results = beCPGAuditService.getAuditStatistics(AuditType.ACTIVITY, RepoConsts.MAX_RESULTS_256, "startedAt", "entityNodeRef=" + dataListFilter.getEntityNodeRef());
		
		for (JSONObject result : results) {
			
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
