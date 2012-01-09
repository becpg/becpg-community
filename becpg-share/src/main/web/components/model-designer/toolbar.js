
/**
 * Designer : Toolbar component.
 * 
 * Displays a list of Toolbar
 * 
 * @namespace beCPG
 * @class beCPG.component.DesignerToolbar
 */
(function()
{
   /**
    * YUI Library aliases
    */
   var Dom = YAHOO.util.Dom,
      Event = YAHOO.util.Event;

   /**
    * Toolbar constructor.
    * 
    * @param htmlId {String} The HTML id of the parent element
    * @return {beCPG.component.DesignerToolbar} The new Toolbar instance
    * @constructor
    */
   beCPG.component.DesignerToolbar = function(htmlId)
   {
	   beCPG.component.DesignerToolbar.superclass.constructor.call(this, "beCPG.component.DesignerToolbar", htmlId, ["button", "container"]);
      
	   YAHOO.Bubbling.on("designerModelNodeChange", this.onDesignerModelNodeChange, this);
	   
      return this;
   };
   
   /**
    * Extend from Alfresco.component.Base
    */
   YAHOO.extend(beCPG.component.DesignerToolbar, Alfresco.component.Base)
   
   
   /**
    * Augment prototype with main class implementation, ensuring overwrite is enabled
    */
   YAHOO.lang.augmentObject(beCPG.component.DesignerToolbar.prototype,
   {
      /**
       * Object container for initialization options
       *
       * @property options
       * @type object
       */
      options:
      {

      },

      /**
       * Current tree node
       */
      currentNode : null,
      /**
       * The current selected tree
       */
      tree : null,
      
      /**
       * Fired by YUI when parent element is available for scripting.
       *
       * @method onReady
       */
      onReady: function DesignerToolbar_onReady()
      {
         this.widgets.newRowButton = Alfresco.util.createYUIButton(this, "newRowButton", this.onCreateElement,
         {
            disabled: true,
            value: "create"
         });
         
         this.widgets.deleteButton = Alfresco.util.createYUIButton(this, "deleteButton", this.onDelete,
         {
            disabled: true,
            value: "delete"
         });
         
         this.widgets.publishButton = Alfresco.util.createYUIButton(this, "publishButton", this.onPublish,
         {
            disabled: true,
            value: "publish"
          });
         
         this.widgets.previewButton = Alfresco.util.createYUIButton(this, "previewButton", this.onPreview,
          {
             disabled: true,
             value: "preview"
          });

         // Finally show the component body here to prevent UI artifacts on YUI button decoration
         Dom.setStyle(this.id + "-body", "visibility", "visible");
      },
      /**
       * New Row button click handler
       *
       * @method onPublish
       * @param e {object} DomEvent
       * @param p_obj {object} Object passed back from addListener method
       */
      onPublish: function DesignerToolbar_onPublish(e, p_obj)
      { 
    	  var me = this;
    	  var templateUrl = Alfresco.constants.PROXY_URI + "becpg/designer/model/publish?nodeRef="+this.tree.modelNodeRef;
    	  Alfresco.util.Ajax.request( {
    	              method : Alfresco.util.Ajax.POST,
    	              url: templateUrl,
    	              successCallback:
	  		            {
	  		               fn: function(){
	     	            	  Alfresco.util.Ajax.request(
	     	            		         {
	     	            		            url: Alfresco.constants.URL_SERVICECONTEXT + "components/console/config/reload",
	     	            		            method: Alfresco.util.Ajax.GET,            
	     	            						responseContentType: Alfresco.util.Ajax.JSON,
	     	            					 successMessage: this.msg("message.publish.success"),
	     	            	    	         failureMessage: this.msg("message.publish.failure"),
	     	            		         });
	     	              },
	  		               scope: this
	  		            },
    	              failureMessage: this.msg("message.publish.failure"),
    	              scope: this,
    	              execScripts: false
    	   });
    	  
    	  
    	  
    	  
      },
      /**
       * Preview button click handler
       *
       * @method onPreview
       * @param e {object} DomEvent
       * @param p_obj {object} Object passed back from addListener method
       */
      onPreview: function onPreview(e, p_obj)
      { 
    	  
    	  var doBeforeDialogShow = function DataListToolbar_onNewRow_doBeforeDialogShow(p_form, p_dialog)
          {
             Alfresco.util.populateHTML(
                [ p_dialog.id + "-dialogTitle", this.msg("label.preview.title") ],
                [ p_dialog.id + "-dialogHeader", this.msg("label.preview.header") ]
             );
      
             if(Dom.get(p_dialog.id  + "-form-submit"))
    		 {
	   			Dom.setStyle(p_dialog.id  + "-form-submit", 'display', 'none');
    		  }
             

             if(Dom.get(p_dialog.id  + "-form-bulkAction"))
    		 {
	   			Dom.setStyle(p_dialog.id  + "-form-bulkAction", 'display', 'none');
	   			Dom.setStyle(p_dialog.id  + "-form-bulkAction-msg", 'display', 'none');
    		  }

             
          };

          var templateUrl = YAHOO.lang.substitute(Alfresco.constants.URL_SERVICECONTEXT + "components/form?bulkEdit=true&formId=create&itemKind={itemKind}&itemId={itemId}&mode={mode}&submitType={submitType}&showCancelButton=true&showSubmitButton=true",
          {
             itemKind: "type",
             itemId: this.currentNode.name, //TODO better
             mode: "create",
             submitType: "json"
          });

          // Using Forms Service, so always create new instance
          var preview = new Alfresco.module.SimpleDialog(this.id + "-preview");

          preview.setOptions(
          {
        	  width: "850px",
             templateUrl: templateUrl,
             destroyOnHide: true,
             doBeforeDialogShow:
             {
                fn: doBeforeDialogShow,
                scope: this
             }
          }).show();
    	  
      },
      /**
       * Delete record.
       *
       * @method onDelete
       * @param e {object} DomEvent
       * @param p_obj {object} Object passed back from addListener method
       */
      onDelete: function onDelete()
      {
         var me = this;
         
         Alfresco.util.PopupManager.displayPrompt(
         {
            title: this.msg("actions.delete"),
            text: this.msg("message.confirm.delete", me.currentNode.name),
            buttons: [
            {
               text: this.msg("button.delete"),
               handler: function DesignerToolbar_onActionDelete_delete()
               {
                  this.destroy();
                  me._onActionDeleteConfirm.call(me);
               }
            },
            {
               text: this.msg("button.cancel"),
               handler: function DesignerToolbar_onActionDelete_cancel()
               {
                  this.destroy();
               },
               isDefault: true
            }]
         });
      },

      /**
       * Delete record confirmed.
       *
       * @method _onActionDeleteConfirm
       * @param record {object} Object literal representing the file or folder to be actioned
       * @private
       */
      _onActionDeleteConfirm: function DesignerToolbar__onActionDeleteConfirm(record)
      {
    	  var me = this,
    	  	 label = me.currentNode.name;
    	  var templateUrl = Alfresco.constants.PROXY_URI  + "slingshot/doclib/action/file/node/"+me.currentNode.nodeRef.replace("://","/");
    	  Alfresco.util.Ajax.request(
    	            {
    	            	
    	              method : Alfresco.util.Ajax.DELETE,
    	               url: templateUrl,
    	               successCallback:
    	               {
    	                  fn: function(){
    	                	  YAHOO.Bubbling.fire("elementDeleted",{nodeRef: me.currentNode.nodeRef,tree : me.tree});
                    		  
                    		  Alfresco.util.PopupManager.displayMessage({
                                         text:  this.msg("message.delete.success", label)
                              });
    	                  },
    	                  scope: this
    	               },
    	               failureMessage: this.msg("message.delete.failure", label),
    	               scope: this,
    	               execScripts: false
    	            });
    	    
      },
      
      /**
       * onCreateElement button click handler
       *
       * @method onCreateElement
       * @param e {object} DomEvent
       * @param p_obj {object} Object passed back from addListener method
       */
      onCreateElement: function DesignerToolbar_onCreateElement(e, p_obj)
      { 
    	  
    	  var me = this,
    	  	  itemType = this.currentNode.itemType,
    	  	  assocType = null,
    	  	  templateUrl = null,
    	  	  actionUrl = Alfresco.constants.PROXY_URI + "becpg/designer/create/element?nodeRef="+this.currentNode.nodeRef;

          var doSetupFormsValidation = function DesignerToolbar_oACT_doSetupFormsValidation(p_form)
            {
               // Validation
               p_form.addValidation(this.id + "-createElement-assocType", function fnValidateType(field, args, event, form, silent, message)
               {
                  return field.options[field.selectedIndex].value !== "-";
               }, null, "change");
               p_form.addValidation(this.id + "-createElement-type", function fnValidateType(field, args, event, form, silent, message)
               {
                  return field.options[field.selectedIndex].value !== "-";
               }, null, "change");
               
               p_form.setShowSubmitStateDynamically(true, false);
            };

            
            if(this.currentNode.subType!=null){
            	itemType = this.currentNode.parentType;
            	assocType = this.currentNode.itemType;
            }
            
            templateUrl = Alfresco.constants.URL_SERVICECONTEXT + "modules/model-designer/create-element?currentType="+itemType;
            if(assocType!=null){
            	templateUrl+="&assocType="+assocType;
            }
            
            // Always create a new instance
            this.modules.createElement = new Alfresco.module.SimpleDialog(this.id + "-createElement").setOptions(
            {
               width: "30em",
               templateUrl: templateUrl,
               actionUrl: actionUrl,
               destroyOnHide : true,
               doSetupFormsValidation:
               {
                  fn: doSetupFormsValidation,
                  scope: this
               },
               firstFocus: this.id + "-createElement-type",
               onSuccess:
               {
                  fn: function DesignerToolbar_onActionChangeType_success(response)
                  {
                	  
                	  if (response.json && response.json.persistedObject)
                      {
                          
                		  var treeNode = response.json.treeNode;
                
                		  YAHOO.Bubbling.fire("elementCreated",{node:treeNode, focusNodeRef : response.json.persistedObject,tree : me.tree});
                		 
                		  
                		  Alfresco.util.PopupManager.displayMessage(
                                  {
                                     text: this.msg("message.create-element.success")
                                  });
                	  } else {
                		  Alfresco.util.PopupManager.displayMessage(
                                  {
                                     text: this.msg("message.create-element.failure")
                                  });
                	  }
                	  
                    
                  },
                  scope: this
               },
               onFailure:
               {
                  fn: function DesignerToolbar_onActionChangeType_failure(response)
                  {
                     Alfresco.util.PopupManager.displayMessage(
                     {
                        text: this.msg("message.create-element.failure")
                     });
                  },
                  scope: this
               }
            });
            this.modules.createElement.show();
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
    	  var obj = args[1],
    	  	  node = obj.node;
    	  
    	  this.tree = obj.tree;
    	  this.currentNode = node;
    	  
         	if(node.nodeRef!=null){
              this.widgets.newRowButton.set("disabled", false);
              this.widgets.deleteButton.set("disabled", false);
              if(node.subType!=null){
            	  this.widgets.deleteButton.set("disabled", true);
              }
              
              if(node.itemType=="m2:type" /* || node.itemType=="dsg:form" */){
            	  this.widgets.previewButton.set("disabled", false);
              } else {
            	  this.widgets.previewButton.set("disabled", true);
              }
              if(node.itemType=="m2:model" || node.itemType=="dsg:config"){
            	  this.widgets.publishButton.set("disabled", false);
              } else {
            	  this.widgets.publishButton.set("disabled", true);
              }
         	} else {
         	  this.widgets.newRowButton.set("disabled", true);
         	  this.widgets.deleteButton.set("disabled", true);
         	}
         	
       }
      
      
   }, true);
})();