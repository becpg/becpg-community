<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">

var allowComparison = false;

 var documentDetails = AlfrescoUtil.getNodeDetails(model.nodeRef, model.site);
   if (documentDetails)
   {
   	
   	allowComparison = (documentDetails.item.node.aspects.indexOf("bcpg:entityListsAspect") >0);
   }

//Find the default DocumentList widget and replace it with the custom widget
for (var i=0; i<model.widgets.length; i++)
{
  if (model.widgets[i].id == "DocumentVersions")
  {
	  
	  
    model.widgets[i].name = "beCPG.custom.DocumentVersions";
    model.widgets[i].options.allowComparison  = allowComparison;
  }
}