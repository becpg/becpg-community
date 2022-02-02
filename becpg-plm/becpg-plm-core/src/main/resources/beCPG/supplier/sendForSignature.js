function main() {

	var projectNode = search.findNode(project.nodeRef);

	var entity = projectNode.assocs["pjt:projectEntity"][0];

	var supplier = projectNode.assocs["bcpg:suppliers"][0].assocs["bcpg:supplierAccountRef"][0];

	var report = bcpg.getReportNode(entity);

	var reportName = report.properties["cm:name"];

	var copy = report.getParent().createNode(reportName + " - Signed", "cm:content");

	copy.addAspect("sign:signatureAspect");
	copy.createAssociation(supplier, "sign:recipients");

	bcpg.copyContent(report, copy);

	bSign.prepareForSignature(copy);

	var deliverables = projectNode.childAssocs["bcpg:entityLists"][0].childByNamePath('deliverableList').children;
	var signDeliverable = null;

    //TODO à revoir 

	for (var i = 0; i < deliverables.length; i++) {
		if (deliverables[i].properties['pjt:dlDescription'] == "Signature") {
			signDeliverable = deliverables[i];
			break;
		}
	}

	var tasks = projectNode.childAssocs["bcpg:entityLists"][0].childByNamePath('taskList').children;

	var signTask = null;

	for (var i = 0; i < tasks.length; i++) {

		if (tasks[i].properties['pjt:tlTaskName'] == "Signature") {
			signTask = tasks[i];
			break;
		}

	}


	var signatureUrl = bSign.getSignatureViewUrl(copy, supplier.properties["cm:userName"], signTask.nodeRef);

	signDeliverable.properties["pjt:dlUrl"] = signatureUrl;

	signDeliverable.save();

	bSupplier.assignToSupplier(project, task, projectEntity, true);

}

main();
