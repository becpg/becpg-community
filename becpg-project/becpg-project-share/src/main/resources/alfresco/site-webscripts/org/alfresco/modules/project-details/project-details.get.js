<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">

function getProjectParam() {
	model.entityNodeRef = args.nodeRef;
	if (!model.entityNodeRef) {
		status.setCode(status.STATUS_BAD_REQUEST, "nodeRef parameter is not present");
		return;
	}

	var result = remote.call("/becpg/project-details/info?nodeRef=" + model.entityNodeRef);

	return JSON.parse(result).projectDetails;
}

function main() {
	model.projectDetails = getProjectParam();
}

main();









