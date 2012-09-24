/**
 * Use this script to migrate declarationType
 * 
 */

var nodes = search.luceneSearch('+TYPE:"bcpg:rawMaterial" +@bcpg\\:productHierarchy2:"workspace://SpacesStore/e66a3800-db40-4a58-b339-dc03011a8dc1"');

if(nodes.length > 0){
	
	for(j in nodes){		
		
		var node = nodes[j];		
		
		if(node.sourceAssocs["bcpg:compoListProduct"] !=null){
		}
		else{
			var entityFolder = node.parent;
			var hierarchyFolder = entityFolder.parent;
			hierarchyFolder.removeNode(entityFolder);
		}
		
	}		
}