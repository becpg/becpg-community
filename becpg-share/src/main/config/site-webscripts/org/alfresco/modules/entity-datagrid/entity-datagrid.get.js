<import resource="classpath:/alfresco/site-webscripts/org/alfresco/modules/entity-datagrid/include/actions.lib.js">




function main(){
   
   var entityDataGrid = {
         id : "entityDataGrid", 
         name : "beCPG.module.EntityDataGrid",
         options : {
            usePagination:args.pagination!=null ?args.pagination!="false":false,
            useFilter: args.filter!=null ?args.filter!="false":false ,
            entityNodeRef: args.nodeRef!=null ? args.nodeRef:"",
            postMethod : true
           }
        };
         
   model.widgets = [entityDataGrid];
   parseActions();
}

main();
