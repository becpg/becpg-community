/***********************************************************************************************************************
 * Copyright (C) 2010-2014 beCPG. This file is part of beCPG beCPG is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version. beCPG is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU Lesser General Public License for more details. You should have received a copy of the GNU
 * Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
function getOrCreateBeCPGMenu() {

   var beCPGMenu = widgetUtils.findObject(model.jsonModel, "id", "HEADER_BECPG");

   if (beCPGMenu == null) {

      beCPGMenu = {
         id : "HEADER_BECPG",
         name : "alfresco/header/AlfMenuBarPopup",
         config : {
            id : "HEADER_BECPG",
            label : "header.menu.becpg.label",
            widgets : []
         }
      };

      //createDockBar(beCPGMenu);
      beCPGMenu.config.widgets.push({
          name : "alfresco/header/BeCPGRecentMenu",
          config : {
             label : "header.becpg.recents",
             entityNodeRef : page.url.args.nodeRef,
             pageUri : page.url.uri
          }
       }); 
      
      
      beCPGMenu.config.widgets.push( {
         id : "HEADER_TOOLS_BECPG",
         name : "alfresco/menus/AlfMenuGroup",
         config : {
            label : "header.becpg.tools",
            widgets : []
         }
      });
   
      if (user.isAdmin || isSystemMgr(user))
      {
      
         beCPGMenu.config.widgets.push( {
            id : "HEADER_ADMIN_BECPG",
            name : "alfresco/menus/AlfMenuGroup",
            config : {
               label : "header.becpg.admin",
               widgets : [
                  {
                     name : "alfresco/header/AlfMenuItem",
                     config : {
                        label : "header.becpg-admin.label",
                        iconClass : "becpg-admin-header",
                        targetUrl : user.isAdmin? "console/admin-console/becpg-admin" : "becpg-admin"
                     }
                  }
                ]
            }
         } );   
         
      }
      
      
      var menuBar = widgetUtils.findObject(model.jsonModel, "id", "HEADER_APP_MENU_BAR");
      if (menuBar != null) {
         menuBar.config.widgets.push(beCPGMenu);
      }

   }

   return beCPGMenu;
}

function isSystemMgr(user){
    return user.capabilities["isbeCPGSystemManager"] !=null && user.capabilities["isbeCPGSystemManager"] == true;
}


function createDockBar(beCPGMenu){
   try {
         var  nodeRef = page.url.args.nodeRef, dockbarUrl = "/becpg/dockbar";
         if (nodeRef !== null && nodeRef.length > 0) {
            dockbarUrl += "?entityNodeRef=" + nodeRef.replace(/\\/g,"");
         }
         
         var result = remote.call(dockbarUrl);
         if (result.status.code == status.STATUS_OK)
         {
            results = eval('(' + result + ')');
      
            if(results.items.length>0){
            	

            	
              var recentsMenu = {
                    name : "alfresco/menus/AlfMenuGroup",
                    config : {
                       label : "header.becpg.recents",
                       widgets : []
                    }
                 }; 
                 
             
               
               for (var i=0; i < results.items.length; i++){
                  var item = results.items[i];
                  
                  var targetUrl = "entity-details?nodeRef=" +item.nodeRef ;
                  if(page.url.uri.indexOf("entity-data-lists")){
                     targetUrl = "entity-data-lists?nodeRef=" +item.nodeRef ;
                     //TODO Should be split or better
                     // page.url.args.list
                     if (item.itemType == "bcpg:finishedProduct" || item.itemType == "bcpg:semiFinishedProduct") {
                        targetUrl += "&list=compoList";
                     } else if (item.itemType == "bcpg:packagingKit") {
                        targetUrl += "&list=packagingList";
                     } else if(item.itemType == "pjt:project"){
                         targetUrl += "&list=taskList";
                     }
                     
                  }
                  
                  
                  if(item.site){
                     targetUrl = "site/" + item.site.shortName+"/"+targetUrl ;
                  }
                  
                  recentsMenu.config.widgets.push( {
                     name : "alfresco/header/AlfMenuItem",
                     config : {
                        label : item.displayName,
                        iconClass : "entity "+item.itemType.split(":")[1],
                        targetUrl : targetUrl
                     }
                  });
                  
               }
               
               beCPGMenu.config.widgets.push( recentsMenu );
            }
         }
      } catch(e){
        logger.log(e);  
      }
   }
 