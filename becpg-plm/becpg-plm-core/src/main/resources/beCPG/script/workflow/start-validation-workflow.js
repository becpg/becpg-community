<import resource="classpath:/beCPG/rules/helpers.js">
/**
 Sample start validation script
 formData contains formData json fields
 items contains current selected items
 */
var workflow = actions.create("start-workflow"),

message = JSON.parse(formData).prop_cm_name;
workflow.parameters.workflowName = "activiti$productValidationWF";

workflow.parameters["bcpgwf:pvTransmitterComment"] = message;
workflow.parameters["bpm:workflowDescription"] = message;
var members = people.getMembers(people.getGroup("GROUP_RDMgr"));
if(members.length>0){
	workflow.parameters["bcpgwf:pvRDApprovalActor"] = members;
}
members = people.getMembers(people.getGroup("GROUP_QualityMgr"));
if(members.length>0){
	workflow.parameters["bcpgwf:pvQualityApprovalActor"] = members;
}
members = people.getMembers(people.getGroup("GROUP_ProductionMgr"));
if(members.length>0){
	workflow.parameters["bcpgwf:pvProductionApprovalActor"] = members;
}
members = people.getMembers(people.getGroup("GROUP_PackagingMgr"));
if(members.length>0){
	workflow.parameters["bcpgwf:pvPackagingApprovalActor"] = members;
}

var futureDate = new Date();
futureDate.setDate(futureDate.getDate() + 7);
workflow.parameters["bpm:workflowDueDate"] = futureDate;

workflow.execute(items);