var items = new Array(); 

if (args.nodeRefs) {
	var nodeRefs = args.nodeRefs.split(",");
	for (var i = 0; i < nodeRefs.length; i++) {
		var nodeRef = nodeRefs[i];
		var node = search.findNode(nodeRef);
		if(node.getContent()!=null) {
			node.properties["cm:content"].delete();
			delete node.properties["cm:content"];
			node.save();
			items.push(nodeRef);
		}
	}
}

model.items  = items;