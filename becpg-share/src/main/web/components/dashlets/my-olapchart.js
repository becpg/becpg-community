
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
   
 

   beCPG.component.OlapChart = function(fieldHtmlId) {
      this.id = fieldHtmlId;
      
      Alfresco.util.YUILoaderHelper.require(["button", "container", "datasource"], this.onReady, this);
      
      // Initialise prototype properties
      this.preferencesService = new Alfresco.service.Preferences();
      
      this.curGraph="";
      
   } ;
   
    YAHOO.extend(beCPG.component.OlapChart, Alfresco.component.Base,{
   
      options: {      
         dataSource :  "/saiku/rest/saiku/admin"         
    	}, 
    	onChartSelected : function OlapChart_onChartSelected(menuItem){
    		
    		 this.curGraph = encodeURIComponent(menuItem.cfg.getProperty("text"));
    		 this.onChartClicked(this);
    		
    	},
    	onChartClicked : function (ev){
    		
         try {
	 		   var jsonData = new YAHOO.util.DataSource(this.options.dataSource+"/query/"+ this.curGraph+"/result/cheat" );
	 		   jsonData.responseType = YAHOO.util.DataSource.TYPE_JSON;
	 		   jsonData.responseSchema =
	 		   {
	 		   		fields: ["value","type","properties"]
	 		   };
	 		   
	 		   
	 		  Alfresco.util.Ajax.request(
	                    {
	                       url: "http://admin:becpg@localhost:8080/saiku/rest/saiku/admin/query/F6F63789-7B82-0B5C-ADE8-3D5EC944B6DE/result/cheat",
	                       successCallback:
	                       {
	                          fn: this.processData,
	                          scope: this
	                       }
	             });
	 		   
	 		   
 		   
    		} catch(e){
    			alert(e);	
    		}
    		
    	},
        onReady: function OlapChart_onReady()
        {
            var me = this;
            
            var chartPicker = new YAHOO.widget.Button(me.id + "-charPicker-button",
                    {
                       type: "split",
                       menu: me.id + "-charPicker-select"
                    });

            chartPicker.on("click", me.onChartClicked, this, true);
            
            var menu =   chartPicker.getMenu();
            
            menu.subscribe("click", function (p_sType, p_aArgs)
            {
                 var menuItem = p_aArgs[1];
                 if (menuItem)
                 {
                     chartPicker.set("label", menuItem.cfg.getProperty("text"));
                     me.onChartSelected.call(me, menuItem);
                 }
             });
        
            Alfresco.util.Ajax.request(
                    {
                       url: me.options.dataSource+"/repository",
                       successCallback:
                       {
                          fn: function(response){
                        	  var json = response.json;
                        	  if(json!=null){
                        		  var items = []; 
                        		  for(i in json){
                        			  items.push({text:json[i].name,value:json[i].name})
                        		  }
                        		  menu.addItems(items);
                        		  menu.render(document.body);
                        	  }
                        	  
                          },
                          scope: this
                       }
             });
            
        },
        processData: function(response) {
        	

        	  // [[{"value":"null","properties":{},"type":"COLUMN_HEADER"},{"value":"Oct","properties":{},"type":"COLUMN_HEADER"}],[{"value":"Type","properties":{"levelindex":"0"},"type":"ROW_HEADER_HEADER"},{"value":"Week41/2011","properties":{"levelindex":"1","dimension":"Date de modification"},"type":"COLUMN_HEADER"}],[{"value":"finishedProduct","properties":{"levelindex":"0","dimension":"Type de produit"},"type":"ROW_HEADER"},{"value":"10","properties":null,"type":"DATA_CELL"}],[{"value":"rawMaterial","properties":{"levelindex":"0","dimension":"Type de produit"},"type":"ROW_HEADER"},{"value":"14","properties":null,"type":"DATA_CELL"}]]
        		   
        	
            this.data = {};
            this.data.resultset = [];
            this.data.metadata = [];
            
            if (response.json && response.json.length > 0) {
                
                var lowest_level = 0;
            
                for (var row = 0; row < response.json.length; row++) {
                    if (response.json[row][0].type == "ROW_HEADER_HEADER") {
                        this.data.metadata = [];
                        for (var field = 0; field < response.json[row].length; field++) {
                            if (response.json[row][field].type == "ROW_HEADER_HEADER") {
                                this.data.metadata.shift();
                                lowest_level = field;
                            }
                            
                            this.data.metadata.push({
                                colIndex: field,
                                colType: typeof(response.json[row + 1][field].value) !== "number" &&
                                    isNaN(response.json[row + 1][field].value
                                    .replace(/[^a-zA-Z 0-9.]+/g,'')) ? "String" : "Numeric",
                                colName: response.json[row][field].value
                            });
                        }
                    } else if (response.json[row][0].value !== "null") {
                        var record = [];
                        for (var col = lowest_level; col < response.json[row].length; col++) {
                            var value = 
                                typeof(response.json[row][col].value) !== "number" &&
                                parseFloat(response.json[row][col].value
                                    .replace(/[^a-zA-Z 0-9.]+/g,'')) ?
                                parseFloat(response.json[row][col].value
                                    .replace(/[^a-zA-Z 0-9.]+/g,'')) :
                                response.json[row][col].value;
                            if (col == lowest_level) {
                                value += " [" + row + "]";
                            }
                            record.push(value);
                        }
                        this.data.resultset.push(record);
                    }
                }
                
                this.render();
            } else {
                //No results
            }
        },
        render : function(){

        	
        	var myFieldDefs = [];
        	
        	 for(i in this.data.metadata){
        		 myFieldDefs.push( this.data.metadata[i].colType);
	 		    }
        	
        	
        	var myDataSource = new YAHOO.util.DataSource(this.data.resultset);
        	myDataSource.responseType = YAHOO.util.DataSource.TYPE_JSARRAY;
        	myDataSource.responseSchema = {
           	      fields : myFieldDefs
        	};
        	 
	 		   
	 		  YAHOO.widget.Chart.SWFURL = "/share/res/yui/charts/assets/charts.swf";
	 		   
	 		     var seriesDef = [
	 		                 {
	 		                     displayName: "Rent",
	 		                     yField: "rent",
	 		                     style: {
	 		                         color: 0xff0000,
	 		                         size: 20
	 		                     }
	 		                 },
	 		                 {
	 		                     displayName: "Utilities",
	 		                     yField: "utilities",
	 		                     style: {
	 		                         color: 0x0000ff,
	 		                         size: 30
	 		                     }
	 		                 }
	 		      ];
	 		     
	 		    var myColumnDefs = [];
	 		    
	 		    for(i in this.data.metadata){
	 		    	myColumnDefs.push({ key:  this.data.metadata[i].colType, label: this.data.metadata[i].colName });
	 		    	
	 		    }
	 		    
	 		     
	 		   var myDataTable = new YAHOO.widget.DataTable(this.id+"-chart", myColumnDefs, myDataSource);
	 		     
//	 		  
//	 		   var mychart = new YAHOO.widget.ColumnChart(this.id+"-chart", myDataSource,
//	 				   {
//	 				   	xField: "x",
//	 				   	yField: "y",
//	 				     wmode: "opaque",
//	 				   	style: {
//	 				        padding: 20,
//	 				        animationEnabled: true,
//	 				    }
//	 				   });
//	 		   
	 		  

        	
        	
        }
        
    });


   
   
})();