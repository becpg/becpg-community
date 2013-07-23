/**
 * Dashboard ProjectDashlet component.
 * 
 * @namespace beCPG
 * @class beCPG.dashlet.ProjectDashlet
 */
(function() {
   /**
    * YUI Library aliases
    */
   var Dom = YAHOO.util.Dom, Event = YAHOO.util.Event, Selector = YAHOO.util.Selector;
   /**
    * Alfresco Slingshot aliases
    */
   var $html = Alfresco.util.encodeHTML;

   /**
    * Preferences
    */
   var PREFERENCES_PROJECT__DASHLET = "org.alfresco.share.project.catalog.dashlet",

   PREFERENCES_PROJECT__DASHLET_FILTER = PREFERENCES_PROJECT__DASHLET + ".filter", PREFERENCES_PROJECT__DASHLET_VIEW = PREFERENCES_PROJECT__DASHLET + ".view";
   PREFERENCES_PROJECT__DASHLET_SIMPLEVIEW = PREFERENCES_PROJECT__DASHLET + ".simpleView";

   /**
    * Use the getDomId function to get unique names for global event handling
    */
   var FAVOURITE_EVENTCLASS = Alfresco.util.generateDomId(null, "favouriteProject"), TWISTER_EVENTCLASS = Alfresco.util
         .generateDomId(null, "show-more-twister");

   /**
    * Dashboard ProjectDashlet constructor.
    * 
    * @param {String}
    *            htmlId The HTML id of the parent element
    * @return {beCPG.dashlet.ProjectDashlet} The new component instance
    * @constructor
    */
   beCPG.dashlet.ProjectDashlet = function ProjectDashlet_constructor(htmlId) {
      return beCPG.dashlet.ProjectDashlet.superclass.constructor.call(this, htmlId);
   };

   YAHOO
         .extend(
               beCPG.dashlet.ProjectDashlet,
               Alfresco.component.SimpleDocList,
               {
                  searchTerm : null,

                  currentPage : 1,

                  queryExecutionId : null,

                  /**
                   * Fired by YUI when parent element is available for scripting
                   * 
                   * @method onReady
                   */
                  onReady : function ProjectDashlet_onReady() {
                     // Create Dropdown filter
                     this.widgets.view = Alfresco.util.createYUIButton(this, "views", this.onViewChange, {
                        type : "menu",
                        menu : "views-menu",
                        lazyloadmenu : false
                     });

                     this.widgets.filter = Alfresco.util.createYUIButton(this, "filters", this.onFilterChange, {
                        type : "menu",
                        menu : "filters-menu",
                        lazyloadmenu : false
                     });

                     // Select the preferred filter in the ui
                     var filter = this.options.filter;
                     filter = Alfresco.util.arrayContains(this.options.validFilters, filter) ? filter
                           : this.options.validFilters[0];
                     this.widgets.filter.set("label", this.msg("filter." + filter));
                     this.widgets.filter.value = filter;

                     // Select the preferred view in the ui
                     var view = this.options.view;
                     view = Alfresco.util.arrayContains(this.options.validViews, view) ? view
                           : this.options.validViews[0];
                     this.widgets.view.set("label", this.msg("view." + view));
                     this.widgets.view.value = view;

                     // Detailed/Simple List button
                     this.widgets.simpleDetailed = new YAHOO.widget.ButtonGroup(this.id + "-simpleDetailed");
                     if (this.widgets.simpleDetailed !== null) {
                        this.widgets.simpleDetailed.check(this.options.simpleView ? 0 : 1);
                        this.widgets.simpleDetailed.on("checkedButtonChange", this.onSimpleDetailed,
                              this.widgets.simpleDetailed, this);
                     }

                     this.configureSearch();

                     // Display the toolbar now that we have selected the filter
                     Dom.removeClass(Selector.query(".toolbar div", this.id, true), "hidden");

                     // DataTable can now be rendered
                     // beCPG.dashlet.ProjectDashlet.superclass.onReady.apply(this, arguments);

                     var me = this;

                     // Tooltip for thumbnail on mouse hover
                     this.widgets.previewTooltip = new YAHOO.widget.Tooltip(this.id + "-previewTooltip", {
                        width : "108px"
                     });
                     this.widgets.previewTooltip.contextTriggerEvent
                           .subscribe(function(type, args) {
                              var context = args[0], record = me.widgets.alfrescoDataTable.getData(context.id), thumbnailUrl = Alfresco.constants.PROXY_URI + "api/node/" + record.nodeRef
                                    .replace(":/", "") + "/content/thumbnails/doclib?c=queue&ph=true";

                              this.cfg.setProperty("text", '<img src="' + thumbnailUrl + '" />');
                           });

                     // Tooltip for metadata on mouse hover
                     this.widgets.metadataTooltip = new YAHOO.widget.Tooltip(this.id + "-metadataTooltip");
                     this.widgets.metadataTooltip.contextTriggerEvent.subscribe(function(type, args) {
                        var context = args[0], record = me.widgets.alfrescoDataTable.getRecord(context.id).getData();

                        var text = '<em>' + me.msg("label.site") + ':</em> ' + $html(record.site.title) + '<br />';
                        text += '<em>' + me.msg("label.path") + ':</em> ' + $html(record.path);

                        this.cfg.setProperty("text", text);
                     });

                     this.widgets.dataSource = new YAHOO.util.DataSource(this.getWebscriptUrl(), {
                        connMethodPost : true,
                        responseType : YAHOO.util.DataSource.TYPE_JSON,
                        responseSchema : {
                           resultsList : "items",
                           metaFields : {
                              startIndex : "startIndex",
                              totalRecords : "totalRecords",
                              queryExecutionId : "queryExecutionId"
                           }
                        }
                     });
                     this.widgets.dataSource.connMgr.setDefaultPostHeader(Alfresco.util.Ajax.JSON);

                     var columDefs = [ {
                        key : "thumbnail",
                        sortable : false,
                        formatter : this.bind(this.renderCellThumbnail),
                        width : 16
                     }, {
                        key : "detail",
                        sortable : false,
                        formatter : this.bind(this.renderCellDetail)
                     } ];

                     this.widgets.alfrescoDataTable = new YAHOO.widget.DataTable(this.id + "-documents", columDefs,
                           this.widgets.dataSource, {
                              initialLoad : false,
                              dynamicData : false,
                              "MSG_EMPTY" : '<span class="wait">' + $html(this.msg("message.loading")) + '</span>',
                              "MSG_ERROR" : this.msg("message.error"),
                              className : "alfresco-datatable simple-doclist",
                              renderLoopSize : 4,
                              paginator : null,
                           });

                     this.widgets.alfrescoDataTable.getDataTable = function() {
                        return this;
                     };

                     this.widgets.alfrescoDataTable.getData = function(recordId) {
                        return this.getRecord(recordId).getData();
                     };

                     // YUI Paginator definition
                     this.widgets.paginator = new YAHOO.widget.Paginator({
                        containers : [ this.id + "-paginator" ],
                        rowsPerPage : this.options.maxItems,
                        initialPage : 1,
                        template : this.msg("pagination.template"),
                        pageReportTemplate : this.msg("pagination.template.page-report"),
                        previousPageLinkLabel : this.msg("pagination.previousPageLinkLabel"),
                        nextPageLinkLabel : this.msg("pagination.nextPageLinkLabel")
                     });

                     var handlePagination = function EntityDataGrid_handlePagination(state, scope) {
                        scope.currentPage = state.page;
                        scope.reloadDataTable();
                     };

                     this.widgets.paginator.subscribe("changeRequest", handlePagination, this);

                     this.widgets.paginator.render();

                     this.widgets.alfrescoDataTable.loadDataTable = function DataTable_loadDataTable(parameters) {

                        me.widgets.dataSource.connMgr.setDefaultPostHeader(Alfresco.util.Ajax.JSON);

                        var jsonParameters = parameters;

                        if (!jsonParameters) {
                           jsonParameters = scopeParameters;
                        }

                        me.widgets.dataSource.sendRequest(YAHOO.lang.JSON.stringify(jsonParameters), {
                           success : function DataTable_loadDataTable_success(oRequest, oResponse, oPayload) {
                              me.widgets.alfrescoDataTable.onDataReturnReplaceRows(oRequest, oResponse, oPayload);

                              if (me.widgets.paginator) {
                                 me.widgets.paginator.set('totalRecords', oResponse.meta.totalRecords);
                                 me.widgets.paginator.setPage(oResponse.meta.startIndex, true);
                              }
                              me.queryExecutionId = oResponse.meta.queryExecutionId;

                           },
                           // success : me.widgets.alfrescoDataTable.onDataReturnSetRows,
                           failure : me.widgets.alfrescoDataTable.onDataReturnReplaceRows,
                           scope : me.widgets.alfrescoDataTable,
                           argument : {}
                        });
                     };
                     // Override DataTable function to set custom empty message
                     var dataTable = this.widgets.alfrescoDataTable, original_doBeforeLoadData = dataTable.doBeforeLoadData;

                     dataTable.doBeforeLoadData = function SimpleDocList_doBeforeLoadData(sRequest, oResponse, oPayload) {
                        if (oResponse.results && oResponse.results.length === 0) {
                           oResponse.results.unshift({
                              isInfo : true,
                              title : me.msg("empty.project.title"),
                              description : me.msg("empty.project.description")
                           });
                        }

                        return original_doBeforeLoadData.apply(this, arguments);
                     };

                     // Rendering complete event handler
                     dataTable.subscribe("renderEvent", function() {
                        // Register tooltip contexts
                        this.widgets.previewTooltip.cfg.setProperty("context", this.previewTooltips);
                        // this.widgets.metadataTooltip.cfg.setProperty("context", this.metadataTooltips);
                     }, this, true);

                     // Hook favourite document events
                     var fnFavouriteHandler = function SimpleDocList_fnFavouriteHandler(layer, args) {
                        var owner = YAHOO.Bubbling.getOwnerByTagName(args[1].anchor, "div");
                        if (owner !== null) {
                           me.onFavourite.call(me, args[1].target.offsetParent, owner);
                        }
                        return true;
                     };

                     YAHOO.Bubbling.addDefaultAction(FAVOURITE_EVENTCLASS, fnFavouriteHandler);
                     YAHOO.Bubbling.addDefaultAction(TWISTER_EVENTCLASS, this.onActionShowMore);

                     this.initTaskHandlers();
                     this.reloadDataTable();
                  },

                  /**
                   * Generate base webscript url. Can be overridden.
                   * 
                   * @method getWebscriptUrl
                   */
                  getWebscriptUrl : function ProjectDashlet_getWebscriptUrl() {
                     if (Alfresco.constants.SITE !== null && Alfresco.constants.SITE.length > 0) {
                        return Alfresco.constants.PROXY_URI + "becpg/entity/datalists/data/node?itemType=pjt:project&site=" + Alfresco.constants.SITE + "&pageSize=" + this.options.maxItems + "&dataListName=projectList&container=documentLibrary&repo=false";
                     }
                     return Alfresco.constants.PROXY_URI + "becpg/entity/datalists/data/node?itemType=pjt:project&pageSize=" + this.options.maxItems + "&dataListName=projectList&repo=true";
                  },

                  /**
                   * Calculate webscript parameters
                   * 
                   * @method getParameters
                   * @override
                   */
                  getParameters : function ProjectDashlet_getParameters() {

                     var isTask = this.widgets.view.value.indexOf("task") > -1, sort = "cm:name";

                     if (isTask) {
                        if (this.widgets.filter.value == "InProgress") {
                           sort = "pjt:tlEnd|true";
                        } else if (this.widgets.filter.value == "Planned") {
                           sort = "pjt:tlStart|true";
                        } else {
                           sort = "pjt:tlEnd|false";
                        }
                     }

                     var req = 'fts(';

                     req += ' +@pjt\\:' + (isTask ? 'tl' : 'project') + 'State:"' + this.widgets.filter.value + '"';

                     if (this.searchTerm !== null && this.searchTerm.length > 0) {
                        req += "  +@" + (isTask ? "pjt\\:tlTaskName" : "cm\\:name") + ":(" + this.searchTerm + ")";
                     }

                     req += ')';

                     var fields = {
                        "project" : [
                              "pjt_projectOverdue",
                              "pjt_projectHierarchy1",
                              "pjt_projectHierarchy2",
                              "pjt_projectPriority",
                              "pjt_completionPercent",
                              "bcpg_code",
                              "cm_name",
                              "pjt_taskList|pjt_tlTaskName|pjt_tlDuration|pjt_tlPrevTasks|pjt_tlState|pjt_completionPercent|pjt_tlStart|pjt_tlEnd|pjt_tlWorkflowInstance|fm_commentCount",
                              "pjt_deliverableList|pjt_dlDescription|pjt_dlContent|pjt_dlState|fm_commentCount",
                              "pjt_projectManager", "pjt_projectStartDate", "pjt_projectCompletionDate",
                              "pjt_projectDueDate", "pjt_projectState" ],
                       "project-simple" : [
                                           "pjt_projectOverdue",
                                           "pjt_projectHierarchy1",
                                           "pjt_projectHierarchy2",
                                           "pjt_projectPriority",
                                           "pjt_completionPercent",
                                           "bcpg_code",
                                           "cm_name",
                                           "pjt_projectManager", "pjt_projectStartDate", "pjt_projectCompletionDate",
                                           "pjt_projectDueDate", "pjt_projectState" ],
                        "task" : [ "pjt_tlTaskName", "pjt_tlDuration",
                              "pjt_tlResources", "pjt_tlTaskLegend", "pjt_tlState", "pjt_completionPercent",
                              "pjt_tlStart", "pjt_tlEnd", "pjt_tlWorkflowInstance","fm_commentCount",
                              "pjt_project|cm_name|pjt_projectHierarchy1|pjt_projectHierarchy2|pjt_completionPercent|bcpg_code" ]
                     };

                     var request = {
                        fields : fields[isTask ? 'task' : ("project"+(this.options.simpleView?'-simple':''))],
                        page : this.currentPage,
                        sort : sort,
                        queryExecutionId : this.queryExecutionId,
                        filter : {
                           filterId : this.widgets.view.value,
                           filterOwner : "Alfresco.component.AllFilter",
                           filterData : this.widgets.filter.value,
                           filterParams : req
                        }
                     };
                     return request;

                  },

                  onFilterChange : function ProjectDashlet_onFilterChange(p_sType, p_aArgs) {
                     var menuItem = p_aArgs[1];
                     if (menuItem) {
                        this.widgets.filter.set("label", menuItem.cfg.getProperty("text"));
                        this.widgets.filter.value = menuItem.value;
                        this.currentPage = 1;

                        this.services.preferences.set(PREFERENCES_PROJECT__DASHLET_FILTER, this.widgets.filter.value);

                        this._cleanSearchText();
                        this.reloadDataTable();
                     }
                  },
                  onViewChange : function ProjectDashlet_onViewChange(p_sType, p_aArgs) {
                     var menuItem = p_aArgs[1];
                     if (menuItem) {
                        this.widgets.view.set("label", menuItem.cfg.getProperty("text"));
                        this.widgets.view.value = menuItem.value;
                        this.currentPage = 1;

                        this.services.preferences.set(PREFERENCES_PROJECT__DASHLET_VIEW, this.widgets.view.value);

                        this._cleanSearchText();
                        this.reloadDataTable();
                     }
                  },

                  _cleanSearchText : function ProjectDashlet__cleanSearchText() {
                     var searchText = this.getSearchText();
                     if (searchText.indexOf("*") > 0 && searchText.replace(/\*/g, "").length < 3) {
                        this.searchTerm = null;
                     } else {
                        this.searchTerm = searchText;
                     }

                  },

                  /**
                   * Show/Hide detailed list buttongroup click handler
                   * 
                   * @method onSimpleDetailed
                   * @param e
                   *            {object} DomEvent
                   * @param p_obj
                   *            {object} Object passed back from addListener method
                   */
                  onSimpleDetailed : function ProjectDashlet_onSimpleDetailed(e, p_obj) {
                     this.options.simpleView = e.newValue.index === 0;
                     this.services.preferences.set(PREFERENCES_PROJECT__DASHLET_SIMPLEVIEW, this.options.simpleView);
                     if (e) {
                        Event.preventDefault(e);
                     }

                     this.reloadDataTable();
                  },
                  /**
                   * Thumbnail custom datacell formatter
                   * 
                   * @method renderCellThumbnail
                   * @param elCell
                   *            {object}
                   * @param oRecord
                   *            {object}
                   * @param oColumn
                   *            {object}
                   * @param oData
                   *            {object|string}
                   */
                  renderCellThumbnail : function SimpleDocList_renderCellThumbnail(elCell, oRecord, oColumn, oData) {
                     var columnWidth = 40, record = oRecord.getData(), desc = "";

                     if (record.isInfo) {
                        columnWidth = 52;
                        desc = '<img src="' + Alfresco.constants.URL_RESCONTEXT + 'components/images/help-docs-bw-32.png" />';
                     } else {

                        var isTask = this.widgets.view.value.indexOf("task") > -1;

                        if (isTask) {
                           record = record.itemData["dt_pjt_project"];
                        }

                        record.jsNode = {};
                        record.jsNode.type = record.itemType;

                        var thumbName = record.itemData["prop_cm_name"].value, recordSiteName = record.site != null ? record.site.shortName
                              : null, extn = thumbName.substring(thumbName.lastIndexOf(".")), nodeRef = new Alfresco.util.NodeRef(
                              record.nodeRef), docDetailsUrl = beCPG.util.entityDetailsURL(recordSiteName,
                              record.nodeRef, record.itemType);

                        if (this.options.simpleView) {
                           /**
                            * Simple View
                            */
                           var id = this.id + '-preview-' + oRecord.getId();
                           desc = '<span id="' + id + '" class="icon32"><a href="' + docDetailsUrl + '"><img src="' + beCPG.util
                                 .getFileIcon(thumbName, record, false, true) + '" alt="' + extn + '" title="' + $html(thumbName) + '" /></a></span>';

                           // Preview tooltip
                           this.previewTooltips.push(id);
                        } else {
                           /**
                            * Detailed View
                            */
                           columnWidth = 100;
                           desc = '<span class="thumbnail"><a href="' + docDetailsUrl + '"><img src="' + Alfresco.constants.PROXY_URI + 'api/node/' + nodeRef.uri + '/content/thumbnails/doclib?c=queue&ph=true" alt="' + extn + '" title="' + $html(thumbName) + '" /></a></span>';
                        }

                     }

                     oColumn.width = columnWidth;

                     Dom.setStyle(elCell, "width", oColumn.width + "px");
                     Dom.setStyle(elCell.parentNode, "width", oColumn.width + "px");

                     elCell.innerHTML = desc;
                  },

                  /**
                   * Detail custom datacell formatter
                   * 
                   * @method renderCellDetail
                   * @param elCell
                   *            {object}
                   * @param oRecord
                   *            {object}
                   * @param oColumn
                   *            {object}
                   * @param oData
                   *            {object|string}
                   */
                  renderCellDetail : function ProjectDashlet_renderCellDetail(elCell, oRecord, oColumn, oData) {
                     var record = oRecord.getData(), desc = "", dateLine = "";

                     if (record.isInfo) {
                        desc += '<div class="empty"><h3>' + record.title + '</h3>';
                        desc += '<span>' + record.description + '</span></div>';
                     } else {
                        
                     
                        var isTask = this.widgets.view.value.indexOf("task") > -1;
                        
                        var dates = this.extractDates(record,null,isTask), end = dates.due;

                        dateLine += (dates.start ? Alfresco.util.formatDate(dates.start, "longDate") : scope
                              .msg("label.none"));

                        if (dates.end != null) {
                           end = dates.end;
                        }

                        dateLine += " - ";

                        dateLine += (dates.start ? Alfresco.util.formatDate(end, "longDate") : scope
                              .msg("label.none"));


                        if (isTask) {
                           if (!this.options.simpleView) {
                              desc += '<h3 class="filename">';
                              desc += this.getTaskTitle(record, record.itemData["dt_pjt_project"].nodeRef, true, null,
                                    true);
                              desc += '</h3>';
                           } else {
                              desc += '<h3 class="filename simple-view">' + this.getTaskTitle(record,
                                    record.itemData["dt_pjt_project"].nodeRef, false, null, false) + '</h3>';
                           }


                           desc += '<div class="detail">';
                           desc += '<span class="project-date">[ ' + dateLine + ' ]</span>';
                           desc += '<span class="project">';
                           desc += this.getProjectTitle(record.itemData["dt_pjt_project"]);
                           desc += "</span>";

                           if (!this.options.simpleView) {
                              desc += this.renderResourcesList(oRecord);
                           }

                           desc += '</div>';

                        } else {

                           var recordSiteName = record.site != null ? record.site.shortName : null;

                          

                           if (this.options.simpleView) {

                              /**
                               * Simple View
                               */
                              desc += '<h3 class="filename">' + this.getProjectTitle(record, true, false) + '</h3>';
                              desc += '<div class="detail">';

                              desc += '<span class="project-date">[ ' + dateLine + ' ]</span>';
                              desc += '<span class="hierachy">';
                              if (record.itemData["prop_pjt_projectHierarchy1"] && record.itemData["prop_pjt_projectHierarchy1"].displayValue!=null) {
                                 desc += record.itemData["prop_pjt_projectHierarchy1"].displayValue;
                              }
                              if (record.itemData["prop_pjt_projectHierarchy2"] && record.itemData["prop_pjt_projectHierarchy2"].displayValue!=null) {

                                 desc += " - " + record.itemData["prop_pjt_projectHierarchy2"].displayValue;
                              }

                              desc += "</span>";
                           } else {
                              /**
                               * Detailed View
                               */

                              desc += '<h3 class="filename">' + this.getProjectTitle(record, true, true) + '</h3>';

                              desc += this.renderProjectManager(oRecord);

                              desc += '<div class="detail">';

                              desc += '<span class="project-date">[ ' + dateLine + ' ]</span>';
                              desc += '<span class="hierachy">';
                              if (record.itemData["prop_pjt_projectHierarchy1"] && record.itemData["prop_pjt_projectHierarchy1"].displayValue!=null) {
                                 desc += record.itemData["prop_pjt_projectHierarchy1"].displayValue;
                              }
                              if (record.itemData["prop_pjt_projectHierarchy2"] && record.itemData["prop_pjt_projectHierarchy2"].displayValue!=null) {

                                 desc += " - " + record.itemData["prop_pjt_projectHierarchy2"].displayValue;
                              }

                              desc += "</span>";

                              desc += this.renderTaskList(oRecord);
                              desc += this.renderDeliverableList(oRecord);

                              desc += '</div>';

                              var charactsUrl = beCPG.util.entityCharactURL(recordSiteName, record.nodeRef,
                                    record.itemType), documentsUrl = beCPG.util.entityDocumentsURL(recordSiteName,
                                    record.path, record.itemData["prop_cm_name"].value);

                              /* Favourite / Charact / Download */
                              desc += '<div class="detail detail-social">';
                              desc += '<span class="item item-social">' + this.generateFavourite(this, oRecord) + '</span>';
                              desc += '<span class="item item-social item-separator"><a class="view-documents" href="' + documentsUrl + '"  title="' + this
                                    .msg("actions.entity.view-documents") + '" tabindex="0">' + this
                                    .msg("actions.entity.view-documents.short") + '</a></span>';
                              desc += '<span class="item item-social item-separator"><a class="view-characts" href="' + charactsUrl + '" title="' + this
                                    .msg("actions.entity.view-datalists") + '" tabindex="0">' + this
                                    .msg("actions.entity.view-datalists.short") + '</a></span>';
                              desc += '</div>';
                           }

                           // Metadata tooltip
                           // this.metadataTooltips.push(id);
                        }
                     }
                     elCell.innerHTML = desc;
                  },

                  generateFavourite : function ProjectDashlet_generateFavourite(scope, record) {
                     var i18n = "favourite.document.", html = "";

                     if (record.getData("isFavourite")) {
                        html = '<a class="favourite-action ' + FAVOURITE_EVENTCLASS + ' enabled" title="' + scope
                              .msg(i18n + "remove.tip") + '" tabindex="0"></a>';
                     } else {
                        html = '<a class="favourite-action ' + FAVOURITE_EVENTCLASS + '" title="' + scope
                              .msg(i18n + "add.tip") + '" tabindex="0">' + scope.msg(i18n + "add.label") + '</a>';
                     }

                     return html;
                  },

                  renderResourcesList : function ProjectDashlet_renderResourcesList(oRecord) {

                     var resources = oRecord.getData("itemData")["assoc_pjt_tlResources"];

                     var ret = '<ul class="task-resources">';

                     for (j in resources) {
                        var resource = resources[j];
                        ret += "<li>";
                        ret += '<span class="avatar" title="' + resource.metadata + '">';
                        ret += Alfresco.Share.userAvatar(resource.displayValue, 32);
                        ret += "</span></li>";
                     }
                     ret += "</ul>";

                     return ret;

                  },

                  renderProjectManager : function ProjectDashlet_renderResourcesList(oRecord) {

                     var resource = oRecord.getData("itemData")["assoc_pjt_projectManager"];

                     var ret = "";

                     if (resource && resource[0]) {
                        ret += '<div class="project-manager avatar" title="' + resource[0].metadata + '">';
                        ret += Alfresco.Share.userAvatar(resource[0].displayValue, 16);
                        ret += "</div>";
                     }

                     return ret;

                  },

                  renderTaskList : function ProjectDashlet_renderTaskList(oRecord) {

                     var tasks = oRecord.getData("itemData")["dt_pjt_taskList"];

                     var ret = '<ul class="hidden">', idx = 0;

                     for (j in tasks) {
                        var task = tasks[j];
                        if (task["itemData"]["prop_pjt_tlState"].value == this.widgets.filter.value) {
                           idx++;
                           ret += "<li>" + this.getTaskTitle(task, oRecord.getData().nodeRef, true) + "</li>";
                        }
                     }
                     ret += "</ul>";

                     return '<div ><a class="alfresco-twister alfresco-twister-closed ' + TWISTER_EVENTCLASS + '">' + this
                           .msg("show.tasks") + '(' + idx + ')</a>' + ret + '</div>';
                  },

                  renderDeliverableList : function ProjectDashlet_renderDeliverableList(oRecord) {

                     var deliverables = oRecord.getData("itemData")["dt_pjt_deliverableList"], idx = 0;

                     var ret = '<ul class="hidden">';

                     for (j in deliverables) {
                        var deliverable = deliverables[j];
                        if (deliverable["itemData"]["prop_pjt_dlState"].value == this.widgets.filter.value) {
                           idx++;
                           ret += "<li>" + this.getDeliverableTitle(deliverable, oRecord.getData().nodeRef) + "</li>";
                        }

                     }
                     ret += "</ul>";

                     return '<div ><a class="alfresco-twister alfresco-twister-closed ' + TWISTER_EVENTCLASS + '">' + this
                           .msg("show.deliverables") + '(' + idx + ')</a>' + ret + '</div>';

                  },

                  onActionShowMore : function ProjectDashlet_onActionShowMore(layer, args) {

                     var elController = args[1].anchor;

                     elPanel = Dom.getNextSibling(elController);

                     if (Dom.hasClass(elController, "alfresco-twister-closed")) {
                        Dom.removeClass(elPanel, "hidden");
                        Dom.replaceClass(elController, "alfresco-twister-closed", "alfresco-twister-open");
                     } else {
                        Dom.addClass(elPanel, "hidden");
                        Dom.replaceClass(elController, "alfresco-twister-open", "alfresco-twister-closed");
                     }

                  },

                  /**
                   * Search Handlers
                   */

                  /**
                   * Configure search area
                   * 
                   * @method configureSearch
                   */
                  configureSearch : function ProjectDashlet_configureSearch() {
                     this.widgets.searchBox = Dom.get(this.id + "-searchText");
                     this.defaultSearchText = this.msg("header.search.default");

                     Event.addListener(this.widgets.searchBox, "focus", this.onSearchFocus, null, this);
                     Event.addListener(this.widgets.searchBox, "blur", this.onSearchBlur, null, this);
                     Event.addListener(this.widgets.searchBox, "change", this.onSearchChange, null, this);

                     this.setDefaultSearchText();

                     this.widgets.searchMore = new YAHOO.widget.Button(this.id + "-search_more", {
                        type : "menu",
                        menu : this.id + "-searchmenu_more"
                     });
                  },

                  /**
                   * Update image class when search box has focus.
                   * 
                   * @method onSearchFocus
                   */
                  onSearchFocus : function ProjectDashlet_onSearchFocus() {
                     if (this.widgets.searchBox.value == this.defaultSearchText) {
                        Dom.removeClass(this.widgets.searchBox, "faded");
                        this.widgets.searchBox.value = "";
                     } else {
                        this.widgets.searchBox.select();
                     }
                  },

                  /**
                   * Set default search text when box loses focus and is empty.
                   * 
                   * @method onSearchBlur
                   */
                  onSearchBlur : function ProjectDashlet_onSearchBlur() {
                     var searchText = YAHOO.lang.trim(this.widgets.searchBox.value);
                     if (searchText.length === 0) {
                        /**
                         * Since the blur event occurs before the KeyListener gets the enter we give the enter listener
                         * a chance of testing against "" instead of the help text.
                         */
                        YAHOO.lang.later(100, this, this.setDefaultSearchText, []);
                     }
                  },

                  /**
                   * Set default search text for search box.
                   * 
                   * @method setDefaultSearchText
                   */
                  setDefaultSearchText : function ProjectDashlet_setDefaultSearchText() {
                     Dom.addClass(this.widgets.searchBox, "faded");
                     this.widgets.searchBox.value = this.defaultSearchText;
                  },

                  /**
                   * Get current search text from search box.
                   * 
                   * @method getSearchText
                   */
                  getSearchText : function ProjectDashlet_getSearchText() {

                     var ret = YAHOO.lang.trim(this.widgets.searchBox.value);
                     if (ret != this.defaultSearchText) {
                        return ret;
                     }
                     return "";
                  },

                  /**
                   * Will trigger a search
                   * 
                   * @method onSearchChange
                   */
                  onSearchChange : function ProjectDashlet_onSearchChange() {

                     this.currentPage = 1;

                     this._cleanSearchText();

                     this.reloadDataTable();
                  },
                  
                  //TODO Duplicate Method
                  _showPanel : function EntityDataGrid__showPanel(panelUrl, htmlid, itemNodeRef) {
                     
                     var me = this;
                     
                     Alfresco.util.Ajax.request({
                        url : panelUrl,
                        dataObj : {
                           htmlid : htmlid
                        },
                        successCallback : {
                           fn : function(response) {
                              // Inject the template from the XHR request into a new DIV
                              // element
                              var containerDiv = document.createElement("div");
                              containerDiv.innerHTML = response.serverResponse.responseText;

                              // The panel is created from the HTML returned in the XHR
                              // request, not the container
                              var panelDiv = Dom.getFirstChild(containerDiv);
                              this.widgets.panel = Alfresco.util.createYUIPanel(panelDiv, {
                                 draggable : true,
                                 width : "50em"
                              });

                              this.widgets.panel.subscribe("hide", function (){
                                 me.reloadDataTable();
                              });
                              
                              this.widgets.panel.show();
                              

                           },
                           scope : this
                        },
                        failureMessage : "Could not load dialog template from '" + panelUrl + "'.",
                        scope : this,
                        execScripts : true
                     });
                  }

               });

   YAHOO.lang.augmentProto(beCPG.dashlet.ProjectDashlet, beCPG.component.ProjectCommons);

})();
