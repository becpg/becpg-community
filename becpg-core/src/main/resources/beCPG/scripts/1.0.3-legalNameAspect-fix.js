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

var nodes = search.luceneSearch('+ASPECT:"bcpg:productAspect" +TYPE:"bcpg:product" ');	

for(i in nodes){
	
	if(nodes[i].hasAspect('bcpg:legalNameAspect') == false){	
		
		nodes[i].addAspect('bcpg:legalNameAspect', null);
		nodes[i].save();
	}
	
//	if(nodes[i].hasAspect('bcpg:productMicrobioCriteriaAspect') == false){	
//		
//		nodes[i].addAspect('bcpg:productMicrobioCriteriaAspect', null);
//		nodes[i].save();
//	}
}