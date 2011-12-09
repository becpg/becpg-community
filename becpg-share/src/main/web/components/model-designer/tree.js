

/**
 * Data Lists: DesignerTree component.
 * 
 * Displays a list of DesignerTree
 * 
 * @namespace beCPG
 * @class beCPG.component.DesignerTree
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
    * DesignerTree constructor.
    * 
    * @param htmlId {String} The HTML id of the parent element
    * @return {beCPG.component.DesignerTree} The new DesignerTree instance
    * @constructor
    */
   beCPG.component.DesignerTree = function(htmlId)
   {
	  this.id = htmlId;
      beCPG.component.DesignerTree.superclass.constructor.call(this, "beCPG.component.DesignerTree", htmlId, ["button", "container"]);
    
      YAHOO.Bubbling.on("selectedModelChanged", this.renderDesignerTree, this);
      YAHOO.Bubbling.on("elementCreated", this.onElementCreated, this);
      YAHOO.Bubbling.on("elementDeleted", this.onElementDeleted, this);
      
      
      return this;
   };
   
   /**
    * Extend from Alfresco.component.Base
    */
   YAHOO.extend(beCPG.component.DesignerTree, Alfresco.component.Base,
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
    	  modelNodeRef: null

      },
      /**
       * Selected tree node.
       * 
       * @property selectedNode
       * @type {YAHOO.widget.Node}
       */
      selectedNode: null,

      /**
     
      /**
       * Fired by YUI when parent element is available for scripting.
       *
       * @method onReady
       */
      onReady: function DesignerTree_onReady()
      {
    	  var me = this;
    	  
         this.widgets.newModel = Alfresco.util.createYUIButton(this, "newModelButton", this.onNewModel);
     
         
         this.widgets.modelSelect =  Alfresco.util.createYUIButton(this, "modelSelect-button", this.onModelSelect,
                 {
                            type: "menu",
                            menu: "modelSelect-menu",
                            lazyloadmenu : false
                  });

         var modelSelectedMenu = this.widgets.modelSelect.getMenu()         
        
         modelSelectedMenu.subscribe("click", function (p_sType, p_aArgs) {
                    var menuItem = p_aArgs[1];
                       if (menuItem){
                                  me.widgets.modelSelect.set("label", menuItem.cfg.getProperty("text"));
                              }
                          });
                 
         var me = this,
         headers = YUISelector.query("h2", this.id);
      
	      if (YAHOO.lang.isArray(headers))
	      {
	    	 for(var i in headers){
	         // Create twister from the first H2 tag found by the query
	         Alfresco.util.createTwister(headers[i], this.filterName);
	    	 }
	      }
                 
         //select first
        
         var modelSelected =  modelSelectedMenu.getItem(0);
         
         if(this.options.modelNodeRef!=null && this.options.modelNodeRef.length>0){
        	 for(var i in modelSelectedMenu.getItems()){
        		 modelSelected =  modelSelectedMenu.getItems()[i];
        		 if(modelSelected && modelSelected._oAnchor.children[0].attributes[0].nodeValue == me.options.modelNodeRef){
        			 me.widgets.modelSelect.set("label", modelSelected.cfg.getProperty("text"));
        			 
        		 }
        	 }
             this.renderDesignerTree();
         } else {
        	 if(modelSelected){
                 me.widgets.modelSelect.set("label", modelSelected.cfg.getProperty("text")); 	 
                 this.options.modelNodeRef = modelSelected._oAnchor.children[0].attributes[0].nodeValue;
                 Bubbling.fire("selectedModelChanged",{nodeRef: this.options.modelNodeRef});
            }
         }
         
         this.renderDesignerControls();
         
     
      },
      
      onModelSelect : function DesignerTree_onModelSelect (sType, aArgs, p_obj)
      {
          var domEvent = aArgs[0],
             eventTarget = aArgs[1];
   
          // Select based upon the className of the clicked item
          this.options.modelNodeRef = Alfresco.util.findEventClass(eventTarget)
         
          Bubbling.fire("selectedModelChanged",{nodeRef: this.options.modelNodeRef});
  
      }, 
      
      /**
       * New model button click handler
       *
       * @method onNewList
       * @param e {object} DomEvent
       * @param p_obj {object} Object passed back from addListener method
       */
      onNewModel: function DesignerTree_onNewModel(e, p_obj)
      {
         var  actionUrl = Alfresco.constants.PROXY_URI + "becpg/designer/create/model";

         var doSetupFormsValidation = function DesignerTree_oACT_doSetupFormsValidation(p_form)
         {
            // Validation
            p_form.addValidation(this.id + "-createModel-type", function fnValidateType(field, args, event, form, silent, message)
            {
               return field.options[field.selectedIndex].value !== "-";
            }, null, "change");
            
            p_form.setShowSubmitStateDynamically(true, false);
         };

         // Always create a new instance
         this.modules.createModel = new Alfresco.module.SimpleDialog(this.id + "-createModel").setOptions(
         {
            width: "30em",
            templateUrl: Alfresco.constants.URL_SERVICECONTEXT + "/modules/model-designer/create-model",
            actionUrl: actionUrl,
            doSetupFormsValidation:
            {
               fn: doSetupFormsValidation,
               scope: this
            },
            firstFocus: this.id + "-createModel-name",
            onSuccess:
            {
               fn: function DesignerTree_onActionChangeType_success(response)
               {
                 
                  Alfresco.util.PopupManager.displayMessage(
                  {
                     text: this.msg("message.create-model.success", displayName)
                  });
               },
               scope: this
            },
            onFailure:
            {
               fn: function DesignerTree_onActionChangeType_failure(response)
               {
                  Alfresco.util.PopupManager.displayMessage(
                  {
                     text: this.msg("message.create-model.failure", displayName)
                  });
               },
               scope: this
            }
         });
         this.modules.createModel.show();
      },
      
    
      /**
       * Renders the Data Lists into the DOM
       *
       * @method renderDesignerTree
       */
      renderDesignerTree: function DesignerTree_renderDesignerTree()
      {
         var me = this;
         

       // Prepare the XHR callback object
       var callback =
            {
               success: function DesignerTreelND_success(oResponse)
               {
                  var results = YAHOO.lang.JSON.parse(oResponse.responseText);

                  if (results)
                  {
                      // Build the TreeView widget
                      this._buildTree(results);
                  }
                  
               },

               // If the XHR call is not successful, fire the TreeView callback anyway
               failure: function DesignerTreelND_failure(oResponse)
               {
                  if (oResponse.status == 401)
                  {
                     // Our session has likely timed-out, so refresh to offer the login page
                     window.location.reload();
                  }
                  alert("Unexpected error");
               },
               
               // Callback function scope
               scope: me
            };
       
        var uri = this._buildTreeNodeUrl(this.options.modelNodeRef);

            // Make the XHR call using Connection Manager's asyncRequest method  
         YAHOO.util.Connect.asyncRequest('GET', uri, callback);
         
         
         
      },
      
      /**
       * Renders the controls palette
       *
       * @method renderDesignerControls
       */
      renderDesignerControls: function DesignerTree_renderDesignerControls()
      {
         var me = this;
         

       // Prepare the XHR callback object
       var callback =
            {
               success: function DesignerTreelND_success(oResponse)
               {
                  var results = YAHOO.lang.JSON.parse(oResponse.responseText);

                  if (results)
                  {
                      // Build the controls widget
                      this._buildControls(results);
                  }     
               },
               // If the XHR call is not successful, fire the controls callback anyway
               failure: function DesignerTreelND_failure(oResponse)
               {
                  if (oResponse.status == 401)
                  {
                     // Our session has likely timed-out, so refresh to offer the login page
                     window.location.reload();
                  }
                  alert("Unexpected error");
               },
               
               // Callback function scope
               scope: me
            };
       
         
        var uri = Alfresco.constants.PROXY_URI + "becpg/designer/controls";

            // Make the XHR call using Connection Manager's asyncRequest method  
         YAHOO.util.Connect.asyncRequest('GET', uri, callback);
         
         
         
      },
       
       
      /**
       * Fired by YUI TreeView when a node label is clicked
       * @method onNodeClicked
       * @param args.event {HTML Event} the event object
       * @param args.node {YAHOO.widget.Node} the node clicked
       * @return allowExpand {boolean} allow or disallow node expansion
       */
      onNodeClicked: function DesignerTree_onNodeClicked(args)
      {
         var node = args.node;
         
         if ( node != this.selectedNode) {
            
        	this._updateSelectedNode(node);
         
            YAHOO.Bubbling.fire("designerModelNodeChange",{node : node.data});
       
         }
         Event.stopEvent(args.event);
         
         return false;
      },
      /**
       * Fired when an element has been created
       * @method onElementCreated
       * @param layer {string} the event source
       * @param args {object} arguments object
       */
      onElementCreated: function DesignerTree_onElementCreated(layer, args)
      {
    	  
    	 var obj = args[1].node;
         if (obj && (obj.parentNodeRef !== null))
         {
        	 try {
             var parentNode = this.widgets.treeview.getNodeByProperty("nodeRef", obj.parentNodeRef);
             if (parentNode !== null)
             {
            	 var tmpNode = null;
            	 for(var n in parentNode.children){
            		 if(parentNode.children[n].data.name == obj.assocName){
            			 tmpNode = parentNode.children[n];
            			 break;
            		 }
            		 
            	 }
            	 if(tmpNode==null){
            		parentNode.isLeaf = false;
            		parentNode.expand();
            		tmpNode = this._buildTreeNode({name:obj.assocName,childrens:[obj], type: "m2:"+obj.assocName}, parentNode, true); 
            		tmpNode = tmpNode.children[0];
            	 } else {
            		tmpNode.expand();
                 	tmpNode =  this._buildTreeNode(obj, tmpNode, false);
            	 }
            		 
            	this.widgets.treeview.render();
            	this._updateSelectedNode(tmpNode);
            	
             }
        	 } catch(e){
        		 alert(e);
        	 }
         }
    	 
   
      },
      /**
       * Fired when an element has been deleted
       * @method onElementDeleted
       * @param layer {string} the event source
       * @param args {object} arguments object
       */
      onElementDeleted: function DesignerTree_onElementDeleted(layer, args)
      {
         var obj = args[1];
         if (obj !== null)
         {
            var node = null;
            
            if (obj.nodeRef)
            {
               node = this.widgets.treeview.getNodeByProperty("nodeRef", obj.nodeRef);
            }
            
            if (node !== null)
            {
               var parentNode = node.parent;
               // Node found, so delete it
               this.widgets.treeview.removeNode(node);
               // Have all the parent child nodes been removed now?
               if (parentNode !== null)
               {
                  if (!parentNode.hasChildren())
                  {
                     parentNode.isLeaf = true;
                  }
               }
               this.widgets.treeview.render();
               this._showHighlight(true);
            }
         }
         
      },
      
      /**
       * PRIVATE FUNCTIONS
       */

      /**
       * Creates the controls palette
       * @method _buildControls
       * @private
       */
      _buildControls : function DesignerTree_buildControls(results){
    	  var controls = Dom.get(this.id + "-form-controls");
    	  var sets = Dom.get(this.id + "-form-sets");
    	  
    	  var id, description,fragment;
    	  for(var i in results.controls){
    		  id =  results.controls[i].id;
    		  var liTag = document.createElement('li');
    		  liTag.setAttribute('id', 'form-control-'+ id );
    		  liTag.innerHTML = id;
    		  controls.appendChild(liTag);
    		  new beCPG.DnD('form-control-'+ id, this);
    	  } 	 
    	  for(var i in results.sets){
    		  id =  results.sets[i].id;
    		  var liTag = document.createElement('li');
    		  liTag.setAttribute('id', 'form-set-'+ id );
    		  liTag.innerHTML = id;
    		  sets.appendChild(liTag);
    		  new beCPG.DnD('form-set-'+ id,this);
    	  } 	 

      },
      
      /**
       * Creates the TreeView control and renders it to the parent element
       * @method _buildTree
       * @private
       */
      _buildTree: function DesignerTree_buildTree(results)
      {
         // Create a new tree
         var tree = new YAHOO.widget.TreeView(this.id + "-tree");
         this.widgets.treeview = tree;
         
         // Having both focus and highlight are just confusing (YUI 2.7.0 addition)
         YAHOO.widget.TreeView.FOCUS_CLASS_NAME = "";

         // Turn dynamic loading on for entire tree
         // tree.setDynamicLoad(this.fnLoadNodeData);

         // Get root node for tree
         var root = tree.getRoot();
         
         var modelNode =  this._buildTreeNode(results, tree.getRoot(), true);

         // Register tree-level listeners
         tree.subscribe("clickEvent", this.onNodeClicked, this, true);

         // Render tree with this one top-level node
         tree.render();
         
         //Select first
         if(modelNode!=null){
	         this._updateSelectedNode(modelNode);
	         YAHOO.Bubbling.fire("designerModelNodeChange",{node : modelNode.data});
         }
      },
      /**
       * Highlights the currently selected node.
       * @method _showHighlight
       * @param isVisible {boolean} Whether the highlight is visible or not
       * @private
       */
      _showHighlight: function  DesignerTree_howHighlight(isVisible)
      {
         if (this.selectedNode !== null)
         {
            if (isVisible)
            {
               Dom.addClass(this.selectedNode.getEl(), "selected");
            }
            else
            {
               Dom.removeClass(this.selectedNode.getEl(), "selected");
            }
         }
      },
      
      /**
       * Updates the currently selected node.
       * @method _updateSelectedNode
       * @param node {object} New node to set as currently selected one
       * @private
       */
      _updateSelectedNode: function  DesignerTree_updateSelectedNode(node)
      {
            this._showHighlight(false);
            this.selectedNode = node;
            this._showHighlight(true);
      },
      

      /**
       * YUI WIDGET EVENT HANDLERS
       * Handlers for standard events fired from YUI widgets, e.g. "click"
       */

      /**
       * Build a tree node using passed-in data
       *
       * @method _buildTreeNode
       * @param p_oData {object} Object literal containing required data for new node
       * @param p_oParent {object} Optional parent node
       * @param p_expanded {object} Optional expanded/collaped state flag
       * @return {YAHOO.widget.TextNode} The new tree node
       */
      _buildTreeNode: function DesignerTree_buildTreeNode(p_oData, p_oParent, p_expanded)
      {
    	  var treeNode = null;
        	 var dropInstruction = null;
        	 var draggable = false,
        	 	 droppable = false;
        	 switch(p_oData.type){
	     	 	case  "m2:property":
	     	 		draggable = true;
	     	 		break;
	     	 	case  "m2:type":
	     	 	case  "m2:aspect":
	     	 	case  "m2:properties":
	     	 	case  "m2:propertyOverrides":
	     	 		dropInstruction = "type";
	     	 		droppable = true;
		     	 	break;
	     	 	case  "dsg:form":
	     	 	case  "dsg:formSet":
	     	 	case  "dsg:fields":
	     	 	case  "dsg:sets":
	     	 		dropInstruction = "form";
	     	 		droppable = true;
		     	 	break;
	     	 	case  "dsg:formField":
	     	 		dropInstruction = "field";
	     	 		droppable = true;
		     	 	break;
	     	 }
        	 treeNode =  new YAHOO.widget.TextNode(
	                   {
	                      label: ((p_oData.title!=null && p_oData.title.length>0)?p_oData.title:p_oData.name)  ,
	                      entityTitle : ((p_oData.title!=null && p_oData.title.length>0)?p_oData.title:p_oData.name) ,
	                      name : p_oData.name,
	                      nodeRef: p_oData.nodeRef,
	                      itemType : p_oData.type,
	                      subType : p_oData.subType,
	                      description: p_oData.description,
	                      draggable : draggable,
	                      droppable : droppable,
	                      dropInstruction : dropInstruction,
	                      formId : p_oData.formId ? p_oData.formId : null
	                   }, p_oParent, p_expanded);
	         
        	 if(p_oData.hasError){
        		 treeNode.labelStyle= p_oData.type.replace(":","-");
        	 } else {
        		 treeNode.labelStyle= "dsg-error";
        	 }
        	 
        	 
        	 
	         if(p_oData.childrens.length>0){
		         for(var i in p_oData.childrens){
		        	this._buildTreeNode( p_oData.childrens[i],treeNode,false)
		         } 	 
	         } else {
	        	  treeNode.isLeaf = true;
	         }
	         if(draggable){
	        	 new beCPG.DnD(treeNode.getEl(),this,p_oData.type);
	         }
	         
         return treeNode;
      },

      /**
       * Build URI parameter string for treenode JSON data webscript
       *
       * @method _buildTreeNodeUrl
       * @param path {string} Path to query
       */
       _buildTreeNodeUrl: function DesignerTree_buildTreeNodeUrl(nodeRef)
       {
          var uriTemplate ="becpg/designer/tree/node/" + nodeRef.replace("://","/");
          return  Alfresco.constants.PROXY_URI + uriTemplate;
       }
   });
   
   
   /**
    * Designer Library Drag and Drop object declaration.
    */
   beCPG.DnD = function(id, designerTree, sGroup, config) 
   {
      beCPG.DnD.superclass.constructor.call(this, id, sGroup, config);
      var el = this.getDragEl();
      Dom.setStyle(el, "opacity", 0.67);
      this.designerTree = designerTree;
   };
   
   /**
    * Extend the default YUI drag and drop proxy object to handle DocumentLibrary move operations.
    */
   YAHOO.extend(beCPG.DnD, YAHOO.util.DDProxy, 
   {
      /**
       * A flag used to indicate whether or not an asynchronous move operation request is in progress.
       */
      _inFlight: false,
      
      /**
       * Handles the beginning of a drag operation by setting up the proxy image element.
       */
      startDrag: function DesignerTree_DND_startDrag(x, y) 
      {
          var dragEl = this.getDragEl();
          var clickEl = this.getEl();
//          Dom.setStyle(clickEl, "visibility", "hidden");
//          var proxyImg = document.createElement("img");
//          proxyImg.src = clickEl.src;
//          dragEl.removeChild(dragEl.firstChild);
//          dragEl.appendChild(proxyImg);
//          Dom.setStyle(dragEl, "border", "none");
      },

      /**
       * Handles the end of the drag operation. Because the move operation is asynchronous
       * it is not know if the operation has been a success at the time this function is 
       * invoked so it uses the _inFlight variable to check whether or not a valid drop
       * target was used.
       * 
       * @param The event object
       */
      endDrag: function DesignerTree_DND_endDrag(e)
      {
         if (!this._inFlight)
         {
            var srcEl = this.getEl();
            var proxy = this.getDragEl();
            this.animateResult(proxy, srcEl);
         }
      },
      
      /**
       * Animates an object to move it to the location of a target object. This should typically
       * be animating the proxy object to return to its source.
       * 
       * @param objectToAnimate The object to animate
       * @param animationTarget The object to create a motion animation to
       */
      animateResult: function DesignerTree_DND_animateResult(objectToAnimate, animationTarget) 
      {
          Dom.setStyle(objectToAnimate, "visibility", "");
          var a = new YAHOO.util.Motion( 
                objectToAnimate, { 
                  points: { 
                      to: Dom.getXY(animationTarget)
                  }
              }, 
              0.2, 
              YAHOO.util.Easing.easeOut 
          );
          var proxyid = objectToAnimate.id;
          var thisid = this.id;

          a.onComplete.subscribe(function() {
                  Dom.setStyle(proxyid, "visibility", "hidden");
                  Dom.setStyle(thisid, "visibility", "");
              });
          a.animate();
      },

      /**
       * Handles a drop operation by determining whether or not a valid drop has been performed (e.g.
       * a document or folder onto a folder - NOT a document) and then fires a request to perform
       * the move operation.
       * 
       * @param e The event object
       * @param id The id of the element that the proxy has been dropped onto
       */
      onDragDrop: function DesignerTree_DND_onDragDrop(e, id) 
      {
          var dropTarget = Dom.get(id);
          if (Dom.hasClass(dropTarget, "elementDroppable"))
             {
                // The "documentDroppable" class is not defined in any CSS files but is simply used as
                // a marker to indicate that the element can be used as a document drop target. Only 
                // documents are dragged and dropped onto these elements should result in the drop
                // target request being fired (it's possible that an element could be specified as a
                // YUI drag and drop target for the purposes of controlling drag events without actually
                // allowing drops to occur
                var payload = 
                {
                   elementId: id,
                   callback: this.onDropTargetOwnerCallBack,
                   scope: this
                }
                this._inFlight = true;
                YAHOO.Bubbling.fire("dropTargetOwnerRequest", payload);
                this._setFailureTimeout();
             }
      },
      /**
       * Callback function that is included in the payload of the "dropTargetOwnerRequest" event.
       * This can then be used by a subscriber to the event that claims ownership of the target to
       * generate the move using the associated nodeRef.
       * 
       * @method onDropTargetOwnerCallBack
       * @property nodeRef The nodeRef to move the dragged object to.
       */
      onDropTargetOwnerCallBack: function DesignerTree_DND_onDropTargetOwnerCallBack(nodeRef, type)
      {
         // Clear the timeout that was set...
         this._clearTimeout();
         
         // Move the document/folder...
         var node = new Alfresco.util.NodeRef(nodeRef);
         this._performDND(node, type);
      },
      
      /**
       * Moves the document or folder associated with the drag proxy to the nodeRef supplied. This 
       * method is either called when dropping onto the DocumentList directly or onto any other 
       * valid drop target that can process "dropTargetOwnerRequest" events.
       * 
       * @method _performMove
       * @property nodeRef The nodeRef onto which the proxy should be moved.
       */
      _performDND: function DesignerTree_DND__performDND(nodeRef, path)
      {
         // Set variables required for move...
         var toMoveRecord = this.designerTree.widgets.treeview.getRecord(this.getEl()),
             webscriptName = "move-to/node/{nodeRef}",
             multipleFiles = []; 
      
         multipleFiles.push(this.getEl().id);
         
         // Success callback function:
         // If the operation succeeded then update the tree and refresh the document list.
         var fnSuccess = function DLCMT__onOK_success(p_data)
         {
            this._inFlight = false; // Indicate that a request is no longer "in-flight"
            
            var result,
                successCount = p_data.json.successCount,
                failureCount = p_data.json.failureCount;

            // Did the operation NOT succeed?
            if (!p_data.json.overallSuccess)
            {
               this.animateResult(this.getDragEl(), this.getEl());
               Alfresco.util.PopupManager.displayMessage(
               {
                  text: this.docLib.msg("message.file-dnd-move.failure")
               });
               Dom.removeClass(this.dragFolderHighlight, "dndFolderHighlight");
               return;
            }

            // Refresh the document list...
            this.docLib._updateDocList.call(this.docLib);
           
            // Update the tree if a folder has been moved...
            var moved = toMoveRecord.getData();
            if (moved.node.isContainer)
            {
               YAHOO.Bubbling.fire("folderMoved",
               {
                  multiple: true,
                  nodeRef: moved.nodeRef,
                  destination: path
               });
            }
         };
         // destination: targetNode.location.path + "/" + targetNode.location.file

         // Failure callback function:
         // If the move operation has failed then animate the proxy to return it to the
         // location from which it was dragged. Also, post a failure message.
         var fnFailure = function DLCMT__onOK_failure(p_data)
         {
            this._inFlight = false; // Indicate that a request is no longer "in-flight"
            this.animateResult(this.getDragEl(), this.getEl());
            Alfresco.util.PopupManager.displayMessage(
            {
               text: this.docLib.msg("message.file-dnd-move.failure")
            });
            Dom.removeClass(this.dragFolderHighlight, "dndFolderHighlight");
         };
         
         // Make the request to move the dragged object to the target
         this.docLib.modules.actions.genericAction(
         {
            success:
            {
               callback:
               {
                  fn: fnSuccess,
                  scope: this
               }
            },
            failure:
            {
               callback:
               {
                  fn: fnFailure,
                  scope: this
               }
            },
            webscript:
            {
               method: Alfresco.util.Ajax.POST,
               name: webscriptName,
               params:
               {
                  nodeRef: nodeRef.uri
               }
            },
            wait:
            {
               message: this.docLib.msg("message.please-wait")
            },
            config:
            {
               requestContentType: Alfresco.util.Ajax.JSON,
               dataObj:
               {
                  nodeRefs: multipleFiles,
                  parentId: this.docLib.doclistMetadata.parent.nodeRef
               }
            }
         });
      },
      /**
       * The id of the current window timeout. This should only be non-null if a proxy has been
       * dropped onto a valid drop target that was NOT part of the DocumentList DataTable widget.
       * This id is used to clear the current timeout associated with a drop if the target owner
       * responds with the node ref.
       * 
       * @property _currTimeoutId
       * @type int
       */
      _currTimeoutId: null,
     
      
      /**
       * Clears the timeout that is set when a proxy is dropped onto a valid drop target that is 
       * NOT part of the DocumentList DataTable widget. This clears the timeout, resets the timeout
       * id to null and removes the inflight status of the drop operation.
       * 
       * @method _clearTimeout
       */
      _clearTimeout: function DesignerTree_DND__clearTimeout()
      {
         if (this._currTimeoutId != null)
         {
            window.clearTimeout(this._currTimeoutId);
            this._currTimeoutId = null;
            this._inFlight = false;
         }
      },
      
      /**
       * Creates a timeout for handling drops onto valid drop targets that are NOT part of the
       * DocumentList DataTable widget. This method is called after firing a "dropTargetOwnerRequest"
       * to wait for the owner of the target to respond with the nodeRef associated with the target.
       * If a response is not sent then a failure will be registered.
       * 
       * @method _setFailureTimeout
       */
      _setFailureTimeout: function DesignerTree_DND__setFailureTimeout()
      {
         // Clear any previous timeout...
         this._clearTimeout();
         var _this = this;
         this._currTimeoutId = window.setTimeout(function()
         {
            // An attempt was made to drop a document or folder into a document - NOT a folder
            _this.animateResult(_this.getDragEl(), _this.getEl());
            _this._inFlight = false
            _this._currTimeoutId = null;
         }, 500);
      },
      
      /**
       * @param e The event object
       * @param id The id of the element that the proxy has been dragged over
       */
      onDragOver: function DesignerTree_DND_onDragOver(e, id) 
      {
          var destEl = Dom.get(id);
          if (Dom.hasClass(destEl, "elementDroppableHighlights"))
          {
             // Fire an event indicating a document drag over
             var payload = 
             {
                elementId: id,
                event: e
             }
             YAHOO.Bubbling.fire("elementDragOver", payload);
          }
      },
      
      /**
       * @param e The event object
       * @param id The id of the element that the proxy has been dragged out of
       */
      onDragOut: function DesignerTree_DND_onDragOut(e, id) 
      {
         var destEl = Dom.get(id);
         if (Dom.hasClass(destEl, "elementDroppableHighlights"))
         {
            // Fire an event indicating a document drag out
            var payload = 
            {
               elementId: id,
               event: e
            }
            YAHOO.Bubbling.fire("elementDragOut", payload);
         }
      }
   });

   
   
})();