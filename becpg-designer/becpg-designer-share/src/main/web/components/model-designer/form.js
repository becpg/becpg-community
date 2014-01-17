/**
 * Designer : DesignerForm component.
 * 
 * Displays DesignerForm
 * 
 * @author "Matthieu Laborie <matthieu.laborie@becpg.fr>"
 * @namespace beCPG
 * @class beCPG.component.DesignerForm
 */
(function() {
	/**
	 * YUI Library aliases
	 */
	var Dom = YAHOO.util.Dom, Bubbling = YAHOO.Bubbling;

	/**
	 * DesignerForm constructor.
	 * 
	 * @param htmlId
	 *           {String} The HTML id of the parent element
	 * @return {beCPG.component.DesignerForm} The new DesignerForm instance
	 * @constructor
	 */
	beCPG.component.DesignerForm = function(htmlId) {
		beCPG.component.DesignerForm.superclass.constructor.call(this, "beCPG.component.DesignerForm", htmlId, [
		      "button", "container" ]);

		YAHOO.Bubbling.on("designerModelNodeChange", this.onDesignerModelNodeChange, this);
		YAHOO.Bubbling.on("beforeFormRuntimeInit", this.onBeforeFormRuntimeInit, this);
		YAHOO.Bubbling.on("elementDragOver", this.onElementDragOver, this);
		YAHOO.Bubbling.on("elementDragOut", this.onElementDragOut, this);
		YAHOO.Bubbling.on("dropTargetOwnerRequest", this.onDropTargetOwnerRequest, this);

		return this;
	};

	/**
	 * Extend from Alfresco.component.Base
	 */
	YAHOO
	      .extend(
	            beCPG.component.DesignerForm,
	            Alfresco.component.Base,
	            {
	               /**
						 * Object container for initialization options
						 * 
						 * @property options
						 * @type object
						 */
	               options : {
		               documentHeight : null

	               },

	               /**
						 * Current tree node
						 */
	               currentNode : null,

	               /**
						 * Current tree
						 */
	               tree : null,

	               /**
						 * 
						 * /** Fired by YUI when parent element is available for
						 * scripting.
						 * 
						 * @method onReady
						 */
	               onReady : function DesignerForm_onReady() {

		               // Reference to Data Grid component
		               this.modules.dataGrid = Alfresco.util.ComponentManager.findFirst("beCPG.module.EntityDataGrid");

		               this.show();

	               },

	               /**
						 * Main entrypoint to show the dialog
						 * 
						 * @method show
						 */
	               show : function DesignerForm_show() {
		               if (this.currentNode != null) {

			               var templateUrl, data, template, templateInstance, container = Dom.get(this.id
			                     + "-dnd-instructions");

			               // First destroy old
			               YAHOO.Bubbling.fire("formContainerDestroyed");

			               // Clean instructions
			               if (container.hasChildNodes()) {
				               while (container.childNodes.length >= 1) {
					               container.removeChild(container.firstChild);
				               }
			               }

			               // show instructions
			               if (this.currentNode.accepts.length > 0) {

				               var dropGroup = null;
				               switch (this.currentNode.itemType) {
				               case "m2:type":
				               case "dsg:fields":
				               case "m2:aspect":
				               case "m2:properties":
				               case "m2:propertyOverrides":
					               dropGroup = "type";
					               break;
				               case "m2:association":
				               case "m2:childAssociation":
					               dropGroup = "association";
					               break;
				               case "dsg:form":
				               case "dsg:formSet":
				               case "dsg:sets":
					               dropGroup = "form";
					               break;
				               case "dsg:formField":
					               dropGroup = "field";
					               break;
				               case "dsg:config":
				               case "dsg:configElements":
					               dropGroup = "config";
					               break;
				               }
				               template = Dom.get(this.id + "-dnd-instructions-" + dropGroup);
				               templateInstance = template.cloneNode(true);
				               templateInstance.id = this.id + "-dropZone";
				               Dom.removeClass(templateInstance, "hidden");
				               Dom.addClass(templateInstance, "elementDroppable");
				               Dom.addClass(templateInstance, "elementDroppableHighlights");
				               container.appendChild(templateInstance);
				               var dndTarget = new YAHOO.util.DDTarget(templateInstance);
				               for (i in this.currentNode.accepts) {
					               dndTarget.addToGroup(this.currentNode.accepts[i]);
				               }

			               }

			               // show datas
			               if (this.currentNode.subType != null) {
				               Dom.setStyle(this.id + "-model-form", "display", "none");
				               Dom.setStyle("modelFormDataGridDiv", "display", "block");
				               if (this.modules.dataGrid) {
					               this.modules.dataGrid.setOptions({
						               entityNodeRef : this.tree.modelNodeRef
					               });
					               Bubbling.fire("activeDataListChanged", {
					                  dataList : {
					                     "title" : this.currentNode.entityTitle,
					                     "description" : this.currentNode.description,
					                     "nodeRef" : this.currentNode.nodeRef,
					                     "entityName" : this.currentNode.name,
					                     "itemType" : this.currentNode.subType,
					                     "name" : "designerList"
					                  },
					                  scrollTo : true
					               });
				               }

			               } else {
			               	Dom.setStyle("modelFormDataGridDiv", "display", "none");
				               Dom.setStyle(this.id + "-model-form", "display", "block");
				               templateUrl = YAHOO.lang
				                     .substitute(
				                           Alfresco.constants.URL_SERVICECONTEXT
				                                 + "components/form?entityNodeRef={entityNodeRef}&itemKind={itemKind}&itemId={itemId}&mode={mode}&submitType={submitType}&showCancelButton=false",
				                           {
				                              itemKind : "node",
				                              itemId : this.currentNode.nodeRef,
				                              mode : "edit",
				                              submitType : "json",
				                              entityNodeRef : this.tree.modelNodeRef
				                           });
				               data = {
					               htmlid : this.id
					              };

				               Alfresco.util.Ajax.request({
				                  url : templateUrl,
				                  dataObj : data,
				                  successCallback : {
				                     fn : this.onTemplateLoaded,
				                     scope : this
				                  },
				                  failureMessage : "Could not load dialog template from '" + this.options.templateUrl
				                        + "'.",
				                  scope : this,
				                  execScripts : true
				               });

			               }
		               }

		               return this;
	               },

	               /**
						 * @method onDesignerModelNodeChange
						 */
	               onDesignerModelNodeChange : function DesignerForm_onNodeClicked(layer, args) {
		               var obj = args[1];

		               if (obj != null && obj.node != null && obj.node.nodeRef != null) {

			               this.currentNode = obj.node;
			               this.tree = obj.tree;

			               this.show();
		               } else {
			               var containerDiv = Dom.get(this.id + "-model-form");
			               containerDiv.innerHTML = this.msg("model.please-select");
		               }
	               },

	               /**
						 * Event callback when dialog template has been loaded
						 * 
						 * @method onTemplateLoaded
						 * @param response
						 *           {object} Server response from load template XHR
						 *           request
						 */
	               onTemplateLoaded : function DesignerForm_onTemplateLoaded(response) {

		               // Inject the template from the XHR request into a new DIV
							// element
		               var containerDiv = Dom.get(this.id + "-model-form");

		               if (containerDiv.hasChildNodes()) {
			               while (containerDiv.childNodes.length >= 1) {
				               containerDiv.removeChild(containerDiv.firstChild);
			               }
		               }

		               containerDiv.innerHTML = response.serverResponse.responseText;

	               },
	               /**
						 * Event handler called when the "beforeFormRuntimeInit" event
						 * is received.
						 * 
						 * @method onBeforeFormRuntimeInit
						 * @param layer
						 *           {String} Event type
						 * @param args
						 *           {Object} Event arguments
						 * 
						 */
	               onBeforeFormRuntimeInit : function DesignerForm_onBeforeFormRuntimeInit(layer, args) {
	               	if(args[1].eventGroup == this.id + "-form"){
			               var formsRuntime = args[1].runtime;
	
			               this.form = formsRuntime;
			               this.form.setAJAXSubmit(true, {
			                  successCallback : {
			                     fn : function DesignerForm_onActionChangeType_failure(response) {
				                     Alfresco.util.PopupManager.displayMessage({
					                     text : this.msg("message.save-element.success")
				                     });
			                     },
			                     scope : this
			                  },
			                  failureCallback : {
			                     fn : function DesignerForm_onActionChangeType_failure(response) {
				                     Alfresco.util.PopupManager.displayMessage({
					                     text : this.msg("message.save-element.failure")
				                     });
			                     },
			                     scope : this
			                  }
			               });
	               	}

	               },

	               /**
						 * Handles applying the styling and node creation required
						 * when a element is dragged over a tree node.
						 * 
						 * @method onElementDragOver
						 * @property layer The name of the event
						 * @property args The event payload
						 */
	               onElementDragOver : function DesignerForm_onElementDragOver(layer, args) {
		               if (args && args[1] && args[1].elementId) {
			               // the node can be highlighted...
			               var dropTargetEl = Dom.get(args[1].elementId);
			               Dom.addClass(dropTargetEl, "elementDragOverHighlight");
		               }
	               },

	               /**
						 * Handles applying the styling and node deletion required
						 * when a document is dragged out of a tree node.
						 * 
						 * @method onElementDragOut
						 * @property layer The name of the event
						 * @property args The event payload
						 */
	               onElementDragOut : function DesignerForm_onElementDragOut(layer, args) {
		               if (args && args[1] && args[1].elementId) {
			               // the node can be highlighted...
			               var dropTargetEl = Dom.get(args[1].elementId);
			               Dom.removeClass(dropTargetEl, "elementDragOverHighlight");
		               }
	               },
	               /**
						 * Handles "dropTargetOwnerRequest" by determining whether or
						 * not the target belongs to the TreeView widget, and if it
						 * does determines it's nodeRef and uses the callback function
						 * with it.
						 * 
						 * @method onDropTargetOwnerRequest
						 * @property layer The name of the event
						 * @property args The event payload
						 */
	               onDropTargetOwnerRequest : function DesignerForm_onDropTargetOwnerRequest(layer, args) {
		               if (args && args[1] && args[1].elementId) {
			               var node = this.currentNode;
			               if (node != null) {
				               // Perform the drag out to clear the highlight...
				               this.onElementDragOut(layer, args);

				               var nodeRef = node.nodeRef;
				               var type = node.itemType;
				               if (node.subType != null) {
					               type = node.subType;
				               }

				               args[1].callback.call(args[1].scope, nodeRef, type, this.tree);
			               }
		               }
	               }

	            });
})();
