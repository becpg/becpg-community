(function() {
	/**
	 * YUI Library aliases
	 */
	var Dom = YAHOO.util.Dom;

	var $html = Alfresco.util.encodeHTML;

	/**
	 * beCPG.component.Watson constructor.
	 * 
	 * @param {String}
	 *            htmlId The HTML id of the parent element
	 * @return {beCPG.component.Watson} The new Watson instance
	 * @constructor
	 */
	beCPG.component.Watson = function Watson_constructor(htmlId) {
		beCPG.component.Watson.superclass.constructor.call(this, "beCPG.component.Watson", htmlId);

		return this;
	};

	YAHOO
		.extend(
			beCPG.component.Watson,
			Alfresco.component.Base,
			{

				/**
				 * Fired by YUI when parent element is available for
				 * scripting.
				 * 
				 * @method onReady
				 */
				onReady: function Watson_onReady() {
					
						var chatButton = YAHOO.util.Dom.get("watson-chatbot-chat-activate-bar");
						var chatContainer = YAHOO.util.Dom.get("watson-container");
						
					    YAHOO.util.Event.addListener(chatButton, 'click', function() {
					        YAHOO.util.Dom.toggleClass(chatButton, 'transition');
					        YAHOO.util.Dom.toggleClass(chatContainer, 'transition');
					        YAHOO.util.Dom.toggleClass(chatContainer, 'round');
					    });

				}
			});

})();