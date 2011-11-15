

/**
 * Data Lists: DesignerForm component.
 * 
 * Displays a list of DesignerForm
 * 
 * @namespace beCPG
 * @class beCPG.component.DesignerForm
 */
(function()
{
   /**
    * YUI Library aliases
    */
   var Dom = YAHOO.util.Dom,
      Event = YAHOO.util.Event,
      Selector = YAHOO.util.Selector,
      Bubbling = YAHOO.Bubbling;

   /**
    * Alfresco Slingshot aliases
    */
   var $html = Alfresco.util.encodeHTML,
      $combine = Alfresco.util.combinePaths;

   /**
    * DesignerForm constructor.
    * 
    * @param htmlId {String} The HTML id of the parent element
    * @return {beCPG.component.DesignerForm} The new DesignerForm instance
    * @constructor
    */
   beCPG.component.DesignerForm = function(htmlId)
   {
      beCPG.component.DesignerForm.superclass.constructor.call(this, "beCPG.component.DesignerForm", htmlId, ["button", "container"]);
      
      YAHOO.Bubbling.on("designerModelNodeChange", this.onDesignerModelNodeChange, this);
      return this;
   };
   
   /**
    * Extend from Alfresco.component.Base
    */
   YAHOO.extend(beCPG.component.DesignerForm, Alfresco.component.Base,
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
           * Current modelNodeRef.
           * 
           * @property modelNodeRef
           * @type string
           * @default ""
           */
    	  modelNodeRef: null,
    	  
    	  /**
           * nodeRef to show.
           * 
           * @property nodeRef
           * @type string
           * @default ""
           */
    	  nodeRef : null

      },

      /**
     
      /**
       * Fired by YUI when parent element is available for scripting.
       *
       * @method onReady
       */
      onReady: function DesignerForm_onReady()
      {
    	
    	this.show();
    	
      },
      
      /**
       * Main entrypoint to show the dialog
       *
       * @method show
       */
      show: function DesignerForm_show()
      {
        if(this.options.nodeRef!=null){ 

	        var templateUrl = YAHOO.lang.substitute(Alfresco.constants.URL_SERVICECONTEXT + "components/form?itemKind={itemKind}&itemId={itemId}&mode={mode}&submitType={submitType}&showCancelButton=false",
	         {
	            itemKind: "node",
	            itemId: this.options.nodeRef,
	            mode: "edit",
	            submitType: "json"
	         });
	        var data =
            {
               htmlid: this.id
            };
            Alfresco.util.Ajax.request(
            {
               url: templateUrl,
               dataObj:data,
               successCallback:
               {
                  fn: this.onTemplateLoaded,
                  scope: this
               },
               failureMessage: "Could not load dialog template from '" + this.options.templateUrl + "'.",
               scope: this,
               execScripts: true
            });
        }
            
         return this;
      },
    
       
      /**
       * Fired by YUI TreeView when a node label is clicked
       * @method onNodeClicked
       * @param args.event {HTML Event} the event object
       * @param args.node {YAHOO.widget.Node} the node clicked
       * @return allowExpand {boolean} allow or disallow node expansion
       */
      onDesignerModelNodeChange: function DesignerForm_onNodeClicked(layer, args)
      {
    	  var obj = args[1];
    	  
         var nodeRef = obj.nodeRef;
         	if(nodeRef!=null){
              this.options.nodeRef = nodeRef;
              this.show();
         	}
       },
       /**
        * Event callback when dialog template has been loaded
        *
        * @method onTemplateLoaded
        * @param response {object} Server response from load template XHR request
        */
       onTemplateLoaded: function DesignerForm_onTemplateLoaded(response)
       {
          // Inject the template from the XHR request into a new DIV element
          var containerDiv = Dom.get(this.id+"-model-form");
          containerDiv.innerHTML = response.serverResponse.responseText;

          // The panel is created from the HTML returned in the XHR request, not the container
//          var dialogDiv = Dom.getFirstChild(containerDiv);
//          while (dialogDiv && dialogDiv.tagName.toLowerCase() != "div")
//          {
//             dialogDiv = Dom.getNextSibling(dialogDiv);
//          }

//         
//          // Are we controlling a Forms Service-supplied form?
//          if (Dom.get(this.id + "-form-submit"))
//          {
//             // FormUI component will initialise form, so we'll continue processing later
//             this.formsServiceDeferred.fulfil("onTemplateLoaded");
//          }
//          
       }
      
     
   });
})();