
function getI18N(step,itemName){
    var label = step.attributes[itemName];
    if (label == null)
    {
        var labelId = step.attributes[itemName+"Id"];
        if (labelId != null)
        {
           label = msg.get(labelId);
            
            if(label == labelId){
                label = "";
            }
            
        }
    }
    return label;
}

function main() {

    var wizardStruct = [];
    
    model.comments = false;
    
    var wizards = config.scoped["wizard"]["wizards"].childrenMap["wizard"];
    for (var i = 0, wizard; i < wizards.size(); i++) {
        wizard = wizards.get(i);
        if(wizard.attributes["id"] == page.url.args.id){
        	model.comments =  wizard.attributes["comments"] == "true";
            var steps = wizard.childrenMap["step"];
            for(var j = 0, step; j < steps.size(); j++){
                step = steps.get(j);
                wizardStruct.push({
                        id : step.attributes["id"],
                        label : getI18N(step,"label"),
                        type : step.attributes["type"],
                        formId : step.attributes["formId"],
                        itemId : step.attributes["itemId"],
                        listId : step.attributes["listId"],
                        title : getI18N(step,"title"),
                        nodeRefStepIndex : step.attributes["nodeRefStepIndex"],
                        nextStepWebScript:  step.attributes["nextStepWebScript"]   
                });
            }
            break;
        }
      
    }
    
    // Widget instantiation metadata...
   var widget = {
      id : "WizardMgr", 
      name : "beCPG.component.WizardMgr",
      options : {
         siteId : (page.url.templateArgs.site != null) ? page.url.templateArgs.site : "",
         destination :  (page.url.args.destination != null) ? page.url.args.destination : "",
         nodeRef :  (page.url.args.nodeRef != null) ? page.url.args.nodeRef : "",
         wizardStruct : wizardStruct
      }
   };
   model.widgets = [widget];
   
   if(model.comments){
	   var commentList = {
			      id : "CommentsList",
			      name : "Alfresco.CommentsList",
			      options : {
			    	 nodeRef :  (page.url.args.nodeRef != null) ? page.url.args.nodeRef : "",
			         siteId : (page.url.templateArgs.site != null) ? page.url.templateArgs.site : "",
			         maxItems : 10,
			         editorConfig : {
			            menu: {},
			            toolbar: "bold italic underline | bullist numlist | forecolor backcolor | undo redo removeformat",
			            language: locale,
			            statusbar: false
			         }
			      }
			   };
	   model.widgets.push(commentList);
   } 
   
}
main();