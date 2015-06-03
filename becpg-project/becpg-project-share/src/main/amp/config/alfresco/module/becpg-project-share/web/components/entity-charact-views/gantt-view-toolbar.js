/*******************************************************************************
 * Copyright (C) 2010-2015 beCPG.
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
(function()
{
    if (beCPG.component.EntityDataListToolbar)
    {

        YAHOO.Bubbling.fire("registerToolbarButtonAction",
        {
            actionName : "toggle-gantt",
            right : true,
            evaluate : function(asset, entity)
            {
                return asset.name !== null && (asset.name.indexOf("View-gantt") > -1 || asset.name === "taskList") ;
            },
            createWidget : function(containerDiv, instance)
            {

                var divEl = document.createElement("div");

                containerDiv.appendChild(divEl);

                Dom.addClass(divEl, "ganttCkeckbox");

                var widget = new YAHOO.widget.Button(
                {
                    type : "checkbox",
                    title : instance.msg("button.toggle-gantt.description"),
                    container : divEl,
                    checked : true
                });

                widget.on("checkedChange", function()
                {
                    YAHOO.Bubbling.fire("viewModeChange");

                });

                return widget;
            }
        });

        YAHOO.Bubbling.fire("registerToolbarButtonAction",
        {
            actionName : "full-screen",
            evaluate : function(asset, entity)
            {
                return asset.name !== null && (asset.name.indexOf("View-gantt") > -1 || asset.name === "taskList") ;
            },
            fn : function(instance)
            {
                
                if (Dom.hasClass("alf-hd", "hidden"))
                {
                    Dom.removeClass("alf-hd", "hidden");
                    Dom.removeClass("alf-filters", "hidden");
                    Dom.removeClass("alf-ft", "hidden");
                    Dom.removeClass("Share", "full-screen");
                    Dom.addClass("alf-content", "yui-b");

                }
                else
                {
                    Dom.addClass("alf-hd", "hidden");
                    Dom.addClass("alf-ft", "hidden");
                    Dom.addClass("Share", "full-screen");
                    Dom.addClass("alf-ft", "hidden");
                    Dom.addClass("alf-filters", "hidden");
                    Dom.removeClass("alf-content", "yui-b");
                    Dom.setStyle("alf-content", "margin-left", null);

                }

            }
        });

    }
})();