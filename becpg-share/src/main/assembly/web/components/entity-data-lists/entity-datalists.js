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
	var Dom = YAHOO.util.Dom, Bubbling = YAHOO.Bubbling, Event = YAHOO.util.Event;

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
						 * Id of the page layout that holds the lists panel and the content
						 */
						LAYOUT_ID : "alfresco-data-lists",

						/**
						 * Id of the lists panel, shared with the versions
						 */
						FILTERS_ID : "alf-filters",

						/**
						 * Class put on the layout when the panel is collapsed to the list icons
						 */
						COLLAPSED_CLASS : "datalists-collapsed",

						/**
						 * Browser storage key of the collapse choice
						 */
						COLLAPSED_STORAGE_KEY : "becpg.entityDataLists.collapsed",

						/**
						 * Name prefix of the where-used lists
						 */
						WUSED_LIST_PREFIX : "WUsed",

						/**
						 * State of a validated list
						 */
						VALID_STATE : "Valid",

						/**
						 * Part of the visible lists kept on screen when scrolling by one page, in pixels
						 */
						SCROLL_PAGE_OVERLAP : 40,

						/**
						 * Tolerance under which a list cut by the panel edge counts as visible, in pixels
						 */
						SCROLL_HINT_TOLERANCE : 4,

						/**
						 * Distance between a collapsed icon and its tooltip, in pixels
						 */
						TOOLTIP_GAP : 8,

						views : {
							"compoList" : "formulation-view",
							"processList": "formulation-view",
							"packagingList": "formulation-view",
							"taskList":"gantt-view",
							"surveyList":"survey-view",
							"ingLabelingList":"labeling-view"
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
							this.widgets.spinner.setBody('<div style="text-align:center;"><img class="icon16" src="' + Alfresco.constants.URL_RESCONTEXT
									+ 'components/images/lightbox/loading.gif" /></div>');
							this.widgets.spinner.render(document.body);

							if(this.options.showCreate){
    							this.widgets.newList = Alfresco.util.createYUIButton(this, "newListButton", this.onNewList, {
    								disabled : true
    							});
							}
							this.initRailTooltip();
							this.initScrollHints();
							this.initCollapse();

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
								if (this.entity && this.entity.entityTplId && this.entity.type) {
									var headerBarSpans = YAHOO.util.Dom.getElementsByClassName("entity", "span", this.id + "-headerBar");
									if (headerBarSpans && headerBarSpans.length > 0) {
										var typeCss = this.entity.type.split(":")[1];
										YAHOO.util.Dom.addClass(headerBarSpans[0], typeCss + "-" + this.entity.entityTplId);
									}
								}
								if(this.options.showCreate){
								    this.widgets.newList.set("disabled", !response.json.permissions.create);
								}   

							    if(this.options.sortOptions!=null && this.options.sortOptions.length>0){
							        
                                    lists.sort(function (listId1, listId2){
                                        var val1 = 500, val2 = 500;
                                        for(var z in me.options.sortOptions){
                                            if(listId1.name.indexOf(me.options.sortOptions[z].id) == 0){
                                                val1 = me.options.sortOptions[z].sortIndex;
                                                 if(listId1.name.indexOf("@")>0){
													val1+=listId1.name.split("@")[1];
												 }
                                                
                                            }
                                            if(listId2.name.indexOf(me.options.sortOptions[z].id) == 0){
                                                val2 = me.options.sortOptions[z].sortIndex;
                                                 if(listId2.name.indexOf("@")>0){
													val2+=listId2.name.split("@")[1];
												 }
                                            }
                                            
                                           
                                            
                                            
                                            if(val1!=500 && val2!=500){
                                            	break;
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
							var fnOnClick = function DataLists_renderDataLists_fnOnClick(listName ) {
								return function DataLists_renderDataLists_onClick() {

									if(listName!=null && (
											listName.indexOf("View-")<0
											&& me.options.listId.indexOf("View-")<0
											&& (me.views[listName] == me.views[me.options.listId])		
									) ){

										var lis = Selector.query("li", listsContainer);
										Dom.removeClass(lis, selectedClass);
										Dom.addClass(this, selectedClass);

										me.options.listId = listName;
										YAHOO.Bubbling.fire("activeDataListChanged", {
											list : me.options.listId,
											dataList : me.dataLists[me.options.listId],
											entity : me.entity,
											scrollTo : true
										});
										return false;
									}
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
							
							/**
							 * Click handler for edit Data List
							 * 
							 * @method fnDeleteOnClick
							 * @param listName
							 *            {String} Name of the Data List
							 */
							var fnDeleteChildrenOnClick = function DataLists_renderDataLists_fnDeleteChildrenOnClick(listName, enabled) {
								return function DataLists_renderDataLists_onEditClick(e) {
									if (enabled) {
										me.onDeleteChildrenList(listName);
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
											el.onclick =  fnOnClick(list.name);                                 											
											
											
											elLink = document.createElement("a");
											elLink.title = list.description;
											elLink.href = "entity-data-lists?list=" + $html(list.name) + "&nodeRef="
													+ $html(this.options.entityNodeRef);
											
											if (permissions["edit"]) {
												var elEdit = document.createElement("span");
												elEdit.className = "edit";
												elEdit.title = this.msg("label.edit-list");
												Event.addListener(elEdit, "click",  fnEditOnClick(list.name, true));
												elLink.appendChild(elEdit);
											}
											
											if (permissions["delete"]) {
												var elDelete = document.createElement("span");
												elDelete.className = "delete";
												elDelete.title = this.msg("label.delete-list");
												Event.addListener(elDelete, "click",  fnDeleteOnClick(list.name, true));
												
												var isTemplate = false;
												for(var index in this.entity.aspects){
													if(this.entity.aspects[index] === "bcpg:entityTplAspect"){
														isTemplate = true;
													}
												}
												
												var elDeleteChildren = null;
												
												if(isTemplate){
													var elDeleteChildren = document.createElement("span");
													elDeleteChildren.className = "delete";
													elDeleteChildren.title = this.msg("label.delete-list");
													Event.addListener(elDeleteChildren, "click",  fnDeleteChildrenOnClick(list.name, true));
													elLink.appendChild(elDeleteChildren);
												} else {
												
													
													elLink.appendChild(elDelete);
												}
												
											}
											
											elState = document.createElement("span");
											elState.className = "state";											
											if(permissions["changeState"] && list.name.indexOf("WUsed")<0){
												elState.title = this.msg("button.datalist-state.description");
												Event.addListener(elState, "click",  fnChangeStateOnClick(list, true));
											}											

											if(list.name.indexOf("WUsed")>-1){
											    elState.className = "state WUsed";
											} else {
											    elState.className = "state dl-"+list.name;
											}
                                            
											if (list.state && list.state.length > 0) {
											    elState.className += " " + list.state;
											}

											elText = document.createTextNode(list.title);
											this.setRailTooltip(el, list);

											// Build the DOM structure with the
											// new
											// elements
											elLink.appendChild(this.createListIcon(list));
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

							this.refreshScrollHints();
						},

						/**
						 * Creates the icon displayed in front of a list when the panel is collapsed. The icon
						 * class is the list type, so the type icons of beCPG.css apply.
						 *
						 * @method createListIcon
						 * @param list
						 *            {Object} The data list
						 * @return {HTMLElement} The icon element
						 */
						createListIcon : function DataLists_createListIcon(list) {
							var icon = document.createElement("span");
							icon.className = "list-icon " + this.getListIconClass(list);
							return icon;
						},

						/**
						 * Returns the icon class of a list: the local name of its type, or its name for the
						 * views and the where-used lists, which have no type.
						 *
						 * @method getListIconClass
						 * @param list
						 *            {Object} The data list
						 * @return {string} The icon class
						 */
						getListIconClass : function DataLists_getListIconClass(list) {
							if (list.name.indexOf(this.WUSED_LIST_PREFIX) > -1) {
								return this.WUSED_LIST_PREFIX;
							}
							if (list.itemType) {
								return list.itemType.split(":")[1];
							}
							return list.name.split("@")[0];
						},

						/**
						 * Stores on a list item what its tooltip shows when the panel is collapsed: the list
						 * title and its validation state.
						 *
						 * @method setRailTooltip
						 * @param el
						 *            {HTMLElement} The list item
						 * @param list
						 *            {Object} The data list
						 */
						setRailTooltip : function DataLists_setRailTooltip(el, list) {
							el.setAttribute("data-rail-tip", list.title);
							if (list.name.indexOf(this.WUSED_LIST_PREFIX) < 0) {
								var valid = list.state === this.VALID_STATE;
								el.setAttribute("data-rail-tip-detail", this.msg(valid ? "label.list-state.valid" : "label.list-state.to-validate"));
								el.setAttribute("data-rail-tip-tone", valid ? "valid" : "pending");
							}
						},

						/**
						 * Collapses the panel to the list icons when the user chose so last time.
						 *
						 * @method initCollapse
						 */
						initCollapse : function DataLists_initCollapse() {
							var toggle = Dom.get(this.id + "-collapseToggle");
							this.layoutEl = Dom.get(this.LAYOUT_ID);
							if (!toggle || !this.layoutEl) {
								return;
							}
							Event.addListener(toggle, "click", this.onCollapseToggleClick, this, true);
							var entity = Dom.getElementsByClassName("entity", "span", this.id + "-headerBar")[0];
							if (entity) {
								entity.setAttribute("data-rail-tip", entity.textContent);
								entity.setAttribute("data-rail-tip-detail", this.msg("label.expand-lists"));
								Event.addListener(entity, "click", this.onEntityIconClick, this, true);
							}
							if (this.readCollapsedChoice()) {
								this.setCollapsed(true);
							}
						},

						/**
						 * Fired when the user clicks on the collapse button of the header
						 *
						 * @method onCollapseToggleClick
						 * @param event
						 *            {Object} The click event
						 */
						onCollapseToggleClick : function DataLists_onCollapseToggleClick(event) {
							Event.preventDefault(event);
							this.setCollapsed(true);
							this.saveCollapsedChoice(true);
						},

						/**
						 * Fired when the user clicks on the entity icon, which expands the collapsed panel
						 *
						 * @method onEntityIconClick
						 */
						onEntityIconClick : function DataLists_onEntityIconClick() {
							if (this.isCollapsed()) {
								this.setCollapsed(false);
								this.saveCollapsedChoice(false);
							}
						},

						/**
						 * Tells whether the panel is collapsed to the list icons
						 *
						 * @method isCollapsed
						 * @return {boolean} true if collapsed
						 */
						isCollapsed : function DataLists_isCollapsed() {
							return !!this.layoutEl && Dom.hasClass(this.layoutEl, this.COLLAPSED_CLASS);
						},

						/**
						 * Collapses or expands the panel, then lets the page lay itself out again
						 *
						 * @method setCollapsed
						 * @param collapsed
						 *            {boolean} true to collapse
						 */
						setCollapsed : function DataLists_setCollapsed(collapsed) {
							if (collapsed) {
								Dom.addClass(this.layoutEl, this.COLLAPSED_CLASS);
							} else {
								Dom.removeClass(this.layoutEl, this.COLLAPSED_CLASS);
							}
							this.hideRailTooltip();
							this.refreshScrollHints();
							this.notifyLayoutChange();
						},

						/**
						 * Tells the components laid out by the resizer that the panel width changed
						 *
						 * @method notifyLayoutChange
						 */
						notifyLayoutChange : function DataLists_notifyLayoutChange() {
							Bubbling.fire("resizerChanged", {
								width : Dom.get(this.FILTERS_ID).offsetWidth
							});
							var resizeEvent = document.createEvent("HTMLEvents");
							resizeEvent.initEvent("resize", true, false);
							window.dispatchEvent(resizeEvent);
						},

						/**
						 * Reads the collapse choice, kept in the browser like a layout preference
						 *
						 * @method readCollapsedChoice
						 * @return {boolean} true if the user collapsed the panel last time
						 */
						readCollapsedChoice : function DataLists_readCollapsedChoice() {
							try {
								return window.localStorage.getItem(this.COLLAPSED_STORAGE_KEY) === "true";
							} catch (e) {
								return false;
							}
						},

						/**
						 * Keeps the collapse choice for the next pages
						 *
						 * @method saveCollapsedChoice
						 * @param collapsed
						 *            {boolean} true if collapsed
						 */
						saveCollapsedChoice : function DataLists_saveCollapsedChoice(collapsed) {
							try {
								window.localStorage.setItem(this.COLLAPSED_STORAGE_KEY, String(collapsed));
							} catch (e) {
								// Storage refused (private window): the choice only lasts for this page
							}
						},

						/**
						 * Wires the arrows that show there are more lists above or below the visible ones
						 *
						 * @method initScrollHints
						 */
						initScrollHints : function DataLists_initScrollHints() {
							Event.addListener(this.id + "-lists", "scroll", this.refreshScrollHints, this, true);
							Event.addListener(this.id + "-listsUp", "click", this.onScrollHintClick, -1, this);
							Event.addListener(this.id + "-listsDown", "click", this.onScrollHintClick, 1, this);
							Event.addListener(window, "resize", this.refreshScrollHints, this, true);
						},

						/**
						 * Scrolls the lists by one page in the arrow direction
						 *
						 * @method onScrollHintClick
						 * @param event
						 *            {Object} The click event
						 * @param direction
						 *            {int} -1 to scroll up, 1 to scroll down
						 */
						onScrollHintClick : function DataLists_onScrollHintClick(event, direction) {
							var lists = Dom.get(this.id + "-lists"), delta = direction * (lists.clientHeight - this.SCROLL_PAGE_OVERLAP);
							if (typeof lists.scrollBy === "function") {
								lists.scrollBy({
									top : delta,
									behavior : "smooth"
								});
							} else {
								lists.scrollTop += delta;
							}
						},

						/**
						 * Shows the up and down arrows when lists are hidden on that side
						 *
						 * @method refreshScrollHints
						 */
						refreshScrollHints : function DataLists_refreshScrollHints() {
							var lists = Dom.get(this.id + "-lists"), up = Dom.get(this.id + "-listsUp"), down = Dom.get(this.id + "-listsDown");
							if (!lists || !up || !down) {
								return;
							}
							var hidden = this.countHiddenLists(lists);
							this.toggleScrollHint(up, hidden.above);
							this.toggleScrollHint(down, hidden.below);
						},

						/**
						 * Displays an arrow and its tooltip when some lists are hidden on its side
						 *
						 * @method toggleScrollHint
						 * @param hint
						 *            {HTMLElement} The arrow
						 * @param hiddenCount
						 *            {int} Number of lists hidden on its side
						 */
						toggleScrollHint : function DataLists_toggleScrollHint(hint, hiddenCount) {
							if (hiddenCount > 0) {
								Dom.addClass(hint, "visible");
								hint.title = this.msg(hiddenCount > 1 ? "label.more-lists" : "label.more-list", hiddenCount);
							} else {
								Dom.removeClass(hint, "visible");
							}
						},

						/**
						 * Counts the lists hidden above and below the visible part of the panel
						 *
						 * @method countHiddenLists
						 * @param lists
						 *            {HTMLElement} The scrollable lists container
						 * @return {Object} The counts, as {above, below}
						 */
						countHiddenLists : function DataLists_countHiddenLists(lists) {
							var items = lists.getElementsByTagName("li"), box = lists.getBoundingClientRect(), hidden = {
								above : 0,
								below : 0
							};
							for (var i = 0; i < items.length; i++) {
								var item = items[i].getBoundingClientRect();
								if (item.top < box.top - this.SCROLL_HINT_TOLERANCE) {
									hidden.above++;
								} else if (item.bottom > box.top + lists.clientHeight + this.SCROLL_HINT_TOLERANCE) {
									hidden.below++;
								}
							}
							return hidden;
						},

						/**
						 * Creates the tooltip that names the lists and the branches when the panel is collapsed
						 *
						 * @method initRailTooltip
						 */
						initRailTooltip : function DataLists_initRailTooltip() {
							this.widgets.railTooltip = document.createElement("div");
							this.widgets.railTooltip.className = "datalists-rail-tooltip";
							document.body.appendChild(this.widgets.railTooltip);
							Event.addListener(this.FILTERS_ID, "mouseover", this.onRailMouseOver, this, true);
							Event.addListener(this.FILTERS_ID, "mouseout", this.hideRailTooltip, this, true);
						},

						/**
						 * Shows the tooltip of the hovered list or branch
						 *
						 * @method onRailMouseOver
						 * @param event
						 *            {Object} The mouseover event
						 */
						onRailMouseOver : function DataLists_onRailMouseOver(event) {
							var target = this.isCollapsed() ? this.findRailTooltipTarget(Event.getTarget(event)) : null;
							if (target) {
								this.showRailTooltip(target);
							} else {
								this.hideRailTooltip();
							}
						},

						/**
						 * Returns the closest ancestor carrying a tooltip, within the panel
						 *
						 * @method findRailTooltipTarget
						 * @param el
						 *            {HTMLElement} The hovered element
						 * @return {HTMLElement} The element carrying the tooltip, or null
						 */
						findRailTooltipTarget : function DataLists_findRailTooltipTarget(el) {
							while (el && el.id !== this.FILTERS_ID) {
								if (el.getAttribute && el.getAttribute("data-rail-tip")) {
									return el;
								}
								el = el.parentNode;
							}
							return null;
						},

						/**
						 * Fills the tooltip from the target attributes and puts it on the right of the target
						 *
						 * @method showRailTooltip
						 * @param target
						 *            {HTMLElement} The element carrying the tooltip
						 */
						showRailTooltip : function DataLists_showRailTooltip(target) {
							var tooltip = this.widgets.railTooltip, detail = target.getAttribute("data-rail-tip-detail"),
								html = $html(target.getAttribute("data-rail-tip"));
							if (detail) {
								html += '<em class="' + $html(target.getAttribute("data-rail-tip-tone") || "") + '">' + $html(detail) + '</em>';
							}
							tooltip.innerHTML = html;
							tooltip.style.display = "block";
							var box = target.getBoundingClientRect();
							tooltip.style.left = (box.right + this.TOOLTIP_GAP) + "px";
							tooltip.style.top = (box.top + (box.height - tooltip.offsetHeight) / 2) + "px";
						},

						/**
						 * Hides the tooltip
						 *
						 * @method hideRailTooltip
						 */
						hideRailTooltip : function DataLists_hideRailTooltip() {
							if (this.widgets.railTooltip) {
								this.widgets.railTooltip.style.display = "none";
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
								var fnOnClick = function DataLists_oNL_fnOnClick(myDiv, listType, listTitle) {
									return function DataLists_oNL_onClick() {
										var divs = Selector.query("div", domId);
										Dom.removeClass(divs, selectedClass);
										Dom.addClass(myDiv, selectedClass);
										
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
											
											
										} else if(Dom.get(me.id+"-newList_prop_cm_title")) {
											Dom.get(me.id+"-newList_prop_cm_title").value = listTitle;
										}
										
										
										p_form.validate();
										return false;
									};
								};

								var list, el, containerEl = Dom.get(domId);
								
								this.options.listTypes.sort(function (a, b){
										if(a == null){
											return -1;
										} 
										if(b == null){
											return 1;
										}
										return ('' + a.title).localeCompare(b.title);
									});

								for (var i = 0, ii = this.options.listTypes.length; i < ii; i++) {
									list = this.options.listTypes[i];
									el = document.createElement("div");

									if(list.title!=null && list.title.length > 0){
										el.innerHTML = '<h4>' + $html(list.title) + '</h4><span>' + $html(list.description) + '</span>';
										el.onclick = fnOnClick(el, list.name, list.title);
										containerEl.appendChild(el);
									}
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
					      },
	
					/**
					       * Delete List and children event handler
					       *
					       * @method onDeleteChildrenList
					       * @param listName {string} Name of the list to edit
					       */
					     onDeleteChildrenList: function DataLists_onDeleteChildrenList(p_listName)
					      {
					         var datalist = this.dataLists[p_listName],
					            me = this;
					         

					         var fnActionDeleteConfirm = function DataLists_onDeleteList_confirm(p_datalist)
					         {
					            var nodeRef = new Alfresco.util.NodeRef(p_datalist.nodeRef),
					               listTypeTitle = this.getListTypeTitle(p_datalist.itemType);
					            
					            //get children here
					            
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
					         
					         var fnActionDeleteChildrenConfirm = function DataLists_onDeleteList_confirm(p_datalist)
					         {
					            var nodeRef = new Alfresco.util.NodeRef(p_datalist.nodeRef),
					               listTypeTitle = this.getListTypeTitle(p_datalist.itemType);
					            
					            Alfresco.util.Ajax.request(
					            {
					               method: Alfresco.util.Ajax.GET,
					               url: Alfresco.constants.PROXY_URI + "becpg/entity/entityTpl/" + this.options.entityNodeRef.replace(":/", "")
						            + "/datalistRecursiveDelete?datalist=" + datalist.name,
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
					               text: this.msg("button.delete-template"),
					               handler: function DataLists_onDeleteList_delete()
					               {
					                  this.destroy();
					                  fnActionDeleteConfirm.call(me, datalist);
					               }
					            },
					            {
					               text: this.msg("button.delete-children"),
					               handler: function DataLists_onDeleteChildrenList_delete()
					               {
					                  this.destroy();
					                  fnActionDeleteChildrenConfirm.call(me, datalist);
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
