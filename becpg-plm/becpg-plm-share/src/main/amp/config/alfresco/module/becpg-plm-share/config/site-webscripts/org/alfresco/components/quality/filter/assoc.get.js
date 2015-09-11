

function getAssocTypes()
{
	var connector = remote.connect("alfresco");
	
   var models = [],
      result = connector.post("/becpg/entity/datalists/data/node?itemType="+args.type, jsonUtils.toJSONString({"fields":["cm_name"]}), "application/json");
   
   
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
