 /**
 * Use this script to add manual aspect on existing nodes
 */
var data =[ {type:"bcpg:costList"}, {type:"bcpg:nutList"}, {type:"bcpg:allergenList"}, {type:"bcpg:ingLabelingList"}, {type:"bcpg:ingList"}];

for(row in data){
	var nodes = search.luceneSearch('+TYPE:"'+data[row].type+'" ');	
	
	for(i in nodes){
		
		var prop = nodes[i].properties["bcpg:isManualListItem"];
		if(prop == null){
			
			nodes[i].properties["bcpg:isManualListItem"] = false;
			nodes[i].save();
		}		
	}
}	