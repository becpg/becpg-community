
/**
 * OlapGraph component.
 * 
 * @namespace beCPG
 * @class beCPG.component.OlapGraph
 */
(function()
{
   /**
	 * YUI Library aliases
	 */
   var Dom = YAHOO.util.Dom,
      Event = YAHOO.util.Event,
      Lang = YAHOO.util.Lang,
      Element = YAHOO.util.Element;
   
   /**
    * YUI Chart SWF
    */
   YAHOO.widget.Chart.SWFURL = Alfresco.constants.URL_CONTEXT+"/res/yui/charts/assets/charts.swf";

   /**
    * Preferences
    */
   var PREFERENCES_OLAP = "fr.becpg.olap.chart.dashlet",
       PREF_QUERY = "query",
       PREF_CHART_TYPE ="chartType";
   
 
   
   beCPG.component.OlapChart = function(fieldHtmlId,instanceId) {
      this.id = fieldHtmlId;
      this.instanceId = instanceId;
      
      Alfresco.util.YUILoaderHelper.require(["button", "container", "datasource"], this.onReady, this);
      
      // Initialise prototype properties
      this.preferencesService = new Alfresco.service.Preferences();
      

      
   } ;
   
    YAHOO.extend(beCPG.component.OlapChart, Alfresco.component.Base,{
   
    	onChartSelected : function OlapChart_onChartSelected(menuItem){
    		
    		 this.chartPicker.value = encodeURIComponent(menuItem.value);
    		 this.preferencesService.set(this.getPreference(PREF_QUERY), this.chartPicker.value);
    		 this.onChartClicked(this);
    		
    	},
    	onChartClicked : function (ev){
	 		  this.loadChartData();
    	},
    	onChartTypeSelected : function OlapChart_onChartSelected(menuItem){	
   		 this.chartTypePicker.value = menuItem.value;
   		 this.preferencesService.set(this.getPreference(PREF_CHART_TYPE), this.chartTypePicker.value);
   		 this.render();
   		
	   	},
	   	getPreference : function(suffix){
	   	   var ret = PREFERENCES_OLAP+"."+this.instanceId.replace(/\.|-|~/g,"");
	   	   	if(suffix!=null){
	   	   		ret+="."+suffix
	   	   	}								
	   		return ret;
	   	},
	   	onChartTypeClicked : function (ev){
	   		this.render();
	   	},
        onReady: function OlapChart_onReady()
        {

            var me = this;
         
            this.chartPicker = new YAHOO.widget.Button(me.id + "-charPicker-button",
                        {
                           type: "split",
                           menu: me.id + "-charPicker-select",
                           lazyloadmenu: false
                        });
            
            
            this.chartTypePicker = new YAHOO.widget.Button(me.id + "-chartTypePicker-button",
                    {
                       type: "split",
                       menu: me.id + "-chartTypePicker-select",
                       lazyloadmenu: false
                    });

            this.chartPicker.on("click", me.onChartClicked, me, true);
            this.chartTypePicker.on("click", me.onChartTypeClicked, me, true);    
	           
            this.chartPicker.getMenu().subscribe("click", function (p_sType, p_aArgs)
                {
                     var menuItem = p_aArgs[1];
                     if (menuItem)
                     {
                         me.chartPicker.set("label", menuItem.cfg.getProperty("text"));
                         me.onChartSelected.call(me, menuItem);
                     }
                 });
         

            this.chartTypePicker.getMenu().subscribe("click", function (p_sType, p_aArgs)
            {
                 var menuItem = p_aArgs[1];
                 if (menuItem)
                 {
                	 me.chartTypePicker.set("label", menuItem.cfg.getProperty("text"));
                     me.onChartTypeSelected.call(me, menuItem);
                 }
             });
           	
            
            
                Alfresco.util.Ajax.request(
                        {
                           url: Alfresco.constants.PROXY_URI + "becpg/olap/chart",
                           successCallback:
                           {
                              fn: me.fillQueries,
                              scope: this
                           },
	                        failureCallback:
	     	               {
	     	                  fn: function()
	     	                  {
	     	                	//DO nothing
	     	                  },
	     	                  scope: this
	     	               }
                 });
                
    
        },fillQueries :  function(response){
        	
        	var me = this,
        		json = response.json;
        	
           	  if(json!=null){
           		  var items = []; 
           		  var firstQueryId = "";
           		  for(i in json.queries){
           			  if(i==0){
           				firstQueryId =json.queries[i].queryId;
           			  }
           			  items.push({text:json.queries[i].queryName,value:json.queries[i].queryId})
           		  }
           		  this.chartPicker.getMenu().addItems(items);
           	      this.chartPicker.getMenu().render(document.body);
           	      me.selectMenuValue(me.chartPicker,encodeURIComponent(firstQueryId));
           	  }
        	
           	  
              // Load preferences to override default filter and range
           	  me.selectMenuValue(me.chartTypePicker,"barChart");
           	  this.preferencesService.request(me.getPreference(),
	            {
	               successCallback:
	               {
	                  fn: function(p_oResponse)
	                  {
	                     var queryPreference = Alfresco.util.findValueByDotNotation(p_oResponse.json, me.getPreference(PREF_QUERY), null);
	                     if (queryPreference !== null)
	                     {
	                    	me.selectMenuValue(me.chartPicker,queryPreference);
	                     }
	                     
	                     var chartTypePreference = Alfresco.util.findValueByDotNotation(p_oResponse.json, me.getPreference(PREF_CHART_TYPE), null);
	                     if (chartTypePreference !== null)
	                     {
	                    	me.selectMenuValue(me.chartTypePicker,chartTypePreference);
	                    	
	                     }
	                     me.loadChartData();
	                  },
	                  scope: this
	               },
	               failureCallback:
	               {
	                  fn: function()
	                  {
	                	  me.loadChartData();
	                  },
	                  scope: this
	               }
	            });
	           	 
           	  
        	
        } ,  selectMenuValue : function(picker, value){
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
        
        loadChartData : function(){
        	Alfresco.util.Ajax.request(
                    {
                       url: Alfresco.constants.PROXY_URI + "becpg/olap/chart?olapQueryId="+ this.chartPicker.value ,
                       successCallback:
                       {
                          fn: this.processData,
                          scope: this
                       },
                       failureCallback:
     	               {
     	                  fn: function()
     	                  {
     	                	//DO nothing
     	                  },
     	                  scope: this
     	               }
             });
        } ,processData: function(response) {

            this.data = response.json;
          
            var myFieldDefs = [];
            this.columnDefs = [];
            this.seriesDef = [];
            this.barChartSeriesDef = [];
        	
        	
        	 for(i in this.data.metadatas){
        		 myFieldDefs.push( "col"+i);
        		 this.columnDefs.push({ key:  "col"+i, label: this.data.metadatas[i].colName });
			    	if(i>0){
			    		 this.seriesDef.push({displayName:this.data.metadatas[i].colName, yField:  "col"+i});
			    		 this.barChartSeriesDef.push({displayName:this.data.metadatas[i].colName, xField: "col"+i});
			    	}
	 		    }
        	 
        	
        	
        	this.dataSource = new YAHOO.util.DataSource(this.data.resultsets);
        	this.dataSource.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;
        	this.dataSource.responseSchema = {
           	      fields : myFieldDefs
        	};
        	 
            
            
            this.render();
          
        },
        render : function(){
 	
	 		
	 		//Create Line Chart
		        if(this.dataSource!=null){
			 		  
			 		if(this.chartTypePicker.value=="lineChart"){
				 		var lineChart = new YAHOO.widget.LineChart( this.id+"-chart", this.dataSource,
				 		{
				 			series: this.seriesDef,
				 			xField: "col0",
				 			wmode: "opaque" ,
				 			style:
					 		{
					 				legend:
					 				{
					 					display: "bottom"
					 				}
					 		}
				 		});
				 		
			 		} else if(this.chartTypePicker.value=="barChart"){
		
				 		//Create Bar Chart
				 		var barChart = new YAHOO.widget.BarChart( this.id+"-chart", this.dataSource,
				 		{
				 			series:this.barChartSeriesDef,
				 			yField: "col0",
				 			wmode: "opaque" ,
			 				style:
				 			{
				 				legend:
				 				{
				 					display: "bottom"
				 				}
				 			}
				 		});
		
			 		} else if(this.chartTypePicker.value=="columnChart"){
				 		//Create Column Chart
				 		var columnChart = new YAHOO.widget.ColumnChart( this.id+"-chart", this.dataSource,
				 		{
				 			series: this.seriesDef,
				 			xField: "col0",
				 			wmode: "opaque" ,
				 			style:
					 		{
					 				legend:
					 				{
					 					display: "bottom"
					 				}
					 		}
				 		});
				 		
			 		}else if(this.chartTypePicker.value=="pieChart"){
			 		
				 		//Create Column Chart
				 		var pieChart = new YAHOO.widget.PieChart( this.id+"-chart", this.dataSource,
				 		{
				 			dataField: "col1",
				 			categoryField: "col0",
				 			wmode: "opaque",
				 			style:
				 			{
				 				legend:
				 				{
				 					display: "right"
				 				}
				 			}
				 		});
			 		}else if(this.chartTypePicker.value=="chartData"){
			 			var chartData = new YAHOO.widget.DataTable(this.id+"-chart", this.columnDefs, this.dataSource);
			 		}
		        }

        }
        
    });


   
   
})();
