/*******************************************************************************
 * Copyright (C) 2010-2026 beCPG.
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

	var Dom = YAHOO.util.Dom, Event = YAHOO.util.Event, $html = Alfresco.util.encodeHTML;

	/**
	 * Messages exchanged with the embedded becpg-regulatory page. Share pushes the data:
	 * the embedded page never receives a credential and never calls an API.
	 */
	var MESSAGE = {
		READY : "regulatory.ready",
		LOADING : "regulatory.loading",
		DATA : "regulatory.data",
		ERROR : "regulatory.error",
		REFRESH : "regulatory.refresh",
		NAVIGATE : "regulatory.navigate",
		RESIZE : "regulatory.resize"
	};

	var ERROR_MESSAGES = {
		notConfigured : "message.not-configured",
		invalidProduct : "message.invalid-product",
		unavailable : "message.unavailable"
	};

	var NAVIGABLE_LISTS = [ "ingList", "regulatoryList" ];
	var EMBED_PATH = "/embed/compliance";
	var READY_TIMEOUT_MS = 20000;
	var MIN_HEIGHT = 600;
	var RESIZE_TOLERANCE = 2;

	/**
	 * RegulatoryView constructor.
	 * 
	 * @param htmlId
	 *            {String} The HTML id of the parent element
	 * @return {beCPG.component.RegulatoryView} The new RegulatoryView instance
	 * @constructor
	 */
	beCPG.component.RegulatoryView = function(htmlId) {
		beCPG.component.RegulatoryView.superclass.constructor.call(this, "beCPG.component.RegulatoryView", htmlId, [ "button" ]);
		this.iframe = null;
		this.regulatoryOrigin = null;
		this.readyTimer = null;
		return this;
	};

	YAHOO.extend(beCPG.component.RegulatoryView, Alfresco.component.Base, {

		options : {
			entityNodeRef : "",
			uiUrl : ""
		},

		/**
		 * Fired by YUI when parent element is available for scripting.
		 * 
		 * @method onReady
		 */
		onReady : function RegulatoryView_onReady() {
			if (!this.options.uiUrl || !this.options.entityNodeRef) {
				return;
			}
			this.regulatoryOrigin = this.originOf(this.options.uiUrl);
			Event.addListener(window, "message", this.onMessage, this, true);
			this.createFrame();
		},

		/**
		 * Accepts a message only from the embedded frame and from the configured origin.
		 * 
		 * @method onMessage
		 * @param event
		 *            {MessageEvent} the received message
		 */
		onMessage : function RegulatoryView_onMessage(event) {
			if (this.iframe === null || event.source !== this.iframe.contentWindow || event.origin !== this.regulatoryOrigin) {
				return;
			}
			var message = event.data;
			if (!message || typeof message.type !== "string") {
				return;
			}
			if (message.type === MESSAGE.READY) {
				this.onFrameReady();
			} else if (message.type === MESSAGE.REFRESH) {
				this.loadView(true);
			} else if (message.type === MESSAGE.NAVIGATE) {
				this.navigateTo(message.list);
			} else if (message.type === MESSAGE.RESIZE) {
				this.resize(message.height);
			}
		},

		createFrame : function RegulatoryView_createFrame() {
			var iframe = document.createElement("iframe");
			iframe.className = "regulatory-view-iframe";
			iframe.setAttribute("referrerpolicy", "no-referrer");
			iframe.setAttribute("sandbox", "allow-scripts allow-same-origin allow-downloads");
			iframe.src = this.options.uiUrl + EMBED_PATH + "?locale=" + encodeURIComponent(Alfresco.constants.JS_LOCALE) + "&theme=light";
			Dom.get(this.id + "-frame").appendChild(iframe);
			this.iframe = iframe;
			this.readyTimer = YAHOO.lang.later(READY_TIMEOUT_MS, this, this.onReadyTimeout);
		},

		onFrameReady : function RegulatoryView_onFrameReady() {
			if (this.readyTimer !== null) {
				this.readyTimer.cancel();
				this.readyTimer = null;
			}
			Dom.addClass(this.id + "-message", "hidden");
			this.loadView(false);
		},

		onReadyTimeout : function RegulatoryView_onReadyTimeout() {
			this.readyTimer = null;
			var messageEl = Dom.get(this.id + "-message");
			messageEl.innerHTML = $html(this.msg("message.unavailable")) + ' <button type="button" id="' + this.id + '-retry">'
					+ $html(this.msg("button.retry")) + "</button>";
			Dom.removeClass(messageEl, "hidden");
			Event.addListener(this.id + "-retry", "click", this.onRetry, this, true);
		},

		onRetry : function RegulatoryView_onRetry() {
			if (this.iframe !== null) {
				this.iframe.parentNode.removeChild(this.iframe);
				this.iframe = null;
			}
			Dom.get(this.id + "-message").innerHTML = $html(this.msg("message.loading"));
			this.createFrame();
		},

		/**
		 * Asks the repository for the compliance of the product and hands it to the frame.
		 * 
		 * @method loadView
		 * @param refresh
		 *            {Boolean} true to bypass the result cache of the regulatory service
		 */
		loadView : function RegulatoryView_loadView(refresh) {
			this.post({
				type : MESSAGE.LOADING
			});
			Alfresco.util.Ajax.jsonGet({
				url : Alfresco.constants.PROXY_URI + "becpg/regulatory/view?nodeRef=" + encodeURIComponent(this.options.entityNodeRef)
						+ "&refresh=" + (refresh === true),
				successCallback : {
					fn : function(response) {
						this.post({
							type : MESSAGE.DATA,
							payload : response.json
						});
					},
					scope : this
				},
				failureCallback : {
					fn : function(response) {
						this.post({
							type : MESSAGE.ERROR,
							message : this.errorMessage(response)
						});
					},
					scope : this
				}
			});
		},

		errorMessage : function RegulatoryView_errorMessage(response) {
			var code = response && response.json ? response.json.error : null;
			return this.msg(ERROR_MESSAGES.hasOwnProperty(code) ? ERROR_MESSAGES[code] : "message.error");
		},

		post : function RegulatoryView_post(message) {
			if (this.iframe !== null && this.iframe.contentWindow) {
				this.iframe.contentWindow.postMessage(message, this.regulatoryOrigin);
			}
		},

		/**
		 * Opens another list of the current product. Only the lists the view can point at are
		 * accepted, whatever the frame asks.
		 * 
		 * @method navigateTo
		 * @param list
		 *            {String} the list to open
		 */
		navigateTo : function RegulatoryView_navigateTo(list) {
			if (NAVIGABLE_LISTS.indexOf(list) < 0) {
				return;
			}
			var href = window.location.href.split("#")[0];
			if (/[?&]list=/.test(href)) {
				window.location.href = href.replace(/([?&])list=[^&]*/, "$1list=" + list);
			} else {
				window.location.href = href + (href.indexOf("?") < 0 ? "?" : "&") + "list=" + list;
			}
		},

		resize : function RegulatoryView_resize(height) {
			if (typeof height !== "number" || !isFinite(height) || height <= 0) {
				return;
			}
			var target = Math.max(MIN_HEIGHT, Math.ceil(height));
			if (Math.abs(this.iframe.offsetHeight - target) > RESIZE_TOLERANCE) {
				this.iframe.style.height = target + "px";
			}
		},

		originOf : function RegulatoryView_originOf(url) {
			var link = document.createElement("a");
			link.href = url;
			return link.protocol + "//" + link.host;
		}
	});

})();
