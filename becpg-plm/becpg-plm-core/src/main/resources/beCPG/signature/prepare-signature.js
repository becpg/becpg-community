function main() {

	var urlDeliverable = null;
	
	var keyName = deliverable.name.replace(" - prepare", "");
	
	for (var i = 0; i < project.deliverableList.size(); i++) {
		var del = project.deliverableList.get(i);
		if (del.name == keyName + " - url") {
			urlDeliverable = del;
			break;
		}
	}

	if (task.resources != null && !task.resources.isEmpty() && urlDeliverable && urlDeliverable.content) {
		var recipient = search.findNode(task.resources.get(0));
		var document = search.findNode(urlDeliverable.content);
	
		var recipients = [];
		recipients.push(recipient);
	
		bSign.prepareForSignature(document, recipients);
	
		var signatureUrl = bSign.getSignatureView(document, recipient, task.nodeRef);
	
		urlDeliverable.url = signatureUrl;
	}

}

main();