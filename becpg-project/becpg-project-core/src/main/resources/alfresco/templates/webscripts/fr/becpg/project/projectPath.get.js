
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
   
   
   var node = search.findNode(nodeRef);
   if(node){
   	while(path.indexOf("..") == 0){
   		node = node.parent;
   		path = path.substr(3);
   	}
   	model.path="";
   	
   	if(path!=""){
		   node = node.childByNamePath(bcpg.getTranslatedPath(path));
		   model.path=bcpg.getTranslatedPath(path);
   	}
       model.entity = node;
   }
   
}

main();
