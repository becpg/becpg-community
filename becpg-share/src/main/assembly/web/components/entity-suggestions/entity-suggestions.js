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
(function() {

	/**
	 * EntitySuggestions constructor.
	 * 
	 * @param htmlId
	 *            {String} The HTML id of the parent element
	 * @return {beCPG.component.EntitySuggestions} The new EntitySuggestions
	 *         instance
	 * @constructor
	 */
	beCPG.component.EntitySuggestions = function(htmlId) {
		beCPG.component.EntitySuggestions.superclass.constructor.call(this, "beCPG.component.EntitySuggestions", htmlId, ["button", "container"]);

		YAHOO.Bubbling.on("showSuggestionsPanel", this.launchSuggestions, this);

		return this;
	};

	/**
	 * Extend from Alfresco.component.Base
	 */
	YAHOO.extend(beCPG.component.EntitySuggestions, Alfresco.component.Base);

	/**
	 * Augment prototype with main class implementation, ensuring overwrite is
	 * enabled
	 */
	YAHOO.lang.augmentObject(beCPG.component.EntitySuggestions.prototype, {
		/**
		 * Object container for initialization options
		 * 
		 * @property options
		 * @type object
		 */
		options: {

			entityNodeRef: "",

			ticket: null
		},



		isIFrameLoaded: false,


		onReady: function EntitySuggestions_onReady() {
		//Do Nothing
		},


		launchSuggestions: function() {
			var me = this;

			var suggestionsDiv = YAHOO.util.Dom.get(me.id + "-entity-suggestions");

			if (!me.isIFrameLoaded) {

				// Load iframe with chat URL including current URL
				suggestionsDiv.innerHTML = '<iframe src="' + Alfresco.constants.URL_CONTEXT + 'proxy/ai/suggestions?ticket=' + me.options.ticket + '&nodeRef=' + me.options.entityNodeRef + '&locale=' + me.options.locale + '" referrerpolicy="origin" sandbox="allow-scripts allow-forms allow-same-origin allow-popups allow-popups-to-escape-sandbox"></iframe>';
				me.isIFrameLoaded = true;
			}

		}


	});
})();