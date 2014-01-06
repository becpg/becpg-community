/*
 * 
 */
package fr.becpg.repo.product;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.model.SystemState;

/**
 * The Class ProductDictionaryServiceImpl.
 *
 * @author querephi
 */
@Service
public class ProductDictionaryServiceImpl implements ProductDictionaryService {
	
	/** The logger. */
	private static Log logger = LogFactory.getLog(ProductDictionaryServiceImpl.class);
	
	/** The node service. */
	private NodeService nodeService;
	
	/**
	 * Sets the node service.
	 *
	 * @param nodeService the new node service
	 */
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}
	
	/**
	 * Gets the system state.
	 *
	 * @param systemState the system state
	 * @return the system state
	 */
	public static SystemState getSystemState(String systemState) {
		
		return (systemState != null && systemState != "") ? SystemState.valueOf(systemState) : SystemState.ToValidate;		
	}	
	
	@Override
	public QName getWUsedList(NodeRef childNodeRef) {
		
		QName wusedList = null;
		QName type = nodeService.getType(childNodeRef);
		
		if(type.equals(BeCPGModel.TYPE_RAWMATERIAL) ||
				type.equals(BeCPGModel.TYPE_SEMIFINISHEDPRODUCT) ||
				type.equals(BeCPGModel.TYPE_LOCALSEMIFINISHEDPRODUCT) ||
				type.equals(BeCPGModel.TYPE_FINISHEDPRODUCT)){
			
			wusedList = BeCPGModel.TYPE_COMPOLIST;
		}
		else if(type.equals(BeCPGModel.TYPE_PACKAGINGMATERIAL) ||
				type.equals(BeCPGModel.TYPE_PACKAGINGKIT)){
			
			wusedList = BeCPGModel.TYPE_PACKAGINGLIST;
		}
		else{
			logger.error("Unknown productType: " + type);
		}
		
		return wusedList;
	}
	
}
