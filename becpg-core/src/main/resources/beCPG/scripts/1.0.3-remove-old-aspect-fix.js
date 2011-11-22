/**
 * Use this script to remove old type
 * 
 */
var data =[ {type:"sva:finishedProduct"}];

for(row in data){
	var nodes = search.luceneSearch('+TYPE:"'+data[row].type+'"');
	for(i in nodes){
		
		nodes[i].remove();
	}
}	
