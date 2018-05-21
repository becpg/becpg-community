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
function getMemberNames(assignees){
	
	var memberNames = null;
	if(assignees != null){
		var memberNames = new java.util.ArrayList();
		
		for (var i=0; i<assignees.size(); i++) {
			if(assignees.get(i).isSubType("cm:authorityContainer")){
				var members = people.getMembers(assignees.get(i));
				for(var j in members) 
				{
				  memberNames.add(getAssigneeOrDelegate(members[j]));
				}
			}
			else if(assignees.get(i).isSubType("cm:person")){
				memberNames.add(getAssigneeOrDelegate(assignees.get(i)));
			}		
		}
	}
	return memberNames;
}

function sendMailToAssignees(assignees, from, subject, message, templatePath, workflowDocuments){
	for (var i = 0; i < assignees.size(); i++){
		sendMail(assignees.get(i), from, subject, message, templatePath, workflowDocuments)
	}
}