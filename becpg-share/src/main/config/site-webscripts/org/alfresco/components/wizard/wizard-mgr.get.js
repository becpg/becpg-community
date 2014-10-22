function main() {

    var wizardStruct = [];
    
    var wizards = config.scoped["wizard"]["wizards"].childrenMap["wizard"];
    for (var i = 0, wizard; i < wizards.size(); i++) {
        wizard = wizards.get(i);
        if(wizard.attributes["id"] == page.url.args.id){
            var steps = wizard.childrenMap["step"]
            for(var j = 0, step; j < steps.size(); j++){
                step = steps.get(j);
                wizardStruct.push({
                        id : step.attributes["id"],
                        label : step.attributes["label"],
                        type : step.attributes["type"],
                        formId : step.attributes["formId"],
                        itemId : step.attributes["itemId"],
                        listId : step.attributes["listId"],
                        title :  step.attributes["title"],
                        nodeRefStepIndex : step.attributes["nodeRefStepIndex"],
                        nextStepWebScript:  step.attributes["nextStepWebScript"]   
                });
            }
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
}
main();