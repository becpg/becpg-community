/***********************************************************************************************************************
 * Copyright (C) 2010-2015 beCPG. This file is part of beCPG beCPG is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version. beCPG is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU Lesser General Public License for more details. You should have received a copy of the GNU
 * Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
function getOrCreateBeCPGMenu() {

   var beCPGMenu = widgetUtils.findObject(model.jsonModel, "id", "HEADER_BECPG");

   if (beCPGMenu == null && !isExternalUser(user)) {

      beCPGMenu = {
         id : "HEADER_BECPG",
         name : "alfresco/header/AlfMenuBarPopup",
         config : {
            id : "HEADER_BECPG",
            label : "header.menu.becpg.label",
            widgets : []
         }
      };

      beCPGMenu.config.widgets.push({
          name : "becpg/header/BeCPGRecentMenu",
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

function isExternalUser(user){
    return user.capabilities["isbeCPGExternalUser"] !=null && user.capabilities["isbeCPGExternalUser"] == true;
}



