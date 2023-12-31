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
	
	var urlDeliverable = null;
	
	var keyName = deliverable.name.replace(" - sign", "");
	
	for (var i = 0; i < project.deliverableList.size(); i++) {
		var del = project.deliverableList.get(i);
		if (del.name.endsWith(" - url") && del.name.startsWith(keyName)) {
			urlDeliverable = del;
			break;
		}
	}

	var document = search.findNode(urlDeliverable.content);

	document = bSign.signDocument(document);
	
	if (document.properties["sign:status"] == "Signed") {
		
		if (document.assocs["cm:original"] && document.assocs["cm:original"].length > 0) {
			
			var originalDoc = document.assocs["cm:original"][0];
			
			var checkout = originalDoc.checkout();
			
			bcpg.copyContent(document, checkout);
			
			checkout.properties["sign:status"] = document.properties["sign:status"];
			checkout.properties["sign:validator"] = document.properties["sign:validator"];
			checkout.properties["sign:validationDate"] = document.properties["sign:validationDate"];
			checkout.properties["sign:recipientsData"] = document.properties["sign:recipientsData"];
			
			checkout.save();
			
			document.remove();
			
			document = checkout.checkin();
		}
		
		var suppliers = extractRecipients(document);
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