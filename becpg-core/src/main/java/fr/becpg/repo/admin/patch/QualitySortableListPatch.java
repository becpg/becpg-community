package fr.becpg.repo.admin.patch;

import java.util.List;

import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.search.BeCPGSearchService;

/**
 * QualityPatch
 * @author quere
 *
 */
public class QualitySortableListPatch extends AbstractPatch {
	
	private static Log logger = LogFactory.getLog(QualitySortableListPatch.class);
	private static final String MSG_SUCCESS = "patch.bcpg.removeCostDetailsListPatch.result";

	private BeCPGSearchService beCPGSearchService;
	
	public void setBeCPGSearchService(BeCPGSearchService beCPGSearchService) {
		this.beCPGSearchService = beCPGSearchService;
	}

	@Override
	protected String applyInternal() throws Exception {
		
		List<NodeRef> dataListNodeRefs = beCPGSearchService.luceneSearch("+TYPE:\"qa:qualityListItem\"");
		logger.info("QualityPatch add sort in qa:qualityListItem, size: " + dataListNodeRefs.size());
		
		for(NodeRef dataListNodeRef : dataListNodeRefs){
			if(nodeService.exists(dataListNodeRef)){
				nodeService.addAspect(dataListNodeRef, BeCPGModel.ASPECT_SORTABLE_LIST, null);
			}
			else{
				logger.warn("dataListNodeRef doesn't exist : " + dataListNodeRef);
			}			
		}
		
		return I18NUtil.getMessage(MSG_SUCCESS);
	}

}
