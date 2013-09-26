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

var nodes = search.luceneSearch('+TYPE:"bcpg:product"');
var propsToRemove = ["npd:npdNumber",
                     "npd:npdType",
                     "npd:npdStatus"];

for(i in nodes){		
	
	// remove props
	var save = false;	
	for(j in propsToRemove){
		
		var propToRemove = propsToRemove[j];
		prop = nodes[i].properties[propToRemove];
		if(prop != null){
			save = true;
			delete nodes[i].properties[propToRemove];
		}
	}
	
	if(save){
		nodes[i].save();
	}	
	
	// remove assoc
	var npdInitiators = nodes[i].assocs["npd:npdInitiator"];
	for(j in npdInitiators){
		var npdInitiator = npdInitiators[j];
		if(npdInitiator != null){
			nodes[i].removeAssociation(npdInitiator, "npd:npdInitiator");
		}
	}
}