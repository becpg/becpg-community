function main() {
	var projectEntity = null;

	var projectNodeRef = search.findNode(project.nodeRef);
	
	if (project.entities != null && project.entities.size() > 0 && projectNodeRef.assocs["bcpg:supplierAccountRef"] && projectNodeRef.assocs["bcpg:supplierAccountRef"].length > 0) {
		
		var supplier = projectNodeRef.assocs["bcpg:supplierAccountRef"][0];
		
		projectEntity = search.findNode(project.entities.get(0));
		/*
		  if no autoMerge date is specified  merge and validate entity 
		 */
		projectEntity = bSupplier.validateProjectEntity(projectEntity);

		project.entities.add(projectEntity.nodeRef);
		
		var suppliers = [];
		
		suppliers.push(supplier);
		
		var notificationSignedDocument = null;

		for (var i = 0; i < project.deliverableList.size(); i++) {
			var deliverable = project.deliverableList.get(i);
			
			if (deliverable.name == "notificationSignedDocument") {
				notificationSignedDocument = deliverable;
			}
		}
		
		if (notificationSignedDocument && notificationSignedDocument.content) {
			
			var signedDocument = search.findNode(notificationSignedDocument.content);
			
			var documentName = signedDocument.properties["cm:name"];
			var shareId = bcpg.shareContent(signedDocument);
			
			var templateArgs = {};
			var templateModel = {};
	
			templateArgs['documentName'] = documentName;
			templateArgs['shareId'] = shareId;
			templateModel['args'] = templateArgs;
			
			bcpg.sendMail(suppliers, bcpg.getMessage("plm.supplier.portal.sign.mail.title", documentName), "/app:company_home/app:dictionary/app:email_templates/cm:workflownotification/cm:signature-notify-email.ftl", templateModel, true);
		}
		
	}
}

main();