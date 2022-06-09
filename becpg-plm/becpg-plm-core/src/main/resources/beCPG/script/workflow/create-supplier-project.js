<import resource="classpath:/beCPG/rules/helpers.js">

function main() {

	var formDataJson = JSON.parse(formData);

	var projectTpl = search.findNode(formDataJson.assoc_bcpg_entityTplRef_added);

	if (projectTpl != null) {

		var project = bSupplier.createSupplierProject(items, projectTpl,
		 formDataJson.assoc_bcpg_supplierAccountRef_added!=null ? formDataJson.assoc_bcpg_supplierAccountRef_added.split(",") : "");

		if (project) {
            formDataJson.assoc_bcpg_entityTplRef_added = "";
            formDataJson.assoc_bcpg_supplierAccountRef_added = "";
			submitForm(project, formDataJson);

			return project.nodeRef;
		}

	}

	throw "Project tpl not provided: " + formData;

}

main();
