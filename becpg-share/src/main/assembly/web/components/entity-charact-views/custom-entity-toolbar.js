/*******************************************************************************
 *  Copyright (C) 2010-2026 beCPG. 
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


	   
	   YAHOO.Bubbling
            .fire(
                  "registerToolbarButtonAction",
                  {
                     actionName : "export-csv",
                     right : true,
                     evaluate : function(asset, entity) {
                        return asset.name !== null && !asset.name.indexOf("View-properties") == 0 && !asset.name.indexOf("View-reports") == 0
                        && !asset.name.indexOf("View-documents") == 0 ;
                     },
                     fn : function(instance) {

                        var dt = Alfresco.util.ComponentManager.find({
                           name : "beCPG.module.EntityDataGrid"
                        })[0];
                        
                        YAHOO.Bubbling.fire("refreshDataGrids",{ clearCache :true,
			            	    		  cacheTimeStamp : (new Date()).getTime() });
                        
	                     Alfresco.util.Ajax
	                     .jsonGet({
	                        url : dt._getColumnUrl("export"),
	                        successCallback : {
	                           fn : function(response) {
	
	                              var requestParams = {
	                                 fields : [],
	                                 labels : [],
	                                 filter : dt.currentFilter,
	                                 page : 1,
	                                 extraParams : dt.options.extraParams
	                              };
	                              
	                              requestParams.filter.filterParams = dt._createFilterURLParameters(dt.currentFilter, dt.options.filterParameters);
	
	                              for ( var i = 0, ii = response.json.columns.length; i < ii; i++) {
		  							 var column = response.json.columns[i], columnName = column.name.replace(":", "_"), columnLabel = (column.label!="hidden"? column.label :"");
	                                 if (Object.keys(column).includes("label") && ["datasource"].indexOf(column.label) < 0) {
		
		                                 if ((column.dataType == "nested" || column.dataType == "nested_column") && column.columns) {
		                                    for ( var j = 0; j < column.columns.length; j++) { 
										    var col = column.columns[j];                             
			 								if (Object.keys(col).includes("label") && ["datasource"].indexOf(col.label) < 0) {                                            
			                                       columnName += "|" + col.name.replace(":", "_");
			                               		   columnLabel += "|" + (col.label!="hidden"?col.label:"");    
		                                       }                                        
		                                    }
		                                 }
		                                 requestParams.fields.push({"id":columnName, "label": columnLabel});
	                                 }
	                              }
	
	                              var MAX_RESULTS_UNLIMITED = -1;
	                              
	                              var name = "export";
	                              if(dt.datalistMeta){
									if(dt.datalistMeta.entityName){
										name  += " - "+dt.datalistMeta.entityName;
									}
									
									if( dt.datalistMeta.title){
									  	name+= " - "+dt.datalistMeta.title;
									} else if( dt.datalistMeta.name){
										name+= " - "+dt.datalistMeta.name;
									}
									
								  }
								  name +=".xlsx"
								
	 							 beCPG.util.launchAsyncDownload(name, name, dt._getDataUrl(MAX_RESULTS_UNLIMITED) + "&format=xlsx" , requestParams);  
	                 
	
	                           },
	                           scope : this
	                        }
	                     });
                   

                     }
                  });

   }
   
   YAHOO.Bubbling.fire("registerToolbarButtonAction", {
       actionName : "entity-edit-metadata",
       evaluate : function(asset, entity) {
           return asset.name !== null && asset.name.indexOf("View-properties") == 0 && entity.userAccess.edit;
       },
       fn : function(instance) {

          window.location.href = Alfresco.util.siteURL("edit-metadata?nodeRef="+this.entity.nodeRef+ (this.options.siteId ? "&siteId=" + this.options.siteId : ""));

       }

    });
   
   
   YAHOO.Bubbling.fire("registerToolbarButtonAction", {
       actionName : "entity-add-aspect",
       evaluate : function(asset, entity) {
           return asset.name !== null && asset.name.indexOf("View-properties") == 0 && (entity.userAccess.edit || entity.aspects.includes("bcpg:archivedEntityAspect"));
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
           return asset.name !== null && asset.name.indexOf("View-reports") == 0 && !entity.aspects.includes("bcpg:archivedEntityAspect");
       },
       fn : function(instance) {
    	   
    	   var refreshReportButton = YAHOO.util.Selector.query('div.entity-refresh-reports'), me = this;

			Dom.addClass(refreshReportButton, "loading");
			
           var msgPopup = Alfresco.util.PopupManager.displayMessage({
               text : this.msg("message.generate-reports.please-wait"),
               spanClass : "wait",
               displayTime : 0
           });

           Alfresco.util.Ajax.request({
              method : Alfresco.util.Ajax.GET,
              url : Alfresco.constants.PROXY_URI + "becpg/entity/generate-report/node/" + this.entity.nodeRef.replace(":/", "")
                    + "/force",
              successCallback : {
                 fn : function EntityDataListToolbar_onFinish_success(response) {
                	 	//#2147 YAHOO.Bubbling.fire("previewChangedEvent");
                	 Dom.removeClass(refreshReportButton, "loading");
                	 window.location.reload();
                 },
                 scope : this
              },
              failureCallback : {
                 fn : function EntityDataListToolbar_onFinish_failure(response) {
                	 msgPopup.destroy();
                	 Dom.removeClass(refreshReportButton, "loading");
                     Alfresco.util.PopupManager.displayMessage({
                         text : this.msg("message.generate-reports.failure")
                     });
                 },
                 scope : this
              }
           });
       }

    });
   
   YAHOO.Bubbling.fire("registerToolbarButtonAction", {
       actionName : "entity-reports-parameters",
       evaluate : function(asset, entity) {
           return asset.name !== null && asset.name.indexOf("View-reports") == 0  && entity.userAccess.edit;
       },
       fn : function(instance) {
    	   var templateUrl = YAHOO.lang
           .substitute(
                 Alfresco.constants.URL_SERVICECONTEXT + "components/form?popup=true&formId=reports-parameters&itemKind=node&itemId={itemId}&mode=edit&submitType=json&showCancelButton=true",
                 {
                    itemId : this.options.entityNodeRef
                 });

	     var editProductMetadata = new Alfresco.module.SimpleDialog(this.id + "-editReportMetadata");
	
	     editProductMetadata.setOptions(
	           {
	              width : "33em",
	              successMessage : this.msg("message.details.success"),
	              failureMessage : this.msg("message.details.failure"),
	              templateUrl : templateUrl,
	              destroyOnHide : true,
	              doBeforeDialogShow : {
	                 fn : function(p_form, p_dialog) {
	                    Alfresco.util.populateHTML([ p_dialog.id + "-dialogTitle",
	                          this.msg("label.entity-reports-parameters.title") ]);
	                 },
	                 scope : this
	              }
	
	           }).show();
	       }

    });
    
    

		YAHOO.Bubbling
			.fire(
				"registerToolbarButtonAction",
				{
					actionName: "formulate",
					hideLabel: true,
					evaluate: function(asset, entity) {
						return asset.name != null &&
							(asset.name != "regulatoryList" && asset.name != "ingRegulatoryList" && asset.name != "View-documents" && asset.name != "View-reports" && asset.name != "activityList" && asset.name != "WUsed")
							&& entity != null && (beCPG.util.contains(entity.aspects,
								"bcpg:productAspect") || entity.type == "bcpg:productSpecification" || entity.type == "qa:batch" || entity.type == "pjt:project" 
								|| entity.type == "bcpg:productCollection" || entity.type == "bcpg:supplier" || entity.type == "bcpg:client") 
								&& entity.userAccess.edit && !entity.aspects.includes("bcpg:archivedEntityAspect");
					},
					fn: function(instance) {


						Alfresco.util.PopupManager.displayMessage({
							text: this.msg("message.formulate.please-wait")
						});

						var formulateButton = YAHOO.util.Selector.query('div.formulate');

						Dom.addClass(formulateButton, "loading");

						Alfresco.util.Ajax
							.request({
								method: Alfresco.util.Ajax.GET,
								responseContentType: Alfresco.util.Ajax.JSON,
								url: Alfresco.constants.PROXY_URI + "becpg/remote/formulate?nodeRef=" + this.options.entityNodeRef+"&format=json",
								successCallback: {
									fn: function(response) {
										Alfresco.util.PopupManager.displayMessage({
											text: this.msg("message.formulate.success")
										});

										YAHOO.Bubbling.fire("refreshDataGrids",{ clearCache :true,
			            	    		  cacheTimeStamp : (new Date()).getTime() });
										Dom.removeClass(formulateButton, "loading");
									},
									scope: this
								},
								failureCallback: {
									fn: function(response) {
										if (response.json && response.json.message) {
											Alfresco.util.PopupManager.displayPrompt({
												title: this.msg("message.formulate.failure"),
												text: response.json.message
											});
										} else {
											Alfresco.util.PopupManager.displayMessage({
												text: this.msg("message.formulate.failure")
											});
										}
										Dom.removeClass(formulateButton, "loading");
									},
									scope: this
								}

							});

					}
				});

    
	YAHOO.Bubbling
		.fire(
			"registerToolbarButtonAction",
			{
				actionName: "formulate-decernis",
				hideLabel: true,
				evaluate: function(asset, entity) {
					return asset.name != null && (asset.name == "regulatoryList" || asset.name == "ingRegulatoryList") && entity != null && (beCPG.util.contains(entity.aspects,
						"bcpg:productAspect")) && entity.userAccess.edit && !entity.aspects.includes("bcpg:archivedEntityAspect");
				},
				fn: function(instance) {

					Alfresco.util.PopupManager.displayMessage({
						text: this.msg("message.formulate.please-wait")
					});

					var formulateButton = YAHOO.util.Selector.query('div.formulate-decernis');

					Dom.addClass(formulateButton, "loading");

					Alfresco.util.Ajax
						.request({
							method: Alfresco.util.Ajax.GET,
							responseContentType: Alfresco.util.Ajax.JSON,
							url: Alfresco.constants.PROXY_URI + "becpg/regulatory/check?nodeRef=" + this.options.entityNodeRef + "&format=json" + "&async=true",
							successCallback: {
								fn: function(response) {
									if (response.json.status) {
										Alfresco.util.PopupManager.displayMessage({
											text: this.msg("message.regulatory." + response.json.status.toLowerCase())
										});
									}

									YAHOO.Bubbling.fire("refreshDataGrids", {
										clearCache: true,
										cacheTimeStamp: (new Date()).getTime()
									});
									Dom.removeClass(formulateButton, "loading");
								},
								scope: this
							},
							failureCallback: {
								fn: function(response) {
									if (response.json && response.json.message) {
										Alfresco.util.PopupManager.displayPrompt({
											title: this.msg("message.formulate.failure"),
											text: response.json.message
										});
									} else {
										Alfresco.util.PopupManager.displayMessage({
											text: this.msg("message.formulate.failure")
										});
									}
									Dom.removeClass(formulateButton, "loading");
								},
								scope: this
							}
						});

				}
			});

	YAHOO.Bubbling
		.fire(
			"registerToolbarButtonAction",
			{
				actionName: "qa-batch-scan",
				hideLabel: true,
				evaluate: function(asset, entity) {
					return asset.name != null && asset.name == "batchAllocationList" && entity != null && entity.type == "qa:batch" && entity.userAccess.edit && !entity.aspects.includes("bcpg:archivedEntityAspect");
				},
				fn: function(instance) {
					var me = this;
					var dialogId = me.id + "-scanDialog";
					
					var showScanDialog = function() {
						var templateUrl = YAHOO.lang
							.substitute(
								Alfresco.constants.URL_SERVICECONTEXT + "components/form?popup=true&formId=scan&itemKind=node&itemId={itemId}&mode=edit&submitType=json&showCancelButton=true&bulkEdit=true",
								{
									itemId: me.options.entityNodeRef
								});

						var scanDialog = new Alfresco.module.SimpleDialog(dialogId);
						scanDialog.setOptions({
							width: "33em",
							templateUrl: templateUrl,
							actionUrl: Alfresco.constants.PROXY_URI + "becpg/quality/scan-batch?nodeRef=" + me.options.entityNodeRef,
							destroyOnHide: true,
							firstFocus: dialogId + "_prop_qa_batchScannerInput",
							doBeforeDialogShow: {
								fn: function(p_form, p_dialog) {
									Alfresco.util.populateHTML([p_dialog.id + "-dialogTitle", me.msg("button.qa-batch-scan")]);
									
									// Explicitly set form action to our dedicated webscript
									var formEl = Dom.get(p_dialog.id + "-form");
									if (formEl) {
										var actionUrl = Alfresco.constants.PROXY_URI + "becpg/quality/scan-batch?nodeRef=" + me.options.entityNodeRef;
										formEl.action = actionUrl;
										formEl.setAttribute("action", actionUrl);
									}

									// Set the dialog submit button label to "Scanner" instead of "Save"
									var okBtn = p_dialog.widgets.okButton;
									if (okBtn) {
										okBtn.set("label", me.msg("button.qa-batch-scan"));
									}

									var inputEl = Dom.get(p_dialog.id + "_prop_qa_batchScannerInput");
									if (inputEl) {
										inputEl.value = "";
										inputEl.focus();
									}
									// Poll to wait for the input element to be rendered in the DOM asynchronously
									var bindListeners = function() {
										var inputEl = Dom.get(p_dialog.id + "_prop_qa_batchScannerInput");
										if (inputEl) {
											inputEl.value = "";
											inputEl.focus();

											var timer = null;

											var appendSeparator = function() {
												var val = inputEl.value.trim();
												if (val && val.indexOf(" - ") === -1) {
													inputEl.value = val + " - ";
												}
											};

											YAHOO.util.Event.addListener(inputEl, "input", function(e) {
												if (timer) {
													clearTimeout(timer);
												}
												timer = setTimeout(function() {
													appendSeparator();
												}, 300);
											});

											YAHOO.util.Event.addListener(inputEl, "keydown", function(e) {
												var charCode = e.keyCode || e.which;
												if (charCode === 13) { // Enter key
													if (timer) {
														clearTimeout(timer);
													}
													var val = inputEl.value.trim();
													if (val && val.indexOf(" - ") === -1) {
														// Prevent form submission
														YAHOO.util.Event.preventDefault(e);
														appendSeparator();
													}
												}
											});
										} else {
											// Try again in 50ms
											setTimeout(bindListeners, 50);
										}
									};

									bindListeners();
									
									var bulkActionCheck = Dom.get(p_dialog.id + "-form-bulkAction");
									if (!bulkActionCheck) {
										var submitBtn = Dom.get(p_dialog.id + "-form-submit");
										if (submitBtn) {
											var parentEl = submitBtn.parentNode;
											var checkContainer = document.createElement("span");
											checkContainer.style.marginRight = "10px";
											checkContainer.innerHTML = '<input id="' + p_dialog.id + '-form-bulkAction" name="-" type="checkbox" checked="checked">&nbsp;<span id="' + p_dialog.id + '-form-bulkAction-msg" style="vertical-align: middle;">' + me.msg("label.qa-batch-scan-multiple") + '</span>';
											parentEl.insertBefore(checkContainer, submitBtn);
										}
									} else {
										bulkActionCheck.checked = true;
										var bulkActionMsg = Dom.get(p_dialog.id + "-form-bulkAction-msg");
										if (bulkActionMsg) {
											bulkActionMsg.innerHTML = me.msg("label.qa-batch-scan-multiple");
										}
									}
								},
								scope: me
							},
							onFailure: {
								fn: function(response) {
									var errorMsgKey = "message.qa-batch-scan.malformed";
									var rawText = "";
									if (response) {
										if (response.json && response.json.message) {
											rawText = response.json.message;
										} else if (response.serverResponse) {
											rawText = response.serverResponse;
										}
									}
									if (rawText) {
										try {
											var parsed = YAHOO.lang.JSON.parse(rawText);
											if (parsed && parsed.message) {
												rawText = parsed.message;
											}
										} catch (e) {
											// Not a JSON string
										}
										var idx = rawText.lastIndexOf("(");
										var lastIdx = rawText.lastIndexOf(")");
										if (idx !== -1 && lastIdx > idx) {
											rawText = rawText.substring(idx + 1, lastIdx);
										}
										errorMsgKey = rawText;
									}
									var errorMsg = me.msg(errorMsgKey);
									if (errorMsg === errorMsgKey) {
										errorMsg = errorMsgKey;
									}
									var inputEl = Dom.get(dialogId + "_prop_qa_batchScannerInput");
									if (inputEl) {
										inputEl.focus();
										inputEl.select();
									}
									Alfresco.util.PopupManager.displayPrompt({
										title: me.msg("title.qa-batch-scan-error"),
										text: errorMsg
									});
								},
								scope: me
							}
						});

						scanDialog.onSuccess = function(response) {
							var result = response.json;
							if (!result && response.serverResponse) {
								try {
									result = YAHOO.lang.JSON.parse(response.serverResponse);
								} catch (e) {
									// Not JSON
								}
							}
							if (result && result.status === "found") {
								Alfresco.util.PopupManager.displayMessage({
									text: me.msg("message.qa-batch-scan.success")
								});
								YAHOO.Bubbling.fire("refreshDataGrids", {
									clearCache: true,
									cacheTimeStamp: (new Date()).getTime()
								});
								var bulkActionCheck = Dom.get(dialogId + "-form-bulkAction");
								if (bulkActionCheck && !bulkActionCheck.checked) {
									this.hide();
								} else {
									this.hide();
									setTimeout(showScanDialog, 150);
								}
							} else {
								var status = (result && result.status) ? result.status : "malformed";
								var errorMsgKey = "message.qa-batch-scan." + status;
								var errorMsg = me.msg(errorMsgKey);
								if (errorMsg === errorMsgKey) {
									errorMsg = (result && result.message) ? result.message : "Error";
								}
								var inputEl = Dom.get(dialogId + "_prop_qa_batchScannerInput");
								if (inputEl) {
									inputEl.focus();
									inputEl.select();
								}
								Alfresco.util.PopupManager.displayPrompt({
									title: me.msg("title.qa-batch-scan-error"),
									text: errorMsg
								});
							}
						};

						scanDialog.show();
					};

					showScanDialog();
				}
			});

	YAHOO.Bubbling
		.fire(
			"registerToolbarButtonAction",
			{
				actionName: "qa-batch-execute",
				hideLabel: true,
				evaluate: function(asset, entity) {
					return asset.name != null && asset.name == "batchAllocationList" && entity != null && entity.type == "qa:batch" && entity.userAccess.edit && !entity.aspects.includes("bcpg:archivedEntityAspect");
				},
				fn: function(instance) {
					var me = this;
					Alfresco.util.PopupManager.displayMessage({
						text: this.msg("message.formulate.please-wait")
					});

					Alfresco.util.Ajax.request({
						method: Alfresco.util.Ajax.POST,
						url: Alfresco.constants.PROXY_URI + "becpg/quality/execute-batch?nodeRef=" + this.options.entityNodeRef,
						successCallback: {
							fn: function(response) {
								Alfresco.util.Ajax.request({
									method: Alfresco.util.Ajax.GET,
									responseContentType: Alfresco.util.Ajax.JSON,
									url: Alfresco.constants.PROXY_URI + "becpg/remote/formulate?nodeRef=" + me.options.entityNodeRef + "&format=json",
									successCallback: {
										fn: function(formulateResponse) {
											Alfresco.util.PopupManager.displayMessage({
												text: me.msg("message.formulate.success")
											});
											YAHOO.Bubbling.fire("refreshDataGrids", {
												clearCache: true,
												cacheTimeStamp: (new Date()).getTime()
											});
										},
										scope: me
									},
									failureCallback: {
										fn: function(formulateResponse) {
											if (formulateResponse.json && formulateResponse.json.message) {
												Alfresco.util.PopupManager.displayPrompt({
													title: me.msg("message.formulate.failure"),
													text: formulateResponse.json.message
												});
											} else {
												Alfresco.util.PopupManager.displayMessage({
													text: me.msg("message.formulate.failure")
												});
											}
										},
										scope: me
									}
								});
							},
							scope: this
						},
						failureCallback: {
							fn: function(response) {
								Alfresco.util.PopupManager.displayMessage({
									text: this.msg("message.details.failure")
								});
							},
							scope: this
						}
					});
				}
			});

})();
