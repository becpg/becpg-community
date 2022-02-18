function getEntityListFromNode(product, listName) {
	var entityList = null;
	if (product.childAssocs["bcpg:entityLists"]) {
		var entityLists = product.childAssocs["bcpg:entityLists"][0];
		var children = entityLists.childFileFolders();
		for (var list in children) {
			if (listName === children[list].properties["cm:name"]) {
				entityList = children[list];
				break;
			}
		}
	}
	return entityList;
}

function main() {

	var formDataJson = JSON.parse(formData);

	var destination = search.findNode(formDataJson.alf_destination);

	var project = destination.createNode(formDataJson.prop_cm_name, "pjt:project");

	if (project) {

		for (var key in formDataJson) {
			if (key != "prop_cm_name" && key.startsWith("prop_")) {
				var prop = key.split("prop_")[1].replace("_", ":");
				project.properties[prop] = formDataJson[key];
				project.save();
			} else if (key.startsWith("assoc_")) {
				var assoc = key.split("assoc_")[1];

				if (assoc.endsWith("_added")) {
					assoc = assoc.split("_added")[0].replace("_", ":");

					if (formDataJson[key] != "") {
						
						var splitted = formDataJson[key].split(",");
						
						for (var value in splitted) {
							var sNode = search.findNode(splitted[value]);
							project.createAssociation(sNode, assoc);
						}
					}
				} else if (assoc.endsWith("_removed")) {
					assoc = assoc.split("_removed")[0].replace("_", ":");

					if (formDataJson[key] != "") {
						
						var splitted = formDataJson[key].split(",");
						
						for (var value in splitted) {
							var sNode = search.findNode(splitted[value]);
							project.removeAssociation(sNode, assoc);
						}
					}
				}
			}
		}

		var taskList = getEntityListFromNode(project, "taskList");
		var deliverableList = getEntityListFromNode(project, "deliverableList");

		if (taskList) {
			for each(var task in taskList.childAssocs["cm:contains"]) {

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
		}

		return project.nodeRef;
	}
	throw "Project not provided: ";
}

main();
