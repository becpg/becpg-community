var projectNode = search.findNode(project.nodeRef);

var entity = projectNode.assocs["pjt:projectEntity"][0];

var docs = entity.childByNamePath('Documents').children;


var sign = null;

for (var i = 0; i < docs.length; i++) {
		
	if (docs[i].properties['cm:name'] == "Signature.pdf") {
		sign = docs[i];
		break;
	}

}

bcpgArtworks.checkinSignature(sign);
