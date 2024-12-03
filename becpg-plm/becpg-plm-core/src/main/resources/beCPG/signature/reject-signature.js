function main() {
	
	var docDeliverable = null;
	
	for (var i = 0; i < project.deliverableList.size(); i++) {
		var del = project.deliverableList.get(i);
		if (del.name == keyName + " - doc") {
			docDeliverable = del;
			break;
		}
	}

	if (urlDeliverable.content) {
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