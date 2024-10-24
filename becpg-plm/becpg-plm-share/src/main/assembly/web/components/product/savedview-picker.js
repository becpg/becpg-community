/*******************************************************************************
 *  Copyright (C) 2010-2021 beCPG. 
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
    * SavedViewPicker constructor.
    * 
    * @param htmlId
    *            {String} The HTML id of the parent element
    * @return {beCPG.component.SavedViewPicker} The new SavedViewPicker instance
    * @constructor
    */
   beCPG.component.SavedViewPicker = function(htmlId) {

      beCPG.component.SavedViewPicker.superclass.constructor.call(this, "beCPG.component.SavedViewPicker", htmlId, [
            "button", "container" ]);

      // message
      this.name = "beCPG.component.EntityDataListToolbar";

      return this;
   };

   /**
    * Extend from Alfresco.component.Base
    */
   YAHOO.extend(beCPG.component.SavedViewPicker, Alfresco.component.Base);

   /**
    * Augment prototype with main class implementation, ensuring overwrite is enabled
    */
   YAHOO.lang
         .augmentObject(
               beCPG.component.SavedViewPicker.prototype,
               {
                  /**
                   * Object container for initialization options
                   * 
                   * @property options
                   * @type object
                   */
                  options : {

                     containerDiv : null,

                     toolBarInstance : null,
                     
                     destNodeRef : null,

                     formWidth : "34em",
                     
                     edit: false

                  },

                  currentSavedViewId : null,
                  /**
                   * Fired by YUI when parent element is available for scripting.
                   * 
                   * @method onReady
                   */
                  onReady : function SavedViewPicker_onReady() {

                     var entitySavedViewsMenu = [

                     {
                        text : '<span class="saved-view-all">' + this.msg("picker.saved-view.none") + '</span>',
                        value : "none",
                        onclick : {
                           fn : this.onMenuItemClick,
                           scope : this
                        }
                     }

                     ];

                     for ( var i in this.options.entity.savedViews) {

                        var savedViewLbl = '<span class="saved-view' + (this.options.savedViews[i].isDefaultSavedView ? '-default'
                              : '') + '">' + this.options.savedViews[i].name + '</span>';

                        entitySavedViewsMenu.push({
                           text : savedViewLbl,
                           value : this.options.entity.savedViews[i].nodeRef,
                           onclick : {
                              fn : this.onMenuItemClick,
                              scope : this
                           }
                        });
                     }

                     this.widgets.SavedViewPicker = new YAHOO.widget.Button({
                        type : "split",
                        label : this.msg("picker.saved-view.choose"),
                        name : "mymenubutton",
                        menu : entitySavedViewsMenu,
                        container : this.options.containerDiv,
                        lazyloadmenu : false
                     });


                     this.widgets.createSavedViewButton = this.createYUIButton(this, "create-saved-view",
                           this.onCreateSavedView);

                     this.widgets.editSavedViewButton = this.createYUIButton(this, "edit-saved-view", this.onEditSavedView);

                     this.widgets.duplicateSavedViewButton = this.createYUIButton(this, "duplicate-saved-view", this.onDuplicateSavedView);
                     
                     this.widgets.deleteSavedViewButton = this.createYUIButton(this, "delete-saved-view",
                           this.onDeleteSavedView);
                           
					if(!this.options.edit){
						Dom.setStyle(this.widgets.createSavedViewButton, 'display', 'none');
					}     

                     Dom.setStyle(this.widgets.editSavedViewButton, 'display', 'none');
                     Dom.setStyle(this.widgets.duplicateSavedViewButton, 'display', 'none');
                     Dom.setStyle(this.widgets.deleteSavedViewButton, 'display', 'none');

                  },

                  createYUIButton : function(instance, actionName, fn) {

                     var template = Dom.get("custom-toolBar-template-button"), buttonWidget = null;

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
                        this.widgets.SavedViewPicker.set("label", sText);

                        if ("none" === value ) {

                          YAHOO.Bubbling.fire("changeFilter", {
                              filterOwner : this.id,
                              filterId : "all"
                           });
                         
                        } else {
                            
                            var currentSavedView = null;
                            
                            this.currentSavedViewId = p_oItem.value

                            for (var i = 0; i < this.options.entity.savedViews.length; i++) {
                              var savedView = this.options.entity.savedViews[i];
                                if (savedView.currentSavedViewId == value) {
                                    currentSavedView = savedView;
                                    break;
                                }
                            }
                            
                            
                           YAHOO.Bubbling
                                 .fire("changeFilter", currentSavedView.filter);

                           Dom.setStyle(this.widgets.createSavedViewButton, 'display', 'none');

							if (this.options.edit) {
								
								Dom.setStyle(this.widgets.duplicateSavedViewButton, 'display', '');
								
								var isProtected = currentSavedView.isModelSavedView ;
								
								if (isProtected) {
									Dom.setStyle(this.widgets.deleteSavedViewButton, 'display', 'none');
									Dom.setStyle(this.widgets.editSavedViewButton, 'display', 'none');
								} else {
									Dom.setStyle(this.widgets.deleteSavedViewButton, 'display', '');
									Dom.setStyle(this.widgets.editSavedViewButton, 'display', '');
								}
							}
                        }
                     }
                  },

                  onCreateSavedView : function SavedViewPicker_onCreateSavedView() {

                     var instance = this;

                     var doBeforeDialogShow = function(p_form, p_dialog) {
                        Alfresco.util.populateHTML([ p_dialog.id + "-dialogTitle",
                              instance.msg("label.new-saved-view.title") ]);
                     };

                     var templateUrl = YAHOO.lang
                           .substitute(
                                 Alfresco.constants.URL_SERVICECONTEXT + "components/form?itemKind={itemKind}&itemId={itemId}&destination={destination}&association={association}&mode={mode}&submitType={submitType}&showCancelButton=true&popup=true",
                                 {
                                    itemKind : "type",
                                    itemId : "cm:content",
                                    formId : "saved-view",
                                    destination : instance.options.destNodeRef,
                                    mode : "create",
                                    submitType : "json"
                                 });

                     var createRow = new Alfresco.module.SimpleDialog(instance.id + "-createSavedView");

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

                                       instance.widgets.SavedViewPicker.getMenu().addItems(menuItems);

                                       Alfresco.util.PopupManager.displayMessage({
                                          text : instance.msg("message.create-saved-view.success")
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
                                 text : instance.msg("message.create-saved-view.failure")
                              });
                           },
                           scope : this
                        }
                     }).show();

                  },

                  onDuplicateSavedView : function SavedViewPicker_onDuplicateSavedView() {
               
                      var instance = this;

                      var doBeforeDialogShow = function(p_form, p_dialog) {
                         Alfresco.util.populateHTML([ p_dialog.id + "-dialogTitle",
                               instance.msg("label.duplicate-saved-view.title") ]);
                      };

                      var templateUrl = YAHOO.lang
                            .substitute(
                                  Alfresco.constants.URL_SERVICECONTEXT + "components/form?itemKind={itemKind}&formId=duplicate&itemId={itemId}&destination={destination}&association={association}&mode={mode}&submitType={submitType}&showCancelButton=true&popup=true",
                                  {
                                     itemKind : "type",
                                     itemId : "bcpg:saved-view",
                                     destination : instance.options.entityNodeRef,
                                     association : "bcpg:saved-views",
                                     mode : "create",
                                     submitType : "json"
                                  });

                      var createRow = new Alfresco.module.SimpleDialog(instance.id + "-createSavedView");

                      createRow.setOptions({
                         width : instance.options.formWidth,
                         templateUrl : templateUrl,
                         actionUrl : Alfresco.constants.PROXY_URI + "/becpg/saved-view/duplicate?nodeRef="+instance.currentSavedViewNodeRef,
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

                                        instance.widgets.SavedViewPicker.getMenu().addItems(menuItems);

                                        YAHOO.Bubbling.fire("changeFilter", {
                                            filterOwner : instance.id,
                                            filterId : "all"
                                         });
                                        
                                        Alfresco.util.PopupManager.displayMessage({
                                            text : instance.msg("message.duplicate-saved-view.success")
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
                                  text : instance.msg("message.duplicate-saved-view.failure")
                               });
                            },
                            scope : this
                         }
                      }).show();

                  },
                  
                  onEditSavedView : function SavedViewPicker_onCreateSavedView() {

                     var instance = this;

                     if (instance.currentSavedViewNodeRef) {

                        var doBeforeDialogShow = function(p_form, p_dialog) {
                           Alfresco.util.populateHTML([ p_dialog.id + "-dialogTitle",
                                 instance.msg("label.edit-saved-view.title") ]);
                        };

                        var templateUrl = YAHOO.lang
                              .substitute(
                                    Alfresco.constants.URL_SERVICECONTEXT + "components/form?itemKind={itemKind}&itemId={itemId}&mode={mode}&submitType={submitType}&showCancelButton=true&popup=true",
                                    {
                                       itemKind : "node",
                                       itemId : instance.currentSavedViewNodeRef,
                                       mode : "edit",
                                       submitType : "json"
                                    });

                        var editRow = new Alfresco.module.SimpleDialog(instance.id + "-editSavedView");

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
                                                            var items = res.json.data.items, menuItems = instance.widgets.SavedViewPicker
                                                                  .getMenu().getItems();

                                                            for ( var i = 0, il = items.length; i < il; i++) {

                                                               for ( var index in menuItems) {
                                                                  if (menuItems.hasOwnProperty(index)) {
                                                                     if (menuItems[index].value === items[i].nodeRef) {
                                                                        instance.widgets.SavedViewPicker.getMenu()
                                                                              .removeItem(menuItems[index]);
                                                                        instance.widgets.SavedViewPicker.getMenu()
                                                                              .addItem({
                                                                                 text : items[i].name,
                                                                                 value : items[i].nodeRef,
                                                                                 onclick : {
                                                                                    fn : instance.onMenuItemClick,
                                                                                    scope : instance
                                                                                 }
                                                                              });
                                                                        instance.widgets.SavedViewPicker.set("label",
                                                                              items[i].name);
                                                                     }

                                                                  }
                                                               }
                                                            }

                                                            instance.widgets.SavedViewPicker.getMenu()
                                                                  .addItems(menuItems);

                                                            Alfresco.util.PopupManager.displayMessage({
                                                               text : instance.msg("message.edit-saved-view.success")
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
                                                text : instance.msg("message.edit-saved-view.failure")
                                             });
                                          },
                                          scope : this
                                       }
                                    }).show();
                     }
                  },

                  onDeleteSavedView : function SavedViewPicker_onCreateSavedView() {
                     var instance = this, nodeRefs = [ instance.currentSavedViewNodeRef ];
                     if (instance.currentSavedViewNodeRef) {
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

                                          var menuItems = instance.widgets.SavedViewPicker.getMenu().getItems();

                                          for ( var i = 0, il = items.length; i < il; i++) {
                                             for ( var index in menuItems) {
                                                if (menuItems.hasOwnProperty(index)) {
                                                   if (menuItems[index].value === items[i]) {
                                                      instance.widgets.SavedViewPicker.getMenu().removeItem(
                                                            menuItems[index]);
                                                      break;
                                                   }
                                                }
                                                instance.widgets.SavedViewPicker.set("label", instance
                                                      .msg("picker.saved-view.choose"));

                                                Dom.setStyle(instance.widgets.createSavedViewButton, 'display', '');
                                                Dom.setStyle(instance.widgets.editSavedViewButton, 'display', 'none');
                                                Dom.setStyle(this.widgets.duplicateSavedViewButton, 'display', 'none');
                                                Dom.setStyle(instance.widgets.deleteSavedViewButton, 'display', 'none');
                                             }
                                          }

                                          Alfresco.util.PopupManager.displayMessage({
                                             text : instance.msg("message.delete-saved-view.success")
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
