function getListModels()
{
   var models = [],
      result = remote.call("/becpg/designer/model/list");
   
   if (result.status == 200)
   {
      var results = eval('(' + result + ')'),
         node;
      
      for (var i = 0, ii = results.items.length; i < ii; i++)
      {
    	  node = results.items[i];
       	  models.push(node);
      }
   }

   return models;
	
	
}

function getListConfigs()
{
   var configs = [],
      result = remote.call("/becpg/designer/config/list");
   
   if (result.status == 200)
   {
      var results = eval('(' + result + ')'),
         node;
      
      for (var i = 0, ii = results.items.length; i < ii; i++)
      {
    	  node = results.items[i];
    	  configs.push(node);
      }
   }

   return configs;
	
}

model.models = getListModels();
model.configs = getListConfigs();
model.nodeRef  =  (page.url.args["nodeRef"] !== null) ? page.url.args["nodeRef"] : null;
