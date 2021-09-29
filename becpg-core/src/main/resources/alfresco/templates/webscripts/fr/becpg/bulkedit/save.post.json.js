// save the property into the given nodes
var field = json.get("field");
var nodeRef = json.get("nodeRef");
var isMultiple = json.get("isMultiple");
var value = json.get("value");

var node = search.findNode(nodeRef.get("storeRef").get("protocol") + "://" + nodeRef.get("storeRef").get("identifier") + "/" + nodeRef.get("id"));
if(node!=null && field.contains("prop_")){
	
   if(!json.isNull("value")){
      var toSave = value;
         if(isMultiple == true){
          toSave = value.split(",");
        }
    	node.properties[field.replace("prop_","").replace("_",":")] =  	toSave;
   } else {
      delete node.properties[field.replace("prop_","").replace("_",":")];
   }
	
	node.save();
	
}


model.newValue = value;
