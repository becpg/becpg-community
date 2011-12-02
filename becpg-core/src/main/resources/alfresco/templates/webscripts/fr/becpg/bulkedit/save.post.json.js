// save the property into the given nodes
var field = json.get("field");
var value = json.get("value");
var nodeRef = json.get("nodeRef");
var isMultiple = json.get("isMultiple");
var toSave = value;

var node = search.findNode(nodeRef);
if(node!=null && field.contains("prop_")){
	if(isMultiple == true){
		toSave = value.split(",");
	}
	node.properties[field.replace("prop_","").replace("_",":")] =  	toSave;
	node.save();
	
}


model.newValue = value;