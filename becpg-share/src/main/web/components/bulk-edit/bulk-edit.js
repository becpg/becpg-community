/**
 * Bulk Edit: BulkEdit component.
 * 
 * @namespace beCPG
 * @class beCPG.component.BulkEdit
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
    * BulkEdit constructor.
    * 
    * @param htmlId {String} The HTML id of the parent element
    * @return {beCPG.component.BulkEdit} The new BulkEdit instance
    * @constructor
    */
   beCPG.component.BulkEdit = function(htmlId)
   {
      beCPG.component.BulkEdit.superclass.constructor.call(this, "beCPG.component.BulkEdit", htmlId, ["button", "container", "datasource", "datatable", "calendar","paginator", "animation", "history"]);

      // Initialise prototype properties
      this.datalistColumns = {};
      this.dataTableColumn= [];
      this.selectedFields = [];
      this.dataRequestFields = [];
      this.dataResponseFields = [];
      this.currentPage = 1;
      this.totalRecords = 0;
      this.currentFilter =
      {
         filterId: "all",
         filterData: ""
      };
      this.selectedItems = {};
      this.afterBulkEditUpdate = [];

      /**
       * Decoupled event listeners
       */

      YAHOO.Bubbling.on("selectedItemsChanged", this.onSelectedItemsChanged, this);
      YAHOO.Bubbling.on("selectedTypeChanged", this.onSelectedTypeChanged, this);
 
      /* Deferred list population until DOM ready */
      this.deferredListPopulation = new Alfresco.util.Deferred(["onReady"],
      {
         fn: this.populateBulkEdit,
         scope: this
      });

      return this;
   };

   /**
    * Extend from Alfresco.component.Base
    */
   YAHOO.extend(beCPG.component.BulkEdit, Alfresco.component.Base);

   /**
    * Augment prototype with main class implementation, ensuring overwrite is enabled
    */
   YAHOO.lang.augmentObject(beCPG.component.BulkEdit.prototype,
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
          * Delay before showing "loading" message for slow data requests
          *
          * @property loadingMessageDelay
          * @type int
          * @default 1000
          */
         loadingMessageDelay: 1000,

         
         /**
          * Advanced Search query - forms data json format based search.
          * @property searchQuery
          * @type string
          * @default null
          */
         searchQuery: null,
        	 
        /**
         * The type of item
         */ 
         itemType: null
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
       * Deferred function calls for after a data grid update
       *
       * @property afterBulkEditUpdate
       * @type array
       */
      afterBulkEditUpdate: null,

      /**
       * Data List columns from Form configuration
       *
       * @param datalistColumns
       * @type Object
       */
      datalistColumns: null,
      
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
      dataRequestFields: null,

      /**
       * Fields returned from the data request
       *
       * @param dataResponseFields
       * @type Object
       */
      dataResponseFields: null,
      
      /**
       * Fields selected to edit
       *
       * @param selectedFields
       * @type Object
       */
      selectedFields: null,
     
      /**
       * The buttons
       */
      buttons : {},

      /**
       * DataTable Cell Renderers
       */

      /**
       * Returns selector custom datacell formatter
       *
       * @method fnRenderCellSelected
       */
      fnRenderCellSelected: function BulkEdit_fnRenderCellSelected()
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
         return function BulkEdit_renderCellSelected(elCell, oRecord, oColumn, oData)
         {
            Dom.setStyle(elCell, "width", oColumn.width + "px");
            Dom.setStyle(elCell.parentNode, "width", oColumn.width + "px");

            elCell.innerHTML = '<input id="checkbox-' + oRecord.getId() + '" type="checkbox" name="fileChecked" value="'+ oData + '"' + (scope.selectedItems[oData] ? ' checked="checked">' : '>');
         };
      },
     /**
      * @method onSelectedItemsChanged
      * React on onSelectedItemsChanged event
      */
      onSelectedItemsChanged : function (){
    	  this.buttons.submit.set("disabled", false);  	  
      },      
      /**
       * @method onSelectedTypeChanged
       * React on onSelectedTypeChanged event
       */
       onSelectedTypeChanged : function (){
    	   this.populateBulkEdit();
    	   this.widgets.showButton.set("disabled", false);
       },   
      
      /**
       * Return data type-specific formatter
       *
       * @method getCellFormatter
       * @return {function} Function to render read-only value
       */
      getCellFormatter: function BulkEdit_getCellFormatter(elCell, oRecord, oColumn, oData)
      {
         var scope = this;
         
         /**
          * Data Type custom formatter
          *
          * @method renderCellDataType
          * @param elCell {object}
          * @param oRecord {object}
          * @param oColumn {object}
          * @param oData {object|string}
          */
         return function BulkEdit_renderCellDataType(elCell, oRecord, oColumn, oData)
         {
            var html = "";
            var value = "";
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
                  oData = oRecord.getData();
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
                        
                      var dataType = datalistColumn.dataType;
                  	  if(dataType==null){
                  		  dataType = datalistColumn.endpointType;
                  	  }

                        switch (dataType.toLowerCase())
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
                     
                           case "subtype":
                              html += '<a href="' + Alfresco.util.siteURL((data.metadata == "container" ? 'folder' : 'document') + '-details?nodeRef=' + data.value) + '">';
                              html += '<img src="' + Alfresco.constants.URL_RESCONTEXT + 'components/images/filetypes/' + Alfresco.util.getFileIcon(data.displayValue, (data.metadata == "container" ? 'cm:folder' : null), 16) + '" width="16" alt="' + $html(data.displayValue) + '" title="' + $html(data.displayValue) + '" />';
                              html += ' ' + $html(data.displayValue) + '</a>'
                              break;

                           default:
                              html += $links($html(data.displayValue));
                              break;
                        }
                        value = data.value;
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
       * Return data type-specific formatter
       *
       * @method getCellFormatter
       * @return {function} Function to render read-only value
       */
      getCellEditor: function BulkEdit_getCellEditor(column)
      {
    	   if("prop_cm_name" == this._buildFormsName(column)
  			  || "prop_bcpg_code" == this._buildFormsName(column)){
    		   return null;
    	   }
    	  var editor = new YAHOO.widget.TextboxCellEditor({disableBtns:true});
    	  var regexp = null;
    	  
    	 
    	  
    	  if(column.constraints!=null){
    		  for(var i in column.constraints){
    			  
    			  switch (column.constraints[i].type){
	    			  case "REGEXP":
	    				  regexp = column.constraints[i].parameters.expressions;
	    			    break;
	    			  case "LIST" :
	    				return  new YAHOO.widget.DropdownCellEditor({dropdownOptions:column.constraints[i].parameters.allowedValues});
	    		      break;
    			  }
    		  }
    		  
    		  
    	  }
    	  
    	  var dataType = column.dataType;
    	  if(dataType==null){
    		  dataType = column.endpointType;
    	  }
    	  
    	  switch (dataType.toLowerCase())
          {
             case "datetime":
             case "date":
          	   	editor =  new YAHOO.widget.DateCellEditor();
                   break;
             case "float":
             case "int": 
             case "long":
            	 editor = new YAHOO.widget.TextboxCellEditor({validator: YAHOO.widget.DataTable.validateNumber,disableBtns:true});
            	 break;
             case "cm:person":
             case "subtype":
             default:
            if(regexp==null){
          	  editor = new YAHOO.widget.TextboxCellEditor({disableBtns:true});
          	} else {
          		//TODO
          	  editor = new YAHOO.widget.TextboxCellEditor({disableBtns:true});
          	}	
                break;
          }
    	  
    
    	  return editor;
      },

      /**
       * Return data type-specific sorter
       *
       * @method getSortFunction
       * @return {function} Function to sort column by
       */
      getSortFunction: function BulkEdit_getSortFunction()
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
         return function BulkEdit_sortFunction(a, b, desc, field)
         {
            var fieldA = a.getData()[field],
               fieldB = b.getData()[field];

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
            
            
            return YAHOO.util.Sort.compare(fieldA, fieldB, desc);
         };
      },

      /**
       * Fired by YUI when parent element is available for scripting
       *
       * @method onReady
       */
      onReady: function BulkEdit_onReady()
      {
         var me = this;

         
         //Select type widget
         
         
         this.widgets.typeSelect =  Alfresco.util.createYUIButton(this, "itemTypeSelect-button", this.onTypeSelect,
         {
                    type: "menu",
                    menu: "itemTypeSelect-menu"
          });

         this.widgets.typeSelect.getMenu().subscribe("click", function (p_sType, p_aArgs)
                 {
                      var menuItem = p_aArgs[1];
                      if (menuItem)
                      {
                          me.widgets.typeSelect.set("label", menuItem.cfg.getProperty("text"));
                      }
                  });
         
         
         // Item Select menu button
         this.widgets.itemSelect = Alfresco.util.createYUIButton(this, "itemSelect-button", this.onItemSelect,
         {
            type: "menu", 
            menu: "itemSelect-menu",
            disabled: true
         });
         
         
         this.widgets.showButton = Alfresco.util.createYUIButton(this, "show-button", this.onBulkEditShow, {
              disabled: true
          });
         
         
         //The submit button
         if (Dom.get(me.id + "-form-submit") !== null)
         {
            this.buttons.submit = Alfresco.util.createYUIButton(this, "form-submit", null,
            {
               type: "submit",
               disabled: true
            });

            // force the generated button to have a name of "-" so it gets ignored in
            // JSON submit. TODO: remove this when JSON submit behaviour is configurable
            Dom.get(this.id + "-form-submit-button").name = "-";
         }
     
         // Assume no list chosen for now
         Dom.removeClass(this.id + "-selectTypeMessage", "hidden");

         this.deferredListPopulation.fulfil("onReady");

         // Finally show the component body here to prevent UI artifacts on YUI button decoration
         Dom.setStyle(this.id + "-body", "visibility", "visible");
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
      _onDataListFailure: function BulkEdit__onDataListFailure(p_response, p_message)
      {
         Alfresco.util.PopupManager.displayPrompt(
         {
            title: p_message.title,
            text: p_message.text,
            modal: true,
            buttons: [
            {
               text: this.msg("button.ok"),
               handler: function BulkEdit__onDataListFailure_OK()
               {
                  this.destroy();
               },
               isDefault: true
            }]
         });
         
      },
      


      /**
       * Retrieves the Data List from the Repository
       *
       * @method populateBulkEdit
       */
      populateBulkEdit: function BulkEdit_populateBulkEdit()
      {
        
         if(this.options.itemType!=null){
	         // Query the visible columns for this list's item type
	         Alfresco.util.Ajax.jsonGet(
	         {
	            url: $combine(Alfresco.constants.URL_SERVICECONTEXT, "components/bulk-edit/config/columns?itemType=" + encodeURIComponent(this.options.itemType)),
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
	      }
      },

      /**
       * Data List column definitions returned from the Repository
       *
       * @method onDatalistColumns
       * @param response {Object} Ajax data structure
       */
      onDatalistColumns: function BulkEdit_onDatalistColumns(response)
      {
	         this.datalistColumns = response.json.data.definition.fields;
	         // Set-up the bulk edit props picker
	         this._setupPropsPicker();
	         // Hide "no list" message
	         Dom.addClass(this.id + "-selectTypeMessage", "hidden"); 
      },

      /**
       * History Manager set-up and event registration
       *
       * @method _setupHistoryManagers
       */
      _setupHistoryManagers: function BulkEdit__setupHistoryManagers()
      {
       
         /**
          * YUI History - page
          */
         var me = this;
  

         if (this.options.usePagination)
         {
         
            this.currentPage = parseInt( this.options.initialPage, 10);

            // YUI Paginator definition
            this.widgets.paginator = new YAHOO.widget.Paginator(
            {
               containers: [this.id + "-paginatorTop", this.id + "-paginatorBottom"],
               rowsPerPage: this.options.pageSize,
               initialPage: this.currentPage,
               template: this.msg("pagination.template"),
               pageReportTemplate: this.msg("pagination.template.page-report"),
               previousPageLinkLabel: this.msg("pagination.previousPageLinkLabel"),
               nextPageLinkLabel: this.msg("pagination.nextPageLinkLabel")
            });
            
            // Display the bottom paginator bar
            Dom.setStyle(this.id + "-bulk-editBarBottom", "display", "block");
         }

       
      },

      /**
       * DataSource set-up and event registration
       *
       * @method _setupDataSource
       * @protected
       * 
       */
      _setupDataSource: function BulkEdit__setupDataSource()
      {
    	 
    	 var fieldsDef = [];
    	  
         for (var i = 0, ii = this.datalistColumns.length; i < ii; i++)
         {
            var column = this.datalistColumns[i],
               columnName = column.name.replace(":", "_"),
               fieldLookup = "itemData."+this._buildFormsName(column)//+".value" TODO;
         
            if(this._isSelectedProp(this._buildFormsName(column))){
            	Alfresco.logger.debug("Select prop "+this._buildFormsName(column));
	            this.dataRequestFields.push(columnName);
	            this.dataResponseFields.push(fieldLookup);
	            fieldsDef.push({
	            	key :  fieldLookup
	            })
	            this.dataTableColumn.push(column);
            }
            this.datalistColumns[fieldLookup] = column;
         }
         
         // DataSource definition
         this.widgets.dataSource = new YAHOO.util.DataSource( 
        		 Alfresco.constants.PROXY_URI_RELATIVE + "/becpg/bulkedit/data?query="+encodeURIComponent(this.options.searchQuery)+"&itemType="+encodeURIComponent(this.options.itemType),
         {
            connMethodPost: true,
            responseType: YAHOO.util.DataSource.TYPE_JSON,
            responseSchema:
            {
               resultsList: "items",
               fields : fieldsDef,
               metaFields:
               {
                  paginationRecordOffset: "startIndex",
                  totalRecords: "totalRecords"
               }
            }
         });
         this.widgets.dataSource.connMgr.setDefaultPostHeader(Alfresco.util.Ajax.JSON);

      },
      /**
       * Check if the field is selected
       */
      _isSelectedProp : function BulkEdit__isSelectedProp(propName){
    	  
    	  if("prop_cm_name" == propName
    			  || "prop_bcpg_code" == propName){
    		  return true;
    	  }
    	  for(var i in this.selectedFields){
    		  if(propName == this.selectedFields[i].value){
    			  if(this.selectedFields[i].checked){
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
      _setupPropsPicker: function BulkEdit__setupDataTable(columns)
      {
    	  var containerEl = Dom.get(this.id+"-itemProps-container");
    	  if(containerEl!=null){
    		 containerEl.innerHTML="<ul>";
    		 var inc = 0;
	         var colCount =0;
	         for (var i = 0, ii = this.datalistColumns.length; i < ii; i++)
	         {
	     
	             var column = this.datalistColumns[i]
	         
	        	var propName = this._buildFormsName(column);
	        	var propLabel = column.label;
	        	 if(!(column.protectedField || column.disabled || "prop_cm_name" == propName
          			  || "prop_bcpg_code" == propName)){
	           	  
		        	var className = "";
		        	if(colCount<Math.floor(inc/5)){
		        		className="reset ";
		        	}
		        	colCount = Math.floor(inc/5);
		        	className+="column-"+colCount;
		        	
		        	containerEl.innerHTML += '<li class="'+className+'"><input id="propSelected-' + i + '" type="checkbox" name="propChecked" value="'+ propName + '" /><label for="propSelected-' + i + '" >'+propLabel+'</label></li>'
		        	inc++;
	        	 } 
	          }
	         containerEl.innerHTML+="</ul>";
	         
	    	 this.selectedFields = Selector.query('input[type="checkbox"]', containerEl);
	      }
         
      },
      
      /**
       * DataTable set-up and event registration
       *
       * @method _setupDataTable
       * @protected
       */
      _setupDataTable: function BulkEdit__setupDataTable(columns)
      {
         // YUI DataTable column definitions
         var columnDefinitions =
         [
            { key: "nodeRef", label: "", sortable: false, formatter: this.fnRenderCellSelected(), width: 16 }
         ];
         
         var column;
         for (var i = 0, ii = this.dataTableColumn.length; i < ii; i++)
         {
            column = this.dataTableColumn[i];
            if(this._isSelectedProp(this._buildFormsName(column))){
        
                columnDefinitions.push({
           	        key:  this.dataResponseFields[i],
           	        label: column.label,
           	        
           	        sortable: true,
           	        sortOptions:
           	               {
           	                  field: this.dataResponseFields[i],
           	                  sortFunction: this.getSortFunction()
           	               },

           	         formatter: this.getCellFormatter(column),
           	         editor :  this.getCellEditor(column)
           	            });
            }
         }
      
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
         this.widgets.dataTable.handleDataReturnPayload = function BulkEdit_handleDataReturnPayload(oRequest, oResponse, oPayload)
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
         this.widgets.dataTable.doBeforeLoadData = function BulkEdit_doBeforeLoadData(sRequest, oResponse, oPayload)
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

         // Override default function so the "Loading..." message is suppressed
         this.widgets.dataTable.doBeforeSortColumn = function BulkEdit_doBeforeSortColumn(oColumn, sSortDir)
         {
            return true;
         }

         // File checked handler
         this.widgets.dataTable.subscribe("checkboxClickEvent", function(e)
         { 
            var id = e.target.value; 
            this.selectedItems[id] = e.target.checked;
            Bubbling.fire("selectedItemsChanged");
         }, this, true);


         this.widgets.dataTable.subscribe("cellMouseoverEvent",function(oArgs) { 
                    var elCell = oArgs.target; 
                 if(YAHOO.util.Dom.hasClass(elCell, "yui-dt-editable")) { 
        	              this.highlightCell(elCell); 
        	            } 
             } ); 
         this.widgets.dataTable.subscribe("cellMouseoutEvent",   this.widgets.dataTable.onEventUnhighlightCell); 
         this.widgets.dataTable.subscribe("cellClickEvent",   this.widgets.dataTable.onEventShowCellEditor); 
         
         // To save onEventSaveCellEditor

      },

      /**
       * Multi-item select button click handler
       *
       * @method onItemSelect
       * @param sType {string} Event type, e.g. "click"
       * @param aArgs {array} Arguments array, [0] = DomEvent, [1] = EventTarget
       * @param p_obj {object} Object passed back from subscribe method
       */
      onItemSelect: function BulkEdit_onItemSelect(sType, aArgs, p_obj)
      {
         var domEvent = aArgs[0],
            eventTarget = aArgs[1];
  
         // Select based upon the className of the clicked item
         
         this.selectItems(Alfresco.util.findEventClass(eventTarget));
         Event.preventDefault(domEvent);
      },
      /**
       * Multi-type select button click handler
       *
       * @method onTypeSelect
       * @param sType {string} Event type, e.g. "click"
       * @param aArgs {array} Arguments array, [0] = DomEvent, [1] = EventTarget
       * @param p_obj {object} Object passed back from subscribe method
       */
      onTypeSelect: function BulkEdit_onItemTypeSelect(sType, aArgs, p_obj)
      {
         var domEvent = aArgs[0],
            eventTarget = aArgs[1];
  
         // Select based upon the className of the clicked item
         
         this.options.itemType = Alfresco.util.findEventClass(eventTarget);
   	  
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
      getSelectedItems: function BulkEdit_getSelectedItems()
      {
         var items = [],
            recordSet = this.widgets.dataTable.getRecordSet(),
            aPageRecords = this.widgets.paginator.getPageRecords(),
            startRecord = aPageRecords[0],
            endRecord = aPageRecords[1],
            record;
         
         for (var i = startRecord; i <= endRecord; i++)
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
      selectItems: function BulkEdit_selectItems(p_selectType)
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
       * BulkEdit show Required event handler
       *
       * @method onBulkEditShow
       * @param args {array} Event parameters (unused)
       */
      onBulkEditShow: function BulkEdit_onBulkEditShow(args)
      {
    	 this.dataRequestFields = [];
         this.dataResponseFields = [];
         this.dataTableColumn= [];

    	  
    	 // Set-up YUI History Managers and Paginator
	     this._setupHistoryManagers();
	    // DataSource set-up and event registration
	     this._setupDataSource();
	    // DataTable set-up and event registration
	     this._setupDataTable();
	        
	     // Enable item select menu
	     this.widgets.itemSelect.set("disabled", false);
         this._updateBulkEdit.call(this);
      },

     


      /**
       * PRIVATE FUNCTIONS
       */

      /**
       * Resets the YUI DataTable errors to our custom messages
       * NOTE: Scope could be YAHOO.widget.DataTable, so can't use "this"
       *
       * @method _setDefaultDataTableErrors
       * @private
       * @param dataTable {object} Instance of the DataTable
       */
      _setDefaultDataTableErrors: function BulkEdit__setDefaultDataTableErrors(dataTable)
      {
         var msg = Alfresco.util.message;
         dataTable.set("MSG_EMPTY", msg("message.empty", "beCPG.component.BulkEdit"));
         dataTable.set("MSG_ERROR", msg("message.error", "beCPG.component.BulkEdit"));
      },

      /**
       * Updates all Data Grid data by calling repository webscript with current list details
       *
       * @method _updateBulkEdit
       * @private
       * @param p_obj.filter {object} Optional filter to navigate with
       */
      _updateBulkEdit: function BulkEdit__updateBulkEdit(p_obj)
      {
         p_obj = p_obj || {};
         Alfresco.logger.debug("BulkEdit__updateBulkEdit: ", p_obj.filter);
         var successFilter = YAHOO.lang.merge({}, p_obj.filter !== undefined ? p_obj.filter : this.currentFilter),
            loadingMessage = null,
            timerShowLoadingMessage = null,
            me = this,
            params =
            {
               filter: successFilter
            };
         
         // Clear the current document list if the data webscript is taking too long
         var fnShowLoadingMessage = function BulkEdit_fnShowLoadingMessage()
         {
            Alfresco.logger.debug("BulkEdit__uDG_fnShowLoadingMessage: slow data webscript detected.");
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
         
         // Slow data webscript message
         this.loadingMessageShowing = false;
         timerShowLoadingMessage = YAHOO.lang.later(this.options.loadingMessageDelay, this, fnShowLoadingMessage);
         
         var destroyLoaderMessage = function BulkEdit__uDG_destroyLoaderMessage()
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
         
         var successHandler = function BulkEdit__uDG_successHandler(sRequest, oResponse, oPayload)
         {
            destroyLoaderMessage();
        
            Alfresco.logger.debug("currentFilter was:", this.currentFilter, "now:", successFilter);
            this.currentFilter = successFilter;
            this.currentPage = p_obj.page || 1;
            this.widgets.dataTable.onDataReturnInitializeTable.call(this.widgets.dataTable, sRequest, oResponse, oPayload);
         };
         
         var failureHandler = function BulkEdit__uDG_failureHandler(sRequest, oResponse)
         {
            destroyLoaderMessage();
            // Clear out deferred functions
            this.afterBulkEditUpdate = [];

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
         var requestParams = this._buildBulkEditParams(params);
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
       * @method this._buildBulkEditParams
       * @param p_obj.filter {string} [Optional] Current filter
       * @return {Object} Request parameters. Can be given directly to Alfresco.util.Ajax, but must be JSON.stringified elsewhere.
       */
      _buildBulkEditParams: function BulkEdit__buildBulkEditParams(p_obj)
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
       * Build formsName parameter
       */
      _buildFormsName : function BulkEdit__buildFormsName(col){
    	  var formsName = ""; 
    	  if(col.type  == "association" ) {
    		  formsName="assoc_";
    		  
    	  }else {
    		  formsName="prop_";
    	  }
    	  formsName+=col.name.replace(/:/g, "_");
    	  return formsName;
    	  
      }

    
   }, true);
})();