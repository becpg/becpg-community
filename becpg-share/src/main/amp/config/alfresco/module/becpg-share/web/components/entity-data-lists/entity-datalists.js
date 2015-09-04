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
/**
 * Entity Data Lists: EntityDataLists component. Displays a list of
 * entitydatalists
 * 
 * @namespace beCPG
 * @class beCPG.component.EntityDataLists
 */
(function() {
	/**
	 * YUI Library aliases
	 */
	var Dom = YAHOO.util.Dom, Bubbling = YAHOO.Bubbling;

	/**
	 * Alfresco Slingshot aliases
	 */
	var $html = Alfresco.util.encodeHTML, $combine = Alfresco.util.combinePaths;

	/**
	 * EntityDataLists constructor.
	 * 
	 * @param htmlId
	 *            {String} The HTML id of the parent element
	 * @return {beCPG.component.EntityDataLists} The new EntityDataLists
	 *         instance
	 * @constructor
	 */
	beCPG.component.EntityDataLists = function(htmlId) {
		return beCPG.component.EntityDataLists.superclass.constructor.call(this, htmlId);
	};

	/**
	 * Extend from Alfresco.component.DataLists
	 */
	YAHOO
			.extend(
					beCPG.component.EntityDataLists,
					Alfresco.component.DataLists,
					{
						/**
						 * Current entity
						 */
						entity : null,
						/**
						 * Object container for initialization options
						 * 
						 * @property options
						 * @type object
						 */
						options : {
							/**
							 * Current entityNodeRef.
							 * 
							 * @property entityNodeRef
							 * @type string
							 * @default ""
							 */
							entityNodeRef : "",

							/**
							 * ListId representing currently selected list
							 * 
							 * @property listId
							 * @type string
							 */
							listId : "",

							/**
							 * List types when creating new Data Lists
							 * 
							 * @property listTypes
							 * @type Array
							 */
							listTypes : [],
							
							
							/**
							 * Sort options
							 */
							
							sortOptions : [],
							
							showCreate : false
						},

						/**
						 * Fired by YUI when parent element is available for
						 * scripting.
						 * 
						 * @method onReady
						 */
						onReady : function DataLists_onReady() {

							this.widgets.spinner = new YAHOO.widget.Panel(this.id + "-waitPanel", {
								width : "240px",
								fixedcenter : true,
								close : false,
								draggable : false,
								zindex : 99,
								modal : true,
								visible : false
							});

							this.widgets.spinner.setHeader(this.msg("message.loading"));
							this.widgets.spinner.setBody('<div style="text-align:center;"><img src="' + Alfresco.constants.URL_RESCONTEXT
									+ '/components/images/rel_interstitial_loading.gif" /></div>');
							this.widgets.spinner.render(document.body);

							if(this.options.showCreate){
    							this.widgets.newList = Alfresco.util.createYUIButton(this, "newListButton", this.onNewList, {
    								disabled : true
    							});
							}
							// Retrieve the lists from the specified Site &
							// Container
							this.populateDataLists({
								fn : function DataLists_onReady_callback() {
									this.renderDataLists();

									// Select current list, if relevant
									if (this.options.listId.length > 0 && this.dataLists[this.options.listId]) {
										YAHOO.Bubbling.fire("activeDataListChanged", {
											list : this.options.listId,
											dataList : this.dataLists[this.options.listId],
											entity : this.entity,
											scrollTo : true
										});
									} else {
										YAHOO.Bubbling.fire("hideFilter");
									}

									if(this.options.showCreate){
    									if (this.dataListsLength === 0 || window.location.hash === "#new") {
    										this.widgets.newList.fireEvent("click");
    									}
									}
								},
								scope : this
							});
						},

						/**
						 * Retrieves the Data Lists from the Repo
						 * 
						 * @method populateDataLists
						 * @param callback
						 *            {Object} Optional callback literal {fn,
						 *            scope, obj} whose function is invoked once
						 *            list has been retrieved
						 */
						populateDataLists : function DataLists_populateDataLists(p_callback) {

							if (document.referrer === null || document.referrer.indexOf("entity-data-lists") < 1) {
								this.showLoadingAjaxSpinner(true);
							}

							/**
							 * Success handler for Data Lists request
							 * 
							 * @method fnSuccess
							 * @param response
							 *            {Object} Ajax response object literal
							 * @param obj
							 *            {Object} Callback object from original
							 *            function call
							 */
							var fnSuccess = function DataLists_pDL_fnSuccess(response, p_obj) {
								this.showLoadingAjaxSpinner(false);

								var lists = response.json.datalists, list,me = this;

								if (response.json.listTypes) {
									this.options.listTypes = response.json.listTypes;
								}
								

								this.dataLists = {};

								if (response.json.container) {
									this.containerNodeRef = new Alfresco.util.NodeRef(response.json.container);
								} else {
									this.containerNodeRef = new Alfresco.util.NodeRef(this.options.entityNodeRef);
								}
								this.entity = response.json.entity;
								if(this.options.showCreate){
								    this.widgets.newList.set("disabled", !response.json.permissions.create);
								}   

							    if(this.options.sortOptions!=null && this.options.sortOptions.length>0){
							        
                                    lists.sort(function (listId1, listId2){
                                        var val1=500, val2 = 500;
                                        for(var z in me.options.sortOptions){
                                            if(listId1.name.indexOf(me.options.sortOptions[z].id)>-1){
                                                val1 = me.options.sortOptions[z].sortIndex;
                                            }
                                            if(listId2.name.indexOf(me.options.sortOptions[z].id)>-1){
                                                val2 = me.options.sortOptions[z].sortIndex;
                                            }
                                        }
                                        return val1 - val2;
                                    });
                                }
								
								for (var i = 0, ii = lists.length; i < ii; i++) {
									list = lists[i];
									this.dataLists[list.name] = list;
								}
								this.dataListsLength = lists.length;

								if (p_callback && (typeof p_callback.fn == "function")) {
									p_callback.fn.call(p_callback.scope || this, p_callback.obj);
								}
							};

							/**
							 * Failure handler for Data Lists request
							 * 
							 * @method fnFailure
							 * @param response
							 *            {Object} Ajax response object literal
							 */
							var fnFailure = function DataLists_pDL_fnFailure(response) {
								this.showLoadingAjaxSpinner(false);

								if (response.status == 401) {
									// Our session has likely timed-out, so
									// refresh to
									// offer the login page
									window.location.reload();
								} else {
									this.dataLists = null;
									this.containerNodeRef = null;
									this.widgets.newList.set("disabled", true);
									var errorMsg = "";
									try {
										errorMsg = $html(YAHOO.lang.JSON.parse(response.responseText).message);
									} catch (e) {
										errorMsg = this.msg("message.error-unknown");
									}
									Alfresco.util.PopupManager.displayMessage({
										text : errorMsg
									});

								}
							};

							Alfresco.util.Ajax.jsonGet({
								url : $combine(Alfresco.constants.PROXY_URI, "becpg/entitylists/node", this.options.entityNodeRef.replace(":/", "")),
								successCallback : {

									fn : fnSuccess,
									obj : p_callback,
									scope : this
								},
								failureCallback : {
									fn : fnFailure,
									scope : this
								}
							});
						},

						showLoadingAjaxSpinner : function(showSpinner) {
							if (showSpinner) {
								this.widgets.spinner.show();
							} else {
								this.widgets.spinner.hide();
							}

						},

						/**
						 * Renders the Data Lists into the DOM
						 * 
						 * @method renderDataLists
						 * @param highlightName
						 *            {String} Optional name of list to
						 *            highlight after rendering
						 */
						renderDataLists : function DataLists_renderDataLists(p_highlightName) {
							var me = this, listsContainer = Dom.get(this.id + "-lists"), selectedClass = "selected";

							listsContainer.innerHTML = "";

							/**
							 * Click handler for selecting Data List
							 * 
							 * @method fnOnClick
							 */
							var fnOnClick = function DataLists_renderDataLists_fnOnClick() {
								return function DataLists_renderDataLists_onClick() {
									var lis = Selector.query("li", listsContainer);
									Dom.removeClass(lis, selectedClass);
									Dom.addClass(this, selectedClass);
									return true;
								};
							};

							/**
							 * Click handler for edit Data List
							 * 
							 * @method fnEditOnClick
							 * @param listName
							 *            {String} Name of the Data List
							 */
							var fnEditOnClick = function DataLists_renderDataLists_fnEditOnClick(listName, enabled) {
								return function DataLists_renderDataLists_onEditClick(e) {
									if (enabled) {
										me.onEditList(listName);
									}
									Event.stopEvent(e || window.event);
								};
							};

							/**
                             * Click handler for state change
                             * 
                             * @method fnEditOnClick
                             * @param listName
                             *            {String} Name of the Data List
                             */
							var fnChangeStateOnClick = function DataLists_renderDataLists_fnChangeStateOnClick(list, enabled) {

							    return function DataLists_renderDataLists_fnChangeStateOnClick(e) {
                                    if (enabled) {
                                        Alfresco.util.Ajax.request({
                                            method : Alfresco.util.Ajax.POST,
                                            url : Alfresco.constants.PROXY_URI + "becpg/entitylist/node/" + list.nodeRef
                                                  .replace(":/", "") + "?state=" + ("Valid" == list.state ? "ToValidate"
                                                  : "Valid"),
                                            successCallback : {
                                               fn : function(response) {
                                                   
                                                   list.state = "Valid" == list.state ? "ToValidate"
                                                           : "Valid";
                                                   
                                                  Alfresco.util.PopupManager.displayMessage({
                                                     text : me.msg("message.entitylist.state.change.success")
                                                  });
                                                  Bubbling.fire("dataListDetailsUpdated", {dataList: list});
                                               },
                                             scope : this
                                            }
                                        });
                                    }
                                    Event.stopEvent(e || window.event);
                                };
                            };

							
							
							/**
							 * Click handler for edit Data List
							 * 
							 * @method fnDeleteOnClick
							 * @param listName
							 *            {String} Name of the Data List
							 */
							var fnDeleteOnClick = function DataLists_renderDataLists_fnDeleteOnClick(listName, enabled) {
								return function DataLists_renderDataLists_onEditClick(e) {
									if (enabled) {
										me.onDeleteList(listName);
									}
									Event.stopEvent(e || window.event);
								};
							};

							try {
								var lists = this.dataLists, list, permissions, elHighlight = null, container, el, elLink, elText;

								if (this.dataListsLength === 0) {
									listsContainer.innerHTML = '<div class="no-lists">' + this.msg("message.no-lists") + '</div>';
								} else {
									container = document.createElement("ul");
									listsContainer.appendChild(container);

									for ( var index in lists) {
										if (lists.hasOwnProperty(index)) {

											list = lists[index];

											if (this.options.listId === null || this.options.listId.length < 1) {
												this.options.listId = list.name;
												if(this.options.sortOptions!=null && this.options.sortOptions.length>0){
												    for(var z in this.options.sortOptions){
			                                            if(list.name.indexOf(this.options.sortOptions[z].id)>-1){
			                                                if(this.options.sortOptions[z].isView){
			                                                    window.location.href = "entity-data-lists?list=" + $html(list.name) + "&nodeRef="
			                                                    + $html(this.options.entityNodeRef);
			                                                }
			                                                break;
			                                            }
												    }
												    
												}
											}

											permissions = list.permissions;

											// Build the DOM elements
											el = document.createElement("li");
											el.onclick = fnOnClick();
											                                   											
											
											
											elLink = document.createElement("a");
											elLink.title = list.description;
											elLink.href = "entity-data-lists?list=" + $html(list.name) + "&nodeRef="
													+ $html(this.options.entityNodeRef);
											
											if (permissions["edit"]) {
												var elEdit = document.createElement("span");
												elEdit.className = "edit";
												elEdit.title = this.msg("label.edit-list");
												elEdit.onclick = fnEditOnClick(list.name, true);
												elLink.appendChild(elEdit);
											}
											
											if (permissions["delete"]) {
												var elDelete = document.createElement("span");
												elDelete.className = "delete";
												elDelete.title = this.msg("label.delete-list");
												elDelete.onclick = fnDeleteOnClick(list.name, true);
												elLink.appendChild(elDelete);
											}
											
											elState = document.createElement("span");
											elState.className = "state";											
											if(permissions["changeState"] && list.name.indexOf("WUsed")<0){
												elState.title = this.msg("button.datalist-state.description");
												elState.onclick = fnChangeStateOnClick(list, true);
											}											

											if(list.name.indexOf("WUsed")>-1){
											    elState.className = "state WUsed";
											} else {
											    elState.className = "state "+list.name;
											}
                                            
											if (list.state && list.state.length > 0) {
											    elState.className += " " + list.state;
											}

											elText = document.createTextNode(list.title);

											// Build the DOM structure with the
											// new
											// elements
											elLink.appendChild(elState);																						
											elLink.appendChild(elText);
											el.appendChild(elLink);
											container.appendChild(el);

											// Mark current list as selected
											if (list.name == this.options.listId) {
												Dom.addClass(el, "selected");
											}

											// Make a note of a highlight
											// request match
											if (list.name == p_highlightName) {
												elHighlight = el;
											}
										}
									}

									if (elHighlight) {
										Alfresco.util.Anim.pulse(elHighlight);
									}
								}
							} catch (e) {
								listsContainer.innerHTML = '<span class="error">' + this.msg("message.error-unknown") + '</span>';
							}

						},


						/**
						 * YUI WIDGET EVENT HANDLERS Handlers for standard
						 * events fired from YUI widgets, e.g. "click"
						 */

						/**
						 * New List button click handler
						 * 
						 * @method onNewList
						 * @param e
						 *            {object} DomEvent
						 * @param p_obj
						 *            {object} Object passed back from
						 *            addListener method
						 */
						onNewList : function DataLists_onNewList(e, p_obj) {
							var destination = this.containerNodeRef.nodeRef, selectedClass = "theme-bg-selected", me = this;

							var fnPopulateItemTypes = function DataLists_onNewList_fnPopulateItemTypes(domId, formFieldId, p_form) {
								var fnOnClick = function DataLists_oNL_fnOnClick(myDiv, listType) {
									return function DataLists_oNL_onClick() {
										var divs = Selector.query("div", domId);
										Dom.removeClass(divs, selectedClass);
										Dom.addClass(myDiv, selectedClass);
										
										if("CustomView" == listType){
											
											var inputType =  Dom.get(formFieldId);
											
											var labelHtml = document.createElement('label');
											labelHtml.id = me.id+'-CustomViewLabel';
											labelHtml.innerHTML = '<label  for="'+formFieldId+'">'+me.msg("label.view.type.header")+'<span class="mandatory-indicator">(*)</span></label>';
											
											Dom.insertBefore(labelHtml,inputType);
											
											
											var nameHtml = document.createElement('div');
											nameHtml.id = me.id+'-CustomViewName';
											nameHtml.className = "form-field"; 
											nameHtml.innerHTML = '<label for="'+me.id+'-prop_cm_name">'+me.msg("label.view.name")+':<span class="mandatory-indicator">*</span> </label>'+
									         '<input type="text"  name="prop_cm_name" id="'+me.id+'-prop_cm_name"  class="mandatory" >';
											
											inputType.type= "text";
											inputType.value = "";
											
											Dom.insertAfter(nameHtml,inputType.parentNode);
											
											
										}  else {
										
											
											var element = document.getElementById(me.id+'-CustomViewLabel');
											if(element){
												element.parentNode.removeChild(element);
											}
										    element = document.getElementById(me.id+'-CustomViewName');
										    if(element){
												element.parentNode.removeChild(element);
											}
											
											Dom.get(formFieldId).type= "hidden";
											Dom.get(formFieldId).value = listType;
										}
										
										
										p_form.validate();
										return false;
									};
								};

								var list, el, containerEl = Dom.get(domId);

								for (var i = 0, ii = this.options.listTypes.length; i < ii; i++) {
									list = this.options.listTypes[i];
									el = document.createElement("div");

									el.innerHTML = '<h4>' + $html(list.title) + '</h4><span>' + $html(list.description) + '</span>';
									el.onclick = fnOnClick(el, list.name);
									containerEl.appendChild(el);
								}
							};

							// Intercept before dialog show
							var doBeforeDialogShow = function DataLists_onNewList_doBeforeDialogShow(p_form, p_dialog) {
								Alfresco.util.populateHTML([ p_dialog.id + "-dialogTitle", this.msg("label.new-list.title") ], [
										p_dialog.id + "-dialogHeader", this.msg("label.new-list.header") ], [ p_dialog.id + "-dataListItemType",
										this.msg("label.item-type") ]);
								fnPopulateItemTypes.apply(this, [ p_dialog.id + "-itemTypesContainer", p_dialog.id + "-dataListItemType-field",
										p_form ]);

								// Must choose a list type
								var fnValidateListChoice = function DataLists_oNL_dBDS_fnValidateListChoice(field, args, event, form, silent, message) {
									return (field.value.length > 0);
								};
								p_form.addValidation(p_dialog.id + "-dataListItemType-field", fnValidateListChoice, null, null, null, {
									validationType : "mandatory"
								});
							};

							var templateUrl = YAHOO.lang
									.substitute(
											Alfresco.constants.URL_SERVICECONTEXT
													+ "components/form?itemKind={itemKind}&itemId={itemId}&destination={destination}&mode={mode}&submitType={submitType}&showCancelButton=true",
											{
												itemKind : "type",
												itemId : "dl:dataList",
												destination : destination,
												mode : "create",
												submitType : "json"
											});

							// Using Forms Service, so always create new
							// instance
							var newList = new Alfresco.module.SimpleDialog(this.id + "-newList");

							newList
									.setOptions(
											{
												width : "33em",
												templateUrl : templateUrl,
												actionUrl : null,
												destroyOnHide : true,
												doBeforeDialogShow : {
													fn : doBeforeDialogShow,
													scope : this
												},
												onSuccess : {
													fn : function DataLists_onNewList_success(response) {
														var nodeRef = new Alfresco.util.NodeRef(response.json.persistedObject), listTitle = response.config.dataObj["prop_cm_title"], listTypeTitle = this
																.getListTypeTitle(response.config.dataObj["prop_dl_dataListItemType"]);

														Bubbling.fire("dataListCreated");
														Alfresco.util.PopupManager.displayMessage({
															text : this.msg("message.new-list.success", listTitle)
														});
														
													},
													scope : this
												},
												onFailure : {
													fn : function DataLists_onNewList_failure(response) {
														Alfresco.util.PopupManager.displayMessage({
															text : this.msg("message.new-list.failure")
														});
													},
													scope : this
												}
											}).show();
						},
						 /**
					       * Delete List event handler
					       *
					       * @method onDeleteList
					       * @param listName {string} Name of the list to edit
					       */
					     onDeleteList: function DataLists_onDeleteList(p_listName)
					      {
					         var datalist = this.dataLists[p_listName],
					            me = this;

					         var fnActionDeleteConfirm = function DataLists_onDeleteList_confirm(p_datalist)
					         {
					            var nodeRef = new Alfresco.util.NodeRef(p_datalist.nodeRef),
					               listTypeTitle = this.getListTypeTitle(p_datalist.itemType);
					            
					            Alfresco.util.Ajax.request(
					            {
					               method: Alfresco.util.Ajax.DELETE,
					               url: Alfresco.constants.PROXY_URI + "slingshot/datalists/list/node/" + nodeRef.uri,
					               successCallback:
					               {
					                  fn: function DataLists_onDeleteList_confirm_success(response, p_obj)
					                  {
					                  
					                        // If we deleted the current list, then redirect to "data-lists"
					                        if (p_obj.name == this.options.listId)
					                        {
					                           window.location = "entity-data-lists?nodeRef="
                                                    + $html(me.options.entityNodeRef);
					                           return;
					                        }

					                        Alfresco.util.PopupManager.displayMessage(
					                        {
					                           text: this.msg("message.delete-list.success")
					                        });
					                     
					                        delete this.dataLists[p_datalist.name];
					                        this.dataListsLength--;
					                        this.renderDataLists();
					                  },
					                  obj: p_datalist,
					                  scope: this
					                    
					               },
					               failureCallback:
					               {
					                  fn: function DataLists_onDeleteList_confirm_failure(response)
					                  {
					                     Alfresco.util.PopupManager.displayMessage(
					                     {
					                        text: this.msg("message.delete-list.failure")
					                     });
					                  },
					                  scope: this
					               }
					            });
					         };

					         Alfresco.util.PopupManager.displayPrompt(
					         {
					            title: this.msg("message.delete-list.title"),
					            text: this.msg("message.delete-list.description", $html(datalist.title)),
					            buttons: [
					            {
					               text: this.msg("button.delete"),
					               handler: function DataLists_onDeleteList_delete()
					               {
					                  this.destroy();
					                  fnActionDeleteConfirm.call(me, datalist);
					               }
					            },
					            {
					               text: this.msg("button.cancel"),
					               handler: function DataLists_onDeleteList_cancel()
					               {
					                  this.destroy();
					               },
					               isDefault: true
					            }]
					         });
					      }
						

					});
	
	
})();
