(function()
{

    if (beCPG.module.GanttViewRendererHelper)
    {

        YAHOO.Bubbling
                .fire(
                        "registerGanttRenderer",
                        {
                            typeName : "pjt:taskList",
                            ganttInitialiser : function(callBack)
                            {

                                var url = Alfresco.constants.PROXY_URI + "becpg/project/info?" + (this.options.siteId != null && this.options.siteId.length > 0 ? "site=" + this.options.siteId 
                                		+ (this.options.entityNodeRef != null  ? "&entityNodeRef=" + this.options.entityNodeRef: "")
                                        : (this.options.entityNodeRef != null  ? "entityNodeRef=" + this.options.entityNodeRef: ""));

                                Alfresco.util.Ajax.request(
                                {
                                    url : url,
                                    successCallback :
                                    {
                                        fn : function(response)
                                        {
                                            var data = response.json.legends,html="";

                                            for ( var i in data)
                                            {
                                                var taskLegend =
                                                {
                                                    id : data[i].nodeRef,
                                                    label : data[i].label,
                                                    color : data[i].color!=null ? data[i].color.replace('#', '') : "FFBC00"
                                                };

                                                this.taskLegends.push(taskLegend);
                                                
                                                html += '<span class="task-legend" style="background-color:#' + taskLegend.color + '" ></span><span>' + taskLegend.label +'</span>&nbsp;';
                                            }
                                            
                                            Dom.get(this.id + "-legend").innerHTML = html;

                                            var fnDrawGantt = function PL_onReady_fnDrawGantt()
                                            {

                                                var recordSet = this.widgets.dataTable.getRecordSet();
                                                if (recordSet.getLength() != 0)
                                                {
                                                    g = new JSGantt.GanttChart('g', Dom.get(this.id + "-gantt"), g != null ? g
                                                            .getFormat() : null, true);
                                                    g.setDateInputFormat("mediumDate");
                                                    g.setDateDisplayFormat("mediumDate");
                                                    g.setCaptionType('Resource');
                                                    var start = new Date();
                                                    var previousTask = null;
                                                    for (var i = 0; i < recordSet.getLength(); i++)
                                                    {
                                                        var oRecord = recordSet.getRecord(i);
                                                        var task = oRecord.getData();
                                                        var taskId = task.nodeRef;
                                                        var precTaskIds = "";
                                                       
                                                        var pParent = 0;
                                                        var pSubProject = null;

                                                        if (task["itemData"]["prop_bcpg_parentLevel"].value != null)
                                                        {
                                                            pParent = task["itemData"]["prop_bcpg_parentLevel"].value;
                                                        } else if(task["itemData"].isMultiLevel){
                                                        	pParent = previousTask;
                                                        } 
                                                       
                                                        
                                                        if(!task["itemData"].isMultiLevel){
                                                        	previousTask = taskId;
                                                        }
                                                        
                                                        
                                                        if (task["itemData"]["assoc_pjt_subProjectRef"] != null
                                                        		&& task["itemData"]["assoc_pjt_subProjectRef"] .length>0)
                                                        {
                                                        	pSubProject = task["itemData"]["assoc_pjt_subProjectRef"][0].displayValue;
                                                        }
                                                        
                                                        var pGroupOpen = 1;
                                                        
                                                        if (!task["itemData"]["open"])
                                                        {
                                                        	pGroupOpen = 0;
                                                        }	
                                                        
                                                        var pGroup = !task["itemData"]["prop_pjt_tlIsGroup"].value ? 0 : 1;
                                                        

                                                        for ( var z in task["itemData"]["assoc_pjt_tlPrevTasks"])
                                                        {
                                                            var precTaskId = task["itemData"]["assoc_pjt_tlPrevTasks"][z].value;
                                                            if (precTaskIds.length > 0)
                                                            {
                                                                precTaskIds += ",";
                                                            }
                                                            precTaskIds += precTaskId;

                                                            if (this.cache[precTaskId] != null && this.cache[precTaskId].end != null && this.cache[precTaskId].end
                                                                    .getTime() > start.getTime())
                                                            {
                                                                start = this.cache[precTaskId].end;
                                                            }

                                                        }

                                                        var tlIsMilestone = task["itemData"]["prop_pjt_tlIsMilestone"].value;
                                                        var tlPercent = task["itemData"]["prop_pjt_completionPercent"].value;
														var tlIsCritical =task["itemData"]["prop_pjt_tlIsCritical"]!=null ? task["itemData"]["prop_pjt_tlIsCritical"].value : false;
                                                        
                                                        var taskOwner = null;
                                                        
                                                        if(task["itemData"]["assoc_pjt_tlResources"].length>0){
                                                        	taskOwner = "";
                                                        	for(var z in  task["itemData"]["assoc_pjt_tlResources"]){
                                                        		if(task["itemData"]["assoc_pjt_tlResources"][z].displayValue!=null){
                                                        			taskOwner += '<span class="resource-title">' + task["itemData"]["assoc_pjt_tlResources"][z].displayValue.replace("null", "").trim() + '</span>';   
                                                        		}
                                                        	}
                                                        }
                                                        

                                                        var tdates = this.cache[taskId];
                                                        if (!tdates)
                                                        {
                                                            tdates = this.extractDates(task, start, true);
                                                            this.cache[taskId] = tdates;
                                                        }


														var taskItem = new JSGantt.TaskItem(taskId, this.getTaskTitle(task,
                                                                this.options.entityNodeRef), tdates.start,  
                                                                tdates.end, this.getTaskColor(task), null, (tlIsMilestone ? 1 : 0),
                                                                taskOwner, tlPercent, pGroup, pParent, pGroupOpen, precTaskIds,null,
																task.color  , pSubProject, tdates.targetStart, tdates.targetEnd,
																tlIsCritical ? 1 : 0);


                                                        g.AddTaskItem(taskItem);
                                                    }

                                                    this.refreshView();

                                                }
                                                else
                                                {
                                                    Alfresco.util
                                                            .populateHTML([
                                                                    this.id + "-gantt",
                                                                    "<div class=\"yui-dt-liner\">" + this.msg("message.empty") + "</div>" ]);
                                                }
                                            };

                                            this.extraAfterDataGridUpdate.push(fnDrawGantt);
                                            this.initTaskHandlers();
                                            
                                            callBack.call(this);

                                        },
                                        scope : this
                                    }
                                });

                            }

                        });
        


    }
})();
