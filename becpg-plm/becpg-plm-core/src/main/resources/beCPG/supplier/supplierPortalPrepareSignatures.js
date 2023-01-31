<import resource="classpath:/beCPG/rules/helpers.js">

function main() {

	var projectNode = search.findNode(project.nodeRef);
	
	var taskNode = search.findNode(task.nodeRef);

	bSupplier.prepareSupplierSignatures(projectNode, taskNode);
	
}

main();
