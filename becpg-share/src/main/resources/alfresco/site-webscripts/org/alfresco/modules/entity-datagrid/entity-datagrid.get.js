<import resource="classpath:/alfresco/site-webscripts/org/alfresco/modules/entity-datagrid/include/actions.lib.js">


function main(){
  
   parseActions();
   
   model.filter = args.filter!=null ?args.filter!="false":false;
   model.pagination = args.pagination!=null ?args.pagination!="false":false;
   
   var entityDataGrid = {
         id : "entityDataGrid", 
         name : "beCPG.module.EntityDataGrid",
         options : {
            usePagination: model.pagination,
            useFilter:  model.filter,
            entityNodeRef: args.nodeRef!=null ? args.nodeRef:"",
            postMethod : true,
            sortable : true,
       		sortUrl : page.url.context+"/proxy/alfresco/becpg/entity/datalists/sort/node",
       		dataUrl : page.url.context+"/proxy/alfresco/" +  (args.dataUrl!=null ? args.dataUrl :"becpg/entity/datalists/data/node"),
      		itemUrl : page.url.context+"/proxy/alfresco/" +  (args.itemUrl!=null ? args.itemUrl :"becpg/entity/datalists/item/node/"),
       		saveFieldUrl : page.url.context+"/proxy/alfresco/becpg/bulkedit/save",
       		readOnly: args.readOnly != null ? args.readOnly : false
           }
        };
         
   model.widgets = [entityDataGrid];

}

main();
