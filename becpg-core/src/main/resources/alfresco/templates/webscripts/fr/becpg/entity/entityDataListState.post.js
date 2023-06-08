function updateNodeState(scriptNode, state) {
	if (scriptNode != null && state != null) {
		scriptNode.properties["bcpg:entityDataListState"] = state;
		scriptNode.save();
	}
}

if (url.templateArgs.store_type && url.templateArgs.store_id && url.templateArgs.id) {
	var nodeRef = url.templateArgs.store_type + "://" + url.templateArgs.store_id + "/" + url.templateArgs.id;
	var node = search.findNode(nodeRef);
	updateNodeState(node, args.state);
} else if (args.nodeRefs) {
	var nodeRefs = args.nodeRefs.split(",");
	for (var i = 0; i < nodeRefs.length; i++) {
		var nodeRef = nodeRefs[i];
		updateNodeState(search.findNode(nodeRef), args.state);
	}
}

model.newValue = args.state;

model.overallSuccess = true;
