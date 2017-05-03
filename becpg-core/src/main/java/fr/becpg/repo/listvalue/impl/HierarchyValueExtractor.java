package fr.becpg.repo.listvalue.impl;

import java.util.ArrayList;
import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;
import org.alfresco.service.cmr.repository.NodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import fr.becpg.model.BeCPGModel;
import fr.becpg.repo.listvalue.ListValueEntry;
import fr.becpg.repo.listvalue.ListValueExtractor;

@Service("hierarchyValueExtractor")
public class HierarchyValueExtractor implements ListValueExtractor<NodeRef>{

	@Autowired
	private NodeService nodeService;
	
	@Override
	public List<ListValueEntry> extract(List<NodeRef> hierarchies) {
		List<ListValueEntry> suggestions = new ArrayList<>();
		if (hierarchies != null) {
			for(NodeRef hierarchy : hierarchies){
				String currentHierarchyPath = colorizeHierarchy(extractHierarchyFullName(hierarchy));
				suggestions.add(new ListValueEntry(hierarchy.toString(), currentHierarchyPath, ""));
			}
		}
		
		return suggestions;
	}
	
	private String extractHierarchyFullName(NodeRef hierarchy){
		String res = extractHierarchyName(hierarchy);
		NodeRef parent = (NodeRef) nodeService.getProperty(hierarchy, BeCPGModel.PROP_PARENT_LEVEL);
		if(parent != null){
			res = extractHierarchyFullName(parent) + " > " + res;
		}
		
		return res;
	}
	
	private String extractHierarchyName(NodeRef hierarchy){
		return (String) nodeService.getProperty(hierarchy, BeCPGModel.PROP_LKV_VALUE);
	}
	
	private String colorizeHierarchy(String hierarchyString){
		String res = hierarchyString;
		
		//.yui-skin-becpg .form-fields label
		return res;
	}

}
