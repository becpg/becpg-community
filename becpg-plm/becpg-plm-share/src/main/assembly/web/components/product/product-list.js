/**
 * @namespace beCPG
 * @class beCPG.component.ProductList
 */
 
(function() {
    /**
     * YUI Library aliases
     */
    var Dom = YAHOO.util.Dom;

    /**
     * Alfresco Slingshot aliases
     */
    var $html = Alfresco.util.encodeHTML;

    /**
     * ProductList constructor.
     * 
     * @param htmlId {String} The HTML id of the parent element
     * @return {beCPG.component.ProductList} The new ProductList instance
     * @constructor
     */
    beCPG.component.ProductList = function(htmlId) {
        beCPG.component.ProductList.superclass.constructor.call(this, htmlId);
        return this;
    };

    /**
     * Extend from beCPG.module.EntityDataGrid
     */
    YAHOO.extend(beCPG.component.ProductList, beCPG.module.EntityDataGrid);

    /**
     * Augment prototype with main class implementation, ensuring overwrite is enabled
     */
    YAHOO.lang.augmentObject(beCPG.component.ProductList.prototype, {
        /**
         * Data Item created event handler
         * 
         * @method onDataItemCreated
         * @param layer {object} Event fired
         * @param args {array} Event parameters (depends on event type)
         */
        onDataItemCreated: function(layer, args) {
            var obj = args[1];
            if (obj && obj.nodeRef !== null) {
                this.queryExecutionId = null;
                window.location = beCPG.util.entityURL(this.options.siteId, obj.nodeRef, "pjt:project", null, "View-properties");
            }
        }
    }, true);
})();
