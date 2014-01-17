function calculatePrevDeliverables(task, prevDeliverables){
  
  	var prevTasks = task.assocs["pjt:tlPrevTasks"];
	for(var i in prevTasks){
      
      var prevTask = prevTasks[i];
      var deliverables = prevTask.sourceAssocs["pjt:dlTask"];
      for(var j in deliverables){
        prevDeliverables.unshift(deliverables[j]);
      }
		calculatePrevDeliverables(prevTask, prevDeliverables);
	}
}

function main()
{
   
   var nodeRef = args.nodeRef;

   if (!nodeRef)
   {
       status.setCode(status.STATUS_BAD_REQUEST, "nodeRef parameter is not present");
       return;
   }
   
   var task = search.findNode(nodeRef), 
   	prevDeliverables = new Array();
   
   model.task = task;
   
   if(task != null){
   	calculatePrevDeliverables(task, prevDeliverables);         
      model.deliverables = model.task.sourceAssocs["pjt:dlTask"];   
      model.prevDeliverables = prevDeliverables;
   }
   
}

main();
