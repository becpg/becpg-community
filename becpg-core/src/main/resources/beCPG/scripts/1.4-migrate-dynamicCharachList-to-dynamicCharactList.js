/**
 * Use this script to migrate dynamicCharachList to dynamicCharactList
 * 
 */


var nodes = search.luceneSearch('+TYPE:"bcpg:dynamicCharachList"');

if(nodes.length > 0){
	
	for(j in nodes){		
		
		var node = nodes[j];
		var parent = node.parent;
		
		if(parent != null){
						
			parent.createNode(null, "bcpg:dynamicCharactList", node.properties);
			parent.removeNode(node);
		}		
	}		
}