package fr.becpg.repo.admin.patch;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.repo.admin.patch.AbstractPatch;
import org.alfresco.service.cmr.model.FileFolderService;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.search.BeCPGSearchService;

/**
 * Remove some datalists
 * @author quere
 *
 */
public class RemoveDataListPatch1 extends AbstractPatch {
	
	private static Log logger = LogFactory.getLog(RemoveDataListPatch1.class);
	private static final String MSG_SUCCESS = "patch.bcpg.removeDataListPatch1.result";
	private static final String [] datalistsToRemoveOnRawMaterials = {"+TYPE:\"dl:dataList\" +@dl\\:dataListItemType:\"bcpg:labelClaim\" -PATH:\" /app:company_home/cm:System/cm:Characts//*\""};
	
	private static final String [] datalistsToRemoveOnFormulatedProducts = {"+TYPE:\"dl:dataList\" +@dl\\:dataListItemType:\"bcpg:priceList\"",
																			"+TYPE:\"dl:dataList\" +@dl\\:dataListItemType:\"bcpg:microbioList\"",
																			"+TYPE:\"dl:dataList\" +@dl\\:dataListItemType:\"bcpg:forbiddenIngList\""};
	

	private BeCPGSearchService beCPGSearchService;
	private FileFolderService fileFolderService;
	
	public void setBeCPGSearchService(BeCPGSearchService beCPGSearchService) {
		this.beCPGSearchService = beCPGSearchService;
	}

	public void setFileFolderService(FileFolderService fileFolderService) {
		this.fileFolderService = fileFolderService;
	}

	@Override
	protected String applyInternal() throws Exception {
		
		logger.info("RemoveDataListPatch1");
		
		List<QName> entityTypes = new ArrayList<>();
		entityTypes.add(BeCPGModel.TYPE_RAWMATERIAL);
		deleteDataLists(datalistsToRemoveOnRawMaterials, entityTypes);
		
		entityTypes.clear();
		entityTypes.add(BeCPGModel.TYPE_SEMIFINISHEDPRODUCT);
		entityTypes.add(BeCPGModel.TYPE_FINISHEDPRODUCT);
		deleteDataLists(datalistsToRemoveOnFormulatedProducts, entityTypes);
		
		return I18NUtil.getMessage(MSG_SUCCESS);
	}
	
	private void deleteDataLists(String [] queries, List<QName> entityTypes){
		for(String query : queries){
			List<NodeRef> dataListNodeRefs = beCPGSearchService.luceneSearch(query);
			logger.info("datalistsToRemove size: " + dataListNodeRefs.size());
			
			for(NodeRef dataListNodeRef : dataListNodeRefs){
				if(nodeService.exists(dataListNodeRef)){
					if(fileFolderService.listFiles(dataListNodeRef).isEmpty()){
						
						NodeRef containerListNodeRef = nodeService.getPrimaryParent(dataListNodeRef).getParentRef();
						NodeRef entityNodeRef = nodeService.getPrimaryParent(containerListNodeRef).getParentRef();
						QName type = nodeService.getType(entityNodeRef);
						if(entityTypes.contains(type)){
							logger.debug("Delete datalist on product " + entityNodeRef + " type: " + type);
							nodeService.deleteNode(dataListNodeRef);
						}
					}					
				}
				else{
					logger.warn("dataListNodeRef doesn't exist : " + dataListNodeRef);
				}			
			}
		}
	}

}
