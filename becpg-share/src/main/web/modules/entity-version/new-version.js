/*******************************************************************************
 *  Copyright (C) 2010-2014 beCPG. 
 *   
 *  This file is part of beCPG 
 *   
 *  beCPG is free software: you can redistribute it and/or modify 
 *  it under the terms of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation, either version 3 of the License, or 
 *  (at your option) any later version. 
 *   
 *  beCPG is distributed in the hope that it will be useful, 
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 *  GNU Lesser General Public License for more details. 
 *   
 *  You should have received a copy of the GNU Lesser General Public License along with beCPG.
 *   If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
/**

/**
 * NewEntityVersion component.
 *
 * Popups a YUI panel and lets the user choose version and comment for the new version.
 *
 * @namespace Alfresco.module
 * @class Alfresco.module.NewEntityVersion
 */
(function()
{
   /**
    * NewEntityVersion constructor.
    *
    * NewEntityVersion is considered a singleton so constructor should be treated as private,
    * please use Alfresco.module.getNewEntityVersionInstance() instead.
    *
    * @param {string} htmlId The HTML id of the parent element
    * @return {Alfresco.module.NewEntityVersion} The new NewEntityVersion instance
    * @constructor
    * @private
    */
   Alfresco.module.NewEntityVersion = function(containerId)
   {
      this.name = "Alfresco.module.NewEntityVersion";
      this.id = containerId;

      var instance = Alfresco.util.ComponentManager.get(this.id);
      if (instance !== null)
      {
         throw new Error("An instance of Alfresco.module.NewEntityVersion already exists.");
      }

      /* Register this component */
      Alfresco.util.ComponentManager.register(this);

      // Load YUI Components
      Alfresco.util.YUILoaderHelper.require(["button", "container", "datatable", "datasource"], this.onComponentsLoaded, this);

      return this;
   };

   Alfresco.module.NewEntityVersion.prototype =
   {

      /**
       * The default config for the gui state for the newVersion dialog.
       * The user can override these properties in the show() method.
       *
       * @property defaultShowConfig
       * @type object
       */
      defaultShowConfig:
      {
         nodeRef: null,
         filename: null,
         version: null,
         onNewEntityVersionComplete: null
      },

      /**
       * The merged result of the defaultShowConfig and the config passed in
       * to the show method.
       *
       * @property defaultShowConfig
       * @type object
       */
      showConfig: {},

      /**
       * Object container for storing YUI widget and HTMLElement instances.
       *
       * @property widgets
       * @type object
       */
      widgets: {},

      /**
       * Fired by YUILoaderHelper when required component script files have
       * been loaded into the browser.
       *
       * @method onComponentsLoaded
       */
      onComponentsLoaded: function RV_onComponentsLoaded()
      {
         // Shortcut for dummy instance
         if (this.id === null)
         {
            return;
         }
      },

      /**
       * Show can be called multiple times and will display the newVersion dialog
       * in different ways depending on the config parameter.
       *
       * @method show
       * @param config {object} describes how the dialog should be displayed
       * The config object is in the form of:
       * {
       *    nodeRef: {string},  // the nodeRef to version
       *    version: {string}   // the version
       * }
       */
      show: function RV_show(config)
      {
         // Merge the supplied config with default config and check mandatory properties
         this.showConfig = YAHOO.lang.merge(this.defaultShowConfig, config);
         if (this.showConfig.nodeRef === undefined &&
             this.showConfig.filename === undefined &&
             this.showConfig.version === undefined)
         {
             throw new Error("A nodeRef, filename and version must be provided");
         }
         // Check if the new version dialog has been showed before
         if (this.widgets.panel)
         {
            this._showPanel();
         }
         else
         {
            // If it hasn't load the gui (template) from the server
            Alfresco.util.Ajax.request(
            {
               url: Alfresco.constants.URL_SERVICECONTEXT + "modules/entity-version/new-version?htmlid=" + this.id,
               successCallback:
               {
                  fn: this.onTemplateLoaded,
                  scope: this
               },
               failureMessage: "Could not load html new version template",
               execScripts: true
            });
         }
      },

      /**
       * Called when the new version dialog html template has been returned from the server.
       * Creates the YIU gui objects such as the panel.
       *
       * @method onTemplateLoaded
       * @param response {object} a Alfresco.util.Ajax.request response object
       */
      onTemplateLoaded: function RV_onTemplateLoaded(response)
      {
         var Dom = YAHOO.util.Dom;

         // Inject the template from the XHR request into a new DIV element
         var containerDiv = document.createElement("div");
         containerDiv.innerHTML = response.serverResponse.responseText;

         // Create the panel from the HTML returned in the server reponse
         var dialogDiv = YAHOO.util.Dom.getFirstChild(containerDiv);
         this.widgets.panel = Alfresco.util.createYUIPanel(dialogDiv);

         // Save a reference to the HTMLElement displaying texts so we can alter the texts later
         this.widgets.headerText = Dom.get(this.id + "-header-span");

         // Save references to hidden fields so we can set them later
         this.widgets.nodeRef = Dom.get(this.id + "-nodeRef-hidden");
         this.widgets.version = Dom.get(this.id + "-version-hidden");

         // Save reference to version section elements so we can set its values later
         this.widgets.description = YAHOO.util.Dom.get(this.id + "-description-textarea");
         this.widgets.minorVersion = YAHOO.util.Dom.get(this.id + "-minorVersion-radioButton");

         // Create and save a reference to the buttons so we can alter them later
         this.widgets.okButton = Alfresco.util.createYUIButton(this, "ok-button", null,
         {
            type: "submit"
         });
         this.widgets.cancelButton = Alfresco.util.createYUIButton(this, "cancel-button", this.onCancelButtonClick);

         // Configure the forms runtime
         var form = new Alfresco.forms.Form(this.id + "-NewEntityVersion-form");
         this.widgets.form = form;

         // The ok button is the submit button, and it should be enabled when the form is ready
         //form.setShowSubmitStateDynamically(true, false);
         form.setSubmitElements(this.widgets.okButton);
         form.doBeforeFormSubmit =
         {
            fn: function()
            {
               this.widgets.okButton.set("disabled", true);
               this.widgets.cancelButton.set("disabled", true);
               this.widgets.panel.hide();
               this.widgets.feedbackMessage = Alfresco.util.PopupManager.displayMessage(
               {
                  text: Alfresco.util.message("message.creatingNewVersion", this.name,
                  {
                     "0": this.showConfig.filename
                  }),
                  spanClass: "wait",
                  displayTime: 0
               });
            },
            obj: null,
            scope: this
         };
         
         // Submit as an ajax submit (not leave the page), in json format
         form.setAJAXSubmit(true,
         {
            successCallback:
            {
               fn: this.onNewVersionSuccess,
               scope: this
            },
            failureCallback:
            {
               fn: this.onNewVersionFailure,
               scope: this
            }
         });
         form.setSubmitAsJSON(true);

         // We're in a popup, so need the tabbing fix
         form.applyTabFix();
         form.init();

         // Show panel
         this._showPanel();
      },

      /**
       * Called when a node has been successfully versioned
       *
       * @method onNewVersionSuccess
       */
      onNewVersionSuccess: function RV_onNewVersionSuccess(response)
      {
         // Hide the current message display
         this.widgets.feedbackMessage.destroy();

         // Tell the document list to refresh itself if present
         YAHOO.Bubbling.fire("newVersionCreated",
         {
            nodeRef: this.showConfig.nodeRef,
            filename: this.showConfig.filename,
            version: this.showConfig.version
         });         

         var objComplete =
         {
            successful: [{nodeRef: response.json.results[0].nodeRef, version: this.showConfig.version}]
         };

         var callback = this.showConfig.onNewEntityVersionComplete;
         if (callback && typeof callback.fn == "function")
         {
            // Call the onNewEntityVersionComplete callback in the correct scope
            callback.fn.call((typeof callback.scope == "object" ? callback.scope : this), objComplete, callback.obj);
         }
      },

      /**
       * Called when a node failed to be versioned
       * Informs the user.
       *
       * @method onNewVersionFailure
       */
      onNewVersionFailure: function RV_onNewVersionFailure(response)
      {
         // Hide the current message display
         this.widgets.feedbackMessage.destroy();

         // Make sure the ok button is enabled for next time
         this.widgets.okButton.set("disabled", false);

         // Inform user that new version was successfully created
         Alfresco.util.PopupManager.displayMessage(
         {
            text: Alfresco.util.message("message.failure", this.name, 
            {
               "0": this.showConfig.filename
            })
         });

      },

      /**
       * Fired when the user clicks the cancel button.
       * Closes the panel.
       *
       * @method onCancelButtonClick
       * @param event {object} a Button "click" event
       */
      onCancelButtonClick: function RV_onCancelButtonClick()
      {
         // Hide the panel
         this.widgets.panel.hide();

         // and make sure ok is enabed next time its showed
         this.widgets.okButton.set("disabled", false);

      },

      /**
       * Adjust the gui according to the config passed into the show method.
       *
       * @method _applyConfig
       * @private
       */
      _applyConfig: function RV__applyConfig()
      {
         var Dom = YAHOO.util.Dom;

         // Set the panel section
         var header = Alfresco.util.message("header.new-version", this.name,
        		 {
		 	 		"0": this.showConfig.filename
		 		 });
         this.widgets.headerText["innerHTML"] = header;

         // Display the version input form
         var versions = (this.showConfig.version || "1.0").split("."),
         majorVersion = parseInt(versions[0], 10),
         minorVersion = parseInt(versions[1], 10);
         Dom.get(this.id + "-minorVersion").innerHTML = Alfresco.util.message("label.minorVersion.more", this.name,
        		 {
        	 		"0": majorVersion + "." + (1 + minorVersion)
        		 });
         Dom.get(this.id + "-majorVersion").innerHTML = Alfresco.util.message("label.majorVersion.more", this.name,
        		 {
		 	 		"0": (1 + majorVersion) + ".0"
		 		 });

         this.widgets.cancelButton.set("disabled", false);

         // Set the hidden parameters
         this.widgets.nodeRef.value = this.showConfig.nodeRef;
         this.widgets.version.value = this.showConfig.version;
      },

      /**
       * Prepares the gui and shows the panel.
       *
       * @method _showPanel
       * @private
       */
      _showPanel: function RV__showPanel()
      {
         // Reset references and the gui before showing it
         this.widgets.description.value = "";
         this.widgets.minorVersion.checked = true;

         // Apply the config before it is showed
         this._applyConfig();

         // Show the new version panel
         this.widgets.panel.show();
      }
   };
})();

Alfresco.module.getNewEntityVersionInstance = function()
{
   var instanceId = "alfresco-NewEntityVersion-instance";
   return Alfresco.util.ComponentManager.get(instanceId) || new Alfresco.module.NewEntityVersion(instanceId);
};
