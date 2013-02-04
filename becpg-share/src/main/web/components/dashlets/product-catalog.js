/**
 * Dashboard ProductCatalog component.
 *
 * @namespace beCPG
 * @class beCPG.dashlet.ProductCatalog
 */
(function()
{
   /**
    * YUI Library aliases
    */
   var Dom = YAHOO.util.Dom,
      Event = YAHOO.util.Event,
      Selector = YAHOO.util.Selector;
   /**
   * Alfresco Slingshot aliases
   */
   var $html = Alfresco.util.encodeHTML,
   		$siteURL = Alfresco.util.siteURL,
         $userProfile = Alfresco.util.userProfileLink,
         $siteDashboard = Alfresco.util.siteDashboardLink,
         $relTime = Alfresco.util.relativeTime,
         $isValueSet = Alfresco.util.isValueSet;

   /**
    * Preferences
    */
   var PREFERENCES_PRODUCTCATALOG_DASHLET = "org.alfresco.share.productcatalog.dashlet",
      PREFERENCES_PRODUCTCATALOG_DASHLET_FILTER = PREFERENCES_PRODUCTCATALOG_DASHLET + ".filter",
      PREFERENCES_PRODUCTCATALOG_DASHLET_VIEW = PREFERENCES_PRODUCTCATALOG_DASHLET + ".simpleView";
   
   var CHARACT_EVENTCLASS = Alfresco.util.generateDomId(null, "charact");

   /**
    * Dashboard ProductCatalog constructor.
    *
    * @param {String} htmlId The HTML id of the parent element
    * @return {beCPG.dashlet.ProductCatalog} The new component instance
    * @constructor
    */
   beCPG.dashlet.ProductCatalog = function ProductCatalog_constructor(htmlId)
   {
      return beCPG.dashlet.ProductCatalog.superclass.constructor.call(this, htmlId);
   };

   YAHOO.extend(beCPG.dashlet.ProductCatalog, Alfresco.component.SimpleDocList,
   {
   	searchTerm: null,
   	
   	
      /**
       * Fired by YUI when parent element is available for scripting
       * @method onReady
       */
      onReady: function ProductCatalog_onReady()
      {
      	var me = this;
         // Create Dropdown filter
         this.widgets.filter = Alfresco.util.createYUIButton(this, "filters", this.onFilterChange,
         {
            type: "menu",
            menu: "filters-menu",
            lazyloadmenu: false
         });

         // Select the preferred filter in the ui
         var filter = this.options.filter;
         filter = Alfresco.util.arrayContains(this.options.validFilters, filter) ? filter : this.options.validFilters[0];
         this.widgets.filter.set("label", this.msg("filter." + filter));
         this.widgets.filter.value = filter;

         // Detailed/Simple List button
         this.widgets.simpleDetailed = new YAHOO.widget.ButtonGroup(this.id + "-simpleDetailed");
         if (this.widgets.simpleDetailed !== null)
         {
            this.widgets.simpleDetailed.check(this.options.simpleView ? 0 : 1);
            this.widgets.simpleDetailed.on("checkedButtonChange", this.onSimpleDetailed, this.widgets.simpleDetailed, this);
         }

         this.configureSearch();
         
         // Display the toolbar now that we have selected the filter
         Dom.removeClass(Selector.query(".toolbar div", this.id, true), "hidden");
         
         var fnOnShowCharactHandler = function ProductCatalog__fnOnShowCharactHandler(layer, args)
         {
            var owner = YAHOO.Bubbling.getOwnerByTagName(args[1].anchor, "div");
            if (owner !== null)
            {
               me.onActionShowCharact.call(me, args[1].target.offsetParent, owner);
            }
            return true;
         };
         YAHOO.Bubbling.addDefaultAction(CHARACT_EVENTCLASS, fnOnShowCharactHandler);
         

         // DataTable can now be rendered
         beCPG.dashlet.ProductCatalog.superclass.onReady.apply(this, arguments);
      },


      /**
       * Generate base webscript url.
       * Can be overridden.
       *
       * @method getWebscriptUrl
       */
      getWebscriptUrl: function ProductCatalog_getWebscriptUrl()
      {
         return Alfresco.constants.PROXY_URI + "slingshot/doclib/doclist/product/node/alfresco/company/home?max=50";
      },

      /**
       * Calculate webscript parameters
       *
       * @method getParameters
       * @override
       */
      getParameters: function ProductCatalog_getParameters()
      {
      	var parameters = "type=product&filter=" + this.widgets.filter.value;
      	
      	if(this.searchTerm!=null && this.searchTerm.length>0){
      		parameters+="&searchTerm="+this.searchTerm;
      	}
      		
         return parameters;
      },

      /**
       * Filter Change menu handler
       *
       * @method onFilterChange
       * @param p_sType {string} The event
       * @param p_aArgs {array}
       */
      onFilterChange: function ProductCatalog_onFilterChange(p_sType, p_aArgs)
      {
         var menuItem = p_aArgs[1];
         if (menuItem)
         {
            this.widgets.filter.set("label", menuItem.cfg.getProperty("text"));
            this.widgets.filter.value = menuItem.value;

            this.services.preferences.set(PREFERENCES_PRODUCTCATALOG_DASHLET_FILTER, this.widgets.filter.value);

            var searchText = this.getSearchText();
            if (searchText.replace(/\*/g, "").length < 3)
            {
            	this.searchTerm = null;
            }
            else  {
            	this.searchTerm = searchText;
            	
            }

            this.reloadDataTable();
         }
      },

      /**
       * Show/Hide detailed list buttongroup click handler
       *
       * @method onSimpleDetailed
       * @param e {object} DomEvent
       * @param p_obj {object} Object passed back from addListener method
       */
      onSimpleDetailed: function ProductCatalog_onSimpleDetailed(e, p_obj)
      {
         this.options.simpleView = e.newValue.index === 0;
         this.services.preferences.set(PREFERENCES_PRODUCTCATALOG_DASHLET_VIEW, this.options.simpleView);
         if (e)
         {
            Event.preventDefault(e);
         }

         this.reloadDataTable();
      },
      /**
       * Thumbnail custom datacell formatter
       *
       * @method renderCellThumbnail
       * @param elCell {object}
       * @param oRecord {object}
       * @param oColumn {object}
       * @param oData {object|string}
       */
      renderCellThumbnail: function SimpleDocList_renderCellThumbnail(elCell, oRecord, oColumn, oData)
      {
         var columnWidth = 40,
            record = oRecord.getData(),
            desc = "";
         
         record.jsNode = {};
         record.jsNode.type = record.nodeType;

         if (record.isInfo)
         {
            columnWidth = 52;
            desc = '<img src="' + Alfresco.constants.URL_RESCONTEXT + 'components/images/help-docs-bw-32.png" />';
         }
         else
         {
            var name = record.fileName,
               extn = name.substring(name.lastIndexOf(".")),
               locn = record.location,
               nodeRef = new Alfresco.util.NodeRef(record.nodeRef),
               docDetailsUrl = (locn.site != null && locn.site != "") ? Alfresco.constants.URL_PAGECONTEXT + "site/" + locn.site + "/document-details?nodeRef=" + nodeRef.toString() : Alfresco.constants.URL_PAGECONTEXT + "document-details?nodeRef=" + nodeRef.toString();

            if (this.options.simpleView)
            {
               /**
                * Simple View
                */
               var id = this.id + '-preview-' + oRecord.getId();
               desc = '<span id="' + id + '" class="icon32"><a href="' + docDetailsUrl + '"><img src="' +beCPG.util.getFileIcon(name,record,false,true) + '" alt="' + extn + '" title="' + $html(name) + '" /></a></span>';

               // Preview tooltip
               this.previewTooltips.push(id);
            }
            else
            {
               /**
                * Detailed View
                */
               columnWidth = 100;
               desc = '<span class="thumbnail"><a href="' + docDetailsUrl + '"><img src="' + Alfresco.constants.PROXY_URI + 'api/node/' + nodeRef.uri + '/content/thumbnails/doclib?c=queue&ph=true" alt="' + extn + '" title="' + $html(name) + '" /></a></span>';
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
      * @param elCell {object}
      * @param oRecord {object}
      * @param oColumn {object}
      * @param oData {object|string}
      */
     renderCellDetail: function ProductCatalog_renderCellDetail(elCell, oRecord, oColumn, oData)
     {
        var record = oRecord.getData(),
           desc = "";

        if (record.isInfo)
        {
           desc += '<div class="empty"><h3>' + record.title + '</h3>';
           desc += '<span>' + record.description + '</span></div>';
        }
        else
        {
           var id = this.id + '-metadata-' + oRecord.getId(),
              version = "",
              dateLine = "",
              locn = record.location,
              nodeRef = new Alfresco.util.NodeRef(record.nodeRef),
              docDetailsUrl = (locn.site != null && locn.site != "") ? Alfresco.constants.URL_PAGECONTEXT + "site/" + locn.site + "/document-details?nodeRef=" + nodeRef.toString() : Alfresco.constants.URL_PAGECONTEXT + "document-details?nodeRef=" + nodeRef.toString(),
              contentUrl = Alfresco.constants.PROXY_URI + record.contentUrl;

           // Version display
           if (record.version && record.version !== "")
           {
              version = '<span class="document-version">' + $html(record.version) + '</span>';
           }
           
           // Date line
           var dateI18N = "modified", dateProperty = record.modifiedOn;
           if (record.custom && record.custom.isWorkingCopy)
           {
              dateI18N = "editing-started";
           }
           else if (record.modifiedOn === record.createdOn)
           {
              dateI18N = "created";
              dateProperty = record.createdOn;
           }
           if (Alfresco.constants.SITE === "")
           {
              dateLine = this.msg("details." + dateI18N + "-in-site", $relTime(dateProperty), $siteDashboard(locn.site, locn.siteTitle, 'class="site-link theme-color-1" id="' + id + '"'));
           }
           else
           {
              dateLine = this.msg("details." + dateI18N + "-by", $relTime(dateProperty), $userProfile(record.modifiedByUser, record.modifiedBy, 'class="theme-color-1"'));
           }

           if (this.options.simpleView)
           {
              /**
               * Simple View
               */
              desc += '<h3 class="filename simple-view"><a class="theme-color-1" href="' + docDetailsUrl + '">' + $html(record.displayName) + '</a></h3>';
              desc += '<div class="detail"><span class="item-simple">' + dateLine + '</span></div>';
           }
           else
           {
              /**
               * Detailed View
               */
              desc += '<h3 class="filename"><a class="theme-color-1" href="' + docDetailsUrl + '">' + $html(record.displayName) + '</a>' + version + '</h3>';

              desc += '<div class="detail">';
              desc +=    '<span class="item">' + dateLine + '</span>';
              desc += '</div>';

              /* Favourite / Charact / Download */
              desc += '<div class="detail detail-social">';
              desc +=    '<span class="item item-social">' + Alfresco.component.SimpleDocList.generateFavourite(this, oRecord) + '</span>';
              desc +=    '<span class="item item-social item-separator"><a class="document-download" href="' + contentUrl + '"  title="' + this.msg( "actions.document.download") + '" tabindex="0">' +  this.msg("actions.document.download") + '</a></span>';
              desc +=    '<span class="item item-social item-separator"><a class="document-characts ' + CHARACT_EVENTCLASS + '" title="' + this.msg( "actions.entity.view-datalist") + '" tabindex="0">' + this.msg("actions.entity.view-datalist") + '</a></span>';
              desc += '</div>';
           }
           
           // Metadata tooltip
           this.metadataTooltips.push(id);
        }

        elCell.innerHTML = desc;
     },
      
      /**
       * Search Handlers
       */
      
      /**
       * Configure search area
       *
       * @method configureSearch
       */
      configureSearch: function ProductCatalog_configureSearch()
      {
         this.widgets.searchBox = Dom.get(this.id + "-searchText");
         this.defaultSearchText = this.msg("header.search.default");
         
         Event.addListener(this.widgets.searchBox, "focus", this.onSearchFocus, null, this);
         Event.addListener(this.widgets.searchBox, "blur", this.onSearchBlur, null, this);
         Event.addListener(this.widgets.searchBox, "change", this.onSearchChange, null, this);
         
         
         this.setDefaultSearchText();
         
         this.widgets.searchMore = new YAHOO.widget.Button(this.id + "-search_more",
         {
            type: "menu",
            menu: this.id + "-searchmenu_more"
         });
      }
,
      
      /**
       * Update image class when search box has focus.
       *
       * @method onSearchFocus
       */
      onSearchFocus: function ProductCatalog_onSearchFocus()
      {
         if (this.widgets.searchBox.value == this.defaultSearchText)
         {
            Dom.removeClass(this.widgets.searchBox, "faded");
            this.widgets.searchBox.value = "";
         }
         else
         {
            this.widgets.searchBox.select();
         }
      },
      
      /**
       * Set default search text when box loses focus and is empty.
       *
       * @method onSearchBlur
       */
      onSearchBlur: function ProductCatalog_onSearchBlur()
      {
         var searchText = YAHOO.lang.trim(this.widgets.searchBox.value);
         if (searchText.length === 0)
         {
            /**
             * Since the blur event occurs before the KeyListener gets
             * the enter we give the enter listener a chance of testing
             * against "" instead of the help text.
             */
            YAHOO.lang.later(100, this, this.setDefaultSearchText, []);
         }
      }, 
      
      /**
       * Set default search text for search box.
       *
       * @method setDefaultSearchText
       */
      setDefaultSearchText: function ProductCatalog_setDefaultSearchText()
      {
         Dom.addClass(this.widgets.searchBox, "faded");
         this.widgets.searchBox.value = this.defaultSearchText;
      },

      /**
       * Get current search text from search box.
       *
       * @method getSearchText
       */
      getSearchText: function ProductCatalog_getSearchText()
      {	
      	
         var ret =  YAHOO.lang.trim(this.widgets.searchBox.value);
         if(ret!=this.defaultSearchText){
         	return ret;
         }
         return "";
      },
      
      /**
       * Will trigger a search
       *
       * @method onSearchChange
       */
      onSearchChange: function ProductCatalog_onSearchChange()
      {
      	
      
         var searchText = this.getSearchText();
         if (searchText.replace(/\*/g, "").length < 3 || searchText.replace(/\*/g, "").length == 0)
         {
            Alfresco.util.PopupManager.displayMessage(
            {
               text: this.msg("message.minimum-length", 3)
            });
         }
         else
         {
         	this.searchTerm = searchText;
         	
         	this.reloadDataTable();
         }
      },
      
      onActionShowCharact: function ProductCatalog_onActionShowCharact(row) {
	   	
      	var p_record = this.widgets.alfrescoDataTable.getData(row),
         nodeRef = new Alfresco.util.NodeRef(p_record.nodeRef);
      	
	   	var recordSiteName = $isValueSet(p_record.location.site) ? p_record.location.site : null;
	   	var redirect  = $siteURL("entity-data-lists?nodeRef="+nodeRef,
         {
            site: recordSiteName
         });
	   	
	   	if(p_record.nodeType == "bcpg:finishedProduct" || p_record.nodeType == "bcpg:semiFinishedProduct"){
	   		redirect+="&list=compoList";
	   	}
			else if(p_record.nodeType == "bcpg:packagingKit"){
				redirect+="&list=packagingList";
			}
	   	
	   	window.location.href = redirect;
      }

      
   });
})();



















