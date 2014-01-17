<import resource="classpath:/alfresco/site-webscripts/org/alfresco/becpg/menu/imports/share-header.lib.js">

var beCPGMenu = getOrCreateBeCPGMenu();


if (beCPGMenu != null) {
   
      var  nodeRef = page.url.args.nodeRef, dockbarUrl = "/becpg/dockbar";
      if (nodeRef !== null && nodeRef.length > 0) {
         dockbarUrl += "?entityNodeRef=" + nodeRef;
      }
      
      var result = remote.call(dockbarUrl);
      if (result.status.code == status.STATUS_OK)
      {
         results = eval('(' + result + ')');

         beCPGMenu.config.widgets.push( {
            name : "alfresco/menus/AlfMenuGroup",
            config : {
               label : "header.becpg.recents",
            }
         } );
         
         for (var i=0; i < results.items.length; i++){
            var item = results.items[i];
            
            var targetUrl = "entity-details?nodeRef=" +item.nodeRef ;
            
            if(item.site){
               targetUrl = "site/" + item.site.shortName+"/"+targetUrl ;
            }
            
            beCPGMenu.config.widgets.push( {
               name : "alfresco/header/AlfMenuItem",
               config : {
                  label : item.displayName,
                  iconClass : item.itemType.split(":")[1],
                  targetUrl : targetUrl
               }
            });
            
         }
                  
      }

      beCPGMenu.config.widgets.push( {
         name : "alfresco/menus/AlfMenuGroup",
         config : {
            label : "header.becpg.tools",
         }
      } );   
      

   beCPGMenu.config.widgets.push( {
      name : "alfresco/header/AlfMenuItem",
      config : {
         label : "header.project-list.label",
         iconClass : "project-list-header",
         targetUrl : "project-list#filter=projects|InProgress"
            
      }
   });
   
   beCPGMenu.config.widgets.push({
      name : "alfresco/header/AlfMenuItem",
      config : {
         label : "header.nc-list.label",
         iconClass : "nc-list-header",
         targetUrl : "nc-list#filter=ncs|Claim"
      }
   });
   
   if (user.isAdmin)
   {
   
      beCPGMenu.config.widgets.push( {
         name : "alfresco/menus/AlfMenuGroup",
         config : {
            label : "header.becpg.admin",
         }
      } );   
      
      beCPGMenu.config.widgets.push({
         name : "alfresco/header/AlfMenuItem",
         config : {
            label : "header.becpg-admin.label",
            iconClass : "becpg-admin-header",
            targetUrl : "console/admin-console/becpg-admin"
         }
      });
   
   }

}