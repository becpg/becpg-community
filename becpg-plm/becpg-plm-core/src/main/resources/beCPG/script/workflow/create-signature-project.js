<import resource="classpath:/beCPG/rules/helpers.js">

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

function createRejectTask(project, doc, taskUser) {
	var taskList = getEntityListFromNode(project, "taskList");
	var deliverableList = getEntityListFromNode(project, "deliverableList");
	
	var rejectTaskName = bcpg.getMessage("signatureWorkflow.task-reject.name", doc.properties["cm:name"]);
	var rejectTask = taskList.createNode(doc.properties["cm:name"] + "-rejectTask-" + doc.nodeRef.id, "pjt:taskList");

	rejectTask.properties["pjt:tlTaskName"] = rejectTaskName;
	rejectTask.properties["pjt:tlTaskDescription"] = bcpg.getMessage("signatureWorkflow.task-reject.description");
	rejectTask.properties["pjt:tlState"] = "Cancelled";
	rejectTask.save();
	rejectTask.createAssociation(taskUser, "pjt:tlResources");

	var rejectDeliv = deliverableList.createNode(rejectTaskName + "-script", "pjt:deliverableList");
	rejectDeliv.properties["pjt:dlDescription"] = doc.properties["cm:name"];
	rejectDeliv.properties["pjt:dlState"] = "Planned";
	rejectDeliv.properties["pjt:dlScriptExecOrder"] = "Pre";
	rejectDeliv.save();
	var rejectScript = search.xpathSearch("/app:company_home/app:dictionary/app:scripts/cm:reject-signature.js")[0];
	rejectDeliv.createAssociation(rejectScript, "pjt:dlContent");
	rejectDeliv.createAssociation(rejectTask, "pjt:dlTask");

	var rejectDocDeliv = deliverableList.createNode(rejectTaskName + "-doc", "pjt:deliverableList");
	rejectDocDeliv.properties["pjt:dlDescription"] = doc.properties["cm:name"];
	rejectDocDeliv.properties["pjt:dlState"] = "Planned";
	rejectDocDeliv.save();
	rejectDocDeliv.createAssociation(doc, "pjt:dlContent");
	rejectDocDeliv.createAssociation(rejectTask, "pjt:dlTask");
	
	return rejectTask;
}

function createValidatingTask(project, doc, taskUser) {

	var taskList = getEntityListFromNode(project, "taskList");
	var deliverableList = getEntityListFromNode(project, "deliverableList");

	var validatingTaskName = bcpg.getMessage("signatureWorkflow.task-checkin.name", doc.properties["cm:name"]);

	var validatingTask = taskList.createNode(doc.properties["cm:name"] + "-validatingTask-" + doc.nodeRef.id, "pjt:taskList");
	validatingTask.properties["pjt:tlTaskName"] = validatingTaskName;
	validatingTask.save();

	validatingTask.createAssociation(taskUser, "pjt:tlResources");

	var validatingDeliv = deliverableList.createNode(validatingTaskName, "pjt:deliverableList");
	validatingDeliv.properties["pjt:dlScriptExecOrder"] = "Post";
	validatingDeliv.properties["pjt:dlDescription"] = validatingTaskName;
	validatingDeliv.properties["pjt:dlState"] = "Planned";
	validatingDeliv.save();
	validatingDeliv.createAssociation(validatingTask, "pjt:dlTask");
	var validatingScript = search.xpathSearch("/app:company_home/app:dictionary/app:scripts/cm:validate-signature.js")[0];
	validatingDeliv.createAssociation(validatingScript, "pjt:dlContent");

	var documentDeliv = deliverableList.createNode(doc.properties["cm:name"] + "-doc", "pjt:deliverableList");
	documentDeliv.properties["pjt:dlDescription"] = doc.properties["cm:name"];
	documentDeliv.properties["pjt:dlState"] = "Planned";
	documentDeliv.properties["pjt:dlUrl"] = bSign.getSignatureView(doc, null, validatingTask.nodeRef);
	documentDeliv.save();
	documentDeliv.createAssociation(validatingTask, "pjt:dlTask");
	documentDeliv.createAssociation(doc, "pjt:dlContent");
	
	return validatingTask;
}

function findDefaultTemplate() {
	var modelTemplates = search.xpathSearch("/app:company_home/cm:System/cm:EntityTemplates/cm:project");
			
	for (var i = 0; i < modelTemplates.length; i++) {
		var modelTemplate = modelTemplates[i];
		
		if (modelTemplate.properties["bcpg:entityTplIsDefault"] == true) {
			return modelTemplate;
		}
	}
}

function extractRecipients(projectNode) {
	
	var originalAssocs = projectNode.assocs["sign:recipients"];
	
	for (var i in originalAssocs) {
		var assoc = originalAssocs[i];
		projectNode.removeAssociation(assoc, "sign:recipients");
	}
	
	var newAssocs = bProject.extractResources(projectNode, originalAssocs);
	
	for (var i in newAssocs) {
		var assoc = newAssocs[i];
		projectNode.createAssociation(assoc, "sign:recipients");
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

function main() {

	var formDataJson = JSON.parse(formData);

	var destination = search.findNode(formDataJson.alf_destination);

	var project = destination.createNode(bcpg.getMessage("signatureWorkflow.project.name"), "pjt:project");

	if (project) {

		submitForm(project, formDataJson);
		
		if (!project.assocs["bcpg:entityTplRef"]) {
			project.createAssociation(findDefaultTemplate(), "bcpg:entityTplRef");
		}
		
		var currentUser = search.findNode(currentUserNodeRef);

		for each(var doc in items) {

			doc.createAssociation(currentUser, "sign:validator");
			
			updateAssoc(doc, "sign:recipients", project.assocs["sign:recipients"]);
			
			var rejectTask = createRejectTask(project, doc, currentUser);

			var lastTask = null;
			
			var recipients = extractRecipients(project);
			
			for (var i in recipients) {
				
				var recipient = recipients[i];
				
				var signTask = createSigningTask(project, doc, recipient);
				
				signTask.createAssociation(rejectTask, "pjt:tlRefusedTaskRef");
				
				if (lastTask != null) {
					signTask.createAssociation(lastTask, "pjt:tlPrevTasks");
				} else {
					signTask.createAssociation(rejectTask, "pjt:tlPrevTasks");
				}

				lastTask = signTask;
				
			}
			
			var validatingTask = createValidatingTask(project, doc, currentUser);
			
			validatingTask.createAssociation(lastTask, "pjt:tlPrevTasks");
			validatingTask.createAssociation(rejectTask, "pjt:tlRefusedTaskRef");
			
		}
		
        return project.nodeRef;
    }
    throw "Project not provided: ";
}

main();