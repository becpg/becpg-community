/**
 * ProductListToolbar component.
 * 
 * @namespace Alfresco
 * @class beCPG.component.ProductListToolbar
 */
(function() {
    /**
     * YUI Library aliases
     */
    var Dom = YAHOO.util.Dom, Selector = YAHOO.util.Selector;

    /**
     * ProductListToolbar constructor.
     * 
     * @param {String} htmlId The HTML id of the parent element
     * @return {beCPG.component.ProductListToolbar} The new ProductListToolbar instance
     * @constructor
     */
    beCPG.component.ProductListToolbar = function ProductListToolbar_constructor(htmlId) {
        beCPG.component.ProductListToolbar.superclass.constructor.call(this, "beCPG.component.ProductListToolbar",
            htmlId, ["button"]);

        // Preferences service
        this.services.preferences = new Alfresco.service.Preferences();


        return this;
    };

    /**
     * Extend from Alfresco.component.Base
     */
    YAHOO.extend(beCPG.component.ProductListToolbar, Alfresco.component.Base);

    /**
     * Augment prototype with Common Actions module
     */
    YAHOO.lang.augmentProto(beCPG.component.ProductListToolbar, beCPG.component.SavedSearchPicker);

    /**
     * Augment prototype with main class implementation, ensuring overwrite is
     * enabled
     */
    YAHOO.lang
        .augmentObject(
            beCPG.component.ProductListToolbar.prototype, {
            options: {
                siteId: "",
                prefsId: "org.alfresco.share.product.list.home",
                selectedType: "finishedProduct",
                selectedFilter: "all",
                showThumbnails: false,
                filters: [],
                types: []
            },

            /**
             * Fired by YUI when parent element is available for scripting. Component initialisation, including
             * instantiation of YUI widgets and event listener binding.
             * 
             * @method onReady
             */
            onReady: function PTL_onReady() {
                var me = this;


                this.widgets.filter = new YAHOO.widget.Button({
                    type: "split",
                    label: this.msg("picker.saved-search.choose"),
                    name: "savedSearchPickerButton",
                    menu: [],
                    container: Dom.get(this.id + "-savedSearchPicker"),
                    lazyloadmenu: false
                });


                this.widgets.type = Alfresco.util.createYUIButton(this, "types", this.onTypeChange, {
                    type: "menu",
                    menu: "types-menu",
                    lazyloadmenu: false
                });

                this.createSavedSearchMenu();

                // Select the preferred type in the UI
                this.widgets.type.set("label", this.msg("type." + this.options.selectedType) + " " + Alfresco.constants.MENU_ARROW_SYMBOL);
                this.widgets.type.value = this.options.selectedType;

                this.loadFilterMenu();



                this.widgets.showThumbnailsButton = Alfresco.util.createYUIButton(this, "show-thumbnails", this.onShowThumbnails, {
                    type: "checkbox",
                    value: this.options.showThumbnails,
                    checked: this.options.showThumbnails
                });

                this.widgets.exportProductList = Alfresco.util.createYUIButton(this, "export-csv-button", this.onExportProductList);

                if (Dom.get(this.id + "-reporting-menu-button")) {
                    // Menu button for export options
                    this.widgets.reportingMenu = Alfresco.util.createYUIButton(this, "reporting-menu-button", this.onReportingMenuClick, {
                        type: "menu",
                        menu: "reporting-menu",
                        lazyloadmenu: true,
                        disabled: true,
                        zindex: 99
                    });

                    this.widgets.reportingMenu.getMenu().subscribe("beforeShow", function() {
                        this.cfg.setProperty("zindex", 99);
                    });

                    this.loadExportMenu();
                }

                Dom.removeClass(Selector.query(".hidden", this.id + "-body", true), "hidden");
            },
            loadExportMenu: function PTL_loadExportMenu() {
                var me = this;

                this.widgets.reportingMenu.getMenu().clearContent();

                var dataType = "bcpg:product";

                if (this.options.selectedType != null) {
                    dataType = this.options.selectedType.indexOf("_") > 0 ? this.options.selectedType.replace("_", ":") : "bcpg:" + this.options.selectedType;
                }

                this.widgets.reportingMenu.set("label", this.msg("button.download-report") + " " + Alfresco.constants.MENU_ARROW_SYMBOL);

                Alfresco.util.Ajax.request({
                    url: Alfresco.constants.PROXY_URI + "becpg/report/exportsearch/templates/" + dataType,
                    successCallback: {
                        fn: function(response) {
                            var json = response.json, items = [];

                            if (json !== null) {
                                for (var i in json.reportTpls) {
                                    items.push({
                                        text: json.reportTpls[i].name,
                                        value: json.reportTpls[i].nodeRef + "#" + json.reportTpls[i].name + "." + json.reportTpls[i].format.toString().toLowerCase() + "#" + json.reportTpls[i].reportTplName
                                    });
                                }
                                if (items.length > 0) {
                                    me.widgets.reportingMenu.getMenu().addItems(items);
                                    me.widgets.reportingMenu.getMenu().render(document.body);
                                    me.widgets.reportingMenu.set("disabled", false);
                                }
                            }
                        },
                        scope: this
                    },
                    failureMessage: "Could not get reports '" + Alfresco.constants.PROXY_URI + "becpg/report/exportsearch/templates/" + dataType + "'."
                });
            },

            loadFilterMenu: function PTL_loadFilterMenu() {
                var me = this;

                var selectedFilter = this.options.selectedFilter.split("|").length > 1 ? this.options.selectedFilter.split("|")[1] : this.options.selectedFilter;

                this.widgets.filter.getMenu().clearContent();

                var filter = "";

                for (var i in this.options.types) {
                    var type = this.options.types[i];
                    if (type.name == this.options.selectedType) {
                        if (type.filter) {
                            filter = type.filter;
                        }
                        break;
                    }
                }

                var items = [];
                var splitFilters = filter.split(","), isSelected = false;

                for (var i in this.options.filters) {
                    var filterId = this.options.filters[i].type;
                    if (selectedFilter == filterId) {
                        isSelected = true;
                    }

                    if (splitFilters.length == 0 || splitFilters.includes(filterId)) {
                        filterValue = filterId;
                        if (filterId != "all" && filterId != "favorite") {
                            filterValue = this.options.selectedType + "|" + filterId;
                        }


                        items.push({
                            text: this.msg("filter." + filterId),
                            value: filterValue,

                            onclick: {
                                fn: this.onFilterChange,
                                scope: this
                            }
                        });
                    }
                }

                if (items.length > 0) {
                    me.widgets.filter.getMenu().addItems(items);
                    me.widgets.filter.getMenu().render(document.body);
                    me.widgets.filter.set("disabled", false);
                }

                if (isSelected) {
                    this.widgets.filter.set("label", this.msg("filter." + selectedFilter));
                } else {
                    this.widgets.filter.set("label", this.msg("filter.all") );
                }
                this.createSavedSearchSubMenu(this.widgets.filter);
                
                this.widgets.filter._menu.render();
            },

            onFilterChange: function(_p_sType, _p_aArgs, menuItem) {
                if (menuItem) {
                    this.queryExecutionId = null;
                    this.resetSelectSearch();
                    this.widgets.filter.set("label", menuItem.cfg.getProperty("text") );
                    this.widgets.filter.value = menuItem.value;

                    this.services.preferences.set(this.options.prefsId + ".filter",
                        this.widgets.filter.value);

                    this.options.selectedFilter = this.widgets.filter.value;


                    var dt = Alfresco.util.ComponentManager.find({
                        name: "beCPG.module.EntityDataGrid"
                    })[0];


                    var value = this.widgets.filter.value;

                    var filterObj = {
                        filterOwner: dt.name,
                        filterId: value.split("|")[0]
                    };

                    if ("all" != filterObj.filterId && value.split("|").length > 1) {
                        filterObj.filterData = value.split("|")[1];
                    } else {
                        filterObj.filterData = "";
                    }


                    dt.options.filter = filterObj;

                    YAHOO.Bubbling.fire("changeFilter", filterObj);

                }
            },


            onShowThumbnails: function PTL_onShowThumbnails() {
                this.options.showThumbnails = !this.options.showThumbnails;
                this.reloadDataTable();
            },

            onTypeChange: function PTL_onTypeChange(p_sType, p_aArgs) {
                var menuItem = p_aArgs[1];
                if (menuItem) {
                    this.widgets.type.set("label", menuItem.cfg.getProperty("text") + " " + Alfresco.constants.MENU_ARROW_SYMBOL);
                    this.widgets.type.value = menuItem.value;

                    this.services.preferences.set(this.options.prefsId + ".type",
                        this.widgets.type.value);

                    this.services.preferences.set(this.options.prefsId + ".filter",
                        "all");

                    this.options.selectedType = this.widgets.type.value;
                    this.options.selectedFilter = "all";

                    this.loadFilterMenu();
                    this.loadExportMenu();

                    //TO remove
                    this.currentSavedSearchNodeRef = null;
                    this.loadSavedSearchMenu();

                    this.reloadDataTable();
                }
            },

            reloadDataTable: function() {
                var dataType = "bcpg:product";

                if (this.options.selectedType != null) {
                    dataType = this.options.selectedType.indexOf("_") > 0 ? this.options.selectedType.replace("_", ":") : "bcpg:" + this.options.selectedType;
                }

                YAHOO.Bubbling.fire("activeDataListChanged", {
                    dataList: {
                        name: this.options.selectedType, itemType: dataType, showThumbnails: this.options.showThumbnails
                    }, force: true
                });
            },

            onReportingMenuClick: function PTL_onReportingMenuClick(p_sType, p_aArgs) {
                var menuItem = p_aArgs[1];
                if (menuItem) {
                    var values = menuItem.value.split("#");

                    var url = Alfresco.constants.PROXY_URI + "becpg/report/exportsearch/" + values[0].replace("://", "/") + "/" + encodeURIComponent(values[1]);

                    var dataType = "bcpg:product";

                    if (this.options.selectedType != null) {
                        dataType = this.options.selectedType.indexOf("_") > 0 ? this.options.selectedType.replace("_", ":") : "bcpg:" + this.options.selectedType;
                    }

                    // Add search data webscript arguments
                    url += "?term=&query=" + encodeURIComponent('{datatype:"' + dataType + '"}');

                    if (this.options.siteId.length !== 0) {
                        url += "&site=" + this.options.siteId + "&repo=false";
                    } else {
                        url += "&site=&repo=true";
                    }

                    beCPG.util.launchAsyncDownload(values[1], values[2], url);
                }
            },

            onExportProductList: function PTL_onExportProductList(e, p_obj) {
                var dt = Alfresco.util.ComponentManager.find({
                    name: "beCPG.module.EntityDataGrid"
                })[0];

                Alfresco.util.Ajax.jsonGet({
                    url: dt._getColumnUrl("product-list"),
                    successCallback: {
                        fn: function(response) {
                            var requestParams = {
                                fields: [],
                                filter: dt.currentFilter,
                                page: 1,
                                extraParams: dt.options.extraParams
                            };

                            requestParams.filter.filterParams = dt._createFilterURLParameters(dt.currentFilter, dt.options.filterParameters);

                            for (var i = 0, ii = response.json.columns.length; i < ii; i++) {
                                var column = response.json.columns[i], columnName = column.name.replace(":", "_");
                                if (column.dataType == "nested" && column.columns) {
                                    for (var j = 0; j < column.columns.length; j++) {
                                        var col = column.columns[j];
                                        columnName += "|" + col.name.replace(":", "_");
                                    }
                                }

                                requestParams.fields.push(columnName);
                            }

                            var MAX_RESULTS_UNLIMITED = -1;

                            beCPG.util.launchAsyncDownload("export.xlsx", "export.xlsx", dt._getDataUrl(MAX_RESULTS_UNLIMITED) + "&format=xlsx", requestParams);
                        },
                        scope: this
                    }
                });
            }

        }, true);
})();
