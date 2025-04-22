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

        // Load the survey form on initialization
        this.loadSurvey();


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

                    Dom.addClass(this.id + "-survey", "hidden");
                    Dom.removeClass(this.id + "-grid", "hidden");
                    Dom.removeClass(this.id + "-datagridBarBottom", "hidden");
                    Dom.removeClass(this.id + "-itemSelect-div", "hidden");
                    YAHOO.Bubbling.fire("refreshFloatingHeader");

                } else {
                    Dom.addClass(this.id + "-datagridBarBottom", "hidden");
                    Dom.addClass(this.id + "-itemSelect-div", "hidden");
                    Dom.addClass(this.id + "-grid", "hidden");
                    Dom.removeClass(this.id + "-survey", "hidden");




                }

            },
            
           loadSurvey: function SurveyView_loadSurvey() {

                var url = YAHOO.lang.substitute(
                    Alfresco.constants.URL_SERVICECONTEXT + "components/survey/survey-form" +
                    "?list={list}&nodeRef={nodeRef}&itemType={itemType}&title={title}",
                    {
                        nodeRef: this.options.entityNodeRef,
                        list: this._getDataListName(),
                        itemType: this._getItemType(),
                        title: this._getDataListName()
                    });


                //Load survey : 

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
                                    // Correct selector for survey form container
                                    Dom.get(this.id + "-survey").innerHTML = response.serverResponse.responseText;
                                },
                                scope: this
                            },
                            scope: this,
                            execScripts: true
                        });

            }

        }, true);

})();
