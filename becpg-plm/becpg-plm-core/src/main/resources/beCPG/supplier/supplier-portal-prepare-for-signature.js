function main() {

	var projectNode = search.findNode(project.nodeRef);

	var supplier = projectNode.assocs["bcpg:suppliers"][0].assocs["bcpg:supplierAccountRef"][0];

	var entity = search.findNode(project.entities.get(0));

	var report = bcpg.getReportNode(entity);

	var reportName = report.properties["cm:name"].split(".pdf")[0];

	var signedName = bcpg.getMessage("plm.supplier.portal.signature.signed.name", reportName) + ".pdf";

	var signedReport = report.getParent().createNode(signedName, "cm:content");

	signedReport.createAssociation(supplier, "sign:recipients");

	bcpg.copyContent(report, signedReport);

	var recipients = [];

	recipients.push(supplier);

	bSign.prepareForSignature(signedReport, recipients);

	var signDeliverable;

	for (var i = 0; i < project.deliverableList.size(); i++) {
		var deliverable = project.deliverableList.get(i);
		if (deliverable.description == bcpg.getMessage("plm.supplier.portal.deliverable.sign.url.name")) {
			signDeliverable = deliverable;
			break;
		}
	}
	
	var signatureUrl = bSign.getSignatureView(signedReport, supplier, task.nodeRef);

	signDeliverable.description = signedName;
	signDeliverable.url = signatureUrl;

	bSupplier.assignToSupplier(project, task, entity);

}

main();
