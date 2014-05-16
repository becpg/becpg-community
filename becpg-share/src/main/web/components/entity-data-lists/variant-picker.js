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
   /**
    * YUI Library aliases
    */
   var Dom = YAHOO.util.Dom;

   /**
    * VariantPicker constructor.
    * 
    * @param htmlId
    *            {String} The HTML id of the parent element
    * @return {beCPG.component.VariantPicker} The new VariantPicker instance
    * @constructor
    */
   beCPG.component.VariantPicker = function(htmlId) {

      beCPG.component.VariantPicker.superclass.constructor.call(this, "beCPG.component.VariantPicker", htmlId, [
            "button", "container" ]);

      // message
      this.name = "beCPG.component.EntityDataListToolbar";

      return this;
   };

   /**
    * Extend from Alfresco.component.Base
    */
   YAHOO.extend(beCPG.component.VariantPicker, Alfresco.component.Base);

   /**
    * Augment prototype with main class implementation, ensuring overwrite is enabled
    */
   YAHOO.lang
         .augmentObject(
               beCPG.component.VariantPicker.prototype,
               {
                  /**
                   * Object container for initialization options
                   * 
                   * @property options
                   * @type object
                   */
                  options : {

                     entityNodeRef : "",

                     entity : null,

                     containerDiv : null,

                     toolBarInstance : null,

                     formWidth : "34em"

                  },

                  currentVariantNodeRef : null,

                  /**
                   * Fired by YUI when parent element is available for scripting.
                   * 
                   * @method onReady
                   */
                  onReady : function VariantPicker_onReady() {

                     var entityVariantsPickerMenu = [

                     {
                        text : '<span class="variant-all">' + this.msg("picker.variant.all") + '</span>',
                        value : "all",
                        onclick : {
                           fn : this.onMenuItemClick,
                           scope : this
                        }
                     }, {
                        text : '<span class="variant-common">' + this.msg("picker.variant.common") + '</span>',
                        value : "common",
                        onclick : {
                           fn : this.onMenuItemClick,
                           scope : this
                        }
                     }

                     ];

                     for ( var i in this.options.entity.variants) {

                        var variantLbl = '<span class="variant' + (this.options.entity.variants[i].isDefaultVariant ? '-default'
                              : '') + '">' + this.options.entity.variants[i].name + '</span>';

                        entityVariantsPickerMenu.push({
                           text : variantLbl,
                           value : this.options.entity.variants[i].nodeRef,
                           onclick : {
                              fn : this.onMenuItemClick,
                              scope : this
                           }
                        });
                     }

                     this.widgets.variantPicker = new YAHOO.widget.Button({
                        type : "split",
                        label : this.msg("picker.variant.choose"),
                        name : "mymenubutton",
                        menu : entityVariantsPickerMenu,
                        container : this.options.containerDiv,
                        lazyloadmenu : false
                     });

                     this.widgets.createVariantButton = this.createYUIButton(this, "create-variant",
                           this.onCreateVariant);

                     this.widgets.editVariantButton = this.createYUIButton(this, "edit-variant", this.onEditVariant);

                     this.widgets.deleteVariantButton = this.createYUIButton(this, "delete-variant",
                           this.onDeleteVariant);

                     Dom.setStyle(this.widgets.editVariantButton, 'display', 'none');
                     Dom.setStyle(this.widgets.deleteVariantButton, 'display', 'none');

                  },

                  createYUIButton : function(instance, actionName, fn) {

                     var template = Dom.get(this.options.toolBarInstance.id + "-toolBar-template-button"), buttonWidget = null;

                     var spanEl = Dom.getFirstChild(template).cloneNode(true);

                     Dom.addClass(spanEl, actionName);

                     Dom.setAttribute(spanEl, "id", this.id + "-" + actionName + "Button");

                     this.options.containerDiv.appendChild(spanEl);

                     buttonWidget = Alfresco.util.createYUIButton(this, actionName + "Button", fn);

                     buttonWidget.set("label", this.msg("button." + actionName));
                     buttonWidget.set("title", this.msg("button." + actionName + ".description"));

                     return buttonWidget;

                  },

                  onMenuItemClick : function(p_sType, p_aArgs, p_oItem) {

                     if (p_oItem) {
                        var sText = p_oItem.cfg.getProperty("text"), value = p_oItem.value;
                        this.widgets.variantPicker.set("label", sText);

                        if ("all" === value || "common" === value) {

                           this.currentVariantNodeRef = null;
                           Dom.setStyle(this.widgets.createVariantButton, 'display', '');
                           Dom.setStyle(this.widgets.editVariantButton, 'display', 'none');
                           Dom.setStyle(this.widgets.deleteVariantButton, 'display', 'none');
                           if ("all" === p_oItem.value) {
                              YAHOO.Bubbling.fire("changeFilter", {
                                 filterOwner : this.id,
                                 filterId : "all"
                              });
                           } else if ("common" === p_oItem.value) {
                              YAHOO.Bubbling.fire("changeFilter", {
                                 filterOwner : this.id,
                                 filterId : "fts",
                                 filterData : "ISNULL:bcpg\\:variantIds OR ISUNSET:bcpg\\:variantIds"
                              });
                           }
                        } else {
                           YAHOO.Bubbling
                                 .fire(
                                       "changeFilter",
                                       {
                                          filterOwner : this.id,
                                          filterId : "fts",
                                          filterData : "@bcpg\\:variantIds:\"" + p_oItem.value + "\" OR ISNULL:bcpg\\:variantIds OR ISUNSET:bcpg\\:variantIds"
                                       });

                           this.currentVariantNodeRef = p_oItem.value;

                           Dom.setStyle(this.widgets.createVariantButton, 'display', 'none');
                           Dom.setStyle(this.widgets.editVariantButton, 'display', '');
                           Dom.setStyle(this.widgets.deleteVariantButton, 'display', '');
                        }
                     }
                  },

                  onCreateVariant : function VariantPicker_onCreateVariant() {

                     var instance = this;

                     var doBeforeDialogShow = function(p_form, p_dialog) {
                        Alfresco.util.populateHTML([ p_dialog.id + "-dialogTitle",
                              instance.msg("label.new-variant.title") ]);
                     };

                     var templateUrl = YAHOO.lang
                           .substitute(
                                 Alfresco.constants.URL_SERVICECONTEXT + "components/form?itemKind={itemKind}&itemId={itemId}&destination={destination}&association={association}&mode={mode}&submitType={submitType}&showCancelButton=true&popup=true",
                                 {
                                    itemKind : "type",
                                    itemId : "bcpg:variant",
                                    destination : instance.options.entityNodeRef,
                                    association : "bcpg:variants",
                                    mode : "create",
                                    submitType : "json"
                                 });

                     var createRow = new Alfresco.module.SimpleDialog(instance.id + "-createVariant");

                     createRow.setOptions({
                        width : instance.options.formWidth,
                        templateUrl : templateUrl,
                        actionUrl : null,
                        destroyOnHide : true,
                        doBeforeDialogShow : {
                           fn : doBeforeDialogShow,
                           scope : this
                        },

                        onSuccess : {
                           fn : function EntityDataGrid_onActionCreate_success(response) {

                              Alfresco.util.Ajax.jsonRequest({
                                 url : Alfresco.constants.PROXY_URI + "api/forms/picker/items",
                                 method : "POST",
                                 dataObj : {
                                    items : [ response.json.persistedObject ]
                                 },
                                 successCallback : {
                                    fn : function(res) {
                                       var items = res.json.data.items, menuItems = [];

                                       for ( var i = 0, il = items.length; i < il; i++) {
                                          menuItems.push({
                                             text : items[i].name,
                                             value : items[i].nodeRef,
                                             onclick : {
                                                fn : instance.onMenuItemClick,
                                                scope : instance
                                             }
                                          });

                                       }

                                       instance.widgets.variantPicker.getMenu().addItems(menuItems);

                                       Alfresco.util.PopupManager.displayMessage({
                                          text : instance.msg("message.create-variant.success")
                                       });

                                    },
                                    scope : instance
                                 }
                              });

                           },
                           scope : this
                        },
                        onFailure : {
                           fn : function EntityDataGrid_onActionCreate_failure(response) {
                              Alfresco.util.PopupManager.displayMessage({
                                 text : instance.msg("message.create-variant.failure")
                              });
                           },
                           scope : this
                        }
                     }).show();

                  },

                  onEditVariant : function VariantPicker_onCreateVariant() {

                     var instance = this;

                     if (instance.currentVariantNodeRef) {

                        var doBeforeDialogShow = function(p_form, p_dialog) {
                           Alfresco.util.populateHTML([ p_dialog.id + "-dialogTitle",
                                 instance.msg("label.edit-variant.title") ]);
                        };

                        var templateUrl = YAHOO.lang
                              .substitute(
                                    Alfresco.constants.URL_SERVICECONTEXT + "components/form?itemKind={itemKind}&itemId={itemId}&mode={mode}&submitType={submitType}&showCancelButton=true&popup=true",
                                    {
                                       itemKind : "node",
                                       itemId : instance.currentVariantNodeRef,
                                       mode : "edit",
                                       submitType : "json"
                                    });

                        var editRow = new Alfresco.module.SimpleDialog(instance.id + "-editVariant");

                        editRow
                              .setOptions(
                                    {
                                       width : instance.options.formWidth,
                                       templateUrl : templateUrl,
                                       actionUrl : null,
                                       destroyOnHide : true,
                                       doBeforeDialogShow : {
                                          fn : doBeforeDialogShow,
                                          scope : this
                                       },

                                       onSuccess : {
                                          fn : function(response) {
                                             Alfresco.util.Ajax
                                                   .jsonRequest({
                                                      url : Alfresco.constants.PROXY_URI + "api/forms/picker/items",
                                                      method : "POST",
                                                      dataObj : {
                                                         items : [ response.json.persistedObject ]
                                                      },
                                                      successCallback : {
                                                         fn : function(res) {
                                                            var items = res.json.data.items, menuItems = instance.widgets.variantPicker
                                                                  .getMenu().getItems();

                                                            for ( var i = 0, il = items.length; i < il; i++) {

                                                               for ( var index in menuItems) {
                                                                  if (menuItems.hasOwnProperty(index)) {
                                                                     if (menuItems[index].value === items[i].nodeRef) {
                                                                        instance.widgets.variantPicker.getMenu()
                                                                              .removeItem(menuItems[index]);
                                                                        instance.widgets.variantPicker.getMenu()
                                                                              .addItem({
                                                                                 text : items[i].name,
                                                                                 value : items[i].nodeRef,
                                                                                 onclick : {
                                                                                    fn : instance.onMenuItemClick,
                                                                                    scope : instance
                                                                                 }
                                                                              });
                                                                        instance.widgets.variantPicker.set("label",
                                                                              items[i].name);
                                                                     }

                                                                  }
                                                               }
                                                            }

                                                            instance.widgets.variantPicker.getMenu()
                                                                  .addItems(menuItems);

                                                            Alfresco.util.PopupManager.displayMessage({
                                                               text : instance.msg("message.edit-variant.success")
                                                            });

                                                         },
                                                         scope : instance
                                                      }
                                                   });

                                          },
                                          scope : this
                                       },
                                       onFailure : {
                                          fn : function(response) {
                                             Alfresco.util.PopupManager.displayMessage({
                                                text : instance.msg("message.edit-variant.failure")
                                             });
                                          },
                                          scope : this
                                       }
                                    }).show();
                     }
                  },

                  onDeleteVariant : function VariantPicker_onCreateVariant() {
                     var instance = this, nodeRefs = [ instance.currentVariantNodeRef ];
                     if (instance.currentVariantNodeRef) {
                        var fnActionDeleteConfirm = function(items) {

                           Alfresco.util.Ajax
                                 .jsonRequest({
                                    url : Alfresco.constants.PROXY_URI + "slingshot/datalists/action/items?alf_method=delete",
                                    method : "POST",
                                    dataObj : {
                                       nodeRefs : items
                                    },
                                    successCallback : {
                                       fn : function(response) {

                                          var menuItems = instance.widgets.variantPicker.getMenu().getItems();

                                          for ( var i = 0, il = items.length; i < il; i++) {
                                             for ( var index in menuItems) {
                                                if (menuItems.hasOwnProperty(index)) {
                                                   if (menuItems[index].value === items[i]) {
                                                      instance.widgets.variantPicker.getMenu().removeItem(
                                                            menuItems[index]);
                                                      break;
                                                   }
                                                }
                                                instance.widgets.variantPicker.set("label", instance
                                                      .msg("picker.variant.choose"));

                                                Dom.setStyle(instance.widgets.createVariantButton, 'display', '');
                                                Dom.setStyle(instance.widgets.editVariantButton, 'display', 'none');
                                                Dom.setStyle(instance.widgets.deleteVariantButton, 'display', 'none');
                                             }
                                          }

                                          Alfresco.util.PopupManager.displayMessage({
                                             text : instance.msg("message.delete-variant.success")
                                          });

                                       },
                                       scope : instance
                                    }
                                 });
                        };

                        Alfresco.util.PopupManager.displayPrompt({
                           title : instance.msg("message.confirm.delete.title", nodeRefs.length),
                           text : instance.msg("message.confirm.delete.description", nodeRefs.length),
                           buttons : [ {
                              text : instance.msg("button.delete"),
                              handler : function() {
                                 this.destroy();
                                 fnActionDeleteConfirm.call(instance, nodeRefs);
                              }
                           }, {
                              text : instance.msg("button.cancel"),
                              handler : function() {
                                 this.destroy();
                              },
                              isDefault : true
                           } ]
                        });
                     }
                  }

               }, true);

})();
