/**
 * Copyright (C) 2005-2014 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */


/**
 * TaskList component.
 *
 * @namespace Alfresco
 * @class Alfresco.component.TaskList
 */
(function()
{
   /**
    * YUI Library aliases
    */
   var Dom = YAHOO.util.Dom,
         Event = YAHOO.util.Event;

   /**
    * Alfresco Slingshot aliases
    */
   var $html = Alfresco.util.encodeHTML,
      $siteURL = Alfresco.util.siteURL;

   
   /**
    * DocumentList constructor.
    *
    * @param htmlId {String} The HTML id of the parent element
    * @return {Alfresco.component.TaskList} The new DocumentList instance
    * @constructor
    */
   Alfresco.component.TaskList = function(htmlId)
   {
      Alfresco.component.TaskList.superclass.constructor.call(this, "Alfresco.component.TaskList", htmlId, ["button", "menu", "container", "datasource", "datatable", "paginator", "json", "history"]);

      /**
       * Decoupled event listeners
       */
      YAHOO.Bubbling.on("filterChanged", this.onFilterChanged, this);
      YAHOO.Bubbling.on("filterSearch", this.onFilterSearch, this);
 

      return this;
   };

   /**
    * Extend from Alfresco.component.Base
    */
   YAHOO.extend(Alfresco.component.TaskList, Alfresco.component.Base);

   /**
    * Augment prototype with Common Workflow actions to reuse createFilterURLParameters
    */
   YAHOO.lang.augmentProto(Alfresco.component.TaskList, Alfresco.action.WorkflowActions);

   /**
    * Augment prototype with main class implementation, ensuring overwrite is enabled
    */
   YAHOO.lang.augmentObject(Alfresco.component.TaskList.prototype,
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
          * Task types not to display
          *
          * @property hiddenTaskTypes
          * @type Array
          * @default []
          */
         hiddenTaskTypes: [],
         
         /**
          * Instruction show to resolve filter id & data to url parameters 
          *
          * @property filterParameters
          * @type Array
          * @default []
          */
         filterParameters: [],

         /**
          * Number of tasks to display at the same time
          *
          * @property maxItems
          * @type int
          * @default 50
          */
         maxItems: 50,
         
  	   /**
          * Column Info Url
          */

         columnsUrl : Alfresco.constants.URL_SERVICECONTEXT + "module/entity-datagrid/config/columns"
      },

	  searchTerm : null,

      /**
       * Fired by YUI when parent element is available for scripting.
       * Initial History Manager event registration
       *
       * @method onReady
       */
      onReady: function DL_onReady()
      {
    	  
    	  
    	  

          // Query the visible columns for this list's item
          // type
          Alfresco.util.Ajax.jsonGet(
          {
              url : this.options.columnsUrl + "?itemType=pjt:project&formId=taskList",
              successCallback :
              {
                  fn : this.onDatalistColumns,
                  scope : this
              }
          });
    	  
         
      },
      
      
      onDatalistColumns : function EntityDataGrid_onDatalistColumns(response)
      {
          this.datalistColumns = response.json.columns;
          this.dataRequestFields = [];
          this.dataResponseFields = [];
         
          var columns = ["bpm_priority", "bpm_status", "bpm_dueDate", "bpm_description","bpm_completionDate"];
          var columnDefs = 
              [
                  { key: "priority", sortable: true,label:"", formatter: this.bind(this.renderCellPriority), width: 16 },
                  { key: "isPooled", sortable: false,label:"", formatter: this.bind(this.renderCellPooled), width: 16 },      
                  { key: "description", sortable: true, label:this.msg("label.description"), formatter: this.bind(this.renderTitleCell) },
                  { key: "type", sortable: true, label:this.msg("label.type"), formatter: this.bind(this.renderTaskTypeCell) },
                  { key: "dueDate", sortable: true, label:this.msg("label.due"), formatter: this.bind(this.renderDueDateCell) },
                  { key: "startDate", sortable: true, label:this.msg("label.started"), formatter: this.bind(this.renderStartDateCell) },
                  { key: "completionDate", sortable: true, label:this.msg("label.ended"), formatter: this.bind(this.renderCompletionDateCell) },
                  { key: "owner", sortable: true, label:this.msg("label.initiator"), formatter: this.bind(this.renderInitiatorCell) }
                 
                  
               ];
          
          
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
        	  
              
              var colDef = {
                          key : fieldLookup,
                          label : column.label == "hidden" ? "" : column.label,
                          hidden : column.label == "hidden" ,
                          sortOptions :
                          {
                              field : column.formsName,
                              sortFunction : beCPG.module.EntityDataRendererHelper.getSortFunction()
                          },
                          formatter : beCPG.module.EntityDataRendererHelper.getCellFormatter(this),
                          sortable : false
                      };
              	
              	if(column.options!=null ){
              		try {
                  		var joptions = YAHOO.lang.JSON.parse(
                  				column.options);
                  		/* Syntax example
                  			{maxAutoWidth:100,resizeable:true,minWidth:150}
                  		*/
                  		if(joptions){
                      		for(var key in joptions) {
                      			colDef[key] = joptions[key];
                      		}
                  		}
              		} catch(e){
              			console.log("ERROR cannot parse options: "+column.options)
              		}
              	}
              	
              	
              	columnDefs.push(colDef);
                columns.push("extra_"+column.name.replace(":", "_"));
              
          }
          
          columnDefs.push({ key: "name", sortable: false, label:"", formatter: this.bind(this.renderCellActions), width: 200 });
          
   
          var url = Alfresco.constants.PROXY_URI + "api/task-instances?authority=" + encodeURIComponent(Alfresco.constants.USERNAME) +
          	   "&exclude=" + this.options.hiddenTaskTypes.join(",")+
                "&properties=" + columns.join(","), me = this;
          
          this.widgets.pagingDataTable = new Alfresco.util.DataTable(
          {
             dataTable:
             {
                container: this.id + "-tasks",
                columnDefinitions:columnDefs,
                config:
                {
                   MSG_EMPTY: this.msg("message.noTasks")
                }
             },
             dataSource:
             {
                url: url,
                defaultFilter:
                {
                   filterId: "workflows.active"
                },
                filterResolver: this.bind(function(filter)
                {
                   // Reuse method form WorkflowActions     
			      var filterParameters = this.createFilterURLParameters(filter, this.options.filterParameters);
                  if(this.searchTerm!=null){
				      filterParameters += "&q="+this.searchTerm;
				   }
	
                   return filterParameters;
                })
             },
             paginator:
             {
                config:
                {
                   containers: [this.id + "-paginator", this.id + "-paginatorBottom"],
                   rowsPerPage: this.options.maxItems
                }
             }
            
             
          });
          
        
          
          this.widgets.pagingDataTable.getDataTable().sortColumn = function(oColumn, sDir) {
 			    if(oColumn && (oColumn instanceof YAHOO.widget.Column)) {
 			        if(!oColumn.sortable) {
 			            Dom.addClass(this.getThEl(oColumn), DT.CLASS_SORTABLE);
 			        }
 			        
 			        // Validate given direction
 			        if(sDir && (sDir !== DT.CLASS_ASC) && (sDir !== DT.CLASS_DESC)) {
 			            sDir = null;
 			        }
 			        
 			        // Get the sort dir
 			        var sSortDir = sDir || this.getColumnSortDir(oColumn);
 			
 			        // Is the Column currently sorted?
 			        var oSortedBy = this.get("sortedBy") || {};
 			        var bSorted = (oSortedBy.key === oColumn.key) ? true : false;
 			
 			        var ok = this.doBeforeSortColumn(oColumn, sSortDir);
 			        if(ok) {
 			            // Server-side sort
 			            if(this.get("dynamicData")) {
 			                // Get current state
 			                var oState = this.getState();
 			                
 			                // Reset record offset, if paginated
 			                if(oState.pagination) {
 			                    oState.pagination.recordOffset = 0;
 			                }
 			                
 			                // Update sortedBy to new values
 			                oState.sortedBy = {
 			                    key: oColumn.key,
 			                    dir: sSortDir
 			                };
 			                
 			                var oSelf = this;
 			                // Get the request for the new state
 			                oState = oState || {pagination:null, sortedBy:null};
 			                var sort = encodeURIComponent((oState.sortedBy) ? oState.sortedBy.key : oSelf.getColumnSet().keys[0].getKey());
 			                var dir = (oState.sortedBy && oState.sortedBy.dir === YAHOO.widget.DataTable.CLASS_DESC) ? "desc" : "asc";
 			        
 			                
 			               var  baseParameters = me.widgets.pagingDataTable.createUrlParameters(),
 			                request = baseParameters+"&sort=" + sort +
 			                        "&dir=" + dir ;
 			
 			                // Purge selections
 			                this.unselectAllRows();
 			                this.unselectAllCells();
 			
 			                // Send request for new data
 			                var callback = {
 			                    success : this.onDataReturnSetRows,
 			                    failure : this.onDataReturnSetRows,
 			                    argument : oState, // Pass along the new state to the callback
 			                    scope : this
 			                };
 			                this._oDataSource.sendRequest(request, callback);            
 			            }
 			         
 			            this.fireEvent("columnSortEvent",{column:oColumn,dir:sSortDir});
 			            return;
 			        }
 			    }
 			};
 			
      },


      
      
      /**
       * Fired when the currently active filter has changed
       *
       * @method onFilterChanged
       * @param layer {string} the event source
       * @param args {object} arguments object
       */
      onFilterChanged: function BaseFilter_onFilterChanged(layer, args)
      {
         var filter = Alfresco.util.cleanBubblingObject(args[1]);
         Dom.get(this.id + "-filterTitle").innerHTML = $html(this.msg("filter." + filter.filterId + (filter.filterData &&  filter.filterId!="search" ? "." + filter.filterData : ""), filter.filterData));
      },
      
      /**
       * Fired when the currently active filter has changed
       *
       * @method onFilterSearch
       * @param layer {string} the event source
       * @param args {object} arguments object
       */
      onFilterSearch: function BaseFilter_onFilterSearch(layer, args)
      {
         var filter = Alfresco.util.cleanBubblingObject(args[1]);
         this.searchTerm = filter.filterData;
         
         this.widgets.pagingDataTable.setPagingState("|");
         this.widgets.pagingDataTable.loadDataTable();
         
      },

      /**
       * DataTable Cell Renderers
       */

      /**
       * Priority & pooled icons custom datacell formatter
       *
       * @method TL_renderCellIcons
       * @param elCell {object}
       * @param oRecord {object}
       * @param oColumn {object}
       * @param oData {object|string}
       */
      renderCellPriority: function renderCellPriority(elCell, oRecord, oColumn, oData)
      {
         var priority = oRecord.getData("properties")["bpm_priority"],
               priorityMap = { "1": "high", "2": "medium", "3": "low" },
               priorityKey = priorityMap[priority + ""];
         elCell.innerHTML = '<img src="' + Alfresco.constants.URL_RESCONTEXT + 'components/images/priority-' + priorityKey + '-16.png" title="' + this.msg("label.priority", this.msg("priority." + priorityKey)) + '"/>';
      },
      
      renderCellPooled: function renderCellPooled(elCell, oRecord, oColumn, oData)
      {
         var pooledTask = oRecord.getData("isPooled");
         var desc = "";
         if (pooledTask)
         {
            desc += '<img src="' + Alfresco.constants.URL_RESCONTEXT + 'components/images/pooled-task-16.png" title="' + this.msg("label.pooledTask") + '"/>';
         }
         elCell.innerHTML = desc;
      },

   
      
      renderTaskTypeCell: function(elCell, oRecord, oColumn, oData)
		{
			var record = oRecord.getData();
			elCell.innerHTML = record.title;
		},
				
	   renderDueDateCell: function(elCell, oRecord, oColumn, oData)
	   {
			var record = oRecord.getData();
			var date = Alfresco.util.fromISO8601(record.properties["bpm_dueDate"]);
			elCell.innerHTML = ( date !== null ? Alfresco.util.formatDate(date, "mediumDate") : "" );
		},
		
		renderInitiatorCell: function(elCell, oRecord, oColumn, oData) {
			 var workflowInstance = oRecord.getData("workflowInstance");
			 if (workflowInstance.initiator) {
				elCell.innerHTML = '<span class="person">' 
				    + Alfresco.util.userProfileLink(workflowInstance.initiator.userName, workflowInstance.initiator.firstName+" "+ workflowInstance.initiator.lastName) + '</span>';
				 }     
		  } ,
		   
		   
		renderCompletionDateCell: function(elCell, oRecord, oColumn, oData)
		 {
			var record = oRecord.getData();
			var date = Alfresco.util.fromISO8601(record.properties["bpm_completionDate"]);
			elCell.innerHTML = ( date !== null ? Alfresco.util.formatDate(date, "mediumDate") : "" );
	   },
			
		
	   renderStartDateCell: function(elCell, oRecord, oColumn, oData)
	   {
		   var workflowInstance = oRecord.getData("workflowInstance");
		   var record = oRecord.getData();
		   var date = Alfresco.util.fromISO8601(workflowInstance.startDate) ;
				elCell.innerHTML = ( date !== null ? Alfresco.util.formatDate(date, "mediumDate") : "" );
		},
		
		renderTitleCell: function(elCell, oRecord, oColumn, oData)
		{
			var data = oRecord.getData();
			var taskId = data.id,
				message = $html(data.properties["bpm_description"]),
				type = $html(data.title),
		        assignee = oRecord.getData("owner"),href = "",
		        status = $html(oRecord.getData("properties")["bpm_status"]),
		        statusTitle = status;
			
			if(status!=null){
				status = status.toUpperCase().replace(/\s/g,"_");
			}

			if (message === type){
				message = this.msg("workflow.no_message");
			}
			
			 // if there is a property label available for the status use that instead
            if (data.propertyLabels && Alfresco.util.isValueSet(data.propertyLabels["bpm_status"], false))
            {
            	statusTitle = data.propertyLabels["bpm_status"];
            }

			if (oRecord.getData('isEditable')){
				href = $siteURL('task-edit?taskId=' + taskId + '&referrer=tasks&myTasksLinkBack=true') + '" class="theme-color-1" title="' + this.msg("link.editTask");
			} else {
				href = $siteURL('task-details?taskId=' + taskId + '&referrer=tasks&myTasksLinkBack=true') + '" class="theme-color-1" title="' + this.msg("link.viewTask");
			}
			var info = '<h3><span title="'+statusTitle+'" class="'+status+'"><a href="' + href + '">' + message + '</a></span></h3>';
			
			 if (!assignee || !assignee.userName)
	         {
	            info += '<div class="unassigned"><span class="theme-bg-color-5 theme-color-5 unassigned-task">' + this.msg("label.unassignedTask") + '</span></div>';
	         }
			
			elCell.innerHTML = info;
		},

      /**
       * Actions custom datacell formatter
       *
       * @method TL_renderCellSelected
       * @param elCell {object}
       * @param oRecord {object}
       * @param oColumn {object}
       * @param oData {object|string}
       */
      renderCellActions: function TL_renderCellActions(elCell, oRecord, oColumn, oData)
      {
         // Create actions using WorkflowAction
         if (oRecord.getData('isEditable'))
         {
            this.createAction(elCell, this.msg("link.editTask"), "task-edit-link", $siteURL('task-edit?taskId=' + oRecord.getData('id') + '&referrer=tasks&myTasksLinkBack=true'));
         }
         this.createAction(elCell, this.msg("link.viewTask"), "task-view-link", $siteURL('task-details?taskId=' + oRecord.getData('id') + '&referrer=tasks&myTasksLinkBack=true'));
         this.createAction(elCell, this.msg("link.viewWorkflow"), "workflow-view-link", $siteURL('workflow-details?workflowId=' + oRecord.getData('workflowInstance').id + '&' + 'taskId=' + oRecord.getData('id') + '&referrer=tasks&myTasksLinkBack=true'));
      // Create actions using WorkflowAction
      	 var instance = oRecord.getData('workflowInstance');
      	 if (instance && instance.isActive) {
      	 	this.createAction(elCell, this.msg("link.viewWorkflowDiagram"), "workflow-view-diagram", function () { this.viewWorkflowDiagram(oRecord); });
      	 }
      
      
      	},

		/**
		 * Called when view workflow diagram button is clicked.
		 * WIll display the workflow's diagram.
		 */
		viewWorkflowDiagram: function(oRecord) {
			var diagramUrl = "api/workflow-instances/" + oRecord.getData('workflowInstance').id + "/diagram";
			Alfresco.Lightbox.show({ src: Alfresco.constants.PROXY_URI + diagramUrl });
		},
		
		 /**
	       * @method createAction
	       * @param label
	       * @param css
	       * @param action
	       * @param oRecord
	       */
	      createAction: function WA_createAction(elCell, label, css, action, oRecord)
	      {
	         var div = document.createElement("div");
	         Dom.addClass(div, css);
	         div.onmouseover = function()
	         {
	            Dom.addClass(this, css + "-over");
	         };
	         div.onmouseout = function()
	         {
	            Dom.removeClass(this, css + "-over");

	         };
	         var a = document.createElement("a");
	         if (YAHOO.lang.isFunction(action))
	         {
	            Event.addListener(a, "click", action, oRecord, this);
	            a.setAttribute("href", "#");
	         }
	         else
	         {
	            a.setAttribute("href", action);
	         }
	         
	         a.setAttribute("title", label);

	         div.appendChild(a);
	         elCell.appendChild(div);
	      }


   }, true);
})();
