

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
    	  modelNodeRef: ""

      },

      /**
     
      /**
       * Fired by YUI when parent element is available for scripting.
       *
       * @method onReady
       */
      onReady: function DesignerTree_onReady()
      {
         this.widgets.newModel = Alfresco.util.createYUIButton(this, "newModelButton", this.onNewModel,
         {
            disabled: true
         });
     
         
         this.widgets.modelSelect =  Alfresco.util.createYUIButton(this, "modelSelect-button", this.onTypeSelect,
                 {
                            type: "menu",
                            menu: "modelSelect-menu",
                            lazyloadmenu : false
                  });

                 
         this.widgets.modelSelect.getMenu().subscribe("click", function (p_sType, p_aArgs)
                         {
                              var menuItem = p_aArgs[1];
                              if (menuItem)
                              {
                                  me.widgets.typeSelect.set("label", menuItem.cfg.getProperty("text"));
                              }
                          });
                 
                 
         //select first
         var modelSelected =  this.widgets.modelSelect.getMenu().getItem(0);
         if(modelSelected){
              me.widgets.typeSelect.set("label", typeSelected.cfg.getProperty("text"));
           //	  this.options.itemType = typeSelected._oAnchor.children[0].attributes[0].nodeValue;
                	 
         }
         
         
         this.renderDesignerTree();
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
         
         if(node.data.nodeRef!=null && node.data.nodeRef.length >0){
         
        	  YAHOO.Bubbling.fire("designerModelNodeChange",{nodeRef: node.data.nodeRef,itemType : node.data.type});
         }
  
         Event.stopEvent(args.event);
         
         return false;
      },
      
      /**
       * PRIVATE FUNCTIONS
       */

      /**
       * Creates the TreeView control and renders it to the parent element.
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
       * YUI WIDGET EVENT HANDLERS
       * Handlers for standard events fired from YUI widgets, e.g. "click"
       */

      /**
       * New List button click handler
       *
       * @method onNewList
       * @param e {object} DomEvent
       * @param p_obj {object} Object passed back from addListener method
       */
      onNewModel: function DesignerTree_onNewModel(e, p_obj)
      {
        
      },
      
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
	                      label: p_oData.title!=null?p_oData.title:p_oData.name  ,
	                      nodeRef: p_oData.nodeRef,
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