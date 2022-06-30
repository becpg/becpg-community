function main() {
	var projectEntity = null;

	var projectNodeRef = search.findNode(project.nodeRef);
	var supplier = projectNodeRef.assocs["bcpg:supplierAccountRef"][0];

	if (project.entities != null && project.entities.size() > 0) {
		projectEntity = search.findNode(project.entities.get(0));
		/*
		  if no autoMerge date is specified  merge and validate entity 
		 */
		projectEntity = bSupplier.validateProjectEntity(projectEntity);

		project.entities.add(projectEntity.nodeRef);
		
		var destFolder = projectEntity.childByNamePath(bcpg.getTranslatedPath('SupplierDocuments'));
		
		var suppliers = [];
		
		suppliers.push(supplier);
		
		if (destFolder != null) {

			for (var i = 0; i < destFolder.children.length; i++) {
				if (destFolder.children[i].hasAspect("sign:signatureAspect") && destFolder.children[i].properties["sign:status"] == "Signed") {
					var documentName = destFolder.children[i].properties["cm:name"];
					var shareId = bcpg.shareContent(destFolder.children[i]);
					
					var templateArgs = {};
					var templateModel = {};

					templateArgs['documentName'] = documentName;
					templateArgs['shareId'] = shareId;
					templateModel['args'] = templateArgs;
					
					bcpg.sendMail(suppliers, bcpg.getMessage("plm.supplier.portal.sign.mail.title", documentName), "/app:company_home/app:dictionary/app:email_templates/cm:workflownotification/cm:signature-notify-email.ftl", templateModel, true);
				}
			}
		}
	}
}

main();