/**
 * Designer : DesignerTree component.
 * 
 * Displays a list of DesignerTree
 * 
 * @author "Matthieu Laborie <matthieu.laborie@becpg.fr>"
 * @namespace beCPG
 * @class beCPG.component.DesignerTree
 */
(function() {
	/**
	 * YUI Library aliases
	 */
	var Dom = YAHOO.util.Dom, Event = YAHOO.util.Event, Bubbling = YAHOO.Bubbling;

	/**
	 * DesignerTree constructor.
	 * 
	 * @param htmlId
	 *           {String} The HTML id of the parent element
	 * @return {beCPG.component.DesignerTree} The new DesignerTree instance
	 * @constructor
	 */
	beCPG.component.DesignerTree = function(htmlId) {
		this.id = htmlId;
		beCPG.component.DesignerTree.superclass.constructor.call(this, "beCPG.component.DesignerTree", htmlId, [
		      "button", "container" ]);

		YAHOO.Bubbling.on("selectedModelChanged", this.renderModelTree, this);
		YAHOO.Bubbling.on("selectedConfigChanged", this.renderConfigTree, this);
		YAHOO.Bubbling.on("elementCreated", this.onElementCreated, this);
		YAHOO.Bubbling.on("elementDeleted", this.onElementDeleted, this);

		return this;
	};

	/**
	 * Extend from Alfresco.component.Base
	 */
	YAHOO.extend(beCPG.component.DesignerTree, Alfresco.component.Base, {
	   /**
		 * Object container for initialization options
		 * 
		 * @property options
		 * @type object
		 */
	   options : {
	      /**
			 * Current modelNodeRef.
			 * 
			 * @property modelNodeRef
			 * @type string
			 * @default ""
			 */
	      modelNodeRef : null,
	      /**
			 * Current configNodeRef.
			 * 
			 * @property configNodeRef
			 * @type string
			 * @default ""
			 */
	      configNodeRef : null

	   },
	   /**
		 * Selected tree node.
		 * 
		 * @property selectedNode
		 * @type {YAHOO.widget.Node}
		 */
	   selectedNode : null,

	   /**
		 * 
		 * /** Fired by YUI when parent element is available for scripting.
		 * 
		 * @method onReady
		 */
	   onReady : function DesignerTree_onReady() {
		   var headers = YUISelector.query("h2", this.id);

		   // Models

		   this.widgets.modelSelect = Alfresco.util.createYUIButton(this, "modelSelect-button", this.onModelSelect, {
		      type : "menu",
		      menu : "modelSelect-menu",
		      lazyloadmenu : false
		   });

		   var modelSelectedMenu = this.widgets.modelSelect.getMenu();

		   // Configs

		   this.widgets.configSelect = Alfresco.util.createYUIButton(this, "configSelect-button", this.onConfigSelect, {
		      type : "menu",
		      menu : "configSelect-menu",
		      lazyloadmenu : false
		   });

		   var configSelectedMenu = this.widgets.configSelect.getMenu();

		   if (YAHOO.lang.isArray(headers)) {
			   for ( var i in headers) {
				   // Create twister from the first H2 tag found by the query
				   Alfresco.util.createTwister(headers[i], this.filterName);
			   }
		   }

		   // select first
		   var configSelected = configSelectedMenu.getItem(0);
		   var modelSelected = modelSelectedMenu.getItem(0);
		   var readOnly = null, className = null;

		   if (this.options.modelNodeRef != null && this.options.modelNodeRef.length > 0) {
			   Bubbling.fire("selectedModelChanged", {
			      nodeRef : this.options.modelNodeRef,
			      readOnly : false
			   });
		   } else {
			   if (modelSelected) {
				   className = modelSelected._oAnchor.children[0].attributes[0].nodeValue;
				   readOnly = className.split("|")[1];
				   Bubbling.fire("selectedModelChanged", {
				      nodeRef : className.split("|")[0],
				      readOnly : readOnly != null ? readOnly == "true" : false
				   });
			   }
		   }
		   if (this.options.configNodeRef != null && this.options.configNodeRef.length > 0) {
			   Bubbling.fire("selectedConfigChanged", {
			      nodeRef : this.options.configNodeRef,
			      readOnly : false
			   });
		   } else {
			   if (configSelected) {
				   className = configSelected._oAnchor.children[0].attributes[0].nodeValue;
				   readOnly = className.split("|")[1];
				   Bubbling.fire("selectedConfigChanged", {
				      nodeRef : className.split("|")[0],
				      readOnly : readOnly != null ? readOnly == "true" : false
				   });
			   }
		   }

	   },

	   onConfigSelect : function DesignerTree_onConfigSelect(sType, aArgs, p_obj) {
		   var eventTarget = aArgs[1], className = Alfresco.util.findEventClass(eventTarget), readOnly = className
		         .split("|")[1];
		   Bubbling.fire("selectedConfigChanged", {
		      nodeRef : className.split("|")[0],
		      readOnly : readOnly != null ? readOnly == "true" : false
		   });

	   },

	   onModelSelect : function DesignerTree_onModelSelect(sType, aArgs, p_obj) {
		   var eventTarget = aArgs[1], className = Alfresco.util.findEventClass(eventTarget), readOnly = className
		         .split("|")[1];
		   Bubbling.fire("selectedModelChanged", {
		      nodeRef : className.split("|")[0],
		      readOnly : readOnly != null ? readOnly == "true" : false
		   });

	   },

	   /**
		 * Renders the Model tree
		 * 
		 * @method renderModelTree
		 */
	   renderModelTree : function DesignerTree_renderModelTree(layers, args) {
		   var obj = args[1];
		   this.options.modelNodeRef = obj.nodeRef;
		   this.renderTree(this.id + "-model-tree", obj.nodeRef, obj.readOnly);
	   },
	   /**
		 * Renders the Model tree
		 * 
		 * @method renderModelTree
		 */
	   renderConfigTree : function DesignerTree_renderConfigTree(layers, args) {
		   var obj = args[1];
		   this.options.configNodeRef = obj.nodeRef;
		   this.renderTree(this.id + "-form-tree", obj.nodeRef, obj.readOnly);
	   },

	   /**
		 * Renders the Model tree
		 * 
		 * @method renderModelTree
		 */
	   renderTree : function DesignerTree_renderModelTree(treeId, rootNodeRef, readOnly) {
		   var me = this;

		   // Prepare the XHR callback object
		   var callback = {
		      success : function DesignerTreelND_success(oResponse) {
		    	  popup.destroy();
			      var results = YAHOO.lang.JSON.parse(oResponse.responseText);

			      if (results) {
				      // Build the TreeView widget
				      this._buildTree(treeId, results, rootNodeRef, readOnly);
			      }

		      },

		      // If the XHR call is not successful, fire the TreeView callback
		      // anyway
		      failure : function DesignerTreelND_failure(oResponse) {
		    	  popup.destroy();
			      if (oResponse.status == 401) {
				      // Our session has likely timed-out, so refresh to offer the
				      // login page
				      window.location.reload();
			      }
			      alert("Unexpected error");
		      },

		      // Callback function scope
		      scope : me
		   };

		   var uri = this._buildTreeNodeUrl(rootNodeRef);
		   
           var popup = Alfresco.util.PopupManager.displayMessage({
               text : this.msg("message.tree-loading"),
               spanClass : "wait",
               displayTime : 0
            });

		   // Make the XHR call using Connection Manager's asyncRequest method

           
		   YAHOO.util.Connect.asyncRequest('GET', uri, callback);

	   },

	   /**
		 * Fired by YUI TreeView when a node label is clicked
		 * 
		 * @method onNodeClicked
		 * @param args.event
		 *           {HTML Event} the event object
		 * @param args.node
		 *           {YAHOO.widget.Node} the node clicked
		 * @return allowExpand {boolean} allow or disallow node expansion
		 */
	   onNodeClicked : function DesignerTree_onNodeClicked(tree) {

		   return function(args) {

			   var node = args.node;

			   if (node != this.selectedNode) {

				   this._updateSelectedNode(node, tree);

			   }
			   Event.stopEvent(args.event);

			   return false;
		   };
	   },
	   /**
		 * Fired when an element has been created
		 * 
		 * @method onElementCreated
		 * @param layer
		 *           {string} the event source
		 * @param args
		 *           {object} arguments object
		 */
	   onElementCreated : function DesignerTree_onElementCreated(layer, args) {

		   var obj = args[1], tree;
		   if (obj !== null) {
			   tree = obj.tree;

			   var parentNode = tree.getNodeByProperty("nodeRef", obj.node.nodeRef);
			   if (parentNode !== null) {
				   // create newParent
				   var newParentNode = this._buildTreeNode(obj.node, parentNode.parent, true);
				   if(newParentNode!=null){
					   newParentNode.insertBefore(parentNode);
					   tree.removeNode(parentNode);
					   tree.render();
				   }
			   }

			   // Focus on node
			   var selected = tree.getNodeByProperty("nodeRef", obj.focusNodeRef);
			   if (selected != null) {
				   selected.parent.expand();
				   this._updateSelectedNode(selected, tree);
			   }

		   }

	   },

	   /**
		 * Fired when an element has been deleted
		 * 
		 * @method onElementDeleted
		 * @param layer
		 *           {string} the event source
		 * @param args
		 *           {object} arguments object
		 */
	   onElementDeleted : function DesignerTree_onElementDeleted(layer, args) {
		   var obj = args[1];
		   if (obj !== null) {
			   var node = null;

			   var tree = obj.tree;

			   if (obj.nodeRef) {
				   node = tree.getNodeByProperty("nodeRef", obj.nodeRef);
			   }

			   if (node !== null) {
				   var parentNode = node.parent;
				   // Node found, so delete it
				   tree.removeNode(node);
				   // Have all the parent child nodes been removed now?
				   if (parentNode !== null) {
					   if (!parentNode.hasChildren()) {
						   parentNode.isLeaf = true;
					   }
				   }
				   tree.render();
				   if (parentNode != null) {
					   this._updateSelectedNode(parentNode, tree);
				   }

			   }
		   }

	   },

	   /**
		 * PRIVATE FUNCTIONS
		 */

	   /**
		 * Creates the TreeView control and renders it to the parent element
		 * 
		 * @method _buildTree
		 * @private
		 */
	   _buildTree : function DesignerTree_buildTree(id, results, modelNodeRef, readOnly) {
		   
		   // Create a new tree
		   var tree = new YAHOO.widget.TreeView(id);

		   tree.modelNodeRef = modelNodeRef;
		   tree.isReadOnly = readOnly;

		   // Having both focus and highlight are just confusing (YUI 2.7.0
		   // addition)
		   YAHOO.widget.TreeView.FOCUS_CLASS_NAME = "";

		   // Get root node for tree
		   var root = tree.getRoot();

		   var modelNode = this._buildTreeNode(results, root, true);

		   // Register tree-level listeners
		   tree.subscribe("clickEvent", this.onNodeClicked(tree), this, true);
		   tree.subscribe("expandComplete", this.onExpandComplete(tree), this, true);

		   // Render tree with this one top-level node
		   tree.render();

		   // Select first
		   if (modelNode != null) {
			   this._updateSelectedNode(modelNode, tree);
		   }
	   },
	   /**
		 * Creates the drag and drop targets within the tree. The targets get
		 * removed each time that the tree is refreshed in anyway, so it is
		 * imperative that they get reset when required.
		 * 
		 * @method _applyDropTargets
		 */
	   _applyDropTargets : function DesignerTree__applyDropTargets(tree) {
		   // add DND

		   var rootEl = tree.getEl();

		   var dndTargets = Dom.getElementsByClassName("m2-property", "span", rootEl);
		   for ( var i = 0, j = dndTargets.length; i < j; i++) {
			   new beCPG.DnD(dndTargets[i].parentNode, this, tree, "property");
		   }

		   ndTargets = Dom.getElementsByClassName("m2-propertyOverride", "span", rootEl);
		   for ( var i = 0, j = dndTargets.length; i < j; i++) {
			   new beCPG.DnD(dndTargets[i].parentNode, this, tree, "property");
		   }

		   dndTargets = Dom.getElementsByClassName("m2-association", "span", rootEl);
		   for ( var i = 0, j = dndTargets.length; i < j; i++) {
			   new beCPG.DnD(dndTargets[i].parentNode, this, tree, "association");
		   }

		   dndTargets = Dom.getElementsByClassName("m2-childAssociation", "span", rootEl);
		   for ( var i = 0, j = dndTargets.length; i < j; i++) {
			   new beCPG.DnD(dndTargets[i].parentNode, this, tree, "association");
		   }

		   dndTargets = Dom.getElementsByClassName("dsg-formField", "span", rootEl);
		   for ( var i = 0, j = dndTargets.length; i < j; i++) {
			   new beCPG.DnD(dndTargets[i].parentNode, this, tree, "field");
		   }

		   dndTargets = Dom.getElementsByClassName("m2-type", "span", rootEl);
		   for ( var i = 0, j = dndTargets.length; i < j; i++) {
			   new beCPG.DnD(dndTargets[i].parentNode, this, tree, "type");
		   }

		   dndTargets = Dom.getElementsByClassName("m2-aspect", "span", rootEl);
		   for ( var i = 0, j = dndTargets.length; i < j; i++) {
			   new beCPG.DnD(dndTargets[i].parentNode, this, tree, "aspect");
		   }

	   },
	   /**
		 * Fired by YUI TreeView when a node has finished expanding
		 * 
		 * @method onExpandComplete
		 * @param oNode
		 *           {YAHOO.widget.Node} the node recently expanded
		 */
	   onExpandComplete : function DesignerTree_onExpandComplete(tree) {
		   return function(oNode) {

			   // Finished expanding, can now safely set DND targets...
			   this._applyDropTargets(tree);
		   };
	   },
	   /**
		 * Highlights the currently selected node.
		 * 
		 * @method _showHighlight
		 * @param isVisible
		 *           {boolean} Whether the highlight is visible or not
		 * @private
		 */
	   _showHighlight : function DesignerTree_howHighlight(isVisible) {
		   if (this.selectedNode !== null) {
			   if (isVisible) {
				   Dom.addClass(this.selectedNode.getEl(), "selected");
			   } else {
				   Dom.removeClass(this.selectedNode.getEl(), "selected");
			   }
		   }
	   },

	   /**
		 * Updates the currently selected node.
		 * 
		 * @method _updateSelectedNode
		 * @param node
		 *           {object} New node to set as currently selected one
		 * @private
		 */
	   _updateSelectedNode : function DesignerTree_updateSelectedNode(node, tree) {
		   this._showHighlight(false);
		   this.selectedNode = node;
		   this._showHighlight(true);

		   YAHOO.Bubbling.fire("designerModelNodeChange", {
		      node : node.data,
		      tree : tree
		   });
	   },

	   /**
		 * YUI WIDGET EVENT HANDLERS Handlers for standard events fired from YUI
		 * widgets, e.g. "click"
		 */

	   /**
		 * Build a tree node using passed-in data
		 * 
		 * @method _buildTreeNode
		 * @param p_oData
		 *           {object} Object literal containing required data for new node
		 * @param p_oParent
		 *           {object} Optional parent node
		 * @param p_expanded
		 *           {object} Optional expanded/collaped state flag
		 * @return {YAHOO.widget.TextNode} The new tree node
		 */
	   _buildTreeNode : function DesignerTree_buildTreeNode(p_oData, p_oParent, p_expanded) {
		   try {
			   	if(p_oData.type){
				   var treeNode = new YAHOO.widget.TextNode({
				      label : ((p_oData.title != null && p_oData.title.length > 0) ? p_oData.title : p_oData.name),
				      entityTitle : ((p_oData.title != null && p_oData.title.length > 0) ? p_oData.title : p_oData.name),
				      name : p_oData.name,
				      nodeRef : p_oData.nodeRef,
				      itemType : p_oData.type,
				      parentType : (p_oParent != null && p_oParent.data != null) ? p_oParent.data.itemType : null,
				      subType : p_oData.subType,
				      description : p_oData.description,
				      draggable : p_oData.draggable,
				      accepts : p_oData.accepts,
				      formId : p_oData.formId ? p_oData.formId : null,
				      formKind : p_oData.formKind ? p_oData.formKind : null,
				      formType : p_oData.formType ? p_oData.formType : null
				   }, p_oParent, p_expanded);
	
				   if (p_oData.hasError) {
					   treeNode.labelStyle = p_oData.type.replace(":", "-");
				   } else {
					   treeNode.labelStyle = "dsg-error";
				   }
	
				   if (p_oData.childrens.length > 0) {
					   for ( var i in p_oData.childrens) {
						   this._buildTreeNode(p_oData.childrens[i], treeNode, false);
					   }
				   } else {
					   treeNode.isLeaf = true;
				   }
	
				   return treeNode;
			   	} 
			   	return null;

		   } catch (e) {
			   alert(e);
		   }
	   },

	   /**
		 * Build URI parameter string for treenode JSON data webscript
		 * 
		 * @method _buildTreeNodeUrl
		 * @param path
		 *           {string} Path to query
		 */
	   _buildTreeNodeUrl : function DesignerTree_buildTreeNodeUrl(nodeRef) {
		   var uriTemplate = "becpg/designer/tree/node/" + nodeRef.replace("://", "/");
		   return Alfresco.constants.PROXY_URI + uriTemplate;
	   }
	});

	/**
	 * Designer Library Drag and Drop object declaration.
	 */
	beCPG.DnD = function(id, designerTree, tree, sGroup, config) {
		beCPG.DnD.superclass.constructor.call(this, id, sGroup, config);
		var el = this.getDragEl();
		Dom.setStyle(el, "opacity", 0.67);
		this.designerTree = designerTree;
		this.tree = tree;
	};

	/**
	 * Extend the default YUI drag and drop proxy object to handle
	 * DocumentLibrary move operations.
	 */
	YAHOO.extend(beCPG.DnD, YAHOO.util.DDProxy, {
	   /**
		 * A flag used to indicate whether or not an asynchronous move operation
		 * request is in progress.
		 */
	   _inFlight : false,

	   /**
		 * Handles the beginning of a drag operation by setting up the proxy image
		 * element.
		 */
	   startDrag : function DesignerTree_DND_startDrag(x, y) {
		   var dragEl = this.getDragEl();
		   var clickEl = this.getEl();
         dragEl.innerHTML = clickEl.innerHTML;
         Dom.setStyle(dragEl, "color", Dom.getStyle(clickEl, "color"));
         Dom.setStyle(dragEl, "background-image", Dom.getStyle(clickEl, "background-image"));
         Dom.setStyle(dragEl, "background-repeat", Dom.getStyle(clickEl, "background-repeat"));
         Dom.setStyle(dragEl, "background-position", Dom.getStyle(clickEl, "background-position"));
         Dom.setStyle(dragEl, "background-size", Dom.getStyle(clickEl, "background-size"));
         Dom.setStyle(dragEl, "padding-left", Dom.getStyle(clickEl, "padding-left"));
         Dom.setStyle(dragEl, "background-color", "#E3EAEC");
         Dom.setStyle(dragEl, "border", "1px solid #6CA5CE");
         Dom.setStyle(dragEl, "width", Dom.getStyle(clickEl, "width"));
	   },

	   /**
		 * Handles the end of the drag operation. Because the move operation is
		 * asynchronous it is not know if the operation has been a success at the
		 * time this function is invoked so it uses the _inFlight variable to
		 * check whether or not a valid drop target was used.
		 * 
		 * @param The
		 *           event object
		 */
	   endDrag : function DesignerTree_DND_endDrag(e) {
		   if (!this._inFlight) {
			   var srcEl = this.getEl();
			   var proxy = this.getDragEl();
			   this.animateResult(proxy, srcEl);
		   }
	   },

	   /**
		 * Animates an object to move it to the location of a target object. This
		 * should typically be animating the proxy object to return to its source.
		 * 
		 * @param objectToAnimate
		 *           The object to animate
		 * @param animationTarget
		 *           The object to create a motion animation to
		 */
	   animateResult : function DesignerTree_DND_animateResult(objectToAnimate, animationTarget) {
		   Dom.setStyle(objectToAnimate, "visibility", "");
		   var a = new YAHOO.util.Motion(objectToAnimate, {
			   points : {
				   to : Dom.getXY(animationTarget)
			   }
		   }, 0.2, YAHOO.util.Easing.easeOut);
		   var proxyid = objectToAnimate.id;
		   var thisid = this.id;

		   a.onComplete.subscribe(function() {
			   Dom.setStyle(proxyid, "visibility", "hidden");
			   Dom.setStyle(thisid, "visibility", "");
		   });
		   a.animate();
	   },

	   /**
		 * Handles a drop operation by determining whether or not a valid drop has
		 * been performed (e.g. a document or folder onto a folder - NOT a
		 * document) and then fires a request to perform the move operation.
		 * 
		 * @param e
		 *           The event object
		 * @param id
		 *           The id of the element that the proxy has been dropped onto
		 */
	   onDragDrop : function DesignerTree_DND_onDragDrop(e, id) {
		   var dropTarget = Dom.get(id);
		   if (Dom.hasClass(dropTarget, "elementDroppable")) {
			   // The "documentDroppable" class is not defined in any CSS files but
			   // is simply used as
			   // a marker to indicate that the element can be used as a document
			   // drop target. Only
			   // documents are dragged and dropped onto these elements should
			   // result in the drop
			   // target request being fired (it's possible that an element could
			   // be specified as a
			   // YUI drag and drop target for the purposes of controlling drag
			   // events without actually
			   // allowing drops to occur
			   var payload = {
			      elementId : id,
			      callback : this.onDropTargetOwnerCallBack,
			      scope : this
			   };
			   this._inFlight = true;
			   YAHOO.Bubbling.fire("dropTargetOwnerRequest", payload);
			   this._setFailureTimeout();
		   }
	   },
	   /**
		 * Callback function that is included in the payload of the
		 * "dropTargetOwnerRequest" event. This can then be used by a subscriber
		 * to the event that claims ownership of the target to generate the move
		 * using the associated nodeRef.
		 * 
		 * @method onDropTargetOwnerCallBack
		 * @property nodeRef The nodeRef to move the dragged object to.
		 */
	   onDropTargetOwnerCallBack : function DesignerTree_DND_onDropTargetOwnerCallBack(nodeRef, type, tree) {
		   // Clear the timeout that was set...
		   this._clearTimeout();

		   // Move the document/folder...
		   var node = new Alfresco.util.NodeRef(nodeRef);
		   this._performDND(node, type, tree);
	   },

	   /**
		 * Moves the document or folder associated with the drag proxy to the
		 * nodeRef supplied. This method is either called when dropping onto the
		 * DocumentList directly or onto any other valid drop target that can
		 * process "dropTargetOwnerRequest" events.
		 * 
		 * @method _performMove
		 * @property nodeRef The nodeRef onto which the proxy should be moved.
		 */
	   _performDND : function DesignerTree_DND__performDND(nodeRef, type, toMovedTree) {
		   try {
			   var id = this.getEl().id, actionUrl, toMoveNodeRef = null;

			   if (id != null && (id.indexOf("formSets_") > -1 || id.indexOf("formControls_") > -1)) {
				   actionUrl = Alfresco.constants.PROXY_URI + "becpg/designer/dnd/" + id + "?nodeRef=" + nodeRef;

			   } else {

				   var toMoveRecord = this.tree.getNodeByElement(this.getEl());
				   toMoveNodeRef = toMoveRecord.data.nodeRef;
				   actionUrl = Alfresco.constants.PROXY_URI + "becpg/designer/dnd/" + toMoveNodeRef.replace(":/", "")
				         + "?nodeRef=" + nodeRef;
			   }

			   var me = this;

			   var callback = function(response) {

				   if (response.json && response.json.persistedObject) {

					   var treeNode = response.json.treeNode;

					   // Delete old if it's a move
					   if (toMoveNodeRef != null && toMoveNodeRef == response.json.persistedObject) {
						   var selected = toMovedTree.getNodeByProperty("nodeRef", toMoveNodeRef);
						   if (selected != null) {
							   toMovedTree.removeNode(selected);
						   }
					   }

					   // Create new
					   YAHOO.Bubbling.fire("elementCreated", {
					      node : treeNode,
					      tree : toMovedTree,
					      focusNodeRef : response.json.persistedObject
					   });

					   Alfresco.util.PopupManager.displayMessage({
						   text : me.designerTree.msg("message.dnd.success")
					   });
				   } else {
					   Alfresco.util.PopupManager.displayMessage({
						   text : me.designerTree.msg("message.dnd.failure")
					   });
				   }

			   };

			   // call webscript...

			   Alfresco.util.Ajax.request({
			      method : Alfresco.util.Ajax.POST,
			      url : actionUrl,
			      successCallback : {
			         fn : callback,
			         scope : this
			      },
			      failureMessage : me.designerTree.msg("message.dnd.failure"),
			      scope : this,
			      execScripts : false
			   });

		   } catch (e) {
			   alert(e);
		   }

	   },
	   /**
		 * The id of the current window timeout. This should only be non-null if a
		 * proxy has been dropped onto a valid drop target that was NOT part of
		 * the DocumentList DataTable widget. This id is used to clear the current
		 * timeout associated with a drop if the target owner responds with the
		 * node ref.
		 * 
		 * @property _currTimeoutId
		 * @type int
		 */
	   _currTimeoutId : null,

	   /**
		 * Clears the timeout that is set when a proxy is dropped onto a valid
		 * drop target that is NOT part of the DocumentList DataTable widget. This
		 * clears the timeout, resets the timeout id to null and removes the
		 * inflight status of the drop operation.
		 * 
		 * @method _clearTimeout
		 */
	   _clearTimeout : function DesignerTree_DND__clearTimeout() {
		   if (this._currTimeoutId != null) {
			   window.clearTimeout(this._currTimeoutId);
			   this._currTimeoutId = null;
			   this._inFlight = false;
		   }
	   },

	   /**
		 * Creates a timeout for handling drops onto valid drop targets that are
		 * NOT part of the DocumentList DataTable widget. This method is called
		 * after firing a "dropTargetOwnerRequest" to wait for the owner of the
		 * target to respond with the nodeRef associated with the target. If a
		 * response is not sent then a failure will be registered.
		 * 
		 * @method _setFailureTimeout
		 */
	   _setFailureTimeout : function DesignerTree_DND__setFailureTimeout() {
		   // Clear any previous timeout...
		   this._clearTimeout();
		   var _this = this;
		   this._currTimeoutId = window.setTimeout(function() {
			   // An attempt was made to drop a document or folder into a document
			   // - NOT a folder
			   _this.animateResult(_this.getDragEl(), _this.getEl());
			   _this._inFlight = false;
			   _this._currTimeoutId = null;
		   }, 500);
	   },

	   /**
		 * @param e
		 *           The event object
		 * @param id
		 *           The id of the element that the proxy has been dragged over
		 */
	   onDragOver : function DesignerTree_DND_onDragOver(e, id) {
		   var destEl = Dom.get(id);
		   if (Dom.hasClass(destEl, "elementDroppableHighlights")) {
			   // Fire an event indicating a document drag over
			   var payload = {
			      elementId : id,
			      event : e
			   };
			   YAHOO.Bubbling.fire("elementDragOver", payload);
		   }
	   },

	   /**
		 * @param e
		 *           The event object
		 * @param id
		 *           The id of the element that the proxy has been dragged out of
		 */
	   onDragOut : function DesignerTree_DND_onDragOut(e, id) {
		   var destEl = Dom.get(id);
		   if (Dom.hasClass(destEl, "elementDroppableHighlights")) {
			   // Fire an event indicating a document drag out
			   var payload = {
			      elementId : id,
			      event : e
			   };
			   YAHOO.Bubbling.fire("elementDragOut", payload);
		   }
	   }
	});

})();
