function extractRecipients(entity) {
	var recipients = [];
			
	for (var i in entity.assocs["sign:recipients"]) {
		
		var authority = entity.assocs["sign:recipients"][i];
		
		if (authority.type == "{http://www.alfresco.org/model/content/1.0}authorityContainer") {
			
			var members = authority.childAssociations["cm:member"];
			
			for (var index in members) {
				recipients.push(members[index]);
			}
		} else {
			recipients.push(authority);
		}
	}
	
	return recipients;
}

function main() {
	
	var docDeliverable;

	for (var i = 0; i < project.deliverableList.size(); i++) {
		var deliverable = project.deliverableList.get(i);
		if (deliverable.name.endsWith("url") && deliverable.tasks.contains(task.nodeRef)) {
			docDeliverable = deliverable;
			break;
		}
	}

	var document = search.findNode(docDeliverable.content);

	document = bSign.signDocument(document);
	
	var suppliers = extractRecipients(document);
	
	if (document.properties["sign:status"] == "Signed") {
		
		var documentName = document.properties["cm:name"];
		var shareId = bcpg.shareContent(document);
		
		var templateArgs = {};
		var templateModel = {};

		templateArgs['documentName'] = documentName;
		templateArgs['shareId'] = shareId;
		templateModel['args'] = templateArgs;
		
		bcpg.sendMail(suppliers, bcpg.getMessage("plm.supplier.portal.sign.mail.title", documentName), "/app:company_home/app:dictionary/app:email_templates/cm:workflownotification/cm:signature-notify-email.ftl", templateModel, true);
	}
	
}

main();