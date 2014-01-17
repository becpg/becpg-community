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
 * AdminConsole tool component.
 * 
 * @namespace Extras
 * @class beCPG.component.AdminConsole
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
   beCPG.component.AdminConsole = function(htmlId) {

      beCPG.component.AdminConsole.superclass.constructor.call(this, "beCPG.component.AdminConsole", htmlId, [
            "button", "menu", "container", "json" ]);

      // Chart google
      google.load('visualization', '1', {
         packages : [ 'gauge' ]
      });
      return this;
   };

   YAHOO
         .extend(beCPG.component.AdminConsole, Alfresco.component.Base,
               {
                  /**
                   * Object container for initialization options
                   * 
                   * @property options
                   * @type object
                   */
                  options : {
                     memory : 0
                  },

                  /**
                   * Fired by YUI when parent element is available for scripting. Component initialisation, including
                   * instantiation of YUI widgets and event listener binding.
                   * 
                   * @method onReady
                   */
                  onReady : function AdminConsole_onReady() {

                     // Call super-class onReady() method
                     this.widgets.reloadModelButton = Alfresco.util.createYUIButton(this, "reload-model-button",
                           this.onReloadModelClick);
                     this.widgets.reloadConfigButton = Alfresco.util.createYUIButton(this, "reload-config-button",
                           this.onReloadConfigClick);
                     this.widgets.initRepoButton = Alfresco.util.createYUIButton(this, "init-repo-button",
                           this.onInitRepoClick);
                     this.widgets.initAclButton = Alfresco.util.createYUIButton(this, "init-acl-button",
                           this.onInitAclClick);
                     this.widgets.emptyCacheButton = Alfresco.util.createYUIButton(this, "empty-cache-button",
                           this.onEmptyCacheClick);

                     this.widgets.showUsersButton = Alfresco.util.createYUIButton(this, "show-users-button",
                           this.onShowUsersClick);

                     var data = google.visualization.arrayToDataTable([ [ 'Label', 'Value' ],
                           [ this.msg("label.memory"), this.options.memory ] ]);

                     var options = {
                        width : 400,
                        height : 120,
                        redFrom : 90,
                        redTo : 100,
                        yellowFrom : 80,
                        yellowTo : 90,
                        greenFrom : 60,
                        greenTo : 80,
                        minorTicks : 5
                     };

                     var chart = new google.visualization.Gauge(document.getElementById(this.id + '-gauge-div'));
                     chart.draw(data, options);

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
                  onReloadModelClick : function AdminConsole_onReloadModelClick(e, args) {
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
                  onReloadModelSuccess : function AdminConsole_onReloadModelSuccess(response) {
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
                  onReloadModelFailure : function AdminConsole_onReloadModelFailure(response) {
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
                  onReloadConfigClick : function AdminConsole_onReloadConfigClick(e, args) {
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
                  onReloadConfigSuccess : function AdminConsole_onReloadConfigSuccess(response) {
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
                  onReloadConfigFailure : function AdminConsole_onReloadConfigFailure(response) {
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
                  onInitRepoClick : function AdminConsole_onInitRepoClick(e, args) {
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
                  onInitRepoSuccess : function AdminConsole_onInitRepoSuccess(response) {
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
                  onInitRepoFailure : function AdminConsole_onInitRepoFailure(response) {
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
                  onInitAclClick : function AdminConsole_onInitAclClick(e, args) {
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
                  onInitAclSuccess : function AdminConsole_onInitAclSuccess(response) {
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
                  onInitAclFailure : function AdminConsole_onInitAclFailure(response) {
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
                  onEmptyCacheClick : function AdminConsole_onEmptyCacheClick(e, args) {
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
                  emptyCacheSuccess : function AdminConsole_emptyCacheRepoSuccess(response) {
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
                  emptyCacheFailure : function AdminConsole_emptyCacheFailure(response) {
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
                  },

                  onShowUsersClick : function AdminConsole_onShowUsersClick(e, args) {

                     Alfresco.util.Ajax.request({
                        url : Alfresco.constants.PROXY_URI + "/becpg/admin/repository/show-users",
                        method : Alfresco.util.Ajax.GET,
                        responseContentType : Alfresco.util.Ajax.JSON,
                        successCallback : {
                           fn : function(response) {
                              if (response.json) {
                                 // Inject the template from the XHR request into a new DIV
                                 // element
                                 var containerDiv = document.createElement("div");

                                 var ret = '<div id="'+this.id+'-show-users-panel" class="about-share"><div class="bd"><ul class="users">';

                                 for (j in response.json.users) {
                                    var user = response.json.users[j];
                                       ret += "<li >";
                                       ret += '<span class="avatar" title="' + user.fullName + '">';
                                       ret += Alfresco.Share.userAvatar(user.username, 64);
                                       ret += '</span><span class="username" ><a id="yui-gen59" class="theme-color-1" tabindex="0" href="/share/page/user/'+user.username+'/profile">' + user.fullName +'</a></span></li>';
                                 }
                                 
                                 ret += "</ul></div></div>";
                                 
                                 containerDiv.innerHTML = ret;

                                 var panelDiv = Dom.getFirstChild(containerDiv);
                                 this.widgets.panel = Alfresco.util.createYUIPanel(panelDiv, { draggable: false, width:"25em" });
                                 
                                 this.widgets.panel.show();

                              }
                           },
                           scope : this
                        }
                     });
                  }
               });

})();
