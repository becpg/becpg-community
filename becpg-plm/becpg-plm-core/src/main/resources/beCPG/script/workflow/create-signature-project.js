<import resource="classpath:/beCPG/rules/helpers.js">

function findDefaultTemplate() {
	var modelTemplates = search.query({query:'+TYPE:"pjt:project" AND =@bcpg:entityTplIsDefault:true AND =@bcpg:entityTplEnabled:true', language:'db-afts'});
	return modelTemplates[0];
}

function main() {

	var formDataJson = JSON.parse(formData);

	var destination = search.findNode(formDataJson.alf_destination);
	
	var projectName = formDataJson.prop_cm_name;
	
	var project = destination.createNode(projectName, "pjt:project");

	if (project) {

		submitForm(project, formDataJson);
		
		if (!project.assocs["bcpg:entityTplRef"]) {
			project.createAssociation(findDefaultTemplate(), "bcpg:entityTplRef");
		}
		
		var project = bSignProject.prepareSignatureProject(project, items);
		
		if (project) {
			return project.nodeRef;
		}
		
	}

    throw "Project not provided: ";
}

main();
