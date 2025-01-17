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
	if (beCPG.component.EntityDataListToolbar) {

		YAHOO.Bubbling
			.fire(
				"registerToolbarButtonAction",
				{
					actionName: "eco-calculate-wused",
					evaluate: function(asset, entity) {
						return asset.name != null && (asset.name === "replacementList" || asset.name === "wUsedList") && entity != null && entity.userAccess.edit;
					},
					fn: function(instance) {


						Alfresco.util.PopupManager.displayMessage({
							text: this.msg("message.eco-calculate-wused.please-wait")
						});

						Alfresco.util.Ajax.request({
							method: Alfresco.util.Ajax.GET,
							url: Alfresco.constants.PROXY_URI + "becpg/ecm/changeorder/" + this.options.entityNodeRef
								.replace(":/", "") + "/calculatewused",
							successCallback: {
								fn: function EntityDataListthis_onECOCalculateWUsed_success(response) {
									Alfresco.util.PopupManager.displayMessage({
										text: this.msg("message.eco-calculate-wused.success")
									});
									YAHOO.Bubbling.fire("refreshDataGrids");
								},
								scope: this
							},
							failureCallback: {
								fn: function EntityDataListthis_onECOCalculateWUsed_failure(response) {
									if (response.message != null) {
										Alfresco.util.PopupManager.displayPrompt({
											text: response.message
										});
									} else {
										Alfresco.util.PopupManager.displayMessage({
											text: this.msg("message.eco-calculate-wused.failure")
										});
									}
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
					actionName: "eco-do-simulation",
					evaluate: function(asset, entity) {
						return asset.name != null && (asset.name === "replacementList" || asset.name === "changeUnitList" || asset.name === "calculatedCharactList") && entity != null && entity.userAccess.edit;
					},
					fn: function(instance) {

						Alfresco.util.PopupManager.displayMessage({
							text: this.msg("message.eco-do-simulation.please-wait")
						});

						Alfresco.util.Ajax.request({
							method: Alfresco.util.Ajax.GET,
							url: Alfresco.constants.PROXY_URI + "becpg/ecm/changeorder/" + this.options.entityNodeRef
								.replace(":/", "") + "/dosimulation",
							successCallback: {
								fn: function EntityDataListthis_onECODoSimulation_success(response) {
									Alfresco.util.PopupManager.displayMessage({
										text: this.msg("message.eco-do-simulation.success"),
										displayTime: 5
									});
									YAHOO.Bubbling.fire("refreshDataGrids");
								},
								scope: this
							},
							failureCallback: {
								fn: function EntityDataListthis_onECODoSimulation_failure(response) {
									if (response.message != null) {
										Alfresco.util.PopupManager.displayPrompt({
											text: response.message
										});
									} else {
										Alfresco.util.PopupManager.displayMessage({
											text: this.msg("message.eco-do-simulation.failure")
										});
									}
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
					actionName: "eco-apply",
					evaluate: function(asset, entity) {
						return asset.name != null && (asset.name === "replacementList" || asset.name === "changeUnitList") && entity != null && entity.userAccess.edit
							&& beCPG.util.contains(entity.userSecurityRoles,
								"ApplyChangeOrder");
					},
					fn: function(instance) {

						var me = this;

						Alfresco.util.PopupManager.displayPrompt({
							title: this.msg("message.confirm.eco-apply.title"),
							text: this.msg("message.confirm.eco-apply.description"),
							buttons: [{
								text: this.msg("button.eco-apply"),
								handler: function() {
									this.destroy();
									Alfresco.util.PopupManager.displayMessage({
										text: me.msg("message.eco-apply.please-wait")
									});

									Alfresco.util.Ajax.request({
										method: Alfresco.util.Ajax.GET,
										url: Alfresco.constants.PROXY_URI + "becpg/ecm/changeorder/" + me.options.entityNodeRef
											.replace(":/", "") + "/apply",
										successCallback: {
											fn: function EntityDataListthis_onECOApply_success(response) {
												Alfresco.util.PopupManager.displayMessage({
													text: me.msg("message.eco-apply.success"),
													displayTime: 5
												});
												YAHOO.Bubbling.fire("refreshDataGrids");
											},
											scope: me
										},
										failureCallback: {
											fn: function EntityDataListthis_onECOApply_failure(response) {
												if (response.message != null) {
													Alfresco.util.PopupManager.displayPrompt({
														text: response.message
													});
												} else {
													Alfresco.util.PopupManager.displayMessage({
														text: me.msg("message.eco-apply.failure")
													});
												}
											},
											scope: me
										}

									});

								}
							}, {
								text: this.msg("button.cancel"),
								handler: function EntityDataGrid__onActionDelete_cancel() {
									this.destroy();
								},
								isDefault: true
							}]
						});


					}
				});

		YAHOO.Bubbling
			.fire(
				"registerToolbarButtonAction",
				{
					actionName: "full-screen",
					evaluate: function(asset, entity) {
						return asset.name != null && (asset.name === "compoList" || asset.name === "packagingList" || asset.name === "processList") && entity != null && entity.userAccess.edit;
					},
					fn: function(instance) {

						var me = this;

						var onBeforeFormRuntimeInit = function(layer, args) {
							var formUI = args[1].component, formsRuntime = args[1].runtime;

							formsRuntime.setAsReusable(true);

							formUI.buttons.submit.set("label", this.msg("button.add"));

							Dom.removeClass("full-screen-form", "hidden");

							formsRuntime.setAJAXSubmit(true, {
								successCallback: {
									fn: function(response) {

										var itemType = me.options.itemType != null ? me.options.itemType
											: me.datalistMeta.itemType;
										var parentInput = Dom.get(me.id + "_prop_bcpg_parentLevel-added");


										if (parentInput != null && parentInput.value != null && parentInput.value.length > 0) {
											me.parentInputNodeRef = parentInput.value;


											Alfresco.util.Ajax
												.jsonPost(
													{
														url: Alfresco.constants.PROXY_URI + "becpg/entity/datalists/openclose?nodeRef="
															+ me.parentInputNodeRef + "&expand=true&entityNodeRef=" + me.options.entityNodeRef + "&listType=" + itemType,
														successCallback:
														{
															fn: function EntityDataGrid_onCollapsedAndExpanded(
																responseColapse) {

																YAHOO.Bubbling.fire("refreshDataGrids");
																YAHOO.Bubbling.fire("dirtyDataTable");

															},
															scope: this
														}
													});

										} else {
											me.parentInputNodeRef = null;

											YAHOO.Bubbling.fire("dataItemCreated", {
												nodeRef: response.json.persistedObject
											});

										}

										formsRuntime.reset();


										if (me.parentInputNodeRef != null) {
											Dom.get(me.id + "_prop_bcpg_parentLevel-added").value = me.parentInputNodeRef;
											Dom.get(me.id + "_prop_bcpg_parentLevel").value = ' '; // avoid blur event to reset the field
											YAHOO.Bubbling.fire(me.id + "_prop_bcpg_parentLevel" + "refreshContent", me.parentInputNodeRef, this);
											
										}


										var form = Dom.get(formsRuntime.formId);
										for (var j = 0; j < form.elements.length; j++) {
											if (Alfresco.util.isVisible(form.elements[j])) {
												try {
													form.elements[j].focus();
													break;
												} catch (e) {/* Ie 8 */
												}
											}
										}

									},
									scope: this
								}


							});
							
							formsRuntime.
							
							
							YAHOO.Bubbling.unsubscribe("beforeFormRuntimeInit", onBeforeFormRuntimeInit, me);
						};



						if (Dom.hasClass("share-header", "hidden")) {

							if (document.exitFullscreen) {
								document.exitFullscreen();
							}
							else if (document.mozCancelFullScreen) {
								document.mozCancelFullScreen();
							}
							else if (document.webkitCancelFullScreen) {
								document.webkitCancelFullScreen();
							}

							Dom.removeClass("share-header", "hidden");
							Dom.removeClass("alf-hd", "hidden");
							Dom.removeClass("alf-filters", "hidden");
							Dom.removeClass("alf-ft", "hidden");
							Dom.removeClass("Share", "full-screen");
							Dom.addClass("alf-content", "yui-b");
							if (this.fullScreen) {
								Dom.setStyle("alf-content", "margin-left", this.fullScreen.marginLeft);
								this.fullScreen = null;
							}
							Dom.addClass("full-screen-form", "hidden");


							YAHOO.Bubbling.fire("refreshFloatingHeader");
						} else {

							var docElm = document.documentElement;
							if (docElm.requestFullscreen) {
								docElm.requestFullscreen();
							}
							else if (docElm.mozRequestFullScreen) {
								docElm.mozRequestFullScreen();
							}
							else if (docElm.webkitRequestFullScreen) {
								docElm.webkitRequestFullScreen();
							}


							Dom.addClass("share-header", "hidden");
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
										Alfresco.constants.URL_SERVICECONTEXT + "components/form?formId=full-screen&showCaption=false&entityNodeRef={entityNodeRef}&itemKind={itemKind}&itemId={itemId}&destination={destination}&mode={mode}&submitType={submitType}&showCancelButton=true&dataListsName={dataListsName}",
										{
											itemKind: "type",
											itemId: this.datalistMeta.itemType,
											destination: destination,
											mode: "create",
											submitType: "json",
											entityNodeRef: this.options.entityNodeRef,
											dataListsName: encodeURIComponent(this.datalistMeta.name != null ? this.datalistMeta.name
												: this.options.list)
										});

								YAHOO.Bubbling.on("beforeFormRuntimeInit", onBeforeFormRuntimeInit, this);

								Alfresco.util.Ajax
									.request({
										url: templateUrl,
										dataObj: {
											htmlid: this.id
										},
										successCallback: {
											fn: function(response) {
												var containerDiv = Dom.get("full-screen-form");

												if (containerDiv.hasChildNodes()) {
													while (containerDiv.childNodes.length >= 1) {
														containerDiv.removeChild(containerDiv.firstChild);
													}
												}

												containerDiv.innerHTML = response.serverResponse.responseText;
												YAHOO.Bubbling.fire("refreshFloatingHeader");

											},
											scope: this
										},
										failureMessage: "Could not load dialog template from '" + this.options.templateUrl + "'.",
										scope: this,
										execScripts: true
									});

							} else {
								Dom.removeClass("full-screen-form", "hidden");
								YAHOO.Bubbling.fire("refreshFloatingHeader");
							}

							me.fullScreen = {
								marginLeft: Dom.getStyle("alf-content", "margin-left"),
								lock: false
							};


							Dom.setStyle("alf-content", "margin-left", null);

						}

					}
				});


		YAHOO.Bubbling.fire("registerToolbarButtonAction", {
			actionName: "product-notifications",
			right: true,
			evaluate: function(asset, entity) {
				return asset.name != null &&
					(asset.name === "compoList" || asset.name === "processList" || asset.name === "packagingList"
						|| asset.name === "ingLabelingList" || asset.name === "ingRegulatoryList" || asset.name === "nutList" || asset.name === "labelClaimList"
						|| asset.name === "costList" || asset.name === "physicoChemList" || asset.name === "ingList" || asset.name === "allergenList"
						|| asset.name === "priceList" || asset.name === "hazardClassificationList" || asset.name === "svhcList"
                         || asset.name === "packMaterialList" || asset.name === "lcaList" || asset.name === "regulatoryList"
						|| asset.name === "View-properties") && beCPG.util.contains(entity.aspects,"bcpg:entityScoreAspect") ;
			},
			createWidget: function(containerDiv, instance) {

				var divEl = document.createElement("div");

				Dom.setAttribute(divEl, "id", instance.id + "-productNotifications");
				Dom.addClass(divEl, "product-notifications");

				containerDiv.appendChild(divEl);

				return new beCPG.component.ProductNotifications(instance.id + "-productNotifications").setOptions({
					entityNodeRef: instance.options.entityNodeRef,
					list: instance.options.list,
					containerDiv: divEl
				});

			}
		});





		YAHOO.Bubbling
			.fire(
				"registerToolbarButtonAction",
				{
					actionName: "product-metadata",
					hideLabel: true,
					evaluate: function(asset, entity) {
						return asset.name != null && asset.permissions.changeState && (asset.name === "compoList" || asset.name === "processList" || asset.name === "packagingList") && entity != null && entity.userAccess.edit;
					},
					fn: function(instance) {

						var templateUrl = YAHOO.lang
							.substitute(
								Alfresco.constants.URL_SERVICECONTEXT + "components/form?popup=true&formId=formulation&itemKind=node&itemId={itemId}&mode=edit&submitType=json&showCancelButton=true&siteId={siteId}",
								{
									itemId: this.options.entityNodeRef,
									siteId: this.options.siteId!=null ? this.options.siteId : ""
								});

						var editProductMetadata = new Alfresco.module.SimpleDialog(this.id + "-editProductMetadata");

						editProductMetadata.setOptions(
							{
								width: "33em",
								onSuccess: {
									fn: function() {
										YAHOO.Bubbling.fire("metadataRefresh");
										YAHOO.Bubbling.fire("dirtyDataTable");
										Alfresco.util.PopupManager.displayMessage(
											{
												text: this.msg("message.details.success")
											});
									},
									scope: this
								},
								failureMessage: this.msg("message.details.failure"),
								templateUrl: templateUrl,
								destroyOnHide: true,
								doBeforeDialogShow: {
									fn: function(p_form, p_dialog) {
										Alfresco.util.populateHTML([p_dialog.id + "-dialogTitle",
										this.msg("label.product-metadata.title")]);
									},
									scope: this
								}

							}).show();

					}
				});

		YAHOO.Bubbling.fire("registerToolbarButtonAction", {
			actionName: "rapid-link",
			right: false,
			evaluate: function(asset, entity) {
				return asset.name != null && asset.permissions.changeState && (asset.name === "compoList" || asset.name === "processList" || asset.name === "packagingList") && entity != null && entity.userAccess.edit;
			},
			createWidget: function(containerDiv, instance) {

				var divEl = document.createElement("div");

				containerDiv.appendChild(divEl);

				Dom.setAttribute(divEl, "id", instance.id + "-rapidLink");

				Dom.addClass(divEl, "rapidLink");

				var dataListNodeRef = instance.datalistMeta.nodeRef != null ? instance.datalistMeta.nodeRef
					: instance.options.parentNodeRef;

				return new beCPG.component.RapidLinkToolbar(instance.id + "-rapidLink").setOptions({
					dataListNodeRef: dataListNodeRef,
					entity: instance.entity,
					containerDiv: divEl,
					list: encodeURIComponent(instance.datalistMeta.name != null ? instance.datalistMeta.name
						: instance.options.list),
					siteId: instance.options.siteId
				});

			}
		});



		YAHOO.Bubbling
			.fire(
				"registerToolbarButtonAction",
				{
					actionName: "variant-picker",
					right: true,
					evaluate: function(asset, entity) {
						return entity != null && (beCPG.util.contains(entity.aspects,
							"bcpg:entityVariantAspect") && (asset.name != null && (asset.name === "compoList" || asset.name === "processList" || asset.name === "packagingList")));

					},
					createWidget: function(containerDiv, instance) {

						var divEl = document.createElement("div");

						containerDiv.appendChild(divEl);

						Dom.setAttribute(divEl, "id", instance.id + "-variantPicker");

						Dom.addClass(divEl, "variantPicker");

						return  new beCPG.component.VariantPicker(instance.id + "-variantPicker").setOptions({
							entityNodeRef: instance.options.entityNodeRef,
							entity: instance.entity,
							containerDiv: divEl,
							toolBarInstance: instance,
							edit: instance.entity.userAccess.edit
						});
					}
				});

		var assetRef;
		YAHOO.Bubbling
			.fire(
				"registerToolbarButtonAction",
				{
					actionName: "import-nuts",
					right: false,
					evaluate: function(asset, entity) {
						assetRef = asset;
						return (entity != null && entity.userAccess.edit && asset.name != null && asset.name === "nutList" && beCPG.util.contains(entity.aspects,
							"bcpg:productAspect"));

					},
					fn: function(instance) {
						var nutImporter = new Alfresco.module.SimpleDialog(this.id + "-nutImporter");

						nutImporter.setOptions({
							width: this.options.formWidth,
							templateUrl: Alfresco.constants.URL_SERVICECONTEXT + "modules/nut-database/nut-importer?entityNodeRef=" + this.options.entityNodeRef + "&nutsCompare=true&writePermission=" + assetRef.permissions.editChildren,
							actionUrl: Alfresco.constants.PROXY_URI + "becpg/product/nutdatabaseimport?dest=" + this.options.entityNodeRef + "&onlyNuts=true",
							validateOnSubmit: false,
							firstFocus: this.id + "-nutImporter-supplier-field",
							doBeforeFormSubmit: {
								fn: function FormulationView_onActionEntityImport_doBeforeFormSubmit(form) {
									Alfresco.util.PopupManager.displayMessage({
										text: this.msg("message.rapid-link.import.please-wait")
									});
								},
								scope: this
							},
							onSuccess: {
								fn: function FormulationView_onActionEntityImport_success(response) {
									if (response.json) {
										YAHOO.Bubbling.fire("refreshDataGrids");
										Alfresco.util.PopupManager.displayMessage({
											text: this.msg("message.rapid-link.nutrient-import.success")
										});
									}

								},
								scope: this
							},
							onFailure: {
								fn: function FormulationView_onActionEntityImport_failure(response) {
									Alfresco.util.PopupManager.displayMessage({
										text: this.msg("message.import.failure")
									});
								},
								scope: this
							}
						});

						nutImporter.show();


					}
				});

		YAHOO.Bubbling
			.fire(
				"registerToolbarButtonAction",
				{
					actionName: "import-lca",
					right: false,
					evaluate: function(asset, entity) {
						return (entity != null && entity.userAccess.edit && asset.name != null && asset.name === "lcaList" && beCPG.util.contains(entity.aspects,
							"bcpg:productAspect"));

					},
					fn: function(instance) {
						var lcaImporter = new Alfresco.module.SimpleDialog(this.id + "-lcaImporter");

						lcaImporter.setOptions({
							width: this.options.formWidth,
							templateUrl: Alfresco.constants.URL_SERVICECONTEXT + "modules/lca-database/lca-importer?entityNodeRef=" + this.options.entityNodeRef,
							actionUrl: Alfresco.constants.PROXY_URI + "becpg/product/lcadatabaseimport?dest=" + this.options.entityNodeRef,
							validateOnSubmit: false,
							firstFocus: this.id + "-lcaImporter-supplier-field",
							doBeforeFormSubmit: {
								fn: function FormulationView_onActionEntityImport_doBeforeFormSubmit(form) {
									Alfresco.util.PopupManager.displayMessage({
										text: this.msg("message.rapid-link.import.please-wait")
									});
								},
								scope: this
							},
							onSuccess: {
								fn: function FormulationView_onActionEntityImport_success(response) {
									if (response.json) {
										YAHOO.Bubbling.fire("refreshDataGrids");
										Alfresco.util.PopupManager.displayMessage({
											text: this.msg("message.rapid-link.lca-import.success")
										});
									}

								},
								scope: this
							},
							onFailure: {
								fn: function FormulationView_onActionEntityImport_failure(response) {
									Alfresco.util.PopupManager.displayMessage({
										text: this.msg("message.import.failure")
									});
								},
								scope: this
							}
						});

						lcaImporter.show();


					}
				});


	}
})();
