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

                     this.nodeType = "entity";

                     // Custom button
                     if (!this.options.pathMode) {

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
                              for (var index in menuItems) {
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

                     var url = 'components/entity-details/entity-header?nodeRef={nodeRef}&rootPage={rootPage}' + '&rootLabelId={rootLabelId}&showFavourite={showFavourite}&showLikes={showLikes}' + '&showComments={showComments}&showQuickShare={showQuickShare}&showDownload={showDownload}&showPath={showPath}' + (this.options.siteId ? '&site={siteId}'
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
                                                   // document.location.reload(false);
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
