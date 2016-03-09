var entityTpls = search.luceneSearch("+ASPECT:\"bcpg:entityTplAspect\" +ASPECT:\"bcpg:productAspect\"");
// var entityTpls = search.findNode('workspace://SpacesStore/d26d1a97-5c30-4f39-960f-7f911457ece0');

for each(var entityTpl in entityTpls){
	var sourceNodes = entityTpl.sourceAssocs["bcpg:entityTplRef"];
	
	if(sourceNodes !== null){
		for (var k = 0; k < sourceNodes.length; k++){
			if(sourceNodes[k].hasAspect("bcpg:entityTplAspect")==false){
				logger.log(sourceNodes[k].name + " - " + sourceNodes[k].properties["bcpg:code"]);
				
				//remove empty folders
				for each(var folder in sourceNodes[k].children){           
					if(folder.name !="DataLists" && folder.children.length==0){                
						logger.log("remove " + folder.name + folder.children.length);
						folder.remove();
					}
				}
	
				//copy folders of template
				for each(var folderTpl in entityTpl.children){
					if(folderTpl.name !="DataLists" &&  sourceNodes[k].childByNamePath(folderTpl.name) == null){
						logger.log("copy folder tpl " + folderTpl.name);
						folderTpl.copy(sourceNodes[k]);
					}
				}
			}				
		}
	}
}