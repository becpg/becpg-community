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
if (beCPG.module.EntityDataGridRenderers) {

    YAHOO.Bubbling.on("dirtyDataTable", function(event, args) {
        if (args && args.length > 1) {
            var field = args[1].column.field;
            if (field == "prop_pjt_tlState" || field == "prop_pjt_tlDuration" || field == "prop_pjt_tlWork" || field == "prop_pjt_blBudgetedExpense" || field == "prop_pjt_blBudgetedInvoice" || field == "prop_pjt_ltlTime") {
                YAHOO.Bubbling.fire("refreshDataGrids", { updateOnly: true });
            }
        }
    }, this);


    YAHOO.Bubbling.fire("registerDataGridRenderer", {
        propertyName: ["pjt:tlTaskName"],
        renderer: function(oRecord, data, label, scope, z, zz, elCell, oColumn) {

            var task = oRecord.getData();

            var padding = 0, className = oRecord.getData("itemData")["prop_pjt_tlIsMilestone"].value ? "task-milestone" : "task", toogleGroupButton = null;

            var tr = scope.widgets.dataTable.getTrEl(elCell);

            if (oRecord.getData("itemData")["prop_bcpg_depthLevel"] && oRecord.getData("itemData")["prop_bcpg_depthLevel"].value) {
                padding = (oRecord.getData("itemData")["prop_bcpg_depthLevel"].value - 1) * 25;

                Dom.addClass(tr, "mtl-level-" + oRecord.getData("itemData")["prop_bcpg_depthLevel"].value);
            }

            if (false === oRecord.getData("itemData")["isLeaf"]) {
                toogleGroupButton = '<div id="group_' + (oRecord.getData("itemData")["open"] ? "expanded" : "collapsed") + '_' + oRecord.getData("nodeRef") + '" style="margin-left:' + padding
                    + 'px;" class="onCollapsedAndExpanded" ><a href="#" class="' + scope.id + '-action-link"><span class="gicon ggroup-'
                    + (oRecord.getData("itemData")["open"] ? "expanded" : "collapsed") + '"></span></a></div>';
                Dom.addClass(tr, "mtl-" + (oRecord.getData("itemData")["open"] ? "expanded" : "collapsed"));

            } else if (true === oRecord.getData("itemData")["isLeaf"]) {
                padding += 25;

                Dom.addClass(tr, "mtl-leaf");
            }

            return (toogleGroupButton != null ? toogleGroupButton : '')
                + '<span class="' + className + ' task-status" ' + (toogleGroupButton == null && padding != 0 ? 'style="margin-left:' + padding + 'px;"' : '') + ' >' + scope.getTaskTitle(task, scope.options.entityNodeRef, false, -1, true, true) + '</span>';
        }

    });


    YAHOO.Bubbling.fire("registerDataGridRenderer", {
        propertyName: "pjt:projectCurrentTasks",
        renderer: function(oRecord, data, label, scope, idx, length) {
            return '<span class="' + data.metadata + '"  >' + Alfresco.util.encodeHTML(data.displayValue) + '</span>';
        }
    });


    YAHOO.Bubbling.fire("registerDataGridRenderer", {
        propertyName: ["pjt:tlState", "pjt:dlState"],
        renderer: function(oRecord, data, label, scope) {
            return '<span class="' + "task-" + data.value.toLowerCase() + '" title="' + data.displayValue + '" />';
        }

    });


    YAHOO.Bubbling.fire("registerDataGridRenderer", {
        propertyName: "pjt:slScreening",
        renderer: function(oRecord, data, label, scope) {
            if (data.displayValue) {
                return '<div class="score-screening">' + data.displayValue + '</div>';
            }
            return "";
        }
    });


    YAHOO.Bubbling.fire("registerDataGridRenderer", {
        propertyName: "pjt:slScoreRange",
        renderer: function(oRecord, data, label, scope) {
            if (data.displayValue) {
                return '<span class="score-letter-' + data.displayValue.toLowerCase() + '">' + data.displayValue + '</div>';
            }
            return "";
        }
    });


    YAHOO.Bubbling.fire("registerDataGridRenderer", {
        propertyName: "pjt:slScore",
        renderer: function(oRecord, data, label, scope) {

        /*    var showColor = true;
            if (oRecord.getData("itemData")["prop_pjt_slScoreRange"] && oRecord.getData("itemData")["prop_pjt_slScoreRange"].value) {
                showColor = false;
            }
            if(showColor){*/
                var className = "";
                if (data.value != null) {
                    if (data.value < 0) {
                        className = "score-black";
                    } else if (data.value < 20) {
                        className = "score-red";
                    }
                    else if (data.value < 40) {
                       className = "score-orange";
                    }
                    else if (data.value < 60) {
                        className = "score-yellow";
                    }
                    else if (data.value < 80) {
                        className = "score-blue";
                    }
                    else {
                        className = "score-green";
                    }
                }
                return '<span class="' + className + '" >' + Alfresco.util.encodeHTML(data.displayValue) + '</span>';
           /* } else {
                return Alfresco.util.encodeHTML(data.displayValue);
            }*/
        }
    });


    YAHOO.Bubbling.fire("registerDataGridRenderer", {
        propertyName: ["pjt:blItem"],
        renderer: function(oRecord, data, label, scope) {

            var padding = 0;
            if (oRecord.getData("itemData")["prop_bcpg_depthLevel"] && oRecord.getData("itemData")["prop_bcpg_depthLevel"].value) {
                padding = (oRecord.getData("itemData")["prop_bcpg_depthLevel"].value - 1) * 25;
            }

            return '<span style="margin-left:' + padding + 'px;" >' + Alfresco.util.encodeHTML(data.displayValue) + '</span>';
        }

    });

    YAHOO.Bubbling.fire("registerDataGridRenderer", {
        propertyName: ["pjt:tlRealDuration"],
        renderer: function(oRecord, data, label, scope) {

            var className = "";
            if (data.value && oRecord.getData("itemData")["prop_pjt_tlDuration"].value && data.value > oRecord.getData("itemData")["prop_pjt_tlDuration"].value) {
                className = "red";
            }
            return '<span class="' + className + '" >' + Alfresco.util.encodeHTML(data.displayValue) + '</span>';
        }

    });

    YAHOO.Bubbling.fire("registerDataGridRenderer", {
        propertyName: ["pjt:tlLoggedTime"],
        renderer: function(oRecord, data, label, scope) {

            var className = "";
            if (data.value && oRecord.getData("itemData")["prop_pjt_tlWork"].value && data.value > oRecord.getData("itemData")["prop_pjt_tlWork"].value) {
                className = "red";
            }

            return '<span class="' + className + '" >' + Alfresco.util.encodeHTML(data.displayValue) + '</span>';
        }

    });

    YAHOO.Bubbling.fire("registerDataGridRenderer", {
        propertyName: ["pjt:tlCapacity"],
        renderer: function(oRecord, data, label, scope) {

            var className = "";
            if (data.value && data.value > 100) {
                className = "red";
            }

            return '<span class="' + className + '" >' + Alfresco.util.encodeHTML(data.displayValue) + '</span>';
        }

    });


    YAHOO.Bubbling.fire("registerDataGridRenderer", {
        propertyName: ["pjt:projectEntity"],
        renderer: function(oRecord, data, label, scope) {

            var url = beCPG.util.entityURL(data.siteId, data.value), version = "";

            if (data.version && data.version !== "") {
                version = '<span class="document-version">' + data.version + '</span>';
            }
            return '<span class="' + data.metadata + '" ><a href="' + url + '">' + Alfresco.util.encodeHTML(data.displayValue)
                + '</a></span>' + version;
        }
    });

    YAHOO.Bubbling.fire("registerDataGridRenderer", {
        propertyName: ["pjt:tlTargetEnd"],
        renderer: function(oRecord, data, label, scope) {

            if (data.value != null) {
                var endDate = oRecord.getData()["itemData"]["prop_pjt_tlEnd"].value;
                var targetEnd = data.value;
                targetEnd = targetEnd != null ? scope.resetDate(Alfresco.util.fromISO8601(targetEnd)) : null;
                endDate = endDate != null ? scope.resetDate(Alfresco.util.fromISO8601(endDate)) : null;

                if (endDate != null && targetEnd != null && endDate.getTime() > targetEnd.getTime()) {
                    return '<span class="red">' + Alfresco.util.encodeHTML(data.displayValue) + '</span>';
                }
            }
            return Alfresco.util.encodeHTML(data.displayValue);
        }

    });

    YAHOO.Bubbling.fire("registerDataGridRenderer", {
        propertyName: ["pjt:tlTargetStart"],
        renderer: function(oRecord, data, label, scope) {

            if (data.value != null) {
                var startDate = oRecord.getData()["itemData"]["prop_pjt_tlStart"].value;
                var targetStart = data.value;
                startDate = startDate != null ? scope.resetDate(Alfresco.util.fromISO8601(startDate)) : null;
                targetStart = targetStart != null ? scope.resetDate(Alfresco.util.fromISO8601(targetStart)) : null;

                if (startDate != null && targetStart != null && startDate.getTime() > targetStart.getTime()) {
                    return '<span class="red">' + Alfresco.util.encodeHTML(data.displayValue) + '</span>';
                }
            }
            return Alfresco.util.encodeHTML(data.displayValue);
        }

    });


}
