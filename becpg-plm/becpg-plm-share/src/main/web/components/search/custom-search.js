// Declare namespace...
(function() {

	/**
	 * YUI Library aliases
	 */
	var Dom = YAHOO.util.Dom, Event = YAHOO.util.Event;

	/**
	 * Alfresco Slingshot aliases
	 */
	var $html = Alfresco.util.encodeHTML;

	// Define constructor...
	beCPG.custom.Search = function CustomSearch_constructor(htmlId) {
		beCPG.custom.Search.superclass.constructor.call(this, htmlId);
		return this;
	};

	// Extend default Search...
	YAHOO
	      .extend(
	            beCPG.custom.Search,
	            Alfresco.Search,
	            {
	               options : {
	                  /**
							 * Current siteId
							 * 
							 * @property siteId
							 * @type string
							 */
	                  siteId : "",

	                  /**
							 * Current site title
							 * 
							 * @property siteTitle
							 * @type string
							 */
	                  siteTitle : "",

	                  /**
							 * Maximum number of results displayed.
							 * 
							 * @property maxSearchResults
							 * @type int
							 * @default 250
							 */
	                  maxSearchResults : 250,

	                  /**
							 * Results page size.
							 * 
							 * @property pageSize
							 * @type int
							 * @default 50
							 */
	                  pageSize : 50,

	                  /**
							 * Search term to use for the initial search
							 * 
							 * @property initialSearchTerm
							 * @type string
							 * @default ""
							 */
	                  initialSearchTerm : "",

	                  /**
							 * Search tag to use for the initial search
							 * 
							 * @property initialSearchTag
							 * @type string
							 * @default ""
							 */
	                  initialSearchTag : "",

	                  /**
							 * metadata to display
							 */
	                  metadataFields : "",

	                  /**
							 * States whether all sites should be searched.
							 * 
							 * @property initialSearchAllSites
							 * @type boolean
							 */
	                  initialSearchAllSites : true,

	                  /**
							 * States whether repository should be searched. This is in
							 * preference to current or all sites.
							 * 
							 * @property initialSearchRepository
							 * @type boolean
							 */
	                  initialSearchRepository : false,

	                  /**
							 * Sort property to use for the initial search. Empty
							 * default value will use score relevance default.
							 * 
							 * @property initialSort
							 * @type string
							 * @default ""
							 */
	                  initialSort : "",

	                  /**
							 * Advanced Search query - forms data json format based
							 * search.
							 * 
							 * @property searchQuery
							 * @type string
							 * @default ""
							 */
	                  searchQuery : "",

	                  /**
							 * Search root node.
							 * 
							 * @property searchRootNode
							 * @type string
							 * @default ""
							 */
	                  searchRootNode : "",

	                  /**
							 * Number of characters required for a search.
							 * 
							 * @property minSearchTermLength
							 * @type int
							 * @default 1
							 */
	                  minSearchTermLength : 1
	               },
	               /**
						 * Fired by YUI when parent element is available for
						 * scripting. Component initialisation, including
						 * instantiation of YUI widgets and event listener binding.
						 * 
						 * @method onReady
						 */
	               onReady : function Search_onReady() {
		               var me = this;

		               // DataSource definition
		               var uriSearchResults = Alfresco.constants.PROXY_URI_RELATIVE + "becpg/search?";
		               this.widgets.dataSource = new YAHOO.util.DataSource(uriSearchResults, {
		                  responseType : YAHOO.util.DataSource.TYPE_JSON,
		                  connXhrMode : "queueRequests",
		                  responseSchema : {
		                     resultsList : "items",
		                     metaFields : {
		                        page : "page",
		                        pageSize : "pageSize",
		                        fullListSize : "fullListSize"
		                     }
		                  }
		               });

		               // YUI Paginator definition
		               var handlePagination = function Search_handlePagination(state, me) {
			               me.currentPage = state.page;
			               me.widgets.paginator.setState(state);
			               YAHOO.Bubbling.fire("onSearch");
		               };
		               this.widgets.paginator = new YAHOO.widget.Paginator({
		                  containers : [ this.id + "-paginator-top", this.id + "-paginator-bottom" ],
		                  rowsPerPage : this.options.pageSize,
		                  initialPage : 1,
		                  template : this.msg("pagination.template"),
		                  pageReportTemplate : this.msg("pagination.template.page-report"),
		                  previousPageLinkLabel : this.msg("pagination.previousPageLinkLabel"),
		                  nextPageLinkLabel : this.msg("pagination.nextPageLinkLabel")
		               });
		               this.widgets.paginator.subscribe("changeRequest", handlePagination, this);

		               // setup of the datatable.
		               this._setupDataTable();

		               // set initial value and register the "enter" event on the
		               // search text field
		               var queryInput = Dom.get(this.id + "-search-text");
		               queryInput.value = this.options.initialSearchTerm;

		               this.widgets.enterListener = new YAHOO.util.KeyListener(queryInput, {
			               keys : YAHOO.util.KeyListener.KEY.ENTER
		               }, {
		                  fn : me._searchEnterHandler,
		                  scope : this,
		                  correctScope : true
		               }, "keydown").enable();

		               // trigger the initial search
		               YAHOO.Bubbling.fire("onSearch", {
		                  searchTerm : this.options.initialSearchTerm,
		                  searchTag : this.options.initialSearchTag,
		                  searchSort : this.options.initialSort,
		                  searchAllSites : this.options.initialSearchAllSites,
		                  searchRepository : this.options.initialSearchRepository
		               });

		               // toggle site scope links
		               var toggleLink = Dom.get(this.id + "-site-link");
		               Event.addListener(toggleLink, "click", this.onSiteSearch, this, true);
		               toggleLink = Dom.get(this.id + "-all-sites-link");
		               Event.addListener(toggleLink, "click", this.onAllSiteSearch, this, true);
		               toggleLink = Dom.get(this.id + "-repo-link");
		               Event.addListener(toggleLink, "click", this.onRepositorySearch, this, true);

		               // search YUI button
		               this.widgets.searchButton = Alfresco.util.createYUIButton(this, "search-button",
		                     this.onSearchClick);

		               // menu button for sort options
		               this.widgets.sortButton = new YAHOO.widget.Button(this.id + "-sort-menubutton", {
		                  type : "menu",
		                  menu : this.id + "-sort-menu",
		                  menualignment : [ "tr", "br" ],
		                  lazyloadmenu : false
		               });
		               // set initially selected sort button label
		               var menuItems = this.widgets.sortButton.getMenu().getItems();
		               for ( var m in menuItems) {
			               if (menuItems[m].value === this.options.initialSort) {
				               this.widgets.sortButton.set("label", this.msg("label.sortby", menuItems[m].cfg
				                     .getProperty("text")));
				               break;
			               }
		               }
		               // event handler for sort menu
		               this.widgets.sortButton.getMenu().subscribe("click", function(p_sType, p_aArgs) {
			               var menuItem = p_aArgs[1];
			               if (menuItem) {
				               me.refreshSearch({
					               searchSort : menuItem.value
				               });
			               }
		               });

		               // bulk-edit YUI button
		               this.widgets.bulkEditButton = Alfresco.util.createYUIButton(this, "bulk-edit",
		                     this.onBulkEditClick);
		               
		               if (Dom.get(this.id + "-wused")) {
			               this.widgets.wUsedButton = Alfresco.util.createYUIButton(this, "wused",
				                     this.onWUsedClick);
		               }

		               /*
							 * beCPG : export search in report
							 */
		               if (Dom.get(this.id + "-export-menubutton")) {
			               // menu button for export options
			               this.widgets.exportButton = new YAHOO.widget.Button(this.id + "-export-menubutton", {
			                  type : "menu",
			                  menu : this.id + "-export-menu",
			                  menualignment : [ "tr", "br" ],
			                  lazyloadmenu : false
			               });

			               // set initially selected export button label
			               var menuItems = this.widgets.exportButton.getMenu().getItems();
			               for ( var m in menuItems) {
				               if (menuItems[m].value === "-") {
					               this.widgets.exportButton.set("label", this.msg("label.export-search", menuItems[m].cfg
					                     .getProperty("text")));
					               break;
				               }
			               }
			               // event handler for export menu
			               this.widgets.exportButton.getMenu().subscribe("click", function(p_sType, p_aArgs) {
				               var menuItem = p_aArgs[1];
				               if (menuItem) {
					               me.exportSearch({
					                  reportTpl : menuItem.value,
					                  reportFileName : menuItem.srcElement.attributes["fileName"].value
					               });
				               }
			               });

		               }

		               // Hook action events
		               var fnActionHandler = function Search_fnActionHandler(layer, args) {
			               var owner = YAHOO.Bubbling.getOwnerByTagName(args[1].anchor, "span");
			               if (owner !== null) {
				               if (typeof me[owner.className] == "function") {
					               args[1].stop = true;
					               var tagId = owner.id.substring(me.id.length + 1);
					               me[owner.className].call(me, tagId);
				               }
			               }
			               return true;
		               };
		               YAHOO.Bubbling.addDefaultAction("search-tag", fnActionHandler);
		               YAHOO.Bubbling.addDefaultAction("search-prop", fnActionHandler);

		               // Finally show the component body here to prevent UI
		               // artifacts on YUI button decoration
		               Dom.setStyle(this.id + "-body", "visibility", "visible");
	               },

	               _setupDataTable : function Search_setupDataTable() {
		               /**
							 * DataTable Cell Renderers
							 * 
							 * Each cell has a custom renderer defined as a custom
							 * function. See YUI documentation for details. These MUST
							 * be inline in order to have access to the Alfresco.Search
							 * class (via the "me" variable).
							 */
		               var me = this;

		               /**
							 * Thumbnail custom datacell formatter
							 * 
							 * @method renderCellThumbnail
							 * @param elCell
							 *           {object}
							 * @param oRecord
							 *           {object}
							 * @param oColumn
							 *           {object}
							 * @param oData
							 *           {object|string}
							 */
		               renderCellThumbnail = function Search_renderCellThumbnail(elCell, oRecord, oColumn, oData) {
			               oColumn.width = 100;
			               Dom.setStyle(elCell.parentNode, "width", oColumn.width + "px");
			               Dom.setStyle(elCell.parentNode, "background-color", "#f4f4f4");
			               Dom.addClass(elCell, "thumbnail-cell");
			               if (oRecord.getData("type") === "document") {
				               Dom.addClass(elCell, "thumbnail");
			               }

			               elCell.innerHTML = me.buildThumbnailHtml(oRecord);
		               };

		               /**
							 * Description/detail custom cell formatter
							 * 
							 * @method renderCellDescription
							 * @param elCell
							 *           {object}
							 * @param oRecord
							 *           {object}
							 * @param oColumn
							 *           {object}
							 * @param oData
							 *           {object|string}
							 */
		               renderCellDescription = function Search_renderCellDescription(elCell, oRecord, oColumn, oData) {
			               // apply class to the appropriate TD cell
			               Dom.addClass(elCell.parentNode, "description");

			               // site and repository items render with different
			               // information available
			               var site = oRecord.getData("site");
			               var url = me._getBrowseUrlForRecord(oRecord);

			               // displayname and link to details page
			               var displayName = oRecord.getData("displayName");
			               var desc = '<h3 class="itemname"><a href="' + url + '" class="theme-color-1">'
			                     + $html(displayName) + '</a>';
			               // add title (if any) to displayname area
			               var title = oRecord.getData("title");
			               if (title && title !== displayName) {
				               desc += '<span class="title">(' + $html(title) + ')</span>';
			               }
			               desc += '</h3>';

			               // description (if any)
			               var txt = oRecord.getData("description");
			               if (txt) {
				               desc += '<div class="details meta">' + $html(txt) + '</div>';
			               }

			               // detailed information, includes site etc. type
			               // specific
			               desc += '<div class="details">';
			               var type = oRecord.getData("type");
			               desc += me.buildTextForType(type);

			               // link to the site and other meta-data details
			               if (site) {
				               desc += ' ' + me.msg("message.insite");
				               desc += ' <a href="' + Alfresco.constants.URL_PAGECONTEXT + 'site/' + $html(site.shortName)
				                     + '/dashboard">' + $html(site.title) + '</a>';
			               }
			               if (oRecord.getData("size") !== -1) {
				               desc += ' ' + me.msg("message.ofsize");
				               desc += ' <span class="meta">' + Alfresco.util.formatFileSize(oRecord.getData("size"))
				                     + '</span>';
			               }
			               if (oRecord.getData("modifiedBy")) {
				               desc += ' ' + me.msg("message.modifiedby");
				               desc += ' <a href="' + Alfresco.constants.URL_PAGECONTEXT + 'user/'
				                     + encodeURI(oRecord.getData("modifiedByUser")) + '/profile">'
				                     + $html(oRecord.getData("modifiedBy")) + '</a>';
			               }
			               desc += ' ' + me.msg("message.modifiedon") + ' <span class="meta">'
			                     + Alfresco.util.formatDate(oRecord.getData("modifiedOn")) + '</span>';
			               desc += '</div>';

			               // folder path (if any)
			               var path = oRecord.getData("path");
			               desc += me.buildPath(type, path, site);

			               // tags (if any)
			               var tags = oRecord.getData("tags");
			               if (tags && tags.length !== 0) {
				               var i, j;
				               desc += '<div class="details"><span class="tags">' + me.msg("label.tags") + ': ';
				               for (i = 0, j = tags.length; i < j; i++) {
					               desc += '<span id="' + me.id + '-' + $html(tags[i])
					                     + '" class="searchByTag"><a class="search-tag" href="#">' + $html(tags[i])
					                     + '</a> </span>';
				               }
				               desc += '</span></div>';
			               }
			               var itemType = oRecord.getData("itemType"), itemData = oRecord.getData("itemData");
			               if (itemData != null) {

				               for (key in itemData) {
					               var item = itemData[key];
					               var propName = key + (item.type == 'subtype' ? "_added" : "");

					               if (item.displayValue != null && item.displayValue.length > 0) {
						               desc += '<div class="details">' + $html(item.label) + ': ';
						               desc += '<span id="' + me.id + '|' + $html(propName) + '|' + $html(item.value) + '|'
						                     + $html(itemType) + '" class="searchByProp" ><a class="search-prop" href="#">'
						                     + $html(item.displayValue) + '</a></span>';
						               desc += '</div>';
					               } else if (item.length > 0) {
						               for (jj in item) {
							               if (jj == 0) {
								               desc += '<div class="details">' + $html(item[jj].label) + ': ';
							               } else {
								               desc += ",&nbsp;";
							               }
							               desc += '<span id="' + me.id + '|' + $html(propName) + '|' + $html(item[jj].value)
							                     + '|' + $html(itemType)
							                     + '" class="searchByProp" ><a class="search-prop" href="#">'
							                     + $html(item[jj].displayValue) + '</a></span>';

						               }
						               desc += '</div>';

					               }
				               }

			               }

			               elCell.innerHTML = desc;
		               };

		               // DataTable column defintions
		               var columnDefinitions = [ {
		                  key : "image",
		                  label : me.msg("message.preview"),
		                  sortable : false,
		                  formatter : renderCellThumbnail,
		                  width : 100
		               }, {
		                  key : "summary",
		                  label : me.msg("label.description"),
		                  sortable : false,
		                  formatter : renderCellDescription
		               } ];

		               // DataTable definition
		               this.widgets.dataTable = new YAHOO.widget.DataTable(this.id + "-results", columnDefinitions,
		                     this.widgets.dataSource, {
		                        renderLoopSize : Alfresco.util.RENDERLOOPSIZE,
		                        initialLoad : false,
		                        MSG_LOADING : ""
		                     });

		               // show initial message
		               this._setDefaultDataTableErrors(this.widgets.dataTable);
		               if (this.options.initialSearchTerm.length === 0 && this.options.initialSearchTag.length === 0) {
			               this.widgets.dataTable.set("MSG_EMPTY", "");
		               }

		               // Override abstract function within DataTable to set
		               // custom error message
		               this.widgets.dataTable.doBeforeLoadData = function Search_doBeforeLoadData(sRequest, oResponse,
		                     oPayload) {
			               if (oResponse.error) {
				               try {
					               var response = YAHOO.lang.JSON.parse(oResponse.responseText);
					               me.widgets.dataTable.set("MSG_ERROR", response.message);
				               } catch (e) {
					               me._setDefaultDataTableErrors(me.widgets.dataTable);
				               }

			               } else if (oResponse.results) {
				               // Pagination
				               me.resultsCount = oResponse.meta.fullListSize;
				               me.currentPage = oResponse.meta.page;

				               // clear the empty error message
				               me.widgets.dataTable.set("MSG_EMPTY", "");

				               // update the results count, update hasMoreResults.
				               me.hasMoreResults = (me.resultsCount > me.options.maxSearchResults);
				               if (me.hasMoreResults) {
					               oResponse.results = oResponse.results.slice(0, me.options.maxSearchResults);
					               me.resultsCount = me.options.maxSearchResults;
				               }

				               if (me.resultsCount > me.options.pageSize) {
					               Dom.removeClass(me.id + "-paginator-top", "hidden");
					               Dom.removeClass(me.id + "-search-bar-bottom", "hidden");
				               }

				               // display help text if no results were found
				               if (me.resultsCount === 0) {
					               Dom.removeClass(me.id + "-help", "hidden");
				               }
			               }
			               // Must return true to have the "Loading..." message
			               // replaced by the error message
			               return true;
		               };

		               // Rendering complete event handler
		               me.widgets.dataTable.subscribe("renderEvent", function() {
			               // Update the paginator
			               me.widgets.paginator.setState({
			                  page : me.currentPage,
			                  totalRecords : me.resultsCount
			               });
			               me.widgets.paginator.render();
		               });
	               },
	               /**
						 * Perform a search for a given prop value The tag is simply
						 * handled as search term
						 */
	               searchByProp : function Search_searchProp(param) {
		               var key = param.split("|")[0], value = param.split("|")[1];
		               type = param.split("|")[2];

		               this.refreshSearch({
		                  searchTag : "",
		                  searchTerm : this.searchTerm,
		                  searchQuery : encodeURIComponent("{\"" + key + "\":\"" + value + "\",\"datatype\":\"" + type
		                        + "\"}")
		               });
	               },
	               /**
						 * Export the search page by full URL refresh
						 * 
						 * @method exportSearch
						 * @param args
						 *           {object} search args
						 */
	               exportSearch : function Search_exportSearch(args) {
		               var searchTerm = this.searchTerm;
		               if (args.searchTerm !== undefined) {
			               searchTerm = args.searchTerm;
		               }
		               var searchTag = this.searchTag;
		               if (args.searchTag !== undefined) {
			               searchTag = args.searchTag;
		               }
		               // var searchAllSites = this.searchAllSites;
		               // if (args.searchAllSites !== undefined)
		               // {
		               // searchAllSites = args.searchAllSites;
		               // }
		               // var searchRepository = this.searchRepository;
		               // if (args.searchRepository !== undefined)
		               // {
		               // searchRepository = args.searchRepository;
		               // }
		               var searchSort = this.searchSort;
		               if (args.searchSort !== undefined) {
			               searchSort = args.searchSort;
		               }
		               var searchQuery = this.options.searchQuery;
		               if (args.searchQuery !== undefined) {
			               searchQuery = args.searchQuery;
		               }

		               // call webscript
		               var url = Alfresco.constants.PROXY_URI + "becpg/report/exportsearch/"
		                     + args.reportTpl.replace("://", "/") + "/" + args.reportFileName;

		               // add search data webscript arguments
		               url += "?term=" + encodeURIComponent(searchTerm);
		               if (searchSort.length !== 0) {
			               url += "&sort=" + searchSort;
		               }
		               if (searchQuery.length !== 0) {
			               url += "&query=" + encodeURI(searchQuery);
		               } else {
			               if (searchTag.length !== 0) {
				               url += "&tag=" + encodeURIComponent(searchTag);
			               }
		               }
		               /*
							 * if(this.options.siteId.length !== 0) { url += "&site=" +
							 * siteId + "&repo=false"; } else
							 */
		               {
			               url += "&site=&repo=true";
		               }

		               window.location = url;
	               },
	               
	               
	               onBulkEditClick : function Search_onBulkEditClick(e, obj) {

		               // redirect back to the search page - with appropriate site
		               // context
		               var url = Alfresco.constants.URL_PAGECONTEXT;

		               // add search data webscript arguments
		               url += "bulk-edit" + location.search;

		               window.location = url;
	               },
	               
	               onWUsedClick :  function Search_onWUsedClick(e, obj) {

		               // redirect back to the search page - with appropriate site
		               // context
		               var url = Alfresco.constants.URL_PAGECONTEXT;

		               // add search data webscript arguments
		               url += "wused" + location.search;

		               window.location = url;
	               },
	               
	               /**
						 * Build URI parameter string for search JSON data webscript
						 * 
						 * @method _buildSearchParams
						 */
	               _buildSearchParams : function Search__buildSearchParams(searchRepository, searchAllSites, searchTerm,
	                     searchTag, searchSort) {
		               var site = searchAllSites ? "" : this.options.siteId;
		               var params = YAHOO.lang
		                     .substitute(
		                           "site={site}&term={term}&tag={tag}&maxResults={maxResults}&sort={sort}&query={query}&repo={repo}&metadataFields={metadataFields}&page={page}&pageSize={pageSize}&rootNode={rootNode}",
		                           {
		                              site : encodeURIComponent(site),
		                              repo : searchRepository.toString(),
		                              term : encodeURIComponent(searchTerm),
		                              tag : encodeURIComponent(searchTag),
		                              sort : encodeURIComponent(searchSort),
		                              query : encodeURIComponent(this.options.searchQuery),
		                              rootNode : encodeURIComponent(this.options.searchRootNode),
		                              metadataFields : encodeURIComponent(this.options.metadataFields),
		                              page : this.currentPage,
		                              pageSize : this.options.pageSize,
		                              maxResults : this.options.maxSearchResults + 1
		                           // to calculate whether more results were
		                           // available
		                           });

		               return params;
	               }

	            });
})();
