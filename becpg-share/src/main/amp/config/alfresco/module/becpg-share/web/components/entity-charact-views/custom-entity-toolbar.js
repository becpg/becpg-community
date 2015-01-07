/*******************************************************************************
 *  Copyright (C) 2010-2014 beCPG. 
 *   
 *  This file is part of beCPG 
 *   
 *  beCPG is free software: you can redistribute it and/or modify 
 *  it under the terms of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation, either version 3 of the License, or 
 *  (at your option) any later version. 
 *   
 *  beCPG is distributed in the hope that it will be useful, 
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 *  GNU Lesser General Public License for more details. 
 *   
 *  You should have received a copy of the GNU Lesser General Public License along with beCPG.
 *   If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
(function() {
   if (beCPG.component.EntityDataListToolbar) {

  
      YAHOO.Bubbling.fire("registerToolbarButtonAction", {
         actionName : "entity-view-details",
         right : true,
         evaluate : function(datalistMeta, entity) {
            return entity != null && entity.type != "bcpg:systemEntity";
         },
         fn : function(instance) {

            var url = beCPG.util.entityDetailsURL(this.options.siteId, this.options.entityNodeRef, this.entity.type);

            window.location.href = url;
   
         }

      });

      YAHOO.Bubbling.fire("registerToolbarButtonAction", {
         actionName : "entity-view-documents",
         right : true,
         evaluate : function(datalistMeta, entity) {
            return entity != null && entity.type != "bcpg:systemEntity";
         },
         fn : function(instance) {

            var url = beCPG.util.entityDocumentsURL(this.options.siteId, this.entity.path, this.entity.name);

            window.location.href = url;

         }

      });

      YAHOO.Bubbling.fire("registerToolbarButtonAction", {
         right : true,
         actionName : "datalist-state",
         evaluate : function(asset, entity) {
            return entity != null && asset.state !== null && asset.name.indexOf("WUsed") < 0;
         },
         createWidget : function(containerDiv, instance) {

            var divEl = document.createElement("div");

            containerDiv.appendChild(divEl);

            Dom.setAttribute(divEl, "id", instance.id + "-stateCkeckbox");

            Dom.addClass(divEl, "stateCkeckbox");

            var stateCkeckbox = new YAHOO.widget.Button({
               type : "checkbox",
               title : instance.msg("button.datalist-state.description"),
               value : instance.datalistMeta.state,
               container : divEl,
               disabled : !instance.entity.userAccess.edit,
               checked : "Valid" == instance.datalistMeta.state
            });

            stateCkeckbox.on("checkedChange", function() {
               Alfresco.util.Ajax.request({
                  method : Alfresco.util.Ajax.POST,
                  url : Alfresco.constants.PROXY_URI + "becpg/entitylist/node/" + instance.datalistMeta.nodeRef
                        .replace(":/", "") + "?state=" + ("Valid" == instance.datalistMeta.state ? "ToValidate"
                        : "Valid"),
                  successCallback : {
                     fn : function(response) {
                        Alfresco.util.PopupManager.displayMessage({
                           text : instance.msg("message.entitylist.state.change.success")
                        });
                     },
                     scope : this
                  }
               });

            });

            return stateCkeckbox;
         }
      });

      YAHOO.Bubbling
            .fire(
                  "registerToolbarButtonAction",
                  {
                     actionName : "export-csv",
                     right : true,
                     evaluate : function(asset, entity) {
                        return asset.name !== null && asset.name.indexOf("WUsed") > -1;
                     },
                     fn : function(instance) {

                        var dt = Alfresco.util.ComponentManager.find({
                           name : "beCPG.module.EntityDataGrid"
                        })[0];

                        var PAGE_SIZE = 5000;

                        document.location.href = dt._getDataUrl(PAGE_SIZE) + "&format=xls&metadata=" + encodeURIComponent(YAHOO.lang.JSON
                              .stringify(dt._buildDataGridParams()));

                     }
                  });

   }

})();
