<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">



function main()
{
 AlfrescoUtil.param('nodeRef', null);
 AlfrescoUtil.param('field', null);
 

   model.mlFields = [];
   
   if (model.nodeRef && model.field)
   {
     
      // Call the repository for the site profile
      var json = remote.call("/becpg/form/multilingual/field/"+model.field+"?nodeRef=" + model.nodeRef);
      if (json.status == 200)
      {
         // Create javascript objects from the repo response
         var obj = eval('(' + json + ')');
         if (obj && obj.items && obj.items.length > 0)
         {
           var langs = config.scoped["Languages"]["languages"].childrenMap["language"];
           for (var i = 0, lang ; i < langs.size(); i++) {
        		lang = langs.get(i);
        		if(lang.getAttribute("locale")!=null){
        			for(var j=0, field; j < obj.items.length;j++ ){
        				field = obj.items[j];
        				if(field.locale == lang.getAttribute("locale")){
        					model.mlFields.push(field);
        				}
        			}
        		
        		}
        		
           }
           
         }
      
      }
   }
}

main();


