
function main()
{
	
// Widget instantiation metadata...
var entityDataListToolbar = {
   id : "EntityDataListToolbar", 
   name : "beCPG.component.EntityDataListToolbar",
   options : {
   	siteId : (page.url.templateArgs.site != null) ? page.url.templateArgs.site : "",
      entityNodeRef : (page.url.args.nodeRef != null) ? page.url.args.nodeRef : ""
   }
};

model.widgets = [entityDataListToolbar];

}

main();
