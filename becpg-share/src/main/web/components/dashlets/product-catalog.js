/**
 * Dashboard BeCPGCatalog component.
 * 
 * @namespace beCPG
 * @class beCPG.dashlet.BeCPGCatalog
 */
(function() {
   /**
    * YUI Library aliases
    */
   var Dom = YAHOO.util.Dom, Event = YAHOO.util.Event, Selector = YAHOO.util.Selector;
   /**
    * Alfresco Slingshot aliases
    */
   var $html = Alfresco.util.encodeHTML, $userProfile = Alfresco.util.userProfileLink, $siteDashboard = Alfresco.util.siteDashboardLink, $relTime = Alfresco.util.relativeTime, $isValueSet = Alfresco.util.isValueSet;

   /**
    * Preferences
    */
   var PREFERENCES_BECPGCATALOG_DASHLET = "org.alfresco.share.product.catalog.dashlet{site}", 
   PREFERENCES_BECPGCATALOG_DASHLET_FILTER = PREFERENCES_BECPGCATALOG_DASHLET + ".filter", 
   PREFERENCES_BECPGCATALOG_DASHLET_VIEW = PREFERENCES_BECPGCATALOG_DASHLET + ".simpleView", 
   PREFERENCES_BECPGCATALOG_DASHLET_TYPE = PREFERENCES_BECPGCATALOG_DASHLET + ".type";

   /**
    * Use the getDomId function to get unique names for global event handling
    */
   var FAVOURITE_EVENTCLASS = Alfresco.util.generateDomId(null, "favourite") + "{type}";

   /**
    * Dashboard BeCPGCatalog constructor.
    * 
    * @param {String}
    *            htmlId The HTML id of the parent element
    * @return {beCPG.dashlet.BeCPGCatalog} The new component instance
    * @constructor
    */
   beCPG.dashlet.BeCPGCatalog = function BeCPGCatalog_constructor(htmlId) {
      return beCPG.dashlet.BeCPGCatalog.superclass.constructor.call(this, htmlId);
   };

   YAHOO
         .extend(
               beCPG.dashlet.BeCPGCatalog,
               Alfresco.component.SimpleDocList,
               {
                  searchTerm : null,

                  /**
                   * Fired by YUI when parent element is available for scripting
                   * 
                   * @method onReady
                   */
                  onReady : function BeCPGCatalog_onReady() {
                     // Create Dropdown filter
                     this.widgets.filter = Alfresco.util.createYUIButton(this, "filters", this.onFilterChange, {
                        type : "menu",
                        menu : "filters-menu",
                        lazyloadmenu : false
                     });

                     // Select the preferred filter in the ui
                     var filter = this.options.filter;
                     filter = Alfresco.util.arrayContains(this.options.validFilters, filter) ? filter
                           : this.options.validFilters[0];
                     this.widgets.filter.set("label", this.msg("filter." + filter));
                     this.widgets.filter.value = filter;

                     this.widgets.type = Alfresco.util.createYUIButton(this, "types", this.onTypeChange, {
                        type : "menu",
                        menu : "types-menu",
                        lazyloadmenu : false
                     });

                     // Select the preferred type in the ui
                     this.widgets.type.set("label", this.msg("type." + this.options.catalogType));
                     this.widgets.type.value = this.options.catalogType;

                     // Detailed/Simple List button
                     this.widgets.simpleDetailed = new YAHOO.widget.ButtonGroup(this.id + "-simpleDetailed");
                     if (this.widgets.simpleDetailed !== null) {
                        this.widgets.simpleDetailed.check(this.options.simpleView ? 0 : 1);
                        this.widgets.simpleDetailed.on("checkedButtonChange", this.onSimpleDetailed,
                              this.widgets.simpleDetailed, this);
                     }

                     this.configureSearch();

                     // Display the toolbar now that we have selected the filter
                     Dom.removeClass(Selector.query(".toolbar div", this.id, true), "hidden");

                     // DataTable can now be rendered
                     // beCPG.dashlet.BeCPGCatalog.superclass.onReady.apply(this, arguments);

                     var me = this;

                     // Tooltip for thumbnail on mouse hover
                     this.widgets.previewTooltip = new YAHOO.widget.Tooltip(this.id + "-previewTooltip", {
                        width : "108px"
                     });
                     this.widgets.previewTooltip.contextTriggerEvent
                           .subscribe(function(type, args) {
                              var context = args[0], record = me.widgets.alfrescoDataTable.getData(context.id), thumbnailUrl = Alfresco.constants.PROXY_URI + "api/node/" + record.nodeRef
                                    .replace(":/", "") + "/content/thumbnails/doclib?c=queue&ph=true";

                              this.cfg.setProperty("text", '<img src="' + thumbnailUrl + '" />');
                           });

                     // Tooltip for metadata on mouse hover
                     this.widgets.metadataTooltip = new YAHOO.widget.Tooltip(this.id + "-metadataTooltip");
                     this.widgets.metadataTooltip.contextTriggerEvent
                           .subscribe(function(type, args) {
                              var context = args[0], record = me.widgets.alfrescoDataTable.getData(context.id), locn = record.location;

                              var text = '<em>' + me.msg("label.site") + ':</em> ' + $html(locn.siteTitle) + '<br />';
                              text += '<em>' + me.msg("label.path") + ':</em> ' + $html(locn.path);

                              this.cfg.setProperty("text", text);
                           });

                     /**
                      * Create datatable
                      */
                     this.widgets.alfrescoDataTable = new Alfresco.util.DataTable({
                        dataSource : {
                           url : this.getWebscriptUrl(),
                           initialParameters : this.getParameters(),
                           config : {
                              responseSchema : {
                                 resultsList : "items"
                              }
                           }
                        },
                        dataTable : {
                           container : this.id + "-documents",
                           columnDefinitions : [ {
                              key : "thumbnail",
                              sortable : false,
                              formatter : this.bind(this.renderCellThumbnail),
                              width : 16
                           }, {
                              key : "detail",
                              sortable : false,
                              formatter : this.bind(this.renderCellDetail)
                           } ],
                           config : {
                              className : "alfresco-datatable simple-doclist",
                              renderLoopSize : 4
                           }
                        }
                     });

                     // Override DataTable function to set custom empty message
                     var dataTable = this.widgets.alfrescoDataTable.getDataTable(), original_doBeforeLoadData = dataTable.doBeforeLoadData;

                     dataTable.doBeforeLoadData = function SimpleDocList_doBeforeLoadData(sRequest, oResponse, oPayload) {
                        if (oResponse.results && oResponse.results.length === 0) {
                           oResponse.results.unshift({
                              isInfo : true,
                              title : me.msg("empty.product.title"),
                              description : me.msg("empty.product.description")
                           });
                        }

                        return original_doBeforeLoadData.apply(this, arguments);
                     };

                     // Rendering complete event handler
                     dataTable.subscribe("renderEvent", function() {
                        // Register tooltip contexts
                        this.widgets.previewTooltip.cfg.setProperty("context", this.previewTooltips);
                        this.widgets.metadataTooltip.cfg.setProperty("context", this.metadataTooltips);
                     }, this, true);

                     // Hook favourite document events
                     var fnFavouriteHandler = function SimpleDocList_fnFavouriteHandler(layer, args) {
                        var owner = YAHOO.Bubbling.getOwnerByTagName(args[1].anchor, "div");
                        if (owner !== null) {
                           me.onFavourite.call(me, args[1].target.offsetParent, owner);
                        }
                        return true;
                     };
                     YAHOO.Bubbling.addDefaultAction(this.substitute(FAVOURITE_EVENTCLASS), fnFavouriteHandler);

                  },

                  /**
                   * Generate base webscript url. Can be overridden.
                   * 
                   * @method getWebscriptUrl
                   */
                  getWebscriptUrl : function BeCPGCatalog_getWebscriptUrl() {
                     if (Alfresco.constants.SITE !== null && Alfresco.constants.SITE.length > 0) {
                        return Alfresco.constants.PROXY_URI + "slingshot/doclib/doclist/product/site/" + Alfresco.constants.SITE + "/documentLibrary?max=" + this.options.maxItems;
                     }
                     return Alfresco.constants.PROXY_URI + "slingshot/doclib/doclist/product/node/alfresco/company/home?max=" + this.options.maxItems;
                  },

                  /**
                   * Calculate webscript parameters
                   * 
                   * @method getParameters
                   * @override
                   */
                  getParameters : function BeCPGCatalog_getParameters() {
                     var parameters = "type=" + this.widgets.type.value + "&filter=" + this.widgets.filter.value;

                     if (this.searchTerm !== null && this.searchTerm.length > 0) {
                        parameters += "&searchTerm=" + this.searchTerm;
                     }

                     return parameters;
                  },

                  /**
                   * Filter Change menu handler
                   * 
                   * @method onFilterChange
                   * @param p_sType
                   *            {string} The event
                   * @param p_aArgs
                   *            {array}
                   */
                  onFilterChange : function BeCPGCatalog_onFilterChange(p_sType, p_aArgs) {
                     var menuItem = p_aArgs[1];
                     if (menuItem) {
                        this.widgets.filter.set("label", menuItem.cfg.getProperty("text"));
                        this.widgets.filter.value = menuItem.value;
                        
                        this.services.preferences.set(this.substitute(PREFERENCES_BECPGCATALOG_DASHLET_FILTER),
                              this.widgets.filter.value);

                        var searchText = this.getSearchText();
                        if (searchText.replace(/\*/g, "").length < 3) {
                           this.searchTerm = null;
                        } else {
                           this.searchTerm = searchText;

                        }

                        this.reloadDataTable();
                     }
                  },

                  onTypeChange : function BeCPGCatalog_onTypeChange(p_sType, p_aArgs) {
                     var menuItem = p_aArgs[1];
                     if (menuItem) {
                        this.widgets.type.set("label", menuItem.cfg.getProperty("text"));
                        this.widgets.type.value = menuItem.value;

                        this.services.preferences.set(this.substitute(PREFERENCES_BECPGCATALOG_DASHLET_TYPE),
                              this.widgets.type.value);

                        var searchText = this.getSearchText();
                        if (searchText.replace(/\*/g, "").length < 3) {
                           this.searchTerm = null;
                        } else {
                           this.searchTerm = searchText;

                        }

                        this.reloadDataTable();
                     }
                  },

                  /**
                   * Show/Hide detailed list buttongroup click handler
                   * 
                   * @method onSimpleDetailed
                   * @param e
                   *            {object} DomEvent
                   * @param p_obj
                   *            {object} Object passed back from addListener method
                   */
                  onSimpleDetailed : function BeCPGCatalog_onSimpleDetailed(e, p_obj) {
                     this.options.simpleView = e.newValue.index === 0;
                     this.services.preferences.set(this.substitute(PREFERENCES_BECPGCATALOG_DASHLET_VIEW),
                           this.options.simpleView);
                     if (e) {
                        Event.preventDefault(e);
                     }

                     this.reloadDataTable();
                  },
                  /**
                   * Thumbnail custom datacell formatter
                   * 
                   * @method renderCellThumbnail
                   * @param elCell
                   *            {object}
                   * @param oRecord
                   *            {object}
                   * @param oColumn
                   *            {object}
                   * @param oData
                   *            {object|string}
                   */
                  renderCellThumbnail : function SimpleDocList_renderCellThumbnail(elCell, oRecord, oColumn, oData) {
                     var columnWidth = 40, record = oRecord.getData(), desc = "";

                     record.jsNode = {};
                     record.jsNode.type = record.nodeType;

                     if (record.isInfo) {
                        columnWidth = 52;
                        desc = '<img src="' + Alfresco.constants.URL_RESCONTEXT + 'components/images/help-docs-bw-32.png" />';
                     } else {
                        var thumbName = record.fileName, recordSiteName = $isValueSet(record.location.site) ? record.location.site
                              : null, extn = thumbName.substring(thumbName.lastIndexOf(".")), nodeRef = new Alfresco.util.NodeRef(
                              record.nodeRef), docDetailsUrl = beCPG.util.entityDetailsURL(recordSiteName,
                              record.nodeRef, record.itemType);

                        if (this.options.simpleView) {
                           /**
                            * Simple View
                            */
                           var id = this.id + '-preview-' + oRecord.getId();
                           desc = '<span id="' + id + '" class="icon32"><a href="' + docDetailsUrl + '"><img src="' + beCPG.util
                                 .getFileIcon(thumbName, record, false, true) + '" alt="' + extn + '" title="' + $html(thumbName) + '" /></a></span>';

                           // Preview tooltip
                           this.previewTooltips.push(id);
                        } else {
                           /**
                            * Detailed View
                            */
                           columnWidth = 100;
                           desc = '<span class="thumbnail"><a href="' + docDetailsUrl + '"><img src="' + Alfresco.constants.PROXY_URI + 'api/node/' + nodeRef.uri + '/content/thumbnails/doclib?c=queue&ph=true" alt="' + extn + '" title="' + $html(thumbName) + '" /></a></span>';
                        }
                     }

                     oColumn.width = columnWidth;

                     Dom.setStyle(elCell, "width", oColumn.width + "px");
                     Dom.setStyle(elCell.parentNode, "width", oColumn.width + "px");

                     elCell.innerHTML = desc;
                  },

                  /**
                   * Detail custom datacell formatter
                   * 
                   * @method renderCellDetail
                   * @param elCell
                   *            {object}
                   * @param oRecord
                   *            {object}
                   * @param oColumn
                   *            {object}
                   * @param oData
                   *            {object|string}
                   */
                  renderCellDetail : function BeCPGCatalog_renderCellDetail(elCell, oRecord, oColumn, oData) {
                     var record = oRecord.getData(), desc = "";

                     if (record.isInfo) {
                        desc += '<div class="empty"><h3>' + record.title + '</h3>';
                        desc += '<span>' + record.description + '</span></div>';
                     } else {
                        var id = this.id + '-metadata-' + oRecord.getId(), recordSiteName = $isValueSet(record.location.site) ? record.location.site
                              : null, version = "", dateLine = "", locn = record.location, docDetailsUrl = beCPG.util
                              .entityDetailsURL(recordSiteName, record.nodeRef, record.itemType);

                        // Version display
                        if (record.version && record.version !== "") {
                           version = '<span class="document-version">' + $html(record.version) + '</span>';
                        }

                        // Date line
                        var dateI18N = "modified", dateProperty = record.modifiedOn;
                        if (record.custom && record.custom.isWorkingCopy) {
                           dateI18N = "editing-started";
                        } else if (record.modifiedOn === record.createdOn) {
                           dateI18N = "created";
                           dateProperty = record.createdOn;
                        }
                        if (Alfresco.constants.SITE === "") {
                           dateLine = this.msg("details." + dateI18N + "-in-site", $relTime(dateProperty),
                                 $siteDashboard(locn.site, locn.siteTitle,
                                       'class="site-link theme-color-1" id="' + id + '"'));
                        } else {
                           dateLine = this.msg("details." + dateI18N + "-by", $relTime(dateProperty), $userProfile(
                                 record.modifiedByUser, record.modifiedBy, 'class="theme-color-1"'));
                        }

                        if (this.options.simpleView) {
                           /**
                            * Simple View
                            */
                           desc += '<h3 class="filename simple-view"><a class="theme-color-1" href="' + docDetailsUrl + '">' + $html(record.displayName) + '</a></h3>';
                           desc += '<div class="detail"><span class="item-simple">' + dateLine + '</span></div>';
                        } else {
                           /**
                            * Detailed View
                            */
                           desc += '<h3 class="filename"><a class="theme-color-1" href="' + docDetailsUrl + '">' + $html(record.displayName) + '</a>' + version + '</h3>';

                           desc += '<div class="detail">';
                           desc += '<span class="item">' + dateLine + '</span>';
                           desc += '</div>';

                           var charactsUrl = beCPG.util.entityCharactURL(recordSiteName, record.nodeRef,
                                 record.nodeType), documentsUrl = beCPG.util.entityDocumentsURL(recordSiteName,
                                 record.location.path, record.location.file, true);

                           /* Favourite / Charact / Download */
                           desc += '<div class="detail detail-social">';
                           desc += '<span class="item item-social">' + this.generateFavourite(this, oRecord) + '</span>';
                           desc += '<span class="item item-social item-separator"><a class="view-documents" href="' + documentsUrl + '"  title="' + this
                                 .msg("actions.entity.view-documents") + '" tabindex="0">' + this
                                 .msg("actions.entity.view-documents.short") + '</a></span>';
                           desc += '<span class="item item-social item-separator"><a class="view-characts" href="' + charactsUrl + '" title="' + this
                                 .msg("actions.entity.view-datalists") + '" tabindex="0">' + this
                                 .msg("actions.entity.view-datalists.short") + '</a></span>';
                           desc += '</div>';
                        }

                        // Metadata tooltip
                        this.metadataTooltips.push(id);
                     }

                     elCell.innerHTML = desc;
                  },

                  generateFavourite : function BeCPGCatalog_generateFavourite(scope, record) {
                     var i18n = "favourite.document.", html = "";

                     if (record.getData("isFavourite")) {
                        html = '<a class="favourite-action ' + scope.substitute(FAVOURITE_EVENTCLASS) + ' enabled" title="' + scope
                              .msg(i18n + "remove.tip") + '" tabindex="0"></a>';
                     } else {
                        html = '<a class="favourite-action ' + scope.substitute(FAVOURITE_EVENTCLASS) + '" title="' + scope
                              .msg(i18n + "add.tip") + '" tabindex="0">' + scope.msg(i18n + "add.label") + '</a>';
                     }

                     return html;
                  },

                  onFavourite : function BeCPGCatalog_onFavourite(row) {
                     var record = this.widgets.alfrescoDataTable.getRecord(row), file = record.getData(), nodeRef = file.nodeRef;

                     file.isFavourite = !file.isFavourite;
                     this.widgets.alfrescoDataTable.getDataTable().updateRow(record, file);

                     var responseConfig = {
                        failureCallback : {
                           fn : function SimpleDocList_onFavourite_failure(event, p_oRow) {
                              // Reset the flag to it's previous state
                              var record = this.widgets.alfrescoDataTable.getRecord(p_oRow), file = record.getData();

                              file.isFavourite = !file.isFavourite;
                              this.widgets.alfrescoDataTable.getDataTable().updateRow(record, file);
                              Alfresco.util.PopupManager.displayPrompt({
                                 text : this.msg("message.save.failure", file.displayName)
                              });
                           },
                           scope : this,
                           obj : row
                        }
                     };

                     this.services.preferences[file.isFavourite ? "add" : "remove"].call(this.services.preferences,
                           Alfresco.service.Preferences.FAVOURITE_FOLDERS, nodeRef, responseConfig);
                  },

                  /**
                   * Search Handlers
                   */

                  /**
                   * Configure search area
                   * 
                   * @method configureSearch
                   */
                  configureSearch : function BeCPGCatalog_configureSearch() {
                     this.widgets.searchBox = Dom.get(this.id + "-searchText");
                     this.defaultSearchText = this.msg("header.search.default");

                     Event.addListener(this.widgets.searchBox, "focus", this.onSearchFocus, null, this);
                     Event.addListener(this.widgets.searchBox, "blur", this.onSearchBlur, null, this);
                     Event.addListener(this.widgets.searchBox, "change", this.onSearchChange, null, this);

                     this.setDefaultSearchText();

                     this.widgets.searchMore = new YAHOO.widget.Button(this.id + "-search_more", {
                        type : "menu",
                        menu : this.id + "-searchmenu_more"
                     });
                  },

                  /**
                   * Update image class when search box has focus.
                   * 
                   * @method onSearchFocus
                   */
                  onSearchFocus : function BeCPGCatalog_onSearchFocus() {
                     if (this.widgets.searchBox.value == this.defaultSearchText) {
                        Dom.removeClass(this.widgets.searchBox, "faded");
                        this.widgets.searchBox.value = "";
                     } else {
                        this.widgets.searchBox.select();
                     }
                  },

                  /**
                   * Set default search text when box loses focus and is empty.
                   * 
                   * @method onSearchBlur
                   */
                  onSearchBlur : function BeCPGCatalog_onSearchBlur() {
                     var searchText = YAHOO.lang.trim(this.widgets.searchBox.value);
                     if (searchText.length === 0) {
                        /**
                         * Since the blur event occurs before the KeyListener gets the enter we give the enter listener
                         * a chance of testing against "" instead of the help text.
                         */
                        YAHOO.lang.later(100, this, this.setDefaultSearchText, []);
                     }
                  },

                  /**
                   * Set default search text for search box.
                   * 
                   * @method setDefaultSearchText
                   */
                  setDefaultSearchText : function BeCPGCatalog_setDefaultSearchText() {
                     Dom.addClass(this.widgets.searchBox, "faded");
                     this.widgets.searchBox.value = this.defaultSearchText;
                  },

                  /**
                   * Get current search text from search box.
                   * 
                   * @method getSearchText
                   */
                  getSearchText : function BeCPGCatalog_getSearchText() {

                     var ret = YAHOO.lang.trim(this.widgets.searchBox.value);
                     if (ret != this.defaultSearchText) {
                        return ret;
                     }
                     return "";
                  },

                  /**
                   * Will trigger a search
                   * 
                   * @method onSearchChange
                   */
                  onSearchChange : function BeCPGCatalog_onSearchChange() {
                     this.searchTerm = this.getSearchText();
                     this.reloadDataTable();
                  },

                  onActionShowCharact : function BeCPGCatalog_onActionShowCharact(row) {

                     var p_record = this.widgets.alfrescoDataTable.getData(row);

                     var recordSiteName = $isValueSet(p_record.location.site) ? p_record.location.site : null;

                     window.location.href = beCPG.util.entityCharactURL(recordSiteName, p_record.nodeRef,
                           p_record.nodeType);
                  },

                  substitute : function BeCPGCatalog_substitute(text) {
                     
                     return YAHOO.lang.substitute(text, {
                        site : Alfresco.constants.SITE !== null && Alfresco.constants.SITE.length > 0 ? "."+Alfresco.constants.SITE: ""
                     });
                  }

               });
})();
