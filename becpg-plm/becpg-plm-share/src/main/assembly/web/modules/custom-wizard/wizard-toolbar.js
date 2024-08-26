(function() {
    if (beCPG.component.WizardMgr) {

        YAHOO.Bubbling.fire("registerWizardToolbarButtonAction", {
            actionName: "product-notifications",
            evaluate: function(wizard) {
                return  wizard.options.requirements;
            },
            createWidget: function(containerDiv, instance,step) {

                var divEl = document.createElement("div");

                // Using native JS methods instead of YUI Dom methods
                divEl.setAttribute("id", instance.id + "-productNotifications");
                divEl.classList.add("product-notifications");

                containerDiv.appendChild(divEl);

                return new beCPG.component.ProductNotifications(instance.id + "-productNotifications").setOptions({
                    entityNodeRef: step.nodeRef,
                    list: instance.options.list,
                    containerDiv: divEl
                });
            },
            stepValidator: function(step) {
                Alfresco.util.Ajax.request({
                    method: Alfresco.util.Ajax.GET,
                    url: Alfresco.constants.PROXY_URI + "becpg/remote/formulate?nodeRef=" + encodeURIComponent(step.nodeRef) + "&chainId=fastFormulationChain&format=json",
                    responseContentType: Alfresco.util.Ajax.JSON,
                    successCallback: {
                        fn: function(response) {
                            // Handle the success response here, e.g., update UI or log success
                            console.log("Request was successful: ", response);
                        },
                        scope: this
                    },
                    failureCallback: {
                        fn: function(response) {
                            // Handle the failure response here, e.g., show error message
                            console.error("Request failed: ", response);
                        },
                        scope: this
                    }
                });
            }
        });

    }
})();
