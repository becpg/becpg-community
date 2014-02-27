package fr.becpg.repo.admin.patch;

import java.util.List;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.RepoConsts;
import fr.becpg.repo.search.BeCPGQueryBuilder;

/**
 * Copy prop value of cm:name in bcpg:lvValue to support all char
 * @author quere
 *
 */
public class ListValuePatch extends AbstractBeCPGPatch {

	private static Log logger = LogFactory.getLog(ListValuePatch.class);
	private static final String MSG_SUCCESS = "patch.bcpg.listValuePatch.result";

	
	@Override
	protected String applyInternal() throws Exception {
		
		List<NodeRef> dataListNodeRefs = BeCPGQueryBuilder.createQuery()
				.ofType(BeCPGModel.TYPE_LIST_VALUE)
				.maxResults(RepoConsts.MAX_RESULTS_UNLIMITED).inDB().list();
		logger.info("ListValuePatch migrator, size: " + dataListNodeRefs.size());
		
		for(NodeRef dataListNodeRef : dataListNodeRefs){
			if(nodeService.exists(dataListNodeRef)){
				String name = (String)nodeService.getProperty(dataListNodeRef, ContentModel.PROP_NAME);
				if(name != null){
					nodeService.setProperty(dataListNodeRef, BeCPGModel.PROP_LV_VALUE, name);
				}
			}
			else{
				logger.warn("dataListNodeRef doesn't exist : " + dataListNodeRef);
			}			
		}
		
		return I18NUtil.getMessage(MSG_SUCCESS);
	}

}
