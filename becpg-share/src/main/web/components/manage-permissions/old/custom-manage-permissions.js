// Declare namespace...
(function()
{
	
	  /**
    * YUI Library aliases
    */
   var Dom = YAHOO.util.Dom;

   /**
    * Alfresco Slingshot aliases
    */
   var $html = Alfresco.util.encodeHTML,
   	$siteURL = Alfresco.util.siteURL
	
  // Define constructor...
  beCPG.component.ManagePermissions = function CustomManagePermissions_constructor(htmlId)
  {
    beCPG.component.ManagePermissions.superclass.constructor.call(this, htmlId);
    return this;
  };

  // Extend default ManagePermissions...
  YAHOO.extend(beCPG.component.ManagePermissions, Alfresco.component.ManagePermissions,
  {
	  /**
      * Object container for initialization options
      *
      * @property options
      * @type {object} object literal
      */
     options:
     {
        /**
         * Reference to the current document
         *
         * @property nodeRef
         * @type string
         */
        nodeRef: null,

        /**
         * Tells if the user may compare the version and the document.
         *
         * @property isEntity
         * @type string
         */
        isEntity: false
     },     
     
     /**
      * Displays the corresponding details page for the current node
      *
      * @method _navigateForward
      * @private
      */
     _navigateForward: function Permissions__navigateForward()
     {
        /* Did we come from the document library? If so, then direct the user back there */
        if (document.referrer.match(/documentlibrary([?]|$)/) || document.referrer.match(/repository([?]|$)/))
        {
           // go back to the referrer page
           history.go(-1);
        }
        else
        {
           // go forward to the appropriate details page for the node
           window.location.href = $siteURL("entity-details?nodeRef=" + this.nodeData.nodeRef);
        }
     }
    
  });
})();