
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
   	if(path!=""){
   		node = node.childByNamePath(path);
   	}
       model.entity = node;
   }
   
}

main();
