/**
 * ProjectListToolbar component.
 * 
 * @namespace Alfresco
 * @class beCPG.component.ProjectListToolbar
 */
(function() {
   /**
    * YUI Library aliases
    */
   var Dom = YAHOO.util.Dom, Selector = YAHOO.util.Selector;

   /**
    * ProjectListToolbar constructor.
    * 
    * @param {String}
    *            htmlId The HTML id of the parent element
    * @return {beCPG.component.ProjectListToolbar} The new WorkflowListToolbar instance
    * @constructor
    */
   beCPG.component.ProjectListToolbar = function ProjectListToolbar_constructor(htmlId) {

      beCPG.component.ProjectListToolbar.superclass.constructor.call(this, "beCPG.component.ProjectListToolbar",
            htmlId, [ "button" ]);
      return this;
   };

   YAHOO
         .extend(
               beCPG.component.ProjectListToolbar,
               Alfresco.component.Base,
               {

                  options : {
                     view : "dataTable",
                     siteId : ""
                  },

                  /**
                   * Fired by YUI when parent element is available for scripting. Component initialisation, including
                   * instantiation of YUI widgets and event listener binding.
                   * 
                   * @method onReady
                   */
                  onReady : function PTL_onReady() {

                     this.widgets.showGanttButton = Alfresco.util.createYUIButton(this, "show-gantt-button",
                           this.onGanttButtonClick);
                     this.widgets.showPlanningButton = Alfresco.util.createYUIButton(this, "show-planning-button",
                           this.onPlanningButtonClick);
                     this.widgets.showResourcesButton = Alfresco.util.createYUIButton(this, "show-resources-button",
                             this.onResourcesButtonClick);
                     
                     this.widgets.exportProjectList = Alfresco.util.createYUIButton(this, "export-csv-button",
                           this.onExportProjectList);

                     if (Dom.get(this.id + "-reporting-menu-button")) {
                        // menu button for export options
                        this.widgets.reportingMenu = Alfresco.util.createYUIButton(this, "reporting-menu-button",
                              this.onReportingMenuClick, {
                           type : "menu",
                           menu : "reporting-menu",
                           lazyloadmenu : true,
                           disabled : true
                        });
                           
                           
                        this.widgets.reportingMenu.set("label", this.msg("button.download-report")+ " " + Alfresco.constants.MENU_ARROW_SYMBOL);
                        
                        // event handler for export menu

                        Alfresco.util.Ajax
                              .request({
                                 url : Alfresco.constants.PROXY_URI + "/becpg/report/exportsearch/templates/pjt:project",
                                 successCallback : {
                                    fn : function(response) {

                                       var me = this, json = response.json, items = [];

                                       if (json !== null) {
                                          for ( var i in json.reportTpls) {
                                             items.push({
                                                text : json.reportTpls[i].name,
                                                value : json.reportTpls[i].nodeRef+"#"+json.reportTpls[i].name + "." + json.reportTpls[i].format.toLowerCase(),
                                             });
                                          }
                                          if(items.length>0){
                                             me.widgets.reportingMenu.getMenu().addItems(items);
                                             me.widgets.reportingMenu.getMenu().render(document.body);
                                             me.widgets.reportingMenu.set("disabled", false);
                                          }
                                       }

                                    },
                                    scope : this
                                 },
                                 failureMessage : "Could not get project reports '" + Alfresco.constants.PROXY_URI + "/becpg/report/exportsearch/templates/pjt:project" + "'.",
                              });

                     }

                     if (this.options.view == "gantt") {
                        this.widgets.showGanttButton.set("disabled", true);
                     }  else if (this.options.view == "resources") { 
                         this.widgets.showResourcesButton.set("disabled", true);
                     } else {
                        this.widgets.showPlanningButton.set("disabled", true);
                     }

                     Dom.removeClass(Selector.query(".hidden", this.id + "-body", true), "hidden");
                  },

                  onReportingMenuClick : function PTL_onReportingMenuClick(p_sType, p_aArgs) {
                     var menuItem = p_aArgs[1];
                     if (menuItem) {

                        var values = menuItem.value.split("#");
                        
                        var url = Alfresco.constants.PROXY_URI + "becpg/report/exportsearch/" + values[0].replace(
                              "://", "/") + "/" + encodeURIComponent(values[1]);

                        // add search data webscript arguments
                        url += "?term=&query=" + encodeURIComponent('{datatype:"pjt:project"}');

                        if (this.options.siteId.length !== 0) {
                           url += "&site=" + this.options.siteId + "&repo=false";
                        } else {
                           url += "&site=&repo=true";
                        }

                        document.location.href = url;

                     }
                  },

                  onGanttButtonClick : function PTL_onGanttButtonClick(e, p_obj) {
                     document.location.href = Alfresco.util
                           .siteURL("project-list?view=gantt"+window.location.hash);
                  },
                  onResourcesButtonClick : function PTL_onGanttButtonClick(e, p_obj) {
                      document.location.href = Alfresco.util
                            .siteURL("project-list?view=resources"+window.location.hash);
                   },
                  onPlanningButtonClick : function PTL_onPlanningButtonClick(e, p_obj) {
                     document.location.href = Alfresco.util.siteURL("project-list"+window.location.hash);
                  },
                  onExportProjectList : function PTL_onExportProjectList(e, p_obj) {
                     var dt = Alfresco.util.ComponentManager.find({
                        name : "beCPG.module.EntityDataGrid"
                     })[0];
                     
                     
                     Alfresco.util.Ajax
                     .jsonGet({
                        url : dt._getColumnUrl("export"),
                        successCallback : {
                           fn : function(response) {
                              

                              var requestParams = {
                                 fields : [],
                                 filter : dt.currentFilter,
                                 page : 1
                              };
                              
                              requestParams.filter.filterParams = dt._createFilterURLParameters(dt.currentFilter, dt.options.filterParameters);

                              for ( var i = 0, ii = response.json.columns.length; i < ii; i++) {
                                 var column = response.json.columns[i], columnName = column.name.replace(":", "_");
                                 if (column.dataType == "nested" && column.columns) {
                                    for ( var j = 0; j < column.columns.length; j++) {                                             
                                       var col = column.columns[j];                                            
                                       columnName += "|" + col.name.replace(":", "_");                                             
                                    }
                                 }

                                 requestParams.fields.push(columnName);
                              }

                              var PAGE_SIZE = 5000;
                 
                              document.location.href = dt._getDataUrl(PAGE_SIZE) + "&format=xlsx&metadata=" + encodeURIComponent(YAHOO.lang.JSON
                                    .stringify(requestParams));

                           },
                           scope : this
                        }
                     });
                     
                  }

               });

})();
