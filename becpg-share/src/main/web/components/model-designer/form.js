

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
      YAHOO.Bubbling.on("selectedModelChanged", this.onSelectedModelChanged, this);
      YAHOO.Bubbling.on("elementCreated", this.onDesignerModelNodeChange, this);
      YAHOO.Bubbling.on("elementDeleted", this.onDesignerModelNodeChange, this);
      YAHOO.Bubbling.on("beforeFormRuntimeInit", this.onBeforeFormRuntimeInit, this);
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

	        var templateUrl = YAHOO.lang.substitute(Alfresco.constants.URL_SERVICECONTEXT + "components/form?entityNodeRef={entityNodeRef}&itemKind={itemKind}&itemId={itemId}&mode={mode}&submitType={submitType}&showCancelButton=false",
	         {
	            itemKind: "node",
	            itemId: this.options.nodeRef,
	            mode: "edit",
	            submitType: "json",
	            entityNodeRef : this.options.modelNodeRef
	            	
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
       * @method onDesignerModelNodeChange
       */
      onDesignerModelNodeChange: function DesignerForm_onNodeClicked(layer, args)
      {
    	  var obj = args[1];
    	 
        
         	if(obj!=null &&  obj.node!=null && obj.node.nodeRef!=null){
              var nodeRef = obj.node.nodeRef;
              this.options.nodeRef = nodeRef;
              this.show();
         	} else {
         		var containerDiv = Dom.get(this.id+"-model-form");
                containerDiv.innerHTML = this.msg("model.please-select");
         	}
       },
       /**
        * @method onSelectedModelChanged
        */
       onSelectedModelChanged: function DesignerForm_onNodeClicked(layer, args)
       {
     	  var obj = args[1];
     	  
          var nodeRef = obj.nodeRef;
          	if(nodeRef!=null){
               this.options.modelNodeRef = nodeRef;
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

          
       }, 
       /**
        * Event handler called when the "beforeFormRuntimeInit" event is received.
        *
        * @method onBeforeFormRuntimeInit
        * @param layer {String} Event type
        * @param args {Object} Event arguments
        * <pre>
        *    args.[1].component: Alfresco.FormUI component instance,
        *    args.[1].runtime: Alfresco.forms.Form instance
        * </pre>
        */
       onBeforeFormRuntimeInit: function DesignerForm_onBeforeFormRuntimeInit(layer, args)
       {
          var formUI = args[1].component,
             formsRuntime = args[1].runtime;

          this.form = formsRuntime;
          this.form.setAJAXSubmit(true,
          {
             successCallback:
             {
            	 fn: function DesignerForm_onActionChangeType_failure(response)
	             {
	                 Alfresco.util.PopupManager.displayMessage(
	                 {
	                    text: this.msg("message.save-element.success")
	                 });
	              },
                scope: this
             },
             failureCallback:
             {
               fn: function DesignerForm_onActionChangeType_failure(response)
                {
                    Alfresco.util.PopupManager.displayMessage(
                    {
                       text: this.msg("message.save-element.failure")
                    });
                 },
                scope: this
             }
          });
          
       },
      
     
   });
})();