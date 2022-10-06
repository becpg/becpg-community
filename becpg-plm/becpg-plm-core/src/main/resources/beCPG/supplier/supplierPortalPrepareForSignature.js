<import resource="classpath:/beCPG/rules/helpers.js">


function main() {


	if (project.entities != null && project.entities.size() > 0) {
		var entity = search.findNode(project.entities.get(0));

		var recipients = bSupplier.assignToSupplier(project, task, entity);

		var report = bcpg.getReportNodeOfKind(entity, "SupplierSheet");

		if (report != null) {

			var destFolder = entity.childByNamePath(bcpg.getTranslatedPath('SupplierDocuments'));
			
			if (destFolder == null) {
				destFolder = entity.createFolderPath(bcpg.getTranslatedPath('SupplierDocuments'));
			}
			
			var signedReport = destFolder.createNode(bcpg.getAvailableName(destFolder, report.name, true), "cm:content");

			updateAssoc(signedReport, "sign:recipients", recipients);

			bcpg.copyContent(report, signedReport);

			bSign.prepareForSignature(signedReport, recipients);

			for (var i = 0; i < project.deliverableList.size(); i++) {
				var deliverable = project.deliverableList.get(i);
				if (deliverable.description == bcpg.getMessage("plm.supplier.portal.deliverable.sign.url.name")) {

					var signatureUrl = bSign.getSignatureView(signedReport, recipients[0], task.nodeRef);

					deliverable.url = signatureUrl;
					break;
				}
			}

		} else {
			bProject.updateTaskState(task, "Cancelled");
		}

	}


}

main();
