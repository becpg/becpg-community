<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">
<import resource="classpath:/alfresco/site-webscripts/org/alfresco/components/document-details/beCPG.lib.js">


function setPermissions(documentDetails)
{
   var rawPerms = documentDetails.item.node.permissions.roles,
      permParts,
      group,
      permission;

   model.roles = rawPerms;
   model.readPermission = false;

   if (rawPerms.length > 0)
   {
      model.readPermission = true;
      model.managers = "None";
      model.collaborators = "None";
      model.contributors = "None";
      model.consumers = "None";
      model.everyone = "None";

      for (i = 0, ii = rawPerms.length; i < ii; i++)
      {
         permParts = rawPerms[i].split(";");
         group = permParts[1];
         permission = permParts[2];
         if (group.indexOf("_SiteManager") != -1)
         {
            model.managers = permission;
         }
         else if (group.indexOf("_SiteCollaborator") != -1)
         {
            model.collaborators = permission;
         }
         else if (group.indexOf("_SiteContributor") != -1)
         {
            model.contributors = permission;
         }
         else if (group.indexOf("_SiteConsumer") != -1)
         {
            model.consumers = permission;
         }
         else if (group.indexOf("GROUP_EVERYONE") == 0 && permission !== "ReadPermissions")
         {
            model.everyone = permission;
         }
      }
   }
}

function main()
{
   AlfrescoUtil.param('nodeRef');
   AlfrescoUtil.param('site', null);

   var documentDetails = AlfrescoUtil.getNodeDetails(model.nodeRef, model.site);
   if (documentDetails)
   {
	   //beCPG
	   if(!isEntity(documentDetails)){
	      setPermissions(documentDetails);
	      model.allowPermissionsUpdate = documentDetails.item.node.permissions.user["ChangePermissions"] || false;
	      model.displayName = documentDetails.item.displayName;
	    }
   }
}

main();
