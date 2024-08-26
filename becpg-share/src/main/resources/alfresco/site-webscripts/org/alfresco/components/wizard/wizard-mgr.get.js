<import resource="classpath:/alfresco/site-webscripts/org/alfresco/components/documentlibrary/include/toolbar.lib.js">
<import resource="classpath:/alfresco/site-webscripts/org/alfresco/components/upload/uploadable.lib.js">
<import resource="classpath:/alfresco/site-webscripts/org/alfresco/components/documentlibrary/include/documentlist.lib.js">

doclibCommon();



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
    model.draft = false;
    
    var wizards = config.scoped["wizard"]["wizards"].childrenMap["wizard"];
    for (var i = 0, wizard; i < wizards.size(); i++) {
        wizard = wizards.get(i);
        if(wizard.attributes["id"] == page.url.args.id){
        	model.comments =  wizard.attributes["comments"] == "true";
        	model.draft = wizard.attributes["draft"] == "true";
        	model.allSteps = wizard.attributes["allSteps"] == "true";
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
         draft : model.draft, 
         allSteps : model.allSteps,
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
   
   if(page.url.args.catalogId != null && page.url.args.nodeRef != null){
	      model.catalogId = page.url.args.catalogId;
	      var entityCatalog = {
	 	         id : "EntityCatalog", 
	 	         name : "beCPG.component.EntityCatalog",
	 	         initArgs :  ["\"" + args.htmlid+"-step-step1_cat\""],
	 	         options : {
	 	            entityNodeRef :  (page.url.args.nodeRef != null) ? page.url.args.nodeRef : "",
	 	        	catalogId : model.catalogId
	 	         }
	 	   };
	      
	    model.widgets.push(entityCatalog);  
   }
   
   
}
main();