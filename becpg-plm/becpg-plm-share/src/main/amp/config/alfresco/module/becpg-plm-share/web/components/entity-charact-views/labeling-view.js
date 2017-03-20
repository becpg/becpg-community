/*******************************************************************************
 * Copyright (C) 2010-2016 beCPG.
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
	 * LabelingView constructor.
	 * 
	 * @param htmlId
	 *            {String} The HTML id of the parent element
	 * @return {beCPG.component.LabelingView} The new LabelingView
	 *         instance
	 * @constructor
	 */
	beCPG.component.LabelingView = function(htmlId) {

		beCPG.component.LabelingView.superclass.constructor.call(this, "beCPG.component.LabelingView", htmlId, [ "button", "container" ]);
		
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
	YAHOO.extend(beCPG.component.LabelingView, Alfresco.component.Base);

	/**
	 * Augment prototype with main class implementation, ensuring overwrite is
	 * enabled
	 */
	YAHOO.lang.augmentObject(beCPG.component.LabelingView.prototype, {
		/**
		 * Object container for initialization options
		 * 
		 * @property options
		 * @type object
		 */
		options : {
			/**
			 * Current entityNodeRef.
			 * 
			 * @property entityNodeRef
			 * @type string
			 * @default ""
			 */
			entityNodeRef : ""
		},

		formulate : function LabelingView_formulate() {
			    
			
				var formulateButton = YAHOO.util.Selector.query('div.formulate'), me = this;

				Dom.addClass(formulateButton, "loading");

				var localCount = beCPG.util.incLockCount();
				
				Alfresco.util.Ajax.request({
					method : Alfresco.util.Ajax.GET,
					url : Alfresco.constants.PROXY_URI + "becpg/product/formulate/node/" + this.options.entityNodeRef.replace(":/", ""),
					successCallback : {
						fn : function(response) {
							if(beCPG.util.lockCount() == localCount){
								YAHOO.Bubbling.fire("refreshDataGrids", {
									updateOnly : true,
									callback : function() {
										Dom.removeClass(formulateButton, "loading");
									}
								});
							}
						},
						scope : this
					}
				});

		}

	});

})();
