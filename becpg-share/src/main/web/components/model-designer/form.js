

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
      YAHOO.Bubbling.on("beforeFormRuntimeInit", this.onBeforeFormRuntimeInit, this);
      YAHOO.Bubbling.on("elementDragOver", this.onElementDragOver, this);
      YAHOO.Bubbling.on("elementDragOut", this.onElementDragOut, this);
      YAHOO.Bubbling.on("dropTargetOwnerRequest", this.onDropTargetOwnerRequest, this);
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
       * Current tree node
       */
      currentNode : null,
      
      
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

	        var templateUrl,
	        	data,
	        	template,
	        	templateInstance,
	        	container = Dom.get(this.id+"-dnd-instructions");
	        
	   	   //First destroy old
	       YAHOO.Bubbling.fire("formContainerDestroyed");
	       YAHOO.Bubbling.fire("dataGridContainerDestroyed");
	             
	       //Clean instructions
	       if(container.hasChildNodes() )
    	   {
    		    while ( container.childNodes.length >= 1 )
    		    {
    		    	container.removeChild( container.firstChild );       
    		    } 
    		}
	       
	       
	       
	       
	       //show instructions
	       if(this.currentNode.accepts.length>0){
	    	   
	    	 	 var dropGroup = null;
	        	 switch(this.currentNode.itemType){
		     	 	case  "m2:type":
		     	 	case  "dsg:fields":
		     	 	case  "m2:aspect":
		     	 	case  "m2:properties":
		     	 	case  "m2:propertyOverrides":
		     	 		dropGroup = "type";
			     	 	break;
		     	 	case  "dsg:form":
		     	 	case  "dsg:formSet":
		     	 	case  "dsg:sets":
		     	 		dropGroup = "form";
			     	 	break;
		     	 	case  "dsg:formField":
		     	 		dropGroup = "field";
			     	 	break;
		     	 }
	           template = Dom.get(this.id+"-dnd-instructions-"+dropGroup);
	           templateInstance = template.cloneNode(true);
	           templateInstance.id = this.id+"-dropZone";
	           Dom.removeClass(templateInstance, "hidden");
	           Dom.addClass(templateInstance, "elementDroppable");
	           Dom.addClass(templateInstance, "elementDroppableHighlights");
	           container.appendChild(templateInstance);
	          var dndTarget =  new YAHOO.util.DDTarget(templateInstance);
	           for(i in this.currentNode.accepts){
	        	   dndTarget.addToGroup(this.currentNode.accepts[i]);
	           }
	           
	       } 
	      
           
	        //show datas
	        if(this.currentNode.subType!=null){
		        templateUrl = YAHOO.lang.substitute(Alfresco.constants.URL_SERVICECONTEXT + "modules/entity-data-lists/entity-datagrid?nodeRef={modelNodeRef}",
		   	         {
		        	modelNodeRef : this.options.modelNodeRef
		   	            	
		   	         });
	        } else {
	        	templateUrl = YAHOO.lang.substitute(Alfresco.constants.URL_SERVICECONTEXT + "components/form?entityNodeRef={entityNodeRef}&itemKind={itemKind}&itemId={itemId}&mode={mode}&submitType={submitType}&showCancelButton=false",
	       	         {
	       	            itemKind: "node",
	       	            itemId: this.options.nodeRef,
	       	            mode: "edit",
	       	            submitType: "json",
	       	            entityNodeRef : this.options.modelNodeRef
	       	         });
	        }
	        
	         data =
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
              this.options.nodeRef = obj.node.nodeRef;
              
              this.currentNode = obj.node
             
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
          
          if(containerDiv.hasChildNodes() )
	   	   {
	   		    while ( containerDiv.childNodes.length >= 1 )
	   		    {
	   		    	containerDiv.removeChild( containerDiv.firstChild );       
	   		    } 
	   		}
          
          containerDiv.innerHTML = response.serverResponse.responseText;

          if(this.currentNode.subType!=null){
        	  Bubbling.fire("activeDataListChanged",{
                     dataList: {
	                  	   "title": this.currentNode.entityTitle,
	                  	   "description":  this.currentNode.description,
	                  	   "nodeRef": this.currentNode.nodeRef,
	                  	   "entityName" :this.currentNode.name,
	                  	   "itemType" :this.currentNode.subType
                  	 },
                     scrollTo: true
                  });
          }
          
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
       
       /**
        * Handles applying the styling and node creation required when a element is dragged
        * over a tree node.
        * 
        * @method onElementDragOver
        * @property layer The name of the event
        * @property args The event payload
        */
       onElementDragOver: function DesignerForm_onElementDragOver(layer, args)
       {
          if (args && args[1] && args[1].elementId)
          {
                // the node can be highlighted...
                var dropTargetEl = Dom.get(args[1].elementId); 
                Dom.addClass(dropTargetEl, "elementDragOverHighlight");
          }
       },
       
       /**
        * Handles applying the styling and node deletion required when a document is dragged
        * out of a tree node.
        *
        * @method onElementDragOut
        * @property layer The name of the event
        * @property args The event payload
        */
       onElementDragOut: function DesignerForm_onElementDragOut(layer, args)
       {
          if (args && args[1] && args[1].elementId)
          {
        	  // the node can be highlighted...
              var dropTargetEl = Dom.get(args[1].elementId); 
              Dom.removeClass(dropTargetEl, "elementDragOverHighlight");
          }
       },
       /**
        * Handles "dropTargetOwnerRequest" by determining whether or not the target belongs to the TreeView
        * widget, and if it does determines it's nodeRef and uses the callback function with it.
        * 
        * @method onDropTargetOwnerRequest
        * @property layer The name of the event
        * @property args The event payload
        */
       onDropTargetOwnerRequest: function DesignerForm_onDropTargetOwnerRequest(layer, args)
       {
          if (args && args[1] && args[1].elementId)
          {
             var node = this.currentNode;
             if (node != null)
             {
                // Perform the drag out to clear the highlight...
                this.onElementDragOut(layer, args);
                
                var nodeRef = node.nodeRef;
                var type = node.itemType;
                if(node.subType!=null){
                	type = node.subType;
                }
             
                args[1].callback.call(args[1].scope, nodeRef, type);
             }
          }
       }
      
   });
})();