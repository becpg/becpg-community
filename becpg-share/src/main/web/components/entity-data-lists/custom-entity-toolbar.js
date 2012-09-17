(function() {
	if (beCPG.component.EntityDataListToolbar) {

		YAHOO.Bubbling.fire("registerToolbarButtonAction", {
		   actionName : "eco-calculate-wused",
		   evaluate : function(asset) {
		   	return asset.name!=null 
		   		&& (asset.name === "replacementList" ); 
		   },
		   fn : function(instance) {

				   Alfresco.util.PopupManager.displayMessage({
					   text : this.msg("message.eco-calculate-wused.please-wait")
				   });

				   Alfresco.util.Ajax.request({
				      method : Alfresco.util.Ajax.GET,
				      url : Alfresco.constants.PROXY_URI + "becpg/ecm/changeorder/"
				            + this.options.entityNodeRef.replace(":/", "") + "/calculatewused",
				      successCallback : {
				         fn : function EntityDataListthis_onECOCalculateWUsed_success(response) {
					         Alfresco.util.PopupManager.displayMessage({
						         text : this.msg("message.eco-calculate-wused.success")
					         });

				         },
				         scope : this
				      },
				      failureCallback : {
				         fn : function EntityDataListthis_onECOCalculateWUsed_failure(response) {
					         if (response.message != null) {
						         Alfresco.util.PopupManager.displayPrompt({
							         text : response.message
						         });
					         } else {
						         Alfresco.util.PopupManager.displayMessage({
							         text : this.msg("message.eco-calculate-wused.failure")
						         });
					         }
				         },
				         scope : this
				      }
				   });
		   }
		});

		YAHOO.Bubbling.fire("registerToolbarButtonAction", {
		   actionName : "eco-do-simulation",
		   evaluate : function(asset) {
		   	return asset.name!=null 
		   		&& (asset.name === "replacementList" ); 
		   },
		   fn : function(instance) {

				   Alfresco.util.PopupManager.displayMessage({
					   text : this.msg("message.eco-do-simulation.please-wait")
				   });

				   Alfresco.util.Ajax.request({
				      method : Alfresco.util.Ajax.GET,
				      url : Alfresco.constants.PROXY_URI + "becpg/ecm/changeorder/"
				            + this.options.entityNodeRef.replace(":/", "") + "/dosimulation",
				      successCallback : {
				         fn : function EntityDataListthis_onECODoSimulation_success(response) {
					         Alfresco.util.PopupManager.displayMessage({
						         text : this.msg("message.eco-do-simulation.success")
					         });

				         },
				         scope : this
				      },
				      failureCallback : {
				         fn : function EntityDataListthis_onECODoSimulation_failure(response) {
					         if (response.message != null) {
						         Alfresco.util.PopupManager.displayPrompt({
							         text : response.message
						         });
					         } else {
						         Alfresco.util.PopupManager.displayMessage({
							         text : this.msg("message.eco-do-simulation.failure")
						         });
					         }
				         },
				         scope : this
				      }

				   });
		   }
		});

		YAHOO.Bubbling.fire("registerToolbarButtonAction", {
		   actionName :  "eco-apply",
		   evaluate : function(asset) {
		   	return asset.name!=null 
		   		&& (asset.name === "replacementList" ); 
		   },
		   fn : function(instance) {

				   Alfresco.util.PopupManager.displayMessage({
					   text : this.msg("message.eco-apply.please-wait")
				   });

				   Alfresco.util.Ajax.request({
				      method : Alfresco.util.Ajax.GET,
				      url : Alfresco.constants.PROXY_URI + "becpg/ecm/changeorder/"
				            + this.options.entityNodeRef.replace(":/", "") + "/apply",
				      successCallback : {
				         fn : function EntityDataListthis_onECOApply_success(response) {
					         Alfresco.util.PopupManager.displayMessage({
						         text : this.msg("message.eco-apply.success")
					         });

				         },
				         scope : this
				      },
				      failureCallback : {
				         fn : function EntityDataListthis_onECOApply_failure(response) {
					         if (response.message != null) {
						         Alfresco.util.PopupManager.displayPrompt({
							         text : response.message
						         });
					         } else {
						         Alfresco.util.PopupManager.displayMessage({
							         text : this.msg("message.eco-apply.failure")
						         });
					         }
				         },
				         scope : this
				      }

				   });
		   }
		});
		

		YAHOO.Bubbling.fire("registerToolbarButtonAction", {
		   actionName : "formulate",		   
		   evaluate : function(asset) {
		   	return asset.name!=null 
		   		&& (asset.name === "compoList" || asset.name === "processList"
		   			|| asset.name === "packagingList"); 
		   },
		   fn : function(instance) {

				   Alfresco.util.PopupManager.displayMessage({
					   text : this.msg("message.formulate.please-wait")
				   });

				   Alfresco.util.Ajax.request({
				      method : Alfresco.util.Ajax.GET,
				      url : Alfresco.constants.PROXY_URI + "becpg/product/formulate/node/"
				            + this.options.entityNodeRef.replace(":/", ""),
				      successCallback : {
				         fn : function(response) {
					         Alfresco.util.PopupManager.displayMessage({
						         text : this.msg("message.formulate.success")
					         });

					         YAHOO.Bubbling.fire("refreshDataGrids");

				         },
				         scope : this
				      },
				      failureCallback : {
				         fn : function(response) {
					         if (response.message != null) {
						         Alfresco.util.PopupManager.displayPrompt({
							         text : response.message
						         });
					         } else {
						         Alfresco.util.PopupManager.displayMessage({
							         text : this.msg("message.formulate.failure")
						         });
					         }
				         },
				         scope : this
				      }

				   });
		   }
		});

		YAHOO.Bubbling.fire("registerToolbarButtonAction",
		      {
		         actionName : "import",
		         evaluate : function(asset) {
				   	return asset.name!=null 
				   		&& (asset.name === "compoList"); 
				   },
		         fn : function(instance) {
				         var actionUrl = Alfresco.constants.PROXY_URI + "becpg/remote/import";

				         var doSetupFormsValidation = function FormulationView_onActionEntityImport_doSetupFormsValidation(
				               form) {
				         
//   TODO               form.addValidation(this.modules.entityImporter.id + "-entities-field", Alfresco.forms.validation.mandatory, null, "blur");
//	   	               form.setShowSubmitStateDynamically(true, false);
				         };
				        

				         // Always create a new instance
				         this.modules.entityImporter = new Alfresco.module.SimpleDialog(this.id + "-entityImporter")
				               .setOptions({
				                  width : "33em",
				                  templateUrl : Alfresco.constants.URL_SERVICECONTEXT
				                        + "modules/entity-importer/entity-importer",
				                  actionUrl : actionUrl,
				                  validateOnSubmit : false,
				                  doSetupFormsValidation : {
				                     fn : doSetupFormsValidation,
				                     scope : this
				                  },
				                  firstFocus : this.id + "-entityImporter-supplier-field",
				                  doBeforeFormSubmit : {
				                  	fn : function FormulationView_onActionEntityImport_doBeforeFormSubmit(form){
				                  		 
				   				         Alfresco.util.PopupManager.displayMessage({
				   							   text : this.msg("message.import.please-wait")
				   						   });
				   				         
				                  	},
				                  	scope: this
				                  },
				                  onSuccess : {
				                     fn : function FormulationView_onActionEntityImport_success(response) {
					                     Alfresco.util.PopupManager.displayMessage({
						                     text : this.msg("message.import.success")
					                     });
				                     },
				                     scope : this
				                  },
				                  onFailure : {
				                     fn : function FormulationView_onActionEntityImport_failure(response) {
					                     Alfresco.util.PopupManager.displayMessage({
						                     text : this.msg("message.import.failure")
					                     });
				                     },
				                     scope : this
				                  }
				               });
				         this.modules.entityImporter.show();
		         }
		      });


		YAHOO.Bubbling.fire("registerToolbarButtonAction", {
		   actionName : "finish",
		   fn : function(instance) {
				   Alfresco.util.PopupManager.displayMessage({
					   text : this.msg("message.generate-report.please-wait")
				   });

				   Alfresco.util.Ajax.request({
				      method : Alfresco.util.Ajax.GET,
				      url : Alfresco.constants.PROXY_URI + "becpg/entity/generate-report/node/"
				            + this.options.entityNodeRef.replace(":/", "") + "/check-datalists",
				      successCallback : {
				         fn : function EntityDataListthis_onFinish_success(response) {
					         Alfresco.util.PopupManager.displayMessage({
						         text : this.msg("message.generate-report.success")
					         });

					         if (this.options.siteId != "") {
						         window.location = Alfresco.constants.URL_PAGECONTEXT + "site/" + this.options.siteId
						               + "/document-details?nodeRef=" + this.options.entityNodeRef;
					         } else {
						         window.location = Alfresco.constants.URL_PAGECONTEXT + "document-details?nodeRef="
						               + this.options.entityNodeRef;
					         }

				         },
				         scope : this
				      },
				      failureCallback : {
				         fn : function EntityDataListthis_onFinish_failure(response) {
					         Alfresco.util.PopupManager.displayMessage({
						         text : this.msg("message.generate-report.failure")
					         });
				         },
         				scope : this
				      }
				   });
		   }
		});
		
	}

})();