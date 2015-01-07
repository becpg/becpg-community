/***********************************************************************************************************************
 * Copyright (C) 2010-2014 beCPG. This file is part of beCPG beCPG is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version. beCPG is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU Lesser General Public License for more details. You should have received a copy of the GNU
 * Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
(function() {

   /**
    * RapidLinkToolbar constructor.
    * 
    * @param htmlId
    *            {String} The HTML id of the parent element
    * @return {beCPG.component.RapidLinkToolbar} The new RapidLinkToolbar instance
    * @constructor
    */
   beCPG.component.RapidLinkToolbar = function(htmlId) {

      beCPG.component.RapidLinkToolbar.superclass.constructor.call(this, "beCPG.component.RapidLinkToolbar", htmlId, [
            "button", "container" ]);

      // message
      this.name = "beCPG.component.EntityDataListToolbar";

      return this;
   };

   /**
    * Extend from Alfresco.component.Base
    */
   YAHOO.extend(beCPG.component.RapidLinkToolbar, Alfresco.component.Base);

   /**
    * Augment prototype with main class implementation, ensuring overwrite is enabled
    */
   YAHOO.lang
         .augmentObject(
               beCPG.component.RapidLinkToolbar.prototype,
               {
                  /**
                   * Object container for initialization options
                   * 
                   * @property options
                   * @type object
                   */
                  options : {

                     entity : null,

                     dataListNodeRef : null,

                     containerDiv : null,

                     siteId : null,

                     list : null,

                     formWidth : "34em"

                  },

                  currentVariantNodeRef : null,

                  /**
                   * Fired by YUI when parent element is available for scripting.
                   * 
                   * @method onReady
                   */
                  onReady : function RapidLinkToolbar_onReady() {

                     var rapidLinkPickerMenu = [], instance = this, createList = null;

                     if ("packagingList" == this.options.list) {
                        createList = [ "packagingMaterial", "packagingKit" ];
                     } else if ("processList" == this.options.list) {
                        createList = [ "resourceProduct" ];
                     } else {
                        createList = [ "finishedProduct", "rawMaterial", "localSemiFinishedProduct",
                              "semiFinishedProduct" ];
                     }

                     
                     for(key in createList){
                    	 var type = createList[key];
                    	 rapidLinkPickerMenu.push({
                             text : '<span class="' + type + '" title="' + instance
                                   .msg("action.rapid-link.create." + type + ".description") + '" >' + instance
                                   .msg("action.rapid-link.create." + type) + '</span>',
                             onclick : {
                                fn : instance.onMenuCreateTypeClick(type),
                                scope : instance
                             }
                          });
                    	 
                     }
                     
                     if ("compoList" == this.options.list) {

                        rapidLinkPickerMenu
                              .push({
                                 text : '<span class="import" title="' + this
                                       .msg("action.rapid-link.import.description") + '" >' + this
                                       .msg("action.rapid-link.import") + '</span>',
                                 onclick : {
                                    fn : this.onClickImport,
                                    scope : this
                                 }
                              });
                     }

                     rapidLinkPickerMenu.push({
                        text : '<span class="simulation" title="' + this
                              .msg("action.rapid-link.simulation.description") + '" >' + this
                              .msg("action.rapid-link.simulation") + '</span>',
                        onclick : {
                           fn : this.onClickCreateSimulation,
                           scope : this
                        }
                     });

                     this.widgets.rapidLinkToolbar = new YAHOO.widget.Button({
                        type : "split",
                        label : this.msg("button.rapid-link"),
                        name : "mymenubutton",
                        menu : rapidLinkPickerMenu,
                        container : this.options.containerDiv,
                        lazyloadmenu : false
                     });

                  },
                  onMenuCreateTypeClick : function RapidLinkToolbar_onMenuCreateTypeClick(type) {
                     var instance = this;

                     return function(p_sType, p_aArgs, p_oItem) {

                        var templateUrl = YAHOO.lang
                              .substitute(
                                    Alfresco.constants.URL_SERVICECONTEXT + "components/form?formId=formulation&itemKind=type&itemId={itemId}&destination={destination}&mode=create&submitType=json&showCancelButton=true&popup=true",
                                    {
                                       itemId : "bcpg:" + type,
                                       destination : instance.options.entity.parentNodeRef,
                                    });

                        var createRow = new Alfresco.module.SimpleDialog(instance.id + "-createType");

                        createRow.setOptions(
                              {
                                 width : instance.options.formWidth,
                                 templateUrl : templateUrl,
                                 actionUrl : null,
                                 destroyOnHide : true,
                                 doBeforeDialogShow : {
                                    fn : function(p_form, p_dialog) {
                                       Alfresco.util.populateHTML([ p_dialog.id + "-dialogTitle",
                                             instance.msg("action.rapid-link.create." + type) ]);
                                    },
                                    scope : this
                                 },
                                 doBeforeFormSubmit : {
                                    fn : function(form) {
                                       Alfresco.util.PopupManager.displayMessage({
                                          text : this.msg("message.rapid-link.create.please-wait")
                                       });
                                    },
                                    scope : this
                                 },
                                 onSuccess : {
                                    fn : function(response) {
                                       if (response.json) {
                                          instance.addToDataList(response.json.persistedObject,
                                                "message.rapid-link.create.success");
                                       }
                                    },
                                    scope : this
                                 },
                                 onFailure : {
                                    fn : function EntityDataGrid_onActionCreate_failure(response) {
                                       Alfresco.util.PopupManager.displayMessage({
                                          text : instance.msg("message.rapid-link.create.failure")
                                       });
                                    },
                                    scope : this
                                 }
                              }).show();

                     };
                  },

                  onClickImport : function RapidLinkToolbar_onClickImport(p_sType, p_aArgs, p_oItem) {
                     this.modules.entityImporter = new Alfresco.module.SimpleDialog(this.id + "-entityImporter")
                           .setOptions({
                              width : this.options.formWidth,
                              templateUrl : Alfresco.constants.URL_SERVICECONTEXT + "modules/entity-importer/entity-importer",
                              actionUrl : Alfresco.constants.PROXY_URI + "becpg/remote/import?destination=" + this.options.entity.parentNodeRef,
                              validateOnSubmit : false,
                              firstFocus : this.id + "-entityImporter-supplier-field",
                              doBeforeFormSubmit : {
                                 fn : function FormulationView_onActionEntityImport_doBeforeFormSubmit(form) {
                                    Alfresco.util.PopupManager.displayMessage({
                                       text : this.msg("message.rapid-link.import.please-wait")
                                    });
                                 },
                                 scope : this
                              },
                              onSuccess : {
                                 fn : function FormulationView_onActionEntityImport_success(response) {
                                    if (response.json) {
                                       this.addToDataList(response.json[0], "message.rapid-link.import.success");
                                    }
                                 },
                                 scope : this
                              },
                              onFailure : {
                                 fn : function FormulationView_onActionEntityImport_failure(response) {
                                    Alfresco.util.PopupManager.displayMessage({
                                       text : this.msg("message.import.failure")
                                    });
                                 },
                                 scope : this
                              }
                           });
                     this.modules.entityImporter.show();
                  },

                  onClickCreateSimulation : function RapidLinkToolbar_onClickCreateSimulation(p_sType, p_aArgs, p_oItem) {
                     Alfresco.util.PopupManager.displayMessage({
                        text : this.msg("message.rapid-link.simulation.please-wait")
                     });

                     Alfresco.util.Ajax
                           .request({
                              method : Alfresco.util.Ajax.POST,
                              url : Alfresco.constants.PROXY_URI + "becpg/entity/simulation/create?entityNodeRef=" + this.options.entity.nodeRef,
                              successCallback : {
                                 fn : function(resp) {
                                    if (resp.json) {
                                       window.location.href = beCPG.util.entityCharactURL(this.options.siteId,
                                             resp.json.persistedObject, this.options.entity.type);
                                    }
                                 },
                                 scope : this
                              },
                              failureCallback : {
                                 fn : function(resp) {
                                    Alfresco.util.PopupManager.displayMessage({
                                       text : this.msg("message.rapid-link.simulation.failure")
                                    });
                                 },
                                 scope : this
                              }
                           });
                  },
                  addToDataList : function RapidLinkToolbar_addToDataList(nodeRef, msgKey) {

                     var instance = this, dataObj = null,
                         listType = "bcpg:"+this.options.list;
                     
                     if ("packagingList" == this.options.list) {
                        dataObj = {
                              "alf_destination" : instance.options.dataListNodeRef,
                              "assoc_bcpg_packagingListProduct_added" : nodeRef,
                              "prop_bcpg_packagingListUnit" : "P",
                              "prop_bcpg_packagingListQty" : "0"
                           };
                     } else if ("processList" == this.options.list) {
                        dataObj = {
                              "alf_destination" : instance.options.dataListNodeRef,
                              "assoc_mpm_plResource_added" : nodeRef
                           };
                        listType = "mpm:processList";
                     } else {
                        dataObj = {
                              "alf_destination" : instance.options.dataListNodeRef,
                              "assoc_bcpg_compoListProduct_added" : nodeRef,
                              "prop_bcpg_compoListUnit" : "kg",
                              "prop_bcpg_compoListQtySubFormula" : "0"
                           };
                     }
                     
                        Alfresco.util.Ajax.jsonRequest({
                           method : Alfresco.util.Ajax.POST,
                           url : Alfresco.constants.PROXY_URI + "api/type/"+listType+"/formprocessor",
                           dataObj : dataObj,
                           successCallback : {
                              fn : function(resp) {
                                 if (resp.json) {
                                    YAHOO.Bubbling.fire("dataItemCreated", {
                                       nodeRef : resp.json.persistedObject,
                                       callback : function(item) {
                                          Alfresco.util.PopupManager.displayMessage({
                                             text : instance.msg(msgKey)
                                          });
                                       }
                                    });
                                 }
                              },
                              scope : this
                           }
                        });
                     }

               }, true);

})();
