
function main()
{
   
   var nodeRef = args.nodeRef;

   if (!nodeRef)
   {
       status.setCode(status.STATUS_BAD_REQUEST, "nodeRef parameter is not present");
       return;
   }
   
   
   
   var entity = search.findNode(nodeRef);

while (!entity.isSubType("bcpg:entityV2")) {
     entity = entity.parent;
}

   var sources = entity.sourceAssocs["pjt:projectEntity"];
   if(sources!=null && sources.length>0){
       model.entity = sources[0];
   }
   
}

main();
