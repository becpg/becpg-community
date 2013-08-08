function endsWith(str, suffix) {  	
    return str.indexOf(suffix, str.length - suffix.length) !== -1;
}

var nodes = search.luceneSearch('+TYPE:"bcpg:finishedProduct" AND (@cm\\:name:"* (1)" OR @cm\\:name:"* (2)" OR @cm\\:name:"* (3)")');

for each(var node in nodes) {
  
  if(endsWith(node.properties["cm:name"], " (1)") || endsWith(node.properties["cm:name"], " (2)") || endsWith(node.properties["cm:name"], " (3)")){
    logger.log(node.name + " (" + node.typeShort + "): " + node.nodeRef);
	node.addAspect('sys:temporary');
    node.remove();
  }    
}