
function main()
{
   var nodeRef = args.nodeRef;

   if (!nodeRef)
   {
       status.setCode(status.STATUS_BAD_REQUEST, "nodeRef parameter is not present");
       return;
   }

   var project = search.findNode(nodeRef);
   if (project == null)
   {
       status.setCode(status.STATUS_NOT_FOUND, "node " + nodeRef + " is not found");
       return;
   }

   var entities = project.assocs["pjt:projectEntity"];
   if (entities == null || entities.length == 0)
   {
       status.setCode(status.STATUS_BAD_REQUEST, "project " + nodeRef + " has no associated entity");
       return;
   }

   if (!entities[0].hasPermission("Read"))
   {
       status.setCode(status.STATUS_FORBIDDEN, "project entity is not readable");
       return;
   }

   model.entity = entities[0];
}

main();
