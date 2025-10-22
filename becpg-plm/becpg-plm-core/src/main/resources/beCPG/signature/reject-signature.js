function main() {
	
	var docDeliverable = bSignProject.findDocDeliverable(project, deliverable);

	if (docDeliverable) {
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