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
(function() {

	/**
	 * FormulationView constructor.
	 * 
	 * @param htmlId
	 *            {String} The HTML id of the parent element
	 * @return {beCPG.component.FormulationView} The new FormulationView
	 *         instance
	 * @constructor
	 */
	beCPG.component.FormulationView = function(htmlId) {

		beCPG.component.FormulationView.superclass.constructor.call(this, "beCPG.component.FormulationView", htmlId, [ "button", "container" ]);

		var dataGridModuleCount = 1;

		YAHOO.Bubbling.on("dataGridReady", function(layer, args) {
			if (dataGridModuleCount == 3) {
				// Initialize the browser history management library
				try {
					YAHOO.util.History.initialize("yui-history-field", "yui-history-iframe");
				} catch (e2) {
					/*
					 * The only exception that gets thrown here is when the
					 * browser is not supported (Opera, or not A-grade)
					 */
					Alfresco.logger.error(this.name + ": Couldn't initialize HistoryManager.", e2);
					var obj = args[1];
					if ((obj !== null) && (obj.entityDataGridModule !== null)) {
						obj.entityDataGridModule.onHistoryManagerReady();
					}
				}
			}
			dataGridModuleCount++;
		});

		YAHOO.Bubbling.on("dirtyDataTable", this.formulate, this);

		return this;
	};

	/**
	 * Extend from Alfresco.component.Base
	 */
	YAHOO.extend(beCPG.component.FormulationView, Alfresco.component.Base);

	/**
	 * Augment prototype with main class implementation, ensuring overwrite is
	 * enabled
	 */
	YAHOO.lang.augmentObject(beCPG.component.FormulationView.prototype, {
		/**
		 * Object container for initialization options
		 * 
		 * @property options
		 * @type object
		 */
		options : {
			/**
			 * Current siteId.
			 * 
			 * @property siteId
			 * @type string
			 * @default ""
			 */
			siteId : "",

			/**
			 * Current entityNodeRef.
			 * 
			 * @property entityNodeRef
			 * @type string
			 * @default ""
			 */
			entityNodeRef : ""

		},

		formulationLock : false,
		
		/**
		 * Fired by YUI when parent element is available for scripting.
		 * 
		 * @method onReady
		 */
		onReady : function FormulationView_onReady() {
			var instance = this;
			YAHOO.util.Event.addListener("dynamicCharactList-" + this.id + "-colCheckbox", "click", function(e) {
				YAHOO.Bubbling.fire("dynamicCharactList-" + instance.id + "refreshDataGrid");
			});

		},

		formulate : function FormulationView_formulate() {
			if (!this.formulationLock) {

					var formulateButton = YAHOO.util.Selector.query('div.formulate'), me = this;
					
					Dom.addClass(formulateButton, "loading");

					me.formulationLock = true;
					Alfresco.util.Ajax.request({
						method : Alfresco.util.Ajax.GET,
						url : Alfresco.constants.PROXY_URI + "becpg/product/formulate/node/" + this.options.entityNodeRef.replace(":/", ""),
						successCallback : {
							fn : function(response) {
								YAHOO.Bubbling.fire("refreshDataGrids", {
									updateOnly : true,
									callback : function() {
										me.formulationLock  = false;
										Dom.removeClass(formulateButton, "loading");
									}
								});
							},
							scope : this
						}
					});
				}

		}
	});

})();
