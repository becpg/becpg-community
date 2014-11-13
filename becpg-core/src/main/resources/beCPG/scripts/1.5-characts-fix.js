// luceneSearch is limited to 1000 even if we pass 2000 in max arg
const MAX_SEARCH = 1;

var bContinue = true, maxWhile=0; 

function addEnforcedProp(alfType, alfProp, alfValue, alfAspects){
  
  var nodes = search.luceneSearch('+TYPE:"' + alfType + '" AND +ISNULL:"' + alfProp + '"', "", true, -1);
  logger.log(alfType + " - " + alfProp + " - " + alfValue + " - length: " + nodes.length);
  
  for each(var node in nodes) {
    
    if(node.properties[alfProp] == null){        
      
      for(i in alfAspects){
        
        logger.log(node.hasAspect[alfAspects[i].aspect]);
        if(node.hasAspect[alfAspects[i].aspect] == undefined){
            node.addAspect(alfAspects[i].aspect);
        }
      }
      
      node.properties[alfProp] = alfValue;
      node.save();
    }
  }
  
  
  
  return nodes.length;
}

var data =[ {type:"bcpg:physicoChem",prop:"bcpg:physicoChemFormulated",value:false, aspects:[{aspect:"bcpg:isDeletedAspect"},{aspect:"bcpg:legalNameAspect"}]},
            {type:"bcpg:cost",prop:"bcpg:costFixed",value:false, aspects:[{aspect:"bcpg:isDeletedAspect"},{aspect:"bcpg:legalNameAspect"}]},
  			{type:"bcpg:charact",prop:"bcpg:isDeleted",value:false, aspects:[{aspect:"bcpg:isDeletedAspect"},{aspect:"bcpg:legalNameAspect"}]}, 
            {type:"bcpg:listValue",prop:"bcpg:isDeleted",value:false},
            {type:"bcpg:linkedValue",prop:"bcpg:isDeleted",value:false, }];

for(row in data){
	
	bContinue = true;
	
	while(bContinue && maxWhile < MAX_SEARCH){
	  
	  if(addEnforcedProp(data[row].type, data[row].prop, data[row].value, data[row].aspects) == 0){
		  bContinue = false;
	  }
	  
	  maxWhile++; 
	}
}
