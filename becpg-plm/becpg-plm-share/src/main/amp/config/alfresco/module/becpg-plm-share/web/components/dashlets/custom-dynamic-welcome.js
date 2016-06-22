// Declare namespace...
(function()
{

    /**
     * YUI Library aliases
     */
    var Event = YAHOO.util.Event;


    // Define constructor...
    beCPG.custom.DynamicWelcome = function CustomSearch_constructor(htmlId, dashboardUrl, dashboardType, site, siteTitle,
            docsEdition)
    {
        beCPG.custom.DynamicWelcome.superclass.constructor.call(this, htmlId, dashboardUrl, dashboardType, site, siteTitle,
                docsEdition);
        return this;
    };

    // Extend default Dynamic Welcome...
    YAHOO
            .extend(
                    beCPG.custom.DynamicWelcome,
                    Alfresco.dashlet.DynamicWelcome,
                    {
                        importEntity : null,
                        createProduct : null,
                        createProject : null,
                        userHomeNodeRef : null,

                        options :
                        {
                            userHomeNodeRef : null,
                            siteId : null
                        },

                        onReady : function DynamicWelcome_onReady()
                        {
                            // Listen on clicks for the create site link
                             this.widgets.hideButton = Alfresco.util.createYUIButton(this, "hide-button", this.onHideButtonClick);
//					         if (this.dashboardType == "user")
//					         {
//					            Event.addListener(this.id + "-get-started-panel-container", "click", function() {
//					               location.href = this.msg("welcome.user.clickable-content-link", this.docsEdition);
//					            }, this, true);
//					         }
					         Event.addListener(this.id + "-createSite-button", "click", this.onCreateSiteLinkClick, this, true);
					         Event.addListener(this.id + "-requestJoin-button", "click", this.onRequestJoinLinkClick, this, true);

                            // Custom
                            if (this.options.siteId != null)
                            {

                                Alfresco.util.Ajax
                                        .request(
                                        {
                                            url : Alfresco.constants.PROXY_URI + "slingshot/doclib/containers/" + this.options.siteId,
                                            method : "GET",
                                            successCallback :
                                            {
                                                fn : function(response)
                                                {

                                                    if (response.json)
                                                    {
                                                        for ( var i in response.json.containers)
                                                        {
                                                            if (response.json.containers[i].name === "documentLibrary")
                                                            {
                                                                this.options.userHomeNodeRef = response.json.containers[i].nodeRef;
                                                                break;
                                                            }
                                                        }

                                                        Event.addListener(this.id + "-createProduct-button", "click",
                                                                this.onCreateProductLinkClick, this, true);
                                                        Event.addListener(this.id + "-createProject-button", "click",
                                                                this.onCreateProjectLinkClick, this, true);
                                                        Event.addListener(this.id + "-importMP-button", "click",
                                                                this.onImportMPLinkClick, this, true);
                                                    }
                                                },
                                                scope : this
                                            },
                                            failureCallback :
                                            {
                                                fn : function(response)
                                                {
                                                   //Fix IE infinite loop : window.location.reload();
                                                    Alfresco.logger.error("Error retrieving documentLibrary: ", response);
                                                },
                                                scope : this
                                            }
                                        });
                            }
                            else
                            {

                                Event.addListener(this.id + "-createProduct-button", "click",
                                        this.onCreateProductLinkClick, this, true);
                                Event.addListener(this.id + "-createProject-button", "click",
                                        this.onCreateProjectLinkClick, this, true);
                                Event.addListener(this.id + "-importMP-button", "click", this.onImportMPLinkClick,
                                        this, true);
                            }

                        },

                        onCreateProductLinkClick : function DynamicWelcome_onCreateProductLinkClick(p_event)
                        {
                            var createProduct = this.createTypePopup("bcpg:finishedProduct");
                            createProduct.show();
                            Event.stopEvent(p_event);

                        },

                        onCreateProjectLinkClick : function DynamicWelcome_onCreateProjectLinkClick(p_event)
                        {
                            // Create the createProject module if it doesn't
                            // exist
                            var createProject = this.createTypePopup("pjt:project");
                            createProject.show();
                            Event.stopEvent(p_event);
                        },

                        createTypePopup : function DynamicWelcome_createTypePopup(type)
                        {

                            var instance = this;

                            var templateUrl = YAHOO.lang
                                    .substitute(
                                            Alfresco.constants.URL_SERVICECONTEXT + "components/form?formId=formulation&itemKind=type&itemId={itemId}&destination={destination}&mode=create&submitType=json&showCancelButton=true&popup=true",
                                            {
                                                itemId : type,
                                                destination : instance.options.userHomeNodeRef
                                            });

                            var createRow = new Alfresco.module.SimpleDialog(instance.id + "-createType");

                            createRow.setOptions(
                            {
                                width : "33em",
                                templateUrl : templateUrl,
                                actionUrl : null,
                                destroyOnHide : true,
                                doBeforeDialogShow :
                                {
                                    fn : function(p_form, p_dialog)
                                    {
                                        Alfresco.util.populateHTML([ p_dialog.id + "-dialogTitle",
                                                instance.msg("action.create." + type.replace(":", "_")) ]);
                                    },
                                    scope : this
                                },
                                doBeforeFormSubmit :
                                {
                                    fn : function(form)
                                    {
                                        Alfresco.util.PopupManager.displayMessage(
                                        {
                                            text : this.msg("message.create.please-wait")
                                        });
                                    },
                                    scope : this
                                },
                                onSuccess :
                                {
                                    fn : function(response)
                                    {
                                        if (response.json)
                                        {
                                            var context = "mine";

                                            if (instance.options.siteId !== null)
                                            {
                                                context = null;
                                            }

                                            window.location = beCPG.util.entityURL(instance.options.siteId,
                                                    response.json.persistedObject, type, context);

                                        }
                                    },
                                    scope : this
                                },
                                onFailure :
                                {
                                    fn : function EntityDataGrid_onActionCreate_failure(response)
                                    {
                                        Alfresco.util.PopupManager.displayMessage(
                                        {
                                            text : instance.msg("message.create.failure")
                                        });
                                    },
                                    scope : this
                                }
                            });

                            return createRow;

                        },

                        onImportMPLinkClick : function DynamicWelcome_onImportMPLinkClick(p_event)
                        {

                            var instance = this;

                            // Create the Importer module if it doesn't exist
                            if (this.importEntity === null)
                            {
                                this.importEntity = new Alfresco.module.SimpleDialog(this.id + "-entityImporter")
                                        .setOptions(
                                        {
                                            width : this.options.formWidth,
                                            templateUrl : Alfresco.constants.URL_SERVICECONTEXT + "modules/entity-importer/entity-importer",
                                            actionUrl : Alfresco.constants.PROXY_URI + "becpg/remote/import?destination=" + this.options.userHomeNodeRef,
                                            validateOnSubmit : false,
                                            firstFocus : this.id + "-entityImporter-supplier-field",
                                            doBeforeFormSubmit :
                                            {
                                                fn : function FormulationView_onActionEntityImport_doBeforeFormSubmit(
                                                        form)
                                                {
                                                    Alfresco.util.PopupManager.displayMessage(
                                                    {
                                                        text : this.msg("message.import.please-wait")
                                                    });
                                                },
                                                scope : this
                                            },
                                            onSuccess :
                                            {
                                                fn : function FormulationView_onActionEntityImport_success(response)
                                                {
                                                    if (response.json)
                                                    {

                                                        var context = "mine";

                                                        if (instance.options.siteId !== null)
                                                        {
                                                            context = null;
                                                        }

                                                        window.location = beCPG.util.entityURL(
                                                                instance.options.siteId, response.json[0], null,
                                                                context);
                                                    }
                                                },
                                                scope : this
                                            },
                                            onFailure :
                                            {
                                                fn : function FormulationView_onActionEntityImport_failure(response)
                                                {
                                                    Alfresco.util.PopupManager.displayMessage(
                                                    {
                                                        text : this.msg("message.import.failure")
                                                    });
                                                },
                                                scope : this
                                            }
                                        });
                            }
                            // and show it
                            this.importEntity.show();
                            Event.stopEvent(p_event);
                        }
                    });
})();
