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
 * DesignerAdminConsole tool component.
 * 
 * @namespace Extras
 * @class beCPG.component.DesignerAdminConsole
 */
(function() {

   /**
     * beCPGDesignerAdminConsole constructor.
     * 
     * @param {String}
     *            htmlId The HTML id of the parent element
     * @constructor
     */
   beCPG.component.DesignerAdminConsole = function(htmlId) {

      beCPG.component.DesignerAdminConsole.superclass.constructor.call(this, "beCPG.component.DesignerAdminConsole", htmlId, [
            "button", "menu", "container", "json" ]);

      return this;
   };

   YAHOO
         .extend(beCPG.component.DesignerAdminConsole, Alfresco.component.Base,
               {
                
                  /**
                     * Fired by YUI when parent element is available for
                     * scripting. Component initialisation, including
                     * instantiation of YUI widgets and event listener binding.
                     * 
                     * @method onReady
                     */
                  onReady : function DesignerAdminConsole_onReady() {

                     this.widgets.reloadModelButton = Alfresco.util.createYUIButton(this, "reload-model-button",
                           this.onReloadModelClick);
                     this.widgets.reloadConfigButton = Alfresco.util.createYUIButton(this, "reload-config-button",
                           this.onReloadConfigClick);
                 
                  },

                  /**
                     * YUI WIDGET EVENT HANDLERS Handlers for standard events
                     * fired from YUI widgets, e.g. "click"
                     */

                  /**
                     * Reload model click event handler
                     * 
                     * @method onReloadModelClick
                     * @param e
                     *            {object} DomEvent
                     * @param args
                     *            {array} Event parameters (depends on event
                     *            type)
                     */
                  onReloadModelClick : function DesignerAdminConsole_onReloadModelClick(e, args) {
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
                  onReloadModelSuccess : function DesignerAdminConsole_onReloadModelSuccess(response) {
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
                  onReloadModelFailure : function DesignerAdminConsole_onReloadModelFailure(response) {
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
                     *            {array} Event parameters (depends on event
                     *            type)
                     */
                  onReloadConfigClick : function DesignerAdminConsole_onReloadConfigClick(e, args) {
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
                  onReloadConfigSuccess : function DesignerAdminConsole_onReloadConfigSuccess(response) {
                     Alfresco.util.PopupManager.displayMessage({
                        text : this.msg("message.reload-config.success")
                     });
                     this.widgets.reloadConfigButton.set("disabled", false);
                  }
               });

})();
