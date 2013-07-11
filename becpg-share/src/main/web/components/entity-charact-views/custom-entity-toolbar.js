(function() {
   if (beCPG.component.EntityDataListToolbar) {

      YAHOO.Bubbling
            .fire(
                  "registerToolbarButtonAction",
                  {
                     actionName : "eco-calculate-wused",
                     evaluate : function(asset, entity) {
                        return asset.name !== null && (asset.name === "replacementList" || asset.name === "wUsedList") && entity != null && entity.userAccess.edit;
                     },
                     fn : function(instance) {

                        Alfresco.util.PopupManager.displayMessage({
                           text : this.msg("message.eco-calculate-wused.please-wait")
                        });

                        Alfresco.util.Ajax.request({
                           method : Alfresco.util.Ajax.GET,
                           url : Alfresco.constants.PROXY_URI + "becpg/ecm/changeorder/" + this.options.entityNodeRef
                                 .replace(":/", "") + "/calculatewused",
                           successCallback : {
                              fn : function EntityDataListthis_onECOCalculateWUsed_success(response) {
                                 Alfresco.util.PopupManager.displayMessage({
                                    text : this.msg("message.eco-calculate-wused.success")
                                 });

                              },
                              scope : this
                           },
                           failureCallback : {
                              fn : function EntityDataListthis_onECOCalculateWUsed_failure(response) {
                                 if (response.message !== null) {
                                    Alfresco.util.PopupManager.displayPrompt({
                                       text : response.message
                                    });
                                 } else {
                                    Alfresco.util.PopupManager.displayMessage({
                                       text : this.msg("message.eco-calculate-wused.failure")
                                    });
                                 }
                              },
                              scope : this
                           }
                        });
                     }
                  });

      YAHOO.Bubbling
            .fire(
                  "registerToolbarButtonAction",
                  {
                     actionName : "eco-do-simulation",
                     evaluate : function(asset, entity) {
                        return asset.name !== null && (asset.name === "replacementList" || asset.name === "calculatedCharactList") && entity != null && entity.userAccess.edit;
                     },
                     fn : function(instance) {

                        Alfresco.util.PopupManager.displayMessage({
                           text : this.msg("message.eco-do-simulation.please-wait")
                        });

                        Alfresco.util.Ajax.request({
                           method : Alfresco.util.Ajax.GET,
                           url : Alfresco.constants.PROXY_URI + "becpg/ecm/changeorder/" + this.options.entityNodeRef
                                 .replace(":/", "") + "/dosimulation",
                           successCallback : {
                              fn : function EntityDataListthis_onECODoSimulation_success(response) {
                                 Alfresco.util.PopupManager.displayMessage({
                                    text : this.msg("message.eco-do-simulation.success")
                                 });

                              },
                              scope : this
                           },
                           failureCallback : {
                              fn : function EntityDataListthis_onECODoSimulation_failure(response) {
                                 if (response.message !== null) {
                                    Alfresco.util.PopupManager.displayPrompt({
                                       text : response.message
                                    });
                                 } else {
                                    Alfresco.util.PopupManager.displayMessage({
                                       text : this.msg("message.eco-do-simulation.failure")
                                    });
                                 }
                              },
                              scope : this
                           }

                        });
                     }
                  });

      YAHOO.Bubbling
            .fire(
                  "registerToolbarButtonAction",
                  {
                     actionName : "eco-apply",
                     evaluate : function(asset, entity) {
                        return asset.name !== null && (asset.name === "replacementList") && entity != null && entity.userAccess.edit;
                     },
                     fn : function(instance) {

                        Alfresco.util.PopupManager.displayMessage({
                           text : this.msg("message.eco-apply.please-wait")
                        });

                        Alfresco.util.Ajax.request({
                           method : Alfresco.util.Ajax.GET,
                           url : Alfresco.constants.PROXY_URI + "becpg/ecm/changeorder/" + this.options.entityNodeRef
                                 .replace(":/", "") + "/apply",
                           successCallback : {
                              fn : function EntityDataListthis_onECOApply_success(response) {
                                 Alfresco.util.PopupManager.displayMessage({
                                    text : this.msg("message.eco-apply.success")
                                 });

                              },
                              scope : this
                           },
                           failureCallback : {
                              fn : function EntityDataListthis_onECOApply_failure(response) {
                                 if (response.message !== null) {
                                    Alfresco.util.PopupManager.displayPrompt({
                                       text : response.message
                                    });
                                 } else {
                                    Alfresco.util.PopupManager.displayMessage({
                                       text : this.msg("message.eco-apply.failure")
                                    });
                                 }
                              },
                              scope : this
                           }

                        });
                     }
                  });

      YAHOO.Bubbling
            .fire(
                  "registerToolbarButtonAction",
                  {
                     actionName : "full-screen",
                     evaluate : function(asset, entity) {
                        return asset.name !== null && (asset.name === "compoList") && entity != null && entity.userAccess.edit;
                     },
                     fn : function(instance) {

                        var me = this;

                        var onBeforeFormRuntimeInit = function(layer, args) {
                           var formUI = args[1].component, formsRuntime = args[1].runtime;

                           formsRuntime.setAsReusable(true);

                           formUI.buttons.submit.set("label", this.msg("button.add"));

                           Dom.removeClass("full-screen-form", "hidden");

                           formsRuntime.setAJAXSubmit(true, {
                              successCallback : {
                                 fn : function(response) {
                                    YAHOO.Bubbling.fire("dataItemCreated", {
                                       nodeRef : response.json.persistedObject
                                    });
                                    formsRuntime.reset();

                                    var form = Dom.get(formsRuntime.formId);
                                    for ( var j = 0; j < form.elements.length; j++) {
                                       if (Alfresco.util.isVisible(form.elements[j])) {
                                          form.elements[j].focus();
                                          break;
                                       }
                                    }

                                 },
                                 scope : this
                              }
                           });

                        };

                        if (Dom.hasClass("alf-hd", "hidden")) {
                           Dom.removeClass("alf-hd", "hidden");
                           Dom.removeClass("alf-filters", "hidden");
                           Dom.removeClass("alf-ft", "hidden");
                           Dom.removeClass("Share", "full-screen");
                           Dom.addClass("alf-content", "yui-b");
                           if (this.fullScreen) {
                              Dom.setStyle("alf-content", "margin-left", this.fullScreen.marginLeft);
                           }
                           Dom.addClass("full-screen-form", "hidden");

                        } else {
                           Dom.addClass("alf-hd", "hidden");
                           Dom.addClass("alf-ft", "hidden");
                           Dom.addClass("Share", "full-screen");
                           Dom.addClass("alf-ft", "hidden");

                           Dom.addClass("alf-filters", "hidden");
                           Dom.removeClass("alf-content", "yui-b");

                           if (!this.fullScreen) {

                              var destination = this.datalistMeta.nodeRef != null ? this.datalistMeta.nodeRef
                                    : this.options.parentNodeRef;

                              var templateUrl = YAHOO.lang
                                    .substitute(
                                          Alfresco.constants.URL_SERVICECONTEXT + "components/form?formId=full-screen&entityNodeRef={entityNodeRef}&itemKind={itemKind}&itemId={itemId}&destination={destination}&mode={mode}&submitType={submitType}&showCancelButton=true&dataListsName={dataListsName}",
                                          {
                                             itemKind : "type",
                                             itemId : this.datalistMeta.itemType,
                                             destination : destination,
                                             mode : "create",
                                             submitType : "json",
                                             entityNodeRef : this.options.entityNodeRef,
                                             dataListsName : encodeURIComponent(this.datalistMeta.name != null ? this.datalistMeta.name
                                                   : this.options.list)
                                          });

                              YAHOO.Bubbling.on("beforeFormRuntimeInit", onBeforeFormRuntimeInit, this);

                              Alfresco.util.Ajax
                                    .request({
                                       url : templateUrl,
                                       dataObj : {
                                          htmlid : this.id
                                       },
                                       successCallback : {
                                          fn : function(response) {
                                             var containerDiv = Dom.get("full-screen-form");

                                             if (containerDiv.hasChildNodes()) {
                                                while (containerDiv.childNodes.length >= 1) {
                                                   containerDiv.removeChild(containerDiv.firstChild);
                                                }
                                             }

                                             containerDiv.innerHTML = response.serverResponse.responseText;

                                          },
                                          scope : this
                                       },
                                       failureMessage : "Could not load dialog template from '" + this.options.templateUrl + "'.",
                                       scope : this,
                                       execScripts : true
                                    });

                           } else {
                              Dom.removeClass("full-screen-form", "hidden");
                           }

                           me.fullScreen = {
                              marginLeft : Dom.getStyle("alf-content", "margin-left"),
                              lock : false
                           };

                           YAHOO.Bubbling.on("dirtyDataTable", function() {
                              if (!me.fullScreen.lock) {
                                 me.fullScreen.lock = true;
                                 Alfresco.util.Ajax
                                 .request({
                                    method : Alfresco.util.Ajax.GET,
                                    url : Alfresco.constants.PROXY_URI + "becpg/product/formulate/node/" + me.options.entityNodeRef
                                          .replace(":/", "")+"?fast=true",
                                    successCallback : {
                                       fn : function(response) {
                                          YAHOO.Bubbling.fire("refreshDataGrids", {
                                             updateOnly : true,
                                             callback : function (){
                                                me.fullScreen.lock = false;
                                             }
                                          });
                                       },
                                       scope : this
                                    }
                                 });
                              }
                           }, this);


                           Dom.setStyle("alf-content", "margin-left", null);

                        }

                     }
                  });

      YAHOO.Bubbling
            .fire(
                  "registerToolbarButtonAction",
                  {
                     actionName : "formulate",
                     evaluate : function(asset, entity) {
                        return asset.name !== null && (asset.name === "compoList" || asset.name === "processList" || asset.name === "packagingList") && entity != null && entity.userAccess.edit;
                     },
                     fn : function(instance) {

                        Alfresco.util.PopupManager.displayMessage({
                           text : this.msg("message.formulate.please-wait")
                        });

                        Alfresco.util.Ajax
                              .request({
                                 method : Alfresco.util.Ajax.GET,
                                 url : Alfresco.constants.PROXY_URI + "becpg/product/formulate/node/" + this.options.entityNodeRef
                                       .replace(":/", ""),
                                 successCallback : {
                                    fn : function(response) {
                                       Alfresco.util.PopupManager.displayMessage({
                                          text : this.msg("message.formulate.success")
                                       });

                                       YAHOO.Bubbling.fire("refreshDataGrids");

                                    },
                                    scope : this
                                 },
                                 failureCallback : {
                                    fn : function(response) {
                                       if (response.message !== null) {
                                          Alfresco.util.PopupManager.displayPrompt({
                                             text : response.message
                                          });
                                       } else {
                                          Alfresco.util.PopupManager.displayMessage({
                                             text : this.msg("message.formulate.failure")
                                          });
                                       }
                                    },
                                    scope : this
                                 }

                              });
                     }
                  });

      YAHOO.Bubbling.fire("registerToolbarButtonAction", {
         actionName : "import",
         evaluate : function(asset, entity) {
            return asset.name !== null && (asset.name === "compoList") && entity != null && entity.userAccess.edit;
         },
         fn : function(instance) {
            var actionUrl = Alfresco.constants.PROXY_URI + "becpg/remote/import";

            var doSetupFormsValidation = function FormulationView_onActionEntityImport_doSetupFormsValidation(form) {

               // TODO form.addValidation(this.modules.entityImporter.id +
               // "-entities-field", Alfresco.forms.validation.mandatory, null,
               // "blur");
               // form.setShowSubmitStateDynamically(true, false);
            };

            // Always create a new instance
            this.modules.entityImporter = new Alfresco.module.SimpleDialog(this.id + "-entityImporter").setOptions({
               width : "33em",
               templateUrl : Alfresco.constants.URL_SERVICECONTEXT + "modules/entity-importer/entity-importer",
               actionUrl : actionUrl,
               validateOnSubmit : false,
               doSetupFormsValidation : {
                  fn : doSetupFormsValidation,
                  scope : this
               },
               firstFocus : this.id + "-entityImporter-supplier-field",
               doBeforeFormSubmit : {
                  fn : function FormulationView_onActionEntityImport_doBeforeFormSubmit(form) {

                     Alfresco.util.PopupManager.displayMessage({
                        text : this.msg("message.import.please-wait")
                     });

                  },
                  scope : this
               },
               onSuccess : {
                  fn : function FormulationView_onActionEntityImport_success(response) {
                     Alfresco.util.PopupManager.displayMessage({
                        text : this.msg("message.import.success")
                     });
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
         }
      });

      var refreshReports = function(scope, url) {
         Alfresco.util.PopupManager.displayMessage({
            text : scope.msg("message.generate-report.please-wait")
         });

         Alfresco.util.Ajax.request({
            method : Alfresco.util.Ajax.GET,
            url : Alfresco.constants.PROXY_URI + "becpg/entity/generate-report/node/" + scope.options.entityNodeRef
                  .replace(":/", "") + "/check-datalists",
            successCallback : {
               fn : function EntityDataListthis_onFinish_success(response) {
                  Alfresco.util.PopupManager.displayMessage({
                     text : scope.msg("message.generate-report.success")
                  });
                  window.location = url;
               },
               scope : scope
            },
            failureCallback : {
               fn : function EntityDataListthis_onFinish_failure(response) {
                  Alfresco.util.PopupManager.displayMessage({
                     text : scope.msg("message.generate-report.failure")
                  });
               },
               scope : scope
            }
         });

      };

      YAHOO.Bubbling
            .fire(
                  "registerToolbarButtonAction",
                  {
                     actionName : "variant-picker",
                     right : true,
                     evaluate : function(asset, entity) {

                        return entity != null && entity.userAccess.edit && (beCPG.util.contains(entity.aspects,
                              "bcpg:entityVariantAspect") || (asset.name !== null && (asset.name === "compoList" || asset.name === "processList" || asset.name === "packagingList")));

                     },
                     createWidget : function(containerDiv, instance) {

                        var divEl = document.createElement("div");

                        containerDiv.appendChild(divEl);

                        Dom.setAttribute(divEl, "id", instance.id + "-variantPicker");

                        Dom.addClass(divEl, "variantPicker");

                        var picker = new beCPG.component.VariantPicker(instance.id + "-variantPicker").setOptions({
                           entityNodeRef : instance.options.entityNodeRef,
                           entity : instance.entity,
                           containerDiv : divEl,
                           toolBarInstance : instance
                        });

                        return picker;
                     }
                  });

      YAHOO.Bubbling.fire("registerToolbarButtonAction", {
         actionName : "view-details",
         right : true,
         evaluate : function(datalistMeta, entity) {
            return entity != null && entity.type != "bcpg:systemEntity";
         },
         fn : function(instance) {

            var url = beCPG.util.entityDetailsURL(this.options.siteId, this.options.entityNodeRef, this.entity.type);

            if (beCPG.util.isEntity(this.entity)) {
               refreshReports(this, url);
            } else {
               window.location.href = url;
            }
         }

      });

      YAHOO.Bubbling.fire("registerToolbarButtonAction", {
         actionName : "view-documents",
         right : true,
         evaluate : function(datalistMeta, entity) {
            return entity != null && entity.type != "bcpg:systemEntity";
         },
         fn : function(instance) {

            var url = beCPG.util.entityDocumentsURL(this.options.siteId, this.entity.path, this.entity.name);

            if (beCPG.util.isEntity(this.entity)) {
               refreshReports(this, url);
            } else {
               window.location.href = url;
            }

         }

      });

      YAHOO.Bubbling.fire("registerToolbarButtonAction", {
         right : true,
         actionName : "datalist-state",
         evaluate : function(asset, entity) {
            return entity != null && asset.state !== null;
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
   }

})();