(function() {

	if (beCPG.module.EntityDataGridRenderers) {

		var $html = Alfresco.util.encodeHTML, $siteURL = Alfresco.util.siteURL;

		YAHOO.Bubbling.fire("registerDataGridRenderer", {
		   propertyName : [ "pjt:projectEntity" ],
		   renderer : function(oRecord, data, label, scope) {

			   var url = null, version = "";

			   if (data.siteId) {
				   url = Alfresco.constants.URL_PAGECONTEXT + "site/" + data.siteId + "/" + 'entity-data-lists?nodeRef='
				         + data.value;
			   } else {
				   url = Alfresco.constants.URL_PAGECONTEXT + 'entity-data-lists?nodeRef=' + data.value;
			   }

			   if (data.version && data.version !== "") {
				   version = '<span class="document-version">' + data.version + '</span>';
			   }

			   return '<span class="' + data.metadata + '" ><a href="' + url + '">' + $html(data.displayValue)
			         + '</a></span>' + version;

		   }

		});

		YAHOO.Bubbling.fire("registerDataGridRenderer", {
		   propertyName : "pjt:projectHierarchy1",
		   renderer : function(oRecord, data, label, scope) {
			   return '<span class="' + scope.getAdvancementClass(oRecord) + '">&nbsp;</span>';
		   }
		});

		YAHOO.Bubbling.fire("registerDataGridRenderer", {
		   propertyName : "cm:name",
		   renderer : function(oRecord, data, label, scope) {

			   return scope.getProjectTitle(oRecord);

		   }
		});

		YAHOO.Bubbling.fire("registerDataGridRenderer", {
		   propertyName : "pjt:taskHistoryList",
		   renderer : function(oRecord, data, label, scope, idx , length) {
			    var projectSteps = idx == 0 ? "<ul>" : "";

			   if (data["itemData"]["prop_pjt_thlState"].value == "InProgress") {

				   projectSteps += '<li>' + scope.getTaskTitle(data) + '</li>';

			   }
			   
			   projectSteps +=  idx == length-1 ? "</ul>" : "";

			   return (projectSteps ? projectSteps : scope.msg("label.none"));
		   }
		});

		YAHOO.Bubbling.fire("registerDataGridRenderer", {
		   propertyName : "pjt:deliverableList",
		   renderer : function(oRecord, data, label, scope, idx , length) {

			    var livrables = idx == 0 ? "<ul>" : "";
			   livrables += '<li>' + scope.getDeliverableTitle(data) + '</li>';

			    livrables +=  idx == length-1 ? "</ul>" : "";

			   return livrables;

		   }
		});

		YAHOO.Bubbling.fire("registerDataGridRenderer", {
		   propertyName : "pjt:projectPriority",
		   renderer : function(oRecord, data, label, scope) {
			   var priority = oRecord.getData("itemData")["prop_pjt_projectPriority"].value, priorityMap = {
			      "1" : "high",
			      "2" : "medium",
			      "3" : "low"
			   }, priorityKey = priorityMap[priority + ""], desc = '<img src="' + Alfresco.constants.URL_RESCONTEXT
			         + 'components/images/priority-' + priorityKey + '-16.png" title="'
			         + scope.msg("label.priority", scope.msg("priority." + priorityKey)) + '"/>';
			   return desc;

		   }

		});

		YAHOO.Bubbling.fire("registerDataGridRenderer", {
		   propertyName : "pjt:projectStartDate",
		   renderer : function(oRecord, data, label, scope) {

			   var dates = scope.extractDates(oRecord);

			   var desc = '<span class="start">'
			         + (dates.start ? Alfresco.util.formatDate(dates.start, "shortDate") : scope.msg("label.none"))
			         + '</span>';

			   var end = dates.due;
			   if (dates.end != null) {
				   end = dates.end;
			   }

			   desc += '<br/><span class="' + dates.end != null ? "end" : "due" + '">'
			         + (end ? Alfresco.util.formatDate(end, "shortDate") : scope.msg("label.none")) + "</span>";

			   return desc;

		   }

		});

		// renderTitle : function PL_renderTitle(elCell, oRecord, oColumn, oData)
		// {
		// var workflow = oRecord.getData();
		// var message = workflow.message;
		// if (message === null) {
		// message = this.msg("workflow.no_message");
		// }
		// elCell.innerHTML = message;
		// },
		// renderOwner : function PL_renderOwner(elCell, oRecord, oColumn, oData)
		// {
		// var workflow = oRecord.getData();
		// elCell.innerHTML = workflow.initiator.firstName + " " +
		// workflow.initiator.lastName;
		// },
		// renderAvancement : function PL_renderAvancement(elCell, oRecord,
		// oColumn,
		// oData) {
		// var workflow = oRecord.getData();
		//
		// elCell.innerHTML = '<span class="'
		// + this.getAdvancementClass(workflow)
		// + '">&nbsp;</span>';
		//        
		//        
		// },
		// renderProjectNumber : function PL_renderProjectNumber(elCell, oRecord,
		// oColumn, oData) {
		// var workflow = oRecord.getData();
		// var projectNumber =
		// workflow.tasks[0].properties.projectwf_projectNumber;
		//
		// elCell.innerHTML = '<span><a href="'
		// + $siteURL('workflow-details?workflowId=' + workflow.id
		// + '&referrer=workflows&myWorkflowsLinkBack=true') + '"
		// class="theme-color-1" title="'
		// + this.msg("link.viewWorkflow") + '">'
		// + $html(projectNumber ? projectNumber : this.msg("label.none")) +
		// '</a></span>';
		//        
		//        
		// },
		// renderProjectProductName : function PL_renderProjectProductName(elCell,
		// oRecord, oColumn, oData) {
		// var workflow = oRecord.getData();
		// var projectProductName =
		// workflow.tasks[0].properties.projectwf_projectProductName;
		// elCell.innerHTML = '<a class="task-details"
		// title="'+this.msg("link.title.task-details")+'"
		// href="'+$siteURL('/task-details?taskId='+workflow.tasks[0].id+'&referrer=workflows')+'">
		// </a>'
		// +(projectProductName ? projectProductName : this.msg("label.none"));
		//		     
		// },
		// renderProjectStatus : function PL_renderProjectStatus(elCell, oRecord,
		// oColumn, oData) {
		// var workflow = oRecord.getData();
		// var projectStatus =
		// workflow.tasks[0].properties.projectwf_projectStatus;
		// elCell.innerHTML = '<div class="projectStatus"
		// style="background-color:#'+this.getProjectStatusColor(workflow.tasks[0])+'"
		// ></div>';
		// },
		//

		// renderDeliverables : function PL_renderDeliverables(elCell, oRecord,
		// oColumn, oData){
		//     	
		//     	
		// var randomnumber=Math.floor(Math.random()*7);
		// elCell.innerHTML = '<ul><li><span class="doc-file"
		// style="background-image:url(/share/res/components/images/states/valid-16.png)"></span><span
		// class="doc-file"
		// style="background-image:url(/share/res/components/images/filetypes/doc-file-16.png)"><a
		// class="theme-color-1">Barquette plastique 100x200</a></span></li>';
		//     	
		//     	 
		// for(var i = 0 ; i <randomnumber ; i++){
		//     		 
		//     		
		// elCell.innerHTML += '<li><span class="doc-file"
		// style="background-image:url(/share/res/components/images/states/'+(i%3==0?'valid':'stop')+'-16.png)"></span><span
		// class="doc-file"
		// style="background-image:url(/share/res/components/images/filetypes/xls-file-16.png)"><a
		// class="theme-color-1">Doc test '+i+'</a></span></li>';
		// }
		// elCell.innerHTML +="</ul>";
		//     	
		// },
		// renderAssignedUsers : function PL_renderAssignedUsers(elCell, oRecord,
		// oColumn, oData) {
		// var workflow = oRecord.getData();
		//
		// var projectAsignedUsers = "";
		//
		// for (i in workflow.tasks) {
		//
		// if (workflow.tasks[i].state == "IN_PROGRESS" && workflow.tasks[i].owner
		// !=
		// null) {
		// if (projectAsignedUsers != "")
		// projectAsignedUsers += ", ";
		// projectAsignedUsers += workflow.tasks[i].owner.firstName + " " +
		// workflow.tasks[i].owner.lastName;
		// }
		// }
		//
		// elCell.innerHTML = (projectAsignedUsers ? projectAsignedUsers :
		// this.msg("label.none"));
		// },
		// renderProjectType : function PL_renderProjectType(elCell, oRecord,
		// oColumn, oData) {
		// var workflow = oRecord.getData();
		// var projectType = (workflow.tasks[0].properties.projectwf_projectType ?
		// workflow.tasks[0].properties.projectwf_projectType
		// : this.msg("label.none"));
		//
		// if (elCell == null) {
		// return projectType;
		// }
		// elCell.innerHTML = projectType;
		// },
		// renderComments : function PL_renderComments(elCell, oRecord, oColumn,
		// oData) {
		// var workflow = oRecord.getData();
		//
		// // var projectComments = workflow.tasks[0].properties.bpm_comment;
		// var projectComments = null;
		//
		// for (i in workflow.tasks) {
		//
		// if (workflow.tasks[i].name == "projectwf:validate-analysis") {
		// projectComments = workflow.tasks[i].properties.bpm_comment;
		// // break; we want the last comment
		// }
		// }
		//
		// elCell.innerHTML = (projectComments ? projectComments :
		// this.msg("label.none"));
		// },
		// renderDueDate : function PL_renderDueDate(elCell, oRecord, oColumn,
		// oData)
		// {
		// var workflow = oRecord.getData();
		// var dueDate = workflow.dueDate ?
		// Alfresco.util.fromISO8601(workflow.dueDate) : null;
		// elCell.innerHTML = (dueDate ? Alfresco.util.formatDate(dueDate,
		// "shortDate") : this.msg("label.none"));
		// },

	}
})();
