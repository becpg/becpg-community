<import resource="classpath:/alfresco/site-webscripts/org/alfresco/becpg/menu/imports/share-header.lib.js">

if(user.isAdmin){

 var beCPGMenu = getOrCreateBeCPGMenu();

   if (beCPGMenu != null) {
     
      beCPGMenu.config.widgets.push ( {
         name : "alfresco/header/AlfMenuItem",
         config : {
            label : "header.model-designer.label",
            iconClass : "model-designer-header",
            targetUrl : "model-designer"
         }
      } );
   }
}