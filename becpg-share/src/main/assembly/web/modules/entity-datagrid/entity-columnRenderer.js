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
 * Entity Data Grid column renderers
 * 
 * @namespace beCPG
 * @class beCPG.component.EntityDataGridRenderers
 */
(function() {

	/**
	 * YUI Library aliases
	 */
	var Bubbling = YAHOO.Bubbling;

	/**
	 * Alfresco Slingshot aliases
	 */
	var $html = Alfresco.util.encodeHTML, $links = Alfresco.util.activateLinks, $userProfile = Alfresco.util.userProfileLink, $isValueSet = Alfresco.util.isValueSet;

	/**
	 * Entity DataGrid Renderers constructor.
	 * 
	 * @param htmlId
	 *           {String} The HTML id of the parent element
	 * @return {beCPG.component.EntityDataGridRenderers} The new
	 *         EntityDataGridRenderers instance
	 * @constructor
	 */
	beCPG.module.EntityDataGridRenderers = function() {

		// Initialise prototype properties
		this.renderers = {};

		this._setupColumnRenderers();

		// Renderers
		Bubbling.on("registerDataGridRenderer", this.onRegisterRenderer, this);

		return this;
	};

	/**
	 * Augment prototype with main class implementation, ensuring overwrite is
	 * enabled
	 */
	YAHOO.lang.augmentObject(beCPG.module.EntityDataGridRenderers.prototype, {

		/**
		  * Registered metadata renderers. Register new renderers via
		  * registerRenderer() or "registerRenderer" bubbling event
		  * 
		  * @property renderers
		  * @type object
		  */
		renderers: null,

		/**
		  * Register a metadata renderer via Bubbling event
		  * 
		  * @method onRegisterRenderer
		  * @param layer
		  *           {object} Event fired (unused)
		  * @param args
		  *           {array} Event parameters (property name, rendering function)
		  */
		onRegisterRenderer: function EntityDataGridRenderers_onRegisterRenderer(layer, args) {
			var obj = args[1];
			if (obj && $isValueSet(obj.propertyName) && $isValueSet(obj.renderer)) {
				this.registerRenderer(obj.propertyName, obj.renderer);
			} else {
				Alfresco.logger.error("EntityDataGridRenderers_onRegisterRenderer: Custom renderer registion invalid: "
					+ obj);
			}
		},

		/**
		  * Register a metadata renderer
		  * 
		  * @method registerRenderer
		  * @param propertyName
		  *           {string} Property name to attach this renderer to
		  * @param renderer
		  *           {function} Rendering function
		  * @return {boolean} Success status of registration
		  */
		registerRenderer: function EntityDataGridRenderers_registerRenderer(propertyName, renderer) {
			if ($isValueSet(propertyName) && $isValueSet(renderer)) {
				if (propertyName instanceof Array) {
					for (var i in propertyName) {
						this.renderers[propertyName[i]] = renderer;
					}

				} else {
					this.renderers[propertyName] = renderer;
				}
				return true;
			}
			return false;
		},

		/**
		  * Configure standard column renderers
		  * 
		  * @method _setupMetadataRenderers
		  */
		_setupColumnRenderers: function EntityDataGridRenderers__setupMetadataRenderers() {


			/**
			  * Person
			  */
			this.registerRenderer("cm:person", function(oRecord, data, label, scope) {
				return '<span class="person">' + $userProfile(data.metadata, data.displayValue) + '</span>';
			});

			/**
			  * Boolean
			  */
			this.registerRenderer("boolean", function(oRecord, data, label, scope) {

				if (typeof data.value === 'undefined' || data.value === null || data.value === "") {
					return "";
				}

				var booleanValueTrue = scope.msg("data.boolean.true");
				var booleanValueFalse = scope.msg("data.boolean.false");
				return $html(data.value === true || data.value === "true" ? booleanValueTrue : booleanValueFalse);
			});

			/**
			  * Authority container
			  */
			this.registerRenderer("cm:authoritycontainer", function(oRecord, data, label, scope) {
				return '<span class="userGroup">' + $html(data.displayValue) + '</span>';
			});

			/**
			  * Content
			  */
			this.registerRenderer(["cm:content", "cm:cmobject", "cm:folder"], function(oRecord, data, label, scope) {
				var url = scope._buildCellUrl(data);
				var html = '<a href="' + url + '">';
				html += '<img src="'
					+ Alfresco.constants.URL_RESCONTEXT
					+ 'components/images/filetypes/'
					+ Alfresco.util.getFileIcon(data.displayValue, (data.metadata == "container" ? 'cm:folder' : null),
						16) + '" width="16" alt="' + $html(data.displayValue) + '" title="' + $html(data.displayValue)
					+ '" />';
				html += ' ' + $html(data.displayValue) + '</a>';
				return html;
			});

			this.registerRenderer(["content_cm:content"], function(oRecord, data, label, scope, z, zz, elCell, oColumn) {
				var nodeRef = new Alfresco.util.NodeRef(oRecord.getData("nodeRef"));

				oColumn.width = 100;

				YAHOO.util.Dom.setStyle(elCell, "width", oColumn.width + "px");
				YAHOO.util.Dom.setStyle(elCell.parentNode, "width", oColumn.width + "px");


				return '<span class="thumbnail"><img src="' + Alfresco.constants.PROXY_URI + 'api/node/' + nodeRef.uri
					+ '/content/thumbnails/doclib?c=queue&ph=true&timestamp=' + (new Date()).getTime() + '"  /></span>';

			});

			this.registerRenderer(
			    ["bcpg:httpLink1", "bcpg:httpLink2", "bcpg:httpLink3", "bcpg:httpLink4", "bcpg:httpLink5"],
			    function(oRecord, data, label, scope, z, zz, elCell, oColumn) {
			        const regex = /"([^"]+)":(https?:\/\/[^\s]+)|(?:https?:\/\/[^\s]+)/g;

			        // Check if data.value exists and is not empty
			        if (typeof data.value === 'undefined' || data.value === null || data.value === "") {
			            return "";
			        }

			        var match = regex.exec(data.value);
			        var outputHTML = "";

			        if (match) {
			            var anchorText = "";
			            var href = "";

			            if (match[1] && match[2]) {
			                // If it's a markdown-style link
			                anchorText = match[1];  // Label
			                href = match[2];        // URL
			            } else {
			                // For a simple URL match
			                anchorText = match[0];
			                href = match[0];
			            }
			            ret = '<span class="link"><a href="'+href+'" target="_blank" rel="noopener noreferrer">'+anchorText+' </a></span>';
			        }

			        return ret;
			    }
			);



		},

		/**
		  * Return data type-specific formatter
		  * 
		  * @method getCellFormatter
		  * @return {function} Function to render read-only value
		  */
		getCellFormatter: function EntityDataGridRenderers_getCellFormatter(datagrid) {
			var scope = this;
			/**
			  * Data Type custom formatter beCPG 
			  * beCPG objects
			  * 
			  * @method renderCellDataType
			  * @param elCell
			  *           {object}
			  * @param oRecord
			  *           {object}
			  * @param oColumn
			  *           {object}
			  * @param oData
			  *           {object|string}
			  */
			return function EntityDataGridRenderers_renderCellDataType(elCell, oRecord, oColumn, oData) {
				var html = "";

				// Populate potentially missing parameters
				if (!oRecord) {
					oRecord = this.getRecord(elCell);
				}
				if (!oColumn) {
					oColumn = this.getColumn(elCell.parentNode.cellIndex);
				}



				if (oRecord && oColumn) {

					var columnKey = oColumn.key;
					var nestedColumnKey = null;
					if (columnKey != null && columnKey.indexOf("nested_") == 0) {
						columnKey = oColumn.key.split("|")[0].replace("nested_", "");
						nestedColumnKey = oColumn.key.split("|")[1];
					}

					if (!oData && oRecord.getData("itemData")) {
						oData = oRecord.getData("itemData")[columnKey];

						if (nestedColumnKey != null && oData) {
							if (YAHOO.lang.isArray(oData) && oData[0].itemData) {
								oData = oData[0].itemData[nestedColumnKey];
							} else if (oData.itemData) {
								oData = oData.itemData[nestedColumnKey];
							}
						}
					}

					if (oData) {

						var datalistColumn = datagrid.datalistColumns[columnKey];
						if (nestedColumnKey != null && datalistColumn) {
							for (var j = 0; j < datalistColumn.columns.length; j++) {
								var nestedColumn = datalistColumn.columns[j];
								if (((nestedColumn.type == "property" ? "prop"
									: "assoc") + "_" + nestedColumn.name.replace(":", "_")) == nestedColumnKey) {
									datalistColumn = nestedColumn;
									break;
								}
							}
						}

						if (datalistColumn) {
							var isArray = YAHOO.lang.isArray(oData);
							oData = isArray ? oData : [oData];


							if (isArray) {
								html += "<ul>";
							}

							for (var i = 0, ii = oData.length, data; i < ii; i++) {
								data = oData[i];


								var p_type = datalistColumn.dataType, p_label = datalistColumn.name;

								var ret = "";
								try {
									if (scope.renderers.hasOwnProperty(p_type + "_" + p_label)
										&& typeof scope.renderers[p_type + "_" + p_label] === "function") {
										ret = scope.renderers[p_type + "_" + p_label].call(scope, oRecord, data, p_label,
											datagrid, i, ii, elCell, oColumn);
									} else if (scope.renderers.hasOwnProperty(p_label)
										&& typeof scope.renderers[p_label] === "function") {
										ret = scope.renderers[p_label].call(scope, oRecord, data, p_label, datagrid, i, ii, elCell, oColumn);
									} else if (scope.renderers.hasOwnProperty(p_type)
										&& typeof scope.renderers[p_type] === "function") {
										ret = scope.renderers[p_type].call(scope, oRecord, data, p_label, datagrid, i, ii, elCell, oColumn);
									} else {

										if (oColumn.numberFormat && data.value != null) {
											ret = beCPG.util.formatNumber(oColumn.numberFormat, data.value);
										} else {
											if (oColumn.tooltip) {
												ret = beCPG.util.createTextTooltip(data.displayValue, oColumn.tooltip);
											} else {
												ret = $links($html(data.displayValue));
											}
										}
									}
								} catch (e) {
									console.log("Error in column renderer:" + p_type + " - " + p_label + " error " + e);
								}

								if (isArray) {
									if (ret != null && ret.length > 0) {
										html += "<li>" + ret + "</li>";
									}
								} else {
									html += ret;
								}

							}

							if (isArray) {
								html += "</ul>";
							}

						}
					}
				}

				elCell.innerHTML = html;
			};
		},

		/**
		 * Return data type-specific formatter
		 * 
		 * @method getCellFormatter
		 * @return {function} Function to render read-only value
		 */
		getCellEditor: function EntityDataGridRenderers__getCellEditor(scope, column, saveFieldUrl) {
			var editor = null, regexp = null;

			// Test permission
			if ((column.protectedField != null && column.protectedField) || (column.readOnly != null && column.readOnly)) {
				return null;
			}

			var repeating = column.repeating ? true : false;

			if (column.constraints != null) {
				for (var i in column.constraints) {

					switch (column.constraints[i].type) {
						case "REGEXP":
							regexp = column.constraints[i].parameters.expressions;
							break;
						case "LIST":
							var dropdownOptions = [];
							for (var j in column.constraints[i].parameters.allowedValues) {
								var val = column.constraints[i].parameters.allowedValues[j];
								if ((typeof val === 'string' || val instanceof String) && val.indexOf && val.indexOf("|") > -1) {
									val = val.split("|");
									if (val.length > 1) {
										dropdownOptions.push({ label: val[1], value: val[0] });
									} else {
										dropdownOptions.push(val[0]);
									}
								} else {
									dropdownOptions.push(val);
								}
							}
							if (column.repeating) {
								editor = new YAHOO.widget.CheckboxCellEditor({
									checkboxOptions: dropdownOptions,
									disableBtns: true
								});
							} else {
								editor = new YAHOO.widget.DropdownCellEditor({
									dropdownOptions: dropdownOptions,
									disableBtns: !(YAHOO.env.ua.ie > 0)
								});
							}
							break;
					}
				}

			}

			var dataType = column.dataType;
			if (dataType == null) {
				dataType = column.endpointType;
			}

			if (editor == null) {

				switch (dataType.toLowerCase()) {
					case "datetime":
					case "date":
						editor = new YAHOO.widget.DateCellEditor();
						editor.renderForm = function() {
							// Calendar widget
							if (YAHOO.widget.Calendar) {
								var calContainer = this.getContainerEl().appendChild(document.createElement("div"));
								calContainer.id = this.getId() + "-dateContainer"; // Needed for Calendar constructor
								var calendar =
									new YAHOO.widget.Calendar(this.getId() + "-date",
										calContainer.id, this.calendarOptions);

								Alfresco.util.calI18nParams(calendar);

								calendar.render();
								calContainer.style.cssFloat = "none";

								// Bug 2528576
								calendar.hideEvent.subscribe(function() { this.cancel(); }, this, true);

								if (YAHOO.env.ua.ie) {
									var calFloatClearer = this.getContainerEl().appendChild(document.createElement("div"));
									calFloatClearer.style.clear = "both";
								}

								this.calendar = calendar;

								if (this.disableBtns) {
									this.handleDisabledBtns();
								}

							}
							else {
								YAHOO.log("Could not find YUI Calendar", "error", this.toString());
							}

						};

						break;
					case "boolean":
						var booleanOptions = [{
							label: scope.msg("data.boolean.true"),
							value: true
						}, {
							label: scope.msg("data.boolean.false"),
							value: false
						}];
						if (!column.mandatory) {
							booleanOptions.push({
								label: scope.msg("data.boolean.empty"),
								value: ""
							});
						}
						editor = new YAHOO.widget.RadioCellEditor({
							radioOptions: booleanOptions,
							validator: function(oData) {
								if (!oData || oData.length < 1) {
									if (column.mandatory) {
										return undefined;
									}
									return null;
								}
								return oData;
							},
							disableBtns: true
						});
						editor.resetForm = function() {
							for (var i = 0, j = this.radios.length; i < j; i++) {
								var elRadio = this.radios[i];
								if (this.value === elRadio.value || this.value == null && elRadio.value == "") {
									elRadio.checked = true;
									return;
								}
							}
						};

						break;
					case "float":
					case "int":
					case "long":
					case "double":
						editor = new YAHOO.widget.TextboxCellEditor({
							validator: function(oData) {
								if (!oData || oData.length < 1) {
									if (column.mandatory) {
										return undefined;
									}
									return null;
								}

								//Convert to number
								try {
									var number = eval(oData.replace(/,/g, '.').replace(/[^-()\d/*+.eE]/g, ''));
								} catch (e) {
									return undefined;
								}

								// Validate
								if (YAHOO.lang.isNumber(number)) {
									return number;
								}

								return undefined;

							},
							disableBtns: true
						});
						break;
					case "text":
					case "mltext":
						if (regexp == null) {
							editor = new YAHOO.widget.TextareaCellEditor({ disableBtns: false });
							editor.focus = function() {
								this.getDataTable()._focusEl(this.textarea);
								//  this.textarea.select();                    	   
							};
						} else {
							editor = new YAHOO.widget.TextboxCellEditor({
								validator: function(oData) {
									if (oData.match(regexp)) {
										return oData;
									}
									return undefined;

								},
								disableBtns: true
							});

						}
						break;
					case "any":
						editor = new YAHOO.widget.TextboxCellEditor({
							validator: function(oData) {
								if (!oData || oData.length < 1) {
									if (column.mandatory) {
										return undefined;
									}
									return null;
								}

								//Convert to number
								try {
									if (!oData.match(/[a-zA-Z_]/)) {
										var number = eval(oData.replace(/,/g, '.').replace(/[^-()\d/*+.]/g, ''));

										// Validate
										if (YAHOO.lang.isNumber(number)) {
											return number;
										}
									}
								} catch (e) {
									return oData;
								}

								return oData;

							},
							disableBtns: true
						});
						break;

					default:
						return null;
				}

			}
			// Overide attach method
			/**
			 * Attach CellEditor for a new interaction.
			 * 
			 * @method attach
			 * @param oDataTable
			 *            {YAHOO.widget.DataTable} Associated
			 *            DataTable instance.
			 * @param elCell
			 *            {HTMLElement} Cell to edit.
			 */
			editor.attach = function(oDataTable, elCell) {
				var oColumn, oRecord, oData = null;

				// Validate
				if (oDataTable instanceof YAHOO.widget.DataTable) {

					editor._oDataTable = oDataTable;

					// Validate cell
					elCell = oDataTable.getTdEl(elCell);
					if (elCell) {
						editor._elTd = elCell;

						// Validate Column
						oColumn = oDataTable.getColumn(elCell);
						if (oColumn) {
							editor._oColumn = oColumn;

							// Validate Record
							oRecord = oDataTable.getRecord(elCell);
							editPermission = oRecord.getData().permissions.userAccess.edit;

							if (oRecord && editPermission) {
								editor._oRecord = oRecord;
								if (oData == null) {
									oData = oRecord.getData("itemData")[editor.getColumn().getField()];
								}
								var value = undefined;

								if (oRecord.getData(this.getColumn().getField()) != null) {
									oData = oRecord.getData(this.getColumn().getField());
								}
								oData = YAHOO.lang.isArray(oData) ? oData : [oData];
								if (oData[0]) {
									switch (dataType.toLowerCase()) {
										case "datetime":
											value = Alfresco.util.fromISO8601(oData[0].value);
											break;
										case "date":
											value = Alfresco.util.fromISO8601(oData[0].value);
											break;
										case "boolean":
											value = "" + oData[0].value;
											break;
										default:
											value = oData[0].value;
											break;
									}

									if (repeating && value != null && value.constructor !== Array) {
										value = value.split(",");
									}
								}
								editor.value = (value !== undefined) ? value : editor.defaultValue;
								return true;
							}
						}
					}
				}
			};

			/**
			 * Saves value of CellEditor and hides UI.
			 * 
			 * @method save
			 */
			editor.save = function() {
				// Get new value
				var inputValue = this.getInputValue(), validValue = inputValue, oSelf = this, tmp = "";

				// Validate new value
				if (this.validator) {
					validValue = this.validator.call(this.getDataTable(), inputValue, this.value, this);
					if (validValue === undefined) {
						if (this.resetInvalidData) {
							this.resetForm();
						}
						this.fireEvent("invalidDataEvent", {
							editor: this,
							oldData: this.value,
							newData: inputValue
						});
						return;
					}
				}

				if (oSelf instanceof YAHOO.widget.CheckboxCellEditor) {
					for (var i in validValue) {
						if (tmp.length > 0) {
							tmp += ",";
						}
						tmp += validValue[i];
					}
					validValue = tmp;
				}

				var finishSave = function(bSuccess, oNewValue) {
					var oOrigValue = oSelf.value, oDisplayValue;
					if (bSuccess) {
						// Update new value
						oDisplayValue = oNewValue;
						oSelf.value = oNewValue;

						if (oSelf instanceof YAHOO.widget.DateCellEditor) {
							oDisplayValue = Alfresco.util.formatDate(oNewValue, scope
								.msg("date-format.defaultDateOnly"));
							oNewValue = Alfresco.util.formatDate(oNewValue, "yyyy-mm-dd'T'HH:MM:ss");
						} else if (oSelf instanceof YAHOO.widget.RadioCellEditor) {

							oDisplayValue = "true" === oNewValue ? scope.msg("data.boolean.true") : ("false" === oNewValue ? scope.msg("data.boolean.false") : "");

						} else if (oSelf instanceof YAHOO.widget.DropdownCellEditor) {

							var allOptions = oSelf.dropdown.options;
							oDisplayValue = allOptions[allOptions.selectedIndex].label;

						} else if (oSelf instanceof YAHOO.widget.CheckboxCellEditor) {
							var values = oDisplayValue.split(",");
							var tmpDisplayValue = "";
							for (var val in values) {
								var allOptions = oSelf.checkboxOptions;
								for (var option in allOptions) {
									if (allOptions[option].value == values[val]) {
										if (tmpDisplayValue.length > 0) {
											tmpDisplayValue += ",";
										}
										tmpDisplayValue += allOptions[option].label;
									}
								}
							}
							oDisplayValue = tmpDisplayValue;
						}


						oSelf.getDataTable().updateCell(oSelf.getRecord(), oSelf.getColumn(), {
							value: oNewValue,
							displayValue: oDisplayValue
						});


						// Hide CellEditor
						oSelf._hide();

						Bubbling.fire("dirtyDataTable", { "record": oSelf.getRecord(), "column": oSelf.getColumn() });

						oSelf.fireEvent("saveEvent", {
							editor: oSelf,
							oldData: oOrigValue,
							newData: oSelf.value
						});


					} else {
						oSelf.resetForm();
						oSelf.fireEvent("revertEvent", {
							editor: oSelf,
							oldData: oOrigValue,
							newData: oNewValue
						});
					}



					oSelf.unblock();
				};

				this.block();

				beCPG.util.incLockCount();

				var record = this.getRecord(), curCol = this.getColumn(), nodeRef = record.getData("nodeRef"), field = curCol.getField();

				if (record.getData(field) != null && record.getData(field).itemNodeRef != null) {
					nodeRef = record.getData(field).itemNodeRef;
					field = column.fieldRef;
				}

				switch (dataType.toLowerCase()) {
					case "datetime":
					case "date":
						validValue = Alfresco.util.formatDate(validValue, "yyyy-mm-dd");
						break;
					default:
						break;
				}

				Alfresco.util.Ajax.jsonPost({
					url: saveFieldUrl,
					dataObj: {
						value: validValue,
						field: field,
						isMultiple: repeating,
						nodeRef: nodeRef
					},
					successCallback: {
						fn: function(response) {
							finishSave(true, validValue);
						},
						scope: scope
					},
					failureCallback: {
						fn: function(response) {
							Alfresco.util.PopupManager.displayMessage({
								text: scope.msg("message.details.failure")
							});
							finishSave(false);
						},
						scope: scope
					}
				});

			};
			/**
			 * Hides CellEditor UI at end of interaction.
			 * 
			 * @method _hide
			 */
			editor._hide = function() {

				this.getContainerEl().style.display = "none";
				if (this._elIFrame) {
					this._elIFrame.style.display = "none";
				}
				this.isActive = false;
				this.getDataTable()._oCellEditor = null;

				this.getDataTable().focus();
			};

			editor.LABEL_CANCEL = scope.msg("button.cancel");
			editor.LABEL_SAVE = scope.msg("button.save");

			return editor;
		},


		/**
		  * Return data type-specific sorter
		  * 
		  * @method getSortFunction
		  * @return {function} Function to sort column by
		  */
		getSortFunction: function EntityDataGridRenderers_getSortFunction() {
			/**
			  * Data Type custom sorter
			  * 
			  * @method sortFunction
			  * @param a
			  *           {object} Sort record a
			  * @param b
			  *           {object} Sort record b
			  * @param desc
			  *           {boolean} Ascending/descending flag
			  * @param field
			  *           {String} Field to sort by
			  */
			return function EntityDataGridRenderers_sortFunction(a, b, desc, field) {
				var fieldA = a.getData().itemData[field], fieldB = b.getData().itemData[field];

				if (YAHOO.lang.isArray(fieldA)) {
					fieldA = fieldA[0];
				}
				if (YAHOO.lang.isArray(fieldB)) {
					fieldB = fieldB[0];
				}

				// Deal with empty values
				if (!YAHOO.lang.isValue(fieldA)) {
					return (!YAHOO.lang.isValue(fieldB)) ? 0 : 1;
				} else if (!YAHOO.lang.isValue(fieldB)) {
					return -1;
				}

				var valA = fieldA.value, valB = fieldB.value;

				if (valA && valA.indexOf && valA.indexOf("workspace://SpacesStore") == 0) {
					valA = fieldA.displayValue;
					valB = fieldB.displayValue;
				}

				return YAHOO.util.Sort.compare(valA, valB, desc);
			};
		}

	});

	beCPG.module.EntityDataRendererHelper = new beCPG.module.EntityDataGridRenderers();

})();
