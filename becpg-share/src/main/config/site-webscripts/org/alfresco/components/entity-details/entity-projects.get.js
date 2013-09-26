<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">


function main()
{
   AlfrescoUtil.param('nodeRef');
   AlfrescoUtil.param('site', null);
   var documentDetails = AlfrescoUtil.getNodeDetails(model.nodeRef, model.site);
   if (documentDetails)
   {
      if(documentDetails.item.node.aspects.indexOf("bcpg:productAspect") >0){
      
         model.show = true;
         var entityProjects = {
               id : "EntityProjects", 
               name : "beCPG.component.EntityProjects",
               options : {
                  nodeRef: model.nodeRef,
                  maxItems : 15
               }
            };   
          model.widgets = [entityProjects];
      }
   }
   
   
}

main();

