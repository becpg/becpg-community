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
(function() {

   /**
    * YUI Library aliases
    */
   var Dom = YAHOO.util.Dom, Event = YAHOO.util.Event, Element = YAHOO.util.Element;

   /**
    * Alfresco Slingshot aliases
    */
   var $html = Alfresco.util.encodeHTML, $siteURL = Alfresco.util.siteURL;

   // Define constructor...
   beCPG.custom.DocListToolbar = function CustomDocListToolbar_constructor(htmlId) {
      beCPG.custom.DocListToolbar.superclass.constructor.call(this, htmlId);
      return this;
   };

   // Extend default DocListToolbar...
   YAHOO
         .extend(
               beCPG.custom.DocListToolbar,
               Alfresco.DocListToolbar,
               {
                  entityLinkCustomClass : null,

                  onReady : function CustomDLTB_onReady() {
                     if (Dom.get(this.id + "-tb-body") != null) {
                        // Create Content menu button
                        if (Dom.get(this.id + "-createContent-button")) {
                           // Create menu button that
                           this.widgets.createContent = Alfresco.util.createYUIButton(this, "createContent-button",
                                 this.onCreateContent, {
                                    type : "menu",
                                    menu : "createContent-menu",
                                    lazyloadmenu : false,
                                    disabled : true,
                                    value : "CreateChildren"
                                 });

                           // Make sure we load sub menu lazily with data on each click
                           var createContentMenu = this.widgets.createContent.getMenu(), groupIndex = 0;

                           // Create content actions
                           if (this.options.createContentActions.length !== 0) {
                              var menuItems = [], menuItem, content, url, config, html, li;
                              for (var i = 0; i < this.options.createContentActions.length; i++) {
                                 // Create menu item from config
                                 content = this.options.createContentActions[i];
                                 config = {
                                    parent : createContentMenu
                                 };
                                 url = null;

                                 // Check config type
                                 if (content.type == "javascript") {
                                    config.onclick = {
                                       fn : function(eventName, eventArgs, obj) {
                                          // Copy node so we can safely pass it to an action
                                          var node = Alfresco.util.deepCopy(this.doclistMetadata.parent);

                                          // Make it more similar to a usual doclib action callback object
                                          var currentFolderItem = {
                                             nodeRef : node.nodeRef,
                                             node : node,
                                             jsNode : new Alfresco.util.Node(node)
                                          };
                                          this[obj.params["function"]].call(this, currentFolderItem);
                                       },
                                       obj : content,
                                       scope : this
                                    };

                                    url = '#';
                                 } else if (content.type == "pagelink") {
                                    url = $siteURL(content.params.page);
                                 } else if (content.type == "link") {
                                    url = content.params.href;
                                 }

                                 // Create menu item
                                 html = '<a href="' + url + '" rel="' + content.permission + '"><span style="background-image:url(' + Alfresco.constants.URL_RESCONTEXT + 'components/images/filetypes/' + content.icon + '-file-16.png)" class="' + content.icon + '-file">' + this
                                       .msg(content.label) + '</span></a>';
                                 li = document.createElement("li");
                                 li.innerHTML = html;
                                 menuItem = new YAHOO.widget.MenuItem(li, config);

                                 menuItems.push(menuItem);
                              }
                              createContentMenu.addItems(menuItems, groupIndex);
                              groupIndex++;
                           }

                           // Create content by template menu item
                           if (this.options.createContentByTemplateEnabled) {
                              // Create menu item elements
                              var li = document.createElement("li");
                              li.innerHTML = '<a href="#"><span>' + this.msg("menu.create-content.by-template-node") + '</span></a>';

                              // Make sure to stop clicks on the sub menu link to close the entire menu
                              YAHOO.util.Event.addListener(Selector.query("a", li, true), "click", function(e) {
                                 Event.preventDefault(e);
                                 Event.stopEvent(e);
                              });

                              // Create placeholder menu
                              var div = document.createElement("div");
                              div.innerHTML = '<div class="bd"><ul></ul></div>';

                              // 
                              var li2 = document.createElement("li");
                              li2.innerHTML = '<a href="#"><span>' + this.msg("menu.create-content.by-template-folder") + '</span></a>';

                              // Make sure to stop clicks on the sub menu link to close the entire menu
                              YAHOO.util.Event.addListener(Selector.query("a", li2, true), "click", function(e) {
                                 Event.preventDefault(e);
                                 Event.stopEvent(e);
                              });

                              // Create placeholder menu
                              var div2 = document.createElement("div");
                              div2.innerHTML = '<div class="bd"><ul></ul></div>';

                              // Add menu item
                              var createContentByTemplate = new YAHOO.widget.MenuItem(li, {
                                 parent : createContentMenu,
                                 submenu : div
                              });

                              // Add menu item
                              var createFolderByTemplate = new YAHOO.widget.MenuItem(li2, {
                                 parent : createContentMenu,
                                 submenu : div2
                              });

                              createContentMenu.addItems([ createContentByTemplate, createFolderByTemplate ],
                                    groupIndex);
                              groupIndex++;

                              // Make sure that the available template are lazily loaded
                              var templateNodesMenus = this.widgets.createContent.getMenu().getSubmenus(), templateNodesMenu = templateNodesMenus.length > 0 ? templateNodesMenus[0]
                                    : null;
                              if (templateNodesMenu) {
                                 templateNodesMenu.subscribe("beforeShow", this.onCreateByTemplateNodeBeforeShow, this,
                                       true);
                                 templateNodesMenu.subscribe("click", this.onCreateByTemplateNodeClick, this, true);
                              }

                              var templateFoldersMenu = templateNodesMenus.length > 1 ? templateNodesMenus[1] : null;
                              if (templateFoldersMenu) {
                                 templateFoldersMenu.subscribe("beforeShow", this.onCreateByTemplateFolderBeforeShow,
                                       this, true);
                                 templateFoldersMenu.subscribe("click", this.onCreateByTemplateFolderClick, this, true);
                              }
                           }

                           // Render menu with all new menu items
                           createContentMenu.render();
                           this.dynamicControls.push(this.widgets.createContent);
                        }

                        // New Folder button: user needs "create" access
                        this.widgets.newFolder = Alfresco.util.createYUIButton(this, "newFolder-button",
                              this.onNewFolder, {
                                 disabled : true,
                                 value : "CreateChildren"
                              });
                        this.dynamicControls.push(this.widgets.newFolder);

                        // File Upload button: user needs "CreateChildren" access
                        this.widgets.fileUpload = Alfresco.util.createYUIButton(this, "fileUpload-button",
                              this.onFileUpload, {
                                 disabled : true,
                                 value : "CreateChildren"
                              });
                        this.dynamicControls.push(this.widgets.fileUpload);

                        // Sync to Cloud button
                        this.widgets.syncToCloud = Alfresco.util.createYUIButton(this, "syncToCloud-button",
                              this.onSyncToCloud, {
                                 disabled : true,
                                 value : "CreateChildren"
                              });
                        this.dynamicControls.push(this.widgets.syncToCloud);

                        // Unsync from Cloud button
                        this.widgets.unsyncFromCloud = Alfresco.util.createYUIButton(this, "unsyncFromCloud-button",
                              this.onUnsyncFromCloud, {
                                 disabled : true,
                                 value : "CreateChildren"
                              });
                        this.dynamicControls.push(this.widgets.unsyncFromCloud);
                        
                        // Bulk edit

                        this.widgets.bulkEdit = Alfresco.util.createYUIButton(this, "bulkEdit-button", this.onBulkEdit,
                        {
                             disabled: false
                        });
                        this.dynamicControls.push(this.widgets.bulkEdit);
                          


                        // Selected Items menu button
                        this.widgets.selectedItems = Alfresco.util.createYUIButton(this, "selectedItems-button",
                              this.onSelectedItems, {
                                 type : "menu",
                                 menu : "selectedItems-menu",
                                 lazyloadmenu : false,
                                 disabled : true
                              });
                        this.dynamicControls.push(this.widgets.selectedItems);

                        if (Dom.get(this.id + "hideNavBar-button")) {
                           // Hide/Show NavBar button
                           this.widgets.hideNavBar = Alfresco.util.createYUIButton(this, "hideNavBar-button",
                                 this.onHideNavBar, {
                                    type : "checkbox",
                                    checked : !this.options.hideNavBar
                                 });
                           if (this.widgets.hideNavBar !== null) {
                              this.widgets.hideNavBar.set("title", this
                                    .msg(this.options.hideNavBar ? "button.navbar.show" : "button.navbar.hide"));
                              Dom.setStyle(this.id + "-navBar", "display", this.options.hideNavBar ? "none" : "block");
                              this.dynamicControls.push(this.widgets.hideNavBar);
                           }
                        }

                        // Hide or show the nav bar depending on the current settings...
                        Dom.setStyle(this.id + "-navBar", "display", this.options.hideNavBar ? "none" : "block");

                        // RSS Feed link button
                        this.widgets.rssFeed = Alfresco.util.createYUIButton(this, "rssFeed-button", null, {
                           type : "link"
                        });
                        this.dynamicControls.push(this.widgets.rssFeed);

                        // Folder Up Navigation button
                        this.widgets.folderUp = Alfresco.util.createYUIButton(this, "folderUp-button", this.onFolderUp,
                              {
                                 disabled : true,
                                 title : this.msg("button.up")
                              });
                        this.dynamicControls.push(this.widgets.folderUp);

                        // Finally show the component body here to prevent UI artifacts on YUI button decoration
                        Dom.setStyle(this.id + "-tb-body", "visibility", "visible");
                     }

                     // DocLib Actions module
                     this.modules.actions = new Alfresco.module.DoclibActions();

                     // Reference to Document List component
                     this.modules.docList = Alfresco.util.ComponentManager.findFirst("Alfresco.DocumentList");

                     // Preferences service
                     this.services.preferences = new Alfresco.service.Preferences();

                  },

                  onBulkEdit : function CustomDLTB_onBulkEdit(sType, aArgs, p_obj) {
                     var eventTarget = aArgs, anchor = eventTarget.getElementsByTagName("a")[0];

                     if (anchor && anchor.nodeName == "A") {
                        anchor.href = YAHOO.lang.substitute(anchor.href, {
                           nodeRef : this.doclistMetadata.parent.nodeRef
                        });

                        // Portlet fix: parameter might be encoded
                        if (anchor.href.indexOf("%7BnodeRef%7D") !== -1) {
                           anchor.href = anchor.href.replace("%7BnodeRef%7D",
                                 encodeURIComponent(this.doclistMetadata.parent.nodeRef));
                        }

                     }
                  },
                  /**
                   * Document List Metadata event handler NOTE: This is a temporary fix to enable access to the View
                   * Details action from the breadcrumb. A more complete solution is to present the full list of parent
                   * folder actions.
                   * 
                   * @method onDoclistMetadata
                   * @param layer
                   *            {object} Event fired
                   * @param args
                   *            {array} Event parameters (depends on event type)
                   */
                  onDoclistMetadata : function DLTB_onDoclistMetadata(layer, args) {
                     var obj = args[1];
                     this.entityLinkCustomClass = null;
                     this.folderDetailsUrl = null;
                     if (obj && obj.metadata) {
                        this.doclistMetadata = Alfresco.util.deepCopy(obj.metadata);
                        if (obj.metadata.parent && obj.metadata.parent.nodeRef) {
                           if (beCPG.util.isEntity(obj.metadata.parent)) {
                              this.entityLinkCustomClass = obj.metadata.parent.type.replace(":", "_");
                              this.folderDetailsUrl = $siteURL("entity-details?nodeRef=" + obj.metadata.parent.nodeRef);
                           } else {
                              this.folderDetailsUrl = $siteURL("folder-details?nodeRef=" + obj.metadata.parent.nodeRef);
                           }

                        }
                     }
                  },
                  /**
                   * Generates the HTML mark-up for the breadcrumb from the currentPath
                   * 
                   * @method _generateBreadcrumb
                   * @private
                   */
                  _generateBreadcrumb : function DLTB__generateBreadcrumb() {
                     var divBC = Dom.get(this.id + "-breadcrumb");
                     if (divBC === null) {
                        return;
                     }
                     divBC.innerHTML = "";

                     var paths = this.currentPath.split("/");
                     // Check for root path special case
                     if (this.currentPath === "/") {
                        paths = [ "/" ];
                     }
                     // Clone the array and re-use the root node name from the DocListTree
                     var me = this, displayPaths = paths.concat();

                     displayPaths[0] = Alfresco.util.message("node.root", this.currentFilter.filterOwner);

                     var fnCrumbIconClick = function DLTB__fnCrumbIconClick(e, path) {
                        Dom.addClass(e.target.parentNode, "highlighted");
                        Event.stopEvent(e);
                     };

                     var fnBreadcrumbClick = function DLTB__fnBreadcrumbClick(e, path) {
                        var filter = me.currentFilter;
                        filter.filterData = path;

                        YAHOO.Bubbling.fire("changeFilter", filter);
                        Event.stopEvent(e);
                     };

                     var eBreadcrumb = new Element(divBC), newPath, eCrumb, eIcon, eFolder;

                     for (var i = 0, j = paths.length; i < j; ++i) {
                        newPath = paths.slice(0, i + 1).join("/");
                        eCrumb = new Element(document.createElement("div"));
                        eCrumb.addClass("crumb");
                        eCrumb.addClass("documentDroppable"); // This class allows documents to be dropped onto the
                                                               // element
                        eCrumb.addClass("documentDroppableHighlights"); // This class allows drag over/out events to be
                                                                        // processed

                        // First crumb doesn't get an icon
                        if (i > 0) {
                           eIcon = new Element(document.createElement("a"), {
                              href : "#",
                              innerHTML : "&nbsp;"
                           });
                           eIcon.on("click", fnBreadcrumbClick, newPath);
                           eIcon.addClass("icon");
                           if (j - i < 2 && this.entityLinkCustomClass != null) {
                        	   eIcon.addClass("entity");
                        	   eIcon.addClass(this.entityLinkCustomClass);
                           }
                           eIcon.addClass("filter-" + $html(this.currentFilter.filterId));
                           eCrumb.appendChild(eIcon);
                        }

                        // Last crumb is rendered as a link if folderDetailsUrl is available (via doclistMetadata)
                        if (j - i < 2) {
                           eFolder = new Element(
                                 document.createElement("span"),
                                 {
                                    innerHTML : (this.folderDetailsUrl) ? '<a href="' + this.folderDetailsUrl + '">' + $html(displayPaths[i]) + '</a>'
                                          : $html(displayPaths[i])
                                 });
                           eFolder.addClass("label");
                           eCrumb.appendChild(eFolder);
                           eBreadcrumb.appendChild(eCrumb);
                        } else {
                           eFolder = new Element(document.createElement("a"), {
                              href : "",
                              innerHTML : $html(displayPaths[i])
                           });
                           eFolder.addClass("folder");
                           eFolder.on("click", fnBreadcrumbClick, newPath);
                           eCrumb.appendChild(eFolder);
                           eBreadcrumb.appendChild(eCrumb);
                           eBreadcrumb.appendChild(new Element(document.createElement("div"), {
                              innerHTML : "&gt;",
                              className : "separator"
                           }));
                        }
                     }

                     var rootEl = Dom.get(this.id + "-breadcrumb");
                     var dndTargets = Dom.getElementsByClassName("crumb", "div", rootEl);
                     for (var i = 0, j = dndTargets.length; i < j; i++) {
                        new YAHOO.util.DDTarget(dndTargets[i]);
                     }
                  }

               });
})();
