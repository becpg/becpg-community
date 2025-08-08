function main() {

	var docDeliverable = null;
	
	for (var i = 0; i < project.deliverableList.size(); i++) {
		var del = project.deliverableList.get(i);
		if (del.name.endsWith(" - doc") && del.name.startsWith(deliverable.name)) {
			docDeliverable = del;
			break;
		}
	}

	if (urlDeliverable) {
		var document = search.findNode(docDeliverable.content);
		document.properties["sign:validationDate"] = new Date();
		document.save();
	}
	
	var projectNode = search.findNode(project.nodeRef);
	projectNode.remove();
}

main();