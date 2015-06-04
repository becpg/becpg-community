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

                                var url = Alfresco.constants.PROXY_URI + "becpg/project/info" + (this.options.siteId != null && this.options.siteId.length > 0 ? "?site=" + this.options.siteId
                                        : "");

                                Alfresco.util.Ajax.request(
                                {
                                    url : url,
                                    successCallback :
                                    {
                                        fn : function(response)
                                        {
                                            var data = response.json.legends;

                                            for ( var i in data)
                                            {
                                                var taskLegend =
                                                {
                                                    id : data[i].nodeRef,
                                                    label : data[i].label,
                                                    color : data[i].color.replace('#', '')
                                                };

                                                this.taskLegends.push(taskLegend);
                                            }

                                            var fnDrawGantt = function PL_onReady_fnDrawGantt()
                                            {

                                                var recordSet = this.widgets.dataTable.getRecordSet();
                                                if (recordSet.getLength() != 0)
                                                {
                                                    g = new JSGantt.GanttChart('g', Dom.get(this.id + "-gantt"), g != null ? g
                                                            .getFormat() : 'day');
                                                    g.setDateInputFormat("mediumDate");
                                                    g.setDateDisplayFormat("mediumDate");
                                                    g.setCaptionType('Resource');
                                                    var start = new Date();
                                                    for (var i = 0; i < recordSet.getLength(); i++)
                                                    {
                                                        var oRecord = recordSet.getRecord(i);
                                                        var task = oRecord.getData();
                                                        var taskId = task.nodeRef;
                                                        var precTaskIds = "";

                                                        var pParent = 0;

                                                        if (task["itemData"]["prop_bcpg_parentLevel"].value != null)
                                                        {
                                                            pParent = task["itemData"]["prop_bcpg_parentLevel"].value;
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

                                                        var taskOwner = task["itemData"]["assoc_pjt_tlResources"].length > 0 ? ('<span class="resource-title">' + task["itemData"]["assoc_pjt_tlResources"][0].displayValue + '</span>')
                                                                : null;

                                                        var tdates = this.cache[taskId];
                                                        if (!tdates)
                                                        {
                                                            tdates = this.extractDates(task, start, true);
                                                            this.cache[taskId] = tdates;
                                                        }

                                                        g.AddTaskItem(new JSGantt.TaskItem(taskId, this.getTaskTitle(task,
                                                                this.options.entityNodeRef, tdates.start), tdates.start,
                                                                tdates.end, this.getTaskColor(task), null, tlIsMilestone ? 1 : 0,
                                                                taskOwner, tlPercent, pGroup, pParent, 1, precTaskIds,null,task.color));
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