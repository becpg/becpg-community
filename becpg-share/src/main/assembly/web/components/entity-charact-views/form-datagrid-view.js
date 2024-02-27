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
 * Document and Folder header component.
 * 
 * @namespace beCPG
 * @class beCPG.component.FormDataGrid
 */
(function() {
	/**
	 * YUI Library aliases
	 */
	var Dom = YAHOO.util.Dom;

	/**
	 * Properties constructor.
	 * 
	 * @param {String}
	 *            htmlId The HTML id of the parent element
	 * @return {beCPG.component.FormDataGrid} The new Properties instance
	 * @constructor
	 */
	beCPG.component.FormDataGrid = function FormDataGrid_constructor(htmlId) {

		beCPG.component.FormDataGrid.superclass.constructor.call(this, "beCPG.component.FormDataGrid", htmlId, ["button", "container"]);


		YAHOO.Bubbling.on("activeDataListChanged", this.onActiveDataListChanged, this);
		YAHOO.Bubbling.on("refreshDataGrids", this.loadForm, this);


		YAHOO.Bubbling.on("versionChangeFilter", this.onVersionChanged, this);

		return this;
	};

	YAHOO
		.extend(
			beCPG.component.FormDataGrid,
			Alfresco.component.Base,
			{
				options: {
					/**
					 * The nodeRefs to load the form for.
					 *
					 * @property nodeRef
					 * @type string
					 * @required
					 */
					nodeRef: null,
					
					/**
					 * Current version nodeRef
					 */
					currVersionNodeRef : null,

					/**
					 * The form id for the form to use.
					 *
					 * @property destination
					 * @type string
					 */
					formId: null
				},

				/**
				 * Fired by YUI when parent element is available for
				 * scripting. Initial History Manager event registration
				 * 
				 * @method onReady
				 */
				onReady: function FormDataGrid_onReady() {

					Alfresco.util.createYUIButton(this, "edit-datagrid-form", function(p_sType, p_aArgs) {

						var editProductMetadata = new Alfresco.module.SimpleDialog(this.id + "-editDataGridForm");

						editProductMetadata.setOptions(
							{
								width: "33em",
								onSuccess: {
									fn: function() {
										var formulateButton = YAHOO.util.Selector.query('div.formulate button');
										if (formulateButton != null && formulateButton.length > 0) {
											formulateButton[0].click();
										} else {
											this.loadForm();
										}
									},
									scope: this
								},
								templateUrl: YAHOO.lang.substitute(
									Alfresco.constants.URL_SERVICECONTEXT
									+ "components/form?popup=true&formId={formId}&itemKind=node&itemId={itemId}&mode=edit&submitType=json&showCancelButton=true",
									{
										itemId: this.options.nodeRef,
										formId: this.options.formId
									}),
								destroyOnHide: true,
								doBeforeDialogShow: {
									fn: function(p_form, p_dialog) {
										Alfresco.util.populateHTML([p_dialog.id + "-dialogTitle",
										this.msg("label.form-metadata.title")]);
									},
									scope: this
								}

							}).show();

					});

					this.loadForm();
				},

				loadForm: function FormDataGrid_onReady() {
					// Load the form
					Alfresco.util.Ajax.request(
						{
							url: Alfresco.constants.URL_SERVICECONTEXT + "components/form",
							dataObj:
							{
								htmlid: this.id + "-formContainer",
								itemKind: "node",
								itemId: this.options.nodeRef,
								formId: this.options.formId,
								forceFormId: "true",
								mode: "view"
							},
							successCallback:
							{
								fn: this.onFormLoaded,
								scope: this
							},
							failureCallback:
							{
								fn: this.onFormFailed,
								scope: this
							},
							scope: this,
							execScripts: true
						});

				},
				onFormLoaded: function(response) {
					var formEl = Dom.get(this.id + "-formContainer");
					formEl.innerHTML = response.serverResponse.responseText;
					Dom.removeClass(this.id + "-formDatagridView", "hidden")
					Dom.setStyle(this.id + "-formDatagridView", "width", "24%");
					Dom.setStyle(this.id + "-formDatagridView", "float", "left");
					Dom.setStyle(this.id + "-body", "width", "74.2%");
					Dom.setStyle(this.id + "-body", "float", "left");
				},
				onFormFailed: function(response) {
					var formEl = Dom.get(this.id + "-formContainer");
					formEl.innerHTML = response.serverResponse.responseText;
					Dom.addClass(this.id + "-formDatagridView", "hidden")
					Dom.setStyle(this.id + "-body", "width", "100%");
				},
				onActiveDataListChanged: function FormDataGrid_onActiveDataListChanged(layer, args) {
					var obj = args[1];

					if ((obj !== null)) {
						var prevId = this.options.formId;
						if (obj.dataList) {
							if (this.datalistMeta != null && this.datalistMeta.name != null) {

								this.options.formId = datalistMeta.name;
							}
						}

						if (obj.list != null && (this.options.list == null || this.options.list.length < 1)) {
							this.options.formId = obj.list;
						}
						if (prevId != this.options.formId) {
							this.loadForm();
						}
					}

				},

				onVersionChanged: function FormDataGrid_onVersionChanged(layer, args) {
                   	var obj = args[1];
					if ((obj !== null) && obj.filterId !== null && obj.filterId === "version" && obj.filterData !== null) {
						if(this.options.currVersionNodeRef == null) {
							this.options.currVersionNodeRef = this.options.nodeRef;
						}
						this.options.nodeRef = obj.filterData;
					} else if (this.options.currVersionNodeRef != null) {
						this.options.nodeRef = this.options.currVersionNodeRef;
					}
					this.loadForm();
				}


			});
})();
