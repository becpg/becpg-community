 
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
   var Dom = YAHOO.util.Dom,
       Event = YAHOO.util.Event,
       Selector = YAHOO.util.Selector,
       Bubbling = YAHOO.Bubbling;

   /**
    * Alfresco Slingshot aliases
    */
   var $html = Alfresco.util.encodeHTML,
      $links = Alfresco.util.activateLinks,
      $combine = Alfresco.util.combinePaths,
      $userProfile = Alfresco.util.userProfileLink;

  /**
    * Entity DataGrid constructor.
    * 
    * @param htmlId {String} The HTML id of the parent element
    * @return {beCPG.component.EntityDataGrid} The new EntityDataGrid instance
    * @constructor
    */
   beCPG.module.EntityDataGrid = function(htmlId)
   {
	   
      beCPG.module.EntityDataGrid.superclass.constructor.call(this, "beCPG.module.EntityDataGrid", htmlId, ["button", "container", "datasource", "datatable", "paginator", "animation", "history"]);

      // Initialise prototype properties
      this.datalistMeta = {};
      this.datalistColumns = {};
      this.dataRequestFields = [];
      this.dataResponseFields = [];
      this.currentPage = 1;
      this.totalRecords = 0;
      this.showingMoreActions = false;
      this.currentFilter =
      {
         filterId: "all",
         filterData: ""
      };
      this.selectedItems = {};
      this.afterDataGridUpdate = [];

      /**
       * Decoupled event listeners
       */
     
      Bubbling.on("changeFilter", this.onChangeFilter, this);
      Bubbling.on("filterChanged", this.onFilterChanged, this);
      Bubbling.on("dataListDetailsUpdated", this.onDataListDetailsUpdated, this);
      Bubbling.on("dataItemCreated", this.onDataItemCreated, this);
      Bubbling.on("dataItemUpdated", this.onDataItemUpdated, this);
      Bubbling.on("dataItemsDeleted", this.onDataItemsDeleted, this);
      Bubbling.on("dataItemsDuplicated", this.onDataGridRefresh, this);
      Bubbling.on("activeDataListChanged", this.onActiveDataListChanged, this);
      Bubbling.on("dataGridContainerDestroyed", this.onDataGridContainerDestroyed, this);

      /* Deferred list population until DOM ready */
      this.deferredListPopulation = new Alfresco.util.Deferred(["onReady", "onActiveDataListChanged"],
      {
         fn: this.populateDataGrid,
         scope: this
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
   YAHOO.lang.augmentProto(beCPG.module.EntityDataGrid, Alfresco.service.DataListActions);

   /**
    * Augment prototype with main class implementation, ensuring overwrite is enabled
    */
   YAHOO.lang.augmentObject(beCPG.module.EntityDataGrid.prototype,
   {
      /**
       * Object container for initialization options
       *
       * @property options
       * @type object
       */
      options:
      {
         /**
          * Current siteId.
          * 
          * @property siteId
          * @type string
          * @default ""
          */
         siteId: "",

         /**
          * ContainerId representing root container.
          *
          * @property containerId
          * @type string
          * @default "dataLists"
          */
         containerId: "dataLists",

         /**
          * Flag indicating whether pagination is available or not.
          * 
          * @property usePagination
          * @type boolean
          * @default false
          */
         usePagination: false,

         /**
          * Initial page to show on load (otherwise taken from URL hash).
          * 
          * @property initialPage
          * @type int
          */
         initialPage: 1,

         /**
          * Number of items per page
          * 
          * @property pageSize
          * @type int
          */
         pageSize: 50,

         /**
          * Initial filter to show on load.
          * 
          * @property initialFilter
          * @type object
          */
         initialFilter: {},

         /**
          * Delay time value for "More Actions" popup, in milliseconds
          *
          * @property actionsPopupTimeout
          * @type int
          * @default 500
          */
         actionsPopupTimeout: 500,

         /**
          * Delay before showing "loading" message for slow data requests
          *
          * @property loadingMessageDelay
          * @type int
          * @default 1000
          */
         loadingMessageDelay: 1000,

         /**
          * How many actions to display before the "More..." container
          *
          * @property splitActionsAt
          * @type int
          * @default 3
          */
         splitActionsAt: 3,

         /**
           * Current entityNodeRef.
           * 
           * @property entityNodeRef
           * @type string
           * @default ""
           */
          entityNodeRef:"",
          
          /**
            * Current list.
            * 
            * @property list
            * @type string
            * @default ""
            */
           list:""
          
      },

      /**
       * Current page being browsed.
       * 
       * @property currentPage
       * @type int
       * @default 1
       */
      currentPage: null,
      
      /**
       * Total number of records (documents + folders) in the currentPath.
       * 
       * @property totalRecords
       * @type int
       * @default 0
       */
      totalRecords: null,

      /**
       * Current filter to filter document list.
       * 
       * @property currentFilter
       * @type object
       */
      currentFilter: null,

      /**
       * Object literal of selected states for visible items (indexed by nodeRef).
       * 
       * @property selectedItems
       * @type object
       */
      selectedItems: null,

      /**
       * Current actions menu being shown
       * 
       * @property currentActionsMenu
       * @type object
       * @default null
       */
      currentActionsMenu: null,

      /**
       * Whether "More Actions" pop-up is currently visible.
       * 
       * @property showingMoreActions
       * @type boolean
       * @default false
       */
      showingMoreActions: null,

      /**
       * Deferred actions menu element when showing "More Actions" pop-up.
       * 
       * @property deferredActionsMenu
       * @type object
       * @default null
       */
      deferredActionsMenu: null,

      /**
       * Deferred function calls for after a data grid update
       *
       * @property afterDataGridUpdate
       * @type array
       */
      afterDataGridUpdate: null,

      /**
       * Data List metadata retrieved from the Repository
       *
       * @param datalistMeta
       * @type Object
       */
      datalistMeta: null,

      /**
       * Data List columns from Form configuration
       *
       * @param datalistColumns
       * @type Object
       */
      datalistColumns: null,

      /**
       * Fields sent in the data request
       *
       * @param dataRequestFields
       * @type Object
       */
      dataRequestFields: null,

      /**
       * Fields returned from the data request
       *
       * @param dataResponseFields
       * @type Object
       */
      dataResponseFields: null,
      
      /**
       * Destroy method
       *
       * @param dataResponseFields
       * @type Object
       */
      onDataGridContainerDestroyed : function EntityDataGrid_onDataGridContainerDestroyed(){    	  
    	  
    	  
    	  if(this.widgets!=null){
	    	  Bubbling.unsubscribe("changeFilter", this.onChangeFilter);
	          Bubbling.unsubscribe("filterChanged", this.onFilterChanged);
	          Bubbling.unsubscribe("dataListDetailsUpdated", this.onDataListDetailsUpdated);
	          Bubbling.unsubscribe("dataItemCreated", this.onDataItemCreated);
	          Bubbling.unsubscribe("dataItemUpdated", this.onDataItemUpdated);
	          Bubbling.unsubscribe("dataItemsDeleted", this.onDataItemsDeleted);
	          Bubbling.unsubscribe("dataItemsDuplicated", this.onDataGridRefresh);
	          Bubbling.unsubscribe("activeDataListChanged", this.onActiveDataListChanged);
	          Bubbling.unsubscribe("dataGridContainerDestroyed", this.onDataGridContainerDestroyed);
        	  delete this.widgets;
          }

         },

      /**
       * DataTable Cell Renderers
       */

      /**
       * Returns selector custom datacell formatter
       *
       * @method fnRenderCellSelected
       */
      fnRenderCellSelected: function EntityDataGrid_fnRenderCellSelected()
      {
         var scope = this;
         
         /**
          * Selector custom datacell formatter
          *
          * @method renderCellSelected
          * @param elCell {object}
          * @param oRecord {object}
          * @param oColumn {object}
          * @param oData {object|string}
          */
         return function EntityDataGrid_renderCellSelected(elCell, oRecord, oColumn, oData)
         {
            Dom.setStyle(elCell, "width", oColumn.width + "px");
            Dom.setStyle(elCell.parentNode, "width", oColumn.width + "px");

            elCell.innerHTML = '<input id="checkbox-' + oRecord.getId() + '" type="checkbox" name="fileChecked" value="'+ oData + '"' + (scope.selectedItems[oData] ? ' checked="checked">' : '>');
         };
      },

      /**
       * Returns actions custom datacell formatter
       *
       * @method fnRenderCellActions
       */
      fnRenderCellActions: function EntityDataGrid_fnRenderCellActions()
      {
         var scope = this;
         
         /**
          * Actions custom datacell formatter
          *
          * @method renderCellActions
          * @param elCell {object}
          * @param oRecord {object}
          * @param oColumn {object}
          * @param oData {object|string}
          */
         return function EntityDataGrid_renderCellActions(elCell, oRecord, oColumn, oData)
         {
            Dom.setStyle(elCell, "width", oColumn.width + "px");
            Dom.setStyle(elCell.parentNode, "width", oColumn.width + "px");

            elCell.innerHTML = '<div id="' + scope.id + '-actions-' + oRecord.getId() + '" class="hidden"></div>';
         };
      },
      
      /**
       * Return data type-specific formatter
       *
       * @method getCellFormatter
       * @return {function} Function to render read-only value
       */
      getCellFormatter: function EntityDataGrid_getCellFormatter()
      {
         var scope = this;
         var booleanValueTrue = this.msg("data.boolean.true");
         var booleanValueFalse = this.msg("data.boolean.false");
         var reqTypeForbidden = this.msg("data.reqtype.forbidden");
         var reqTypeTolerated = this.msg("data.reqtype.tolerated");
         var reqTypeInfo = this.msg("data.reqtype.info");
         
         /**
          * Data Type custom formatter
          * beCPG - PQU : we want to navigate on beCPG objects
          *
          * @method renderCellDataType
          * @param elCell {object}
          * @param oRecord {object}
          * @param oColumn {object}
          * @param oData {object|string}
          */
         return function EntityDataGrid_renderCellDataType(elCell, oRecord, oColumn, oData)
         {
            var html = "", url = "";

            // Populate potentially missing parameters
            if (!oRecord)
            {
               oRecord = this.getRecord(elCell);
            }
            if (!oColumn)
            {
               oColumn = this.getColumn(elCell.parentNode.cellIndex);
            }

            if (oRecord && oColumn)
            {
               if (!oData)
               {
                  oData = oRecord.getData("itemData")[oColumn.field];
               }
            
               if (oData)
               {
                  var datalistColumn = scope.datalistColumns[oColumn.key];
                  if (datalistColumn)
                  {
                     oData = YAHOO.lang.isArray(oData) ? oData : [oData];
                     for (var i = 0, ii = oData.length, data; i < ii; i++)
                     {
                        data = oData[i];

                        switch (datalistColumn.dataType.toLowerCase())
                        {
                           case "cm:person":
                              html += '<span class="person">' + $userProfile(data.metadata, data.displayValue) + '</span>';
                              break;
                        
                           case "datetime":
                              html += Alfresco.util.formatDate(Alfresco.util.fromISO8601(data.value), scope.msg("date-format.default"));
                              break;
                     
                           case "date":
                              html += Alfresco.util.formatDate(Alfresco.util.fromISO8601(data.value), scope.msg("date-format.defaultDateOnly"));
                              break;
                     
                           case "cm:content":
                           case "cm:cmobject":
                           case "cm:folder":  
                           case "bcpg:supplier":
                           case "bcpg:client":
                           case "bcpg:entity":     	   
                        	  url  = scope._buildCellUrl(data); 
                              html += '<a href="' + url + '">';
                              html += '<img src="' + Alfresco.constants.URL_RESCONTEXT + 'components/images/filetypes/' + Alfresco.util.getFileIcon(data.displayValue, (data.metadata == "container" ? 'cm:folder' : null), 16) + '" width="16" alt="' + $html(data.displayValue) + '" title="' + $html(data.displayValue) + '" />';
                              html += ' ' + $html(data.displayValue) + '</a>'
                              break;
                            //beCPG : link and icons.
                           case "bcpg:product":
                        	   url  = scope._buildCellUrl(data);
                        	   if(datalistColumn.name == "bcpg:compoListProduct")
								{
                        		   var padding = 5 + oRecord.getData("itemData")["prop_bcpg_depthLevel"].value * 15;
                        		   html += '<span class="' + data.metadata + '" style="padding-left:' + padding + 'px;"><a href="' + url + '">' + $html(data.displayValue) + '</a></span>';                        	   
								}
                        	   	else if(datalistColumn.name == "bcpg:packagingListProduct")
								{
                        		   html += '<span class="' + data.metadata + '"><a href="' + url + '">' + $html(data.displayValue) + '</a></span>';                        	   
								}
								else
								{
									html += '<a href="' + url + '">' + $html(data.displayValue) + '</a>';
								}
                        	   break;
                           case "boolean":
								//color in red present allergens
								if((datalistColumn.name == "bcpg:allergenListVoluntary" || datalistColumn.name == "bcpg:allergenListInVoluntary") && data.displayValue == true)
								{											
									html += '<span class="presentAllergen">' + $html(data.displayValue == true ? booleanValueTrue : booleanValueFalse ) + '</span>';
								}
								else
								{
									html += $html(data.displayValue == true ? booleanValueTrue : booleanValueFalse );											
								}
								break;  
                           case "cm:authoritycontainer":
                               html += '<span class="userGroup">' + $html(data.displayValue) + '</span>';
                               break;                               
                           default:
                        	   if(datalistColumn.name == "bcpg:rclReqType" || datalistColumn.name == "bcpg:filReqType" || datalistColumn.name == "eco:culReqType")
                    		   {
                        		   if(data.displayValue == "Forbidden")
                        		   {
                        			   html += '<span class="reqTypeForbidden">' + $html(reqTypeForbidden) + '</span>';
                        		   }
                        		   else if(data.displayValue == "Tolerated")
                        		   {
                        			   html += '<span class="reqTypeTolerated">' + $html(reqTypeTolerated) + '</span>';
                        		   }
                        		   else if(data.displayValue == "Info")
                        		   {
                        			   html += '<span class="reqTypeInfo">' + $html(reqTypeInfo) + '</span>';
                        		   }                        		   
                        		   else
                    			   {
                        			   html += $html(data.displayValue);
                    			   }
                    		   }
                        	   else if(datalistColumn.name == "eco:rlRevisionType" || datalistColumn.name == "eco:culRevision")
                    		   {
                        		   if(data.displayValue != null)
                    			   {
                        			   html += scope.msg("data.revisiontype." + data.displayValue.toLowerCase());
                    			   }                        		                           		  
                    		   }
                        	   else if(datalistColumn.name == "qa:sdlControlPoint" || datalistColumn.name == "qa:slControlPoint")
                        	   {      
                        		   url  = scope._buildCellUrl(data);
                        		   html += '<span class="sample"><a href="' + url + '">' + $html(data.displayValue) + '</a></span>';
                        	   }
                        	   else if(datalistColumn.name == "qa:clCharacts")
                        	   {                        		   
                        		   html += '<span class="control">' + $html(data.displayValue) + '</span>';
                        	   }
                        	   else
                        	   {
                        		   html += $html(data.displayValue);
                        	   }
                              
                              break;
                        }

                        if (i < ii - 1)
                        {
                           html += "<br />";
                        }
                     }
                  }
               }
            }

            elCell.innerHTML = html;
         };
      },
      /**
       * Build the cell Url
       */
      _buildCellUrl : function EntityDataGrid__buildCellUrl(data){
    	  
    	  var containerType = "document", url = null;
    	  if(data.metadata!=null && data.metadata.length){
    		  containerType = (data.metadata == "container" ? 'folder' : 'document');
    	  }
    	  
          if (data.siteId)
          {
             url = Alfresco.constants.URL_PAGECONTEXT + "site/" + data.siteId+ "/"+containerType+ '-details?nodeRef=' + data.value;
          }
          else
          {
             url = Alfresco.constants.URL_PAGECONTEXT +containerType+ '-details?nodeRef=' + data.value;
          }
          return url;
    	  
    	  
      },
      
      /**
       * Return data type-specific sorter
       *
       * @method getSortFunction
       * @return {function} Function to sort column by
       */
      getSortFunction: function EntityDataGrid_getSortFunction()
      {
         /**
          * Data Type custom sorter
          *
          * @method sortFunction
          * @param a {object} Sort record a
          * @param b {object} Sort record b
          * @param desc {boolean} Ascending/descending flag
          * @param field {String} Field to sort by
          */
         return function EntityDataGrid_sortFunction(a, b, desc, field)
         {
            var fieldA = a.getData().itemData[field],
               fieldB = b.getData().itemData[field];

            if (YAHOO.lang.isArray(fieldA))
            {
               fieldA = fieldA[0];
            }
            if (YAHOO.lang.isArray(fieldB))
            {
               fieldB = fieldB[0];
            }

            // Deal with empty values
            if (!YAHOO.lang.isValue(fieldA))
            {
               return (!YAHOO.lang.isValue(fieldB)) ? 0 : 1;
            }
            else if (!YAHOO.lang.isValue(fieldB))
            {
               return -1;
            }
            
            var valA = fieldA.value,
               valB = fieldB.value;

            if (valA.indexOf && valA.indexOf("workspace://SpacesStore") == 0)
            {
               valA = fieldA.displayValue;
               valB = fieldB.displayValue;
            }

            return YAHOO.util.Sort.compare(valA, valB, desc);
         };
      },

      /**
       * Fired by YUI when parent element is available for scripting
       *
       * @method onReady
       */
      onReady: function EntityDataGrid_onReady()
      {
         var me = this;
         
         // Item Select menu button
         this.widgets.itemSelect = Alfresco.util.createYUIButton(this, "itemSelect-button", this.onItemSelect,
         {
            type: "menu", 
            menu: "itemSelect-menu",
            disabled: true
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
                  var asset = me.widgets.dataTable.getRecord(args[1].target.offsetParent).getData();
                  me[owner.className].call(me, asset, owner);
               }
            }
            return true;
         };
         Bubbling.addDefaultAction("action-link", fnActionHandler);
         Bubbling.addDefaultAction("show-more", fnActionHandler);

         // Hook filter change events
         var fnChangeFilterHandler = function EntityDataGrid_fnChangeFilterHandler(layer, args)
         {
            var owner = args[1].anchor;
            if (owner !== null)
            {
               var filter = owner.rel,
                  filters,
                  filterObj = {};
               if (filter && filter !== "")
               {
                  args[1].stop = true;
                  filters = filter.split("|");
                  filterObj =
                  {
                     filterOwner: window.unescape(filters[0] || ""),
                     filterId: window.unescape(filters[1] || ""),
                     filterData: window.unescape(filters[2] || ""),
                     filterDisplay: window.unescape(filters[3] || "")
                  };
                  Alfresco.logger.debug("DL_fnChangeFilterHandler", "changeFilter =>", filterObj);
                  Bubbling.fire("changeFilter", filterObj);
               }
            }
            return true;
         };
         Bubbling.addDefaultAction("filter-change", fnChangeFilterHandler);

         // DataList Actions module
         this.modules.actions = new Alfresco.module.DataListActions();

         // Reference to Data Grid component (required by actions module)
         this.modules.dataGrid = this;
         
         // Assume no list chosen for now
         Dom.removeClass(this.id + "-selectListMessage", "hidden");

         this.deferredListPopulation.fulfil("onReady");

         // Finally show the component body here to prevent UI artifacts on YUI button decoration
         Dom.setStyle(this.id + "-body", "visibility", "visible");
      },

      /**
       * Fired by YUI when History Manager is initialised and available for scripting.
       * Component initialisation, including instantiation of YUI widgets and event listener binding.
       *
       * @method onHistoryManagerReady
       */
      onHistoryManagerReady: function EntityDataGrid_onHistoryManagerReady()
      {
         // Fire changeFilter event for first-time population
         Alfresco.logger.debug("EntityDataGrid_onHistoryManagerReady", "changeFilter =>", this.options.initialFilter);
         Bubbling.fire("changeFilter", YAHOO.lang.merge(
         {
            datagridFirstTimeNav: true
         }, this.options.initialFilter));
      },

      /**
       * Display an error message pop-up
       *
       * @private
       * @method _onDataListFailure
       * @param response {Object} Server response object from Ajax request wrapper
       * @param message {Object} Object literal of the format:
       *    <pre>
       *       title: Dialog title string
       *       text: Dialog body message
       *    </pre>
       */
      _onDataListFailure: function EntityDataGrid__onDataListFailure(p_response, p_message)
      {
         Alfresco.util.PopupManager.displayPrompt(
         {
            title: p_message.title,
            text: p_message.text,
            modal: true,
            buttons: [
            {
               text: this.msg("button.ok"),
               handler: function EntityDataGrid__onDataListFailure_OK()
               {
                  this.destroy();
               },
               isDefault: true
            }]
         });
         
      },
      
      /**
       * Renders Data List metadata, i.e. title and description
       *
       * @method renderDataListMeta
       */
      renderDataListMeta: function EntityDataGrid_renderDataListMeta()
      {
         if (!YAHOO.lang.isObject(this.datalistMeta))
         {
            return;
         }
         
         Alfresco.util.populateHTML(
            [ this.id + "-title", $html(this.datalistMeta.entityName + " - " + this.datalistMeta.title) ],
            [ this.id + "-description", $links($html(this.datalistMeta.description, true)) ]
         );
      },

       /**
       * Retrieves the Data List from the Repository
       *
       * @method populateDataGrid
       */
      populateDataGrid: function EntityDataGrid_populateDataGrid()
      {
         if (!YAHOO.lang.isObject(this.datalistMeta))
         {
            return;
         }
         
         this.renderDataListMeta();
         
         // Query the visible columns for this list's item type
         Alfresco.util.Ajax.jsonGet(
         {
            url: $combine(Alfresco.constants.URL_SERVICECONTEXT, "components/data-lists/config/columns?itemType=" + encodeURIComponent(this.datalistMeta.itemType) + "&list=" + encodeURIComponent(this.options.list)),
            successCallback:
            {
               fn: this.onDatalistColumns,
               scope: this
            },
            failureCallback:
            {
               fn: this._onDataListFailure,
               obj:
               {
                  title: this.msg("message.error.columns.title"),
                  text: this.msg("message.error.columns.description")
               },
               scope: this
            }
         });
      },

      /**
       * Data List column definitions returned from the Repository
       *
       * @method onDatalistColumns
       * @param response {Object} Ajax data structure
       */
      onDatalistColumns: function EntityDataGrid_onDatalistColumns(response)
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
         // Enable item select menu
         this.widgets.itemSelect.set("disabled", false);

         // Continue only when History Manager fires its onReady event
         YAHOO.util.History.onReady(this.onHistoryManagerReady, this, true);
      },

      /**
       * History Manager set-up and event registration
       *
       * @method _setupHistoryManagers
       */
      _setupHistoryManagers: function EntityDataGrid__setupHistoryManagers()
      {
         /**
          * YUI History - filter
          */
         var bookmarkedFilter = YAHOO.util.History.getBookmarkedState("filter");
         bookmarkedFilter = bookmarkedFilter === null ? "all" : (YAHOO.env.ua.gecko > 0) ? bookmarkedFilter : window.escape(bookmarkedFilter);

         try
         {
            while (bookmarkedFilter != (bookmarkedFilter = decodeURIComponent(bookmarkedFilter))){}
         }
         catch (e1)
         {
            // Catch "malformed URI sequence" exception
         }
         
         var fnDecodeBookmarkedFilter = function EntityDataGrid_fnDecodeBookmarkedFilter(strFilter)
         {
            var filters = strFilter.split("|"),
               filterObj =
               {
                  filterId: window.unescape(filters[0] || ""),
                  filterData: window.unescape(filters[1] || "")
               };
            
            filterObj.filterOwner = Alfresco.util.FilterManager.getOwner(filterObj.filterId);
            return filterObj;
         };
         
         this.options.initialFilter = fnDecodeBookmarkedFilter(bookmarkedFilter);

         // Register History Manager filter update callback
         YAHOO.util.History.register("filter", bookmarkedFilter, function EntityDataGrid_onHistoryManagerFilterChanged(newFilter)
         {
            Alfresco.logger.debug("HistoryManager: filter changed:" + newFilter);
            // Firefox fix
            if (YAHOO.env.ua.gecko > 0)
            {
               newFilter = window.unescape(newFilter);
               Alfresco.logger.debug("HistoryManager: filter (after Firefox fix):" + newFilter);
            }
            
            this._updateDataGrid.call(this,
            {
               filter: fnDecodeBookmarkedFilter(newFilter)
            });
         }, null, this);


         /**
          * YUI History - page
          */
         var me = this;
         var handlePagination = function EntityDataGrid_handlePagination(state, me)
         {
            me.widgets.paginator.setState(state);
            YAHOO.util.History.navigate("page", String(state.page));
         };

         if (this.options.usePagination)
         {
            var bookmarkedPage = YAHOO.util.History.getBookmarkedState("page") || "1";
            while (bookmarkedPage != (bookmarkedPage = decodeURIComponent(bookmarkedPage))){}
            this.currentPage = parseInt(bookmarkedPage || this.options.initialPage, 10);

            // Register History Manager page update callback
            YAHOO.util.History.register("page", bookmarkedPage, function EntityDataGrid_onHistoryManagerPageChanged(newPage)
            {
               Alfresco.logger.debug("HistoryManager: page changed:" + newPage);
               me.widgets.paginator.setPage(parseInt(newPage, 10));
            }, null, this);

            // YUI Paginator definition
            this.widgets.paginator = new YAHOO.widget.Paginator(
            {
               containers: [this.id + "-paginator", this.id + "-paginatorBottom"],
               rowsPerPage: this.options.pageSize,
               initialPage: this.currentPage,
               template: this.msg("pagination.template"),
               pageReportTemplate: this.msg("pagination.template.page-report"),
               previousPageLinkLabel: this.msg("pagination.previousPageLinkLabel"),
               nextPageLinkLabel: this.msg("pagination.nextPageLinkLabel")
            });
            
            this.widgets.paginator.subscribe("changeRequest", handlePagination, this);
            
            // Display the bottom paginator bar
            Dom.setStyle(this.id + "-datagridBarBottom", "display", "block");
         }

         // Initialize the browser history management library
         try
         {
             YAHOO.util.History.initialize("yui-history-field", "yui-history-iframe");
         }
         catch (e2)
         {
            /*
             * The only exception that gets thrown here is when the browser is
             * not supported (Opera, or not A-grade)
             */
            Alfresco.logger.error(this.name + ": Couldn't initialize HistoryManager.", e2);
            this.onHistoryManagerReady();
         }
      },

      /**
       * DataSource set-up and event registration
       *
       * @method _setupDataSource
       * @protected
       */
      _setupDataSource: function EntityDataGrid__setupDataSource()
      {
         var listNodeRef = new Alfresco.util.NodeRef(this.datalistMeta.nodeRef);
         
         for (var i = 0, ii = this.datalistColumns.length; i < ii; i++)
         {
            var column = this.datalistColumns[i],
               columnName = column.name.replace(":", "_"),
               fieldLookup = (column.type == "property" ? "prop" : "assoc") + "_" + columnName;
            
            this.dataRequestFields.push(columnName);
            this.dataResponseFields.push(fieldLookup);
            this.datalistColumns[fieldLookup] = column;
         }
         
         var url = Alfresco.constants.PROXY_URI + "slingshot/datalists/data/node/" + listNodeRef.uri+"?itemType=" + encodeURIComponent(this.datalistMeta.itemType);
         
         // DataSource definition
         //beCPG - PQU : manage WUsed 
		if(this.datalistMeta.name == "WUsed")
		{
		  url = Alfresco.constants.PROXY_URI + "becpg/entity/wused/node/" + this.options.entityNodeRef.replace(":/", "");
		}
		
         // DataSource definition
         this.widgets.dataSource = new YAHOO.util.DataSource(url,
         {
            connMethodPost: true,
            responseType: YAHOO.util.DataSource.TYPE_JSON,
            responseSchema:
            {
               resultsList: "items",
               metaFields:
               {
                  paginationRecordOffset: "startIndex",
                  totalRecords: "totalRecords"
               }
            }
         });
         this.widgets.dataSource.connMgr.setDefaultPostHeader(Alfresco.util.Ajax.JSON);

         // Intercept data returned from data webscript to extract custom metadata
         this.widgets.dataSource.doBeforeCallback = function EntityDataGrid_doBeforeCallback(oRequest, oFullResponse, oParsedResponse)
         {
            // Container userAccess event
            var permissions = oFullResponse.metadata.parent.permissions;
            if (permissions && permissions.userAccess)
            {
               Bubbling.fire("userAccess",
               {
                  userAccess: permissions.userAccess
               });
            }
            
            return oParsedResponse;
         };
      },
      
      /**
       * DataTable set-up and event registration
       *
       * @method _setupDataTable
       * @protected
       */
      _setupDataTable: function EntityDataGrid__setupDataTable(columns)
      {
         // YUI DataTable column definitions
         var columnDefinitions =
         [
            { key: "nodeRef", label: "", sortable: false, formatter: this.fnRenderCellSelected(), width: 16 }
         ];
         
         var column;
         for (var i = 0, ii = this.datalistColumns.length; i < ii; i++)
         {
            column = this.datalistColumns[i];
            columnDefinitions.push(
            {
               key: this.dataResponseFields[i],
               label: column.label,
               sortable: true,
               sortOptions:
               {
                  field: column.formsName,
                  sortFunction: this.getSortFunction()
               },
               formatter: this.getCellFormatter(column.dataType)
            });
         }

         // Add actions as last column
         columnDefinitions.push(
            { key: "actions", label: this.msg("label.column.actions"), sortable: false, formatter: this.fnRenderCellActions(), width: 80 }
         );

         // DataTable definition
         var me = this;
         this.widgets.dataTable = new YAHOO.widget.DataTable(this.id + "-grid", columnDefinitions, this.widgets.dataSource,
         {
            renderLoopSize: this.options.usePagination ? 16 : 32,
            initialLoad: false,
            dynamicData: false,
            "MSG_EMPTY": this.msg("message.empty"),
            "MSG_ERROR": this.msg("message.error"),
            paginator: this.widgets.paginator
         });
         
         // Update totalRecords with value from server
         this.widgets.dataTable.handleDataReturnPayload = function EntityDataGrid_handleDataReturnPayload(oRequest, oResponse, oPayload)
         {
            me.totalRecords = oResponse.meta.totalRecords;
            oResponse.meta.pagination = 
            {
               rowsPerPage: me.options.pageSize,
               recordOffset: (me.currentPage - 1) * me.options.pageSize
            };
            return oResponse.meta;
         };

         // Override abstract function within DataTable to set custom error message
         this.widgets.dataTable.doBeforeLoadData = function EntityDataGrid_doBeforeLoadData(sRequest, oResponse, oPayload)
         {
            if (oResponse.error)
            {
               try
               {
                  var response = YAHOO.lang.JSON.parse(oResponse.responseText);
                  me.widgets.dataTable.set("MSG_ERROR", response.message);
               }
               catch(e)
               {
                  me._setDefaultDataTableErrors(me.widgets.dataTable);
               }
            }
            
            // We don't get an renderEvent for an empty recordSet, but we'd like one anyway
            if (oResponse.results.length === 0)
            {
               this.fireEvent("renderEvent",
               {
                  type: "renderEvent"
               });
            }
            
            // Must return true to have the "Loading..." message replaced by the error message
            return true;
         };

         // File checked handler
         this.widgets.dataTable.subscribe("checkboxClickEvent", function(e)
         { 
            var id = e.target.value; 
            this.selectedItems[id] = e.target.checked;
            Bubbling.fire("selectedItemsChanged");
         }, this, true);

         // Rendering complete event handler
         this.widgets.dataTable.subscribe("renderEvent", function()
         {
            Alfresco.logger.debug("DataTable renderEvent");
            
            // IE6 fix for long filename rendering issue
            if (YAHOO.env.ua.ie < 7)
            {
               var ie6fix = this.widgets.dataTable.getTableEl().parentNode;
               ie6fix.className = ie6fix.className;
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
         this.widgets.dataTable.subscribe("rowMouseoutEvent", this.onEventUnhighlightRow, this, true);
      },

      /**
       * Multi-item select button click handler
       *
       * @method onItemSelect
       * @param sType {string} Event type, e.g. "click"
       * @param aArgs {array} Arguments array, [0] = DomEvent, [1] = EventTarget
       * @param p_obj {object} Object passed back from subscribe method
       */
      onItemSelect: function EntityDataGrid_onItemSelect(sType, aArgs, p_obj)
      {
         var domEvent = aArgs[0],
            eventTarget = aArgs[1];

         // Select based upon the className of the clicked item
         this.selectItems(Alfresco.util.findEventClass(eventTarget));
         Event.preventDefault(domEvent);
      },

      /**
       * Custom event handler to highlight row.
       *
       * @method onEventHighlightRow
       * @param oArgs.event {HTMLEvent} Event object.
       * @param oArgs.target {HTMLElement} Target element.
       */
      onEventHighlightRow: function EntityDataGrid_onEventHighlightRow(oArgs)
      {
         // elActions is the element id of the active table cell where we'll inject the actions
         var elActions = Dom.get(this.id + "-actions-" + oArgs.target.id);

         // Inject the correct action elements into the actionsId element
         if (elActions && elActions.firstChild === null)
         {
            // Call through to get the row highlighted by YUI
            this.widgets.dataTable.onEventHighlightRow.call(this.widgets.dataTable, oArgs);

            // Clone the actionSet template node from the DOM
            var record = this.widgets.dataTable.getRecord(oArgs.target.id),
               clone = Dom.get(this.id + "-actionSet").cloneNode(true);
            
            // Token replacement
            clone.innerHTML = YAHOO.lang.substitute(window.unescape(clone.innerHTML), this.getActionUrls(record));

            // Generate an id
            clone.id = elActions.id + "_a";

            // Simple view by default
            Dom.addClass(clone, "simple");
            
            // Trim the items in the clone depending on the user's access
            var userAccess = record.getData("permissions").userAccess,
               actionLabels = record.getData("actionLabels") || {};
            
            // Inject the current filterId to allow filter-scoped actions
            userAccess["filter-" + this.currentFilter.filterId] = true;
            
            // Remove any actions the user doesn't have permission for
            var actions = YAHOO.util.Selector.query("div", clone),
               action, aTag, spanTag, actionPermissions, aP, i, ii, j, jj;

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
               // Insert the two necessary DIVs before the splitAt action item
               Dom.insertBefore(containerDivs[0], actions[splitAt]);
               Dom.insertBefore(containerDivs[1], actions[splitAt]);
               // Now make action items after the split, children of the 2nd DIV
               var index, moreActions = actions.slice(splitAt);
               for (index in moreActions)
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
       * @param oArgs.event {HTMLEvent} Event object.
       * @param oArgs.target {HTMLElement} Target element.
       */
      onEventUnhighlightRow: function EntityDataGrid_onEventUnhighlightRow(oArgs)
      {
         // Call through to get the row unhighlighted by YUI
         this.widgets.dataTable.onEventUnhighlightRow.call(this.widgets.dataTable, oArgs);

         var elActions = Dom.get(this.id + "-actions-" + (oArgs.target.id));

         // Don't hide unless the More Actions drop-down is showing, or a dialog mask is present
         if (!this.showingMoreActions || Dom.hasClass(document.body, "masked"))
         {
            // Just hide the action links, rather than removing them from the DOM
            Dom.addClass(elActions, "hidden");
            this.deferredActionsMenu = null;
         }
      },

      /**
       * The urls to be used when creating links in the action cell
       *
       * @method getActionUrls
       * @param record {YAHOO.widget.Record | Object} A data record, or object literal describing the item in the list
       * @return {object} Object literal containing URLs to be substituted in action placeholders
       */
      getActionUrls: function EntityDataGrid_getActionUrls(record)
      {
         var recordData = YAHOO.lang.isFunction(record.getData) ? record.getData() : record,
            nodeRef = recordData.nodeRef;

         return (
         {
            editMetadataUrl: "edit-dataitem?nodeRef=" + nodeRef
         });
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
      getSelectedItems: function EntityDataGrid_getSelectedItems()
      {
         var items = [],
            recordSet = this.widgets.dataTable.getRecordSet(),
            record;
         
         for (var i = 0, j = recordSet.getLength(); i < j; i++)
         {
            record = recordSet.getRecord(i);
            if (this.selectedItems[record.getData("nodeRef")])
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
       * @param p_selectType {string} Can be one of the following:
       * <pre>
       * selectAll - all items
       * selectNone - deselect all
       * selectInvert - invert selection
       * </pre>
       */
      selectItems: function EntityDataGrid_selectItems(p_selectType)
      {
         var recordSet = this.widgets.dataTable.getRecordSet(),
            checks = Selector.query('input[type="checkbox"]', this.widgets.dataTable.getTbodyEl()),
            aPageRecords = this.widgets.paginator.getPageRecords(),
            startRecord = aPageRecords[0],
            len = checks.length,
            record, i, fnCheck;

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
            this.selectedItems[record.getData("nodeRef")] = checks[i].checked = fnCheck(record.getData("type"), checks[i].checked);
         }
         
         Bubbling.fire("selectedItemsChanged");
      },


      /**
       * ACTIONS WHICH ARE LOCAL TO THE DATAGRID COMPONENT
       */

       /**
       * Edit Data Item pop-up
       *
       * @method onActionEdit
       * @param item {object} Object literal representing one data item
       */
      onActionEdit: function EntityDataGrid_onActionEdit(item)
      {
         var scope = this;
         
         // Intercept before dialog show
         var doBeforeDialogShow = function EntityDataGrid_onActionEdit_doBeforeDialogShow(p_form, p_dialog)
         {
            Alfresco.util.populateHTML(
               [ p_dialog.id + "-dialogTitle", this.msg("label.edit-row.title") ]
            );
            
        	// Is it a bulk action?
	   	     if(Dom.get(p_dialog.id  + "-form-bulkAction"))
	   		 {
   				Dom.get(p_dialog.id  + "-form-bulkAction").checked = this.bulkEdit;
   				Dom.get(p_dialog.id  + "-form-bulkAction-msg").innerHTML = this.msg("button.bulk-action-edit");
   			}

         };

         var templateUrl = YAHOO.lang.substitute(Alfresco.constants.URL_SERVICECONTEXT + "components/form?bulkEdit=true&entityNodeRef={entityNodeRef}&itemKind={itemKind}&itemId={itemId}&mode={mode}&submitType={submitType}&showCancelButton=true",
         {
            itemKind: "node",
            itemId: item.nodeRef,
            mode: "edit",
            submitType: "json",
            entityNodeRef : this.options.entityNodeRef
         });

         // Using Forms Service, so always create new instance
         
         var editDetails = new Alfresco.module.SimpleDialog(this.id + "-editDetails");
         editDetails.bulkEdit = false;
         editDetails.setOptions(
         {
            width: "34em",
            templateUrl: templateUrl,
            actionUrl: null,
            destroyOnHide: false,
            doBeforeDialogShow:
            {
               fn: doBeforeDialogShow,
               scope: this
            },
            onSuccess:
            {
               fn: function EntityDataGrid_onActionEdit_success(response)
               {
                  // Reload the node's metadata
                  Alfresco.util.Ajax.jsonPost(
                  {
                     url: Alfresco.constants.PROXY_URI + "slingshot/datalists/item/node/" + new Alfresco.util.NodeRef(item.nodeRef).uri,
                     dataObj: this._buildDataGridParams(),
                     successCallback:
                     {
                        fn: function EntityDataGrid_onActionEdit_refreshSuccess(response)
                        {
                           // Fire "itemUpdated" event
                           Bubbling.fire("dataItemUpdated",
                           {
                              item: response.json.item
                           });
                           
                           //recall edit for next item
                           var checkBoxEl =  Dom.get(this.id + "-editDetails" + "-form-bulkAction");
                           
	                       	if ( checkBoxEl && checkBoxEl.checked)
	              		    {
	                       		this.bulkEdit = true;
								var recordFound = scope._findNextItemByParameter(response.json.item.nodeRef, "nodeRef");	
								if(recordFound != null)
								{
									scope.onActionEdit(recordFound)
								}		
	              		    } else {
	              		    	this.bulkEdit = false;
	              		    }
                           
                           // Display success message
                           Alfresco.util.PopupManager.displayMessage(
                           {
                              text: this.msg("message.details.success")
                           });
                        },
                        scope: this
                     },
                     failureCallback:
                     {
                        fn: function EntityDataGrid_onActionEdit_refreshFailure(response)
                        {
                           Alfresco.util.PopupManager.displayMessage(
                           {
                              text: this.msg("message.details.failure")
                           });
                        },
                        scope: this
                     }
                  });
               },
               scope: this
            },
            onFailure:
            {
               fn: function EntityDataGrid_onActionEdit_failure(response)
               {
                  Alfresco.util.PopupManager.displayMessage(
                  {
                     text: this.msg("message.details.failure")
                  });
               },
               scope: this
            }
         }).show();
      },

      /**
       * BUBBLING LIBRARY EVENT HANDLERS FOR PAGE EVENTS
       * Disconnected event handlers for inter-component event notification
       */

      /**
       * Current DataList changed event handler
       *
       * @method onActiveDataListChanged
       * @param layer {object} Event fired (unused)
       * @param args {array} Event parameters (unused)
       */
      onActiveDataListChanged: function EntityDataGrid_onActiveDataListChanged(layer, args)
      {
         var obj = args[1];
         if ((obj !== null) && (obj.dataList !== null))
         {
            this.datalistMeta = obj.dataList;
            // Could happen more than once, so check return value of fulfil()
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
       * @param layer {object} Event fired (unused)
       * @param args {array} Event parameters (unused)
       */
      onDataListDetailsUpdated: function EntityDataGrid_onDataListDetailsUpdated(layer, args)
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
       * @param layer {object} Event fired (unused)
       * @param args {array} Event parameters (unused)
       */
      onDataGridRefresh: function EntityDataGrid_onDataGridRefresh(layer, args)
      {
         this._updateDataGrid.call(this);
      },

      /**
       * DataList View change filter request event handler
       *
       * @method onChangeFilter
       * @param layer {object} Event fired (unused)
       * @param args {array} Event parameters (new filterId)
       */
      onChangeFilter: function EntityDataGrid_onChangeFilter(layer, args)
      {
         var obj = args[1];
         if ((obj !== null) && (obj.filterId !== null))
         {
            // Should be a filter in the arguments
            var filter = Alfresco.util.cleanBubblingObject(obj),
               strFilter = YAHOO.lang.substitute("{filterId}|{filterData}", filter, function(p_key, p_value, p_meta)
               {
                  return typeof p_value == "undefined" ? "" : window.escape(p_value);
               }),
               aFilters = strFilter.split("|");
            
            // Remove trailing blank entry
            if (aFilters[1].length === 0)
            {
               strFilter = aFilters[0];
            }

            Alfresco.logger.debug("EntityDataGrid_onChangeFilter: ", filter);
            
            var objNav =
            {
               filter: strFilter
            };
            
            // Initial navigation won't fire the History event
            if (obj.datagridFirstTimeNav)
            {
               this._updateDataGrid.call(this,
               {
                  filter: filter,
                  page: this.currentPage
               });
            }
            else
            {
               if (this.options.usePagination)
               {
                  this.currentPage = 1;
                  objNav.page = "1";
               }

               Alfresco.logger.debug("EntityDataGrid_onChangeFilter: objNav = ", objNav);
               YAHOO.util.History.multiNavigate(objNav);
            }
         }
      },

      /**
       * DataGrid View Filter changed event handler
       *
       * @method onFilterChanged
       * @param layer {object} Event fired (unused)
       * @param args {array} Event parameters (new filterId)
       */
      onFilterChanged: function EntityDataGrid_onFilterChanged(layer, args)
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
       * Data Item created event handler
       *
       * @method onDataItemCreated
       * @param layer {object} Event fired
       * @param args {array} Event parameters (depends on event type)
       */
      onDataItemCreated: function EntityDataGrid_onDataItemCreated(layer, args)
      {
         var obj = args[1];
         if (obj && (obj.nodeRef !== null))
         {
            var nodeRef = new Alfresco.util.NodeRef(obj.nodeRef);
            // Reload the node's metadata
            Alfresco.util.Ajax.jsonPost(
            {
               url: Alfresco.constants.PROXY_URI + "slingshot/datalists/item/node/" + nodeRef.uri,
               dataObj: this._buildDataGridParams(),
               successCallback:
               {
                  fn: function EntityDataGrid_onDataItemCreated_refreshSuccess(response)
                  {
                     var item = response.json.item;
                     var fnAfterUpdate = function EntityDataGrid_onDataItemCreated_refreshSuccess_fnAfterUpdate()
                     {
                        var recordFound = this._findRecordByParameter(item.nodeRef, "nodeRef");
                        if (recordFound !== null)
                        {
                           var el = this.widgets.dataTable.getTrEl(recordFound);
                           Alfresco.util.Anim.pulse(el);
                        }
                     };
                     this.afterDataGridUpdate.push(fnAfterUpdate);
                     this.widgets.dataTable.addRow(item);
                  },
                  scope: this
               },
               failureCallback:
               {
                  fn: function EntityDataGrid_onDataItemCreated_refreshFailure(response)
                  {
                     Alfresco.util.PopupManager.displayMessage(
                     {
                        text: this.msg("message.create.refresh.failure")
                     });
                  },
                  scope: this
               }
            });
         }
      },

      /**
       * Data Item updated event handler
       *
       * @method onDataItemUpdated
       * @param layer {object} Event fired
       * @param args {array} Event parameters (depends on event type)
       */
      onDataItemUpdated: function EntityDataGrid_onDataItemUpdated(layer, args)
      {
         var obj = args[1];
         if (obj && (obj.item !== null))
         {
            var recordFound = this._findRecordByParameter(obj.item.nodeRef, "nodeRef");
            if (recordFound !== null)
            {
               this.widgets.dataTable.updateRow(recordFound, obj.item);
               var el = this.widgets.dataTable.getTrEl(recordFound);
               Alfresco.util.Anim.pulse(el);
            }
         }
      },

      /**
       * Data Items deleted event handler
       *
       * @method onDataItemsDeleted
       * @param layer {object} Event fired
       * @param args {array} Event parameters (depends on event type)
       */
      onDataItemsDeleted: function EntityDataGrid_onDataItemsDeleted(layer, args)
      {
         var obj = args[1];
         if (obj && (obj.items !== null))
         {
            var recordFound, el,
               fnCallback = function(record)
               {
                  return function EntityDataGrid_onDataItemsDeleted_anim()
                  {
                     this.widgets.dataTable.deleteRow(record);
                  };
               };
            
            for (var i = 0, ii = obj.items.length; i < ii; i++)
            {
               recordFound = this._findRecordByParameter(obj.items[i].nodeRef, "nodeRef");
               if (recordFound !== null)
               {
                  el = this.widgets.dataTable.getTrEl(recordFound);
                  Alfresco.util.Anim.fadeOut(el,
                  {
                     callback: fnCallback(recordFound),
                     scope: this
                  });
               }
            }
         }
      },


      /**
       * PRIVATE FUNCTIONS
       */

      /**
       * Searches the recordSet for the next item of a record with the given parameter value
       *
       * @method _findNextRecordByParameter
       * @private
       * @param p_value {string} Value to find (to get the previous)
       * @param p_parameter {string} Parameter to look for the value in
       */
      _findNextItemByParameter: function EntityDataGrid__findNextItemByParameter(p_value, p_parameter)
      {
        var recordSet = this.widgets.dataTable.getRecordSet();
        for (var i = 0, j = recordSet.getLength(); i < j; i++)
        {
           if (recordSet.getRecord(i).getData(p_parameter) == p_value)
           {
					if((i+1) != j)
					{
              		return recordSet.getRecord(i + 1).getData();
					}
           }
        }
        return null;
      },
      /**
       * Resets the YUI DataTable errors to our custom messages
       * NOTE: Scope could be YAHOO.widget.DataTable, so can't use "this"
       *
       * @method _setDefaultDataTableErrors
       * @private
       * @param dataTable {object} Instance of the DataTable
       */
      _setDefaultDataTableErrors: function EntityDataGrid__setDefaultDataTableErrors(dataTable)
      {
         var msg = Alfresco.util.message;
         dataTable.set("MSG_EMPTY", msg("message.empty", "beCPG.module.EntityDataGrid"));
         dataTable.set("MSG_ERROR", msg("message.error", "beCPG.module.EntityDataGrid"));
      },

      /**
       * Updates all Data Grid data by calling repository webscript with current list details
       *
       * @method _updateDataGrid
       * @private
       * @param p_obj.filter {object} Optional filter to navigate with
       */
      _updateDataGrid: function EntityDataGrid__updateDataGrid(p_obj)
      {
         p_obj = p_obj || {};
         Alfresco.logger.debug("EntityDataGrid__updateDataGrid: ", p_obj.filter);
         var successFilter = YAHOO.lang.merge({}, p_obj.filter !== undefined ? p_obj.filter : this.currentFilter),
            loadingMessage = null,
            timerShowLoadingMessage = null,
            me = this,
            params =
            {
               filter: successFilter
            };
         
         // Clear the current document list if the data webscript is taking too long
         var fnShowLoadingMessage = function EntityDataGrid_fnShowLoadingMessage()
         {
            Alfresco.logger.debug("EntityDataGrid__uDG_fnShowLoadingMessage: slow data webscript detected.");
            // Check the timer still exists. This is to prevent IE firing the event after we cancelled it. Which is "useful".
            if (timerShowLoadingMessage)
            {
               loadingMessage = Alfresco.util.PopupManager.displayMessage(
               {
                  displayTime: 0,
                  text: '<span class="wait">' + $html(this.msg("message.loading")) + '</span>',
                  noEscape: true
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
         timerShowLoadingMessage = YAHOO.lang.later(this.options.loadingMessageDelay, this, fnShowLoadingMessage);
         
         var destroyLoaderMessage = function EntityDataGrid__uDG_destroyLoaderMessage()
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
                  // Wait and try again later. Scope doesn't get set correctly with "this"
                  YAHOO.lang.later(100, me, destroyLoaderMessage);
               }
            }
         };
         
         var successHandler = function EntityDataGrid__uDG_successHandler(sRequest, oResponse, oPayload)
         {
            destroyLoaderMessage();
            // Updating the DotaGrid may change the item selection
            var fnAfterUpdate = function EntityDataGrid__uDG_sH_fnAfterUpdate()
            {
               Bubbling.fire("selectedFilesChanged");
            };
            this.afterDataGridUpdate.push(fnAfterUpdate);
            
            Alfresco.logger.debug("currentFilter was:", this.currentFilter, "now:", successFilter);
            this.currentFilter = successFilter;
            this.currentPage = p_obj.page || 1;
            Bubbling.fire("filterChanged", successFilter);
            this.widgets.dataTable.onDataReturnInitializeTable.call(this.widgets.dataTable, sRequest, oResponse, oPayload);
         };
         
         var failureHandler = function EntityDataGrid__uDG_failureHandler(sRequest, oResponse)
         {
            destroyLoaderMessage();
            // Clear out deferred functions
            this.afterDataGridUpdate = [];

            if (oResponse.status == 401)
            {
               // Our session has likely timed-out, so refresh to offer the login page
               window.location.reload(true);
            }
            else
            {
               try
               {
                  var response = YAHOO.lang.JSON.parse(oResponse.responseText);
                  this.widgets.dataTable.set("MSG_ERROR", response.message);
                  this.widgets.dataTable.showTableMessage(response.message, YAHOO.widget.DataTable.CLASS_ERROR);
                  if (oResponse.status == 404)
                  {
                     // Site or container not found - deactivate controls
                     Bubbling.fire("deactivateAllControls");
                  }
               }
               catch(e)
               {
                  this._setDefaultDataTableErrors(this.widgets.dataTable);
               }
            }
         };
         
         // Update the DataSource
         var requestParams = this._buildDataGridParams(params);
         Alfresco.logger.debug("DataSource requestParams: ", requestParams);

         // TODO: No-cache? - add to URL retrieved from DataSource
         // "&noCache=" + new Date().getTime();
         
         this.widgets.dataSource.sendRequest(YAHOO.lang.JSON.stringify(requestParams),
         {
            success: successHandler,
            failure: failureHandler,
            scope: this
         });
      },

      /**
       * Build URI parameter string for doclist JSON data webscript
       *
       * @method _buildDataGridParams
       * @param p_obj.filter {string} [Optional] Current filter
       * @return {Object} Request parameters. Can be given directly to Alfresco.util.Ajax, but must be JSON.stringified elsewhere.
       */
      _buildDataGridParams: function EntityDataGrid__buildDataGridParams(p_obj)
      {
         var request =
         {
            fields: this.dataRequestFields
         };
         
         if (p_obj && p_obj.filter)
         {
            request.filter =
            {
               filterId: p_obj.filter.filterId,
               filterData: p_obj.filter.filterData
            };
         }

         return request;
      },

      /**
       * Searches the current recordSet for a record with the given parameter value
       *
       * @method _findRecordByParameter
       * @private
       * @param p_value {string} Value to find
       * @param p_parameter {string} Parameter to look for the value in
       */
      _findRecordByParameter: function EntityDataGrid__findRecordByParameter(p_value, p_parameter)
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
      }
   }, true);
})();