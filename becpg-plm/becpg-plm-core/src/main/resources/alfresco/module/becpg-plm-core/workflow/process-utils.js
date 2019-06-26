function sendMail(userOrGroup, from, subject, message, templatePath, workflowDocuments) {
	try {
		if (userOrGroup != null) {
			var mail = actions.create("mail");
			mail.parameters.template_model = templateModel;

			if (userOrGroup.typeShort && userOrGroup.typeShort == "cm:authorityContainer") {
				mail.parameters.to_many = new Array(userOrGroup.properties["cm:authorityName"]);
			} else if (userOrGroup.typeShort && userOrGroup.typeShort == "cm:person") {
				mail.parameters.to_many = new Array(userOrGroup.properties["cm:userName"]);
			} else {
				mail.parameters.to_many = new Array(userOrGroup);
			}

			mail.parameters.subject = subject.replace("\n", " ").replace("\r", " ");
			if (from != null) {
				mail.parameters.from = from.properties.email;
			}
			mail.parameters.ignore_send_failure = true;

			var template = search.xpathSearch(templatePath)[0];
			if (template) {
				mail.parameters.template = template;
				var templateArgs = new Array();
				templateArgs['workflowTitle'] = message;
				templateArgs['workflowPooled'] = false;
				templateArgs['workflowDescription'] = bpm_workflowDescription;
				templateArgs['workflowDueDate'] = task.dueDate;
				templateArgs['workflowPriority'] = task.priority;
				templateArgs['workflowDocuments'] = workflowDocuments;
				templateArgs['workflowId'] = "activiti$" + task.id;

				var templateModel = new Array();
				templateModel['args'] = templateArgs;
				mail.parameters.template_model = templateModel;

				mail.execute(bpm_package);

			} else {
				logger.error("No template found for email : " + templatePath);
			}
		}
	} catch (e) {
		logger.error("Cannot send mail to :");
		logger.error(" - subject: " + subject);
		logger.error(" - e: " + e);
	}
}

function getDelegate(user) {
	var curDate = new Date(), startDate = (user.properties["pjt:delegationStartDate"] != null ? new Date(user.properties["pjt:delegationStartDate"]
			.getTime()) : null), endDate = (user.properties["pjt:delegationEndDate"] != null ? new Date(user.properties["pjt:delegationEndDate"]
			.getTime()) : null);

	if (user != null && user.properties["pjt:delegationActivated"] && (startDate == null || startDate <= curDate)
			&& (endDate == null || curDate <= endDate) && user.assocs["pjt:reassignTo"][0] != null) {
		var reassignResource = getDelegate(user.assocs["pjt:reassignTo"][0]);
		if (reassignResource != null) {
			return reassignResource;
		} else {
			return user.assocs["pjt:reassignTo"][0].properties["userName"];
		}
	}
	return null;
}

function getAssigneeOrDelegate(user) {
	var delegate = getDelegate(user);
	if (delegate != null) {
		return delegate;
	} else {
		return user.properties["userName"];
	}
}

function getAssignees(authorities) {
	var assignees = new java.util.ArrayList();
	if (authorities != null) {
		for (var i = 0; i < authorities.size(); i++) {
			if (authorities.get(i).isSubType("cm:authorityContainer")) {
				var members = people.getMembers(authorities.get(i));
				for ( var j in members) {
					assignees.add(getAssigneeOrDelegate(members[j]));
				}
			} else if (authorities.get(i).isSubType("cm:person")) {
				assignees.add(getAssigneeOrDelegate(authorities.get(i)));
			}
		}
	}
	return assignees;
}

function onCreateProductValidationTask(authorities) {
	if (typeof bpm_workflowDueDate != 'undefined')
		task.dueDate = bpm_workflowDueDate;
	if (typeof bpm_workflowPriority != 'undefined')
		task.priority = bpm_workflowPriority;
		

	var assignees = getAssignees(authorities)

	if (assignees != null && assignees.size() > 0) {
		if (assignees.size() == 1) {
			task.setAssignee(assignees.get(0));
		} else {
			for (var i = 0; i < assignees.size(); i++) {
				task.addCandidateUser(assignees.get(i));

				if (bcpgwf_notifyAssignee) {
					sendMail(
							assignees.get(i),
							initiator,
							bcpg.getMessage('productValidationWF.mail.notify.subject', bpm_workflowDescription),
							bcpg.getMessage('productValidationWF.mail.notify.message'),
							"/app:company_home/app:dictionary/app:email_templates/cm:workflownotification/cm:product-validation-notify-task-email.ftl",
							bpm_package.children);
				}
			}
		}
	}
}

function onAssignmentProductValidationTask() {
	if (bcpgwf_notifyAssignee) {
		sendMail(task.assignee, initiator, bcpg.getMessage('productValidationWF.mail.notify.subject', bpm_workflowDescription), bcpg
				.getMessage('productValidationWF.mail.notify.message'),
				"/app:company_home/app:dictionary/app:email_templates/cm:workflownotification/cm:product-validation-notify-task-email.ftl",
				bpm_package.children);
	}
}

function startProductValidationWF() {

	execution.setVariable('bpm_comment', bcpgwf_pvTransmitterComment);
	execution.setVariable('bcpgwf_reviewRDApproval', null);
	execution.setVariable('bcpgwf_reviewPackagingApproval', null);
	execution.setVariable('bcpgwf_reviewProductionApproval', null);
	execution.setVariable('bcpgwf_reviewQualityApproval', null);
	execution.setVariable('bcpgwf_reviewCallerApproval', null);
	execution.setVariable('bcpgwf_reviewCaller2Approval', null);
	if (execution.getVariable('bcpgwf_pvRDApprovalActor') == null) {
		execution.setVariable('bcpgwf_pvRDApprovalActor', null);
	}
	if (execution.getVariable('bcpgwf_pvPackagingApprovalActor') == null) {
		execution.setVariable('bcpgwf_pvPackagingApprovalActor', null);
	}
	if (execution.getVariable('bcpgwf_pvProductionApprovalActor') == null) {
		execution.setVariable('bcpgwf_pvProductionApprovalActor', null);
	}
	if (execution.getVariable('bcpgwf_pvQualityApprovalActor') == null) {
		execution.setVariable('bcpgwf_pvQualityApprovalActor', null);
	}
	if (execution.getVariable('bcpgwf_pvCallerActor') == null) {
		execution.setVariable('bcpgwf_pvCallerActor', null);
	}
	if (execution.getVariable('bcpgwf_pvCaller2Actor') == null) {
		execution.setVariable('bcpgwf_pvCaller2Actor', null);
	}
	if (execution.getVariable('bcpgwf_notifyUsers') == null) {
		execution.setVariable('bcpgwf_notifyUsers', null);
	}

	execution.setVariable('bcpgwf_pvRDApprovalActorAssignee', null);
	execution.setVariable('bcpgwf_pvPackagingApprovalActorAssignee', null);
	execution.setVariable('bcpgwf_pvProductionApprovalActorAssignee', null);
	execution.setVariable('bcpgwf_pvQualityApprovalActorAssignee', null);
	execution.setVariable('bcpgwf_pvCallerActorAssignee', null);
	execution.setVariable('bcpgwf_pvCaller2ActorAssignee', null);

	for (var i = 0; i < bpm_package.children.length; i++) {
		var product = bpm_package.children[i];

		if (product.isSubType("bcpg:product") && product.properties["bcpg:productState"] != "ToValidate" ) {
			product.properties["bcpg:productState"] = "ToValidate";
			product.save();
		}
		
	    var desc =  execution.getVariable('bpm_workflowDescription')
		
		if (  isEmpty(desc) ||  isBlank(desc)) {
	
			 bpm_workflowDescription = extractName(product);
		     bpm_description = bpm_workflowDescription;
		     execution.setVariable('bpm_workflowDescription', bpm_workflowDescription);
		     execution.setVariable('bpm_description', bpm_workflowDescription);

		}
	}

}

function isEmpty(str) {
    return (!str || 0 === str.length);
}

function isBlank(str) {
    return (!str || /^\s*$/.test(str));
}

function extractName(product){
	var ret = bcpg.getMessage('productValidationWF.workflow.title');

//	if(product.properties["bcpg:erpCode"]!=null){
//		ret+=" - " + product.properties["bcpg:erpCode"];
//	} else if(product.properties["bcpg:code"]!=null){
//		ret+=" - " + product.properties["bcpg:code"];
//	}
	
	ret+= " - " + product.name;
	return ret;
}

function onCreateApproveProductTask() {

	if (typeof bpm_workflowDueDate != 'undefined')
		task.dueDate = bpm_workflowDueDate;
	if (typeof bpm_workflowPriority != 'undefined')
		task.priority = bpm_workflowPriority;
	task.setAssignee(getAssigneeOrDelegate(initiator));

	if (bcpgwf_notifyUsers != null) {
		for (var i = 0; i < bcpgwf_notifyUsers.size(); i++) {
			sendMail(bcpgwf_notifyUsers.get(i), null, bcpg.getMessage('productValidationWF.mail.approved.subject', bpm_workflowDescription), bcpg
					.getMessage('productValidationWF.mail.approved.message'),
					"/app:company_home/app:dictionary/app:email_templates/cm:workflownotification/cm:product-validation-approved-task-email.ftl",
					bpm_package.children);
		}
	}

	for (var i = 0; i < bpm_package.children.length; i++) {
		var product = bpm_package.children[i];

		var assocNames = [ "bcpgwf:pvCallerActor", "bcpgwf:pvCaller2Actor", "bcpgwf:pvRDApprovalActor", "bcpgwf:pvPackagingApprovalActor",
				"bcpgwf:pvQualityApprovalActor", "bcpgwf:pvProductionApprovalActor", "bcpgwf:pvTransmitterActor" ];
		var assocActors = [ bcpgwf_pvCallerActorAssignee, bcpgwf_pvCaller2ActorAssignee, bcpgwf_pvRDApprovalActorAssignee,
				bcpgwf_pvPackagingApprovalActorAssignee, bcpgwf_pvQualityApprovalActorAssignee, bcpgwf_pvProductionApprovalActorAssignee, initiator ];
		for (var j = 0; j < assocNames.length; j++) {
			var assocName = assocNames[j];
			var assocActor = assocActors[j];

			/* remove assocs */
			var targetNodes = product.assocs[assocName];
			if (targetNodes != null) {
				for (var k = 0; k < targetNodes.length; k++) {
					product.removeAssociation(targetNodes[k], assocName);
				}
			}

			/* add assocs */
			if (assocActor != null) {
				if (assocActor.typeShort == null) {
					assocActor = people.getPerson(assocActor);
				}
				product.createAssociation(assocActor, assocName);
			}
		}
		product.properties["bcpgwf:pvValidationDate"] = new Date();
		product.save();

	}
}
