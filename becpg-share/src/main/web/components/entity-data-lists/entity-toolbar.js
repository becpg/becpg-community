

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
   	
   	beCPG.component.EntityDataListToolbar.superclass.constructor.call(this, "beCPG.component.EntityDataListToolbar", htmlId, ["button", "container"]);

		return this;
   };
   
   /**
    * Extend from Alfresco.component.Base
    */
   YAHOO.extend(beCPG.component.EntityDataListToolbar, Alfresco.component.Base);


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


        // Finally show the component body here to prevent UI artifacts on YUI button decoration
        Dom.setStyle(this.id + "-body", "visibility", "visible");
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
                  
                  YAHOO.Bubbling.fire("refreshDataGrids");
                  
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
            url: Alfresco.constants.PROXY_URI + "becpg/entity/generate-report/node/" + this.options.entityNodeRef.replace(":/", "") + "/check-datalists",				
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