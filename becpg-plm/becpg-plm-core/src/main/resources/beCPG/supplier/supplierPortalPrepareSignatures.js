<import resource="classpath:/beCPG/rules/helpers.js">

function findDocumentsToSign(folder) {
	
	var docs = [];
	
	var children = folder.childAssocs["cm:contains"];

	for (var i in children) {
		if (children[i].type == "{http://www.alfresco.org/model/content/1.0}content" && children[i].assocs["sign:recipients"]) {
			docs.push(children[i]);
		} else if (children[i].type == "{http://www.alfresco.org/model/content/1.0}folder") {
			docs = docs.concat(findDocumentsToSign(children[i]));
		}
	}
	
	return docs;
	
}

function extractRecipients(projectNode, entity) {
	
	var originalAssocs = entity.assocs["sign:recipients"];
	
	for (var i in originalAssocs) {
		var assoc = originalAssocs[i];
		entity.removeAssociation(assoc, "sign:recipients");
	}
	
	var newAssocs = bProject.extractResources(projectNode, originalAssocs);
	
	for (var i in newAssocs) {
		var assoc = newAssocs[i];
		entity.createAssociation(assoc, "sign:recipients");
	}
	
	var recipients = [];
	
	for (var i in newAssocs) {
		
		var authority = newAssocs[i];
		
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

function createPrepareDeliverable(deliverableList, doc, signTask, recipient) {
	var prepareDeliv = deliverableList.createNode(doc.properties["cm:name"] + recipient.properties["cm:firstName"] + recipient.properties["cm:lastName"] + " - prepare", "pjt:deliverableList");
	prepareDeliv.properties["pjt:dlDescription"] = doc.properties["cm:name"];
	prepareDeliv.properties["pjt:dlState"] = "Planned";
	prepareDeliv.properties["pjt:dlScriptExecOrder"] = "Pre";
	prepareDeliv.save();
	
	var prepareScript = search.xpathSearch("/app:company_home/app:dictionary/app:scripts/cm:prepare-signature.js")[0];
	prepareDeliv.createAssociation(prepareScript, "pjt:dlContent");
	prepareDeliv.createAssociation(signTask, "pjt:dlTask");
}

function createUrlDeliverable(deliverableList, doc, signTask, recipient) {
	var urlDeliv = deliverableList.createNode(doc.properties["cm:name"] + recipient.properties["cm:firstName"] + recipient.properties["cm:lastName"] + " - url", "pjt:deliverableList");
	urlDeliv.properties["pjt:dlDescription"] = doc.properties["cm:name"];
	urlDeliv.properties["pjt:dlState"] = "Planned";
	urlDeliv.save();
	urlDeliv.createAssociation(doc, "pjt:dlContent");
	urlDeliv.createAssociation(signTask, "pjt:dlTask");
}

function createSigningDeliverable(deliverableList, doc, signTask, recipient) {
	var signingDeliv = deliverableList.createNode(doc.properties["cm:name"] + recipient.properties["cm:firstName"] + recipient.properties["cm:lastName"] + " - signing", "pjt:deliverableList");
	signingDeliv.properties["pjt:dlDescription"] = doc.properties["cm:name"];
	signingDeliv.properties["pjt:dlState"] = "Planned";
	signingDeliv.properties["pjt:dlScriptExecOrder"] = "Post";
	signingDeliv.save();
	var signingScript = search.xpathSearch("/app:company_home/app:dictionary/app:scripts/cm:sign-document.js")[0];
	signingDeliv.createAssociation(signingScript, "pjt:dlContent");
	signingDeliv.createAssociation(signTask, "pjt:dlTask");
}

function createSigningTask(project, doc, recipient) {
	var taskList = getEntityListFromNode(project, "taskList");
	var deliverableList = getEntityListFromNode(project, "deliverableList");
	
	var signTaskName = bcpg.getMessage("signatureWorkflow.task-signature.name", doc.properties["cm:name"]);
				
	var signTask = taskList.createNode(recipient.properties["cm:userName"] + "-signTask-" + doc.nodeRef.id, "pjt:taskList");
	signTask.properties["pjt:tlTaskName"] = signTaskName;
	
	var signTaskDescription = bcpg.getMessage("signatureWorkflow.task-signature.description", doc.properties["cm:name"], recipient.properties["cm:firstName"], recipient.properties["cm:lastName"]);
	signTask.properties["pjt:tlTaskDescription"] = signTaskDescription;
	signTask.properties["pjt:notificationFrequencyValue"] = 7;
	signTask.properties["pjt:notificationInitialValue"] = -1;
	signTask.save();
	signTask.createAssociation(recipient, "pjt:tlResources");
	signTask.createAssociation(recipient, "pjt:notificationAuthorities");
	
	createPrepareDeliverable(deliverableList, doc, signTask, recipient);
	
	createUrlDeliverable(deliverableList, doc, signTask, recipient);
	
	createSigningDeliverable(deliverableList, doc, signTask, recipient);
		
	return signTask;
}

function createClosingTask(project) {

	var taskList = getEntityListFromNode(project, "taskList");
	var deliverableList = getEntityListFromNode(project, "deliverableList");

	var validatingTaskName = bcpg.getMessage("plm.supplier.portal.task.closing.name");

	var validatingTask = taskList.createNode("validatingTask", "pjt:taskList");
	validatingTask.properties["pjt:tlTaskName"] = validatingTaskName;
	validatingTask.save();

	validatingTask.createAssociation(people.getPerson(project.properties["cm:creator"]), "pjt:tlResources");

	var validatingDeliv = deliverableList.createNode(validatingTaskName, "pjt:deliverableList");
	validatingDeliv.properties["pjt:dlScriptExecOrder"] = "Post";
	validatingDeliv.properties["pjt:dlDescription"] = validatingTaskName;
	validatingDeliv.properties["pjt:dlState"] = "Planned";
	validatingDeliv.save();
	validatingDeliv.createAssociation(validatingTask, "pjt:dlTask");
	var validatingScript = search.xpathSearch("/app:company_home/app:dictionary/app:scripts/cm:validateProjectEntity.js")[0];
	validatingDeliv.createAssociation(validatingScript, "pjt:dlContent");

	return validatingTask;
}

function copySupplierSheet(projectNode, entity, documentFolder) {
		
	var report = bcpg.getReportNodeOfKind(entity, "SupplierSheet");
	
	if (report != null) {
		
		var reportName = report.name;
		
		var lastDotIndex = reportName.lastIndexOf(".");
		
		if (lastDotIndex != -1) {
			var extension = reportName.substring(lastDotIndex);
			
			var nameWithoutExtension = reportName.substring(0, lastDotIndex) + " - " + new Date().toISOString().slice(0, 10);
			
			reportName = nameWithoutExtension + extension;
		}
			
		var signedReport = documentFolder.createNode(bcpg.getAvailableName(documentFolder, reportName, true), "cm:content");
		
		var suppliers = projectNode.assocs["bcpg:supplierAccountRef"];
		
		updateAssoc(signedReport, "sign:recipients", suppliers);
		
		bcpg.copyContent(report, signedReport);
		
	}
}

function main() {

	if (project.entities != null && project.entities.size() > 0) {
		
		var entity = search.findNode(project.entities.get(0));

		var projectNode = search.findNode(project.nodeRef);
		
		var supplierFolder = getOrCreateFolderByPath(entity, 'SupplierDocuments');
		
		copySupplierSheet(projectNode, entity, supplierFolder);
		
		var docs = findDocumentsToSign(supplierFolder);
		
		var taskNode = search.findNode(task.nodeRef);
		
		var previousTasks = [];
		
		previousTasks.push(taskNode);
		
		for each(var doc in docs) {

			var lastTask = null;
			
			var recipients = extractRecipients(projectNode, doc);
			
			for (var i in recipients) {
				
				var recipient = recipients[i];
				
				var signTask = createSigningTask(projectNode, doc, recipient);
				
				if (lastTask == null) {
					signTask.createAssociation(taskNode, "pjt:tlPrevTasks");
				} else {
					signTask.createAssociation(lastTask, "pjt:tlPrevTasks");
				}

				lastTask = signTask;
				
			}
			
			previousTasks.push(lastTask);
			
		}
		
		var closingTask = createClosingTask(projectNode);
		
		for (var i in previousTasks) {
			closingTask.createAssociation(previousTasks[i], "pjt:tlPrevTasks");
		}
		
	}
}

main();
