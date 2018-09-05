<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">

function getProjectParam(){
	var nodeRef = args.nodeRef;
	model.entityNodeRef=args.nodeRef;
	 if (!nodeRef){
	       status.setCode(status.STATUS_BAD_REQUEST, "nodeRef parameter is not present");
	       return;
	   }
	 
	var result = remote.call("/becpg/project-details/info?nodeRef="+nodeRef);
     
    return JSON.parse(result).projectDetails;
}

function main(){
	
	model.projectDetails = getProjectParam();
	
}

main();









