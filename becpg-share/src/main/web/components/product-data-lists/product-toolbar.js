

/**
 * Product Data Lists: ProductDataListToolbar component.
 * 
 * Displays a list of ProductDataListToolbar
 * 
 * @namespace beCPG
 * @class beCPG.component.ProductDataListToolbar
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
    * ProductDataListToolbar constructor.
    * 
    * @param htmlId {String} The HTML id of the parent element
    * @return {beCPG.component.ProductDataListToolbar} The new ProductDataListToolbar instance
    * @constructor
    */
   beCPG.component.ProductDataListToolbar = function(htmlId)
   {
		return beCPG.component.ProductDataListToolbar.superclass.constructor.call(this, htmlId);
   }
   
   /**
    * Extend from Alfresco.component.DataListToolbar
    */
   YAHOO.extend(beCPG.component.ProductDataListToolbar, Alfresco.component.DataListToolbar);

   /**
    * Augment prototype with DataListActions module, ensuring overwrite is enabled
    */
   YAHOO.lang.augmentProto(beCPG.component.ProductDataListToolbar,Alfresco.service.DataListActions, true);

   /**
    * Augment prototype with main class implementation, ensuring overwrite is enabled
    */
   YAHOO.lang.augmentObject(beCPG.component.ProductDataListToolbar.prototype,
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
           * Current siteId.
           * 
           * @property siteId
           * @type string
           * @default ""
           */
        	siteId: "",
		 
		 	/**
           * Current productNodeRef.
           * 
           * @property productNodeRef
           * @type string
           * @default ""
           */
         productNodeRef:"",
			
			/**
           * bulk action
           *
           * @property bulkAction
           * @type boolean
           * @default false
           */
			bulkAction:"false"
      },

      /**
       * Fired by YUI when parent element is available for scripting.
       *
       * @method onReady
       */
      onReady: function ProductDataListToolbar_onReady()
      {
         this.widgets.newRowButton = Alfresco.util.createYUIButton(this, "newRowButton", this.onNewRow,
         {
            disabled: true,
            value: "create"
         });

         // Selected Items menu button
         this.widgets.selectedItems = Alfresco.util.createYUIButton(this, "selectedItems-button", this.onSelectedItems,
         {
            type: "menu", 
            menu: "selectedItems-menu",
            lazyloadmenu: false,
            disabled: true
         });

         this.widgets.printButton = Alfresco.util.createYUIButton(this, "printButton",
         {
            disabled: true
         });
         this.widgets.rssFeedButton = Alfresco.util.createYUIButton(this, "rssFeedButton",
         {
            disabled: true
         });
			
			//formulate button
			this.widgets.formulateButton = Alfresco.util.createYUIButton(this, "formulateButton", this.onFormulate,
         {
            disabled: false
         });

		 	//finish button
			this.widgets.finishButton = Alfresco.util.createYUIButton(this, "finishButton", this.onFinish,
         {
            disabled: false
         });

         // DataList Actions module
         this.modules.actions = new Alfresco.module.DataListActions();

         // Reference to Data Grid component
         this.modules.dataGrid = Alfresco.util.ComponentManager.findFirst("Alfresco.component.DataGrid");

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
      onNewRow: function DataListToolbar_onNewRow(e, p_obj)
      {
         var datalistMeta = this.modules.dataGrid.datalistMeta,
            destination = datalistMeta.nodeRef,
            itemType = datalistMeta.itemType;

         // Intercept before dialog show
         var doBeforeDialogShow = function DataListToolbar_onNewRow_doBeforeDialogShow(p_form, p_dialog)
         {
            Alfresco.util.populateHTML(
               [ p_dialog.id + "-dialogTitle", this.msg("label.new-row.title") ],
               [ p_dialog.id + "-dialogHeader", this.msg("label.new-row.header") ]
            );
         };
         
         var templateUrl = YAHOO.lang.substitute(Alfresco.constants.URL_SERVICECONTEXT + "components/form?itemKind={itemKind}&itemId={itemId}&destination={destination}&mode={mode}&submitType={submitType}&showCancelButton=true",
         {
            itemKind: "type",
            itemId: itemType,
            destination: destination,
            mode: "create",
            submitType: "json"
         });

         // Using Forms Service, so always create new instance
         var createRow = new beCPG.module.SimpleBulkDialog(this.id + "-createRow");

         createRow.setOptions(
         {
            width: "33em",
            templateUrl: templateUrl,
            actionUrl: null,
            destroyOnHide: true,
				bulkAction : this.bulkAction,
				bulkActionMessage : this.msg("button.bulk-action-create"),
            doBeforeDialogShow:
            {
               fn: doBeforeDialogShow,
               scope: this
            },
            onSuccess:
            {
               fn: function DataListToolbar_onNewRow_success(response)
               {
                  YAHOO.Bubbling.fire("dataItemCreated",
                  {
                     nodeRef: response.json.persistedObject
                  });

                  Alfresco.util.PopupManager.displayMessage(
                  {
                     text: this.msg("message.new-row.success")
                  });
               },
               scope: this
            },
				onBulkActionSuccess:
            {
               fn: function DataListToolbar_onNewRow_bulkActionsuccess(response)
               {
                  YAHOO.Bubbling.fire("dataItemCreated",
                  {
                     nodeRef: response.json.persistedObject							
                  });

                  Alfresco.util.PopupManager.displayMessage(
                  {
                     text: this.msg("message.new-row.success")
                  });
						
						//recall create for another item
						this.bulkAction = true;
						this.onNewRow();
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
      },

		/**
       * Formulate button click handler
       *
       * @method onFormulate
       * @param e {object} DomEvent
       * @param p_obj {object} Object passed back from addListener method
       */
      onFormulate: function ProductDataListToolbar_onFormulate(e, p_obj)
      {
    	  Alfresco.util.PopupManager.displayMessage(
         {
            text: this.msg("message.formulate.please-wait")
         });
         
    	  Alfresco.util.Ajax.request(
         {
            method: Alfresco.util.Ajax.GET,
            url: Alfresco.constants.PROXY_URI + "becpg/product/formulate/node/" + this.options.productNodeRef.replace(":/", ""),
            successCallback:
            {
               fn: function ProductDataListToolbar_onFormulate_success(response)
               {
                  Alfresco.util.PopupManager.displayMessage(
                  {
                     text: this.msg("message.formulate.success")
                  });
                  
               },               
               scope: this
            },
            failureCallback:
            {
               fn: function ProductDataListToolbar_onFormulate_failure(response)
               {
                  Alfresco.util.PopupManager.displayMessage(
                  {
                     text: this.msg("message.formulate.failure")
                  });
               },
               scope: this
            }
         });
		},

	  	/**
       * Finish button click handler
       *
       * @method onFinish
       * @param e {object} DomEvent
       * @param p_obj {object} Object passed back from addListener method
       */
      onFinish: function ProductDataListToolbar_onFinish(e, p_obj)
      {
    	  Alfresco.util.PopupManager.displayMessage(
         {
            text: this.msg("message.generate-report.please-wait")
         });

         Alfresco.util.Ajax.request(
         {
            method: Alfresco.util.Ajax.GET,
            url: Alfresco.constants.PROXY_URI + "becpg/product/generate-report/node/" + this.options.productNodeRef.replace(":/", ""),				
            successCallback:
            {
               fn: function ProductDataListToolbar_onFinish_success(response)
               {
                  Alfresco.util.PopupManager.displayMessage(
                  {
                     text: this.msg("message.generate-report.success")
                  });

                  if(this.options.siteId != "")
                  {
                	 	window.location = Alfresco.constants.URL_PAGECONTEXT + "site/" + this.options.siteId + "/document-details?nodeRef=" + this.options.productNodeRef;
                  }
                  else
            	  	{
                		window.location = Alfresco.constants.URL_PAGECONTEXT + "document-details?nodeRef=" + this.options.productNodeRef;
            	  	}
                  
               },               
               scope: this
            },
            failureCallback:
            {
               fn: function ProductDataListToolbar_onFinish_failure(response)
               {
                  Alfresco.util.PopupManager.displayMessage(
                  {
                     text: this.msg("message.generate-report.failure")
                  });
               },
               scope: this
            }
         });
      }


   }, true);
})();