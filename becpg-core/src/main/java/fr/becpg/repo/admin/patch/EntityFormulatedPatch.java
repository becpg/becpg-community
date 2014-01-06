package fr.becpg.repo.admin.patch;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.policy.BehaviourFilter;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.helper.LuceneHelper;
import fr.becpg.repo.search.BeCPGSearchService;

/**
 * EntityFormulatedPatch
 * @author Philippe
 *
 */
public class EntityFormulatedPatch extends AbstractPatch {
	
	private static Log logger = LogFactory.getLog(EntityFormulatedPatch.class);
	private static final String MSG_SUCCESS = "patch.bcpg.EntityFormulatedPatch.result";

	private BeCPGSearchService beCPGSearchService;
	
	private BehaviourFilter policyBehaviourFilter;
	
	public void setBeCPGSearchService(BeCPGSearchService beCPGSearchService) {
		this.beCPGSearchService = beCPGSearchService;
	}

	public void setPolicyBehaviourFilter(BehaviourFilter policyBehaviourFilter) {
		this.policyBehaviourFilter = policyBehaviourFilter;
	}

	@Override
	protected String applyInternal() throws Exception {
		
		QName [] classQNames = { BeCPGModel.TYPE_SEMIFINISHEDPRODUCT, BeCPGModel.TYPE_FINISHEDPRODUCT, BeCPGModel.TYPE_PACKAGINGKIT };
		
		for(QName classQName : classQNames){
			
			List<NodeRef> productNodeRefs = beCPGSearchService.luceneSearch(LuceneHelper.getCondType(classQName) + 
					LuceneHelper.exclude(LuceneHelper.getCondAspect(BeCPGModel.ASPECT_FORMULATED_ENTITY)));
			logger.info("EntityFormulatedPatch add formulatedAspect in products " + classQName + " , size: " + productNodeRefs.size());
			
			for(NodeRef productNodeRef : productNodeRefs){
				if(nodeService.exists(productNodeRef)){
					try{
						policyBehaviourFilter.disableBehaviour(ContentModel.ASPECT_AUDITABLE);
						nodeService.addAspect(productNodeRef, BeCPGModel.ASPECT_FORMULATED_ENTITY, null);
					}
					finally{
						policyBehaviourFilter.enableBehaviour(ContentModel.ASPECT_AUDITABLE);
					}					
				}
				else{
					logger.warn("productNodeRef doesn't exist : " + productNodeRef);
				}			
			}
		}
		
		return I18NUtil.getMessage(MSG_SUCCESS);
	}

}
