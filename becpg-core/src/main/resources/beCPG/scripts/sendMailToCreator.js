var processName = document.parent.parent.name;
var folderName = document.parent.name;

// create mail action
var mail = actions.create("mail");
mail.parameters.to = people.getPerson(document.properties["cm:creator"]).properties["cm:email"];
mail.parameters.subject = processName + " : " + document.name + " (" + document.properties["cm:title"] + ")";
mail.parameters.from = "support@becpg.fr";
//mail.parameters.template = companyhome.childrenByXPath("app:dictionary/app:email_templates/app:notify_email_templates/cm:notify_user_email.ftl")[0];
mail.parameters.text = "La demande d'investissement se trouve dans le dossier " + document.parent.name;
mail.parameters.ignore_send_failure = true;

var templateModel = new Array();
templateModel['person'] = person;
mail.parameters.template_model = templateModel

// execute action against a document    
mail.execute(document);