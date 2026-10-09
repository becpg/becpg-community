package org.alfresco.repo.content;

import java.io.Serializable;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

/**
 * <p>BeCPGContentServiceImpl class.</p>
 *
 * @author matthieu
 */
public class BeCPGContentServiceImpl extends ContentServiceImpl {
	

	/** Constant <code>BECPG_URI="http://www.bcpg.fr/model/becpg/1.0"</code> */
	public static final String BECPG_URI = "http://www.bcpg.fr/model/becpg/1.0";
	/** Constant <code>ASPECT_SORTABLE_LIST</code> */
	public static final QName ASPECT_SORTABLE_LIST = QName.createQName(BECPG_URI, "sortableListAspect");
	
	private NodeService nodeService;
	

	/** {@inheritDoc} */
	@Override
	public void setNodeService(NodeService nodeService) {
		this.nodeService = nodeService;
		super.setNodeService(nodeService);
	}



	/** {@inheritDoc} */
	@Override
	public void onUpdateProperties(NodeRef nodeRef, Map<QName, Serializable> before, Map<QName, Serializable> after) {
		
		//PERFS issue here 
		//Do not test rules on dataListItem
        if (nodeService.hasAspect(nodeRef, ASPECT_SORTABLE_LIST))
        {
            return;
        }
		
		
		super.onUpdateProperties(nodeRef, before, after);
	}
	
}
