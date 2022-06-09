function main() {

	var entity = null;

	if (project.entities != null && project.entities.size() > 0) {
		entity = search.findNode(project.entities.get(0));

		var destFolder = entity.childByNamePath(bcpg.getTranslatedPath('SupplierDocuments'));

		if (destFolder != null) {

		
				for (var i = 0; i < project.deliverableList.size(); i++) {
					var deliverable = project.deliverableList.get(i);
					if (deliverable.description == bcpg.getMessage("plm.supplier.portal.deliverable.sign.url.name")) {
//Mettre le doc dans le livrable // Ou un aspect mais pas ce baser sur le nom
						
							bSign.signDocument(deliverable.content);
						
						break;
					}
				}

		
		}
	}
}

main();

