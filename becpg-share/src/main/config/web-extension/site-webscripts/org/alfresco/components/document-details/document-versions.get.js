function main()
{
   // allow for content to be loaded from id
   if (args.nodeRef != null)
   {
      var nodeRef = args.nodeRef;      		
      
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
            versions[i].downloadURL = "/api/node/content/" + versions[i].nodeRef.replace(":/", "") + "/" + versions[i].name + "?a=true";
            versions[i].compareURL = "/becpg/entity/compare/" + versions[i].name + "?entity1=" + versions[i].nodeRef + "&entity2=" + nodeRef;
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
      model.nodeRef = nodeRef;
      model.filename = versions.length > 0 ? versions[0].name : null;
      model.versions = versions;      
   }
}

main();
