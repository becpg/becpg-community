/**
 * beCPGAdminConsole tool component.
 * 
 * @namespace Extras
 * @class beCPG.component.beCPGAdminConsole
 */
(function() {

   /**
    * beCPGAdminConsole constructor.
    * 
    * @param {String}
    *            htmlId The HTML id of the parent element
    * @return {Extras.ConsoleCreateUsers} The new ConsoleCreateUsers instance
    * @constructor
    */
   beCPG.component.beCPGAdminConsole = function(htmlId) {
      this.name = "beCPGAdminConsole";
      beCPG.component.beCPGAdminConsole.superclass.constructor.call(this, htmlId);

      /* Register this component */
      Alfresco.util.ComponentManager.register(this);

      /* Load YUI Components */
      Alfresco.util.YUILoaderHelper.require([ "button", "container", "datasource", "datatable", "json", "history" ],
            this.onComponentsLoaded, this);

      /* Define panel handlers */
      var me = this;

      // NOTE: the panel registered first is considered the "default" view and
      // is displayed first

      /* Search Panel Handler */
      FormPanelHandler = function FormPanelHandler_constructor() {
         FormPanelHandler.superclass.constructor.call(this, "form");
      };

      YAHOO.extend(FormPanelHandler, Alfresco.ConsolePanelHandler, {
         /**
          * PANEL LIFECYCLE CALLBACKS
          */

         /**
          * Called by the ConsolePanelHandler when this panel shall be loaded
          * 
          * @method onLoad
          */
         onLoad : function onLoad() {
            // Buttons
            me.widgets.reloadModelButton = Alfresco.util.createYUIButton(me, "reload-model-button",
                  me.onReloadModelClick);
            me.widgets.reloadConfigButton = Alfresco.util.createYUIButton(me, "reload-config-button",
                  me.onReloadConfigClick);
            me.widgets.initRepoButton = Alfresco.util.createYUIButton(me, "init-repo-button",
                  me.onInitRepoClick);
            me.widgets.initAclButton = Alfresco.util.createYUIButton(me, "init-acl-button",
                  me.onInitAclClick);
            me.widgets.emptyCacheButton = Alfresco.util.createYUIButton(me, "empty-cache-button",
                  me.onEmptyCacheClick);
         },

         onShow : function onShow() {

         }
      });
      new FormPanelHandler();

      return this;
   };

   YAHOO.extend(beCPG.component.beCPGAdminConsole, Alfresco.ConsoleTool, {
      /**
       * Object container for initialization options
       * 
       * @property options
       * @type object
       */
      options : {

      },

      /**
       * Fired by YUI when parent element is available for scripting. Component initialisation, including instantiation
       * of YUI widgets and event listener binding.
       * 
       * @method onReady
       */
      onReady : function ConsolebeCPGAdmin_onReady() {
         // Call super-class onReady() method
         beCPG.component.beCPGAdminConsole.superclass.onReady.call(this);

         // Do stuff here
      },

      /**
       * YUI WIDGET EVENT HANDLERS Handlers for standard events fired from YUI widgets, e.g. "click"
       */

      /**
       * Reload model click event handler
       * 
       * @method onReloadModelClick
       * @param e
       *            {object} DomEvent
       * @param args
       *            {array} Event parameters (depends on event type)
       */
      onReloadModelClick : function ConsolebeCPGAdmin_onReloadModelClick(e, args) {
         // Disable the button temporarily
         this.widgets.reloadModelButton.set("disabled", true);

         Alfresco.util.Ajax.request({
            url : Alfresco.constants.PROXY_URI + "/becpg/admin/repository/reload-model",
            method : Alfresco.util.Ajax.GET,
            responseContentType : Alfresco.util.Ajax.JSON,
            successCallback : {
               fn : this.onReloadModelSuccess,
               scope : this
            },
            failureCallback : {
               fn : this.onReloadModelFailure,
               scope : this
            }
         });
      },

      /**
       * Reload model success handler
       * 
       * @method onReloadModelSuccess
       * @param response
       *            {object} Server response
       */
      onReloadModelSuccess : function ConsolebeCPGAdmin_onReloadModelSuccess(response) {
         Alfresco.util.PopupManager.displayMessage({
            text : this.msg("message.reload-model.success")
         });
         this.widgets.reloadModelButton.set("disabled", false);
      },
      /**
       * Reload model failure handler
       * 
       * @method onReloadModelFailure
       * @param response
       *            {object} Server response
       */
      onReloadModelFailure : function ConsolebeCPGAdmin_onReloadModelFailure(response) {
         if (response.json.message !== null) {
            Alfresco.util.PopupManager.displayPrompt({
               text : response.json.message
            });
         } else {
            Alfresco.util.PopupManager.displayMessage({
               text : this.msg("message.reload-model.failure")
            });
         }
         this.widgets.reloadModelButton.set("disabled", false);
      },

      /**
       * Reload config click event handler
       * 
       * @method onReloadConfigClick
       * @param e
       *            {object} DomEvent
       * @param args
       *            {array} Event parameters (depends on event type)
       */
      onReloadConfigClick : function ConsolebeCPGAdmin_onReloadConfigClick(e, args) {
         // Disable the button temporarily
         this.widgets.reloadConfigButton.set("disabled", true);

         Alfresco.util.Ajax.request({
            url : Alfresco.constants.URL_SERVICECONTEXT + "components/console/config/reload",
            method : Alfresco.util.Ajax.GET,
            responseContentType : Alfresco.util.Ajax.JSON,
            successCallback : {
               fn : this.onReloadConfigSuccess,
               scope : this
            },
            failureCallback : {
               fn : this.onReloadConfigFailure,
               scope : this
            }
         });
      },

      /**
       * Reload config success handler
       * 
       * @method onReloadConfigSuccess
       * @param response
       *            {object} Server response
       */
      onReloadConfigSuccess : function ConsolebeCPGAdmin_onReloadConfigSuccess(response) {
         Alfresco.util.PopupManager.displayMessage({
            text : this.msg("message.reload-config.success")
         });
         this.widgets.reloadConfigButton.set("disabled", false);
      },

      /**
       * Reload config failure handler
       * 
       * @method onReloadModelFailure
       * @param response
       *            {object} Server response
       */
      onReloadConfigFailure : function ConsolebeCPGAdmin_onReloadConfigFailure(response) {
         if (response.json.message !== null) {
            Alfresco.util.PopupManager.displayPrompt({
               text : response.json.message
            });
         } else {
            Alfresco.util.PopupManager.displayMessage({
               text : this.msg("message.reload-config.failure")
            });
         }
         this.widgets.reloadModelButton.set("disabled", false);
      },

      /**
       * Initialize repository click event handler
       * 
       * @method onInitRepoClick
       * @param e
       *            {object} DomEvent
       * @param args
       *            {array} Event parameters (depends on event type)
       */
      onInitRepoClick : function ConsolebeCPGAdmin_onInitRepoClick(e, args) {
         // Disable the button temporarily
         this.widgets.initRepoButton.set("disabled", true);

         Alfresco.util.Ajax.request({
            url : Alfresco.constants.PROXY_URI + "/becpg/admin/repository/init-repo",
            method : Alfresco.util.Ajax.GET,
            responseContentType : Alfresco.util.Ajax.JSON,
            successCallback : {
               fn : this.onInitRepoSuccess,
               scope : this
            },
            failureCallback : {
               fn : this.onInitRepoFailure,
               scope : this
            }
         });
      },

      /**
       * Init repo success handler
       * 
       * @method onInitRepoSuccess
       * @param response
       *            {object} Server response
       */
      onInitRepoSuccess : function ConsolebeCPGAdmin_onInitRepoSuccess(response) {
         Alfresco.util.PopupManager.displayMessage({
            text : this.msg("message.init-repo.success")
         });
         this.widgets.initRepoButton.set("disabled", false);
      },

      /**
       * Init repo failure handler
       * 
       * @method onInitRepoFailure
       * @param response
       *            {object} Server response
       */
      onInitRepoFailure : function ConsolebeCPGAdmin_onInitRepoFailure(response) {
         if (response.json.message !== null) {
            Alfresco.util.PopupManager.displayPrompt({
               text : response.json.message
            });
         } else {
            Alfresco.util.PopupManager.displayMessage({
               text : this.msg("message.init-repo.failure")
            });
         }
         this.widgets.initRepoButton.set("disabled", false);
      },
      /**
       * Initialize repository click event handler
       * 
       * @method onInitRepoClick
       * @param e
       *            {object} DomEvent
       * @param args
       *            {array} Event parameters (depends on event type)
       */
      onInitAclClick : function ConsolebeCPGAdmin_onInitAclClick(e, args) {
         // Disable the button temporarily
         this.widgets.initAclButton.set("disabled", true);

         Alfresco.util.Ajax.request({
            url : Alfresco.constants.PROXY_URI + "/becpg/admin/repository/reload-acl",
            method : Alfresco.util.Ajax.GET,
            responseContentType : Alfresco.util.Ajax.JSON,
            successCallback : {
               fn : this.onInitAclSuccess,
               scope : this
            },
            failureCallback : {
               fn : this.onInitAclFailure,
               scope : this
            }
         });
      },

      /**
       * Init repo success handler
       * 
       * @method onInitRepoSuccess
       * @param response
       *            {object} Server response
       */
      onInitAclSuccess : function ConsolebeCPGAdmin_onInitAclSuccess(response) {
         Alfresco.util.PopupManager.displayMessage({
            text : this.msg("message.init-acl.success")
         });
         this.widgets.initAclButton.set("disabled", false);
      },

      /**
       * Init repo failure handler
       * 
       * @method onInitRepoFailure
       * @param response
       *            {object} Server response
       */
      onInitAclFailure : function ConsolebeCPGAdmin_onInitAclFailure(response) {
         if (response.json.message !== null) {
            Alfresco.util.PopupManager.displayPrompt({
               text : response.json.message
            });
         } else {
            Alfresco.util.PopupManager.displayMessage({
               text : this.msg("message.init-acl.failure")
            });
         }
         this.widgets.initAclButton.set("disabled", false);
      },
      /**
       * Empty cache click event handler
       * 
       * @method onEmptyCacheClick
       * @param e
       *            {object} DomEvent
       * @param args
       *            {array} Event parameters (depends on event type)
       */
      onEmptyCacheClick : function ConsolebeCPGAdmin_onEmptyCacheClick(e, args) {
         // Disable the button temporarily
         this.widgets.emptyCacheButton.set("disabled", true);

         Alfresco.util.Ajax.request({
            url : Alfresco.constants.PROXY_URI + "/becpg/admin/repository/reload-cache",
            method : Alfresco.util.Ajax.GET,
            responseContentType : Alfresco.util.Ajax.JSON,
            successCallback : {
               fn : this.emptyCacheSuccess,
               scope : this
            },
            failureCallback : {
               fn : this.emptyCacheFailure,
               scope : this
            }
         });
      },

      /**
       * emptyCache success handler
       * 
       * @method emptyCacheRepoSuccess
       * @param response
       *            {object} Server response
       */
      emptyCacheSuccess : function ConsolebeCPGAdmin_emptyCacheRepoSuccess(response) {
         Alfresco.util.PopupManager.displayMessage({
            text : this.msg("message.empty-cache.success")
         });
         this.widgets.emptyCacheButton.set("disabled", false);
      },

      /**
       * emptyCache failure handler
       * 
       * @method emptyCacheFailure
       * @param response
       *            {object} Server response
       */
      emptyCacheFailure : function ConsolebeCPGAdmin_emptyCacheFailure(response) {
         if (response.json.message !== null) {
            Alfresco.util.PopupManager.displayPrompt({
               text : response.json.message
            });
         } else {
            Alfresco.util.PopupManager.displayMessage({
               text : this.msg("message.empty-cache.failure")
            });
         }
         this.widgets.emptyCacheButton.set("disabled", false);
      }
   });

})();
