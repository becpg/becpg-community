<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">

function getHiddenStates()
{
   var myConfig = new XML(config.script);
   var hiddenStates = myConfig["hidden-states"].toString();
   return hiddenStates.length > 0 ? hiddenStates.split(",") : [];
}

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
         list: model.list,
         hiddenStates : getHiddenStates()
      }
   };
   
   model.widgets = [entityVersions];
}

main();

