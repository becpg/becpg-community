<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">
// Find the default ManagePermissions widget and replace it with the custom widget

var isEntity = false;

var documentDetails = AlfrescoUtil.getNodeDetails(args.nodeRef, null);
if (documentDetails) {
	isEntity = (documentDetails.item.node.aspects.indexOf("bcpg:entityListsAspect") > 0);
}
   
for (var i=0; i<model.widgets.length; i++)
{
  if (model.widgets[i].id == "ManagePermissions")
  {
	model.widgets[i].name = "beCPG.component.ManagePermissions";
    model.widgets[i].options.isEntity = isEntity;
  }
}
