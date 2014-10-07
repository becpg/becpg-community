<import resource="classpath:/alfresco/site-webscripts/org/alfresco/becpg/menu/imports/share-header.lib.js">

var beCPGMenu = getOrCreateBeCPGMenu();


if (beCPGMenu != null) {
  
  model.jsonModel.services.push("alfresco/services/EcmService");

  var tools =  widgetUtils.findObject(model.jsonModel, "id", "HEADER_TOOLS_BECPG");
	
  tools.config.widgets.push({
      name: "alfresco/header/AlfMenuItem",
      config: {
         label: "header.ecm-auto-record.label",
         publishTopic: "BECPG_ECM_AUTOMATIC_CHANGE",
         iconClass : "ecm-record"
      }
   });
  
  tools.config.widgets.push({
      name : "alfresco/header/AlfMenuItem",
      config : {
         label : "header.nc-list.label",
         iconClass : "nc-list-header",
         targetUrl : "nc-list#filter=ncs|Claim"
      }
   });
  
   var warningBar =  widgetUtils.findObject(model.jsonModel, "id", "HEADER_LICENSE_WARNING");
   if(warningBar){
     warningBar.name ="alfresco/header/EcmWarningBar";
     warningBar.config = {};
   }
  
   var searchLink = widgetUtils.findObject(model.jsonModel, "id", "HEADER_ADVANCED_SEARCH");
	if(searchLink!=null){
		searchLink.config.label = "header.wused.search.label";
		searchLink.config.targetUrl= "wused";
	}
	
	
  if(!user.isAdmin){
    //  widgetUtils.deleteObjectFromArray(model.jsonModel, "id", "HEADER_PEOPLE"); 
  }
	
	
}

	
