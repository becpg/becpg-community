function main() {

	var entity = null;

	if (project.entities != null && project.entities.size() > 0) {
		entity = search.findNode(project.entities.get(0));

		var destFolder = entity.childByNamePath(bcpg.getTranslatedPath('SupplierDocuments'));

		var notificationSignedDocument = null;
		
		for (var i = 0; i < project.deliverableList.size(); i++) {
			var deliverable = project.deliverableList.get(i);
			
			if (deliverable.name == "notificationSignedDocument") {
				notificationSignedDocument = deliverable;
			}
		}

		if (destFolder != null) {
			
			for (var i = 0; i < destFolder.children.length; i++) {
				if (destFolder.children[i].hasAspect("sign:signatureAspect") && destFolder.children[i].properties["sign:status"] == "ReadyToSign") {
					
					var signedDocument = bSign.signDocument(destFolder.children[i]);
					notificationSignedDocument.description =  signedDocument.name;
					notificationSignedDocument.content =  signedDocument.nodeRef;
				}
			}
		}
	}
}

main();

