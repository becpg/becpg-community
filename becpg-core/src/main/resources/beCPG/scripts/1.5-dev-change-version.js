var data =[ {entityVersion:"workspace://SpacesStore/66a9066e-d566-4f4a-a59f-320aa11be9fe", version:"1.0"}, 
                      {entityVersion:"workspace://SpacesStore/723ee06d-b112-4a21-8f2e-d7743c121568", version:"1.1"},
                      {entityVersion:"workspace://SpacesStore/6adf027b-32bd-4328-8057-4e4c656fc02a", version:"1.2"}];


for(row in data){
	
	var entityVersion = search.findNode(data[row].entityVersion);

	if(entityVersion !=null){
		
      	logger.log("entityVersion " + data[row].entityVersion + " version " + data[row].version);
      	entityVersion.properties["cm:versionLabel"] = data[row].version;
      	delete entityVersion.properties["bcpg:versionLabel"];
        entityVersion.properties["cm:autoVersionOnUpdateProps"] = false;
        entityVersion.properties["cm:autoVersion"] = false;
        entityVersion.save();
	}	
}