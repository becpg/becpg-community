package fr.becpg.repo.admin.patch;

import java.util.List;

import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.repo.googledocs.GoogleDocsModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.repo.helper.LuceneHelper;
import fr.becpg.repo.search.BeCPGSearchService;

/**
 * RemoveGoogleDocsPatch
 * @author matthieu
 *
 */
public class RemoveGoogleDocsPatch extends AbstractPatch {
	
	private static Log logger = LogFactory.getLog(RemoveGoogleDocsPatch.class);
	private static final String MSG_SUCCESS = "patch.bcpg.removeGoogleDocsPatch.result";

	private BeCPGSearchService beCPGSearchService;
	
	
	public void setBeCPGSearchService(BeCPGSearchService beCPGSearchService) {
		this.beCPGSearchService = beCPGSearchService;
	}

	@Override
	protected String applyInternal() throws Exception {
		
		QName [] classQNames = { GoogleDocsModel.ASPECT_GOOGLEEDITABLE, GoogleDocsModel.ASPECT_GOOGLERESOURCE };
		
		for(QName classQName : classQNames){
			
			List<NodeRef> productNodeRefs = beCPGSearchService.luceneSearch(LuceneHelper.getCondAspect(classQName));
			logger.info("removeGoogleDocsPatch remove aspect" + classQName + " , size: " + productNodeRefs.size());
			
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
