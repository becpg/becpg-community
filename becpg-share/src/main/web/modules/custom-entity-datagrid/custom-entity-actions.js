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
/**
 * Entity Data Grid Custom Actions module
 * 
 * @namespace beCPG.module
 * @class beCPG.module.CustomEntityDataGridActions
 */
(function() {

   /**
    * beCPG.module.CustomEntityDataGridActions implementation
    */
   beCPG.module.CustomEntityDataGridActions = {};
   beCPG.module.CustomEntityDataGridActions.prototype = {
      /**
       * ACTIONS WHICH ARE LOCAL TO THE DATAGRID COMPONENT
       */

      /**
       * @method onActionShowDetails
       * @param items
       *            {Object | Array} Object literal representing the Data Item to be actioned, or an Array thereof
       */
      onActionShowDetails : function EntityDataGrid_onActionShowDetails(p_items) {
         var items = YAHOO.lang.isArray(p_items) ? p_items : [ p_items ], nodeRefs = [];

         for ( var i = 0, ii = items.length; i < ii; i++) {
            nodeRefs.push(items[i].nodeRef);
         }

         var url = Alfresco.constants.URL_SERVICECONTEXT + "modules/entity-charact-details/entity-charact-details" + "?entityNodeRef=" + this.options.entityNodeRef + "&itemType=" + encodeURIComponent(this.options.itemType != null ? this.options.itemType
               : this.datalistMeta.itemType) + "&dataListName=" + encodeURIComponent(this.datalistMeta.name) + "&dataListItems=" + nodeRefs
               .join(",");

         this._showPanel(url, this.id);

      },
      onActionBulkEdit : function EntityDataGrid_onActionShowDetails(p_items) {
         var items = YAHOO.lang.isArray(p_items) ? p_items : [ p_items ];

         var query = "";
         for ( var i = 0, ii = items.length; i < ii; i++) {
            if (query.length > 0) {
               query += " OR ";
            }
            if (items[i].itemData["assoc_bcpg_compoListProduct"] != null) {
               // TODO
               query += "ID:\"" + items[i].itemData["assoc_bcpg_compoListProduct"][0].value + "\"";
            }
         }

         window.location = Alfresco.constants.URL_PAGECONTEXT + "bulk-edit?t=" + encodeURIComponent(query) + "&a=true&r=true";
      },

      onActionShowComments : function EntityDataGrid_onActionShowComments(item) {

         var url = Alfresco.constants.URL_SERVICECONTEXT + "modules/comments/list?nodeRef=" + item.nodeRef + "&activityType=datalist" + (item.siteId ? "&site=" + item.siteId
               : "") + (this.options.entityNodeRef ? "&entityNodeRef=" + this.options.entityNodeRef : "");

         this._showPanel(url, this.id + "_comments", item.nodeRef);

      },

      onActionShowWused : function EntityDataGrid_onActionShowWused(p_items) {
         var items = YAHOO.lang.isArray(p_items) ? p_items : [ p_items ];

         function onActionShowWused_redirect(itemAssocName, assocName ){
            var nodeRefs = [];
            
            for ( var i = 0, ii = items.length; i < ii; i++) {
               if(!assocName){
                  nodeRefs.push(items[i].nodeRef);
               } else {
                  if(items[i].itemData[itemAssocName].value){
                     nodeRefs.push(items[i].itemData[itemAssocName].value);
                  }else {
                     for ( var j in items[i].itemData[itemAssocName]) {
                        nodeRefs.push(items[i].itemData[itemAssocName][j].value);
                     }
                  }
               }
            }
            
            if(!assocName){
               window.location = Alfresco.constants.URL_PAGECONTEXT + "wused?type=" + items[0].itemType + "&nodeRefs=" + nodeRefs
               .join();
            } else {
               window.location = Alfresco.constants.URL_PAGECONTEXT + "wused?assocName=" + assocName + "&nodeRefs=" + nodeRefs
                     .join();
            }
         }
         var showPopup = false;

         
         var html = '<div class="hd">' + this.msg("header.wused.picker") + '</div>';
         html += '<div class="bd">';
         html += '<form  class="form-container">';
         html += '<div class="form-fields">';
         html += '   <div class="set">';
         html += '        <div class="form-field">';
         html += '            <select  id="wused-selected-picker">';
         html += '                  <option value="">' + this.msg("wused.picker.choose") + '</option>';
         html += '                  <option value="selectlines">' + this.msg("wused.picker.selectedlines") + '</option>';
         for ( var key in items[0].itemData) {
            if (key.indexOf("assoc_")>-1) {
               showPopup = true;
               html += "<option value='" + key + "'>" + this.datalistColumns[key].label + "</option>";
            }
         }
         html += '            </select>';
         html += '          </div>';
         html += '       </div>';
         html += '    </div>';
         html += '</form></div>';


         if (showPopup && this.datalistMeta.name.indexOf("WUsed") != 0) {
            var containerDiv = document.createElement("div");
            containerDiv.innerHTML = html;

            this.widgets.panel = Alfresco.util.createYUIPanel(containerDiv, {
               draggable : true,
               width : "25em"
            });

            this.widgets.panel.show();

            YAHOO.util.Event
                  .on(
                        YAHOO.util.Dom.get("wused-selected-picker"),
                        'change',
                        function(e) {
                           var val = this.value == "selectlines" ? null : this.value;
                           onActionShowWused_redirect(val,val);
                        });
         } else {
            if(this.datalistMeta.name.indexOf("WUsed") == 0){
               var val = null, val2 =  "assoc_bcpg_compoListProduct";
               if(this.datalistMeta.name.indexOf("|")>0){
                  val ="assoc_"+this.datalistMeta.name.split("|")[1].replace(":","_");
               } else if(this.datalistMeta.itemType==="bcpg:packagingList"){
                  val = "assoc_bcpg_packagingListProduct";
                  val2 = val;
               } else if(this.datalistMeta.itemType==="mpm:processList"){
                  val = "assoc_mpm_plResource";
                  val2 = val;
               } else {
                  val = "assoc_bcpg_compoListProduct";
               }
               onActionShowWused_redirect(val, val2);
            } else {
               onActionShowWused_redirect();
            }
         }

      },

      onAddLabelingAspect : function EntityDataGrid_onAddLabelingAspect(p_items) {
         var items = YAHOO.lang.isArray(p_items) ? p_items : [ p_items ];

         for ( var i = 0, ii = items.length; i < ii; i++) {
            this._manageAspect(items[i].nodeRef, "pack:labelingAspect");
         }
      },
      
      onActionSimulate : function EntityDataGrid_onActionSimulate(p_items) {
         var items = YAHOO.lang.isArray(p_items) ? p_items : [ p_items ], me=this, nodeRefs = "";

         for ( var i = 0, ii = items.length; i < ii; i++) {
            if(nodeRefs.length>0){
               nodeRefs+=",";
            }
            nodeRefs+=items[i].nodeRef;
         }
         
         Alfresco.util.Ajax.request({
            method : Alfresco.util.Ajax.POST,
            url : Alfresco.constants.PROXY_URI + "becpg/entity/simulation/create?dataListItems="+nodeRefs,
            successCallback : {
               fn : function(resp) {
                  if (resp.json) {
                     for ( var i = 0, ii = items.length; i < ii; i++) {
                        YAHOO.Bubbling.fire(me.scopeId + "dataItemUpdated", {
                           nodeRef : items[i].nodeRef
                        });
                     }
                  }
               },
               scope : this
            },
            failureCallback : {
               fn : function EntityDataGrid_onActionUp_refreshFailure(response) {
                   Alfresco.util.PopupManager.displayMessage({
                       text : me.msg("message.details.failure")
                   });
               },
               scope : this
            }
         });
         
         
      },
      

      _manageAspect : function EntityDataGrid_manageAspect(itemNodeRef, aspect) {
         var itemUrl = itemNodeRef.replace(":/", ""), me = this;

         Alfresco.util.Ajax
               .request({
                  url : Alfresco.constants.PROXY_URI + "/slingshot/doclib/aspects/node/" + itemUrl,
                  method : Alfresco.util.Ajax.GET,
                  successCallback : {
                     fn : function(response) {

                        if (response.json) {
                           var dataObj = null;
                           var msgKey = "delete-aspect";

                           if (beCPG.util.contains(response.json.current, "pack:labelingAspect")) {

                              dataObj = {
                                 added : [],
                                 removed : [ aspect ]
                              };

                           } else {
                              msgKey = "add-aspect";
                              dataObj = {
                                 added : [ aspect ],
                                 removed : []
                              };
                           }

                           Alfresco.util.PopupManager
                                 .displayPrompt({
                                    title : me.msg("message.confirm." + msgKey + ".title", me.msg("aspect." + aspect
                                          .replace(":", "_"))),
                                    text : me.msg("message.confirm." + msgKey + ".description", me
                                          .msg("aspect." + aspect.replace(":", "_"))),
                                    buttons : [
                                          {
                                             text : me.msg("button." + msgKey),
                                             handler : function EntityDataGrid__onActionDelete_delete() {
                                                this.destroy();
                                                Alfresco.util.Ajax
                                                      .request({
                                                         url : Alfresco.constants.PROXY_URI + "slingshot/doclib/action/aspects/node/" + itemUrl,
                                                         method : Alfresco.util.Ajax.POST,
                                                         requestContentType : Alfresco.util.Ajax.JSON,
                                                         dataObj : dataObj,
                                                         successCallback : {
                                                            fn : function() {
                                                               YAHOO.Bubbling.fire(me.scopeId + "dataItemUpdated", {
                                                                  nodeRef : itemNodeRef
                                                               });
                                                            },
                                                            scope : this
                                                         },
                                                         successMessage : me.msg("message.success." + msgKey, me
                                                               .msg("aspect." + aspect.replace(":", "_")))
                                                      });
                                             }
                                          }, {
                                             text : this.msg("button.cancel"),
                                             handler : function EntityDataGrid__onActionDelete_cancel() {
                                                this.destroy();
                                             },
                                             isDefault : true
                                          } ]
                                 });

                        }

                     },
                     scope : this
                  },
               });
      },

      _showPanel : function EntityDataGrid__showPanel(url, htmlid, itemNodeRef) {

         var me = this;

         Alfresco.util.Ajax.request({
            url : url,
            dataObj : {
               htmlid : htmlid
            },
            successCallback : {
               fn : function(response) {
                  // Inject the template from the XHR request into a new DIV
                  // element
                  var containerDiv = document.createElement("div");
                  containerDiv.innerHTML = response.serverResponse.responseText;

                  // The panel is created from the HTML returned in the XHR
                  // request, not the container
                  var panelDiv = Dom.getFirstChild(containerDiv);
                  this.widgets.panel = Alfresco.util.createYUIPanel(panelDiv, {
                     draggable : true,
                     width : "50em"
                  });

                  this.widgets.panel.subscribe("hide", function() {
                     YAHOO.Bubbling.fire(me.scopeId + "dataItemUpdated", {
                        nodeRef : itemNodeRef
                     });
                  });

                  this.widgets.panel.show();

               },
               scope : this
            },
            failureMessage : "Could not load dialog template from '" + url + "'.",
            scope : this,
            execScripts : true
         });
      }

   };

   /**
    * Augment prototype with Common Actions module
    */
   YAHOO.lang.augmentProto(beCPG.module.EntityDataGridActions, beCPG.module.CustomEntityDataGridActions);

})();
