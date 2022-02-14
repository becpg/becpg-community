/**
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * StartWorkflow component.
 *
 * @namespace Alfresco.component
 * @class beCPG.component.StartProcess
 */
(function() {
	/**
	 * YUI Library aliases
	 */
	var Dom = YAHOO.util.Dom,
		Event = YAHOO.util.Event;

	/**
	 * StartWorkflow constructor.
	 *
	 * @param {String} htmlId The HTML id of the parent element
	 * @return {beCPG.component.StartProcess} The new StartWorkflow instance
	 * @constructor
	 */
	beCPG.component.StartProcess = function StartWorkflow_constructor(htmlId) {
		beCPG.component.StartProcess.superclass.constructor.call(this, htmlId, ["button"]);

		// Re-register with our own name
		this.name = "beCPG.component.StartProcess";
		Alfresco.util.ComponentManager.reregister(this);

		// Instance variables
		this.options = YAHOO.lang.merge(this.options, beCPG.component.StartProcess.superclass.options);
		this.selectedItems = "";
		this.destination = "";
		this.currentDefinition = null;
		this.workflowTypes = [];

		YAHOO.Bubbling.on("objectFinderReady", this.onObjectFinderReady, this);
		YAHOO.Bubbling.on("formContentReady", this.onStartWorkflowFormContentReady, this);
		YAHOO.Bubbling.on("afterFormRuntimeInit", this.onStartWorkflowAfterFormRuntimeInit, this);

		return this;
	};

	YAHOO.extend(beCPG.component.StartProcess, Alfresco.component.ShareFormManager,
		{
			/**
			 * Object container for initialization options
			 *
			 * @property options
			 * @type object
			 */
			options:
			{
				/**
				 * The nodeRefs, separated by commas, to display in the workflow forms packageItems control.
				 *
				 * @property selectedItems
				 * @type string
				 */
				selectedItems: "",

				/**
				 * A nodeRef that represents the context of the workflow
				 *
				 * @property destination
				 * @type string
				 */
				destination: "",

				/**
				 * The workflow types that can be started
				 *
				 * @property workflowDefinitions
				 * @type Array of
				 *    {
				 *       name: {String} The workflow name (unique)
				 *       title: {String} The title of the workflow
				 *       description {String} The description of the workflow
				 *    }
				 */
				workflowDefinitions: [],
				/**
				 Current Site 
				 */
				siteId : ""
			},

			/**
			 * Fired by YUI when parent element is available for scripting.
			 * Template initialisation, including instantiation of YUI widgets and event listener binding.
			 *
			 * @method onReady
			 */
			onReady: function StartWorkflow_onReady() {
				this.widgets.workflowDefinitionMenuButton = Alfresco.util.createYUIButton(this, "workflow-definition-button",
					this.onWorkflowSelectChange,
					{
						label: this.msg("label.selectWorkflowDefinition") + " " + Alfresco.constants.MENU_ARROW_SYMBOL,
						title: this.msg("title.selectWorkflowDefinition"),
						type: "menu",
						menu: "workflow-definition-menu"
					});
				return beCPG.component.StartProcess.superclass.onReady.call(this);
			},

			/**
			 * Will populate the form packageItem's objectFinder with selectedItems when its ready
			 *
			 * @method onObjectFinderReady
			 * @param layer {object} Event fired (unused)
			 * @param args {array} Event parameters
			 */
			onObjectFinderReady: function StartWorkflow_onObjectFinderReady(layer, args) {
				var objectFinder = args[1].eventGroup;
				var formFields = this.currentDefinition.formFields;
				
				if (objectFinder.options.field == "assoc_packageItems" && objectFinder.eventGroup.indexOf(this.id) == 0) {
					objectFinder.selectItems(this.options.selectedItems);
				} else if(formFields!=null){
					var savedValue = formFields[objectFinder.options.field];
					if (savedValue !== undefined && savedValue !== "") {
					 objectFinder.selectItems(savedValue);
					}
				}
			},

			/**
			 * Called when a workflow definition has been selected
			 *
			 * @method onWorkflowSelectChange
			 */
			onWorkflowSelectChange: function StartWorkflow_onWorkflowSelectChange(p_sType, p_aArgs) {
				var i = p_aArgs[1].index;
				if (i >= 0) {
					// Update label of workflow menu button
					var workflowDefinition = this.options.workflowDefinitions[i];

					this.widgets.workflowDefinitionMenuButton.set("label", workflowDefinition.title + " " + Alfresco.constants.MENU_ARROW_SYMBOL);
					this.widgets.workflowDefinitionMenuButton.set("title", workflowDefinition.description);

					this.currentDefinition = workflowDefinition;

					var data = {
						htmlid: this.id + "-startWorkflowForm-" + Alfresco.util.generateDomId(),
						itemKind: workflowDefinition.itemKind,
						itemId: workflowDefinition.itemId,
						mode: "create",
						submitType: "json",
						showCaption: true,
						formUI: true,
						showCancelButton: true,
						destination: this.options.destination
					};


					if (workflowDefinition.formId != null) {
						data.formId = workflowDefinition.formId;
					}

					if (workflowDefinition.destination != null) {
						data.destination = workflowDefinition.destination;
					}

					if (workflowDefinition.submissionUrl != null) {
						data.submissionUrl = YAHOO.lang
							.substitute( workflowDefinition.submissionUrl,
								{
									selectedItems: this.options.selectedItems
								});
					}

					// Load the form for the specific workflow
					Alfresco.util.Ajax.request(
						{
							url: Alfresco.constants.URL_SERVICECONTEXT + "components/form",
							dataObj: data,
							successCallback:
							{
								fn: this.onWorkflowFormLoaded,
								scope: this
							},
							failureMessage: this.msg("message.failure"),
							scope: this,
							execScripts: true
						});
				}

			},

			 /**
             * Handler called when the form was submitted successfully
             *
             * @method onFormSubmitSuccess
             * @param response The response from the submission
             */
            onFormSubmitSuccess: function FormManager_onFormSubmitSuccess(response)
            {
	
				if (this.currentDefinition.onFormSubmitSuccessUrl) {
					
					var onFormSubmitSuccessUrl  = YAHOO.lang
							.substitute( this.currentDefinition.onFormSubmitSuccessUrl,
								{
									nodeRef: response.json.persistedObject,
									selectedItems: this.options.selectedItems
								});
								
					Alfresco.util.Ajax.request(
						{
							url: Alfresco.constants.PROXY_URI_RELATIVE + onFormSubmitSuccessUrl,
							method : Alfresco.util.Ajax.GET,
							scope: this,
						});
				}
	
				if(this.currentDefinition.redirectUrl){
					
					var redirectUrl  = YAHOO.lang
							.substitute( this.currentDefinition.redirectUrl,
								{
									nodeRef: response.json.persistedObject
								});
					
					
					document.location.href =   Alfresco.util.siteURL(redirectUrl,
			        {
			            site : this.options.siteId
			        });
					
					
				} else {
               		this.navigateForward(true);
				}
            },

			/**
			 * Called when a workflow form has been loaded.
			 * Will insert the form in the Dom.
			 *
			 * @method onWorkflowFormLoaded
			 * @param response {Object}
			 */
			onWorkflowFormLoaded: function StartWorkflow_onWorkflowFormLoaded(response) {
				var formEl = Dom.get(this.id + "-workflowFormContainer");

				Dom.addClass(formEl, "hidden");
				formEl.innerHTML = response.serverResponse.responseText;
			},

			/**
			 * Event handler called when the "formContentReady" event is received
			 */
			onStartWorkflowFormContentReady: function FormManager_onStartWorkflowFormContentReady(layer, args) {
				var formEl = Dom.get(this.id + "-workflowFormContainer");
				Dom.removeClass(formEl, "hidden");
			},

			/**
			  Populate form with process variables
			 */
			populateCurrentForm: function StartWorkflow_populateCurrentForm() {
				var formFields = this.currentDefinition.formFields;
				var elForm = Dom.get(this.formruntime.formId);

				for (var i = 0, j = elForm.elements.length; i < j; i++) {
					var element = elForm.elements[i];
					var name = element.name;
					if (name != undefined && name !== "-") {
						var savedValue = formFields[name];

						if (savedValue !== undefined && savedValue !== "") {
							if (element.type === "checkbox" || element.type === "radio") {
								element.checked = (savedValue === "true");
							} else if (name.match("-range$") == "-range") {
								// found number range?
								var cntrl = Dom.get(element.id + "-cntrl-min");
								if (cntrl) {
									// populate number range
									// elements
									cntrl.value = savedValue.substring(0, savedValue.indexOf("|"));
									cntrl = Dom.get(element.id + "-cntrl-max");
									cntrl.value = savedValue.substring(savedValue.indexOf("|") + 1, savedValue.length);
									// set range value to the
									// input hidden field
									cntrl = Dom.get(element.id);
									cntrl.value = savedValue;
								} else {
									// probably date range -
									// just set value and
									// control will handle it
									element.value = savedValue;
								}
							} else if (name.indexOf("assoc_") == 0 && name.indexOf("_added") > 0) {
								element.value = savedValue;

								var cleanElementId = element.id.replace("-cntrl-added", "");

								if (Dom.get(cleanElementId + "-autocomplete") != null) {
									YAHOO.Bubbling.fire(cleanElementId + "refreshContent", savedValue, this);
								}
							} else if (name.indexOf("assoc_") != 0) {
								element.value = savedValue;
							}


							// reverse value setting doesn't
							// work with
							// checkboxes or multi-select boxes
							// because of the
							// hidden field used to store the
							// underlying field
							// value
							if (element.type === "hidden") {
								// hidden fields could be a part
								// of a checkbox
								// or similar in the Forms
								// runtime
								// so look if there is a entry
								// element attached
								// this hidden field and set the
								// value
								var cntrl = Dom.get(element.id + "-entry");
								if (cntrl) {
									switch (cntrl.type) {
										case "checkbox":
											cntrl.checked = (savedValue === "true");
											break;
										default: // "select-multiple"
											// - and
											// potentially
											// others following
											// the same pattern
											cntrl.value = savedValue;
											break;
									}
								}
							}
						}
					}
				}
				YAHOO.Bubbling.fire("formContentsUpdated");

			},

			/**
			 * Event handler called when the "afterFormRuntimeInit" event is received
			 */
			onStartWorkflowAfterFormRuntimeInit: function StartWorkflow_onStartWorkflowAfterFormRuntimeInit(layer, args) {
				this.formruntime = args[1].runtime;
				this.formruntime.setAsReusable(false);


				if (this.currentDefinition != null) {
					if (this.currentDefinition.formFields != null) {
						this.populateCurrentForm();
					}

					if (this.currentDefinition.hiddenFields != null) {
						for (idx in this.currentDefinition.hiddenFields) {
							var propName = this.formruntime.formId.substring(0, this.formruntime.formId.length - 5) + "_" + this.currentDefinition.hiddenFields[idx];
							var inputEl = YAHOO.util.Dom.get(propName);
							while (inputEl != null) {
								inputEl = inputEl.parentNode;
								if (YAHOO.util.Dom.hasClass(inputEl, "form-field")) {
									break;
								}
							}
							YAHOO.util.Dom.addClass(inputEl, "hidden");
						}
					}
				}


			},

			onFormSubmitFailure: function StartWorkflow_onFormSubmitFailure(response) {
				var message = (response.json && response.json.message ? response.json.message : this.msg(this.options.failureMessageKey));

				// Since it's a WorkflowException (expected), no need to show the Exception-type to the user, only
				// the message and error-number will do.
				message = message.replace("org.alfresco.service.cmr.workflow.WorkflowException:", "");
				Alfresco.util.PopupManager.displayPrompt(
					{
						title: this.msg(this.options.failureMessageKey),
						text: (message)
					});
			}

		});

})();
