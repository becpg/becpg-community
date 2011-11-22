/**
 * Use this script to remove old type
 * 
 */
var data =[ {type:"bcpg:productListsAspect"}];

for(row in data){
	var nodes = search.luceneSearch('+ASPECT:"'+data[row].type+'"');
	for(i in nodes){
		
		nodes[i].remove();
	}
}	
