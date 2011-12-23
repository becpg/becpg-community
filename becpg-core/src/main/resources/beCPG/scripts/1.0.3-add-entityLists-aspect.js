/**
 * Use this script to add entityListsAspect on clients and suppliers
 * 
 */

var nodes = search.luceneSearch('TYPE:"bcpg:client" TYPE:"bcpg:supplier" -ASPECT:"bcpg:entityListsAspect"');


for(i in nodes){		
	
	nodes[i].addAspect("bcpg:entityListsAspect", null);
}
