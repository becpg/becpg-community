function main() {
	
	var document = search.findNode("workspace://SpacesStore/" + task.name.split("-rejectTask-")[1]);
	
	if (document.assocs["cm:workingcopylink"] && document.assocs["cm:workingcopylink"].length > 0) {
		var workingCopy = document.assocs["cm:workingcopylink"][0];
		workingCopy.cancelCheckout();
	}
}

main();