// Declare namespace...
(function() {

	/**
	 * YUI Library aliases
	 */
	var Dom = YAHOO.util.Dom, Event = YAHOO.util.Event, Selector = YAHOO.util.Selector, Bubbling = YAHOO.Bubbling;

	/**
	 * Alfresco Slingshot aliases
	 */
	var $html = Alfresco.util.encodeHTML;

	// Define constructor...
	beCPG.custom.Search = function CustomSearch_constructor(htmlId) {
		beCPG.custom.Search.superclass.constructor.call(this, htmlId);
		this.selectedItems = {};

		YAHOO.Bubbling.on("selectedItemsChanged", this.onSelectedItemsChanged, this);
		YAHOO.Bubbling.on("registerAction", this.onRegisterAction, this);

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
							 * States whether repository should be searched.
							 * This is in preference to current or all sites.
							 * 
							 * @property initialSearchRepository
							 * @type boolean
							 */
							initialSearchRepository : false,

							/**
							 * Sort property to use for the initial search.
							 * Empty default value will use score relevance
							 * default.
							 * 
							 * @property initialSort
							 * @type string
							 * @default ""
							 */
							initialSort : "",

							/**
							 * Advanced Search query - forms data json format
							 * based search.
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
						 * instantiation of YUI widgets and event listener
						 * binding.
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

							// set initial value and register the "enter" event
							// on the
							// search text field
							var queryInput = Dom.get(this.id + "-search-text");
							queryInput.value = this.options.initialSearchTerm;

							// this.widgets.enterListener = new
							// YAHOO.util.KeyListener(queryInput, {
							// keys : YAHOO.util.KeyListener.KEY.ENTER
							// }, {
							// fn : me._searchEnterHandler,
							// scope : this,
							// correctScope : true
							// }, "keydown").enable();

							// this.widgets.enterListener.enable();

							// search YUI button
							// this.widgets.searchButton =
							// Alfresco.util.createYUIButton(this,
							// "search-button", this.onSearchClick);
							// this._disableItems();

							// trigger the initial search
							YAHOO.Bubbling.fire("onSearch", {
								searchTerm : this.options.initialSearchTerm,
								searchTag : this.options.initialSearchTag,
								searchSort : this.options.initialSort,
								searchAllSites : this.options.initialSearchAllSites,
								searchRepository : this.options.initialSearchRepository
							});

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
									this.widgets.sortButton.set("label", this.msg("label.sortby", menuItems[m].cfg.getProperty("text")) + " "
											+ Alfresco.constants.MENU_ARROW_SYMBOL);
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
							this.widgets.bulkEditButton = Alfresco.util.createYUIButton(this, "bulk-edit", this.onBulkEditClick);

							if (Dom.get(this.id + "-wused")) {
								this.widgets.wUsedButton = Alfresco.util.createYUIButton(this, "wused", this.onWUsedClick);
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
										this.widgets.exportButton.set("label", this.msg("label.export-search", menuItems[m].cfg.getProperty("text"))
												+ " " + Alfresco.constants.MENU_ARROW_SYMBOL);
										break;
									}
								}
								// event handler for export menu
								this.widgets.exportButton.getMenu().subscribe("click", function(p_sType, p_aArgs) {
									var menuItem = p_aArgs[1];
									if (menuItem) {
										me.exportSearch({
											reportTpl : menuItem.value,
											reportFileName : menuItem.srcElement.attributes["fileName"].value,
											reportTplName : menuItem.srcElement.attributes["reportTplName"].value
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

							// Item Select menu button
							this.widgets.itemSelect = Alfresco.util.createYUIButton(this, "itemSelect-button", this.onItemSelect, {
								type : "menu",
								menu : "itemSelect-menu",
								disabled : false
							});

							// Selected Items menu button
							this.widgets.selectedItems = Alfresco.util.createYUIButton(this, "selectedItems-button", this.onSelectedItems, {
								type : "menu",
								menu : "selectedItems-menu",
								lazyloadmenu : false,
								disabled : true
							});

							

							// DocLib Actions module
					        this.modules.actions = new Alfresco.module.DoclibActions();
							
							// Finally show the component body here to prevent
							// UI
							// artifacts on YUI button decoration
							Dom.setStyle(this.id + "-body", "visibility", "visible");
						},
						/**
						 * Refresh the search page by full URL refresh
						 * 
						 * @method refreshSearch
						 * @param args
						 *            {object} search args
						 */
						refreshSearch : function Search_refreshSearch(args) {
							var searchTerm = this.searchTerm;
							if (args.searchTerm !== undefined) {
								searchTerm = args.searchTerm;
							}
							var searchTag = this.searchTag;
							if (args.searchTag !== undefined) {
								searchTag = args.searchTag;
							}
							var searchAllSites = this.searchAllSites;
							if (args.searchAllSites !== undefined) {
								searchAllSites = args.searchAllSites;
							}
							var searchRepository = this.searchRepository;
							if (args.searchRepository !== undefined) {
								searchRepository = args.searchRepository;
							}
							var searchSort = this.searchSort;
							if (args.searchSort !== undefined) {
								searchSort = args.searchSort;
							}
							var searchQuery = this.options.searchQuery;
							if (args.searchQuery !== undefined) {
								searchQuery = args.searchQuery;
							}

							// redirect back to the search page - with
							// appropriate site context
							var url = Alfresco.constants.URL_PAGECONTEXT;
							if (this.options.siteId.length !== 0) {
								url += "site/" + this.options.siteId + "/";
							}

							// add search data webscript arguments
							url += "search?t=" + encodeURIComponent(searchTerm);
							url += "&s=" + encodeURIComponent(searchSort);
							if (searchQuery.length !== 0) {
								// if we have a query (already encoded), then
								// apply it
								// most other options such as tag, terms are
								// trumped
								url += "&q=" + encodeURIComponent(searchQuery);
							} else if (searchTag.length !== 0) {
								url += "&tag=" + encodeURIComponent(searchTag);
							}
							url += "&a=" + searchAllSites + "&r=" + searchRepository;
							window.location = url;
						},

						_setupDataTable : function Search_setupDataTable() {
							/**
							 * DataTable Cell Renderers
							 * 
							 * Each cell has a custom renderer defined as a
							 * custom function. See YUI documentation for
							 * details. These MUST be inline in order to have
							 * access to the Alfresco.Search class (via the "me"
							 * variable).
							 */
							var me = this;

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
							renderCellThumbnail = function Search_renderCellThumbnail(elCell, oRecord, oColumn, oData) {
								oColumn.width = 100;
								Dom.setStyle(elCell.parentNode, "width", oColumn.width + "px");
								//Dom.setStyle(elCell.parentNode, "background-color", "#f4f4f4");
								Dom.addClass(elCell, "thumbnail-cell");
								if (oRecord.getData("type") === "document") {
									Dom.addClass(elCell, "thumbnail");
								}

								elCell.innerHTML = me.buildThumbnailHtml(oRecord);
							};

							renderCellSelected = function(elCell, oRecord, oColumn, oData) {
								Dom.setStyle(elCell, "width", oColumn.width + "px");
								Dom.setStyle(elCell.parentNode, "width", oColumn.width + "px");

								elCell.innerHTML = '<input id="checkbox-' + oRecord.getId() + '" type="checkbox" name="fileChecked" value="' + oData
										+ '"' + (me.selectedItems[oData] ? ' checked="checked">' : '>');

							};

							/**
							 * Description/detail custom cell formatter
							 * 
							 * @method renderCellDescription
							 * @param elCell
							 *            {object}
							 * @param oRecord
							 *            {object}
							 * @param oColumn
							 *            {object}
							 * @param oData
							 *            {object|string}
							 */
							renderCellDescription = function Search_renderCellDescription(elCell, oRecord, oColumn, oData) {
								// apply class to the appropriate TD cell
								Dom.addClass(elCell.parentNode, "description");

								// site and repository items render with
								// different
								// information available
								var site = oRecord.getData("site");
								var url = me._getBrowseUrlForRecord(oRecord);

								// displayname and link to details page
								var displayName = oRecord.getData("displayName");

								var metadata = oRecord.getData("metadata");
								
								const entityStateProps = [
										"prop_bcpg_productState",
								 		"prop_bcpg_supplierState",
								        "prop_bcpg_clientState",
								        "prop_bcpg_documentState"
								];
								
								const effectivityProps = [
										"prop_cm_from",
								 		"prop_cm_to"
								];

								var desc = '<span class="itemname ' + (metadata ? (' ' + metadata) : '') + '"><a href="' + url
										+ '" class="theme-color-1">' + $html(displayName);
										
										
								var itemData = oRecord.getData("itemData");
								
								if (itemData != null) {
								    var stateToDisplay = null;
								
								    for (var i = 0; i < entityStateProps.length; i++) {
								        var entityStateProp = entityStateProps[i];
								        if (itemData[entityStateProp] != null && itemData[entityStateProp].value != null) {
								            stateToDisplay = itemData[entityStateProp];
								            break;
								        }
								    }
								
								    if (stateToDisplay != null) {
								        desc += ' <span class="product-state entity-' + stateToDisplay.value + '">[' + stateToDisplay.displayValue + ']</span>';
								    }
								}
										
								desc += '</a>';
								// add title (if any) to displayname area
								var title = oRecord.getData("title");
								if (title && title !== displayName) {
									desc += '&nbsp;<span class="title">(' + $html(title) + ')</span>';
								}

								desc += '</span>';
								
								
								// Nutriscore
								if(oRecord.getData("itemData")!=null &&  oRecord.getData("itemData")["prop_bcpg_nutrientProfilingClass"]!=null
										&&  oRecord.getData("itemData")["prop_bcpg_nutrientProfilingClass"].value!=null){
									var nutriScore = oRecord.getData("itemData")["prop_bcpg_nutrientProfilingClass"].value;
									desc += '<div class="nutriscore"><span class="nutrient-class">'
							      	if (nutriScore != "" 
							      	   && (
							      			 nutriScore == "A"
							      	       || nutriScore == "B"
							      	       || nutriScore == "C"
							      	       || nutriScore == "D"
							      	       || nutriScore == "E")){
							      		 desc += '<span class="'+( nutriScore == "A"? 'selected ':'' )+'nutrient-class-a">A</span>';
								         desc += '<span class="'+( nutriScore == "B"? 'selected ':'' )+'nutrient-class-b">B</span>';
								         desc += '<span class="'+( nutriScore == "C"? 'selected ':'' )+'nutrient-class-c">C</span>';
								         desc += '<span class="'+( nutriScore == "D"? 'selected ':'' )+'nutrient-class-d">D</span>';
								         desc += '<span class="'+( nutriScore == "E"? 'selected ':'' )+'nutrient-class-e">E</span>';
							      	}
									desc += '</span></div>';
								} 
								

								// description (if any)
								var txt = oRecord.getData("description");
								if (txt) {
									desc += '<div class="details meta ">';
									
									if(txt.length>200){
										desc += $html(txt.substring(0,200).trim())+"...";
									}  else {
										desc += $html(txt);
									}
									desc +='</div>';
								}

								// detailed information, includes site etc. type
								// specific
								desc += '<div class="details">';
								var type = oRecord.getData("type");
								desc += me.buildTextForType(type);

								// link to the site and other meta-data details
								if (site) {
									desc += ' ' + me.msg("message.insite");
									desc += ' <a href="' + Alfresco.constants.URL_PAGECONTEXT + 'site/' + $html(site.shortName) + '/dashboard">'
											+ $html(site.title) + '</a>';
								}
								if (oRecord.getData("size") !== -1) {
									desc += ' ' + me.msg("message.ofsize");
									desc += ' <span class="meta">' + Alfresco.util.formatFileSize(oRecord.getData("size")) + '</span>';
								}
								if (oRecord.getData("modifiedBy")) {
									desc += ' ' + me.msg("message.modifiedby");
									desc += ' <a href="' + Alfresco.constants.URL_PAGECONTEXT + 'user/'
											+ encodeURI(oRecord.getData("modifiedByUser")) + '/profile">' + $html(oRecord.getData("modifiedBy"))
											+ '</a>';
								}
								desc += ' ' + me.msg("message.modifiedon") + ' <span class="meta">'
										+ oRecord.getData("modifiedOn") + '</span>';
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
										desc += '<span id="' + me.id + '-' + $html(tags[i]) + '" class="searchByTag"><a class="search-tag" href="#">'
												+ $html(tags[i]) + '</a> </span>';
									}
									desc += '</span></div>';
								}
								var itemType = oRecord.getData("itemType"), itemData = oRecord.getData("itemData");
								if (itemData != null) {
									for (key in itemData) {
										if(key != null && entityStateProps.indexOf(key) == -1 && effectivityProps.indexOf(key) == -1 && key !="prop_bcpg_nutrientProfilingClass"){
											
											var item = itemData[key];
											var propName = key + (item.type == 'subtype' ? "_added" : "");
	
											if (item.displayValue != null && item.displayValue.length > 0) {
												desc += '<div class="details">' + $html(item.label) + ': ';
												
													desc += '<span id="' + me.id + '|' + $html(propName) + '|' + $html(item.value) + '|' + $html(itemType)
															+ '" class="searchByProp" ><a class="search-prop" href="#">' + $html(item.displayValue)
															+ '</a></span>';
												desc += '</div>';
											} else if (item.length > 0) {
												for (jj in item) {
													if (jj == 0) {
														desc += '<div class="details">' + $html(item[jj].label) + ': ';
													} else {
														desc += ",&nbsp;";
													}
													desc += '<span id="' + me.id + '|' + $html(propName) + '|' + $html(item[jj].value) + '|'
															+ $html(itemType) + '" class="searchByProp" ><a class="search-prop" href="#">'
															+ $html(item[jj].displayValue) + '</a></span>';
	
												}
												desc += '</div>';
	
											}
										}
									}
								
								    for (var i = 0; i < effectivityProps.length; i++) {
								        var effectivityProp = effectivityProps[i];
								        var effectivityItem = itemData[effectivityProp];
								        if (effectivityItem != null && effectivityItem.value != null) {
											
								            desc += '<div class="details">' + $html(effectivityItem.label) + ': ';
											
											desc += '<span id="' + me.id + '|' + $html(effectivityProp) + '|' + $html(effectivityItem.value) + '|' + $html(itemType)
													+ '" class="searchByProp" ><a class="search-prop" href="#">' + $html(effectivityItem.displayValue)
													+ '</a></span>';
											desc += '</div>';
								        }
								    }
								}

								elCell.innerHTML = desc;
							};

							// DataTable column defintions
							var columnDefinitions = [ {
								key : "nodeRef",
								label : "",
								sortable : false,
								formatter : renderCellSelected,
								width : 16,
								className : "search-select-cell-adv"
							},

							{
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
							this.widgets.dataTable = new YAHOO.widget.DataTable(this.id + "-results", columnDefinitions, this.widgets.dataSource, {
								renderLoopSize : Alfresco.util.RENDERLOOPSIZE,
								initialLoad : false,
								MSG_LOADING : ""
							});

							// show initial message
							this._setDefaultDataTableErrors(this.widgets.dataTable);
							if (this.options.initialSearchTerm.length === 0 && this.options.initialSearchTag.length === 0) {
								this.widgets.dataTable.set("MSG_EMPTY", "");
							}

							// Override abstract function within DataTable to
							// set
							// custom error message
							this.widgets.dataTable.doBeforeLoadData = function Search_doBeforeLoadData(sRequest, oResponse, oPayload) {
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

									// update the results count, update
									// hasMoreResults.
									me.hasMoreResults = (me.resultsCount > me.options.maxSearchResults);
									if (me.hasMoreResults) {
										oResponse.results = oResponse.results.slice(0, me.options.maxSearchResults);
										me.resultsCount = me.options.maxSearchResults;
									}

									if (me.resultsCount > me.options.pageSize) {
										Dom.removeClass(me.id + "-paginator-top", "hidden");
										Dom.removeClass(me.id + "-search-bar-bottom", "hidden");
									}

									// display help text if no results were
									// found
									if (me.resultsCount === 0) {
										Dom.removeClass(me.id + "-help", "hidden");
									}
								}
								// Must return true to have the "Loading..."
								// message
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

							// File checked handler
							me.widgets.dataTable.subscribe("checkboxClickEvent", function(e) {
								var id = e.target.value;
								me.selectedItems[id] = e.target.checked;
								Bubbling.fire("selectedItemsChanged");
							}, this, true);

						},
						/**
						 * Perform a search for a given prop value The tag is
						 * simply handled as search term
						 */
						searchByProp : function Search_searchProp(param) {
							var key = param.split("|")[0], value = param.split("|")[1];
							type = param.split("|")[2];

							this.refreshSearch({
								searchTag : "",
								searchTerm : this.searchTerm,
								searchQuery :"{\"" + key + "\":\"" + value + "\",\"datatype\":\"" + type + "\"}"
							});
						},
						/**
						 * Export the search page by full URL refresh
						 * 
						 * @method exportSearch
						 * @param args
						 *            {object} search args
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

							var searchSort = this.searchSort;
							if (args.searchSort !== undefined) {
								searchSort = args.searchSort;
							}
							var searchQuery = this.options.searchQuery;
							if (args.searchQuery !== undefined) {
								searchQuery = args.searchQuery;
							}
							
							var searchAllSites = this.searchAllSites;
					         if (args.searchAllSites !== undefined)
					         {
					            searchAllSites = args.searchAllSites;
					         }

							var searchRepository = this.searchRepository;
							if (args.searchRepository !== undefined) {
								searchRepository = args.searchRepository;
							}

							// call webscript
							var url = Alfresco.constants.PROXY_URI + "becpg/report/exportsearch/" + args.reportTpl.replace("://", "/") + "/"
									+ encodeURIComponent(args.reportFileName);

							// add search data webscript arguments
							url += "?term=" + encodeURIComponent(searchTerm);
							if (searchSort.length !== 0) {
								url += "&sort=" + encodeURIComponent(searchSort);
							}
							if (searchQuery.length !== 0) {
								url += "&query=" + encodeURIComponent(searchQuery);
							} else {
								if (searchTag.length !== 0) {
									url += "&tag=" + encodeURIComponent(searchTag);
								}
							}
							
							 if(this.options.siteId.length !== 0 && !searchAllSites && !searchRepository) { 
							    url +="&site=" + this.options.siteId + "&repo=false"; } 
							 else {
								url += "&site=&repo=true";
							}
							
							beCPG.util.launchAsyncDownload(args.reportFileName, args.reportTplName , url);

						},

						onBulkEditClick : function Search_onBulkEditClick(e, obj) {

							// redirect back to the search page - with
							// appropriate site
							// context
							var url = Alfresco.constants.URL_PAGECONTEXT;

							// add search data webscript arguments
							url += "bulk-edit" + location.search;

							window.location = url;
						},

						onWUsedClick : function Search_onWUsedClick(e, obj) {

							// redirect back to the search page - with
							// appropriate site
							// context
							var url = Alfresco.constants.URL_PAGECONTEXT;

							// add search data webscript arguments
							url += "wused" + location.search;

							window.location = url;
						},

						/**
						 * Build URI parameter string for search JSON data
						 * webscript
						 * 
						 * @method _buildSearchParams
						 */
						_buildSearchParams : function Search__buildSearchParams(searchRepository, searchAllSites, searchTerm, searchTag, searchSort) {
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
											// to calculate whether more results
											// were
											// available
											});

							return params;
						},

						_disableItems : function Search__disableItems() {
							// disables "All Sites" link
							var toggleLink = Dom.get(this.id + "-all-sites-link");
							if (toggleLink) {
								Event.removeListener(toggleLink, "click");
								toggleLink.style.color = "#aaa";
							}
							// disables "Repository" link
							toggleLink = Dom.get(this.id + "-repo-link");
							if (toggleLink) {
								Event.removeListener(toggleLink, "click");
								toggleLink.style.color = "#aaa";
							}
							// disables Site link
							toggleLink = Dom.get(this.id + "-site-link");
							if (toggleLink) {
								Event.removeListener(toggleLink, "click");
								toggleLink.style.color = "#aaa";
							}
							// // disables Search button
							// this.widgets.searchButton.set("disabled", true);
							//
							// // disables KeyListener (Enter)
							// if (this.widgets.enterListener)
							// {
							// this.widgets.enterListener.disable();
							// }
						},

						/**
						 * Enables Search button and links
						 * 
						 * @method _enableItems
						 */
						_enableItems : function Search__enableItems() {
							// enables "All Sites" link
							var toggleLink = Dom.get(this.id + "-all-sites-link");
							if (toggleLink) {
								Event.addListener(toggleLink, "click", this.onAllSiteSearch, this, true);
								toggleLink.style.color = "";
							}
							// enables "Repository" link
							toggleLink = Dom.get(this.id + "-repo-link");
							if (toggleLink) {
								Event.addListener(toggleLink, "click", this.onRepositorySearch, this, true);
								toggleLink.style.color = "";
							}
							// enables "Site" link
							toggleLink = Dom.get(this.id + "-site-link");
							if (toggleLink) {
								Event.addListener(toggleLink, "click", this.onSiteSearch, this, true);
								toggleLink.style.color = "";
							}

						},

						/**
						 * Public functions
						 * 
						 * Functions designed to be called form external sources
						 */

						/**
						 * Public function to get array of selected items
						 * 
						 * @method getSelectedItems
						 * @return {Array} Currently selected items
						 */
						getSelectedItems : function EntityDataGrid_getSelectedItems() {
							var items = [], recordSet = this.widgets.dataTable.getRecordSet(), startRecord = 0, endRecord = recordSet.getLength(), record;

							for (var i = startRecord; i <= endRecord; i++) {
								record = recordSet.getRecord(i);
								if (record != null && this.selectedItems[record.getData("nodeRef")]) {
									var node = record.getData();
									node.jsNode = {};
								    var isContainer = record._oData.type != "document" ? true : false;
								    node.jsNode.isContainer = isContainer;
									items.push(node);
								}
							}

							return items;
						},

						/**
						 * Public function to select items by specified groups
						 * 
						 * @method selectItems
						 * @param p_selectType
						 *            {string} Can be one of the following:
						 * 
						 * <pre>
						 * selectAll - all items
						 * selectNone - deselect all
						 * selectInvert - invert selection
						 * </pre>
						 */
						selectItems : function EntityDataGrid_selectItems(p_selectType) {
							var recordSet = this.widgets.dataTable.getRecordSet(), checks = Selector.query('input[type="checkbox"]',
									this.widgets.dataTable.getTbodyEl()), startRecord = 0, len = checks.length, record, i, fnCheck;

							switch (p_selectType) {
							case "selectAll":
							case "selectAllPages":
								fnCheck = function(assetType, isChecked) {
									return true;
								};
								break;

							case "selectNone":
								fnCheck = function(assetType, isChecked) {
									return false;
								};
								break;

							case "selectInvert":
								fnCheck = function(assetType, isChecked) {
									return !isChecked;
								};
								break;
							default:
								fnCheck = function(assetType, isChecked) {
									return isChecked;
								};
							}

							if (p_selectType == "selectAllPages") {
								this.allPages = true;
							} else {
								this.allPages = false;
							}

							for (i = 0; i < len; i++) {
								record = recordSet.getRecord(i + startRecord);
								this.selectedItems[record.getData("nodeRef")] = checks[i].checked = fnCheck(record.getData("type"), checks[i].checked);
							}

							Bubbling.fire("selectedItemsChanged");
						},

						/**
						 * Selected Item Menu
						 */

						/**
						 * @method onSelectedItemsChanged React on
						 *         onSelectedItemsChanged event
						 */
						onSelectedItemsChanged : function() {

							if (this.allPages) {
								Dom.get(this.id + "-message").innerHTML = '<span class="info">'
										+ this.msg("message.edit.allPages", this.totalRecords) + '</span>';
								Dom.removeClass(this.id + "-message", "hidden");
							} else {
								Dom.addClass(this.id + "-message", "hidden");
							}

							var items = this.getSelectedItems(), item, userAccess = {}, itemAccess, menuItems = this.widgets.selectedItems.getMenu()
									.getItems(), menuItem, actionPermissions, disabled, i, ii, disabledForAllPages;

							// Check each item for user permissions
							for (i = 0, ii = items.length; i < ii; i++) {
								item = items[i];
								
								if(!item.jsNode.isContainer){
									item.permissions.userAccess.content = true;
								}

								// Required user access level - logical AND of
								// each
								// item's permissions
								itemAccess = item.permissions.userAccess;
								for ( var index in itemAccess) {
									if (itemAccess.hasOwnProperty(index)) {
										userAccess[index] = (userAccess[index] === undefined ? itemAccess[index] : userAccess[index]
												&& itemAccess[index]);
									}
								}
							}

							// Now go through the menu items, setting the
							// disabled flag
							// appropriately
							for ( var index in menuItems) {
								if (menuItems.hasOwnProperty(index)) {
									// Defaulting to enabled
									menuItem = menuItems[index];
									disabled = false;
									disabledForAllPages = true;

									if (menuItem.element.firstChild) {
										// Check permissions required - stored
										// in "rel"
										// attribute in the DOM
										if (menuItem.element.firstChild.rel && menuItem.element.firstChild.rel !== "") {
											// Comma-separated indicates and
											// "AND" match
											actionPermissions = menuItem.element.firstChild.rel.split(",");
											for (i = 0, ii = actionPermissions.length; i < ii; i++) {
												// Disable if the user doesn't
												// have ALL the
												// permissions
												if (actionPermissions[i] != "allPages") {
													if ((!userAccess[actionPermissions[i]])) {
														disabled = true;
														break;
													}
												} else {
													disabledForAllPages = false;
												}
											}
										}

										if (this.allPages && (this.queryExecutionId == null || disabledForAllPages)) {
											disabled = true;
										}

										menuItem.cfg.setProperty("disabled", disabled);
									}
								}
							}
							this.widgets.selectedItems.set("disabled", (items.length === 0));

						},

						/**
						 * Selected Items button click handler
						 * 
						 * @method onSelectedItems
						 * @param sType
						 *            {string} Event type, e.g. "click"
						 * @param aArgs
						 *            {array} Arguments array, [0] = DomEvent,
						 *            [1] = EventTarget
						 * @param p_obj
						 *            {object} Object passed back from subscribe
						 *            method
						 */
						onSelectedItems : function Search_onSelectedItems(sType, aArgs, p_obj) {
							var domEvent = aArgs[0], eventTarget = aArgs[1];

							// Check mandatory docList module is present

							// Get the function related to the clicked item
							var fn = Alfresco.util.findEventClass(eventTarget);
							if (fn && (typeof this[fn] == "function")) {
								this[fn].call(this, this.getSelectedItems());
							}

							Event.preventDefault(domEvent);
						},

						/**
						 * Multi-item select button click handler
						 * 
						 * @method onItemSelect
						 * @param sType
						 *            {string} Event type, e.g. "click"
						 * @param aArgs
						 *            {array} Arguments array, [0] = DomEvent,
						 *            [1] = EventTarget
						 * @param p_obj
						 *            {object} Object passed back from subscribe
						 *            method
						 */
						onItemSelect : function Search_onItemSelect(sType, aArgs, p_obj) {
							
							var domEvent = aArgs[0], eventTarget = aArgs[1];

							// Select based upon the className of the clicked
							// item

							this.selectItems(Alfresco.util.findEventClass(eventTarget));
							Event.preventDefault(domEvent);
						},
						
						
						/**
					       * Delete Multiple Records.
					       *
					       * @method onActionDelete
					       * @param records {object} Object literal representing one or more file(s) or folder(s) to be actioned
					       */
					      onActionDeleteMultiple: function DLTB_onActionDelete(records)
					      {
					         var me = this,
					            fileNames = [];
					         
					         // Handle a single record being provided...
					         if (typeof records.length === "undefined")
					         {
					            records = [records];
					         }
					         for (var i = 0, j = records.length; i < j; i++)
					         {
					            fileNames.push("<span class=\"" + (records[i].jsNode.isContainer ? "folder" : "document") + "\">" + $html(records[i].displayName) + "</span>");
					         }
					         
					         var confirmTitle = this.msg("title.multiple-delete.confirm"),
					            confirmMsg = this.msg("message.multiple-delete.confirm", records.length);

					         confirmMsg += "<div class=\"toolbar-file-list\">" + fileNames.join("") + "</div>";

					         Alfresco.util.PopupManager.displayPrompt(
					         {
					            title: confirmTitle,
					            text: confirmMsg,
					            noEscape: true,
					            modal: true,
					            buttons: [
					            {
					               text: this.msg("button.delete"),
					               handler: function DLTB_onActionDelete_delete()
					               {
					                  this.destroy();
					                  me._onActionDeleteConfirm.call(me, records);
					               }
					            },
					            {
					               text: this.msg("button.cancel"),
					               handler: function DLTB_onActionDelete_cancel()
					               {
					                  this.destroy();
					               },
					               isDefault: true
					            }]
					         });
					      },

					      /**
					       * Delete Multiple Records confirmed.
					       *
					       * @method _onActionDeleteConfirm
					       * @param records {array} Array containing records to be deleted
					       * @private
					       */
					      _onActionDeleteConfirm: function DLTB__onActionDeleteConfirm(records)
					      {
					         var multipleRecords = [], i, ii, me = this;
					         for (i = 0, ii = records.length; i < ii; i++)
					         {
					            multipleRecords.push(records[i].nodeRef);
					         }
					         
					         // Success callback function
					         var fnSuccess = function DLTB__oADC_success(data, records)
					         {
					            var result;
					            var successCount = 0;
					            
					            // Did the operation succeed?
					            if (!data.json.overallSuccess)
					            {
					               Alfresco.util.PopupManager.displayMessage(
					               {
					                  text: this.msg("message.multiple-delete.failure")
					               });
					               return;
					            }
					            
					            
					            for (i = 0, ii = data.json.totalResults; i < ii; i++)
					            {
					               result = data.json.results[i];
					               
					               if (result.success)
					               {
					                  
					                  successCount++;
					                
					               }
					            }
					          
					            Alfresco.util.PopupManager.displayMessage(
					            {
					               text: this.msg("message.multiple-delete.success", successCount)
					            });

								me.refreshSearch({});
					         };
					         
					         // Construct the data object for the genericAction call
					         this.modules.actions.genericAction(
					         {
					            success:
					            {
					               callback:
					               {
					                  fn: fnSuccess,
					                  scope: this,
					                  obj: records
					               }
					            },
					            failure:
					            {
					               message: this.msg("message.multiple-delete.failure")
					            },
					            webscript:
					            {
					               method: Alfresco.util.Ajax.DELETE,
					               name: "files"
					            },
					            wait:
					            {
					               message: this.msg("message.multiple-delete.please-wait")
					            },
					            config:
					            {
					               requestContentType: Alfresco.util.Ajax.JSON,
					               dataObj:
					               {
					                  nodeRefs: multipleRecords
					               }
					            }
					         });
					      }


					});

	if (Alfresco.doclib && Alfresco.doclib.Actions) {
		YAHOO.lang.augmentProto(beCPG.custom.Search, Alfresco.doclib.Actions);
		
	}

})();
