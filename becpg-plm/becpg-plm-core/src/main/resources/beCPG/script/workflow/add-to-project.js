<import resource="classpath:/beCPG/rules/helpers.js">
var projectNodeRef=JSON.parse(formData).assoc_bcpg_entityTplRef_added;

function main (){
	if(projectNodeRef) {
		updateAssoc(projectNodeRef,"pjt:projectEntity",items);
		return  projectNodeRef;
	}
	
	throw "Project not provided: "+formData;
}

main();