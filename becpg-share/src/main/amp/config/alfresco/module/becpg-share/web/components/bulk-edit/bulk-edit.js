/*******************************************************************************
 *  Copyright (C) 2010-2018 beCPG. 
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
 * Bulk Edit: BulkEdit component.
 * 
 * @author "Matthieu Laborie <matthieu.laborie@becpg.fr>"
 * @namespace beCPG
 * @class beCPG.component.BulkEdit
 */
(function() {
	/**
	 * YUI Library aliases
	 */
	var Dom = YAHOO.util.Dom, Event = YAHOO.util.Event, Selector = YAHOO.util.Selector, Bubbling = YAHOO.Bubbling;

	/**
	 * Alfresco Slingshot aliases
	 */
	var $html = Alfresco.util.encodeHTML, $combine = Alfresco.util.combinePaths;
	/**
	 * BulkEdit constructor.
	 * 
	 * @param htmlId
	 *            {String} The HTML id of the parent element
	 * @return {beCPG.component.BulkEdit} The new BulkEdit instance
	 * @constructor
	 */
	beCPG.component.BulkEdit = function(htmlId) {
		beCPG.component.BulkEdit.superclass.constructor.call(this, "beCPG.component.BulkEdit", htmlId, [ "button", "container", "datasource",
				"datatable", "calendar", "paginator", "animation", "history" ]);

		this.rendererHelper = beCPG.module.EntityDataRendererHelper;
		// Initialise prototype properties
		this.datalistColumns = {};
		this.dataTableColumn = [];
		this.selectedFields = [];
		this.dataRequestFields = [];
		this.dataResponseFields = [];
		this.currentPage = 1;
		this.selectedItems = {};
		this.afterBulkEditUpdate = [];

		/**
		 * Decoupled event listeners
		 */

		YAHOO.Bubbling.on("selectedItemsChanged", this.onSelectedItemsChanged, this);
		YAHOO.Bubbling.on("selectedTypeChanged", this.onSelectedTypeChanged, this);
		YAHOO.Bubbling.on("dataItemUpdated", this.onDataItemUpdated, this);
		YAHOO.Bubbling.on("bulkDataChanged", this.onBulkEditShow, this);		
		YAHOO.Bubbling.on("folderSelected", this.onSimulationFolderSelected, this);

		/* Deferred list population until DOM ready */
		this.deferredListPopulation = new Alfresco.util.Deferred([ "onReady" ], {
			fn : this.populateBulkEdit,
			scope : this
		});

		return this;
	};

	/**
	 * Extend from Alfresco.component.Base
	 */
	YAHOO.extend(beCPG.component.BulkEdit, Alfresco.component.Base);

	/**
	 * Augment prototype with main class implementation, ensuring overwrite is
	 * enabled
	 */
	YAHOO.lang
			.augmentObject(
					beCPG.component.BulkEdit.prototype,
					{
						/**
						 * Object container for initialization options
						 * 
						 * @property options
						 * @type object
						 */
						options : {

							/**
							 * Current siteId
							 * 
							 * @property siteId
							 * @type string
							 */
							siteId : "",

							/**
							 * Current site title
							 * 
							 * @property siteTitle
							 * @type string
							 */
							siteTitle : "",

							/**
							 * Search term to use for the initial search
							 * 
							 * @property initialSearchTerm
							 * @type string
							 * @default ""
							 */
							initialSearchTerm : "",

							/**
							 * Search tag to use for the initial search
							 * 
							 * @property initialSearchTag
							 * @type string
							 * @default ""
							 */
							initialSearchTag : "",

							/**
							 * States whether all sites should be searched.
							 * 
							 * @property initialSearchAllSites
							 * @type boolean
							 */
							initialSearchAllSites : true,

							/**
							 * States whether repository should be searched.
							 * This is in preference to current or all sites.
							 * 
							 * @property initialSearchRepository
							 * @type boolean
							 */
							initialSearchRepository : false,

							/**
							 * Sort property to use for the initial search.
							 * Empty default value will use score relevance
							 * default.
							 * 
							 * @property initialSort
							 * @type string
							 * @default ""
							 */
							initialSort : "",

							/**
							 * Advanced Search query - forms data json format
							 * based search.
							 * 
							 * @property searchQuery
							 * @type string
							 * @default ""
							 */
							searchQuery : "",

							/**
							 * Flag indicating whether pagination is available
							 * or not.
							 * 
							 * @property usePagination
							 * @type boolean
							 * @default false
							 */
							usePagination : false,

							/**
							 * Initial page to show on load (otherwise taken
							 * from URL hash).
							 * 
							 * @property initialPage
							 * @type int
							 */
							initialPage : 1,

							/**
							 * Number of items per page
							 * 
							 * @property pageSize
							 * @type int
							 */
							pageSize : 75,
							/**
							 * @property pageSize
							 * @type int
							 */
							totalRecords : null,
							/**
							 * Max search results
							 */
							maxResults : 1000,

							/**
							 * Delay before showing "loading" message for slow
							 * data requests
							 * 
							 * @property loadingMessageDelay
							 * @type int
							 * @default 1000
							 */
							loadingMessageDelay : 1000,

							/**
							 * Advanced Search query - forms data json format
							 * based search.
							 * 
							 * @property searchQuery
							 * @type string
							 * @default null
							 */
							searchQuery : null,

							/**
							 * The type of item
							 */
							itemType : null,

							/**
							 * The formId
							 */
							formId : null,

							/**
							 * The formId used to edit selected fields
							 */
							editSelectedFormId : "create",

							/**
							 * Parent nodeRef
							 */
							nodeRef : null,
							
							/**
							 * EntityNodeRefs
							 */
							entityNodeRefs : null,

							/**
							 * Display thumbnail column
							 */
							showThumbnails : false
							
							
						},

						/**
						 * Registered metadata renderer helper
						 * 
						 * @property rendererHelper
						 * @type object
						 */
						rendererHelper : null,

						/**
						 * Current page being browsed.
						 * 
						 * @property currentPage
						 * @type int
						 * @default 1
						 */
						currentPage : null,

						currentSort : null,
						
						currentSortDir : null,
						
						queryExecutionId : null,
						
						currentQueryExecutionId : null,
						
						/**
						 * Total number of records (documents + folders) in the
						 * currentPath.
						 * 
						 * @property totalRecords
						 * @type int
						 * @default 0
						 */
						totalRecords : null,

						/**
						 * Object literal of selected states for visible items
						 * (indexed by nodeRef).
						 * 
						 * @property selectedItems
						 * @type object
						 */
						selectedItems : null,
					
						/**
						 * Is all page selected
						 * (indexed by nodeRef).
						 * 
						 * @property allPages
						 * @type boolean
						 */
						allPages: false,

						/**
						 * Deferred function calls for after a data grid update
						 * 
						 * @property afterBulkEditUpdate
						 * @type array
						 */
						afterBulkEditUpdate : null,

						/**
						 * Data List columns from Form configuration
						 * 
						 * @param datalistColumns
						 * @type Object
						 */
						datalistColumns : null,

						/**
						 * DataTable columns from Form configuration
						 * 
						 * @param dataTableColumn
						 * @type Object
						 */
						dataTableColumn : null,

						/**
						 * Fields sent in the data request
						 * 
						 * @param dataRequestFields
						 * @type Object
						 */
						dataRequestFields : null,

						/**
						 * Fields returned from the data request
						 * 
						 * @param dataResponseFields
						 * @type Object
						 */
						dataResponseFields : null,

						/**
						 * Fields selected to edit
						 * 
						 * @param selectedFields
						 * @type Object
						 */
						selectedFields : null,

						/**
						 * The widgets
						 */
						widgets : {},

						dataUrl : Alfresco.constants.PROXY_URI_RELATIVE + "becpg/entity/datalists/data/node",
			
						itemUrl : Alfresco.constants.PROXY_URI_RELATIVE + "becpg/entity/datalists/item/node/",

						columnsUrl : Alfresco.constants.URL_SERVICECONTEXT + "module/entity-datagrid/config/columns",

						saveFieldUrl : Alfresco.constants.PROXY_URI_RELATIVE + "becpg/bulkedit/save",

						/**
						 * DataTable Cell Renderers
						 */

						/**
						 * Returns selector custom datacell formatter
						 * 
						 * @method fnRenderCellSelected
						 */
						fnRenderCellSelected : function BulkEdit_fnRenderCellSelected() {
							var scope = this;

							/**
							 * Selector custom datacell formatter
							 * 
							 * @method renderCellSelected
							 * @param elCell
							 *            {object}
							 * @param oRecord
							 *            {object}
							 * @param oColumn
							 *            {object}
							 * @param oData
							 *            {object|string}
							 */
							return function BulkEdit_renderCellSelected(elCell, oRecord, oColumn, oData) {
								var editPermission = oRecord.getData().permissions.userAccess.edit;

								if (editPermission) {
									Dom.setStyle(elCell, "width", oColumn.width + "px");
									Dom.setStyle(elCell.parentNode, "width", oColumn.width + "px");

									elCell.innerHTML = '<input id="checkbox-' + oRecord.getId() + '" type="checkbox" name="fileChecked" value="'
											+ oData + '"' + (scope.selectedItems[oData] ? ' checked="checked">' : '>');
								}
							};
						},
						/**
						 * Returns selector custom datacell formatter
						 * 
						 * @method fnRenderCellSelected
						 */
						fnRenderCellThumbnail : function BulkEdit_fnRenderCellThumbnail() {

							/**
							 * Selector custom datacell formatter
							 * 
							 * @method renderCellSelected
							 * @param elCell
							 *            {object}
							 * @param oRecord
							 *            {object}
							 * @param oColumn
							 *            {object}
							 * @param oData
							 *            {object|string}
							 */
							return function BulkEdit_function_renderCellThumbnail(elCell, oRecord, oColumn, oData) {
								var columnWidth = 100, record = oRecord.getData(), desc = "";

								record.jsNode = {};
								record.jsNode.type = record.nodeType;

								var thumbName = record.itemData["prop_cm_name"].value, nodeRef = new Alfresco.util.NodeRef(record.nodeRef), extn = thumbName
										.substring(thumbName.lastIndexOf("."));

								desc = '<span class="thumbnail"><img src="' + Alfresco.constants.PROXY_URI + 'api/node/' + nodeRef.uri
										+ '/content/thumbnails/doclib?c=queue&ph=true" alt="' + extn + '" title="' + $html(thumbName) + '" /></span>';

								oColumn.width = columnWidth;

								Dom.setStyle(elCell, "width", oColumn.width + "px");
								Dom.setStyle(elCell.parentNode, "width", oColumn.width + "px");

								elCell.innerHTML = desc;
							};
						},
                        
						fnRenderCellCode : function BulkEdit_fnRenderCellCode(datalistColumn)
                        {

                            return function BulkEdit_renderCellCode(elCell, oRecord, oColumn, oData)
                            {
                                var html = "";
                                if (oRecord && oColumn)
                                {
                                    if (!oData)
                                    {
                                        oData = oRecord.getData("itemData")[oColumn.field];
                                    }
                                    if (oData)
                                    {
                                        oData = YAHOO.lang.isArray(oData) ? oData : [ oData ];
                                        for (var i = 0, ii = oData.length, data; i < ii; i++)
                                        {
                                            data = oData[i];
                                            html+= '<a href="' + Alfresco.util
                                                    .siteURL('entity-data-lists?list=View-properties&nodeRef=' + oRecord.getData("nodeRef"),{
                                                        site : oRecord.getData("site")!=null ? oRecord.getData("site").shortName :null
                                                    }) + '">' + $html(data.displayValue) + '</a>';
                                        }
                                    }
                                }
                                elCell.innerHTML = html;
                            };

                        },
						
						/**
						 * Returns actions custom datacell formatter
						 * 
						 * @method fnRenderCellActions
						 */
						fnRenderCellActions : function BulkEdit_fnRenderCellActions() {
							var scope = this;

							/**
							 * Actions custom datacell formatter
							 * 
							 * @method renderCellActions
							 * @param elCell
							 *            {object}
							 * @param oRecord
							 *            {object}
							 * @param oColumn
							 *            {object}
							 * @param oData
							 *            {object|string}
							 */
							return function BulkEdit_renderCellActions(elCell, oRecord, oColumn, oData) {

								var editPermission = oRecord.getData().permissions.userAccess.edit;

								if (editPermission) {
									Dom.setStyle(elCell, "width", oColumn.width + "px");
									Dom.setStyle(elCell.parentNode, "width", oColumn.width + "px");
									elCell.innerHTML = '<div id="' + scope.id + '-actions-' + oRecord.getId() + '" class="action-set simple" >'
											+ '<div class="onActionEdit"><a title="' + scope.msg("action.modify")
											+ '"  class="action-link" href="" rel="edit"><span>' + scope.msg("action.modify") + '</span></a></div>'
											+ '</div>';
								}
							};
						},
						/**
						 * @method onSelectedItemsChanged React on
						 *         onSelectedItemsChanged event
						 */
						onSelectedItemsChanged : function() {

							if(this.allPages){
								Dom.get(this.id + "-message").innerHTML = '<span class="info">'+this.msg("message.edit.allPages",this.totalRecords)+'</span>';
								Dom.removeClass(this.id + "-message", "hidden");
							} else {
								Dom.addClass(this.id + "-message", "hidden");
							}
							
							
                            var items = this.getSelectedItems(), item, userAccess = {}, itemAccess, menuItems = this.widgets.selectedItems
                                    .getMenu().getItems(), menuItem, actionPermissions, disabled, i, ii, disabledForAllPages;

                            // Check each item for user permissions
                            for (i = 0, ii = items.length; i < ii; i++)
                            {
                                item = items[i];

                                // Required user access level - logical AND of
                                // each
                                // item's permissions
                                itemAccess = item.permissions.userAccess;
                                for ( var index in itemAccess)
                                {
                                    if (itemAccess.hasOwnProperty(index))
                                    {
                                        userAccess[index] = (userAccess[index] === undefined ? itemAccess[index]
                                                : userAccess[index] && itemAccess[index]);
                                    }
                                }
                            }

                            // Now go through the menu items, setting the
                            // disabled flag
                            // appropriately
                            for ( var index in menuItems)
                            {
                                if (menuItems.hasOwnProperty(index))
                                {
                                    // Defaulting to enabled
                                    menuItem = menuItems[index];
                                    disabled = false;
                                    disabledForAllPages = true;

                                    if (menuItem.element.firstChild)
                                    {
                                        // Check permissions required - stored
                                        // in "rel"
                                        // attribute in the DOM
                                        if (menuItem.element.firstChild.rel && menuItem.element.firstChild.rel !== "")
                                        {
                                            // Comma-separated indicates and
                                            // "AND" match
                                            actionPermissions = menuItem.element.firstChild.rel.split(",");
                                            for (i = 0, ii = actionPermissions.length; i < ii; i++)
                                            {
                                                // Disable if the user doesn't
                                                // have ALL the
                                                // permissions
                                            	if(actionPermissions[i] != "allPages" && actionPermissions[i] != "supplier"){
	                                                if ((!userAccess[actionPermissions[i]] ))
	                                                {
	                                                    disabled = true;
	                                                    break;
	                                                }
                                            	} else {
                                            		if(actionPermissions[i] == "supplier") {
                                            			if(this.options.itemType != "bcpg:rawMaterial" 
                                            				&& this.options.itemType != "bcpg:packagingMaterial" 
                                            					&& this.options.itemType != "bcpg:supplier" ){
                                            				disabled = true;
                                            			}
                                            		} else {

                                            			disabledForAllPages = false;
                                            		}
                                            	}
                                            }
                                        }

                                        if(this.allPages && (this.queryExecutionId==null || disabledForAllPages)){
                                        	disabled = true;
                                        }
                                        
                                        menuItem.cfg.setProperty("disabled", disabled);
                                    }
                                }
                            }
                            this.widgets.selectedItems.set("disabled", (items.length === 0));
							
							
							
						},
						
						 /**
                         * Selected Items button click handler
                         * 
                         * @method onSelectedItems
                         * @param sType
                         *            {string} Event type, e.g. "click"
                         * @param aArgs
                         *            {array} Arguments array, [0] = DomEvent,
                         *            [1] = EventTarget
                         * @param p_obj
                         *            {object} Object passed back from subscribe
                         *            method
                         */
                        onSelectedItems : function EntityDataGrid_onSelectedItems(sType, aArgs, p_obj)
                        {
                            var domEvent = aArgs[0], eventTarget = aArgs[1];

                            // Check mandatory docList module is present

                            // Get the function related to the clicked item
                            var fn = Alfresco.util.findEventClass(eventTarget);
                            if (fn && (typeof this[fn] == "function"))
                            {
                                this[fn].call(this, this.getSelectedItems());
                            }

                            Event.preventDefault(domEvent);
                        },
						
						/**
						 * @method onSelectedTypeChanged React on
						 *         onSelectedTypeChanged event
						 */
						onSelectedTypeChanged : function() {
							this.populateBulkEdit();
							this.widgets.showButton.set("disabled", false);
						},

						/**
						 * Fired by YUI when parent element is available for
						 * scripting
						 * 
						 * @method onReady
						 */
						onReady : function BulkEdit_onReady() {
							var me = this;

							// Select type widget

							this.widgets.typeSelect = Alfresco.util.createYUIButton(this, "itemTypeSelect-button", this.onTypeSelect, {
								type : "menu",
								menu : "itemTypeSelect-menu",
								lazyloadmenu : false
							});

							this.widgets.typeSelect.getMenu().subscribe("click", function(p_sType, p_aArgs) {
								var menuItem = p_aArgs[1];
								if (menuItem) {
									me.widgets.typeSelect.set("label", menuItem.cfg.getProperty("text")+ " " + Alfresco.constants.MENU_ARROW_SYMBOL);
								}
							});

							// Item Select menu button
							this.widgets.itemSelect = Alfresco.util.createYUIButton(this, "itemSelect-button", this.onItemSelect, {
								type : "menu",
								menu : "itemSelect-menu",
								disabled : true
							});

							// select first
							var typeSelected = this.widgets.typeSelect.getMenu().getItem(0);
							if (typeSelected) {
								me.widgets.typeSelect.set("label", typeSelected.cfg.getProperty("text")+ " " + Alfresco.constants.MENU_ARROW_SYMBOL);
								var className = typeSelected._oAnchor.children[0].attributes[0].nodeValue;
								this.options.itemType = className.split("#")[0];
								this.options.formId = className.split("#")[1];
								this.options.editSelectedFormId = className.split("#")[2];
							}
							

                            // Selected Items menu button
                            this.widgets.selectedItems = Alfresco.util.createYUIButton(this, "selectedItems-button",
                                    this.onSelectedItems,
                                    {
                                        type : "menu",
                                        menu : "selectedItems-menu",
                                        lazyloadmenu : false,
                                        disabled : true
                                    });


							// Hook action events
							var fnActionHandler = function DataGrid_fnActionHandler(layer, args) {
								var owner = Bubbling.getOwnerByTagName(args[1].anchor, "div");
								if (owner !== null) {
									if (typeof me[owner.className] == "function") {
										args[1].stop = true;
										var asset = me.widgets.dataTable.getRecord(args[1].target.offsetParent).getData();
										me[owner.className].call(me, asset, owner);
									}
								}
								return true;
							};
							Bubbling.addDefaultAction("action-link", fnActionHandler);

							this.widgets.showButton = Alfresco.util.createYUIButton(this, "show-button", this.onBulkEditShow, {
								disabled : false
							});


							this.widgets.exportCSVButton = Alfresco.util.createYUIButton(this, "export-csv", this.onExportCSV, {
								disabled : true,
								value : "export"
							});

							this.widgets.showThumbnailsButton = Alfresco.util.createYUIButton(this, "show-thumbnails", this.onShowThumbnails, {
								type : "checkbox",
								value : this.options.showThumbnails,
								checked : this.options.showThumbnails
							});

							// Assume no list chosen for now
							Dom.removeClass(this.id + "-selectTypeMessage", "hidden");

							this.deferredListPopulation.fulfil("onReady");

							// Finally show the component body here to prevent
							// UI artifacts on YUI button decoration
							Dom.setStyle(this.id + "-body", "visibility", "visible");
						},

						/**
						 * Display an error message pop-up
						 * 
						 * @private
						 * @method _onDataListFailure
						 * @param response
						 *            {Object} Server response object from Ajax
						 *            request wrapper
						 * @param message
						 *            {Object} Object literal of the format:
						 * 
						 * <pre>
						 *       title: Dialog title string
						 *       text: Dialog body message
						 * </pre>
						 */
						_onDataListFailure : function BulkEdit__onDataListFailure(p_response, p_message) {
							Alfresco.util.PopupManager.displayPrompt({
								title : p_message.title,
								text : p_message.text,
								modal : true,
								buttons : [ {
									text : this.msg("button.ok"),
									handler : function BulkEdit__onDataListFailure_OK() {
										this.destroy();
									},
									isDefault : true
								} ]
							});

						},

						/**
						 * Retrieves the Data List from the Repository
						 * 
						 * @method populateBulkEdit
						 */
						populateBulkEdit : function BulkEdit_populateBulkEdit() {

							if (this.options.itemType != null) {
								// Query the visible columns for this list's
								// item type
								Alfresco.util.Ajax.jsonGet({
									url : $combine(this.columnsUrl + "?mode=bulk-edit&itemType=" + encodeURIComponent(this.options.itemType)
											+ "&formId=" + encodeURIComponent(this.options.formId)),
									successCallback : {
										fn : this.onDatalistColumns,
										scope : this
									},
									failureCallback : {
										fn : this._onDataListFailure,
										obj : {
											title : this.msg("message.error.columns.title"),
											text : this.msg("message.error.columns.description")
										},
										scope : this
									}
								});
							}
						},

						/**
						 * Data List column definitions returned from the
						 * Repository
						 * 
						 * @method onDatalistColumns
						 * @param response
						 *            {Object} Ajax data structure
						 */
						onDatalistColumns : function BulkEdit_onDatalistColumns(response) {
							this.datalistColumns = response.json.columns;
							// Set-up the bulk edit props picker
							this._setupPropsPicker();
							// Hide "no list" message
							Dom.addClass(this.id + "-selectTypeMessage", "hidden");
						},

					

						_buildDataParamsUrl : function BulkEdit__buildDataParamsUrl(pageSize,maxResults) {

							var site = this.options.initialSearchAllSites ? "" : this.options.siteId;
							var params = YAHOO.lang.substitute("dataListName=bulk-edit&site={site}&repo={repo}&itemType={itemType}&pageSize={pageSize}&maxResults={maxResults}", {
								site : encodeURIComponent(site),
								repo : (this.options.initialSearchRepository || this.options.searchQuery.length !== 0).toString(), // always
								maxResults : maxResults? maxResults : this.options.maxResults,
								itemType : encodeURIComponent(this.options.itemType),
								pageSize : pageSize? pageSize : this.options.pageSize
							});
							
							
							
							if(this.options.entityNodeRefs!=null) {
								params+="&entityNodeRef="+this.options.entityNodeRefs;
							}
							

							return params;
						},

						/**
						 * DataSource set-up and event registration
						 * 
						 * @method _setupDataSource
						 * @protected
						 * 
						 */
						_setupDataSource : function BulkEdit__setupDataSource() {

							for (var i = 0, ii = this.datalistColumns.length; i < ii; i++) {
								var column = this.datalistColumns[i], columnName = column.name.replace(":", "_"), fieldLookup = this
										._buildFormsName(column);

								if (this._isSelectedProp(fieldLookup)) {
									this.dataRequestFields.push(columnName);
									this.dataResponseFields.push(fieldLookup);
									this.dataTableColumn.push(column);
								}
								this.datalistColumns[fieldLookup] = column;
							}

							// DataSource definition
							this.widgets.dataSource = new YAHOO.util.DataSource(this.dataUrl + "?" + this._buildDataParamsUrl(), {
								connMethodPost : true,
								responseType : YAHOO.util.DataSource.TYPE_JSON,
								responseSchema : {
									resultsList : "items",
									metaFields : {
										startIndex : "startIndex",
										totalRecords : "totalRecords",
										queryExecutionId : "queryExecutionId"
									}
								}
							});
							this.widgets.dataSource.connMgr.setDefaultPostHeader(Alfresco.util.Ajax.JSON);

						},
						/**
						 * Check if the field is selected
						 */
						_isSelectedProp : function BulkEdit__isSelectedProp(propName) {

							if ("prop_cm_name" == propName || "prop_bcpg_code" == propName) {
								return true;
							}
							for ( var i in this.selectedFields) {
								if (propName == this.selectedFields[i].value) {
									if (this.selectedFields[i].checked) {
										return true;
									}
								}
							}

							return false;
						},
						/**
						 * DataTable set-up and event registration
						 * 
						 * @method _setupDataTable
						 * @protected
						 */
						_setupPropsPicker : function BulkEdit__setupPropsPicker() {
							var containerEl = Dom.get(this.id + "-itemProps-container"), html = "";
							if (containerEl != null) {
								var inc = 0;
								var colCount = 0;
								for (var i = 0, ii = this.datalistColumns.length; i < ii; i++) {

									var column = this.datalistColumns[i];

									var propName = this._buildFormsName(column);
									var propLabel = column.label;
									if (!(column.protectedField || column.disabled || "prop_cm_name" == propName || "prop_bcpg_code" == propName)) {

										var className = "";
										if (colCount < Math.floor(inc / 5)) {
											className = "reset ";
										}
										colCount = Math.floor(inc / 5);
										className += "column-" + colCount;

										html += '<li class="' + className + '"><input id="propSelected-' + i
												+ '" type="checkbox" name="propChecked" value="' + propName + '" /><label for="propSelected-' + i
												+ '" >' + propLabel + '</label></li>';
										inc++;
									}
								}

								containerEl.innerHTML = "<ul style=\"width:" + ((colCount + 1) * 20) + "em;\">" + html + "</ul>";

								this.selectedFields = Selector.query('input[type="checkbox"]', containerEl);
							}

						},

						/**
						 * DataTable set-up and event registration
						 * 
						 * @method _setupDataTable
						 * @protected
						 */
						_setupDataTable : function BulkEdit__setupDataTable(columns) {
							// YUI DataTable column definitions
							var columnDefinitions = [ {
								key : "nodeRef",
								label : "",
								sortable : false,
								formatter : this.fnRenderCellSelected(),
								width : 16
							} ];

							if (this.options.showThumbnails) {
								columnDefinitions.push({
									key : "thumbnail",
									label : "",
									sortable : false,
									formatter : this.fnRenderCellThumbnail(),
									width : 100
								}

								);
							}

							var column, colName;
							for (var i = 0, ii = this.dataTableColumn.length; i < ii; i++) {
								column = this.dataTableColumn[i];
								colName = this._buildFormsName(column);
								
								if (this._isSelectedProp(colName)) {

									columnDefinitions.push({
										key : this.dataResponseFields[i],
										label : column.label,
										sortable : (column.type == "property"),
										sortOptions : {
											field : this.dataResponseFields[i],
											sortFunction : this.rendererHelper.getSortFunction()
										},

										formatter : colName == "prop_bcpg_code"? this.fnRenderCellCode(column) :  this.rendererHelper.getCellFormatter(this),
										editor : colName == "prop_bcpg_code"? null : this.rendererHelper.getCellEditor(this, column, this.saveFieldUrl)
									});
								}
							}

							columnDefinitions.sort(function(a, b) {
								var keyA = a.key;
								var keyB = b.key;

								if (keyA == "nodeRef") {
									return -1;
								} else if (keyB == "nodeRef") {
									return 1;
								}

								if (keyA == "thumbnail") {
									return -1;
								} else if (keyB == "thumbnail") {
									return 1;
								}

								if (keyA == "prop_cm_name" && keyB != "prop_bcpg_code") {
									return -1;
								} else if (keyA == "prop_bcpg_code") {
									return -1;
								} else if (keyB == "prop_bcpg_code") {
									return 1;
								} else if (keyB == "prop_cm_name" && keyA != "prop_bcpg_code") {
									return 1;
								}

								return 0;

							});

							// Add actions as last column
							columnDefinitions.push({
								key : "actions",
								label : "",
								sortable : false,
								formatter : this.fnRenderCellActions(),
								width : 35
							});

							// DataTable definition
							var me = this;

							this.widgets.dataTable = new YAHOO.widget.DataTable(this.id + "-grid", columnDefinitions, this.widgets.dataSource, {
								renderLoopSize : this.options.usePagination ? 16 : 32,
								initialLoad : false,
								dynamicData : false,
								"MSG_EMPTY" : this.msg("message.empty"),
								"MSG_ERROR" : this.msg("message.error"),
								paginator : null
							});

							this.widgets.dataTable.handleDataReturnPayload = function EntityDataGrid_handleDataReturnPayload(oRequest, oResponse,
									oPayload) {
								if (me.widgets.paginator) {
									me.widgets.paginator.set('totalRecords', oResponse.meta.totalRecords);
									me.widgets.paginator.setPage(oResponse.meta.startIndex, true);
								}
								if(me.currentSort!=null){
									me.widgets.dataTable.set("sortedBy", {key:me.currentSort, dir:me.currentSortDir}); 
								}
								
								me.totalRecords =  oResponse.meta.totalRecords;
								me.queryExecutionId = oResponse.meta.queryExecutionId;
								return oResponse.meta;
							};


							// Override abstract function within DataTable to
							// set custom error message
							this.widgets.dataTable.doBeforeLoadData = function BulkEdit_doBeforeLoadData(sRequest, oResponse, oPayload) {
								if (oResponse.error) {
									try {
										var response = YAHOO.lang.JSON.parse(oResponse.responseText);
										me.widgets.dataTable.set("MSG_ERROR", response.message);
									} catch (e) {
										me._setDefaultDataTableErrors(me.widgets.dataTable);
									}
								}

								// We don't get an renderEvent for an empty
								// recordSet, but we'd like one anyway
								if (oResponse.results.length === 0) {
									this.fireEvent("renderEvent", {
										type : "renderEvent"
									});
								}

								// Must return true to have the "Loading..."
								// message replaced by the error message
								return true;
							};

							// Override default function so the "Loading..."
							// message is suppressed
							this.widgets.dataTable.doBeforeSortColumn = function BulkEdit_doBeforeSortColumn(oColumn, sSortDir) {
								
								if(oColumn.key.indexOf("prop_")==0){
									me.currentSort = oColumn.key;
									me.currentSortDir = sSortDir;	
									me.currentPage = 1;
									me.currentQueryExecutionId = null;						
									me._updateBulkEdit.call(me);						
									return false;
								
								} else {
									me.currentSort = null;
									return true;
								}
							};

							// File checked handler
							this.widgets.dataTable.subscribe("checkboxClickEvent", function(e) {
								var id = e.target.value;
								this.selectedItems[id] = e.target.checked;
								Bubbling.fire("selectedItemsChanged");
							}, this, true);

							this.widgets.dataTable.subscribe("cellMouseoverEvent", function(oArgs) {
								var elCell = oArgs.target;
								if (YAHOO.util.Dom.hasClass(elCell, "yui-dt-editable")) {
									this.highlightCell(elCell);
								}
							});
							this.widgets.dataTable.subscribe("cellMouseoutEvent", this.widgets.dataTable.onEventUnhighlightCell);
							this.widgets.dataTable.subscribe("cellClickEvent", this.widgets.dataTable.onEventShowCellEditor);
							this.widgets.dataTable.subscribe("cellUpdateEvent", this.onCellChanged);


							
							if (me.options.usePagination) {

								me.currentPage = parseInt(me.options.initialPage, 10);

								// YUI Paginator definition
								this.widgets.paginator = new YAHOO.widget.Paginator({
									containers : [ me.id + "-paginatorTop", me.id + "-paginatorBottom" ],
									rowsPerPage : me.options.pageSize,
									initialPage : this.options.initialPage,
									template : me.msg("pagination.template"),
									pageReportTemplate : me.msg("pagination.template.page-report"),
									previousPageLinkLabel : me.msg("pagination.previousPageLinkLabel"),
									nextPageLinkLabel : me.msg("pagination.nextPageLinkLabel")
								});

								var handlePagination = function EntityDataGrid_handlePagination(state, me) {
									me.currentPage = state.page;
									me.currentQueryExecutionId = me.queryExecutionId;
									me._updateBulkEdit.call(me);
								};

								this.widgets.paginator.subscribe("changeRequest", handlePagination, this);

								// Display the bottom paginator bar
								Dom.setStyle(me.id + "-bulk-editBarBottom", "display", "block");
								
								if (!this.widgets.paginator.getContainerNodes().length) {
			                           this.widgets.paginator.set('containers', this.widgets.dataTable
			                                 ._defaultPaginatorContainers(true));
			                    }

			                   this.widgets.paginator.render();
							}
						},

						/**
						 * Multi-item select button click handler
						 * 
						 * @method onItemSelect
						 * @param sType
						 *            {string} Event type, e.g. "click"
						 * @param aArgs
						 *            {array} Arguments array, [0] = DomEvent,
						 *            [1] = EventTarget
						 * @param p_obj
						 *            {object} Object passed back from subscribe
						 *            method
						 */
						onItemSelect : function BulkEdit_onItemSelect(sType, aArgs, p_obj) {
							var domEvent = aArgs[0], eventTarget = aArgs[1];

							// Select based upon the className of the clicked
							// item

							this.selectItems(Alfresco.util.findEventClass(eventTarget));
							Event.preventDefault(domEvent);
						},
						/**
						 * Multi-type select button click handler
						 * 
						 * @method onTypeSelect
						 * @param sType
						 *            {string} Event type, e.g. "click"
						 * @param aArgs
						 *            {array} Arguments array, [0] = DomEvent,
						 *            [1] = EventTarget
						 * @param p_obj
						 *            {object} Object passed back from subscribe
						 *            method
						 */
						onTypeSelect : function BulkEdit_onItemTypeSelect(sType, aArgs, p_obj) {
							var eventTarget = aArgs[1];

							// Select based upon the className of the clicked
							// item
							var className = Alfresco.util.findEventClass(eventTarget);
							this.options.itemType = className.split("#")[0];
							this.options.formId = className.split("#")[1];
							this.options.editSelectedFormId = className.split("#")[2];
							Bubbling.fire("selectedTypeChanged");

						},

						/**
						 * Public functions
						 * 
						 * Functions designed to be called form external sources
						 */

						/**
                         * Public function to get array of selected items
                         * 
                         * @method getSelectedItems
                         * @return {Array} Currently selected items
                         */
                        getSelectedItems : function EntityDataGrid_getSelectedItems()
                        {
                            var items = [], recordSet = this.widgets.dataTable.getRecordSet(), startRecord = 0, endRecord = recordSet
                                    .getLength(), record;

                            for (var i = startRecord; i <= endRecord; i++)
                            {
                                record = recordSet.getRecord(i);
                                if (record != null && this.selectedItems[record.getData("nodeRef")])
                                {
                                    items.push(record.getData());
                                }
                            }

                            return items;
                        },

                        /**
                         * Public function to select items by specified groups
                         * 
                         * @method selectItems
                         * @param p_selectType
                         *            {string} Can be one of the following:
                         * 
                         * <pre>
                         * selectAll - all items
                         * selectNone - deselect all
                         * selectInvert - invert selection
                         * </pre>
                         */
                        selectItems : function EntityDataGrid_selectItems(p_selectType)
                        {
                            var recordSet = this.widgets.dataTable.getRecordSet(), checks = Selector.query(
                                    'input[type="checkbox"]', this.widgets.dataTable.getTbodyEl()), startRecord = 0, len = checks.length, record, i, fnCheck;

                            switch (p_selectType)
                            {
                                case "selectAll":
                                case "selectAllPages":
                                    fnCheck = function(assetType, isChecked)
                                    {
                                        return true;
                                    };
                                    break;

                                case "selectNone":
                                    fnCheck = function(assetType, isChecked)
                                    {
                                        return false;
                                    };
                                    break;

                                case "selectInvert":
                                    fnCheck = function(assetType, isChecked)
                                    {
                                        return !isChecked;
                                    };
                                    break;
                                default:
                                    fnCheck = function(assetType, isChecked)
                                    {
                                        return isChecked;
                                    };
                            }

                            
                            if(p_selectType == "selectAllPages"){
                            	this.allPages = true;
                            } else {
                            	this.allPages = false;
                            }
                            
                            
                            for (i = 0; i < len; i++)
                            {
                                record = recordSet.getRecord(i + startRecord);
                                this.selectedItems[record.getData("nodeRef")] = checks[i].checked = fnCheck(record
                                        .getData("type"), checks[i].checked);
                            }

                            Bubbling.fire("selectedItemsChanged");
                        },
						/**
						 * Fired when cell content changed
						 * 
						 * @method onCellChanged
						 */
						onCellChanged : function BulkEdit_onCellChanged(oRecord, oColumn, oldData) {

							// Dom.get("checkbox-" + oRecord.getId()
							// ).checked="checked";

							// Bubbling.fire("selectedItemsChanged");
						},

						/**
						 * BulkEdit show Required event handler
						 * 
						 * @method onBulkEditShow
						 * @param args
						 *            {array} Event parameters (unused)
						 */
						onBulkEditShow : function BulkEdit_onBulkEditShow(args) {
							this.dataRequestFields = [];
							this.dataResponseFields = [];
							this.dataTableColumn = [];

							// DataSource set-up and event registration
							this._setupDataSource();
							// DataTable set-up and event registration
							this._setupDataTable();

							// Enable item select menu
							this.widgets.itemSelect.set("disabled", false);
							this.widgets.exportCSVButton.set("disabled", false);
							this._updateBulkEdit.call(this);
						},

						/**
						 * PRIVATE FUNCTIONS
						 */

						/**
						 * Resets the YUI DataTable errors to our custom
						 * messages NOTE: Scope could be YAHOO.widget.DataTable,
						 * so can't use "this"
						 * 
						 * @method _setDefaultDataTableErrors
						 * @private
						 * @param dataTable
						 *            {object} Instance of the DataTable
						 */
						_setDefaultDataTableErrors : function BulkEdit__setDefaultDataTableErrors(dataTable) {
							var msg = Alfresco.util.message;
							dataTable.set("MSG_EMPTY", msg("message.empty", "beCPG.component.BulkEdit"));
							dataTable.set("MSG_ERROR", msg("message.error", "beCPG.component.BulkEdit"));
						},

						/**
						 * Updates all Data Grid data by calling repository
						 * webscript with current list details
						 * 
						 * @method _updateBulkEdit
						 */
						_updateBulkEdit : function BulkEdit__updateBulkEdit(p_obj) {
							p_obj = p_obj || {};
							var loadingMessage = null, timerShowLoadingMessage = null, me = this;

							// Clear the current document list if the data
							// webscript is taking too long
							var fnShowLoadingMessage = function BulkEdit_fnShowLoadingMessage() {
								// Check the timer still exists. This is to
								// prevent IE firing the event after we
								// cancelled it. Which is "useful".
								if (timerShowLoadingMessage) {
									loadingMessage = Alfresco.util.PopupManager.displayMessage({
										displayTime : 0,
										text : '<span class="wait">' + $html(this.msg("message.loading")) + '</span>',
										noEscape : true
									});

									if (YAHOO.env.ua.ie > 0) {
										this.loadingMessageShowing = true;
									} else {
										loadingMessage.showEvent.subscribe(function() {
											this.loadingMessageShowing = true;
										}, this, true);
									}
								}
							};

							// Reset the custom error messages
							this._setDefaultDataTableErrors(this.widgets.dataTable);

							// Slow data webscript message
							this.loadingMessageShowing = false;
							timerShowLoadingMessage = YAHOO.lang.later(this.options.loadingMessageDelay, this, fnShowLoadingMessage);

							var destroyLoaderMessage = null;
							destroyLoaderMessage = function BulkEdit__uDG_destroyLoaderMessage() {
								if (timerShowLoadingMessage) {
									// Stop the "slow loading" timed function
									timerShowLoadingMessage.cancel();
									timerShowLoadingMessage = null;
								}

								if (loadingMessage) {
									if (this.loadingMessageShowing) {
										// Safe to destroy
										loadingMessage.destroy();
										loadingMessage = null;
									} else {
										// Wait and try again later. Scope
										// doesn't get set correctly with "this"
										if (destroyLoaderMessage != null) {
											YAHOO.lang.later(100, me, destroyLoaderMessage);
										}
									}
								}
							};

							var successHandler = function BulkEdit__uDG_successHandler(sRequest, oResponse, oPayload) {
								destroyLoaderMessage();

								this.currentPage = p_obj.page || 1;
								this.currentQueryExecutionId = null;
								this.widgets.dataTable.onDataReturnInitializeTable.call(this.widgets.dataTable, sRequest, oResponse, oPayload);
							};

							var failureHandler = function BulkEdit__uDG_failureHandler(sRequest, oResponse) {
								destroyLoaderMessage();
								// Clear out deferred functions
								this.afterBulkEditUpdate = [];

								if (oResponse.status == 401) {
									// Our session has likely timed-out, so
									// refresh to offer the login page
									window.location.reload(true);
								} else {
									try {
										var response = YAHOO.lang.JSON.parse(oResponse.responseText);
										this.widgets.dataTable.set("MSG_ERROR", response.message);
										this.widgets.dataTable.showTableMessage(response.message, YAHOO.widget.DataTable.CLASS_ERROR);
										if (oResponse.status == 404) {
											// Site or container not found -
											// deactivate controls
											Bubbling.fire("deactivateAllControls");
										}
									} catch (e) {
										this._setDefaultDataTableErrors(this.widgets.dataTable);
									}
								}
							};

							// Update the DataSource
							var requestParams = this._buildBulkEditParams();

							 this.widgets.dataSource.connMgr.setDefaultPostHeader(Alfresco.util.Ajax.JSON);

							if (Alfresco.util.CSRFPolicy.isFilterEnabled()) {
								me.widgets.dataSource.connMgr.initHeader(Alfresco.util.CSRFPolicy.getHeader(), Alfresco.util.CSRFPolicy.getToken(),
										false);
							}

							this.widgets.dataSource.sendRequest(YAHOO.lang.JSON.stringify(requestParams), {
								success : successHandler,
								failure : failureHandler,
								scope : this
							});
						},

						/**
						 * Build URI parameter string for doclist JSON data
						 * webscript
						 * 
						 * @method this._buildBulkEditParams
						 * @param p_obj.filter
						 *            {string} [Optional] Current filter
						 * @return {Object} Request parameters. Can be given
						 *         directly to Alfresco.util.Ajax, but must be
						 *         JSON.stringified elsewhere.
						 */
						_buildBulkEditParams : function BulkEdit__buildBulkEditParams(page) {
							var request = {
								fields : this.dataRequestFields,
								page :  page ? page : this.currentPage
							};
							
							if(this.currentSort!=null){
								request.sort = this.currentSort.replace("prop_","").replace("_",":")+"|"+((this.currentSortDir == "yui-dt-asc") ? "true" : "false");
							}
							
							if(this.currentQueryExecutionId != null) {
								request.queryExecutionId = this.currentQueryExecutionId;
							}
							
							if (this.options.nodeRef != null && this.options.nodeRef.length > 0) {
								request.filter = {
									filterId : "nodePath",
									filterData : this.options.nodeRef
								};
							} else if (this.options.searchQuery != null && this.options.searchQuery.length > 0) {
								request.filter = {
									filterId : "filterform",
									filterData : this.options.searchQuery
								};
								if (this.options.initialSearchTerm != null && this.options.initialSearchTerm.length > 0) {
								    request.extraParams = YAHOO.lang.JSON.stringify({ searchTerm : this.options.initialSearchTerm  });
    							}
								
							} else if (this.options.initialSearchTerm != null && this.options.initialSearchTerm.length > 0) {
								request.filter = {
									filterId : "fts",
									filterData : this.options.initialSearchTerm
								};
							} else if (this.options.initialSearchTag != null && this.options.initialSearchTag.length > 0) {
								request.filter = {
									filterId : "tag",
									filterData : this.options.initialSearchTag
								};
							}

							return request;
						},
						/**
						 * Build formsName parameter
						 */
						_buildFormsName : function BulkEdit__buildFormsName(col) {
							var formsName = "";
							if (col.type == "association") {
								formsName = "assoc_";

							} else {
								formsName = "prop_";
							}
							formsName += col.name.replace(/:/g, "_");
							return formsName;

						},
						onExportCSV : function BulkEdit_onExportCSV() {
							var fields = "";
							for ( var i in this.dataRequestFields) {
								if (fields.length > 0) {
									fields += "$";
								}
								fields += this.dataRequestFields[i];

							}
							
							var PAGE_SIZE = 5000;
							var CURRENT_PAGE = 1;
							window.location = this.dataUrl + "/export?format=xlsx&" + this._buildDataParamsUrl(PAGE_SIZE, PAGE_SIZE)
									+ "&metadata=" + encodeURIComponent(YAHOO.lang.JSON
		                           .stringify(this._buildBulkEditParams(CURRENT_PAGE)));
							
						},
						onShowThumbnails : function BulkEdit_onShowThumbnails() {
							this.options.showThumbnails = !this.options.showThumbnails;
							this.onBulkEditShow.call(this);
						},

						onEditSelected : function BulkEdit_onEditSelected() {
							

							// Intercept before dialog show
							var doBeforeDialogShow = function BulkEdit_onNewRow_doBeforeDialogShow(p_form, p_dialog) {
								Alfresco.util.populateHTML([ p_dialog.id + "-dialogTitle", this.msg("label.edit-selected.title") ], [
										p_dialog.id + "-dialogHeader", this.msg("label.edit-selected.header") ]);

								if (Dom.get(p_dialog.id + "-form-bulkAction")) {
									Dom.setStyle(p_dialog.id + "-form-bulkAction", 'display', 'none');
									Dom.setStyle(p_dialog.id + "-form-bulkAction-msg", 'display', 'none');
								}

							};
							var displayFields = [];
							for ( var i in this.selectedFields) {
								if (this.selectedFields[i].checked) {
									displayFields.push(this.selectedFields[i].value);
								}
							}

							if (displayFields.length < 1) {
								Alfresco.util.PopupManager.displayMessage({
									text : this.msg("message.edit-selected.nofields")
								});
								return false;
							}
							
							//Warning before editing ERP code
							
							var popupKind = "bulk-edit",  li = '', colCount = 0;
							var html = '<div class="hd">' + this.msg("header." + popupKind + ".picker") + '</div>';
				       		html += '<div class="bd">';
				       		html += '<form  class="form-container">';
				       		html += '<div class="form-fields '+popupKind+'">';
				       		html += '   <div class="set">';
				       		html += '        <div class="form-field">';
				       		html += '			<div  id="'+this.id+'-columns-list" />';
				       		
				       		for(i in displayFields){
				       			var obj = this.datalistColumns.find(function(element){ return element.formsName == displayFields[i]});
				       			li += '<li><input id="propSelected-' + i + '"  type="checkbox" name="propChecked" value="'+ displayFields[i] + '" /> <label for="propSelected-' + i + '" >' + obj.label + '</label></li>';
				       		}
				       		
				       		html += '<span>'+this.msg("label.edit-columns.title") + '</span><br/><br/><ul style="width:' + ((colCount + 1) * 20) + 'em;">'+ li +'</ul>';	
				       		
				       		html += '          </div>';
				       		html += '       </div>';
				       		html += '    </div>';
				       		html += '<div id="'+this.id+'-'+popupKind+'-ft" class="bdft">';
				       		html += '</div>';
				       		html += '</form></div>';
				       		
			       			var containerDiv = document.createElement("div");
			       			containerDiv.innerHTML = html;
				       			
			       			this.widgets.columnsListPanel = Alfresco.util.createYUIPanel(containerDiv, {
			       				draggable : true,
			       				width : "33em"
			       			});
			       			
			       			var divEl = Dom.get(this.id+'-' + popupKind + '-ft');
			       			
			       			divEl.innerHTML = '<input id="'+this.id+'-bulk-edit-ok" type="button" value="'+this.msg("button.ok")+'" />';
			       			
			       			this.widgets.columnsListPanel.show();
			       			
			       			var containerEl = Dom.get(this.id+'-columns-list').parentNode
			       			
			       			this.widgets.okBkButton = Alfresco.util.createYUIButton(this, "bulk-edit-ok", function (){
				       			var me = this, selectedFields = Selector.query('input[type="checkbox"]', containerEl);
			       				displayFields = [];
			       				
			       				for ( var i in selectedFields) {	
			       					if(selectedFields[i].checked){
			       						displayFields.push(selectedFields[i].value);
			       					}	
			       				}
			       				
			       				me.widgets.columnsListPanel.hide();
			       				fnDialogShow.call(me);
			       				
				            });
	
			       			
			       			var fnDialogShow =  function(){
			       				var selectedNodeRef = this.getSelectedItems(), submissionParams = "";
			       				for ( var i in selectedNodeRef) {
			       					if (submissionParams.length > 0) {
			       						submissionParams += ",";
			       					}
			       					submissionParams += selectedNodeRef[i].nodeRef.replace(/workspace:\/\/SpacesStore\//g,"");
			       				}
			       				
			       				var templateUrl = YAHOO.lang
			       				.substitute(
			       						Alfresco.constants.URL_SERVICECONTEXT
			       						+ "components/form?formId={formId}&bulkEdit=true&fields={fields}&submissionUrl={submissionUrl}&itemKind={itemKind}&itemId={itemId}&mode={mode}&submitType={submitType}&showCancelButton=true",
			       						{
			       							itemKind : "type",
			       							formId : this.options.editSelectedFormId,
			       							itemId : this.options.itemType,
			       							mode : "create",
			       							submitType : "json",
			       							submissionUrl : encodeURIComponent("/becpg/bulkedit/type/" + this.options.itemType.replace(":", "_")
			       									+ "/bulksave?nodeRefs=" + submissionParams+"&allPages="+this.allPages+"&queryExecutionId="+this.queryExecutionId),
			       							fields : encodeURIComponent(displayFields)
			       						});
			       				
			       				// Using Forms Service, so always create new
			       				// instance
			       				var createRow = new Alfresco.module.SimpleDialog(this.id + "-bulk-createRow");
			       				
			       				var submit = displayFields.length > 1 ? false : true;
			       				var confirmMessage = "message.confirm.edit-columns.description";
			       				if(displayFields.length == 1 && displayFields[0] == "prop_bcpg_erpCode"){
			       					submit = false;
			       					confirmMessage = "message.confirm.edit-erpcode.description";
			       				}
			       				createRow.setOptions({
			       					width : "36em",
			       					templateUrl : templateUrl,
			       					actionUrl : null,
			       					destroyOnHide : true,
			       					doBeforeDialogShow : {
			       						fn : doBeforeDialogShow,
			       						scope : this
			       					},
			       					doBeforeFormSubmit : {
			       						fn: function(){
			       							if(!submit){
			       								
			       								Alfresco.util.PopupManager.displayPrompt({
			       									title : this.msg("message.confirm.edit-columns.title"),
			       									text : this.msg(confirmMessage),
			       									buttons : [ {
			       										text : this.msg("button.ok"),
			       										handler : function columnsUpdate() {
			       											this.destroy();
			       											submit = true;
			       											createRow.widgets.okButton._button.click();
			       										}
			       									}, {
			       										text : this.msg("button.cancel"),
			       										handler : function EntityDataGrid__onActionEdit_cancel() {
			       											this.destroy();
			       											submit = false;
			       										},
			       										isDefault : true
			       									} ]
			       								});
			       							
			       							}
			       						},
			       						scope: this
			       		          },

			       					doBeforeAjaxRequest : {
			       		             fn : function() {
			       		            	  return submit;
			       		              },
			       		             scope: this
			       		            },
			       					onSuccess : {
			       						fn : function BulkEdit_onNewRow_success(response) {
			       							YAHOO.Bubbling.fire("bulkDataChanged");
			       							Alfresco.util.PopupManager.displayMessage({
			       								text : this.msg("message.edit-selected.success")
			       							});
			       						},
			       						scope : this
			       					},
			       					onFailure : {
			       						fn : function BulkEdit_onNewRow_failure(response) {
			       							Alfresco.util.PopupManager.displayMessage({
			       								text : this.msg("message.edit-selected.failure", response.json.message)
			       							});
			       						},
			       						scope : this
			       					}
			       				}).show();
			       				
			       			}


						},
						
						onExportAsZip : function(){
							alert("Not yet implemented")
						},

						onSendToSupplier : function(){

							var selectedNodeRef = this.getSelectedItems(), submissionParams = "";
							for ( var i in selectedNodeRef) {
								if (submissionParams.length > 0) {
									submissionParams += ",";
								}
								submissionParams += selectedNodeRef[i].nodeRef;
							}

							var actionUrl = Alfresco.constants.PROXY_URI + "becpg/supplier/send-to-supplier?nodeRefs="+ 
							submissionParams+"&allPages="+this.allPages+"&queryExecutionId="+this.queryExecutionId;

							this.modules.sendToSupplier = new Alfresco.module.SimpleDialog(this.id + "-sendToSupplier").setOptions({
								width : "33em",
								templateUrl : Alfresco.constants.URL_SERVICECONTEXT + "modules/supplier/send-to-supplier",
								actionUrl : actionUrl,
								validateOnSubmit : false,
								destroyOnHide: true,
								firstFocus : this.id + "-sendToSupplier-projectTpl-field",
								doBeforeFormSubmit : {
									fn : function onActionSendToSupplier_doBeforeFormSubmit(form) {
										Alfresco.util.PopupManager.displayMessage({
											text : this.msg("message.send-to-supplier.inprogress")
										});
									},
									scope : this
								},
								onSuccess : {
									fn : function onActionSendToSupplier_success(response) {
										if (response.json) {
											window.location.href = beCPG.util.entityURL(recordSiteName,
													response.json.persistedObject, p_record.node.type);
										}
									},
									scope : this
								},
								onFailure : {
									fn : function onActionSendToSupplier_failure(response) {
										if(response.json && response.json.message){
											Alfresco.util.PopupManager.displayMessage({
												text : response.json.message
											});  
										} else {
											Alfresco.util.PopupManager.displayMessage({
												text : this.msg("message.import.failure")
											});
										}
									},
									scope : this
								}
							});

							this.modules.sendToSupplier.show();				 
						},

						
						
						onSimulateSelected : function BulkEdit_onSimulateSelected() {
							
							if (!this.modules.simulateFolderPicker)
					         {
					            this.modules.simulateFolderPicker = new Alfresco.module.DoclibGlobalFolder();
					         }
					         
					         this.modules.simulateFolderPicker.setOptions(
					         {
					            allowedViewModes: [ Alfresco.module.DoclibGlobalFolder.VIEW_MODE_SITE ],
					            title: this.msg("message.simulate-selected.chooseFolder", $html(this.options.siteTitle))
					         }).showDialog();
					         
					    
						},
						
						onSimulationFolderSelected : function BulkEdit_onSimulationFolderSelected(event, args) {
						     

							var destFolder = args[1].selectedFolder.nodeRef;
							
							if(destFolder!=null){
							
								var selectedNodeRef = this.getSelectedItems(), submissionParams = "";
								for ( var i in selectedNodeRef) {
									if (submissionParams.length > 0) {
										submissionParams += ",";
									}
									submissionParams += selectedNodeRef[i].nodeRef;
								}
								
				               Alfresco.util.Ajax
				                 .request({
				                    method : Alfresco.util.Ajax.POST,
				                    url : Alfresco.constants.PROXY_URI + "becpg/entity/simulation/create?nodeRefs=" 
				                    	+ submissionParams+"&allPages="+this.allPages+"&queryExecutionId="+this.queryExecutionId+"&destNodeRef="+destFolder+"&mode=recur",
				                    successCallback : {
				                       fn : function(resp) {
				                        	 Alfresco.util.PopupManager.displayMessage({
				 			                	 text : this.msg("message.branch-entities.inprogress")
				 			       		    });
				                       },
				                       scope : this
				                    }
				                 });
							}
						},
						
						
						/**
						 * ACTIONS WHICH ARE LOCAL TO THE DATAGRID COMPONENT
						 */

						/**
						 * Edit Data Item pop-up
						 * 
						 * @method onActionEdit
						 * @param item
						 *            {object} Object literal representing one
						 *            data item
						 */
						onActionEdit : function BulkEdit_onActionEdit(item) {
							var scope = this;

							// Intercept before dialog show
							var doBeforeDialogShow = function DataGrid_onActionEdit_doBeforeDialogShow(p_form, p_dialog) {
								Alfresco.util.populateHTML([ p_dialog.id + "-dialogTitle", this.msg("label.edit-row.title") ]);

								// Is it a bulk action?
								if (Dom.get(p_dialog.id + "-form-bulkAction")) {
									Dom.get(p_dialog.id + "-form-bulkAction").checked = true;
									Dom.get(p_dialog.id + "-form-bulkAction-msg").innerHTML = this.msg("button.bulk-action-edit");
								}

							};

							var templateUrl = YAHOO.lang
									.substitute(
											Alfresco.constants.URL_SERVICECONTEXT
													+ "components/form?formId=bulk-edit&itemKind={itemKind}&itemId={itemId}&mode={mode}&submitType={submitType}&showCancelButton=true&siteId={siteId}",
											{
												itemKind : "node",
												itemId : item.nodeRef,
												mode : "edit",
												submitType : "json",
												siteId : this.options.siteId
											});

							// Using Forms Service, so always create new
							// instance
							var editDetails = new Alfresco.module.SimpleDialog(this.id + "bulk-editDetails");
							editDetails.setOptions({
								width : "850px",
								templateUrl : templateUrl,
								actionUrl : null,
								destroyOnHide : false,
								doBeforeDialogShow : {
									fn : doBeforeDialogShow,
									scope : this
								},
								onSuccess : {
									fn : function DataGrid_onActionEdit_success(response) {
										// Reload the node's metadata
										Alfresco.util.Ajax.jsonPost({
											url : this.itemUrl + new Alfresco.util.NodeRef(item.nodeRef).uri,
											dataObj : this._buildBulkEditParams(),
											successCallback : {
												fn : function DataGrid_onActionEdit_refreshSuccess(resp) {

													// Fire
													// "itemUpdated"
													// event
													Bubbling.fire("dataItemUpdated", {
														item : resp.json.item
													});

													// recall edit for
													// next item
													var checkBoxEl = Dom.get(this.id + "bulk-editDetails" + "-form-bulkAction");
													

													if (checkBoxEl && checkBoxEl.checked) {
														var recordFound = scope._findNextItemByParameter(resp.json.item.nodeRef, "nodeRef");
														if (recordFound != null) {
															scope.onActionEdit(recordFound);
														}
													}

													// Display success
													// message
													Alfresco.util.PopupManager.displayMessage({
														text : this.msg("message.details.success")
													});

												},
												scope : this
											},
											failureCallback : {
												fn : function DataGrid_onActionEdit_refreshFailure(resp) {
													Alfresco.util.PopupManager.displayMessage({
														text : this.msg("message.details.failure")
													});
												},
												scope : this
											}
										});
									},
									scope : this
								},
								onFailure : {
									fn : function DataGrid_onActionEdit_failure(resp) {
										Alfresco.util.PopupManager.displayMessage({
											text : this.msg("message.details.failure")
										});
									},
									scope : this
								}
							}).show();

						},
						/**
						 * Searches the recordSet for the next item of a record
						 * with the given parameter value
						 * 
						 * @method _findNextRecordByParameter
						 * @private
						 * @param p_value
						 *            {string} Value to find (to get the
						 *            previous)
						 * @param p_parameter
						 *            {string} Parameter to look for the value
						 *            in
						 */
						_findNextItemByParameter : function DataGrid__findNextItemByParameter(p_value, p_parameter) {
							var recordSet = this.widgets.dataTable.getRecordSet();
							for (var i = 0, j = recordSet.getLength(); i < j; i++) {
								if (recordSet.getRecord(i).getData(p_parameter) == p_value) {
									if ((i + 1) != j) {
										return recordSet.getRecord(i + 1).getData();
									}
								}
							}
							return null;
						},

						/**
						 * Data Item updated event handler
						 * 
						 * @method onDataItemUpdated
						 * @param layer
						 *            {object} Event fired
						 * @param args
						 *            {array} Event parameters (depends on event
						 *            type)
						 */
						onDataItemUpdated : function BulkEdit_onDataItemUpdated(layer, args) {
							var obj = args[1];
							if (obj && (obj.item !== null)) {
								var recordFound = this._findRecordByParameter(obj.item.nodeRef, "nodeRef");
								if (recordFound !== null) {
									this.widgets.dataTable.updateRow(recordFound, obj.item);
									var el = this.widgets.dataTable.getTrEl(recordFound);
									Alfresco.util.Anim.pulse(el);
								}
							}
						},

						/**
						 * Searches the current recordSet for a record with the
						 * given parameter value
						 * 
						 * @method _findRecordByParameter
						 * @private
						 * @param p_value
						 *            {string} Value to find
						 * @param p_parameter
						 *            {string} Parameter to look for the value
						 *            in
						 */
						_findRecordByParameter : function BulkEdit__findRecordByParameter(p_value, p_parameter) {
							var recordSet = this.widgets.dataTable.getRecordSet();
							for (var i = 0, j = recordSet.getLength(); i < j; i++) {
								if (recordSet.getRecord(i).getData(p_parameter) == p_value) {
									return recordSet.getRecord(i);
								}
							}
							return null;
						}

					}, true);

})();
