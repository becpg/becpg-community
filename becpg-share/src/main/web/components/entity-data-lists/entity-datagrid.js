
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
   beCPG.component.EntityDataGrid = function(htmlId)
   {
		return beCPG.component.EntityDataGrid.superclass.constructor.call(this, htmlId);
		
   }     
   
   /**
    * Extend from Alfresco.component.DataGrid
    */
   YAHOO.extend(beCPG.component.EntityDataGrid, Alfresco.component.DataGrid);

   /**
    * Augment prototype with DataListActions module, ensuring overwrite is enabled
    */
   YAHOO.lang.augmentProto(beCPG.component.EntityDataGrid,Alfresco.service.DataListActions, true);

   /**
    * Augment prototype with main class implementation, ensuring overwrite is enabled
    */
   YAHOO.lang.augmentObject(beCPG.component.EntityDataGrid.prototype,
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
       * ACTIONS WHICH ARE LOCAL TO THE DATAGRID COMPONENT
       */

      /**
       * Edit Data Item pop-up
       *
       * @method onActionEdit
       * @param item {object} Object literal representing one data item
       */
      onActionEdit: function DataGrid_onActionEdit(item)
      {
         var scope = this;
         
         // Intercept before dialog show
         var doBeforeDialogShow = function DataGrid_onActionEdit_doBeforeDialogShow(p_form, p_dialog)
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
               fn: function DataGrid_onActionEdit_success(response)
               {
                  // Reload the node's metadata
                  Alfresco.util.Ajax.jsonPost(
                  {
                     url: Alfresco.constants.PROXY_URI + "slingshot/datalists/item/node/" + new Alfresco.util.NodeRef(item.nodeRef).uri,
                     dataObj: this._buildDataGridParams(),
                     successCallback:
                     {
                        fn: function DataGrid_onActionEdit_refreshSuccess(response)
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
                        fn: function DataGrid_onActionEdit_refreshFailure(response)
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
               fn: function DataGrid_onActionEdit_failure(response)
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
       * Return data type-specific formatter
       *
       * @method getCellFormatter
       * @return {function} Function to render read-only value
       */
      getCellFormatter: function DataGrid_getCellFormatter()
      {
         var scope = this;
         var booleanValueTrue = this.msg("data.boolean.true");
         var booleanValueFalse = this.msg("data.boolean.false");
         var reqTypeForbidden = this.msg("data.reqType.Forbidden");
         var reqTypeTolerated = this.msg("data.reqType.Tolerated");
         var reqTypeInfo = this.msg("data.reqType.Info");
         
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
         return function DataGrid_renderCellDataType(elCell, oRecord, oColumn, oData)
         {
            var html = "";

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
                              html += '<a href="' + Alfresco.util.siteURL((data.metadata == "container" ? 'folder' : 'document') + '-details?nodeRef=' + data.value) + '">';
                              html += '<img src="' + Alfresco.constants.URL_RESCONTEXT + 'components/images/filetypes/' + Alfresco.util.getFileIcon(data.displayValue, (data.metadata == "container" ? 'cm:folder' : null), 16) + '" width="16" alt="' + $html(data.displayValue) + '" title="' + $html(data.displayValue) + '" />';
                              html += ' ' + $html(data.displayValue) + '</a>'
                              break;
                            //beCPG : link and icons.
                           case "bcpg:product":
                        	   if(datalistColumn.name == "bcpg:compoListProduct")
								{
                        		   var padding = 10 + oRecord.getData("itemData")["prop_bcpg_depthLevel"].value * 10;
                        		   html += '<span class="' + data.metadata + '" style="padding-left:' + padding + 'px;"><a href="' + Alfresco.util.siteURL('document-details?nodeRef=' + data.value) + '">' + $html(data.displayValue) + '</a></span>';                        	   
								}
                        	   	else if(datalistColumn.name == "bcpg:packagingListProduct")
								{
                        		   html += '<span class="' + data.metadata + '"><a href="' + Alfresco.util.siteURL('document-details?nodeRef=' + data.value) + '">' + $html(data.displayValue) + '</a></span>';                        	   
								}
								else
								{
									html += '<a href="' + Alfresco.util.siteURL('document-details?nodeRef=' + data.value) + '">' + $html(data.displayValue) + '</a>';
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
                        	   if(datalistColumn.name == "bcpg:rclReqType" || datalistColumn.name == "bcpg:filReqType")
                    		   {
                        		   if(data.displayValue == reqTypeForbidden)
                        		   {
                        			   html += '<span class="reqTypeForbidden">' + $html(data.displayValue) + '</span>';
                        		   }
                        		   else if(data.displayValue == reqTypeTolerated)
                        		   {
                        			   html += '<span class="reqTypeTolerated">' + $html(data.displayValue) + '</span>';
                        		   }
                        		   else if(data.displayValue == reqTypeInfo)
                        		   {
                        			   html += '<span class="reqTypeInfo">' + $html(data.displayValue) + '</span>';
                        		   }                        		   
                        		   else
                    			   {
                        			   html += $html(data.displayValue);
                    			   }
                    		   }
                        	   else if(datalistColumn.name == "qa:sdlControlPoint" || datalistColumn.name == "qa:slControlPoint")
                        	   {                        		   
                        		   html += '<span class="sample"><a href="' + Alfresco.util.siteURL('document-details?nodeRef=' + data.value) + '">' + $html(data.displayValue) + '</a></span>';
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
       * Renders Data List metadata, i.e. title and description
       *
       * @method renderDataListMeta
       */
      renderDataListMeta: function DataGrid_renderDataListMeta()
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
      populateDataGrid: function DataGrid_populateDataGrid()
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
      _findNextItemByParameter: function DataGrid__findNextItemByParameter(p_value, p_parameter)
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
       * DataSource set-up and event registration
		 * beCPG : manage WUsed
       *
       * @method _setupDataSource
       * @protected
       */
      _setupDataSource: function DataGrid__setupDataSource()
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
         
         // DataSource definition
         //beCPG - PQU : manage WUsed 
		if(this.datalistMeta.name == "WUsed")
		{
			this.widgets.dataSource = new YAHOO.util.DataSource(Alfresco.constants.PROXY_URI + "becpg/entity/wused/node/" + this.options.entityNodeRef.replace(":/", ""),
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
		}
		else
		{
	      this.widgets.dataSource = new YAHOO.util.DataSource(Alfresco.constants.PROXY_URI + "slingshot/datalists/data/node/" + listNodeRef.uri+"?entityNodeRef="+this.options.entityNodeRef,
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
		}

         this.widgets.dataSource.connMgr.setDefaultPostHeader(Alfresco.util.Ajax.JSON);

         // Intercept data returned from data webscript to extract custom metadata
         this.widgets.dataSource.doBeforeCallback = function DataGrid_doBeforeCallback(oRequest, oFullResponse, oParsedResponse)
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
      }

   }, true);
})();