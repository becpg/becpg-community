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
			entityNodeRef: "",

			ticket: null
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


			YAHOO.Bubbling.addDefaultAction(this.id + "-apply-suggestion", this.applySuggestion);

		},

		subscribeToSuggestions: function() {
			var me = this;
			var counter = 0;

			me.stompClient.subscribe('/queue/suggestions', function(suggestionResponse) {
				counter++;
				var suggestions = JSON.parse(suggestionResponse.body);
				var suggestionHtml = '<div class="card" id="suggestion-' + counter + '" data-id="' + counter + '">';
				suggestionHtml += '<div class="card-body">';
				suggestionHtml += '<div class="row">';
				suggestionHtml += '<div class="col-sm-3">';

				if (suggestions.type == "Image" && suggestions.suggestedChanges != null) {
					suggestionHtml += '<img width="128px" height="128px" src="data:image/png;base64, ' + suggestions.suggestedChanges.attributes["cm:contains"][0].attributes["cm:contains"][0].content + '">';
				}
				suggestionHtml += '</div>';
				suggestionHtml += '<div class="col-sm-7">';
				suggestionHtml += '<h5 class="card-title"><b>' + suggestions.type + '</b></h5>';

				if ((suggestions.type == "I18n" || suggestions.type == "Organoleptic") && suggestions.suggestedChanges != null) {
					suggestionHtml += suggestions.description.split(" : ")[0] + " : ";
					suggestionHtml += '<ul>';
					var descriptionSplit = suggestions.description.split(" : ")[1].split("  ");
					for (var i = 0; i < descriptionSplit.length; i++) {
						var data = descriptionSplit[i];
						var languageDescriptionSplit = data.split(" , ");
						var language = languageDescriptionSplit[0];
						var description = languageDescriptionSplit[1];
						suggestionHtml += '<li>' + language + " : " + description + '</li>';
					}
					suggestionHtml += '</ul>';
				}

				if (suggestions.type == "SpellCheck" && suggestions.suggestedChanges != null) {
					var spellMistakes = suggestions.description.split(", ");
					var fileType = spellMistakes.shift();
					suggestionHtml += '<ul>';
					suggestionHtml += fileType + '<br>' + suggestions.suggestedChanges.attributes[Object.keys(suggestions.suggestedChanges.attributes)];
					for (var j = 0; j < spellMistakes.length; j++) {
						var mistakeSplit = spellMistakes[j].split(" -> ");
						var original = mistakeSplit[0];
						var corrected = mistakeSplit[1];
						suggestionHtml += '<li>' + original + ' -> ' + corrected + '</li>';
					}
					suggestionHtml += '</ul>';
				}

				if ((suggestions.type == "ClassificationGS1" || suggestions.type == "ClassificationBCPG") && suggestions.suggestedChanges != null) {
					var descriptions = suggestions.description.split(": ");
					suggestionHtml += '<p class="card-text">' + descriptions[0] + " : " + '<br>' + descriptions[1] + '</p>';
				}

				if (suggestions.type != "I18n" && suggestions.type != "SpellCheck" && suggestions.type != "Organoleptic" && suggestions.type != "ClassificationGS1" && suggestions.type != "ClassificationBCPG") {
					suggestionHtml += '<p class="card-text">' + suggestions.description + ' ' + '</p>';
				}
				suggestionHtml += '</div>';
				suggestionHtml += '<div class="col-sm-2">';
				suggestionHtml += '<a class="btn btn-primary ' + me.id + '-apply-suggestion" data-id="suggestion-' + counter + '" data-entity="' + encodeURIComponent(JSON.stringify(suggestions.suggestedChanges)) + '">' + me
					.msg("button.apply") + '</a>';
				suggestionHtml += '</div>';
				suggestionHtml += '</div>';
				suggestionHtml += '</div>';
				suggestionHtml += '</div>';

				var suggestionsDiv = YAHOO.util.Dom.get(me.id + "-entity-suggestions");
				suggestionsDiv.innerHTML += suggestionHtml;
			});
		},

		connectToStomp: function() {
			var protocolPrefix = (window.location.protocol === 'https:') ? 'wss:' : 'ws:', me = this;

			var socket = new WebSocket(protocolPrefix + location.host + Alfresco.constants.URL_CONTEXT + "aiws?ticket=" + me.options.ticket, "v12.stomp");

			me.stompClient = Stomp.over(socket);
			//TODO Remove that
			me.stompClient.debug = function(str) { console.log(str) };
			me.stompClient.connect({ "BECPG_TICKET": me.options.ticket }, function() {
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
					me.stompClient.send("/app/suggestions", {}, JSON.stringify({ entityId: me.options.entityNodeRef, locale: Alfresco.constants.JS_LOCALE }));
				}
			}, 100);
		},


		applySuggestion: function(layer, args) {

			var owner = args[1].anchor, me = this;
			var remoteEntity = JSON.parse(decodeURIComponent(owner.getAttribute("data-entity")));

			var url = Alfresco.constants.URL_CONTEXT + "proxy/ai/suggestions/apply";

			Alfresco.util.Ajax
				.jsonPost(
					{
						url: url,
						dataObj: remoteEntity,
						successCallback:
						{
							fn: function(
								response) {
								YAHOO.util.Dom.remove(YAHOO.util.Dom.get(owner.getAttribute("data-id")));
							},
							scope: this
						},
						failureCallback:
						{
							fn: function(
								response) {
								Alfresco.util.PopupManager
									.displayMessage(
										{
											text: me
												.msg("message.details.failure")
										});
							},
							scope: this
						}
					});
		}


	});
})();