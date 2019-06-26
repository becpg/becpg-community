

function getAssocTypes()
{
	var connector = remote.connect("alfresco");
	
	
	var jsonVar = {"fields":["cm_name"]};
	
	if(args.path){
		jsonVar.filter = 		{
			filterId : "path",
			filterData : args.path
		};
	}
	
	
   var models = [],
      result = connector.post("/becpg/entity/datalists/data/node?itemType="+args.type+"&path="+args.path, jsonUtils.toJSONString(jsonVar), "application/json");
   
   
   if (result.status == 200)
   {
      var results = eval('(' + result + ')'),
         node;
   
      model.test = result;
      
      for (var i = 0, ii = results.items.length; i < ii; i++)
      {
    	  node = results.items[i];
       	  models.push(node);
      }
   }

   return models;

}


model.filters = getAssocTypes();
