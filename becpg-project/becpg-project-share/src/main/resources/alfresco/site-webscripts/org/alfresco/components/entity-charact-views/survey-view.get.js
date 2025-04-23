<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">
<import resource="classpath:/alfresco/site-webscripts/org/alfresco/modules/entity-datagrid/include/actions.lib.js">


function main()
{
   if(page.url.args.list!=null){
	model.listName = page.url.args.list;
   }

   model.pagination = true;
   model.filter = true;

   model.preferences = AlfrescoUtil.getPreferences("org.alfresco.share.project.survey");

   
   parseActions(model.listName);

   //Widget instantiation metadata...
   var entityDataGrid = {
    id : "entityDataGrid", 
    name : "beCPG.component.SurveyView",
    options : {
       siteId : (page.url.templateArgs.site != null) ? page.url.templateArgs.site : "",
       usePagination: model.pagination,
       useFilter: model.filter,
       entityNodeRef: page.url.args.nodeRef!=null ?page.url.args.nodeRef : "",
       list:  model.listName!=null ? model.listName : "",
	   viewMode :  model.preferences.mode ?  model.preferences.mode  : "survey",
       pageSize : 100,
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
