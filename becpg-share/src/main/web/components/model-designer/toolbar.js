
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
         /**
           * Current selected itemType.
           * 
           * @property itemType
           * @type string
           * @default ""
           */
          itemType: null,
          
          /**
           * Current selected itemType.
           * 
           * @property itemType
           * @type string
           * @default ""
           */
          destination: null
      },

      typeList : { 
    	  "m2:model" : ["m2:imports","m2:namespaces","m2:dataTypes","m2:types,m2:aspects","m2:constraints"],
          "m2:type" :  ["m2:properties","m2:propertyOverrides","m2:associations"],
          "m2:aspect" :["m2:properties","m2:propertyOverrides","m2:associations"],
          "m2:property" :["m2:constraints"],
      	  "m2:constraint" : ["m2:parameters"]
      },
      
      /**
       * Fired by YUI when parent element is available for scripting.
       *
       * @method onReady
       */
      onReady: function DesignerToolbar_onReady()
      {
         this.widgets.newRowButton = Alfresco.util.createYUIButton(this, "newRowButton", this.onNewRow,
         {
            disabled: true,
            value: "create"
         });

         // Finally show the component body here to prevent UI artifacts on YUI button decoration
         Dom.setStyle(this.id + "-body", "visibility", "visible");
      },
      
      /**
       * New Row button click handler
       *
       * @method onNewRow
       * @param e {object} DomEvent
       * @param p_obj {object} Object passed back from addListener method
       */
      onNewRow: function DesignerToolbar_onNewRow(e, p_obj)
      {
    	 
    	  
    	  
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
         	itemType = obj.itemType;
         	if(nodeRef!=null){
              this.options.destination = nodeRef;
              this.options.itemType = itemType;
              this.widgets.newRowButton.setDisable(false);
         	} else {
         	  this.widgets.newRowButton.setDisable(true);
         	}
         	
       },
      
      /**
       * CreateNewItem
       *
       * @method createNewItem
       * @param itemType 
       * @param destination 
       */
      _createNewItem : function DesignerToolbar_createNewItem (itemType, destination) {
    	  
    	  var scope = this;

       // Intercept before dialog show
       var doBeforeDialogShow = function DataListToolbar_onNewRow_doBeforeDialogShow(p_form, p_dialog)
       {
          Alfresco.util.populateHTML(
             [ p_dialog.id + "-dialogTitle", this.msg("label.new-row.title") ],
             [ p_dialog.id + "-dialogHeader", this.msg("label.new-row.header") ]
          );
          
          // Is it a bulk action?
          if(Dom.get(p_dialog.id  + "-form-bulkAction"))
          {
         		Dom.get(p_dialog.id  + "-form-bulkAction").checked = this.bulkEdit;
         		Dom.get(p_dialog.id  + "-form-bulkAction-msg").innerHTML = this.msg("button.bulk-action-create");
         	}

       };
       
       var templateUrl = YAHOO.lang.substitute(Alfresco.constants.URL_SERVICECONTEXT + "components/form?bulkEdit=true&entityNodeRef={entityNodeRef}&itemKind={itemKind}&itemId={itemId}&destination={destination}&mode={mode}&submitType={submitType}&showCancelButton=true",
       {
          itemKind: "type",
          itemId: itemType,
          destination: destination,
          mode: "create",
          submitType: "json",
          entityNodeRef : this.options.entityNodeRef
       });

       
       // Using Forms Service, so always create new instance
       var createRow = new Alfresco.module.SimpleDialog(this.id + "-createRow");
       createRow.bulkEdit = false;
       createRow.setOptions(
       {
      	width: "33em",
          templateUrl: templateUrl,
          actionUrl: null,
          destroyOnHide: false,
          doBeforeDialogShow:
          {
             fn: doBeforeDialogShow,
             scope: this
          },
          onSuccess:
          {
          	 fn: function DataListToolbar_onNewRow_success(response)
         		 {
//        TODO     YAHOO.Bubbling.fire("dataItemCreated",
//             {
//                nodeRef: response.json.persistedObject							
//             });

             Alfresco.util.PopupManager.displayMessage(
             {
                text: this.msg("message.new-row.success")
             });
						
             //recall edit for next item
             var checkBoxEl =  Dom.get(this.id + "-createRow" + "-form-bulkAction");
             
	           	if ( checkBoxEl && checkBoxEl.checked)
	  		    {
	           		this.bulkEdit = true;
					scope.onNewRow();
	             } else {
	            	 this.bulkEdit = false;
	             }
	
          },
          scope: this
          },
          onFailure:
          {
          	fn: function DataListToolbar_onNewRow_failure(response)
          	{
		               Alfresco.util.PopupManager.displayMessage(
		               {
		                  text: this.msg("message.new-row.failure")
		               });
		         },
		        scope: this
          }
       }).show();
    	  
    	  
      }
      
      
   }, true);
})();