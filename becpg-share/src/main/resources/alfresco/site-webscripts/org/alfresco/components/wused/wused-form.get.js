<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">


function main()
{
 AlfrescoUtil.param('type', null);
 AlfrescoUtil.param('nodeRefs', "");
 AlfrescoUtil.param('assocName', null);
 
 model.searchQuery = (page.url.args["q"] != null) ? page.url.args["q"] : "";
 model.searchTerm = (page.url.args["t"] != null) ? page.url.args["t"] : "";
 
 var datatype = null;
 if (model.searchQuery !== null && model.searchQuery.length !== 0)
 {
   var formJson = jsonUtils.toObject(model.searchQuery);
	        
    if(formJson.length !== 0)
	 {
		  datatype = formJson.datatype;
	 }
  } else {
	  model.searchQuery = null;
  }	

   model.itemTypes = [];
   
   var url = "/becpg/dictionary/entity";
   if(model.type){
      url+="?itemType=" + model.type;
   } else if(model.assocName){
      url+="?assocName=" + model.assocName;
   } else if(datatype){
	   url+="?itemType=" + datatype.replace("_",":");
   } else {
	   // get the wused types from the config
	  	var wusedTypes = config.scoped["wused-search"]["itemTypes"].childrenMap["itemType"];
	  	var itemTypes = [];
	  	for (var i = 0, itemType, label; i < wusedTypes.size(); i++) {
	  		itemType = wusedTypes.get(i);

	  		// resolve label text
	  		label = itemType.attributes["label"];
	  		if (label == null) {
	  			label = "type." + itemType.attributes["name"].replace(":", "_");
	  			if (label != null) {
	  				label = msg.get(label);
	  			}
	  		}

	  		// create the model object to represent the sort field definition
	  		itemTypes.push({
	  			 name : itemType.attributes["name"],
	  			label : label ? label : itemType.attributes["name"]
	  		});
	  	}
	  		
	  	model.wusedTypes = itemTypes;
   }
   
 if(!model.wusedTypes) {   
      // Call the repository for the site profile
	 var json = remote.call(url);
	      if (json.status == 200)
	      {
	         // Create javascript objects from the repo response
	         var obj = eval('(' + json + ')');
	         if (obj && obj.items)
	         {
	           model.itemTypes = obj.items;
	         } 
	         if(obj.type){
	            model.type = obj.type;
	         }
	     }
	      
	//Load filters

	 var filterTypesConfig = config.scoped["wused-search"]["filterTypes"].childrenMap["itemType"];
	 var filterTypes = [];
	 for (var i = 0, itemType, label; i < filterTypesConfig.size(); i++) {
	  		itemType = filterTypesConfig.get(i);

	  		// resolve label text
	  		label = itemType.attributes["label"];
	  		if (label == null) {
	  			label = "type." + itemType.attributes["name"].replace(":", "_");
	  			if (label != null) {
	  				label = msg.get(label);
	  			}
	  		}

	  		// create the model object to represent the sort field definition
	  		filterTypes.push({
	  			name : itemType.attributes["name"],
	  			label : label ? label : itemType.attributes["name"],
	  		    selected : itemType.attributes["selected"] ? itemType.attributes["selected"] : false
	  		});
	  	}
	  		
	 model.filterTypes = filterTypes;
	 
	 // Aspect type replacements 
	 
	 var aspectSubstitutionsConfig = config.scoped["wused-search"]["aspectSubstitutions"].childrenMap["aspect"];
	 var aspectSubstitutions = [];
	 for (var i = 0, aspectSubstitution; i < aspectSubstitutionsConfig.size(); i++) {
		   aspectSubstitution = aspectSubstitutionsConfig.get(i);

	  		aspectSubstitutions.push({
	  			name : aspectSubstitution.attributes["name"],
	  			type : aspectSubstitution.attributes["type"]
	  		});
	  	}
	  		
	 model.aspectSubstitutions = aspectSubstitutions;
 }
    
  
   
 // Widget instantiation metadata...
   var wUsedForm = {
    id : "wUsedForm", 
    name : "beCPG.component.WUsedForm",
    options : {
       type: model.type,
       nodeRefs : model.nodeRefs, 
       searchQuery :  model.searchQuery,
       searchTerm : model.searchTerm,
       aspectSubstitutions :  model.aspectSubstitutions
      }
   };
    
   
   model.widgets = [wUsedForm];

}

main();


