
/**
 * Rules "VersionCleaner" Action module.
 *
 * @namespace Alfresco.module
 * @class Alfresco.module.RulesVersionCleanerAction
 */
(function()
{
   /**
   * YUI Library aliases
   */
   var Dom = YAHOO.util.Dom,
      KeyListener = YAHOO.util.KeyListener;

   /**
    * Alfresco Slingshot aliases
    */

   Alfresco.module.RulesVersionCleanerAction = function(htmlId)
   {
      Alfresco.module.RulesVersionCleanerAction.superclass.constructor.call(this, "Alfresco.module.RulesVersionCleanerAction", htmlId, ["button", "container", "connection"]);
      return this;
   };

   YAHOO.extend(Alfresco.module.RulesVersionCleanerAction, Alfresco.component.Base,
   {
      /**
       * Object container for initialization options
       */
      options:
      {
         /**
          * Template URL
          *
          * @property templateUrl
          * @type string
          * @default Alfresco.constants.URL_SERVICECONTEXT + "modules/rules/actions/versioncleaner"
          */
         templateUrl: Alfresco.constants.URL_SERVICECONTEXT + "modules/rules/actions/versioncleaner"
      },


      /**
       * Container element for template in DOM.
       *
       * @property containerDiv
       * @type HTMLElement
       */
      containerDiv: null,

      /**
       * Main entry point
       * @method showDialog
       * @param vcleanerConfig {object} Data to fill the form with
       *        vcleanerConfig.version {string} ["minor"|"version"|null]
       *        vcleanerConfig.comments {string}
       */
      showDialog: function RCIA_showDialog(vcleanerConfig)
      {
         if (!this.containerDiv)
         {
            // Load the UI template from the server
            Alfresco.util.Ajax.request(
            {
               url: this.options.templateUrl,
               dataObj:
               {
                  htmlid: this.id
               },
               successCallback:
               {
                  fn: this.onTemplateLoaded,
                  obj: vcleanerConfig,
                  scope: this
               },
               failureMessage: "Could not load template:" + this.options.templateUrl,
               execScripts: true
            });
         }
         else
         {
            // Show the dialog
            this._showDialog(vcleanerConfig);
         }
      },

      /**
       * Event callback when dialog template has been loaded
       *
       * @method onTemplateLoaded
       * @param response {object} Server response from load template XHR request
       * @param vcleanerConfig {object} Data to fill the form with
       */
      onTemplateLoaded: function RCIA_onTemplateLoaded(response, vcleanerConfig)
      {
         // Inject the template from the XHR request into a new DIV element
         this.containerDiv = document.createElement("div");
         this.containerDiv.setAttribute("style", "display:none");
         this.containerDiv.innerHTML = response.serverResponse.responseText;

         // The panel is created from the HTML returned in the XHR request, not the container
         var dialogDiv = Dom.getFirstChild(this.containerDiv);

         // Create and render the YUI dialog
         this.widgets.dialog = Alfresco.util.createYUIPanel(dialogDiv);

         // Buttons (note: ok buttons click will be handled in forms onBeforeAjaxSubmit)
         this.widgets.okButton = Alfresco.util.createYUIButton(this, "ok-button", null,
         {
            type: "submit"
         });
         this.widgets.cancelButton = Alfresco.util.createYUIButton(this, "cancel-button", this.onCancelClick);

         // Configure the forms runtime
         var form = new Alfresco.forms.Form(this.id + "-form");
         this.widgets.form = form;
         

         // ...and has a maximum length
         form.addValidation(this.id + "-number-by-day", Alfresco.forms.validation.number,null, "keyup");
         form.addValidation(this.id + "-number-of-day", Alfresco.forms.validation.number,null, "keyup");
         form.addValidation(this.id + "-number-of-version", Alfresco.forms.validation.number,null, "keyup");

         form.setSubmitElements(this.widgets.okButton);

         // Stop the form from being submitted and fire and event from the collected information
         form.doBeforeAjaxRequest =
         {
            fn: function(p_config, p_obj)
            {
               // Fire event so other component know
               YAHOO.Bubbling.fire("vcleanerConfigCompleted",
               {
                  options:
                  {
                     versionType: p_config.dataObj.versionType,
                     numberByDay: p_config.dataObj.numberByDay,
                     numberOfDay: p_config.dataObj.numberOfDay,
                     numberOfVersion: p_config.dataObj.numberOfVersion
                  },
                  eventGroup: this
               });

               this.widgets.dialog.hide();

               // Return false so the form isn't submitted
               return false;
            },
            obj: null,
            scope: this
         };

         // We're in a popup, so need the tabbing fix
         form.applyTabFix();
         form.init();

         // Register the ESC key to close the dialog
         var escapeListener = new KeyListener(document,
         {
            keys: KeyListener.KEY.ESCAPE
         },
         {
            fn: function(id, keyEvent)
            {
               this.onCancelClick();
            },
            scope: this,
            correctScope: true
         });
         escapeListener.enable();

         // Show the dialog
         this._showDialog(vcleanerConfig);
      },

      /**
       * Internal show dialog function
       *
       * @method _showDialog
       * @param vcleanerConfig {object} Data to fill the form with
       */
      _showDialog: function RCIA__showDialog(vcleanerConfig)
      {
         // Display form data from config
         vcleanerConfig = vcleanerConfig ? vcleanerConfig : {};
         var majorEl = Dom.get(this.id + "-version-type-major"),
            minorEl = Dom.get(this.id + "-version-type-minor"),
            allEl = Dom.get(this.id + "-version-type-all"),
            focusEl;

         if (vcleanerConfig.versionType == "minor" )
         {
            minorEl.checked = true;
            focusEl = minorEl;
            majorEl.checked = false;
            allEl.checked = false;
         }
         else if (vcleanerConfig.versionType == "major")
         {
            majorEl.checked = true;
            focusEl = majorEl;
            minorEl.checked = false;
            allEl.checked = false;
         } else {
            allEl.checked = true;
            focusEl = allEl;
            majorEl.checked = false;
            minorEl.checked = false;
         } 
         
         Dom.get(this.id + "-number-by-day").value = vcleanerConfig.numberByDay ? vcleanerConfig.numberByDay : "";
         Dom.get(this.id + "-number-of-day").value = vcleanerConfig.numberOfDay ? vcleanerConfig.numberOfDay : "";
         Dom.get(this.id + "-number-of-version").value = vcleanerConfig.numberOfVersion ? vcleanerConfig.numberOfVersion : "";
         
         
         this.widgets.form.validate();

         // Show the dialog
         this.widgets.dialog.show();

         // Focus when element is visible so IE is happy
         focusEl.focus();
      },

      /**
       * YUI WIDGET EVENT HANDLERS
       * Handlers for standard events fired from YUI widgets, e.g. "click"
       */

      /**
       * Dialog Cancel button event handler
       *
       * @method onCancelClick
       * @param e {object} DomEvent
       * @param p_obj {object} Object passed back from addListener method
       */
      onCancelClick: function RCIA_onCancelClick(e, p_obj)
      {
         this.widgets.dialog.hide();
      }

   });

   /* Dummy instance to load optional YUI components early */
   var dummyInstance = new Alfresco.module.RulesVersionCleanerAction("null");
})();
