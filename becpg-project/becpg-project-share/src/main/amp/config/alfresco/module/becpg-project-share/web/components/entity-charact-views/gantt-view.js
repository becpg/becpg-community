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

var g = null; // uggly gantt var

(function()
{
    /**
     * YUI Library aliases
     */
    var Bubbling = YAHOO.Bubbling;

    /**
     * Alfresco Slingshot aliases
     */
    var $html = Alfresco.util.encodeHTML, $links = Alfresco.util.activateLinks, $userProfile = Alfresco.util.userProfileLink, $isValueSet = Alfresco.util.isValueSet;

    beCPG.module.GanttViewRenderer = function()
    {
        // Renderers
        Bubbling.on("registerGanttRenderer", this.onRegisterGanttRenderer, this);
    };

    YAHOO.lang.augmentObject(beCPG.module.GanttViewRenderer.prototype,
    {

        ganttRenderers : {},
        onRegisterGanttRenderer : function GanttView_onRegisterGanttRenderer(layer, args)
        {

            var obj = args[1];
            if (obj && $isValueSet(obj.typeName))
            {
                this.ganttRenderers[obj.typeName] = obj;

            }
        }
    });

    beCPG.module.GanttViewRendererHelper = new beCPG.module.GanttViewRenderer();

    /**
     * GanttView constructor.
     * 
     * @param htmlId
     *            {String} The HTML id of the parent element
     * @return {beCPG.component.GanttView} The new GanttView instance
     * @constructor
     */
    beCPG.component.GanttView = function(htmlId)
    {

        beCPG.component.GanttView.superclass.constructor.call(this, htmlId);

        Bubbling.on("viewModeChange", this.onViewModeChange, this);
        Bubbling.on("dirtyDataTable", function(){
             YAHOO.Bubbling.fire("refreshDataGrids", {updateOnly : true});
        }, this);

        JSGantt.register(this);

        return this;
    };

    /**
     * Extend from Alfresco.component.Base
     */
    YAHOO.extend(beCPG.component.GanttView, beCPG.module.EntityDataGrid);

    /**
     * Augment prototype with Actions module
     */
    YAHOO.lang.augmentProto(beCPG.component.GanttView, beCPG.component.ProjectCommons);

    /**
     * Augment prototype with main class implementation, ensuring overwrite is
     * enabled
     */
    YAHOO.lang.augmentObject(beCPG.component.GanttView.prototype,
    {

        view : "gantt",

        taskLegends : [],

        ganttRenderers : {},

        cache : [],

        // Hook setup dataTable
        onDatalistColumns : function GanttView_onDatalistColumns(response)
        {
            var me = this;
            if (g == null)
            {
                var p_type = this.options.itemType != null ? this.options.itemType : this.datalistMeta.itemType;
                if (beCPG.module.GanttViewRendererHelper.ganttRenderers.hasOwnProperty(p_type))
                {
                    var ganttRenderer = beCPG.module.GanttViewRendererHelper.ganttRenderers[p_type];

                    ganttRenderer.ganttInitialiser.call(this, function()
                    {
                        beCPG.component.GanttView.superclass.onDatalistColumns.call(me, response);
                    });

                    return;
                }
            }
            beCPG.component.GanttView.superclass.onDatalistColumns.call(me, response);

        },

        onViewModeChange : function GanttView_onViewModeChange()
        {
            if (this.view == "dataTable")
            {
                this.view = "gantt";
            }
            else
            {
                this.view = "dataTable";
            }
            
            this.refreshView();
        },
        refreshView : function GanttView_refreshView()
        {

            if (this.view == "gantt")
            {
                Dom.addClass(this.id + "-datagridBarBottom", "hidden");
                Dom.addClass(this.id + "-itemSelect-div", "hidden");
                Dom.addClass(this.id + "-grid", "hidden");
                Dom.removeClass(this.id + "-gantt", "hidden");
                this.cache = [];
                g.Draw();
                g.DrawDependencies();

            }  else {
                Dom.addClass(this.id + "-gantt", "hidden");
                Dom.removeClass(this.id + "-grid", "hidden");
                Dom.removeClass(this.id + "-datagridBarBottom", "hidden");
                Dom.removeClass(this.id + "-itemSelect-div", "hidden");
            }

        }

    }, true);

})();
