function isEntity(documentDetails) {
	if(documentDetails.item!=null
			&& documentDetails.item.node!=null
			&& documentDetails.item.node.aspects!=null){
		return documentDetails.item.node.aspects.indexOf("bcpg:entityListsAspect") >0
	}
	return false;
}
