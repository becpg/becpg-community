 /**
 * Use this script to add legalName aspect on existing nodes
 */

var nodes = search.luceneSearch('+TYPE:"bcpg:charact"');	

for(i in nodes){
	
	if(nodes[i].hasAspect('bcpg:legalNameAspect') == false){
		
		nodes[i].addAspect('bcpg:legalNameAspect', null);
	}
	nodes[i].save();
}

var nodes = search.luceneSearch('+ASPECT:"bcpg:productAspect"');	

for(i in nodes){
	
	if(nodes[i].hasAspect('bcpg:legalNameAspect') == false){
	
		var propLegalName = nodes[i].properties["bcpg:legalName"];
		
		nodes[i].addAspect('bcpg:legalNameAspect', null);
		nodes[i].save();
	}
}