package fr.becpg.repo.admin.patch;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.product.data.productList.PackagingLevel;
import fr.becpg.repo.search.BeCPGSearchService;

/**
 * QualityPatch
 * @author quere
 *
 */
public class PackagingLevelPatch extends AbstractBeCPGPatch {
	
	private static Log logger = LogFactory.getLog(PackagingLevelPatch.class);
	private static final String MSG_SUCCESS = "patch.bcpg.packagingLevelPatch.result";

	private BeCPGSearchService beCPGSearchService;
	
	public void setBeCPGSearchService(BeCPGSearchService beCPGSearchService) {
		this.beCPGSearchService = beCPGSearchService;
	}

	@Override
	protected String applyInternal() throws Exception {
		
		List<NodeRef> dataListNodeRefs = beCPGSearchService.luceneSearch("+TYPE:\"bcpg:packagingList\"");
		logger.info("PackagingLevel migrator, size: " + dataListNodeRefs.size());
		
		for(NodeRef dataListNodeRef : dataListNodeRefs){
			if(nodeService.exists(dataListNodeRef)){
				String packagingLevel = (String)nodeService.getProperty(dataListNodeRef, BeCPGModel.PROP_PACKAGINGLIST_PKG_LEVEL);
				if(packagingLevel != null){
					if(packagingLevel.equals("Primaire")){
						nodeService.setProperty(dataListNodeRef, BeCPGModel.PROP_PACKAGINGLIST_PKG_LEVEL, PackagingLevel.Primary.toString());
					}
					else if(packagingLevel.equals("Secondaire")){
						nodeService.setProperty(dataListNodeRef, BeCPGModel.PROP_PACKAGINGLIST_PKG_LEVEL, PackagingLevel.Secondary.toString());
					}
					else if(packagingLevel.equals("Tertiaire")){
						nodeService.setProperty(dataListNodeRef, BeCPGModel.PROP_PACKAGINGLIST_PKG_LEVEL, PackagingLevel.Tertiary.toString());
					}
					else{
						logger.warn("Unknown packagingLevel " + packagingLevel);
					}
				}
			}
			else{
				logger.warn("dataListNodeRef doesn't exist : " + dataListNodeRef);
			}			
		}
		
		// delete list
		NodeRef packagingLevelList = searchFolder("/app:company_home/cm:System/cm:Lists/bcpg:entityLists/cm:PackagingLevels");
		if(packagingLevelList != null){
			nodeService.deleteNode(packagingLevelList);
		}
		
		return I18NUtil.getMessage(MSG_SUCCESS);
	}

}
