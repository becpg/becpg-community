package fr.becpg.repo.quality;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.alfresco.model.ContentModel;
import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;
import org.springframework.extensions.surf.util.I18NUtil;

import fr.becpg.common.RepoConsts;
import fr.becpg.model.BeCPGModel;
import fr.becpg.model.DataListModel;

/**
 * 
 * @author querephi
 *
 */
public class DataListsDAOImpl implements DataListsDAO{

	/** The Constant RESOURCE_TITLE. */
	private static final String RESOURCE_TITLE = "bcpg_bcpgmodel.type.bcpg_%s.title";
	
	/** The Constant RESOURCE_DESCRIPTION. */
	private static final String RESOURCE_DESCRIPTION = "bcpg_bcpgmodel.type.bcpg_%s.description";
	
	
	private NodeService nodeService;
	
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
	}

	@Override
	public NodeRef getListContainer(NodeRef nodeRef) {
				
		return nodeService.getChildByName(nodeRef, BeCPGModel.ASSOC_DATALISTS, RepoConsts.CONTAINER_DATALISTS);
	}
	
	@Override
	public NodeRef getList(NodeRef listContainerNodeRef, QName listQName) {
		
		if(listQName== null){
			return null;
		}
			
		NodeRef listNodeRef = null;		
		if(listContainerNodeRef != null){
			listNodeRef = nodeService.getChildByName(listContainerNodeRef, ContentModel.ASSOC_CONTAINS, listQName.getLocalName());		
		}
		return listNodeRef;	
	}

	@Override
	public NodeRef createListContainer(NodeRef nodeRef) {
		
		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		properties.put(ContentModel.PROP_NAME, RepoConsts.CONTAINER_DATALISTS);
		properties.put(ContentModel.PROP_TITLE, RepoConsts.CONTAINER_DATALISTS);
		return nodeService.createNode(nodeRef, BeCPGModel.ASSOC_DATALISTS, 
				BeCPGModel.ASSOC_DATALISTS, ContentModel.TYPE_FOLDER, properties).getChildRef();
	}
	
	@Override
	public NodeRef createList(NodeRef listContainerNodeRef, QName listQName) {
		
		Map<QName, Serializable> properties = new HashMap<QName, Serializable>();
		properties.put(ContentModel.PROP_NAME, listQName.getLocalName());
		properties.put(ContentModel.PROP_TITLE, I18NUtil.getMessage(String.format(RESOURCE_TITLE, listQName.getLocalName())));
		properties.put(ContentModel.PROP_DESCRIPTION, I18NUtil.getMessage(String.format(RESOURCE_DESCRIPTION, listQName.getLocalName())));
		properties.put(DataListModel.PROP_DATALISTITEMTYPE, BeCPGModel.BECPG_PREFIX + ":" + listQName.getLocalName());
		return nodeService.createNode(listContainerNodeRef, ContentModel.ASSOC_CONTAINS, listQName, DataListModel.TYPE_DATALIST, properties).getChildRef();
	}

}
