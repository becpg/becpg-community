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
			/**
			 * Current entityNodeRef.
			 * 
			 * @property entityNodeRef
			 * @type string
			 * @default ""
			 */
			entityNodeRef: ""
		},

		stompClient: null,

		isStompConnected: false,

		/**
		 * Fired by YUI when parent element is available for scripting.
		 * 
		 * @method onReady
		 */
		onReady: function EntitySuggestions_onReady() {

			var suggestionsDiv = YAHOO.util.Dom.get(this.id + "-entity-suggestions");

			suggestionsDiv.innerHTML = '<span class="wait">' + Alfresco.util.encodeHTML(this.msg("label.loading")) + '</span>';

			var formulateButton = YAHOO.util.Selector.query('div.formulate');

			if (formulateButton != null) {
				YAHOO.util.Dom.addClass(formulateButton, "loading");
			}

		},

		subscribeToSuggestions: function() {
			stompClient.subscribe('/queue/suggestions', function(suggestionResponse) {
				var suggestions = JSON.parse(suggestionResponse.body);
				var suggestionHtml = "";
				for (var i = 0; i < suggestions.length; i++) {
					suggestionHtml += '<div class="card">';
					suggestionHtml += '<div class="card-body">';
					suggestionHtml += '<div class="row">';
					suggestionHtml += '<div class="col-sm-3">';
					if (suggestions[i].type == "Image" && suggestions[i].suggestedChanges != null) {
						suggestionHtml += '<img width="128px" height="128px" src="data:image/png;base64, ' + suggestions[i].suggestedChanges.attributes["cm:contains"][0].attributes["cm:contains"][0].content + '">';
					} else {
						suggestionHtml += '<i class="material-icons card-icon">settings_suggest</i>';
					}
					suggestionHtml += '</div>';
					suggestionHtml += '<div class="col-sm-7">';
					suggestionHtml += '<h5 class="card-title">' + suggestions[i].type + '</h5>';
					suggestionHtml += '<p class="card-text">' + suggestions[i].description + '</p>';
					suggestionHtml += '</div>';
					suggestionHtml += '<div class="col-sm-2">';
					suggestionHtml += '<button class="btn btn-primary apply-btn" data-entity="' + encodeURIComponent(JSON.stringify(suggestions[i].suggestedChanges)) + '">Apply</button>';
					suggestionHtml += '<button class="btn btn-secondary decline-btn" data-entity="' + encodeURIComponent(JSON.stringify(suggestions[i].suggestedChanges)) + '">Useless</button>';
					suggestionHtml += '</div>';
					suggestionHtml += '</div>';
					suggestionHtml += '</div>';
					suggestionHtml += '</div>';
				}


				var suggestionsDiv = YAHOO.util.Dom.get(this.id + "-entity-suggestions");

				suggestionsDiv.innerHTML = suggestionHtml;

			});
		},

		connectToStomp: function() {
			var protocolPrefix = (window.location.protocol === 'https:') ? 'wss:' : 'ws:', me = this;

			var socket = new WebSocket(protocolPrefix + location.host + Alfresco.constants.URL_CONTEXT + "aiws");
			
			me.stompClient = Stomp.over(socket);
			me.stompClient.connect({}, function() {
				console.log('Stomp connected!');
				me.isStompConnected = true;
				me.subscribeToSuggestions();
			}, function(error) {
				console.log('Stomp error:', error);
				me.isStompConnected = false;
			});
		},

		launchSuggestions: function() {
			var me = this;

			if (me.stompClient === null || !me.isStompConnected) {
				me.connectToStomp();
			}

			var checkStompConnectionInterval = setInterval(function() {
				if (me.isStompConnected) {
					clearInterval(checkStompConnectionInterval);
					me.stompClient.send("/app/suggestions", {}, JSON.stringify({ entityId: me.entityNodeRef }));
				}
			}, 100);
		}

	});
})();