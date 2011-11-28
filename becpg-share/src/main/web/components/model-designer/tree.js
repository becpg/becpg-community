

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
         
        this._buildTreeNode(results, tree.getRoot(), true);

         // Register tree-level listeners
         tree.subscribe("clickEvent", this.onNodeClicked, this, true);

         // Render tree with this one top-level node
         tree.render();
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
         try {
        	 treeNode =  new YAHOO.widget.TextNode(
	                   {
	                      label: ((p_oData.title!=null && p_oData.title.length>0)?p_oData.title:p_oData.name)  ,
	                      name : p_oData.name,
	                      nodeRef: p_oData.nodeRef,
	                      itemType : p_oData.type,
	                      description: p_oData.description
	                   }, p_oParent, p_expanded);
	         
	         if(p_oData.childrens.length>0){
		         for(var i in p_oData.childrens){
		        	this._buildTreeNode( p_oData.childrens[i],treeNode,false)
		         } 	 
	         } else {
	        	  treeNode.isLeaf = true;
	         }
         } catch (e) {
			alert(e);
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
})();