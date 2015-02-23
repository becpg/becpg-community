/*******************************************************************************
 * Copyright (C) 2010-2014 beCPG.
 * 
 * This file is part of beCPG
 * 
 * beCPG is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * beCPG is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

var g; // uggly gantt var

(function()
{

    /**
     * TaskListView constructor.
     * 
     * @param htmlId
     *            {String} The HTML id of the parent element
     * @return {beCPG.component.TaskListView} The new TaskListView instance
     * @constructor
     */
    beCPG.component.TaskListView = function(htmlId)
    {

        beCPG.component.TaskListView.superclass.constructor.call(this, htmlId);

        YAHOO.Bubbling.on("viewModeChange", this.onViewModeChange, this);
        
        YAHOO.Bubbling.on("dataItemUpdated", this.initGantt, this);
        YAHOO.Bubbling.on("dataItemsDeleted", this.initGantt, this);
        YAHOO.Bubbling.on("dataItemCreated", this.initGantt, this);

        return this;
    };

    /**
     * Extend from Alfresco.component.Base
     */
    YAHOO.extend(beCPG.component.TaskListView, beCPG.module.EntityDataGrid);

    /**
     * Augment prototype with Actions module
     */
    YAHOO.lang.augmentProto(beCPG.component.TaskListView, beCPG.component.ProjectCommons);

    /**
     * Augment prototype with main class implementation, ensuring overwrite is
     * enabled
     */
    YAHOO.lang
            .augmentObject(
                    beCPG.component.TaskListView.prototype,
                    {

                        view : "dataTable",

                        /**
                         * State definition
                         */
                        taskLegends : [],

                        onReady : function()
                        {
                            JSGantt.register(this);

                            var me = this;

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

                                            me.taskLegends.push(taskLegend);
                                        }

                                        me.initGantt();

                                        beCPG.component.TaskListView.superclass.onReady.call(me);
                                        

                                        this.initTaskHandlers();

                                    },
                                    scope : this
                                }
                            });

                        },

                        onViewModeChange : function TaskListView_onViewModeChange()
                        {

                            if (this.view == "dataTable")
                            {
                                this.view = "gantt";
                                Dom.addClass(this.id + "-datagridBarBottom","hidden");
                                Dom.addClass(this.id + "-itemSelect-div","hidden");
                                Dom.addClass(this.id + "-grid", "hidden");
                                Dom.removeClass(this.id + "-gantt", "hidden");
                               this.refreshGantt();
                                
                            } else if( this.view == "gantt"){
                                this.view = "dataTable";
                                Dom.addClass(this.id + "-gantt", "hidden");
                                Dom.removeClass(this.id + "-grid", "hidden");
                                Dom.removeClass(this.id + "-datagridBarBottom","hidden");
                                Dom.removeClass(this.id + "-itemSelect-div","hidden");
                              
                            }

                        },

                        initGantt : function PL_initGantt()
                        {

                            var fnDrawGantt = function PL_onReady_fnDrawGantt()
                            {
                                var recordSet = this.widgets.dataTable.getRecordSet();
                                if (recordSet.getLength() != 0)
                                {
                                    g = new JSGantt.GanttChart('g', Dom.get(this.id + "-gantt"), g!=null ? g.getFormat() : 'day');
                                    g.setDateInputFormat("shortDate");
                                    g.setDateDisplayFormat("shortDate");
                                    g.setCaptionType('Resource');
                                    var start = new Date();
                                    for (var i = 0; i < recordSet.getLength(); i++)
                                    {
                                        var oRecord = recordSet.getRecord(i);
                                        var task = oRecord.getData();
                                        var taskId = task.nodeRef;
                                        var precTaskIds = "";
                                        
                                        var pParent = 0;
                                        
                                        if(task["itemData"]["prop_bcpg_parentLevel"].value!=null ){
                                            pParent = task["itemData"]["prop_bcpg_parentLevel"].value;
                                        }
                                        var pGroup = !task["itemData"]["prop_pjt_tlIsGroup"].value? 0 : 1;
                                        
                                        for ( var z in task["itemData"]["assoc_pjt_tlPrevTasks"])
                                        {
                                            var precTaskId = task["itemData"]["assoc_pjt_tlPrevTasks"][z].value;
                                            if (precTaskIds.length > 0)
                                            {
                                                precTaskIds += ",";
                                            }
                                            precTaskIds += precTaskId;

                                            if (this.cache[precTaskId] != null && this.cache[precTaskId].end != null && this.cache[precTaskId].end
                                                    .getTime() > start.getTime() )
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
                                                this.options.entityNodeRef, tdates.start), tdates.start, tdates.end,
                                                this.getTaskColor(task), null, tlIsMilestone ? 1 : 0, taskOwner,
                                                tlPercent, pGroup,  pParent, 1, precTaskIds));
                                    }
                                    
                                    this.refreshGantt();
                                    
                                }
                                else
                                {
                                    Alfresco.util.populateHTML([ this.id + "-gantt",
                                            "<div class=\"yui-dt-liner\">" + this.msg("message.empty") + "</div>" ]);
                                }

                            };
                            this.cache = [];
                            this.extraAfterDataGridUpdate.push(fnDrawGantt);

                        },
                        
                        refreshGantt : function TaskListView_refreshGantt(){

                            g.Draw();
                            g.DrawDependencies();
                        }

                        
                    }, true);

})();
