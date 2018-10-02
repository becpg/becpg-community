

var object = null;

var user = args.user;

 if (user != null) {
   object = people.getPerson(user);
  
  //TODO Add beCPG personAttributeMapping
  //bcpg:userLocale, bcpg:userContentLocale
  var node = search.findNode(object.nodeRef);
  model.properties = {};
  
  model.properties["userContentLocale"] = node.properties["bcpg:userContentLocale"];
  model.properties["userLocale"] = node.properties["bcpg:userLocale"];
  model.properties["userNodeRef"] = object.nodeRef.toString();
   
}

if (object != null)
{
	model.code = "OK";
}

