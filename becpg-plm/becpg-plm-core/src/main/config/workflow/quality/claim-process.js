function extractName(nc) {

   var tName = nc.properties["cm:name"];

   if (execution.getVariable('bcpg_clients') != null && execution.getVariable('bcpg_clients').size() > 0) {
      var client = execution.getVariable('bcpg_clients').get(0);

      tName += " - " + execution.getVariable('bcpg_clients').get(0).properties["cm:name"];
   }

   if (execution.getVariable('qa_product') != null ) {
      var product = execution.getVariable('qa_product');

      tName += " - " + product.properties["cm:name"];
      if(product.properties["bcpg:erpCode"]!=null && product.properties["bcpg:erpCode"].length >0){
         tName += " - " + product.properties["bcpg:erpCode"];
      }
   }

   return tName;
}

function sendMail(user, from, subject, message, isAction) {
   var mail = actions.create("mail");
   
   
   mail.parameters.template_model = templateModel;
   mail.parameters.to = user.properties.email;
   mail.parameters.subject = subject;
   mail.parameters.from = from.properties.email;
   mail.parameters.ignore_send_failure = true;

   // for Local person.properties["{http://www.alfresco.org/model/system/1.0}locale"]

   mail.parameters.template = search
         .xpathSearch("/app:company_home/app:dictionary/app:email_templates/cm:workflownotification/cm:claim-" + (isAction ? "action"
               : "notify") + "-task-email.ftl")[0];
   var templateArgs = new Array();
   templateArgs['workflowTitle'] = message;
   templateArgs['workflowPooled'] = false;
   templateArgs['workflowDescription'] = bpm_workflowDescription ;
   templateArgs['workflowDueDate'] = task.dueDate;
   templateArgs['workflowPriority'] = task.priority;
   //templateArgs['workflowDocuments'] = [];
   templateArgs['workflowId'] = "activiti$"+task.id;

   var templateModel = new Array();
   templateModel['args'] = templateArgs;
   mail.parameters.template_model=templateModel;

   mail.execute(bpm_package);
}
