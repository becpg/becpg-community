//<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">



function main()
{
 AlfrescoUtil.param('nodeRef', null);
 AlfrescoUtil.param('field', null);
 

   model.mlFields = [];
   model.langs = [];
   
   
   var langs = config.scoped["Languages"]["languages"].childrenMap["language"];
   var showAll = config.scoped["Languages"]["showAll"].value;
   
   model.showAll = showAll!=null && showAll == "true";
   
   for (var i = 0, lang ; i < langs.size(); i++) {
		lang = langs.get(i);
		if(lang.getAttribute("locale")!=null){
			model.langs.push({key : lang.getAttribute("locale"), label: msg.get("locale.name."+lang.getAttribute("locale"))});
		}
		
   }
   
   if (model.nodeRef && model.field)
   {
     var description = "";
    
     
      // Call the repository for the site profile
      var json = remote.call("/becpg/form/multilingual/field/"+model.field+"?nodeRef=" + model.nodeRef + (args.diffField != null ? "&diffField="+args.diffField: ""));
      if (json.status == 200)
      {
         // Create javascript objects from the repo response
         var obj = eval('(' + json + ')');
         if (obj && obj.items && obj.items.length > 0)
         {
        	  
              for (var i = 0, lang ; i < model.langs.length; i++) {
           		   lang = model.langs[i];
           		   var added = false;
           			for(var j=0, field; j < obj.items.length;j++ ){
           				field = obj.items[j];
           				description = field.description;
           				if(field.locale == lang.key){
           					field.localeLabel = lang.label;
           					field.control = {params: {editorAppearance: "custom"
           						, editorParameters:"toolbar: \"bold italic underline\",formats: { bold : {inline :'b' }},menu: {},entity_encoding : \"raw\",forced_root_blocks : false,forced_root_block: false,force_p_newlines: false"}};

           					model.mlFields.push(field);
           					added = true;
           					break;
           				}
           			}
           			
           			if(!added && model.showAll){
           				var country = lang.key.toLowerCase();
           			  	if(lang.key.indexOf("_")>0){
           			  	 country = lang.key.split("_")[1].toLowerCase();
           			  	}
           				var toAdd  = { "localeLabel" : lang.label, "locale" : lang.key, "value": "", "description":description, "country":country, "control": { "params": 
           					{"editorAppearance": "custom", "editorParameters":"toolbar: \"bold italic underline\",menu: {},formats: { bold : {inline :'b' }},entity_encoding : \"raw\",forced_root_blocks : false,forced_root_block: false,force_p_newlines: false"}
           					}};
           				model.mlFields.push(toAdd);
           			}
           			
           		
              }
              model.currentLocale = obj.currentLocale;
              
         }
      }
   }
}

main();

