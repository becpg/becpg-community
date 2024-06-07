/***********************************************************************************************************************
 * Copyright (C) 2010-2021 beCPG. This file is part of beCPG beCPG is free software: you can redistribute it and/or
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
             pageUri : page.url.uri,
             list : page.url.args.list
          }
       }); 
      
      var toolsWidget = [], olapSSOUrl = getOlapSSOUrl(user);
      if (olapSSOUrl ){
          toolsWidget.push( {
              name : "alfresco/header/AlfMenuItem",
              config : {
                 label : "header.becpg-olap.label",
                 iconClass : "becpg-olap-header",
                 targetUrl : olapSSOUrl,
                 targetUrlType: "FULL_PATH",
                 targetUrlLocation: "NEW"
              }
           })
      }
      
      beCPGMenu.config.widgets.push({
         id : "HEADER_TOOLS_BECPG",
         name : "alfresco/menus/AlfMenuGroup",
         config : {
            label : "header.becpg.tools",
            widgets : toolsWidget
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
  
	 if(isLanguageMgr(user)){
	   var languageMenu = widgetUtils.findObject(model.jsonModel, "id", "HEADER_BECPG_LANGUAGE");
	
	   
	   if(languageMenu == null){
		   
		   var flag = getUserLocal(user);
		   
		   if(flag == null){
			   flag = locale;
		   }
		   
		   if(flag!=null){
			   if(flag.indexOf("_")>0){
				   flag = flag.substring(3,5).toLowerCase();
			   } else {
				   flag = flag.substring(0,2).toLowerCase();
			   }
		   }
		   
		   languageMenu = {
				   id : "HEADER_BECPG_LANGUAGE",
				   name : "alfresco/menus/AlfMenuBarItem",
				   config : {
					   id : "HEADER_BECPG_LANGUAGE",
					   label : "",
					   iconImage: url.context + "/res/components/images/flags/" + flag + ".png",
					   targetUrl : "user-language?nodeRef="+ getUserNodeRef(user)
				   }
		   };
		   
		   var userMenuBar = widgetUtils.findObject(model.jsonModel, "id", "HEADER_USER_MENU_BAR");
		   
		   if (userMenuBar) {
			   userMenuBar.config.widgets.push(languageMenu);
		   }
	   
	   }
	 }


   return beCPGMenu;
}

function getOlapSSOUrl(user){
    for(var i  in user.capabilities){
       if(i.indexOf("olapSSOUrl_") == 0){
           return i.substring(11);
       }
    }
    return null;
}


function getUserLocal(user){
    for(var i  in user.capabilities){
       if(i.indexOf("userLocale_") == 0){
           return i.substring(11);
       }
    }
    return null;
}

function getUserNodeRef(user){
	 for(var i  in user.capabilities){
	       if(i.indexOf("personNodeRef_") == 0){
	           return i.substring(14);
	       }
	    }
	    return null;
	
}


function isSystemMgr(user){
    return user.capabilities["isbeCPGSystemManager"] !=null && user.capabilities["isbeCPGSystemManager"] == true;
}

function isExternalUser(user){
    return user.capabilities["isbeCPGExternalUser"] !=null && user.capabilities["isbeCPGExternalUser"] == true;
}


function isAIUser(user){
    return user.capabilities["isAIUser"] !=null && user.capabilities["isAIUser"] == true;
}


function isLanguageMgr(user){
    return user.capabilities["isbeCPGLanguageMgr"] !=null && user.capabilities["isbeCPGLanguageMgr"] == true;
}

function isMemberOfLicenseGroup(user){
	return user.capabilities["isMemberOfLicenseGroup"];
}

function isLicenseValid(user){
	return user.capabilities["isLicenseValid"];
}

function floatingLicensesExceeded(user){
	return user.capabilities["floatingLicensesExceeded"];
}







