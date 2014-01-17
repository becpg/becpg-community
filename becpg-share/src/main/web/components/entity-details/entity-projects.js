/*******************************************************************************
 *  Copyright (C) 2010-2014 beCPG. 
 *   
 *  This file is part of beCPG 
 *   
 *  beCPG is free software: you can redistribute it and/or modify 
 *  it under the terms of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation, either version 3 of the License, or 
 *  (at your option) any later version. 
 *   
 *  beCPG is distributed in the hope that it will be useful, 
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 *  GNU Lesser General Public License for more details. 
 *   
 *  You should have received a copy of the GNU Lesser General Public License along with beCPG.
 *   If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
/**
 * Dashboard EntityProjects component.
 * 
 * @namespace beCPG
 * @class beCPG.component.EntityProjects
 */
(function() {
   /**
    * YUI Library aliases
    */
   var Dom = YAHOO.util.Dom;
   /**
    * Alfresco Slingshot aliases
    */
   var $html = Alfresco.util.encodeHTML;
   var  TWISTER_EVENTCLASS = Alfresco.util
   .generateDomId(null, "show-more-twister");

   /**
    * Dashboard EntityProjects constructor.
    * 
    * @param {String}
    *            htmlId The HTML id of the parent element
    * @return {beCPG.component.EntityProjects} The new component instance
    * @constructor
    */
   beCPG.component.EntityProjects = function EntityProjects_constructor(htmlId) {
      return beCPG.component.EntityProjects.superclass.constructor.call(this, htmlId);
   };

   YAHOO
         .extend(
               beCPG.component.EntityProjects,
               Alfresco.component.SimpleDocList,
               {

                  queryExecutionId : null,

                  /**
                   * Fired by YUI when parent element is available for scripting
                   * 
                   * @method onReady
                   */
                  onReady : function EntityProjects_onReady() {
                    

                     var me = this;


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

                   
                     this.widgets.alfrescoDataTable.loadDataTable = function DataTable_loadDataTable(parameters) {

                        me.widgets.dataSource.connMgr.setDefaultPostHeader(Alfresco.util.Ajax.JSON);

                        var jsonParameters = parameters;

                        if (!jsonParameters) {
                           jsonParameters = scopeParameters;
                        }

                        if (Alfresco.util.CSRFPolicy.isFilterEnabled())
                        {
                           me.widgets.dataSource.connMgr.initHeader(Alfresco.util.CSRFPolicy.getHeader(), Alfresco.util.CSRFPolicy.getToken(), false);
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

                   
                     YAHOO.Bubbling.addDefaultAction(TWISTER_EVENTCLASS, this.onActionShowMore);
                     

                     this.initTaskHandlers();
                     this.reloadDataTable();
                  },

                  /**
                   * Generate base webscript url. Can be overridden.
                   * 
                   * @method getWebscriptUrl
                   */
                  getWebscriptUrl : function EntityProjects_getWebscriptUrl() {
                     return Alfresco.constants.PROXY_URI + "becpg/entity/datalists/data/node?itemType=pjt:project&pageSize=" + this.options.maxItems + "&entityNodeRef="+this.options.nodeRef+"&dataListName=projectList&repo=true";
                  },

                  /**
                   * Calculate webscript parameters
                   * 
                   * @method getParameters
                   * @override
                   */
                  getParameters : function EntityProjects_getParameters() {

                

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
                              "pjt_projectDueDate", "pjt_projectState" ]
                     };

                     var request = {
                        fields : fields["project"],
                        page : 1,
                        sort : "cm:name",
                        queryExecutionId : this.queryExecutionId,
                        filter : {
                           filterId : "entity-projects",
                           filterOwner : "Alfresco.component.AllFilter",
                           filterData : "InProgress" //Better for perfs
                        }
                     };
                     return request;

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
                     var columnWidth = 90, record = oRecord.getData(), desc = "";

                     if (record.isInfo) {
                        columnWidth = 52;
                        desc = '<img src="' + Alfresco.constants.URL_RESCONTEXT + 'components/images/help-docs-bw-32.png" />';
                     } else {


                        record.jsNode = {};
                        record.jsNode.type = record.itemType;

                        var thumbName = record.itemData["prop_cm_name"].value, recordSiteName = record.site != null ? record.site.shortName
                              : null, extn = thumbName.substring(thumbName.lastIndexOf(".")), nodeRef = new Alfresco.util.NodeRef(
                              record.nodeRef), docDetailsUrl = beCPG.util.entityDetailsURL(recordSiteName,
                              record.nodeRef, record.itemType);

                           desc = '<span class="thumbnail" ><a href="' + docDetailsUrl + '"><img width="90%" height="90%"  src="' + Alfresco.constants.PROXY_URI + 'api/node/' + nodeRef.uri + '/content/thumbnails/doclib?c=queue&ph=true" alt="' + extn + '" title="' + $html(thumbName) + '" /></a></span>';
                   
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
                  renderCellDetail : function EntityProjects_renderCellDetail(elCell, oRecord, oColumn, oData) {
                     var record = oRecord.getData(), desc = "", dateLine = "";

                     if (record.isInfo) {
                        desc += '<div class="empty"><h3>' + record.title + '</h3>';
                        desc += '<span>' + record.description + '</span></div>';
                     } else {
                        
                     
                        var dates = this.extractDates(record,null,false), end = dates.due;

                        dateLine += (dates.start ? Alfresco.util.formatDate(dates.start, "longDate") : scope
                              .msg("label.none"));

                        if (dates.end != null) {
                           end = dates.end;
                        }

                        dateLine += " - ";

                        dateLine += (dates.start ? Alfresco.util.formatDate(end, "longDate") : scope
                              .msg("label.none"));


                              desc += '<h3 class="filename">' + this.getProjectTitle(record, true, true) + '</h3>';

                              desc += this.renderProjectManager(oRecord);

                              desc += '<div class="detail">';

                              desc += '<span class="project-date">[ ' + dateLine + ' ]</span><br/>';
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
       
                        }
                     
                     elCell.innerHTML = desc;
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

                  renderProjectManager : function EntityProjects_renderResourcesList(oRecord) {

                     var resource = oRecord.getData("itemData")["assoc_pjt_projectManager"];

                     var ret = "";

                     if (resource && resource[0]) {
                        ret += '<div class="project-manager avatar" title="' + resource[0].metadata + '">';
                        ret += Alfresco.Share.userAvatar(resource[0].displayValue, 32);
                        ret += "</div>";
                     }

                     return ret;

                  },

                  renderTaskList : function EntityProjects_renderTaskList(oRecord) {

                     var tasks = oRecord.getData("itemData")["dt_pjt_taskList"];

                     var ret = '<ul class="hidden">', idx = 0;

                     for (j in tasks) {
                        var task = tasks[j];
                           idx++;
                           ret += "<li>" + this.getTaskTitle(task, oRecord.getData().nodeRef, true) + "</li>";
                     }
                     ret += "</ul>";

                     return '<div ><a class="alfresco-twister alfresco-twister-closed ' + TWISTER_EVENTCLASS + '">' + this
                           .msg("show.tasks") + '(' + idx + ')</a>' + ret + '</div>';
                  },

                  renderDeliverableList : function EntityProjects_renderDeliverableList(oRecord) {

                     var deliverables = oRecord.getData("itemData")["dt_pjt_deliverableList"], idx = 0;

                     var ret = '<ul class="hidden">';

                     for (j in deliverables) {
                        var deliverable = deliverables[j];
                           idx++;
                           ret += "<li>" + this.getDeliverableTitle(deliverable, oRecord.getData().nodeRef) + "</li>";
                     }
                     ret += "</ul>";

                     return '<div ><a class="alfresco-twister alfresco-twister-closed ' + TWISTER_EVENTCLASS + '">' + this
                           .msg("show.deliverables") + '(' + idx + ')</a>' + ret + '</div>';

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

   YAHOO.lang.augmentProto(beCPG.component.EntityProjects, beCPG.component.ProjectCommons);

})();
