<import resource="classpath:/alfresco/site-webscripts/org/alfresco/becpg/menu/imports/share-header.lib.js">

var beCPGMenu = getOrCreateBeCPGMenu();

if (beCPGMenu != null) {

   beCPGMenu.config.widgets.push( {
      name : "alfresco/header/AlfMenuItem",
      config : {
         label : "header.project-list.label",
         targetUrl : "project-list#filter=projects|InProgress"
      }
   });
   
   beCPGMenu.config.widgets.push({
      name : "alfresco/header/AlfMenuItem",
      config : {
         label : "header.nc-list.label",
         targetUrl : "nc-list#filter=ncs|Claim"
      }
   });
   
   beCPGMenu.config.widgets.push({
      name : "alfresco/header/AlfMenuItem",
      config : {
         label : "header.becpg-admin.label",
         targetUrl : "console/admin-console/becpg-admin"
      }
   });

}