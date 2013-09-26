package fr.becpg.repo.admin.patch;

import java.util.List;

import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.repo.search.BeCPGSearchService;

/**
 * Remove the class datalist bcpg:costDetailsList
 * @author quere
 *
 */
public class RemoveCostDetailsListPatch extends AbstractPatch {
	
	private static Log logger = LogFactory.getLog(RemoveCostDetailsListPatch.class);
	private static final String MSG_SUCCESS = "patch.bcpg.removeCostDetailsListPatch.result";

	private BeCPGSearchService beCPGSearchService;
	
	public void setBeCPGSearchService(BeCPGSearchService beCPGSearchService) {
		this.beCPGSearchService = beCPGSearchService;
	}

	@Override
	protected String applyInternal() throws Exception {
		
		List<NodeRef> dataListNodeRefs = beCPGSearchService.luceneSearch("+TYPE:\"dl:dataList\" +@dl\\:dataListItemType:\"bcpg:costDetailsList\"");
		logger.info("RemoveCostDetailsListPatch size: " + dataListNodeRefs.size());
		
		for(NodeRef dataListNodeRef : dataListNodeRefs){
			if(nodeService.exists(dataListNodeRef)){
				nodeService.deleteNode(dataListNodeRef);
			}
			else{
				logger.warn("dataListNodeRef doesn't exist : " + dataListNodeRef);
			}			
		}
		
		return I18NUtil.getMessage(MSG_SUCCESS);
	}

}
