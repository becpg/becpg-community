var nodes = search.luceneSearch('+TYPE:"bcpg:entityV2"');

for each(var node in nodes) {
  var save = false;
  
  if(node.hasAspect('bcpg:effectivityAspect') == false){
    node.addAspect('bcpg:effectivityAspect', null);
	save = true;
  }
  
  if(node.hasAspect('bcpg:entityVersionable') == false){
    node.addAspect('bcpg:entityVersionable', null);
	save = true;
  }
  
  if(node.hasAspect('bcpg:entityTplRefAspect') == false){
    node.addAspect('bcpg:entityTplRefAspect', null);
	save = true;
  }
  
  if(save){
	  node.save();
  }
}