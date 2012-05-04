
function swap(nodeRef,destNodeRef){
	 
   var node1 = search.findNode(nodeRef);
   if (node1 === null)
   {
      status.setCode(status.STATUS_NOT_FOUND, "Not a valid nodeRef: '" + nodeRef + "'");
      return null;
   }
   
   
   var node2 = search.findNode(destNodeRef);
   if (node2 === null)
   {
      status.setCode(status.STATUS_NOT_FOUND, "Not a valid nodeRef: '" + destNodeRef + "'");
      return null;
   }
  
   model.origSort = node1.properties["bcpg:sort"];
   model.destSort = node2.properties["bcpg:sort"];
   	
   if(model.origSort){
   	node2.properties["bcpg:sort"] = model.origSort; 
   	node1.properties["bcpg:sort"] = model.destSort; 
   	node2.save();
   	node1.save();
   }
	
}



function main()
{
   // nodeRef input
   var storeType = url.templateArgs.store_type,
      storeId = url.templateArgs.store_id,
      id = url.templateArgs.id,
      nodeRef = storeType + "://" + storeId + "/" + id;
   
   var dir = args.dir!=null ?args.dir : "up"
   
   var destNodeRefs = args.destNodeRef.split(",");
   
   
   if(dir!="up"){
   	destNodeRefs.reverse();
   }
   
   for(var i = 0; i< destNodeRefs.length;i++){
   	swap(nodeRef,destNodeRefs[i]);
   }
   
}

main();