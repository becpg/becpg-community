<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">

function getNodeProcessData(nodeRef)
{
   var result = remote.call("/becpg/node/" + nodeRef.replace(":/", "") + "/entity-process-list");
   if (result.status != 200)
   {
      AlfrescoUtil.error(result.status, 'Could not load node processes for ' + nodeRef);
   }
   return JSON.parse(result).data;
}

function main()
{
   AlfrescoUtil.param('nodeRef');
   AlfrescoUtil.param('site', null);
   var nodeDetails = AlfrescoUtil.getNodeDetails(model.nodeRef, model.site);
   if (nodeDetails)
   {
      model.destination = nodeDetails.item.parent.nodeRef;
      var processData = getNodeProcessData(model.nodeRef);
      model.processes = processData.processInstances;
      model.processTypes = processData.processTypes;
   }
   

   // Widget instantiation metadata...
   var entityProcess = {
      id : "EntityProcess", 
      name : "beCPG.component.EntityProcess"
   };
   
   model.widgets = [entityProcess];
   
   
}

main();