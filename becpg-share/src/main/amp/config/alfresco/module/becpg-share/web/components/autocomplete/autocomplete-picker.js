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
                            readOnly : false,
                            multipleSelectMode : true,
                            targetLinkTemplate : null,
                            dsStr : null,
                            parentFieldHtmlId : null,
                            isMandatory : false,
                            isLocalProxy : false,
                            showToolTip : false,
                            showPage : true,
                            saveTitle : true,
                            urlParamsToPass : null
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
                            if (me.options.multipleSelectMode || me.isAssoc)
                            {
                                me.loadItems();
                            }

                            if (me.options.mode != "view" && !this.options.readOnly)
                            {

                                var oDS = null, oAC = null, bToggler = null, previewTooltips = [], initialValue = "";

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
                                oAC = new YAHOO.widget.AutoComplete(me.fieldHtmlId, me.fieldHtmlId + "-container", oDS);

                                oAC.queryDelay = .1;
                                oAC.page = 1;
                                oAC.maxResultsDisplayed = 15;
                                oAC.forceSelection = false;

                                if (me.options.multipleSelectMode)
                                {
                                    // attach event to basket
                                    Event.delegate(me.controlId + "-basket", "click", function(e, matchedEl, container)
                                    {
                                        var nodeRef = matchedEl.id.split('ac-close-' + me.fieldHtmlId + '-')[1];
                                        me.removeFromBasket(nodeRef);
                                        YAHOO.Bubbling.fire("mandatoryControlValueUpdated", oAC.getInputEl());
                                        Event.preventDefault(e);
                                        Event.stopPropagation(e);
                                    }, "span.ac-closebutton");

                                    Event.on(Dom.get(me.fieldHtmlId).form, "reset", function(e)
                                    {
                                        oAC._clearSelection();
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
                                        inputRemoved.value = "";
                                    })

                                }

                                // The webservice needs additional parameters
                                oAC.generateRequest = function(sQuery)
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
                                        var parentElem = Dom.get(me.options.parentFieldHtmlId + (me.isAssoc ? "-added"
                                                : ""));
                                        if (parentElem != null)
                                        {
                                            oParentField = parentElem.value;
                                        }
                                    }

                                    if (oParentField != null)
                                    {
                                        q = Lang.substitute("q={query}&parent={parent}&page={page}",
                                        {
                                            query : sQuery,
                                            parent : oParentField,
                                            page : oAC.page
                                        });
                                    }
                                    else
                                    {
                                        q = Lang.substitute("q={query}&page={page}",
                                        {
                                            query : sQuery,
                                            page : oAC.page
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

                                oAC
                                        .setHeader("<div class='ac-header' ><span>" + me.msg("autocomplete.header.msg") + "</span></div>");

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
                                                this.cfg
                                                        .setProperty(
                                                                "text",
                                                                '<img src="' + Alfresco.constants.PROXY_URI_RELATIVE + "api/node/" + nodeRef + "/content/thumbnails/doclib?c=queue&ph=true" + '" />');
                                            });
                                }

                                oAC.formatResult = function(oResultData, sQuery, sResultMatch)
                                {
                                    if (me.options.showToolTip)
                                    {
                                        previewTooltips.push("ac-choice-" + me.fieldHtmlId + "-" + oResultData[0]);
                                    }
                                    return "<span id='ac-choice-" + me.fieldHtmlId + "-" + oResultData[0] + "' class='" + oResultData[2] + "'  >" + oResultData[1] + "</span>";
                                };

                                // Toggle button
                                bToggler = Dom.get(me.fieldHtmlId + "-toggle-autocomplete");

                                // Add focus to selected element
                                Event.on(me.fieldHtmlId + "-autocomplete", "click", function(e)
                                {
                                    if (!oAC.isContainerOpen())
                                    {
                                        oAC.getInputEl().focus();
                                    }

                                });

                                Event.on(bToggler, "click", function(e)
                                {
                                    if (oAC.isContainerOpen())
                                    {
                                        oAC.collapseContainer();
                                    }
                                    else
                                    {
                                        oAC.getInputEl().focus();
                                        initialValue = oAC.getInputEl().value;
                                        setTimeout(function()
                                        { // For IE
                                            oAC.sendQuery("*");
                                        }, 0);
                                    }
                                    oAC.page = 1;
                                    previewTooltips = [];
                                    Event.preventDefault(e);
                                    Event.stopPropagation(e);
                                });

                                oAC.containerExpandEvent.subscribe(function()
                                {
                                    Dom.addClass(bToggler, "openToggle");
                                });
                                oAC.containerCollapseEvent.subscribe(function()
                                {
                                    Dom.removeClass(bToggler, "openToggle");
                                    oAC.page = 1;
                                    previewTooltips = [];
                                });

                                oAC.doBeforeLoadData = function(sQuery, oResponse, oPayload)
                                {
                                    if (me.options.showPage)
                                    {
                                        oAC.fullListSize = oResponse.meta.fullListSize;
                                        oAC.pageSize = oResponse.meta.pageSize;
                                        oAC.page = oResponse.meta.page;
                                    }
                                    return true;
                                };

                                oAC.doBeforeExpandContainer = function(elTextbox, elContainer, sQuery, aResults)
                                {
                                    if (!me.options.showPage || parseInt(oAC.fullListSize) < parseInt(oAC.pageSize) + 1)
                                    {
                                        oAC.setFooter("");
                                    }
                                    else
                                    {
                                        oAC
                                                .setFooter("<div class='ac-footer'><div id='" + me.fieldHtmlId + "-container-paging'></div></div>");
                                        var oACPagination = new YAHOO.widget.Paginator(
                                                {
                                                    rowsPerPage : oAC.pageSize,
                                                    totalRecords : oAC.fullListSize,
                                                    containers : me.fieldHtmlId + '-container-paging',
                                                    initialPage : parseInt(oAC.page),
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
                                            oAC.page = state.page;
                                            previewTooltips = [];
                                            setTimeout(function()
                                            { // For IE
                                                var input = oAC.getInputEl().value;
                                                if (input == initialValue)
                                                {
                                                    oAC.sendQuery("*");
                                                }
                                                else
                                                {
                                                    oAC.sendQuery(input);
                                                }
                                            }, 0);
                                        });
                                        oACPagination.render();

                                    }
                                    if (me.options.showToolTip && previewTooltip != null)
                                    {
                                        previewTooltip.cfg.setProperty("context", previewTooltips);
                                    }
                                    return true;
                                };

                                oAC.textboxKeyEvent.subscribe(function()
                                {
                                    oAC.page = 1;
                                    previewTooltips = [];
                                });

                                oAC.textboxBlurEvent
                                        .subscribe(function()
                                        {

                                            if (me.openOnce && (!oAC._bOverContainer || (oAC._nKeyCode == 9)))
                                            {
                                                // Current query needs to be
                                                // validated as a selection
                                                if (!oAC._bItemSelected)
                                                {
                                                    var elMatchListItem = oAC._textMatchesOption();
                                                    // Container is closed or
                                                    // current query doesn't
                                                    // match any result
                                                    if (!oAC._bContainerOpen || (oAC._bContainerOpen && (elMatchListItem === null)))
                                                    {
                                                        // Force selection is
                                                        // enabled so clear the
                                                        // current query
                                                        oAC._clearSelection();
                                                    }
                                                }
                                            }

                                            if (!me.options.multipleSelectMode && me.isAssoc)
                                            {
                                                if (oAC.getInputEl().value == null || oAC.getInputEl().value.length < 1)
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

                                oAC.itemSelectEvent.subscribe(function(type, args)
                                {
                                    var selectedObj = args[2], itemValue = selectedObj[0], itemTitle = selectedObj[1];

                                    if (me.isAssoc || me.options.multipleSelectMode)
                                    {

                                        var inputAdded = Dom.get(me.controlId + "-added");

                                        if (!me.options.multipleSelectMode)
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
                                        oAC.getInputEl().value = "";
                                    }
                                    else
                                    {
                                        if (me.options.saveTitle)
                                        {
                                            oAC.getInputEl().value = itemTitle;
                                        }
                                        else
                                        {
                                            oAC.getInputEl().value = itemValue;
                                        }
                                    }

                                    YAHOO.Bubbling.fire("mandatoryControlValueUpdated", oAC.getInputEl());

                                    return true;

                                });

                                Dom.removeClass(oAC.getInputEl(), "hidden");

                            }
                            else if (!me.options.multipleSelectMode && !me.isAssoc)
                            {
                                Dom.removeClass(me.fieldHtmlId + "-values", "hidden");
                            }

                        },

                        /**
                         * @param basket
                         * @param itemTitle
                         * @param itemValue
                         */
                        addToBasket : function AutoCompletePicker_addToBasket(basket, itemTitle, itemValue)
                        {

                            var displayVal = "<span id='ac-m-selected-" + this.fieldHtmlId + "-" + itemValue + "' class='ac-m-selected'><span class='ac-m-selected-body'>";
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
                                else
                                {
                                    var items = me.options.currentValue.split(",");
                                    for (var i = 0, il = items.length; i < il; i++)
                                    {
                                        var basket = Dom.get(me.controlId + "-basket");
                                        me.addToBasket(basket, items[i], items[i]);
                                    }

                                }
                            }
                        },

                        /**
                         * @param items
                         */
                        renderItems : function AutoCompletePicker_renderItems(items)
                        {
                            var displayValue = "", link;

                            if (items === null)
                            {
                                displayValue = "<span class=\"error\">" + this
                                        .msg("form.control.object-picker.current.failure") + "</span>";
                            }
                            else
                            {
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
                                        for ( var key in items)
                                        {
                                            var item = items[key];
                                            this.addToBasket(basket, item.name, item.nodeRef);
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
                            var me = this, inputAdded = Dom.get(me.controlId + "-added");
                            inputOrig = Dom.get(me.controlId + "-orig"), inputRemoved = Dom
                                    .get(me.controlId + "-removed"), orig = [], removed = [], ret = [];

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

                        },

                    });
})();
