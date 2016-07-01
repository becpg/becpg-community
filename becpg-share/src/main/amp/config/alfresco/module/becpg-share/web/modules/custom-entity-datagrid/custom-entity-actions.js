/*******************************************************************************
 *  Copyright (C) 2010-2016 beCPG. 
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
 * Entity Data Grid Custom Actions module
 * 
 * @namespace beCPG.module
 * @class beCPG.module.CustomEntityDataGridActions
 */
(function() {

	/**
	 * beCPG.module.CustomEntityDataGridActions implementation
	 */
	beCPG.module.CustomEntityDataGridActions = {};
	beCPG.module.CustomEntityDataGridActions.prototype = {
		/**
		 * ACTIONS WHICH ARE LOCAL TO THE DATAGRID COMPONENT
		 */

		/**
		 * @method onActionShowDetails
		 * @param items {Object | Array} Object literal representing the Data
		 *            Item to be actioned, or an Array thereof
		 */
		onActionShowDetails : function EntityDataGrid_onActionShowDetails(p_items) {
			var items = YAHOO.lang.isArray(p_items) ? p_items : [ p_items ], nodeRefs = [];

			for (var i = 0, ii = items.length; i < ii; i++) {
				nodeRefs.push(items[i].nodeRef);
			}

			var url = Alfresco.constants.URL_SERVICECONTEXT + "modules/entity-charact-details/entity-charact-details" + "?entityNodeRef="
					+ this.options.entityNodeRef + "&itemType="
					+ encodeURIComponent(this.options.itemType != null ? this.options.itemType : this.datalistMeta.itemType) + "&dataListName="
					+ encodeURIComponent(this.datalistMeta.name) + "&dataListItems=" + nodeRefs.join(",");

			this._showPanel(url, this.id, null ,"60em");

		},
		
		
		onActionSelectColor : function EntityDataGrid_onActionShowDetails(p_items) {
			var items = YAHOO.lang.isArray(p_items) ? p_items : [ p_items ], nodeRefs = [];

			for (var i = 0, ii = items.length; i < ii; i++) {
				nodeRefs.push(items[i].nodeRef);
			}
			var itemType =  this.options.itemType != null ? this.options.itemType : this.datalistMeta.itemType;
			
			beCPG.module.getColorPickerInstance().show({
				nodeRefs : nodeRefs,
				itemType : itemType
             });

		},

		onActionBulkEdit : function EntityDataGrid_onActionBulkEdit(p_items) {
			var items = YAHOO.lang.isArray(p_items) ? p_items : [ p_items ], me = this;

			function onActionBulkEdit_redirect(itemAssocName, assocName) {
				var nodeRefs = [];

				for (var i = 0, ii = items.length; i < ii; i++) {
					if (!assocName) {
						nodeRefs.push(items[i].nodeRef);
					} else {
						if (items[i].itemData[itemAssocName].value) {
							nodeRefs.push(items[i].itemData[itemAssocName].value);
						} else {
							for ( var j in items[i].itemData[itemAssocName]) {
								nodeRefs.push(items[i].itemData[itemAssocName][j].value);
							}
						}
					}
				}

				if (!assocName) {
					me._setupPropsPicker(nodeRefs);
				} else {
					window.location = Alfresco.constants.URL_PAGECONTEXT + "bulk-edit?nodeRefs=" + nodeRefs.join() + "&a=true&r=true";
				}
			}

			this._showWusedPopup("bulk-edit", items, onActionBulkEdit_redirect);

		},

		_setupPropsPicker : function  EntityDataGrid__setupPropsPicker(nodeRefs) {
			var containerEl = Dom.get(this.id+'-wused-selected-picker').parentNode, html = "";
			if (containerEl != null) {
				var inc = 0;
				var colCount = 0;

				for (var i = 0, ii = this.datalistColumns.length; i < ii; i++) {

					var column = this.datalistColumns[i];

					var propName = this._buildFormsName(column);
					var propLabel = column.label;
					if (!column.protectedField && !column.disabled && propLabel!="hidden" && !column.readOnly ) {

						var className = "";
						if (colCount < Math.floor(inc / 5)) {
							className = "reset ";
						}
						colCount = Math.floor(inc / 5);
						className += "column-" + colCount;

						html += '<li class="' + className + '"><input id="propSelected-' + i + '" type="checkbox" name="propChecked" value="'
								+ propName + '" /><label for="propSelected-' + i + '" >' + propLabel + '</label></li>';
						inc++;
					}
				}

				 html = "<span>"+this.msg("label.select-prop.title")
				 	+"</span><br/><br/><ul style=\"width:" + ((colCount + 1) * 20) + "em;\">" + html + "</ul>";
				
			    containerEl.innerHTML = html;

	           
				var divEl = Dom.get(this.id+'-bulk-edit-ft');
				divEl.innerHTML = '<input id="'+this.id+'-bulk-edit-ok" type="button" value="'+this.msg("button.ok")+'" />';
				
	            this.widgets.okBkButton = Alfresco.util.createYUIButton(this, "bulk-edit-ok", function (){
	            	this.widgets.wUsedPanel.hide();
					this._onEditSelected(nodeRefs, containerEl);
	            });

			}
		},
		
		_buildFormsName : function EntityDataGrid__buildFormsName(col) {
			var formsName = "";
			if (col.type == "association") {
				formsName = "assoc_";

			} else {
				formsName = "prop_";
			}
			formsName += col.name.replace(/:/g, "_");
			return formsName;

		},

		_onEditSelected : function EntityDataGrid___onEditSelected(nodeRefs,containerEl) {

			 var me = this;
			 var selectedFields = Selector.query('input[type="checkbox"]', containerEl);
			
			// Intercept before dialog show
			var doBeforeDialogShow = function BulkEdit_onNewRow_doBeforeDialogShow(p_form, p_dialog) {
				Alfresco.util.populateHTML([ p_dialog.id + "-dialogTitle", this.msg("label.edit-selected.title") ], [
						p_dialog.id + "-dialogHeader", this.msg("label.edit-selected.header") ]);

				if (Dom.get(p_dialog.id + "-form-bulkAction")) {
					Dom.setStyle(p_dialog.id + "-form-bulkAction", 'display', 'none');
					Dom.setStyle(p_dialog.id + "-form-bulkAction-msg", 'display', 'none');
				}

			};
			var displayFields = [];
			for ( var i in selectedFields) {
				if (selectedFields[i].checked) {
					displayFields.push(selectedFields[i].value);
				}
			}

			if (displayFields.length < 1) {
				Alfresco.util.PopupManager.displayMessage({
					text : this.msg("message.edit-selected.nofields")
				});
				return false;
			}

			var itemType =  this.options.itemType != null ? this.options.itemType : this.datalistMeta.itemType;
			
			var templateUrl = YAHOO.lang
					.substitute(
							Alfresco.constants.URL_SERVICECONTEXT
									+ "components/form?formId={formId}&bulkEdit=true&fields={fields}&submissionUrl={submissionUrl}&itemKind={itemKind}&itemId={itemId}&mode={mode}&submitType={submitType}&showCancelButton=true",
							{
								itemKind : "type",
								formId : "create",
								itemId : itemType,
								mode : "create",
								submitType : "json",
								submissionUrl : "/becpg/bulkedit/type/" + itemType.replace(":", "_")
										+ "/bulksave?nodeRefs=" + nodeRefs.join(),
								fields : displayFields
							});

			// Using Forms Service, so always create new
			// instance
			var createRow = new Alfresco.module.SimpleDialog(this.id + "-bulkEditRow");

			createRow.setOptions({
				width : "36em",
				templateUrl : templateUrl,
				actionUrl : null,
				destroyOnHide : true,
				doBeforeDialogShow : {
					fn : doBeforeDialogShow,
					scope : this
				},
				onSuccess : {
					fn : function BulkEdit_onNewRow_success(response) {
						if(nodeRefs.length >10){
							 me._updateDataGrid.call(me, {
						         page : me.currentPage
					         });
						} else {
							for (var i = 0, ii = nodeRefs.length; i < ii; i++) {
								YAHOO.Bubbling.fire(me.scopeId + "dataItemUpdated", {
									nodeRef : nodeRefs[i]
								});
							}
						}
						
						Alfresco.util.PopupManager.displayMessage({
							text : this.msg("message.edit-selected.success")
						});
					},
					scope : this
				},
				onFailure : {
					fn : function BulkEdit_onNewRow_failure(response) {
						Alfresco.util.PopupManager.displayMessage({
							text : this.msg("message.edit-selected.failure")
						});
					},
					scope : this
				}
			}).show();

		},
		
		
		_showWusedPopup : function EntityDataGrid___showWusedPopup(popupKind, items, callBack) {
			var showPopup = false, entryCount = 0, entryKey =null;

			var html = '<div class="hd">' + this.msg("header." + popupKind + ".picker") + '</div>';
			html += '<div class="bd">';
			html += '<form  class="form-container">';
			html += '<div class="form-fields '+popupKind+'">';
			html += '   <div class="set">';
			html += '        <div class="form-field">';
			html += '            <select  id="'+this.id+'-wused-selected-picker">';
			html += '                  <option value="">' + this.msg(popupKind + ".picker.choose") + '</option>';
			if(popupKind == "bulk-edit") {
				html += '                  <option value="selectlines">' + this.msg(popupKind + ".picker.selectedlines") + '</option>';
			}
			for ( var key in items[0].itemData) {
				if (key.indexOf("assoc_") > -1 && this.datalistColumns[key].label != "hidden") {
					if(popupKind != "bulk-edit" || key == "assoc_bcpg_compoListProduct"
						|| key == "assoc_bcpg_packagingListProduct" 
						|| key == "assoc_mpm_plResource"){
						showPopup = true;
						html += "<option value='" + key + "'>" + this.datalistColumns[key].label + "</option>";
						entryCount++;
						entryKey = key;
					}
				}
			}
			html += '            </select>';
			html += '          </div>';
			html += '       </div>';
			html += '    </div>';
			html += '<div id="'+this.id+'-'+popupKind+'-ft" class="bdft">';
			html += '</div>';
			html += '</form></div>';
			
		   if (popupKind == "bulk-edit" || (showPopup && this.datalistMeta.name.indexOf("WUsed") != 0)) {
				
			   if(popupKind == "wused" && entryCount==1){
				   callBack.call(this, entryKey, entryKey);
				   return;
			   }
			   
			   var containerDiv = document.createElement("div");
				containerDiv.innerHTML = html;

				this.widgets.wUsedPanel = Alfresco.util.createYUIPanel(containerDiv, {
					draggable : true,
					width : "33em"
				});

				this.widgets.wUsedPanel.show();

				if(showPopup){
					YAHOO.util.Event.on(YAHOO.util.Dom.get(this.id+'-wused-selected-picker'), 'change', function(e) {
						var val = this.value == "selectlines" ? null : this.value;
						callBack.call(this, val, val);
					});
				} else {
					callBack.call(this);
				}
			} else {
				if (this.datalistMeta.name.indexOf("WUsed") == 0) {
					var val = null, val2 = "assoc_bcpg_compoListProduct";
					if (this.datalistMeta.name.indexOf("|") > 0) {
						val = "assoc_" + this.datalistMeta.name.split("|")[1].replace(":", "_");
					} else if (this.datalistMeta.itemType === "bcpg:packagingList") {
						val = "assoc_bcpg_packagingListProduct";
						val2 = val;
					} else if (this.datalistMeta.itemType === "mpm:processList") {
						val = "assoc_mpm_plResource";
						val2 = val;
					} else {
						val = "assoc_bcpg_compoListProduct";
					}
					callBack.call(this, val, val2);
				} else {
					callBack.call(this);
				}
			}
			
		},
		
		
		

		
		onActionShowComments : function EntityDataGrid_onActionShowComments(item) {

			var url = Alfresco.constants.URL_SERVICECONTEXT + "modules/comments/list?nodeRef=" + item.nodeRef + "&activityType=datalist"
					+ (item.siteId ? "&site=" + item.siteId : "")
					+ (this.options.entityNodeRef ? "&entityNodeRef=" + this.options.entityNodeRef : "");

			this._showPanel(url, this.id + "_comments", item.nodeRef,"50em");

		},

		onActionShowWused : function EntityDataGrid_onActionShowWused(p_items) {
			var items = YAHOO.lang.isArray(p_items) ? p_items : [ p_items ];

			function onActionShowWused_redirect(itemAssocName, assocName) {
				var nodeRefs = [];

				for (var i = 0, ii = items.length; i < ii; i++) {
					if (!assocName) {
						nodeRefs.push(items[i].nodeRef);
					} else {
						if (items[i].itemData[itemAssocName].value) {
							nodeRefs.push(items[i].itemData[itemAssocName].value);
						} else {
							for ( var j in items[i].itemData[itemAssocName]) {
								nodeRefs.push(items[i].itemData[itemAssocName][j].value);
							}
						}
					}
				}

				if (!assocName) {
					window.location = Alfresco.constants.URL_PAGECONTEXT + "wused?type=" + items[0].itemType + "&nodeRefs=" + nodeRefs.join();
				} else {
					window.location = Alfresco.constants.URL_PAGECONTEXT + "wused?assocName=" + assocName + "&nodeRefs=" + nodeRefs.join();
				}
			}
			this._showWusedPopup("wused", items, onActionShowWused_redirect);

		},

		onAddLabelingAspect : function EntityDataGrid_onAddLabelingAspect(p_items) {
			var items = YAHOO.lang.isArray(p_items) ? p_items : [ p_items ];

			for (var i = 0, ii = items.length; i < ii; i++) {
				this._manageAspect(items[i].nodeRef, "pack:labelingAspect");
			}
		},
		
		onActionShowLabelingDetails  : function EntityDataGrid_onActionShowLabelingDetails(p_items) {
			var items = YAHOO.lang.isArray(p_items) ? p_items : [ p_items ];

			var formatTree = function onActionShowLabelingDetails_formatTree(tree){
			    var html = '<ul><li >';
			    if(tree.name && tree.name!="root"){
			        if(tree.nodeRef){
        			    html += '<span class="'+tree.cssClass+'">';
        			    
        			    if(tree.cssClass == "ingType"){
        			    	html+= tree.legal+ ':';
        			    } else {
	        			    if(!tree.decl){
	        			        html += this.msg("label.labeling-details.tree.item.nodec",tree.legal, beCPG.util.sigFigs(tree.qte,4), beCPG.util.sigFigs(tree.vol,4));
	        			    } else {
	        			        html += '<a href=' + beCPG.util.entityURL(tree.siteId, tree.nodeRef) + '>' 
	        			        + this.msg("label.labeling-details.tree.item",tree.legal, beCPG.util.sigFigs(tree.qte,4), beCPG.util.sigFigs(tree.vol,4), tree.decl) +'</a>';
	        			    }
        			    }

        			    html += '</span>';
        			    if(tree.cssClass != "ingType" && (tree.legal!=null && tree.legal.toLowerCase()!=tree.name.toLowerCase())){
        			    	html += '<p>&nbsp;&nbsp;<i>('+tree.name+')</i></p>';
        			    }
        			    if(tree.allergens){
	    			         for(var i = 0; i < tree.allergens.length ; i++){
	    			              html+=     '&nbsp;<span class="allergen">';
	    			              html+= tree.allergens[i];
	    			              html+=     '</span>';
	    			          }
        			    }
			        } else {
			            html += '<span class="packagingMaterial">'; 
                        html +=  this.msg("label.labeling-details.tree.item",tree.legal, beCPG.util.sigFigs(tree.vol,4), beCPG.util.sigFigs(tree.qte,4), tree.decl);
                        html += '</span>';
			        }
			    }
			    if(tree.children){    
			        html+= '<ul>';
			            for(var i = 0; i< tree.children.length ; i++){
			                html+=     '<li>';
			                html+= onActionShowLabelingDetails_formatTree.call(this, tree.children[i]);
			                html+=     '</li>';
			            }
			        html+='</ul>';
			    }
			    html+='</li></ul>';
			    return html;
			};	   
			if(items.length>0){
				var noderef = items[0].nodeRef;
				Alfresco.util.Ajax.request({
					method : Alfresco.util.Ajax.GET,
					url : Alfresco.constants.PROXY_URI + "becpg/labeling/showLabelingDetails?nodeRef=" + noderef  ,
					requestContentType: Alfresco.util.Ajax.JSON,
				    requestHeaders: {Accept: 'application/json'},
					successCallback : {
						fn : function(resp) {
					         var stringJson = resp.json; 
							 if (stringJson) {
                                var html = '<div class="hd">' + this.msg("label.labeling-details.title") + '</div>';
                                    html += '<div class="bd labeling-details">';
                                    html+=formatTree.call(this, stringJson); 
                                    html += '</div>';
								
                                 var containerDiv = document.createElement("div");     
                                       containerDiv.className = "detailsDialog";
                                       containerDiv.innerHTML = html;
							
                                this.widgets.labelingDetailsPanel = Alfresco.util.createYUIPanel(containerDiv, {
									draggable : true,
									width : "45em"
								});
								this.widgets.labelingDetailsPanel.show();
							}
						},
						scope : this
					}
				});
			}
		},
		onActionSimulate : function EntityDataGrid_onActionSimulate(p_items) {
			var items = YAHOO.lang.isArray(p_items) ? p_items : [ p_items ], me = this, nodeRefs = "";

			for (var i = 0, ii = items.length; i < ii; i++) {
				if (nodeRefs.length > 0) {
					nodeRefs += ",";
				}
				nodeRefs += items[i].nodeRef;
			}

			Alfresco.util.Ajax.request({
				method : Alfresco.util.Ajax.POST,
				url : Alfresco.constants.PROXY_URI + "becpg/entity/simulation/create?dataListItems=" + nodeRefs
						+ (this.options.entityNodeRef ? "&entityNodeRef=" + this.options.entityNodeRef : ""),
				successCallback : {
					fn : function(resp) {
						if (resp.json) {
							for (var i = 0, ii = items.length; i < ii; i++) {
								YAHOO.Bubbling.fire(me.scopeId + "dataItemUpdated", {
									nodeRef : items[i].nodeRef
								});
							}
						}
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

		},

		_manageAspect : function EntityDataGrid_manageAspect(itemNodeRef, aspect) {
			var itemUrl = itemNodeRef.replace(":/", ""), me = this;

			Alfresco.util.Ajax.request({
				url : Alfresco.constants.PROXY_URI + "/slingshot/doclib/aspects/node/" + itemUrl,
				method : Alfresco.util.Ajax.GET,
				successCallback : {
					fn : function(response) {

						if (response.json) {
							var dataObj = null;
							var msgKey = "delete-aspect";

							if (beCPG.util.contains(response.json.current, "pack:labelingAspect")) {

								dataObj = {
									added : [],
									removed : [ aspect ]
								};

							} else {
								msgKey = "add-aspect";
								dataObj = {
									added : [ aspect ],
									removed : []
								};
							}

							Alfresco.util.PopupManager.displayPrompt({
								title : me.msg("message.confirm." + msgKey + ".title", me.msg("aspect." + aspect.replace(":", "_"))),
								text : me.msg("message.confirm." + msgKey + ".description", me.msg("aspect." + aspect.replace(":", "_"))),
								buttons : [ {
									text : me.msg("button." + msgKey),
									handler : function EntityDataGrid__onActionDelete_delete() {
										this.destroy();
										Alfresco.util.Ajax.request({
											url : Alfresco.constants.PROXY_URI + "slingshot/doclib/action/aspects/node/" + itemUrl,
											method : Alfresco.util.Ajax.POST,
											requestContentType : Alfresco.util.Ajax.JSON,
											dataObj : dataObj,
											successCallback : {
												fn : function() {
													YAHOO.Bubbling.fire(me.scopeId + "dataItemUpdated", {
														nodeRef : itemNodeRef
													});
												},
												scope : this
											},
											successMessage : me.msg("message.success." + msgKey, me.msg("aspect." + aspect.replace(":", "_")))
										});
									}
								}, {
									text : this.msg("button.cancel"),
									handler : function EntityDataGrid__onActionDelete_cancel() {
										this.destroy();
									},
									isDefault : true
								} ]
							});

						}

					},
					scope : this
				}
			});
		},

		_showPanel : function EntityDataGrid__showPanel(url, htmlid, itemNodeRef, vWidth) {

			var me = this;

			Alfresco.util.Ajax.request({
				url : url,
				dataObj : {
					htmlid : htmlid
				},
				successCallback : {
					fn : function(response) {
						// Inject the template from the XHR request into a new
						// DIV
						// element
						var containerDiv = document.createElement("div");
						containerDiv.innerHTML = response.serverResponse.responseText;

						// The panel is created from the HTML returned in the
						// XHR
						// request, not the container
						var panelDiv = Dom.getFirstChild(containerDiv);
						this.widgets.panel = Alfresco.util.createYUIPanel(panelDiv, {
							draggable : true,
							width : vWidth
						});

						if(itemNodeRef!=null){
							this.widgets.panel.subscribe("hide", function() {
								YAHOO.Bubbling.fire(me.scopeId + "dataItemUpdated", {
									nodeRef : itemNodeRef
								});
							});
						}

						this.widgets.panel.show();

					},
					scope : this
				},
				failureMessage : "Could not load dialog template from '" + url + "'.",
				scope : this,
				execScripts : true
			});
		}

	};

	/**
	 * Augment prototype with Common Actions module
	 */
	YAHOO.lang.augmentProto(beCPG.module.EntityDataGridActions, beCPG.module.CustomEntityDataGridActions);

})();
