/*******************************************************************************
 * Copyright (C) 2010-2020 beCPG.
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


	if (beCPG.component.EntityDataListToolbar) {
		var PREF_VIEW_MODE = "org.alfresco.share.project.gantt.mode"

		YAHOO.Bubbling.fire("registerToolbarButtonAction",
			{
				actionName: "toggle-gantt",
				right: true,
				evaluate: function(asset, entity) {
					return asset.name !== null && (asset.name.indexOf("View-gantt") > -1 || asset.name === "taskList");
				},
				createWidget: function(containerDiv, instance) {

					var divEl = document.createElement("div");

					containerDiv.appendChild(divEl);

					Dom.addClass(divEl, "ganttCkeckbox");

					var ganttViewOn = "dataTable" != Alfresco.util.findValueByDotNotation(instance.services.preferences.get(), PREF_VIEW_MODE);

					var widget = new YAHOO.widget.Button(
						{
							type: "checkbox",
							title: instance.msg("button.toggle-gantt.description"),
							container: divEl,
							checked: ganttViewOn
						});

					widget.on("checkedChange", function() {
						YAHOO.Bubbling.fire("viewModeChange");

					});

					return widget;
				}
			});

		YAHOO.Bubbling.fire("registerToolbarButtonAction",
			{
				actionName: "full-screen",
				evaluate: function(asset, entity) {
					return asset.name !== null && (asset.name.indexOf("View-gantt") > -1 || asset.name === "taskList");
				},
				fn: function(instance) {

					if (Dom.hasClass("alf-hd", "hidden")) {
						Dom.removeClass("alf-hd", "hidden");
						Dom.removeClass("alf-filters", "hidden");
						Dom.removeClass("alf-ft", "hidden");
						Dom.removeClass("Share", "full-screen");
						Dom.addClass("alf-content", "yui-b");
						Dom.setStyle("alf-content", "margin-left", "200px");

					}
					else {
						Dom.addClass("alf-hd", "hidden");
						Dom.addClass("alf-ft", "hidden");
						Dom.addClass("Share", "full-screen");
						Dom.addClass("alf-ft", "hidden");
						Dom.addClass("alf-filters", "hidden");
						Dom.removeClass("alf-content", "yui-b");
						Dom.setStyle("alf-content", "margin-left", null);

					}

				}
			});

		YAHOO.Bubbling.fire("registerToolbarButtonAction",
			{
				actionName: "print",
				evaluate: function(asset, entity) {
					return asset.name !== null && (asset.name.indexOf("View-gantt") > -1 || asset.name === "taskList");
				},
				fn: function(instance) {

					var styleSheets = document.getElementsByTagName("link");
					for (var i in styleSheets) {
						var sheet = styleSheets[i];
						if(sheet.rel == "stylesheet"){
							sheet.media = "all";
						}
					}

					Dom.addClass("alf-hd", "hidden");
					Dom.addClass("alf-ft", "hidden");
					Dom.addClass("Share", "full-screen");
					Dom.addClass("alf-ft", "hidden");
					Dom.addClass("alf-filters", "hidden");
					Dom.addClass(this.id, "hidden");
					Dom.removeClass("alf-content", "yui-b");
					Dom.setStyle("alf-content", "margin-left", null);

					print();

					Dom.removeClass("alf-hd", "hidden");
					Dom.removeClass("alf-ft", "hidden");
					Dom.removeClass("Share", "full-screen");
					Dom.removeClass("alf-ft", "hidden");
					Dom.removeClass("alf-filters", "hidden");
					Dom.removeClass(this.id, "hidden");
					Dom.addClass("alf-content", "yui-b");
					Dom.setStyle("alf-content", "margin-left", "200px");
				}
			});


		YAHOO.Bubbling.fire("registerToolbarButtonAction", {
			actionName: "sub-project",
			right: false,
			evaluate: function(asset, entity) {
				return asset.name != null && (asset.name === "taskList") && entity != null && entity.userAccess.edit;
			},
			fn: function(inst) {
				var instance = this;

				var templateUrl = YAHOO.lang
					.substitute(
						Alfresco.constants.URL_SERVICECONTEXT + "components/form?formId=formulation&itemKind=type&itemId={itemId}&destination={destination}&mode=create&submitType=json&showCancelButton=true&popup=true&siteId={siteId}",
						{
							itemId: "pjt:project",
							destination: instance.entity.parentNodeRef,
							siteId: this.options.siteId != null ? this.options.siteId : ""
						});

				var createRow = new Alfresco.module.SimpleDialog(instance.id + "-createSubProject");

				createRow.setOptions(
					{
						width: "34em",
						templateUrl: templateUrl,
						actionUrl: null,
						destroyOnHide: true,
						doBeforeDialogShow: {
							fn: function(p_form, p_dialog) {
								Alfresco.util.populateHTML([p_dialog.id + "-dialogTitle",
								instance.msg("action.sub-project.create.title")]);
							},
							scope: this
						},
						doBeforeFormSubmit: {
							fn: function(form) {
								Alfresco.util.PopupManager.displayMessage({
									text: this.msg("message.sub-project.create.please-wait")
								});
							},
							scope: this
						},
						onSuccess: {
							fn: function(response) {
								if (response.json) {
									Alfresco.util.Ajax.jsonRequest({
										method: Alfresco.util.Ajax.POST,
										url: Alfresco.constants.PROXY_URI + "api/type/pjt:taskList/formprocessor",
										dataObj: {
											"alf_destination": (instance.datalistMeta.nodeRef != null ? instance.datalistMeta.nodeRef : instance.options.parentNodeRef),
											"assoc_pjt_subProjectRef_added": response.json.persistedObject,
											"prop_pjt_tlTaskName": "Sub project"
										},
										successCallback: {
											fn: function(resp) {
												if (resp.json) {
													YAHOO.Bubbling.fire("dataItemCreated", {
														nodeRef: resp.json.persistedObject,
														callback: function(item) {
															Alfresco.util.PopupManager.displayMessage({
																text: instance.msg("message.sub-project.create.success")
															});
														}
													});
												}
											},
											scope: this
										}
									});
								}
							},
							scope: this
						}
					}).show();
			}

		});



		YAHOO.Bubbling
			.fire(
				"registerToolbarButtonAction",
				{
					actionName: "project-metadata",
					hideLabel: true,
					evaluate: function(asset, entity) {
						return asset.name != null && (asset.name === "taskList") && entity != null && entity.userAccess.edit;
					},
					fn: function(instance) {

						var templateUrl = YAHOO.lang
							.substitute(
								Alfresco.constants.URL_SERVICECONTEXT + "components/form?popup=true&formId=project-metadata&itemKind=node&itemId={itemId}&mode=edit&submitType=json&showCancelButton=true&siteId={siteId}",
								{
									itemId: this.options.entityNodeRef,
									siteId: this.options.siteId != null ? this.options.siteId : ""
								});

						var editProductMetadata = new Alfresco.module.SimpleDialog(this.id + "-editProjectMetadata");

						editProductMetadata.setOptions(
							{
								width: "33em",
								onSuccess: {
									fn: function() {
										YAHOO.Bubbling.fire("metadataRefresh");
										YAHOO.Bubbling.fire("dirtyDataTable");
										Alfresco.util.PopupManager.displayMessage(
											{
												text: this.msg("message.details.success")
											});
									},
									scope: this
								},
								failureMessage: this.msg("message.details.failure"),
								templateUrl: templateUrl,
								destroyOnHide: true,
								doBeforeDialogShow: {
									fn: function(p_form, p_dialog) {
										Alfresco.util.populateHTML([p_dialog.id + "-dialogTitle",
										this.msg("label.project-metadata.title")]);
									},
									scope: this
								}

							}).show();

					}
				});


		YAHOO.Bubbling
			.fire(
				"registerToolbarButtonAction",
				{
					actionName: "show-critical-path",
					hideLabel: true,
					evaluate: function(asset, entity) {
						return asset.name != null && (asset.name === "taskList") && entity != null && entity.userAccess.edit;
					},
					createWidget: function(containerDiv, instance) {

						var divEl = document.createElement("div");

						containerDiv.appendChild(divEl);

						Dom.addClass(divEl, "criticalPathCkeckbox");

						var widget = new YAHOO.widget.Button(
							{
								type: "checkbox",
								title: instance.msg("button.toggle-critical-path.description"),
								container: divEl,
								checked: false
							});

						widget.on("checkedChange", function() {
							var el = YAHOO.util.Dom.get("alf-content");

							if (YAHOO.util.Dom.hasClass(el, "show-critical")) {
								YAHOO.util.Dom.removeClass(el, "show-critical");
							} else {
								YAHOO.util.Dom.addClass(el, "show-critical");
							}

						});

						return widget;
					}
				});

		YAHOO.Bubbling
			.fire(
				"registerToolbarButtonAction",
				{
					actionName: "show-project-details",
					hideLabel: true,
					evaluate: function(asset, entity) {
						return asset.name != null && (asset.name === "taskList") && entity != null && entity.userAccess.edit;
					},
					fn: function(instance) {
						YAHOO.Bubbling.fire("toggleProjectDetails");
					}
				});


	}
})();