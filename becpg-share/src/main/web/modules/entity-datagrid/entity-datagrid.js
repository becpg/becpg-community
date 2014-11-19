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
/**
 * Entity Data Lists: EntityDataGrid component.
 * 
 * @namespace beCPG
 * @class beCPG.component.EntityDataGrid
 */
(function()
{

    /**
     * YUI Library aliases
     */
    var Dom = YAHOO.util.Dom, Event = YAHOO.util.Event, Selector = YAHOO.util.Selector, Bubbling = YAHOO.Bubbling;

    /**
     * Alfresco Slingshot aliases
     */
    var $html = Alfresco.util.encodeHTML, $links = Alfresco.util.activateLinks;

    /**
     * Entity DataGrid constructor.
     * 
     * @param htmlId
     *            {String} The HTML id of the parent element
     * @return {beCPG.component.EntityDataGrid} The new EntityDataGrid instance
     * @constructor
     */
    beCPG.module.EntityDataGrid = function(htmlId, scopeable)
    {

        beCPG.module.EntityDataGrid.superclass.constructor.call(this, "beCPG.module.EntityDataGrid", htmlId, [
                "button", "container", "datasource", "datatable", "paginator", "animation", "history" ]);

        // Initialise prototype properties
        this.rendererHelper = beCPG.module.EntityDataRendererHelper;
        this.datalistMeta = {};
        this.datalistColumns = {};
        this.dataRequestFields = [];
        this.dataResponseFields = [];
        this.currentPage = 1;
        this.showingMoreActions = false;
        this.currentFilter =
        {
            filterId : "all",
            filterData : ""
        };
        this.selectedItems = {};
        this.afterDataGridUpdate = [];
        this.extraAfterDataGridUpdate = [];

        this.scopeId = "";
        if (scopeable)
        {
            this.scopeId = htmlId;
        }

        /**
         * Decoupled event listeners
         */

        // Data Lists components events
        Bubbling.on("activeDataListChanged", this.onActiveDataListChanged, this);
        Bubbling.on("dataListDetailsUpdated", this.onDataListDetailsUpdated, this);

        Bubbling.on("refreshDataGrids", this.onDataGridRefresh, this);

        Bubbling.on(this.scopeId + "columnRenamed", this.onColumnRenamed, this);

        // Local Events
        Bubbling.on(this.scopeId + "scopedActiveDataListChanged", this.onActiveDataListChanged, this);
        Bubbling.on(this.scopeId + "userAccess", this.onUserAccess, this);
        Bubbling.on(this.scopeId + "filterChanged", this.onFilterChanged, this);
        Bubbling.on(this.scopeId + "changeFilter", this.onChangeFilter, this);
        Bubbling.on(this.scopeId + "versionChangeFilter", this.onChangeFilter, this);
        Bubbling.on(this.scopeId + "dataItemCreated", this.onDataItemCreated, this);
        Bubbling.on(this.scopeId + "dataItemUpdated", this.onDataItemUpdated, this);
        Bubbling.on(this.scopeId + "dataItemsDeleted", this.onDataItemsDeleted, this);
        Bubbling.on(this.scopeId + "dataItemsDuplicated", this.onDataGridRefresh, this);
        Bubbling.on(this.scopeId + "refreshDataGrid", this.onDataGridRefresh, this);
        Bubbling.on(this.scopeId + "selectedItemsChanged", this.onSelectedItemsChanged, this);

        // Form filter
        Bubbling.on("beforeFormRuntimeInit", this.onBeforeFormRuntimeInit, this);

        /* Deferred list population until DOM ready */
        this.deferredListPopulation = new Alfresco.util.Deferred([ "onReady", "onActiveDataListChanged" ],
        {
            fn : this.populateDataGrid,
            scope : this
        });

        return this;
    };

    /**
     * Extend from Alfresco.component.Base
     */
    YAHOO.extend(beCPG.module.EntityDataGrid, Alfresco.component.Base);

    /**
     * Augment prototype with Common Actions module
     */
    YAHOO.lang.augmentProto(beCPG.module.EntityDataGrid, beCPG.module.EntityDataGridActions);

    /**
     * Augment prototype with main class implementation, ensuring overwrite is
     * enabled
     */
    YAHOO.lang
            .augmentObject(
                    beCPG.module.EntityDataGrid.prototype,
                    {
                        /**
                         * Object container for initialization options
                         * 
                         * @property options
                         * @type object
                         */
                        options :
                        {
                            /**
                             * Current siteId.
                             * 
                             * @property siteId
                             * @type string
                             * @default ""
                             */
                            siteId : "",

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
                             * Display bottom pagination
                             */

                            displayBottomPagination : true,

                            /**
                             * Flag indicating whether filter is available or
                             * not.
                             * 
                             * @property useFilter
                             * @type boolean
                             * @default false
                             */
                            useFilter : false,

                            /**
                             * Instruction show to resolve filter id & data to
                             * url parameters
                             * 
                             * @property filterParameters
                             * @type Array
                             * @default []
                             */
                            filterParameters : [],
                            /**
                             * Can column be sorted
                             */
                            sortable : false,
                            /**
                             * Sort Url
                             */
                            sortUrl : null,

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
                            pageSize : 25,

                            /**
                             * Initial filter to show on load.
                             * 
                             * @property initialFilter
                             * @type object
                             */
                            initialFilter : {},

                            /**
                             * Delay time value for "More Actions" popup, in
                             * milliseconds
                             * 
                             * @property actionsPopupTimeout
                             * @type int
                             * @default 500
                             */
                            actionsPopupTimeout : 500,

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
                             * How many actions to display before the "More..."
                             * container
                             * 
                             * @property splitActionsAt
                             * @type int
                             * @default 3
                             */
                            splitActionsAt : 3,

                            /**
                             * Current entityNodeRef.
                             * 
                             * @property entityNodeRef
                             * @type string
                             * @default ""
                             */
                            entityNodeRef : "",

                            /**
                             * Allow to provide extraParams to backend
                             */
                            extraParams : null,

                            /**
                             * Current list.
                             * 
                             * @property list
                             * @type string
                             * @default ""
                             */
                            list : "",

                            /**
                             * Data Url
                             */
                            dataUrl : Alfresco.constants.PROXY_URI + "slingshot/datalists/data/node/",

                            /**
                             * Item Data Url
                             */
                            itemUrl : Alfresco.constants.PROXY_URI + "slingshot/datalists/item/node/",

                            /**
                             * Column Info Url
                             */

                            columnsUrl : Alfresco.constants.URL_SERVICECONTEXT + "module/entity-datagrid/config/columns",

                            /**
                             * Save field Url
                             */
                            saveFieldUrl : null,

                            /** Override Params * */

                            /**
                             * Item type : Force item Type let it null for
                             * default
                             */
                            itemType : null,

                            /**
                             * Specify a column to groupBy
                             */
                            groupBy : null,

                            /**
                             * Specify a sort plugin
                             */
                            sortId : null,

                            /**
                             * Specify a group formater
                             */
                            groupFormater : null,
                            /**
                             * Specify hidden column
                             */
                            hiddenColumns : [],
                            /**
                             * Extra data Params
                             */
                            extraDataParams : "&repo=true",

                            /**
                             * Form width when creating/editing
                             */
                            formWidth : "34em",

                            /**
                             * Should register history event
                             */
                            initHistoryManager : true,

                            /**
                             * Should use historyManager
                             */
                            useHistoryManager : true,
                            /**
                             * Load data when ready
                             */
                            forceLoad : false,

                            /**
                             * Use post method
                             */
                            postMethod : true
                        },

                        /**
                         * Current page being browsed.
                         * 
                         * @property currentPage
                         * @type int
                         * @default 1
                         */
                        currentPage : null,

                        /**
                         * Used to speedUp path pagination
                         */
                        queryExecutionId : null,

                        /**
                         * Current filter to filter document list.
                         * 
                         * @property currentFilter
                         * @type object
                         */
                        currentFilter : null,

                        /**
                         * Object literal of selected states for visible items
                         * (indexed by nodeRef).
                         * 
                         * @property selectedItems
                         * @type object
                         */
                        selectedItems : null,

                        /**
                         * Current actions menu being shown
                         * 
                         * @property currentActionsMenu
                         * @type object
                         * @default null
                         */
                        currentActionsMenu : null,

                        /**
                         * Whether "More Actions" pop-up is currently visible.
                         * 
                         * @property showingMoreActions
                         * @type boolean
                         * @default false
                         */
                        showingMoreActions : null,

                        /**
                         * Deferred actions menu element when showing "More
                         * Actions" pop-up.
                         * 
                         * @property deferredActionsMenu
                         * @type object
                         * @default null
                         */
                        deferredActionsMenu : null,

                        /**
                         * Deferred function calls for after a data grid update
                         * 
                         * @property afterDataGridUpdate
                         * @type array
                         */
                        afterDataGridUpdate : null,

                        /**
                         * Deferred function calls for after a data grid update
                         * and not deleted after
                         * 
                         * @property extraAfterDataGridUpdate
                         * @type array
                         */

                        extraAfterDataGridUpdate : null,

                        /**
                         * Data List metadata retrieved from the Repository
                         * 
                         * @param datalistMeta
                         * @type Object
                         */
                        datalistMeta : null,
                        /**
                         * Entity informations
                         */
                        entity : null,

                        /**
                         * Data List columns from Form configuration
                         * 
                         * @param datalistColumns
                         * @type Object
                         */
                        datalistColumns : null,

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
                         * Registered metadata renderer helper
                         * 
                         * @property rendererHelper
                         * @type object
                         */
                        rendererHelper : null,

                        /**
                         * Scope Id
                         * 
                         * @property scopeId
                         * @type string
                         */
                        scopeId : "",

                        /**
                         * The form filters runTime
                         */
                        formsFilterRuntime : null,

                        /**
                         * DataTable Cell Renderers
                         */

                        /**
                         * Returns selector custom datacell formatter
                         * 
                         * @method fnRenderCellSelected
                         */
                        fnRenderCellSelected : function EntityDataGrid_fnRenderCellSelected()
                        {
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
                            return function EntityDataGrid_renderCellSelected(elCell, oRecord, oColumn, oData)
                            {
                                Dom.setStyle(elCell, "width", oColumn.width + "px");
                                Dom.setStyle(elCell.parentNode, "width", oColumn.width + "px");

                                if (scope.options.sortable)
                                {
                                    var userAccess = oRecord.getData("permissions").userAccess;
                                    if (userAccess.sort)
                                    {
                                        YAHOO.util.Dom.addClass(elCell.parentNode, "datagrid-sort-handle");
                                    }
                                }

                                elCell.innerHTML = '<input id="checkbox-' + oRecord.getId() + '" type="checkbox" name="fileChecked" value="' + oData + '"' + (scope.selectedItems[oData] ? ' checked="checked">'
                                        : '>');
                            };
                        },
                        
                        fnRenderCellSelectedHeader : function EntityDataGrid_fnRenderCellSelectedHeader()
                        {
                        
                            this.timeStampId = (new Date()).getTime();
                            
                            var ret = "";
                            ret+="<div id=\""+this.id+"-"+this.timeStampId+"itemSelect-div\" class=\"item-select hidden\">";
                            ret+="<button id=\""+this.id+"-"+this.timeStampId+"itemSelect-button\" name=\"datagrid-itemSelect-button\">&nbsp;</button>";
                            ret+="<div id=\""+this.id+"-"+this.timeStampId+"itemSelect-menu\" class=\"yuimenu\">";
                            ret+="   <div class=\"bd\">";
                            ret+="      <ul>";
                            ret+="         <li><a href=\"#\"><span class=\"selectAll\">"+this.msg("menu.select.all")+"</span></a></li>";
                            ret+="         <li><a href=\"#\"><span class=\"selectInvert\">"+this.msg("menu.select.invert")+"</span></a></li>";
                            ret+="         <li><a href=\"#\"><span class=\"selectNone\">"+this.msg("menu.select.none")+"</span></a></li>";
                            ret+="      </ul>";
                            ret+="   </div>";
                            ret+=" </div>";
                            ret+=" </div>";
                            
                            return ret;
                        },

                        /**
                         * Returns actions custom datacell formatter
                         * 
                         * @method fnRenderCellActions
                         */
                        fnRenderCellActions : function EntityDataGrid_fnRenderCellActions()
                        {
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
                            return function EntityDataGrid_renderCellActions(elCell, oRecord, oColumn, oData)
                            {
                                Dom.setStyle(elCell, "width", oColumn.width + "px");
                                Dom.setStyle(elCell.parentNode, "width", oColumn.width + "px");

                                elCell.innerHTML = '<div id="' + scope.id + '-actions-' + oRecord.getId() + '" class="hidden"></div>';
                            };
                        },
                        /**
                         * Add color to row
                         */
                        rowFormatter : function EntityDataGrid_rowFormatter(elTr, oRecord)
                        {

                            if (oRecord.getData("color"))
                            {
                                Dom.setStyle(elTr, 'background-color', oRecord.getData("color"));
                            }
                            return true;
                        },

                        /**
                         * Build the cell Url
                         */
                        _buildCellUrl : function EntityDataGridRenderers__buildCellUrl(data)
                        {

                            var containerType = "document", url = null;
                            if (data.metadata != null && data.metadata.length)
                            {
                                containerType = (data.metadata == "container" ? 'folder' : 'document');
                            }

                            if (data.siteId)
                            {
                                url = Alfresco.constants.URL_PAGECONTEXT + "site/" + data.siteId + "/" + containerType + '-details?nodeRef=' + data.value;
                            }
                            else
                            {
                                url = Alfresco.constants.URL_PAGECONTEXT + containerType + '-details?nodeRef=' + data.value;
                            }
                            return url;

                        },

                        /**
                         * Fired by YUI when parent element is available for
                         * scripting
                         * 
                         * @method onReady
                         */
                        onReady : function EntityDataGrid_onReady()
                        {
                            var me = this;

                            // new row button
                            this.widgets.newRowButton = Alfresco.util.createYUIButton(this, "newRowButton",
                                    this.onActionCreate,
                                    {
                                        disabled : false,
                                        value : "create"
                                    });

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
                            var fnActionHandler = function EntityDataGrid_fnActionHandler(layer, args)
                            {
                                var owner = Bubbling.getOwnerByTagName(args[1].anchor, "div");
                                if (owner !== null)
                                {
                                    if (typeof me[owner.className] == "function")
                                    {
                                        args[1].stop = true;
                                        var asset = me.widgets.dataTable.getRecord(args[1].target.offsetParent)
                                                .getData();
                                        me[owner.className].call(me, asset, owner);
                                    }
                                }
                                return true;
                            };
                            Bubbling.addDefaultAction(me.id + "-action-link", fnActionHandler);
                            Bubbling.addDefaultAction(me.id + "-show-more", fnActionHandler);

                            // Hook filter change events
                            var fnChangeFilterHandler = function EntityDataGrid_fnChangeFilterHandler(layer, args)
                            {
                                var owner = args[1].anchor;
                                if (owner !== null)
                                {
                                    var filter = owner.rel, filters, filterObj = {};
                                    if (filter && filter !== "")
                                    {
                                        args[1].stop = true;
                                        filters = filter.split("|");
                                        filterObj =
                                        {
                                            filterOwner : window.unescape(filters[0] || ""),
                                            filterId : window.unescape(filters[1] || ""),
                                            filterData : window.unescape(filters[2] || "").replace("$ML$", "|"),
                                            filterDisplay : window.unescape(filters[3] || "")
                                        };
                                        Alfresco.logger.debug("DL_fnChangeFilterHandler", "changeFilter =>", filterObj);
                                        Bubbling.fire(this.scopeId + "changeFilter", filterObj);
                                    }
                                }
                                return true;
                            };
                            Bubbling.addDefaultAction("filter-change", fnChangeFilterHandler);

                            // DataList Actions module
                            this.modules.actions = new Alfresco.module.DataListActions();

                            // Reference to Data Grid component (required by
                            // actions
                            // module)
                            this.modules.dataGrid = this;

                            // Assume no list chosen for now
                            Dom.removeClass(this.id + "-selectListMessage", "hidden");

                           
                            // Filter forms
                            if (this.options.useFilter)
                            {

                                this.widgets.filterFormSubmit = Alfresco.util.createYUIButton(this,
                                        "filterform-submit", this.onFilterFormSubmit);
                                this.widgets.filterFormSubmit = Alfresco.util.createYUIButton(this, "filterform-clear",
                                        this.onFilterFormClear);

                                this.widgets.filterForm = Alfresco.util.createYUIButton(this, "filterform-button",
                                        null,
                                        {
                                            type : "menu",
                                            menu : "filterform-panel",
                                            disabled : true
                                        });
                            }

                            // Toolbar contribs
                            if (Dom.get("toolbar-contribs-" + this.id))
                            {
                                var controls = Dom.getChildren("toolbar-contribs-" + this.id);
                                if (controls)
                                {
                                    for ( var el in controls)
                                    {
                                        (new YAHOO.util.Element("toolbar-contribs")).appendChild(controls[el]);
                                    }
                                }
                            }
                            
                            this.deferredListPopulation.fulfil("onReady");

                            if(this.options.forceLoad)
                            {
                               this.populateDataGrid();
                            }

                            // Finally show the component body here to prevent
                            // UI
                            // artifacts on YUI button decoration
                            Dom.setStyle(this.id + "-body", "visibility", "visible");

                        },

                        /**
                         * Fired by YUI when History Manager is initialised and
                         * available for scripting. Component initialisation,
                         * including instantiation of YUI widgets and event
                         * listener binding.
                         * 
                         * @method onHistoryManagerReady
                         */
                        onHistoryManagerReady : function EntityDataGrid_onHistoryManagerReady()
                        {
                            // Fire changeFilter event for first-time population
                            Alfresco.logger.debug("EntityDataGrid_onHistoryManagerReady", "changeFilter =>",
                                    this.options.initialFilter);
                            Bubbling.fire(this.scopeId + "changeFilter", YAHOO.lang.merge(
                            {
                                datagridFirstTimeNav : true
                            }, this.options.initialFilter));
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
                        _onDataListFailure : function EntityDataGrid__onDataListFailure(p_response, p_message)
                        {
                            Alfresco.util.PopupManager.displayPrompt(
                            {
                                title : p_message.title,
                                text : p_message.text,
                                modal : true,
                                buttons : [
                                {
                                    text : this.msg("button.ok"),
                                    handler : function EntityDataGrid__onDataListFailure_OK()
                                    {
                                        this.destroy();
                                    },
                                    isDefault : true
                                } ]
                            });

                        },

                        /**
                         * Renders Data List metadata, i.e. title and
                         * description
                         * 
                         * @method renderDataListMeta
                         */
                        renderDataListMeta : function EntityDataGrid_renderDataListMeta()
                        {
                            if (!YAHOO.lang.isObject(this.datalistMeta) || !this.datalistMeta.title)
                            {
                                return;
                            }

                            Alfresco.util.populateHTML([ this.id + "-title",
                                    $html(this.datalistMeta.entityName + " - " + this.datalistMeta.title) ], [
                                    this.id + "-description", $links($html(this.datalistMeta.description, true)) ]);
                        },

                        /**
                         * Retrieves the Data List from the Repository
                         * 
                         * @method populateDataGrid
                         */
                        populateDataGrid : function EntityDataGrid_populateDataGrid()
                        {
                            
                            if (!YAHOO.lang.isObject(this.datalistMeta))
                            {
                                return;
                            }

                            // Filter form
                            if (this.options.useFilter)
                            {
                                this.populateFilterForm();
                            }

                            this.renderDataListMeta();

                            // Query the visible columns for this list's item
                            // type
                            Alfresco.util.Ajax.jsonGet(
                            {
                                url : this._getColumnUrl(),
                                successCallback :
                                {
                                    fn : this.onDatalistColumns,
                                    scope : this
                                },
                                failureCallback :
                                {
                                    fn : this._onDataListFailure,
                                    obj :
                                    {
                                        title : this.msg("message.error.columns.title"),
                                        text : this.msg("message.error.columns.description")
                                    },
                                    scope : this
                                }
                            });
                        },

                        _getColumnUrl : function(formId)
                        {
                            return this.options.columnsUrl + "?itemType=" + encodeURIComponent(this.options.itemType != null ? this.options.itemType
                                    : this.datalistMeta.itemType) + "&list=" + encodeURIComponent(this.datalistMeta.name != null ? this.datalistMeta.name
                                    : this.options.list) + (formId != null ? "&formId=" + formId : "");
                        },

                        /**
                         * Populate filter form
                         */
                        populateFilterForm : function EntityDataGrid_populateFilterForm()
                        {
                            var listName = this.datalistMeta.name != null ? this.datalistMeta.name : this.options.list;

                            var filterFormUrl = YAHOO.lang
                                    .substitute(
                                            Alfresco.constants.URL_SERVICECONTEXT + "module/entity-datagrid/filter/form?itemKind={itemKind}&list={list}&itemId={itemId}&formId={formId}&submitType=json&showCancelButton=false&showSubmitButton=false" + ((this.options.entityNodeRef != null && this.options.entityNodeRef.length > 0) ? "&entityNodeRef=" + this.options.entityNodeRef
                                                    : ""),
                                            {
                                                itemKind : "type",
                                                itemId : this.options.itemType != null ? this.options.itemType
                                                        : this.datalistMeta.itemType,
                                                formId : "filter",
                                                list : listName,
                                                submitType : "json"
                                            });

                            Alfresco.util.Ajax.request(
                            {
                                url : filterFormUrl,
                                dataObj :
                                {
                                    htmlid : this.id + "-filterForm"
                                },
                                successCallback :
                                {
                                    fn : this.onFilterFormTemplateLoaded,
                                    scope : this
                                },
                                failureCallback :
                                {
                                    fn : function()
                                    {
                                        this.widgets.filterForm.set("disabled", true);
                                    },
                                    scope : this
                                },
                                scope : this,
                                execScripts : true
                            });
                        },
                        /**
                         * Event callback when filtr template has been loaded
                         * 
                         * @method onFilterFormTemplateLoaded
                         * @param response
                         *            {object} Server response from load
                         *            template XHR request
                         */
                        onFilterFormTemplateLoaded : function EntityDataGrid_onFilterFormTemplateLoaded(response)
                        {
                            Dom.get(this.id + "-filterform").innerHTML = response.serverResponse.responseText;

                            this.widgets.filterForm.set("disabled", false);

                        },

                        onFilterFormSubmit : function EntityDataGrid_onFilterFormSubmit()
                        {
                            Bubbling.fire(this.scopeId + "changeFilter",
                            {
                                filterOwner : this.id,
                                filterId : "filterform",
                                filterData : YAHOO.lang.JSON.stringify(
                                        this.cleanFilterData(this.formsFilterRuntime.getFormData())).replace("|",
                                        "$ML$")
                            });

                            this.widgets.filterForm.getMenu().hide();

                        },

                        onFilterFormClear : function EntityDataGrid_onFilterFormClear()
                        {
                            Bubbling.fire(this.scopeId + "changeFilter",
                            {
                                filterOwner : this.id,
                                filterId : "all"
                            });

                            this.formsFilterRuntime.reset();
                            this.widgets.filterForm.getMenu().hide();
                        },

                        cleanFilterData : function EntityDataGrid_cleanFilterData(formData)
                        {
                            var ret = {};
                            if (formData != null)
                            {
                                for ( var key in formData)
                                {
                                    if (formData[key].length > 0)
                                    {
                                        ret[key] = formData[key];
                                    }
                                }
                            }
                            return ret;
                        },
                        /**
                         * Event handler called when the
                         * "onBeforeFormRuntimeInit" event is received.
                         * 
                         * @method onBeforeFormRuntimeInit
                         * @param layer
                         *            {String} Event type
                         * @param args
                         *            {Object} Event arguments
                         * 
                         * <pre>
                         *    args.[1].runtime: Alfresco.forms.Form instance
                         * </pre>
                         */
                        onBeforeFormRuntimeInit : function ExtDataGrid_onBeforeFormRuntimeInit(layer, args)
                        {
                            // Get filter form runtime

                            if (this.formsFilterRuntime == null && args[1].eventGroup == this.id + "-filterForm-form")
                            {
                                this.formsFilterRuntime = args[1].runtime;
                                this.formsFilterRuntime.validations = [];
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
                        onDatalistColumns : function EntityDataGrid_onDatalistColumns(response)
                        {
                            this.datalistColumns = response.json.columns;
                            // Set-up YUI History Managers and Paginator
                            this._setupHistoryManagers();
                            // DataSource set-up and event registration
                            this._setupDataSource();
                            // DataTable set-up and event registration
                            this._setupDataTable();
                            // Hide "no list" message
                            Dom.addClass(this.id + "-selectListMessage", "hidden");
                           

                            Bubbling.fire(this.scopeId + "onDatalistColumnsReady",
                            {
                                entityDatagrid : this
                            });

                            
                            if(this.options.useHistoryManager){
                                // Continue only when History Manager fires its
                                // onReady
                                // event
                                YAHOO.util.History.onReady(this.onHistoryManagerReady, this, true);
                            } else {
                                this.onHistoryManagerReady.call(this);
                            }
                        },

                        /**
                         * History Manager set-up and event registration
                         * 
                         * @method _setupHistoryManagers
                         */
                        _setupHistoryManagers : function EntityDataGrid__setupHistoryManagers()
                        {
                            /**
                             * YUI History - filter
                             */
                            var bookmarkedFilter = YAHOO.util.History.getBookmarkedState(this.scopeId + "filter");
                            bookmarkedFilter = bookmarkedFilter === null ? "all"
                                    : (YAHOO.env.ua.gecko > 0) ? bookmarkedFilter : window.escape(bookmarkedFilter);

                            try
                            {
                                while (bookmarkedFilter != (bookmarkedFilter = decodeURIComponent(bookmarkedFilter)))
                                {
                                }
                            }
                            catch (e1)
                            {
                                // Catch "malformed URI sequence" exception
                            }

                            var fnDecodeBookmarkedFilter = function EntityDataGrid_fnDecodeBookmarkedFilter(strFilter)
                            {
                                var filters = strFilter.split("|"), filterObj =
                                {
                                    filterId : window.unescape(filters[0] || ""),
                                    filterData : window.unescape((filters[1] || "")).replace("$ML$", "|")
                                };

                                filterObj.filterOwner = Alfresco.util.FilterManager.getOwner(filterObj.filterId);
                                return filterObj;
                            };

                            this.options.initialFilter = fnDecodeBookmarkedFilter(bookmarkedFilter);

                            // Register History Manager filter update callback
                            YAHOO.util.History.register(this.scopeId + "filter", bookmarkedFilter,
                                    function EntityDataGrid_onHistoryManagerFilterChanged(newFilter)
                                    {
                                        Alfresco.logger.debug("HistoryManager: filter changed:" + newFilter);
                                        // Firefox fix
                                        if (YAHOO.env.ua.gecko > 0)
                                        {
                                            newFilter = window.unescape(newFilter);
                                            Alfresco.logger
                                                    .debug("HistoryManager: filter (after Firefox fix):" + newFilter);
                                        }

                                        this._updateDataGrid.call(this,
                                        {
                                            filter : fnDecodeBookmarkedFilter(newFilter)
                                        });
                                    }, null, this);

                            if (this.options.initHistoryManager && this.options.useHistoryManager)
                            {
                                try
                                {
                                    YAHOO.util.History.initialize("yui-history-field", "yui-history-iframe");
                                }
                                catch (e2)
                                {
                                    /*
                                     * The only exception that gets thrown here
                                     * is when the browser is not supported
                                     * (Opera, or not A-grade)
                                     */
                                    Alfresco.logger.error(this.name + ": Couldn't initialize HistoryManager.", e2);
                                    var obj = args[1];
                                    if ((obj !== null) && (obj.entityDataGridModule !== null))
                                    {
                                        obj.entityDataGridModule.onHistoryManagerReady();
                                    }
                                }
                            }
                            else
                            {
                                Bubbling.fire("dataGridReady",
                                {
                                    entityDataGridModule : this
                                });

                            }

                        },
                        /**
                         * DataGrid View Filter changed event handler
                         * 
                         * @method onFilterChanged
                         * @param layer
                         *            {object} Event fired (unused)
                         * @param args
                         *            {array} Event parameters (new filterId)
                         */
                        onFilterChanged : function EntityDataGrid_onFilterChanged(layer, args)
                        {
                            var obj = args[1];
                            if ((obj !== null) && (obj.filterId !== null))
                            {
                                obj.filterOwner = obj.filterOwner || Alfresco.util.FilterManager.getOwner(obj.filterId);

                                // Should be a filterId in the arguments
                                this.currentFilter = Alfresco.util.cleanBubblingObject(obj);
                                Alfresco.logger.debug("DL_onFilterChanged: ", this.currentFilter);
                            }
                        },

                        /**
                         * DataSource set-up and event registration
                         * 
                         * @method _setupDataSource
                         * @protected
                         */
                        _setupDataSource : function EntityDataGrid__setupDataSource()
                        {
                            var me = this;

                            this.dataRequestFields = [];
                            this.dataResponseFields = [];
                            for (var i = 0, ii = this.datalistColumns.length; i < ii; i++)
                            {
                                var column = this.datalistColumns[i], columnName = column.name.replace(":", "_"), fieldLookup = (column.type == "property" ? "prop"
                                        : "assoc");

                                if (column.dataType == "nested" && column.columns)
                                {
                                    fieldLookup = "dt";
                                    fieldLookup += "_" + columnName;
                                    for (var j = 0; j < column.columns.length; j++)
                                    {
                                        columnName += "|" + column.columns[j].name.replace(":", "_");
                                    }

                                }
                                else
                                {
                                    fieldLookup += "_" + columnName;
                                }

                                this.dataRequestFields.push(columnName);
                                this.dataResponseFields.push(fieldLookup);
                                this.datalistColumns[fieldLookup] = column;
                            }

                            // DataSource definition
                            this.widgets.dataSource = new YAHOO.util.DataSource(me._getDataUrl(),
                            {
                                connMethodPost : me.options.postMethod,
                                responseType : YAHOO.util.DataSource.TYPE_JSON,
                                responseSchema :
                                {
                                    resultsList : "items",
                                    metaFields :
                                    {
                                        startIndex : "startIndex",
                                        totalRecords : "totalRecords",
                                        queryExecutionId : "queryExecutionId"
                                    }
                                }
                            });

                            if (me.options.postMethod)
                            {
                                this.widgets.dataSource.connMgr.setDefaultPostHeader(Alfresco.util.Ajax.JSON);
                            }

                            // Intercept data returned from data webscript to
                            // extract
                            // custom metadata
                            this.widgets.dataSource.doBeforeCallback = function EntityDataGrid_doBeforeCallback(
                                    oRequest, oFullResponse, oParsedResponse)
                            {
                                // Container userAccess event
                                var permissions = oFullResponse.metadata.parent.permissions;
                                if (permissions && permissions.userAccess)
                                {
                                    Bubbling.fire(me.scopeId + "userAccess",
                                    {
                                        userAccess : permissions.userAccess
                                    });
                                }

                                return oParsedResponse;
                            };
                        },

                        _getDataUrl : function(pageSize)
                        {
                            var listNodeRef = this.datalistMeta.nodeRef != null ? new Alfresco.util.NodeRef(
                                    this.datalistMeta.nodeRef) : null;

                            return this.options.dataUrl + (listNodeRef != null ? listNodeRef.uri : "") + ((this.options.entityNodeRef != null && this.options.entityNodeRef.length > 0) ? "?entityNodeRef=" + this.options.entityNodeRef + "&"
                                    : "?") + "itemType=" + encodeURIComponent(this.options.itemType != null ? this.options.itemType
                                    : this.datalistMeta.itemType) + "&dataListName=" + encodeURIComponent(this.datalistMeta.name != null ? this.datalistMeta.name
                                    : this.options.list) + "&pageSize=" + (pageSize != null ? pageSize
                                    : this.options.pageSize) + "&site=" + this.options.siteId + this._buildSortParam() + this.options.extraDataParams;
                        },
                        /**
                         * DataTable set-up and event registration
                         * 
                         * @method _setupDataTable
                         * @protected
                         */
                        _setupDataTable : function EntityDataGrid__setupDataTable(columns)
                        {

                            var me = this;

                            // YUI DataTable column definitions
                            var columnDefinitions = [
                            {
                                key : "nodeRef",
                                label : this.fnRenderCellSelectedHeader(),
                                sortable : false,
                                formatter : this.fnRenderCellSelected(),
                                width : 16
                            } ];
                            
                            delete me.widgets.itemSelect;
                            
                            for (var i = 0, ii = this.datalistColumns.length; i < ii; i++)
                            {
                                var column = this.datalistColumns[i];
                                var key = this.dataResponseFields[i];
                                if (this.options.hiddenColumns.length < 1 || !beCPG.util.contains(
                                        this.options.hiddenColumns, key))
                                {
                                    columnDefinitions.push(
                                    {
                                        key : key,
                                        label : column.label == "hidden" ? "" : column.label,
                                        hidden : column.label == "hidden",
                                        sortable : true,
                                        sortOptions :
                                        {
                                            field : column.formsName,
                                            sortFunction : this.rendererHelper.getSortFunction()
                                        },
                                        formatter : this.rendererHelper.getCellFormatter(this),
                                        editor : this.options.saveFieldUrl != null ? this.rendererHelper.getCellEditor(
                                                this, column, this.options.saveFieldUrl) : null
                                    });
                                }
                            }

                            // Add actions as last column
                            columnDefinitions.push(
                            {
                                key : "actions",
                                label : this.msg("label.column.actions"),
                                sortable : false,
                                formatter : this.fnRenderCellActions(),
                                width : 80
                            });

                            // DataTable definition

                            if (!YAHOO.widget.GroupedDataTable)
                            {
                                YAHOO.widget.GroupedDataTable = YAHOO.widget.DataTable;
                            }

                            var dataTableOptions =
                            {
                                initialLoad : false,
                                dynamicData : false,
                                "MSG_EMPTY" : this.msg("message.empty"),
                                "MSG_ERROR" : this.msg("message.error"),
                                "MSG_NOGROUP" : this.msg("message.nogroup"),
                                paginator : null,
                                groupBy : this.options.groupBy,
                                groupFormater : this.options.groupFormater,
                                formatRow : this.rowFormatter
                                
                                
                            };

                            if (this.options.saveFieldUrl != null)
                            {
                                dataTableOptions.selectionMode = "singlecell";
                            }

                            this.widgets.dataTable = new YAHOO.widget.GroupedDataTable(this.id + "-grid",
                                    columnDefinitions, this.widgets.dataSource, dataTableOptions);

                            if (this.options.usePagination)
                            {

                                var paginationContainers = [ this.id + "-paginator" ];

                                if (this.options.displayBottomPagination)
                                {
                                    paginationContainers.push(this.id + "-paginatorBottom");
                                }

                                // YUI Paginator definition
                                this.widgets.paginator = new YAHOO.widget.Paginator(
                                {
                                    containers : paginationContainers,
                                    rowsPerPage : this.options.pageSize,
                                    initialPage : this.options.initialPage,
                                    template : this.msg("pagination.template"),
                                    pageReportTemplate : this.msg("pagination.template.page-report"),
                                    previousPageLinkLabel : this.msg("pagination.previousPageLinkLabel"),
                                    nextPageLinkLabel : this.msg("pagination.nextPageLinkLabel")
                                });

                                var handlePagination = function EntityDataGrid_handlePagination(state, me)
                                {

                                    me._updateDataGrid.call(me,
                                    {
                                        page : state.page
                                    });
                                };

                                this.widgets.paginator.subscribe("changeRequest", handlePagination, this);

                                // Display the bottom paginator bar
                                if (this.options.displayBottomPagination)
                                {
                                    Dom.setStyle(this.id + "-datagridBarBottom", "display", "block");
                                }

                                // Add the containers if the Paginator is not
                                // configured
                                // with containers
                                if (!this.widgets.paginator.getContainerNodes().length)
                                {
                                    this.widgets.paginator.set('containers', this.widgets.dataTable
                                            ._defaultPaginatorContainers(true));
                                }

                                this.widgets.paginator.render();

                            }

                            // Update totalRecords with value from server
                            this.widgets.dataTable.handleDataReturnPayload = function EntityDataGrid_handleDataReturnPayload(
                                    oRequest, oResponse, oPayload)
                            {
                                if (me.widgets.paginator)
                                {
                                    me.widgets.paginator.set('totalRecords', oResponse.meta.totalRecords);
                                    me.widgets.paginator.setPage(oResponse.meta.startIndex, true);
                                    
                                    if(oResponse.meta.totalRecords > me.options.pageSize){
                                        Dom.removeClass(me.id + "-paginator", "hidden");
                                        if (me.options.displayBottomPagination)
                                        {
                                            Dom.removeClass(me.id + "-paginatorBottom", "hidden");
                                        }
                                    } else {
                                        Dom.addClass(me.id + "-paginator", "hidden");
                                        if (me.options.displayBottomPagination)
                                        {
                                            Dom.addClass(me.id + "-paginatorBottom", "hidden");
                                        }
                                    }
                                    
                                }
                                me.queryExecutionId = oResponse.meta.queryExecutionId;
                                return oResponse.meta;
                            };

                            // Override abstract function within DataTable to
                            // set
                            // custom error message
                            this.widgets.dataTable.doBeforeLoadData = function EntityDataGrid_doBeforeLoadData(
                                    sRequest, oResponse, oPayload)
                            {
                                if (!oResponse || oResponse.error)
                                {
                                    try
                                    {
                                        var response = YAHOO.lang.JSON.parse(oResponse.responseText);
                                        me.widgets.dataTable.set("MSG_ERROR", response.message);
                                    }
                                    catch (e)
                                    {
                                        me._setDefaultDataTableErrors(me.widgets.dataTable);
                                    }
                                }

                                // We don't get an renderEvent for an empty
                                // recordSet,
                                // but we'd like one anyway
                                if (oResponse.results.length === 0)
                                {
                                    this.fireEvent("renderEvent",
                                    {
                                        type : "renderEvent"
                                    });
                                }

                                // Must return true to have the "Loading..."
                                // message
                                // replaced by the error message
                                return true;
                            };

                            // Override default function so the "Loading..."
                            // message is
                            // suppressed
                            this.widgets.dataTable.doBeforeSortColumn = function DataGrid_doBeforeSortColumn(oColumn,
                                    sSortDir)
                            {
                                me.currentSort =
                                {
                                    oColumn : oColumn,
                                    sSortDir : sSortDir
                                };
                                // Change when dynamic sort

                                var oSortedBy = this.get("sortedBy") || {};
                                var bSorted = (oSortedBy.key === oColumn.key) ? true : false;

                                // Is there a custom sort handler function
                                // defined?
                                var sortFnc = (oColumn.sortOptions && YAHOO.lang
                                        .isFunction(oColumn.sortOptions.sortFunction)) ?
                                // Custom sort function
                                oColumn.sortOptions.sortFunction : null;

                                // Sort the Records
                                if (!bSorted || sortFnc)
                                {
                                    // Default sort function if necessary
                                    sortFnc = sortFnc || this.get("sortFunction");
                                    // Get the field to sort
                                    var sField = (oColumn.sortOptions && oColumn.sortOptions.field) ? oColumn.sortOptions.field
                                            : oColumn.field;

                                    // Sort the Records
                                    this._oRecordSet.sortRecords(sortFnc,
                                            ((sSortDir == YAHOO.widget.DataTable.CLASS_DESC) ? true : false), sField);
                                }
                                // Just reverse the Records
                                else
                                {
                                    this._oRecordSet.reverseRecords();
                                }

                                // Update UI via sortedBy
                                this.render();
                                this.set("sortedBy",
                                {
                                    key : oColumn.key,
                                    dir : sSortDir,
                                    column : oColumn
                                });

                                return false;
                            };

                            // File checked handler
                            this.widgets.dataTable.subscribe("checkboxClickEvent", function(e)
                            {
                                var id = e.target.value;
                                this.selectedItems[id] = e.target.checked;
                                Bubbling.fire(this.scopeId + "selectedItemsChanged");
                            }, this, true);

                            // Before render event handler
                            this.widgets.dataTable
                                    .subscribe(
                                            "beforeRenderEvent",
                                            function()
                                            {
                                                if (me.currentSort)
                                                {
                                                    // Is there a custom sort
                                                    // handler
                                                    // function defined?
                                                    var oColumn = me.currentSort.oColumn, sSortDir = me.currentSort.sSortDir, sortFnc = (oColumn.sortOptions && YAHOO.lang
                                                            .isFunction(oColumn.sortOptions.sortFunction)) ?
                                                    // Custom sort function
                                                    oColumn.sortOptions.sortFunction : null;

                                                    // Sort the Records
                                                    if (sSortDir || sortFnc)
                                                    {
                                                        // Default sort function
                                                        // if necessary
                                                        sortFnc = sortFnc || this.rendererHelper.get("sortFunction");
                                                        // Get the field to sort
                                                        var sField = (oColumn.sortOptions && oColumn.sortOptions.field) ? oColumn.sortOptions.field
                                                                : oColumn.field;

                                                        // Sort the Records
                                                        this._oRecordSet.sortRecords(sortFnc,
                                                                ((sSortDir == YAHOO.widget.DataTable.CLASS_DESC) ? true
                                                                        : false), sField);
                                                    }
                                                }
                                            }, this.widgets.dataTable, true);

                            // Rendering complete event handler
                            this.widgets.dataTable.subscribe("renderEvent", function()
                            {
                                Alfresco.logger.debug("DataTable renderEvent");
                                
                                
                             // Item Select menu button
                                if( this.widgets.itemSelect ==null){
                                   this.widgets.itemSelect = Alfresco.util.createYUIButton(this, me.timeStampId+"itemSelect-button",
                                           this.onItemSelect,
                                         {
                                                    type : "menu",
                                                    menu : me.timeStampId+"itemSelect-menu",
                                                    disabled : false
                                       });
     
                                    
                                 // Enable item select menu
                                    Dom.removeClass(me.id +"-"+me.timeStampId+"itemSelect-div", "hidden");
                                }

                                // IE6 fix for long filename rendering issue
                                if (YAHOO.env.ua.ie < 7)
                                {
                                    var ie6fix = this.widgets.dataTable.getTableEl().parentNode;
                                    ie6fix.className = ie6fix.className;
                                }

                                for (var i = 0, j = this.extraAfterDataGridUpdate.length; i < j; i++)
                                {
                                    this.extraAfterDataGridUpdate[i].call(this);
                                }

                                // Deferred functions specified?
                                for (var i = 0, j = this.afterDataGridUpdate.length; i < j; i++)
                                {
                                    this.afterDataGridUpdate[i].call(this);
                                }
                                this.afterDataGridUpdate = [];

                            }, this, true);

                            // Enable row highlighting
                            this.widgets.dataTable.subscribe("rowMouseoverEvent", this.onEventHighlightRow, this, true);
                            this.widgets.dataTable
                                    .subscribe("rowMouseoutEvent", this.onEventUnhighlightRow, this, true);

                            // Editor
                            if (this.options.saveFieldUrl != null)
                            {

                                var showCellEditor = function(oArgs)
                                {

                                    var elTarget = oArgs.target;

                                    var elTargetCell = me.widgets.dataTable.getTdEl(elTarget);
                                    if (elTargetCell)
                                    {
                                        var elCell = me.widgets.dataTable.getTdEl(elTargetCell);

                                        if (Dom.hasClass(elCell, YAHOO.widget.DataTable.CLASS_SELECTED))
                                        {
                                            me.widgets.dataTable.onEventShowCellEditor(oArgs);
                                        }
                                    }

                                };

                                this.widgets.dataTable.subscribe("cellClickEvent", function(oArgs)
                                {

                                    var column = me.widgets.dataTable.getColumn(oArgs.target);

                                    if (column.editor != null)
                                    {
                                        me.widgets.dataTable.focus();
                                        showCellEditor(oArgs);
                                        me.widgets.dataTable.onEventSelectCell(oArgs);
                                    }

                                });
                                this.widgets.dataTable.subscribe("tbodyKeyEvent", function(oArgs)
                                {
                                    var nKey = Event.getCharCode(oArgs.event);

                                    if ((nKey > 47 && nKey < 90) || (nKey > 95 && nKey < 106))
                                    {
                                        showCellEditor(
                                        {
                                            target : this.getLastSelectedCell()
                                        });

                                    }

                                });

                                this.widgets.dataTable.subscribe("cellSelectEvent",
                                        this.widgets.dataTable.clearTextSelection);

                            }

                            if (this.options.sortable)
                            {

                                var ddGroup = "group-" + me.id;

                                // ////////////////////////////////////////////////////////////////////////////
                                // Create DD Event
                                // ////////////////////////////////////////////////////////////////////////////
                                me.widgets.dataTable.dtdTargets = {};

                                var onRowSelect = function(ev)
                                {
                                    if (me.widgets.dataTable.getRecord(Event.getTarget(ev)) != null)
                                    {
                                        var userAccess = me.widgets.dataTable.getRecord(Event.getTarget(ev)).getData(
                                                "permissions").userAccess;
                                        if (userAccess.sort)
                                        {

                                            var par = me.widgets.dataTable.getTrEl(Event.getTarget(ev)), destIndex = null, ddRow = new YAHOO.util.DDProxy(
                                                    par.id, ddGroup), proxyEl = null, srcEl = null, destEl = null;

                                            ddRow.handleMouseDown(ev.event);

                                            /**
                                             * Once we start dragging a row, we
                                             * make the proxyEl look like the
                                             * src Element. We get also cache
                                             * all the data related to the row
                                             * 
                                             * @return void
                                             * @static
                                             * @method startDrag
                                             */
                                            ddRow.startDrag = function()
                                            {
                                                proxyEl = this.getDragEl();
                                                srcEl = this.getEl();
                                                srcIndex = srcEl.sectionRowIndex;

                                                // Make the proxy look like the
                                                // source element
                                                Dom.setStyle(srcEl, "visibility", "hidden");
                                                proxyEl.innerHTML = "<table><tbody>" + srcEl.innerHTML + "</tbody></table>";
                                            };

                                            /**
                                             * Once we end dragging a row, we
                                             * swap the proxy with the real
                                             * element.
                                             * 
                                             * @param x :
                                             *            The x Coordinate
                                             * @param y :
                                             *            The y Coordinate
                                             * @return void
                                             * @static
                                             * @method endDrag
                                             */
                                            ddRow.endDrag = function(x, y)
                                            {
                                                Dom.setStyle(proxyEl, "visibility", "hidden");
                                                Dom.setStyle(srcEl, "visibility", "");

                                                if (me.options.sortUrl)
                                                {
                                                    var destRecord = me.widgets.dataTable.getRecord(destIndex);
                                                    if (destRecord != null)
                                                    {
                                                        dstData = destRecord.getData();
                                                        srcData = me.widgets.dataTable.getRecord(srcEl).getData();

                                                        if (dstData && srcData)
                                                        {

                                                            var url = me.options.sortUrl + "/" + dstData.nodeRef
                                                                    .replace(":/", "") + "?selectedNodeRefs=" + srcData.nodeRef;

                                                            Alfresco.util.Ajax
                                                                    .jsonPost(
                                                                    {
                                                                        url : url,
                                                                        successCallback :
                                                                        {
                                                                            fn : function EntityDataGrid_onActionEdit_refreshFailure(
                                                                                    response)
                                                                            {
                                                                                // Insert
                                                                                // after
                                                                                me._updateDataGrid.call(me,
                                                                                {
                                                                                    page : me.currentPage
                                                                                });
                                                                            },
                                                                            scope : this
                                                                        },
                                                                        failureCallback :
                                                                        {
                                                                            fn : function EntityDataGrid_onActionEdit_refreshFailure(
                                                                                    response)
                                                                            {
                                                                                Alfresco.util.PopupManager
                                                                                        .displayMessage(
                                                                                        {
                                                                                            text : me
                                                                                                    .msg("message.details.failure")
                                                                                        });
                                                                            },
                                                                            scope : this
                                                                        }
                                                                    });
                                                        }
                                                    }

                                                }

                                            };

                                            /**
                                             * @param e :
                                             *            The drag event
                                             * @param id :
                                             *            The id of the row
                                             *            being dragged
                                             * @return void
                                             * @static
                                             * @method onDragOver
                                             */
                                            ddRow.onDragOver = function(e, id)
                                            {
                                                // Reorder rows as user drags
                                                destEl = Dom.get(id);

                                                if (destEl != null && destEl.nodeName.toLowerCase() === "tr")
                                                {
                                                    destIndex = destEl.sectionRowIndex;
                                                    Dom.addClass(id, "elementDragOverHighlight");
                                                }
                                            };

                                            /**
                                             * @param e :
                                             *            The drag event
                                             * @param id :
                                             *            The id of the row
                                             *            being dragged
                                             * @return void
                                             * @static
                                             * @method onDragOver
                                             */
                                            ddRow.onDragOut = function(e, id)
                                            {
                                                // Reorder rows as user drags
                                                var tmp = Dom.get(id);

                                                if (tmp != null && tmp.nodeName.toLowerCase() === "tr")
                                                {
                                                    Dom.removeClass(id, "elementDragOverHighlight");
                                                }
                                            };

                                        }
                                    }
                                };

                                this.widgets.dataTable.subscribe('cellMousedownEvent', onRowSelect);

                                // ////////////////////////////////////////////////////////////////////////////
                                // Create DDTarget instances when new row is
                                // added
                                // ////////////////////////////////////////////////////////////////////////////
                                this.widgets.dataTable.subscribe("rowAddEvent", function(e)
                                {
                                    if (e.record)
                                    {
                                        var id = e.record.getId();
                                        me.widgets.dataTable.dtdTargets[id] = new YAHOO.util.DDTarget(id, ddGroup);
                                    }
                                });

                                // ////////////////////////////////////////////////////////////////////////////
                                // Delete DDTarget instances when row is deleted
                                // ////////////////////////////////////////////////////////////////////////////
                                this.widgets.dataTable.subscribe("rowDeleteEvent", function(e)
                                {
                                    if (e.record)
                                    {
                                        var id = e.record.getId();
                                        me.widgets.dataTable.dtdTargets[id].unreg();
                                        delete me.widgets.dataTable.dtdTargets[id];
                                    }
                                });

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
                        onItemSelect : function EntityDataGrid_onItemSelect(sType, aArgs, p_obj)
                        {
                            var domEvent = aArgs[0], eventTarget = aArgs[1];

                            // Select based upon the className of the clicked
                            // item
                            this.selectItems(Alfresco.util.findEventClass(eventTarget));
                            Event.preventDefault(domEvent);
                        },

                        /**
                         * Custom event handler to highlight row.
                         * 
                         * @method onEventHighlightRow
                         * @param oArgs.event
                         *            {HTMLEvent} Event object.
                         * @param oArgs.target
                         *            {HTMLElement} Target element.
                         */
                        onEventHighlightRow : function EntityDataGrid_onEventHighlightRow(oArgs)
                        {

                            // Call through to get the row highlighted by YUI
                            this.widgets.dataTable.onEventHighlightRow.call(this.widgets.dataTable, oArgs);

                            // elActions is the element id of the active table
                            // cell
                            // where we'll inject the actions
                            var elActions = Dom.get(this.id + "-actions-" + oArgs.target.id);

                            // Inject the correct action elements into the
                            // actionsId
                            // element
                            if (elActions && elActions.firstChild === null)
                            {

                                // Clone the actionSet template node from the
                                // DOM
                                var record = this.widgets.dataTable.getRecord(oArgs.target.id), clone = Dom.get(
                                        this.id + "-actionSet").cloneNode(true);

                                // Token replacement
                                clone.innerHTML = YAHOO.lang.substitute(window.unescape(clone.innerHTML), this
                                        .getActionUrls(record));

                                // Generate an id
                                clone.id = elActions.id + "_a";

                                // Simple view by default
                                Dom.addClass(clone, "simple");

                                // Trim the items in the clone depending on the
                                // user's
                                // access
                                var userAccess = record.getData("permissions").userAccess, actionLabels = record
                                        .getData("actionLabels") || {};

                                // Inject the current filterId to allow
                                // filter-scoped
                                // actions
                                userAccess["filter-" + this.currentFilter.filterId] = true;

                                // Remove any actions the user doesn't have
                                // permission
                                // for
                                var actions = YAHOO.util.Selector.query("div", clone), action, aTag, spanTag, actionPermissions, aP, i, ii, j, jj;

                                for (i = 0, ii = actions.length; i < ii; i++)
                                {
                                    action = actions[i];
                                    aTag = action.firstChild;
                                    spanTag = aTag.firstChild;
                                    if (spanTag && actionLabels[action.className])
                                    {
                                        spanTag.innerHTML = $html(actionLabels[action.className]);
                                    }

                                    if (aTag.rel !== "")
                                    {
                                        actionPermissions = aTag.rel.split(",");
                                        for (j = 0, jj = actionPermissions.length; j < jj; j++)
                                        {
                                            aP = actionPermissions[j];
                                            // Support "negative" permissions
                                            if ((aP.charAt(0) == "~") ? !!userAccess[aP.substring(1)] : !userAccess[aP])
                                            {
                                                clone.removeChild(action);
                                                break;
                                            }
                                        }
                                    }
                                }

                                // Need the "More >" container?
                                var splitAt = this.options.splitActionsAt;
                                actions = YAHOO.util.Selector.query("div", clone);
                                if (actions.length > splitAt)
                                {
                                    var moreContainer = Dom.get(this.id + "-moreActions").cloneNode(true);
                                    var containerDivs = YAHOO.util.Selector.query("div", moreContainer);
                                    // Insert the two necessary DIVs before the
                                    // splitAt
                                    // action item
                                    Dom.insertBefore(containerDivs[0], actions[splitAt]);
                                    Dom.insertBefore(containerDivs[1], actions[splitAt]);
                                    // Now make action items after the split,
                                    // children of
                                    // the 2nd DIV
                                    var moreActions = actions.slice(splitAt);
                                    for ( var index in moreActions)
                                    {
                                        if (moreActions.hasOwnProperty(index))
                                        {
                                            containerDivs[1].appendChild(moreActions[index]);
                                        }
                                    }
                                }

                                elActions.appendChild(clone);
                            }

                            if (this.showingMoreActions)
                            {
                                this.deferredActionsMenu = elActions;
                            }
                            else if (!Dom.hasClass(document.body, "masked"))
                            {
                                this.currentActionsMenu = elActions;
                                // Show the actions
                                Dom.removeClass(elActions, "hidden");
                                this.deferredActionsMenu = null;
                            }

                        },

                        /**
                         * Custom event handler to unhighlight row.
                         * 
                         * @method onEventUnhighlightRow
                         * @param oArgs.event
                         *            {HTMLEvent} Event object.
                         * @param oArgs.target
                         *            {HTMLElement} Target element.
                         */
                        onEventUnhighlightRow : function EntityDataGrid_onEventUnhighlightRow(oArgs)
                        {
                            // Call through to get the row unhighlighted by YUI
                            this.widgets.dataTable.onEventUnhighlightRow.call(this.widgets.dataTable, oArgs);

                            var elActions = Dom.get(this.id + "-actions-" + (oArgs.target.id));

                            // Don't hide unless the More Actions drop-down is
                            // showing,
                            // or a dialog mask is present
                            if (!this.showingMoreActions || Dom.hasClass(document.body, "masked"))
                            {
                                // Just hide the action links, rather than
                                // removing them
                                // from the DOM
                                Dom.addClass(elActions, "hidden");
                                this.deferredActionsMenu = null;
                            }
                        },

                        /**
                         * The urls to be used when creating links in the action
                         * cell
                         * 
                         * @method getActionUrls
                         * @param record
                         *            {YAHOO.widget.Record | Object} A data
                         *            record, or object literal describing the
                         *            item in the list
                         * @return {object} Object literal containing URLs to be
                         *         substituted in action placeholders
                         */
                        getActionUrls : function EntityDataGrid_getActionUrls(record)
                        {
                            var recordData = YAHOO.lang.isFunction(record.getData) ? record.getData() : record, nodeRef = recordData.nodeRef;

                            return (
                            {
                                editMetadataUrl : "edit-dataitem?nodeRef=" + nodeRef
                            });
                        },

                        /**
                         * Public functions Functions designed to be called form
                         * external sources
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

                            for (i = 0; i < len; i++)
                            {
                                record = recordSet.getRecord(i + startRecord);
                                this.selectedItems[record.getData("nodeRef")] = checks[i].checked = fnCheck(record
                                        .getData("type"), checks[i].checked);
                            }

                            Bubbling.fire(this.scopeId + "selectedItemsChanged");
                        },
                        /**
                         * Selected Items Changed event handler. Determines
                         * whether to enable or disable the multi-item action
                         * drop-down
                         * 
                         * @method onSelectedItemsChanged
                         * @param layer
                         *            {object} Event fired
                         * @param args
                         *            {array} Event parameters (depends on event
                         *            type)
                         */
                        onSelectedItemsChanged : function DataListToolbar_onSelectedItemsChanged(layer, args)
                        {
                            var items = this.getSelectedItems(), item, userAccess = {}, itemAccess, menuItems = this.widgets.selectedItems
                                    .getMenu().getItems(), menuItem, actionPermissions, disabled, i, ii;

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
                                                if (!userAccess[actionPermissions[i]])
                                                {
                                                    disabled = true;
                                                    break;
                                                }
                                            }
                                        }

                                        menuItem.cfg.setProperty("disabled", disabled);
                                    }
                                }
                            }
                            this.widgets.selectedItems.set("disabled", (items.length === 0));
                        },
                        /**
                         * Deselect currectly selected assets.
                         * 
                         * @method onActionDeselectAll
                         */
                        onActionDeselectAll : function DataListToolbar_onActionDeselectAll()
                        {
                            this.selectItems("selectNone");
                        },
                        /**
                         * User Access event handler
                         * 
                         * @method onUserAccess
                         * @param layer
                         *            {object} Event fired
                         * @param args
                         *            {array} Event parameters (depends on event
                         *            type)
                         */
                        onUserAccess : function DataListToolbar_onUserAccess(layer, args)
                        {
                            var obj = args[1];
                            if (obj && obj.userAccess)
                            {
                                var widget, widgetPermissions, orPermissions, orMatch;
                                for ( var index in this.widgets)
                                {
                                    if (this.widgets.hasOwnProperty(index))
                                    {
                                        widget = this.widgets[index];

                                        // Skip if this action specifies
                                        // "no-access-check"
                                        if (widget["get"] && widget.get("srcelement") != null && widget
                                                .get("srcelement").className != "no-access-check")
                                        {
                                            // Default to disabled: must be
                                            // enabled via
                                            // permission
                                            widget.set("disabled", false);
                                            if (typeof widget.get("value") == "string")
                                            {
                                                // Comma-separation indicates
                                                // "AND"
                                                widgetPermissions = widget.get("value").split(",");
                                                for (var i = 0, ii = widgetPermissions.length; i < ii; i++)
                                                {
                                                    // Pipe-separation is a
                                                    // special case and
                                                    // indicates an "OR" match.
                                                    // The matched
                                                    // permission is stored in
                                                    // "activePermission" on the
                                                    // widget.
                                                    if (widgetPermissions[i].indexOf("|") !== -1)
                                                    {
                                                        orMatch = false;
                                                        orPermissions = widgetPermissions[i].split("|");
                                                        for (var j = 0, jj = orPermissions.length; j < jj; j++)
                                                        {
                                                            if (obj.userAccess[orPermissions[j]])
                                                            {
                                                                orMatch = true;
                                                                widget.set("activePermission", orPermissions[j], true);
                                                                break;
                                                            }
                                                        }
                                                        if (!orMatch)
                                                        {
                                                            widget.set("disabled", true);
                                                            break;
                                                        }
                                                    }
                                                    else if (!obj.userAccess[widgetPermissions[i]])
                                                    {
                                                        widget.set("disabled", true);
                                                        break;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
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
                         * Show more actions pop-up.
                         * 
                         * @method onActionShowMore
                         * @param record
                         *            {object} Object literal representing file
                         *            or folder to be actioned
                         * @param elMore
                         *            {element} DOM Element of "More Actions"
                         *            link
                         */
                        onActionShowMore : function EntityDataGrid_onActionShowMore(record, elMore)
                        {
                            var me = this;

                            // Fix "More Actions" hover style
                            Dom.addClass(elMore.firstChild, "highlighted");

                            // Get the pop-up div, sibling of the "More Actions"
                            // link
                            var elMoreActions = Dom.getNextSibling(elMore);
                            Dom.removeClass(elMoreActions, "hidden");
                            me.showingMoreActions = true;

                            // Hide pop-up timer function
                            var fnHidePopup = function DL_oASM_fnHidePopup()
                            {
                                // Need to rely on the "elMoreActions" enclosed
                                // variable, as MSIE doesn't support
                                // parameter passing for timer functions.
                                Event.removeListener(elMoreActions, "mouseover");
                                Event.removeListener(elMoreActions, "mouseout");
                                Dom.removeClass(elMore.firstChild, "highlighted");
                                Dom.addClass(elMoreActions, "hidden");
                                me.showingMoreActions = false;
                                if (me.deferredActionsMenu !== null)
                                {
                                    Dom.addClass(me.currentActionsMenu, "hidden");
                                    me.currentActionsMenu = me.deferredActionsMenu;
                                    me.deferredActionsMenu = null;
                                    Dom.removeClass(me.currentActionsMenu, "hidden");
                                }
                            };

                            // Initial after-click hide timer - 4x the mouseOut
                            // timer delay
                            if (elMoreActions.hideTimerId)
                            {
                                window.clearTimeout(elMoreActions.hideTimerId);
                            }
                            elMoreActions.hideTimerId = window.setTimeout(fnHidePopup,
                                    me.options.actionsPopupTimeout * 4);

                            // Mouse over handler
                            var onMouseOver = function DLSM_onMouseOver(e, obj)
                            {
                                // Clear any existing hide timer
                                if (obj.hideTimerId)
                                {
                                    window.clearTimeout(obj.hideTimerId);
                                    obj.hideTimerId = null;
                                }
                            };

                            // Mouse out handler
                            var onMouseOut = function DLSM_onMouseOut(e, obj)
                            {
                                var elTarget = Event.getTarget(e);
                                var related = elTarget.relatedTarget;

                                // In some cases we should ignore this mouseout
                                // event
                                if ((related !== obj) && (!Dom.isAncestor(obj, related)))
                                {
                                    if (obj.hideTimerId)
                                    {
                                        window.clearTimeout(obj.hideTimerId);
                                    }
                                    obj.hideTimerId = window.setTimeout(fnHidePopup, me.options.actionsPopupTimeout);
                                }
                            };

                            Event.on(elMoreActions, "mouseover", onMouseOver, elMoreActions);
                            Event.on(elMoreActions, "mouseout", onMouseOut, elMoreActions);
                        },

                        /**
                         * BUBBLING LIBRARY EVENT HANDLERS FOR PAGE EVENTS
                         * Disconnected event handlers for inter-component event
                         * notification
                         */

                        /**
                         * Current DataList changed event handler
                         * 
                         * @method onActiveDataListChanged
                         * @param layer
                         *            {object} Event fired (unused)
                         * @param args
                         *            {array} Event parameters (unused)
                         */
                        onActiveDataListChanged : function EntityDataGrid_onActiveDataListChanged(layer, args)
                        {
                            var obj = args[1];
                            if ((obj !== null) && (obj.dataList !== null))
                            {
                                this.datalistMeta = obj.dataList;
                                this.entity = obj.entity;

                                if (obj.list != null && (this.options.list == null || this.options.list.length < 1))
                                {
                                    this.options.list = obj.list;
                                }

                                // Could happen more than once, so check return
                                // value of
                                // fulfil()
                                if (!this.deferredListPopulation.fulfil("onActiveDataListChanged"))
                                {
                                    this.populateDataGrid();
                                }
                            }
                        },

                        /**
                         * Data List modified event handler
                         * 
                         * @method onDataListDetailsUpdated
                         * @param layer
                         *            {object} Event fired (unused)
                         * @param args
                         *            {array} Event parameters (unused)
                         */
                        onDataListDetailsUpdated : function EntityDataGrid_onDataListDetailsUpdated(layer, args)
                        {
                            var obj = args[1];
                            if ((obj !== null) && (obj.dataList !== null))
                            {
                                this.dataListMeta = obj.dataList;
                                this.renderDataListMeta();
                            }
                        },

                        /**
                         * DataGrid Refresh Required event handler
                         * 
                         * @method onDataGridRefresh
                         * @param layer
                         *            {object} Event fired (unused)
                         * @param args
                         *            {array} Event parameters (unused)
                         */
                        onDataGridRefresh : function EntityDataGrid_onDataGridRefresh(layer, args)
                        {
                            this._updateDataGrid.call(this,
                            {
                                page : this.currentPage,
                                updateOnly : args[1] != null && args[1].updateOnly ? args[1].updateOnly : false,
                                callback : args[1] != null && args[1].updateOnly ? args[1].callback : null,
                            });
                        },

                        /**
                         * DataList View change filter request event handler
                         * 
                         * @method onChangeFilter
                         * @param layer
                         *            {object} Event fired (unused)
                         * @param args
                         *            {array} Event parameters (new filterId)
                         */
                        onChangeFilter : function EntityDataGrid_onChangeFilter(layer, args)
                        {
                            var obj = args[1];
                            if ((obj !== null) && (obj.filterId !== null))
                            {
                                // Should be a filter in the arguments
                                var filter = Alfresco.util.cleanBubblingObject(obj), strFilter = YAHOO.lang.substitute(
                                        "{filterId}|{filterData}", filter, function(p_key, p_value, p_meta)
                                        {
                                            return typeof p_value == "undefined" ? "" : window.escape(p_value);
                                        }), aFilters = strFilter.split("|");

                                // Remove trailing blank entry
                                if (aFilters[1].length === 0)
                                {
                                    strFilter = aFilters[0];
                                }

                                Alfresco.logger.debug("EntityDataGrid_onChangeFilter: ", filter);

                                // Initial navigation won't fire the History
                                // event
                                if (obj.datagridFirstTimeNav || !this.options.useHistoryManager)
                                {
                                    this._updateDataGrid.call(this,
                                    {
                                        filter : filter,
                                        page : this.currentPage
                                    });
                                }
                                else
                                {
                                    var objNav = {};
                                    if (this.options.usePagination)
                                    {
                                        this.currentPage = 1;
                                    }
                                    objNav[this.scopeId + "filter"] = strFilter;

                                    Alfresco.logger.debug("EntityDataGrid_onChangeFilter: objNav = ", objNav);
                                    YAHOO.util.History.multiNavigate(objNav);
                                }
                            }
                        },

                        /**
                         * Data Item created event handler
                         * 
                         * @method onDataItemCreated
                         * @param layer
                         *            {object} Event fired
                         * @param args
                         *            {array} Event parameters (depends on event
                         *            type)
                         */
                        onDataItemCreated : function EntityDataGrid_onDataItemCreated(layer, args)
                        {
                            var obj = args[1];
                            if (obj && (obj.nodeRef !== null))
                            {

                                var nodeRef = new Alfresco.util.NodeRef(obj.nodeRef), url = this.options.itemUrl + nodeRef.uri + ((this.options.entityNodeRef != null && this.options.entityNodeRef.length > 0) ? "?entityNodeRef=" + this.options.entityNodeRef + "&"
                                        : "?") + "itemType=" + encodeURIComponent(this.options.itemType != null ? this.options.itemType
                                        : this.datalistMeta.itemType) + "&dataListName=" + encodeURIComponent(this.datalistMeta.name != null ? this.datalistMeta.name
                                        : this.options.list) + "&site=" + this.options.siteId;

                                // Reload the node's metadata
                                Alfresco.util.Ajax
                                        .jsonPost(
                                        {
                                            url : url,
                                            dataObj : this._buildDataGridParams(),
                                            successCallback :
                                            {
                                                fn : function EntityDataGrid_onDataItemCreated_refreshSuccess(response)
                                                {

                                                    if (response.json && (response.json.item !== null))
                                                    {
                                                        var item = response.json.item;

                                                        var fnAfterUpdate = function EntityDataGrid_onDataItemCreated_refreshSuccess_fnAfterUpdate()
                                                        {
                                                            var recordFound = this._findRecordByParameter(nodeRef,
                                                                    "nodeRef");
                                                            if (recordFound !== null)
                                                            {
                                                                var el = this.widgets.dataTable.getTrEl(recordFound);
                                                                Alfresco.util.Anim.pulse(el);
                                                            }

                                                            if (obj.callback)
                                                            {
                                                                obj.callback.call(response.json.item);
                                                            }
                                                        };
                                                        this.afterDataGridUpdate.push(fnAfterUpdate);

                                                        if (response.json.lastSiblingNodeRef != null)
                                                        {
                                                            var prevRecord = this._findRecordByParameter(
                                                                    response.json.lastSiblingNodeRef, "nodeRef");
                                                            if (prevRecord !== null)
                                                            {
                                                                var idx = this.widgets.dataTable
                                                                        .getRecordIndex(prevRecord);
                                                                this.widgets.dataTable.addRow(item, idx + 1);
                                                            }
                                                        }
                                                        else
                                                        {
                                                            this.widgets.dataTable.addRow(item);
                                                        }

                                                        Bubbling.fire("dirtyDataTable");

                                                    }
                                                },
                                                scope : this
                                            },
                                            failureCallback :
                                            {
                                                fn : function EntityDataGrid_onDataItemCreated_refreshFailure(response)
                                                {
                                                    Alfresco.util.PopupManager.displayMessage(
                                                    {
                                                        text : this.msg("message.create.refresh.failure")
                                                    });
                                                },
                                                scope : this
                                            }
                                        });
                            }
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
                        onDataItemUpdated : function EntityDataGrid_onDataItemUpdated(layer, args)
                        {
                            var obj = args[1];
                            if (obj && (obj.nodeRef !== null))
                            {
                                var nodeRef = new Alfresco.util.NodeRef(obj.nodeRef);
                                var fnAfterUpdate = function EntityDataGrid_onDataItemCreated_refreshSuccess_fnAfterUpdate()
                                {
                                    var recordFound = this._findRecordByParameter(nodeRef, "nodeRef");
                                    if (recordFound !== null)
                                    {
                                        var el = this.widgets.dataTable.getTrEl(recordFound);
                                        Alfresco.util.Anim.pulse(el);
                                    }

                                    if (obj.callback)
                                    {
                                        obj.callback.call(recordFound);
                                    }
                                };

                                this.afterDataGridUpdate.push(fnAfterUpdate);

                                this._updateDataGrid.call(this,
                                {
                                    page : this.currentPage
                                });

                            }
                        },

                        /**
                         * Data Items deleted event handler
                         * 
                         * @method onDataItemsDeleted
                         * @param layer
                         *            {object} Event fired
                         * @param args
                         *            {array} Event parameters (depends on event
                         *            type)
                         */
                        onDataItemsDeleted : function EntityDataGrid_onDataItemsDeleted(layer, args)
                        {
                            var obj = args[1];
                            if (obj && (obj.items !== null))
                            {
                                var recordFound, el;

                                for (var i = 0, ii = obj.items.length; i < ii; i++)
                                {
                                    recordFound = this._findRecordByParameter(obj.items[i].nodeRef, "nodeRef");
                                    if (recordFound !== null)
                                    {
                                        el = this.widgets.dataTable.getTrEl(recordFound);
                                        Alfresco.util.Anim.fadeOut(el);
                                    }
                                }

                                this._updateDataGrid.call(this,
                                {
                                    page : this.currentPage
                                });
                            }
                        },

                        onColumnRenamed : function EntityDataGrid_onColumnRenamed(layer, args)
                        {
                            var obj = args[1];
                            if (obj && (obj.columnId !== null))
                            {

                                var oColumn = this.widgets.dataTable.getColumn(obj.columnId);
                                if (oColumn)
                                {
                                    if (oColumn.hidden  ) {
                                        this.widgets.dataTable.showColumn(oColumn);
                                       // Dom.removeClass(elCell.parentNode, "yui-dt-hidden");
                                    }
                                    
                                    oColumn.label = obj.label;
                                    this.widgets.dataTable.formatTheadCell(oColumn._elThLabel, oColumn,
                                            this.widgets.dataTable.get("sortedBy"));

                                }
                            }
                        },

                        /**
                         * PRIVATE FUNCTIONS
                         */

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
                        _findNextItemByParameter : function EntityDataGrid__findNextItemByParameter(p_value,
                                p_parameter)
                        {
                            var recordSet = this.widgets.dataTable.getRecordSet();
                            for (var i = 0, j = recordSet.getLength(); i < j; i++)
                            {
                                if (recordSet.getRecord(i).getData(p_parameter) == p_value)
                                {
                                    if ((i + 1) != j)
                                    {
                                        return recordSet.getRecord(i + 1).getData();
                                    }
                                }
                            }
                            return null;
                        },
                        /**
                         * Searches the recordSet for the prev item of a record
                         * with the given parameter value
                         * 
                         * @method _findPrevItemByParameter
                         * @private
                         * @param p_value
                         *            {string} Value to find (to get the
                         *            previous)
                         * @param p_parameter
                         *            {string} Parameter to look for the value
                         *            in
                         */
                        _findPrevItemByParameter : function EntityDataGrid__findNextItemByParameter(p_value,
                                p_parameter)
                        {
                            var recordSet = this.widgets.dataTable.getRecordSet();
                            for (var i = 0, j = recordSet.getLength(); i < j; i++)
                            {
                                if (recordSet.getRecord(i).getData(p_parameter) == p_value)
                                {
                                    if ((i - 1) >= 0)
                                    {
                                        return recordSet.getRecord(i - 1).getData();
                                    }
                                }
                            }
                            return null;
                        },
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
                        _setDefaultDataTableErrors : function EntityDataGrid__setDefaultDataTableErrors(dataTable)
                        {
                            var msg = Alfresco.util.message;
                            dataTable.set("MSG_EMPTY", msg("message.empty", "beCPG.module.EntityDataGrid"));
                            dataTable.set("MSG_ERROR", msg("message.error", "beCPG.module.EntityDataGrid"));
                        },

                        /**
                         * Updates all Data Grid data by calling repository
                         * webscript with current list details
                         * 
                         * @method _updateDataGrid
                         * @private
                         * @param p_obj.filter
                         *            {object} Optional filter to navigate with
                         */
                        _updateDataGrid : function EntityDataGrid__updateDataGrid(p_obj)
                        {
                            p_obj = p_obj || {};
                            var successFilter = YAHOO.lang.merge({}, p_obj.filter !== undefined ? p_obj.filter
                                    : this.currentFilter), loadingMessage = null, timerShowLoadingMessage = null, me = this, params =
                            {
                                filter : successFilter,
                                page : p_obj.page
                            };

                            // Clear the current document list if the data
                            // webscript is
                            // taking too long
                            var fnShowLoadingMessage = function EntityDataGrid_fnShowLoadingMessage()
                            {
                                // Check the timer still exists. This is to
                                // prevent IE
                                // firing the event after we cancelled it. Which
                                // is
                                // "useful".
                                if (timerShowLoadingMessage)
                                {
                                    loadingMessage = Alfresco.util.PopupManager.displayMessage(
                                    {
                                        displayTime : 0,
                                        text : '<span class="wait">' + $html(this.msg("message.loading")) + '</span>',
                                        noEscape : true
                                    });

                                    if (YAHOO.env.ua.ie > 0)
                                    {
                                        this.loadingMessageShowing = true;
                                    }
                                    else
                                    {
                                        loadingMessage.showEvent.subscribe(function()
                                        {
                                            this.loadingMessageShowing = true;
                                        }, this, true);
                                    }
                                }
                            };

                            // Reset the custom error messages
                            this._setDefaultDataTableErrors(this.widgets.dataTable);

                            // More Actions menu no longer relevant
                            this.showingMoreActions = false;

                            // Slow data webscript message
                            this.loadingMessageShowing = false;
                            timerShowLoadingMessage = YAHOO.lang.later(this.options.loadingMessageDelay, this,
                                    fnShowLoadingMessage);
                            var destroyLoaderMessage = null;
                            destroyLoaderMessage = function EntityDataGrid__uDG_destroyLoaderMessage()
                            {
                                if (timerShowLoadingMessage)
                                {
                                    // Stop the "slow loading" timed function
                                    timerShowLoadingMessage.cancel();
                                    timerShowLoadingMessage = null;
                                }

                                if (loadingMessage)
                                {
                                    if (this.loadingMessageShowing)
                                    {
                                        // Safe to destroy
                                        loadingMessage.destroy();
                                        loadingMessage = null;
                                    }
                                    else
                                    {
                                        // Wait and try again later. Scope
                                        // doesn't get set
                                        // correctly with "this"
                                        if (destroyLoaderMessage != null)
                                        {
                                            YAHOO.lang.later(100, me, destroyLoaderMessage);
                                        }
                                    }
                                }
                            };

                            var successHandler = function EntityDataGrid__uDG_successHandler(sRequest, oResponse,
                                    oPayload)
                            {
                                destroyLoaderMessage();

                                if (p_obj.updateOnly && this.scopeId == "")
                                {
                                    this.widgets.dataTable.onDataReturnUpdateRows.call(this.widgets.dataTable,
                                            sRequest, oResponse, oPayload);
                                    if (p_obj.callback)
                                    {
                                        p_obj.callback.call(this);
                                    }

                                }
                                else
                                {
                                    // Updating the DataGrid may change the item
                                    // selection
                                    var fnAfterUpdate = function EntityDataGrid__uDG_sH_fnAfterUpdate()
                                    {
                                        Bubbling.fire(this.scopeId + "selectedFilesChanged");
                                    };
                                    this.afterDataGridUpdate.push(fnAfterUpdate);
                                    this.afterDataGridUpdate.push(this._addSortDnD);

                                    this.currentFilter = successFilter;
                                    this.currentPage = p_obj.page || 1;
                                    Bubbling.fire(this.scopeId + "filterChanged", successFilter);

                                    this.widgets.dataTable.onDataReturnReplaceRows.call(this.widgets.dataTable,
                                            sRequest, oResponse, oPayload);
                                }
                            };

                            var failureHandler = function EntityDataGrid__uDG_failureHandler(sRequest, oResponse)
                            {
                                destroyLoaderMessage();
                                // Clear out deferred functions
                                this.afterDataGridUpdate = [];

                                if (oResponse.status == 401)
                                {
                                    // Our session has likely timed-out, so
                                    // refresh to
                                    // offer the login page
                                    window.location.reload(true);
                                }
                                else
                                {
                                    try
                                    {
                                        var response = YAHOO.lang.JSON.parse(oResponse.responseText);
                                        this.widgets.dataTable.set("MSG_ERROR", response.message);
                                        this.widgets.dataTable.showTableMessage(response.message,
                                                YAHOO.widget.DataTable.CLASS_ERROR);
                                        if (oResponse.status == 404)
                                        {
                                            // Site or container not found -
                                            // deactivate
                                            // controls
                                            Bubbling.fire("deactivateAllControls");
                                        }
                                    }
                                    catch (e)
                                    {
                                        this._setDefaultDataTableErrors(this.widgets.dataTable);
                                    }
                                }
                            };

                            // Update the DataSource
                            var requestParams = this._buildDataGridParams(params);

                            if (me.options.postMethod)
                            {
                                this.widgets.dataSource.connMgr.setDefaultPostHeader(Alfresco.util.Ajax.JSON);
                                requestParams = YAHOO.lang.JSON.stringify(requestParams);

                                if (Alfresco.util.CSRFPolicy.isFilterEnabled())
                                {
                                    this.widgets.dataSource.connMgr.initHeader(Alfresco.util.CSRFPolicy.getHeader(),
                                            Alfresco.util.CSRFPolicy.getToken(), false);
                                }

                            }
                            else
                            {
                                requestParams = "&metadata=" + encodeURIComponent(YAHOO.lang.JSON
                                        .stringify(requestParams));
                            }

                            this.widgets.dataSource.sendRequest(requestParams,
                            {
                                success : successHandler,
                                failure : failureHandler,
                                scope : this
                            });
                        },
                        /**
                         * Build URI parameter string for doclist JSON data
                         * webscript
                         * 
                         * @method _buildDataGridParams
                         * @param p_obj.filter
                         *            {string} [Optional] Current filter
                         * @return {Object} Request parameters. Can be given
                         *         directly to Alfresco.util.Ajax, but must be
                         *         JSON.stringified elsewhere.
                         */
                        _buildDataGridParams : function EntityDataGrid__buildDataGridParams(p_obj)
                        {

                            var request =
                            {
                                fields : this.dataRequestFields,
                                page : p_obj && p_obj.page ? p_obj.page : this.currentPage,
                                queryExecutionId : this.queryExecutionId,
                                extraParams : this.options.extraParams
                            };

                            if (p_obj && p_obj.filter)
                            {
                                request.filter =
                                {
                                    filterOwner : p_obj.filter.filterOwner,
                                    filterId : p_obj.filter.filterId,
                                    filterData : p_obj.filter.filterData,
                                    filterParams : this._createFilterURLParameters(p_obj.filter,
                                            this.options.filterParameters)
                                };
                            }

                            return request;
                        },

                        /**
                         * @returns {String}
                         */
                        _buildSortParam : function EntityDataGrid__buildSortParam()
                        {
                            var sortUrl = "";
                            if (this.options.groupBy != null && this.options.groupBy.length > 0)
                            {
                                sortUrl += "&sort=" + this.options.groupBy.replace("prop_", "").replace("_", ":");
                            }
                            if (this.options.sortId != null)
                            {
                                sortUrl += "&sortId=" + this.options.sortId;
                            }
                            return sortUrl;
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
                        _findRecordByParameter : function EntityDataGrid__findRecordByParameter(p_value, p_parameter)
                        {
                            var recordSet = this.widgets.dataTable.getRecordSet();
                            for (var i = 0, j = recordSet.getLength(); i < j; i++)
                            {
                                if (recordSet.getRecord(i).getData(p_parameter) == p_value)
                                {
                                    return recordSet.getRecord(i);
                                }
                            }
                            return null;
                        },

                        /**
                         * Takes a filter and looks for its url parameter
                         * representation
                         * 
                         * @method createFilterURLParameters
                         * @param filter
                         *            {object} The filter to create url
                         *            parameters for
                         * @param filterParameters
                         *            {Array} List of configured filter
                         *            parameters that shall create url
                         *            parameters
                         * @return URL parameters created from the instructions
                         *         in filterParameters based on data from the
                         *         filter OR null no instructions were found
                         * @override
                         */
                        _createFilterURLParameters : function DateFilter_createFilterURLParameters(filter,
                                filterParameters)
                        {
                            if (YAHOO.lang.isString(filter.filterData))
                            {
                                var filterParameter;
                                for (var fpi = 0, fpil = filterParameters.length; fpi < fpil; fpi++)
                                {
                                    filterParameter = filterParameters[fpi];
                                    if ((filter.filterId == filterParameter.id || filterParameter.id == "*") && (filter.filterData == filterParameter.data || filterParameter.data == "*"))
                                    {
                                        return this._substituteParameters(filterParameter.parameters,
                                        {
                                            id : filter.filterId,
                                            data : filter.filterData
                                        });
                                    }
                                }
                            }
                            return null;
                        },

                        /**
                         * Takes a template and performs substituion against
                         * "Obj" and according to date instructions as described
                         * below. Assumes the template data may contain date
                         * instructions where the instructions are placed inside
                         * curly brackets: "param={attr}" - the name of an
                         * attribute in "obj" "param={0dt}" - the current date
                         * time in iso8601 format "param={1d}" - the current
                         * date (time set to end of day) and rolled l days
                         * forward "param={-2d}" - the current date (time set to
                         * end of day) and rolled 2 days backward
                         * 
                         * @param template
                         *            The template containing attributes from
                         *            obj and dates to resolve
                         * @param obj
                         *            Contains runtime values
                         */
                        _substituteParameters : function(template, obj)
                        {
                            var unresolvedTokens = template.match(/{[^}]+}/g);
                            if (unresolvedTokens)
                            {
                                var resolvedTokens = {}, name, value, date;
                                for (var i = 0, il = unresolvedTokens.length; i < il; i++)
                                {
                                    name = unresolvedTokens[i].substring(1, unresolvedTokens[i].length - 1);
                                    value = name;
                                    date = new Date();
                                    if (/^[\-\+]?\d+(d|dt)$/.test(value))
                                    {
                                        if (/^[\-\+]?\d+(d)$/.test(value))
                                        {
                                            // Only date (and not datetime) that
                                            // was requested
                                            date.setHours(11);
                                            date.setMinutes(59);
                                            date.setSeconds(59);
                                            date.setMilliseconds(999);
                                        }
                                        date.setDate(date.getDate() + parseInt(value));
                                        value = date;
                                    }
                                    else
                                    {
                                        value = obj[name];
                                    }
                                    resolvedTokens[name] = Alfresco.util.isDate(value) ? Alfresco.util.toISO8601(value)
                                            : value;
                                }
                                return YAHOO.lang.substitute(template, resolvedTokens);
                            }
                            return template;
                        }

                    }, true);
})();
