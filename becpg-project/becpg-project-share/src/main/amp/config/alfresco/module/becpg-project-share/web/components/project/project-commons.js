(function() {

   var Bubbling = YAHOO.Bubbling, $html = Alfresco.util.encodeHTML;

   var TASK_EVENTCLASS = Alfresco.util.generateDomId(null, "task");
   var COMMENT_EVENTCLASS = Alfresco.util.generateDomId(null, "comment");

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
                     Alfresco.constants.URL_SERVICECONTEXT + "components/form?formId=simple-form&entityNodeRef={entityNodeRef}&itemKind={itemKind}&itemId={itemId}&mode={mode}&submitType={submitType}&showCancelButton=true&popup=true",
                     {
                        itemKind : "node",
                        itemId : nodes[0],
                        mode : "edit",
                        submitType : "json",
                        entityNodeRef : nodes[1] != "#access_forbidden" ? nodes[1] : ""
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
    
      onActionCommentTask : function PL_onActionShowTask(className) {
         var nodes = className.replace("node-", "").split("|")
         , entityNodeRef =  nodes[1] != "#access_forbidden" ? nodes[1] : "";

         var url = Alfresco.constants.URL_SERVICECONTEXT + "modules/comments/list?nodeRef=" + nodes[0] + "&activityType=task&entityNodeRef="+entityNodeRef;

         this._showPanel(url ,this.id+"_comments", nodes[1]);
         

      },
      getOverdueClass : function PL_getOverdueClass(project, size) {
         var percent = 0, overdue = project.itemData["prop_pjt_projectOverdue"], dates = this.extractDates(project), suffix = size != null ? "-" + size
               : "";

         if (overdue.value != null && dates.start != null && dates.due != null) {
            percent = 100 * (overdue.value / (dates.due.getTime() - dates.start.getTime())) * 24 * 60 * 60 * 1000;
         }

         if (percent > 45) {
            return "overdue-45plus" + suffix;
         }
         if (percent > 30) {
            return "overdue-30to45" + suffix;
         }
         if (percent > 15) {
            return "overdue-15to29" + suffix;
         }
         if (percent > 0) {
            return "overdue-0to14" + suffix;
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
                  if (record["itemData"]["prop_pjt_tlIsMilestone"] != null && record["itemData"]["prop_pjt_tlIsMilestone"].value) {
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

      getTaskTitle : function PL_getTaskTitle(task, entityNodeRef, start, large) {

          var ret = '<span class="task-status task-status-' + task["itemData"]["prop_pjt_tlState"].value + '">', duration ='';

          ret += '<span class="node-' + task.nodeRef + '|' + entityNodeRef + '"><a class="theme-color-1 ' + TASK_EVENTCLASS + '" title="' + this
          .msg("link.title.task-edit") + '" >' + task["itemData"]["prop_pjt_tlTaskName"].displayValue;

          if( task["itemData"]["prop_pjt_tlState"].value == "InProgress"){
              if(task["itemData"]["prop_pjt_completionPercent"] && 
                      task["itemData"]["prop_pjt_completionPercent"].value != null)  {
                  duration += '<span title="' + this.msg("completion.title") + '">' + task["itemData"]["prop_pjt_completionPercent"].displayValue + '%</span>';
              }
    
              if(task["itemData"]["prop_pjt_tlRealDuration"] && task["itemData"]["prop_pjt_tlRealDuration"].value!=null)  {
                  if(duration.length>0){
                      duration+=" - ";
                  }
    
                  var className = "";
                  if(task["itemData"]["prop_pjt_tlRealDuration"].value > task["itemData"]["prop_pjt_tlDuration"].value){
                  	duration += '<span class="red" title="' + this.msg("overdue.title") + '">' + Alfresco.util.encodeHTML(task["itemData"]["prop_pjt_tlRealDuration"].value - task["itemData"]["prop_pjt_tlDuration"].value)+" "+this
                     .msg("overdue.day")+ '</span>';
                  }
              } 
    
              if(duration.length>0){
                  ret +=' ('+duration+')';
              }
          }

          ret += '</a></span>';

          if (task["itemData"]["prop_pjt_tlWorkflowInstance"] && task["itemData"]["prop_pjt_tlWorkflowInstance"].value) {
              ret += '<a class="task-link" title="' + this.msg("link.title.open-workflow") + '" href="' + Alfresco.constants.URL_PAGECONTEXT + 'workflow-details?workflowId=' + task["itemData"]["prop_pjt_tlWorkflowInstance"].value + '&referrer=project-list&myWorkflowsLinkBack=true' + '" >&nbsp;</a>';
          }

          ret += '<span class="node-' + task.nodeRef + '|' + entityNodeRef + '">';
          ret += '<a class="task-comments '+COMMENT_EVENTCLASS+'" title="' + this.msg("link.title.comment-task") + '" href="" >';

          if (task["itemData"]["prop_fm_commentCount"] && task["itemData"]["prop_fm_commentCount"].value) {
              ret += task["itemData"]["prop_fm_commentCount"].value;
          } else {
              ret +="&nbsp;";
          }
          ret += "</a></span></span>";

          return ret;
      },
      getDeliverableTitle : function PL_getDeliverableTitle(deliverable, entityNodeRef) {

         var ret = '<span class="delivrable-status delivrable-status-' + deliverable["itemData"]["prop_pjt_dlState"].value + '">';

         var contents = deliverable["itemData"]["assoc_pjt_dlContent"];

         if (contents.length > 0) {
            ret += '<span class="doc-file"><a title="' + this.msg("link.title.open-document") + '" href="' +  beCPG.util.entityURL(contents[0].siteId,contents[0].value, "document") + '"><img src="' + Alfresco.constants.URL_RESCONTEXT + 'components/images/filetypes/' + Alfresco.util
                  .getFileIcon(contents[0].displayValue, "cm:content", 16) + '" /></a></span>';
         }
         
         var url =  deliverable["itemData"]["prop_pjt_dlUrl"]!=null ? deliverable["itemData"]["prop_pjt_dlUrl"].value : null;
         if(url!=null && url.length>0){
             ret += '<span class="doc-url"><a title="' + this.msg("link.title.open-link") + '" href="' + url  + '"><img src="' + Alfresco.constants.URL_RESCONTEXT + 'components/images/link-16.png" /></a></span>';
         }
         

         ret += '<span class="node-' + deliverable.nodeRef + '|' + entityNodeRef + '"><a class="theme-color-1 ' + TASK_EVENTCLASS + '" title="' + this
               .msg("link.title.deliverable-edit") + '" >' + deliverable["itemData"]["prop_pjt_dlDescription"].displayValue + '</a></span>';

         ret += '<span class="node-' + deliverable.nodeRef + '|' + entityNodeRef + '">';
         ret += '<a class="task-comments '+COMMENT_EVENTCLASS+'" title="' + this.msg("link.title.comment-task") + '" href="" >';
         if (deliverable["itemData"]["prop_fm_commentCount"] && deliverable["itemData"]["prop_fm_commentCount"].value) {
            ret += deliverable["itemData"]["prop_fm_commentCount"].value;
         } else {
            ret +="&nbsp;";
         }
         ret += "</a></span>";
         
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
         var propertiesUrl = null, dataListUrl = null, folderUrl = null, version = "";

         var title = record.itemData["prop_cm_name"].displayValue, code = record.itemData["prop_bcpg_code"].displayValue, 
         overdue = '', ret = "";

         propertiesUrl = beCPG.util.entityURL(record.siteId, record.nodeRef,"pjt:project", null, "View-properties");
         dataListUrl = beCPG.util.entityURL(record.siteId, record.nodeRef,"pjt:project");
         folderUrl = beCPG.util.entityDocumentsURL(record.siteId, record.path, title);

         if (record.version && record.version !== "") {
            version = '<span class="document-version">' + record.version + '</span>';
         }

         if (full) {
            overdue = record.itemData["prop_pjt_projectOverdue"].displayValue != null ? $html(record.itemData["prop_pjt_projectOverdue"].displayValue) + '&nbsp;' + this
                  .msg("overdue.day") : "";
            ret += '<span class="' + this.getOverdueClass(record, large ? 32 : null) + '" title="' + overdue + '">';

         } else {
            ret += '<span class="project-title"><a class="folder-link" href="' + folderUrl + '" title="' + this
                  .msg("link.title.open-folder") + '">&nbsp;</a>';
         }
         
         if (full) {
            ret += this.getPriorityImg(record, large);
         }

         ret += '<a class="theme-color-1" href="' + (full ? propertiesUrl : dataListUrl) + '" title="' + (full ? "" : this.msg("actions.entity.view-tasks")) + '">' + code + "&nbsp;-&nbsp;" + $html(title) + '</a></span>' + version;

         return ret;
      },

      getTaskColor : function PL_getTaskColor(task) {
      	if(task["itemData"]["assoc_pjt_tlTaskLegend"][0] != null){
      		var id = task["itemData"]["assoc_pjt_tlTaskLegend"][0].value;

            for (i in this.taskLegends) {
               if (this.taskLegends[i].id == id) {
                  return this.taskLegends[i].color.replace('#','');
               }
            }
      	}
         return '006600';
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
         
         
         var fnOnCommentTaskHandler = function PL__fnOnShowTaskHandler(layer, args) {
            var owner = YAHOO.Bubbling.getOwnerByTagName(args[1].anchor, "span");
            if (owner !== null) {
               me.onActionCommentTask.call(me, owner.className, owner);
            }
            return true;
         };
         YAHOO.Bubbling.addDefaultAction(COMMENT_EVENTCLASS, fnOnCommentTaskHandler);
         
      }

   };
})();
