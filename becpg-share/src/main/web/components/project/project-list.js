/**
 * @namespace beCPG
 * @class beCPG.component.ProjectList
 */

var g; // gantt var

(function() {
	/**
	 * YUI Library aliases
	 */
	var Dom = YAHOO.util.Dom;

	/**
	 * Alfresco Slingshot aliases
	 */
	var $html = Alfresco.util.encodeHTML;

	var TASK_EVENTCLASS = Alfresco.util.generateDomId(null, "task");

	/**
	 * DocumentList constructor.
	 * 
	 * @param htmlId
	 *           {String} The HTML id of the parent element
	 * @return {beCPG.component.ProjectList} The new DocumentList instance
	 * @constructor
	 */
	beCPG.component.ProjectList = function(htmlId, view) {

		this.view = view;

		beCPG.component.ProjectList.superclass.constructor.call(this, htmlId);

		/**
		 * Decoupled event listeners
		 */
		YAHOO.Bubbling.on("filterChanged", this.onFilterChanged, this);

		return this;
	};

	/**
	 * Extend from Alfresco.component.Base
	 */
	YAHOO.extend(beCPG.component.ProjectList, beCPG.module.EntityDataGrid);

	/**
	 * Augment prototype with main class implementation, ensuring overwrite is
	 * enabled
	 */
	YAHOO.lang
	      .augmentObject(
	            beCPG.component.ProjectList.prototype,
	            {
	               view : "dataTable",
	               /**
						 * State definition
						 */
	               taskLegends : [],

	               /**
						 * Fired by YUI when parent element is available for
						 * scripting. Initial History Manager event registration
						 * 
						 * @method onReady
						 */
	               onReady : function PL_onReady() {

		               var url = Alfresco.constants.PROXY_URI
		                     + "becpg/project/info?site="+this.options.site;

		               Alfresco.util.Ajax.request({
		                  url : url,
		                  successCallback : {
		                     fn : function(response) {

			                     var data = response.json.result;

			                     var html = "";

			                     for ( var i in data) {

				                     var taskLegend = {
				                        id : data[i].value,
				                        label : data[i].name
				                     };
				                     for ( var j in data[i].metadatas) {
					                     if (data[i].metadatas[j].key == "color") {

						                     taskLegend.color = data[i].metadatas[j].value;
					                     }
				                     }

				                     this.taskLegends.push(taskLegend);

				                     html += '<div class="projectStatus" style="background-color:#' + taskLegend.color
				                           + '" ></div><span>' + taskLegend.label + '</span>&nbsp;';
			                     }

			                     Dom.get(this.id + "-legend").innerHTML = html;
			                     if (this.view == "gantt") {
				                     this.initGantt();
			                     }
			                     this.initDataTable();

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

	               },
	               initDataTable : function PL_initDataTable() {
		               beCPG.component.ProjectList.superclass.onReady.call(this);

		               var me = this;
		               this.populateDataGrid();

		               var fnOnShowTaskHandler = function PL__fnOnShowTaskHandler(layer, args) {
			               var owner = YAHOO.Bubbling.getOwnerByTagName(args[1].anchor, "span");
			               if (owner !== null) {
				               me.onActionShowTask.call(me, owner.className, owner);
			               }
			               return true;
		               };
		               YAHOO.Bubbling.addDefaultAction(TASK_EVENTCLASS, fnOnShowTaskHandler);
	               },

	               onActionShowTask : function PL_onActionShowTask(className) {

		               var me = this;

		               // Intercept before dialog show
		               var doBeforeDialogShow = function PL_onActionShowTask_doBeforeDialogShow(p_form, p_dialog) {
			               Alfresco.util.populateHTML([ p_dialog.id + "-dialogTitle", me.msg("label.edit-row.title") ]);

		               };

		               var nodes = className.replace("node-", "").split("|");
		               
		               var templateUrl = YAHOO.lang
		                     .substitute(
		                           Alfresco.constants.URL_SERVICECONTEXT
		                                 + "components/form?entityNodeRef={entityNodeRef}&itemKind={itemKind}&itemId={itemId}&mode={mode}&submitType={submitType}&showCancelButton=true",
		                           {
		                              itemKind : "node",
		                              itemId : nodes[0],
		                              mode : "edit",
		                              submitType : "json",
		                              entityNodeRef : nodes[1]
		                           });

		               // Using Forms Service, so always create new instance

		               var editDetails = new Alfresco.module.SimpleDialog(this.id + "-editCharacts");

		               editDetails.setOptions({
		                  width : "34em",
		                  templateUrl : templateUrl,
		                  actionUrl : null,
		                  destroyOnHide : true,
		                  doBeforeDialogShow : {
		                     fn : doBeforeDialogShow,
		                     scope : this
		                  },

		                  onSuccess : {
		                     fn : function PL_onActionShowTask_success(response) {

			                     // Display success message
			                     Alfresco.util.PopupManager.displayMessage({
				                     text : me.msg("message.details.success")
			                     });

		                     },
		                     scope : this
		                  },
		                  onFailure : {
		                     fn : function PL_onActionShowTask_failure(response) {
			                     Alfresco.util.PopupManager.displayMessage({
				                     text : me.msg("message.details.failure")
			                     });
		                     },
		                     scope : this
		                  }
		               }).show();

	               },
	               initGantt : function PL_initGantt() {

		               var fnDrawGantt = function PL_onReady_fnDrawGantt() {
			               var recordSet = this.widgets.dataTable.getRecordSet();

			               g = new JSGantt.GanttChart('g', Dom.get(this.id + "-gantt"), 'day');
			               g.setDateInputFormat("shortDate");
			               g.setDateDisplayFormat("shortDate");
			               g.setCaptionType('Resource');

			               for ( var i = 0; i < recordSet.getLength(); i++) {

				               var oRecord = recordSet.getRecord(i);
				               var oData = oRecord.getData();

				               var title = '<span class="' + this.getAdvancementClass(oRecord) + '">'
				                     + this.getProjectTitle(oRecord) + '</span>';

				               var initiator = oRecord.getData("itemData")["prop_cm_creator"].displayValue;
				               var percent = oRecord.getData("itemData")["prop_pjt_completionPercent"].value;

				               var dates = this.extractDates(oRecord);

				               g.AddTaskItem(new JSGantt.TaskItem(i, title, dates.start, dates.due, 'FFBC00', '', 0,
				                     initiator, percent, 1, 1, 1));

				               var start = dates.start;

				               var taskList = oRecord.getData("itemData")["dt_pjt_taskList"];

				               var cache = [];

				               for (j in taskList) {
					               var task = taskList[j];
					               var taskId = task.nodeRef;
					               var precTaskIds = "";
					               for ( var z in task["itemData"]["assoc_pjt_tlPrevTasks"]) {
						               var precTaskId = task["itemData"]["assoc_pjt_tlPrevTasks"][z].value;
						               if (precTaskIds.length > 0) {
							               precTaskIds += ",";
						               }
						               precTaskIds += precTaskId;

						               if (cache[precTaskId] && cache[precTaskId].end.getTime() > start.getTime()) {
							               start = cache[precTaskId].end;
						               }

					               }

					               var tlIsMilestone = task["itemData"]["prop_pjt_tlIsMilestone"].value;
					               var tlPercent = task["itemData"]["prop_pjt_completionPercent"].value;
					               var taskOwner = task["itemData"]["assoc_pjt_tlResources"].length > 0 ? task["itemData"]["assoc_pjt_tlResources"][0].displayValue
					                     : null;

					               var tdates = this.extractDates(task, start);

					               cache[taskId] = tdates;

					               g.AddTaskItem(new JSGantt.TaskItem(taskId, this.getTaskTitle(task, oData.nodeRef), tdates.start,
					                     tdates.end, this.getTaskColor(task), null, tlIsMilestone ? 1 : 0, taskOwner,
					                     tlPercent, 0, i, 1, precTaskIds));

				               }

			               }

			               g.Draw();
			               g.DrawDependencies();

		               };
		               this.afterDataGridUpdate.push(fnDrawGantt);

	               },

	               getTaskColor : function PL_getTaskColor(task) {
		               var id = task["itemData"]["assoc_pjt_tlTaskLegend"][0].value;

		               for (i in this.taskLegends) {
			               if (this.taskLegends[i].id == id) {
				               return this.taskLegends[i].color;
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
	               onFilterChanged : function PL_onFilterChanged(layer, args) {
		               var filter = Alfresco.util.cleanBubblingObject(args[1]);
		               Dom.get(this.id + "-filterTitle").innerHTML = $html(this.msg("filter." + filter.filterId
		                     + (filter.filterData ? "." + filter.filterData : ""), filter.filterData));

	               },

	               getAdvancementClass : function PL_getAdvancementClass(oRecord, task) {
		               var percent = 0, suffix = "";

		               if (task != null) {
			               percent = this.getTaskAdvancementPercent(task);
		               } else {
			               suffix = "-32";

			               var taskList = oRecord.getData("itemData")["dt_pjt_taskList"];
			               for (i in taskList) {
				               percent += this.getTaskAdvancementPercent(taskList[i]);
			               }

			               percent = percent / taskList.length;

			               var dates = this.extractDates(oRecord);

			               if (dates.due != null && dates.end == null) {
				               var completionDate = this.resetDate(new Date());
				               if (completionDate.getTime() == dates.due.getTime()) {
					               percent = 75;
				               } else if (completionDate.getTime() > dates.due.getTime()) {
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
	               extractDates : function(oRecord, start) {

		               var startDate = null, endDate = null, dueDate = null;

		               if (oRecord["itemData"]) {
			               startDate = oRecord["itemData"]["prop_pjt_tlStart"].value;
			               endDate = oRecord["itemData"]["prop_pjt_tlEnd"].value;

			               startDate = startDate != null ? this.resetDate(Alfresco.util.fromISO8601(startDate)) : this
			                     .resetDate(start);

			               if (endDate == null) {
				               var duration = oRecord["itemData"]["prop_pjt_tlDuration"].value;
				               if (duration == null) {
					               duration = 0;
				               }
				               endDate = new Date();
				               endDate.setDate(startDate.getDate() + duration);

			               }

			               return {
			                  start : startDate,
			                  end : endDate
			               };

		               }

		               startDate = oRecord.getData("itemData")["prop_pjt_projectStartDate"].value;
		               endDate = oRecord.getData("itemData")["prop_pjt_projectCompletionDate"].value;
		               dueDate = oRecord.getData("itemData")["prop_pjt_projectDueDate"].value;

		               return {
		                  start : startDate != null ? this.resetDate(Alfresco.util.fromISO8601(startDate)) : new Date(),
		                  end : endDate != null ? this.resetDate(Alfresco.util.fromISO8601(endDate)) : null,
		                  due : dueDate != null ? this.resetDate(Alfresco.util.fromISO8601(dueDate)) : null
		               };

	               },

	               getTaskAdvancementPercent : function PL_getTaskAdvancementPercent(task) {

		               var dates = this.extractDates(task), now = new Date();

		               if (!task["itemData"]["prop_pjt_tlState"].value == "Completed") {

			               if (now.getTime() == dates.due.getTime()) {
				               return 50;
			               }
			               if (now.getTime() > dates.due.getTime()) {
				               return 0;
			               }

		               }

		               return 100;
	               },
	               resetDate : function PL_resetDate(date) {
		               if (date == null) {
			               date = new Date();
		               }

		               date.setHours(0);
		               date.setMinutes(0);
		               date.setSeconds(0);
		               return date;
	               },

	               getTaskTitle : function PL_getTaskTitle(task, entityNodeRef) {
		               return '<span class="' + this.getAdvancementClass(null, task) + '">' + '<span class="node-'
		                     + task.nodeRef+'|'+ entityNodeRef + '"><a class="theme-color-1 ' + TASK_EVENTCLASS + '" title="'
		                     + this.msg("link.title.task-edit") + '" >'
		                     + task["itemData"]["prop_pjt_tlTaskName"].displayValue + '</a></span></span>';
	               },
	               getDeliverableTitle : function PL_getDeliverableTitle(deliverable, entityNodeRef) {

		               var ret = '<span class="doc-file" ';
		               if (deliverable["itemData"]["prop_pjt_dlState"].value == "Completed") {
			               ret += 'style="background-image:url(' + Alfresco.constants.URL_RESCONTEXT
			                     + '/components/images/states/valid-16.png)">';
		               } else {
			               ret += 'style="background-image:url(' + Alfresco.constants.URL_RESCONTEXT
			                     + '/components/images/states/stop-16.png)">';
		               }

		               ret += '<span class="node-' + deliverable.nodeRef +'|'+ entityNodeRef + '"><a class="theme-color-1 ' + TASK_EVENTCLASS
		                     + '" title="' + this.msg("link.title.task-edit") + '" >'
		                     + deliverable["itemData"]["prop_pjt_dlDescription"].displayValue + '</a></span>';

		               return ret;
	               },
	               getProjectTitle : function PL_getProjectTitle(oRecord) {
		               var url = null, version = "";

		               var title = oRecord.getData("itemData")["prop_cm_name"].displayValue;
		               var code = oRecord.getData("itemData")["prop_bcpg_code"].displayValue;

		               var oData = oRecord.getData();

		               if (oData.siteId) {
			               url = Alfresco.constants.URL_PAGECONTEXT + "site/" + oData.siteId + "/"
			                     + 'entity-data-lists?nodeRef=' + oData.nodeRef;
		               } else {
			               url = Alfresco.constants.URL_PAGECONTEXT + 'entity-data-lists?nodeRef=' + oData.nodeRef;
		               }
		               if (oData.version && oData.version !== "") {
			               version = '<span class="document-version">' + oData.version + '</span>';
		               }

		               return '<span  ><a class="theme-color-1" href="' + url + '">' + code + "&nbsp;-&nbsp;"
		                     + $html(title) + '</a></span>' + version;
	               }
	            }, true);
	// ,
	// /**
	// * Takes a filter and looks for its url parameter
	// * representation
	// *
	// * @method createFilterURLParameters
	// * @param filter
	// * {object} The filter to create url parameters for
	// * @param filterParameters
	// * {Array} List of configured filter parameters that
	// * shall create url parameters
	// * @return URL parameters created from the instructions in
	// * filterParameters based on data from the filter OR
	// * null no instructions were found
	// * @override
	// */
	// createFilterURLParameters : function
	// DateFilter_createFilterURLParameters(filter, filterParameters) {
	// if (YAHOO.lang.isString(filter.filterData)) {
	// var filterParameter, result = null;
	// for ( var fpi = 0, fpil = filterParameters.length; fpi < fpil; fpi++) {
	// filterParameter = filterParameters[fpi];
	// if ((filter.filterId == filterParameter.id || filterParameter.id == "*")
	// && (filter.filterData == filterParameter.data || filterParameter.data ==
	// "*")) {
	// return this.substituteParameters(filterParameter.parameters, {
	// id : filter.filterId,
	// data : filter.filterData
	// });
	// }
	// }
	// }
	// return null;
	// },
	//
	// /**
	// * Takes a template and performs substituion against "Obj" and
	// * according to date instructions as described below.
	// *
	// * Assumes the template data may contain date instructions
	// * where the instructions are placed inside curly brackets:
	// * "param={attr}" - the name of an attribute in "obj"
	// * "param={0dt}" - the current date time in iso8601 format
	// * "param={1d}" - the current date (time set to end of day)
	// * and rolled l days forward "param={-2d}" - the current date
	// * (time set to end of day) and rolled 2 days backward
	// *
	// * @param template
	// * The template containing attributes from obj and
	// * dates to resolve
	// * @param obj
	// * Contains runtime values
	// */
	// substituteParameters : function(template, obj) {
	// var unresolvedTokens = template.match(/{[^}]+}/g);
	// if (unresolvedTokens) {
	// var resolvedTokens = {}, name, value, date;
	// for ( var i = 0, il = unresolvedTokens.length; i < il; i++) {
	// name = unresolvedTokens[i].substring(1, unresolvedTokens[i].length - 1);
	// value = name;
	// date = new Date();
	// if (/^[\-\+]?\d+(d|dt)$/.test(value)) {
	// if (/^[\-\+]?\d+(d)$/.test(value)) {
	// // Only date (and not datetime) that was
	// // requested
	// date.setHours(11);
	// date.setMinutes(59);
	// date.setSeconds(59);
	// date.setMilliseconds(999);
	// }
	// date.setDate(date.getDate() + parseInt(value));
	// value = date;
	// } else {
	// value = obj[name];
	// }
	// resolvedTokens[name] = Alfresco.util.isDate(value) ?
	// Alfresco.util.toISO8601(value) : value;
	// }
	// return YAHOO.lang.substitute(template, resolvedTokens);
	// }
	// return template;
	// }
	//
	// }, true);

	// filterResolver: this.bind(function(filter)
	// {
	// // Reuse method from WorkflowActions
	// return this.createFilterURLParameters(filter,
	// this.options.filterParameters);
	// }

})();