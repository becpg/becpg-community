<import resource="classpath:/beCPG/rules/helpers.js">

function getEntityListFromNode(product, listName) {
    var entityList = null;
    if (product != null && product.childAssocs["bcpg:entityLists"] != null) {
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

    if (project) {
        var taskList = getEntityListFromNode(project, "taskList");
        var deliverableList = getEntityListFromNode(project, "deliverableList");
				
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
       
        return project.nodeRef;
    }
    throw "Project not provided: ";
}

main();
