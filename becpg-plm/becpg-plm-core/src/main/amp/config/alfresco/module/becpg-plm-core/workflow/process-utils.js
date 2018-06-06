function sendMail(userOrGroup, from, subject, message, templatePath, workflowDocuments)
{
    try
    {
        if (userOrGroup != null)
        {
            var mail = actions.create("mail");
            mail.parameters.template_model = templateModel;
            
            if (userOrGroup.typeShort && userOrGroup.typeShort == "cm:authorityContainer") {
            	mail.parameters.to_many = new Array(userOrGroup.properties["cm:authorityName"]);
            } else if(userOrGroup.typeShort && userOrGroup.typeShort == "cm:person") {
               mail.parameters.to_many = new Array(userOrGroup.properties["cm:userName"]);
            }else {
               mail.parameters.to_many = new Array(userOrGroup);
            }
           
            mail.parameters.subject = subject;
            if (from != null)
            {
                mail.parameters.from = from.properties.email;
            }
            mail.parameters.ignore_send_failure = true;

            var template = search.xpathSearch(templatePath)[0];
            if (template)
            {
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

            }
            else
            {
                logger
                        .error("No template found for email : " + templatePath);
            }
        }
    }
    catch (e)
    {
        logger.error("Cannot send mail to :");
        logger.error(" - subject: " + subject);
        logger.error(" - e: " + e);
    }
}

function getDelegate(user){
	var curDate = new Date(), 
	startDate = (user.properties["pjt:delegationStartDate"] != null ? new Date(user.properties["pjt:delegationStartDate"].getTime()) : null),
	endDate = (user.properties["pjt:delegationEndDate"] != null ? new Date(user.properties["pjt:delegationEndDate"].getTime()) : null);

	if(user != null && user.properties["pjt:delegationActivated"] &&
			(startDate == null || startDate <= curDate) &&
			(endDate == null || curDate <= endDate) &&
			user.assocs["pjt:reassignTo"][0] != null){
			var reassignResource = getDelegate(user.assocs["pjt:reassignTo"][0]);
			if(reassignResource != null){
				return reassignResource;
			}
			else{
				return user.assocs["pjt:reassignTo"][0].properties["userName"];
			}			
	}
	return null;
}

function getAssigneeOrDelegate(user){
	var delegate = getDelegate(user); 
	if(delegate != null){
		return delegate;
	}
	else{
		return user.properties["userName"];
	}
}

function getAssignees(authorities){
	var assignees = new java.util.ArrayList();
	if(authorities != null){		
		for (var i=0; i<authorities.size(); i++) {
			if(authorities.get(i).isSubType("cm:authorityContainer")){
				var members = people.getMembers(authorities.get(i));
				for(var j in members) 
				{
				  assignees.add(getAssigneeOrDelegate(members[j]));
				}
			}
			else if(authorities.get(i).isSubType("cm:person")){
				assignees.add(getAssigneeOrDelegate(authorities.get(i)));
			}		
		}
	}
	return assignees;
}

function onCreateProductValidationTask(authorities){
	if (typeof bpm_workflowDueDate != 'undefined') task.dueDate = bpm_workflowDueDate;
   if (typeof bpm_workflowPriority != 'undefined') task.priority = bpm_workflowPriority;
   
   var assignees = getAssignees(authorities)
   
   if (assignees != null && assignees.size()>0){
   	if(assignees.size()==1){
   		task.setAssignee(assignees.get(0));
   	}
   	else{
   		for (var i = 0; i < assignees.size(); i++){
   			task.addCandidateUser(assignees.get(i));
   			
   			if(bcpgwf_notifyAssignee){
      			sendMail(assignees.get(i), initiator,
    						bcpg.getMessage('productValidationWF.mail.notify.subject', bpm_workflowDescription),
    						bcpg.getMessage('productValidationWF.mail.notify.message'), 
    						"/app:company_home/app:dictionary/app:email_templates/cm:workflownotification/cm:product-validation-notify-task-email.ftl",
    						bpm_package.children);
      		}
         }
   	}
   }
}

function onAssignmentProductValidationTask(){
	if (bcpgwf_notifyAssignee){
		sendMail(task.assignee, initiator,
			bcpg.getMessage('productValidationWF.mail.notify.subject', bpm_workflowDescription),
			bcpg.getMessage('productValidationWF.mail.notify.message'), 
			"/app:company_home/app:dictionary/app:email_templates/cm:workflownotification/cm:product-validation-notify-task-email.ftl",
			bpm_package.children);
	}
}
