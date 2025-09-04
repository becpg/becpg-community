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
                types: [],
                searchType: "product-list"
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

                // Efface le contenu existant du menu
                this.widgets.reportingMenu.getMenu().clearContent();

                // Déterminer le type de données
                var selectedType = this.options.selectedType;
                var dataType = selectedType
                    ? (selectedType.indexOf("_") > 0
                        ? selectedType.replace("_", ":")
                        : "bcpg:" + selectedType)
                    : "bcpg:product";
                if (selectedType == "document") {
                    dataType = "cm:content";
                }


                // Définir le libellé du menu
                this.widgets.reportingMenu.set("label", this.msg("button.download-report") + " " + Alfresco.constants.MENU_ARROW_SYMBOL);

                // Fonction pour vérifier si un élément est déjà présent dans le menu
                var isReportAlreadyInMenu = function(reportValue) {
                    var existingItems = me.widgets.reportingMenu.getMenu().getItems();
                    for (var i = 0; i < existingItems.length; i++) {
                        if (existingItems[i].value === reportValue) {
                            return true; // Report already exists
                        }
                    }
                    return false; // Report is not present
                };

                // Fonction pour récupérer et ajouter les rapports
                var fetchAndAddReports = function(dataType) {
                    Alfresco.util.Ajax.request({
                        url: Alfresco.constants.PROXY_URI + "becpg/report/exportsearch/templates/" + dataType,
                        successCallback: {
                            fn: function(response) {
                                var json = response.json;
                                if (json && json.reportTpls) {
                                    var items = [];
                                    for (var i = 0; i < json.reportTpls.length; i++) {
                                        var template = json.reportTpls[i];
                                        var reportValue = template.nodeRef + "#" + template.name + "." + template.format.toLowerCase() + "#" + template.reportTplName;

                                        // Vérifier si le rapport est déjà dans le menu
                                        if (!isReportAlreadyInMenu(reportValue)) {
                                            items.push({
                                                text: template.name,
                                                value: reportValue
                                            });
                                        }
                                    }
                                    if (items.length > 0) {
                                        me.widgets.reportingMenu.getMenu().addItems(items);
                                        me.widgets.reportingMenu.getMenu().render(document.body);
                                        me.widgets.reportingMenu.set("disabled", false);
                                    }
                                }
                            },
                            scope: me
                        },
                        failureMessage: "Could not get reports from '" + Alfresco.constants.PROXY_URI + "becpg/report/exportsearch/templates/" + dataType + "'."
                    });
                };

                fetchAndAddReports(dataType);

                if (dataType && (dataType.indexOf("Product") > 0 || dataType.indexOf("Material") > 0)) {
                    fetchAndAddReports("bcpg:product");
                }
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
                    this.widgets.filter.set("label", this.msg("filter.all"));
                }
                this.createSavedSearchSubMenu(this.widgets.filter);

                this.widgets.filter._menu.render();
            },

            onFilterChange: function(_p_sType, _p_aArgs, menuItem) {
                if (menuItem) {
                    this.queryExecutionId = null;
                    this.resetSelectSearch();
                    this.widgets.filter.set("label", menuItem.cfg.getProperty("text"));
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
                
                // Store the preference
                this.services.preferences.set(this.options.prefsId + ".showThumbnails", this.options.showThumbnails);
                
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

                    this.currentSavedSearchNodeRef = null;
                    this.loadExportMenu();

                    this.reloadDataTable();
                }
            },

            reloadDataTable: function() {
                var dataType = "bcpg:product";

                if (this.options.selectedType != null) {
                    dataType = this.options.selectedType.indexOf("_") > 0 ? this.options.selectedType.replace("_", ":") : "bcpg:" + this.options.selectedType;
                    if (this.options.selectedType == "document") {
                        dataType = "cm:content";
                    }
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
                        if (this.options.selectedType == "document") {
                            dataType = "cm:content";
                        }
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

                            for ( var i = 0, ii = response.json.columns.length; i < ii; i++) {
	  							 var column = response.json.columns[i], columnName = column.name.replace(":", "_"), columnLabel = (column.label!="hidden"? column.label :"");
                                 if (Object.keys(column).includes("label") && ["datasource"].indexOf(column.label) < 0) {
	
	                                 if ((column.dataType == "nested" || column.dataType == "nested_column") && column.columns) {
	                                    for ( var j = 0; j < column.columns.length; j++) { 
									    var col = column.columns[j];                             
		 								if (Object.keys(col).includes("label") && ["datasource"].indexOf(col.label) < 0) {                                            
		                                       columnName += "|" + col.name.replace(":", "_");
		                               		   columnLabel += "|" + (col.label!="hidden"?col.label:"");    
	                                       }                                        
	                                    }
	                                 }
	                                 requestParams.fields.push({"id":columnName, "label": columnLabel});
                                 }
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
