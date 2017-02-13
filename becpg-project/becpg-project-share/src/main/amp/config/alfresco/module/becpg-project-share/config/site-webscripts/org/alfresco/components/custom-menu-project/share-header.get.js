<import resource="classpath:/alfresco/site-webscripts/org/alfresco/becpg/menu/imports/share-header.lib.js">

 var beCPGMenu = getOrCreateBeCPGMenu();

   if (beCPGMenu != null) {
        var tools =   widgetUtils.findObject(model.jsonModel, "id", "HEADER_TOOLS_BECPG");
        
        if(tools){
            tools.config.widgets.push( {
                 name : "alfresco/header/AlfMenuItem",
                 config : {
                    label : "header.project-list.label",
                    iconClass : "project-list-header",
                    targetUrl : "project-list#filter=projects|InProgress"
                 }
              });
        }
   }

   
   var siteService = widgetUtils.findObject(model.jsonModel, "id", "SITE_SERVICE");
   if (siteService && siteService.config)
   {
	   if(siteService.config.additionalSitePresets == null){
		   siteService.config.additionalSitePresets = [];
	   }
   	
     siteService.config.additionalSitePresets.push({ label: "title.projectSite", value: "project-site-dashboard" });
   }