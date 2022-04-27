<import resource="classpath:/alfresco/site-webscripts/org/alfresco/becpg/menu/imports/share-header.lib.js">


if(isExternalUser(user)){
    widgetUtils.deleteObjectFromArray(model.jsonModel, "id", "HEADER_SITES_MENU"); 
    widgetUtils.deleteObjectFromArray(model.jsonModel, "id", "HEADER_SHARED_FILES"); 
    widgetUtils.deleteObjectFromArray(model.jsonModel, "id", "HEADER_PEOPLE"); 
    widgetUtils.deleteObjectFromArray(model.jsonModel, "id", "HEADER_SEARCH"); 
    widgetUtils.deleteObjectFromArray(model.jsonModel, "id", "HEADER_TASKS");
    widgetUtils.deleteObjectFromArray(model.jsonModel, "id", "HEADER_REPOSITORY");
} else {

	
	if (config.scoped["BecpgMenu"]){
		if (  config.scoped["BecpgMenu"]["hidePeople"] &&
	    config.scoped["BecpgMenu"]["hidePeople"].getValue() == "true"){
	     widgetUtils.deleteObjectFromArray(model.jsonModel, "id", "HEADER_PEOPLE"); 
	}
	
	if (  config.scoped["BecpgMenu"]["hideSharedFiles"] &&
		    config.scoped["BecpgMenu"]["hideSharedFiles"].getValue() == "true"){
		     widgetUtils.deleteObjectFromArray(model.jsonModel, "id", "HEADER_SHARED_FILES"); 
		}
	
	if (  config.scoped["BecpgMenu"]["hideMyFiles"] &&
		    config.scoped["BecpgMenu"]["hideMyFiles"].getValue() == "true"){
		     widgetUtils.deleteObjectFromArray(model.jsonModel, "id", "MY_FILES"); 
		}
	}
	
	
    var beCPGMenu = getOrCreateBeCPGMenu();
    
    
    if (beCPGMenu != null) {
      
      model.jsonModel.services.push("becpg/services/EcmService");
    
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
	         warningBar.name = "becpg/header/WarningBar";
		
			 var label = null;
	
			if (!isLicenseValid(user)) {
				label = "NoLicenseWarning"
			} else if (!isMemberOfLicenseGroup(user)) {
				label = "UnauthorizedWarning"
			} else {
				label = "EcmWarning"
			}
			
	         warningBar.config = {
				label : label
		     };
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
    
    var siteService = widgetUtils.findObject(model.jsonModel, "id", "SITE_SERVICE");
    if (siteService && siteService.config)
    {
    	if(siteService.config.additionalSitePresets == null){
    		siteService.config.additionalSitePresets = [];
    	}
    	
      siteService.config.additionalSitePresets.push({ label: "title.productSite", value: "product-site-dashboard" });
    }
    
    
    var siteConfig =  widgetUtils.findObject(model.jsonModel, "id", "HEADER_SITE_CONFIGURATION_DROPDOWN");
    
    // If on the dashboard then add the customize dashboard option...
    if (siteConfig && page.titleId == "page.productSiteDashboard.title" )
    {
       // Add Customize Dashboard
       siteConfig.config.widgets.push({
          id: "HEADER_CUSTOMIZE_SITE_DASHBOARD",
          name: "alfresco/menus/AlfMenuItem",
          config: {
             id: "HEADER_CUSTOMIZE_SITE_DASHBOARD",
             label: "customize_dashboard.label",
             iconClass: "alf-cog-icon",
             targetUrl: "site/" + page.url.templateArgs.site + "/customise-site-dashboard"
          }
       });
    }
  
}
	

if(page.url.url.startsWith(page.url.servletContext + "/site/" + page.url.templateArgs.site + "/entity-data-lists") ) {
	
	var siteNavigationWidgets = widgetUtils.findObject(model.jsonModel, "id", "HEADER_NAVIGATION_MENU_BAR");
	if(siteNavigationWidgets!=null){
		for(var i in siteNavigationWidgets.config.widgets){
			 if(siteNavigationWidgets.config.widgets[i].id == "HEADER_SITE_DOCUMENTLIBRARY"){
				 siteNavigationWidgets.config.widgets[i].config.selected = true;
			 }
		}
	}
	
} else if(page.url.url.startsWith(page.url.servletContext + "/site/" + page.url.templateArgs.site + "/project-list")){
	var siteNavigationWidgets = widgetUtils.findObject(model.jsonModel, "id", "HEADER_NAVIGATION_MENU_BAR");
	if(siteNavigationWidgets!=null){
		for(var i in siteNavigationWidgets.config.widgets){
			 if(siteNavigationWidgets.config.widgets[i].id == "HEADER_SITE_PROJECT-LIST"){
				 siteNavigationWidgets.config.widgets[i].config.selected = true;
			 }
		}
	}
}





