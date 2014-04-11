function startEvent() {
	//execution.setVariable('bpm_workflowPriority',2);
	execution.setVariable('ncwf_claimRejectedState', 'none');
	execution.setVariable('ncwf_ncType', "Claim");
	if (execution.getVariable('bcpgwf_notifyUsers') == null) {
		execution.setVariable('bcpgwf_notifyUsers', null);
	}
	execution.setVariable('ncwf_ncState', "analysis");
	if (execution.getVariable('ncwf_claimStartAsDraft') == null) {
		execution.setVariable('ncwf_claimStartAsDraft', false);
	}
	execution.setVariable('skipClassification', false);
}

function onCreateEnteringClaimTask() {
	task.setVariable('ncwf_claimRejectedCause', execution.getVariable('ncwf_claimRejectedCause'));
	for (var i = 0; i < bpm_package.children.length; i++) {
		var nc = bpm_package.children[i];
		if (nc.isSubType("qa:nc")) {
			var bpm_workflowDescription = extractName(nc);
			task.setVariable('bpm_workflowDescription', bpm_workflowDescription);
		}
	}
}

function onCompleteEnteringClaimTask() {
	execution.setVariable('ncwf_claimRejectedState', 'none');
	execution.setVariable('ncwf_claimRejectedCause', '');

	execution.setVariable('qa_claimSource', task.getVariable('qa_claimSource'));
	execution.setVariable('qa_claimTradeContact', task.getVariable('qa_claimTradeContact'));
	execution.setVariable('bcpg_clients', task.getVariable('bcpg_clients'));
	execution.setVariable('qa_claimDistributionNetwork', task.getVariable('qa_claimDistributionNetwork'));
	execution.setVariable('qa_claimCustomerInformation', task.getVariable('qa_claimCustomerInformation'));
	execution.setVariable('qa_claimTracking', task.getVariable('qa_claimTracking'));

	execution.setVariable('qa_product', task.getVariable('qa_product'));
	execution.setVariable('qa_batchId', task.getVariable('qa_batchId'));
	execution.setVariable('qa_productUseByDate', task.getVariable('qa_productUseByDate'));
	execution.setVariable('qa_claimDescription', task.getVariable('qa_claimDescription'));
	execution.setVariable('qa_ncComment', task.getVariable('qa_ncComment'));
	execution.setVariable('bcpgwf_notifyUsers', task.getVariable('bcpgwf_notifyUsers'));
	execution.setVariable('skipClassification', false);

	task.setVariable('ncwf_ncState', "analysis");

}

function onCreateAnalysisTask() {
	var bpm_workflowDescription = null;

	for (var i = 0; i < bpm_package.children.length; i++) {
		var nc = bpm_package.children[i];
		if (nc.isSubType("qa:nc")) {
			bpm_workflowDescription = extractName(nc);
			task.setVariable('bpm_workflowDescription', bpm_workflowDescription);
		}
	}

	var tDate = new java.util.Date();
	tDate.setDate(tDate.getDate() + 5);
	var rDate = new java.util.Date();
	rDate.setDate(rDate.getDate() + 9);

	task.setVariable('ncwf_claimTreatmentDueDate', tDate);
	task.setVariable('ncwf_claimResponseDueDate', rDate);

	task.setVariable('ncwf_claimRejectedCause', execution.getVariable('ncwf_claimRejectedCause'));
	task.setVariable('ncwf_claimRejectedState', 'none');

	if (bcpgwf_notifyUsers != null) {
		for (var i = 0; i < bcpgwf_notifyUsers.size(); i++) {
			sendMail(bcpgwf_notifyUsers.get(i), initiator, bcpg.getMessage('claimProcess.mail.notify.analysis.subject', bpm_workflowDescription),
					bcpg.getMessage('claimProcess.mail.notify.analysis.message'));
		}
	}

}

function onCompleteAnalysisTask() {
	execution.setVariable('ncwf_claimTreatmentDueDate', task.getVariable('ncwf_claimTreatmentDueDate'));
	execution.setVariable('ncwf_claimResponseDueDate', task.getVariable('ncwf_claimResponseDueDate'));

	task.setVariable('ncwf_ncState', 'classification');

	if (task.getVariable('qa_claimResponseActor') != null) {
		execution.setVariable('qa_claimResponseActor', task.getVariable('qa_claimResponseActor'));
		task.setVariable('ncwf_ncState', 'response');
	} else {
		execution.setVariable('qa_claimResponseActor', null);
	}

	if (task.getVariable('qa_claimTreatmentActor') != null) {
		execution.setVariable('qa_claimTreatmentActor', task.getVariable('qa_claimTreatmentActor'));
		task.setVariable('ncwf_ncState', 'treatment');
	} else {
		execution.setVariable('qa_claimTreatmentActor', null);
	}

	if (task.getVariable('ncwf_claimRejectedState') != 'none') {
		task.setVariable('ncwf_ncState', "new");
		execution.setVariable('ncwf_claimRejectedCause', task.getVariable('ncwf_claimRejectedCause'));
	} else {
		execution.setVariable('ncwf_claimRejectedCause', '');

		if (bcpgwf_notifyUsers != null) {
			for (var i = 0; i < bcpgwf_notifyUsers.size(); i++) {
				sendMail(bcpgwf_notifyUsers.get(i), initiator, bcpg.getMessage('claimProcess.mail.notify.analysis.end.subject', execution
						.getVariable('bpm_workflowDescription')), bcpg.getMessage('claimProcess.mail.notify.analysis.end.message'));
			}
		}
		sendMail(initiator, initiator, bcpg.getMessage('claimProcess.mail.notify.analysis.end.subject', execution
				.getVariable('bpm_workflowDescription')), bcpg.getMessage('claimProcess.mail.notify.analysis.end.message'));
	}

	execution.setVariable('bcpg_plants', task.getVariable('bcpg_plants'));
	execution.setVariable('bcpgwf_notifyAssignee', task.getVariable('bcpgwf_notifyAssignee'));
	execution.setVariable('qa_claimAnalysisComment', task.getVariable('qa_claimAnalysisComment'));
	execution.setVariable('ncwf_claimRejectedState', task.getVariable('ncwf_claimRejectedState'));
}

function onCreateClaimClosingTask() {
	if (bcpgwf_notifyUsers != null) {
		for (var i = 0; i < bcpgwf_notifyUsers.size(); i++) {
			sendMail(initiator, initiator, bcpg.getMessage('claimProcess.mail.notify.closing.subject', execution
					.getVariable('bpm_workflowDescription')), bcpg.getMessage('claimProcess.mail.notify.closing.message'));
		}
	}
	sendMail(initiator, initiator, bcpg.getMessage('claimProcess.mail.notify.closing.subject', execution.getVariable('bpm_workflowDescription')),
			bcpg.getMessage('claimProcess.mail.notify.closing.message'));

	task.setVariable('ncwf_claimTreatmentDueDate', execution.getVariable('ncwf_claimTreatmentDueDate'));
	task.setVariable('ncwf_claimResponseDueDate', execution.getVariable('ncwf_claimResponseDueDate'));

	task.setVariable('qa_claimResponseState', execution.getVariable('qa_claimResponseState'));
	task.setVariable('qa_claimResponseDetails', execution.getVariable('qa_claimResponseDetails'));
	task.setVariable('qa_claimTreatementDetails', execution.getVariable('qa_claimTreatementDetails'));
	task.setVariable('qa_claimTreatementPrevActions', execution.getVariable('qa_claimTreatementPrevActions'));
	task.setVariable('qa_claimType', execution.getVariable('qa_claimType'));

	task.setVariable('ncwf_claimRejectedCause', execution.getVariable('ncwf_claimRejectedCause'));
	task.setVariable('ncwf_claimRejectedState', 'none');
}

function onCompleteClaimClosingTask() {
	if (task.getVariable('ncwf_claimRejectedState') == 'none') {
		task.setVariable('qa_claimClosingDate', new java.util.Date());
		task.setVariable('ncwf_ncState', 'closed');
		execution.setVariable('ncwf_claimRejectedCause', '');

		if (bcpgwf_notifyUsers != null) {
			for (var i = 0; i < bcpgwf_notifyUsers.size(); i++) {
				sendMail(bcpgwf_notifyUsers.get(i), initiator, bcpg.getMessage('claimProcess.mail.notify.closing.end.subject', execution
						.getVariable('bpm_workflowDescription')), bcpg.getMessage('claimProcess.mail.notify.closing.end.message'));
			}
		}
		sendMail(initiator, initiator, bcpg.getMessage('claimProcess.mail.notify.closing.end.subject', execution
				.getVariable('bpm_workflowDescription')), bcpg.getMessage('claimProcess.mail.notify.closing.end.message'));

	} else {
		task.setVariable('ncwf_ncState', task.getVariable('ncwf_claimRejectedState'));
		execution.setVariable('ncwf_claimRejectedCause', task.getVariable('ncwf_claimRejectedCause'));
	}

	execution.setVariable('qa_claimClosingComment', task.getVariable('qa_claimClosingComment'));
	execution.setVariable('bcpgwf_notifyUsers', task.getVariable('bcpgwf_notifyUsers'));
	execution.setVariable('ncwf_claimRejectedState', task.getVariable('ncwf_claimRejectedState'));
}

function onCreateClassificationTask() {
	task.setVariable('ncwf_claimTreatmentDueDate', execution.getVariable('ncwf_claimTreatmentDueDate'));
	task.setVariable('ncwf_claimResponseDueDate', execution.getVariable('ncwf_claimResponseDueDate'));
	task.setVariable('ncwf_ncState', 'classification');
}

function onCompleteClassificationTask() {
	execution.setVariable('qa_claimType', task.getVariable('qa_claimType'));
	execution.setVariable('qa_claimOriginHierarchy1', task.getVariable('qa_claimOriginHierarchy1'));
	execution.setVariable('qa_claimOriginHierarchy2', task.getVariable('qa_claimOriginHierarchy2'));

	execution.setVariable('qa_claimClassificationComment', task.getVariable('qa_claimClassificationComment'));
	execution.setVariable('bcpgwf_notifyUsers', task.getVariable('bcpgwf_notifyUsers'));
	if (execution.getVariable('ncwf_claimRejectedState') == 'classification') {
		execution.setVariable('ncwf_claimRejectedState', 'none');
	}
	execution.setVariable('skipClassification', true);
}

function onCreateClaimTreatmentTask() {
	if (typeof ncwf_claimTreatmentDueDate != 'undefined')
		task.dueDate = ncwf_claimTreatmentDueDate;
	task.setVariable('ncwf_claimTreatmentDueDate', execution.getVariable('ncwf_claimTreatmentDueDate'));
	task.setVariable('ncwf_claimResponseDueDate', execution.getVariable('ncwf_claimResponseDueDate'));

	task.setVariable('ncwf_claimRejectedCause', execution.getVariable('ncwf_claimRejectedCause'));
	task.setVariable('ncwf_claimRejectedState', 'none');

	if (bcpgwf_notifyAssignee) {
		sendMail(qa_claimTreatmentActor, initiator, bcpg.getMessage('claimProcess.mail.action.treatment.subject', execution
				.getVariable('bpm_workflowDescription')), bcpg.getMessage('claimProcess.mail.action.treatment.message'), true);
	}
}

function onCompleteClaimTreatmentTask() {
	task.setVariable('ncwf_ncState', task.getVariable('ncwf_claimRejectedState'));

	if (task.getVariable('ncwf_claimRejectedState') == 'none') {
		task.setVariable('qa_claimTreatementDate', new java.util.Date());
		if (execution.getVariable('qa_claimResponseActor') != null) {
			task.setVariable('ncwf_ncState', 'response');
		} else {
			task.setVariable('ncwf_ncState', 'closing');
		}
		execution.setVariable('ncwf_claimRejectedCause', '');

		if (bcpgwf_notifyUsers != null) {
			for (var i = 0; i < bcpgwf_notifyUsers.size(); i++) {
				sendMail(bcpgwf_notifyUsers.get(i), initiator, bcpg.getMessage('claimProcess.mail.notify.treatment.end.subject', execution
						.getVariable('bpm_workflowDescription')), bcpg.getMessage('claimProcess.mail.notify.treatment.end.message'));
			}
		}
		sendMail(initiator, initiator, bcpg.getMessage('claimProcess.mail.notify.treatment.end.subject', execution
				.getVariable('bpm_workflowDescription')), bcpg.getMessage('claimProcess.mail.notify.treatment.end.message'));

	} else {
		execution.setVariable('ncwf_claimRejectedCause', task.getVariable('ncwf_claimRejectedCause'));
	}

	execution.setVariable('ncwf_claimRejectedState', task.getVariable('ncwf_claimRejectedState'));
	execution.setVariable('qa_claimTreatementDetails', task.getVariable('qa_claimTreatementDetails'));
	execution.setVariable('qa_claimTreatementPrevActions', task.getVariable('qa_claimTreatementPrevActions'));
	execution.setVariable('qa_claimTreatementComment', task.getVariable('qa_claimTreatementComment'));
	execution.setVariable('bcpgwf_notifyUsers', task.getVariable('bcpgwf_notifyUsers'));
}

function onCreateClaimResponseTask() {
	if (typeof ncwf_claimResponseDueDate != 'undefined')
		task.dueDate = ncwf_claimResponseDueDate;
	task.setVariable('ncwf_claimTreatmentDueDate', execution.getVariable('ncwf_claimTreatmentDueDate'));
	task.setVariable('ncwf_claimResponseDueDate', execution.getVariable('ncwf_claimResponseDueDate'));

	task.setVariable('ncwf_claimRejectedCause', execution.getVariable('ncwf_claimRejectedCause'));
	task.setVariable('ncwf_claimRejectedState', 'none');

	task.setVariable('qa_claimTreatementDetails', execution.getVariable('qa_claimTreatementDetails'));
	sendMail(qa_claimResponseActor, initiator, bcpg.getMessage('claimProcess.mail.action.response.subject', execution
			.getVariable('bpm_workflowDescription')), bcpg.getMessage('claimProcess.mail.action.response.message'), true);
}

function onCompleteClaimResponseTask() {
	if (task.getVariable('ncwf_claimRejectedState') == 'none') {
		task.setVariable('qa_claimResponseDate', new java.util.Date());
		task.setVariable('ncwf_ncState', 'closing');
		execution.setVariable('ncwf_claimRejectedCause', '');

		if (bcpgwf_notifyUsers != null) {
			for (var i = 0; i < bcpgwf_notifyUsers.size(); i++) {
				sendMail(bcpgwf_notifyUsers.get(i), initiator, bcpg.getMessage('claimProcess.mail.notify.response.end.subject', execution
						.getVariable('bpm_workflowDescription')), bcpg.getMessage('claimProcess.mail.notify.response.end.message'));
			}
		}
		sendMail(initiator, initiator, bcpg.getMessage('claimProcess.mail.notify.response.end.subject', execution
				.getVariable('bpm_workflowDescription')), bcpg.getMessage('claimProcess.mail.notify.response.end.message'));

	} else {
		task.setVariable('ncwf_ncState', task.getVariable('ncwf_claimRejectedState'));
		execution.setVariable('ncwf_claimRejectedCause', task.getVariable('ncwf_claimRejectedCause'));
	}

	execution.setVariable('bcpgwf_notifyAssignee', task.getVariable('bcpgwf_notifyAssignee'));
	execution.setVariable('qa_claimResponseComment', task.getVariable('qa_claimResponseComment'));

	execution.setVariable('ncwf_claimRejectedState', task.getVariable('ncwf_claimRejectedState'));
	execution.setVariable('qa_claimResponseState', task.getVariable('qa_claimResponseState'));
	execution.setVariable('qa_claimResponseDetails', task.getVariable('qa_claimResponseDetails'));
}

// #################### Utils ######################

function extractName(nc) {

	var tName = nc.properties["cm:name"];

	if (execution.getVariable('bcpg_clients') != null && execution.getVariable('bcpg_clients').size() > 0) {
		var client = execution.getVariable('bcpg_clients').get(0);

		tName += " - " + execution.getVariable('bcpg_clients').get(0).properties["cm:name"];
	}

	if (execution.getVariable('qa_product') != null) {
		var product = execution.getVariable('qa_product');

		tName += " - " + product.properties["cm:name"];
		if (product.properties["bcpg:erpCode"] != null && product.properties["bcpg:erpCode"].length > 0) {
			tName += " - " + product.properties["bcpg:erpCode"];
		}
	}

	return tName;
}

function sendMail(userOrGroup, from, subject, message, isAction) {
	if (userOrGroup != null && userOrGroup.exists()) {
		try {
			var mail = actions.create("mail");
			mail.parameters.template_model = templateModel;
			if (userOrGroup.typeShort == "cm:authorityContainer") {
				mail.parameters.to_many = new Array(userOrGroup.properties["cm:authorityName"]);
			} else {
				mail.parameters.to_many = new Array(userOrGroup.properties["cm:userName"]);
			}
			mail.parameters.subject = subject;
			mail.parameters.from = from.properties.email;
			mail.parameters.ignore_send_failure = true;

			// for Local
			// person.properties["{http://www.alfresco.org/model/system/1.0}locale"]

			mail.parameters.template = search.xpathSearch("/app:company_home/app:dictionary/app:email_templates/cm:workflownotification/cm:claim-"
					+ (isAction ? "action" : "notify") + "-task-email.ftl")[0];
			var templateArgs = new Array();
			templateArgs['workflowTitle'] = message;
			templateArgs['workflowPooled'] = false;
			templateArgs['workflowDescription'] = execution.getVariable('bpm_workflowDescription');
			templateArgs['workflowDueDate'] = task.dueDate;
			templateArgs['workflowPriority'] = task.priority;
			// templateArgs['workflowDocuments'] = [];
			templateArgs['workflowId'] = "activiti$" + task.id;

			var templateModel = new Array();
			templateModel['args'] = templateArgs;
			mail.parameters.template_model = templateModel;

			mail.execute(bpm_package);
		} catch (e) {
			logger.error("Cannot send mail :");
			// logger.error(" - user: "+user);
			logger.error(" - subject: " + subject);
			logger.error(" - e: " + e);
		}
	}
}
