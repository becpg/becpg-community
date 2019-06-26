<import resource="classpath:/alfresco/site-webscripts/org/alfresco/modules/entity-datagrid/include/actions.lib.js">


function main()
{
   if(page.url.args.list!=null){
	model.listName = page.url.args.list;
   }

   model.pagination = true;
   model.filter = true;
   
   parseActions(model.listName);

   //Widget instantiation metadata...
   var entityDataGrid = {
    id : "entityDataGrid", 
    name : "beCPG.component.GanttView",
    options : {
       siteId : (page.url.templateArgs.site != null) ? page.url.templateArgs.site : "",
       usePagination: model.pagination,
       useFilter: model.filter,
       entityNodeRef: page.url.args.nodeRef!=null ?page.url.args.nodeRef : "",
       list:  model.listName!=null ? model.listName : "",
       sortable : true,
       sortUrl : page.url.context+"/proxy/alfresco/becpg/entity/datalists/sort/node",
       dataUrl : page.url.context+"/proxy/alfresco/becpg/entity/datalists/data/node/",
       itemUrl : page.url.context+"/proxy/alfresco/becpg/entity/datalists/item/node/",
       saveFieldUrl : page.url.context+"/proxy/alfresco/becpg/bulkedit/save"
      }
   };
    
   model.widgets = [entityDataGrid];
}


main();
