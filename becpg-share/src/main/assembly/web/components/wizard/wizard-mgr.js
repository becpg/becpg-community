(function() {
    var Dom = YAHOO.util.Dom, Bubbling = YAHOO.Bubbling;
    var $siteURL = Alfresco.util.siteURL;

    beCPG.component.WizardMgr = function(htmlId) {
        beCPG.component.WizardMgr.superclass.constructor.call(this, "beCPG.component.WizardMgr", htmlId);
        Bubbling.on("beforeFormRuntimeInit", this.onBeforeFormRuntimeInit, this);
        return this;
    };

    var nextAllowed = true, button, firstStepTab, validationInProgress = false;

    function setNextAllowed(val) {
        if (!button) button = $(".wizard-mgr").find(".actions a[href$='#next']")[0].parentElement;
        if (!firstStepTab && val) firstStepTab = $("li.first");
        nextAllowed = val;
        button.classList[val ? "remove" : "add"]("disabled");
    }

    YAHOO.extend(beCPG.component.WizardMgr, Alfresco.component.Base, {
        currentIndex: 0,
        options: {
            siteId: "", nodeRef: "", destination: "", draft: false,
            allSteps: false, readOnly: false, wizardStruct: []
        },

        onReady: function() {
            var me = this;

            if (Dom.get(this.id + '-tabview')) {
                this.widgets.tabView = new YAHOO.widget.TabView(this.id + '-tabview');
            }

            this.widgets.wizard = jQuery("#" + this.id + "-wizard").steps({
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
                onStepChanging: function(__event, currentIndex, newIndex) {

                    var step = me.options.wizardStruct[currentIndex];
                    if (currentIndex > newIndex && step && (step.type === "form" || step.type === "survey")) {
                        me.showStepChangeConfirmation(function() {
                            me.widgets.wizard.steps("previous");
                        });
                        return false;
                    }

                    if (currentIndex > newIndex) return true;
                    if (!nextAllowed || validationInProgress) return false;

                    if (!step) return true;

                    if (step.type === "form" || step.type === "survey") {
                        if (step.form) {
                            validationInProgress = true;
                            Dom.get(me.id + "-step-" + step.id + "-form-submit").click();
                            var isValid = me.options.readOnly || step.readOnly ||
                                step.form.validate(Alfresco.forms.Form.NOTIFICATION_LEVEL_CONTAINER);
                            validationInProgress = false;
                            return isValid;
                        } else {
                            me.loadStep(me.options.wizardStruct[newIndex]);
                        }
                    }
                    return true;
                },
                onStepChanged: function(__event, currentIndex, priorIndex) {
                    setNextAllowed(false);
                    if (firstStepTab && !firstStepTab.hasClass("Valid")) {
                        firstStepTab.addClass("Valid");
                    }
                    me.currentIndex = currentIndex;
                    me.handleStepTransition(priorIndex, currentIndex);
                },
                onFinished: function(__event, currentIndex) {
                    return me.handleFinish(currentIndex);
                },
                onCanceled: function(__event, __currentIndex) {
                    me.handleCancel();
                }
            });

            this.options.wizardStruct.forEach(function(step, i) {
                step.index = i;
                me.widgets.wizard.steps("add", {
                    title: me.msg(step.label),
                    content: "<div id='" + me.id + "-step-" + step.id + "'>" + me.msg("wizard.loading.msg") + "</div>"
                });
                if (i === 0) me.loadStep(step);
            });
        },

        handleStepTransition: function(priorIndex, currentIndex) {
            var step = this.options.wizardStruct[priorIndex];
            var nextStep = this.options.wizardStruct[currentIndex];

            if (!step || !nextStep) return;

            var forward = currentIndex > priorIndex;
            var isFormStep = step.type === "form" || step.type === "survey";
            var stepReadOnly = this.options.readOnly || step.readOnly;

            if (isFormStep && forward && !stepReadOnly) return;

            if (forward) nextStep.nodeRef = step.nodeRef;

            if (step.nextStepWebScript && (!isFormStep || stepReadOnly)) {
                this.executeWebScript(step, nextStep);
            } else {
                this.loadStep(nextStep);
            }
        },

        executeWebScript: function(step, nextStep) {
            var url = YAHOO.lang.substitute(Alfresco.constants.PROXY_URI + step.nextStepWebScript, {
                nodeRef: step.nodeRef
            });

            Alfresco.util.Ajax.jsonRequest({
                url: url, method: "GET",
                successCallback: {
                    fn: function(response) {
                        nextStep.nodeRef = response.json.nodeRef;
                        this.loadStep(nextStep);
                    },
                    scope: this
                }
            });
        },

        handleFinish: function(currentIndex) {
            var step = this.options.wizardStruct[currentIndex];
            if (!step) return true;

            if (step.type === "form" || step.type === "survey") {
                if (step.form) {
                    validationInProgress = true;
                    Dom.get(this.id + "-step-" + step.id + "-form-submit").click();
                    var isValid = this.options.readOnly || step.readOnly ||
                        step.form.validate(Alfresco.forms.Form.NOTIFICATION_LEVEL_CONTAINER);
                    validationInProgress = false;
                    step.finish = true;
                    if (!isValid) return false;
                }
            }

            if (!(step && step.finish)) {
                this._navigateForward(this.options.wizardStruct[0].nodeRef);
            }
            return true;
        },

        handleCancel: function() {
            var me = this;
            this.showStepChangeConfirmation(function() {
                if (me.options.nodeRef === "" && me.options.wizardStruct[0].nodeRef) {
                    me.deleteAndNavigate();
                } else {
                    me._navigateForward();
                }
            });
        },

        showStepChangeConfirmation: function(callback) {
            var me = this;
            Alfresco.util.PopupManager.displayPrompt({
                title: me.msg("message.confirm.cancel.title"),
                text: me.msg("message.confirm.cancel.description"),
                buttons: [{
                    text: me.msg("wizard.ok.button"),
                    handler: function() {
                        this.destroy();
                        callback();
                    }
                }, {
                    text: me.msg("wizard.cancel.button"),
                    handler: function() { this.destroy(); },
                    isDefault: true
                }]
            });
        },

        deleteAndNavigate: function() {
            Alfresco.util.Ajax.request({
                method: Alfresco.util.Ajax.DELETE,
                url: Alfresco.constants.PROXY_URI + "slingshot/doclib/action/file/node/" +
                    this.options.wizardStruct[0].nodeRef.replace("://", "/"),
                successCallback: {
                    fn: this._navigateForward,
                    scope: this
                }
            });
        },

        onBeforeFormRuntimeInit: function(__layer, args) {
            var splitted = args[1].eventGroup.split("-step-");
            if (splitted.length !== 2) return;

            var stepId = splitted[1];
            var step = this.options.wizardStruct.find(function(s) {
                return s.id + "-form" === stepId;
            });

            if (step) {
                step.form = args[1].runtime;
                step.form.setAJAXSubmit(true, {
                    successCallback: { fn: this.onFormSubmit, scope: this },
                    failureCallback: { fn: this.onFormSubmitFailure, scope: this }
                });
            }
        },

        onFormSubmitFailure: function(response) {
            if (response.json && response.json.message) {
                var errorMsg = response.json.message;
                var pattern = /Failed to execute script 'workspace:\/\/SpacesStore\/[a-zA-Z-0-9-]{36}': [0-9]{8}/;
                var match = errorMsg.match(pattern);
                if (match) errorMsg = errorMsg.split(match[0])[1];

                Alfresco.util.PopupManager.displayPrompt({
                    title: this.msg("message.failure"),
                    text: errorMsg
                });
            }
        },

        onFormSubmit: function(response) {
            if (!response.json.persistedObject) return;

            var nextStep = this.options.wizardStruct[this.currentIndex];
            if (!nextStep) return;

            if (nextStep.finish) {
                this._navigateForward(this.options.wizardStruct[0].nodeRef);
                return;
            }

            var step = this.options.wizardStruct[this.currentIndex - 1];
            if (step) {
                step.nodeRef = response.json.persistedObject;
                if (step.nextStepWebScript) {
                    this.executeWebScript(step, nextStep);
                } else {
                    nextStep.nodeRef = step.nodeRef;
                    this.loadStep(nextStep);
                }
            }
        },

        loadStep: function(step) {
            this.updateStepTitle(step);
            this.updateStepClass(step);

            if (!step.nodeRef) step.nodeRef = this.options.nodeRef;
            if (step.nodeRefStepIndex != null) {
                step.nodeRef = this.options.wizardStruct[step.nodeRefStepIndex].nodeRef;
            }

            var readOnly = this.options.readOnly || step.readOnly;
            var me = this;

            this.checkValidation(step, readOnly, function(validated, datalists) {
                var url = me.buildStepUrl(step, readOnly, validated);
                if (url && (!step.loaded || (step.type !== "entityDataList" && step.type !== "documents"))) {
                    me.loadStepContent(step, url, readOnly, validated, datalists);
                } else {
                    setNextAllowed(step.index !== me.options.wizardStruct.length - 1);
                }
            });
        },

        updateStepTitle: function(step) {
            var titleEl = Dom.get(this.id + "-wizardTitle");
            if (!step.title) {
                titleEl.innerHTML = "";
                Dom.addClass(this.id + "-wizardTitle", "hidden");
            } else {
                titleEl.innerHTML = this.msg(step.title);
                Dom.removeClass(this.id + "-wizardTitle", "hidden");
            }
        },

        updateStepClass: function(step) {
            var className = "entity-data-lists";
            var element = Dom.get(this.id + "-wizard-p-" + step.index);
            if (step.type === "entityDataList") {
                Dom.addClass(element, className);
            } else {
                Dom.removeClass(element, className);
            }
        },

        checkValidation: function(step, readOnly, callback) {
            if (!readOnly && step.nodeRef && step.nodeRef.length > 0) {
                Alfresco.util.Ajax.jsonGet({
                    url: Alfresco.constants.PROXY_URI + "becpg/entitylists/node/" + step.nodeRef.replace(":/", ""),
                    successCallback: {
                        fn: function(response) {
                            var datalists = response.json.datalists;
                            var listName = step.type === "form" ? "View-properties" :
                                step.type === "documents" ? "View-documents" : step.listId;
                            var validated = datalists.some(function(dl) {
                                return dl.name === listName && dl.state === "Valid";
                            });
                            callback(validated, datalists);
                        }
                    }
                });
            } else {
                // Don't fetch for readonly steps - just call callback
                callback(readOnly, null);
            }
        },

        buildStepUrl: function(step, readOnly, validated) {
            var baseUrl = Alfresco.constants.URL_SERVICECONTEXT + "components/";
            var params = {
                nodeRef: step.nodeRef,
                title: encodeURIComponent(step.label),
                formId: step.formId || ""
            };

            switch (step.type) {
                case "form":
                    return YAHOO.lang.substitute(baseUrl + "form?destination={destination}&formId={formId}&itemId={itemId}&itemKind={itemKind}&mode={mode}&submitType=json&showCancelButton=false&showSubmitButton=true", {
                        mode: readOnly || validated ? "view" : (step.nodeRef && step.nodeRef.length > 0) ? "edit" : "create",
                        itemKind: (step.nodeRef && step.nodeRef.length > 0) ? "node" : "type",
                        itemId: (step.nodeRef && step.nodeRef.length > 0) ? step.nodeRef : step.itemId,
                        destination: this.options.destination,
                        formId: params.formId
                    });
                case "entityDataList":
                    return YAHOO.lang.substitute(baseUrl + "entity-charact-views/simple-view?list={list}&nodeRef={nodeRef}&itemType={itemType}&title={title}&formId={formId}&readOnly={readOnly}",
                        YAHOO.lang.merge(params, {
                            list: step.listId,
                            itemType: step.itemId,
                            readOnly: String(readOnly || validated)
                        }));
                case "documents":
                    return YAHOO.lang.substitute(baseUrl + "entity-charact-views/simple-documents-view?nodeRef={nodeRef}", params);
                case "survey":
                    var surveyParams = YAHOO.lang.merge(params, {
                        list: step.listId,
                        itemType: step.itemId
                    });
                    var surveyUrl = baseUrl + "survey/survey-form?list={list}&nodeRef={nodeRef}&itemType={itemType}&title={title}";
                    if (readOnly || validated) surveyUrl += "&mode=view";
                    return YAHOO.lang.substitute(surveyUrl, surveyParams);
                default:
                    return null;
            }
        },

        loadStepContent: function(step, url, readOnly, validated, datalists) {
            var me = this;
            Alfresco.util.Ajax.request({
                url: url,
                dataObj: { htmlid: this.id + "-step-" + step.id },
                successCallback: {
                    fn: function(response) {
                        var stepDOM = Dom.get(me.id + "-step-" + step.id);
                        stepDOM.innerHTML = response.serverResponse.responseText;
                        if (step.type === "form" && (readOnly || validated)) {
                            stepDOM.classList.add("properties-view");
                        }
                        step.loaded = true;
                        if (step.type === "entityDataList") {
                            me.loadDataList(step, datalists);
                        } else {
                            setNextAllowed(step.index !== me.options.wizardStruct.length - 1);
                        }
                    }
                },
                execScripts: true
            });
        },

        loadDataList: function(step, datalists) {
            var me = this;
            function processDataLists(lists) {
                var list = lists.find(function(l) { return l.name === step.listId; });
                if (list) {
                    var stepAnchor = me.widgets.wizard.steps("getStepAnchor");
                    stepAnchor.parent().addClass(list.state);
                    YAHOO.Bubbling.fire("simpleView-" + me.id + "-step-" + step.id + "scopedActiveDataListChanged", {
                        list: list.name, dataList: list, entity: null
                    });
                }
                setNextAllowed(step.index !== me.options.wizardStruct.length - 1);
            }

            if (datalists) {
                processDataLists(datalists);
            } else if (step.nodeRef && step.nodeRef.length > 0) {
                Alfresco.util.Ajax.jsonGet({
                    url: Alfresco.constants.PROXY_URI + "becpg/entitylists/node/" + step.nodeRef.replace(":/", ""),
                    successCallback: {
                        fn: function(response) { processDataLists(response.json.datalists); }
                    }
                });
            } else {
                // No datalists and no nodeRef - still need to enable next button
                setNextAllowed(step.index !== me.options.wizardStruct.length - 1);
            }
        },

        _navigateForward: function(nodeRef) {
            if (YAHOO.lang.isObject(nodeRef)) {
                window.location.href = $siteURL("entity-data-lists?list=View-properties&nodeRef=" + nodeRef.toString());
            } else if (document.referrer) {
                if (document.referrer.match(/documentlibrary([?]|$)/) || document.referrer.match(/repository([?]|$)/)) {
                    history.go(-1);
                } else {
                    document.location.href = document.referrer;
                }
            } else if (this.options.siteId) {
                window.location.href = $siteURL("documentlibrary");
            } else {
                window.location.href = Alfresco.constants.PORTLET ? $siteURL("repository") : Alfresco.constants.URL_CONTEXT;
            }
        }
    });
})();