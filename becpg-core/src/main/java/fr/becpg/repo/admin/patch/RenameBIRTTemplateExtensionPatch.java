package fr.becpg.repo.admin.patch;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.search.BeCPGSearchService;

/**
 * Rename the BIRT extension template to *.rptdesign
 * @author quere
 *
 */
public class RenameBIRTTemplateExtensionPatch extends AbstractPatch {
	
	private static Log logger = LogFactory.getLog(RenameBIRTTemplateExtensionPatch.class);
	private static final String MSG_SUCCESS = "patch.bcpg.renameBIRTTemplateExtensionPatch.result";

	private BeCPGSearchService beCPGSearchService;
	
	public void setBeCPGSearchService(BeCPGSearchService beCPGSearchService) {
		this.beCPGSearchService = beCPGSearchService;
	}

	@Override
	protected String applyInternal() throws Exception {
		
		List<NodeRef> nodeRefs = beCPGSearchService.luceneSearch("+TYPE:\"rep:reportTpl\"");
		logger.info("RenameBIRTTemplateExtensionPatch size: " + nodeRefs.size());
		try {
		
		for(NodeRef nodeRef : nodeRefs){
			if(nodeService.exists(nodeRef)){
				String name = (String)nodeService.getProperty(nodeRef, ContentModel.PROP_NAME);
				if(name != null){
					String newName = null;
					if(name.endsWith(".pdf")){
						newName = name.replace("pdf", RepoConsts.REPORT_EXTENSION_BIRT);
					}					
					else if(!name.contains(".")){
						newName = name + "." + RepoConsts.REPORT_EXTENSION_BIRT;
					}
					if(newName != null){
						nodeService.setProperty(nodeRef, ContentModel.PROP_NAME, newName);
					}
				}
			}
			else{
				logger.warn("nodeRef doesn't exist : " + nodeRef);
			}			
		}
		} catch(Exception e){
			logger.error(e,e);
		}
		
		return I18NUtil.getMessage(MSG_SUCCESS);
	}

}
