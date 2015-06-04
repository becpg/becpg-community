/**
 * @namespace beCPG
 * @class beCPG.component.ProjectList
 */

var g; // gantt var

(function()
{
    /**
     * YUI Library aliases
     */
    var Dom = YAHOO.util.Dom;

    /**
     * Alfresco Slingshot aliases
     */
    var $html = Alfresco.util.encodeHTML;

    /**
     * DocumentList constructor.
     * 
     * @param htmlId
     *            {String} The HTML id of the parent element
     * @return {beCPG.component.ProjectList} The new DocumentList instance
     * @constructor
     */
    beCPG.component.ProjectList = function(htmlId, view)
    {

        this.view = view;

        beCPG.component.ProjectList.superclass.constructor.call(this, htmlId);

        /**
         * Decoupled event listeners
         */
        YAHOO.Bubbling.on("filterChanged", this.onFilterChanged, this);

        if (view == "resources")
        {

            this.setOptions(
            {
                sortable : false,
                usePagination : true,
                useFilter : true,
                itemType : "pjt:taskList",
                list : "projectList",
                sortId : "TaskResources",
                extraParams : "resources",
                formWidth : "65em"

            });

        }
        else
        {
            this
                    .setOptions(
                    {
                        sortable : false,
                        usePagination : true,
                        useFilter : true,
                        itemType : "pjt:project",
                        list : "projectList",
                        groupBy : "prop_pjt_projectHierarchy2",
                        sortId : "ProjectList",
                        groupFormater : function(args, record)
                        {

                            var groupName = "";
                            if (record.getData("itemData")["prop_pjt_projectHierarchy1"] != null && record
                                    .getData("itemData")["prop_pjt_projectHierarchy1"].displayValue != null)
                            {
                                groupName += record.getData("itemData")["prop_pjt_projectHierarchy1"].displayValue;
                            }

                            if (groupName.length > 0)
                            {
                                groupName += " - ";
                            }

                            if (record.getData("itemData")["prop_pjt_projectHierarchy2"] != null && record
                                    .getData("itemData")["prop_pjt_projectHierarchy2"].displayValue != null)
                            {
                                groupName += record.getData("itemData")["prop_pjt_projectHierarchy2"].displayValue;
                            }

                            return groupName;
                        },
                        hiddenColumns : [ "prop_pjt_projectHierarchy1", "prop_pjt_projectHierarchy2", "prop_bcpg_code",
                                "prop_pjt_projectCompletionDate", "prop_pjt_projectDueDate", "prop_pjt_projectState" ],
                        formWidth : "65em"

                    });
        }

        return this;
    };

    /**
     * Extend from Alfresco.component.Base
     */
    YAHOO.extend(beCPG.component.ProjectList, beCPG.module.EntityDataGrid);

    /**
     * Augment prototype with Actions module
     */
    YAHOO.lang.augmentProto(beCPG.component.ProjectList, beCPG.component.ProjectCommons);

    /**
     * Augment prototype with main class implementation, ensuring overwrite is
     * enabled
     */
    YAHOO.lang
            .augmentObject(
                    beCPG.component.ProjectList.prototype,
                    {
                        view : "dataTable",
                        /**
                         * State definition
                         */
                        taskLegends : [],

                        /**
                         * Fired by YUI when parent element is available for
                         * scripting. Initial History Manager event registration
                         * 
                         * @method onReady
                         */
                        onReady : function PL_onReady()
                        {

                            JSGantt.register(this);

                            var url = Alfresco.constants.PROXY_URI + "becpg/project/info" + (this.options.siteId != null && this.options.siteId.length > 0 ? "?site=" + this.options.siteId
                                    : "");

                            Alfresco.util.Ajax
                                    .request(
                                    {
                                        url : url,
                                        successCallback :
                                        {
                                            fn : function(response)
                                            {

                                                var data = response.json.legends;

                                                this.options.parentNodeRef = response.json.parentNodeRef;

                                                var html = "";

                                                for ( var i in data)
                                                {

                                                    var taskLegend =
                                                    {
                                                        id : data[i].nodeRef,
                                                        label : data[i].label,
                                                        color : data[i].color.replace('#', ''),
                                                        nbProjects : data[i].nbProjects
                                                    };

                                                    this.taskLegends.push(taskLegend);

                                                    html += '<span class="task-legend" style="background-color:#' + taskLegend.color + '" ></span><span><a href='+Alfresco.util
                                                    .siteURL("project-list?view="+
                                                            this.view+"#filter=filterform|%7B%22prop_pjt_projectState%22%3A%22InProgress%22%2C%22prop_pjt_projectLegends%22%3A%22"
                                                            +encodeURIComponent(taskLegend.id)+"%22%7D")+">"
                                                    + taskLegend.label + ' ('+taskLegend.nbProjects +')</a></span>&nbsp;';
                                                }

                                                Dom.get(this.id + "-legend").innerHTML = html;
                                                if (this.view == "gantt")
                                                {
                                                    this.options.pageSize = 10;
                                                    this.initGantt();
                                                }

                                                if (this.view == "resources")
                                                {
                                                    this.initResources();
                                                }

                                                this.initDataTable();

                                            },
                                            scope : this
                                        },
                                        failureCallback :
                                        {
                                            fn : function()
                                            {
                                                // DO nothing
                                            },
                                            scope : this
                                        }
                                    });

                        },
                        initDataTable : function PL_initDataTable()
                        {
                            beCPG.component.ProjectList.superclass.onReady.call(this);

                            this.populateDataGrid();

                            this.initTaskHandlers();

                        },

                        _buildDataGridParams : function EntityDataGrid__buildDataGridParams(p_obj)
                        {

                            var fields = this.dataRequestFields;

                            if (this.view == "resources")
                            {
                                fields = [ "pjt_tlTaskName", "pjt_tlIsMilestone", "pjt_tlDuration", "pjt_tlResources",
                                        "pjt_tlTaskLegend", "pjt_tlState", "pjt_completionPercent", "pjt_tlStart",
                                        "pjt_tlEnd", "pjt_tlWorkflowInstance", "fm_commentCount",
                                        "pjt_project|cm_name|pjt_projectHierarchy1|pjt_projectHierarchy2|pjt_completionPercent|bcpg_code" ];

                            }

                            var request =
                            {
                                fields : fields,
                                page : p_obj && p_obj.page ? p_obj.page : this.currentPage,
                                queryExecutionId : this.queryExecutionId,
                                extraParams : this.options.extraParams
                            };

                            if (p_obj && p_obj.filter)
                            {
                                request.filter =
                                {
                                    filterOwner : p_obj.filter.filterOwner,
                                    filterId : p_obj.filter.filterId,
                                    filterData : p_obj.filter.filterData,
                                    filterParams : this._createFilterURLParameters(p_obj.filter,
                                            this.options.filterParameters)
                                };
                            }

                            return request;
                        },

                        initGantt : function PL_initGantt()
                        {

                            var fnDrawGantt = function PL_onReady_fnDrawGantt()
                            {
                                var recordSet = this.widgets.dataTable.getRecordSet();
                                if (recordSet.getLength() != 0)
                                {
                                    g = new JSGantt.GanttChart('g', Dom.get(this.id + "-gantt"), 'day');
                                    g.setDateInputFormat("mediumDate");
                                    g.setDateDisplayFormat("mediumDate");
                                    g.setCaptionType('Resource');

                                    for (var i = 0; i < recordSet.getLength(); i++)
                                    {

                                        var oRecord = recordSet.getRecord(i);
                                        var oData = oRecord.getData();
                                        var projectId = oData.nodeRef;

                                        var title = '<span class="' + this.getOverdueClass(oData) + '">' + this
                                                .getProjectTitle(oData) + '</span>';

                                        var initiator = oRecord.getData("itemData")["assoc_pjt_projectManager"].displayValue;

                                        if (initiator && initiator != null && initiator.length > 0)
                                        {
                                            initiator = '<span class="resource-title">' + initiator + '</span>';
                                        }
                                        var percent = oRecord.getData("itemData")["prop_pjt_completionPercent"].value;

                                        var dates = this.extractDates(oData);

                                        g.AddTaskItem(new JSGantt.TaskItem(projectId, title, dates.start, dates.due,
                                                'FFBC00', '', 0, initiator, percent, 1, 0, 0));

                                        var start = dates.start;

                                        var taskList = oRecord.getData("itemData")["dt_pjt_taskList"];

                                        for (j in taskList)
                                        {
                                            var task = taskList[j];
                                            var taskId = task.nodeRef;
                                            var precTaskIds = "";
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
                                                    oData.nodeRef, tdates.start), tdates.start, tdates.end, this
                                                    .getTaskColor(task), null, tlIsMilestone ? 1 : 0, taskOwner,
                                                    tlPercent, 0, projectId, 1, precTaskIds));

                                        }

                                    }

                                    g.Draw();
                                    g.DrawDependencies();

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

                        initResources : function PL_initResources()
                        {

                            var fnDrawGantt = function PL_onReady_fnDrawGantt()
                            {
                                var recordSet = this.widgets.dataTable.getRecordSet();
                                if (recordSet.getLength() != 0)
                                {
                                    g = new JSGantt.GanttChart('g', Dom.get(this.id + "-gantt"), 'day');
                                    g.setDateInputFormat("shortDate");
                                    g.setDateDisplayFormat("shortDate");
                                    g.setCaptionType('Resource');
                                    var start = new Date();
                                    var resources = [];

                                    for (var i = 0; i < recordSet.getLength(); i++)
                                    {

                                        var oRecord = recordSet.getRecord(i);
                                        var task = oRecord.getData();
                                        var taskId = task.nodeRef;

                                        var title = task["itemData"]["dt_pjt_project"]["itemData"]["prop_cm_name"].displayValue;

                                        var tlIsMilestone = task["itemData"]["prop_pjt_tlIsMilestone"].value;
                                        var tlPercent = task["itemData"]["prop_pjt_completionPercent"].value;

                                        var taskOwner = task["itemData"]["assoc_pjt_tlResources"].length > 0 ? task["itemData"]["assoc_pjt_tlResources"][0].value
                                                : null;
                                        var taskOwnerTitle = task["itemData"]["assoc_pjt_tlResources"].length > 0 ? ('<span class="resource-title">' + task["itemData"]["assoc_pjt_tlResources"][0].displayValue + '</span>')
                                                : null;

                                        if (!resources[taskOwner])
                                        {
                                            g.AddTaskItem(new JSGantt.TaskItem(taskOwner, taskOwnerTitle, null, null,
                                                    'FFBC00', '', 0, '', 0, 1, 0, 1));
                                        }

                                        resources[taskOwner] = true;

                                        var tdates = this.cache[taskId];
                                        if (!tdates)
                                        {
                                            tdates = this.extractDates(task, start, true);
                                            this.cache[taskId] = tdates;
                                        }

                                        task["itemData"]["prop_pjt_tlTaskName"].displayValue = task["itemData"]["dt_pjt_project"]["itemData"]["prop_bcpg_code"].displayValue + " - " + task["itemData"]["prop_pjt_tlTaskName"].displayValue;

                                        var taskTitle = this.getTaskTitle(task, null, tdates.start);

                                        g.AddTaskItem(new JSGantt.TaskItem(taskId, taskTitle, tdates.start, tdates.end,
                                                this.getTaskColor(task), null, tlIsMilestone ? 1 : 0, title , tlPercent,
                                                0, taskOwner, 1));

                                    }

                                    g.Draw();
                                    g.DrawDependencies();

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

                        /**
                         * Fired when the currently active filter has changed
                         * 
                         * @method onFilterChanged
                         * @param layer
                         *            {string} the event source
                         * @param args
                         *            {object} arguments object
                         */
                        onFilterChanged : function PL_onFilterChanged(layer, args)
                        {
                            var filter = Alfresco.util.cleanBubblingObject(args[1]);
                            if (filter.filterId == "filterform")
                            {
                                Dom.get(this.id + "-filterTitle").innerHTML = $html(this
                                        .msg("filter." + filter.filterId));
                            }
                            else if (filter.filterId == "tag")
                            {
                                Dom.get(this.id + "-filterTitle").innerHTML = $html(this.msg(
                                        "filter." + filter.filterId, filter.filterData));
                            }
                            else
                            {
                                Dom.get(this.id + "-filterTitle").innerHTML = $html(this
                                        .msg("filter." + filter.filterId + (filter.filterData ? "." + filter.filterData
                                                : ""), filter.filterData));
                            }

                        },

                        /**
                         * Data Item created event handler
                         * 
                         * @method onDataItemCreated
                         * @param layer
                         *            {object} Event fired
                         * @param args
                         *            {array} Event parameters (depends on event
                         *            type)
                         */
                        onDataItemCreated : function EntityDataGrid_onDataItemCreated(layer, args)
                        {
                            var obj = args[1];
                            if (obj && (obj.nodeRef !== null))
                            {

                                var nodeRef = new Alfresco.util.NodeRef(obj.nodeRef), url = this.options.itemUrl + nodeRef.uri + ((this.options.entityNodeRef != null && this.options.entityNodeRef.length > 0) ? "?entityNodeRef=" + this.options.entityNodeRef + "&"
                                        : "?") + "itemType=" + encodeURIComponent(this.options.itemType != null ? this.options.itemType
                                        : this.datalistMeta.itemType) + "&dataListName=" + encodeURIComponent(this.datalistMeta.name != null ? this.datalistMeta.name
                                        : this.options.list);

                                // Reload the node's metadata
                                Alfresco.util.Ajax.jsonPost(
                                {
                                    url : url,
                                    dataObj : this._buildDataGridParams(),
                                    successCallback :
                                    {
                                        fn : function EntityDataGrid_onDataItemCreated_refreshSuccess(response)
                                        {

                                            if (response.json && (response.json.item !== null))
                                            {
                                                var item = response.json.item;

                                                YAHOO.Bubbling.fire("changeFilter", filter =
                                                {
                                                    filterOwner : "Alfresco.component.AllFilter",
                                                    filterId : "projects",
                                                    filterData : item["itemData"]["prop_pjt_projectState"].value
                                                });
                                            }
                                        },
                                        scope : this
                                    },
                                    failureCallback :
                                    {
                                        fn : function EntityDataGrid_onDataItemCreated_refreshFailure(response)
                                        {
                                            Alfresco.util.PopupManager.displayMessage(
                                            {
                                                text : this.msg("message.create.refresh.failure")
                                            });
                                        },
                                        scope : this
                                    }
                                });
                            }
                        }
                    }, true);

})();
