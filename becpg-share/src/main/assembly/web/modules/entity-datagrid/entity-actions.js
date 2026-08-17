/*******************************************************************************
 *  Copyright (C) 2010-2026 beCPG. 
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
 * Entity Data Grid Actions module
 * 
 * @namespace beCPG.module
 * @class beCPG.module.EntityDataGridActions
 */
(function() {

    var Dom = YAHOO.util.Dom, Event = YAHOO.util.Event, Selector = YAHOO.util.Selector, Bubbling = YAHOO.Bubbling;

    /**
     * Delay after which a pending form dialog is unlocked, in case its template could not be loaded.
     */
    var FORM_DIALOG_LOCK_TIMEOUT = 10000;

    /**
     * Width of the popups holding a checkbox picker, wide enough for its four columns.
     */
    var PICKER_PANEL_WIDTH = "76em";

    /**
     * beCPG.module.EntityDataGridActions implementation
     */
    beCPG.module.EntityDataGridActions = {};
    beCPG.module.EntityDataGridActions.prototype = {
        /**
          * ACTIONS WHICH ARE LOCAL TO THE DATAGRID COMPONENT
          */

        /**
          * Takes the form dialog lock. The create and edit dialogs share the same popup id, so a second
          * request sent while the previous form template is still loading injects the same html ids twice
          * and leaves the page broken until it is reloaded.
          *
          * @method _lockFormDialog
          * @return {boolean} true when the caller is allowed to open the dialog
          */
        _lockFormDialog: function EntityDataGrid__lockFormDialog() {
            if (this.formDialogPending) {
                return false;
            }

            this.formDialogPending = true;
            YAHOO.lang.later(FORM_DIALOG_LOCK_TIMEOUT, this, this._unlockFormDialog);

            return true;
        },

        /**
          * Releases the form dialog lock, once the form template is loaded or has failed to load.
          *
          * @method _unlockFormDialog
          */
        _unlockFormDialog: function EntityDataGrid__unlockFormDialog() {
            this.formDialogPending = false;
        },

        /**
          * New Row button click handler
          * 
          * @method onActionCreate
          * @param e
          *           {object} DomEvent
          * @param p_obj
          *           {object} Object passed back from addListener method
          */
        onActionCreate: function EntityDataGrid_onActionCreate(e, p_obj) {
            var destination = this.datalistMeta.nodeRef != null ? this.datalistMeta.nodeRef : this.options.parentNodeRef, itemType = this.options.itemType != null ? this.options.itemType
                : this.datalistMeta.itemType, me = this;

            // #35600 : on the product referential there is neither a data list nor a parent entity,
            // so the destination stays empty, the form is rendered without alf_destination and the
            // submit fails. Fall back to the site document library, resolved once then cached.
            if (destination == null || destination.length === 0) {
                if (this.siteContainerNodeRef != null) {
                    destination = this.siteContainerNodeRef;
                } else if (this.options.siteId && !this.siteContainerResolved) {
                    this.siteContainerResolved = true;
                    Alfresco.util.Ajax.request({
                        url: Alfresco.constants.PROXY_URI + "slingshot/doclib/containers/"
                            + encodeURIComponent(this.options.siteId),
                        method: "GET",
                        successCallback: {
                            fn: function EntityDataGrid_onActionCreate_containers(response) {
                                var containers = response.json != null ? response.json.containers : null;
                                for (var i = 0; containers != null && i < containers.length; i++) {
                                    if (containers[i].name === "documentLibrary") {
                                        me.siteContainerNodeRef = containers[i].nodeRef;
                                        break;
                                    }
                                }
                                me.onActionCreate(e, p_obj);
                            },
                            scope: this
                        },
                        failureCallback: {
                            fn: function EntityDataGrid_onActionCreate_containersFailure() {
                                me.onActionCreate(e, p_obj);
                            },
                            scope: this
                        }
                    });
                    return;
                }
            }

            // Intercept before dialog show
            var doBeforeDialogShow = function EntityDataGrid_onActionCreate_doBeforeDialogShow(p_form, p_dialog) {
                this._unlockFormDialog();

                Alfresco.util.populateHTML([p_dialog.id + "-dialogTitle", this.msg("label.new-row.title")], [
                    p_dialog.id + "-dialogHeader", this.msg("label.new-row.header")]);

                this._freezeGridScroll(p_dialog);

                // Is it a bulk action?
                if (Dom.get(p_dialog.id + "-form-bulkAction")) {
                    Dom.get(p_dialog.id + "-form-bulkAction").checked = this.onActionCreateBulkEdit;
                    Dom.get(p_dialog.id + "-form-bulkAction-msg").innerHTML = this.msg("button.bulk-action-create");
                }

                if (this.options.formWidth != "34em") {
                    Dom.addClass(p_dialog.id + "-dialog", "large-dialog");
                }

                var propInputNodeRefs = {};
                propInputNodeRefs["bcpg_parentLevel"] = this.parentInputNodeRef;
                propInputNodeRefs["bcpg_variantIds"] = this.variantInputNodeRef;

                for (var prop in propInputNodeRefs) {
                    if (propInputNodeRefs[prop] != null) {
                        Dom.get(p_dialog.id + "_prop_" + prop + "-added").value = propInputNodeRefs[prop];
                        Bubbling.fire(p_dialog.id + "_prop_" + prop + "refreshContent", propInputNodeRefs[prop], this);
                    }
                }

            };

            //Note is important to have the same popupId as component manager will use it to destroy previous popup components
            var popupId = this.id + "-editDetails";

            var templateUrl = YAHOO.lang
                .substitute(
                    Alfresco.constants.URL_SERVICECONTEXT
                    + "components/form?bulkEdit=true&entityNodeRef={entityNodeRef}&entityType={entityType}&itemKind={itemKind}&formId=create&itemId={itemId}&destination={destination}&mode={mode}&submitType={submitType}&showCancelButton=true&list={list}&siteId={siteId}",
                    {
                        itemKind: "type",
                        itemId: itemType,
                        destination: destination!=null ? destination: "",
                        mode: "create",
                        submitType: "json",
                        entityNodeRef: this.options.entityNodeRef,
                        entityType: this.entity != null ? encodeURIComponent(this.entity.type) : "",
                        list: encodeURIComponent(this.datalistMeta.name != null ? this.datalistMeta.name : this.options.list),
                        siteId: this.options.siteId
                    });

            if (!this._lockFormDialog()) {
                return;
            }

            // Using Forms Service, so always create new instance
            var createRow = new Alfresco.module.SimpleDialog(popupId);
            createRow.bulkEdit = false;
            createRow.setOptions({
                width: this.options.formWidth,
                templateUrl: templateUrl,
                actionUrl: null,
                destroyOnHide: true,
                doBeforeDialogShow: {
                    fn: doBeforeDialogShow,
                    scope: this
                },
                doBeforeFormSubmit: {
                    fn: function() {
                        var checkBoxEl = Dom.get(popupId + "-form-bulkAction");

                        var parentInput = Dom.get(popupId + "_prop_bcpg_parentLevel-added");
                        var variantInput = Dom.get(popupId + "_prop_bcpg_variantIds-added");
                        me.parentInputNodeRef = null;
                        me.variantInputNodeRef = null;
                        if (parentInput != null && parentInput.value != null && parentInput.value.length > 0) {
                            me.parentInputNodeRef = parentInput.value;
                        }
                        if (variantInput != null && variantInput.value != null && variantInput.value.length > 0) {
                            me.variantInputNodeRef = variantInput.value;
                        }

                        if (checkBoxEl && checkBoxEl.checked) {
                            me.onActionCreateBulkEdit = true;
                        } else {
                            me.onActionCreateBulkEdit = false;
                        }
                    },
                    scope: this
                },
                onSuccess: {
                    fn: function EntityDataGrid_onActionCreate_success(response) {

                        if (me.parentInputNodeRef != null) {

                            url = Alfresco.constants.PROXY_URI + "becpg/entity/datalists/openclose?nodeRef="
                                + me.parentInputNodeRef + "&expand=true&entityNodeRef=" + me.options.entityNodeRef + "&listType=" + itemType;
                            Alfresco.util.Ajax
                                .jsonPost(
                                    {
                                        url: url,
                                        successCallback:
                                        {
                                            fn: function EntityDataGrid_onCollapsedAndExpanded(
                                                response) {

                                                me.queryExecutionId = null;
                                                me._updateDataGrid.call(me,
                                                    {
                                                        page: me.currentPage
                                                    });


                                                YAHOO.Bubbling.fire("dirtyDataTable");


                                                Alfresco.util.PopupManager.displayMessage({
                                                    text: me.msg("message.new-row.success")
                                                });

                                                // recall edit for next item

                                                if (me.onActionCreateBulkEdit) {
                                                    me.onActionCreate();
                                                }

                                            },
                                            scope: this
                                        }
                                    });

                        } else {

                            YAHOO.Bubbling.fire(me.scopeId + "dataItemCreated", {
                                nodeRef: response.json.persistedObject,
                                callback: function(item) {

                                    YAHOO.Bubbling.fire("refreshFloatingHeader");

                                    Alfresco.util.PopupManager.displayMessage({
                                        text: me.msg("message.new-row.success")
                                    });

                                    // recall edit for next item

                                    if (me.onActionCreateBulkEdit) {
                                        me.onActionCreate();
                                    }

                                }
                            });
                        }

                    },
                    scope: this
                },
                onFailure: {
                    fn: function EntityDataGrid_onActionCreate_failure(response) {
                        Alfresco.util.PopupManager.displayMessage({
                            text: me.msg("message.new-row.failure")
                        });
                    },
                    scope: this
                }
            }).show();
        },
        /**
          * Edit Data Item pop-up
          * 
          * @method onActionEdit
          * @param item
          *           {object} Object literal representing one data item
          */
        onActionEdit: function EntityDataGrid_onActionEdit(item) {
            var me = this;

            if (!this._lockFormDialog()) {
                return;
            }

            // Intercept before dialog show
            var doBeforeDialogShow = function EntityDataGrid_onActionEdit_doBeforeDialogShow(p_form, p_dialog) {
                this._unlockFormDialog();

                Alfresco.util.populateHTML([p_dialog.id + "-dialogTitle", this.msg("label.edit-row.title")]);

                this._freezeGridScroll(p_dialog);

                // Is it a bulk action?
                if (Dom.get(p_dialog.id + "-form-bulkAction")) {
                    Dom.get(p_dialog.id + "-form-bulkAction").checked = this.onActionEditBulkEdit;
                    Dom.get(p_dialog.id + "-form-bulkAction-msg").innerHTML = this.msg("button.bulk-action-edit");
                }

                if (this.options.formWidth != "34em") {
                    Dom.addClass(p_dialog.id + "-dialog", "large-dialog");
                }

            };

            var templateUrl = YAHOO.lang
                .substitute(
                    Alfresco.constants.URL_SERVICECONTEXT
                    + "components/form?bulkEdit=true&entityNodeRef={entityNodeRef}&entityType={entityType}&itemKind={itemKind}&itemId={itemId}&mode={mode}&submitType={submitType}&showCancelButton=true&list={list}&siteId={siteId}",
                    {
                        itemKind: "node",
                        itemId: item.nodeRef,
                        mode: "edit",
                        submitType: "json",
                        entityType: this.entity != null ? encodeURIComponent(this.entity.type) : "",
                        entityNodeRef: this.options.entityNodeRef,
                        list: encodeURIComponent(this.datalistMeta.name != null ? this.datalistMeta.name : this.options.list),
                        siteId: this.options.siteId
                    });

            //Note is important to have the same popupId as component manager will use it to destroy previous popup components
            var popupId = this.id + "-editDetails";

            var editDetails = new Alfresco.module.SimpleDialog(popupId);

            editDetails.bulkEdit = false;
            editDetails.setOptions({
                width: this.options.formWidth,
                templateUrl: templateUrl,
                actionUrl: null,
                destroyOnHide: true,
                doBeforeDialogShow: {
                    fn: doBeforeDialogShow,
                    scope: this
                },
                doBeforeFormSubmit: {
                    fn: function() {
                        var checkBoxEl = Dom.get(popupId + "-form-bulkAction");

                        if (checkBoxEl && checkBoxEl.checked) {
                            me.onActionEditBulkEdit = true;
                        } else {
                            me.onActionEditBulkEdit = false;
                        }
                    },
                    scope: this
                },
                onSuccess: {
                    fn: function EntityDataGrid_onActionEdit_success(response) {

                        // Fire "itemUpdated" event
                        Bubbling.fire(me.scopeId + "dataItemUpdated", {
                            nodeRef: response.json.persistedObject,
                            callback: function(item) {

                                // Display success message
                                Alfresco.util.PopupManager.displayMessage({
                                    text: me.msg("message.details.success")
                                });

                                if (me.onActionEditBulkEdit) {
                                    var recordFound = me._findNextItemByParameter(response.json.persistedObject, "nodeRef");
                                    if (recordFound != null && recordFound.nodeRef) {
                                        me.onActionEdit(recordFound);
                                    } else {
                                        // No more items to edit - disable bulk edit mode
                                        me.onActionEditBulkEdit = false;
                                        Alfresco.util.PopupManager.displayMessage({
                                            text: me.msg("message.bulk-edit.completed")
                                        });
                                    }
                                }

                            }
                        });

                    },
                    scope: this
                },
                onFailure: {
                    fn: function EntityDataGrid_onActionEdit_failure(response) {
                        Alfresco.util.PopupManager.displayMessage({
                            text: me.msg("message.details.failure")
                        });
                    },
                    scope: this
                }
            }).show();
        },
        /**
          * Sort item(s).
          * 
          * @method onActionUp
          * @param items
          *           {Object | Array} Object literal representing the Data Item to
          *           be actioned, or an Array thereof
          */
        onActionUp: function EntityDataGrid_onActionUp(p_items) {
            var me = this;
            if (me.options.sortable) {
                var items = YAHOO.lang.isArray(p_items) ? p_items : [p_items];

                if (items.length > 0) {
                    var recordFound = me._findPrevItemByParameter(items[0].nodeRef, "nodeRef");

                    if (recordFound == null) {
                        recordFound = items[0];
                    }

                    me._sort(items, recordFound, "up");
                }

            }

        },
        /**
          * Sort item(s).
          * 
          * @method onActionDown
          * @param items
          *           {Object | Array} Object literal representing the Data Item to
          *           be actioned, or an Array thereof
          */
        onActionDown: function EntityDataGrid_onActionDown(p_items) {
            var me = this;
            if (me.options.sortable) {
                var items = YAHOO.lang.isArray(p_items) ? p_items : [p_items];

                if (items.length > 0) {
                    var recordFound = me._findNextItemByParameter(items[items.length - 1].nodeRef, "nodeRef");

                    if (recordFound == null) {
                        recordFound = items[items.length - 1];
                    }

                    me._sort(items, recordFound, "down");


                }

            }
        },
        /**
          * Sort item(s) in server side
          * 
          * @param items
          * @param node
          * @param dir
          */
        _sort: function EntityDataGrid__sort(items, node, dir) {
            var me = this, nodeRefs = [];
            if (me.options.sortable) {

                for (var i = 0, ii = items.length; i < ii; i++) {
                    nodeRefs.push(items[i].nodeRef);
                }

                var url = me.options.sortUrl + "/" + node.nodeRef.replace(":/", "") + "?selectedNodeRefs="
                    + nodeRefs.join(",") + "&dir=" + dir;

                Alfresco.util.Ajax.jsonPost({
                    url: url,
                    successCallback: {
                        fn: function EntityDataGrid_onActionUp_refreshSuccess(response) {
                            me.queryExecutionId = null;
                            me._updateDataGrid.call(me, {
                                page: me.currentPage

                            });
                        },
                        scope: this
                    },
                    failureCallback: {
                        fn: function EntityDataGrid_onActionUp_refreshFailure(response) {
                            Alfresco.util.PopupManager.displayMessage({
                                text: me.msg("message.details.failure")
                            });
                        },
                        scope: this
                    }
                });

            }

        },

        _addSortDnD: function() {
            var me = this;

            var ddGroup = "group-" + me.id;

            if (me.options.sortable) {
                // ////////////////////////////////////////////////////////////////////////////
                // Create DDTarget instances when DataTable is
                // initialized
                // ////////////////////////////////////////////////////////////////////////////
                YAHOO.util.DragDropMgr.refreshCache();

                var i, id, allRows = me.widgets.dataTable.getTbodyEl().rows;

                for (i = 0; i < allRows.length; i++) {
                    id = allRows[i].id;
                    // Clean up any existing Drag instances
                    if (me.widgets.dataTable.dtdTargets[id]) {
                        me.widgets.dataTable.dtdTargets[id].unreg();
                        delete me.widgets.dataTable.dtdTargets[id];
                    }
                    // Create a Drag instance for each row
                    me.widgets.dataTable.dtdTargets[id] = new YAHOO.util.DDTarget(id, ddGroup);
                }
            }

        },

        /**
          * Delete item(s).
          * 
          * @method onActionDelete
          * @param items
          *           {Object | Array} Object literal representing the Data Item to
          *           be actioned, or an Array thereof
          */
        onActionDelete: function EntityDataGrid_onActionDelete(p_items) {
            var me = this, items = YAHOO.lang.isArray(p_items) ? p_items : [p_items], hasMultiLevelItems = this._hasMultiLevelItems ? this._hasMultiLevelItems(items) : false;

            var fnActionDeleteConfirm = function EntityDataGrid__onActionDelete_confirm(items) {
                var nodeRefs = [];
                for (var i = 0, ii = items.length; i < ii; i++) {
                    nodeRefs.push(items[i].nodeRef);
                }

                this.modules.actions.genericAction({
                    success: {
                        event: {
                            name: this.scopeId + "dataItemsDeleted",
                            obj: {
                                items: items
                            }
                        },
                        message: this.msg("message.delete.success", items.length)
                    },
                    failure: {
                        callback:
                        {
                            fn: function(response, obj) {
                                if (response.json && response.json.message) {
                                    Alfresco.util.PopupManager.displayPrompt({
                                        title: me.msg("message.delete.failure"),
                                        text: response.json.message
                                    });
                                } else {
                                    Alfresco.util.PopupManager.displayMessage({
                                        text: me.msg("message.delete.failure")
                                    });
                                }
                            }
                        }
                    },
                    webscript: {
                        method: Alfresco.util.Ajax.DELETE,
                        name: "items"
                    },
                    config: {
                        requestContentType: Alfresco.util.Ajax.JSON,
                        dataObj: {
                            nodeRefs: nodeRefs
                        }
                    }
                });
            };

            if (hasMultiLevelItems) {
                if (this._showMultiLevelDeleteWarning) {
                    this._showMultiLevelDeleteWarning();
                }
            } else if (this._clearMultiLevelDeleteWarning) {
                this._clearMultiLevelDeleteWarning();
            }

            var promptTitle = this.msg("message.confirm.delete.title", items.length);
            var promptText = this.msg("message.confirm.delete.description", items.length);
            var promptConfig = {
                title: promptTitle,
                text: promptText,
                buttons: [{
                    text: this.msg("button.delete"),
                    handler: function EntityDataGrid__onActionDelete_delete() {
                        this.destroy();
                        if (me._clearMultiLevelDeleteWarning) {
                            me._clearMultiLevelDeleteWarning(true);
                        }
                        fnActionDeleteConfirm.call(me, items);
                    }
                }, {
                    text: this.msg("button.cancel"),
                    handler: function EntityDataGrid__onActionDelete_cancel() {
                        this.destroy();
                        if (me._clearMultiLevelDeleteWarning) {
                            me._clearMultiLevelDeleteWarning(true);
                        }
                    },
                    isDefault: true
                }]
            };

            if (hasMultiLevelItems) {
                promptConfig.title = this.msg("message.warn.delete.multilevel.title");
                promptConfig.text = '<div class="multi-level-warning-text">' + this.msg("message.warn.delete.multilevel.description") + '</div><div>' + promptText + '</div>';
                promptConfig.noEscape = true;
            }

            Alfresco.util.PopupManager.displayPrompt(promptConfig);
        },

        /**
          * Duplicate item(s).
          * 
          * @method onActionDuplicate
          * @param items
          *           {Object | Array} Object literal representing the Data Item to
          *           be actioned, or an Array thereof
          */
        onActionDuplicate: function EntityDataGrid_onActionDuplicate(p_items) {
            var me = this, items = YAHOO.lang.isArray(p_items) ? p_items : [p_items], destinationNodeRef = this.modules.dataGrid.datalistMeta.nodeRef != null ? new Alfresco.util.NodeRef(
                this.modules.dataGrid.datalistMeta.nodeRef) : new Alfresco.util.NodeRef(
                    this.modules.dataGrid.options.parentNodeRef), nodeRefs = [];

            var fnActionDuplicateConfirm = function EntityDataGrid__onActionDuplicate_confirm(items) {
                for (var i = 0, ii = items.length; i < ii; i++) {
                    nodeRefs.push(items[i].nodeRef);
                }

                this.modules.actions.genericAction({
                    success: {
                        event: {
                            name: this.scopeId + "dataItemsDuplicated",
                            obj: {
                                items: items
                            }
                        },
                        message: this.msg("message.duplicate.success", items.length)
                    },
                    failure: {
                        message: this.msg("message.duplicate.failure")
                    },
                    webscript: {
                        method: Alfresco.util.Ajax.POST,
                        name: "duplicate/node/" + destinationNodeRef.uri
                    },
                    config: {
                        requestContentType: Alfresco.util.Ajax.JSON,
                        dataObj: {
                            nodeRefs: nodeRefs
                        }
                    }
                });
            }
            Alfresco.util.PopupManager.displayPrompt({
                title: this.msg("message.confirm.duplicate.title", items.length),
                text: this.msg("message.confirm.duplicate.description", items.length),
                buttons: [{
                    text: this.msg("button.duplicate"),
                    handler: function EntityDataGrid__onActionDelete_delete() {
                        this.destroy();
                        fnActionDuplicateConfirm.call(me, items);
                    }
                }, {
                    text: this.msg("button.cancel"),
                    handler: function EntityDataGrid__onActionDelete_cancel() {
                        this.destroy();
                    },
                    isDefault: true
                }]
            });

        },


        onActionColumnConf: function EntityDataGrid_onActionColumnConf() {

            var popupKind = "columns-conf";
            var html = '<div class="hd">' + this.msg("header." + popupKind + ".picker") + '</div>';
            html += '<div class="bd">';
            html += '<form  class="form-container">';
            html += '<div class="form-fields bulk-edit">';
            html += '   <div class="set">';
            html += '        <div class="form-field">';
            html += '			<div  id="' + this.id + '-columns-list" />'
            html += '          </div>';
            html += '       </div>';
            html += '    </div>';
            html += '<div id="' + this.id + '-' + popupKind + '-ft" class="bdft">';
            html += '</div>';
            html += '</form></div>';

            var containerDiv = document.createElement("div");
            containerDiv.innerHTML = html;

            this.widgets.columnsListPanel = Alfresco.util.createYUIPanel(containerDiv, {
                draggable: true,
                width: PICKER_PANEL_WIDTH
            });

            var hiddenColumnsInPopup = ["bcpg_startEffectivity", "bcpg_endEffectivity", "bcpg_depthLevel"];
            
            if(this.options.columnFormId == "product-list"){
                hiddenColumnsInPopup = []; 
            }

            var itemType = this.options.itemType != null ? this.options.itemType : this.datalistMeta.itemType;
            var containerEl = Dom.get(this.id + '-columns-list').parentNode;
            var siteId = this.options.siteId;

            var timeStamp = (new Date().getTime());

            Alfresco.util.Ajax.jsonGet({
                url: Alfresco.constants.URL_SERVICECONTEXT + "module/entity-datagrid/config/columns?mode=datagrid-prefs&itemType=" + encodeURIComponent(itemType) + "&clearCache=true"
                    + (siteId ? "&siteId=" + siteId : "")
                    + (this.entity != null ? "&entityType=" + encodeURIComponent(this.entity.type) : "")
                    + (this.options.entityNodeRef != null ? "&entityNodeRef=" + encodeURIComponent(this.options.entityNodeRef) : "")
                    + (this.options.columnFormId != null ? "&formId=" + this.options.columnFormId : "")
                    + (this.options.list != null ? "&list=" + this.options.list : "")
                    + ("&noCache=" + timeStamp),
                successCallback: {
                    fn: function(response) {

                        this._renderCheckboxPicker({
                            containerEl: containerEl,
                            panel: this.widgets.columnsListPanel,
                            title: this.msg("label.select-columns.title"),
                            itemsHtml: this._buildColumnPickerItems(response.json.columns, hiddenColumnsInPopup),
                            selectAllButtons: true
                        });

                        var divEl = Dom.get(this.id + '-columns-conf-ft');

                        divEl.innerHTML = '<input id="' + this.id + '-bulk-edit-ok" type="button" value="' + this.msg("button.ok") + '" />';

                        var updateSelection = function() {
                            var prefsValue = {};
                            var selectedFields = Selector.query('input[type="checkbox"]', containerEl);
                            for (var z = 0; z < selectedFields.length; z++) {
                                var fieldNode = selectedFields[z];
                                prefsValue[fieldNode.value] = { checked: fieldNode.checked };
                            }
                            return prefsValue;
                        };

                        this.widgets.okBkButton = Alfresco.util.createYUIButton(this, "bulk-edit-ok", function() {
                            var prefsValue = updateSelection();
                            YAHOO.Bubbling.fire("changeSelectedColumns", {
                                selectedColumns: prefsValue
                            });

                            this.widgets.columnsListPanel.hide();


                            setTimeout(function() {
                                YAHOO.Bubbling.fire("activeDataListChanged",
                                    {
                                        clearCache: true,
                                        cacheTimeStamp: timeStamp
                                    }
                                );
                            }, 1000);


                        });

                    },
                    scope: this
                }
            });

            this.widgets.columnsListPanel.show();

        },

        /**
          * Builds the checkbox lines of the column chooser, nested columns included.
          *
          * @method _buildColumnPickerItems
          * @param columns {Array} the columns returned by the configuration webscript
          * @param hiddenColumns {Array} the column names never offered to the user
          * @return {String} the list items markup
          */
        _buildColumnPickerItems: function EntityDataGrid__buildColumnPickerItems(columns, hiddenColumns) {
            var me = this, itemsHtml = "", index = 0;

            var appendColumn = function(column, value) {
                if (column.label && column.label != "hidden" && hiddenColumns.indexOf(value) < 0) {
                    itemsHtml += me._buildPickerItem({
                        id: "propSelected-" + index,
                        value: value,
                        label: column.label,
                        checked: column.checked
                    });
                }
                index++;
            };

            for (var i = 0; i < columns.length; i++) {
                var column = columns[i];
                var value = column.name.replace(":", "_");

                appendColumn(column, value);

                if (column.dataType == "nested_column") {
                    for (var j = 0; j < column.columns.length; j++) {
                        appendColumn(column.columns[j], value + "_" + column.columns[j].name.replace(":", "_"));
                    }
                }
            }

            return itemsHtml;
        },

        /**
          * Builds one checkbox line of a picker.
          *
          * @method _buildPickerItem
          * @param item {object} the html id, the submitted value, the label and the initial state
          * @return {String} the list item markup
          */
        _buildPickerItem: function EntityDataGrid__buildPickerItem(item) {
            var encodedLabel = Alfresco.util.encodeHTML(item.label);

            return '<li class="picker-list-item" data-label="' + encodedLabel + '">'
                + '<input id="' + item.id + '" type="checkbox" name="propChecked" value="' + item.value + '"' + (item.checked ? ' checked' : '') + '/>'
                + '<label for="' + item.id + '" title="' + encodedLabel + '">' + encodedLabel + '</label>'
                + '</li>';
        },

        /**
          * Renders the checkbox picker shared by the column chooser and the bulk edit field chooser.
          *
          * Both dialogs offer one checkbox per property, so they share the same search field, four
          * column layout and internal scroll. Only the column chooser gets the select all and deselect
          * all buttons, a bulk edit form holding every field of the type being unusable.
          *
          * @method _renderCheckboxPicker
          * @param picker {object} the container element, the panel to resize, the title, the items
          *           markup and whether the select all buttons are wanted
          */
        _renderCheckboxPicker: function EntityDataGrid__renderCheckboxPicker(picker) {
            var me = this;

            var html = '<div class="picker-list-header">';
            html += '<span class="picker-list-title">' + picker.title + '</span>';
            html += '<input class="picker-list-filter" type="text" autocomplete="off" placeholder="'
                + this.msg("label.picker-list.filter") + '" />';

            if (picker.selectAllButtons) {
                html += '<span class="picker-list-actions">'
                    + '<input id="' + this.id + '-columns-select-all" type="button" value="' + this.msg("button.columns-conf.select-all") + '" />'
                    + '<input id="' + this.id + '-columns-deselect-all" type="button" value="' + this.msg("button.columns-conf.deselect-all") + '" />'
                    + '</span>';
            }

            html += '</div>';
            html += '<ul class="picker-list">' + picker.itemsHtml + '</ul>';
            html += '<div class="picker-list-no-match hidden">' + this.msg("label.picker-list.no-match") + '</div>';

            picker.containerEl.innerHTML = html;

            Event.on(Selector.query("input.picker-list-filter", picker.containerEl, true), "input", function() {
                me._filterPickerItems(picker.containerEl, this.value);
            });

            if (picker.selectAllButtons) {
                this.widgets.selectAllColumnsButton = Alfresco.util.createYUIButton(this, "columns-select-all", function() {
                    this._checkVisiblePickerItems(picker.containerEl, true);
                });

                this.widgets.deselectAllColumnsButton = Alfresco.util.createYUIButton(this, "columns-deselect-all", function() {
                    this._checkVisiblePickerItems(picker.containerEl, false);
                });
            }

            if (picker.panel != null) {
                picker.panel.cfg.setProperty("width", PICKER_PANEL_WIDTH);
                picker.panel.center();
            }
        },

        /**
          * Hides the picker lines whose label does not contain the searched text.
          *
          * @method _filterPickerItems
          * @param containerEl {object} the element holding the picker
          * @param filterText {String} the text typed in the search field
          */
        _filterPickerItems: function EntityDataGrid__filterPickerItems(containerEl, filterText) {
            var normalizedFilter = this._normalizeForFilter(filterText);
            var items = Selector.query("li.picker-list-item", containerEl);
            var matchCount = 0;

            for (var i = 0; i < items.length; i++) {
                var matches = normalizedFilter.length === 0
                    || this._normalizeForFilter(items[i].getAttribute("data-label")).indexOf(normalizedFilter) > -1;

                Dom.setStyle(items[i], "display", matches ? "" : "none");

                if (matches) {
                    matchCount++;
                }
            }

            var noMatchEl = Selector.query("div.picker-list-no-match", containerEl, true);

            if (matchCount === 0) {
                Dom.removeClass(noMatchEl, "hidden");
            } else {
                Dom.addClass(noMatchEl, "hidden");
            }
        },

        /**
          * Ticks or unticks every line the search field currently leaves visible, so that a filter
          * narrows down what the select all buttons act on.
          *
          * @method _checkVisiblePickerItems
          * @param containerEl {object} the element holding the picker
          * @param checked {boolean} the state to apply
          */
        _checkVisiblePickerItems: function EntityDataGrid__checkVisiblePickerItems(containerEl, checked) {
            var items = Selector.query("li.picker-list-item", containerEl);

            for (var i = 0; i < items.length; i++) {
                if (Dom.getStyle(items[i], "display") != "none") {
                    var checkbox = Selector.query('input[type="checkbox"]', items[i], true);
                    if (checkbox != null) {
                        checkbox.checked = checked;
                    }
                }
            }
        },

        /**
          * Lowers the case and drops the diacritics so that typing "energie" finds "Énergie".
          *
          * @method _normalizeForFilter
          * @param text {String} the text to compare
          * @return {String} the comparable text
          */
        _normalizeForFilter: function EntityDataGrid__normalizeForFilter(text) {
            var lowered = (text != null ? text : "").toLowerCase();

            return lowered.normalize ? lowered.normalize("NFD").replace(/[\u0300-\u036f]/g, "") : lowered;
        },

        _hasMultiLevelItems: function EntityDataGrid__hasMultiLevelItems(items) {
            if (!items || !items.length) {
                return false;
            }

            for (var i = 0; i < items.length; i++) {
                var currentItem = items[i];
                if (!currentItem || !currentItem.itemData) {
                    continue;
                }

                var multiLevelFlag = currentItem.itemData.isMultiLevel;
                if (multiLevelFlag === true || multiLevelFlag === 1 || multiLevelFlag === "1") {
                    return true;
                }

                if (typeof multiLevelFlag === "string" && multiLevelFlag.toLowerCase() === "true") {
                    return true;
                }

                if (typeof multiLevelFlag === "number" && multiLevelFlag > 0) {
                    return true;
                }
            }

            return false;
        },

        _getMultiLevelWarningNode: function EntityDataGrid__getMultiLevelWarningNode() {
            if (!this.modules || !this.modules.dataGrid) {
                return null;
            }
            return Dom.get(this.modules.dataGrid.id + "-message");
        },

        _showMultiLevelDeleteWarning: function EntityDataGrid__showMultiLevelDeleteWarning() {
            var messageNode = this._getMultiLevelWarningNode();
            if (!messageNode) {
                return;
            }

            messageNode.innerHTML = '<span class="info">' + this.msg("message.warn.delete.multilevel.banner") + "</span>";
            Dom.removeClass(messageNode, "hidden");
            Dom.addClass(messageNode, "multi-level-warning");
        },

        _clearMultiLevelDeleteWarning: function EntityDataGrid__clearMultiLevelDeleteWarning(forceHide) {
            var messageNode = this._getMultiLevelWarningNode();
            if (!messageNode) {
                return;
            }

            Dom.removeClass(messageNode, "multi-level-warning");
            Dom.removeClass(messageNode, "warning");

            if (forceHide) {
                Dom.addClass(messageNode, "hidden");
                messageNode.innerHTML = "";
            }
        },

        /**
          * Freezes the datagrid scroll while a create/edit dialog is open and restores it when the
          * dialog is hidden or destroyed. Avoids the Chrome double-scroll issue where the mouse
          * wheel over the dialog scrolls the list behind it (#29203).
          *
          * @method _freezeGridScroll
          * @param p_dialog
          *           {object} the Alfresco.module.SimpleDialog instance being shown
          */
        _freezeGridScroll: function EntityDataGrid__freezeGridScroll(p_dialog) {
            var gridEl = Dom.get(this.id + "-grid");
            if (gridEl === null) {
                return;
            }

            Dom.addClass(gridEl, "dialog-opened");

            if (p_dialog && p_dialog.dialog) {
                var unfreeze = function EntityDataGrid__freezeGridScroll_unfreeze() {
                    Dom.removeClass(gridEl, "dialog-opened");
                };
                p_dialog.dialog.hideEvent.subscribe(unfreeze);
                p_dialog.dialog.destroyEvent.subscribe(unfreeze);
            }
        }

    };

})();
