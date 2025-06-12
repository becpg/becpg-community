(function() {
    // Shortcuts for YUI components
    var Dom = YAHOO.util.Dom,
        Event = YAHOO.util.Event,
        DD = YAHOO.util.DD;

    // Constructor function for beCPG.component.Watson
    beCPG.component.Watson = function(htmlId) {
        // Call superclass constructor
        beCPG.component.Watson.superclass.constructor.call(this, "beCPG.component.Watson", htmlId);
        return this;
    };

    // Extend beCPG.component.Watson from Alfresco.component.Base
    YAHOO.extend(beCPG.component.Watson, Alfresco.component.Base, {

        options: {
            ticket: null,
            minWidth: 380,
            minHeight: 600,
            maxWidth: 800,
            maxHeight: 800
        },

        isIFrameLoaded: false,

        // Resize state tracking
        resizeData: {
            isResizing: false,
            startX: 0,
            startY: 0,
            startWidth: 0,
            startHeight: 0
        },

        // Function to toggle a CSS class
        toggleClass: function(element, className) {
            if (Dom.hasClass(element, className)) {
                Dom.removeClass(element, className);
            } else {
                Dom.addClass(element, className);
            }
        },

        /**
         * Initialize resize handle functionality
         */
        initResizeHandle: function() {
            var me = this,
                resizeHandle = Dom.get("watson-resize-handle"),
                chatContainer = Dom.get("watson-chatbot-container");

            if (!resizeHandle) return;

            // Mouse down event on resize handle
            Event.addListener(resizeHandle, 'mousedown', function(e) {
                // Prevent default browser behavior
                Event.preventDefault(e);

                // Get current dimensions
                var containerWidth = chatContainer.offsetWidth,
                    containerHeight = chatContainer.offsetHeight;

                // Store initial state
                me.resizeData.isResizing = true;
                me.resizeData.startX = e.clientX;
                me.resizeData.startY = e.clientY;
                me.resizeData.startWidth = containerWidth;
                me.resizeData.startHeight = containerHeight;

                // Add being-resized class
                Dom.addClass(chatContainer, 'being-resized');

                // Add document-wide mouse move and mouse up listeners
                Event.addListener(document, 'mousemove', me.onResize, me, true);
                Event.addListener(document, 'mouseup', me.endResize, me, true);
            });
        },

        /**
         * Handle resize during mousemove
         */
        onResize: function(e) {
            // Prevent default browser behavior
            Event.preventDefault(e);

            if (!this.resizeData.isResizing) return;

            var chatContainer = Dom.get("watson-chatbot-container"),
                chatFrame = Dom.get("watson-chatbot-chat-frame"),
                // Calculate deltas (how much the mouse has moved)
                deltaX = e.clientX - this.resizeData.startX,
                deltaY = e.clientY - this.resizeData.startY,
                // For top-left resizing, we need to invert the deltas
                // as moving left/up should increase the size
                newWidth = this.resizeData.startWidth - deltaX,
                newHeight = this.resizeData.startHeight - deltaY;

            // Apply min/max constraints
            newWidth = Math.max(this.options.minWidth, Math.min(this.options.maxWidth, newWidth));
            newHeight = Math.max(this.options.minHeight, Math.min(this.options.maxHeight, newHeight));

            // Calculate position adjustments to keep the bottom-right corner fixed
            var deltaWidth = newWidth - this.resizeData.startWidth,
                deltaHeight = newHeight - this.resizeData.startHeight,
                currentLeft = parseInt(chatContainer.style.left || 0, 10),
                currentTop = parseInt(chatContainer.style.top || 0, 10),
                newLeft = currentLeft - deltaX,
                newTop = currentTop - deltaY;

            // Apply new dimensions
            chatContainer.style.width = newWidth + 'px';
            chatContainer.style.height = newHeight + 'px';

            // Ensure chat frame adjusts properly
            if (chatFrame) {
                chatFrame.style.height = (newHeight - 130) + 'px'; // Adjust for header/footer
            }
        },

        /**
         * End resize operation
         */
        endResize: function(e) {
            var chatContainer = Dom.get("watson-chatbot-container");

            // Reset resize state
            this.resizeData.isResizing = false;

            // Remove being-resized class
            Dom.removeClass(chatContainer, 'being-resized');

            // Remove document-wide listeners
            Event.removeListener(document, 'mousemove', this.onResize);
            Event.removeListener(document, 'mouseup', this.endResize);
        },

        // Fired by YUI when parent element is available for scripting
        onReady: function() {

            var me = this;

            // Initialize resize handle
            this.initResizeHandle();

            // Function to reset chat container size to default
            this.resetChatSize = function() {
                var chatContainer = Dom.get("watson-chatbot-container");
                var chatFrame = Dom.get("watson-chatbot-chat-frame");

                // Reset explicit dimensions
                chatContainer.style.width = '';
                chatContainer.style.height = '';
                if (chatFrame) {
                    chatFrame.style.height = '';
                }
            };

            // Bind click event to handle chat button click
            Event.addListener(Dom.get("watson-chatbot-chat-activate-bar"), 'click',
                function() {
                    var chatButton = Dom.get("watson-chatbot-chat-activate-bar");
                    var chatContainer = Dom.get("watson-container");
                    var chatFrame = Dom.get("watson-chatbot-chat-frame");
                    var isCurrentlyOpen = Dom.hasClass(chatContainer, 'transition');

                    // Toggle visibility classes
                    me.toggleClass(chatButton, 'transition');
                    me.toggleClass(chatContainer, 'transition');
                    me.toggleClass(chatContainer, 'round');

                    // If we're closing the chat, reset its size
                    if (isCurrentlyOpen) {
                        me.resetChatSize();
                    }

                    if (!me.isIFrameLoaded) {
                        var currentUrl = window.location.href;

                        // Load iframe with chat URL including current URL
                        chatFrame.innerHTML = '<iframe src="' + Alfresco.constants.URL_CONTEXT + 'proxy/ai/watson/chat?ticket=' + me.options.ticket + '&referer=' + encodeURIComponent(currentUrl) + '&locale=' + me.options.locale + '" referrerpolicy="origin" sandbox="allow-scripts allow-forms allow-same-origin allow-popups allow-popups-to-escape-sandbox"></iframe>';
                        me.isIFrameLoaded = true;
                    }
                }

                , this);
        }
    });

})();
