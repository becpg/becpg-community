<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">

function main()
{
   AlfrescoUtil.param('nodeRef');
   AlfrescoUtil.param('site', null);

   // Widget instantiation metadata...
   var entityVersions = {
      id : "EntityVersions", 
      name : "beCPG.component.EntityVersions",
      options : {
         nodeRef : model.nodeRef,
         siteId : model.site
      }
   };
   
   model.widgets = [entityVersions];
}

main();

