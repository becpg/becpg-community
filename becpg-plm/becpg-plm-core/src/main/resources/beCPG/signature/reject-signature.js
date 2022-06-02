function main() {
	
	var docDeliverable;

	for (var i = 0; i < project.deliverableList.size(); i++) {
		var deliverable = project.deliverableList.get(i);
		if (deliverable.name.endsWith("doc") && deliverable.tasks.contains(task.nodeRef)) {
			docDeliverable = deliverable;
			break;
		}
	}

	var document = search.findNode(docDeliverable.content);

	if (document.assocs["cm:workingcopylink"] && document.assocs["cm:workingcopylink"].length > 0) {
		var workingCopy = document.assocs["cm:workingcopylink"][0];
		workingCopy.cancelCheckout();
	}
	
	var preparedRecipients = document.assocs["sign:preparedRecipients"];

	if (preparedRecipients) {
		for (var i = 0; i < preparedRecipients.length; i++) {
			var preparedRecipient = preparedRecipients[i];
	
			document.removeAssociation(preparedRecipient, "sign:preparedRecipients");
		}
	}
	
	document.properties["sign:status"] = "Initialized";
	
	document.save();
}

main();