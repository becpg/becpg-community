/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG.
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

(function() {
    /**
     * YUI Library aliases
     */
	var Bubbling = YAHOO.Bubbling;

    /**
     * Alfresco Slingshot aliases
     */
	var $isValueSet = Alfresco.util.isValueSet;

	var PREF_VIEW_MODE = "org.alfresco.share.project.gantt.mode";
	

	beCPG.module.GanttViewRenderer = function() {
		// Renderers
		Bubbling.on("registerGanttRenderer", this.onRegisterGanttRenderer, this);
	};

	YAHOO.lang.augmentObject(beCPG.module.GanttViewRenderer.prototype,
		{

			ganttRenderers: {},
			onRegisterGanttRenderer: function GanttView_onRegisterGanttRenderer(layer, args) {

				var obj = args[1];
				if (obj && $isValueSet(obj.typeName)) {
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
	beCPG.component.GanttView = function(htmlId) {


		beCPG.component.GanttView.superclass.constructor.call(this, htmlId);

		// Preferences service
		this.services.preferences = new Alfresco.service.Preferences();


		Bubbling.on("viewModeChange", this.onViewModeChange, this);
		Bubbling.on("toggleProjectDetails", this.toggleProjectDetails, this);

		Bubbling.on("dirtyDataTable", function() {
			YAHOO.Bubbling.fire("refreshDataGrids", { updateOnly: true });
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

			taskLegends: [],

			ganttRenderers: {},

			cache: [],

			detailsLoaded: false,


			// Hook setup dataTable
			onDatalistColumns: function GanttView_onDatalistColumns(response) {
				var me = this;
				if (g == null) {
					var p_type = this.options.itemType != null ? this.options.itemType : this.datalistMeta.itemType;
					if (beCPG.module.GanttViewRendererHelper.ganttRenderers.hasOwnProperty(p_type)) {
						var ganttRenderer = beCPG.module.GanttViewRendererHelper.ganttRenderers[p_type];

						ganttRenderer.ganttInitialiser.call(this, function() {
							beCPG.component.GanttView.superclass.onDatalistColumns.call(me, response);
						});

						return;
					}
				}
				beCPG.component.GanttView.superclass.onDatalistColumns.call(me, response);

			},

			onViewModeChange: function GanttView_onViewModeChange() {
				if (this.options.viewMode == "dataTable") {
					this.options.viewMode = "gantt";
				}
				else {
					this.options.viewMode = "dataTable";
				}

				this.services.preferences.set(PREF_VIEW_MODE, this.options.viewMode);


				this.refreshView();
			},
			
			refreshView: function GanttView_refreshView() {

				if (this.options.viewMode != "gantt") {

					Dom.addClass(this.id + "-gantt", "hidden");
					Dom.addClass(this.id + "-legend", "hidden");
					Dom.removeClass(this.id + "-grid", "hidden");
					Dom.removeClass(this.id + "-datagridBarBottom", "hidden");
					Dom.removeClass(this.id + "-itemSelect-div", "hidden");
					YAHOO.Bubbling.fire("refreshFloatingHeader");

				} else {
					Dom.addClass(this.id + "-datagridBarBottom", "hidden");
					Dom.addClass(this.id + "-itemSelect-div", "hidden");
					Dom.addClass(this.id + "-grid", "hidden");
					Dom.removeClass(this.id + "-gantt", "hidden");
					Dom.removeClass(this.id + "-legend", "hidden");
					this.cache = [];
					g.Draw();
					g.DrawDependencies();

				}

			},
			toggleProjectDetails: function GanttView_toggleProjectDetails() {
				var panel = Dom.get(this.id + "-project-details"), me = this;
				if (Dom.hasClass(panel, "hidden")) {
					if (!this.detailsLoaded) {

						Alfresco.util.Ajax.request({
							url: Alfresco.constants.URL_SERVICECONTEXT + "modules/project-details/project-details?nodeRef=" + this.options.entityNodeRef,
							dataObj: {
								htmlid: me.id + "-project-details-panel"
							},
							successCallback: {
								fn: function(response) {
									panel.innerHTML = response.serverResponse.responseText;

									Dom.removeClass(panel, "hidden")
									Dom.setStyle(panel, "width", "30%");
									Dom.setStyle(panel, "float", "left");
									Dom.setStyle(me.id + "-project-list", "width", "70%");
									Dom.setStyle(me.id + "-project-list", "float", "left");
									YAHOO.Bubbling.fire("refreshFloatingHeader");
								},
								scope: this
							},
							scope: this,
							execScripts: true
						});

					} else {
						Dom.removeClass(panel, "hidden")
						Dom.setStyle(panel, "width", "30%");
						Dom.setStyle(panel, "float", "left");
						Dom.setStyle(me.id + "-project-list", "width", "70%");
						Dom.setStyle(me.id + "-project-list", "float", "left");
						YAHOO.Bubbling.fire("refreshFloatingHeader");
					}


				} else {
					Dom.addClass(panel, "hidden");
					Dom.setStyle(this.id + "-project-list", "width", "100%");
					YAHOO.Bubbling.fire("refreshFloatingHeader");
				}

			}

		}, true);

})();
