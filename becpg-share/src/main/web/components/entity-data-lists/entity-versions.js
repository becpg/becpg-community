/**
 * Entity Version component.
 * 
 * @namespace beCPG
 * @class beCPG.component.EntityVersions
 */
(function() {
   /**
    * YUI Library aliases
    */
   var Dom = YAHOO.util.Dom, Event = YAHOO.util.Event;

   /**
    * Alfresco Slingshot aliases
    */
   var $html = Alfresco.util.encodeHTML;

   /**
    * EntityVersions constructor.
    * 
    * @param {String}
    *            htmlId The HTML id of the parent element
    * @return {beCPG.component.EntityVersions} The new component instance
    * @constructor
    */
   beCPG.component.EntityVersions = function EntityVersions_constructor(htmlId) {
      beCPG.component.EntityVersions.superclass.constructor.call(this, "beCPG.component.EntityVersions", htmlId, [
            "datasource", "datatable", "paginator", "history", "animation" ]);

      return this;
   };

   YAHOO
         .extend(
               beCPG.component.EntityVersions,
               Alfresco.component.Base,
               {
                  /**
                   * Object container for initialization options
                   * 
                   * @property options
                   * @type {object} object literal
                   */
                  options : {
                     /**
                      * Reference to the current document
                      * 
                      * @property nodeRef
                      * @type string
                      */
                     nodeRef : null,

                     /**
                      * Current siteId, if any.
                      * 
                      * @property siteId
                      * @type string
                      */
                     siteId : ""

                  },

                  /**
                   * A local cache of the document version response this is retrieved during set up & updated on each
                   * show
                   * 
                   * @property versions
                   * @type array
                   */
                  versions : [],

                  /**
                   * A reference to the earliest version. Set when creating the dropdown menu
                   * 
                   * @property earliestVersion
                   * @type object
                   */
                  earliestVersion : {},
                  /**
                   * The latest version of the document
                   * 
                   * @property latestVersion
                   * @type {Object}
                   */
                  latestVersion : null,

                  /**
                   * A cached copy of the version history to limit duplicate calls.
                   * 
                   * @property versionCache
                   * @type {Object} XHR response object
                   */
                  versionCache : null,

                  /**
                   * Fired by YUI when parent element is available for scripting
                   * 
                   * @method onReady
                   */
                  onReady : function EntityVersions_onReady() {
                     var containerElement = Dom.get(this.id + "-branches");
                     if (!containerElement) {
                        return;
                     }

                     var instance = this;
                     
                     this.widgets.alfrescoDataTable = new Alfresco.util.DataTable(
                           {
                              dataSource : {
                                 url : Alfresco.constants.PROXY_URI + "becpg/api/entity-version?mode=branches&nodeRef=" + this.options.nodeRef,
                                 doBeforeParseData : this.bind(function(oRequest, oFullResponse) {
                                    // Versions are returned in an array but must be placed in an object to be able to
                                    // be parse by yui
                                    // Also skip the first version since that is the current version
                                    instance.latestVersion = oFullResponse.versions.splice(0, 1)[0];

                                    // Cache the version data for other components (e.g. version-historicViewer)
                                    instance.versionCache = oFullResponse.versions;
                                    
                                    if(!instance.widgets.versionMenu){
                                       // Create menu button:
                                       instance.createMenu();
   
                                       // Set up Nav events:
                                       var navEls = Dom.getElementsByClassName("version-historic-nav", "a", instance.id);
                                       Event.addListener(navEls[0], "click", instance.onNavButtonClick, instance, true);
                                       Event.addListener(navEls[1], "click", instance.onNavButtonClick, instance, true);
                                       instance.updateNavState();
                                       
                                       Dom.removeClass(this.id + "-body", "hidden");
                                    }

                                    return ({
                                       "data" : oFullResponse.branches
                                    });
                                 })
                              },
                              dataTable : {
                                 container : this.id + "-branches",
                                 columnDefinitions : [ {
                                    key : "version",
                                    sortable : false,
                                    formatter : this.bind(this.renderCellVersion)
                                 } ],
                                 config : {
                                    MSG_EMPTY : this.msg("message.noVersions")
                                 }
                              }
                           });

                  },

                  /**
                   * Version renderer
                   * 
                   * @method renderCellVersion
                   */
                  renderCellVersion : function EntityVersions_renderCellVersions(elCell, oRecord, oColumn, oData) {
                     elCell.innerHTML = this.getDocumentVersionMarkup(oRecord.getData());
                  },


                  /**
                   * Builds and returns the markup for a version.
                   * 
                   * @method getDocumentVersionMarkup
                   * @param doc
                   *            {Object} The details for the document
                   */
                  getDocumentVersionMarkup : function EntityVersions_getDocumentVersionMarkup(doc) {
                     var compareURL =  Alfresco.constants.PROXY_URI + 'becpg/entity/compare/'
                     + this.options.nodeRef.replace(":/", "") + "/compare.pdf?entities="+doc.nodeRef, html = '', current = ( this.options.nodeRef == doc.nodeRef);

                     html += '<div class="entity-branches">';
                     html += '   <span class="document-version">' + $html(doc.label) + '</span>';
                     html += '   <span class="'+doc.metadata+(current ? " current":"")+'" ><a title="'+doc.description+'" href="'+beCPG.util.entityCharactURL(doc.siteId, doc.nodeRef, doc.itemType)+'">' + $html(doc.name) + '</a></span>';
                   if(!current) {
                     html += '   <span class="actions"><a href="' + compareURL + '" class="compare" title="' + this
                     .msg("label.compare") + '">&nbsp;</a></span>';
                   } else {
                      html += '  <span class="actions"><a href="#" name=".onVersionsGraphClick" rel="' + this.options.nodeRef + '" class="' + this.id + ' versions-graph" title="' + this
                      .msg("label.versionsGraph") + '">&nbsp;</a></span>';
                   }
                     html += '</div>';

                     return html;
                  },

                  
                  
       
                  /**
                  *
                  * Fired when the option in the dropdown version menu is changed
                  *
                  * @method onVersionMenuChange
                  *
                  */
                 onVersionMenuChange: function HPV_onVersionMenuChange(sType, aArgs, p_obj)
                 {
                    var eventTarget = aArgs[1],
                       newNodeRef = eventTarget.value;

                    // Update the display:
                    this.update(newNodeRef);
                 },

                 /**
                  *
                  * This function updates the display, menu and config with a new NodeRef.
                  *
                  * @method update
                  *
                  */
                 update: function HPV_update(newNodeRef)
                 {
                    if (newNodeRef) {

                       // Update Config Node Ref.
                       this.options.nodeRef = newNodeRef;

                       var filterObj =
                       {
                             filterId: "all"
                       };
         
                     if(this.latestVersion.nodeRef != newNodeRef){
                        filterObj =
                        {
                           filterOwner: "beCPG.component.EntityVersions",
                           filterId: "version",
                           filterData : newNodeRef
                        };
                     }
                      
                     YAHOO.Bubbling.fire("versionChangeFilter", filterObj);
                    
                      // Update the Menu
                      this.setMenuTitle();

                      // Update the Navigation
                      this.updateNavState();
                    }
                 },

                  /**
                   * Determines if the Next and Previous buttons should be enabled. Buttons are disabled by added a
                   * disabled class to them
                   * 
                   * @method updateNavState
                   */
                  updateNavState : function EntityVersions_updateNavState() {
                     var navEls = Dom.getElementsByClassName("version-historic-nav", "a", this.id + "-body");

                     // Start from a known state, default = enabled.
                     Dom.removeClass(navEls, "disabled");

                     if (this.options.nodeRef === this.earliestVersion.nodeRef) {
                        // At earliest, so disable the previous button
                        Dom.addClass(navEls[0], "disabled");
                     } else if (this.options.nodeRef === this.latestVersion.nodeRef) {
                        // at latest, so disable the next button.
                        Dom.addClass(navEls[1], "disabled");
                     }
                  },

                  /**
                   * Instantiates a YUI Menu Button & Writes the Menu HTML for it.
                   * 
                   * @method createMenu
                   * @param {HTMLElement}
                   *            The containing element for the Version History Dialogue
                   */

                  createMenu : function EntityVersions_createMenu() {
                     
                     var menuContainer = Dom.get(this.id + "-versionNav-menu"),
                     navContainer = Dom.getElementsByClassName("nav", "div", Dom.get(this.id + "-body"))[0], currentVersionHeader = document
                           .createElement("h6"), previousVersionHeader = document.createElement("h6"), currentTitle = Alfresco.util
                           .message("version-historic.menu.title", this.name, {
                              "0" : this.latestVersion.label
                           }), menuHTML = [];

                     // Write HTML for menu & add current version and option groups to menu:
                     menuHTML.push({
                        value : this.latestVersion.nodeRef,
                        text : currentTitle
                     });

                     // Add an option element for each of the previous versions
                     for ( var i in this.versionCache) {
                        var version = this.versionCache[i], title = Alfresco.util.message("version-historic.menu.title",
                              this.name, {
                                 "0" : version.label
                              });

                        // Check if this version is the earliest available
                        if (parseInt(i, 10) === this.versionCache.length - 1) {
                           this.earliestVersion = version;
                        }

                        menuHTML.push({
                           value : version.nodeRef,
                           text : title
                        });
                     }

                     for (var i = 0; i < menuHTML.length; i++) {
                        var option = document.createElement("option");
                        option.text = menuHTML[i].text;
                        option.value = menuHTML[i].value;
                        menuContainer.add(option);
                     }

                     // Instantiate the Menu
                     this.widgets.versionMenu = new Alfresco.util.createYUIButton(this, "versionNav-button",
                           this.onVersionMenuChange, {
                              type : "menu",
                              menu : menuContainer,
                              lazyloadmenu : false
                           });

                     // Set the menu title:
                     this.setMenuTitle(currentTitle);

                     var firstUL = Dom.getElementsByClassName("first-of-type", "ul", navContainer)[0], firstLI = Dom
                           .getElementsByClassName("first-of-type", "li", firstUL)[0];

                     // Inject item headers
                     currentVersionHeader.innerHTML = Alfresco.util.message("version-historic.menu.current",
                           this.name);
                     previousVersionHeader.innerHTML = Alfresco.util.message("version-historic.menu.previous",
                           this.name);

                     Dom.insertBefore(currentVersionHeader, firstLI);
                     Dom.insertAfter(previousVersionHeader, firstLI);
                  },

                  /**
                   * @method getVersion
                   * @param {string} -
                   *            either "previous", "next", or a version label
                   * @return {string} - nodeRef to the specified version
                   */
                  getVersionNodeRef : function EntityVersions_getVersionNodeRef(returnLabel) {
                     var visibleNodeRef = this.options.nodeRef, returnNodeRef, visibleIndex, returnIndex = null;

                     // Latest version isn't in the versions array, so default visibleIndex to -1 to allow the maths
                     // below to work
                     visibleIndex = -1;

                     // find the index of the version we're showing at the moment:
                     for (var i in this.versionCache) {
                        if (this.versionCache[i].nodeRef === visibleNodeRef) {
                           visibleIndex = i;
                        }
                        // While we're looping through, check to see if we were passed in a label.
                        if (this.versionCache[i].label === returnLabel) {
                           returnIndex = i;
                        }
                     }

                     if (returnLabel === this.latestVersion.label) {
                        return this.latestVersion.nodeRef;
                     }
                     // NOTE: the versions array has the most recent item first, so to navigate backwards in time, we
                     // need to increase the index.
                     else if (returnLabel === "next") {
                        returnIndex = parseInt(visibleIndex, 10) - 1;
                     } else if (returnLabel === "previous") {
                        returnIndex = parseInt(visibleIndex, 10) + 1;
                     }

                     // Treat current version specially: -1 = current version
                     if (returnIndex === -1) {
                        return this.latestVersion.nodeRef;
                     }

                     returnVersion = this.versionCache[returnIndex];
                     if (typeof (returnVersion) !== "undefined") {
                        returnNodeRef = returnVersion.nodeRef;
                        return returnNodeRef;
                     }
                  },

                  /**
                   * Triggered by the user clicking on the Next or Previous navigation buttons.
                   * 
                   * @method onNavButtonClick
                   */
                  onNavButtonClick : function EntityVersions_onNavButtonClick(event, p_obj) {
                     var target = Event.getTarget(event), dir = target.rel, newNodeRef = this.getVersionNodeRef(dir);

                     if (!Dom.hasClass(target, "disabled")) {
                        this.update(newNodeRef);
                     }
                     // prevent the default action.
                     Event.preventDefault(event);
                  },
                  
                  

                  /**
                   * Updates the title on the dropdown box with the current version number.
                   * 
                   * @method setMenuTitle
                   */
                  setMenuTitle : function EntityVersions_setMenuTitle(vTitle) {
                     var label = "", i = null;

                     // If the title hasn't been passed, we'll need to find it from the currentNodeRef.
                     if (!vTitle) {
                        if (this.options.nodeRef === this.latestVersion.nodeRef) {
                           label = this.latestVersion.label;

                           title = Alfresco.util.message("version-historic.menu.title.latest", this.name, {
                              "0" : label
                           });

                        } else {
                           for (i in this.versionCache) {
                              if (this.versionCache[i].nodeRef === this.options.nodeRef) {
                                 label = this.versionCache[i].label;
                              }
                           }

                           title = Alfresco.util.message("version-historic.menu.title", this.name, {
                              "0" : label
                           });

                        }
                     } else {
                        title = vTitle;
                     }

                     // Set the title.
                     this.widgets.versionMenu.set("label", title);

                  },
                  
                  /**
                   * Called when a "onViewHistoricPropertiesClick" link has been clicked for a version. Will display the
                   * Properties dialogue for that version.
                   * 
                   * @method onViewHistoricPropertiesClick
                   * @param version
                   */
                  onVersionsGraphClick : function EntityVersions_onVersionsGraphClick(nodeRef) {

                     beCPG.module.getVersionsGraphInstance().show({
                        nodeRef : nodeRef
                     });

                  },

               });
})();
