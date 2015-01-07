<import resource="classpath:/alfresco/site-webscripts/org/alfresco/modules/entity-datagrid/include/actions.lib.js">


function main()
{
   if(page.url.args.list!=null){
	model.listName = page.url.args.list;
   }
	
   parseActions(model.listName);

   //Widget instantiation metadata...
   var entityDataGrid = {
    id : "entityDataGrid", 
    name : "beCPG.module.EntityDataGrid",
    options : {
       siteId : (page.url.templateArgs.site != null) ? page.url.templateArgs.site : "",
       usePagination: args.pagination!=null ? args.pagination!="false" : false,
       useFilter: args.filter!=null ? args.filter!="false" : false,
       entityNodeRef: page.url.args.nodeRef!=null ?page.url.args.nodeRef : "",
       list:  model.listName!=null ? model.listName : "",
       sortable : true,
       sortUrl : page.url.context+"/proxy/alfresco/becpg/entity/datalists/sort/node",
       dataUrl : page.url.context+"/proxy/alfresco/" +  (args.dataUrl!=null ? args.dataUrl :"becpg/entity/datalists/data/node/"),
       itemUrl : page.url.context+"/proxy/alfresco/" +  (args.itemUrl!=null ? args.itemUrl :"becpg/entity/datalists/item/node/"),
       saveFieldUrl : page.url.context+"/proxy/alfresco/becpg/bulkedit/save"
      }
   };
    
   model.widgets = [entityDataGrid];
}


main();
