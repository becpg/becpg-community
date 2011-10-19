
/**
 * AutoCompletePicker component.
 * 
 * @namespace beCPG
 * @class beCPG.component.AutoCompletePicker
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

   beCPG.component.AutoCompletePicker = function AutoCompletePicker_constructor(controlId, fieldHtmlId, isAssoc)
   {
	  this.name = " beCPG.component.AutoCompletePicker";
      this.controlId = controlId;
      this.fieldHtmlId = fieldHtmlId;
      this.isAssoc = isAssoc;
  
      /* Register this component */
      Alfresco.util.ComponentManager.register(this);
      
      /* Load YUI Components */
      Alfresco.util.YUILoaderHelper.require(["container"], this.onComponentsLoaded, this);
      
      
      return this;
   } ;
   
    YAHOO.extend(beCPG.component.AutoCompletePicker, Alfresco.component.Base,{
   
      options: {   
    	 objectRenderer: new Alfresco.ObjectRenderer(this),
         currentValue: "",
         mode: "view",
         multipleSelectMode: true,
         targetLinkTemplate: null,      
         dsStr : null,
         parentFieldHtmlId : null,
         isMandatory : false
    	},     
      onComponentsLoaded: function AutoCompletePicker_onComponentsLoaded ()
      {
    	  if(this.options.mode!="view"){
            Event.onAvailable(this.fieldHtmlId+"-container", this.render, this, true);
    	  } else {
    		Event.onAvailable(this.fieldHtmlId+"-values", this.render, this, true);
    	  }
      },
      setOptions: function AutoCompletePicker_setOptions (obj){
			this.options = YAHOO.lang.merge(this.options, obj);

         return this;
      },

      
      render :  function AutoCompletePicker_render() {
    	 
    	  var instance = this;
    	  
    	  
    	  if(instance.isAssoc){
	    	  // Start by loading new Item
	    	  instance.loadItems();
	    	  
	    	  // attach event to basket
	
		  		if(instance.options.multipleSelectMode){
		  			Event.delegate(instance.controlId+"-basket","click", function(e, matchedEl, container) {
		  		 
		  		 		var nodeRef = matchedEl.id.split('ac-close-')[1];
		  		 		instance.removeFromBasket(nodeRef);
		    			YAHOO.Bubbling.fire("mandatoryControlValueUpdated", oAC.getInputEl());
		  		 		e.preventDefault();
		  		 	    e.stopPropagation();
		  			}, "span.ac-closebutton");
		  		}
    	  }
    	  
    	 
    	  
    	  
    	  // Load autocomplete
    	  if(instance.options.mode!="view"){
    	  // Use an XHRDataSource
    	   var oDS = new YAHOO.util.XHRDataSource(Alfresco.constants.PROXY_URI + instance.options.dsStr);  
    	   
    	   // Set the responseType
    	   oDS.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;
    	 
    	   // Define the schema of the JSON results
    	   oDS.responseSchema = 
    	   {
    	      resultsList : "result",
    	      fields : ["value", "name","type"],
    	      metaFields : {
    	      	page : "page",
    	      	pageSize : "pageSize",
    	      	fullListSize : "fullListSize"
    	      }
    	   };
    	   
    	   

    	   // Instantiate the AutoComplete
    	   var oAC = new YAHOO.widget.AutoComplete(instance.fieldHtmlId, instance.fieldHtmlId+"-container", oDS);

    	  
    	   
    	   oAC.queryDelay = .5;
    	   oAC.page = 1;

    	   // The webservice needs additional parameters
    	   oAC.generateRequest = function(sQuery) 
    	   {
    	 
    			if(instance.options.multipleSelectMode && sQuery.indexOf("%2C%20") > 0)
    			{
    				var arrQuery = sQuery.split("%2C%20");
    				sQuery = arrQuery[arrQuery.length - 1];
    			}
  
	    	   var oParentField = '';	
	    	   
	    	   if(instance.options.parentFieldHtmlId!=null){
	    		   var parentElem = Dom.get(instance.options.parentFieldHtmlId);	
		   			if(parentElem != null){
		   					oParentField = parentElem.value;
		   			}	
	    	   }
	    	  
	    	    var q = Lang.substitute("q={query}&parent={parent}&page={page}",{query:sQuery,parent:oParentField,page: oAC.page});
	    	     if(instance.options.dsStr.indexOf("?")>0){
	    	    	 return "&" + q;
	    	     } else {
	    	    	 return "?" + q;
	    	     }
    	   
    	   };

    	   oAC.setHeader("<div class='ac-header' ><span>"+instance.msg("autocomplete.header.msg")+"</span></div>");
    	   
    	   var previewTooltips=[];
    	   var previewTooltip = new YAHOO.widget.Tooltip("previewTooltip" ,
        	         {
    		   			container: instance.fieldHtmlId+"-container",
        	            width: "108px",
        	            showDelay: 500,
        	            zIndex:9999
        	         });
    	   
    	   previewTooltip.contextTriggerEvent.subscribe(function(type, args)
        	         {
        	            var context = args[0];
        	            var nodeRef = context.id.split('ac-choice-')[1].replace(":/","");
        	            this.cfg.setProperty("text", '<img src="' + Alfresco.constants.PROXY_URI + "api/node/" + nodeRef + "/content/thumbnails/doclib?c=queue&ph=true" + '" />');
        	         });
             
        
    	   oAC.formatResult = function(oResultData, sQuery, sResultMatch) {
    		  
    		    previewTooltips.push("ac-choice-"+oResultData[0]);
    	   		return "<span id='ac-choice-"+oResultData[0]+"' class='"+oResultData[2]+"' style='padding-left: 20px;' >"+oResultData[1]+"</span>";
    	   	}

           var initialValue = "";
    	   
    	    // Toggle button
    		var bToggler = Dom.get(instance.fieldHtmlId+"-toggle-autocomplete"); 
    		
    		
    		 //Add focus to selected element
     	   Event.on(instance.fieldHtmlId+"-autocomplete","click", function(e) {
     		   if(!oAC.isContainerOpen()) { 
     			   oAC.getInputEl().focus(); // Needed to keep widget active
     		   }
      		 
     	   	});
     	   
    
    		  Event.on(bToggler,"click", function(e) { 	  
    	    		 if(oAC.isContainerOpen()) { 
        		         oAC.collapseContainer(); 
        		  } else { 
        		     oAC.getInputEl().focus(); // Needed to keep widget active
        		     initialValue = oAC.getInputEl().value;
        		     setTimeout(function() { // For IE
        		        oAC.sendQuery("*"); 
        		      },0); 
        		    } 
        		 	oAC.page=1;
        		 	previewTooltips=[];
        		 	e.preventDefault();
    		 	    e.stopPropagation();
        		  } ); 
    		 
    		  oAC.containerExpandEvent.subscribe(function(){ 
    		  	Dom.addClass(bToggler, "openToggle");
    		  }); 
    		  oAC.containerCollapseEvent.subscribe(function(){
    		  		Dom.removeClass(bToggler, "openToggle");
    		  		oAC.page=1;
    		  		previewTooltips=[];
    		  	}); 
    		 
    	      
    		   oAC.doBeforeLoadData = function ( sQuery , oResponse , oPayload ) {
    			    	oAC.fullListSize = oResponse.meta.fullListSize;
    			    	oAC.pageSize = oResponse.meta.pageSize;   
    		 			oAC.page = oResponse.meta.page;   
    	 			return true;
    		   }
    		 
    		 
    		 oAC.doBeforeExpandContainer = function(elTextbox , elContainer , sQuery , aResults) {
    			try{
	    			 if(parseInt(oAC.fullListSize)< parseInt(oAC.pageSize)+1 ) {
	    			      oAC.setFooter("");
	    			   } else {
	    			       oAC.setFooter("<div class='ac-footer'><div id='"+instance.fieldHtmlId+"-container-paging'></div></div>");
	    			   		var oACPagination = new YAHOO.widget.Paginator({
	    			   					rowsPerPage : oAC.pageSize,
	    							    totalRecords : oAC.fullListSize,
	    								containers : instance.fieldHtmlId+'-container-paging' ,
	    								initialPage: parseInt(oAC.page),
	    						        template: "<div>{CurrentPageReport}</div> {PreviousPageLink} {PageLinks} {NextPageLink}",
	    						        pageReportTemplate: instance.msg("autocomplete.pagination.template.page-report"),
	    						        previousPageLinkLabel: instance.msg("autocomplete.pagination.previousPageLinkLabel"),
	    						        nextPageLinkLabel: instance.msg("autocomplete.pagination.nextPageLinkLabel")
	    					});
	    			  		oACPagination.subscribe('changeRequest', function(state){
	    			  					oAC.page=state.page;
	    			  					previewTooltips=[];
	    			  					setTimeout(function() { // For IE
	    			  						var input = oAC.getInputEl().value;
	    			  						if(input==initialValue){
	    			  							oAC.sendQuery("*");
	    			  						} else {
	    			  							oAC.sendQuery(input);
	    			  						}
	    			  	    		     },0); 
	    						}); 
	    					oACPagination.render();
	    			
	    			   }
	    			 
	    			 previewTooltip.cfg.setProperty("context", previewTooltips);
    			} catch (e) {
  					alert(e);
				}
    			 
    		   return true;
    		}
    		
    		oAC.textboxChangeEvent.subscribe(function(){
    			oAC.page=1;
    			previewTooltips=[];
    		});
    					
    		
    		oAC.itemSelectEvent.subscribe(function(type , args){
    			try{
	    			var selectedObj = args[2];
	
	    	 		var itemValue = selectedObj[0];
	    	 		var itemTitle = selectedObj[1];
	
	    	 		 if(instance.isAssoc){
	    	 			 
	    	 		     var inputOrig = Dom.get(instance.controlId+"-orig");
	    	 		     var inputAdded = Dom.get(instance.controlId+"-added");
	    	 		     var inputRemoved = Dom.get(instance.controlId+"-removed");
	    	 			 
		    		    if(inputOrig != null && inputAdded != null && inputRemoved != null) {
		    		
		    				if(!instance.options.multipleSelectMode){
		    					if(inputOrig.value != itemValue) {
		    						if(inputOrig.value != ""){
		    							inputRemoved.value = inputOrig.value;
		    						}
		    						inputAdded.value = itemValue;
		    					}
		    				}
		    				else{			
		    					if(inputAdded.value != ""){
		    						inputAdded.value += ",";
		    					}
		    					inputAdded.value += itemValue;
		    				}			
		    			}	
	    	 		 }
	    	 		 
	    			if(instance.isAssoc && instance.options.multipleSelectMode)
	    			{
	    				var  basket =  Dom.get(instance.controlId+"-basket");
	    				instance.addToBasket(basket,itemTitle,itemValue);
	    				oAC.getInputEl().value = "";
	    			} else {
	    				oAC.getInputEl().value = itemTitle;
	    			}
	    	 		
	    			YAHOO.Bubbling.fire("mandatoryControlValueUpdated", oAC.getInputEl());
	 
    			} catch (e) {
  					alert(e);
				}
    			return true;
    		
    		});
    		
    	  }
    	  
    	  
      },
    		
      addToBasket :  function AutoCompletePicker_addToBasket(basket, itemTitle,itemValue){
    		
    				
    			var displayVal =  "<span id='ac-m-selected-"+itemValue+"' class='ac-m-selected'><span class='ac-m-selected-body'>";
    				displayVal += itemTitle;				
    				displayVal += "</span>";
    				displayVal +="<span id='ac-close-"+itemValue+"' class='ac-closebutton' ></span>";
    				displayVal += "</span>";
    				
    				basket.innerHTML+=displayVal;
    				
    		},
    		
      removeFromBasket:	function AutoCompletePicker_removeFromBasket(nodeRef){
    	  	var  basket =  new Element(this.controlId+"-basket");
    	  	var inputRemoved = Dom.get(this.controlId+"-removed");
    	  	basket.removeChild( Dom.get("ac-m-selected-"+nodeRef));
    		 		
    				if(inputRemoved.value != ""){
    					inputRemoved.value += ",";
    				}
    				inputRemoved.value += nodeRef;
    		},
    		
    		
    	loadItems :	function AutoCompletePicker_loadItems(){
    			if (this.options.currentValue != "") {
    			 Alfresco.util.Ajax.jsonRequest(
    	            {
    	               url: Alfresco.constants.PROXY_URI + "api/forms/picker/items",
    	               method: "POST",
    	               dataObj:
    	               {
    	                  items: this.options.currentValue.split(",")
    	               },
    	               successCallback:
    	               {
    	                  fn: function(response){
    	                  	 var items = response.json.data.items,
    			               item,selectedItems = {};
    			            for (var i = 0, il = items.length; i < il; i++){
    			               item = items[i];
    			               selectedItems[item.nodeRef] = item;
    			            }
    			            this.renderItems(selectedItems);
    			                           
    	                  },
    	                  scope: this
    	               },
    	               failureCallback:
    	               {
    	                  fn: function(response){
    	                  
    	                  },
    	                  scope: this
    	               }
    	            });
    	         }
    		
    		},
    		
  
      
      
      renderItems :  function AutoCompletePicker_renderItems(items){
		  	var displayValue = "", link;

  		  if (items === null){
  			  displayValue = "<span class=\"error\">" + this.msg("form.control.object-picker.current.failure") + "</span>";            
  		  }	  else  {                   			  
					// multiple selection
						if(this.options.mode == "view"){
							for (var key in items)
				 			{
								item = items[key];
								if(displayValue != ""){
									displayValue += ", ";
								}
								link=Lang.substitute(this.options.targetLinkTemplate,item);
								displayValue += this.options.objectRenderer.renderItem(item, 16,
		                                 "<div>{icon} <a href='" + link + "'>{name}</a></div>");
				 			}
						   Dom.get(this.fieldHtmlId+"-values").innerHTML = displayValue;
						} else	{
							if(this.options.multipleSelectMode)	{	
								var  basket =  Dom.get(this.controlId+"-basket");
								for (var key in items) {
									item = items[key];
									this.addToBasket(basket,item.name,item.nodeRef);
					 			}
							}	else {
								var htmlInput = Dom.get(this.fieldHtmlId);
								for (var key in items) {
									item = items[key];
									displayValue += item.name;					
					 			}
								htmlInput.value = displayValue;
							}
					   }						
					}		
	
     }
            
      
   });
})();
