(function() {
    "use strict";

  

    var INPUT_TYPES = {
        "text": { type: "text" },
        "number": { type: "number" },
        "int": { 
            type: "number", 
            step: 1, 
            pattern: "\\d+", 
            onchange: "this.value = parseInt(this.value, 10) || 0" 
        },
        "percentage": { 
            type: "number", 
            min: 0, 
            max: 100, 
            onchange: "this.value = Math.min(100, Math.max(0, parseFloat(this.value) || 0))" 
        },
        "date": { type: "date" },
        "dateTime": { type: "datetime-local" }
    };

    // YUI Library aliases
    var Dom = YAHOO.util.Dom;
    var Event = YAHOO.util.Event;
    var Bubbling = YAHOO.Bubbling;

    /**
     * DecisionTree constructor.
     * 
     * @param {String} htmlId The HTML id of the parent element
     * @param {String} fieldId The field identifier
     * @return {beCPG.component.DecisionTree} The new DecisionTree instance
     * @constructor
     */
    beCPG.component.DecisionTree = function(htmlId, fieldId) {
        beCPG.component.DecisionTree.superclass.constructor.call(this, "beCPG.component.DecisionTree", htmlId, ["button", "container"]);

        // Event listeners
        Bubbling.on("afterFormRuntimeInit", this.onAfterFormRuntimeInit, this);
        Bubbling.on("refreshDecisionTree", this.refreshDecisionTree, this);

        this.fieldId = fieldId;
        this.afterFormInitStack = [];
        
        // Instance-specific event classes
        this.QUESTION_EVENTCLASS = Alfresco.util.generateDomId(null, "question");
        this.LIST_EVENTCLASS = Alfresco.util.generateDomId(null, "list");
        this.COMMENT_EVENTCLASS = Alfresco.util.generateDomId(null, "comment");

        return this;
    };

    // Extend from Alfresco.component.Base
    YAHOO.extend(beCPG.component.DecisionTree, Alfresco.component.Base);

    /**
     * Augment prototype with main class implementation
     */
    YAHOO.lang.augmentObject(beCPG.component.DecisionTree.prototype, {
        
        formRuntime: null,
        afterFormInitStack: null,

        /**
         * Configuration options
         */
        options: {
            disabled: false,
            prefix: "",
            data: [],
            currentValue: []
        },

        /**
         * Initialize component when DOM is ready
         */
        onReady: function DecisionTree_onReady() {
            
            var me = this;
            var htmlForm = this._buildFormHTML();
            
            var ctrlBody = Dom.get(this.id + "-body");
            if (ctrlBody) {
                ctrlBody.innerHTML = htmlForm;
                this._executeAfterFormInitStack();
                this._setupEventHandlers();
            }
        },

        /**
         * Build the complete form HTML
         * @return {String} Complete HTML string
         * @private
         */
        _buildFormHTML: function() {
            var htmlForm = "";
            this.afterFormInitStack = [];

            for (var i = 0; i < this.options.data.length; i++) {
                var question = this.options.data[i];
                htmlForm += this._buildQuestionHTML(question);
            }

            return htmlForm;
        },

        /**
         * Build HTML for a single question
         * @param {Object} question Question configuration
         * @return {String} HTML string for the question
         * @private
         */
        _buildQuestionHTML: function(question) {
            var htmlForm = "";
            var showComment = false;
            var me = this;

            if (question.choices) {
                htmlForm += this._buildQuestionFieldset(question);
                
                var commentChoice = null;
                for (var j = 0; j < question.choices.length; j++) {
                    var choice = question.choices[j];
                    htmlForm += this._buildChoiceHTML(question, choice);
                    
                    if (choice.comment) {
                        showComment = true;
                        commentChoice = choice;
                    }
                }

                if (showComment && commentChoice) {
                    htmlForm += '<div id="' + this.id + '-comment_' + question.id + '" class="decision-tree-comments hidden"></div>';
                    
                    // Closure to capture variables for async execution
                    (function(questionId, choiceRef) {
                        me.afterFormInitStack.push(function() {
                            me.insertComment(questionId, choiceRef);
                        });
                    })(question.id, commentChoice);
                }

                if (question.lowerNote) {
                    htmlForm += '<span class="decision-tree-note">' + this._escapeHtml(question.lowerNote) + '</span>';
                }

                htmlForm += '</fieldset>';
            } else {
                htmlForm += this._buildMessageHTML(question);
            }

            return htmlForm;
        },

        /**
         * Build fieldset for question with metadata
         * @param {Object} question Question configuration
         * @return {String} HTML string
         * @private
         */
        _buildQuestionFieldset: function(question) {
            var mandatoryClass = question.mandatory ? " mandatory" : "";
            var htmlForm = '<fieldset id="' + this.id + '-question_' + question.id + '" class="hidden' + mandatoryClass + '">';
            
            // Legend
            var description = this.msg("form.control.decision-tree." + this.options.prefix + "." + question.id + ".description");
            var label = question.label || this.msg("form.control.decision-tree." + this.options.prefix + "." + question.id + ".label");
            var legendTitle = question.id.length < 10 ? question.id.toUpperCase() + ' - ' : "";
            
            var mandatoryIndicator = question.mandatory ? '<span id="' + this.id + '-mandatory_' + question.id + '" class="mandatory-indicator" title="' + this._escapeHtml(this.msg("form.field.incomplete")) + '">*</span>' : "";
            
            htmlForm += '<legend title="' + this._escapeHtml(description) + '">' + 
                       this._escapeHtml(legendTitle + label) + mandatoryIndicator + '</legend>';

            // Requirements
            if (question.requirements && question.requirements.length > 0) {
                htmlForm += this._buildRequirementsHTML(question.requirements);
            }

            // Notes and URLs
            if (question.note) {
                htmlForm += '<span class="decision-tree-note">' + this._escapeHtml(question.note) + '</span>';
            }
            if (question.upperNote) {
                htmlForm += '<span class="decision-tree-note">' + this._escapeHtml(question.upperNote) + '</span>';
            }
            if (question.url) {
                htmlForm += this._buildUrlsHTML(question.url);
            }

            return htmlForm;
        },

        /**
         * Build requirements HTML
         * @param {Array} requirements Array of requirement objects
         * @return {String} HTML string
         * @private
         */
        _buildRequirementsHTML: function(requirements) {
            var htmlForm = '<div class="decision-tree-requirements">';
            
            for (var r = 0; r < requirements.length; r++) {
                var req = requirements[r];
                var reqClass = "requirement-" + (req.type ? req.type.toLowerCase() : "info");
                
                htmlForm += '<div class="decision-tree-requirement ' + reqClass + '">';
                if (req.code) {
                    htmlForm += '<span class="requirement-code">' + this._escapeHtml(req.code) + '</span>: ';
                }
                htmlForm += '<span class="requirement-message">' + this._escapeHtml(req.message) + '</span>';
                htmlForm += '</div>';
            }
            
            htmlForm += '</div>';
            return htmlForm;
        },

        /**
         * Build URLs HTML
         * @param {String|Array} urls URL or array of URLs
         * @return {String} HTML string
         * @private
         */
        _buildUrlsHTML: function(urls) {
            var htmlForm = "";
            var urlArray = YAHOO.lang.isArray(urls) ? urls : [urls];
            var linkTitle = this.msg("link.title.open-link");
            var iconSrc = Alfresco.constants.URL_RESCONTEXT + 'components/images/link-16.png';

            for (var z = 0; z < urlArray.length; z++) {
                htmlForm += '<span class="decision-tree-url">' +
                           '<a title="' + this._escapeHtml(linkTitle) + '" href="' + this._escapeHtml(urlArray[z]) + '">' +
                           '<img src="' + iconSrc + '" alt="Link" />' + this._escapeHtml(urlArray[z]) + '</a></span>';
            }

            return htmlForm;
        },

        /**
         * Build choice HTML (radio, checkbox, select)
         * @param {Object} question Question configuration
         * @param {Object} choice Choice configuration
         * @return {String} HTML string
         * @private
         */
        _buildChoiceHTML: function(question, choice) {
            if (choice.list) {
                return this._buildListChoiceHTML(question, choice);
            } else if (choice.label !== "hidden") {
                return this._buildRadioChoiceHTML(question, choice);
            }
            return "";
        },

        /**
         * Build list choice HTML (select or checkboxes)
         * @param {Object} question Question configuration
         * @param {Object} choice Choice configuration
         * @return {String} HTML string
         * @private
         */
        _buildListChoiceHTML: function(question, choice) {
            var htmlForm = "";
            var listOption = this.getCurrentListOptions(question.id, choice.id);
            var msgKey = choice.id === "-" ? "form.control.decision-tree.empty" : 
                        "form.control.decision-tree." + this.options.prefix + "." + question.id + "." + choice.id;

            if (choice.multiple && choice.checkboxes) {
                htmlForm += this._buildCheckboxesHTML(question, choice, listOption);
            } else {
                if (!choice.checkboxes) {
                    htmlForm += '<p class="form-field">';
                }
                
                if (choice.label !== "hidden") {
                    var label = choice.label || this.msg(msgKey);
                    // Ajout de l'indicateur si la question est obligatoire
                    var mandatoryIndicator = (question.mandatory && !choice.checkboxes) ? '<span id="' + this.id + '-mandatory_' + question.id + '_' + choice.id + '" class="mandatory-indicator" title="' + this._escapeHtml(this.msg("form.field.incomplete")) + '">*</span>' : "";
                    
                    htmlForm += '<label for="' + this.id + '-select_' + question.id + '_' + choice.id + '">' + 
                               this._escapeHtml(label) + mandatoryIndicator + '</label>';
                }
                
                htmlForm += this._buildSelectHTML(question, choice, listOption);
                
                if (!choice.checkboxes) {
                    htmlForm += "</p>";
                }
            }

            return htmlForm;
        },

        /**
         * Build checkboxes HTML
         * @param {Object} question Question configuration
         * @param {Object} choice Choice configuration
         * @param {String} listOption Current selected values
         * @return {String} HTML string
         * @private
         */
        _buildCheckboxesHTML: function(question, choice, listOption) {
            var htmlForm = "";
            var selectedValues = listOption ? listOption.split(",") : [];

            for (var z = 0; z < choice.list.length; z++) {
                var item = this._parseListItem(choice.list[z]);
                var isSelected = this._isValueSelected(item.value, selectedValues);
                var checkboxId = 'checkbox-' + this.id + question.id + '_' + choice.id + '_' + z;

                htmlForm += '<p class="form-field">' +
                           '<input type="checkbox" id="' + checkboxId + '" ' +
                           'name="--group_' + this.id + question.id + '_' + choice.id + '" ' +
                           (this.options.disabled ? 'disabled ' : '') +
                           'tabindex="0" class="' + this.QUESTION_EVENTCLASS + '" ' +
                           'value="' + this._escapeHtml(item.value) + '" ' +
                           (isSelected ? 'checked="checked" ' : '') + '/>' +
                           '<label for="' + checkboxId + '">' + this._escapeHtml(item.label) + '</label>' +
                           '</p>';
            }

            return htmlForm;
        },

        /**
         * Build select HTML
         * @param {Object} question Question configuration
         * @param {Object} choice Choice configuration
         * @param {String} listOption Current selected values
         * @return {String} HTML string
         * @private
         */
        _buildSelectHTML: function(question, choice, listOption) {
            var htmlForm = '<select ' +
                          (choice.multiple ? 'multiple="multiple" ' : '') +
                          (this.options.disabled ? 'disabled ' : '') +
                          'tabindex="0" id="' + this.id + '-select_' + question.id + '_' + choice.id + '" ' +
                          'class="' + this.LIST_EVENTCLASS + '" ' +
                          'name="--group_' + this.id + question.id + '_' + choice.id + '">';

            var selectedValues = choice.multiple ? (listOption || "").split(",") : [listOption];

            for (var z = 0; z < choice.list.length; z++) {
                var item = this._parseListItem(choice.list[z]);
                var isSelected = this._isValueSelected(item.value, selectedValues);

                htmlForm += '<option value="' + this._escapeHtml(item.value) + '" ' +
                           (isSelected ? 'selected="selected" ' : '') + '>' +
                           this._escapeHtml(item.label) + '</option>';
            }

            htmlForm += '</select>';
            return htmlForm;
        },

        /**
         * Build radio choice HTML
         * @param {Object} question Question configuration
         * @param {Object} choice Choice configuration
         * @return {String} HTML string
         * @private
         */
        _buildRadioChoiceHTML: function(question, choice) {
            var checked = this.getCurrentValueChecked(question.id, choice.id);
            var msgKey = choice.id === "-" ? "form.control.decision-tree.empty" : 
                        "form.control.decision-tree." + this.options.prefix + "." + question.id + "." + choice.id;
            var label = choice.label || this.msg(msgKey);

            return '<p class="form-field">' +
                   '<input ' + (this.options.disabled ? 'disabled ' : '') +
                   'tabindex="0" id="' + this.id + '-choice_' + question.id + '_' + choice.id + '" ' +
                   'class="' + this.QUESTION_EVENTCLASS + '" ' +
                   'name="--group_' + this.id + question.id + '" type="radio" ' +
                   (checked ? 'checked="checked" ' : '') + '/>' +
                   '<label for="' + this.id + '-choice_' + question.id + '_' + choice.id + '">' +
                   this._escapeHtml(label) + '</label>' +
                   '</p>';
        },

        /**
         * Build message HTML for questions without choices
         * @param {Object} question Question configuration
         * @return {String} HTML string
         * @private
         */
        _buildMessageHTML: function(question) {
            var label = question.label || this.msg("form.control.decision-tree." + this.options.prefix + "." + question.id + ".label");
            return '<div id="' + this.id + '-question_' + question.id + '" class="hidden decision-tree-message">' +
                   '<span>' + this._escapeHtml(label) + '</span>' +
                   '</div>';
        },

        /**
         * Execute all queued after-form-init functions
         * @private
         */
        _executeAfterFormInitStack: function() {
            for (var i = 0; i < this.afterFormInitStack.length; i++) {
                try {
                    this.afterFormInitStack[i]();
                } catch (e) {
                    YAHOO.log("Error executing after form init function: " + e.message, "error");
                }
            }
            this.afterFormInitStack = [];
        },

        /**
         * Setup event handlers
         * @private
         */
        _setupEventHandlers: function() {
            var me = this;

            // Setup change listeners for select elements
            for (var i = 0; i < this.options.data.length; i++) {
                var question = this.options.data[i];
                if (question.choices) {
                    for (var j = 0; j < question.choices.length; j++) {
                        var choice = question.choices[j];
                        if (choice.list && !choice.checkboxes) {
                            var selectId = this.id + '-select_' + question.id + '_' + choice.id;
                            Event.addListener(selectId, "change", function() {
                                me.toggleVisible();
                            });
                        }
                    }
                }
            }

            var fnOnSelectChoice = function(layer, args) {
                var owner = Bubbling.getOwnerByTagName(args[1].input, "input");
                if (owner) {
                    if (owner.type !== "checkbox") {
                        var previousState = owner.previousState;
                        owner.checked = !previousState;
                        owner.previousState = owner.checked;
                    }
                    me.toggleVisible();
                    return false;
                }
            };

            Bubbling.addDefaultAction(this.QUESTION_EVENTCLASS, fnOnSelectChoice);
        },

        /**
         * Parse list item with format "value|label" or just "value"
         * @param {String} item List item string
         * @return {Object} Object with value and label properties
         * @private
         */
        _parseListItem: function(item) {
            if (item.indexOf('|') > 0) {
                var parts = item.split('|');
                return { value: parts[0], label: parts[1] };
            }
            return { value: item, label: item };
        },

        /**
         * Check if a value is selected in an array of values
         * @param {String} value Value to check
         * @param {Array} selectedValues Array of selected values
         * @return {Boolean} True if value is selected
         * @private
         */
        _isValueSelected: function(value, selectedValues) {
            for (var i = 0; i < selectedValues.length; i++) {
                if (selectedValues[i] === value) {
                    return true;
                }
            }
            return false;
        },

        /**
         * Escape HTML special characters
         * @param {String} str String to escape
         * @return {String} Escaped string
         * @private
         */
        _escapeHtml: function(html) {
            if (!html) return '';
                
                // Create a temporary div
                var div = document.createElement('div');
                div.innerHTML = html;
                
                // Allow these tags
                var allowedTags = {
                    'b': {},
                    'i': {},
                    'u': {},
                    'em': {},
                    'strong': {},
                    'br': {},
                    'p': {},
                    'ul': {},
                    'ol': {},
                    'li': {},
                    'span': ['style'],
                    'a': ['href', 'title', 'target']
                };
                
                // Remove scripts and other potentially dangerous elements
                var scripts = div.getElementsByTagName('script');
                while (scripts[0]) {
                    scripts[0].parentNode.removeChild(scripts[0]);
                }
                
                // Process all elements
                var all = div.getElementsByTagName('*');
                for (var i = 0; i < all.length; i++) {
                    var el = all[i];
                    var tagName = el.nodeName.toLowerCase();
                    
                    // If tag is not allowed, replace with its contents
                    if (!allowedTags[tagName]) {
                        var parent = el.parentNode;
                        while (el.firstChild) {
                            parent.insertBefore(el.firstChild, el);
                        }
                        parent.removeChild(el);
                        i--; // Adjust index after removal
                        continue;
                    }
                    
                    // Remove disallowed attributes
                    var attrs = el.attributes;
                    for (var j = attrs.length - 1; j >= 0; j--) {
                        var attrName = attrs[j].name.toLowerCase();
                        if (allowedTags[tagName].indexOf(attrName) === -1) {
                            el.removeAttribute(attrName);
                        }
                    }
                }
                
                return div.innerHTML;
        },

        /**
         * Refresh decision tree visibility
         */
        refreshDecisionTree: function() {
            this.toggleVisible();
        },

        /**
         * Check if a specific choice is currently selected
         * @param {String} qid Question ID
         * @param {String} cid Choice ID
         * @return {Boolean} True if choice is selected
         */
        getCurrentValueChecked: function(qid, cid) {
            for (var i = 0; i < this.options.currentValue.length; i++) {
                var current = this.options.currentValue[i];
                if (qid === current.qid && cid === current.cid) {
                    return true;
                }
            }
            return false;
        },

        /**
         * Get current list options for a specific question/choice
         * @param {String} qid Question ID
         * @param {String} cid Choice ID
         * @return {String} Comma-separated list of selected options
         */
        getCurrentListOptions: function(qid, cid) {
            for (var i = 0; i < this.options.currentValue.length; i++) {
                var current = this.options.currentValue[i];
                if (qid === current.qid && cid === current.cid) {
                    return current.listOptions || "";
                }
            }
            return "";
        },

        /**
         * Get current comment value for a question
         * @param {String} qid Question ID
         * @return {String} Comment text
         */
        getCurrentValueComment: function(qid) {
            for (var i = 0; i < this.options.currentValue.length; i++) {
                var current = this.options.currentValue[i];
                if (qid === current.qid && current.comment) {
                    return current.comment;
                }
            }
            return "";
        },

        /**
         * Handle form runtime initialization
         * @param {Object} layer Event layer
         * @param {Array} args Event arguments
         */
        onAfterFormRuntimeInit: function(layer, args) {
            if (!this.options.disabled && !this.formRuntime && 
                this.id.indexOf(args[1].runtime.formId.replace("-form", "")) > -1) {
                
                this.formRuntime = args[1].runtime;
            }
            this.toggleVisible();
        },

        /**
         * Toggle visibility of questions based on current selections
         */
        toggleVisible: function() {
            var result = [];
            var visible = [];
            var me = this;

           

            // Determine visible questions
            for (var i = 0; i < this.options.data.length; i++) {
                var question = this.options.data[i];
                
                // Show first question or questions marked as start
                if ((i === 0 || question.start === true) && 
                    (!this.options.disabled || this.options.currentValue.length > 0 || !question.choices)) {
                    visible.push(question.id);
                }

                if (this._isQuestionVisible(visible, question.id) && question.choices) {
                    this._processQuestionChoices(question, visible, result);
                } else if (question.choices && this.formRuntime && question.mandatory) {
                    this._removeValidationsForQuestion(question);
                }

                // Update DOM visibility
                var questionEl = Dom.get(this.id + "-question_" + question.id);
                if (questionEl) {
                    if (this._isQuestionVisible(visible, question.id)) {
                        Dom.removeClass(questionEl, "hidden");
                    } else {
                        Dom.addClass(questionEl, "hidden");
                    }
                }
            }

            this._updateHiddenInput(result);

            if (!this.options.disabled) {
                Bubbling.fire("mandatoryControlValueUpdated");
            }
        },

        /**
         * Check if question is visible
         * @param {Array} visible Array of visible question IDs
         * @param {String} questionId Question ID to check
         * @return {Boolean} True if question is visible
         * @private
         */
        _isQuestionVisible: function(visible, questionId) {
            return beCPG.util.contains(visible, questionId);
        },

        /**
         * Process choices for a visible question
         * @param {Object} question Question configuration
         * @param {Array} visible Array of visible question IDs
         * @param {Array} result Result array to populate
         * @private
         */
        _processQuestionChoices: function(question, visible, result) {
            var showComment = false;
            var me = this;

            for (var j = 0; j < question.choices.length; j++) {
                var choice = question.choices[j];
                
                this._handleValidation(question, choice);

                var choiceData = this._getChoiceData(question, choice);
                if (choiceData.isSelected) {
                    if (choiceData.value !== null) {
                        result.push({
                            qid: question.id,
                            cid: choice.id,
                            listOptions: choiceData.value
                        });
                    } else {
                        result.push({ qid: question.id, cid: choice.id });
                    }

                    // Add child questions to visible list ONLY if choice is actually selected
                    if (choice.cid && choiceData.showVisible) {
                        if (YAHOO.lang.isArray(choice.cid)) {
                            for (var z = 0; z < choice.cid.length; z++) {
                                visible.push(choice.cid[z]);
                            }
                        } else {
                            visible.push(choice.cid);
                        }
                    }

                    // Handle comments
                    if (choice.comment) {
                        this.insertComment(question.id, choice);
                        showComment = true;
                        this._updateCommentLabel(question.id, choice);
                    }
                }
            }

            this._handleCommentVisibility(question.id, showComment, result);
        },

        /**
         * Get choice data (selected state and value)
         * @param {Object} question Question configuration
         * @param {Object} choice Choice configuration
         * @return {Object} Object with isSelected, showVisible, and value properties
         * @private
         */
        _getChoiceData: function(question, choice) {
            if (choice.list) {
                return this._getListChoiceData(question, choice);
            } else {
                var element = Dom.get(this.id + "-choice_" + question.id + '_' + choice.id);
                var isChecked = element && element.checked;
                var isHidden = choice.label === "hidden";
                
                return {
                    isSelected: isHidden || isChecked,
                    showVisible: isHidden || isChecked, // Only show children if actually selected
                    value: null
                };
            }
        },

        /**
         * Get list choice data
         * @param {Object} question Question configuration
         * @param {Object} choice Choice configuration
         * @return {Object} Object with isSelected, showVisible, and value properties
         * @private
         */
        _getListChoiceData: function(question, choice) {
            var value = "";
            var showVisible = false;

            if (choice.multiple && choice.checkboxes) {
                var checkboxes = document.getElementsByName('--group_' + this.id + question.id + '_' + choice.id);
                for (var k = 0; k < checkboxes.length; k++) {
                    if (checkboxes[k].checked) {
                        if (value.length > 0) {
                            value += ",";
                        }
                        value += checkboxes[k].value;
                        showVisible = true;
                    }
                }
            } else {
                var selectElem = Dom.get(this.id + "-select_" + question.id + '_' + choice.id);
                if (selectElem) {
                    if (choice.multiple) {
                        for (var j = 0; j < selectElem.options.length; j++) {
                            if (selectElem.options[j].selected) {
                                if (value.length > 0) {
                                    value += ",";
                                }
                                value += selectElem.options[j].value;
                            }
                        }
                        showVisible = selectElem.selectedIndex > -1;
                    } else {
                        value = selectElem.value || "";
                        showVisible = value !== "";
                    }
                }
            }

            return {
                isSelected: choice.checkboxes || showVisible,
                showVisible: showVisible,
                value: value
            };
        },

        /**
         * Handle validation for question choices
         * @param {Object} question Question configuration
         * @param {Object} choice Choice configuration
         * @private
         */
        _handleValidation: function(question, choice) {
            if (!this.formRuntime || !question.mandatory) {
                return;
            }

            // Mandatory select (single or multiple, without explicit checkboxes rendering)
            if (choice.list && !choice.checkboxes) {
                if (!choice.hasValidation) {
                    choice.hasValidation = true;
                    var selectId = this.id + '-select_' + question.id + '_' + choice.id;
                    this.formRuntime.addValidation(
                        selectId,
                        Alfresco.forms.validation.mandatory,
                        { validationType: "mandatory" },
                        "keyup"
                    );
                }

            // Mandatory checkbox group (at least one item selected)
            } else if (choice.list && choice.checkboxes) {
                if (!choice.hasValidation) {
                    choice.hasValidation = true;

                    var groupName = '--group_' + this.id + question.id + '_' + choice.id;
                    var fieldsetId = this.id + '-question_' + question.id;

                    this.formRuntime.addValidation(
                        fieldsetId,
                        Alfresco.forms.validation.decisionTreeCheckboxGroup,
                        {
                            groupName: groupName,
                            validationType: "mandatory"
                        },
                        "click"
                    );
                }

            // Mandatory radio choice
            } else if (choice.label !== "hidden" && !choice.checkboxes && !choice.hasValidation) {
                choice.hasValidation = true;
                var radioId = this.id + '-choice_' + question.id + '_' + choice.id;
                this.formRuntime.addValidation(
                    radioId,
                    Alfresco.forms.validation.mandatory,
                    { validationType: "mandatory" },
                    "keyup"
                );
            }

            // Handle comment validation
            if (choice.comment) {
                var needsCommentValidation = choice.label === "hidden" || 
                                           (choice.list && choice.checkboxes) || 
                                           this._isChoiceSelected(question.id, choice.id);

                if (needsCommentValidation && !choice.hasCommentValidation) {
                    choice.hasCommentValidation = true;
                    var commentId = this.id + "-comment_" + question.id + "-input";
                    this.formRuntime.addValidation(
                        commentId,
                        Alfresco.forms.validation.mandatory,
                        null,
                        "keyup"
                    );
                } else if (!needsCommentValidation && choice.hasCommentValidation) {
                    choice.hasCommentValidation = false;
                    this.formRuntime.removeValidation(this.id + "-comment_" + question.id + "-input");
                }
            }
        },

        /**
         * Check if a choice is selected
         * @param {String} questionId Question ID
         * @param {String} choiceId Choice ID
         * @return {Boolean} True if choice is selected
         * @private
         */
        _isChoiceSelected: function(questionId, choiceId) {
            var element = Dom.get(this.id + "-choice_" + questionId + '_' + choiceId);
            return element && element.checked;
        },

        /**
         * Remove validations for a question
         * @param {Object} question Question configuration
         * @private
         */
        _removeValidationsForQuestion: function(question) {
            if (!this.formRuntime) {
                return;
            }

            for (var j = 0; j < question.choices.length; j++) {
                var choice = question.choices[j];

                if (choice.list && choice.checkboxes && choice.hasValidation) {
                    choice.hasValidation = false;
                    var fieldsetId = this.id + '-question_' + question.id;
                    if (this.formRuntime.removeValidation) {
                        this.formRuntime.removeValidation(fieldsetId);
                    }
                } else if (choice.list && !choice.checkboxes && choice.hasValidation) {
                    choice.hasValidation = false;
                    this.formRuntime.removeValidation(this.id + '-select_' + question.id + '_' + choice.id);
                } else if (choice.label !== "hidden" && choice.hasValidation) {
                    choice.hasValidation = false;
                    this.formRuntime.removeValidation(this.id + '-choice_' + question.id + '_' + choice.id);
                }

                if (choice.comment && choice.hasCommentValidation) {
                    choice.hasCommentValidation = false;
                    this.formRuntime.removeValidation(this.id + "-comment_" + question.id + "-input");
                }
            }
        },

        /**
         * Update comment label based on choice configuration
         * @param {String} questionId Question ID
         * @param {Object} choice Choice configuration
         * @private
         */
        _updateCommentLabel: function(questionId, choice) {
            if (!choice.commentLabel) {
                return;
            }

            var labelEl = Dom.get(this.id + '-comment_' + questionId + '-label');
            if (labelEl) {
                if (choice.commentLabel === "hidden") {
                    labelEl.innerHTML = "";
                } else {
                    labelEl.innerHTML = this._escapeHtml(choice.commentLabel);
                }
            }
        },

        /**
         * Handle comment visibility and event binding
         * @param {String} questionId Question ID
         * @param {Boolean} showComment Whether to show comment
         * @param {Array} result Result array to update
         * @private
         */
        _handleCommentVisibility: function(questionId, showComment, result) {
            var commentEl = Dom.get(this.id + "-comment_" + questionId);
            if (!commentEl) {
                return;
            }

            var inputEl = Dom.get(this.id + "-comment_" + questionId + "-input");
            if (inputEl) {
                Event.purgeElement(inputEl);
            }

            if (showComment) {
                Dom.removeClass(commentEl, "hidden");
                if (inputEl && result.length > 0) {
                    result[result.length - 1].comment = inputEl.value || "";
                    
                    var me = this;
                    Event.addListener(inputEl, "blur", function() {
                        me.toggleVisible();
                    });
                }
            } else {
                Dom.addClass(commentEl, "hidden");
            }
        },

        /**
         * Update the hidden input with current form state
         * @param {Array} result Current form state
         * @private
         */
        _updateHiddenInput: function(result) {
            var hiddenInput = Dom.get(this.fieldId);
            if (hiddenInput) {
                try {
                    hiddenInput.value = JSON.stringify(result);
                } catch (e) {
                    YAHOO.log("Error serializing decision tree data: " + e.message, "error");
                    hiddenInput.value = "[]";
                }
            }
        },

        /**
         * Insert comment input field
         * @param {String} questionId Question ID
         * @param {Object} choice Choice configuration
         */
        insertComment: function(questionId, choice) {
            if (!choice) {
                return;
            }

            var commentId = this.id + '-comment_' + questionId;
            var inputId = commentId + '-input';
            var labelId = commentId + '-label';
            var container = Dom.get(commentId);

            if (!container) {
                return;
            }

            // Check if comment already exists to avoid duplicates
            var existingInput = Dom.get(inputId);
            if (existingInput && existingInput.getAttribute('data-choice-id') === choice.id) {
                return;
            }

            var htmlForm = this._buildCommentHTML(questionId, choice, inputId, labelId);
            container.innerHTML = htmlForm;
        },

        /**
         * Build comment input HTML
         * @param {String} questionId Question ID
         * @param {Object} choice Choice configuration
         * @param {String} inputId Input element ID
         * @param {String} labelId Label element ID
         * @return {String} HTML string
         * @private
         */
        _buildCommentHTML: function(questionId, choice, inputId, labelId) {
            var htmlForm = "";
            var currentValue = this.getCurrentValueComment(questionId);

            // Add label if not hidden
            if (choice.label && choice.label !== "hidden") {
                var labelText = this.msg("form.control.decision-tree." + this.options.prefix + "." + questionId + ".comment");
                htmlForm += '<label id="' + labelId + '" for="' + inputId + '">' + 
                           this._escapeHtml(labelText) + ':</label>';
            }

            var baseAttrs = 'data-choice-id="' + this._escapeHtml(choice.id) + '" ' +
                           'tabindex="0" id="' + inputId + '" ' +
                           'class="' + this.COMMENT_EVENTCLASS + '" ' +
                           'name="--comment_' + this.id + questionId + '"';

            if (this.options.disabled) {
                htmlForm += '<span ' + baseAttrs + '>' + this._escapeHtml(currentValue) + '</span>';
            } else {
                htmlForm += this._buildCommentInputHTML(choice, baseAttrs, currentValue);
            }

            return htmlForm;
        },

        /**
         * Build comment input HTML based on type
         * @param {Object} choice Choice configuration
         * @param {String} baseAttrs Base HTML attributes
         * @param {String} currentValue Current value
         * @return {String} HTML string
         * @private
         */
        _buildCommentInputHTML: function(choice, baseAttrs, currentValue) {
            var escapedValue = this._escapeHtml(currentValue);

            if (choice.commentType === "textarea") {
                return '<textarea ' + baseAttrs + '>' + escapedValue + '</textarea>';
            }

            if (choice.commentType && INPUT_TYPES[choice.commentType]) {
                var inputConfig = INPUT_TYPES[choice.commentType];
                var inputAttrs = baseAttrs;

                for (var attrKey in inputConfig) {
                    if (inputConfig.hasOwnProperty(attrKey)) {
                        inputAttrs += ' ' + attrKey + '="' + this._escapeHtml(inputConfig[attrKey]) + '"';
                    }
                }

                inputAttrs += ' value="' + escapedValue + '"';
                return '<input ' + inputAttrs + ' />';
            }

            // Fallback to text input
            return '<input type="text" ' + baseAttrs + ' value="' + escapedValue + '" />';
        }

    }, true);

})();