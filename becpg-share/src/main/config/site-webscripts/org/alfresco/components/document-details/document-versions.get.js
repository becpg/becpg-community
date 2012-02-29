<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">

function main()
{
   AlfrescoUtil.param('nodeRef');
   AlfrescoUtil.param('site', null);
   AlfrescoUtil.param('container', 'documentLibrary');
   var documentDetails = AlfrescoUtil.getNodeDetails(model.nodeRef, model.site);
   if (documentDetails)
   {
      var userPermissions = documentDetails.item.node.permissions.user;
      model.allowNewVersionUpload = (userPermissions["Write"] && userPermissions["Delete"]) || false;      
      model.allowComparison = (documentDetails.item.node.aspects.indexOf("bcpg:entityListsAspect") >0);
      if (documentDetails.workingCopy && documentDetails.workingCopy.workingCopyVersion)
      {
         model.workingCopyVersion = documentDetails.workingCopy.workingCopyVersion;
      }
   }
}

main();
