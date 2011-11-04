/**
 * SimpleBulkDialog module.
 * 
 * @namespace beCPG.module
 * @class beCPG.module.SimpleBulkDialog
 */
(function()
{
   var Dom = YAHOO.util.Dom,
      Selector = YAHOO.util.Selector,
      KeyListener = YAHOO.util.KeyListener;

	/**
    * SimpleBulkDialog constructor.
    * 
    * @param htmlId {String} The HTML id of the parent element
    * @return {beCPG.module.SimpleBulkDialog} The new SimpleBulkDialog instance
    * @constructor
    */
   beCPG.module.SimpleBulkDialog = function(htmlId, components)
   {
		return beCPG.module.SimpleBulkDialog.superclass.constructor.call(this, htmlId, components);
   }      

   YAHOO.extend(beCPG.module.SimpleBulkDialog, Alfresco.module.SimpleDialog,
   {      

       /**
        * Object container for initialization options
        */
       options:
       {
          /**
           * URL which will return template body HTML
           *
           * @property templateUrl
           * @type string
           * @default null
           */
          templateUrl: null,

          /**
           * URL of the form action
           *
           * @property actionUrl
           * @type string
           * @default null
           */
          actionUrl: null,

          /**
           * ID of form element to receive focus on show
           *
           * @property firstFocus
           * @type string
           * @default null
           */
          firstFocus: null,

          /**
           * Object literal representing callback upon successful operation.
           *   fn: function, // The handler to call when the event fires.
           *   obj: object, // An object to pass back to the handler.
           *   scope: object // The object to use for the scope of the handler.
           *
           * @property onSuccess
           * @type object
           * @default null
           */
          onSuccess:
          {
             fn: null,
             obj: null,
             scope: window
          },

          /**
           * Message to display on successful operation
           *
           * @property onSuccessMessage
           * @type string
           * @default ""
           */
          onSuccessMessage: "",

			/**
           * Object literal representing callback upon successful operation when bulkAction is checked.

           *   fn: function, // The handler to call when the event fires.
           *   obj: object, // An object to pass back to the handler.
           *   scope: object // The object to use for the scope of the handler.
           *
           * @property onBulkActionSuccess

           * @type object
           * @default null
           */
          onBulkActionSuccess:
          {
             fn: null,
             obj: null,
             scope: window
          },
			
			/**
           * Check bulkAction button
           *
           * @property bulkAction
           * @type boolean
           * @default false
           */
          bulkAction: false,
          
          /**
           * forceBulkMode
           * @property forceBulkMode
           * @type boolean
           * @default false
           */
          forceBulkMode : false,
			/**

           * Message to display on check box
           *
           * @property onFailureMessage
           * @type string
           * @default ""

           */
          bulkActionMessage: "",

          /**
           * Object literal representing callback upon failed operation.
           *   fn: function, // The handler to call when the event fires.
           *   obj: object, // An object to pass back to the handler.
           *   scope: object // The object to use for the scope of the handler.
           *
           * @property onFailure
           * @type object
           * @default null
           */
          onFailure:
          {
             fn: null,
             obj: null,
             scope: window
          },

          /**
           * Message to display on failed operation
           *
           * @property onFailureMessage
           * @type string
           * @default ""
           */
          onFailureMessage: "",
          
          /**
           * Object literal representing function to intercept dialog just before shown.
           *   fn: function(formsRuntime, Alfresco.module.SimpleDialog), // The handler to call when the event fires.
           *   obj: object, // An object to pass back to the handler.
           *   scope: object // The object to use for the scope of the handler. SimpleDialog instance if unset.
           *
           * @property doBeforeDialogShow
           * @type object
           * @default null
           */
          doBeforeDialogShow:
          {
             fn: null,
             obj: null,
             scope: null
          },
          
          /**
           * Object literal representing function to set forms validation.
           *   fn: function, // The handler to call when the event fires.
           *   obj: object, // An object to pass back to the handler.
           *   scope: object // The object to use for the scope of the handler. SimpleDialog instance if unset.
           *
           * @property doSetupFormsValidation
           * @type object
           * @default null
           */
          doSetupFormsValidation:
          {
             fn: null,
             obj: null,
             scope: null
          },
          
          /**
           * Object literal representing function to intercept form before submit.
           *   fn: function, // The override function.
           *   obj: object, // An object to pass back to the function.
           *   scope: object // The object to use for the scope of the function.
           *
           * @property doBeforeFormSubmit
           * @type object
           * @default null
           */
          doBeforeFormSubmit:
          {
             fn: null,
             obj: null,
             scope: window
          },
          
          /**
           * Object literal containing the abstract function for intercepting AJAX form submission.
           *   fn: function, // The override function.
           *   obj: object, // An object to pass back to the function.
           *   scope: object // The object to use for the scope of the function.
           * 
           * @property doBeforeAjaxRequest
           * @type object
           * @default null
           */
          doBeforeAjaxRequest:
          {
             fn: null,
             obj: null,
             scope: window
          },
          
          /**
           * Width for the dialog
           *
           * @property width
           * @type integer
           * @default 30em
           */
          width: "30em",
          
          /**
           * Clear the form before showing it?
           *
           * @property: clearForm
           * @type: boolean
           * @default: false
           */
          clearForm: false,
          
          /**
           * Destroy the dialog instead of hiding it?
           *
           * @property destroyOnHide
           * @type boolean
           * @default false
           */
          destroyOnHide: false
       },

      /**
       * Event callback when dialog template has been loaded
       *
       * @method onTemplateLoaded
       * @param response {object} Server response from load template XHR request
       */
      onTemplateLoaded: function AmSD_onTemplateLoaded(response)
      {
         // Inject the template from the XHR request into a new DIV element
         var containerDiv = document.createElement("div");
         containerDiv.innerHTML = response.serverResponse.responseText;

         // The panel is created from the HTML returned in the XHR request, not the container
         var dialogDiv = Dom.getFirstChild(containerDiv);
         while (dialogDiv && dialogDiv.tagName.toLowerCase() != "div")
         {
            dialogDiv = Dom.getNextSibling(dialogDiv);
         }

         // Create and render the YUI dialog
         this.dialog = Alfresco.util.createYUIPanel(dialogDiv,
         {
            width: this.options.width
         });

         // Hook close button
         this.dialog.hideEvent.subscribe(this.onHideEvent, null, this);

			// Is it a bulk action?
			if(Dom.get(this.id + "-form-bulkAction"))
			{
				Dom.get(this.id + "-form-bulkAction").checked = this.options.bulkAction;
				Dom.get(this.id + "-form-bulkAction-msg").innerHTML = this.options.bulkActionMessage;
			}

         // Are we controlling a Forms Service-supplied form?
         if (Dom.get(this.id + "-form-submit"))
         {
            this.isFormOwner = false;
            // FormUI component will initialise form, so we'll continue processing later
            this.formsServiceDeferred.fulfil("onTemplateLoaded");
         }
         else
         {
            // OK button needs to be "submit" type
            this.widgets.okButton = Alfresco.util.createYUIButton(this, "ok", null,
            {
               type: "submit"
            });

            // Cancel button
            this.widgets.cancelButton = Alfresco.util.createYUIButton(this, "cancel", this.onCancel);

            // Form definition
            this.isFormOwner = true;
            this.form = new Alfresco.forms.Form(this.id + "-form");
            this.form.setSubmitElements(this.widgets.okButton);
            this.form.setAJAXSubmit(true,
            {
               successCallback:
               {
                  fn: this.onSuccess,
                  scope: this
               },
               failureCallback:
               {
                  fn: this.onFailure,
                  scope: this
               }
            });
            this.form.setSubmitAsJSON(true);
            this.form.setShowSubmitStateDynamically(true, false);

            // Initialise the form
            this.form.init();

            this._showDialog();
         }
      },

		/**
       * Successful data webscript call event handler
       *
       * @method onSuccess
       * @param response {object} Server response object
       */
      onSuccess: function AmSD_onSuccess(response)
      {
         this.dialog.hide();

         if (!response)
         {
            // Invoke the callback if one was supplied
            if (this.options.onFailure && typeof this.options.onFailure.fn == "function")
            {
               this.options.onFailure.fn.call(this.options.onFailure.scope, null, this.options.onFailure.obj);
            }
            else
            {
               Alfresco.util.PopupManager.displayMessage(
               {
                  text: this.options.failureMessage || "Operation failed."
               });
            }
         }
         else
         {
				//bulk action is checked and callback was supplied ?
			if (this.options.forceBulkMode  || (Dom.get(this.id + "-form-bulkAction") && Dom.get(this.id + "-form-bulkAction").checked) && this.options.onBulkActionSuccess && typeof this.options.onBulkActionSuccess.fn == "function")
		    {
					this.options.onBulkActionSuccess.fn.call(this.options.onBulkActionSuccess.scope, response, this.options.onBulkActionSuccess.obj);
		      }
				// Invoke the callback if one was supplied
				else if (this.options.onSuccess && typeof this.options.onSuccess.fn == "function")
	         {
	            this.options.onSuccess.fn.call(this.options.onSuccess.scope, response, this.options.onSuccess.obj);
	         }
	         else
	         {
	            Alfresco.util.PopupManager.displayMessage(
	            {
	               text: this.options.successMessage || "Operation succeeded."
	            });
	         }
         }
      }

   });

})();
