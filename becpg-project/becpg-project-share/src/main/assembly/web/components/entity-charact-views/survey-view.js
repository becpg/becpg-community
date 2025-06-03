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
     * YUI Library aliases
     */
    var Bubbling = YAHOO.Bubbling;

    /**
     * Alfresco Slingshot aliases
     */
    var $isValueSet = Alfresco.util.isValueSet;

    var PREF_VIEW_MODE = "org.alfresco.share.project.survey.mode";


    /**
     * SurveyView constructor.
     * 
     * @param htmlId
     *            {String} The HTML id of the parent element
     * @return {beCPG.component.SurveyView} The new SurveyView instance
     * @constructor
     */
    beCPG.component.SurveyView = function(htmlId) {


        beCPG.component.SurveyView.superclass.constructor.call(this, htmlId);

        // Preferences service
        this.services.preferences = new Alfresco.service.Preferences();

        Bubbling.on("viewModeChange", this.onViewModeChange, this);

        Bubbling.on("beforeFormRuntimeInit", this.onBeforeFormRuntimeInit, this);

        return this;
    };


    /**
     * Extend from Alfresco.component.Base
     */
    YAHOO.extend(beCPG.component.SurveyView, beCPG.module.EntityDataGrid);


    /**
     * Augment prototype with main class implementation, ensuring overwrite is
     * enabled
     */
    YAHOO.lang.augmentObject(beCPG.component.SurveyView.prototype,
        {

            onReady: function SurveyView_onReady() {
                beCPG.component.SurveyView.superclass.onReady.call(this);
                if (this.options.viewMode == "survey") {
                    Dom.addClass(this.id + "-datagridBarBottom", "hidden");
                    Dom.addClass(this.id + "-itemSelect-div", "hidden");
                    Dom.addClass(this.id + "-grid", "hidden");
                    Dom.removeClass(this.id + "-survey-view", "hidden");
                    this.loadSurvey();
                }

            },


            onViewModeChange: function SurveyView_onViewModeChange() {
                if (this.options.viewMode == "dataTable") {
                    this.options.viewMode = "survey";
                }
                else {
                    this.options.viewMode = "dataTable";
                }
                this.services.preferences.set(PREF_VIEW_MODE, this.options.viewMode);
                this.refreshView();
            },

            refreshView: function SurveyView_refreshView() {

                if (this.options.viewMode != "survey") {
                    Dom.addClass(this.id + "-survey-view", "hidden");
                    Dom.removeClass(this.id + "-grid", "hidden");
                    Dom.removeClass(this.id + "-datagridBarBottom", "hidden");
                    Dom.removeClass(this.id + "-itemSelect-div", "hidden");
                    YAHOO.Bubbling.fire("refreshDataGrids", { updateOnly: true });
                } else {
                    Dom.addClass(this.id + "-datagridBarBottom", "hidden");
                    Dom.addClass(this.id + "-itemSelect-div", "hidden");
                    Dom.addClass(this.id + "-grid", "hidden");
                    Dom.removeClass(this.id + "-survey-view", "hidden");
                    this.loadSurvey();
                }

            },

            onBeforeFormRuntimeInit: function WizardMgr_onBeforeFormRuntimeInit(layer, args) {
                var runtime = args[1].runtime;
                // Only attach to our survey form (by htmlid or eventGroup)
                if (args[1].eventGroup && args[1].eventGroup.indexOf("survey-form") !== -1) {
                    runtime.setAJAXSubmit(true, {
                        successCallback: {
                            fn: function(response) {
                                Alfresco.util.PopupManager.displayMessage({
                                    text: this.msg("message.survey.success")
                                });
                                // Optionally, reload form or fire event
                            },
                            scope: this
                        },
                        failureCallback: {
                            fn: function(response) {
                                Alfresco.util.PopupManager.displayPrompt({
                                    title: this.msg("message.failure"),
                                    text: (response.json && response.json.message) ? response.json.message : "Error submitting the survey."
                                });
                            },
                            scope: this
                        }
                    });
                }
            },


            loadSurvey: function SurveyView_loadSurvey() {

                var url = YAHOO.lang.substitute(
                    Alfresco.constants.URL_SERVICECONTEXT + "components/survey/survey-form" +
                    "?list={list}&nodeRef={nodeRef}&itemType={itemType}&title={title}" + (this.options.readOnly ? "&mode={mode}" : ""),
                    {
                        nodeRef: this.options.entityNodeRef,
                        list: encodeURIComponent(this._getDataListName()),
                        itemType: encodeURIComponent(this._getItemType()),
                        title: encodeURIComponent(this._getDataListName()),
                        mode: this.options.readOnly ? "view" : undefined
                    });


                Alfresco.util.Ajax
                    .request(
                        {
                            url: url,
                            dataObj:
                            {
                                htmlid: this.id + "-survey-form"
                            },
                            successCallback:
                            {
                                fn: function(response) {
                                    var container = Dom.get(this.id + "-survey-view");
                                    container.innerHTML = response.serverResponse.responseText;
                                },
                                scope: this
                            },
                            scope: this,
                            execScripts: true
                        });
            }

        }, true);

})();
