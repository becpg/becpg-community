function main() {
	
	var document = search.findNode("workspace://SpacesStore/" + task.name.split("-signTask-")[1]);
	
	var recipient = search.findNode(task.resources.get(0));
	
	var taskScriptNode = search.findNode(task.nodeRef);
	
	var recipients = [];
	
	recipients.push(recipient);
	
	search.findNode(bSign.prepareForSignature(document, recipients));
			
	var signatureUrl = bSign.getSignatureView(document, null, document.nodeRef);
	
	for each (var deliverable in taskScriptNode.sourceAssocs["pjt:dlTask"]) {
		if (deliverable.properties["cm:name"].includes(" - url")) {
			deliverable.properties["pjt:dlUrl"] = signatureUrl;
			deliverable.save();
			break;
		}
	}

}

main();