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

      onAddLabelingAspect : function EntityDataGrid_onActionShowDetails(p_items) {
         var items = YAHOO.lang.isArray(p_items) ? p_items : [ p_items ];

         for ( var i = 0, ii = items.length; i < ii; i++) {
            this._manageAspect(items[i].nodeRef, "pack:labelingAspect");
         }
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