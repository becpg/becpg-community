// Declare namespace...
(function()
{

    /**
     * YUI Library aliases
     */
    var Dom = YAHOO.util.Dom, Event = YAHOO.util.Event;

    /**
     * Alfresco Slingshot aliases
     */
    var $html = Alfresco.util.encodeHTML;

    // Define constructor...
    beCPG.custom.DynamicWelcome = function CustomSearch_constructor(htmlId, dashboardUrl, dashboardType, site)
    {
        beCPG.custom.DynamicWelcome.superclass.constructor.call(this, htmlId, dashboardUrl, dashboardType, site);
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
                        
                        options : {
                            userHomeNodeRef :null,
                        },

                        onReady : function DynamicWelcome_onReady()
                        {
                            // Listen on clicks for the create site link
                            Event.addListener(this.id + "-close-button", "click", this.onCloseClick, this, true);
                            Event.addListener(this.id + "-createSite-button", "click", this.onCreateSiteLinkClick,
                                    this, true);
                            Event.addListener(this.id + "-requestJoin-button", "click", this.onRequestJoinLinkClick,
                                    this, true);

                            // Custom
                            Event.addListener(this.id + "-createProduct-button", "click",
                                    this.onCreateProductLinkClick, this, true);
                            Event.addListener(this.id + "-createProject-button", "click",
                                    this.onCreateProjectLinkClick, this, true);
                            Event.addListener(this.id + "-importMP-button", "click", this.onImportMPLinkClick, this,
                                    true);

                        },

                        onCreateProductLinkClick : function DynamicWelcome_onCreateProductLinkClick(p_event)
                        {
                            // Create the createProduct module if it doesn't
                            // exist
                            if (this.createProduct === null)
                            {
                                this.createProduct = this.createTypePopup("bcpg:finishedProduct");
                            }
                            // and show it
                            this.createProduct.show();
                            Event.stopEvent(p_event);

                        },

                        onCreateProjectLinkClick : function DynamicWelcome_onCreateProjectLinkClick(p_event)
                        {
                            // Create the createProject module if it doesn't
                            // exist
                            if (this.createProject === null)
                            {
                                this.createProject = this.createTypePopup("pjt:project");
                            }
                            // and show it
                            this.createProject.show();
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
                                                destination : instance.options.userHomeNodeRef,
                                            });

                            var createRow = new Alfresco.module.SimpleDialog(instance.id + "-createType");

                            createRow
                                    .setOptions(
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
                                                        instance.msg("action.create." + type.replace(":","_")) ]);
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
                                                    window.location = beCPG.util.entityCharactURL(null,
                                                            response.json.persistedObject, type,"mine");

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

                            // Create the Importer module if it doesn't exist
                            if (this.importEntity === null)
                            {
                                this.importEntity = new Alfresco.module.SimpleDialog(
                                        this.id + "-entityImporter")
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
                                                        window.location = beCPG.util.entityDetailsURL(null,
                                                                response.json[0],null,"mine");
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
