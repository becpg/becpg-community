(function() {

   var Bubbling = YAHOO.Bubbling, $html = Alfresco.util.encodeHTML;

   var TASK_EVENTCLASS = Alfresco.util.generateDomId(null, "task");

   beCPG.component.ProjectCommons = {};
   beCPG.component.ProjectCommons.prototype = {

      cache : [],
   

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

         var editDetails = new Alfresco.module.SimpleDialog(this.id + "-editCharacts-" + Alfresco.util.generateDomId());

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
      getTaskAdvancementClass : function PL_getAdvancementClass(task) {

         if (task["itemData"]["prop_pjt_tlState"].value == "Completed") {
            return "advancement-done";
         }

         var dates = this.extractDates(task, null, true), now = new Date();

         if (now.getTime() == dates.end.getTime()) {
            return "overdue-0to9";
         }
         if (now.getTime() > dates.end.getTime()) {
            return "overdue-20to29";
         }

         return "overdue-negative";
      },
      getTaskAdvancement : function PL_getTaskAdvancement(task) {

         if (task["itemData"]["prop_pjt_tlState"].value == "Completed") {
            return this.msg("overdue.complete");
         }
         var dates = this.extractDates(task, null, true), now = new Date();

         return Math.floor((now.getTime() - dates.end.getTime()) / (24 * 60 * 60 * 1000)) + this.msg("overdue.day");

      },
      getOverdueClass : function PL_getOverdueClass(project, size) {
         var percent = 0, overdue = project.itemData["prop_pjt_projectOverdue"], dates = this.extractDates(project), suffix = size != null ? "-" + size
               : "";

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
      extractDates : function(record, start, isTask) {

         var startDate = null, endDate = null, dueDate = null;

         if (isTask) {
            startDate = record["itemData"]["prop_pjt_tlStart"].value;
            endDate = record["itemData"]["prop_pjt_tlEnd"].value;

            endDate = endDate != null ? this.resetDate(Alfresco.util.fromISO8601(endDate)) : null;
            startDate = startDate != null ? this.resetDate(Alfresco.util.fromISO8601(startDate)) : this
                  .resetDate(start);

            if (endDate == null) {
               var duration = record["itemData"]["prop_pjt_tlDuration"].value;
               if (duration == null) {
                  var tlIsMilestone = record["itemData"]["prop_pjt_tlIsMilestone"].value;
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

         startDate = record.itemData["prop_pjt_projectStartDate"].value;
         endDate = record.itemData["prop_pjt_projectCompletionDate"].value;
         dueDate = record.itemData["prop_pjt_projectDueDate"].value;

         startDate = startDate != null ? this.resetDate(Alfresco.util.fromISO8601(startDate)) : new Date();
         endDate = endDate != null ? this.resetDate(Alfresco.util.fromISO8601(endDate)) : null;
         dueDate = dueDate != null ? this.resetDate(Alfresco.util.fromISO8601(dueDate)) : this.computeDueDate(
               startDate, record);
         return {
            start : startDate,
            end : endDate,
            due : dueDate
         };

      },
      computeDueDate : function(start, record) {
         var taskList = record.itemData["dt_pjt_taskList"];
         var ret = start, vstart = start;
         for (j in taskList) {
            var task = taskList[j];
            var taskId = task.nodeRef;

            var tdates = this.cache[taskId];
            if (!tdates) {
               for ( var z in task["itemData"]["assoc_pjt_tlPrevTasks"]) {
                  var precTaskId = task["itemData"]["assoc_pjt_tlPrevTasks"][z].value;

                  if (this.cache[precTaskId] != null && this.cache[precTaskId].end != null && this.cache[precTaskId].end
                        .getTime() > vstart.getTime()) {
                     vstart = this.cache[precTaskId].end;
                  }

               }

               tdates = this.extractDates(task, vstart, true);
               this.cache[taskId] = tdates;
            }

            ret = tdates.end;

         }
         return ret;
      },

      resetDate : function PL_resetDate(date) {
         if (date == null) {
            return new Date();
         }

         date.setHours(0);
         date.setMinutes(0);
         date.setSeconds(0);
         return date;
      },

      getTaskTitle : function PL_getTaskTitle(task, entityNodeRef, full, start, large) {
         var ret = '<span class="' + this.getTaskAdvancementClass(task) + (large ? "-32" : "") + '" title="' + this
               .getTaskAdvancement(task) + '">';

         if (full && this.taskLegends) {
            ret += '<span class="task-legend" style="background-color:#' + this.getTaskColor(task) + '" ></span>';
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
            ret += '<span class="doc-file"><a title="' + this.msg("link.title.open-document") + '" href="' +  beCPG.util.entityDetailsURL(contents[0].siteId,contents[0].value, "document") + '"><img src="' + Alfresco.constants.URL_RESCONTEXT + 'components/images/filetypes/' + Alfresco.util
                  .getFileIcon(contents[0].displayValue, "cm:content", 16) + '" /></a></span>';
         }

         ret += '<span class="node-' + deliverable.nodeRef + '|' + entityNodeRef + '"><a class="theme-color-1 ' + TASK_EVENTCLASS + '" title="' + this
               .msg("link.title.deliverable-edit") + '" >' + deliverable["itemData"]["prop_pjt_dlDescription"].displayValue + '</a></span>';

         ret += "</span>";

         return ret;
      },
      

      getPriorityImg : function PL_getPriorityImg(record, large) {
         var priority = record.itemData["prop_pjt_projectPriority"].value, priorityMap = {
            "1" : "high",
            "2" : "medium",
            "3" : "low"
         }, priorityKey = priorityMap[priority + ""];
         return '<img class="priority" src="' + Alfresco.constants.URL_RESCONTEXT + 'components/images/priority-' + priorityKey + '-16.png" title="' + this
               .msg("label.priority", this.msg("priority." + priorityKey)) + '"/>';
      },

      getProjectTitle : function PL_getProjectTitle(record, full, large) {
         var url = null, urlFolder = null, version = "";

         var title = record.itemData["prop_cm_name"].displayValue, code = record.itemData["prop_bcpg_code"].displayValue, 
         overdue = '', ret = "";

         detailsUrl = beCPG.util.entityDetailsURL(record.siteId, record.nodeRef);
         url = beCPG.util.entityCharactURL(record.siteId, record.nodeRef);
         urlFolder = beCPG.util.entityDocumentsURL(record.siteId, record.path, title);

         if (record.version && record.version !== "") {
            version = '<span class="document-version">' + record.version + '</span>';
         }

         if (full) {
            overdue = record.itemData["prop_pjt_projectOverdue"].displayValue != null ? $html(record.itemData["prop_pjt_projectOverdue"].displayValue) + '&nbsp;' + this
                  .msg("overdue.day") : "";
            ret += '<span class="' + this.getOverdueClass(record, large ? 32 : null) + '" title="' + overdue + '">';

         } else {
            ret += '<span class="project-title"><a class="folder-link" href="' + urlFolder + '" title="' + this
                  .msg("link.title.open-folder") + '">&nbsp;</a>';
         }
         
         if (full) {
            ret += this.getPriorityImg(record, large);
         }

         ret += '<a class="theme-color-1" href="' + (full ?detailsUrl : url) + '" title="' + this.msg("link.title.open-project") + '">' + code + "&nbsp;-&nbsp;" + $html(title) + '</a></span>' + version;

         return ret;
      },

      getTaskColor : function PL_getTaskColor(task) {
         var id = task["itemData"]["assoc_pjt_tlTaskLegend"][0].value;

         for (i in this.taskLegends) {
            if (this.taskLegends[i].id == id) {
               return this.taskLegends[i].color;
            }
         }

      },
      initTaskHandlers : function PL_initGantt() {
         var me = this;
         var fnOnShowTaskHandler = function PL__fnOnShowTaskHandler(layer, args) {
            var owner = YAHOO.Bubbling.getOwnerByTagName(args[1].anchor, "span");
            if (owner !== null) {
               me.onActionShowTask.call(me, owner.className, owner);
            }
            return true;
         };
         YAHOO.Bubbling.addDefaultAction(TASK_EVENTCLASS, fnOnShowTaskHandler);
      }

   };
})();