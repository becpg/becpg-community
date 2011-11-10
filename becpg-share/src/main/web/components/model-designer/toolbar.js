
/**
 * Designer : Toolbar component.
 * 
 * Displays a list of Toolbar
 * 
 * @namespace beCPG
 * @class beCPG.component.DesignerToolbar
 */
(function()
{
   /**
    * YUI Library aliases
    */
   var Dom = YAHOO.util.Dom,
      Event = YAHOO.util.Event;

   /**
    * Toolbar constructor.
    * 
    * @param htmlId {String} The HTML id of the parent element
    * @return {beCPG.component.DesignerToolbar} The new Toolbar instance
    * @constructor
    */
   beCPG.component.DesignerToolbar = function(htmlId)
   {
	   beCPG.component.DesignerToolbar.superclass.constructor.call(this, "beCPG.component.DesignerToolbar", htmlId, ["button", "container"]);
      
      return this;
   };
   
   /**
    * Extend from Alfresco.component.Base
    */
   YAHOO.extend(beCPG.component.DesignerToolbar, Alfresco.component.Base)
   
   
   /**
    * Augment prototype with main class implementation, ensuring overwrite is enabled
    */
   YAHOO.lang.augmentObject(beCPG.component.DesignerToolbar.prototype,
   {
      /**
       * Object container for initialization options
       *
       * @property options
       * @type object
       */
      options:
      {
         /**
           * Current selected itemType.
           * 
           * @property itemType
           * @type string
           * @default ""
           */
          itemType: ""
      },

      /**
       * Fired by YUI when parent element is available for scripting.
       *
       * @method onReady
       */
      onReady: function DesignerToolbar_onReady()
      {
         this.widgets.newRowButton = Alfresco.util.createYUIButton(this, "newRowButton", this.onNewRow,
         {
            disabled: true,
            value: "create"
         });

         // Finally show the component body here to prevent UI artifacts on YUI button decoration
         Dom.setStyle(this.id + "-body", "visibility", "visible");
      },
      
      /**
       * New Row button click handler
       *
       * @method onNewRow
       * @param e {object} DomEvent
       * @param p_obj {object} Object passed back from addListener method
       */
      onNewRow: function DesignerToolbar_onNewRow(e, p_obj)
      {
        
      }
   }, true);
})();