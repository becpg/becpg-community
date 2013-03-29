<import resource="classpath:/alfresco/site-webscripts/org/alfresco/modules/entity-datagrid/include/actions.lib.js">


parseActions();

function main()
{
	
// Widget instantiation metadata...
var formulationView = {
   id : "FormulationView", 
   name : "beCPG.component.FormulationView",
   options : {
      siteId : (page.url.templateArgs.site != null) ? page.url.templateArgs.site : "",
      entityNodeRef : (page.url.args.nodeRef != null) ? page.url.args.nodeRef : ""
   }
};

model.widgets = [formulationView];

}


main();