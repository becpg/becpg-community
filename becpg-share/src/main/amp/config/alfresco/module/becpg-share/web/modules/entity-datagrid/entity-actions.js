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
 * Entity Data Grid Actions module
 * 
 * @namespace beCPG.module
 * @class beCPG.module.EntityDataGridActions
 */
(function() {
	
	var Dom = YAHOO.util.Dom, Bubbling = YAHOO.Bubbling;
	
	/**
	 * beCPG.module.EntityDataGridActions implementation
	 */
	beCPG.module.EntityDataGridActions = {};
	beCPG.module.EntityDataGridActions.prototype = {
	   /**
		 * ACTIONS WHICH ARE LOCAL TO THE DATAGRID COMPONENT
		 */

	   /**
		 * New Row button click handler
		 * 
		 * @method onActionCreate
		 * @param e
		 *           {object} DomEvent
		 * @param p_obj
		 *           {object} Object passed back from addListener method
		 */
	   onActionCreate : function EntityDataGrid_onActionCreate(e, p_obj) {
		   var destination = this.datalistMeta.nodeRef!=null ?this.datalistMeta.nodeRef : this.options.parentNodeRef , itemType = this.options.itemType != null ? this.options.itemType
		         : this.datalistMeta.itemType, me = this;

		   // Intercept before dialog show
		   var doBeforeDialogShow = function EntityDataGrid_onActionCreate_doBeforeDialogShow(p_form, p_dialog) {
			   Alfresco.util.populateHTML([ p_dialog.id + "-dialogTitle", this.msg("label.new-row.title") ], [
			         p_dialog.id + "-dialogHeader", this.msg("label.new-row.header") ]);

			   // Is it a bulk action?
			   if (Dom.get(p_dialog.id + "-form-bulkAction")) {
				   Dom.get(p_dialog.id + "-form-bulkAction").checked = this.onActionCreateBulkEdit;
				   Dom.get(p_dialog.id + "-form-bulkAction-msg").innerHTML = this.msg("button.bulk-action-create");
			   }
			   
			   if(this.options.formWidth !="34em"){
                  Dom.addClass(p_dialog.id+"-dialog","large-dialog");
                }
			  
		   };

		   var templateUrl = YAHOO.lang
		         .substitute(
		               Alfresco.constants.URL_SERVICECONTEXT
		                     + "components/form?bulkEdit=true&entityNodeRef={entityNodeRef}&itemKind={itemKind}&itemId={itemId}&destination={destination}&mode={mode}&submitType={submitType}&showCancelButton=true&dataListsName={dataListsName}",
		               {
		                  itemKind : "type",
		                  itemId : itemType,
		                  destination : destination,
		                  mode : "create",
		                  submitType : "json",
		                  entityNodeRef : this.options.entityNodeRef,
		                  dataListsName :  encodeURIComponent(this.datalistMeta.name!=null ? this.datalistMeta.name : this.options.list)
		               });

		   // Using Forms Service, so always create new instance
		   var createRow = new Alfresco.module.SimpleDialog(this.id + "-createRow");
		   createRow.bulkEdit = false;
		   createRow.setOptions({
		      width : this.options.formWidth,
		      templateUrl : templateUrl,
		      actionUrl : null,
		      destroyOnHide : true,
		      doBeforeDialogShow : {
		         fn : doBeforeDialogShow,
		         scope : this
		      },
		      doBeforeFormSubmit : {
		         fn : function() {
			         var checkBoxEl = Dom.get(this.id + "-createRow" + "-form-bulkAction");

			         if (checkBoxEl && checkBoxEl.checked) {
				         me.onActionCreateBulkEdit = true;
			         } else {
				         me.onActionCreateBulkEdit = false;
			         }
		         },
		         scope : this
		      },
		      onSuccess : {
		         fn : function EntityDataGrid_onActionCreate_success(response) {
			         YAHOO.Bubbling.fire(me.scopeId + "dataItemCreated", {
			            nodeRef : response.json.persistedObject,
			            callback : function(item) {

				            Alfresco.util.PopupManager.displayMessage({
					            text : me.msg("message.new-row.success")
				            });

				            // recall edit for next item

				            if (me.onActionCreateBulkEdit) {
					            me.onActionCreate();
				            }

			            }
			         });

		         },
		         scope : this
		      },
		      onFailure : {
		         fn : function EntityDataGrid_onActionCreate_failure(response) {
			         Alfresco.util.PopupManager.displayMessage({
				         text : me.msg("message.new-row.failure")
			         });
		         },
		         scope : this
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
	   onActionEdit : function EntityDataGrid_onActionEdit(item) {
		   var me = this;

		   // Intercept before dialog show
		   var doBeforeDialogShow = function EntityDataGrid_onActionEdit_doBeforeDialogShow(p_form, p_dialog) {
			   Alfresco.util.populateHTML([ p_dialog.id + "-dialogTitle", this.msg("label.edit-row.title") ]);

			   // Is it a bulk action?
			   if (Dom.get(p_dialog.id + "-form-bulkAction")) {
				   Dom.get(p_dialog.id + "-form-bulkAction").checked = this.onActionEditBulkEdit;
				   Dom.get(p_dialog.id + "-form-bulkAction-msg").innerHTML = this.msg("button.bulk-action-edit");
			   }

			   if(this.options.formWidth !="34em"){
	                 Dom.addClass(p_dialog.id+"-dialog","large-dialog");
	           }
			   
		   };

		   var templateUrl = YAHOO.lang
		         .substitute(
		               Alfresco.constants.URL_SERVICECONTEXT
		                     + "components/form?bulkEdit=true&entityNodeRef={entityNodeRef}&itemKind={itemKind}&itemId={itemId}&mode={mode}&submitType={submitType}&showCancelButton=true&dataListsName={dataListsName}",
		               {
		                  itemKind : "node",
		                  itemId : item.nodeRef,
		                  mode : "edit",
		                  submitType : "json",
		                  entityNodeRef : this.options.entityNodeRef,
		                  dataListsName :  encodeURIComponent(this.datalistMeta.name!=null ? this.datalistMeta.name : this.options.list)
		               });

		   // Using Forms Service, so always create new instance

		   var editDetails = new Alfresco.module.SimpleDialog(this.id + "-editDetails");

		   editDetails.bulkEdit = false;
		   editDetails.setOptions({
		      width : this.options.formWidth,
		      templateUrl : templateUrl,
		      actionUrl : null,
		      destroyOnHide : true,
		      doBeforeDialogShow : {
		         fn : doBeforeDialogShow,
		         scope : this
		      },
		      doBeforeFormSubmit : {
		         fn : function() {
			         var checkBoxEl = Dom.get(me.id + "-editDetails" + "-form-bulkAction");

			         if (checkBoxEl && checkBoxEl.checked) {
				         me.onActionEditBulkEdit = true;
			         } else {
				         me.onActionEditBulkEdit = false;
			         }
		         },
		         scope : this
		      },
		      onSuccess : {
		         fn : function EntityDataGrid_onActionEdit_success(response) {

			         // Fire "itemUpdated" event
			         Bubbling.fire(me.scopeId + "dataItemUpdated", {
			            nodeRef : response.json.persistedObject,
			            callback : function(item) {

				            // Display success message
				            Alfresco.util.PopupManager.displayMessage({
					            text : me.msg("message.details.success")
				            });

				            if (me.onActionEditBulkEdit) {
					            var recordFound = me._findNextItemByParameter(response.json.persistedObject, "nodeRef");
					            if (recordFound != null) {
						            me.onActionEdit(recordFound);
					            }
				            }

			            }
			         });

		         },
		         scope : this
		      },
		      onFailure : {
		         fn : function EntityDataGrid_onActionEdit_failure(response) {
			         Alfresco.util.PopupManager.displayMessage({
				         text : me.msg("message.details.failure")
			         });
		         },
		         scope : this
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
	   onActionUp : function EntityDataGrid_onActionUp(p_items) {
		   var me = this;
		   if (me.options.sortable) {
			   var items = YAHOO.lang.isArray(p_items) ? p_items : [ p_items ];

			   if (items.length > 0) {
				   var recordFound = me._findPrevItemByParameter(items[0].nodeRef, "nodeRef");

				   if (recordFound != null) {

					   me._sort(items, recordFound, "up");

				   }

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
	   onActionDown : function EntityDataGrid_onActionDown(p_items) {
		   var me = this;
		   if (me.options.sortable) {
			   var items = YAHOO.lang.isArray(p_items) ? p_items : [ p_items ];

			   if (items.length > 0) {
				   var recordFound = me._findNextItemByParameter(items[items.length - 1].nodeRef, "nodeRef");

				   if (recordFound != null) {

					   me._sort(items, recordFound, "down");

				   }

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
	   _sort : function EntityDataGrid__sort(items, node, dir) {
		   var me = this, nodeRefs = [];
		   if (me.options.sortable) {

			   for ( var i = 0, ii = items.length; i < ii; i++) {
				   nodeRefs.push(items[i].nodeRef);
			   }

			   var url = me.options.sortUrl + "/" + node.nodeRef.replace(":/", "") + "?selectedNodeRefs="
			         + nodeRefs.join(",") + "&dir=" + dir;

			   Alfresco.util.Ajax.jsonPost({
			      url : url,
			      successCallback : {
			         fn : function EntityDataGrid_onActionUp_refreshSuccess(response) {
				         me._updateDataGrid.call(me, {
					         page : me.currentPage
				         });
			         },
			         scope : this
			      },
			      failureCallback : {
			         fn : function EntityDataGrid_onActionUp_refreshFailure(response) {
				         Alfresco.util.PopupManager.displayMessage({
					         text : me.msg("message.details.failure")
				         });
			         },
			         scope : this
			      }
			   });

		   }

	   },

	   _addSortDnD : function() {
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
	   onActionDelete : function EntityDataGrid_onActionDelete(p_items) {
		   var me = this, items = YAHOO.lang.isArray(p_items) ? p_items : [ p_items ];

		   var fnActionDeleteConfirm = function EntityDataGrid__onActionDelete_confirm(items) {
			   var nodeRefs = [];
			   for ( var i = 0, ii = items.length; i < ii; i++) {
				   nodeRefs.push(items[i].nodeRef);
			   }

			   this.modules.actions.genericAction({
			      success : {
			         event : {
			            name : this.scopeId + "dataItemsDeleted",
			            obj : {
				            items : items
			            }
			         },
			         message : this.msg("message.delete.success", items.length)
			      },
			      failure : {
				      message : this.msg("message.delete.failure")
			      },
			      webscript : {
			         method : Alfresco.util.Ajax.DELETE,
			         name : "items"
			      },
			      config : {
			         requestContentType : Alfresco.util.Ajax.JSON,
			         dataObj : {
				         nodeRefs : nodeRefs
			         }
			      }
			   });
		   };

		   Alfresco.util.PopupManager.displayPrompt({
		      title : this.msg("message.confirm.delete.title", items.length),
		      text : this.msg("message.confirm.delete.description", items.length),
		      buttons : [ {
		         text : this.msg("button.delete"),
		         handler : function EntityDataGrid__onActionDelete_delete() {
			         this.destroy();
			         fnActionDeleteConfirm.call(me, items);
		         }
		      }, {
		         text : this.msg("button.cancel"),
		         handler : function EntityDataGrid__onActionDelete_cancel() {
			         this.destroy();
		         },
		         isDefault : true
		      } ]
		   });
	   },

	   /**
		 * Duplicate item(s).
		 * 
		 * @method onActionDuplicate
		 * @param items
		 *           {Object | Array} Object literal representing the Data Item to
		 *           be actioned, or an Array thereof
		 */
	   onActionDuplicate : function EntityDataGrid_onActionDuplicate(p_items) {
		   var items = YAHOO.lang.isArray(p_items) ? p_items : [ p_items ], destinationNodeRef = this.modules.dataGrid.datalistMeta.nodeRef!=null ? new Alfresco.util.NodeRef(
		         this.modules.dataGrid.datalistMeta.nodeRef): new Alfresco.util.NodeRef(
				         this.modules.dataGrid.options.parentNodeRef), nodeRefs = [];

		   for ( var i = 0, ii = items.length; i < ii; i++) {
			   nodeRefs.push(items[i].nodeRef);
		   }

		   this.modules.actions.genericAction({
		      success : {
		         event : {
		            name : this.scopeId + "dataItemsDuplicated",
		            obj : {
			            items : items
		            }
		         },
		         message : this.msg("message.duplicate.success", items.length)
		      },
		      failure : {
			      message : this.msg("message.duplicate.failure")
		      },
		      webscript : {
		         method : Alfresco.util.Ajax.POST,
		         name : "duplicate/node/" + destinationNodeRef.uri
		      },
		      config : {
		         requestContentType : Alfresco.util.Ajax.JSON,
		         dataObj : {
			         nodeRefs : nodeRefs
		         }
		      }
		   });
	   }

	};
	
})();
