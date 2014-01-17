package fr.becpg.repo.listvalue.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class TargetAssocValueExtractor implements ListValueExtractor<NodeRef> {

	private QName propName;
	
	private NodeService nodeService;
	
	private NamespaceService namespaceService;
	

	public TargetAssocValueExtractor(QName propName,NodeService nodeService,NamespaceService namespaceService) {
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
    			QName type =  nodeService.getType(nodeRef);
    			String cssClass = type.getLocalName();
    			Map<String,String> props = new HashMap<String,String>(2);
    			props.put("type", type.toPrefixString(namespaceService));
    			//#798
//    			if(nodeService.hasAspect(nodeRef, ProductModel.ASPECT_PRODUCT)){
//    				String state = (String )nodeService.getProperty(nodeRef, ProductModel.PROP_PRODUCT_STATE);
//    				cssClass+="-"+state;
//    				props.put("state", state);
//    			}
    			if(nodeService.hasAspect(nodeRef, BeCPGModel.ASPECT_COLOR)){
    				props.put("color", (String)nodeService.getProperty(nodeRef, BeCPGModel.PROP_COLOR));
    			}
    			
    			ListValueEntry entry = new ListValueEntry(nodeRef.toString(),name, cssClass, props);
   
    			suggestions.add(entry);
    			
    		}
    	}
		return suggestions;
	}
	
}