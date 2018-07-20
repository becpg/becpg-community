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
         maxItems: 50
      },

      /**
       * Fired by YUI when parent element is available for scripting.
       * Initial History Manager event registration
       *
       * @method onReady
       */
      onReady: function DL_onReady()
      {
         var url = Alfresco.constants.PROXY_URI + "api/task-instances?authority=" + encodeURIComponent(Alfresco.constants.USERNAME) +
         	   "&exclude=" + this.options.hiddenTaskTypes.join(",")+
               "&properties=" + ["bpm_priority", "bpm_status", "bpm_dueDate", "bpm_description"].join(","), me = this;
         
         this.widgets.pagingDataTable = new Alfresco.util.DataTable(
         {
            dataTable:
            {
               container: this.id + "-tasks",
               columnDefinitions:
               [
                  { key: "id", sortable: true,label:"", formatter: this.bind(this.renderCellIcons), width: 40 },
                  { key: "title", sortable: true, label:this.msg("label.description"), formatter: this.bind(this.renderTitleCell) },
                  { key: "path", sortable: true, label:this.msg("label.due"), formatter: this.bind(this.renderDueDateCell) },
                  { key: "state", sortable: true, label:this.msg("label.started"), formatter: this.bind(this.renderStartDateCell) },
                  { key: "description", sortable: true, label:this.msg("label.initiator"), formatter: this.bind(this.renderInitiatorCell) },
                  { key: "name", sortable: false, label:"", formatter: this.bind(this.renderCellActions), width: 200 }
               ],
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
                  return this.createFilterURLParameters(filter, this.options.filterParameters);
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
       * @method onFilterChanged
       * @param layer {string} the event source
       * @param args {object} arguments object
       */
      onFilterSearch: function BaseFilter_onFilterChanged(layer, args)
      {
         var filter = Alfresco.util.cleanBubblingObject(args[1]);
         this.widgets.pagingDataTable.loadDataTable("q="+filter.filterData);
         
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
      renderCellIcons: function TL_renderCellIcons(elCell, oRecord, oColumn, oData)
      {
         var priority = oRecord.getData("properties")["bpm_priority"],
               priorityMap = { "1": "high", "2": "medium", "3": "low" },
               priorityKey = priorityMap[priority + ""],
               pooledTask = oRecord.getData("isPooled");
         var desc = '<img src="' + Alfresco.constants.URL_RESCONTEXT + 'components/images/priority-' + priorityKey + '-16.png" title="' + this.msg("label.priority", this.msg("priority." + priorityKey)) + '"/>';
         if (pooledTask)
         {
            desc += '&nbsp;<img src="' + Alfresco.constants.URL_RESCONTEXT + 'components/images/pooled-task-16.png" title="' + this.msg("label.pooledTask") + '"/>';
         }
         elCell.innerHTML = desc;
      },

      /**
       * Task info custom datacell formatter
       *
       * @method TL_renderCellTaskInfo
       * @param elCell {object}
       * @param oRecord {object}
       * @param oColumn {object}
       * @param oData {object|string}
       */
      renderCellTaskInfo: function TL_renderCellTaskInfo(elCell, oRecord, oColumn, oData)
      {
         var taskId = oRecord.getData("id"),
               message = $html(oRecord.getData("properties")["bpm_description"]),
               dueDateStr = oRecord.getData("properties")["bpm_dueDate"],
               dueDate = dueDateStr ? Alfresco.util.fromISO8601(dueDateStr) : null,
               workflowInstance = oRecord.getData("workflowInstance"),
               startedDate = workflowInstance.startDate ? Alfresco.util.fromISO8601(workflowInstance.startDate) : null,
               type = $html(oRecord.getData("title")),
               status = $html(oRecord.getData("properties")["bpm_status"]),
               assignee = oRecord.getData("owner"),
               description = $html(oRecord.getData("description")),
               initiator;
         // initiator may not be present, see MNT-11622
         if (workflowInstance.initiator)
         {
            initiator = $html(workflowInstance.initiator.firstName);
         }
               
         // if there is a property label available for the status use that instead
         var data = oRecord.getData();
         if (data.propertyLabels && Alfresco.util.isValueSet(data.propertyLabels["bpm_status"], false))
         {
            status = data.propertyLabels["bpm_status"];
         }
               
         // if message is the same as the task type show the <no message> label
         if (message == type)
         {
            message = this.msg("workflow.no_message");
         }
               
         var href;
         if(oRecord.getData('isEditable'))
         {
            href = $siteURL('task-edit?taskId=' + taskId + '&referrer=tasks&myTasksLinkBack=true') + '" class="theme-color-1" title="' + this.msg("link.editTask");
         }
         else
         {
            href = $siteURL('task-details?taskId=' + taskId + '&referrer=tasks&myTasksLinkBack=true') + '" class="theme-color-1" title="' + this.msg("link.viewTask");
         }

         var info = '<h3><a href="' + href + '">' + message + '</a></h3>';
         info += '<div class="due"><label>' + this.msg("label.due") + ':</label><span>' + (dueDate ? Alfresco.util.formatDate(dueDate, "longDate") : this.msg("label.none")) + '</span></div>';
         info += '<div class="started"><label>' + this.msg("label.started") + ':</label><span>' + (startedDate ? Alfresco.util.formatDate(startedDate, "longDate") : this.msg("label.none")) + '</span></div>';
         if (!workflowInstance.isActive)
         {
            var endedDate = workflowInstance.endDate ? Alfresco.util.fromISO8601(workflowInstance.endDate) : null;
            info += '<div class=ended"><label>' + this.msg("label.ended") + ':</label><span>' + (endedDate ? Alfresco.util.formatDate(endedDate, "longDate") : this.msg("label.none")) + '</span></div>';
         }
         info += '<div class="status"><label>' + this.msg("label.status") + ':</label><span>' + status + '</span></div>';
         info += '<div class="type"><label>' + this.msg("label.type", type) + ':</label><span>' + type + '</span></div>';
         info += '<div class="description"><label>' + this.msg("label.description") + ':</label><span>' + description + '</span></div>';
         if (initiator)
         {
            info += '<div class="initiator"><label>' + this.msg("label.initiator") + ':</label><span>' + initiator + '</span></div>';
         }
         if (!assignee || !assignee.userName)
         {
            info += '<div class="unassigned"><span class="theme-bg-color-5 theme-color-5 unassigned-task">' + this.msg("label.unassignedTask") + '</span></div>';
         }
         elCell.innerHTML = info;
         this.widgets.alfrescoDataTable.loadDataTable("q="+encodeURIComponent(this.searchTerm));
      },
     
      renderInitiatorCell: function(elCell, oRecord, oColumn, oData) {
    	  var workflowInstance = oRecord.getData("workflowInstance");
		    // initiator may not be present, see MNT-11622
		    if (workflowInstance.initiator)
		    {
		       elCell.innerHTML = '<span class="person">' 
		    	   + Alfresco.util.userProfileLink(workflowInstance.initiator.userName, workflowInstance.initiator.firstName+" "+ workflowInstance.initiator.lastName) + '</span>';
		       
		    }     
      } ,
      
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
					type = $html(data.title);

			if (message === type)
				message = this.msg("workflow.no_message");

			var href;
			if (oRecord.getData('isEditable'))
				href = $siteURL('task-edit?taskId=' + taskId + '&referrer=tasks&myTasksLinkBack=true') + '" class="theme-color-1" title="' + this.msg("link.editTask");
			else
				href = $siteURL('task-details?taskId=' + taskId + '&referrer=tasks&myTasksLinkBack=true') + '" class="theme-color-1" title="' + this.msg("link.viewTask");

			var info = '<h3><a href="' + href + '">' + message + '</a></h3>';
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
