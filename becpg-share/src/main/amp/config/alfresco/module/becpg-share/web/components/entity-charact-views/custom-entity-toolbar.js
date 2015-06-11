/*******************************************************************************
 *  Copyright (C) 2010-2015 beCPG. 
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
                        return asset.name !== null && !asset.name.indexOf("View-properties") == 0 && !asset.name.indexOf("View-reports") == 0;
                     },
                     fn : function(instance) {

                        var dt = Alfresco.util.ComponentManager.find({
                           name : "beCPG.module.EntityDataGrid"
                        })[0];

                        var PAGE_SIZE = 5000;

                        document.location.href = dt._getDataUrl(PAGE_SIZE) + "&format=xlsx&metadata=" + encodeURIComponent(YAHOO.lang.JSON
                              .stringify(dt._buildDataGridParams()));

                     }
                  });

   }
   
   YAHOO.Bubbling.fire("registerToolbarButtonAction", {
       actionName : "entity-edit-metadata",
       evaluate : function(asset, entity) {
           return asset.name !== null && asset.name.indexOf("View-properties") == 0;
       },
       fn : function(instance) {

          window.location.href = Alfresco.util.siteURL("edit-metadata?nodeRef="+this.entity.nodeRef);

       }

    });
   
   
   YAHOO.Bubbling.fire("registerToolbarButtonAction", {
       actionName : "entity-add-aspect",
       evaluate : function(asset, entity) {
           return asset.name !== null && asset.name.indexOf("View-properties") == 0 && entity.userAccess.edit;
       },
       fn : function(instance) {
           
           this["onActionManageAspects"].call(this, this.recordData);
           
       }

    });
   
   YAHOO.Bubbling.fire("registerToolbarButtonAction", {
       actionName : "entity-print-metadata",
       evaluate : function(asset, entity) {
           return asset.name !== null && asset.name.indexOf("View-properties") == 0;
       },
       fn : function(instance) {
           var wnd = window.open(Alfresco.constants.URL_PAGECONTEXT+"print-details?nodeRef="+this.entity.nodeRef);
           setTimeout(function() {
               wnd.print();
           }, 3000);
       }

    });
   
  
   
   
   YAHOO.Bubbling.fire("registerToolbarButtonAction", {
       actionName : "entity-refresh-reports",
       evaluate : function(asset, entity) {
           return asset.name !== null && asset.name.indexOf("View-reports") == 0  && entity.userAccess.edit;
       },
       fn : function(instance) {
           Alfresco.util.PopupManager.displayMessage({
               text : this.msg("message.generate-reports.please-wait")
           });

           Alfresco.util.Ajax.request({
              method : Alfresco.util.Ajax.GET,
              url : Alfresco.constants.PROXY_URI + "becpg/entity/generate-report/node/" + this.entity.nodeRef.replace(":/", "")
                    + "/force",
              successCallback : {
                 fn : function EntityDataListToolbar_onFinish_success(response) {
                     YAHOO.Bubbling.fire("previewChangedEvent");
                 },
                 scope : this
              },
              failureCallback : {
                 fn : function EntityDataListToolbar_onFinish_failure(response) {
                     Alfresco.util.PopupManager.displayMessage({
                         text : this.msg("message.generate-reports.failure")
                     });
                 },
                 scope : this
              }
           });
       }

    });
   
   

})();
