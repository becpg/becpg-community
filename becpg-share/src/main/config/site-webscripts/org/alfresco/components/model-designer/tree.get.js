

function getListModels()
{
	//TDOD http://localhost:8080/share/proxy/alfresco/slingshot/doclib/doclist/all/node/alfresco/company/home/Dictionnaire%20de%20donn%C3%A9es/Mod%C3%A8les?filter=path&size=50&pos=1&noCache=1320849306454&libraryRoot=alfresco%3A%2F%2Fcompany%2Fhome
   var models = [],
      result = remote.call("slingshot/doclib/doclist/all/node/alfresco/company/home/Dictionnaire%20de%20donn%C3%A9es/Mod%C3%A8les?filter=path");
   
   if (result.status == 200)
   {
      var results = eval('(' + result + ')'),
         node;
      
      for (var i = 0, ii = results.items.length; i < ii; i++)
      {
    	  node = results.items[i];
         if (node.type == "cm:dictionaryModel")
         {
        	 models.push(node);
         }

        
      }
   }

   return models;
	
	
}

model.models = getListModels();