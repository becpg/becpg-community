/*
 * Toolbar for Survey View - adds a toggle button to switch between data list and survey view
 */
(function() {
    if (beCPG.component.EntityDataListToolbar) {
        var PREF_VIEW_MODE = "org.alfresco.share.project.survey.mode";

        YAHOO.Bubbling.fire("registerToolbarButtonAction", {
            actionName: "toggle-survey-view",
            right: true,
            evaluate: function(asset, entity) {
                // Show the button for surveyList type
                return asset.name !== null && (asset.name.indexOf("View-survey") > -1 || asset.name.indexOf("surveyList") > -1 );
            },
            createWidget: function(containerDiv, instance) {
                var divEl = document.createElement("div");
                containerDiv.appendChild(divEl);
                Dom.addClass(divEl, "surveyToggleCheckbox");
                var surveyViewOn = "dataTable" != Alfresco.util.findValueByDotNotation(instance.services.preferences.get(), PREF_VIEW_MODE);
                var widget = new YAHOO.widget.Button({
                    type: "checkbox",
                    title: instance.msg("button.toggle-survey.description"),
                    container: divEl,
                    checked: surveyViewOn
                });
                widget.on("checkedChange", function() {
                    YAHOO.Bubbling.fire("viewModeChange");
                });
                return widget;
            }
        });
    }
})();
