/*******************************************************************************
 * Copyright (C) 2010-2014 beCPG.
 * 
 * This file is part of beCPG
 * 
 * beCPG is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * beCPG is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
(function() {

	/**
	 * WUsedForm constructor.
	 * 
	 * @param htmlId
	 *            {String} The HTML id of the parent element
	 * @return {beCPG.component.WUsedForm} The new WUsedForm instance
	 * @constructor
	 */
	beCPG.component.WUsedForm = function(htmlId) {

		beCPG.component.WUsedForm.superclass.constructor.call(this, "beCPG.component.WUsedForm", htmlId, [ "button", "container" ]);

		return this;
	};

	/**
	 * Extend from Alfresco.component.Base
	 */
	YAHOO.extend(beCPG.component.WUsedForm, Alfresco.component.Base);

	/**
	 * Augment prototype with main class implementation, ensuring overwrite is
	 * enabled
	 */
	YAHOO.lang.augmentObject(beCPG.component.WUsedForm.prototype, {
		/**
		 * Object container for initialization options
		 * 
		 * @property options
		 * @type object
		 */
		options : {
			type : null,
			itemType : null,
			assocType : null,
			nodeRefs : null,
			searchQuery : null,
			searchTerm : null
		},

		/**
		 * Fired by YUI when parent element is available for scripting.
		 * 
		 * @method onReady
		 */
		onReady : function WUsedForm_onReady() {

			var me = this;

			this.widgets.wusedTypeSelect = Alfresco.util.createYUIButton(this, "wusedTypeSelect-button", this.onWusedTypeSelect, {
				type : "menu",
				menu : "wusedTypeSelect-menu",
				lazyloadmenu : false
			});

			if (this.widgets.wusedTypeSelect == null) {

				this.widgets.operators = Alfresco.util.createYUIButton(this, "operators", function(p_sType, p_aArgs) {

					var menuItem = p_aArgs[1];
					if (menuItem) {
						this.widgets.operators.set("label", menuItem.cfg.getProperty("text"));
						this.widgets.operators.value = menuItem.value;
					}
				}, {
					type : "menu",
					menu : "operators-menu",
					lazyloadmenu : false
				});

				this.widgets.operators.value = me.options.searchQuery ? "OR" : "AND";

				this.widgets.operators.set("label", this.msg("operator." + this.widgets.operators.value.toLowerCase()));

				this.widgets.typeSelect = Alfresco.util.createYUIButton(this, "itemTypeSelect-button", this.onTypeSelect, {
					type : "menu",
					menu : "itemTypeSelect-menu",
					lazyloadmenu : false
				});

				this.widgets.typeSelect.getMenu().subscribe("click", function(p_sType, p_aArgs) {
					var menuItem = p_aArgs[1];
					if (menuItem) {
						me.widgets.typeSelect.set("label", menuItem.cfg.getProperty("text"));
					}
				});

				var dt = Alfresco.util.ComponentManager.find({
					name : "beCPG.module.EntityDataGrid"
				})[0], oldFunc = dt.onDatalistColumns;

				var onShow = function() {

					if (!me.options.searchQuery) {
						dt.options.entityNodeRef = me._getNodeRefs();
						if (dt.options.entityNodeRef == null || dt.options.entityNodeRef.length < 1) {
							return;
						}

						dt.options.extraParams = YAHOO.lang.JSON.stringify({
							operator : me.widgets.operators.value
						});
					} else {
						dt.options.extraParams = YAHOO.lang.JSON.stringify({
							operator : me.widgets.operators.value,
							searchQuery : YAHOO.lang.JSON.parse(me.options.searchQuery),
							searchTerm : me.options.searchTerm
						});
					}

					dt.onDatalistColumns = function(response) {
						var rename = true, columnId = "assoc_" + me.options.assocType.replace(":", "_");
						if (response.json.columns.length < 1) {
							response.json.columns.push({
								"type" : "association",
								"name" : me.options.assocType,
								"formsName" : columnId,
								"label" : me.msg("column.wused"),
								"dataType" : me.options.itemType

							});
							rename = false;
						}

						oldFunc.call(this, response);

						if (rename) {
							YAHOO.Bubbling.fire("columnRenamed", {
								columnId : columnId,
								label : me.msg("column.wused")
							});
						}
					};

					YAHOO.Bubbling.fire("registerDataGridRenderer", {
						propertyName : [ me.options.itemType + "_" + me.options.assocType ],
						renderer : function(oRecord, data, label, scope) {
							var url = beCPG.util.entityCharactURL(data.siteId, data.value);

							return '<span class="' + data.metadata + '" ><a href="' + url + '">' + Alfresco.util.encodeHTML(data.displayValue)
									+ '</a></span>';
						}

					});

					YAHOO.Bubbling.fire("activeDataListChanged", {
						dataList : {
							name : "WUsed-" + me.options.assocType.replace(":", "_"),
							itemType : me.options.itemType
						}
					});

				};

				this.widgets.showButton = Alfresco.util.createYUIButton(this, "show-button", onShow, {
					disabled : true
				});

				if (!me.options.searchQuery) {

					this.widgets.entitiesPicker = new beCPG.component.AutoCompletePicker(this.id + '-entities', this.id + '-entities-field', true)
							.setOptions({
								mode : "edit",
								currentValue : this.options.nodeRefs,
								multipleSelectMode : true,
								dsStr : "/becpg/autocomplete/targetassoc/associations/" + this.options.type
							});
				}

				// select first

				var items = this.widgets.typeSelect.getMenu().getItems();

				if (items && items.length > 0) {
					me.widgets.showButton.set("disabled", false);
				}

				for ( var i in items) {
					var typeSelected = items[i];
					if (typeSelected) {
						me.widgets.typeSelect.set("label", typeSelected.cfg.getProperty("text"));
						var className = typeSelected._oAnchor.children[0].attributes[0].nodeValue;
						this._extractValues(className);
						if (className.indexOf("selected") > -1) {
							onShow();
							break;
						}
					}
				}
			}
		},

		onTypeSelect : function WUsedForm_onItemTypeSelect(sType, aArgs, p_obj) {
			var eventTarget = aArgs[1];

			var className = Alfresco.util.findEventClass(eventTarget);
			this._extractValues(className);
		},

		onWusedTypeSelect : function WUsedForm_onItemTypeSelect(sType, aArgs, p_obj) {
			var eventTarget = aArgs[1];

			var className = Alfresco.util.findEventClass(eventTarget);

			window.location.href = Alfresco.constants.URL_PAGECONTEXT + "wused?type=" + className;

		},

		_extractValues : function WUsedForm__extractValues(className) {
			this.options.itemType = className.split("#")[0];
			this.options.assocType = className.split("#")[1];
		},

		_getNodeRefs : function WUsedForm__getNodeRefs() {
			return this.widgets.entitiesPicker.getValues();
		}

	}, true);

})();
