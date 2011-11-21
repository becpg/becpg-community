

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

model.models = getListModels();
model.nodeRef  =  (page.url.args["nodeRef"] !== null) ? page.url.args["nodeRef"] : null;