/**
 * @namespace beCPG
 * @class beCPG.component.ProductList
 */

(function() {

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


})();
