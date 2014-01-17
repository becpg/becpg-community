
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
      }

      var menuBar = widgetUtils.findObject(model.jsonModel, "id", "HEADER_APP_MENU_BAR");
      if (menuBar != null) {

         menuBar.config.widgets.push(beCPGMenu);
      }

   }

   return beCPGMenu;
}
