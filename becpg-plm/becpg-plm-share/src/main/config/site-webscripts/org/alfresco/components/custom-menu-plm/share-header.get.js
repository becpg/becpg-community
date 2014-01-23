<import resource="classpath:/alfresco/site-webscripts/org/alfresco/becpg/menu/imports/share-header.lib.js">

var beCPGMenu = getOrCreateBeCPGMenu();


if (beCPGMenu != null) {

   widgetUtils.findObject(model.jsonModel, "id", "HEADER_TOOLS_BECPG").config.widgets.push({
      name : "alfresco/header/AlfMenuItem",
      config : {
         label : "header.nc-list.label",
         iconClass : "nc-list-header",
         targetUrl : "nc-list#filter=ncs|Claim"
      }
   });
  
}
