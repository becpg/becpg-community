<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">


function main()
{
 AlfrescoUtil.param('type', null);
 AlfrescoUtil.param('nodeRefs', "");
 AlfrescoUtil.param('assocName', null);
 
 

   model.itemTypes = [];
   
   var url = "/becpg/dictionnary/entity";
   if(model.type){
      url+="?itemType=" + model.type;
   } else if(model.assocName){
      url+="?assocName=" + model.assocName;
   }
   
     
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
   
 //Widget instantiation metadata...
   var wUsedForm = {
    id : "wUsedForm", 
    name : "beCPG.component.WUsedForm",
    options : {
       type: model.type,
       nodeRefs : model.nodeRefs
      }
   };
    
   
   model.widgets = [wUsedForm];

}

main();


