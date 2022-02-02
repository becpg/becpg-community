function main() {

   //TODO Parcours tasks instead

	var projectNode = search.findNode(project.nodeRef);

	var entity = projectNode.assocs["pjt:projectEntity"][0];

	var docs = entity.childByNamePath('Documents').children;

	var signDeliverable = null;

	for (var i = 0; i < docs.length; i++) {

		if (docs[i].properties['cm:name'].endsWith(" - Signed")) {
			signDeliverable = docs[i];
			break;
		}

	}

	bArtworks.checkinAndSign(signDeliverable);

}

main()