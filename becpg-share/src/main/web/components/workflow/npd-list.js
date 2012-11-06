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

var g; // gantt var

(function() {
	/**
	 * YUI Library aliases
	 */
	var Dom = YAHOO.util.Dom, Event = YAHOO.util.Event;

	/**
	 * Alfresco Slingshot aliases
	 */
	var $html = Alfresco.util.encodeHTML, $siteURL = Alfresco.util.siteURL;

	/**
	 * DocumentList constructor.
	 * 
	 * @param htmlId
	 *           {String} The HTML id of the parent element
	 * @return {beCPG.component.NpdList} The new DocumentList instance
	 * @constructor
	 */
	beCPG.component.NpdList = function(htmlId, view) {

		this.view = view;

		beCPG.component.NpdList.superclass.constructor.call(this, "beCPG.component.NpdList", htmlId, [ "button", "menu",
		      "container", "datasource", "datatable", "paginator", "json", "history" ]);

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
	 * Augment prototype with Common Workflow actions to reuse
	 * createFilterURLParameters
	 */
	YAHOO.lang.augmentProto(beCPG.component.NpdList, Alfresco.action.WorkflowActions);

	/**
	 * Augment prototype with main class implementation, ensuring overwrite is
	 * enabled
	 */
	YAHOO.lang.augmentObject(beCPG.component.NpdList.prototype,
	      {
	         view : "dataTable",
	         /**
	          * State definition
	          */
	         statesDef : [],
	         /**
				 * Object container for initialization options
				 * 
				 * @property options
				 * @type object
				 */
	         options : {
	            /**
					 * Workflow names not to display
					 * 
					 * @property hiddenWorkflowNames
					 * @type Array
					 * @default []
					 */
	            hiddenWorkflowNames : [],

	            /**
					 * Instruction show to resolve filter id & data to url parameters
					 * 
					 * @property filterParameters
					 * @type Array
					 * @default []
					 */
	            filterParameters : [],

	            /**
					 * Workflow definitions containing the titles dto display in the
					 * filter title
					 * 
					 * @property workflowDefinitions
					 * @type Array
					 * @default []
					 */
	            workflowDefinitions : [],

	            /**
					 * Number of workflows to display at the same time
					 * 
					 * @property maxItems
					 * @type int
					 * @default 50
					 */
	            maxItems : 50
	         },

	         /**
				 * Fired by YUI when parent element is available for scripting.
				 * Initial History Manager event registration
				 * 
				 * @method onReady
				 */
	         onReady : function DL_onReady() {

		         var url = Alfresco.constants.PROXY_URI
		               + "api/npd-instances?definitionName=jbpm$npdwf:newProductDevelopmentWF&exclude="
		               + this.options.hiddenWorkflowNames.join(",");
		         
		         this.loadStateDefs();
		         
		         if (this.view == "gantt") {

			         Alfresco.util.Ajax.request({
			            url : url,
			            successCallback : {
			               fn : function(response) {

				               var data = response.json.data;
				               g = new JSGantt.GanttChart('g', Dom.get(this.id + "-npds"), 'day');
				               g.setDateInputFormat("shortDate");
				               g.setDateDisplayFormat("shortDate");
				               g.setCaptionType('Resource');
				       
				               // TaskItem(pID, pName, pStart, pEnd, pColor, pLink,
									// pMile, pRes, pComp, pGroup, pParent, pOpen,
									// pDepend)
				               for ( var i = 0; i < data.length; i++) {
					               var workflow = data[i];
					              
					               var title = 
					               '<span class="'
					               + this.getAdvancementClass(workflow)
					               + '">'+workflow.message+'</span>';

					               var initiator = workflow.initiator.firstName + " " + workflow.initiator.lastName;
					               var workflowUrl = $siteURL('workflow-details?workflowId=' + workflow.id
					                     + '&referrer=workflows&myWorkflowsLinkBack=true');
					               g.AddTaskItem(new JSGantt.TaskItem(i,title , workflow.startDate,
					                     workflow.dueDate, 'FFBC00', workflowUrl, 0, initiator, 20, 1, 1, 1));
					              var precTaskId =0;
					               
					               for (j in workflow.tasks) {
						               var task = workflow.tasks[j];
						               var taskOwner = "";
						               var percent = 35;
						               var taskId = i*100+j;
						               
						               if (task.owner) {
							               taskOwner = task.owner.firstName + " " + task.owner.lastName;
						               }
						               
						               var dueDate = task.properties.bpm_dueDate;
						               
						               if (task.state == "COMPLETED") {
						               	dueDate = task.properties.bpm_completionDate;
						               	percent = 100;
						               }
 
						               if(precTaskId>0){
						               	 g.AddTaskItem(new JSGantt.TaskItem(taskId, '<span class="' + this.getAdvancementClass(workflow, task) + '">'
										               +task.title + '</span>', task.properties.cm_created, dueDate,
								               		this.getNpdStatusColor(task), null, 0, taskOwner, percent, 0, i, 1, precTaskId));
						               } else {
							               g.AddTaskItem(new JSGantt.TaskItem(taskId, '<span class="' + this.getAdvancementClass(workflow, task) + '">'
									               +task.title + '</span>', task.properties.cm_created, dueDate,
							               		this.getNpdStatusColor(task), null, j==0?1:0, taskOwner, percent, 0, i, 1));
						               }
						               
						               precTaskId = taskId;
					               }

				               }

				               g.Draw();
				               g.DrawDependencies();

			               },
			               scope : this
			            },
			            failureCallback : {
			               fn : function() {
				               // DO nothing
			               },
			               scope : this
			            }
			         });

		         } else {
			         this.widgets.alfrescoDataTable = new beCPG.util.GroupedDataTable({
			            dataTable : {
			               container : this.id + "-npds",
			               columnDefinitions :
			               // Com Date Réception Fiche N° Demande Date Mise à
			               // disposition Reseau France / Export CLIENT Résumé de
			               // l'offre Etat Commentaire Date Dernière Modification
			               // Active Mois

			               [ {
			                  key : "id",
			                  label : "",
			                  sortable : false,
			                  formatter : this.bind(this.renderCellIcons),
			                  width : 20
			               }, 
			               {
			                  key : "avc",
			                  label : "",
			                  sortable : false,
			                  formatter : this.bind(this.renderAvancement),
			                  width : 40
			               }, 
			               {
			                  key : "npdNumber",
			                  label : this.msg("label.npdNumber"),
			                  sortable : false,
			                  formatter : this.bind(this.renderNpdNumber)
			               },
			               {
			                  key : "npdProductName",
			                  label : this.msg("label.npdProductName"),
			                  sortable : false,
			                  formatter : this.bind(this.renderNpdProductName)
			               },
			               // { key: "npdType", label:this.msg("label.npdType"),
			               // sortable: false, formatter:
			               // this.bind(this.renderNpdType) },
			              
			               // { key: "description",
			               // label:this.msg("label.npdDescription"),sortable:
			               // false, formatter: this.bind(this.renderTitle) },
			               {
			                  key : "npdStatus",
			                  label : this.msg("label.npdStatus"),
			                  sortable : false,
			                  formatter : this.bind(this.renderNpdStatus)
			               }, {
			                  key : "activeSteps",
			                  label : this.msg("label.activeSteps"),
			                  sortable : false,
			                  formatter : this.bind(this.renderActiveSteps)
			               },
			               // { key: "assignedUsers",
			               // label:this.msg("label.assignedUsers"),sortable:
			               // false, formatter: this.bind(this.renderAssignedUsers)
			               // },
			               { 
			               key: "deliverables",
			                label:this.msg("label.deliverables"),sortable:
			                false, formatter: this.bind(this.renderDeliverables)
			                },
			               {
			                  key : "wfOwner",
			                  label : this.msg("label.owner"),
			                  sortable : false,
			                  formatter : this.bind(this.renderOwner)
			               }, {
			                  key : "startedDate",
			                  label : this.msg("label.started"),
			                  sortable : false,
			                  formatter : this.bind(this.renderStartedDate),
			               }, 
			               //{
//			                  key : "dueDate",
//			                  label : this.msg("label.due"),
//			                  sortable : false,
//			                  formatter : this.bind(this.renderDueDate)
//			               },
			               // { key: "npdComments",
			               // label:this.msg("label.npdComments") , sortable:
			               // false, formatter: this.bind(this.renderComments) },
			               {
			                  key : "actions",
			                  label : this.msg("label.actions"),
			                  sortable : false,
			                  formatter : this.bind(this.renderCellActions),
			                  width : 150
			               } ],
			               config : {
			                  MSG_EMPTY : this.msg("message.noWorkflows"),
			                  groupBy : "npdType",
			                  groupFormater : this.bind(this.renderNpdType)
			               }
			            },
			            dataSource : {
			               url : url,
			               defaultFilter : {
				               filterId : "all"
			               },
			               filterResolver : this.bind(function(filter) {
				               // Reuse method from WorkflowActions
				               return this.createFilterURLParameters(filter, this.options.filterParameters);
			               })
			            },
			            paginator : {
				            config : {
				               containers : [ this.id + "-paginator" ],
				               rowsPerPage : this.options.maxItems
				            }
			            }
			         });
		         }

	         },
	         /**
	          * 
	          */
	         loadStateDefs : function NPDL_loadStateDefs(){

	         	this.statesDef = [{ id: "démarré",
	         							  label: "Démarré" ,
	         							  color : "FF9999"
	         							 },
	         							 { id: "brief-marketing",
		         							  label: "Brief Marketing" ,
		         							  color : "FFCC99"
		         							 }
	         							 ,
	         							 { id: "etude-du-besoin",
		         							  label: "Etude du besoin" ,
		         							  color : "FFFF99"
		         							 }
	         							 ,
	         							 { id: "faisabilité-en-cours",
		         							  label: "Faisabilité en cours" ,
		         							  color : "CCFF99"
		         							 }
	         							 ,
	         							 { id: "commercialisé",
		         							  label: "Commercialisé" ,
		         							  color : "FF99CC"
		         							 }
	         							 ,
	         							 { id: "réservé",
		         							  label: "Réservé" ,
		         							  color : "FF5C5C"
		         							 }
	         							 ,
	         							 { id: "offre",
		         							  label: "Offre" ,
		         							  color : "5CFFFF"
		         							 }
	         							 ,
	         							 { id: "stop",
		         							  label: "Stop" ,
		         							  color : "5CFFFF"
		         							 }
	         							 ,
	         							 { id: "standby",
		         							  label: "Standby" ,
		         							  color : "FFFFFF"
		         							 }
	         							 ,
	         							 { id: "prototypage",
		         							  label: "Prototypage" ,
		         							  color : "E0E0E0"
		         							 }
	         							 ,
	         							 { id: "mise-en-production",
		         							  label: "Mise en production" ,
		         							  color : "FF0080"
		         							 }
	         	                  ];
	         	
	         	var html = "";
	         	
	         	for(i in this.statesDef){
	         		html+='<div class="npdStatus" style="background-color:#'+this.statesDef[i].color+'" ></div><span>'
		               + this.statesDef[i].label + '</span>&nbsp;';
	         	}
	         	 Dom.get(this.id + "-legend").innerHTML=html;

	         	
	         	return this.stateDef;
	         	
	         },
	         getStateColor : function NPDL_getStateColor(id){
	         	for(i in this.statesDef){
	         		if(this.statesDef[i].id==id){
	         			return this.statesDef[i].color;
	         		}
	         	}
	         	
	         	
	         },

	         /**
				 * Fired when the currently active filter has changed
				 * 
				 * @method onFilterChanged
				 * @param layer
				 *           {string} the event source
				 * @param args
				 *           {object} arguments object
				 */
	         onFilterChanged : function NPDL_onFilterChanged(layer, args) {
		         var filter = Alfresco.util.cleanBubblingObject(args[1]);
		         Dom.get(this.id + "-filterTitle").innerHTML = $html(this.msg("filter." + filter.filterId
		               + (filter.filterData ? "." + filter.filterData : ""), filter.filterData));

	         },

	         /**
				 * Fired when the currently active filter has changed
				 * 
				 * @method onFilterChanged
				 * @param layer
				 *           {string} the event source
				 * @param args
				 *           {object} arguments object
				 */
	         onWorkflowCancelled : function NPDL_onFilterChanged(layer, args) {
		         if (this.view == null || this.view == "dataTable") {
			         // Reload data table so the cancelled workflow is removed
			         this.widgets.alfrescoDataTable.loadDataTable();
		         }
	         },
	         

	         /**
				 * DataTable Cell Renderers
				 */

	
	         renderCellIcons : function NPDL_renderCellIcons(elCell, oRecord, oColumn, oData) {
		         var priority = oRecord.getData("priority"), priorityMap = {
		            "1" : "high",
		            "2" : "medium",
		            "3" : "low"
		         }, priorityKey = priorityMap[priority + ""], desc = '<img src="' + Alfresco.constants.URL_RESCONTEXT
		               + 'components/images/priority-' + priorityKey + '-16.png" title="'
		               + this.msg("label.priority", this.msg("priority." + priorityKey)) + '"/>';

		         elCell.innerHTML = desc;
	         },
	         renderTitle : function NPDL_renderTitle(elCell, oRecord, oColumn, oData) {
		         var workflow = oRecord.getData();
		         var message = workflow.message;
		         if (message === null) {
			         message = this.msg("workflow.no_message");
		         }
		         elCell.innerHTML = message;
	         },
	         renderOwner : function NPDL_renderOwner(elCell, oRecord, oColumn, oData) {
		         var workflow = oRecord.getData();
		         elCell.innerHTML = workflow.initiator.firstName + " " + workflow.initiator.lastName;
	         },
	         renderAvancement : function NPDL_renderAvancement(elCell, oRecord, oColumn, oData) {
		         var workflow = oRecord.getData();

		         elCell.innerHTML = '<span class="'
	               + this.getAdvancementClass(workflow)
	               + '">&nbsp;</span>';
		         
		         
	         },
	         renderNpdNumber : function NPDL_renderNpdNumber(elCell, oRecord, oColumn, oData) {
		         var workflow = oRecord.getData();
		         var npdNumber = workflow.tasks[0].properties.npdwf_npdNumber;

		         elCell.innerHTML = '<span><a href="'
	               + $siteURL('workflow-details?workflowId=' + workflow.id
	                     + '&referrer=workflows&myWorkflowsLinkBack=true') + '" class="theme-color-1" title="'
	               + this.msg("link.viewWorkflow") + '">'
	               + $html(npdNumber ? npdNumber : this.msg("label.none")) + '</a></span>';
		         
		         
	         },
	         renderNpdProductName : function NPDL_renderNpdProductName(elCell, oRecord, oColumn, oData) {
		         var workflow = oRecord.getData();
		         var npdProductName = workflow.tasks[0].properties.npdwf_npdProductName;
		         elCell.innerHTML =  '<a class="task-details" title="'+this.msg("link.title.task-details")+'" href="'+$siteURL('/task-details?taskId='+workflow.tasks[0].id+'&referrer=workflows')+'"> </a>'
		         +(npdProductName ? npdProductName : this.msg("label.none"));
				     
	         },
	         renderNpdStatus : function NPDL_renderNpdStatus(elCell, oRecord, oColumn, oData) {
		         var workflow = oRecord.getData();
		         var npdStatus = workflow.tasks[0].properties.npdwf_npdStatus;
		         elCell.innerHTML = '<div class="npdStatus" style="background-color:#'+this.getNpdStatusColor(workflow.tasks[0])+'" ></div>';
	         },
	         getNpdStatusColor : function NPDL_getNpdStatusColor(task) {
		         var npdStatus = task.properties.npdwf_npdStatus;
		         
		         var id = npdStatus!=null ? npdStatus.replace(new RegExp('\\s', 'g'), "-").toLowerCase(): "commercialisé";

		         
		         
		         return this.getStateColor(id);
	         },

	         renderActiveSteps : function NPDL_renderSteps(elCell, oRecord, oColumn, oData) {
		         var workflow = oRecord.getData();
		         var npdSteps = "<ul>";

		         for (i in workflow.tasks) {

			         if (workflow.tasks[i].state == "IN_PROGRESS") {

				         var taskOwner = "";
				         if (workflow.tasks[i].owner) {
					         taskOwner = "&nbsp(" + workflow.tasks[i].owner.firstName + " "
					               + workflow.tasks[i].owner.lastName + ")";
				         }

				         
				         npdSteps +='<li><a class="task-edit" title="'+this.msg("link.title.task-edit")+'" href="'+$siteURL('/task-edit?taskId='+workflow.tasks[i].id+'&referrer=workflows')+'"> </a>';
						     
				         npdSteps += '<span class="' + this.getAdvancementClass(workflow, workflow.tasks[i]) + '">'
				               + workflow.tasks[i].title + taskOwner + '</span></li>';
				         
				          
			         }
		         }
		         npdSteps +="</ul>";

		         elCell.innerHTML = (npdSteps ? npdSteps : this.msg("label.none"));
	         },
	         renderDeliverables : function NPDL_renderDeliverables(elCell, oRecord, oColumn, oData){
	         	
	         	
	         	var randomnumber=Math.floor(Math.random()*7);
	         	 elCell.innerHTML = '<ul><li><span class="doc-file" style="background-image:url(/share/res/components/images/states/valid-16.png)"></span><span class="doc-file" style="background-image:url(/share/res/components/images/filetypes/doc-file-16.png)"><a class="theme-color-1">Barquette plastique 100x200</a></span></li>';
	         	
	         	 
	         	 for(var i = 0 ; i <randomnumber ; i++){
	         		 
	         		
	         		 elCell.innerHTML += '<li><span class="doc-file" style="background-image:url(/share/res/components/images/states/'+(i%3==0?'valid':'stop')+'-16.png)"></span><span class="doc-file" style="background-image:url(/share/res/components/images/filetypes/xls-file-16.png)"><a class="theme-color-1">Doc test '+i+'</a></span></li>';
	         	}
	         	 elCell.innerHTML +="</ul>";
	         	
	         },
	         renderAssignedUsers : function NPDL_renderAssignedUsers(elCell, oRecord, oColumn, oData) {
		         var workflow = oRecord.getData();

		         var npdAsignedUsers = "";

		         for (i in workflow.tasks) {

			         if (workflow.tasks[i].state == "IN_PROGRESS" && workflow.tasks[i].owner != null) {
				         if (npdAsignedUsers != "")
					         npdAsignedUsers += ", ";
				         npdAsignedUsers += workflow.tasks[i].owner.firstName + " " + workflow.tasks[i].owner.lastName;
			         }
		         }

		         elCell.innerHTML = (npdAsignedUsers ? npdAsignedUsers : this.msg("label.none"));
	         },
	         renderNpdType : function NPDL_renderNpdType(elCell, oRecord, oColumn, oData) {
		         var workflow = oRecord.getData();
		         var npdType = (workflow.tasks[0].properties.npdwf_npdType ? workflow.tasks[0].properties.npdwf_npdType
		               : this.msg("label.none"));

		         if (elCell == null) {
			         return npdType;
		         }
		         elCell.innerHTML = npdType;
	         },
	         renderComments : function NPDL_renderComments(elCell, oRecord, oColumn, oData) {
		         var workflow = oRecord.getData();

		         // var npdComments = workflow.tasks[0].properties.bpm_comment;
		         var npdComments = null;

		         for (i in workflow.tasks) {

			         if (workflow.tasks[i].name == "npdwf:validate-analysis") {
				         npdComments = workflow.tasks[i].properties.bpm_comment;
				         // break; we want the last comment
			         }
		         }

		         elCell.innerHTML = (npdComments ? npdComments : this.msg("label.none"));
	         },
	         renderDueDate : function NPDL_renderDueDate(elCell, oRecord, oColumn, oData) {
		         var workflow = oRecord.getData();
		         var dueDate = workflow.dueDate ? Alfresco.util.fromISO8601(workflow.dueDate) : null;
		         elCell.innerHTML = (dueDate ? Alfresco.util.formatDate(dueDate, "shortDate") : this.msg("label.none"));
	         },
	         renderStartedDate : function NPDL_renderStartedDate(elCell, oRecord, oColumn, oData) {
		         var workflow = oRecord.getData();
		         var dueDate = workflow.dueDate ? Alfresco.util.fromISO8601(workflow.dueDate) : null;
		        
		         var startedDate = workflow.startDate ? Alfresco.util.fromISO8601(workflow.startDate) : null;
		         elCell.innerHTML = (startedDate ? Alfresco.util.formatDate(startedDate, "shortDate") : this
		               .msg("label.none"));
		         elCell.innerHTML += "<br/>" + (dueDate ? Alfresco.util.formatDate(dueDate, "shortDate") : this.msg("label.none"));  
	         },
	         renderCellActions : function NPDL_renderCellActions(elCell, oRecord, oColumn, oData) {
		         // Create actions using WorkflowAction
		         this.createAction(elCell, this.msg("link.viewWorkflow"), "npd-view-link",
		               $siteURL('workflow-details?workflowId=' + oRecord.getData('id')
		                     + '&referrer=workflows&myWorkflowsLinkBack=true'));
		         this.createAction(elCell, this.msg("link.cancelWorkflow"), "npd-cancel-link", function(event, oRecord) {
			         this.cancelWorkflow(oRecord.getData("id"), oRecord.getData("message"));
			         Event.preventDefault(event);
		         }, oRecord);
	         },
	         getAdvancementClass : function NPDL_getAdvancementClass(workflow, task) {
		         var percent = 0, suffix = "";

		         if (task != null) {
			         percent = this.getTaskAdvancementPercent(task);
		         } else {
			         suffix = "-32";
			         for (i in workflow.tasks) {
				         percent += this.getTaskAdvancementPercent(workflow.tasks[i]);
			         }

			         percent = percent / workflow.tasks.length;

			         if (workflow.dueDate != null) {
				         var completionDate = this.resetDate(new Date());
				         var dueDate = this.resetDate(Alfresco.util.fromISO8601(workflow.dueDate));
				         if (completionDate.getTime() == dueDate.getTime()) {
					         percent = 75;
				         } else if (completionDate.getTime() > dueDate.getTime()) {
					         percent = 0;
				         }
			         }
		         }

		         if (percent > 80) {
			         return "advancement-less100" + suffix;
		         }
		         if (percent > 60) {
			         return "advancement-less80" + suffix;
		         }
		         if (percent > 40) {
			         return "advancement-less60" + suffix;
		         }
		         if (percent > 20) {
			         return "advancement-less40" + suffix;
		         }

		         return "advancement-less20" + suffix;
	         },

	         getTaskAdvancementPercent : function NPDL_getAdvancementClass(task) {

	         	    var dueDate = task.properties.bpm_dueDate;
	    
	         	
		         if (dueDate != null) {
			         dueDate = Alfresco.util.fromISO8601(dueDate);
			         var completionDate = new Date();
			         if (task.state == "COMPLETED") {
				         completionDate = Alfresco.util.fromISO8601(task.properties.bpm_completionDate);
			         }
			         this.resetDate(completionDate);
			         this.resetDate(dueDate);
			         if (completionDate.getTime() == dueDate.getTime()) {
				         return 50;
			         }
			         if (completionDate.getTime() > dueDate.getTime()) {
				         return 0;
			         }

		         }
		         return 100;
	         },
	         resetDate : function NPDL_resetDate(date) {
		         date.setHours(0);
		         date.setMinutes(0);
		         date.setSeconds(0);
		         return date;
	         }

	       
	      }, true);
})();