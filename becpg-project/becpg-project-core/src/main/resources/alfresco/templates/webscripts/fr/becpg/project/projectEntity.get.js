
function main()
{
   
   var nodeRef = args.nodeRef;

   if (!nodeRef)
   {
       status.setCode(status.STATUS_BAD_REQUEST, "nodeRef parameter is not present");
       return;
   }
   
   
   
   var project = search.findNode(nodeRef);
   if(project.assocs["pjt:projectEntity"]!=null && project.assocs["pjt:projectEntity"].length>0){
       model.entity = project.assocs["pjt:projectEntity"][0];
   }
   
}

main();
