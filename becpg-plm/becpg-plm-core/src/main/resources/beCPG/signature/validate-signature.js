function main() {
	
	var projectNode = search.findNode(project.nodeRef);
	
	var taskNode = search.findNode(task.nodeRef);

    var taskList = getEntityListFromNode(projectNode, "taskList");

	var canBeDeleted = true;

	for (var i in taskList.childAssocs["cm:contains"]) {
		var otherTask = taskList.childAssocs["cm:contains"][i];
		if (otherTask != taskNode && otherTask.properties["cm:name"].includes("-validatingTask-")) {
			if (otherTask.properties["pjt:tlState"] != "Completed") {
				canBeDeleted = false;
				break;
			}
		}
	}
	
	var document = search.findNode("workspace://SpacesStore/" + task.name.split("-validatingTask-")[1]);
	
	document.properties["sign:validationDate"] = new Date();
	document.save();
	
	if (canBeDeleted) {
		projectNode.remove();
	}
}

function getEntityListFromNode(product, listName) {
    var entityList = null;
    if (product && product.childAssocs["bcpg:entityLists"]) {
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

main();