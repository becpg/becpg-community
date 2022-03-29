<import resource="classpath:/beCPG/rules/helpers.js">

function main() {

	var formDataJson = JSON.parse(formData);

	var destination = search.findNode(formDataJson.alf_destination);

	var project = destination.createNode(formDataJson.prop_cm_name, "pjt:project");

	if (project) {

		submitForm(project, formDataJson);

		var deliverableList = getEntityListFromNode(project, "deliverableList");
		
		var taskList = listItems(project, "pjt:taskList");

		for (var i = 0; i < taskList.length; i++) {
			
			var task = search.findNode(taskList[i]);

			for each(var doc in items) {
				var newDeliverable = deliverableList.createNode(delivName, "pjt:deliverableList");
				var delivName = task.properties["pjt:tlTaskName"] + " - " + doc.properties["cm:name"];
				newDeliverable.properties["pjt:dlDescription"] = delivName;
				newDeliverable.properties["pjt:dlState"] = "Planned";
				newDeliverable.save();
				newDeliverable.createAssociation(task, "pjt:dlTask");
				newDeliverable.createAssociation(doc, "pjt:dlContent");
			}
		}

		return project.nodeRef;
	}
	throw "Project not provided: ";
}

main();
