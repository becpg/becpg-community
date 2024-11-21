/*******************************************************************************
 *  Copyright (C) 2010-2021 beCPG.
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

    beCPG.component.SavedSearchPicker = {};
    beCPG.component.SavedSearchPicker.prototype = {

        currentSavedSearchNodeRef: null,

        currentSavedSearchs: [],

        destNodeRef: null,

        globalDestNodeRef: null,

        createSavedSearchMenu: function createSavedSearchMenu() {


            this.widgets.createSavedSearchButton = this.createYUIButton("create-saved-search",
                this.onCreateSavedSearch);

            this.widgets.editSavedSearchButton = this.createYUIButton("edit-saved-search", this.onEditSavedSearch);

            this.widgets.deleteSavedSearchButton = this.createYUIButton("delete-saved-search",
                this.onDeleteSavedSearch);

            Dom.setStyle(this.widgets.editSavedSearchButton, 'display', 'none');
            Dom.setStyle(this.widgets.deleteSavedSearchButton, 'display', 'none');
        },

        createSavedSearchSubMenu: function(picker) {
            this.widgets.savedSearchPicker = picker;

            // Create menu item elements
            var li = document.createElement("li");
            li.innerHTML = '<a href="#"><span>' + this.msg("picker.saved-search.choose") + '</span></a>';

            // Make sure to stop clicks on the sub
            // menu link to close the entire menu
            YAHOO.util.Event.addListener(Selector.query("a", li, true), "click", function(e) {
                Event.preventDefault(e);
                Event.stopEvent(e);
            });

            // Create placeholder menu
            var div = document.createElement("div");
            div.innerHTML = '<div class="bd"><ul></ul></div>';

            // Add menu item
            var createContentByTemplate = new YAHOO.widget.MenuItem(li, {
                parent: this.widgets.savedSearchPicker.getMenu(),
                submenu: div
            });

            this.widgets.savedSearchPicker.getMenu().addItems([createContentByTemplate], 1);

            this.widgets.savedSearchMenu = this.widgets.savedSearchPicker.getMenu().getSubmenus()[0];
            /*

            this.widgets.savedSearchPicker.getMenu().addItem({
                text: this.msg("picker.saved-search.choose"),
                submenu: this.widgets.savedSearchMenu
            });
*/
            this.loadSavedSearchMenu();
        },

        createYUIButton: function(actionName, fn) {
            var template = Dom.get("custom-toolBar-template-button"),
                buttonWidget = null;

            var spanEl = Dom.getFirstChild(template).cloneNode(true);

            Dom.addClass(spanEl, actionName);
            Dom.setAttribute(spanEl, "id", this.id + "-savedSearchPicker-" + actionName + "Button");
            Dom.get(this.id + "-savedSearchPicker").appendChild(spanEl);

            buttonWidget = Alfresco.util.createYUIButton(this, "savedSearchPicker-" + actionName + "Button", fn);

            buttonWidget.set("label", this.msg("button." + actionName));
            buttonWidget.set("title", this.msg("button." + actionName + ".description"));

            return buttonWidget;
        },

        loadSavedSearchMenu: function() {
            this.currentSavedSearchs = [];

            Dom.setStyle(this.widgets.createSavedSearchButton, 'display', '');
            Dom.setStyle(this.widgets.editSavedSearchButton, 'display', 'none');
            Dom.setStyle(this.widgets.deleteSavedSearchButton, 'display', 'none');

            this.widgets.savedSearchMenu.clearContent();

            var savedSearchUrl = Alfresco.constants.PROXY_URI + "becpg/search/savedsearch?type=" + this.findSearchType();

            if (this.options.siteId) {
                savedSearchUrl += "&site=" + this.options.siteId;
            }

            Alfresco.util.Ajax.request({
                url: savedSearchUrl,
                successCallback: {
                    fn: function(response) {
                        var json = response.json;
                        if (json !== null) {
                            this.globalDestNodeRef = json.globalDestNodeRef;
                            this.destNodeRef = json.destNodeRef;

                            var items = json.items;
                            for (var i in items) {
                                if (items.hasOwnProperty(i)) {
                                    this.currentSavedSearchs.push(items[i]);

                                    var savedSearchLbl = '<span class="saved-search' +
                                        (items[i].isGlobal ? '-global' : '') + '">' + items[i].name + '</span>';

                                    if (this.currentSavedSearchNodeRef && this.currentSavedSearchNodeRef === items[i].nodeRef) {
                                        this.selectSavedSearch(items[i].nodeRef, items[i]);
                                        this.widgets.savedSearchPicker.set("label", savedSearchLbl);
                                    }
                                    this.widgets.savedSearchMenu.addItem({
                                        text: savedSearchLbl,
                                        value: items[i].nodeRef,
                                        onclick: {
                                            fn: this.onMenuItemClick,
                                            scope: this
                                        }
                                    });
                                }
                            }
                        }
                        this.widgets.savedSearchMenu.render();
                    },
                    scope: this
                },
                failureMessage: "Could not get savedsearch"
            });
        },

        findSearchType: function findSearchType() {
            var selectedType = this.options.selectedType;
            var dataType = selectedType 
                ? selectedType.indexOf("_") > 0 
                    ? selectedType.replace("_", ":") 
                    : "bcpg:" + selectedType
                : "bcpg:product";

            return this.options.searchType + "-" + dataType;
        },
        findCurrentFilter: function findCurrentFilter() {
            var dt = Alfresco.util.ComponentManager.find({
                name: "beCPG.module.EntityDataGrid"
            })[0];
            return dt.currentFilter;
        },

        onMenuItemClick: function(_p_sType, _p_aArgs, p_oItem) {
            if (p_oItem) {
                var sText = p_oItem.cfg.getProperty("text"),
                    value = p_oItem.value;
                this.widgets.savedSearchPicker.set("label", sText);
               this.resetSelectSearch();

                var currentSavedSearch = this.selectSavedSearch(p_oItem.value, null);
                if (currentSavedSearch != null) {
                    var savedSearchUrl = Alfresco.constants.PROXY_URI + "becpg/search/savedsearch?nodeRef=" + currentSavedSearch.nodeRef;

                    Alfresco.util.Ajax.request({
                        url: savedSearchUrl,
                        successCallback: {
                            fn: function(response) {
                                var json = response.json;
                                if (json !== null) {
                                    var data = JSON.parse(json.items[0].data);
                                    if (data.selectedColumns) {
                                        YAHOO.Bubbling.fire("changeSelectedColumns", {
                                            selectedColumns: data.selectedColumns
                                        });
                                    }
                                    if (data.filter) {
                                        YAHOO.Bubbling.fire("changeFilter", data.filter);
                                    }
                                }
                            },
                            scope: this
                        },
                        failureMessage: "Could not get savedsearch"
                    });
                }
            }
        },
        
        resetSelectSearch : function(){
            Dom.setStyle(this.widgets.createSavedSearchButton, 'display', '');
            Dom.setStyle(this.widgets.editSavedSearchButton, 'display', 'none');
            Dom.setStyle(this.widgets.deleteSavedSearchButton, 'display', 'none');
        },
        selectSavedSearch: function(nodeRef, currentSavedSearch) {
            this.currentSavedSearchNodeRef = nodeRef;

            if (currentSavedSearch === null) {
                for (var i = 0; i < this.currentSavedSearchs.length; i++) {
                    var savedSearch = this.currentSavedSearchs[i];
                    if (savedSearch.nodeRef === nodeRef) {
                        currentSavedSearch = savedSearch;
                        break;
                    }
                }
            }

            if (currentSavedSearch != null) {
                Dom.setStyle(this.widgets.createSavedSearchButton, 'display', 'none');

                var isProtected = currentSavedSearch.isGlobal && this.globalDestNodeRef == null;
                Dom.setStyle(this.widgets.deleteSavedSearchButton, 'display', isProtected ? 'none' : '');
                Dom.setStyle(this.widgets.editSavedSearchButton, 'display', isProtected ? 'none' : '');
            }

            return currentSavedSearch;
        },

        getTemplateUrl: function getTemplateUrl(actionType, nodeRef) {
            var params = {
                itemKind: actionType === "create" ? "type" : "node",
                itemId: actionType === "create" ? "bcpg:savedSearch" : nodeRef,
                mode: actionType,
                destination: this.destNodeRef,
                submitType: "json"
            };
            return YAHOO.lang.substitute(
                Alfresco.constants.URL_SERVICECONTEXT + "components/form?itemKind={itemKind}&itemId={itemId}&mode={mode}&submitType={submitType}&destination={destination}&showCancelButton=true&popup=true",
                params
            );
        },

        createDialog: function createDialog(instance, dialogId, templateUrl, title, onSuccessMsg, onFailureMsg) {
            var doBeforeDialogShow = function(_p_form, p_dialog) {
                Alfresco.util.populateHTML([p_dialog.id + "-dialogTitle", instance.msg(title)]);

                var form = Dom.get(p_dialog.id + "-form");
                var hiddenInput = document.createElement("input");
                hiddenInput.type = "hidden";
                hiddenInput.name = "prop_cm_content";

                hiddenInput.value = JSON.stringify({ filter: this.findCurrentFilter() });

                form.appendChild(hiddenInput);

                var hiddenInput2 = document.createElement("input");
                hiddenInput2.type = "hidden";
                hiddenInput2.name = "prop_bcpg_savedSearchType";
                hiddenInput2.value = this.findSearchType();

                form.appendChild(hiddenInput2);
                if(this.options.siteId){
                    var hiddenInput3 = document.createElement("input");
                        hiddenInput3.type = "hidden";
                        hiddenInput3.name = "prop_bcpg_savedSearchSiteId";
                        hiddenInput3.value = this.options.siteId;
                        form.appendChild(hiddenInput3);
                 }
                    
                var isGlobalSavedSearchElement = Dom.get(p_dialog.id + "_prop_bcpg_isGlobalSavedSearch-entry");

                if (this.globalDestNodeRef != null) {

                    if (isGlobalSavedSearchElement) {
                        YAHOO.util.Event.addListener(isGlobalSavedSearchElement, "change", function() {
                            Dom.get(p_dialog.id + "-form-destination").value = isGlobalSavedSearchElement.checked ?
                                this.globalDestNodeRef : this.destNodeRef;
                        }, this, true);
                    }
                } else {
                    var parentElement = Dom.getAncestorByTagName(isGlobalSavedSearchElement, "div");
                    if (parentElement) {
                        YAHOO.util.Dom.addClass(parentElement, "hidden");
                    }
                }


            };

            var dialog = new Alfresco.module.SimpleDialog(dialogId);
            dialog.setOptions({
                width: instance.options.formWidth,
                templateUrl: templateUrl,
                actionUrl: null,
                destroyOnHide: true,
                doBeforeDialogShow: {
                    fn: doBeforeDialogShow,
                    scope: instance
                },
                onSuccess: {
                    fn: function(response) {
                        this.currentSavedSearchNodeRef = response.json.persistedObject;
                        this.loadSavedSearchMenu();
                        Alfresco.util.PopupManager.displayMessage({
                            text: instance.msg(onSuccessMsg)
                        });
                    },
                    scope: instance
                },
                onFailure: {
                    fn: function() {
                        Alfresco.util.PopupManager.displayMessage({
                            text: instance.msg(onFailureMsg)
                        });
                    },
                    scope: instance
                }
            }).show();
        },

        showDeleteConfirmation: function showDeleteConfirmation(instance, nodeRefs) {
            var fnActionDeleteConfirm = function(items) {
                Alfresco.util.Ajax.jsonRequest({
                    url: Alfresco.constants.PROXY_URI + "slingshot/datalists/action/items?alf_method=delete",
                    method: "POST",
                    dataObj: { nodeRefs: items },
                    successCallback: {
                        fn: function() {
                            this.currentSavedSearchNodeRef = null;
                            this.loadSavedSearchMenu();
                        },
                        scope: instance
                    }
                });
            };

            Alfresco.util.PopupManager.displayPrompt({
                title: instance.msg("message.confirm.delete.title", nodeRefs.length),
                text: instance.msg("message.confirm.delete.description", nodeRefs.length),
                buttons: [{
                    text: instance.msg("button.delete"),
                    handler: function() {
                        this.destroy();
                        fnActionDeleteConfirm.call(instance, nodeRefs);
                    }
                }, {
                    text: instance.msg("button.cancel"),
                    handler: function() {
                        this.destroy();
                    },
                    isDefault: true
                }]
            });
        },

        onCreateSavedSearch: function SavedSearchPicker_onCreateSavedSearch() {
            var templateUrl = this.getTemplateUrl("create");
            this.createDialog(this, this.id + "-createSavedSearch", templateUrl,
                "label.new-saved-search.title",
                "message.create-saved-search.success",
                "message.create-saved-search.failure");
        },

        onEditSavedSearch: function SavedSearchPicker_onEditSavedSearch() {
            if (this.currentSavedSearchNodeRef) {
                var templateUrl = this.getTemplateUrl("edit", this.currentSavedSearchNodeRef);
                this.createDialog(this, this.id + "-editSavedSearch", templateUrl,
                    "label.edit-saved-search.title",
                    "message.edit-saved-search.success",
                    "message.edit-saved-search.failure");
            }
        },

        onDeleteSavedSearch: function SavedSearchPicker_onDeleteSavedSearch() {
            if (this.currentSavedSearchNodeRef) {
                var nodeRefs = [this.currentSavedSearchNodeRef];
                this.showDeleteConfirmation(this, nodeRefs);
            }
        }
    };
})();
