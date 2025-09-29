function main() {

	var docDeliverable = bSignProject.findDocDeliverable(project, deliverable);

	if (docDeliverable) {
		var document = search.findNode(docDeliverable.content);
		document.properties["sign:validationDate"] = new Date();
		document.save();
	}
	
	var projectNode = search.findNode(project.nodeRef);
	projectNode.remove();
}

main();