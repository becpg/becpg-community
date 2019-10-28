
function main()
{
   
   var nodeRef = args.nodeRef;
   var path = args.path;

   if (!nodeRef)
   {
       status.setCode(status.STATUS_BAD_REQUEST, "nodeRef parameter is not present");
       return;
   }
   
   if (!path)
   {
       status.setCode(status.STATUS_BAD_REQUEST, "path parameter is not present");
       return;
   }
   
   
   var project = search.findNode(nodeRef);
   if(project){
       model.entity = project.childByNamePath(path);
   }
   
}

main();
