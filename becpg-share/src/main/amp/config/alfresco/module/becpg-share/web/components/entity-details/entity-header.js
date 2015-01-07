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
 * Document and Folder header component.
 * 
 * @namespace beCPG
 * @class beCPG.custom.NodeHeader
 */
(function() {

   /**
    * NodeHeader constructor.
    * 
    * @param {String}
    *            htmlId The HTML id of the parent element
    * @return {beCPG.custom.NodeHeader} The new NodeHeader instance
    * @constructor
    */
   beCPG.custom.NodeHeader = function NodeHeader_constructor(htmlId) {
      beCPG.custom.NodeHeader.superclass.constructor.call(this, htmlId);

      this.preferencesService = new Alfresco.service.Preferences();

      return this;
   };

   YAHOO
         .extend(
               beCPG.custom.NodeHeader,
               Alfresco.component.NodeHeader,
               {

                  /**
                   * Fired by YUI when parent element is available for scripting. Initial History Manager event
                   * registration
                   * 
                   * @method onReady
                   */
                  onReady : function NodeHeader_onReady() {
                     var me = this;
                     
                     // MNT-9081 fix, redirect user to the correct location, if requested site is not the actual site
                     // where document is located
                     if (this.options.siteId != this.options.actualSiteId) {
                        // Moved to a site...
                        if (this.options.actualSiteId != null) {
                           var correctUrl = window.location.href
                                 .replace(this.options.siteId, this.options.actualSiteId);
                           Alfresco.util.PopupManager.displayPrompt({
                              text : this.msg("message.document.moved", this.options.actualSiteId),
                              buttons : [ {
                                 text : this.msg("button.ok"),
                                 handler : function() {
                                    window.location = correctUrl;
                                 },
                                 isDefault : true
                              } ]
                           });
                           YAHOO.lang.later(10000, this, function() {
                              window.location = correctUrl;
                           });
                        } else {
                           // Moved elsewhere in repository...
                           var correctUrl = "/share/page/entity-details?nodeRef=" + this.options.nodeRef;
                           Alfresco.util.PopupManager.displayPrompt({
                              text : this.msg("message.document.movedToRepo"),
                              buttons : [ {
                                 text : this.msg("button.ok"),
                                 handler : function() {
                                    window.location = correctUrl;
                                 },
                                 isDefault : true
                              } ]
                           });
                           YAHOO.lang.later(10000, this, function() {
                              window.location = correctUrl;
                           });
                           
                        }
                        return;
                     }

                     this.nodeType = "entity";

                     // Custom button
                     if (!this.options.showOnlyLocation) {

                        if (this.options.showLikes) {
                           // Create like widget
                           new Alfresco.Like(this.id + '-like').setOptions({
                              nodeRef : this.options.nodeRef,
                              siteId : this.options.siteId,
                              type : this.nodeType,
                              displayName : this.options.displayName,
                              activity : {
                                 "entity" : {
                                    type : "org.alfresco.documentlibrary.entity-liked",
                                    page : "entity-details?nodeRef={nodeRef}"
                                 }
                              }
                           }).display(this.options.likes.isLiked, this.options.likes.totalLikes);
                        }

                        if (this.options.showFavourite) {
                           // Create favourite widget
                           new Alfresco.Favourite(this.id + '-favourite').setOptions({
                              nodeRef : this.options.nodeRef,
                              type : "folder"
                           }).display(this.options.isFavourite);
                        }

                        if (this.options.showQuickShare) {
                           // Create favourite widget
                           new Alfresco.QuickShare(this.id + '-quickshare').setOptions({
                              nodeRef : this.options.nodeRef,
                              displayName : this.options.displayName
                           }).display(this.options.sharedId, this.options.sharedBy);
                        }

                        Alfresco.util.useAsButton(this.id+"-print-button", function(e)
                                {
                                    var wnd = window.open(Alfresco.constants.URL_PAGECONTEXT+"print-details?nodeRef="+this.options.nodeRef);
                                    setTimeout(function() {
                                        wnd.print();
                                    }, 3000);
                                }, null, this);
                        
                        
                        // Parse the date
                        var dateEl = Dom.get(this.id + '-modifyDate');
                        dateEl.innerHTML = Alfresco.util.formatDate(Alfresco.util.fromISO8601(dateEl.innerHTML),
                              Alfresco.util.message("date-format.default"));

                        this.widgets.viewEntityDatalist = Alfresco.util.createYUIButton(me,
                              "viewEntityDatalist-button", function(sType, aArgs, p_obj) {

                                 window.location.href = beCPG.util.entityCharactURL(me.options.siteId,
                                       me.options.nodeRef, me.options.itemType);
                              });
                        this.widgets.viewEntityDocuments = Alfresco.util.createYUIButton(me,
                              "viewEntityDocuments-button", function(sType, aArgs, p_obj) {
                                 window.location.href = beCPG.util.entityDocumentsURL(me.options.siteId,
                                       me.options.path, me.options.itemName, true);
                              });

                      YAHOO.util.Event.addListener(me.id + "-uploadLogo-button", "click", function(e){
                    	  if (!me.fileUpload)
                          {
                             me.fileUpload = Alfresco.getFileUploadInstance();
                          }
                          
                          // Show uploader for single file select - override the upload URL to use appropriate upload service
                          var uploadConfig =
                          {
                             flashUploadURL: "becpg/entity/uploadlogo" ,
                             htmlUploadURL: "becpg/entity/uploadlogo.html" ,
                             updateNodeRef: me.options.nodeRef,
                             mode: me.fileUpload.MODE_SINGLE_UPLOAD,
                             onFileUploadComplete:
                             {
                                fn: function onFileUploadComplete(complete)
                                {
                                    var success = complete.successful.length;
                                    if (success != 0)
                                    {
                                       var noderef = complete.successful[0].nodeRef;
                                       YAHOO.Bubbling.fire("metadataRefresh");
                                       // replace image URL with the updated one
//                                       var logoImg = Dom.get(this.id + "-logoimg");
//                                       logoImg.src = Alfresco.constants.PROXY_URI + "api/node/" + noderef.replace("://", "/") + "/content";
//                                       
//                                       // set noderef value in hidden field ready for options form submit
//                                       Dom.get("console-options-logo").value = noderef;
                                    }
                                 },
                                scope: this
                             }
                          };
                          me.fileUpload.show(uploadConfig);
                          YAHOO.util.Event.preventDefault(e);
                         });
                        
                        if (this.options.report !== null) {

                           this.widgets.entityReportPicker = new YAHOO.widget.Button(
                                 me.id + "-entityReportPicker-button", {
                                    type : "split",
                                    menu : me.id + "-entityReportPicker-select",
                                    lazyloadmenu : false
                                 });

                           this.widgets.entityReportPicker.on("click", me.onEntityReportPickerClicked, me, true);

                           this.widgets.entityReportPicker.getMenu().subscribe("click", function(p_sType, p_aArgs) {
                              var menuItem = p_aArgs[1];
                              if (menuItem) {
                                 me.widgets.entityReportPicker.set("label", menuItem.cfg.getProperty("text"));
                                 me.onEntityReportPickerClicked.call(me, menuItem);
                              }
                           });

                           if (this.options.report.isSelected) {
                              var menuItems = me.widgets.entityReportPicker.getMenu().getItems();
                              for ( var index in menuItems) {
                                 if (menuItems.hasOwnProperty(index)) {
                                    if (menuItems[index].value === this.options.report.nodeRef) {
                                       me.widgets.entityReportPicker.set("label", menuItems[index].cfg
                                             .getProperty("text"));
                                       me.onEntityReportPickerClicked.call(me, menuItems[index]);
                                       break;
                                    }
                                 }

                              }

                           }

                           this.widgets.downloadEntityReport = Alfresco.util.createYUIButton(me,
                                 "downloadEntityReport-button", function(sType, aArgs, p_obj) {
                                    window.location.href = Alfresco.constants.PROXY_URI + me.options.report.contentURL+"&a=true&noCache=" + new Date().getTime();

                                 });
                        }

                     }

                     if (this.options.report === null || !this.options.report.isSelected) {
                        Dom.removeClass("properties-tab", "hidden");
                        Dom.addClass("preview-tab", "hidden");
                     }

                  },
                  
                  

                  /**
                   * Refresh component in response to metadataRefresh event
                   * 
                   * @method doRefresh
                   */
                  doRefresh : function NodeHeader_doRefresh() {
                     YAHOO.Bubbling.unsubscribe("metadataRefresh", this.doRefresh, this);

                     var url = 'components/entity-details/entity-header?nodeRef={nodeRef}&rootPage={rootPage}' + '&rootLabelId={rootLabelId}&showFavourite={showFavourite}&showLikes={showLikes}' + '&showComments={showComments}&showQuickShare={showQuickShare}&showDownload={showDownload}&showPath={showPath}' + (this.options.pagecontext ? '&pagecontext={pagecontext}'
                           : '') + (this.options.libraryRoot ? '&libraryRoot={libraryRoot}' : '') + (this.options.siteId ? '&site={siteId}'
                           : '');

                     this.refresh(url);
                  },

                  /**
                   * @param menuItem
                   */
                  onEntityReportPickerClicked : function NodeHeader_onEntityReportPickerClicked(menuItem) {
                     var scope = this;
                     if (menuItem) {
                        scope.widgets.entityReportPicker.value = encodeURIComponent(menuItem.value);
                        scope.preferencesService
                              .set(
                                    scope.getPickerPreference(),
                                    menuItem.cfg.getProperty("text"),
                                    {
                                       successCallback : {
                                          fn : function() {

                                             if ("properties" === scope.widgets.entityReportPicker.value) {
                                                Dom.removeClass("properties-tab", "hidden");
                                                Dom.addClass("preview-tab", "hidden");
                                             } else {

                                                if (encodeURIComponent(scope.options.report.nodeRef) != scope.widgets.entityReportPicker.value) {
                                                   // Reload page with cache on
                                                   //document.location.reload(false);
                                                   window.location.href = window.location.href.split("#")[0];
                                                }
                                                Dom.addClass("properties-tab", "hidden");
                                                Dom.removeClass("preview-tab", "hidden");

                                             }

                                          },
                                          scope : this
                                       }
                                    });
                     }

                  },

                  getPickerPreference : function NodeHeader_getPickerPreference() {
                     return "fr.becpg.repo.report." + this.options.itemType.replace(":", "_") + ".view";

                  }

               });
})();
