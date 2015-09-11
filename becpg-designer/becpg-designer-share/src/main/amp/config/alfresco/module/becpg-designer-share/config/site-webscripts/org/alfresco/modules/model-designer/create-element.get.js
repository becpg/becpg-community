function main()
{
   var scopedRoot = config.scoped["ModelDesigner"]["types"];

   return (
   {
      selectable: getConfigTypes(scopedRoot, args.currentType || "", args.assocType || "")
   });
}


function getConfigTypes(scopedRoot, currentType, assocType)
{
   var assocs = [],types =[], models=[],
       configs, typeConfig, typeName, assocName , modelName, subTypeConfigs, modelConfigs, assocConfigs;

      configs = scopedRoot.getChildren("type");
      if (configs)
      {
         for (var i = 0; i < configs.size(); i++)
         {
            // Get type qname from each config item
            typeConfig = configs.get(i);
            typeName = typeConfig.attributes["name"];
            if (typeName == currentType)
            {
              assocConfigs = typeConfig.childrenMap["association"];
               if (assocConfigs)
               {
                  for (var j = 0; j < assocConfigs.size(); j++)
                  {
                	 assocName = assocConfigs.get(j).attributes["name"];
                	 if(assocType=="" || assocName==assocType ){
	                     if (assocName)
	                     {
	                    	 assocs.push({name:assocName.toString(),value:assocName.toString()});
	                     }
	                     subTypeConfigs = assocConfigs.get(j).childrenMap["subtype"];
	                     if (subTypeConfigs)
	                     {
	                        for (var z = 0; z < subTypeConfigs.size(); z++)
	                        {
	                           typeName = subTypeConfigs.get(z).attributes["name"];
	                           if (typeName)
	                           {
	                          	 types.push({name:typeName.toString(),value:assocName.toString()+"-"+typeName.toString()});
	                           }
	                           
	                           modelConfigs =  subTypeConfigs.get(z).childrenMap["model"];
	                           if (modelConfigs)
	                           {
	                              for (var jj = 0; jj < modelConfigs.size(); jj++)
	                              {
	                                 modelName = modelConfigs.get(jj).attributes["name"];
	                                 if (modelName)
	                                 {
	                                	 models.push({name:modelName.toString(),value:assocName.toString()+"-"+typeName.toString()+"-"+modelName.toString()});
	                                 }
	                                 
	                              }
	                           }
	                           
	                        }
	                     }
                	 }
                  }
               }
            }
         }
         
         return {assocs : assocs,types : types, models : models};
      }
 

   return {assocs : assocs,types : types, models : models};
}

model.lists = main();
