 /**
 * Use this script to remove npd:npdAspect aspect on existing nodes
 */
var data =[ {aspect:"npd:npdAspect"}];

for(row in data){
	var nodes = search.luceneSearch('+ASPECT:"'+data[row].aspect+'"');	
	
	for(i in nodes){
		
		nodes[i].removeAspect(data[row].aspect);
		nodes[i].save();
	}
}	