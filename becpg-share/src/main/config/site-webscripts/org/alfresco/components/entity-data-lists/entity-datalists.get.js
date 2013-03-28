	

function main()
{
	
// Widget instantiation metadata...
var entityDataLists = {
   id : "EntityDataLists", 
   name : "beCPG.component.EntityDataLists",
   options : {
   	listId : (page.url.args.list != null) ? page.url.args.list : "",
      entityNodeRef : (page.url.args.nodeRef != null) ? page.url.args.nodeRef : ""
   }
};

model.widgets = [entityDataLists];

}


main();

