package fr.becpg.repo.listvalue.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

import fr.becpg.repo.listvalue.ListValueExtractor;

/**
 * Used to extract properties from nodeRef
 * @author "Matthieu Laborie <matthieu.laborie@becpg.fr>"
 *
 */
public class NodeRefListValueExtractor implements ListValueExtractor<NodeRef> {

	private QName propName;
	
	private NodeService nodeService;
	
	

	public NodeRefListValueExtractor(QName propName,NodeService nodeService) {
		super();
		this.propName = propName;
		this.nodeService = nodeService;
	}

	@Override
	public Map<String, String> extract(List<NodeRef> nodeRefs) {
		Map<String, String> suggestions = new HashMap<String, String>();
    	if(nodeRefs!=null){
    		for(NodeRef nodeRef : nodeRefs){
    			
    			String name = (String)nodeService.getProperty(nodeRef, propName);
                suggestions.put(nodeRef.toString(), name); 			
    		}
    	}
		return suggestions;
	}
	
}