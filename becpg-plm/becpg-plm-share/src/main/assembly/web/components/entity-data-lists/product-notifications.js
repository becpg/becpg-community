/*******************************************************************************
 * Copyright (C) 2010-2026 beCPG.
 * 
 * This file is part of beCPG
 * 
 * beCPG is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * beCPG is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
(function() {
    /**
     * YUI Library aliases
     */
    var Dom = YAHOO.util.Dom;
    /**
     * Alfresco Slingshot aliases
     */
    var $html = Alfresco.util.encodeHTML;

    var REQFILTER_EVENTCLASS = Alfresco.util.generateDomId(null, "notificationsReqType");
    var CHARACTDETAILS_EVENTCLASS = Alfresco.util.generateDomId(null, "charactDetails");

    /**
     * ProductNotifications constructor.
     * 
     * @param htmlId
     *            {String} The HTML id of the parent element
     * @return {beCPG.component.ProductNotifications} The new
     *         ProductNotifications instance
     * @constructor
     */
    beCPG.component.ProductNotifications = function(htmlId) {

        beCPG.component.ProductNotifications.superclass.constructor.call(this, "beCPG.component.ProductNotifications", htmlId, [ "button",
                "container" ]);

        // message
        this.name = "beCPG.component.EntityDataListToolbar";

        return this;
    };

    /**
     * Extend from Alfresco.component.Base
     */
    YAHOO.extend(beCPG.component.ProductNotifications, Alfresco.component.Base);

    /**
     * Augment prototype with main class implementation, ensuring overwrite is
     * enabled
     */
    YAHOO.lang
            .augmentObject(beCPG.component.ProductNotifications.prototype,
                    {
                        /**
                         * Object container for initialization options
                         * 
                         * @property options
                         * @type object
                         */
                        options : {

                            entityNodeRef : "",

                            reqCtrlListNodeRef : "",

                            containerDiv : null,

                            list : null,
                            
                            maxResults: 50,

                            scores: null,

                            /**
                             * Requirements carried by the scores instead of the product reqCtrlList,
                             * used by a change unit whose simulated product is not persisted.
                             */
                            localRequirements: false

                        },

                        filterId : "all",

                        filterData : "",

                        filterParams : null,

                        currentPage : 1,

                        /**
                         * Fired by YUI when parent element is available for scripting.
                         * 
                         * @method onReady
                         */
                        onReady : function ProductNotifications_onReady() {

                            var instance = this;

                            // Inject the template from the XHR request into a
                            // new DIV
                            // element
                            this.widgets.panelDiv = document.createElement("div");

                            this.widgets.panelDiv.style.zIndex = "3";

                            var html = "<div class=\"notifications-panel\"><div id=\"" + instance.id + "-scoresDiv\" class=\"ctrlSumPreview  \" >";

                            html += "</div><div id=\"" + instance.id + "-notificationTable\"></div></div>";
                            this.widgets.panelDiv.innerHTML = html;


                            this.widgets.showNotificationsButton = instance.createShowNotificationButton(this, "show-notifications",
                                    this.widgets.panelDiv, function() {
                                        instance.reloadDataTable();
                                    });

                            this.loadPanelData();

                            if (!this.options.scores) {
                                YAHOO.Bubbling.on("refreshDataGrids", this.loadPanelData, this);
                            }

                            YAHOO.Bubbling.addDefaultAction(instance.id +REQFILTER_EVENTCLASS,function(layers, args) {
                                    var owner = YAHOO.Bubbling.getOwnerByTagName(args[1].anchor, "span");

                                    
                                    var selectedItems = document.getElementsByClassName("rclFilterSelected");

                                    // sets clicked item to selected
                                    for (var i = 0; i < selectedItems.length; i++) {
                                        selectedItems[i].classList.remove("rclFilterSelected");
                                    }
                                    var chgClass = owner;
                                    if (owner.parentNode.nodeName == "LI") {
                                        chgClass = owner.parentNode;
                                    }
                                    Dom.addClass(chgClass, "rclFilterSelected");

                                    // refreshes view by calling filter
                                    var splits = owner.className.split(" ")[0].split("-");
                                    var type = (splits.length > 2 ? splits[2] : undefined);
                                    var dataType = splits[1].charAt(0).toUpperCase() + splits[1].slice(1);
                                    instance.filterId = (type === "all" && dataType === "All" ? "all" : "filterform");
                                    instance.localFilter = {
                                        reqType : (type === "all" || dataType == "Regulatorycodes" ? null : type),
                                        reqDataType : (dataType === "All" || dataType == "Regulatorycodes" ? null : dataType)
                                    };
                                    
                                    if(dataType == "Regulatorycodes" && type!=null) {
                                        instance.filterData =  "{\"prop_bcpg_regulatoryCode\":\"=" + type.replace(/@/gi," ").replace(/\$/gi,"-") +"\"}";
                                    } else {
                                        instance.filterData = (type === "all" && dataType === "All" ? undefined : "{"
                                                + (type !== undefined ? ("\"prop_bcpg_rclReqType\":\"=" + type+"\"") : "")
                                                + (dataType != null ? (type !== undefined ? "," : "") + ("\"prop_bcpg_rclDataType\":\"=" + dataType+"\"") : "")
                                                + "}");
                                    }
                                    
                                    instance.reloadDataTable();

                            }, true );
                            
                            YAHOO.Bubbling.addDefaultAction(instance.id +CHARACTDETAILS_EVENTCLASS, function(layer, args) {
                                    var owner = YAHOO.Bubbling.getOwnerByTagName(args[1].anchor, "span");
                                    if (owner !== null) {
                                        var splitted = owner.className.split("#");
                                     
                                           var dt = Alfresco.util.ComponentManager.find({
                                            name: "beCPG.module.EntityDataGrid"
                                        })[0];

                                        var url = Alfresco.constants.URL_SERVICECONTEXT + "modules/entity-charact-details/entity-charact-details" + "?entityNodeRef="
                                            + dt.options.entityNodeRef + "&itemType="
                                            + encodeURIComponent(splitted[0].replace("_",":")) + "&dataListName="
                                            + encodeURIComponent(dt.datalistMeta.name) + "&dataListItems=workspace://SpacesStore/" +splitted[1];

                                        dt._showPanel(url, dt.id, null, "60em");

                                    }
                                    return true;
                                });
                        },
                        

                        createShowNotificationButton : function(instance, actionName, containerDiv, fn) {

                            var template = Dom.get("custom-toolBar-template-button"), buttonWidget = null;

                            var spanEl = Dom.getFirstChild(template).cloneNode(true);

                            Dom.addClass(spanEl, actionName);
                            Dom.addClass(spanEl, "loading");

                            Dom.setAttribute(spanEl, "id", instance.id + "-" + actionName + "Button");

                            this.options.containerDiv.appendChild(spanEl);

                            buttonWidget = Alfresco.util.createYUIButton(instance, actionName + "Button", null, {
                                type : "menu",
                                menu : containerDiv,
                                lazyloadmenu : false
                            });

                            buttonWidget.getMenu().subscribe("show", fn, buttonWidget, this);

                            return buttonWidget;

                        },

                        createDataTable : function() {

                            var instance = this;

                            instance.widgets.dataSource = new YAHOO.util.DataSource(this.getWebscriptUrl(), {
                                connMethodPost : true,
                                responseType : YAHOO.util.DataSource.TYPE_JSON,
                                responseSchema : {
                                    resultsList : "items",
                                    metaFields : {
                                        startIndex : "startIndex",
                                        totalRecords : "totalRecords"
                                    }
                                }
                            });
                            var columDefs = [ {
                                key : "detail",
                                sortable : false,
                                formatter : this.bind(this.renderCellDetail)
                            } ];

                            instance.widgets.notificationsDataTable = new YAHOO.widget.DataTable(instance.id + "-notificationTable", columDefs,
                                    instance.widgets.dataSource, {
                                        initialLoad : false,
                                        dynamicData : false,
                                        "MSG_EMPTY" : '<span class="wait">' + $html(this.msg("message.loading")) + '</span>',
                                        "MSG_ERROR" : this.msg("message.error"),
                                        className : "alfresco-datatable simple-doclist body notifications-list",
                                        renderLoopSize : 4,
                                        paginator : null
                                    });

                            instance.widgets.notificationsDataTable.getDataTable = function() {
                                return this;
                            };

                            instance.widgets.notificationsDataTable.getData = function(recordId) {
                                return this.getRecord(recordId).getData();
                            };

                            instance.widgets.notificationsDataTable.loadDataTable = function DataTable_loadDataTable(parameters) {

                                instance.widgets.dataSource.connMgr.setDefaultPostHeader(Alfresco.util.Ajax.JSON);

                                if (Alfresco.util.CSRFPolicy.isFilterEnabled()) {
                                    instance.widgets.dataSource.connMgr.initHeader(Alfresco.util.CSRFPolicy.getHeader(), Alfresco.util.CSRFPolicy
                                            .getToken(), false);
                                }

                                instance.widgets.dataSource.sendRequest(YAHOO.lang.JSON.stringify(parameters), {
                                    success : function DataTable_loadDataTable_success(oRequest, oResponse, oPayload) {
                                        instance.widgets.notificationsDataTable.onDataReturnReplaceRows(oRequest, oResponse, oPayload);

                                    /* if (instance.widgets.paginator) {
                                            instance.widgets.paginator.set('totalRecords', oResponse.meta.totalRecords);
                                            instance.widgets.paginator.setPage(oResponse.meta.startIndex, true);
                                        } */

                                    },
                                    failure : instance.widgets.notificationsDataTable.onDataReturnReplaceRows,
                                    scope : instance.widgets.notificationsDataTable,
                                    argument : {}
                                });
                            };
                            // Override DataTable
                            // function to set
                            // custom empty
                            // message
                            var original_doBeforeLoadData = instance.widgets.notificationsDataTable.doBeforeLoadData;

                            instance.widgets.notificationsDataTable.doBeforeLoadData = function SimpleDocList_doBeforeLoadData(sRequest, oResponse,
                                    oPayload) {
                                if (oResponse.results && oResponse.results.length === 0) {
                                    oResponse.results.unshift({
                                        isInfo : true,
                                        title : instance.msg("empty.notifications.title"),
                                        description : instance.msg("empty.notifications.description")
                                    });
                                } 

                                return original_doBeforeLoadData.apply(this, arguments);
                            };
                        },

                        loadPanelData : function() {

                            var instance = this;

                            // Modification: si scores fournis dans options, les utiliser directement
                            if (this.options.scores) {
                                this.renderScoresData(this.options.scores);
                                return;
                            }

                            // Code original pour appel AJAX
                            Alfresco.util.Ajax.request({

                                url : Alfresco.constants.PROXY_URI + "becpg/product/reqctrllist/node/"
                                        + instance.options.entityNodeRef.replace(":/", ""),
                                method : Alfresco.util.Ajax.GET,
                                responseContentType : Alfresco.util.Ajax.JSON,
                                successCallback : {
                                    fn : function(response) {

                                        if (response.json && response.json.scores) {
                                            instance.renderScoresData(response.json.scores);
                                        }

                                    },
                                    scope : instance
                                },
                                failureMessage : "Could not load html template for version graph",
                                execScripts : true
                            });

                        },

                        // Nouveau: méthode pour rendre les données de scores
                        renderScoresData : function(scores) {
                            var instance = this;
                            var html = "";
                            var isGridContext = this.options.containerDiv && Dom.hasClass(this.options.containerDiv, "product-notifications-container");
                            
                            if (scores !== undefined && scores.details !==undefined) {

                                var intScore = parseInt(scores.global);
                                var spriteIndex = (intScore / 5 >> 0);
                                var progressPercent = Math.max(0, Math.min(100, Math.floor(scores.global)));
                                var progressState = "high";
                                if (progressPercent < 50) {
                                    progressState = "low";
                                } else if (progressPercent < 80) {
                                    progressState = "medium";
                                }
                                var scoreTitle = instance.msg("tooltip.components.validation") + ": "
                                        + Math.floor(scores.details.componentsValidation) + "%\n"
                                        + instance.msg("tooltip.mandatory.completion") + ": "
                                        + Math.floor(scores.details.mandatoryFields) + "%\n"
                                        + instance.msg("tooltip.specification.respect") + ": "
                                        + Math.floor(scores.details.specifications) + "%";

                                html += "<ul><li class=\"title\">" + instance.msg("label.product.scores") + "</li>"
                                        + "<li class=\"score score-" + spriteIndex + "\" " + "title=\"" + scoreTitle + "\">";

                                html += "<span>" + Math.floor(scores.global) + "%</span>";
                                html += "</li></ul>";

                                var buttonClass = "score-" + spriteIndex;

                                var isChangeUnit = this.options.localRequirements,
                                    totalForbidden = isChangeUnit ? scores.newForbiddenCount : scores.totalForbidden,
                                    ctrlCount = isChangeUnit ? scores.newCtrlCount : scores.ctrlCount;
                                instance.widgets.showNotificationsButton.removeClass("loading");

                                for (var idx = 1; idx <= 20; idx++) {
                                    instance.widgets.showNotificationsButton.removeClass("score-" + idx);
                                }

                                if (isGridContext) {
                                    instance.widgets.showNotificationsButton.removeClass("score");
                                    instance.widgets.showNotificationsButton.removeClass("progress-button");
                                    instance.widgets.showNotificationsButton.addClass("progress-button");
                                    var progressHtml = '<span class="progress-bar-container progress-state-' + progressState
                                            + '"><span class="progress-bar-fill" style="width:' + progressPercent
                                            + '%;"></span><span class="progress-bar-text">' + progressPercent + '%</span></span>';
                                    instance.widgets.showNotificationsButton.set("label", progressHtml);
                                } else {
                                    if (!Dom.hasClass(instance.widgets.showNotificationsButton, "score")) {
                                        instance.widgets.showNotificationsButton.addClass("score");
                                    }
                                    instance.widgets.showNotificationsButton.removeClass("progress-button");
                                    instance.widgets.showNotificationsButton.addClass(buttonClass);
                                    instance.widgets.showNotificationsButton.set("label", progressPercent + "%");
                                }

                                if (totalForbidden !== undefined && totalForbidden !== null && totalForbidden > 0) {
                                    instance.widgets.showNotificationsButton.set("title", instance.msg(isChangeUnit
                                            ? "tooltip.change-unit.new-forbidden" : "tooltip.notifications-button", totalForbidden));

                                    var errorSpan = Dom.getFirstChildBy(instance.options.containerDiv, function(el) {
                                        return el.className.indexOf("warning") > -1;
                                    });

                                    if (errorSpan === null) {
                                        errorSpan = document.createElement("span");
                                        errorSpan.className = "warning";
                                        instance.options.containerDiv.appendChild(errorSpan);
                                    }

                                    errorSpan.innerHTML = (totalForbidden != undefined && totalForbidden != null
                                            && totalForbidden > 0 ? totalForbidden : "");
                                }

                                // if we have some constraints in res
                                if (ctrlCount !== undefined && ctrlCount != null
                                        && ctrlCount.length > 0) {
                                    // Parses each array mapped to dataType
                                    html += "<div class=\"dataTypeList\"><div class=\"title\">"
                                            + instance.msg(isChangeUnit ? "label.change-unit.new-violations" : "label.constraints.violations")
                                            + "<span class=\"req-all-all rclFilterSelected\"><a class=\"req-filter "
                                            + instance.id +REQFILTER_EVENTCLASS + "\" href=\"#\">" + instance.msg("label.constraints.view-all")
                                            + "</a></span></div>";

                                    html += "<div class=\"rclFilterElt\"><div>";

                                    for ( var dataType in ctrlCount) {
                                        var scoreInfo = "";
                                        var dataTypeName = Object.keys(ctrlCount[dataType])[0];
                                        html += "<div class=\"div-" + dataTypeName.toString().toLowerCase()
                                                + "\"><span class=\"span-" + dataTypeName.toString().toLowerCase()
                                                + "\"><a class=\"req-filter " + instance.id + REQFILTER_EVENTCLASS + "\" href=\"#\" >"
                                                + instance.msg("label.constraints." + dataTypeName.toString().toLowerCase())
                                                + scoreInfo + "</a></span><ul>";

                                        var types = ctrlCount[dataType];
                                        
                                    

                                        for ( var type in types[dataTypeName]) {
                                            var value = types[dataTypeName][type];
                                            if(dataTypeName == "RegulatoryCodes"){
                                                var regulatoryLabel = (scores.regulatoryCodeLabels && scores.regulatoryCodeLabels[type]) ? scores.regulatoryCodeLabels[type] : type;
                                                html += '<li><span class="req-' + dataTypeName.toString().toLowerCase() + '-' + type.replace(/ /gi,"@").replace(/-/gi,"$")
                                                + '" ><a class="req-filter tag '
                                                + instance.id +REQFILTER_EVENTCLASS + '" href="#"><span>' + Alfresco.util.encodeHTML(regulatoryLabel) +
                                                ' ('+ value + ')</span></a></li>';
                                            } else {
                                                html += '<li><span class="req-' + dataTypeName.toString().toLowerCase() + '-' + type
                                                        + '" title="' + instance.msg("reqTypes." + type) + '"><a class="req-filter '
                                                        + instance.id +REQFILTER_EVENTCLASS + '" href="#"><span class="reqType' + type + '"></span>'
                                                        + value + '</a></li>';
                                            }

                                        }
                                        html += "</ul></div>";

                                    }
                                    html += "</div></div></div>";
                                }
                            } else {
                                var buttonClass = "score-100" ;

                                instance.widgets.showNotificationsButton.removeClass("loading");

                                for (var defaultIdx = 1; defaultIdx <= 20; defaultIdx++) {
                                    instance.widgets.showNotificationsButton.removeClass("score-" + defaultIdx);
                                }

                                if (isGridContext) {
                                    instance.widgets.showNotificationsButton.removeClass("score");
                                    instance.widgets.showNotificationsButton.removeClass("progress-button");
                                    instance.widgets.showNotificationsButton.addClass("progress-button");
                                    var defaultProgress = '<span class="progress-bar-container progress-state-high"><span class="progress-bar-fill" style="width:100%;"></span><span class="progress-bar-text">100%</span></span>';
                                    instance.widgets.showNotificationsButton.set("label", defaultProgress);
                                } else {
                                    if (!Dom.hasClass(instance.widgets.showNotificationsButton, "score")) {
                                        instance.widgets.showNotificationsButton.addClass("score");
                                    }
                                    instance.widgets.showNotificationsButton.removeClass("progress-button");
                                    instance.widgets.showNotificationsButton.addClass(buttonClass);
                                    instance.widgets.showNotificationsButton.set("label", "100%");
                                }

                            }
                            if (scores && scores.previous) {
                                html = this.renderPreviousScores(scores) + html;
                                if (isGridContext) {
                                    this.renderChangeSummary(scores);
                                }
                            }

                            if(Dom.get(instance.id + "-scoresDiv")!=null){
                                Dom.get(instance.id + "-scoresDiv").innerHTML = html;
                            }

                            if(!instance.widgets.notificationsDataTable && !this.options.localRequirements){
                                instance.createDataTable();
                            }
                        },

                        /**
                         * Summary of the product before the change order, shown on top of the panel.
                         *
                         * @method renderPreviousScores
                         * @param scores {object} the scores of a change unit
                         * @return {string} the HTML of the summary
                         */
                        renderPreviousScores : function ProductNotifications_renderPreviousScores(scores) {
                            var previous = scores.previous;
                            return '<div class="change-unit-previous">'
                                    + $html(this.msg("label.change-unit.before", Math.floor(previous.global || 0), previous.totalForbidden || 0))
                                    + '</div>';
                        },

                        /**
                         * Shows next to the gauge what the change order changes: the alerts it
                         * introduces, the alerts it resolves and the completion it gains or loses.
                         *
                         * @method renderChangeSummary
                         * @param scores {object} the scores of a change unit
                         */
                        renderChangeSummary : function ProductNotifications_renderChangeSummary(scores) {
                            var summarySpan = Dom.getElementsByClassName("change-unit-delta", "span", this.options.containerDiv)[0];

                            if (!summarySpan) {
                                summarySpan = document.createElement("span");
                                summarySpan.className = "change-unit-delta";
                                this.options.containerDiv.appendChild(summarySpan);
                            }
                            summarySpan.title = this.msg("label.change-unit.before", Math.floor(scores.previous.global || 0),
                                    scores.previous.totalForbidden || 0);
                            summarySpan.innerHTML = this.renderChangeSummaryHtml(scores);
                        },

                        /**
                         * @method renderChangeSummaryHtml
                         * @param scores {object} the scores of a change unit
                         * @return {string} the new alerts, the resolved alerts and the completion change
                         */
                        renderChangeSummaryHtml : function ProductNotifications_renderChangeSummaryHtml(scores) {
                            var parts = [],
                                completionDelta = Math.floor(scores.global || 0) - Math.floor(scores.previous.global || 0);

                            if (scores.newCount > 0) {
                                parts.push('<span class="delta-worse">' + $html(this.msg("label.change-unit.summary.new", scores.newCount)) + '</span>');
                            }
                            if (scores.resolvedCount > 0) {
                                parts.push('<span class="delta-better">' + $html(this.msg("label.change-unit.summary.resolved", scores.resolvedCount))
                                        + '</span>');
                            }
                            if (scores.lessSevereCount > 0) {
                                parts.push('<span class="delta-better">' + $html(this.msg("label.change-unit.summary.less-severe", scores.lessSevereCount))
                                        + '</span>');
                            }
                            if (completionDelta !== 0) {
                                parts.push('<span class="' + (completionDelta > 0 ? "delta-better" : "delta-worse") + '">' + (completionDelta > 0 ? "+" : "")
                                        + completionDelta + '%</span>');
                            }
                            if (parts.length === 0) {
                                return '<span class="delta-none">' + $html(this.msg("label.change-unit.summary.none")) + '</span>';
                            }
                            return parts.join(" ");
                        },

                        /**
                         * Renders the requirements carried by the scores: what the change order
                         * introduces, then what it resolves, then, folded, what the product already had.
                         *
                         * @method renderLocalRequirements
                         */
                        renderLocalRequirements : function ProductNotifications_renderLocalRequirements() {
                            var scores = this.options.scores || {},
                                requirements = scores.requirements || [],
                                newRequirements = [],
                                existingRequirements = [],
                                newCount = scores.newCount || 0,
                                resolvedCount = scores.resolvedCount || 0,
                                existingCount = (scores.requirementsCount || 0) - newCount,
                                html = '<div class="notifications-list change-unit-requirements">';

                            for (var i = 0; i < requirements.length; i++) {
                                (requirements[i].isNew ? newRequirements : existingRequirements).push(requirements[i]);
                            }

                            html += '<div class="change-unit-section change-unit-new-title">' + $html(this.msg("label.change-unit.new-alerts", newCount)) + '</div>';
                            html += newCount > 0 ? this.renderLocalRequirementList(newRequirements, newCount)
                                    : '<div class="change-unit-none">' + $html(this.msg("label.change-unit.no-new-alert")) + '</div>';

                            if (resolvedCount > 0) {
                                html += '<div class="change-unit-section change-unit-resolved-title">'
                                        + $html(this.msg("label.change-unit.resolved", resolvedCount)) + '</div>';
                                html += '<div class="change-unit-resolved">' + this.renderLocalRequirementList(scores.resolved, resolvedCount) + '</div>';
                            }

                            if (scores.lessSevereCount > 0) {
                                html += '<div class="change-unit-none">' + $html(this.msg("label.change-unit.less-severe", scores.lessSevereCount)) + '</div>';
                            }

                            if (existingCount > 0) {
                                html += '<details class="change-unit-existing"><summary class="change-unit-section">'
                                        + $html(this.msg("label.change-unit.existing", existingCount)) + '</summary>'
                                        + this.renderLocalRequirementList(existingRequirements, existingCount) + '</details>';
                            }

                            html += '</div>';
                            Dom.get(this.id + "-notificationTable").innerHTML = html;
                        },

                        /**
                         * @method renderLocalRequirementList
                         * @param requirements {array} the requirements to render
                         * @param totalCount {number} the number of requirements before the server capped the list
                         * @return {string} the HTML of the list
                         */
                        renderLocalRequirementList : function ProductNotifications_renderLocalRequirementList(requirements, totalCount) {
                            var html = "", filter = this.localFilter || {};

                            requirements = requirements || [];
                            for (var i = 0; i < requirements.length; i++) {
                                var requirement = requirements[i];
                                if ((!filter.reqType || filter.reqType == requirement.reqType)
                                        && (!filter.reqDataType || filter.reqDataType == requirement.reqDataType)) {
                                    html += this.renderLocalRequirement(requirement);
                                }
                            }

                            if (totalCount && totalCount > requirements.length) {
                                html += '<div class="change-unit-more">' + $html(this.msg("label.change-unit.more", totalCount - requirements.length))
                                        + '</div>';
                            }
                            return html;
                        },

                        /**
                         * @method renderLocalRequirement
                         * @param requirement {object} a requirement of the change unit
                         * @return {string} the HTML of the requirement
                         */
                        renderLocalRequirement : function ProductNotifications_renderLocalRequirement(requirement) {
                            var html = '<div class="rclReq-details">';
                            if (requirement.reqType) {
                                html += '<div class="icon"><span class="reqType' + requirement.reqType + '" title="'
                                        + $html(this.msg("data.reqtype." + requirement.reqType.toLowerCase())) + '">&nbsp;</span></div>';
                            }
                            html += '<div class="rclReq-title">';
                            html += $html(this.getLocalizedMessage(requirement.message)) + '</div>';
                            html += '<div class="clear"></div></div>';
                            return html;
                        },

                        /**
                         * @method getLocalizedMessage
                         * @param message {object} the message of a requirement, by locale
                         * @return {string} the message in the user locale, or the closest one
                         */
                        getLocalizedMessage : function ProductNotifications_getLocalizedMessage(message) {
                            var locale = Alfresco.constants.JS_LOCALE || "";
                            if (!message) {
                                return "";
                            }
                            if (message[locale]) {
                                return message[locale];
                            }
                            if (message[locale.split("_")[0]]) {
                                return message[locale.split("_")[0]];
                            }
                            if (message[""]) {
                                return message[""];
                            }
                            for (var key in message) {
                                if (message.hasOwnProperty(key)) {
                                    return message[key];
                                }
                            }
                            return "";
                        },

                        /**
                         * Generate base webscript url. Can be overridden.
                         * 
                         * @method getWebscriptUrl
                         */
                        getWebscriptUrl : function ProductNotifications_getWebscriptUrl() {
                            return Alfresco.constants.PROXY_URI + "becpg/entity/datalists/data/node"
                                    + "?guessContainer=true&repo=true&itemType=bcpg:reqCtrlList&pageSize="+this.options.maxResults+"&dataListName=reqCtrlList&entityNodeRef="
                                    + this.options.entityNodeRef+"&locale="+Alfresco.constants.JS_LOCALE;

                        },

                        /**
                         * Calculate webscript parameters
                         * 
                         * @method getParameters
                         * @override
                         */
                        getParameters : function ProductNotifications_getParameters() {

                            return {
                                fields : [ "bcpg_rclReqType", "bcpg_rclReqMessage", "bcpg_rclSourcesV2", "bcpg_rclDataType", "bcpg_regulatoryCode", "bcpg_rclErrorLog" ],
                                page : this.currentPage,
                                filter : {
                                    filterId : this.filterId,
                                    filterOwner : null,
                                    filterData : this.filterData,
                                    filterParams : this.filterParams
                                },
                                extraParams : null
                            };

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
                        renderCellDetail : function ProductNotifications_renderCellDetail(elCell, oRecord, oColumn, oData) {
                            var record = oRecord.getData(), desc = "", instance =this;

                            if (record.isInfo) {
                                desc += '<div class="empty"><h3>' + record.title + '</h3>';
                                desc += '<span>' + record.description + '</span></div>';
                            } else {

                                var reqType = oRecord.getData("itemData")["prop_bcpg_rclReqType"].value;
                                var reqDataType = oRecord.getData("itemData")["prop_bcpg_rclDataType"].value;
                                var regulatoryCode = oRecord.getData("itemData")["prop_bcpg_regulatoryCode"].value;
                                var regulatoryLabel = oRecord.getData("displayLabel") ? oRecord.getData("displayLabel") : regulatoryCode;

                                var reqProducts = oRecord.getData("itemData")["prop_bcpg_rclSourcesV2"];
                                desc += '<div class="rclReq-details">';
                                if (reqType) {
                                    desc += '   <div class="icon" ><span class="reqType' + reqType + '" title="'
                                            + Alfresco.util.encodeHTML(this.msg("data.reqtype." + reqType.toLowerCase())) + '">&nbsp;</span></div>';
                                }
                                var displayMsg = oRecord.getData("itemData")["prop_bcpg_rclReqMessage"].displayValue;
                                var errorLog = oRecord.getData("itemData")["prop_bcpg_rclErrorLog"] ? oRecord.getData("itemData")["prop_bcpg_rclErrorLog"].value : null;
                                if (regulatoryCode && displayMsg) {
                                    desc += '      <div class="rclReq-regulatoryCode">'
                                        + Alfresco.util.encodeHTML(regulatoryLabel) + '</div>';
                                    desc += '      <div class="rclReq-title">'
                                        + Alfresco.util.encodeHTML(displayMsg.replace(regulatoryCode,"")) + '</div>';
                                } else {
                                    desc += '      <div class="rclReq-title">'
                                        + Alfresco.util.encodeHTML(displayMsg) + '</div>';
                                }
                                if (errorLog) {
                                    desc += '      <div class="rclReq-formula-error" title="' + Alfresco.util.encodeHTML(errorLog) + '">'
                                        + Alfresco.util.encodeHTML(errorLog) + '</div>';
                                }
                                desc += '      <div class="rclReq-content"><ul>';

                                if (reqProducts) {
                                    for (var i in reqProducts) {
                                        var product = reqProducts[i], pUrl = beCPG.util.entityURL(product.siteId, product.value);
                                        var dataList = null;

                                        if (product.metadata == "ing") {

                                            var ingNodeRef = new Alfresco.util.NodeRef(product.value);
                                            desc += '<li><span class="' + product.metadata + '" ><span class="bcpg_ingList#' + ingNodeRef.id + '"><a class="charact-details '
                                                +instance.id +CHARACTDETAILS_EVENTCLASS+'" href="">'
                                                + Alfresco.util.encodeHTML(product.displayValue) + '</a></span></span></li>';

                                        } else {

                                            if (product.metadata.indexOf("finishedProduct") != -1
                                                || product.metadata.indexOf("semiFinishedProduct") != -1) {
                                                dataList = "compoList";
                                            } else if (product.metadata.indexOf("packagingKit") != -1) {
                                                dataList = "packagingList";
                                            }

                                            switch (reqDataType) {
                                                case 'Labelling':
                                                    dataList = "ingLabelingList";
                                                    break;
                                                case 'Labelclaim':
                                                    dataList = "labelClaimList";
                                                    break;
                                                case 'Physicochem':
                                                    dataList = "physicoChemList";
                                                    break;
                                                case 'Nutrient':
                                                    dataList = "nutList";
                                                    break;
                                                case 'Ingredient':
                                                    dataList = "ingList";
                                                    break;
                                                case 'Allergen':
                                                    dataList = "allergenList";
                                                    break;
                                                case 'Cost':
                                                    dataList = "costList";
                                                    break;
                                            }

                                            if (dataList != null) {
                                                pUrl = beCPG.util.entityURL(product.siteId, product.value, null, null, dataList);
                                            }

                                            if (pUrl) {
                                                pUrl += "&bcPath=true&bcList=" + this.options.list;
                                            }

                                            desc += '<li><span class="' + product.metadata + '" ><a href="' + pUrl + '">'
                                                + Alfresco.util.encodeHTML(product.displayValue) + '</a></span></li>';
                                        }

                                    }
                                }
                                desc += '</ul></div>';
                                desc += '   </div>';
                                desc += '   <div class="clear"></div>';
                                desc += '</div>';

                            }
                            elCell.innerHTML = desc;
                        },
                        /**
                         * Reloads the DataTable
                         * 
                         * @method reloadDataTable
                         */
                        reloadDataTable : function SimpleDocList_reloadDataTable() {
                            if (this.options.localRequirements) {
                                this.renderLocalRequirements();
                                return;
                            }
                            this.widgets.notificationsDataTable.loadDataTable(this.getParameters());
                        },

                        destroy : function (){

                            if (!this.options.scores) {
                                YAHOO.Bubbling.unsubscribe("refreshDataGrids",this.loadPanelData, this);
                            }
                            this.widgets.showNotificationsButton.destroy();
                            if (this.widgets.notificationsDataTable) {
                                this.widgets.notificationsDataTable.destroy();
                            }
                            this.options.containerDiv.parentNode.removeChild(this.options.containerDiv);
                            this.options.containerDiv.innerHTML = "";
                            this.widgets.panelDiv.innerHTML = "";
                            
                        }
                        
                        

                    }, true);

})();