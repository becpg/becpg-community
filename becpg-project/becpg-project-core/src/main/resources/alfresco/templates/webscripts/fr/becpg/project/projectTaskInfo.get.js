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
   model.deliverables = [];
   
   
   if(task != null &&  task.sourceAssocs["pjt:dlTask"]!=null){
      for(var i = 0; i < task.sourceAssocs["pjt:dlTask"].length; i++){
          var deliverable = task.sourceAssocs["pjt:dlTask"][i];
          if(deliverable.properties["pjt:dlScriptExecOrder"] == null || 
                  deliverable.properties["pjt:dlScriptExecOrder"] == "None"){
              model.deliverables.push(deliverable);
          }
      }
   }
   
}

main();
