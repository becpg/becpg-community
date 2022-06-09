<import resource="classpath:/beCPG/rules/helpers.js">


function main() {


	if (project.entities != null && project.entities.size() > 0) {
		var entity = search.findNode(project.entities.get(0));

		var recipients = bSupplier.assignToSupplier(project, task, entity);

		var report = bcpg.getReportNode(entity);//,"SupplierSheet"); // TODO get report of type supplier

		//Sinon skip tâche

		if (report != null) {


			var destFolder = entity.childByNamePath(bcpg.getTranslatedPath('SupplierDocuments'));

			if (destFolder != null) {

				var signedReport = destFolder.createNode(getAvailableName(dest, report.name), "cm:content");

				updateAssoc(signedReport, "sign:recipients", recipients)


				bcpg.copyContent(report, signedReport);

				bSign.prepareForSignature(signedReport, recipients);


				for (var i = 0; i < project.deliverableList.size(); i++) {
					var deliverable = project.deliverableList.get(i);
					if (deliverable.description == bcpg.getMessage("plm.supplier.portal.deliverable.sign.url.name")) {

						var signatureUrl = bSign.getSignatureView(signedReport, supplier, task.nodeRef);

						deliverable.url = signatureUrl;
						break;
					}
				}



			}

		}

	}


}

main();
