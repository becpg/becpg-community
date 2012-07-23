/**
 * Use this script to migrate declarationType
 * 
 */


var nodes = search.luceneSearch('+TYPE:"bcpg:entity"');

if(nodes.length > 0){
	
	for(j in nodes){		
		
		var node = nodes[j];
		var parent = node.parent;
		
		if(parent.getTypeShort() == "bcpg:entityFolder"){
			
			parent.properties["bcpg:entityFolderClassName"] = node.getType();
			parent.save();
		}
		
	}		
}