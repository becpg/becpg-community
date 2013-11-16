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
 * EntitySortableListPatch
 * @author matthieu
 *
 */
public class EntitySortableListPatch extends AbstractPatch {
	
	private static Log logger = LogFactory.getLog(EntitySortableListPatch.class);
	private static final String MSG_SUCCESS = "patch.bcpg.EntitySortableListPatch.result";

	private BeCPGSearchService beCPGSearchService;
	
	public void setBeCPGSearchService(BeCPGSearchService beCPGSearchService) {
		this.beCPGSearchService = beCPGSearchService;
	}

	@Override
	protected String applyInternal() throws Exception {
		
		List<NodeRef> dataListNodeRefs = beCPGSearchService.luceneSearch("+TYPE:\"bcpg:entityListItem\" NOT ASPECT:\"bcpg:sortableListAspect\" ");
		logger.info("EntitySortableListPatch add sort in bcpg:entityListItem, size: " + dataListNodeRefs.size());
		
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
