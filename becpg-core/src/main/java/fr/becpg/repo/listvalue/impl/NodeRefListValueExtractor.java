package fr.becpg.repo.listvalue.impl;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.alfresco.service.namespace.QName;

import fr.becpg.repo.listvalue.ListValueEntry;
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
	public List<ListValueEntry> extract(List<NodeRef> nodeRefs) {
		List<ListValueEntry> suggestions = new ArrayList<ListValueEntry>();
    	if(nodeRefs!=null){
    		for(NodeRef nodeRef : nodeRefs){
    			
    			String name = (String)nodeService.getProperty(nodeRef, propName);
    			suggestions.add(new ListValueEntry(nodeRef.toString(),name,nodeService.getType(nodeRef).getLocalName()));
    			
    		}
    	}
		return suggestions;
	}
	
}