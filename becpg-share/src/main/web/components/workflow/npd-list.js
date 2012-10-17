/**
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
 * WorkflowList component.
 *
 * @namespace Alfresco
 * @class beCPG.component.NpdList
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
    * @return {beCPG.component.NpdList} The new DocumentList instance
    * @constructor
    */
   beCPG.component.NpdList = function(htmlId)
   {
      beCPG.component.NpdList.superclass.constructor.call(this, "beCPG.component.NpdList", htmlId, ["button", "menu", "container", "datasource", "datatable", "paginator", "json", "history"]);

      /**
       * Decoupled event listeners
       */
      YAHOO.Bubbling.on("filterChanged", this.onFilterChanged, this);
      YAHOO.Bubbling.on("workflowCancelled", this.onWorkflowCancelled, this);

      return this;
   };

   /**
    * Extend from Alfresco.component.Base
    */
   YAHOO.extend(beCPG.component.NpdList, Alfresco.component.Base);

   /**
    * Augment prototype with Common Workflow actions to reuse createFilterURLParameters
    */
   YAHOO.lang.augmentProto(beCPG.component.NpdList, Alfresco.action.WorkflowActions);

   /**
    * Augment prototype with main class implementation, ensuring overwrite is enabled
    */
   YAHOO.lang.augmentObject(beCPG.component.NpdList.prototype,
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
          * Workflow names not to display
          *
          * @property hiddenWorkflowNames
          * @type Array
          * @default []
          */
         hiddenWorkflowNames: [],

         /**
          * Instruction show to resolve filter id & data to url parameters
          *
          * @property filterParameters
          * @type Array
          * @default []
          */
         filterParameters: [],

         /**
          * Workflow definitions containing the titles dto display in the filter title
          *
          * @property workflowDefinitions
          * @type Array
          * @default []
          */
         workflowDefinitions: [],

         /**
          * Number of workflows to display at the same time
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
    	  
    	
    	  
         var url = Alfresco.constants.PROXY_URI + "api/npd-instances?definitionName=jbpm$npdwf:newProductDevelopmentWF&exclude=" + this.options.hiddenWorkflowNames.join(",");
         this.widgets.alfrescoDataTable = new Alfresco.util.DataTable(
         {
            dataTable:
            {
               container: this.id + "-npds",
               columnDefinitions:
//                 Com	Date Réception Fiche	N° Demande	Date Mise à disposition	Reseau	France / Export	CLIENT	Résumé de l'offre	Etat	Commentaire	Date Dernière Modification	Active	Mois																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																																			

               [
                  { key: "id", label:"" ,sortable: false, formatter: this.bind(this.renderCellIcons), width: 20 },
                  { key: "npdProductName",  label:this.msg("label.npdProductName"),sortable: false, formatter: this.bind(this.renderNpdProductName) },
                  { key: "npdType", label:this.msg("label.npdType"), sortable: false, formatter: this.bind(this.renderNpdType) },                  
                  { key: "npdNumber",  label:this.msg("label.npdNumber"),sortable: false, formatter: this.bind(this.renderNpdNumber) },
//                  { key: "description",  label:this.msg("label.npdDescription"),sortable: false, formatter: this.bind(this.renderTitle) },
                  { key: "npdStatus",  label:this.msg("label.npdStatus"),sortable: false, formatter: this.bind(this.renderNpdStatus) },
                  { key: "activeSteps",  label:this.msg("label.activeSteps"),sortable: false, formatter: this.bind(this.renderActiveSteps) },
              //    { key: "assignedUsers",  label:this.msg("label.assignedUsers"),sortable: false, formatter: this.bind(this.renderAssignedUsers) },
                  { key: "wfOwner",  label:this.msg("label.owner"),sortable: false, formatter: this.bind(this.renderOwner) },
                  { key: "startedDate", label:this.msg("label.started")  ,sortable: false, formatter: this.bind(this.renderStartedDate) },
                  { key: "dueDate", label:this.msg("label.due") , sortable: false, formatter: this.bind(this.renderDueDate) }, 
            //      { key: "npdComments", label:this.msg("label.npdComments") , sortable: false, formatter: this.bind(this.renderComments) }, 
                  { key: "actions", label:this.msg("label.actions"), sortable: false, formatter: this.bind(this.renderCellActions), width: 200 }
               ],
               config:
               {
                  MSG_EMPTY: this.msg("message.noWorkflows")
               }
            },
            dataSource:
            {
               url: url,
               defaultFilter:
               {
                  filterId: "all"
               },
               filterResolver: this.bind(function(filter)
               {
                  // Reuse method from WorkflowActions
                  return this.createFilterURLParameters(filter, this.options.filterParameters);
               })
            },
            paginator:
            {
               config:
               {
                  containers: [this.id + "-paginator"],
                  rowsPerPage: this.options.maxItems
               }
            }
         });
      },

      /**
       * Fired when the currently active filter has changed
       *
       * @method onFilterChanged
       * @param layer {string} the event source
       * @param args {object} arguments object
       */
      onFilterChanged: function NPDL_onFilterChanged(layer, args)
      {
         var filter = Alfresco.util.cleanBubblingObject(args[1]);
         Dom.get(this.id + "-filterTitle").innerHTML = $html(this.msg("filter." + filter.filterId + (filter.filterData ? "." + filter.filterData : ""), filter.filterData));
       
      },

      /**
       * Fired when the currently active filter has changed
       *
       * @method onFilterChanged
       * @param layer {string} the event source
       * @param args {object} arguments object
       */
      onWorkflowCancelled: function NPDL_onFilterChanged(layer, args)
      {
         // Reload data table so the cancelled workflow is removed
         this.widgets.alfrescoDataTable.loadDataTable();
      },

      /**
       * DataTable Cell Renderers
       */

      /**
       * Priority & pooled icons custom datacell formatter
       *
       * @method renderCellIcons
       * @param elCell {object}
       * @param oRecord {object}
       * @param oColumn {object}
       * @param oData {object|string}
       */
      renderCellIcons: function NPDL_renderCellIcons(elCell, oRecord, oColumn, oData)
      {
         var priority = oRecord.getData("priority"),
            priorityMap = { "1": "high", "2": "medium", "3": "low" },
            priorityKey = priorityMap[priority + ""],
            desc = '<img src="' + Alfresco.constants.URL_RESCONTEXT + 'components/images/priority-' + priorityKey + '-16.png" title="' + this.msg("label.priority", this.msg("priority." + priorityKey)) + '"/>';
         
         elCell.innerHTML = desc;
      },
      renderTitle: function NPDL_renderTitle(elCell, oRecord, oColumn, oData)
      {
         var workflow = oRecord.getData();
         var message = workflow.message;
         if (message === null)
         {
            message = this.msg("workflow.no_message");
         }
         elCell.innerHTML = message;
      },
      renderOwner: function NPDL_renderTitle(elCell, oRecord, oColumn, oData)
      {
         var workflow = oRecord.getData();
         elCell.innerHTML = workflow.initiator.firstName+" "+workflow.initiator.lastName;
      },
      renderNpdNumber: function NPDL_renderNpdNumber(elCell, oRecord, oColumn, oData)
      {
    	 var workflow = oRecord.getData();
    	 var npdNumber = workflow.tasks[0].properties.npdwf_npdNumber;
    	 
         elCell.innerHTML = (npdNumber? npdNumber :this.msg("label.none"));
      },
      renderNpdProductName: function NPDL_renderNpdProductName(elCell, oRecord, oColumn, oData)
      {
    	 var workflow = oRecord.getData();
    	 var npdProductName = workflow.tasks[0].properties.npdwf_npdProductName;
         elCell.innerHTML = '<span class="'+this.getAdvancementClass(workflow)+'"><a href="' + $siteURL('workflow-details?workflowId=' + workflow.id + '&referrer=workflows&myWorkflowsLinkBack=true') + '" class="theme-color-1" title="' + this.msg("link.viewWorkflow") + '">' + $html(npdProductName? npdProductName :this.msg("label.none")) + '</a></span>';
      },
      renderNpdStatus: function NPDL_renderNpdStatus(elCell, oRecord, oColumn, oData)
      {
    	 var workflow = oRecord.getData();
    	 var npdStatus = workflow.tasks[0].properties.npdwf_npdStatus;
         elCell.innerHTML = '<div class="'+this.getNpdStatusClass(workflow)+'"></div><span>'+(npdStatus ? npdStatus  :this.msg("label.none"))+'</span>';
      },
      renderActiveSteps: function NPDL_renderSteps(elCell, oRecord, oColumn, oData)
      {
    	 var workflow = oRecord.getData();    	 
    	 var npdSteps = "";
    	 
    	 for(i in workflow.tasks){
    		 
    		 if(workflow.tasks[i].state == "IN_PROGRESS"){
    			 if(npdSteps != "")
    				 npdSteps += "<br/>";
    			 npdSteps += '<span class="'+this.getAdvancementClass(workflow, workflow.tasks[i])+'">'+workflow.tasks[i].title+'</span>';    			 
    		 }
    	 }
    		     	     	 
         elCell.innerHTML = (npdSteps ? npdSteps : this.msg("label.none"));
      },
      renderAssignedUsers: function NPDL_renderAssignedUsers(elCell, oRecord, oColumn, oData)
      {
    	 var workflow = oRecord.getData();
    	 
    	 var npdAsignedUsers = "";
    	 
    	 for(i in workflow.tasks){
    		 
    		 if(workflow.tasks[i].state == "IN_PROGRESS" && workflow.tasks[i].owner != null){
    			 if(npdAsignedUsers != "")
    				 npdAsignedUsers += ", ";
    			 npdAsignedUsers += workflow.tasks[i].owner.firstName + " " + workflow.tasks[i].owner.lastName;    			 
    		 }
    	 }
    		     	     	 
         elCell.innerHTML = (npdAsignedUsers ? npdAsignedUsers : this.msg("label.none"));
      },
      renderNpdType: function NPDL_renderNpdType(elCell, oRecord, oColumn, oData)
      {
    	 var workflow = oRecord.getData();
    	 var npdType = (workflow.tasks[0].properties.npdwf_npdType ? workflow.tasks[0].properties.npdwf_npdType : this.msg("label.none"));
    	 
         elCell.innerHTML = npdType;
      },
      renderComments: function NPDL_renderComments(elCell, oRecord, oColumn, oData)
      {
    	 var workflow = oRecord.getData();
    	 
    	 //var npdComments = workflow.tasks[0].properties.bpm_comment;
    	 var npdComments = null;
    	 
    	 for(i in workflow.tasks){
    		 
    		 if(workflow.tasks[i].name == "npdwf:validate-analysis"){
    			 npdComments = workflow.tasks[i].properties.bpm_comment;
    			 //break; we want the last comment
    		 }
    	 }
    		     	     	 
         elCell.innerHTML = (npdComments ? npdComments : this.msg("label.none"));
      },
      renderDueDate: function NPDL_renderDueDate(elCell, oRecord, oColumn, oData)
      {
         var workflow = oRecord.getData();
         var dueDate = workflow.dueDate ? Alfresco.util.fromISO8601(workflow.dueDate) : null;
         elCell.innerHTML = (dueDate ? Alfresco.util.formatDate(dueDate, "shortDate") : this.msg("label.none")) ;
      },
      renderStartedDate: function NPDL_renderStartedDate(elCell, oRecord, oColumn, oData)
      {
         var workflow = oRecord.getData();
         var startedDate = workflow.startDate ? Alfresco.util.fromISO8601(workflow.startDate) : null;
         elCell.innerHTML = (startedDate ? Alfresco.util.formatDate(startedDate, "shortDate") : this.msg("label.none"));
      },
      renderCellActions: function NPDL_renderCellActions(elCell, oRecord, oColumn, oData)
      {
         // Create actions using WorkflowAction
         this.createAction(elCell, this.msg("link.viewWorkflow"), "npd-view-link", $siteURL('workflow-details?workflowId=' + oRecord.getData('id') + '&referrer=workflows&myWorkflowsLinkBack=true'));
         this.createAction(elCell, this.msg("link.cancelWorkflow"), "npd-cancel-link", function(event, oRecord)
         {
            this.cancelWorkflow(oRecord.getData("id"), oRecord.getData("message"));
            Event.preventDefault(event);
         }, oRecord);
      },
      getAdvancementClass : function NPDL_getAdvancementClass(workflow, task){
        var percent = 0;
        
	     if(task!=null){	
		      percent = this.getTaskAdvancementPercent(task);
	      } else {
	      	 for(i in workflow.tasks){
	 		      percent+=this.getTaskAdvancementPercent( workflow.tasks[i]);
		    	 }
	      
		      percent = percent / workflow.tasks.length;
		      	
	      	 if(workflow.dueDate!=null){
	      		 var completionDate =  this.resetDate(new Date());
		      	 var dueDate =  this.resetDate(Alfresco.util.fromISO8601(workflow.dueDate));
			    		if(completionDate.getTime()==dueDate.getTime()){
			    			 percent= 75;
			    		} else if(completionDate.getTime()>dueDate.getTime()){
			    			 percent= 0;
			    		} 
		      }
	      }
	    
	     if(percent > 80){
	   	  return "advancement-less100";
	     }
	     if(percent > 60){
	   	  return "advancement-less80";
	     }
	     if(percent > 40){
	   	  return "advancement-less60";
	     }
	     if(percent > 20){
	   	  return "advancement-less40";
	     }
      	
        return "advancement-less20";
      },
      
      getTaskAdvancementPercent : function NPDL_getAdvancementClass( task){

      	if(task.bpm_dueDate!=null){
      		var dueDate = Alfresco.util.fromISO8601(task.bpm_dueDate);
      		var completionDate = new Date();
	    		if(task.state == "COMPLETED"){
	    			completionDate = Alfresco.util.fromISO8601(task.bpm_completionDate);
	    		}
	    		this.resetDate(completionDate);
	    		this.resetDate(dueDate);
	    		if(completionDate.getTime()==dueDate.getTime()){
	    			return 50;
	    		}
	    		if(completionDate.getTime()>dueDate.getTime()){
	    			return 0;
	    		}
	    		
      	}
      	return 100;
      },
      resetDate : function NPDL_resetDate(date){
      	date.setHours(0);
      	date.setMinutes(0);
      	date.setSeconds(0);
      	return date;
      },
      
      getNpdStatusClass: function NPDL_getAdvancementClass(workflow){
	    	 var npdStatus = workflow.tasks[0].properties.npdwf_npdStatus;
      	
      	return "npdStatus npdStatus-"+npdStatus.replace(new RegExp('\\s', 'g'),"-").toLowerCase();
      }
      
   }, true);
})();