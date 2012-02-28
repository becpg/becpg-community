package fr.becpg.repo.listvalue;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.NamespaceService;
import org.alfresco.service.namespace.QName;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.listvalue.ListValueEntry;
import fr.becpg.repo.listvalue.ListValueExtractor;

/**
 * Used to extract properties from product
 * @author "Matthieu Laborie <matthieu.laborie@becpg.fr>"
 *
 */
public class ProductValueExtractor implements ListValueExtractor<NodeRef> {

	private QName propName;
	
	private NodeService nodeService;
	
	private NamespaceService namespaceService;
	
	

	public ProductValueExtractor(QName propName,NodeService nodeService,NamespaceService namespaceService) {
		super();
		this.propName = propName;
		this.nodeService = nodeService;
		this.namespaceService = namespaceService;
	}


	@Override
	public List<ListValueEntry> extract(List<NodeRef> nodeRefs) {
		List<ListValueEntry> suggestions = new ArrayList<ListValueEntry>();
    	if(nodeRefs!=null){
    		for(NodeRef nodeRef : nodeRefs){
    			
    			String name = (String)nodeService.getProperty(nodeRef, propName);
    			String state = (String )nodeService.getProperty(nodeRef, BeCPGModel.PROP_PRODUCT_STATE);
    			QName type = nodeService.getType(nodeRef);
    			ListValueEntry entry = new ListValueEntry(nodeRef.toString(),name,type.getLocalName()+"-"+state);
    			entry.getMetadatas().put("type", type.toPrefixString(namespaceService));
    			entry.getMetadatas().put("state", state);
    			suggestions.add(entry);
    			
    		}
    	}
		return suggestions;
	}
	
}