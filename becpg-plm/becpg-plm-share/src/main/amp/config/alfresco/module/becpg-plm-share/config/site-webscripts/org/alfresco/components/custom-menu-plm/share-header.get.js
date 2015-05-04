<import resource="classpath:/alfresco/site-webscripts/org/alfresco/becpg/menu/imports/share-header.lib.js">


if(isExternalUser(user)){
    widgetUtils.deleteObjectFromArray(model.jsonModel, "id", "HEADER_SITES_MENU"); 
    widgetUtils.deleteObjectFromArray(model.jsonModel, "id", "HEADER_SHARED_FILES"); 
    widgetUtils.deleteObjectFromArray(model.jsonModel, "id", "HEADER_PEOPLE"); 
    widgetUtils.deleteObjectFromArray(model.jsonModel, "id", "HEADER_SEARCH"); 
    widgetUtils.deleteObjectFromArray(model.jsonModel, "id", "HEADER_TASKS");
    widgetUtils.deleteObjectFromArray(model.jsonModel, "id", "HEADER_REPOSITORY");
} else {

    var beCPGMenu = getOrCreateBeCPGMenu();
    
    
    if (beCPGMenu != null) {
      
      model.jsonModel.services.push("alfresco/services/EcmService");
    
      var tools =  widgetUtils.findObject(model.jsonModel, "id", "HEADER_TOOLS_BECPG");
    	
      if(tools){
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
         }
          
       var warningBar =  widgetUtils.findObject(model.jsonModel, "id", "HEADER_LICENSE_WARNING");
       if(warningBar){
         warningBar.name ="alfresco/header/EcmWarningBar";
         warningBar.config = {};
       }
      
       
       if (page.id == "search")
       {
           var searchLink = widgetUtils.findObject(model.jsonModel, "id", "HEADER_ADVANCED_SEARCH");
        	if(searchLink!=null){
        		searchLink.config.label = "header.wused.search.label";
        		searchLink.config.targetUrl= "wused";
        	}
       }
       
       var searchBox =  widgetUtils.findObject(model.jsonModel, "id","HEADER_SEARCH");
         if(searchBox){
             searchBox.name ="alfresco/header/BecpgSearchBox";
             searchBox.config.linkToFacetedSearch=false;
         }
    	
    }
}
	
