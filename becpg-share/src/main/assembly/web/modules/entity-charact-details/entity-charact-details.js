/*******************************************************************************
 *  Copyright (C) 2010-2020 beCPG. 
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
(function()
{

    /**
     * Preferences
     */
    var PREFERENCES_DETAILS_VIEW = "beCPG.module.EntityCharactDetails", PREF_CHART_TYPE = "chartType";

    /**
     * for Path Navigation
     */
    var BAR_EVENTCLASS = Alfresco.util.generateDomId(null, "barItem");

    /**
     * Dashboard EntityCharactDetails constructor.
     * 
     * @param {String}
     *            htmlId The HTML id of the parent element
     * @return {beCPG.module.EntityCharactDetails } The new component instance
     * @constructor
     */
    beCPG.module.EntityCharactDetails = function(fieldHtmlId)
    {
        this.id = fieldHtmlId;

        beCPG.module.EntityCharactDetails.superclass.constructor.call(this, "beCPG.module.EntityCharactDetails", fieldHtmlId, [ "button", "container", "menu", "datasource" ]);

        // Initialise prototype properties
        this.preferencesService = new Alfresco.service.Preferences();

    };

    YAHOO
            .extend(
                    beCPG.module.EntityCharactDetails,
                    Alfresco.component.Base,
                    {

                        /**
                         * dataSource
                         */
                        dataSource : null,
                        
                        /**
                         * dataSource for ccc charts
                         */
                        cDataSource : null,

                        /**
                         * Object container for initialization options
                         * 
                         * @property options
                         * @type object
                         */
                        options :
                        {

                            entityNodeRef : null,

                            backEntityNodeRef : [],

                            navigationPath : [],

                            itemType : null,

                            dataListItems : null,

                            dataListName : null

                        },

                        /**
                         * 
                         * @param menuItem
                         */
                        onChartTypeSelected : function EntityCharactDetails_onChartSelected(menuItem)
                        {
                            var scope = this;
                            if (menuItem)
                            {
                            	
                            	if(menuItem.value != "chartData"){
                            		scope.options.level = '1';
                            	    scope.widgets.levelMenu.set("label", scope.msg("button.level"));
                            	    scope.widgets.levelMenu.set("disabled", true);
                            	} 
                            	
                                scope.widgets.chartTypePicker.value = menuItem.value;
                                scope.preferencesService.set(scope.getPreference(PREF_CHART_TYPE), scope.widgets.chartTypePicker.value,
                                {
                                    successCallback :
                                    {
                                        fn : scope.loadChartData(),
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
                        getPreference : function EntityCharactDetails_getPreference(suffix)
                        {
                            return PREFERENCES_DETAILS_VIEW + (this.options.itemType ? "." + this.options.itemType.replace(":", "_") : "") + (suffix ? "." + suffix : "");
                        },

                        /**
                         * 
                         * @param ev
                         */
                        onChartTypeClicked : function EntityCharactDetails_onChartTypeClicked(ev)
                        {
                            this.loadChartData();
                        },

                        /**
                         * @returns {EntityCharactDetails_onReady}
                         */
                        onReady : function EntityCharactDetails_onReady()
                        {

                            var me = this;

                            this.widgets.chartTypePicker = new YAHOO.widget.Button(me.id + "-chartTypePicker-button",
                            {
                                type : "split",
                                menu : me.id + "-chartTypePicker-select",
                                lazyloadmenu : false
                            });

                            this.widgets.chartTypePicker.on("click", me.onChartTypeClicked, me, true);

                            this.widgets.chartTypePicker.getMenu().subscribe("click", function(p_sType, p_aArgs)
                            {
                                var menuItem = p_aArgs[1];
                                if (menuItem)
                                {
                                    me.widgets.chartTypePicker.set("label", menuItem.cfg.getProperty("text"));
                                    me.onChartTypeSelected.call(me, menuItem);
                                }
                            });

                            this.widgets.exportCSVButton = Alfresco.util.createYUIButton(this, "export-csv", this.onExportCSV,
                            {
                                disabled : false,
                                value : "export"
                            });

                            this.widgets.backButton = Alfresco.util.createYUIButton(this, "back", this.onBack,
                            {
                                disabled : true,
                                value : "back"
                            });

                            this.widgets.levelMenu = new YAHOO.widget.Button(me.id + "-level-button",{
                            	type : "split",
                            	menu : me.id + "-levelbuttonselect",
                            	lazyloadmenu : false,
                            	disabled : true
                            });
                            this.widgets.levelMenu.getMenu().subscribe("click", function(p_sType, p_aArgs){
                            	var levelItem = p_aArgs[1];
                            	if(levelItem){
                            		me.widgets.levelMenu.set("label", levelItem.cfg.getProperty("text"));
                            		me.options.level = levelItem.value;
                            		me.loadChartData();
                            	}
                            });


                            // Load preferences to override default filter and
                            // range

                            this.selectMenuValue(this.widgets.chartTypePicker, "chartData");

                            this.preferencesService.request(me.getPreference(),
                            {
                                successCallback :
                                {
                                    fn : function(p_oResponse)
                                    {

                                        var chartTypePreference = Alfresco.util.findValueByDotNotation(p_oResponse.json, me.getPreference(PREF_CHART_TYPE), null);
                                        if (chartTypePreference !== null)
                                        {
                                            me.selectMenuValue(me.widgets.chartTypePicker, chartTypePreference);

                                        }
                                    },
                                    scope : this
                                }
                            });

                            me.loadChartData();

                            var fnOnBarItemClick = function fnOnBarItemClick(layer, args)
                            {
                                var owner = YAHOO.Bubbling.getOwnerByTagName(args[1].anchor, "span");
                                if (owner !== null)
                                {
                                    var splitted = owner.className.split(" ");
                                    var nodeRef = splitted[0];
                                    me.options.entityNodeRef = nodeRef.replace("bar-","");
                                    
                                    for (var index = 0; index < me.options.navigationPath.length; index++) {
                                       if( me.options.navigationPath[index].nodeRef == me.options.entityNodeRef){
                                           me.options.navigationPath = me.options.navigationPath.slice(0,index+1);
                                           break;
                                        }
                                    }  
                                    
                                    me.loadChartData();
                                    return false;
                                }
                                return true;
                            };

                            YAHOO.Bubbling.addDefaultAction(BAR_EVENTCLASS, fnOnBarItemClick);
                        },

                        onExportCSV : function BulkEdit_onExportCSV()
                        {
                            if (this.options.entityNodeRef != null && this.options.entityNodeRef.length > 0)
                            {
                                window.location = this._buildDetailsUrl("csv");
                            }
                        },

                        onBack : function BulkEdit_onBack()
                        {
                            if (this.options.backEntityNodeRef != null && this.options.backEntityNodeRef.length > 0)
                            {
                                this.options.entityNodeRef = this.options.backEntityNodeRef.pop();
                                this.options.navigationPath.pop();
                                this.loadChartData();
                            }

                            if (this.options.backEntityNodeRef == null || this.options.backEntityNodeRef.length < 1)
                            {
                                this.widgets.backButton.set("disabled", true);
                            }

                        },

                        /**
                         * 
                         * @param picker
                         * @param value
                         */
                        selectMenuValue : function EntityCharactDetails_selectMenuValue(picker, value)
                        {
                            picker.value = value;
                            // set the correct menu label
                            var menuItems = picker.getMenu().getItems();
                            for (index in menuItems)
                            {
                                if (menuItems.hasOwnProperty(index))
                                {
                                    if (menuItems[index].value === value)
                                    {
                                        picker.set("label", menuItems[index].cfg.getProperty("text"));
                                        break;
                                    }
                                }
                            }

                        },

                        /**
                         * 
                         */
                        loadChartData : function EntityCharactDetails_loadChartData()
                        {
                            if (this.options.entityNodeRef != null && this.options.entityNodeRef.length > 0)
                            {
                                Alfresco.util.Ajax.request(
                                {
                                    url : this._buildDetailsUrl("json"),
                                    successCallback :
                                    {
                                        fn : this.processData,
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
                            }
                        },

                        _buildDetailsUrl : function EntityCharactDetails__buildUrl(format)
                        {
                            return Alfresco.constants.PROXY_URI + "becpg/charact/formulate" + (format != null && format.length > 0 ? "." + format : "") + "?entityNodeRef=" + this.options.entityNodeRef + "&itemType=" + this.options.itemType + "&dataListName=" 
                            + this.options.dataListName + "&dataListItems=" + this.options.dataListItems
                            + "&level="+(this.options.level!=null? this.options.level : "1");
                        },
                        /**
                         * 
                         * @param response
                         * @returns {EntityCharactDetails_processData}
                         */
                        processData : function EntityCharactDetails_processData(response)
                        {
                        	

                            var data = response.json;
                            

                            var myFieldDefs = [];
                            this.columnDefs = [];
                            this.seriesDef = [];
                            this.barChartSeriesDef = [];
                            var i = 0;

                            for (i = 0; i < data.metadatas.length; i++)
                            {
                                var colName = data.metadatas[i].colName+(data.metadatas[i].colUnit!=null?" ("+data.metadatas[i].colUnit+")":"");
                                
                                myFieldDefs.push(
                                {
                                    key : "col" + i,
                                    parser : function(oData)
                                    {
                                        if (!YAHOO.lang.isValue(oData) || (oData === ""))
                                        {
                                            return null;
                                        }

                                        // Convert to number
                                        var number = oData * 1;

                                        // Validate
                                        if (YAHOO.lang.isNumber(number))
                                        {
                                            return number.toFixed(4);
                                        }
                                        return oData;
                                    }
                                    
                                });
                                
                                
                                var columnDef = {
                                		key : "col" + i,
                                        label : colName
                                		
                                }
                                	
                                if(i ==0 ){
                                	columnDef.formatter = function (elCell, oRecord, oColumn, oData) {
                                       	
                                    	if(oRecord.getData("cssClass")){
                                    		var padding = oRecord.getData("level") * 25;
                                    		
                                            elCell.innerHTML = '<span class="'+ oRecord.getData("cssClass") + '" style="margin-left:' + padding + 'px;">'+oData+'</span>';
                                        } else {
                                            elCell.innerHTML = '<b>'+oData+'</b>';
                                        }
                                    };
                                } else {
                                	columnDef.formatter = function (elCell, oRecord, oColumn, oData) {
                                		Dom.setStyle(elCell, "text-align", "right");  
                                      	if(oRecord.getData("cssClass")){
                                      		
                                      		if (YAHOO.lang.isNumber(oData))
                                            {
                                      			elCell.innerHTML = (new Intl.NumberFormat(Alfresco.constants.JS_LOCALE.replace("_","-") ,{minimumFractionDigits : 4, maximumFractionDigits : 4 })).format(oData);
                                            } else {
                                            	elCell.innerHTML = oData;
                                            }
                                      	} else {
                                      		elCell.innerHTML = '<b>'+oData+'</b>';
                                      	}
                                	};
                                	
                                }
                                
                                this.columnDefs.push(columnDef);
                                
                                
                                if (i > 0)
                                {
                                    this.seriesDef.push(
                                    {
                                        displayName : colName,
                                        yField : "col" + i
                                    });
                                    this.barChartSeriesDef.push(
                                    {
                                        displayName : colName,
                                        xField : "col" + i
                                    });
                                }
                            }

                            myFieldDefs.push(
                            {
                                key : "nodeRef"
                            });
                            myFieldDefs.push(
                            {
                                key : "type"
                            });
                            myFieldDefs.push(
                            {
                                key : "cssClass"
                            });
                            myFieldDefs.push(
                             {
                                 key : "level"
                             });
                    
                            if(this.widgets.chartTypePicker.value != "chartData"){
                               //Remove totals
                                data.resultsets.pop();
                            }
                            
                            this.cDataSource = {};
                            this.cDataSource.metadata = data.metadatas;
                            
                            this.cDataSource.resultset = data.resultsets;
                            
                            this.dataSource = new YAHOO.util.DataSource(data.resultsets);
                            this.dataSource.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;
                            this.dataSource.responseSchema =
                            {
                                fields : myFieldDefs
                            };
                          

                            this.render();

                        },
                        
                        /**
                         * On chart click event handler 
                         */
                        onClickChart : function EntityCharactDetails_onClickChart(scene, me){   
                        	var result = me.cDataSource.resultset.filter(function (obj) {
                        		return obj[0] === scene.firstAtoms.category.value;
                        	});
                        	if( result[0][me.cDataSource.metadata.length+1].indexOf("Material") < 0){
                        		var itemNodeRef = result[0][me.cDataSource.metadata.length];
                        		me.options.backEntityNodeRef.push(me.options.entityNodeRef);
                        		me.options.navigationPath.push({"name":scene.firstAtoms.category.value,"nodeRef":itemNodeRef,"cssClass":result[0][me.cDataSource.metadata.length+2]});
                        		me.options.entityNodeRef = itemNodeRef;
                        		me.widgets.backButton.set("disabled", false);
                        		me.loadChartData();
                        	}
                        },
                        
                        /**
                         * Show toolTip message 
                         */
                        showTooltipMessage : function OlapChart_render(scene){
                      	  return scene.vars.series.label + 
                  	       "<br /> " + scene.vars.category.label + 
                  	       "<br /> "    + scene.vars.value.value ;
                        },
                    
                        /**
                         * Render Details Chart
                         */
                        render : function EntityCharactDetails_render()
                        {
                         var me = this;
                         require(dojoConfig,["bccc"], function(pvc){
                            if (me.dataSource != null)
                            {
                            	var elWidth = document.getElementById(me.id+"-chartContainer").offsetWidth - 10;
                            	var elHeight = document.getElementById(me.id+"-chartContainer").offsetHeight;
								elHeight = Math.max(elHeight, 500);
                            	
                                if (me.widgets.chartTypePicker.value == "lineChart")
                                {
                                	 new pvc.LineChart({
                                	        canvas: me.id + "-chart",
                                	        width:  elWidth,
                                	        height: 500,
                                	        dotsVisible: true,
                                	        baseAxisSize: 40,
                                	        baseAxisDomainRoundMode: 'nice',
                                	        // Panels/legend
                                    	    legend: true,
                                    	    legendPosition: 'bottom',
                                            legendAlign: 'center',
                                            legendDot_shape: 'circle',
                                	        animate:    true,
                                	        selectable: true,
                                	        hoverable:  true,
                                	        clickable: true,
                                    	    clickAction: function(scene) { 
                                    	    	me.onClickChart(scene, me);
                                    	    },
                                    	    tooltipFormat : function(scene){
                                	        	return me.showTooltipMessage(scene);
                                	        }
                                	    })
                                	    .setData(me.cDataSource, {crosstabMode: true})
                                	    .render();

                                }
                                else if (me.widgets.chartTypePicker.value == "barChart")
                                {
                                	new pvc.BarChart({
                                	    canvas: me.id + "-chart",
                                	    width:  elWidth,
                                	    height: elHeight,
                                	    orientation: 'horizontal',
                                	    axisGrid: true,
                                	    axisGrid_strokeStyle: '#F7F8F9',
                                	    axisLabel_font: 'normal 10px "Open Sans"',
                                	    legend: true,
                                	    legendPosition: 'bottom',
                                        legendAlign: 'center',
                                        panelSizeRatio: 0.3,
                                	    animate:    true,
                                	    selectable: true,
                                	    hoverable:  true,
                                	    clickable: true,
                                	    clickAction: function(scene) { 
                                	    	me.onClickChart(scene, me);
                                	    },
                                	    tooltipFormat : function(scene){
                            	        	return me.showTooltipMessage(scene);
                            	        }

                                	})
                                	.setData(me.cDataSource, {crosstabMode: true})
                                	.render();

                                }
                                else if (me.widgets.chartTypePicker.value == "columnChart")
                                {
                                	 new pvc.BarChart({
                                		 canvas: me.id + "-chart",
                                		 width:  elWidth,
                                		 height: elHeight,
                                		 legend: true,
                                		 legendPosition: 'bottom',
                                		 legendAlign: 'center',
                                		 panelSizeRatio: 0.3,
                                		 animate: true,
                                		 baseAxisGrid: true,
                                		 selectable: true,
                                		 clickable: true,
                                		 clickAction: function(scene) {  
                                			 me.onClickChart(scene, me);
                                		 },
                                		 tooltipFormat : function(scene){
                             	        	return me.showTooltipMessage(scene);
                                		 }
                                	    })
                                	    .setData(me.cDataSource, {crosstabMode: true})
                                	    .render();

                                }
                                else if (me.widgets.chartTypePicker.value == "pieChart")
                                {
                                	new pvc.PieChart({
                                	    canvas: me.id + "-chart",
                                	    width:  elWidth,
                                	    height: 500,
                                	    valuesVisible: true,
                                	    valuesFont: 'lighter 11px "Open Sans"',
                                	    explodedSliceRadius: '10%',
                                	    slice_offsetRadius: function(scene) {
                                	        return scene.isSelected() ? '10%' : 0;
                                	    },
                                	    //multiChartIndexes: 1,
                                	    legend: true,
                                	    legendPosition: 'right',
                                        legendAlign: 'center',
                                        legendDot_shape: 'circle',
                                        legendLabel_textStyle: function(scene) {
                                            var colorScale = me.panel.axes.color.scale;
                                            return colorScale(me.getValue());
                                        },
                                	    selectable: true,
                                	    hoverable:  true,
                                	    clickable: true,
                                	    clickAction: function(scene) { 
                                	    	me.onClickChart(scene, me);
                                	    },
                                	    tooltipFormat : function(scene){
                             	        	return me.showTooltipMessage(scene);
                                		 }
                                	})
                                	.setData(me.cDataSource, {crosstabMode: true})
                                	.render();
                                }
                                else if (me.widgets.chartTypePicker.value == "chartData")
                                {
                                	me.widgets.levelMenu.set("disabled", false);
                                	
                                    me.widgets.chart = null;
                                    me.widgets.dataTable = new YAHOO.widget.DataTable(me.id + "-chart", me.columnDefs, me.dataSource);
                                }
                                
                                Dom.setStyle(me.id + "-chart", "display", "flex");

                                if (me.widgets.chart != null)
                                {
                                    me.widgets.chart.subscribe("itemClickEvent", function(event)
                                    {
                                        if (event.item.type.indexOf("Material") < 0)
                                        {
                                            me.options.backEntityNodeRef.push(me.options.entityNodeRef);
                                            me.options.navigationPath.push({"name":event.item.col0,"nodeRef":event.item.nodeRef,"cssClass":event.item.cssClass});
                                            me.options.entityNodeRef = event.item.nodeRef;
                                            me.widgets.backButton.set("disabled", false);
                                            me.loadChartData();
                                        }
                                    });
                                }

                                if (me.widgets.dataTable != null)
                                {
                                    me.widgets.dataTable.subscribe("cellClickEvent", function(events)
                                    {
                                        var target = events.target;
                                        var recordRowIndex = this.getRecordIndex(target);
                                        var recordSet = this.getRecordSet(), record;
                                        record = recordSet.getRecord(recordRowIndex);
                                        if ((record.getData("type")).indexOf("Material") < 0)
                                        {
                                            me.options.backEntityNodeRef.push(me.options.entityNodeRef);
                                            me.options.navigationPath.push({"name":record.getData("col0"),"nodeRef":record.getData("nodeRef"),"cssClass":record.getData("cssClass")});
                                            me.options.entityNodeRef = record.getData("nodeRef");
                                            me.widgets.backButton.set("disabled", false);
                                            me.loadChartData();
                                        }
                                    });
                                }

                                if (me.options.navigationPath != null && me.options.navigationPath.length > 0)
                                {
                                    var html = "";
                                    for (var i = 0; i < me.options.navigationPath.length; i++)
                                    {
                                        html += '<span class="separator"> > </span><span class="bar-'+me.options.navigationPath[i].nodeRef+' ' 
                                        + me.options.navigationPath[i].cssClass + '">'
                                        
                                        + '<a href="#" class="'+BAR_EVENTCLASS+'" >' 
                                        + me.options.navigationPath[i].name + '</a>' + '</span>';

                                    }
                                    Dom.get(me.id + "-chartPath").innerHTML = html;
                                    Dom.setStyle(me.id + "-chartPath", "visibility", "inherit");

                                } else {
                                    Dom.setStyle(me.id + "-chartPath", "visibility", "hidden");
                                }
                                
                                Dom.setStyle(me.id + "-chart", "visibility", "inherit");

                            }
                        });
                     }

                    });

})();
