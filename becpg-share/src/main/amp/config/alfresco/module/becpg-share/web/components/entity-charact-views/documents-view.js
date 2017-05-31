/*******************************************************************************
 *  Copyright (C) 2010-2017 beCPG. 
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
 * 
 * @namespace beCPG
 * @class beCPG.component.DocumentsView
 */
(function() {

	/**
	 * DocumentsView constructor.
	 * 
	 * @param {String}
	 *            htmlId The HTML id of the parent element
	 * @return {beCPG.component.Properties} The new Properties instance
	 * @constructor
	 */
	beCPG.component.DocumentsView = function DocumentsView_constructor(htmlId) {
		beCPG.component.DocumentsView.superclass.constructor.call(this, htmlId);

		// Path filterId
		this.filterId = "path";

		// Register with Filter Manager
		Alfresco.util.FilterManager.register("beCPG.DocListTree", this.filterId);

		YAHOO.Bubbling.on("versionChangeFilter", this.onVersionChanged, this);
		return this;
	};

	YAHOO.extend(beCPG.component.DocumentsView, Alfresco.component.Base, {

		options : {
			currVersionNodeRef : null
		},

		fileUpload : null,
		/**
		 * Fired by YUI when parent element is available for scripting. Initial
		 * History Manager event registration
		 * 
		 * @method onReady
		 */
		onReady : function DocumentsView_onReady() {

		},

		onVersionChanged : function Properties_onVersionChanged(layer, args) {
			alert("Not implemented Yet, please use download link.");
		}

	});
})();
