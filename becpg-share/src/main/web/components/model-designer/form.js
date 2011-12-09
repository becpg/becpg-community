

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
    	this.dropZone = new YAHOO.util.DDTarget(this.id+"-dropZone");
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

	        var templateUrl,data;
	        
	   	   //First destroy old
	       YAHOO.Bubbling.fire("formContainerDestroyed");
	       YAHOO.Bubbling.fire("dataGridContainerDestroyed");
	             
	    	   
	        
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
             
              
              if(this.currentNode.droppable){
            	  var nodes = YAHOO.util.Selector.query('div.instruction');
            	  for(var i in nodes){
            		  nodes[i].style.display = "none";
            	  }
         		  Dom.get(this.id+"-dropZone").style.display = "block";
         		  Dom.get(this.id+"-dnd-instructions-"+this.currentNode.dropInstruction).style.display = "block";
         		  
         	  } else {
         		  Dom.get(this.id+"-dropZone").style.display = "none";
         	  }
              
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
        * Handles applying the styling and node creation required when a document is dragged
        * over a tree node.
        * 
        * @method onDocumentDragOver
        * @property layer The name of the event
        * @property args The event payload
        */
       onDocumentDragOver: function DesignerForm_onDocumentDragOver(layer, args)
       {
          if (args && args[1] && args[1].elementId)
          {
             var rootEl = this.widgets.treeview.getEl();
             if (args[1].event.clientX > rootEl.clientWidth)
             {
                // If the current x co-ordinate of the mouse pointer is greater than the width
                // of the tree element then we shouldn't add a highlight. This is to address
                // the issue where the overflow of wide tree nodes is hidden behind the 
                // document list. Without this test it is possible to show a highlight on 
                // a tree node when it appears as though the mouse is not over it.
             }
             else
             {
                // The current x co-ordinate of the mouse pointer is within the tree element so
                // the node can be highlighted...
                var dropTargetEl = Dom.get(args[1].elementId); 
                if (dropTargetEl != this.widgets.treeview.getEl())
                {
                   var node = this.widgets.treeview.getNodeByElement(dropTargetEl);
                   if (node != null)
                   {
                      var currEl = dropTargetEl;
                      while (currEl.tagName != "TABLE")
                      {
                         currEl = currEl.parentNode;
                      }
                      Dom.addClass(currEl, "documentDragOverHighlight");
                      
                      var folderCell = dropTargetEl.parentNode.children[dropTargetEl.parentNode.children.length - 2];
                      while (folderCell.children.length == 1)
                      {
                         var arrowSpan = document.createElement("span");
                         Dom.addClass(arrowSpan, "documentDragOverArrow");
                         folderCell.appendChild(arrowSpan);
                      }
                   }
                }
             }
          }
       },
       
       /**
        * Handles applying the styling and node deletion required when a document is dragged
        * out of a tree node.
        *
        * @method onDocumentDragOut
        * @property layer The name of the event
        * @property args The event payload
        */
       onDocumentDragOut: function DesignerForm_onDocumentDragOut(layer, args)
       {
          if (args && args[1] && args[1].elementId)
          {
             var dropTargetEl = Dom.get(args[1].elementId); 
             if (dropTargetEl == this.widgets.treeview.getEl())
             {
                // If the document has been dragged out of the tree element then we need 
                // to remove any highlight and arrow from previously highlighted tree nodes.
                // This would be the case if the highlighted tree node is wider than the 
                // tree element and the mouse has moved to the right of the splitter so is
                // outside of the tree but still over the tree node...
                var highlights = Dom.getElementsByClassName("documentDragOverHighlight", "table", dropTargetEl); // Should be only one
                for (var i = 0, j = highlights.length; i < j ; i++)
                {
                   Dom.removeClass(highlights[i], "documentDragOverHighlight");
                }
                var arrows = Dom.getElementsByClassName("documentDragOverArrow", "span", dropTargetEl);
                for (var i = 0, j = arrows.length; i < j ; i++)
                {
                   arrows[i].parentNode.removeChild(arrows[i]);
                }
             }
             else
             {
                // If the document has been dragged out of a tree node then we need to 
                // remove the highlight and arrow previously added when the document was
                // dragged over it...
                var node = this.widgets.treeview.getNodeByElement(dropTargetEl);
                if (node != null)
                {
                   var currEl = dropTargetEl;
                   while (currEl.tagName != "TABLE")
                   {
                      currEl = currEl.parentNode;
                   }
                   Dom.removeClass(currEl, "documentDragOverHighlight");
                   var folderCell = dropTargetEl.parentNode.children[dropTargetEl.parentNode.children.length - 2];
                   while (folderCell.children.length > 1)
                   {
                      folderCell.removeChild(Dom.getLastChild(folderCell));
                   }
                }
             }
          }
       },
       /**
        * Creates the drag and drop targets within the tree. The targets get removed
        * each time that the tree is refreshed in anyway, so it is imperative that they
        * get reset when required.
        * 
        * @method _applyDropTargets
        */
       _applyDropTargets: function DesignerForm__applyDropTargets()
       {
          if (this.options.setDropTargets)
          {
             var rootEl = this.widgets.treeview.getEl();
             
             // Set the root element of the tree as a drop target. This is necessary in order
             // to handle the specific problem of the hidden overflow of tree nodes being at
             // the same location of the screen as the main DocumentList drop targets. Drop events
             // will be ignored for this element, but dragOut events will be used to ensure that
             // all tree highlights are cleared.
             new YAHOO.util.DDTarget(rootEl);
             Dom.addClass(rootEl, "documentDroppableHighlights");
             
             var dndTargets = Dom.getElementsByClassName("ygtvcell", "td", rootEl);
             for (var i = 0, j = dndTargets.length; i < j; i++)
             {
                new YAHOO.util.DDTarget(dndTargets[i]);
                Dom.addClass(dndTargets[i], "documentDroppable");
                Dom.addClass(dndTargets[i], "documentDroppableHighlights");
             }
          }
       }
      
   });
})();