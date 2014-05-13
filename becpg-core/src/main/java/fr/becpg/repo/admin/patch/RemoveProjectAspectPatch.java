package fr.becpg.repo.admin.patch;

import java.util.List;

import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.ProjectModel;
import fr.becpg.repo.helper.LuceneHelper;
import fr.becpg.repo.search.BeCPGSearchService;

/**
 * removeProjectAspectPatch
 * @author matthieu
 *
 */
public class RemoveProjectAspectPatch extends AbstractPatch {
	
	private static Log logger = LogFactory.getLog(RemoveProjectAspectPatch.class);
	private static final String MSG_SUCCESS = "patch.bcpg.removeProjectAspectPatch.result";

	private BeCPGSearchService beCPGSearchService;
	
	
	
	public void setBeCPGSearchService(BeCPGSearchService beCPGSearchService) {
		this.beCPGSearchService = beCPGSearchService;
	}

	@Override
	protected String applyInternal() throws Exception {
		
		QName [] classQNames = { ProjectModel.ASPECT_PROJECT_ASPECT };
		
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
		
		
		
		return I18NUtil.getMessage(MSG_SUCCESS);
	}

}
