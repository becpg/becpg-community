

/**
 * Entity Data Lists: EntityDataListToolbar component.
 * 
 * Displays a list of EntityDataListToolbar
 * 
 * @namespace beCPG
 * @class beCPG.component.EntityDataListToolbar
 */
(function()
{
   /**
    * YUI Library aliases
    */
   var Dom = YAHOO.util.Dom;


   /**
    * EntityDataListToolbar constructor.
    * 
    * @param htmlId {String} The HTML id of the parent element
    * @return {beCPG.component.EntityDataListToolbar} The new EntityDataListToolbar instance
    * @constructor
    */
   beCPG.component.EntityDataListToolbar = function(htmlId)
   {
		return beCPG.component.EntityDataListToolbar.superclass.constructor.call(this, htmlId);
   };
   
   /**
    * Extend from Alfresco.component.DataListToolbar
    */
   YAHOO.extend(beCPG.component.EntityDataListToolbar, Alfresco.component.DataListToolbar);

   /**
    * Augment prototype with DataListActions module, ensuring overwrite is enabled
    */
   YAHOO.lang.augmentProto(beCPG.component.EntityDataListToolbar,Alfresco.service.DataListActions, true);

   /**
    * Augment prototype with main class implementation, ensuring overwrite is enabled
    */
   YAHOO.lang.augmentObject(beCPG.component.EntityDataListToolbar.prototype,
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
           * Current entityNodeRef.
           * 
           * @property entityNodeRef
           * @type string
           * @default ""
           */
         entityNodeRef:"",
			
         /**
          * Current showFormulate.
          * 
          * @property showFormulate
          * @type boolean
          * @default ""
          */	
		 showFormulate:false,
			
         /**
          * Current showECO.
          * 
          * @property showECO
          * @type boolean
          * @default ""
          */	
		 showECO:false
      },

      /**
       * Fired by YUI when parent element is available for scripting.
       *
       * @method onReady
       */
      onReady: function EntityDataListToolbar_onReady()
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
         if(this.options.showFormulate){
			this.widgets.formulateButton = Alfresco.util.createYUIButton(this, "formulateButton", this.onFormulate,
	         {
	            disabled: false
	         });
         } else {
        	 Dom.setStyle(this.id+"-formulateButton","display","none");
         }
         
         //eco button
         if(this.options.showECO){
			this.widgets.ecoCalculateWUsedButton = Alfresco.util.createYUIButton(this, "ecoCalculateWUsedButton", this.onECOCalculateWUsed,
	         {
	            disabled: false
	         });
			this.widgets.ecoDoSimulationButton = Alfresco.util.createYUIButton(this, "ecoDoSimulationButton", this.onECODoSimulation,
	         {
	            disabled: false
	         });
			this.widgets.ecoApplyButton = Alfresco.util.createYUIButton(this, "ecoApplyButton", this.onECOApply,
	         {
	            disabled: false
	         });
         } else {
        	 Dom.setStyle(this.id+"-ecoCalculateWUsedButton","display","none");
        	 Dom.setStyle(this.id+"-ecoDoSimulationButton","display","none");
        	 Dom.setStyle(this.id+"-ecoApplyButton","display","none");
         }

		 //finish button
		 this.widgets.finishButton = Alfresco.util.createYUIButton(this, "finishButton", this.onFinish,
         {
            disabled: false
         });

         // DataList Actions module
         this.modules.actions = new Alfresco.module.DataListActions();

         // Reference to Data Grid component
         this.modules.dataGrid = Alfresco.util.ComponentManager.findFirst("beCPG.module.EntityDataGrid");

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
            itemType = datalistMeta.itemType,
            scope = this;

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
               YAHOO.Bubbling.fire("dataItemCreated",
               {
                  nodeRef: response.json.persistedObject							
               });

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
		},
	  /**
       * Formulate button click handler
       *
       * @method onFormulate
       * @param e {object} DomEvent
       * @param p_obj {object} Object passed back from addListener method
       */
      onFormulate: function EntityDataListToolbar_onFormulate(e, p_obj)
      {
    	  Alfresco.util.PopupManager.displayMessage(
         {
            text: this.msg("message.formulate.please-wait")
         });
         
    	  Alfresco.util.Ajax.request(
         {
            method: Alfresco.util.Ajax.GET,
            url: Alfresco.constants.PROXY_URI + "becpg/product/formulate/node/" + this.options.entityNodeRef.replace(":/", ""),
            successCallback:
            {
               fn: function EntityDataListToolbar_onFormulate_success(response)
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
               fn: function EntityDataListToolbar_onFormulate_failure(response)
               {
            	   if (response.message != null)
                   {
                      Alfresco.util.PopupManager.displayPrompt({
                         text: response.message
                      });
                   }
                   else
                   {
                      Alfresco.util.PopupManager.displayMessage({
                         text: this.msg("message.formulate.failure")
                      });
                   }
               },
               scope: this
            }
         });
		},
		
	  /**
       * ECOCalculateWUsed button click handler
       *
       * @method onECOCalculateWUsed
       * @param e {object} DomEvent
       * @param p_obj {object} Object passed back from addListener method
       */
      onECOCalculateWUsed: function EntityDataListToolbar_onECOCalculateWUsed(e, p_obj)
      {
    	  Alfresco.util.PopupManager.displayMessage(
         {
            text: this.msg("message.eco-calculate-wused.please-wait")
         });
         
    	  Alfresco.util.Ajax.request(
         {
            method: Alfresco.util.Ajax.GET,
            url: Alfresco.constants.PROXY_URI + "becpg/ecm/changeorder/" + this.options.entityNodeRef.replace(":/", "") + "/calculatewused",
            successCallback:
            {
               fn: function EntityDataListToolbar_onECOCalculateWUsed_success(response)
               {
                  Alfresco.util.PopupManager.displayMessage(
                  {
                     text: this.msg("message.eco-calculate-wused.success")
                  });
                  
               },               
               scope: this
            },
            failureCallback:
            {
               fn: function EntityDataListToolbar_onECOCalculateWUsed_failure(response)
               {
            	   if (response.message != null)
                   {
                      Alfresco.util.PopupManager.displayPrompt({
                         text: response.message
                      });
                   }
                   else
                   {
                      Alfresco.util.PopupManager.displayMessage({
                         text: this.msg("message.eco-calculate-wused.failure")
                      });
                   }
               },
               scope: this
            }
         });
		},		

	  /**
       * ECODoSimulation button click handler
       *
       * @method onECODoSimulation
       * @param e {object} DomEvent
       * @param p_obj {object} Object passed back from addListener method
       */
      onECODoSimulation: function EntityDataListToolbar_onECODoSimulation(e, p_obj)
      {
    	  Alfresco.util.PopupManager.displayMessage(
         {
            text: this.msg("message.eco-do-simulation.please-wait")
         });
         
    	  Alfresco.util.Ajax.request(
         {
            method: Alfresco.util.Ajax.GET,
            url: Alfresco.constants.PROXY_URI + "becpg/ecm/changeorder/" + this.options.entityNodeRef.replace(":/", "") + "/dosimulation",
            successCallback:
            {
               fn: function EntityDataListToolbar_onECODoSimulation_success(response)
               {
                  Alfresco.util.PopupManager.displayMessage(
                  {
                     text: this.msg("message.eco-do-simulation.success")
                  });
                  
               },               
               scope: this
            },
            failureCallback:
            {
               fn: function EntityDataListToolbar_onECODoSimulation_failure(response)
               {
            	   if (response.message != null)
                   {
                      Alfresco.util.PopupManager.displayPrompt({
                         text: response.message
                      });
                   }
                   else
                   {
                      Alfresco.util.PopupManager.displayMessage({
                         text: this.msg("message.eco-do-simulation.failure")
                      });
                   }
               },
               scope: this
            }
         });
		},	
		
	  /**
       * ECOApply button click handler
       *
       * @method onECOApply
       * @param e {object} DomEvent
       * @param p_obj {object} Object passed back from addListener method
       */
      onECOApply: function EntityDataListToolbar_onECOApply(e, p_obj)
      {
    	  Alfresco.util.PopupManager.displayMessage(
         {
            text: this.msg("message.eco-apply.please-wait")
         });
         
    	  Alfresco.util.Ajax.request(
         {
            method: Alfresco.util.Ajax.GET,
            url: Alfresco.constants.PROXY_URI + "becpg/ecm/changeorder/" + this.options.entityNodeRef.replace(":/", "") + "/apply",
            successCallback:
            {
               fn: function EntityDataListToolbar_onECOApply_success(response)
               {
                  Alfresco.util.PopupManager.displayMessage(
                  {
                     text: this.msg("message.eco-apply.success")
                  });
                  
               },               
               scope: this
            },
            failureCallback:
            {
               fn: function EntityDataListToolbar_onECOApply_failure(response)
               {
            	   if (response.message != null)
                   {
                      Alfresco.util.PopupManager.displayPrompt({
                         text: response.message
                      });
                   }
                   else
                   {
                      Alfresco.util.PopupManager.displayMessage({
                         text: this.msg("message.eco-apply.failure")
                      });
                   }
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
      onFinish: function EntityDataListToolbar_onFinish(e, p_obj)
      {
    	  Alfresco.util.PopupManager.displayMessage(
         {
            text: this.msg("message.generate-report.please-wait")
         });

         Alfresco.util.Ajax.request(
         {
            method: Alfresco.util.Ajax.GET,
            url: Alfresco.constants.PROXY_URI + "becpg/entity/generate-report/node/" + this.options.entityNodeRef.replace(":/", ""),				
            successCallback:
            {
               fn: function EntityDataListToolbar_onFinish_success(response)
               {
                  Alfresco.util.PopupManager.displayMessage(
                  {
                     text: this.msg("message.generate-report.success")
                  });

                  if(this.options.siteId != "")
                  {
                	 	window.location = Alfresco.constants.URL_PAGECONTEXT + "site/" + this.options.siteId + "/document-details?nodeRef=" + this.options.entityNodeRef;
                  }
                  else
            	  	{
                		window.location = Alfresco.constants.URL_PAGECONTEXT + "document-details?nodeRef=" + this.options.entityNodeRef;
            	  	}
                  
               },               
               scope: this
            },
            failureCallback:
            {
               fn: function EntityDataListToolbar_onFinish_failure(response)
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