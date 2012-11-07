(function() {

	if (beCPG.module.EntityDataGridRenderers) {

		var $html = Alfresco.util.encodeHTML;

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
			   return '<span class="' + scope.getAdvancementClass(oRecord,null,32) + '">&nbsp;</span>';
		   }
		});

		YAHOO.Bubbling.fire("registerDataGridRenderer", {
		   propertyName : "cm:name",
		   renderer : function(oRecord, data, label, scope) {

			   return scope.getProjectTitle(oRecord);

		   }
		});

		YAHOO.Bubbling.fire("registerDataGridRenderer", {
		   propertyName : "pjt:taskList",
		   renderer : function(oRecord, data, label, scope, idx, length) {
			   var projectSteps = "";
			   
			   var oData = oRecord.getData();
            
			   if (data["itemData"]["prop_pjt_tlState"].value == "InProgress") {
				   projectSteps += scope.getTaskTitle(data, oData.nodeRef);
			   }

			   return projectSteps;
		   }
		});

		YAHOO.Bubbling.fire("registerDataGridRenderer", {
		   propertyName : "pjt:deliverableList",
		   renderer : function(oRecord, data, label, scope, idx, length) {
		   	var oData = oRecord.getData();
		   	
			   return scope.getDeliverableTitle(data,oData.nodeRef);

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

			   var dates = scope.extractDates(oRecord), end = dates.due;

			   var desc = '<span class="start">'
			         + (dates.start ? Alfresco.util.formatDate(dates.start, "shortDate") : scope.msg("label.none"))
			         + '</span>';

			   if (dates.end != null) {
				   end = dates.end;
			   }

			   desc += '<br/><span class="' + (dates.end != null ? "end" : "due") + '">'
			         + (end != null ? Alfresco.util.formatDate(end, "shortDate") : scope.msg("label.none")) + "</span>";

			   return desc;

		   }

		});

	}
})();
