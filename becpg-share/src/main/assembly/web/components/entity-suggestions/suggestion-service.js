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
				
				return !(aiValidationDate !== undefined && aiValidationDate !== null && aiValidationDate !== "");
			},
			
			/**
			 * Show the AI suggestions panel for the given node reference
			 * 
			 * @method showSuggestions
			 * @param record {object} Record object representing a node
			 * @return {boolean} True if suggestions were shown, false otherwise
			 */
			showSuggestions: function(record) {

				// Create container for suggestions component
				var suggestionDivId = "aisuggestions-" + Alfresco.util.generateDomId();
				var containerDiv = document.createElement("div");
				containerDiv.id = suggestionDivId;
				document.body.appendChild(containerDiv);
				
				// Instantiate the EntitySuggestions component
				var suggestions = new beCPG.component.EntitySuggestions(suggestionDivId);
				suggestions.setOptions({
					entityNodeRef: record.nodeRef,
					ticket: Alfresco.util.CSRFPolicy.getToken(),
					locale: Alfresco.constants.JS_LOCALE
				});
				
				// Fire event to show the suggestions panel
				YAHOO.Bubbling.fire("showSuggestionsPanel", {
					nodeRef: record.nodeRef
				});
				
				return true;
			}
		});
})();
