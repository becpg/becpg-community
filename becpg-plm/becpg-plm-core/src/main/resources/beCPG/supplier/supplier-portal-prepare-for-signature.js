function main() {

	var projectNode = search.findNode(project.nodeRef);

	var supplier = projectNode.assocs["bcpg:suppliers"][0].assocs["bcpg:supplierAccountRef"][0];

	for each (var entity in projectNode.assocs["pjt:projectEntity"]) {

		var report = bcpg.getReportNode(entity);

		var reportName = report.properties["cm:name"].split(".pdf")[0];
		
		var signedName = bcpg.getMessage("plm.supplier.portal.signature.signed.name", reportName) + ".pdf";

		var signedReport = report.getParent().createNode(signedName, "cm:content");

		signedReport.createAssociation(supplier, "sign:recipients");

		bcpg.copyContent(report, signedReport);

		var recipients = [];
		
		recipients.push(supplier);

		bSign.prepareForSignature(signedReport, recipients);

		var deliverables = projectNode.childAssocs["bcpg:entityLists"][0].childByNamePath('deliverableList').children;
		var signDeliverable = null;

		for (var i = 0; i < deliverables.length; i++) {
			if (deliverables[i].properties["pjt:dlDescription"] == bcpg.getMessage("plm.supplier.portal.deliverable.sign.url.name")) {
				signDeliverable = deliverables[i];
				break;
			}
		}
		
		var signatureUrl = bSign.getSignatureView(signedReport, null, signedReport.nodeRef);

		signDeliverable.properties["pjt:dlDescription"] = signedName;
		signDeliverable.properties["pjt:dlUrl"] = signatureUrl;

		signDeliverable.save();

		bSupplier.assignToSupplier(project, task, entity);

	}
}

main();
