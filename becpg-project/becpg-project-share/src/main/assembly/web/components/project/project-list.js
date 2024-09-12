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
        YAHOO.Bubbling.on("simpleDetailedChanged", this.onSimpleDetailed, this);
        
        this.services.preferences = new Alfresco.service.Preferences();

        if (view == "resources")
        {

            this.setOptions(
            {
                sortable : false,
                localSort : true,
                usePagination : true,
                displayTopPagination : false,
                configurableColumns : false,
                useFilter : true,
                itemType : "pjt:taskList",
                list : "projectList",
                sortId : "TaskResources",
                extraParams : "resources",
                formWidth : "65em"

            });

        } else if(view == "tasks"){
        	 this.setOptions(
        	            {
        	                sortable : false,
        	                localSort : false,
        	                usePagination : true,
        	                displayTopPagination : false,
        	                configurableColumns : false,
        	                useFilter : true,
        	                itemType : "pjt:taskList",
        	                list : "projectList",
        	                columnFormId: "projectList",
        	                saveFieldUrl : Alfresco.constants.PROXY_URI + "becpg/bulkedit/save",
        	                extraParams : "tasks",
        	                hiddenColumns : [ "prop_fm_commentCount" ]
        	            });
        	
        	
        } else {
            this
                    .setOptions(
                    {
                        sortable : false,
                        localSort : false,
                        usePagination : true,
                        displayTopPagination : false,
                        useFilter : true,
                        configurableColumns : false,
                        itemType : "pjt:project",
                        list : "projectList",
                        groupBy : "prop_pjt_projectHierarchy2",
                        sortId : "ProjectList",
                        saveFieldUrl : Alfresco.constants.PROXY_URI + "becpg/bulkedit/save",
                        groupFormater : function(args, record)
                        {

                            var groupName = "";
                            if (record.getData("itemData")["prop_pjt_projectHierarchy1"] != null && record
                                    .getData("itemData")["prop_pjt_projectHierarchy1"].displayValue != null)
                            {
                                groupName += record.getData("itemData")["prop_pjt_projectHierarchy1"].displayValue;
                            }


                            if (record.getData("itemData")["prop_pjt_projectHierarchy2"] != null && record
                                    .getData("itemData")["prop_pjt_projectHierarchy2"].displayValue != null)
                            {
                            	 if (groupName.length > 0 )
                                 {
                                     groupName += " - ";
                                 }
                            	
                                groupName += record.getData("itemData")["prop_pjt_projectHierarchy2"].displayValue;
                            }

                            return groupName;
                        },
                        hiddenColumns : [ "prop_pjt_projectHierarchy1", "prop_pjt_projectHierarchy2" ],
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
							var defaultFilter = "projects.InProgress";
							
							if (this.view == "resources" || this.view == "tasks")
						    {
								defaultFilter = "tasks.InProgress";
							
								if(this.options.filter == null){
		                        	this.options.filter = {
		                        			"filterId" : "tasks",
		                        			"filterData" : "InProgress"
		                        		};
		                        } 
                        	
							} else {
									if(this.options.filter == null){
		                        		this.options.filter = {
		                        			"filterId" : "projects",
		                        			"filterData" : "InProgress"
		                        		};
		                        	}
                        	
							} 


                            this.widgets.filter = Alfresco.util.createYUIButton(this, "filters", this.onMenuFilterChanged, {
                                type : "menu",
                                menu : "filters-menu",
                                lazyloadmenu : false,
                                zindex: 99
                             });

                            this.widgets.filter.getMenu().subscribe("beforeShow", function () {
                            	this.cfg.setProperty("zindex", 99);
                            });
                            

                            var filterKey = (this.options.filter.filterId ? this.options.filter.filterId + (this.options.filter.filterData ? "." + this.options.filter.filterData : "") : defaultFilter );
                            this.widgets.filter.set("label", this.msg("filter."+filterKey)+ " " + Alfresco.constants.MENU_ARROW_SYMBOL);
                            
                        	
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
                                                
                                                data.sort(function (a, b){
                                                    if(a!=null && a.sort!=null	
                                                    		&& b!=null && b.sort!=null	){
                                                    	return a.sort - b.sort;
                                                    }
                                                	return 0;
                                                });

                                                this.options.parentNodeRef = response.json.parentNodeRef;

                                                var html = "";

                                                for ( var i in data)
                                                {

                                                    var taskLegend =
                                                    {
                                                        id : data[i].nodeRef,
                                                        label : data[i].label,
                                                        color : data[i].color!=null ? data[i].color.toString().replace('#', '') : "FFBC00",
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
                                                
                                                if(this.view == "dataTable"){
	                                           		 if(this.options.simpleView){
	                                           			 this.options.columnFormId = "datagrid-simple";
	                                           		 } 
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
                                extraParams : this.options.extraParams
                            };
                            
                            if(this.currentSort!=null){
								request.sort = this.currentSort.replace("prop_","").replace("_",":")+"|"+((this.currentSortDir == "yui-dt-asc") ? "true" : "false");
							}
							if(this.queryExecutionId != null) {
								request.queryExecutionId = this.queryExecutionId;
							}
							if(this.options.filter.filterId && p_obj.filter.filterId != "filterform"){
								p_obj.filter = this.options.filter;
							}

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
                                    g = new JSGantt.GanttChart('g', Dom.get(this.id + "-gantt"), g != null ? g
                                            .getFormat() : null, false);
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

                                        for (var j in taskList)
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
                                            

                                            var pParent = projectId;

                                            if (task["itemData"]["prop_bcpg_parentLevel"].value != null)
                                            {
                                                  pParent = task["itemData"]["prop_bcpg_parentLevel"].value;
                                            }
                                            var pGroup = !task["itemData"]["prop_pjt_tlIsGroup"].value ? 0 : 1;


                                            var tlIsMilestone = task["itemData"]["prop_pjt_tlIsMilestone"].value;
                                            var tlPercent = task["itemData"]["prop_pjt_completionPercent"].value;

                                            var taskOwner = null;
                                            
                                            if(task["itemData"]["assoc_pjt_tlResources"].length>0){
                                            	taskOwner = "";
                                            	for(var zz in  task["itemData"]["assoc_pjt_tlResources"]){
                                            		taskOwner += '<span class="resource-title">' + task["itemData"]["assoc_pjt_tlResources"][zz].displayValue + '</span>';                               	
                                            	}
                                            }
 

                                            var tdates = this.cache[taskId];
                                            if (!tdates)
                                            {
                                                tdates = this.extractDates(task, start, true);
                                                this.cache[taskId] = tdates;
                                            }

                                            g.AddTaskItem(new JSGantt.TaskItem(taskId, this.getTaskTitle(task,
                                                    oData.nodeRef), tdates.start, tdates.end, this
                                                    .getTaskColor(task), null, tlIsMilestone ? 1 : 0, taskOwner,
                                                    tlPercent, pGroup, pParent, 1, precTaskIds));

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
                                    g = new JSGantt.GanttChart('g', Dom.get(this.id + "-gantt"), g != null ? g
                                            .getFormat() : null, false);
                                    g.setDateInputFormat("mediumDate");
                                    g.setDateDisplayFormat("mediumDate");
                                    g.setCaptionType('Resource');
                                    var start = new Date();
                                    var resources = [];

                                    for (var i = 0; i < recordSet.getLength(); i++){

                                        var oRecord = recordSet.getRecord(i);
                                        var task = oRecord.getData();
                                       
                                        		
                                         for (var z = 0; z < task["itemData"]["assoc_pjt_tlResources"].length ; z++){	
											var taskResourceParent = task["itemData"]["assoc_pjt_tlResources"][z];
	 										var taskOwnerParent = taskResourceParent.value;
	                                        if (!resources[taskOwnerParent]){
	                                        	resources[taskOwnerParent] = true;
	                                        
	                                            g.AddTaskItem(new JSGantt.TaskItem(taskOwnerParent, ('<span class="resource-title">' + taskResourceParent.displayValue + '</span>'), null, null,
	                                                    'FFBC00', '', 0, '', 0, 1, 0, 1));
	                                            
	                                            
	                                            for (var j = 0; j < recordSet.getLength(); j++){
	                                            	oRecord = recordSet.getRecord(j);
	                                                task = oRecord.getData();
	                                                
	                                                for (var zz = 0; zz < task["itemData"]["assoc_pjt_tlResources"].length ; zz++){	
		
													   	var taskResource = task["itemData"]["assoc_pjt_tlResources"][zz];
				 										var taskOwner = taskResource.value;
				 										
		                                                if(taskOwnerParent == taskOwner){
		                                                	var taskId = task.nodeRef;
		                                                	var title = task["itemData"]["dt_pjt_project"]["itemData"]["prop_cm_name"].displayValue;
		                                                	var tlIsMilestone = task["itemData"]["prop_pjt_tlIsMilestone"].value;
		                                                	var tlPercent = task["itemData"]["prop_pjt_completionPercent"].value;
		                                                	var tdates = this.cache[taskId];
		                                                	if (!tdates){
		                                                		tdates = this.extractDates(task, start, true);
		                                                		this.cache[taskId] = tdates;
		                                                	}
		                                                	task["itemData"]["prop_pjt_tlTaskName"].displayValue = task["itemData"]["dt_pjt_project"]["itemData"]["prop_bcpg_code"].displayValue + " - " + task["itemData"]["prop_pjt_tlTaskName"].displayValue;
		                                                	var taskTitle = this.getTaskTitle(task, null);
		                                                	
		                                                	g.AddTaskItem(new JSGantt.TaskItem(taskId, taskTitle, tdates.start, tdates.end,
		                                                			this.getTaskColor(task), null, tlIsMilestone ? 1 : 0, title , tlPercent,
		                                                					0, taskOwner, 1));
		                                                }
	                                                }		
	                                            }
	                                            
	                                        }
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
                        	this.queryExecutionId = null;
                            var filter = Alfresco.util.cleanBubblingObject(args[1]);
                            filter = (this.options.filter.filterId && filter.filterId != "filterform") ? this.options.filter : filter; 
                            this.options.filter = filter;
                          
                            if (filter.filterId == "filterform" || filter.filterId == "all" || filter.filterId == "my-projects")
                            {
                            	this.widgets.filter.set("label", $html(this
                                        .msg("filter." + filter.filterId))+ " " + Alfresco.constants.MENU_ARROW_SYMBOL);
                            	
                            }
                            else if (filter.filterId == "tag")
                            {
                            	this.widgets.filter.set("label", $html(this.msg(
                                        "filter." + filter.filterId, filter.filterData))+ " " + Alfresco.constants.MENU_ARROW_SYMBOL);
                            	
                            }
                            else
                            {
                            	this.widgets.filter.set("label", $html(this
                                        .msg("filter." + filter.filterId + (filter.filterData ? "." + filter.filterData
                                                : ""), filter.filterData))+ " " + Alfresco.constants.MENU_ARROW_SYMBOL);
                            	
                            }

                        },
                        
                        

                        onMenuFilterChanged : function PTL_onFilterChange(p_sType, p_aArgs) {
                           var menuItem = p_aArgs[1];
                           if (menuItem) {
                          	 this.queryExecutionId = null;
                          	var value = menuItem.value;
                              var me = this

                              me.widgets.filter.set("label", menuItem.cfg.getProperty("text")+ " " + Alfresco.constants.MENU_ARROW_SYMBOL);
         
                              var filterObj = {
                                       filterOwner: me.name,
                                       filterId: value.split("|")[0]
                                    };

                              if("all" != filterObj.filterId &&  value.split("|").length>1){
                            	  filterObj.filterData = value.split("|")[1];
                              } else {
                            	  filterObj.filterData = "";
                              }
                              
                              
                              this.options.filter = filterObj;
                              this.services.preferences.set("org.alfresco.share.project.list."+this.view+"." + (this.options.siteId != null && this.options.siteId.length>0 ? this.options.siteId : "home") + ".filter", this.options.filter);
                              
                              YAHOO.Bubbling.fire("changeFilter", filterObj);
                           }
                        },
                        
                        

                        onSimpleDetailed : function PL_onSimpleDetailed(layer, args) {
                        	 var simpleViewMode = Alfresco.util.cleanBubblingObject(args[1]);
                        	 this.queryExecutionId = null;
                        	 if(this.view == "dataTable"){
                        		 if(simpleViewMode.simpleView){
                        			 this.options.simpleView = true;
                        			 this.options.columnFormId = "datagrid-simple";
                        		 } else {
                        			 this.options.simpleView = false;
                        			 this.options.columnFormId = null;
                        		 }
                        		 
                        		 this.populateDataGrid();
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
                        	var obj = args[1], instance = this;
                            if (obj && (obj.nodeRef !== null))
                            {
                                this.queryExecutionId = null;
                                window.location = beCPG.util.entityURL(instance.options.siteId,
                                		obj.nodeRef , "pjt:project",null,"View-properties");
                                                
                            }
                        }
                    }, true);

})();
