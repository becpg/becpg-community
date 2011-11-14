// save the property into the given nodes
var field = json.get("field");
var value = json.get("value");
var nodeRef = json.get("nodeRef");

var savedValue = "";
for(var i in value){
	if(savedValue.length>0){
		savedValue+=",";
	}
	savedValue+=value[i];
}

var node = search.findNode(nodeRef);
if(node!=null && field.contains("prop_")){
	
	node.properties[field.replace("prop_","").replace("_",":")] =  	savedValue;
	node.save();
	
}


model.newValue = savedValue;