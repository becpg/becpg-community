function main() {
	
	var document = search.findNode("workspace://SpacesStore/" + task.name.split("-signTask-")[1]);
	
	bSign.signDocument(document);
	
}

main();