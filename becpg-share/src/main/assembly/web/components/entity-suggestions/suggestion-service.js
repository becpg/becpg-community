/**
 * AI Suggestions Service.
 *
 * @namespace beCPG.service
 * @class beCPG.service.AiSuggestion
 */
(function() {
	
	/**
	 * AiSuggestion constructor.
	 *
	 * @return {beCPG.service.AiSuggestion} The new beCPG.service.AiSuggestion instance
	 * @constructor
	 */
	beCPG.service.AiSuggestion = function AiSuggestion_constructor() {
		beCPG.service.AiSuggestion.superclass.constructor.call(this);
		this.resizeData = {
			isResizing: false,
			startX: 0,
			startY: 0,
			startWidth: 750,
			startHeight: 650,
			currentWidth: 750,
			currentHeight: 650
		};
		return this;
	};

	YAHOO.extend(beCPG.service.AiSuggestion, Alfresco.service.BaseService,
		{
			/**
			 * Check if AI suggestions are available for the given record
			 * 
			 * @method isEnabled
			 * @param record {object} Record object representing a node
			 * @return {boolean} True if AI suggestions are enabled for this node
			 */
			isEnabled: function(record) {
				if (!record || record.jsNode.isContainer) {
					return false;
				}
				
				// Check if the node has the bcpg:aiValidationAspect
				var node = record.node || {};
				var aspects = node.aspects || [];
				if (aspects.indexOf("bcpg:aiValidationAspect") === -1) {
					return false;
				}
				
				// Check if the node has the bcpg:aiValidationDate property and it's not empty
				var properties = record.jsNode.properties || {};
				var aiValidationDate = properties["bcpg:aiValidationDate"];
				
				return aiValidationDate !== undefined && aiValidationDate !== null && aiValidationDate !== "";
			},
			
			/**
			 * Show the AI suggestions panel for the given node reference
			 * 
			 * @method showSuggestions
			 * @param record {object} Record object representing a node
			 * @param event {object} Optional event object (used for positioning the overlay)
			 * @return {boolean} True if suggestions were shown, false otherwise
			 */
			showSuggestions: function(record, event) {
				var me = this;
				var Dom = YAHOO.util.Dom;
				var Event = YAHOO.util.Event;
				
				// Get the target element for positioning
				var targetEl = null;
				if (event && event.target) {
					targetEl = event.target;
					while (targetEl && !Dom.hasClass(targetEl, "ai-suggestion-action")) {
						targetEl = targetEl.parentNode;
					}
				}
				
				if (!targetEl) {
					targetEl = Dom.getElementsByClassName("ai-suggestion-action", "a")[0];
				}
				
				if (!me.widgets) {
					me.widgets = {};
				}
				
				// Build the iframe URL for this node
				var listIdParam = record.listId ? '&listId=' + encodeURIComponent(record.listId) : '';
				var iframeUrl = Alfresco.constants.URL_CONTEXT + 'proxy/ai/suggestions?ticket=' + 
				    beCPG.constants.AI_AUTH_TOKEN + '&nodeRef=' + record.nodeRef + 
					'&locale=' + Alfresco.constants.JS_LOCALE + listIdParam;
					
				// Create overlay if it doesn't exist
				if (!me.widgets.overlay) {
					var overlayEl = document.createElement("div");
					Dom.addClass(overlayEl, "yuimenu");
					Dom.addClass(overlayEl, "suggestion-overlay");
					
					var titleText = Alfresco.util.message("label.tab.suggestions") || "Suggestions";
					var fullscreenTitle = Alfresco.util.message("button.suggestions.fullscreen") || "Plein écran";
					var closeTitle = Alfresco.util.message("button.close") || "Fermer";

					var html = '' +
						'<div class="suggestion-overlay-header">' +
						'  <span class="suggestion-overlay-title">' + titleText + '</span>' +
						'  <div class="suggestion-overlay-actions">' +
						'    <button class="suggestion-overlay-btn-fullscreen" type="button" title="' + fullscreenTitle + '"></button>' +
						'    <button class="suggestion-overlay-btn-close" type="button" title="' + closeTitle + '"></button>' +
						'  </div>' +
						'</div>' +
						'<div class="bd suggestion-container">' +
						'  <iframe src="' + iframeUrl + '" style="width:750px; height:650px;" referrerpolicy="origin" ' + 
						'sandbox="allow-scripts allow-same-origin allow-forms allow-popups allow-popups-to-escape-sandbox"></iframe>' +
						'</div>';
						
					overlayEl.innerHTML = html;
					
					// Store the current nodeRef and listId for future comparison
					me.currentNodeRef = record.nodeRef;
					me.currentListId = record.listId || null;
					
					me.widgets.overlay = Alfresco.util.createYUIOverlay(overlayEl,
					{
						context: [
							targetEl,
							"tl",
							"bl",
							["beforeShow", "windowResize"]
						],
						effect: {
							effect: YAHOO.widget.ContainerEffect.FADE,
							duration: 0.25
						},
						visible: false
					}, {
						type: YAHOO.widget.Menu
					});
					me.widgets.overlay.render(Dom.get("doc3"));
					
					// Setup button actions
					var closeBtn = overlayEl.getElementsByClassName("suggestion-overlay-btn-close")[0];
					if (closeBtn) {
						Event.addListener(closeBtn, "click", function(e) {
							me.widgets.overlay.hide();
							Event.preventDefault(e);
						});
					}

					var fullscreenBtn = overlayEl.getElementsByClassName("suggestion-overlay-btn-fullscreen")[0];
					if (fullscreenBtn) {
						Event.addListener(fullscreenBtn, "click", function(e) {
							var iframe = overlayEl.getElementsByTagName("iframe")[0];
							if (Dom.hasClass(overlayEl, "fullscreen")) {
								Dom.removeClass(overlayEl, "fullscreen");
								if (iframe) {
									iframe.style.width = (me.resizeData.currentWidth || 750) + "px";
									iframe.style.height = (me.resizeData.currentHeight || 650) + "px";
								}
								overlayEl.style.width = "";
								overlayEl.style.height = "";
							} else {
								Dom.addClass(overlayEl, "fullscreen");
								if (iframe) {
									iframe.style.width = "100%";
									iframe.style.height = "100%";
								}
							}
							Event.preventDefault(e);
						});
					}

					// Resize handle functionality
					var resizeHandle = document.createElement("div");
					Dom.addClass(resizeHandle, "suggestion-overlay-resize-handle");
					overlayEl.appendChild(resizeHandle);
					
					Event.addListener(resizeHandle, "mousedown", function(e) {
						Event.preventDefault(e);
						
						if (Dom.hasClass(overlayEl, "fullscreen")) {
							return;
						}
						
						var iframe = overlayEl.getElementsByTagName("iframe")[0];
						if (!iframe) return;
						
						me.resizeData.isResizing = true;
						me.resizeData.startX = e.clientX;
						me.resizeData.startY = e.clientY;
						me.resizeData.startWidth = iframe.offsetWidth;
						me.resizeData.startHeight = iframe.offsetHeight;
						
						iframe.style.pointerEvents = "none";
						Dom.addClass(overlayEl, "being-resized");
						
						var onMouseMove = function(event) {
							if (!me.resizeData.isResizing) return;
							
							var deltaX = event.clientX - me.resizeData.startX;
							var deltaY = event.clientY - me.resizeData.startY;
							
							var newWidth = me.resizeData.startWidth + deltaX;
							var newHeight = me.resizeData.startHeight + deltaY;
							
							newWidth = Math.max(350, newWidth);
							newHeight = Math.max(400, newHeight);
							
							iframe.style.width = newWidth + "px";
							iframe.style.height = newHeight + "px";
							
							me.resizeData.currentWidth = newWidth;
							me.resizeData.currentHeight = newHeight;
						};
						
						var onMouseUp = function(event) {
							me.resizeData.isResizing = false;
							iframe.style.pointerEvents = "auto";
							Dom.removeClass(overlayEl, "being-resized");
							
							Event.removeListener(document, "mousemove", onMouseMove);
							Event.removeListener(document, "mouseup", onMouseUp);
						};
						
						Event.addListener(document, "mousemove", onMouseMove);
						Event.addListener(document, "mouseup", onMouseUp);
					});

					// Set flag when iframe is loaded
					me.isIFrameLoaded = true;
				}
				else {
					// Check if nodeRef or listId has changed and update iframe src if needed
					if (me.currentNodeRef !== record.nodeRef || me.currentListId !== (record.listId || null)) {
						var iframe = me.widgets.overlay.element.getElementsByTagName("iframe")[0];
						if (iframe) {
							iframe.src = iframeUrl;
							me.currentNodeRef = record.nodeRef;
							me.currentListId = record.listId || null;
						}
					}
					
					// Update the context to align with the new target element
					me.widgets.overlay.cfg.setProperty("context", [
						targetEl,
						"tl",
						"bl",
						["beforeShow", "windowResize"]
					]);
				}
				
				// Show overlay
				me.widgets.overlay.show();

				return true;
			}
		});
})();
