(function() {

	
   var Bubbling = YAHOO.Bubbling, $html = Alfresco.util.encodeHTML, $siteURL = Alfresco.util.siteURL;

   var TASK_EVENTCLASS = Alfresco.util.generateDomId(null, "task");
   var SUBMITTASK_EVENTCLASS = Alfresco.util.generateDomId(null, "submitTask");
   var SHOWDETAILS_EVENTCLASS = Alfresco.util.generateDomId(null, "showDetails");
   var COMMENT_EVENTCLASS = Alfresco.util.generateDomId(null, "comment");
   var COMMENT_PROJECTEVENTCLASS = Alfresco.util.generateDomId(null, "commentProject");

   beCPG.component.ProjectCommons = {};
   beCPG.component.ProjectCommons.prototype = {

      cache : [],
      taskEventClass: TASK_EVENTCLASS,
      taskSubmitEventClass: SUBMITTASK_EVENTCLASS,
      commentEventClass : COMMENT_EVENTCLASS,
      commentProjectEventClass : COMMENT_PROJECTEVENTCLASS,
      showDetailsEventClass :  SHOWDETAILS_EVENTCLASS,
      

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
                        entityNodeRef : nodes[1] != "#access_forbidden" ? nodes[1] : ""
                     });

         // Using Forms Service, so always create new instance

         var editDetails = new Alfresco.module.SimpleDialog(this.id + "-editCharacts-" + Alfresco.util.generateDomId());

         editDetails.setOptions({
            width : "34em",
            templateUrl : templateUrl,
            actionUrl : null,
            destroyOnHide : true,
            zIndex: 10,
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
      
      onActionSubmitTask : function onActionSubmitTask(className) {
    	  var nodes = className.replace("node-", "").split("|")
          , taskNodeRef =  nodes[0] != "#access_forbidden" ? nodes[0] : "", me = this;
    	  
    	  Alfresco.util.PopupManager.displayPrompt({
              title : me.msg("message.confirm.submit-task.title"),
              text : me.msg("message.confirm.submit-task.description"),
              buttons : [ {
                 text : me.msg("button.submit"),
                 handler : function() {
                	 Alfresco.util.Ajax
                     .jsonRequest({
                        url : Alfresco.constants.PROXY_URI + "becpg/project/complete/task?nodeRef="+taskNodeRef,
                        method : "POST",
                        successCallback : {
                           fn : function(response) {
                              Alfresco.util.PopupManager.displayMessage({
                                 text : me.msg("message.submit-task.success")
                              });
                              
                              YAHOO.Bubbling.fire(me.scopeId + "dataItemUpdated", {
									nodeRef : taskNodeRef
								});
                              
                           },
                           scope : me
                        }
                     });  
                     this.destroy(); 
                 }
              }, {
                 text : me.msg("button.cancel"),
                 handler : function() {
                    this.destroy();
                 },
                 isDefault : true
              } ]
           });
    	  
          

       },
       
      
      onActionCommentProject : function PL_onActionShowTask(className) {
          var node = className.replace("node-", "");
          node = (node != "#access_forbidden" ? node : "");

          var url = Alfresco.constants.URL_SERVICECONTEXT + "modules/comments/list?nodeRef=" + node + "&activityType=project&entityNodeRef="+node;

          this._showPanel(url ,this.id+"_comments", node);
          

       },
       
       onActionShowProjectDetails : function PL_onActionShowDetails(item) {
         
           var url = Alfresco.constants.URL_SERVICECONTEXT + "modules/project-details/project-details?nodeRef=" + item.nodeRef ;

           this._showPanel(url ,this.id+"_projectDetails", item.nodeRef);

        },
        
        onActionShowProjectFolder : function PL_onActionShowProjectFolder(record){

            var folderUrl = beCPG.util.entityDocumentsURL(record.siteId, record.path, record.itemData["prop_cm_name"].displayValue);      
            
        	window.location.href = folderUrl;
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

         var startDate = null, endDate = null, dueDate = null,targetStartDate = null, targetEndDate= null;

         if (isTask) {
            startDate = record["itemData"]["prop_pjt_tlStart"].value;
            endDate = record["itemData"]["prop_pjt_tlEnd"]!=null ?record["itemData"]["prop_pjt_tlEnd"].value:null;
 			dueDate = record["itemData"]["prop_pjt_tlDue"]!=null ?record["itemData"]["prop_pjt_tlDue"].value: null;
			targetStartDate = record["itemData"]["prop_pjt_tlTargetStart"]!=null ?  record["itemData"]["prop_pjt_tlTargetStart"].value : null;
			targetEndDate = record["itemData"]["prop_pjt_tlTargetEnd"]!=null ? record["itemData"]["prop_pjt_tlTargetEnd"].value : null;

 			targetStartDate = targetStartDate != null ? this.resetDate(Alfresco.util.fromISO8601(targetStartDate)) : null;
			targetEndDate = targetEndDate != null ? this.resetDate(Alfresco.util.fromISO8601(targetEndDate)) : null;
            endDate = endDate != null ? this.resetDate(Alfresco.util.fromISO8601(endDate)) : null;
			dueDate = dueDate != null ? this.resetDate(Alfresco.util.fromISO8601(dueDate)) : null;
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
               end : endDate,
			   due: dueDate,
			   targetStart : targetStartDate,
   			   targetEnd : targetEndDate
            };

         }

         startDate = record.itemData["prop_pjt_projectStartDate"].value;
         endDate = record.itemData["prop_pjt_projectCompletionDate"].value;
         dueDate = record.itemData["prop_pjt_projectDueDate"].value;
		 targetStartDate =  record.itemData["prop_pjt_tlTargetStart"]!=null ?  record.itemData["prop_pjt_tlTargetStart"].value : null;
		 targetEndDate = record.itemData["prop_pjt_tlTargetEnd"]!=null ? record.itemData["prop_pjt_tlTargetEnd"].value : null;

         startDate = startDate != null ? this.resetDate(Alfresco.util.fromISO8601(startDate)) : new Date();
         endDate = endDate != null ? this.resetDate(Alfresco.util.fromISO8601(endDate)) : null;
         dueDate = dueDate != null ? this.resetDate(Alfresco.util.fromISO8601(dueDate)) : this.computeDueDate(
               startDate, record);
		targetStartDate = targetStartDate != null ? this.resetDate(Alfresco.util.fromISO8601(targetStartDate)) : null;
		targetEndDate = targetEndDate != null ? this.resetDate(Alfresco.util.fromISO8601(targetEndDate)) : null;
         return {
            start : startDate,
            end : endDate,
            due : dueDate,
			targetStart : targetStartDate,
   			targetEnd : targetEndDate
         };

      },
      computeDueDate : function(start, record) {
         var taskList = record.itemData["dt_pjt_taskList"];
         var ret = start, vstart = start;
         for (var j in taskList) {
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

      getTaskTitle : function PL_getTaskTitle(task, entityNodeRef, showLegend, size, hideState, hideDuration) {
    	  var subProject = null, subProjectClass = "";
    	  
    	  if (task["itemData"]["assoc_pjt_subProjectRef"] != null
            		&& task["itemData"]["assoc_pjt_subProjectRef"] .length>0)
            {
          	   subProject = task["itemData"]["assoc_pjt_subProjectRef"][0];
          	   subProjectClass = " sub-project";
            }  
    	  
    	  var classGroup = (subProject==null && task["itemData"]["prop_pjt_tlIsGroup"]!=null && task["itemData"]["prop_pjt_tlIsGroup"].value ) ? " task-group" : "";

    	  var legend = "";
    	  
    	  if(showLegend){
    		 var color =   this.getTaskColor(task);
    		 legend = '<span class="task-legend" style="background-color:#'+color+'"></span>';
    	  }
    	  
    	  
          var ret = legend, duration ='';

		  if(!hideState){
			 ret += '<span class="task-status task-status-' + task["itemData"]["prop_pjt_tlState"].value +classGroup+subProjectClass+ '">';
		  }          

          var text = $html(task["itemData"]["prop_pjt_tlTaskName"].displayValue);
          
          if(task.permissions.userAccess.edit ){
        	  
        	  if(size && size > -1 && text.length>size){
        		  ret += '<span class="node-' + (subProject!=null ? subProject.value : task.nodeRef) + '|' + entityNodeRef 
        		  + ' text-tooltip se" data-tooltip="'+ beCPG.util.encodeAttr(text) +'"><a href="" class="theme-color-1 ' + TASK_EVENTCLASS + '" title="' + this
		          .msg("link.title.task-edit") + '" >' + $html(text.substring(0,size).trim())+"..." +"</span>";
        	  } else {
        		  ret += '<span class="node-' + (subProject!=null ? subProject.value : task.nodeRef) + '|' + entityNodeRef + '"><a href="" class="theme-color-1 ' + TASK_EVENTCLASS + '" title="' + this
		          .msg("link.title.task-edit") + '" >' + text +"</span>";
        	  }
          } else {
        	  if(size && size > -1 && text.length>size){
        		   ret += '<span class="node-' + (subProject!=null ? subProject.value : task.nodeRef) + '|' + entityNodeRef 
    	    		    + ' text-tooltip se" data-tooltip="'+ beCPG.util.encodeAttr(text) +'"><span>'
    	    		  +$html(text.substring(0,size).trim())+"..."+'</span></span>';
        	  } else {
	        	  ret += '<span class="node-' + (subProject!=null ? subProject.value : task.nodeRef) + '|' + entityNodeRef + '">' + text +"</span>";
        	  }
          }
		
		if(!hideDuration){
		          
		          if( task["itemData"]["prop_pjt_tlState"].value == "InProgress"){
		              if(task["itemData"]["prop_pjt_completionPercent"] && 
		                      task["itemData"]["prop_pjt_completionPercent"].value != null)  {
		                  duration += '<span title="' + this.msg("completion.title") + '">' + task["itemData"]["prop_pjt_completionPercent"].displayValue + '%</span>';
		              }              
		          }
		          
		          if(task["itemData"]["prop_pjt_tlRealDuration"] && task["itemData"]["prop_pjt_tlRealDuration"].value!=null &&
		         		 task["itemData"]["prop_pjt_tlRealDuration"].value > task["itemData"]["prop_pjt_tlDuration"].value)  {             
		         	 if(duration.length>0){
		         		 duration+=" - ";
		         	 }
		         	 duration += '<span class="red" title="' + this.msg("overdue.title") + '">' + $html(task["itemData"]["prop_pjt_tlRealDuration"].value - task["itemData"]["prop_pjt_tlDuration"].value)+" "+this
		             .msg("overdue.day")+ '</span>';
		         }
		} 

         if(task.permissions.userAccess.edit){
        	 ret += '</a>';
         }

         if(!hideDuration && duration.length>0){
             ret +=' ('+duration+')';
         }
         
         if (task["itemData"]["prop_pjt_tlWorkflowTaskInstance"] && task["itemData"]["prop_pjt_tlWorkflowTaskInstance"].value
	       		 && task["itemData"]["prop_pjt_tlWorkflowTaskInstance"].value!=''
         	){
	       		ret += '<a href="';
	      	    ret +=  $siteURL('task-edit?taskId=' + task["itemData"]["prop_pjt_tlWorkflowTaskInstance"].value+  '&referrer=project-list') + '" class="workflow-task-link" title="' + this.msg("link.title.open-workflow")+'">';
	            ret +='&nbsp;</a>';
	      } else if (task["itemData"]["prop_pjt_tlWorkflowInstance"] && task["itemData"]["prop_pjt_tlWorkflowInstance"].value
	       		 && task["itemData"]["prop_pjt_tlWorkflowInstance"].value!=''){
	       		ret += '<a href="';
	      	    ret +=  $siteURL('workflow-details?workflowId=' + task["itemData"]["prop_pjt_tlWorkflowInstance"].value+  '&referrer=project-list') + '" class="workflow-task-link" title="' + this.msg("link.title.open-workflow")+'">';
	            ret +='&nbsp;</a>';
	      }
	     
          

          if (subProject!=null)
          {
             ret += '<a  class="sub-project-link" title="' + subProject.displayValue + '" href="'+
             	beCPG.util.entityURL(subProject.siteId, subProject.value,"pjt:project") +'" >';
             ret +="&nbsp;</a>";
          } else {
	          if(task.permissions.userAccess.edit && task["itemData"]["prop_pjt_tlState"].value == "InProgress" && classGroup == "" ){
		          ret += '<span class="node-' + task.nodeRef + '|' + entityNodeRef + '">';
		          ret += '<a class="submit-task '+SUBMITTASK_EVENTCLASS+'" title="' + this.msg("link.title.submit-task") + '" href="" >';
		          ret +="&nbsp;";
		          ret += "</a></span>";
	          }
          }
       

          var commentRef = 'node-' + task.nodeRef + '|' + entityNodeRef;
          
          if (subProject!=null) {
        	  commentRef = 'node-' + subProject.value + '|' + entityNodeRef;
          }
          
          ret += '<span class="'+commentRef+'">';
 

          if (task["itemData"]["prop_fm_commentCount"] && task["itemData"]["prop_fm_commentCount"].value) {
        	  ret += '<a class="task-comments active-comments '+COMMENT_EVENTCLASS+'" title="' + this.msg("link.title.comment-task") + '" href="" >';
              ret += task["itemData"]["prop_fm_commentCount"].value;
          } else {
        	  ret += '<a class="task-comments '+COMMENT_EVENTCLASS+'" title="' + this.msg("link.title.comment-task") + '" href="" >';
              ret +="&nbsp;";
          }
          ret += "</a></span>";

		if(!hideState){
			ret +="</span>";
		}
          

          return ret;
      },
      getDeliverableTitle : function PL_getDeliverableTitle(deliverable, entityNodeRef) {

         var ret = '<span class="delivrable-status delivrable-status-' + deliverable["itemData"]["prop_pjt_dlState"].value + '">';

         var contents = deliverable["itemData"]["assoc_pjt_dlContent"]? deliverable["itemData"]["assoc_pjt_dlContent"].value : null

         if (contents!=null && contents.length > 0) {
            ret += '<span class="doc-file"><a title="' + this.msg("link.title.open-document") + '" href="' +  beCPG.util.entityURL(contents[0].siteId,contents[0].value, "document") + '"><img src="' + Alfresco.constants.URL_RESCONTEXT + 'components/images/filetypes/' + Alfresco.util
                  .getFileIcon(contents[0].displayValue, "cm:content", 16) + '" /></a></span>';
         }
         
         var url =  deliverable["itemData"]["prop_pjt_dlUrl"]!=null ? deliverable["itemData"]["prop_pjt_dlUrl"].value : null;
         if(url!=null && url.length>0){
             ret += '<span class="doc-url"><a title="' + this.msg("link.title.open-link") + '" href="' + url  + '"><img src="' + Alfresco.constants.URL_RESCONTEXT + 'components/images/link-16.png" /></a></span>';
         }
         
         if(deliverable.permissions && deliverable.permissions.userAccess.edit){
	         ret += '<span class="node-' + deliverable.nodeRef + '|' + entityNodeRef + '"><a class="theme-color-1 ' + TASK_EVENTCLASS + '" title="' + this
	               .msg("link.title.deliverable-edit") + '" >' + deliverable["itemData"]["prop_pjt_dlDescription"].displayValue + '</a></span>';
         } else {
        	  ret += '<span class="node-' + deliverable.nodeRef + '|' + entityNodeRef + '">' + deliverable["itemData"]["prop_pjt_dlDescription"].displayValue + '</span>';
         }

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
      

       getPriorityImg: function PL_getPriorityImg(record, large) {
           if (record.itemData["prop_pjt_projectPriority"]) {
               var priority = record.itemData["prop_pjt_projectPriority"].value, priorityMap = {
                   "1": "high",
                   "2": "medium",
                   "3": "low"
               }, priorityKey = priorityMap[priority + ""];
               if (priorityKey) {
                   return '<img class="priority" src="' + Alfresco.constants.URL_RESCONTEXT + 'components/images/priority-' + priorityKey + '-16.png" title="' + this
                       .msg("label.priority", this.msg("priority." + priorityKey)) + '"/>';
               }
           }
           return "";
       },

      getProjectTitle : function PL_getProjectTitle(record, full, large) {
         var propertiesUrl = null, dataListUrl = null, folderUrl = null, version = "";

         var title = record.itemData["prop_cm_name"].displayValue, code = record.itemData["prop_bcpg_code"]!=null ? record.itemData["prop_bcpg_code"].displayValue : "", 
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
      
      getProjectTitleV2 : function PL_getProjectTitle(record, full, oColumn) {
    	  

          var title = record.itemData["prop_cm_name"].displayValue, code = record.itemData["prop_bcpg_code"]!=null ? record.itemData["prop_bcpg_code"].displayValue : "", 
          overdue = '', ret = "", state = record.itemData["prop_pjt_projectState"].value;

          var size = oColumn.tooltip!=null ? oColumn.tooltip : 100;
          
          var displayTitle = code + "-" + $html(title);
          
          if(oColumn.pattern!=null){
        	  //TODO Do it better
        	  displayTitle=oColumn.pattern.replace("{cm:name}",$html(title)).replace("{bcpg:code}",code);
          }
          
          var light = "light";
          if(full){
        	  light="full"; 
          }
          
          if(size && displayTitle!=null && displayTitle.length>size){
    		  ret += '<span class="project-title text-tooltip se '+light+' project-status-'+state+'" data-tooltip="'+ beCPG.util.encodeAttr(displayTitle)+'" >';
    		  displayTitle = $html(displayTitle.substring(0,size).trim())+"...";
    	  } else {
    	    	ret += '<span class="project-title '+light+' project-status-'+state+'"  >';
      	  }
          
          ret += '<a class="theme-color-1" href="' + beCPG.util.entityURL(record.siteId, record.nodeRef,"pjt:project") + '" >' + displayTitle + 
            '</a>' ;
          
          ret += '<span class="node-' + record.nodeRef +'">';
          ret += '<a class="show-details '+SHOWDETAILS_EVENTCLASS+'" title="' + this.msg("link.title.project-details") + '" href="" >';
          ret +="&nbsp;";
          ret += "</a></span>"
          
          ret += '<span class="node-' + record.nodeRef + '">';
          
          if (record["itemData"]["prop_fm_commentCount"] && record["itemData"]["prop_fm_commentCount"].value) {
        	  ret += '<a class="project-comments active-comments '+COMMENT_PROJECTEVENTCLASS+'" title="' + this.msg("link.title.comment-project") + '" href="" >';
        	  ret += record["itemData"]["prop_fm_commentCount"].value;
          } else {
        	  ret += '<a class="project-comments '+COMMENT_PROJECTEVENTCLASS+'" title="' + this.msg("link.title.comment-project") + '" href="" >';
        	  ret +="&nbsp;";
          }      
          
          ret += "</a></span></span>";
         
          
          if (record.version && record.version !== "") {
        	  ret += '<span class="document-version">' + record.version + '</span>';
           }
           
          if (full) {
        	  
        	  var projectTitle = record.itemData["prop_cm_title"]
        	  
        	  ret +="<ul>"
        		  
        	 if(projectTitle!=null &&projectTitle.displayValue!=null && projectTitle.displayValue.length>0){
        		 
        		  if(size && projectTitle.displayValue.length>(size+10)){
        			  ret += '<li><span class="project-subtitle text-tooltip se"  data-tooltip="'+ beCPG.util.encodeAttr(projectTitle.displayValue)+'">'
        			   + $html(projectTitle.displayValue.substring(0,(size+10)).trim()) +'...</span></li>';   
            	  } else {
        			 ret += '<li><span class="project-subtitle">' + $html(projectTitle.displayValue) + '</span></li>';   
            	  }
        	 }	  
        		  
        	  var dates = this.extractDates(record), end = dates.due;
              var  dateLine = (dates.start ? Alfresco.util.formatDate(dates.start, "shortDate") : scope
                    .msg("label.none"));

              if (dates.end != null) {
                 end = dates.end;
              }
              dateLine += " - ";
              
              dateLine += (dates.start ? Alfresco.util.formatDate(end, "shortDate") : scope
                    .msg("label.none"));
    		   
              ret+="<li>";
              
              overdue = record.itemData["prop_pjt_projectOverdue"];
              if(overdue!=null && overdue.value!=null){
            	  ret +=  '<span class="small ' + this.getOverdueClass(record,32) + '" title="'+$html(overdue.value)+ '&nbsp;' + this.msg("overdue.day")+'"></span>';
              }
              
              ret += '<span class="project-date">' + dateLine + '</span>';    
              
             
              if(overdue!=null && overdue.value!=null  && overdue.value > 0){ 
            	 ret += '&nbsp;<span class="project-overdue">('+ $html(overdue.value)+ '&nbsp;' + this.msg("overdue.day")+')</span>';
              }
              ret += '</li>';
              var progress =record.itemData["prop_pjt_completionPercent"];
              if(progress!=null){
              
            	  ret += "<li><progress max='100' value='"+progress.value+"' >'"+$html(progress.value)+ "&nbsp; %</progress></li>";
   			
              }
              ret +="</ul>"

          } 
          
          return ret;
       },

      getTaskColor : function PL_getTaskColor(task) {
      	if(task["itemData"]["assoc_pjt_tlTaskLegend"][0] != null){
      		var id = task["itemData"]["assoc_pjt_tlTaskLegend"][0].value;

            for (var i in this.taskLegends) {
               if (this.taskLegends[i].id == id) {
                  return this.taskLegends[i].color.replace('#','');
               }
            }
      	}
         return '37C700';
      },
      initTaskHandlers : function PL_initTaskHandlers() {
         var me = this;
         var fnOnShowTaskHandler = function PL__fnOnShowTaskHandler(layer, args) {
            var owner = YAHOO.Bubbling.getOwnerByTagName(args[1].anchor, "span");
            if (owner !== null) {
               me.onActionShowTask.call(me, owner.className, owner);
            }
            return true;
         };
         
         YAHOO.Bubbling.addDefaultAction(TASK_EVENTCLASS, fnOnShowTaskHandler);
         
         var fnOnSubmitTaskHandler = function PL__fnOnSubmitTaskHandler(layer, args) {
             var owner = YAHOO.Bubbling.getOwnerByTagName(args[1].anchor, "span");
             if (owner !== null) {
                me.onActionSubmitTask.call(me, owner.className, owner);
             }
             return true;
          };
          YAHOO.Bubbling.addDefaultAction(SUBMITTASK_EVENTCLASS, fnOnSubmitTaskHandler);
          
         var fnOnCommentTaskHandler = function PL__fnOnShowTaskHandler(layer, args) {
            var owner = YAHOO.Bubbling.getOwnerByTagName(args[1].anchor, "span");
            if (owner !== null) {
               me.onActionCommentTask.call(me, owner.className, owner);
            }
            return true;
         };
         YAHOO.Bubbling.addDefaultAction(COMMENT_EVENTCLASS, fnOnCommentTaskHandler);
         
         var fnOnCommentProjectHandler = function PL__fnOnCommentProjectHandler(layer, args) {
            var owner = YAHOO.Bubbling.getOwnerByTagName(args[1].anchor, "span");
            if (owner !== null) {
               me.onActionCommentProject.call(me, owner.className, owner);
            }
            return true;
         };
         YAHOO.Bubbling.addDefaultAction(COMMENT_PROJECTEVENTCLASS, fnOnCommentProjectHandler);
         
         
         var fnOnShowDetailsHandler = function PL__fnOnShowDetailsHandler(layer, args) {
             var owner = YAHOO.Bubbling.getOwnerByTagName(args[1].anchor, "span");
             if (owner !== null) {
            	 var node = owner.className.replace("node-", "");
                 me.onActionShowProjectDetails.call(me, {nodeRef:node});
             }
             return true;
          };
          YAHOO.Bubbling.addDefaultAction(SHOWDETAILS_EVENTCLASS, fnOnShowDetailsHandler)
    
      }

   };
})();
