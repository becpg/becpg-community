(function() {

	if (beCPG.module.EntityDataGridRenderers) {

		var $html = Alfresco.util.encodeHTML;

		YAHOO.Bubbling.fire("registerDataGridRenderer", {
		   propertyName : [ "pjt:projectEntity" ],
		   renderer : function(oRecord, data, label, scope) {

			   var url = scope._buildCellUrl(data), version = "";

			   if (data.version && data.version !== "") {
				   version = '<span class="document-version">' + data.version + '</span>';
			   }

			   return '<span class="' + data.metadata + '" ><a href="' + url + '">' + $html(data.displayValue)
			         + '</a></span>' + version;

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

			   var oData = oRecord.getData();

			   if (data["itemData"]["prop_pjt_tlState"].value == "InProgress") {
				  return scope.getTaskTitle(data, oData.nodeRef, true);
			   }

			   return null;
		   }
		});

		YAHOO.Bubbling.fire("registerDataGridRenderer", {
		   propertyName : "pjt:deliverableList",
		   renderer : function(oRecord, data, label, scope, idx, length) {
			   var oData = oRecord.getData();

			   var deliverables = oRecord.getData("itemData")["dt_pjt_deliverableList"];

			   var moreDeliverablesHtlm = "";
			   var deliverableHtlm = "<ul>";

			   if (idx == 0) {
				   for (j in deliverables) {
					   var deliverable = deliverables[j], state = deliverable["itemData"]["prop_pjt_dlState"].value;
					   if (state != "Planned") {
					   	if(state == "Closed"){
					   		moreDeliverablesHtlm += "<li>" + scope.getDeliverableTitle(deliverable, oData.nodeRef) + "</li>";
					   	} else {
					   		deliverableHtlm += "<li>" + scope.getDeliverableTitle(deliverable, oData.nodeRef) + "</li>";
					   	}
					   }
				   }
				   deliverableHtlm += "</ul>";

				   if (moreDeliverablesHtlm.length > 0) {
					   deliverableHtlm += '<div class="more-deliverable"><div class="onActionShowMore">' + '<a href="#" class="' + scope.id
					         + '-show-more show-more" title="' + scope.msg("deliverables.more") + '">' + '<span>'
					         + scope.msg("deliverables.more") + '</span></a></div>' + ' <div class="more-actions hidden"><ul>'
					         + moreDeliverablesHtlm + '</ul></div></div>';
				   }

				   return deliverableHtlm;

			   }

			   return null;

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
		
		YAHOO.Bubbling.fire("registerDataGridRenderer", {
			   propertyName : "pjt:projectOverdue",
			   renderer : function(oRecord, data, label, scope) {
				   var overdue = data.displayValue != null ? $html(data.displayValue)+ '&nbsp;' + scope.msg("overdue.day") : '';
				   return '<span class="center ' + scope.getOverdueClass(oRecord, 32) + '" title="'+overdue+'">' + overdue + '</span>';
			   }
			});
		
		YAHOO.Bubbling.fire("registerDataGridRenderer", {
			   propertyName : ["pjt:projectScore","pjt:completionPercent"],
			   renderer : function(oRecord, data, label, scope) {
				   return (data.value !=null) ? $html(data.displayValue)+ '&nbsp; %' : '';
			   }
			});
	}
})();
