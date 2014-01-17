function createDocCodification() {
	var codification = "";
	var folder = document;

	// create codification from folders
	do {
		folder = folder.parent;
		codification = (folder.properties["cm:title"] != null && folder.properties["cm:title"] != "" ? folder.properties["cm:title"] : folder.properties["cm:name"]) + "-" + codification;
	} while (folder != null && !folder.equals(space));

	// Get the current record count
	var countAction = actions.create("counter");
	countAction.execute(space);
	codification = codification + space.properties["cm:counter"]

	// Add extension
	var re = /(?:\.([^.]+))?$/;
	var documentExtension = re.exec(document.name)[1];
	if (documentExtension != undefined) {
		codification = codification + "." + documentExtension;
	}

	// set name
	document.properties["cm:name"] = codification;
	document.properties["cm:title"] = codification;
	document.save();
}

createDocCodification();
