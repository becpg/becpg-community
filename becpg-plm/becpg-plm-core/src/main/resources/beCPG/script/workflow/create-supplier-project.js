<import resource="classpath:/beCPG/rules/helpers.js">

function main() {

	var formDataJson = JSON.parse(formData);

	var projectTpl = search.findNode(formDataJson.assoc_bcpg_entityTplRef_added);
	
	var supplierTaskDueDate = formDataJson.prop_pjt_tlDue;
	
	delete formDataJson.prop_pjt_tlDue;
	
	if (projectTpl != null) {

		var project = bSupplier.createSupplierProject(items, projectTpl,
		 formDataJson.assoc_bcpg_supplierAccountRef_added!=null ? formDataJson.assoc_bcpg_supplierAccountRef_added.split(",") : "");

		if (project) {
            formDataJson.assoc_bcpg_entityTplRef_added = "";
            formDataJson.assoc_bcpg_supplierAccountRef_added = "";
            formDataJson.assoc_pjt_projectEntity_added = "";
			submitForm(project, formDataJson);
			
			if (supplierTaskDueDate && supplierTaskDueDate != "") {
				
				var supplierTask = findSupplierTask(project);
				
				if (supplierTask) {
					var taskDuration = bProject.calculateTaskDuration("NOW", supplierTaskDueDate);
					supplierTask.properties["pjt:tlDuration"] = taskDuration;
					supplierTask.save();
				}
			}
			
			return project.nodeRef;
		}

	}

	throw "Project tpl not provided: " + formData;

}


function findSupplierTask(project) {
	
	var delList = listItems(project, "pjt:deliverableList");
	var scriptSearch = search.xpathSearch("/app:company_home/app:dictionary/app:scripts/cm:supplierPortalScript.js");
	
	if (scriptSearch && scriptSearch.length > 0 && delList) {
		var supplierPortalScript = scriptSearch[0];
		for (var i = 0; i < delList.length; i++) {
			var del =  search.findNode(delList[i]);
			
			var contents = del.assocs["pjt:dlContent"];
			
			if (contents) {
				for (var j = 0; j < contents.length; j++) {
					var content = contents[j];
					if (content.nodeRef.toString() == supplierPortalScript.nodeRef.toString()) {
						var tasks = del.assocs["pjt:dlTask"];
						if (tasks) {
							return tasks[0];
						}
					}
				}
			}
		}
	}
	
	return null;
}

main();
