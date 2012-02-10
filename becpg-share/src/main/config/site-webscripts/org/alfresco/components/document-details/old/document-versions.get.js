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
      if (documentDetails.workingCopy && documentDetails.workingCopy.workingCopyVersion)
      {
         model.workingCopyVersion = documentDetails.workingCopy.workingCopyVersion;
      }
   }
//   versions[i].compareURL = "/becpg/entity/compare/" + encodeURIComponent(versions[i].name) + ".pdf?entity1=" + versions[i].nodeRef + "&entity2=" + nodeRef;
 //  model.isEntity = true;
   
   
}

main();



function main()
{
   // allow for content to be loaded from id
   if (args.nodeRef != null)
   {
      var nodeRef = args.nodeRef;      		
      
      var isEntity = false;
      
      // Call the repo to get the document versions
      var result = remote.call("/becpg/document/version-history/node/" + nodeRef.replace(":/", ""));
      
      // Create javascript objects from the server response
      var versions = [];
      
      if (result.status == 200)
      {
         versions = eval('(' + result + ')');
         
   
         var foundCurrent = false;
         var versionGroup = "newerVersion";
         for (var i = 0; i < versions.length; i++)
         {
	       if(!isEntity){
	       		isEntity =  versions[i].isEntity;
	       	}
            versions[i].downloadURL = "/api/node/content/" + versions[i].nodeRef.replace(":/", "") + "/" + versions[i].name + "?a=true";
            if(isEntity){
            	versions[i].compareURL = "/becpg/entity/compare/" + encodeURIComponent(versions[i].name) + ".pdf?entity1=" + versions[i].nodeRef + "&entity2=" + nodeRef;
            }
            if (versions[i].nodeRef == nodeRef)
            {
               versionGroup = "currentVersion";
               foundCurrent = true;
            }
            versions[i].versionGroup = versionGroup;
            if (foundCurrent && versions[i].nodeRef == nodeRef)
            {
               versionGroup = "olderVersion";            
            }
         }
      }
      
      // Prepare the model for the template
      model.isEntity = isEntity;
      model.nodeRef = nodeRef;
      model.filename = versions.length > 0 ? versions[0].name : null;
      model.versions = versions;      
   }
}

main();
