(function() {

	if (beCPG.module.EntityDataGridRenderers) {
	

		var $html = Alfresco.util.encodeHTML;

		YAHOO.Bubbling.fire("registerDataGridRenderer", {
		   propertyName : [ "pjt:projectEntity", "pjt:parentProjectRef" ],
		   renderer : function(oRecord, data, label, scope, i, ii, elCell, oColumn) {
			   if(data.value!=null){
				   var url = beCPG.util.entityURL(data.siteId, data.value), version = "";
	
				   var size = 100;
				   if(oColumn.tooltip){
					   size = oColumn.tooltip;
				   }
				   
				   if (data.version && data.version !== "") {
					   version = '<span class="document-version">' + data.version + '</span>';
				   }
				   return '<span class="' + data.metadata + '"><a href="' + url + '">' + beCPG.util.createTextTooltip(data.displayValue, size) + '</a></span>' + version;
			   }
			   return "";
		   }

		});				

		YAHOO.Bubbling.fire("registerDataGridRenderer", {
			   propertyName : "cm:person_pjt:projectManager",
			   renderer : function(oRecord, data, label, scope) { 

                   if(scope.options.columnFormId != "datagrid-simple"){
                	   var ret = "";
                	   var resource = oRecord.getData("itemData")["assoc_pjt_projectManager"];
	                   if (resource && resource[0]) {
	                      ret += '<div class="project-manager avatar" title="' + resource[0].displayValue + '">';
	                      ret += Alfresco.Share.userAvatar(resource[0].metadata, 32) +"&nbsp;" +Alfresco.util.userProfileLink(data.metadata, data.displayValue);
	                      ret += "</div>";
	                   }
	                   return ret;
                   } else {
                	   return '<span class="person">' + Alfresco.util.userProfileLink(data.metadata, data.displayValue) + '</span>';
                   }
			   }
			});


		
		YAHOO.Bubbling.fire("registerDataGridRenderer", {
		   propertyName : "cm:name",
		   renderer : function(oRecord, data, label, scope, i, ii, elCell, oColumn) { 
			   return scope.getProjectTitleV2(oRecord.getData(),scope.options.columnFormId != "datagrid-simple",oColumn);
		   }
		});
		
		

	   YAHOO.Bubbling.fire("registerDataGridRenderer", {
		   propertyName : "pjt:taskList",
		   renderer : function(oRecord, data, label, scope, idx, length, elCell, oColumn) {
			   var oData = oRecord.getData();
			   if(data["itemData"]){
				  
				   if (idx == 0) {
					   
					   var size = 100;
					   if(oColumn.tooltip){
						   size = oColumn.tooltip;
					   }
					   
					   var tasks = oRecord.getData("itemData")["dt_pjt_taskList"];

					   tasks.sort(function (a, b){
	                       if(a!=null && a.sort!=null	
	                       		&& b!=null && b.sort!=null	){
	                       	return a.sort - b.sort;
	                       }
	                   	return 0;
	                   });
					   
					   
					   var moreTasksHtlm = "";
					   var taskHtlm = "<ul>";
					   var count =0;
					   for (j in tasks) {
						   var task = tasks[j];
						   count++;
						   
						    var padding ="";
						    if(task["itemData"]["prop_bcpg_parentLevel"].value!=null){
						    	padding='style="padding-left:10px;"'
						    }
						   
						 	if(count>4){
						   		moreTasksHtlm += "<li "+padding+">" + scope.getTaskTitle(task, oData.nodeRef,true) + "</li>";
						   	} else {
						   		taskHtlm += "<li "+padding+">" + scope.getTaskTitle(task, oData.nodeRef,true,size) + "</li>";
						   	}
					   }
					   taskHtlm += "</ul>";

					   if (moreTasksHtlm.length > 0) {
						   taskHtlm += '<div class="more-task"><div class="onActionShowMore">' + '<a href="#" class="' + scope.id
						         + '-show-more show-more" title="' + scope.msg("tasks.more") + '">' + '<span>'
						         + scope.msg("tasks.more") + '</span></a></div>' + ' <div class="more-actions hidden"><ul>'
						         + moreTasksHtlm + '</ul></div></div>';
					   }

					   return taskHtlm;

				   }

				   return null;
			       
			       
			   }
		   }
		});

        YAHOO.Bubbling.fire("registerDataGridRenderer", {
            propertyName: "bcpg:activityList",
            renderer: function(__oRecord, data, __label, __scope, __idx, __length, __elCell, oColumn) {
                if (data["itemData"]) {
                    var user = data["itemData"]["prop_bcpg_alUserId"];
                    var alData = data["itemData"]["prop_bcpg_alData"] ? data["itemData"]["prop_bcpg_alData"] : null;
                    var html = "";
                    if (alData != null && alData.title) {
                        html += '<div class="project-activity-details">';
                        html += '   <div class="icon" title="' + user.displayValue + '">' + Alfresco.Share.userAvatar(user.value, 32) + '</div>';
                        html += '   <div class="details">';
                        if (alData.content) {
                            if (oColumn.tooltip) {
                                html += '      <div class="activity-content" >' + beCPG.util.createTextTooltip(alData.content, oColumn.tooltip) + '</div>';
                            } else {
                                html += '      <div class="activity-content">' + (alData.content) + '</div>';
                            }
                        }
                        html += '   </div>';
                        html += '   <div class="clear"></div>';
                        html += '</div>';
                    }
                    return html;
                }
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
				   
				   deliverables.sort(function (a, b){
                       if(a!=null && a.sort!=null	
                       		&& b!=null && b.sort!=null	){
                       	return a.sort - b.sort;
                       }
                   	return 0;
                   });
				   
				   
				   for (j in deliverables) {
					   var deliverable = deliverables[j], state = deliverable["itemData"]["prop_pjt_dlState"].value, 
					   scriptOrder  = deliverable["itemData"]["prop_pjt_dlScriptExecOrder"].value;
					 
					   if (state != "Planned" && (scriptOrder==null || scriptOrder.length == 0  || scriptOrder=="None" )) {
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
			   return scope.getPriorityImg(oRecord.getData(), false);
		   }

		});

		YAHOO.Bubbling.fire("registerDataGridRenderer", {
		   propertyName : "pjt:projectStartDate",
		   renderer : function(oRecord, data, label, scope) {

			   var dates = scope.extractDates(oRecord.getData()), end = dates.due;

			   var desc = '<span class="start">'
			         + (dates.start ? Alfresco.util.formatDate(dates.start, "shortDate") : scope.msg("label.none"))
			         + '</span>';

			   if (dates.end != null) {
				   end = dates.end;
			   }

			   desc += '&nbsp;-&nbsp;<span class="' + (dates.end != null ? "end" : "due") + '">'
			         + (end != null ? Alfresco.util.formatDate(end, "shortDate") : scope.msg("label.none")) + "</span>";

			   return desc;

		   }

		});
		
		YAHOO.Bubbling.fire("registerDataGridRenderer", {
			   propertyName : "pjt:projectOverdue",
			   renderer : function(oRecord, data, label, scope) {
				   var overdue = data.displayValue != null ? $html(data.displayValue)+ '&nbsp;' + scope.msg("overdue.day") : '';
				   return '<span class="center ' + scope.getOverdueClass(oRecord.getData(), 32) + '" title="'+overdue+'">' + overdue + '</span>';
			   }
			});
		
		  YAHOO.Bubbling.fire("registerDataGridRenderer", {
		      propertyName : ["pjt:slScore","pjt:projectScore"],
		      renderer : function(oRecord, data, label, scope, i, ii, elCell, oColumn) {

		    	Dom.setStyle(elCell, "width", "25px");
				Dom.setStyle(elCell.parentNode, "width", "25px");
				if (oColumn.hidden) {
					scope.widgets.dataTable.showColumn(oColumn);
					Dom.removeClass(elCell.parentNode, "yui-dt-hidden");
				} 
		    	  
		      	var className="";
		      	if(data.value != null){
		      		if(data.value <= 0){
			      		className="score-black";
			      	}
		      		 else if(data.value < 25){
		      			className="score-red";
		      		}
		      		else if(data.value < 50){
		      			className="score-orange";
		      		}
		      		else if(data.value < 75){
		      			className="score-blue";
		      		}
		      		else{
		      			className="score-green";
		      		}
		      	}
		      	return '<span class="' + className + '" >' + Alfresco.util.encodeHTML(data.displayValue) + '</span>';         
		      }
		   });

		   
		   YAHOO.Bubbling.fire("registerDataGridRenderer", {
				propertyName : [ "pjt:completionPercent" ],
				renderer : function(oRecord, data, label, scope) {
					return "<progress max='100' value='"+data.value+"' >'"+$html(data.displayValue)+ "&nbsp; %</progress>";
				}
			});
		   

		   YAHOO.Bubbling.fire("registerDataGridRenderer", {
		      propertyName : [ "pjt:tlTaskName" ],
		      renderer : function(oRecord, data, label, scope) {
		    	  var record = oRecord.getData();
		    	  
		    	  
		      	return scope.getTaskTitle(record, record.itemData["dt_pjt_project"]!=null ? record.itemData["dt_pjt_project"].nodeRef : "" ,true);
		      }

		   });
		   

		   YAHOO.Bubbling.fire("registerDataGridRenderer", {
		        propertyName : [ "pjt:tlState", "pjt:dlState" ],
		        renderer : function(oRecord, data, label, scope) {
		           return '<span class="' + "task-" + data.value.toLowerCase() + '" title="' + data.displayValue + '" />';
		        }

		     });
		   
		   
		   YAHOO.Bubbling.fire("registerDataGridRenderer", {
			   propertyName : "pjt:project",
			   renderer : function(oRecord, data, label, scope, idx, length, elCell, oColumn) {
					   var project = oRecord.getData("itemData")["dt_pjt_project"];
					   return scope.getProjectTitleV2(project, false , oColumn);
			   }
			});
		   
		   
		   YAHOO.Bubbling.fire("registerDataGridRenderer", {
			   propertyName : "pjt:tlResources",
			   renderer : function(oRecord, data, label, scope, idx, length) {
					  return '<span class="resource-title"  >' + Alfresco.util.encodeHTML(data.displayValue) + '</span>';
			   }
			});
		
	}
})();
