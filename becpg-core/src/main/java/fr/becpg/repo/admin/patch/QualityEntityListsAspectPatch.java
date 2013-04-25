package fr.becpg.repo.admin.patch;

import java.util.List;

import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.QualityModel;
import fr.becpg.repo.helper.LuceneHelper;
import fr.becpg.repo.search.BeCPGSearchService;

/**
 * QualityEntityListsAspectPatch
 * @author quere
 *
 */
public class QualityEntityListsAspectPatch extends AbstractPatch {
	
	private static Log logger = LogFactory.getLog(QualityEntityListsAspectPatch.class);
	private static final String MSG_SUCCESS = "patch.bcpg.qualityEntityListAspectPatch.result";

	private BeCPGSearchService beCPGSearchService;
	
	public void setBeCPGSearchService(BeCPGSearchService beCPGSearchService) {
		this.beCPGSearchService = beCPGSearchService;
	}

	@Override
	protected String applyInternal() throws Exception {
		
		QName [] entityQNames = {QualityModel.TYPE_NC, QualityModel.TYPE_CONTROL_PLAN, QualityModel.TYPE_QUALITY_CONTROL, QualityModel.TYPE_CONTROL_POINT, QualityModel.TYPE_WORK_ITEM_ANALYSIS};
		
		for(QName entityQName : entityQNames){
			String query = LuceneHelper.mandatory(LuceneHelper.getCondType(entityQName)) +
							LuceneHelper.exclude(LuceneHelper.getCondAspect(BeCPGModel.ASPECT_ENTITYLISTS));
			
			List<NodeRef> entityNodeRefs = beCPGSearchService.luceneSearch("query");
			logger.info("QualityEntityListAspectPatch bcpg:entityListsAspect, size: " + entityNodeRefs.size());
			
			for(NodeRef entityNodeRef : entityNodeRefs){
				if(nodeService.exists(entityNodeRef)){
					nodeService.addAspect(entityNodeRef, BeCPGModel.ASPECT_ENTITYLISTS, null);
				}
				else{
					logger.warn("dataListNodeRef doesn't exist : " + entityNodeRef);
				}			
			}
		}
		
		
		return I18NUtil.getMessage(MSG_SUCCESS);
	}

}
