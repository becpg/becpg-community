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
   
   var siteConfig =  widgetUtils.findObject(model.jsonModel, "id", "HEADER_SITE_CONFIGURATION_DROPDOWN");
   
   // If on the dashboard then add the customize dashboard option...
   if (siteConfig && page.titleId == "page.projectSiteDashboard.title" )
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