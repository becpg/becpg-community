<import resource="classpath:/beCPG/rules/helpers.js">

function main() {
	
	var nodeRef = args.nodeRef;
	
	if (!nodeRef) {
		status.setCode(status.STATUS_BAD_REQUEST, "nodeRef parameter is not present");
		return;
	}
	
	// Get node form nodeRef and check if existing
	var node = search.findNode(nodeRef);
	
	if (node != null) {
		
		// If node is not an entity, search for the entity containing the node
		var entity = node; 
		
		if (!node.isSubType("bcpg:entityV2")) {
			entity = getEntity(node);
		}
		
		if (entity != null) {
			
			
			// Get entity source based on assoc pjt:projectEntity (projectNode)
			var filter = {
				"maxResults": 1
			};
			
			var sources = entitySourceAssocs(entity, "pjt:projectEntity", filter);
			if(sources !=null && sources.length>0){
				model.entity = sources[0];
			} else {
				status.setCode(status.STATUS_BAD_REQUEST, "node entity source is not found");
				return;
			}
			
		} else {
			status.setCode(status.STATUS_BAD_REQUEST, "node " + nodeRef + " entity is not found");
			return;
		}
	} else {
		status.setCode(status.STATUS_BAD_REQUEST, "node " + nodeRef + " is not found");
		return;
	}
}

main();
