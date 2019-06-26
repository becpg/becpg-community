<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">

function main()
{
   AlfrescoUtil.param('nodeRef');
   AlfrescoUtil.param('site', null);
   AlfrescoUtil.param('list',null);

   // Widget instantiation metadata...
   var entityVersions = {
      id : "EntityVersions", 
      name : "beCPG.component.EntityVersions",
      options : {
         nodeRef : model.nodeRef,
         siteId : model.site,
         list: model.list
      }
   };
   
   model.widgets = [entityVersions];
}

main();

