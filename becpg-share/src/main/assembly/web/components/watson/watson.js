(function() {
	// Shortcut for Dom
	var Dom = YAHOO.util.Dom;

	// Constructor function for beCPG.component.Watson
	beCPG.component.Watson = function(htmlId) {
		// Call superclass constructor
		beCPG.component.Watson.superclass.constructor.call(this, "beCPG.component.Watson", htmlId);
		return this;
	};

	// Extend beCPG.component.Watson from Alfresco.component.Base
	YAHOO.extend(beCPG.component.Watson, Alfresco.component.Base, {

		options: {
			ticket: null
		},

		isIFrameLoaded: false,

		// Function to toggle a CSS class
		toggleClass: function(element, className) {
			if (Dom.hasClass(element, className)) {
				Dom.removeClass(element, className);
			} else {
				Dom.addClass(element, className);
			}
		},

		// Fired by YUI when parent element is available for scripting
		onReady: function() {
	
			var me = this;
			// Bind click event to handle chat button click
			YAHOO.util.Event.addListener(Dom.get("watson-chatbot-chat-activate-bar"), 'click',
				function() {
					var chatButton = Dom.get("watson-chatbot-chat-activate-bar");
					var chatContainer = Dom.get("watson-container");
					var chatFrame = Dom.get("watson-chatbot-chat-frame");

					// Toggle visibility classes
					me.toggleClass(chatButton, 'transition');
					me.toggleClass(chatContainer, 'transition');
					me.toggleClass(chatContainer, 'round');

					if (!me.isIFrameLoaded) {
						var currentUrl = window.location.href;

						// Load iframe with chat URL including current URL
						chatFrame.innerHTML = '<iframe src="' + Alfresco.constants.URL_CONTEXT + 'proxy/ai/watson/chat?ticket=' + me.options.ticket + '&referer=' + encodeURIComponent(currentUrl) + '" referrerpolicy="origin" sandbox="allow-scripts allow-forms allow-same-origin allow-popups allow-popups-to-escape-sandbox"></iframe>';
						me.isIFrameLoaded = true;
					}
				}

				, this);
		}
	});

})();
