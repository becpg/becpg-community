 /**
 * Use this script to add sortable aspect on existing nodes
 */
var data =[ {type:"bcpg:costList"},
            {type:"bcpg:nutList"},
            {type:"bcpg:allergenList"},
            {type:"bcpg:ingList"},
            {type:"bcpg:physicoChemList"},
            {type:"bcpg:microbioList"},
            {type:"bcpg:compoList"},
            {type:"bcpg:packagingList"},
            {type:"bcpg:costDetailsList"},
            {type:"bcpg:priceList"},
            {type:"bcpg:organoList"},
            {type:"bcpg:forbiddenIngList"},
            {type:"bcpg:reqCtrlList"},
            {type:"mpm:processList"},
            {type:"bcpg:ingLabelingList"}];

for(row in data){
	var nodes = search.luceneSearch('+TYPE:"'+data[row].type+'"');	
	
	for(i in nodes){
		
		if(nodes[i].hasAspect('bcpg:sortableListAspect') == false){
			
			nodes[i].addAspect('bcpg:sortableListAspect', null);
		}
		nodes[i].save();
	}
}	