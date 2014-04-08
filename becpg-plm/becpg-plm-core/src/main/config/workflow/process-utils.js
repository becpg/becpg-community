
function sendMail(authority, from, subject, message, templatePath, workflowDocuments) {
	try {
		if(authority.exists){
			var mail = actions.create("mail");
			mail.parameters.template_model = templateModel;
			if(authority.typeShort == "cm:authorityContainer"){
				mail.parameters.to_many = new Array(authority.properties["cm:authorityName"]);
			}
			else{
				mail.parameters.to_many = new Array(authority.properties["cm:userName"]);
			}
			mail.parameters.subject = subject;
			if(from != null){
				mail.parameters.from = from.properties.email;
			}		
			mail.parameters.ignore_send_failure = true;

			mail.parameters.template = search.xpathSearch(templatePath)[0];
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
		} catch (e) {
			logger.error("Cannot send mail to :");	
			logger.error(" - subject: " + subject);
			logger.error(" - e: " + e);
		}
		}		
}