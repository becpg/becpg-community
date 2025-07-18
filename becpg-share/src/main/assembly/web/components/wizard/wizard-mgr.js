(function() {
    /**
     * YUI Library aliases
     */
    var Dom = YAHOO.util.Dom, Bubbling = YAHOO.Bubbling;

    /**
     * Alfresco Slingshot aliases
     */
    var $siteURL = Alfresco.util.siteURL;

    /**
     * beCPG.component.WizardMgr constructor.
     * 
     * @param {String}
     *            htmlId The HTML id of the parent element
     * @return {beCPG.component.WizardMgr} The new WizardMgr instance
     * @constructor
     */
    beCPG.component.WizardMgr = function WizardMgr_constructor(htmlId) {
        beCPG.component.WizardMgr.superclass.constructor.call(this, "beCPG.component.WizardMgr", htmlId);

        Bubbling.on("beforeFormRuntimeInit", this.onBeforeFormRuntimeInit, this);
        return this;
    };
    
    var nextAllowed = true;
    var button;
    var firstStepTab = null;
    
    function setNextAllowed(val) {
		if (button == null) {
			button = $(".wizard-mgr").find(".actions a[href$='#next']")[0].parentElement;
		}
		if (firstStepTab == null && val) {
			firstStepTab = $("list.first");
		}
		nextAllowed = val;
		const methodName = nextAllowed ? "remove" : "add";
		button.classList[methodName]("disabled");
	}

    YAHOO
        .extend(
            beCPG.component.WizardMgr,
            Alfresco.component.Base,
            {

                currentIndex: 0,

                /**
                 * Object container for initialization options
                 * 
                 * @property options
                 * @type object
                 */
                options:
                {
                    siteId: "",

                    nodeRef: "",

                    destination: "",

                    draft: false,

                    allSteps: false,
                    
                    readOnly: false,

                    wizardStruct: []
                },

                /**
                 * Fired by YUI when parent element is available for
                 * scripting.
                 * 
                 * @method onReady
                 */
                onReady: function WizardMgr_onReady() {
                    var me = this;


                    if (Dom.get(this.id + '-tabview') != null) {
                        this.widgets.tabView = new YAHOO.widget.TabView(this.id + '-tabview');
                    }

                    // Initialize wizard
                    this.widgets.wizard = jQuery("#" + this.id + "-wizard")
                        .steps(
                            {
                                stepsOrientation: "vertical",
                                enableCancelButton: true,
                                showFinishButtonAlways: this.options.draft,
                                enableAllSteps: this.options.allSteps,
                                enableKeyNavigation: false,
                                labels: {
                                    cancel: me.msg("wizard.cancel.button"),
                                    current: me.msg("wizard.current.step"),
                                    pagination: me.msg("wizard.pagination"),
                                    finish: me.msg("wizard.finish.button"),
                                    next: me.msg("wizard.next.button"),
                                    previous: me.msg("wizard.previous.button"),
                                    loading: me.msg("wizard.loading.msg")
                                },
                                onStepChanging: function(event, currentIndex, newIndex) {
                                    // Always allow previous
                                    // action even if the
                                    // current form is not
                                    // valid!
                                    if (currentIndex > newIndex) {
                                        return true;
                                    }
                                    
                                    if (!nextAllowed) return false;

                                    var isValid = false;

                                    var step = me.options.wizardStruct[currentIndex];
                                    if (step != null) {
                                        if (step.type == "form" || step.type == "survey") {
                                            if (step.form != null) {
                                            	Dom.get(me.id + "-step-" + step.id + "-form-submit").click();
                                                isValid = me.options.readOnly || step.readOnly || step.form.validate(Alfresco.forms.Form.NOTIFICATION_LEVEL_CONTAINER);
                                            } else {
												me.loadStep(me.options.wizardStruct[newIndex]);
												isValid = true;
											}
                                        } else {
                                            isValid = true;
                                        }
                                    }

                                    return isValid;

                                },
                                onStepChanged: function(event, currentIndex, priorIndex) {
									
                                    setNextAllowed(false);
                                    
	                                if (firstStepTab != null && !firstStepTab.hasClass("Valid")) {
                        				firstStepTab.addClass("Valid");
                    				}

                                    me.currentIndex = currentIndex;

                                    var step = me.options.wizardStruct[priorIndex];
                                    var nextStep = me.options.wizardStruct[currentIndex];
                                    // Load next step
                                    if (step != null && nextStep != null && ((step.type != "form" && step.type != "survey") || currentIndex < priorIndex)) {
                                        if (currentIndex > priorIndex) {
                                            nextStep.nodeRef = step.nodeRef;
                                        }

                                        if ((step.type != "form" && step.type != "survey") && step.nextStepWebScript != null) {

                                            var url = YAHOO.lang.substitute(
                                                Alfresco.constants.PROXY_URI + step.nextStepWebScript, {
                                                nodeRef: step.nodeRef
                                            });

                                            Alfresco.util.Ajax.jsonRequest({
                                                url: url,
                                                method: "GET",
                                                successCallback: {
                                                    fn: function(response) {
                                                        nextStep.nodeRef = response.json.nodeRef;
                                                        me.loadStep(nextStep);
                                                    },
                                                    scope: this
                                                }
                                            });
                                        } else {
                                            me.loadStep(nextStep);
                                        }
                                    }


                                },
                                onFinished: function(event, currentIndex) {
                                    var isValid = true;

                                    var step = me.options.wizardStruct[currentIndex];
                                    if (step != null) {
                                        if (step.type == "form" || step.type == "survey") {
                                            if (step.form != null) {
                                                Dom.get(me.id + "-step-" + step.id + "-form-submit")
                                                    .click();
                                                isValid = step.form
                                                    .validate(Alfresco.forms.Form.NOTIFICATION_LEVEL_CONTAINER);
                                                step.finish = true;

                                            } else {
                                                isValid = true;
                                            }
                                        }
                                    }

                                    if (isValid && !(step != null && step.finish)) {
                                        me._navigateForward(me.options.wizardStruct[0].nodeRef);
                                    }
                                    return isValid;

                                },
                                onCanceled: function(event, currentIndex) {
                                    Alfresco.util.PopupManager.displayPrompt({
                                        title: me.msg("message.confirm.cancel.title"),
                                        text: me.msg("message.confirm.cancel.description"),
                                        buttons: [{
                                            text: me.msg("wizard.ok.button"),
                                            handler: function() {
                                                this.destroy();
                                                //If first step is a creation delete project
                                                if (me.options.nodeRef == "" &&
                                                    me.options.wizardStruct[0].nodeRef) {
                                                    Alfresco.util.Ajax.request({
                                                        method: Alfresco.util.Ajax.DELETE,
                                                        url: Alfresco.constants.PROXY_URI + "slingshot/doclib/action/file/node/" + me.options.wizardStruct[0].nodeRef.replace("://", "/"),
                                                        scope: this,
                                                        execScripts: false,
                                                        successCallback: {
                                                            fn: function(response) {
                                                                me._navigateForward();
                                                            },
                                                            scope: this
                                                        }
                                                    });

                                                } else {
                                                    me._navigateForward();
                                                }
                                            }
                                        }, {
                                            text: me.msg("wizard.cancel.button"),
                                            handler: function() {
                                                this.destroy();
                                            },
                                            isDefault: true
                                        }]
                                    });

                                }
                            });

                    for (var i = 0; i < this.options.wizardStruct.length; i++) {
                        var step = this.options.wizardStruct[i];
                        step.index = i;

                        this.widgets.wizard.steps("add",
                            {
                                title: this.msg(step.label),
                                content: "<div id='" + this.id + "-step-" + step.id + "'>" + this.msg("wizard.loading.msg") + "</div>"
                            });

                        if (i == 0) {
                            me.loadStep(step);
                        }

                    }

                },

                onBeforeFormRuntimeInit: function WizardMgr_onBeforeFormRuntimeInit(layer, args) {

                    var splitted = args[1].eventGroup.split("-step-"), me = this;

                    if (splitted.length == 2) {
                        var stepId = splitted[1];
                        for (var i = 0; i < this.options.wizardStruct.length; i++) {
                            var step = this.options.wizardStruct[i];
                            if (step.id + "-form" == stepId) {
                                step.form = args[1].runtime;
                                step.form.setAJAXSubmit(true,
                                    {
                                        successCallback:
                                        {
                                            fn: me.onFormSubmit,
                                            scope: this
                                        },
                                        failureCallback:
                                        {
                                            fn: me.onFormSubmitFailure,
                                            scope: this
                                        }
                                    });
                            }
                        }

                    }

                },

                onFormSubmitFailure: function(response) {
                    if (response.json && response.json.message) {
                        var errorMsg;
                        var pattern = "Failed to execute script 'workspace:\/\/SpacesStore\/[a-zA-Z-0-9]{8}-[a-zA-Z-0-9]{4}-[a-zA-Z-0-9]{4}-[a-zA-Z-0-9]{4}-[a-zA-Z-0-9]{12}': [0-9]{8}";
                        var match = response.json.message.match(pattern);
                        if (match) {
                            errorMsg = response.json.message.split(match)[1];
                        } else {
                            errorMsg = response.json.message;
                        }
                        Alfresco.util.PopupManager.displayPrompt(
                            {
                                title: this.msg("message.failure"),
                                text: errorMsg
                            });
                    }
                },

                onFormSubmit: function(response) {
                    var me = this;
                    if (response.json.persistedObject) {

                        var nextStep = me.options.wizardStruct[me.currentIndex];
                        if (nextStep != null) {

                            if (nextStep.finish) {
                                me._navigateForward(me.options.wizardStruct[0].nodeRef);
                                return;
                            }

                            var step = me.options.wizardStruct[me.currentIndex - 1];
                            if (step != null) {
                                step.nodeRef = response.json.persistedObject;

                                if (step.nextStepWebScript != null) {

                                    var url = YAHOO.lang.substitute(
                                        Alfresco.constants.PROXY_URI + step.nextStepWebScript, {
                                        nodeRef: step.nodeRef
                                    });

                                    Alfresco.util.Ajax.jsonRequest({
                                        url: url,
                                        method: "GET",
                                        successCallback: {
                                            fn: function(response) {
                                                if (response.json && response.json.nodeRef) {
                                                    nextStep.nodeRef = response.json.nodeRef;
                                                }
                                                me.loadStep(nextStep);
                                            },
                                            scope: this
                                        }
                                    });
                                } else {
                                    nextStep.nodeRef = step.nodeRef;
                                    me.loadStep(nextStep);
                                }
                            }

                        }

                    }
                },

                loadStep: function WizardMgr_loadStep(step) {

                    if (!step.title || step.title == null) {
                        Dom.get(this.id + "-wizardTitle").innerHTML = "";
                        Dom.addClass(this.id + "-wizardTitle", "hidden");
                    } else {
                        Dom.get(this.id + "-wizardTitle").innerHTML = this.msg(step.title);
                        Dom.removeClass(this.id + "-wizardTitle", "hidden");
                    }

                    if (step.type == "entityDataList") {
                        Dom.addClass(this.id + "-wizard-p-" + step.index, "entity-data-lists");
                    } else {
                        Dom.removeClass(this.id + "-wizard-p-" + step.index, "entity-data-lists");
                    }

                    if (!step.nodeRef || step.nodeRef == null || step.nodeRef.length < 1) {
                        step.nodeRef = this.options.nodeRef;
                    }

                    if (step.nodeRefStepIndex != null && step.nodeRefStepIndex != "") {
                        step.nodeRef = this.options.wizardStruct[step.nodeRefStepIndex].nodeRef;
                    }

                    var url = null;
                    
                    const readOnly = this.options.readOnly || step.readOnly;
	                const self = this;
	                
					function then(validated, datalists) {
						if (step.type == "form") {
							url = YAHOO.lang.substitute(
	                            Alfresco.constants.URL_SERVICECONTEXT + "components/form" + "?destination={destination}" +
	                                "&formId={formId}" +
	                                "&itemId={itemId}" +
	                                "&itemKind={itemKind}" +
	                                "&mode={mode}&submitType=json&showCancelButton=false&showSubmitButton=true", {
	                                    mode: readOnly || validated ? "view" : (step.nodeRef != null && step.nodeRef.length > 0) ? "edit" : "create",
	                                    itemKind: (step.nodeRef != null && step.nodeRef.length > 0) ? "node" : "type",
	                                    itemId: (step.nodeRef != null && step.nodeRef.length > 0) ? step.nodeRef : step.itemId,
	                                    destination: self.options.destination,
	                                    formId: step.formId != null ? step.formId : ""
		                    });
	                    } else if (step.type == "entityDataList") {
	
	                        url = YAHOO.lang
	                            .substitute(
	                                Alfresco.constants.URL_SERVICECONTEXT + "components/entity-charact-views/simple-view" +
	                                "?list={list}&nodeRef={nodeRef}&itemType={itemType}&title={title}&formId={formId}&readOnly={readOnly}",
	                                {
	                                    nodeRef: step.nodeRef,
	                                    list: step.listId,
	                                    itemType: step.itemId,
	                                    title: encodeURIComponent(step.label),
	                                    formId: step.formId != null ? step.formId : "",
	                                    readOnly: String(readOnly || validated)
	                                });
	                    } else if (step.type == "documents") {
	
	                        url = YAHOO.lang
	                            .substitute(
	                                Alfresco.constants.URL_SERVICECONTEXT + "components/entity-charact-views/simple-documents-view" +
	                                "?nodeRef={nodeRef}",
	                                {
	                                    nodeRef: step.nodeRef,
	                                    title: encodeURIComponent(step.label)
	                                });
	                    } else if (step.type == "survey") {
	                        url = YAHOO.lang
	                            .substitute(
	                                Alfresco.constants.URL_SERVICECONTEXT + "components/survey/survey-form" +
	                                "?list={list}&nodeRef={nodeRef}&itemType={itemType}&title={title}" + (readOnly ? "&mode={mode}" : ""),
	                                {
	                                    nodeRef: step.nodeRef,
	                                    list: step.listId,
	                                    itemType: step.itemId,
	                                    title: encodeURIComponent(step.label),
	                                    mode: readOnly || validated ? "view" : undefined
	                                });
	
	                    }
	
	
	                    if (url != null && (step.type != "entityDataList" && step.type != "documents" || !step.loaded)) {	
	                            Alfresco.util.Ajax
	                                .request(
	                                    {
	                                        url: url,
	                                        dataObj:
	                                        {
	                                            htmlid: self.id + "-step-" + step.id
	                                        },
	                                        successCallback:
	                                        {
	                                            fn: function(response) {
													const stepDOM = Dom.get(self.id + "-step-" + step.id);
													stepDOM.innerHTML = response.serverResponse.responseText;
													if (step.type == "form" && (readOnly || validated)) {
	                                                	stepDOM.classList.add("properties-view");
	                                                }
	                                                step.loaded = true;
	                                                if (step.type == "entityDataList") {
	                                                    self.loadDataList(step, datalists);
	                                                } else {
														setNextAllowed(step.index != self.options.wizardStruct.length - 1);
													}
	                                            },
	                                            scope: this
	                                        },
	                                        execScripts: true
	                                    });
	                        } else {
								setNextAllowed(step.index != self.options.wizardStruct.length - 1);
							}
					}
					if (!readOnly) {
						Alfresco.util.Ajax.jsonGet({
	                        url: Alfresco.constants.PROXY_URI + "becpg/entitylists/node/" + step.nodeRef.replace(":/", ""),
	                        successCallback: {
	                            fn: function(response) {
									const datalists = response.json.datalists;
									then(datalists.filter(function (datalist) {
										return datalist.name === (
											step.type === "form" ? "View-properties" : (
												step.type === "documents" ? "View-documents" : step.listId));
									}).some(function (datalist) { 
										return datalist.state === "Valid";
									}), datalists);
		                        }        								 
	                        }
		                 });
		            } else {
						then(false);
					}
                },

                loadDataList: function WizardMgr_loadDataList(step, datalists) {
                    var me = this;
                    function then(datalists) {
                        var lists = datalists, list;
                        for (var i = 0, ii = lists.length; i < ii; i++) {
                            list = lists[i];
                            if (list.name == step.listId) {

                                var stepAnchor = me.widgets.wizard.steps("getStepAnchor");
                                stepAnchor.parent().addClass(list.state);

                                YAHOO.Bubbling.fire("simpleView-" + me.id + "-step-" + step.id + "scopedActiveDataListChanged", {
                                    list: list.name,
                                    dataList: list,
                                    entity: null
                                });

                            }
                        }
                        setNextAllowed(step.index != me.options.wizardStruct.length - 1);
                    }
                    if (datalists == null) {
	                    Alfresco.util.Ajax.jsonGet({
	                        url: Alfresco.constants.PROXY_URI + "becpg/entitylists/node/" + step.nodeRef.replace(":/", ""),
	                        successCallback: {
	                            fn: function(response) {
	                                var lists = response.json.datalists, list;
	                                then(lists);
	                                setNextAllowed(step.index != me.options.wizardStruct.length - 1);
	                            },
	                            scope: this
	                        }
	                    });
                    } else {
						then(datalists);
					}
                },

                /**
                 * Displays the corresponding details page for the current node
                 *
                 * @method _navigateForward
                 * @private
                 * @param nodeRef {Alfresco.util.NodeRef} Optional: NodeRef of just-created content item
                 */
                _navigateForward: function WizardMgr__navigateForward(nodeRef) {
                    /* Have we been given a nodeRef from the Forms Service? */
                    if (YAHOO.lang.isObject(nodeRef)) {
                        window.location.href = $siteURL("entity-data-lists?list=View-properties&nodeRef=" + nodeRef.toString());
                    }
                    else if (document.referrer) {
                        /* Did we come from the document library? If so, then direct the user back there */
                        if (document.referrer.match(/documentlibrary([?]|$)/) || document.referrer.match(/repository([?]|$)/)) {
                            // go back to the referrer page
                            history.go(-1);
                        }
                        else {
                            document.location.href = document.referrer;
                        }
                    }
                    else if (this.options.siteId && this.options.siteId !== "") {
                        // In a Site, so go back to the document library root
                        window.location.href = $siteURL("documentlibrary");
                    }
                    else {
                        // Nowhere sensible to go other than the default page unless we're in a portal
                        if (Alfresco.constants.PORTLET) {
                            window.location.href = $siteURL("repository");
                        }
                        else {
                            window.location.href = Alfresco.constants.URL_CONTEXT;
                        }
                    }


                }
            });
})();
