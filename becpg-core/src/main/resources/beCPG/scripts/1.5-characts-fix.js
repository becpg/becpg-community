// luceneSearch is limited to 1000 even if we pass 2000 in max arg
const MAX_SEARCH = 1000;

var bContinue = true, maxWhile=0; 

function addEnforcedProp(alfType, alfProp, alfValue){
  
  var nodes = search.luceneSearch('+TYPE:"' + alfType + '" AND +ISNULL:"' + alfProp + '"', "", true, -1);

  for each(var node in nodes) {
    
    if(node.properties[alfProp] == null){
        node.properties[alfProp] = alfValue;
        node.save();
    }
  }
  
  logger.log(alfType + " - " + alfProp + " - " + alfValue + " - length: " + nodes.length);
  
  return nodes.length;
}

var data =[ {type:"bcpg:charact",prop:"bcpg:isDeleted",value:false}, 
            {type:"bcpg:listValue",prop:"bcpg:isDeleted",value:false},
            {type:"bcpg:linkedValue",prop:"bcpg:isDeleted",value:false},
            {type:"bcpg:physicoChem",prop:"bcpg:physicoChemFormulated",value:false},
            {type:"bcpg:cost",prop:"bcpg:costFixed",value:false}];

for(row in data){
	
	bContinue = true;
	
	while(bContinue && maxWhile < MAX_SEARCH){
	  
	  if(addEnforcedProp(data[row].type, data[row].prop, data[row].value) == 0){
		  bContinue = false;
	  }
	  
	  maxWhile++; 
	}
}

