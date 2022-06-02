function main()
{

	var entity = search.findNode(project.entities.get(0));

	var docs = entity.childByNamePath('Documents').children;

	var signedDocument = null;

	var report = bcpg.getReportNode(entity);

	var reportName = report.properties["cm:name"].split(".pdf")[0];

	var signedName = bcpg.getMessage("plm.supplier.portal.signature.signed.name", reportName) + ".pdf";

	for (var i = 0; i < docs.length; i++) {
		if (docs[i].properties['cm:name'] == signedName) {
			signedDocument = docs[i];
			break;
		}
	}

	bSign.signDocument(signedDocument);
}

main();