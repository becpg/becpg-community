<import resource="classpath:/alfresco/site-webscripts/org/alfresco/modules/entity-datagrid/include/actions.lib.js">
<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">


/**
 * The type of the entity holding the list. This view is opened outside of the entity page
 * (wizard steps), where no entity is published to the datagrid: without it the column and
 * dialog forms fall back to the generic ones instead of their entity type variant.
 */
function getEntityType(nodeRef)
{
   if (nodeRef == null || nodeRef.length == 0)
   {
      return "";
   }
   var nodeDetails = AlfrescoUtil.getNodeDetails(nodeRef, null);
   if (nodeDetails && nodeDetails.item && nodeDetails.item.node)
   {
      return nodeDetails.item.node.type;
   }
   return "";
}

function main()
{  
   parseActions(args.list);
   model.entityType = getEntityType(args.nodeRef);
}


main();
