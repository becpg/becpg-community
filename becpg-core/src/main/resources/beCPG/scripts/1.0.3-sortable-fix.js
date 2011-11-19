 /**
 * Use this script to add sortable aspect on existing nodes
 */
var data =[ {type:"bcpg:costList"}];

for(row in data){
	var nodes = search.luceneSearch('+TYPE:"'+data[row].type+'"');	
	
	for(i in nodes){
		
		if(nodes[i].hasAspect('bcpg:sortableListAspect') == false){
			
			nodes[i].addAspect('bcpg:sortableListAspect', null);
		}
		nodes[i].save();
	}
}	