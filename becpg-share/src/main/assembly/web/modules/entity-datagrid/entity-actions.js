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
			   
			   var propInputNodeRefs = {};
			   propInputNodeRefs["bcpg_parentLevel"] = this.parentInputNodeRef;
			   propInputNodeRefs["bcpg_variantIds"] = this.variantInputNodeRef;
			   
			   for(var prop in propInputNodeRefs) {
				   if(propInputNodeRefs[prop] != null) {
	                	Dom.get(p_dialog.id + "_prop_"+prop+"-added").value = propInputNodeRefs[prop];
			        	Bubbling.fire(p_dialog.id + "_prop_"+prop + "refreshContent", propInputNodeRefs[prop], this );
			        }
			   }

		   };
		   
		   //Note is important to have the same popupId as component manager will use it to destroy previous popup components
		   var popupId = this.id + "-editDetails";

		   var templateUrl = YAHOO.lang
		         .substitute(
		               Alfresco.constants.URL_SERVICECONTEXT
		                     + "components/form?bulkEdit=true&entityNodeRef={entityNodeRef}&entityType={entityType}&itemKind={itemKind}&itemId={itemId}&destination={destination}&mode={mode}&submitType={submitType}&showCancelButton=true&dataListsName={dataListsName}&siteId={siteId}",
		               {
		                  itemKind : "type",
		                  itemId : itemType,
		                  destination : destination,
		                  mode : "create",
		                  submitType : "json",
		                  entityNodeRef : this.options.entityNodeRef,
		                  entityType:   this.entity!=null ?  encodeURIComponent(this.entity.type) :"",
		                  dataListsName :  encodeURIComponent(this.datalistMeta.name!=null ? this.datalistMeta.name : this.options.list),
		                  siteId : this.options.siteId
		               });

		   // Using Forms Service, so always create new instance
		   var createRow = new Alfresco.module.SimpleDialog(popupId);
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
			         var checkBoxEl = Dom.get(popupId+ "-form-bulkAction");
			         
			         var parentInput =   Dom.get(popupId+"_prop_bcpg_parentLevel-added");
			         var variantInput =   Dom.get(popupId+"_prop_bcpg_variantIds-added");
			         me.parentInputNodeRef = null;
			         me.variantInputNodeRef = null;
			         if(parentInput !=null && parentInput.value!=null && parentInput.value.length>0){
			        	 me.parentInputNodeRef = parentInput.value;
			         }
			         if(variantInput !=null && variantInput.value!=null && variantInput.value.length>0){
			        	 me.variantInputNodeRef = variantInput.value;
			         }
	
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
		        	 
		        	 if(me.parentInputNodeRef!=null){
		            		
                 		url = Alfresco.constants.PROXY_URI + "becpg/entity/datalists/openclose?nodeRef=" 
                 		+me.parentInputNodeRef+"&expand=true&entityNodeRef="+me.options.entityNodeRef +"&listType="+itemType;
                 	  Alfresco.util.Ajax
                           .jsonPost(
                           {
                               url : url,
                               successCallback :
                               {
                                   fn : function EntityDataGrid_onCollapsedAndExpanded(
                                           response)
                                   {

                                       me.queryExecutionId = null;
	                                   me._updateDataGrid.call(me,
	                                     {
	                                         page : me.currentPage
	                                    });

	           				            
	           				        YAHOO.Bubbling.fire("dirtyDataTable");   
	                                   

           				            Alfresco.util.PopupManager.displayMessage({
           					            text : me.msg("message.new-row.success")
           				            });

           				            // recall edit for next item

           				            if (me.onActionCreateBulkEdit) {
           					            me.onActionCreate();
           				            }

                                   },
                                   scope : this
                               }
                           });
		            		
		            	} else {
		        	 
					         YAHOO.Bubbling.fire(me.scopeId + "dataItemCreated", {
					            nodeRef : response.json.persistedObject,
					            callback : function(item) {

						            	YAHOO.Bubbling.fire("refreshFloatingHeader");
			
							            Alfresco.util.PopupManager.displayMessage({
								            text : me.msg("message.new-row.success")
							            });
			
							            // recall edit for next item
			
							            if (me.onActionCreateBulkEdit) {
								            me.onActionCreate();
							            }
					            
					            }
					         });
		            	}

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
		                     + "components/form?bulkEdit=true&entityNodeRef={entityNodeRef}&entityType={entityType}&itemKind={itemKind}&itemId={itemId}&mode={mode}&submitType={submitType}&showCancelButton=true&dataListsName={dataListsName}&siteId={siteId}",
		               {
		                  itemKind : "node",
		                  itemId : item.nodeRef,
		                  mode : "edit",
		                  submitType : "json",
		                  entityType:  this.entity!=null ?  encodeURIComponent(this.entity.type) :"",
		                  entityNodeRef : this.options.entityNodeRef,
		                  dataListsName :  encodeURIComponent(this.datalistMeta.name!=null ? this.datalistMeta.name : this.options.list),
		                  siteId : this.options.siteId
		               });

		   //Note is important to have the same popupId as component manager will use it to destroy previous popup components
		   var popupId = this.id + "-editDetails";

		   var editDetails = new Alfresco.module.SimpleDialog(popupId);

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
			         var checkBoxEl = Dom.get(popupId + "-form-bulkAction");

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
	   onActionDown : function EntityDataGrid_onActionDown(p_items) {
		   var me = this;
		   if (me.options.sortable) {
			   var items = YAHOO.lang.isArray(p_items) ? p_items : [ p_items ];

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
			        	 me.queryExecutionId = null;
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
		               callback:
		               {
		                  fn: function(response, obj)
		                  {
		                	  if (response.json && response.json.message) {
		                          Alfresco.util.PopupManager.displayPrompt({
		                             title : me.msg("message.delete.failure"),
		                             text : response.json.message
		                          });
		                       } else {
		                          Alfresco.util.PopupManager.displayMessage({
		                             text : me.msg("message.delete.failure")
		                          });
		                      }
		                  }
		               }
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
			   var me = this, items = YAHOO.lang.isArray(p_items) ? p_items : [ p_items ], destinationNodeRef = this.modules.dataGrid.datalistMeta.nodeRef!=null ? new Alfresco.util.NodeRef(
			         this.modules.dataGrid.datalistMeta.nodeRef): new Alfresco.util.NodeRef(
					         this.modules.dataGrid.options.parentNodeRef), nodeRefs = [];
	
			   var fnActionDuplicateConfirm = function EntityDataGrid__onActionDuplicate_confirm(items) {
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
		   Alfresco.util.PopupManager.displayPrompt({
			      title : this.msg("message.confirm.duplicate.title", items.length),
			      text : this.msg("message.confirm.duplicate.description", items.length),
			      buttons : [ {
			         text : this.msg("button.duplicate"),
			         handler : function EntityDataGrid__onActionDelete_delete() {
				         this.destroy();
				         fnActionDuplicateConfirm.call(me, items);
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
	   
	   
	   onActionColumnConf : function EntityDataGrid_onActionColumnConf() {
		   
		   if(!this.services.preferences) {
		   		this.services.preferences = new Alfresco.service.Preferences();
	   		}
	   
    	   var me = this;
    	   var popupKind = "columns-conf";
    	   var html = '<div class="hd">' + this.msg("header." + popupKind + ".picker") + '</div>';
   			html += '<div class="bd">';
   			html += '<form  class="form-container">';
   			html += '<div class="form-fields bulk-edit">';
   			html += '   <div class="set">';
   			html += '        <div class="form-field">';
   			html += '			<div  id="'+this.id+'-columns-list" />'		
   			html += '          </div>';
   			html += '       </div>';
   			html += '    </div>';
   			html += '<div id="'+this.id+'-'+popupKind+'-ft" class="bdft">';
   			html += '</div>';
   			html += '</form></div>';
   			   
			    var containerDiv = document.createElement("div");
				containerDiv.innerHTML = html;
   			
				this.widgets.columnsListPanel = Alfresco.util.createYUIPanel(containerDiv, {
					draggable : true,
					width : "33em"
				});
				
				var hiddenColumnsInPopup = ["bcpg_startEffectivity", "bcpg_endEffectivity", "bcpg_depthLevel"];
				
				var itemType =  this.options.itemType != null ? this.options.itemType : this.datalistMeta.itemType;
				var containerEl = Dom.get(this.id+'-columns-list').parentNode, html = "";
				var colCount = 0;
				var siteId = this.options.siteId;
				
				var timeStamp =  (new Date().getTime());
					
				Alfresco.util.Ajax.jsonGet({
				url : Alfresco.constants.URL_SERVICECONTEXT + "module/entity-datagrid/config/columns?mode=datagrid-prefs&itemType=" + encodeURIComponent(itemType) + "&clearCache=true" 
					+ (this.options.siteId ? "&siteId=" + this.options.siteId : "")
					+ (this.entity!=null ? "&entityType="+encodeURIComponent(this.entity.type) : "")
					+ (this.options.entityNodeRef != null ? "&entityNodeRef=" + encodeURIComponent(this.options.entityNodeRef) : "")
					+ ("&noCache="+ timeStamp),
				successCallback : {
					fn : function (response) {
						var prefs = "fr.becpg.formulation.dashlet.custom.datagrid-prefs." + (this.entity != null ? this.entity.type.replace(":", "_") + "." : "") + itemType.replace(":", "_");
						var idx=0;
						for (var i = 0; i < response.json.columns.length; i++) {
							var column = response.json.columns[i];
							var propLabel = column.label;
							var value = column.name.replace(":", "_");
							var checked = column.checked ? "checked" : "";
							
							if (propLabel!="hidden" && propLabel && hiddenColumnsInPopup.indexOf(value) < 0) {
								html += '<li class=""><input id="propSelected-' + idx + '" type="checkbox" name="propChecked" value="'+ value +'" '+ checked + '/>' 
										+ '<label for="propSelected-' + idx + '" >' + propLabel + '</label></li>';
							}
							
							idx++;
							if (column.dataType == "nested_column") {
								for (var j = 0; j < column.columns.length; j++) {
									var nestedColumn = column.columns[j];								
									var subLabel = nestedColumn.label;
									var subValue = value+"_"+nestedColumn.name.replace(":", "_");
									
									if (subLabel!="hidden" && subLabel && hiddenColumnsInPopup.indexOf(subValue) < 0) {
										html += '<li class=""><input id="propSelected-' + idx + '" type="checkbox" name="propChecked" value="'+ subValue +'" '+  (nestedColumn.checked ? "checked" : "") + '/>' 
												+ '<label for="propSelected-' + idx + '" >' + subLabel + '</label></li>';
									}	
								  idx++;
								}
							}
							
						}

						 html = "<span>"+this.msg("label.select-columns.title")
						 	+"</span><br/><br/><ul style=\"width:" + ((colCount + 2) * 20) + "em;\">" + html + "</ul>";		    

						containerEl.innerHTML = html;
						 
						var divEl = Dom.get(this.id+'-columns-conf-ft');
						
						divEl.innerHTML = '<input id="'+this.id+'-bulk-edit-ok" type="button" value="'+this.msg("button.ok")+'" />';
						
			            this.widgets.okBkButton = Alfresco.util.createYUIButton(this, "bulk-edit-ok", function (){

			            	var selectedFields = Selector.query('input[type="checkbox"]', containerEl);
			            	var prefsValue = {};

			            	for ( var i in selectedFields) {
					 				var fieldId = selectedFields[i].value;
					 				prefsValue[fieldId] = {checked : selectedFields[i].checked}
					 			}
			            	
			            	me.services.preferences.set(prefs, prefsValue);
			            	
			            	this.widgets.columnsListPanel.hide();
			            	
			            	setTimeout(function(){
			            		YAHOO.Bubbling.fire("activeDataListChanged", 
			            	    		{ clearCache :true,
			            	    		  cacheTimeStamp : timeStamp }
			            	    );
			            	}, 1000);
			            	
			            });
					},
					scope : this
				}
			});
				
		this.widgets.columnsListPanel.show();
     
	   }

	};
	
})();
