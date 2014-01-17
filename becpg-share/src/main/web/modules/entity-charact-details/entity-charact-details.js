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
 * EntityCharact Details module.
 * 
 * @author "Matthieu Laborie <matthieu.laborie@becpg.fr
 * @namespace beCPG
 * @class beCPG.module.EntityCharactDetails
 */
(function() {

	/**
	 * YUI Chart SWF
	 */
	YAHOO.widget.Chart.SWFURL = Alfresco.constants.URL_CONTEXT + "res/yui/charts/assets/charts.swf";

	/**
	 * Preferences
	 */
	var PREFERENCES_DETAILS_VIEW = "beCPG.module.EntityCharactDetails", PREF_CHART_TYPE = "chartType";

	/**
	 * Dashboard EntityCharactDetails constructor.
	 * 
	 * @param {String}
	 *           htmlId The HTML id of the parent element
	 * @return {beCPG.module.EntityCharactDetails } The new component instance
	 * @constructor
	 */
	beCPG.module.EntityCharactDetails = function(fieldHtmlId) {
		this.id = fieldHtmlId;

		beCPG.module.EntityCharactDetails.superclass.constructor.call(this, "beCPG.module.EntityCharactDetails",
		      fieldHtmlId, [ "button", "container", "menu", "datasource" ]);

		// Initialise prototype properties
		this.preferencesService = new Alfresco.service.Preferences();

	};

	YAHOO.extend(beCPG.module.EntityCharactDetails, Alfresco.component.Base, {

	   /**
		 * dataSource
		 */
	   dataSource : null,

	   /**
		 * Object container for initialization options
		 * 
		 * @property options
		 * @type object
		 */
	   options : {

	      entityNodeRef : null,

	      itemType : null,

	      dataListItems : null,

	      dataListName : null

	   },

	   /**
		 * 
		 * @param menuItem
		 */
	   onChartTypeSelected : function EntityCharactDetails_onChartSelected(menuItem) {
		   var scope = this;
		   if (menuItem) {
			   scope.widgets.chartTypePicker.value = menuItem.value;
			   scope.preferencesService.set(scope.getPreference(PREF_CHART_TYPE), scope.widgets.chartTypePicker.value, {
				   successCallback : {
				      fn : scope.render(),
				      scope : this
				   }
			   });
		   }
	   },
	   /**
		 * 
		 * @param suffix
		 * @returns {String}
		 */
	   getPreference : function EntityCharactDetails_getPreference(suffix) {
		   return PREFERENCES_DETAILS_VIEW + (this.options.itemType ? "." + this.options.itemType : "") + (suffix ? "." + suffix : "");

	   },

	   /**
		 * 
		 * @param ev
		 */
	   onChartTypeClicked : function EntityCharactDetails_onChartTypeClicked(ev) {
		   this.render();
	   },

	   /**
		 * @returns {EntityCharactDetails_onReady}
		 */
	   onReady : function EntityCharactDetails_onReady() {

		   var me = this;

		   this.widgets.chartTypePicker = new YAHOO.widget.Button(me.id + "-chartTypePicker-button", {
		      type : "split",
		      menu : me.id + "-chartTypePicker-select",
		      lazyloadmenu : false
		   });

		   this.widgets.chartTypePicker.on("click", me.onChartTypeClicked, me, true);

		   this.widgets.chartTypePicker.getMenu().subscribe("click", function(p_sType, p_aArgs) {
			   var menuItem = p_aArgs[1];
			   if (menuItem) {
				   me.widgets.chartTypePicker.set("label", menuItem.cfg.getProperty("text"));
				   me.onChartTypeSelected.call(me, menuItem);
			   }
		   });
		   

			this.widgets.exportCSVButton = Alfresco.util.createYUIButton(this, "export-csv",
					this.onExportCSV, {
						disabled : false,
						value : "export"
			});

		   // Load preferences to override default filter and range
		   me.selectMenuValue(me.widgets.chartTypePicker, "barChart");

		   this.preferencesService.request(me.getPreference(), {
		      successCallback : {
		         fn : function(p_oResponse) {

			         var chartTypePreference = Alfresco.util.findValueByDotNotation(p_oResponse.json, me
			               .getPreference(PREF_CHART_TYPE), null);
			         if (chartTypePreference !== null) {
				         me.selectMenuValue(me.widgets.chartTypePicker, chartTypePreference);

			         }
		         },
		         scope : this
		      }
		   });

		   me.loadChartData();
		   
	   },
	   /**
	    * 
	    */
	   onExportCSV : function BulkEdit_onExportCSV() {
	   	  if (this.options.entityNodeRef != null && this.options.entityNodeRef.length > 0) {
				window.location = this._buildDetailsUrl("csv");
	   	  }
		},
	   /**
		 * 
		 * @param picker
		 * @param value
		 */
	   selectMenuValue : function EntityCharactDetails_selectMenuValue(picker, value) {
		   picker.value = value;
		   // set the correct menu label
		   var menuItems = picker.getMenu().getItems();
		   for (index in menuItems) {
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
	   loadChartData : function EntityCharactDetails_loadChartData() {
		   if (this.options.entityNodeRef != null && this.options.entityNodeRef.length > 0) {
		   	
			   Alfresco.util.Ajax.request({
			      url : this._buildDetailsUrl("json"),
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
	   
	   _buildDetailsUrl : function EntityCharactDetails__buildUrl(format){
	   	return Alfresco.constants.PROXY_URI + "becpg/charact/formulate"
	   	+ (format!=null && format.length>0 ? "."+format : "")
   		+"?entityNodeRef="+ this.options.entityNodeRef
   		+"&itemType=" + this.options.itemType 
   		+ "&dataListName="+this.options.dataListName
         + "&dataListItems="+this.options.dataListItems;
	   },
	   /**
		 * 
		 * @param response
		 * @returns {EntityCharactDetails_processData}
		 */
	   processData : function EntityCharactDetails_processData(response) {

		   var data = response.json;

		   var myFieldDefs = [];
		   this.columnDefs = [];
		   this.seriesDef = [];
		   this.barChartSeriesDef = [];

		   for (i in data.metadatas) {
			   myFieldDefs.push({key : "col" + i ,
                  parser : function(oData) {
                     if(!YAHOO.lang.isValue(oData) || (oData === "")) {
                        return null;
                    }

                    //Convert to number
                    var number = oData * 1;
                   
                    // Validate
                    if(YAHOO.lang.isNumber(number)) {
                        return Math.floor(number*10000)/10000;
                    }
                    return oData;
                }});
			   this.columnDefs.push({
			      key : "col" + i,
			      label : data.metadatas[i].colName
			   });
			   if (i > 0) {
				   this.seriesDef.push({
				      displayName : data.metadatas[i].colName,
				      yField : "col" + i
				   });
				   this.barChartSeriesDef.push({
				      displayName : data.metadatas[i].colName,
				      xField : "col" + i
				   });
			   }
		   }

		   this.dataSource = new YAHOO.util.DataSource(data.resultsets);
		   this.dataSource.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;
		   this.dataSource.responseSchema = {
			   fields : myFieldDefs
		   };

		   this.render();

	   },
	   /**
		 * Render Details Chart
		 */
	   render : function EntityCharactDetails_render() {

		   if (this.dataSource != null) {

			   if (this.widgets.chartTypePicker.value == "lineChart") {
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

			   } else if (this.widgets.chartTypePicker.value == "barChart") {

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

			   } else if (this.widgets.chartTypePicker.value == "columnChart") {
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

			   } else if (this.widgets.chartTypePicker.value == "pieChart") {
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
			   } else if (this.widgets.chartTypePicker.value == "chartData") {
				   new YAHOO.widget.DataTable(this.id + "-chart", this.columnDefs, this.dataSource);
			   }
		   }

	   }

	});

})();
