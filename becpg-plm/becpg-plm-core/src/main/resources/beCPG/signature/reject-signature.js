function main() {
	
	var docDeliverable = null;
	
	for (var i = 0; i < project.deliverableList.size(); i++) {
		var del = project.deliverableList.get(i);
		if (del.name.endsWith(" - doc") && del.name.startsWith(deliverable.name)) {
			docDeliverable = del;
			break;
		}
	}

	if (urlDeliverable && urlDeliverable.content) {
		var document = search.findNode(docDeliverable.content);
		
		var recipients = document.assocs["sign:recipients"];
			
		document = bSign.cancelSignature(document);
		
		for (var j in recipients) {
			var recipient = recipients[j];
			document.createAssociation(recipient, "sign:recipients");
		}
	
		document.properties["sign:status"] = "Initialized";
		
		document.save();
	}
}

main();