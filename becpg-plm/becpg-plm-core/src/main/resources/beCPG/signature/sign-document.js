function main() {
	
	var docDeliverable;

	for (var i = 0; i < project.deliverableList.size(); i++) {
		var deliverable = project.deliverableList.get(i);
		if (deliverable.name.endsWith("url") && deliverable.tasks.contains(task.nodeRef)) {
			docDeliverable = deliverable;
			break;
		}
	}

	var document = search.findNode(docDeliverable.content);

	bSign.signDocument(document);
	
}

main();