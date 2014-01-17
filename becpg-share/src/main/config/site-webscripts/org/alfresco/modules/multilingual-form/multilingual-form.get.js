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
         if (obj && obj.items)
         {
           model.mlFields = obj.items;
         } 
      }
      
   }

}

main();


