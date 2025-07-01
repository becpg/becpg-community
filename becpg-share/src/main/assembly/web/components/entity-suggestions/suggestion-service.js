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
				var iframeUrl = Alfresco.constants.URL_CONTEXT + 'proxy/ai/suggestions?ticket=' + 
				    beCPG.constants.AI_AUTH_TOKEN + '&nodeRef=' + record.nodeRef + 
					'&locale=' + Alfresco.constants.JS_LOCALE;
					
				// Create overlay if it doesn't exist
				if (!me.widgets.overlay) {
					var overlayEl = document.createElement("div");
					Dom.addClass(overlayEl, "yuimenu");
					Dom.addClass(overlayEl, "suggestion-overlay");
					
					var html = '' +
						'<div class="bd suggestion-container">' +
						'<iframe src="' + iframeUrl + '" style="width:450px; height:600px;" referrerpolicy="origin" ' + 
						'sandbox="allow-scripts allow-forms allow-same-origin allow-popups allow-popups-to-escape-sandbox"></iframe>' +
						'</div>';
						
					overlayEl.innerHTML = html;
					
					// Store the current nodeRef for future comparison
					me.currentNodeRef = record.nodeRef;
					
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
					
					// Set flag when iframe is loaded
					me.isIFrameLoaded = true;
				}
				else {
					// Check if nodeRef has changed and update iframe src if needed
					if (me.currentNodeRef !== record.nodeRef) {
						var iframe = me.widgets.overlay.element.getElementsByTagName("iframe")[0];
						if (iframe) {
							iframe.src = iframeUrl;
							me.currentNodeRef = record.nodeRef;
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
