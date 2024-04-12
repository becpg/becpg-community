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
						// Créer une fonction pour ajouter ou supprimer la classe
					    function toggleClass(element, className) {
					        if (YAHOO.util.Dom.hasClass(element, className)) {
					            YAHOO.util.Dom.removeClass(element, className);
					        } else {
					            YAHOO.util.Dom.addClass(element, className);
					        }
					    }
					
					    // Ajouter un écouteur d'événement de clic aux boutons d'activation du chat
					    YAHOO.util.Event.addListener(chatButton, 'click', function() {
					        // Basculer la classe 'transition' pour changer la visibilité
					        toggleClass(chatButton, 'transition');
					        toggleClass(chatContainer, 'transition');
					        toggleClass(chatContainer, 'round');
					    });

				}
			});

})();