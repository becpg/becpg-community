// save the property into the given nodes
var field = json.get("field");
var value = json.get("value");
var nodeRef = json.get("nodeRef");

var node = search.findNode(nodeRef);
if(node!=null && field.contains("prop_")){
	
	node.properties[field.replace("prop_","").replace("_",":")] =  	value;
	node.save();
	
}


model.newValue = value;