function contains(a, obj) {
    for (var i = 0; i < a.length; i++) {
        if (a[i] === obj) {
            return true;
        }
    }
    return false;
}


function main()
{
   
   var nodeRef = args.nodeRef;

   if (!nodeRef)
   {
       status.setCode(status.STATUS_BAD_REQUEST, "nodeRef parameter is not present");
       return;
   }
   
   var task = search.findNode(nodeRef);
   
   model.task = task;
   
   if(task != null){
      model.deliverables = model.task.sourceAssocs["pjt:dlTask"];   
   }
   
}

main();
