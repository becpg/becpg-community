
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
	   YAHOO.Bubbling.on("selectedModelChanged", this.onSelectedModelChanged, this);
	   
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
         /**
           * Current selected itemType.
           * 
           * @property itemType
           * @type string
           * @default ""
           */
          itemType: null,
          
          /**
           * Current selected nodeRef.
           * 
           * @property destination
           * @type string
           * @default ""
           */
          destination: null,
          
          /**
           * Current selected displayName.
           * 
           * @property displayName
           * @type string
           * @default ""
           */
          displayName: null,
          
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
            disabled: false,
            value: "publish"
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
    	  var templateUrl = Alfresco.constants.PROXY_URI + "becpg/designer/model/publish?nodeRef="+this.options.modelNodeRef;
    	  Alfresco.util.Ajax.request( {
    	              method : Alfresco.util.Ajax.POST,
    	              url: templateUrl,
    	              successMessage: this.msg("message.publish.success"),
    	              failureMessage: this.msg("message.publish.failure"),
    	              scope: this,
    	              execScripts: false
    	   });
    	  
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
            text: this.msg("message.confirm.delete", me.options.displayName),
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
    	  var me = this;
    	  var templateUrl = Alfresco.constants.PROXY_URI  + "slingshot/doclib/action/file/node/"+me.options.destination.replace("://","/");
    	  Alfresco.util.Ajax.request(
    	            {
    	            	
    	              method : Alfresco.util.Ajax.DELETE,
    	               url: templateUrl,
    	               successCallback:
    	               {
    	                  fn: function(){
    	                	  YAHOO.Bubbling.fire("elementDeleted",{nodeRef: me.options.destination});
                    		  
                    		  Alfresco.util.PopupManager.displayMessage({
                                         text:  this.msg("message.delete.success", me.options.displayName)
                              });
    	                  },
    	                  scope: this
    	               },
    	               failureMessage: this.msg("message.delete.failure", me.options.displayName),
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
          var  actionUrl = Alfresco.constants.PROXY_URI + "becpg/designer/create/element?nodeRef="+this.options.destination;

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

            // Always create a new instance
            this.modules.createElement = new Alfresco.module.SimpleDialog(this.id + "-createElement").setOptions(
            {
               width: "30em",
               templateUrl: Alfresco.constants.URL_SERVICECONTEXT + "/modules/model-designer/create-element?currentType="+this.options.itemType,
               actionUrl: actionUrl,
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
                          
                		  YAHOO.Bubbling.fire("elementCreated",{nodeRef: response.json.persistedObject});
                		  
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
          * @method onSelectedModelChanged
          */
         onSelectedModelChanged: function DesignerToolbar_onNodeClicked(layer, args)
         {
       	  var obj = args[1];
       	  
            var nodeRef = obj.nodeRef;
            	if(nodeRef!=null){
                 this.options.modelNodeRef = nodeRef;
            	} 
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
    	  
         var nodeRef = obj.nodeRef,
         	itemType = obj.itemType,
            label = obj.label;
         	if(nodeRef!=null){
              this.options.destination = nodeRef;
              this.options.itemType = itemType;
              this.options.displayName = label;
              this.widgets.newRowButton.set("disabled", false);
              this.widgets.deleteButton.set("disabled", false);
         	} else {
         	  this.widgets.newRowButton.set("disabled", true);
         	  this.widgets.deleteButton.set("disabled", true);
         	}
         	
       }
      
      
   }, true);
})();