/**
 * @module alfresco/services/EcmService
 * @extends module:alfresco/core/Core
 * @author Matthieu Laborie
 */
define([ "dojo/_base/declare", "alfresco/core/Core", "dojo/_base/lang", "dojo/sniff", "dijit/registry",
        "alfresco/core/CoreXhr", "alfresco/core/NotificationUtils", "alfresco/dialogs/AlfDialog",
        "alfresco/buttons/AlfButton" ], function(declare, AlfCore, lang, has, registry, AlfXhr, NotificationUtils,
        AlfDialog, AlfButton)
{

    return declare([ AlfCore, AlfXhr, NotificationUtils ],
    {

        /**
         * An array of the i18n files to use with this widget.
         * 
         * @instance
         * @type {object[]}
         * @default [{i18nFile: "./i18n/EcmService.properties"}]
         */
        i18nRequirements : [
        {
            i18nFile : "./i18n/EcmService.properties"
        } ],

        /**
         * Sets up the subscriptions for the EcmService
         * 
         * @instance
         * @param {array}
         *            args The constructor arguments.
         */
        constructor : function alfresco_services_EcmService__constructor(args)
        {
            lang.mixin(this, args);
            this.alfSubscribe("BECPG_ECM_AUTOMATIC_CHANGE", lang.hitch(this, "onEcmAutomaticChange"));

        },

        detailsDialog : null,

        createNewEcm : "BECPG_CREATE_AUTO_ECO",

        cancelAutoEcmDialog : "BECPG_CANCEL_AUTO_ECO_DIALOG",


        onEcmAutomaticChange : function alfresco_services_EcmService__onDetailsDialog(payload)
        {
          
                    if (this.detailsDialog == null)
                    {
                        this.alfSubscribe(this.createNewEcm, lang.hitch(this, "onCreateNewEcm"));
                        this.alfSubscribe(this.cancelAutoEcmDialog, lang.hitch(this, "onCreateNewEcmCancel"));
                        this.detailsDialog = new AlfDialog(
                        {
                            title : this.message("ecm.auto.dialog.create.title"),
                            widgetsContent : [
                            {
                                name : "alfresco/forms/controls/DojoValidationTextBox",
                                config :
                                {
                                    id : this.id + "_ECM_NAME",
                                    name : "name",
                                    label : this.message("ecm.auto.dialog.field.name.label"),
                                    description : this.message("ecm.auto.dialog.field.name.description")
                                }
                            } ],
                            widgetsButtons : [
                            {
                                name : "alfresco/buttons/AlfButton",
                                config :
                                {
                                    label : this.message("button.create-ecm"),
                                    publishTopic : this.createNewEcm,
                                    publishPayload : payload
                                }
                            },
                            {
                                name : "alfresco/buttons/AlfButton",
                                config :
                                {
                                    label : this.message("button.cancel-create-ecm"),
                                    publishTopic : this.cancelAutoEcmDialog,
                                    publishPayload : payload
                                }
                            } ]
                        });
                    }
                    this.detailsDialog.show();
   
        },

        onCreateNewEcm : function alfresco_services_EcmService__onPrefsUpdateSave(payload)
        {
            var ecmNameWidget = registry.byId(this.id + "_ECM_NAME");

            if (ecmNameWidget != null)
            {
                var ecmName = ecmNameWidget.getValue();

                if (ecmName != null && ecmName.length > 0)
                {
                    var url = Alfresco.constants.PROXY_URI + "becpg/ecm/changeorder/create";
                    this.serviceXhr(
                    {
                        url : url,
                        data :
                        {
                            name : ecmNameWidget.getValue()
                        },
                        method : "POST",
                        successCallback : this.ecmCreatedSuccess,
                        failureCallback : this.ecmCreatedFailure,
                        callbackScope : this
                    });

                }

            }

        },

        onCreateNewEcmCancel : function alfresco_services_EcmService__onPrefsUpdateCancel(payload)
        {
            var ecmNameWidget = registry.byId(this.id + "_ECM_NAME");
            if (ecmNameWidget != null)
            {
                var ecmName = ecmNameWidget.setValue("");
            }
        },

        ecmCreatedSuccess : function alfresco_services_EcmService__ecmCreatedSuccesss(response, originalRequestConfig)
        {

            if (typeof response == "string")
            {
                var response = JSON.parse(this.cleanupJSONResponse(response));
            }

            // Display a success message...
            this.displayMessage(this.message("message.ecm.created.success"));

            
            this.alfPublish("BECPG_ECM_CREATED_SUCCESS", response);
        },

        ecmCreatedFailure : function alfresco_services_EcmService__ecmCreatedFailure(response, originalRequestConfig)
        {
            if (typeof response == "string")
            {
                var response = JSON.parse(this.cleanupJSONResponse(response));
            }

            // Display a success message...
            this.displayMessage(this.message("message.ecm.created.failure"));

            this.alfPublish("BECPG_ECM_CREATED_FAILURE", response);
        }

    });
});
