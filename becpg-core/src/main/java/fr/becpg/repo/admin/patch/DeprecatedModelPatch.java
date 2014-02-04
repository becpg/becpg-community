package fr.becpg.repo.admin.patch;

import java.util.List;

import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.service.cmr.repository.AssociationRef;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.helper.LuceneHelper;
import fr.becpg.repo.search.BeCPGSearchService;

/**
 * DeprecatedModelPatch
 * @author matthieu
 *
 */
public class DeprecatedModelPatch extends AbstractPatch {
	
	private static Log logger = LogFactory.getLog(DeprecatedModelPatch.class);
	private static final String MSG_SUCCESS = "patch.bcpg.removeGoogleDocsPatch.result";

	private BeCPGSearchService beCPGSearchService;
	
	
	
	public void setBeCPGSearchService(BeCPGSearchService beCPGSearchService) {
		this.beCPGSearchService = beCPGSearchService;
	}

	@Override
	protected String applyInternal() throws Exception {
		
		QName [] classQNames = { BeCPGModel.ASPECT_PRODUCT_STATE,BeCPGModel.ASPECT_COMPOSITE_VERSIONABLE, BeCPGModel.ASPECT_ENTITY_VERSIONABLE };
		
		for(QName classQName : classQNames){
			
			List<NodeRef> productNodeRefs = beCPGSearchService.luceneSearch(LuceneHelper.getCondAspect(classQName));
			logger.info("deprecatedModelPatch remove aspect" + classQName + " , size: " + productNodeRefs.size());
			
			for(NodeRef productNodeRef : productNodeRefs){
				if(nodeService.exists(productNodeRef)){
					nodeService.removeAspect(productNodeRef, classQName);
				}
				else{
					logger.warn("productNodeRef doesn't exist : " + productNodeRef);
				}			
			}
		}
		
		
       QName [] propertyQNames = { BeCPGModel.PROP_COMPOLIST_QTY_AFTER_PROCESS };
		
		for(QName propertyQName : propertyQNames){
		
			List<NodeRef> productNodeRefs = beCPGSearchService.luceneSearch(LuceneHelper.exclude(LuceneHelper.getCondIsNullValue(propertyQName)));
			logger.info("deprecatedModelPatch remove property" + propertyQName + " , size: " + productNodeRefs.size());
		
			for(NodeRef productNodeRef : productNodeRefs){
				if(nodeService.exists(productNodeRef)){
					nodeService.removeProperty(productNodeRef, propertyQName);
				}
				else{
					logger.warn("productNodeRef doesn't exist : " + productNodeRef);
				}			
			}
			
		}
		
		
		 List<NodeRef> productNodeRefs = beCPGSearchService.luceneSearch(LuceneHelper.getCondType(BeCPGModel.TYPE_PRODUCT_SPECIFICATION));
		logger.info("deprecatedModelPatch remove assoc" + BeCPGModel.ASSOC_PRODUCT_SPECIFICATION + " , size: " + productNodeRefs.size());
			
		for(NodeRef productNodeRef : productNodeRefs){
			if(nodeService.exists(productNodeRef)){
				for(AssociationRef associationRef : nodeService.getSourceAssocs(productNodeRef, BeCPGModel.ASSOC_PRODUCT_SPECIFICATION)) {
					nodeService.removeAssociation(associationRef.getTargetRef(), associationRef.getSourceRef(), BeCPGModel.ASSOC_PRODUCT_SPECIFICATION);
				}
			}
			else{
				logger.warn("productNodeRef doesn't exist : " + productNodeRef);
			}		
				
		}
		 
		
		return I18NUtil.getMessage(MSG_SUCCESS);
	}

}
