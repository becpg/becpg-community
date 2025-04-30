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
	 * UserWebSocket constructor.
	 * 
	 * @param htmlId
	 *            {String} The HTML id of the parent element
	 * @return {beCPG.component.UserWebSocket} The new UserWebSocket
	 *         instance
	 * @constructor
	 */
	beCPG.component.UserWebSocket = function(htmlId) {
		beCPG.component.UserWebSocket.superclass.constructor.call(this, "beCPG.component.UserWebSocket", htmlId, ["button", "container"]);
		YAHOO.Bubbling.on("dirtyDataTable", this.entityUpdated, this);
		return this;
	};
	
	/**
	 * Extend from Alfresco.component.Base
	 */
	YAHOO.extend(beCPG.component.UserWebSocket, Alfresco.component.Base);

	/**
	 * Augment prototype with main class implementation, ensuring overwrite is
	 * enabled
	 */
	YAHOO.lang.augmentObject(beCPG.component.UserWebSocket.prototype, {
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

			mode: null
		},

		/**
		 * Fired by YUI when parent element is available for scripting.
		 * 
		 * @method onReady
		 */
		onReady: function UserWebSocket_onReady() {
			this.registerWebSocket();
		},

		entityUpdated: function NodeHeader_entityUpdated(layer, args){
        	  if(this.ws!=null){
        		  var message = {
             			 type : "UPDATE",
             			 user : Alfresco.constants.USERNAME	 
             	 };
        		  this.ws.send( YAHOO.lang.JSON.stringify(message) );
        	  }
          },
                  
		registerWebSocket: function registerWebSocket() {
			if ("WebSocket" in window && this.ws == null) {
				var protocolPrefix = (window.location.protocol === 'https:') ? 'wss:' : 'ws:', me = this;

				me.ws = new WebSocket(protocolPrefix + '//' + location.host + Alfresco.constants.URL_CONTEXT + "becpgws/" + this.options.nodeRef.replace(":/", "") + "/" + encodeURIComponent(Alfresco.constants.USERNAME) + "/" + this.options.mode);

				me.ws.onmessage = function(evt) {

					var message = YAHOO.lang.JSON.parse(evt.data);
					if (message.type && message.type == "JOINING" && message.user != Alfresco.constants.USERNAME) {

						if (YAHOO.util.Dom.get(me.id + "-chat-user-" + message.user) == null) {
							var ulEl = YAHOO.util.Dom.get(me.id + "-node-users");
							var html = ulEl.innerHTML;

							html += '<span id="' + me.id + '-chat-user-' + message.user + '" class="avatar-container" title="' + message.user + '">';
							html += '<img src="' + Alfresco.constants.PROXY_URI + 'slingshot/profile/avatar/' + encodeURIComponent(message.user) + '/thumbnail/avatar32" alt="Avatar" class="avatar-image">';

							html += '<span id="' + me.id + '-edit-indicator-' + message.user + '" class="edit-indicator" style="display: none;">✏️</span>';

							html += "</span>";
							ulEl.innerHTML = html;
						}

						if (message.mode === "edit") {
							var indicator = document.getElementById(me.id + '-edit-indicator-' + message.user);
							if (indicator) indicator.style.display = "inline";
						}

					} else if (message.type == "LEAVING" && message.user != Alfresco.constants.USERNAME) {
						var child = document.getElementById(me.id + "-chat-user-" + message.user);
						if (child != null) {
							child.parentNode.removeChild(child);
						}

					} else if (message.type == "UPDATE" && message.user != Alfresco.constants.USERNAME) {
						var indicator = document.getElementById(me.id + '-edit-indicator-' + message.user);
						if (indicator) indicator.style.display = "inline";

						setTimeout(function() {
							YAHOO.Bubbling.fire("refreshDataGrids");
							// Hide the edit indicator after timeout
							var indicator = document.getElementById(me.id + '-edit-indicator-' + message.user);
							if (indicator) indicator.style.display = "none";
						}, 3000);
					}

					else if (message.user != Alfresco.constants.USERNAME && message.mode === "edit") {
						var indicator = document.getElementById(me.id + '-edit-indicator-' + message.user);
						if (indicator) indicator.style.display = "inline";
					}

				};

			}
		},

	});
})();