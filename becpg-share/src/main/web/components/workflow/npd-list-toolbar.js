/**
 * NpdListToolbar component.
 *
 * @namespace Alfresco
 * @class beCPG.component.NpdListToolbar
 */
(function()
{
   /**
    * YUI Library aliases
    */
   var Dom = YAHOO.util.Dom,
      Selector = YAHOO.util.Selector;

   /**
    * WorkflowListToolbar constructor.
    *
    * @param {String} htmlId The HTML id of the parent element
    * @return {beCPG.component.NpdListToolbar} The new WorkflowListToolbar instance
    * @constructor
    */
   beCPG.component.NpdListToolbar = function TDH_constructor(htmlId)
   {
      beCPG.component.NpdListToolbar.superclass.constructor.call(this, "beCPG.component.NpdListToolbar", htmlId, ["button"]);
      return this;
   };

   YAHOO.extend(beCPG.component.NpdListToolbar, Alfresco.component.Base,
   {
      /**
       * Fired by YUI when parent element is available for scripting.
       * Component initialisation, including instantiation of YUI widgets and event listener binding.
       *
       * @method onReady
       */
      onReady: function WLT_onReady()
      {
         this.widgets.startWorkflowButton = Alfresco.util.createYUIButton(this, "startNpd-button", this.onStartWorkflowButtonClick, {});
         Dom.removeClass(Selector.query(".hidden", this.id + "-body", true), "hidden");
      },

      /**
       * Start workflow button click handler
       *
       * @method onNewFolder
       * @param e {object} DomEvent
       * @param p_obj {object} Object passed back from addListener method
       */
      onStartWorkflowButtonClick: function WLT_onNewFolder(e, p_obj)
      {
         document.location.href = Alfresco.util.siteURL("start-workflow?referrer=workflows&myWorkflowsLinkBack=true");
      }

   });

})();
