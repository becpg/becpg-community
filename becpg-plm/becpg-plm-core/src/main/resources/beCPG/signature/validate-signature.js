function main() {

	var canBeDeleted = true;

	for (var i = 0; i < project.taskList.size(); i++) {
		var otherTask = project.taskList.get(i);
		if (otherTask != task && otherTask.name.includes("-validatingTask-")) {
			if (otherTask.taskState != "Completed") {
				canBeDeleted = false;
				break;
			}
		}
	}

	if (canBeDeleted) {
		var docDeliverable;
	
		for (var i = 0; i < project.deliverableList.size(); i++) {
			var deliverable = project.deliverableList.get(i);
			if (deliverable.name.endsWith("doc") && deliverable.tasks.contains(task.nodeRef)) {
				docDeliverable = deliverable;
				break;
			}
		}

		var document = search.findNode(docDeliverable.content);

		document.properties["sign:validationDate"] = new Date();
		document.save();

		var projectNode = search.findNode(project.nodeRef);
		
		projectNode.remove();
	}
}

main();