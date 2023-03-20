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
   model.urlMap = {};
   model.contentMap = {};
   
   
	if (task != null && task.sourceAssocs["pjt:dlTask"] != null) {
		for (var i = 0; i < task.sourceAssocs["pjt:dlTask"].length; i++) {
			var deliverable = task.sourceAssocs["pjt:dlTask"][i];
			if (deliverable.properties["pjt:dlScriptExecOrder"] == null ||
				deliverable.properties["pjt:dlScriptExecOrder"] == "None") {
				model.contentMap[deliverable.nodeRef.toString()] = [];
				
				var delContents = deliverable.assocs["pjt:dlContent"];
				
				if (delContents) {
					for (var j = 0; j < delContents.length; j++) {
						var delContent = delContents[j];
						model.contentMap[deliverable.nodeRef.toString()].push(delContent);
					}
				}
				
				var url = bProject.getDeliverableUrl(deliverable);
				
				if (url) {
					
					var hasContentFromUrl = false;
					
					if (url.startsWith("content:")) {
						var contentRef = url.substring(url.indexOf("content:") + "content:".length);
						if (contentRef) {
							var regex = new RegExp(".+://.+/.+");
							if (contentRef.match(regex)) {
								var contentNode = search.findNode(contentRef);
								if (contentNode) {
									hasContentFromUrl = true;
									model.contentMap[deliverable.nodeRef.toString()].push(contentNode);
								}
							}
						}
					}
					
					if (!hasContentFromUrl) {
						model.urlMap[deliverable.nodeRef.toString()] = url;
					}
				}

				model.deliverables.push(deliverable);
			}
		}
	}
   
}

main();
