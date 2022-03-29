function main() {

	var urlDeliverable;

	for (var i = 0; i < project.deliverableList.size(); i++) {
		var deliverable = project.deliverableList.get(i);
		if (deliverable.name.endsWith("url") && deliverable.tasks.contains(task.nodeRef)) {
			urlDeliverable = deliverable;
			break;
		}
	}

	var document = search.findNode(urlDeliverable.content);

	var recipient = search.findNode(task.resources.get(0));

	var recipients = [];

	recipients.push(recipient);

	bSign.prepareForSignature(document, recipients);

	var signatureUrl = bSign.getSignatureView(document, null, task.nodeRef);

	urlDeliverable.url = signatureUrl;

}

main();