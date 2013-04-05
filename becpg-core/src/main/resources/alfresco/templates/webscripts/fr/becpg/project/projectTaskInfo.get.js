
function main()
{
   
   var nodeRef = args.nodeRef;

   if (!nodeRef)
   {
       status.setCode(status.STATUS_BAD_REQUEST, "nodeRef parameter is not present");
       return;
   }
   
   model.task  = search.findNode(nodeRef);
   model.deliverables = model.task.sourceAssocs["pjt:dlTask"];      

}

main();
