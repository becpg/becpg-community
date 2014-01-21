<import resource="classpath:/alfresco/site-webscripts/org/alfresco/becpg/menu/imports/share-header.lib.js">

 var beCPGMenu = getOrCreateBeCPGMenu();

   if (beCPGMenu != null) {
      beCPGMenu.config.widgets.push( {
         name : "alfresco/header/AlfMenuItem",
         config : {
            label : "header.project-list.label",
            iconClass : "project-list-header",
            targetUrl : "project-list#filter=projects|InProgress"
               
         }
      });
   }
