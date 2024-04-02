/*******************************************************************************
 *  Copyright (C) 2010-2021 beCPG. 
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
 * AutoCompletePicker component.
 * 
 * @author "Matthieu Laborie <matthieu.laborie@becpg.fr>"
 * @namespace beCPG
 * @class beCPG.component.AutoCompletePicker
 */
(function()
{
    /**
     * YUI Library aliases
     */
    var Dom = YAHOO.util.Dom, Event = YAHOO.util.Event, Lang = YAHOO.util.Lang, Element = YAHOO.util.Element;

    beCPG.component.AutoCompletePicker = function AutoCompletePicker_constructor(controlId, fieldHtmlId, isAssoc)
    {
        beCPG.component.AutoCompletePicker.superclass.constructor.call(this, "beCPG.component.AutoCompletePicker",
                controlId, [ "button", "menu", "container" ]);

        YAHOO.Bubbling.on("beforeFormRuntimeInit", this.beforeFormRuntimeInit, this);

        this.controlId = controlId;
        this.fieldHtmlId = fieldHtmlId;
        this.isAssoc = isAssoc;
        
        YAHOO.Bubbling.on(this.fieldHtmlId + "refreshContent", this.refreshContent, this);
        YAHOO.Bubbling.on("formContainerDestroyed", this.onFormContainerDestroyed, this);
  		YAHOO.Bubbling.on("mandatoryControlValueUpdated", this.checkParentField, this);
        

        return this;
    };

    /**
     * Extend from Alfresco.component.Base
     */
    YAHOO.extend(beCPG.component.AutoCompletePicker, Alfresco.component.Base);

    /**
     * Augment prototype with main class implementation, ensuring overwrite is
     * enabled
     */
    YAHOO
            .extend(
                    beCPG.component.AutoCompletePicker,
                    Alfresco.component.Base,
                    {

                        objectRenderer : new Alfresco.ObjectRenderer(this),

                        openOnce : false,

                        /**
                         * Object container for initialization options
                         * 
                         * @property options
                         * @type object
                         */
                        options :
                        {
                            currentValue : "",
                            mode : "view",
                            formId : null,
                            readOnly : false,
                            multipleSelectMode : true,
                            targetLinkTemplate : null,
                            dsStr : null,
                            parentFieldHtmlId : null,
                            isMandatory : false,
                            isLocalProxy : false,
                            showColor: false,
                            showToolTip : false,
                            showPage : true,
                            saveTitle : true,
                            urlParamsToPass : null,
                            isParentMode : false
                        },
                        
                        /**
                         * Notification that form is being destroyed.
                         * 
                         * @method onFormContainerDestroyed
                         * @param layer
                         *            {object} Event fired (unused)
                         * @param args
                         *            {array} Event parameters
                         */
                        onFormContainerDestroyed : function AutoCompletePicker_onFormContainerDestroyed(layer, args) {
							var form = args[1];
							
                        	if((form==null || (form!=null && form.id == this.options.formId)) &&  this.widgets.oAC){
                        		this.widgets.oAC.destroy();
                        		delete this.widgets.oAC
                        	}
                        },
                        
                        /**
                         * Destroy method - deregister Bubbling event handlers
                         * 
                         * @method destroy
                         */
                        destroy : function AutoCompletePicker_destroy() {
                           try {
                              YAHOO.Bubbling.unsubscribe("formContainerDestroyed", this.onFormContainerDestroyed, this);
                              YAHOO.Bubbling.unsubscribe(this.fieldHtmlId + "refreshContent", this.refreshContent, this);
                              YAHOO.Bubbling.unsubscribe("beforeFormRuntimeInit", this.beforeFormRuntimeInit, this);
							  YAHOO.Bubbling.unsubscribe("mandatoryControlValueUpdated", this.checkParentField, this);
                           } catch (e) {
                              // Ignore
                           }
                           beCPG.component.AutoCompletePicker.superclass.destroy.call(this);
                        },
                        
                        /**
                         * Fired by YUI when parent element is available for
                         * scripting. Component initialisation, including
                         * instantiation of YUI widgets and event listener
                         * binding.
                         * 
                         * @method onReady
                         */
                        onReady : function AutoCompletePicker_onReady()
                        {

                            var me = this, previewTooltip = null, q = "";

                            // Start by loading new Item
           					me.loadItems();
                            
                            if (me.options.mode != "view" && !this.options.readOnly)
                            {
                            	
                                var oDS = null, bToggler = null, previewTooltips = [], previewTooltipsData = {}, initialValue = "";

                                // Use an XHRDataSource
                                if (this.options.isLocalProxy)
                                {
                                    oDS = new YAHOO.util.XHRDataSource(
                                            Alfresco.constants.URL_SERVICECONTEXT + me.options.dsStr);
                                }
                                else
                                {
                                    oDS = new YAHOO.util.XHRDataSource(Alfresco.constants.PROXY_URI + me.options.dsStr);
                                }

                                // Set the responseType
                                oDS.responseType = YAHOO.util.XHRDataSource.TYPE_JSON;

                                // Define the schema of the JSON results
                                oDS.responseSchema =
                                {
                                    resultsList : "result",
                                    fields : [ "value", "name", "cssClass", "metadatas" ],
                                    metaFields :
                                    {
                                        page : "page",
                                        pageSize : "pageSize",
                                        fullListSize : "fullListSize"
                                    }
                                };
                                // Instantiate the AutoComplete
                                me.widgets.oAC = new YAHOO.widget.AutoComplete(me.fieldHtmlId, me.fieldHtmlId + "-container", oDS);
                                me.widgets.oAC.queryDelay = .5;
                                me.widgets.oAC.page = 1;
                                me.widgets.oAC.maxResultsDisplayed = 15;
                                me.widgets.oAC.forceSelection = false;

                                
                                if(Dom.get(me.fieldHtmlId).hasFocus){
                                	me.widgets.oAC._onTextboxFocus(this,me.widgets.oAC);
                                }
                                

                                if (me.options.multipleSelectMode)
                                {
                                    // attach event to basket
                                    Event.delegate(me.controlId + "-basket", "click", function(e, matchedEl, container)
                                    {
                                        var nodeRef = matchedEl.id.split('ac-close-' + me.fieldHtmlId + '-')[1];
                                        me.removeFromBasket(nodeRef);
                                        YAHOO.Bubbling.fire("mandatoryControlValueUpdated", me.widgets.oAC.getInputEl());
                                        Event.preventDefault(e);
                                        Event.stopPropagation(e);
                                    }, "span.ac-closebutton");

                                    Event.on(Dom.get(me.fieldHtmlId).form, "reset", function(e)
                                    {
                                        me.widgets.oAC._clearSelection();
                                        Dom.get(me.controlId + "-basket").innerHTML = "";
                                        var inputOrig = Dom.get(me.controlId + "-orig"), inputAdded = Dom
                                                .get(me.controlId + "-added"), inputRemoved = Dom
                                                .get(me.controlId + "-removed");
                                        if (inputOrig != null)
                                        {
                                            inputAdded.value = inputOrig.value;
                                        }
                                        else
                                        {
                                            inputAdded.value = "";
                                        }
                                        if(inputRemoved!=null){
                                        	inputRemoved.value = "";
                                        }
                                    });

                                }

							


                                // The webservice needs additional parameters
                                me.widgets.oAC.generateRequest = function(sQuery)
                                {

                                    me.openOnce = true;

                                    if (me.options.multipleSelectMode && sQuery.indexOf("%2C%20") > 0)
                                    {
                                        var arrQuery = sQuery.split("%2C%20");
                                        sQuery = arrQuery[arrQuery.length - 1];
                                    }

                                    var oParentField = null;

                                    if (me.options.parentFieldHtmlId != null)
                                    {
                                        var parentElem = null;
                                        if(me.isAssoc){
                                        	oParentField = me.getValuesFromId(me.options.parentFieldHtmlId);
                                        } else {
                                        	parentElem  =  Dom.get(me.options.parentFieldHtmlId);
                                        	 if (parentElem != null)
                                             {
                                                 oParentField = parentElem.value;
                                             } 
                                        }
                                       
                                    }

                                    if (oParentField != null)
                                    {
                                        q = Lang.substitute("q={query}&parent={parent}&page={page}",
                                        {
                                            query : sQuery,
                                            parent : oParentField,
                                            page : me.widgets.oAC.page
                                        });
                                    }
                                    else
                                    {
                                        q = Lang.substitute("q={query}&page={page}",
                                        {
                                            query : sQuery,
                                            page : me.widgets.oAC.page
                                        });
                                    }

                                    if (me.options.urlParamsToPass != null)
                                    {
                                        q = q + "&" + me.options.urlParamsToPass;
                                    }

                                    if (me.options.dsStr.indexOf("?") > 0)
                                    {
                                        return "&" + q;
                                    }
                                    return "?" + q;

                                };

                                me.widgets.oAC.setHeader("<div class='ac-header' ><span>" + me.msg("autocomplete.header.msg") + "</span></div>");

                                if (me.options.showToolTip)
                                {
                                    previewTooltip = new YAHOO.widget.Tooltip("previewTooltip",
                                    {
                                        container : me.fieldHtmlId + "-container",
                                        width : "108px",
                                        showDelay : 500,
                                        zIndex : 9999
                                    });

                                    previewTooltip.contextTriggerEvent
                                            .subscribe(function(type, args)
                                            {
                                            	  var context = args[0];
                                                  var nodeRef = context.id.split('ac-choice-' + me.fieldHtmlId + '-')[1]
                                                          .replace(":/", "");
                                                  
                                                  var metadatas = previewTooltipsData[context.id], title = "", description = "", width=108;
                                                  if(metadatas!=null){
                                                	  for(var i =0; i< metadatas.length; i++){
                                                		  if(metadatas[i].key == "title"){
                                                			  title = metadatas[i].value;
                                                		  }
                                                		  if(metadatas[i].key == "description"){
                                                			  description = metadatas[i].value;
                                                		  }
                                                	  }
                                                	  
                                                  }
                                                  var html = "<table><tr><td>"
                                                    	+'<img src="' + Alfresco.constants.PROXY_URI_RELATIVE + "api/node/"
                                                    	+ nodeRef + "/content/thumbnails/doclib?c=queue&ph=true" + '" />'
                                                    	+"</td><td>";
                                                  if(title!=null && title.length>0){
                                                	  width = 350;
                                                	  html +="<h3>"+title+"</h3>";
                                                  }
                                                  if(description!=null && description.length>0){
                                                	  width = 350;
                                                	  html +="<p>"+description+"</p>";
                                                  }
                                                  
                                                   html += "</td></table>";
                                                    
                                                  this.cfg.setProperty("text",html);
                                                  this.cfg.setProperty("width",width+"px");
                                              
                                            });
                                }

                                me.widgets.oAC.formatResult = function(oResultData, sQuery, sResultMatch)
                                {
                                    if (me.options.showToolTip)
                                    {
                                        previewTooltips.push("ac-choice-" + me.fieldHtmlId + "-" + oResultData[0]);
                                        previewTooltipsData["ac-choice-" + me.fieldHtmlId + "-" + oResultData[0]] = oResultData[3];
                                    }
                                    
                                    
                                    getColor = function (metadatas){
                                    	var color = null;
                                    	for(key in metadatas){
                                    		obj = metadatas[key];
                                    		if (obj["key"] == "color"){
                                    			color = obj["value"];
                                    		}
                                    	} 
                                    	return color;
                                    }
                        
                                    var showColorSpan = ""; 
                                    if(me.options.showColor){
                                    	showColorSpan = "<span class='show-color' style='background:" + getColor(oResultData[3]) + "'>&nbsp;</span>";
                                    }
                                    
                                    return  showColorSpan + "<span id='ac-choice-" + me.fieldHtmlId + "-" + oResultData[0] + "' class='" + oResultData[2] + "'>" +
                                    		"<span class='ctn-fav'></span> " + oResultData[1] + "</span>";
                                };

                                // Toggle button
                                bToggler = Dom.get(me.fieldHtmlId + "-toggle-autocomplete");

                                // Add focus to selected element
                                Event.on(me.fieldHtmlId + "-autocomplete", "click", function(e)
                                {
                                    if (!me.widgets.oAC.isContainerOpen())
                                    {
                                        me.widgets.oAC.getInputEl().focus();
                                    }

                                });

                                Event.on(bToggler, "click", function(e)
                                {
                                    if (me.widgets.oAC.isContainerOpen())
                                    {
                                        me.widgets.oAC.collapseContainer();
                                    }
                                    else
                                    {
                                        me.widgets.oAC.getInputEl().focus();
                                        initialValue = me.widgets.oAC.getInputEl().value;
                                        setTimeout(function()
                                        { // For IE
                                            me.widgets.oAC.sendQuery("*");
                                        }, 0);
                                    }
                                    me.widgets.oAC.page = 1;
                                    previewTooltips = [];
                                    previewTooltipsData = {};
                                    Event.preventDefault(e);
                                    Event.stopPropagation(e);
                                });

                                me.widgets.oAC.containerExpandEvent.subscribe(function()
                                {
                                    Dom.addClass(bToggler, "openToggle");
                                });
                                me.widgets.oAC.containerCollapseEvent.subscribe(function()
                                {
                                    Dom.removeClass(bToggler, "openToggle");
                                    me.widgets.oAC.page = 1;
                                    previewTooltips = [];
                                    previewTooltipsData = {};
                                });

                                me.widgets.oAC.doBeforeLoadData = function(sQuery, oResponse, oPayload)
                                {
                                    if (me.options.showPage)
                                    {
                                        me.widgets.oAC.fullListSize = oResponse.meta.fullListSize;
                                        me.widgets.oAC.pageSize = oResponse.meta.pageSize;
                                        me.widgets.oAC.page = oResponse.meta.page;
                                    }
                                    return true;
                                };
                                

                                me.widgets.oAC.doBeforeExpandContainer = function(elTextbox, elContainer, sQuery, aResults)
                                {
                                    if (!me.options.showPage || parseInt(me.widgets.oAC.fullListSize) < parseInt(me.widgets.oAC.pageSize) + 1)
                                    {
                                        me.widgets.oAC.setFooter("");
                                    }
                                    else
                                    {
                                    	me.widgets.oAC.setFooter("<div class='ac-footer'><div id='" + me.fieldHtmlId + "-container-paging'></div></div>");
                                        var oACPagination = new YAHOO.widget.Paginator(
                                                {
                                                    rowsPerPage : me.widgets.oAC.pageSize,
                                                    totalRecords : me.widgets.oAC.fullListSize,
                                                    containers : me.fieldHtmlId + '-container-paging',
                                                    initialPage : parseInt(me.widgets.oAC.page),
                                                    template : "<div>{CurrentPageReport}</div> {PreviousPageLink} {PageLinks} {NextPageLink}",
                                                    pageReportTemplate : me
                                                            .msg("autocomplete.pagination.template.page-report"),
                                                    previousPageLinkLabel : me
                                                            .msg("autocomplete.pagination.previousPageLinkLabel"),
                                                    nextPageLinkLabel : me
                                                            .msg("autocomplete.pagination.nextPageLinkLabel")
                                                });
                                        oACPagination.subscribe('changeRequest', function(state)
                                        {
                                            me.widgets.oAC.page = state.page;
                                            previewTooltips = [];
                                            previewTooltipsData = {};
                                            setTimeout(function()
                                            { // For IE
                                                var input = me.widgets.oAC.getInputEl().value;
                                                if (input == initialValue)
                                                {
                                                    me.widgets.oAC.sendQuery("*");
                                                }
                                                else
                                                {
                                                    me.widgets.oAC.sendQuery(input);
                                                }
                                            }, 0);
                                        });
                                        oACPagination.render();

                                    }
                                    if (me.options.showToolTip && previewTooltip != null)
                                    {
                                        previewTooltip.cfg.setProperty("context", previewTooltips);
                                    }
                                    
                                    //Force the autocomplete content width to be equals or grater than it's footer width.
                                    me.observer = new MutationObserver(function(mutations) {
                                    	if(me.widgets.oAC){
                                    		mutations.forEach(function(mutationRecord) {
                                    			var footerWidth = me.widgets.oAC._elFooter.scrollWidth,
                                    			contentWidth = me.widgets.oAC._elContent.style.width.replace('px', '');
                                    			if (footerWidth > contentWidth){
                                    				me.widgets.oAC._elContent.style.width =  footerWidth + "px";
                                    			}
                                    		});    
                                    	}
                                    });
                                    me.observer.observe( me.widgets.oAC._elContent, { attributes : true, attributeFilter : ['style'] });
                                    
                                    return true;
                                };

                                me.widgets.oAC.textboxKeyEvent.subscribe(function()
                                {
                                    me.widgets.oAC.page = 1;
                                    previewTooltips = [];
                                });

                                me.widgets.oAC.textboxBlurEvent
                                        .subscribe(function()
                                        {

                                            if (me.openOnce && (!me.widgets.oAC._bOverContainer || (me.widgets.oAC._nKeyCode == 9)))
                                            {
                                                // Current query needs to be
                                                // validated as a selection
                                                if (!me.widgets.oAC._bItemSelected)
                                                {
                                                    var elMatchListItem = me.widgets.oAC._textMatchesOption();
                                                    // Container is closed or
                                                    // current query doesn't
                                                    // match any result
                                                    if (!me.widgets.oAC._bContainerOpen || (me.widgets.oAC._bContainerOpen && (elMatchListItem === null)))
                                                    {
                                                        // Force selection is
                                                        // enabled so clear the
                                                        // current query
                                                        me.widgets.oAC._clearSelection();
                                                    }
                                                }
                                            }

                                            if (!me.options.multipleSelectMode && me.isAssoc)
                                            {
                                                if (me.widgets.oAC.getInputEl().value == null || me.widgets.oAC.getInputEl().value.length < 1)
                                                {
                                                    var inputOrig = Dom.get(me.controlId + "-orig"), inputAdded = Dom
                                                            .get(me.controlId + "-added"), inputRemoved = Dom
                                                            .get(me.controlId + "-removed");
                                                    if (inputOrig != null && inputOrig.value != "")
                                                    {
                                                        inputRemoved.value = inputOrig.value;
                                                    }
                                                    inputAdded.value = "";
                                                }
                                            }

                                        });

                                me.widgets.oAC.itemSelectEvent.subscribe(function(type, args)
                                {
                                    var selectedObj = args[2], itemValue = selectedObj[0], itemTitle = selectedObj[1];

                                    if (me.isAssoc || me.options.multipleSelectMode)
                                    {

                                        var inputAdded = Dom.get(me.controlId + "-added");

                                        if (!me.options.multipleSelectMode || me.options.isParentMode)
                                        {
                                            var inputOrig = Dom.get(me.controlId + "-orig"), inputRemoved = Dom
                                                    .get(me.controlId + "-removed");
                                            if (inputOrig != null)
                                            {
                                                if (inputOrig.value != itemValue)
                                                {
                                                    if (inputOrig.value != "")
                                                    {
                                                        inputRemoved.value = inputOrig.value;
                                                    }
                                                    inputAdded.value = itemValue;
                                                }
                                                else
                                                {
                                                    inputRemoved.value = "";
                                                    inputAdded.value = "";
                                                }
                                            }
                                            else
                                            {
                                                inputAdded.value = itemValue;
                                            }
                                        }
                                        else
                                        {
                                            if (inputAdded.value != "")
                                            {
                                                inputAdded.value += ",";
                                            }

                                            inputAdded.value += itemValue;

                                        }
                                    }

                                    if (me.options.multipleSelectMode)
                                    {
                                        me.addToBasket(Dom.get(me.controlId + "-basket"), itemTitle, itemValue);
                                        me.widgets.oAC.getInputEl().value = "";
                                    }
                                    else
                                    {
                                        if (me.options.saveTitle)
                                        {
                                            me.widgets.oAC.getInputEl().value = itemTitle;
                                        }
                                        else
                                        {
                                            me.widgets.oAC.getInputEl().value = itemValue;
                                        }
                                    }

                                    YAHOO.Bubbling.fire("mandatoryControlValueUpdated", me.widgets.oAC.getInputEl());

                                    return true;

                                });

                                Dom.removeClass(me.widgets.oAC.getInputEl(), "hidden");

                            }
                            else if ((me.options.mode == "view" || me.options.readOnly) || !me.options.multipleSelectMode && !me.isAssoc)
                            {
                                Dom.removeClass(me.fieldHtmlId + "-values", "hidden");
                            }

                        },

						checkParentField : function(layer, args){
						    if (this.options.parentFieldHtmlId != null && args!=null && args[1].id == this.options.parentFieldHtmlId){
								 this.widgets.oAC._clearSelection();
                               }
						},

                        /**
                         * @param basket
                         * @param itemTitle
                         * @param itemValue
                         */
                        addToBasket : function AutoCompletePicker_addToBasket(basket, itemTitle, itemValue)
                        {

                            var displayVal = "<span id='ac-m-selected-" + this.fieldHtmlId + "-" + itemValue + "' class='ac-m-selected'>";
                            if(this.options.isParentMode && basket!=null && basket.innerHTML != ''){
                                displayVal += "<span  class='ac-parent-sep' >&nbsp;</span>";
                            }
                            displayVal += "<span class='ac-m-selected-body'>";
                            displayVal += itemTitle;
                            displayVal += "</span>";
                            displayVal += "<span id='ac-close-" + this.fieldHtmlId + "-" + itemValue + "' class='ac-closebutton' ></span>";
                           
                            displayVal += "</span>";
 
                            basket.innerHTML += displayVal;

                        },
                        /**
                         * @param nodeRef
                         */
                        removeFromBasket : function AutoCompletePicker_removeFromBasket(nodeRef)
                        {

                            var basket = new Element(this.controlId + "-basket"), inputAdded = Dom
                                    .get(this.controlId + "-added");

                            basket.removeChild(Dom.get("ac-m-selected-" + this.fieldHtmlId + "-" + nodeRef));

                            if (this.isAssoc && Dom.get(this.controlId + "-removed"))
                            {
                                var inputRemoved = Dom.get(this.controlId + "-removed");
                                if (inputRemoved.value != "")
                                {
                                    inputRemoved.value += ",";
                                }
                                inputRemoved.value += nodeRef;
                            }

                            if (inputAdded)
                            {
                                inputAdded.value = inputAdded.value.replace("," + nodeRef, "").replace(nodeRef + ",",
                                        "").replace(nodeRef, "");
                            }
                        },

                        /**
                         * 
                         */
                        loadItems : function AutoCompletePicker_loadItems()
                        {

                            var me = this;

                            if (me.options.currentValue != "")
                            {

                                if (me.isAssoc)
                                {

                                    Alfresco.util.Ajax.jsonRequest(
                                    {
                                        url : Alfresco.constants.PROXY_URI + "api/forms/picker/items",
                                        method : "POST",
                                        dataObj :
                                        {
                                            items : me.options.currentValue.split(",")
                                        },
                                        successCallback :
                                        {
                                            fn : function(response)
                                            {
                                                var items = response.json.data.items, selectedItems = {};
                                                for (var i = 0, il = items.length; i < il; i++)
                                                {
                                                    selectedItems[items[i].nodeRef] = items[i];
                                                }
                                                me.renderItems(selectedItems);
                                            },
                                            scope : this
                                        }
                                    });
                                }
                                else if(me.options.multipleSelectMode)
                                {
									var basket = Dom.get(me.controlId + "-basket");
									if (basket) {
										basket.innerHTML = "";
	                                    var items = me.options.currentValue.split(",");
	                                    for (var i = 0, il = items.length; i < il; i++)
	                                    {
	                                      
	                                        me.addToBasket(basket, items[i], items[i]);
	                                    }
									}
                                }
                            }
                        },

                        /**
                         * @param items
                         */
                        renderItems : function AutoCompletePicker_renderItems(items)
                        {
                          

                            if (items === null)
                            {
                                Dom.get(this.fieldHtmlId + "-values").innerHTML = "<span class=\"error\">" + this
                                        .msg("form.control.object-picker.current.failure") + "</span>";
                                Dom.removeClass(this.fieldHtmlId + "-values", "hidden");        
                            }
                            else
                            {
							    var displayValue = "", link;
                                // multiple selection
                                if (this.options.mode == "view" || this.options.readOnly)
                                {
                                    for ( var key in items)
                                    {
                                        var item = items[key];
                                        if (displayValue != "")
                                        {
                                            displayValue += ", ";
                                        }
                                        if (this.options.targetLinkTemplate != null)
                                        {

                                            link = Alfresco.util.siteURL(Lang.substitute(
                                                    this.options.targetLinkTemplate, item),
                                            {
                                                site : item.site
                                            });

                                            displayValue += this.objectRenderer.renderItem(item, 16,
                                                    "<div>{icon} <a href='" + link + "'>{name}</a></div>");
                                        }
                                        else
                                        {
                                            displayValue += item.name;
                                        }
                                    }
                                    Dom.get(this.fieldHtmlId + "-values").innerHTML = displayValue;
                                    Dom.removeClass(this.fieldHtmlId + "-values", "hidden");
                                }
                                else
                                {
                                    if (this.options.multipleSelectMode)
                                    {
                                        var basket = Dom.get(this.controlId + "-basket");
										if (basket) {
	                                        basket.innerHTML = "";
	                                        for ( var key in items)
	                                        {
	                                            var item = items[key];
	                                            this.addToBasket(basket, item.name, item.nodeRef);
	                                        }
										}
                                    }
                                    else
                                    {
                                        var htmlInput = Dom.get(this.fieldHtmlId);
                                        for ( var key in items)
                                        {
                                            var item = items[key];
                                            displayValue += item.name;
                                        }
                                        htmlInput.value = displayValue;
                                        YAHOO.Bubbling.fire("mandatoryControlValueUpdated", htmlInput);
                                        Dom.removeClass(this.fieldHtmlId, "hidden");
                                    }
                                }
                            }

                        },

                        getValues : function AutoCompletePicker_getValues()
                        {
                            return this.getValuesFromId(this.controlId);
                        },
                        
                        getInnerText : function AutoCompletePicker_getInnerText()
                        {
                            var basket = Dom.get(this.controlId + "-basket");
                            return basket.innerText;
                        },
                        
                        getValuesFromId : function AutoCompletePicker_getValues(controlId)
                        {
                            var inputAdded = Dom.get(controlId + "-added");
                            inputOrig = Dom.get(controlId + "-orig"), inputRemoved = Dom
                                    .get(controlId + "-removed"), orig = [], removed = [], ret = [];

                            if (inputAdded != null && inputAdded.value.length > 0)
                            {
                                ret = inputAdded.value.split(",");
                            }

                            if (inputOrig != null && inputOrig.value.length > 0)
                            {
                                orig = inputOrig.value.split(",");
                            }

                            if (inputRemoved != null && inputRemoved.value.length > 0)
                            {
                                removed = inputRemoved.value.split(",");
                            }

                            for ( var i in orig)
                            {
                                var add = true;
                                for ( var j in removed)
                                {
                                    if (orig[i] == removed[j])
                                    {
                                        add = false;
                                        break;
                                    }
                                }
                                if (add)
                                {
                                    ret.push(orig[i]);
                                }
                            }

                            return ret.join();
                        },
                        
                        refreshContent : function(layer, args)
                        {
                        	this.options.currentValue = args[1];
 							this.loadItems();
                        },

                        beforeFormRuntimeInit : function(layer, args)
                        {
                            var me = this, formRuntime = args[1].runtime;
                            if (this.options.multipleSelectMode && this.fieldHtmlId.indexOf(formRuntime.formId.replace("-form",""))>-1)
                            {

                                for (var j = 0; j < args[1].runtime.validations.length; j++)
                                {

                                    if (args[1].runtime.validations[j].fieldId == this.fieldHtmlId)
                                    {
                                        args[1].runtime.validations[j].handler = function mandatory(field, args, event,
                                                form)
                                        {
                                            var values = me.getValues();
                                           
                                            return YAHOO.lang.trim(values).length !== 0;
                                        };
                                        
                                    }
                                }

                            }

                        }

                    });
})();
