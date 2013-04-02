/**
 * @namespace beCPG
 * @class beCPG.component.ProjectList
 */

var g; // gantt var

(function() {
   /**
    * YUI Library aliases
    */
   var Dom = YAHOO.util.Dom, Bubbling = YAHOO.Bubbling;

   /**
    * Alfresco Slingshot aliases
    */
   var $html = Alfresco.util.encodeHTML;

   var TASK_EVENTCLASS = Alfresco.util.generateDomId(null, "task");

   /**
    * DocumentList constructor.
    * 
    * @param htmlId
    *            {String} The HTML id of the parent element
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
    * Augment prototype with main class implementation, ensuring overwrite is enabled
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

                  cache : [],

                  /**
                   * Fired by YUI when parent element is available for scripting. Initial History Manager event
                   * registration
                   * 
                   * @method onReady
                   */
                  onReady : function PL_onReady() {

                     var url = Alfresco.constants.PROXY_URI + "becpg/project/info" + (this.options.siteId != null && this.options.siteId.length > 0 ? "?site=" + this.options.siteId
                           : "");

                     Alfresco.util.Ajax
                           .request({
                              url : url,
                              successCallback : {
                                 fn : function(response) {

                                    var data = response.json.legends;

                                    this.options.parentNodeRef = response.json.parentNodeRef;

                                    var html = "";

                                    for ( var i in data) {

                                       var taskLegend = {
                                          id : data[i].nodeRef,
                                          label : data[i].label,
                                          color : data[i].color
                                       };

                                       this.taskLegends.push(taskLegend);

                                       html += '<div class="projectStatus" style="background-color:#' + taskLegend.color + '" ></div><span>' + taskLegend.label + '</span>&nbsp;';
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
                                 Alfresco.constants.URL_SERVICECONTEXT + "components/form?entityNodeRef={entityNodeRef}&itemKind={itemKind}&itemId={itemId}&mode={mode}&submitType={submitType}&showCancelButton=true&popup=true",
                                 {
                                    itemKind : "node",
                                    itemId : nodes[0],
                                    mode : "edit",
                                    submitType : "json",
                                    entityNodeRef : nodes[1]
                                 });

                     // Using Forms Service, so always create new instance

                     var editDetails = new Alfresco.module.SimpleDialog(this.id + "-editCharacts-" + Alfresco.util
                           .generateDomId());

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

                              Bubbling.fire(me.scopeId + "dataItemUpdated", {
                                 nodeRef : nodes[1],
                                 callback : function(item) {

                                    // Display success message
                                    Alfresco.util.PopupManager.displayMessage({
                                       text : me.msg("message.details.success")
                                    });
                                 }
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
                        if (recordSet.getLength() != 0) {
                           g = new JSGantt.GanttChart('g', Dom.get(this.id + "-gantt"), 'day');
                           g.setDateInputFormat("shortDate");
                           g.setDateDisplayFormat("shortDate");
                           g.setCaptionType('Resource');

                           for ( var i = 0; i < recordSet.getLength(); i++) {

                              var oRecord = recordSet.getRecord(i);
                              var oData = oRecord.getData();
                              var projectId = oData.nodeRef;

                              var title = '<span class="' + this.getOverdueClass(oRecord) + '">' + this
                                    .getProjectTitle(oRecord) + '</span>';

                              var initiator = '<span class="resource-title">' + oRecord.getData("itemData")["assoc_pjt_projectManager"].displayValue + '</span>';
                              var percent = oRecord.getData("itemData")["prop_pjt_completionPercent"].value;

                              var dates = this.extractDates(oRecord);

                              g.AddTaskItem(new JSGantt.TaskItem(projectId, title, dates.start, dates.due, 'FFBC00',
                                    '', 0, initiator, percent, 1, 1, 1));

                              var start = dates.start;

                              var taskList = oRecord.getData("itemData")["dt_pjt_taskList"];

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

                                    if (this.cache[precTaskId] != null && this.cache[precTaskId].end != null && this.cache[precTaskId].end
                                          .getTime() > start.getTime()) {
                                       start = this.cache[precTaskId].end;
                                    }

                                 }

                                 var tlIsMilestone = task["itemData"]["prop_pjt_tlIsMilestone"].value;
                                 var tlPercent = task["itemData"]["prop_pjt_completionPercent"].value;

                                 var taskOwner = task["itemData"]["assoc_pjt_tlResources"].length > 0 ? ('<span class="resource-title">' + task["itemData"]["assoc_pjt_tlResources"][0].displayValue + '</span>')
                                       : null;

                                 var tdates = this.cache[taskId];
                                 if (!tdates) {
                                    tdates = this.extractDates(task, start);
                                    this.cache[taskId] = tdates;
                                 }

                                 g.AddTaskItem(new JSGantt.TaskItem(taskId, this.getTaskTitle(task, oData.nodeRef,
                                       null, tdates.start), tdates.start, tdates.end, this.getTaskColor(task), null,
                                       tlIsMilestone ? 1 : 0, taskOwner, tlPercent, 0, projectId, 1, precTaskIds));

                              }

                           }

                           g.Draw();
                           g.DrawDependencies();
                        } else {
                           Alfresco.util.populateHTML([ this.id + "-gantt",
                                 "<div class=\"yui-dt-liner\">" + this.msg("message.empty") + "</div>" ]);
                        }

                     };
                     this.cache = [];
                     this.extraAfterDataGridUpdate.push(fnDrawGantt);

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
                   *            {string} the event source
                   * @param args
                   *            {object} arguments object
                   */
                  onFilterChanged : function PL_onFilterChanged(layer, args) {
                     var filter = Alfresco.util.cleanBubblingObject(args[1]);
                     if (filter.filterId == "filterform") {
                        Dom.get(this.id + "-filterTitle").innerHTML = $html(this.msg("filter." + filter.filterId));
                     } else if (filter.filterId == "tag") {
                        Dom.get(this.id + "-filterTitle").innerHTML = $html(this.msg("filter." + filter.filterId,
                              filter.filterData));
                     } else {
                        Dom.get(this.id + "-filterTitle").innerHTML = $html(this.msg(
                              "filter." + filter.filterId + (filter.filterData ? "." + filter.filterData : ""),
                              filter.filterData));
                     }

                  },

                  /**
                   * Data Item created event handler
                   * 
                   * @method onDataItemCreated
                   * @param layer
                   *            {object} Event fired
                   * @param args
                   *            {array} Event parameters (depends on event type)
                   */
                  onDataItemCreated : function EntityDataGrid_onDataItemCreated(layer, args) {
                     var obj = args[1];
                     if (obj && (obj.nodeRef !== null)) {

                        var nodeRef = new Alfresco.util.NodeRef(obj.nodeRef), url = this.options.itemUrl + nodeRef.uri + ((this.options.entityNodeRef != null && this.options.entityNodeRef.length > 0) ? "?entityNodeRef=" + this.options.entityNodeRef + "&"
                              : "?") + "itemType=" + encodeURIComponent(this.options.itemType != null ? this.options.itemType
                              : this.datalistMeta.itemType) + "&dataListName=" + encodeURIComponent(this.datalistMeta.name != null ? this.datalistMeta.name
                              : this.options.list);

                        // Reload the node's metadata
                        Alfresco.util.Ajax.jsonPost({
                           url : url,
                           dataObj : this._buildDataGridParams(),
                           successCallback : {
                              fn : function EntityDataGrid_onDataItemCreated_refreshSuccess(response) {

                                 if (response.json && (response.json.item !== null)) {
                                    var item = response.json.item;

                                    YAHOO.Bubbling.fire("changeFilter", filter = {
                                       filterOwner : "Alfresco.component.AllFilter",
                                       filterId : "projects",
                                       filterData : item["itemData"]["prop_pjt_projectState"].value
                                    });
                                 }
                              },
                              scope : this
                           },
                           failureCallback : {
                              fn : function EntityDataGrid_onDataItemCreated_refreshFailure(response) {
                                 Alfresco.util.PopupManager.displayMessage({
                                    text : this.msg("message.create.refresh.failure")
                                 });
                              },
                              scope : this
                           }
                        });
                     }
                  },

                  getTaskAdvancementClass : function PL_getAdvancementClass(task) {

                     if (task["itemData"]["prop_pjt_tlState"].value == "Completed") {
                        return "advancement-done";
                     }

                     var dates = this.extractDates(task), now = new Date();

                     if (now.getTime() == dates.end.getTime()) {
                        return "overdue-0to9";
                     }
                     if (now.getTime() > dates.end.getTime()) {
                        return "overdue-20to29";
                     }

                     return "overdue-negative";
                  },
                  getOverdueClass : function PL_getOverdueClass(oRecord, size) {
                     var percent = 0, overdue = oRecord.getData("itemData")["prop_pjt_projectOverdue"], dates = this
                           .extractDates(oRecord), suffix = size != null ? "-" + size : "";

                     if (overdue.value != null && dates.start != null && dates.due != null) {
                        percent = 100 * (overdue.value / (dates.due.getTime() - dates.start.getTime())) * 24 * 60 * 60 * 1000;
                     }

                     if (percent > 30) {
                        return "overdue-30plus" + suffix;
                     }
                     if (percent > 20) {
                        return "overdue-20to29" + suffix;
                     }
                     if (percent > 10) {
                        return "overdue-10to19" + suffix;
                     }
                     if (percent > 0) {
                        return "overdue-0to9" + suffix;
                     }

                     return "overdue-negative" + suffix;
                  },
                  extractDates : function(oRecord, start) {

                     var startDate = null, endDate = null, dueDate = null;

                     if (oRecord["itemData"]) {
                        startDate = oRecord["itemData"]["prop_pjt_tlStart"].value;
                        endDate = oRecord["itemData"]["prop_pjt_tlEnd"].value;

                        endDate = endDate != null ? this.resetDate(Alfresco.util.fromISO8601(endDate)) : null;
                        startDate = startDate != null ? this.resetDate(Alfresco.util.fromISO8601(startDate)) : this
                              .resetDate(start);

                        if (endDate == null) {
                           var duration = oRecord["itemData"]["prop_pjt_tlDuration"].value;
                           if (duration == null) {
                              var tlIsMilestone = oRecord["itemData"]["prop_pjt_tlIsMilestone"].value;
                              if (tlIsMilestone) {
                                 duration = 1;
                              } else {
                                 duration = 0;
                              }
                           }

                           endDate = new Date(startDate.getTime() + duration * 24 * 60 * 60 * 1000);

                        }

                        return {
                           start : startDate,
                           end : endDate
                        };

                     }

                     startDate = oRecord.getData("itemData")["prop_pjt_projectStartDate"].value;
                     endDate = oRecord.getData("itemData")["prop_pjt_projectCompletionDate"].value;
                     dueDate = oRecord.getData("itemData")["prop_pjt_projectDueDate"].value;

                     startDate = startDate != null ? this.resetDate(Alfresco.util.fromISO8601(startDate)) : new Date();
                     endDate = endDate != null ? this.resetDate(Alfresco.util.fromISO8601(endDate)) : null;
                     dueDate = dueDate != null ? this.resetDate(Alfresco.util.fromISO8601(dueDate)) : this
                           .computeDueDate(startDate, oRecord);
                     return {
                        start : startDate,
                        end : endDate,
                        due : dueDate
                     };

                  },
                  computeDueDate : function(start, oRecord) {
                     var taskList = oRecord.getData("itemData")["dt_pjt_taskList"];
                     var ret = start;
                     for (j in taskList) {
                        var task = taskList[j];
                        var taskId = task.nodeRef;

                        var tdates = this.cache[taskId];
                        if (!tdates) {
                           for ( var z in task["itemData"]["assoc_pjt_tlPrevTasks"]) {
                              var precTaskId = task["itemData"]["assoc_pjt_tlPrevTasks"][z].value;

                              if (this.cache[precTaskId] != null && this.cache[precTaskId].end != null && this.cache[precTaskId].end
                                    .getTime() > start.getTime()) {
                                 start = this.cache[precTaskId].end;
                              }

                           }

                           tdates = this.extractDates(task, start);
                           this.cache[taskId] = tdates;
                        }

                        ret = tdates.end;

                     }
                     return ret;
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

                  getTaskTitle : function PL_getTaskTitle(task, entityNodeRef, full, start) {
                     var ret = '<span class="' + this.getTaskAdvancementClass(task) + '">';

                     if (full) {
                        ret += '<div class="projectStatus" style="background-color:#' + this.getTaskColor(task) + '" ></div>';
                     }

                     ret += '<span class="node-' + task.nodeRef + '|' + entityNodeRef + '"><a class="theme-color-1 ' + TASK_EVENTCLASS + '" title="' + this
                           .msg("link.title.task-edit") + '" >' + task["itemData"]["prop_pjt_tlTaskName"].displayValue + ' (' + task["itemData"]["prop_pjt_completionPercent"].displayValue + '%)</a></span>';

                     if (task["itemData"]["prop_pjt_tlWorkflowInstance"] && task["itemData"]["prop_pjt_tlWorkflowInstance"].value) {
                        ret += '<a class="task-link" title="' + this.msg("link.title.open-workflow") + '" href="' + Alfresco.constants.URL_PAGECONTEXT + 'workflow-details?workflowId=' + task["itemData"]["prop_pjt_tlWorkflowInstance"].value + '&referrer=project-list&myWorkflowsLinkBack=true' + '" >&nbsp;</a>';
                     }

                     ret += "</span>";

                     return ret;
                  },
                  getDeliverableTitle : function PL_getDeliverableTitle(deliverable, entityNodeRef) {

                     var ret = '<span class="delivrable-status delivrable-status-' + deliverable["itemData"]["prop_pjt_dlState"].value + '">';

                     var contents = deliverable["itemData"]["assoc_pjt_dlContent"];

                     if (contents.length > 0) {
                        ret += '<span class="doc-file"><a title="' + this.msg("link.title.open-document") + '" href="' + this
                              ._buildCellUrl(contents[0]) + '"><img src="' + Alfresco.constants.URL_RESCONTEXT + 'components/images/filetypes/' + Alfresco.util
                              .getFileIcon(contents[0].displayValue, "cm:content", 16) + '" /></a></span>';
                     }

                     ret += '<span class="node-' + deliverable.nodeRef + '|' + entityNodeRef + '"><a class="theme-color-1 ' + TASK_EVENTCLASS + '" title="' + this
                           .msg("link.title.deliverable-edit") + '" >' + deliverable["itemData"]["prop_pjt_dlDescription"].displayValue + '</a></span>';

                     return ret;
                  },
                  getProjectTitle : function PL_getProjectTitle(oRecord) {
                     var url = null, urlFolder = null, version = "";

                     var title = oRecord.getData("itemData")["prop_cm_name"].displayValue;
                     var code = oRecord.getData("itemData")["prop_bcpg_code"].displayValue;

                     var oData = oRecord.getData();
                     url = beCPG.util.entityCharactURL(oData.siteId, oData.nodeRef);
                     urlFolder = beCPG.util.entityDocumentsURL(oData.siteId, oData.path, title);

                     if (oData.version && oData.version !== "") {
                        version = '<span class="document-version">' + oData.version + '</span>';
                     }

                     return '<span class="project-title"><a class="folder-link" href="' + urlFolder + '" title="' + this
                           .msg("link.title.open-folder") + '">&nbsp;</a><a class="theme-color-1" href="' + url + '" title="' + this
                           .msg("link.title.open-project") + '">' + code + "&nbsp;-&nbsp;" + $html(title) + '</a></span>' + version;
                  }
               }

               , true);

})();