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
 * OlapGraph component.
 * 
 * @author "Matthieu Laborie <matthieu.laborie@becpg.fr>
 * @namespace beCPG
 * @class beCPG.component.OlapGraph
 */
(function() {

   /**
    * YUI Chart SWF
    */
   YAHOO.widget.Chart.SWFURL = Alfresco.constants.URL_CONTEXT + "res/yui/charts/assets/charts.swf";

   /**
    * Preferences
    */
   var PREFERENCES_OLAP = "fr.becpg.olap.chart.dashlet", PREF_QUERY = "query", PREF_CHART_TYPE = "chartType";

   /**
    * Dashboard OlapChart constructor.
    * 
    * @param {String}
    *            htmlId The HTML id of the parent element
    * @return {beCPG.component.OlapChart } The new component instance
    * @constructor
    */
   beCPG.dashlet.OlapChart = function(fieldHtmlId) {
      this.id = fieldHtmlId;

      beCPG.dashlet.OlapChart.superclass.constructor.call(this, "beCPG.dashlet.OlapChart", fieldHtmlId, [ "button",
            "container", "menu", "datasource" ]);

      // Initialise prototype properties
      this.preferencesService = new Alfresco.service.Preferences();

   };

   YAHOO.extend(beCPG.dashlet.OlapChart, Alfresco.component.Base, {

      /**
       * dataSource
       */
      dataSource : null,

      /**
       * saiku-ui
       */
      saikuUrl : null,

      /**
       * currentUser
       */

      saikuUser : null,

      /**
       * Object container for initialization options
       * 
       * @property options
       * @type object
       */
      options : {

         /**
          * Current siteId.
          * 
          * @property siteId
          * @type string
          */
         siteId : "",

         /**
          * Component region ID.
          * 
          * @property regionId
          * @type string
          */
         regionId : ""
      },

      /**
       * @param menuItem
       */
      onChartSelected : function OlapChart_onChartSelected(menuItem) {
         var scope = this;
         if (menuItem) {
            scope.chartPicker.value = encodeURIComponent(menuItem.value);
            scope.preferencesService.set(scope.getPreference(PREF_QUERY), scope.chartPicker.value, {
               successCallback : {
                  fn : scope.onChartClicked(scope),
                  scope : this
               }
            });
         }

      },

      /**
       * @param ev
       */
      onChartClicked : function(ev) {
         this.loadChartData();
      },
      /**
       * @param menuItem
       */
      onChartTypeSelected : function OlapChart_onChartSelected(menuItem) {
         var scope = this;
         if (menuItem) {
            scope.chartTypePicker.value = menuItem.value;
            scope.preferencesService.set(scope.getPreference(PREF_CHART_TYPE), scope.chartTypePicker.value, {
               successCallback : {
                  fn : scope.render(),
                  scope : this
               }
            });
         }
      },
      /**
       * @param suffix
       * @returns {String}
       */
      getPreference : function OlapChart_getPreference(suffix) {
         var opt = this.options;
         return PREFERENCES_OLAP + "." + opt.regionId + (opt.siteId ? ("." + opt.siteId) : "") + (suffix ? "." + suffix
               : "");

      },

      /**
       * @param ev
       */
      onChartTypeClicked : function OlapChart_onChartTypeClicked(ev) {
         this.render();
      },

      /**
       * @returns {OlapChart_onReady}
       */
      onReady : function OlapChart_onReady() {

         var me = this;

         this.chartPicker = new YAHOO.widget.Button(me.id + "-charPicker-button", {
            type : "split",
            menu : me.id + "-charPicker-select",
            lazyloadmenu : true
         });

         this.chartTypePicker = new YAHOO.widget.Button(me.id + "-chartTypePicker-button", {
            type : "split",
            menu : me.id + "-chartTypePicker-select",
            lazyloadmenu : false
         });

         this.chartPicker.on("click", me.onChartClicked, me, true);
         this.chartTypePicker.on("click", me.onChartTypeClicked, me, true);

         this.chartPicker.getMenu().subscribe("click", function(p_sType, p_aArgs) {
            var menuItem = p_aArgs[1];
            if (menuItem) {
               me.chartPicker.set("label", menuItem.cfg.getProperty("text"));
               me.onChartSelected.call(me, menuItem);
            }
         });

         this.chartTypePicker.getMenu().subscribe("click", function(p_sType, p_aArgs) {
            var menuItem = p_aArgs[1];
            if (menuItem) {
               me.chartTypePicker.set("label", menuItem.cfg.getProperty("text"));
               me.onChartTypeSelected.call(me, menuItem);
            }
         });

         Alfresco.util.Ajax.request({
            url : Alfresco.constants.PROXY_URI + "becpg/olap/chart",
            successCallback : {
               fn : me.fillQueries,
               scope : this
            },
            failureCallback : {
               fn : function() {
                  // DO nothing
               },
               scope : this
            }
         });

      },

      /**
       * @param response
       */
      fillQueries : function OlapChart_fillQueries(response) {

         var me = this, json = response.json,items = [],firstQueryId = "";

         if (json !== null) {
            for (var i in json.queries) {
               if (i === 0) {
                  firstQueryId = json.queries[i].queryId;
               }
               items.push({
                  text : json.queries[i].queryName,
                  value : json.queries[i].queryId
               });
            }
            me.chartPicker.getMenu().addItems(items);
            me.chartPicker.getMenu().render(document.body);
            me.selectMenuValue(me.chartPicker, encodeURIComponent(firstQueryId));

            me.saikuUrl = json.metadata.olapServerUrl;
            me.saikuUser = json.metadata.currentUserName;

         }

         // Load preferences to override default filter and range
         me.selectMenuValue(me.chartTypePicker, "barChart");

         this.preferencesService.request(me.getPreference(), {
            successCallback : {
               fn : function(p_oResponse) {
                  var queryPreference = Alfresco.util.findValueByDotNotation(p_oResponse.json, me
                        .getPreference(PREF_QUERY), null);
                  if (queryPreference !== null) {
                     me.selectMenuValue(me.chartPicker, queryPreference);
                  }

                  var chartTypePreference = Alfresco.util.findValueByDotNotation(p_oResponse.json, me
                        .getPreference(PREF_CHART_TYPE), null);
                  if (chartTypePreference !== null) {
                     me.selectMenuValue(me.chartTypePicker, chartTypePreference);

                  }
                  me.loadChartData();
               },
               scope : this
            },
            failureCallback : {
               fn : function() {
                  me.loadChartData();
               },
               scope : this
            }
         });

      },

      /**
       * @param picker
       * @param value
       */
      selectMenuValue : function OlapChart_selectMenuValue(picker, value) {
         picker.value = value;
         // set the correct menu label
         var menuItems = picker.getMenu().getItems();
         for (var index in menuItems) {
            if (menuItems.hasOwnProperty(index)) {
               if (menuItems[index].value === value) {
                  picker.set("label", menuItems[index].cfg.getProperty("text"));
                  break;
               }
            }
         }

      },

      /**
       * 
       */
      loadChartData : function OlapChart_loadChartData() {
         if (this.chartPicker.value !== null && this.chartPicker.value.length > 0) {
            Alfresco.util.Ajax.request({
               url : Alfresco.constants.PROXY_URI + "becpg/olap/chart?olapQueryId=" + this.chartPicker.value,
               successCallback : {
                  fn : this.processData,
                  scope : this
               },
               failureCallback : {
                  fn : function() {
                     // DO nothing
                  },
                  scope : this
               }
            });
         }
      },
      /**
       * @param response
       * @returns {OlapChart_processData}
       */
      processData : function OlapChart_processData(response) {

         this.data = response.json;

         var myFieldDefs = [];
         this.columnDefs = [];
         this.seriesDef = [];
         this.barChartSeriesDef = [];

         for (var i in this.data.metadatas) {
            myFieldDefs.push("col" + i);
            this.columnDefs.push({
               key : "col" + i,
               label : this.data.metadatas[i].colName
            });
            if (i > 0) {
               this.seriesDef.push({
                  displayName : this.data.metadatas[i].colName,
                  yField : "col" + i
               });
               this.barChartSeriesDef.push({
                  displayName : this.data.metadatas[i].colName,
                  xField : "col" + i
               });
            }
         }

         this.dataSource = new YAHOO.util.DataSource(this.data.resultsets);
         this.dataSource.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;
         this.dataSource.responseSchema = {
            fields : myFieldDefs
         };

         this.render();

      },
      /**
       * Render OLAP Chart
       */
      render : function OlapChart_render() {

         if (this.dataSource !== null) {

            if (this.chartTypePicker.value == "lineChart") {
               new YAHOO.widget.LineChart(this.id + "-chart", this.dataSource, {
                  series : this.seriesDef,
                  xField : "col0",
                  wmode : "opaque",
                  style : {
                     legend : {
                        display : "bottom"
                     }
                  }
               });

            } else if (this.chartTypePicker.value == "barChart") {

               new YAHOO.widget.BarChart(this.id + "-chart", this.dataSource, {
                  series : this.barChartSeriesDef,
                  yField : "col0",
                  wmode : "opaque",
                  style : {
                     legend : {
                        display : "bottom"
                     }
                  }
               });

            } else if (this.chartTypePicker.value == "columnChart") {
               new YAHOO.widget.ColumnChart(this.id + "-chart", this.dataSource, {
                  series : this.seriesDef,
                  xField : "col0",
                  wmode : "opaque",
                  style : {
                     legend : {
                        display : "bottom"
                     }
                  }
               });

            } else if (this.chartTypePicker.value == "pieChart") {
               new YAHOO.widget.PieChart(this.id + "-chart", this.dataSource, {
                  dataField : "col1",
                  categoryField : "col0",
                  wmode : "opaque",
                  style : {
                     legend : {
                        display : "right"
                     }
                  }
               });
            } else if (this.chartTypePicker.value == "chartData") {
               new YAHOO.widget.DataTable(this.id + "-chart", this.columnDefs, this.dataSource);
            }
         }

      },
      /**
       * 
       */
      openSaikuClick : function OlapChart_openSaikuClick() {
         if (this.saikuUrl !== null) {
            window.open(this.saikuUrl + "?username=" + this.saikuUser);
         }
      }

   });

})();
