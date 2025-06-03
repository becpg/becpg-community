/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG.
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
     * EntityCatalog constructor.
     * 
     * @param htmlId
     *            {String} The HTML id of the parent element
     * @return {beCPG.component.EntityCatalog} The new EntityCatalog
     *         instance
     * @constructor
     */
    beCPG.component.EntityCatalog = function(htmlId) {
        beCPG.component.EntityCatalog.superclass.constructor.call(this, "beCPG.component.EntityCatalog", htmlId, ["button", "container"]);
        return this;
    };

    /**
     * Extend from Alfresco.component.Base
     */
    YAHOO.extend(beCPG.component.EntityCatalog, Alfresco.component.Base);

    /**
     * Augment prototype with main class implementation, ensuring overwrite is
     * enabled
     */
    YAHOO.lang.augmentObject(beCPG.component.EntityCatalog.prototype, {
        /**
         * Object container for initialization options
         * 
         * @property options
         * @type object
         */
        options: {
            /**
             * Current entityNodeRef.
             * 
             * @property entityNodeRef
             * @type string
             * @default ""
             */
            entityNodeRef: "",

            catalogId: null
        },


        /**
         * Fired by YUI when parent element is available for scripting.
         * 
         * @method onReady
         */
        onReady: function EntityCatalog_onReady() {
            var instance = this;

            var catalogsDiv = YAHOO.util.Dom.get(this.id + "-entity-catalog");

            catalogsDiv.innerHTML = '<span class="wait">' + Alfresco.util.encodeHTML(this.msg("label.loading")) + '</span>';

            var formulateButton = YAHOO.util.Selector.query('div.formulate');

            if (formulateButton != null) {
                YAHOO.util.Dom.addClass(formulateButton, "loading");
            }


            var catalogUrl = Alfresco.constants.PROXY_URI + "becpg/entity/catalog/node/" + instance.options.entityNodeRef.replace(":/", "");
            if (instance.options.catalogId != null) {
                catalogUrl += "?catalogId=" + instance.options.catalogId;
            }

            Alfresco.util.Ajax.request({
                url: catalogUrl,
                method: Alfresco.util.Ajax.GET,
                responseContentType: Alfresco.util.Ajax.JSON,
                successCallback: {
                    fn: function(response) {

                        var alerts = [];
                        var protectedFields = [];
                        if (response.json && response.json.catalogs && Object.keys(response.json.catalogs).length > 0) {

                            var catalogs = response.json.catalogs;

                            // Collect protected fields from all catalogs
                            for (var key in catalogs) {
                                if (catalogs[key].protectedFields) {
                                    for (var i = 0; i < catalogs[key].protectedFields.length; i++) {
                                        var field = catalogs[key].protectedFields[i];
                                        // Only add if not already in the array
                                        if (protectedFields.indexOf(field) === -1) {
                                            protectedFields.push(field);
                                        }
                                    }
                                }
                            }

                            var html = "<div class=\"entity-catalog\">";
                            for (var key in catalogs) {
                                var score = catalogs[key].score;
                                var locale = catalogs[key].locale;
                                var label = catalogs[key].label;
                                var catalogId = catalogs[key].id;
                                var color = catalogs[key].color;


                                if (instance.options.catalogId == null || instance.options.catalogId == catalogId) {

                                    var modifiedDate = catalogs[key].modifiedDate;
                                    var country = null;

                                    if (locale !== undefined && locale != null) {

                                        country = locale.toLowerCase();
                                        if (locale.indexOf("_") > 0) {
                                            country = locale.split("_")[1].toLowerCase();
                                        }
                                    }

                                    html += "<div class=\"catalog " + (key == 0 ? "first-catalog" : "") + "\">";
                                    html += "<div class=\"catalog-header set-bordered-panel-heading\">";
                                    html += "<table><tr><td><span style=\"background-color: " + color + ";\" class=\"catalog-color\" ></span><span class=\"catalog-name\">" + instance.msg("label.catalog") + " " + label +
                                        (country != null ? "<img title=" + instance.msg("locale.name." + locale)
                                            + " src=\"/share/res/components/images/flags/" + country + ".png\">" : "") + "</span></td>";

                                    html += "<td><progress value=\"" + (score / 100) + "\">";
                                    //IE fix
                                    html += "<div class=\"progress-bar\">";
                                    html += "<span style=\"width: " + score + "%;\">" + score + "%</span>";
                                    html += "</div>";
                                    html += "</progress></td></tr><tr>";

                                    if (modifiedDate != null) {
                                        html += '<td><span class="date-info">';
                                        html += instance.msg("label.modifiedDate",
                                            Alfresco.util.relativeTime(Alfresco.util.fromISO8601(modifiedDate))
                                            + ' (' + Alfresco.util.formatDate(modifiedDate, instance.msg("date.format")) + ')');
                                        html += '</span></td>';
                                    } else {
                                        html += "<td></td>";
                                    }

                                    html += "<td><span class=\"score-info\">" + Math.floor(score) + " % " + instance.msg("label.completed") + "</span></td>";


                                    html += "</tr></table></div>";

                                    if (catalogs[key].missingFields !== undefined || catalogs[key].nonUniqueFields !== undefined) {
                                        html += "<div class=\"catalog-details\">";
                                    }


                                    var field, displayName;
                                    //display missing props, if any
                                    if (catalogs[key].missingFields !== undefined) {
                                        html += "<h3 >" + instance.msg("label.missing_properties") + "</h3>";
                                        html += "<ul class=\"catalog-missing-propList\">";
                                        for (field in catalogs[key].missingFields) {

                                            var flag = null;
                                            if (catalogs[key].missingFields[field].locale != null) {
                                                flag = catalogs[key].missingFields[field].locale.toLowerCase();
                                                if (catalogs[key].missingFields[field].locale.indexOf("_") > 0) {
                                                    flag = catalogs[key].missingFields[field].locale.split("_")[1].toLowerCase();
                                                }
                                            }

                                            displayName = catalogs[key].missingFields[field].displayName;
                                            if (catalogs[key].missingFields[field]["displayName_" + Alfresco.constants.JS_LOCALE]) {
                                                displayName = catalogs[key].missingFields[field]["displayName_" + Alfresco.constants.JS_LOCALE];
                                            }

                                            html += "<li class=\"missing-field\" >"
                                                + displayName +
                                                (flag != null ? "<img title=" + instance.msg("locale.name." + catalogs[key].missingFields[field].locale)
                                                    + " src=\"/share/res/components/images/flags/"
                                                    + flag + ".png\">" : "") + "</li>";
                                        }

                                        html += "</ul>";
                                    }


                                    //Non unique props

                                    if (catalogs[key].nonUniqueFields !== undefined) {
                                        html += "<h3>" + instance.msg("label.non-unique-properties") + "</h3>";

                                        html += "<ul class=\"catalog-missing-propList\">";
                                        for (field in catalogs[key].nonUniqueFields) {

                                            displayName = catalogs[key].nonUniqueFields[field].displayName;
                                            if (catalogs[key].nonUniqueFields[field]["displayName_" + Alfresco.constants.JS_LOCALE]) {
                                                displayName = catalogs[key].nonUniqueFields[field]["displayName_" + Alfresco.constants.JS_LOCALE];
                                            }

                                            html += "<li class=\"non-unique-field\" >"
                                                + displayName
                                                + "</li>";


                                            alerts.push(displayName);
                                        }

                                        html += "</ul>";
                                    }

                                    if (catalogs[key].missingFields !== undefined || catalogs[key].nonUniqueFields !== undefined) {
                                        html += "</div>";
                                    }
                                    html += "</div>";
                                }
                            }
                            html += "</div>";



                            catalogsDiv.innerHTML = html;



                            var insertId = this.id.replace("wizard-mgr", "%%%").replace("_cat", "")
                                .replace("-mgr", "").replace("%%%", "wizard-mgr");

                            var formId = insertId + "-form";

                            YAHOO.util.Event.onAvailable(formId, function() {

                                if (instance.id.indexOf("wizard-mgr") < 1) {
                                    var form = YAHOO.util.Dom.get(formId);

                                    if (form !== undefined && form != null) {

                                        var pageContent = YAHOO.util.Dom.get(insertId);
                                        YAHOO.util.Dom.addClass(pageContent, "inline-block");
                                        YAHOO.util.Dom.addClass(catalogsDiv, "inline-block");
                                        YAHOO.util.Dom.addClass(catalogsDiv, "catalogs");
                                        YAHOO.util.Dom.insertAfter(catalogsDiv, pageContent);
                                        YAHOO.util.Dom.removeClass(instance.id + "-entity-catalog", "hidden");

                                        instance.addProtectedFields(insertId, protectedFields);
                                    }


                                }
                                instance.colorizeMissingFields(response.json, insertId);

                            }, this);


                            if (alerts.length > 0) {
                                var uniqueAlerts = alerts.filter(function(item, pos) {
                                    return alerts.indexOf(item) == pos;
                                })

                                Alfresco.util.PopupManager.displayPrompt({
                                    title: instance.msg("label.non-unique-properties"),
                                    text: uniqueAlerts.join(", ")
                                });
                            }

                        } else {
                            catalogsDiv.innerHTML = "<span class=\"no-missing-prop\">" + instance.msg("label.no_missing_prop") + "</span>";
                        }
                        YAHOO.util.Dom.removeClass(formulateButton, "loading");
                    },
                    scope: instance
                },
                failureCallback: {
                    fn: function(response) {
                        if (response.json && response.json.message) {
                            Alfresco.util.PopupManager.displayPrompt({
                                title: this.msg("message.formulate.failure"),
                                text: response.json.message
                            });
                        } else {
                            Alfresco.util.PopupManager.displayMessage({
                                text: this.msg("message.formulate.failure")
                            });
                        }
                        YAHOO.util.Dom.removeClass(formulateButton, "loading");
                    },
                    scope: this
                },
                execScripts: true
            });
        },


        addProtectedFields: function(formId, fields) {
            if (!fields || fields.length === 0) {
                return;
            }

            // Store the protected fields in the component instance
            this.protectedFields = fields;

            // Initialize tracking objects
            this.modifiedProtectedFields = {};
            this.modifiedCount = 0;

            // Get form element
            var form = YAHOO.util.Dom.get(formId + "-form");
            if (!form) {
                return;
            }

            // Store form info
            this.submitButtonId = formId + "-form-submit-button";
            this.reauthButtonId = this.submitButtonId + "-form-reauth-button";

            // Make sure we use component methods for better maintainability
            var instance = this;

            /**
             * Updates the submit button state based on modified fields
             */
            this.createReauthButton = function() {

                var reauthButton = YAHOO.util.Dom.get(instance.reauthButtonId);
                if (reauthButton) {
                    return;
                }

                var submitButton = YAHOO.util.Dom.get(instance.submitButtonId);
                if (!submitButton) {
                    return;
                }

                var spanOuter = document.createElement("span");
                spanOuter.className = "yui-button yui-push-button";
                spanOuter.id = instance.reauthButtonId;

                var spanInner = document.createElement("span");
                spanInner.className = "first-child";

                var button = document.createElement("button");
                button.type = "button";
                button.tabIndex = 0;
                button.id = instance.reauthButtonId + "-button";
                button.innerText = instance.msg("button.reauth.save");

                button.onclick = function() {
                    // Show reauthentication popup
                    instance.openReauthPopup(function(token) {
                        if (token) {
                            submitButton.click();
                        } else {
                            Alfresco.util.PopupManager.displayMessage({
                                text: instance.msg("message.reauth.failed")
                            });
                        }
                    });
                };

                spanInner.appendChild(button);
                spanOuter.appendChild(spanInner);

                // Insert the reauth button next to the original button
                submitButton.parentNode.parentNode.insertBefore(spanOuter, submitButton.nextSibling);
                // If protected fields are modified, show reauth button
                submitButton.parentNode.style.display = 'none';
            };

            /**
             * Add field validations to detect changes in protected fields
             */
            this.setupFieldValidations = function() {


                // Setup validations for each protected field
                this.protectedFields.forEach(function(field) {
                    // Common field ID patterns
                    var fieldPatterns = [
                        formId + "_prop_" + field.replace(":", "_") + "-entry",
                        formId + "_assoc_" + field.replace(":", "_") + "-cntrl",
                        formId + "_prop_" + field.replace(":", "_")

                    ];

                    // Check each possible field ID
                    for (var i = 0; i < fieldPatterns.length; i++) {
                        var fieldId = fieldPatterns[i];
                        var element = YAHOO.util.Dom.get(fieldId);
                        if (element) {
                            // Add direct change event handler
                            YAHOO.util.Event.addListener(element, "change", function() {
                                instance.createReauthButton();
                            });
                        }
                    }
                });

                return true;
            };

            // Initialize the component
            YAHOO.util.Event.onAvailable(this.submitButtonId, function() {
                instance.setupFieldValidations();

            }, this);


        },

        openReauthPopup: function(callback) {
            var redirectUri = window.location.origin + "/share/page/reauth-callback";
            var aimsLoginUrl = Alfresco.constants.URL_CONTEXT + "page/aims-login?prompt=true&redirectUrl=" + encodeURIComponent(redirectUri);

            // Clear any existing message listeners to prevent duplicates
            if (this._reauthMessageHandler) {
                window.removeEventListener("message", this._reauthMessageHandler);
            }

            var popup = window.open(aimsLoginUrl, "ReauthPopup", "width=600,height=500,menubar=no,location=no,resizable=yes,scrollbars=yes,status=no");

            if (!popup || popup.closed || typeof popup.closed === 'undefined') {
                Alfresco.util.PopupManager.displayMessage({
                    text: me.msg("alert.popup.blocked")
                });
                callback(null);
                return;
            }

            var messageReceived = false;
            var popupCheckInterval;
            var self = this;

            // Check if popup is closed by user
            popupCheckInterval = setInterval(function() {
                if (popup.closed) {
                    clearInterval(popupCheckInterval);
                    if (!messageReceived) {
                        // If we get here, the popup was closed without sending a message
                        window.removeEventListener("message", self._reauthMessageHandler);
                        callback(null);
                    }
                }
            }, 500);

            // Handle the message from the popup
            this._reauthMessageHandler = function(event) {
                // Ignore messages from other origins
                if (event.origin !== window.location.origin) {
                    return;
                }

                // Only process messages with our specific source
                if (!event.data || typeof event.data !== 'object' || event.data.source !== 'beCPG-reauth') {
                    return; // Not our message
                }

                messageReceived = true;
                clearInterval(popupCheckInterval);

                // Only process our specific success code
                if (event.data.code === 'REAUTH_SUCCESS') {
                    // Success - pass the code to the callback
                    callback(event.data.code);
                } else {
                    // No valid code in the message - authentication likely failed
                    callback(null);
                }

                // Clean up
                window.removeEventListener("message", self._reauthMessageHandler);

                // Close the popup if it's still open
                if (popup && !popup.closed) {
                    popup.close();
                }
            };

            // Add the event listener
            window.addEventListener("message", this._reauthMessageHandler);

            // Set focus to the popup
            try {
                popup.focus();
            } catch (e) {
                // Ignore focus errors
            }
        },
        /**
         * Colorizes input fields using a color palette per catalog in json
         * json : catalogs
         * id : radical id of inputs (eg : $id_prop_bcpg_legalName)
         */
        colorizeMissingFields: function(json, id) {

            if (json.catalogs !== undefined && json.catalogs != null && Object.keys(json.catalogs).length > 0) {
                var instance = this;

                var catalogs = json.catalogs;

                for (var key in catalogs) {

                    if (catalogs[key].missingFields !== undefined) {

                        //put a color tip for this catalog
                        var catalogId = catalogs[key].id;


                        if (instance.options.catalogId == null || instance.options.catalogId == catalogId) {

                            var color = catalogs[key].color;

                            var colorTipElement = document.createElement("SPAN");
                            colorTipElement.style.backgroundColor = color;
                            colorTipElement.className += "catalog-color";
                            colorTipElement.title = instance.msg("label.catalog") + " '" + catalogs[key].label + (catalogs[key].locale !== undefined && catalogs[key].locale.length == 1 ? "(" + catalogs[key].locale + ")'" : "'");


                            var locale = catalogs[key].locale;

                            if (locale !== undefined && locale != null) {
                                catalogId = catalogId + "_" + locale;
                            }


                            if (catalogs[key].missingFields.length > 0) {
                                var label = YAHOO.util.Dom.get(instance.id + "_" + catalogId + "_missingPropLabel");

                                if (label !== undefined && label != null) {
                                    label.parentNode.insertBefore(colorTipElement.cloneNode(false), label.nextSibling);
                                }
                            }

                            //put color tip next to each non validated field according to the catalog
                            for (var field in catalogs[key].missingFields) {
                                //try to find a prop or assoc with this field

                                var fieldArray = new Array();
                                var fieldCode = catalogs[key].missingFields[field].id;

                                if (fieldCode.indexOf("|") > -1) {
                                    fieldArray = fieldCode.split("|");
                                } else {
                                    fieldArray.push(fieldCode);
                                }

                                for (var subField in fieldArray) {

                                    var curField = fieldArray[subField].replace(":", "_");
                                    var fieldId = id + "_assoc_" + curField + "-cntrl";

                                    var found = YAHOO.util.Dom.get(fieldId);

                                    if (found === undefined || found == null) {
                                        fieldId = id + "_prop_" + curField + "-entry";
                                        found = YAHOO.util.Dom.get(fieldId);

                                    }

                                    if (found === undefined || found == null) {
                                        fieldId = id + "_prop_" + curField;
                                        found = YAHOO.util.Dom.get(fieldId);
                                    }

                                    if (found !== undefined && found != null) {
                                        if (found.className.indexOf("multi-assoc") != -1) {
                                            found = found.parentNode;
                                        }

                                        //put color tip
                                        var labels = document.getElementsByTagName("label");


                                        for (var labelIndex = 0; labelIndex < labels.length; labelIndex++) {
                                            var currentLabel = labels[labelIndex];

                                            //checks if we're on the right label, and the catalog is not already labelled
                                            if (currentLabel.htmlFor == fieldId) {
                                                var hasLocaleIcon = false;
                                                if (currentLabel.parentNode.innerHTML.indexOf(colorTipElement.style.backgroundColor) == -1) {
                                                    if (currentLabel.childNodes) {
                                                        for (var child in currentLabel.childNodes) {
                                                            var currentChildNode = currentLabel.childNodes[child];
                                                            if (currentChildNode.nodeType == Node.ELEMENT_NODE && currentChildNode.className.indexOf("locale-icon") != -1) {
                                                                hasLocaleIcon = true;
                                                                break;
                                                            }
                                                        }
                                                    }

                                                    if (hasLocaleIcon) {
                                                        currentLabel.appendChild(colorTipElement.cloneNode(false));
                                                    } else {
                                                        currentLabel.innerHTML += colorTipElement.outerHTML;
                                                    }
                                                    break;
                                                }

                                            }
                                        }
                                    } else {

                                        var absentMissingFieldId = "missing-field_" + catalogs[key] + "_" + catalogs[key].missingFields[field].id;

                                        var absentMissingFieldHTMLElement = YAHOO.util.Dom.get(absentMissingFieldId);

                                        if (absentMissingFieldHTMLElement !== undefined && absentMissingFieldHTMLElement != null) {
                                            absentMissingFieldHTMLElement.outerHTML = "";
                                        }
                                    }

                                }
                            }
                        }
                    }
                }
            }
        }

    });
})();