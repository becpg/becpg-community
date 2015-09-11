<import resource="classpath:alfresco/site-webscripts/org/alfresco/components/workflow/filter/filter.lib.js">
<import resource="classpath:alfresco/site-webscripts/org/alfresco/modules/entity-datagrid/include/actions.lib.js">



function main()
{
   
   parseActions();
   
   var filterParameters =  getFilterParameters();
   
   model.pagination = true;
   model.filter = true;

   //Widget instantiation metadata...
   var entityDataGrid = {
    id : "entityDataGrid", 
    name : "beCPG.module.EntityDataGrid",
    options : {
       siteId : (page.url.templateArgs.site != null) ? page.url.templateArgs.site : "",
       extraDataParams: page.url.templateArgs.site!=null ? "&repo=false&container=documentLibrary":"&repo=true",
       usePagination: true,
       useFilter: true,
       itemType : "qa:nc",
       list: "ncList",
       filterParameters : filterParameters,
       sortable : false,
       dataUrl : page.url.context+"/proxy/alfresco/becpg/entity/datalists/data/node",
       itemUrl : page.url.context+"/proxy/alfresco/becpg/entity/datalists/item/node/",
     //  forceLoad : true,
       hiddenColumns : ["prop_bcpg_code", "prop_qa_ncType", "prop_qa_ncPriority"],
       groupBy : "prop_cm_created%7Cfalse",
       formWidth : "65em"
      }
   };
   
   model.widgets = [entityDataGrid];

}




main();


