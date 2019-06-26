<import resource="classpath:/alfresco/site-webscripts/org/alfresco/modules/entity-datagrid/include/actions.lib.js">


function main()
{
   if(page.url.args.list!=null){
	model.listName = page.url.args.list;
   }
	
   model.pagination = args.pagination!=null ? args.pagination!="false" : false;
   model.filter = args.filter!=null ? args.filter!="false" : false;
   
   parseActions(model.listName);

   //Widget instantiation metadata...
   var entityDataGrid = {
    id : "entityDataGrid", 
    name : "beCPG.module.EntityDataGrid",
    options : {
       siteId : (page.url.templateArgs.site != null) ? page.url.templateArgs.site : "",
       usePagination: model.pagination,
       useFilter: model.filter,
       entityNodeRef: page.url.args.nodeRef!=null ?page.url.args.nodeRef : "",
       list:  model.listName!=null ? model.listName : "",
       sortable : true,
       sortUrl : page.url.context+"/proxy/alfresco/becpg/entity/datalists/sort/node",
       dataUrl : page.url.context+"/proxy/alfresco/" +  (args.dataUrl!=null ? args.dataUrl :"becpg/entity/datalists/data/node/"),
       itemUrl : page.url.context+"/proxy/alfresco/" +  (args.itemUrl!=null ? args.itemUrl :"becpg/entity/datalists/item/node/"),
       saveFieldUrl : page.url.context+"/proxy/alfresco/becpg/bulkedit/save"
      }
   };

   var formDatagridView = {
	      id : "formDatagridView", 
	      name : "beCPG.component.FormDataGrid",
	      options : {
	          nodeRef : page.url.args.nodeRef,
	          formId : model.listName
	       }
	  };    
  
   model.widgets = [entityDataGrid,formDatagridView];
}


main();
