/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG.
 * 
 * This file is part of beCPG
 * 
 * beCPG is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * beCPG is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
(function() {
	/**
	 * YUI Library aliases
	 */
	var Dom = YAHOO.util.Dom;
	/**
	 * Alfresco Slingshot aliases
	 */
	var $html = Alfresco.util.encodeHTML;

	var REQFILTER_EVENTCLASS = Alfresco.util.generateDomId(null, "notificationsReqType");

	/**
	 * ProductNotifications constructor.
	 * 
	 * @param htmlId
	 *            {String} The HTML id of the parent element
	 * @return {beCPG.component.ProductNotifications} The new
	 *         ProductNotifications instance
	 * @constructor
	 */
	beCPG.component.ProductNotifications = function(htmlId) {

		beCPG.component.ProductNotifications.superclass.constructor.call(this, "beCPG.component.ProductNotifications", htmlId, [ "button",
				"container" ]);

		// message
		this.name = "beCPG.component.EntityDataListToolbar";

		return this;
	};

	/**
	 * Extend from Alfresco.component.Base
	 */
	YAHOO.extend(beCPG.component.ProductNotifications, Alfresco.component.Base);

	/**
	 * Augment prototype with main class implementation, ensuring overwrite is
	 * enabled
	 */
	YAHOO.lang
			.augmentObject(beCPG.component.ProductNotifications.prototype,
					{
						/**
						 * Object container for initialization options
						 * 
						 * @property options
						 * @type object
						 */
						options : {

							entityNodeRef : "",

							reqCtrlListNodeRef : "",

							containerDiv : null,

							list : null,
							
							maxResults: 50

						},

						filterId : "all",

						filterData : "",

						filterParams : null,

						currentPage : 1,

						/**
						 * Fired by YUI when parent element is available for scripting.
						 * 
						 * @method onReady
						 */
						onReady : function ProductNotifications_onReady() {

							var instance = this;

							// Inject the template from the XHR request into a
							// new DIV
							// element
							this.widgets.panelDiv = document.createElement("div");

							this.widgets.panelDiv.style.zIndex = "3";

							var html = "<div class=\"notifications-panel\"><div id=\"" + instance.id + "-scoresDiv\" class=\"ctrlSumPreview  \" >";

							html += "</div><div id=\"" + instance.id + "-notificationTable\"></div></div>";
							this.widgets.panelDiv.innerHTML = html;


							this.widgets.showNotificationsButton = instance.createShowNotificationButton(this, "show-notifications",
									this.widgets.panelDiv, function() {
										instance.reloadDataTable();
									});

							this.loadPanelData();


							YAHOO.Bubbling.on("refreshDataGrids", this.loadPanelData, this);

							YAHOO.Bubbling.addDefaultAction(REQFILTER_EVENTCLASS,function(layers, args) {
									var owner = YAHOO.Bubbling.getOwnerByTagName(args[1].anchor, "span");

									
									var selectedItems = document.getElementsByClassName("rclFilterSelected");

									// sets clicked item to selected
									for (var i = 0; i < selectedItems.length; i++) {
										selectedItems[i].classList.remove("rclFilterSelected");
									}
									var chgClass = owner;
									if (owner.parentNode.nodeName == "LI") {
										chgClass = owner.parentNode;
									}
									Dom.addClass(chgClass, "rclFilterSelected");

									// refreshes view by calling filter
									var splits = owner.className.split(" ")[0].split("-");
									var type = (splits.length > 2 ? splits[2] : undefined);
									var dataType = splits[1].charAt(0).toUpperCase() + splits[1].slice(1);
									instance.filterId = (type === "all" && dataType === "All" ? "all" : "filterform");
									
									if(dataType == "Regulatorycodes" && type!=null) {
										instance.filterData =  "{\"prop_bcpg_regulatoryCode\":\"=" + type.replace(/@/gi," ").replace(/\$/gi,"-") +"\"}";
									} else {
									
										instance.filterData = (type === "all" && dataType === "All" ? undefined : "{"
												+ (type !== undefined ? ("\"prop_bcpg_rclReqType\":\"" + type+"\"") : "")
												+ (dataType != null ? (type !== undefined ? "," : "") + ("\"prop_bcpg_rclDataType\":\"" + dataType+"\"") : "")
												+ "}");
									}
									
									instance.reloadDataTable();

							}, true );
						},
						

						createShowNotificationButton : function(instance, actionName, containerDiv, fn) {

							var template = Dom.get("custom-toolBar-template-button"), buttonWidget = null;

							var spanEl = Dom.getFirstChild(template).cloneNode(true);

							Dom.addClass(spanEl, actionName);
							Dom.addClass(spanEl, "loading");

							Dom.setAttribute(spanEl, "id", instance.id + "-" + actionName + "Button");

							this.options.containerDiv.appendChild(spanEl);

							buttonWidget = Alfresco.util.createYUIButton(instance, actionName + "Button", null, {
								type : "menu",
								menu : containerDiv,
								lazyloadmenu : false
							});

							buttonWidget.getMenu().subscribe("show", fn, buttonWidget, this);

							return buttonWidget;

						},

						createDataTable : function() {

							var instance = this;

							instance.widgets.dataSource = new YAHOO.util.DataSource(this.getWebscriptUrl(), {
								connMethodPost : true,
								responseType : YAHOO.util.DataSource.TYPE_JSON,
								responseSchema : {
									resultsList : "items",
									metaFields : {
										startIndex : "startIndex",
										totalRecords : "totalRecords"
									}
								}
							});
							var columDefs = [ {
								key : "detail",
								sortable : false,
								formatter : this.bind(this.renderCellDetail)
							} ];

							instance.widgets.notificationsDataTable = new YAHOO.widget.DataTable(instance.id + "-notificationTable", columDefs,
									instance.widgets.dataSource, {
										initialLoad : false,
										dynamicData : false,
										"MSG_EMPTY" : '<span class="wait">' + $html(this.msg("message.loading")) + '</span>',
										"MSG_ERROR" : this.msg("message.error"),
										className : "alfresco-datatable simple-doclist body notifications-list",
										renderLoopSize : 4,
										paginator : null
									});

							instance.widgets.notificationsDataTable.getDataTable = function() {
								return this;
							};

							instance.widgets.notificationsDataTable.getData = function(recordId) {
								return this.getRecord(recordId).getData();
							};

							instance.widgets.notificationsDataTable.loadDataTable = function DataTable_loadDataTable(parameters) {

								instance.widgets.dataSource.connMgr.setDefaultPostHeader(Alfresco.util.Ajax.JSON);

								if (Alfresco.util.CSRFPolicy.isFilterEnabled()) {
									instance.widgets.dataSource.connMgr.initHeader(Alfresco.util.CSRFPolicy.getHeader(), Alfresco.util.CSRFPolicy
											.getToken(), false);
								}

								instance.widgets.dataSource.sendRequest(YAHOO.lang.JSON.stringify(parameters), {
									success : function DataTable_loadDataTable_success(oRequest, oResponse, oPayload) {
										instance.widgets.notificationsDataTable.onDataReturnReplaceRows(oRequest, oResponse, oPayload);

									/* if (instance.widgets.paginator) {
											instance.widgets.paginator.set('totalRecords', oResponse.meta.totalRecords);
											instance.widgets.paginator.setPage(oResponse.meta.startIndex, true);
										} */

									},
									failure : instance.widgets.notificationsDataTable.onDataReturnReplaceRows,
									scope : instance.widgets.notificationsDataTable,
									argument : {}
								});
							};
							// Override DataTable
							// function to set
							// custom empty
							// message
							var original_doBeforeLoadData = instance.widgets.notificationsDataTable.doBeforeLoadData;

							instance.widgets.notificationsDataTable.doBeforeLoadData = function SimpleDocList_doBeforeLoadData(sRequest, oResponse,
									oPayload) {
								if (oResponse.results && oResponse.results.length === 0) {
									oResponse.results.unshift({
										isInfo : true,
										title : instance.msg("empty.notifications.title"),
										description : instance.msg("empty.notifications.description")
									});
								} 

								return original_doBeforeLoadData.apply(this, arguments);
							};
						},

						loadPanelData : function() {

							var instance = this;

							Alfresco.util.Ajax.request({

								url : Alfresco.constants.PROXY_URI + "becpg/product/reqctrllist/node/"
										+ instance.options.entityNodeRef.replace(":/", ""),
								method : Alfresco.util.Ajax.GET,
								responseContentType : Alfresco.util.Ajax.JSON,
								successCallback : {
									fn : function(response) {

										if (response.json) {

											var html = "";
											if (response.json.scores !== undefined) {

												var scores = response.json.scores;
												var intScore = parseInt(scores.global);
												var spriteIndex = (intScore / 5 >> 0);
												var scoreTitle = instance.msg("tooltip.components.validation") + ": "
														+ Math.floor(scores.details.componentsValidation) + "%\n"
														+ instance.msg("tooltip.mandatory.completion") + ": "
														+ Math.floor(scores.details.mandatoryFields) + "%\n"
														+ instance.msg("tooltip.specification.respect") + ": "
														+ Math.floor(scores.details.specifications) + "%";

												html += "<ul><li class=\"title\">" + instance.msg("label.product.scores") + "</li>"
														+ "<li class=\"score score-" + spriteIndex + "\" " + "title=\"" + scoreTitle + "\">";

												html += "<span>" + Math.floor(scores.global) + "%</span>";
												html += "</li></ul>";

												var buttonClass = "score-" + spriteIndex;

												var totalForbidden = scores.totalForbidden;
												if (!Dom.hasClass(instance.widgets.showNotificationsButton, "score")) {
													instance.widgets.showNotificationsButton.removeClass("loading");
													instance.widgets.showNotificationsButton.addClass("score");
													instance.widgets.showNotificationsButton.addClass(buttonClass);
												}

												instance.widgets.showNotificationsButton.set("label", Math.floor(scores.global) + "%");

												if (totalForbidden !== undefined && totalForbidden !== null && totalForbidden > 0) {
													instance.widgets.showNotificationsButton.set("title", this.msg("tooltip.notifications-button",
															totalForbidden));

													var errorSpan = Dom.getFirstChildBy(this.options.containerDiv, function(el) {
														return el.className.indexOf("warning") > -1;
													});

													if (errorSpan === null) {
														errorSpan = document.createElement("span");
														errorSpan.className = "warning";
														this.options.containerDiv.appendChild(errorSpan);
													}

													errorSpan.innerHTML = (totalForbidden != undefined && totalForbidden != null
															&& totalForbidden > 0 ? totalForbidden : "");
												}

												// if we have some constraints in res
												if (scores.ctrlCount !== undefined && scores.ctrlCount != null
														&& scores.ctrlCount.length > 0) {
													// Parses each array mapped to dataType
													html += "<div class=\"dataTypeList\"><div class=\"title\">"
															+ instance.msg("label.constraints.violations")
															+ "<span class=\"req-all-all rclFilterSelected\"><a class=\"req-filter "
															+ REQFILTER_EVENTCLASS + "\" href=\"#\">" + instance.msg("label.constraints.view-all")
															+ "</a></span></div>";

													html += "<div class=\"rclFilterElt\"><div>";

													for ( var dataType in scores.ctrlCount) {
														var scoreInfo = "";
														var dataTypeName = Object.keys(scores.ctrlCount[dataType])[0];
														html += "<div class=\"div-" + dataTypeName.toString().toLowerCase()
																+ "\"><span class=\"span-" + dataTypeName.toString().toLowerCase()
																+ "\"><a class=\"req-filter " + REQFILTER_EVENTCLASS + "\" href=\"#\" >"
																+ instance.msg("label.constraints." + dataTypeName.toString().toLowerCase())
																+ scoreInfo + "</a></span><ul>";

														var types = scores.ctrlCount[dataType];
														
													

														for ( var type in types[dataTypeName]) {
															var value = types[dataTypeName][type];
															if(dataTypeName == "RegulatoryCodes"){
																html += '<li><span class="req-' + dataTypeName.toString().toLowerCase() + '-' + type.replace(/ /gi,"@").replace(/-/gi,"$")
																+ '" ><a class="req-filter tag '
																+ REQFILTER_EVENTCLASS + '" href="#"><span>' + type + 
																' ('+ value + ')</span></a></li>';
															} else {
																html += '<li><span class="req-' + dataTypeName.toString().toLowerCase() + '-' + type
																		+ '" title="' + instance.msg("reqTypes." + type) + '"><a class="req-filter '
																		+ REQFILTER_EVENTCLASS + '" href="#"><span class="reqType' + type + '"></span>'
																		+ value + '</a></li>';
															}

														}
														html += "</ul></div>";

													}
													html += "</div></div></div>";
												}
											}
											if(Dom.get(instance.id + "-scoresDiv")!=null){
												Dom.get(instance.id + "-scoresDiv").innerHTML = html;
											}
											
											if(!instance.widgets.notificationsDataTable){
												instance.createDataTable();
											}
											
										}

									},
									scope : instance
								},
								failureMessage : "Could not load html template for version graph",
								execScripts : true
							});

						},

						/**
						 * Generate base webscript url. Can be overridden.
						 * 
						 * @method getWebscriptUrl
						 */
						getWebscriptUrl : function ProductNotifications_getWebscriptUrl() {
							return Alfresco.constants.PROXY_URI + "becpg/entity/datalists/data/node"
									+ "?guessContainer=true&repo=true&itemType=bcpg:reqCtrlList&pageSize="+this.options.maxResults+"&dataListName=reqCtrlList&entityNodeRef="
									+ this.options.entityNodeRef+"&locale="+Alfresco.constants.JS_LOCALE;

						},

						/**
						 * Calculate webscript parameters
						 * 
						 * @method getParameters
						 * @override
						 */
						getParameters : function ProductNotifications_getParameters() {

							var request = {
								fields : [ "bcpg_rclReqType", "bcpg_rclReqMessage", "bcpg_rclSources", "bcpg_rclDataType", "bcpg_regulatoryCode" ],
								page : this.currentPage,
								filter : {
									filterId : this.filterId,
									filterOwner : null,
									filterData : this.filterData,
									filterParams : this.filterParams
								},
								extraParams : null
							};
							return request;

						},

						/**
						 * Detail custom datacell formatter
						 * 
						 * @method renderCellDetail
						 * @param elCell
						 *            {object}
						 * @param oRecord
						 *            {object}
						 * @param oColumn
						 *            {object}
						 * @param oData
						 *            {object|string}
						 */
						renderCellDetail : function ProductNotifications_renderCellDetail(elCell, oRecord, oColumn, oData) {
							var record = oRecord.getData(), desc = "", dateLine = "";

							if (record.isInfo) {
								desc += '<div class="empty"><h3>' + record.title + '</h3>';
								desc += '<span>' + record.description + '</span></div>';
							} else {

								var reqType = oRecord.getData("itemData")["prop_bcpg_rclReqType"].value;
								var reqDataType = oRecord.getData("itemData")["prop_bcpg_rclDataType"].value;
								var regulatoryCode = oRecord.getData("itemData")["prop_bcpg_regulatoryCode"].value;
								
								var reqProducts = oRecord.getData("itemData")["assoc_bcpg_rclSources"];
								desc += '<div class="rclReq-details">';
								if (reqType) {
									desc += '   <div class="icon" ><span class="reqType' + reqType + '" title="'
											+ Alfresco.util.encodeHTML(this.msg("data.reqtype." + reqType.toLowerCase())) + '">&nbsp;</span></div>';
								}
								if (regulatoryCode) {
									desc += '      <div class="rclReq-regulatoryCode">'
										+ Alfresco.util.encodeHTML(regulatoryCode) + '</div>';
									desc += '      <div class="rclReq-title">'
										+ Alfresco.util.encodeHTML(oRecord.getData("itemData")["prop_bcpg_rclReqMessage"].displayValue.replace(regulatoryCode,"")) + '</div>';
								} else {
								desc += '      <div class="rclReq-title">'
										+ Alfresco.util.encodeHTML(oRecord.getData("itemData")["prop_bcpg_rclReqMessage"].displayValue) + '</div>';
								}
								desc += '      <div class="rclReq-content"><ul>';

								if (reqProducts) {
									for ( var i in reqProducts) {
										var product = reqProducts[i], pUrl = beCPG.util.entityURL(product.siteId, product.value);
										var dataList = null;
										if (product.metadata.indexOf("finishedProduct") != -1
												|| product.metadata.indexOf("semiFinishedProduct") != -1) {
											dataList =  "compoList";
										} else if (product.metadata.indexOf("packagingKit") != -1) {
											dataList =  "packagingList";
										}
										
										switch (reqDataType) {
											case 'Labelling':
												dataList = "ingLabelingList";
												break;
											case 'Labelclaim':
												dataList = "labelClaimList";
												break;
											case 'Physicochem':
												dataList = "physicoChemList";
												break;
											case 'Nutrient':
												dataList = "nutList";
												break;
											case 'Ingredient':
												dataList = "ingList";
												break;
											case 'Allergen':
												dataList = "allergenList";
												break;
											case 'Cost':
												dataList = "costList";
												break;
										}

										if(dataList!=null){
											pUrl = beCPG.util.entityURL(product.siteId, product.value, null, null, dataList);
										}

										if (pUrl) {
											pUrl += "&bcPath=true&bcList=" + this.options.list;
										}

										desc += '<li><span class="' + product.metadata + '" ><a href="' + pUrl + '">'
												+ Alfresco.util.encodeHTML(product.displayValue) + '</a></span></li>';

									}
								}
								desc += '</ul></div>';
								desc += '   </div>';
								desc += '   <div class="clear"></div>';
								desc += '</div>';

							}
							elCell.innerHTML = desc;
						},
						/**
						 * Reloads the DataTable
						 * 
						 * @method reloadDataTable
						 */
						reloadDataTable : function SimpleDocList_reloadDataTable() {
							this.widgets.notificationsDataTable.loadDataTable(this.getParameters());
						},
						
						destroy : function (){
							
							YAHOO.Bubbling.unsubscribe("refreshDataGrids",this.loadPanelData, this);
							this.widgets.showNotificationsButton.destroy();
							this.widgets.notificationsDataTable.destroy();
							this.options.containerDiv.parentNode.removeChild(this.options.containerDiv);
							this.options.containerDiv.innerHTML = "";
							this.widgets.panelDiv.innerHTML = "";
							
						}
						
						

					}, true);

})();
