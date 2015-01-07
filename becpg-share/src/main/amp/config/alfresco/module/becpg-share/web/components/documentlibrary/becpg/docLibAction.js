/*******************************************************************************
 *  Copyright (C) 2010-2014 beCPG. 
 *   
 *  This file is part of beCPG 
 *   
 *  beCPG is free software: you can redistribute it and/or modify 
 *  it under the terms of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation, either version 3 of the License, or 
 *  (at your option) any later version. 
 *   
 *  beCPG is distributed in the hope that it will be useful, 
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 *  GNU Lesser General Public License for more details. 
 *   
 *  You should have received a copy of the GNU Lesser General Public License along with beCPG.
 *   If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
/**
 * beCPG document actions
 * 
 * @namespace Alfresco
 * @class DocumentActions
 */
(function() {
	
	  var	$isValueSet = Alfresco.util.isValueSet;

	YAHOO.Bubbling.fire("registerAction", {
	   actionName : "onActionCheckOutEntity",
	   fn : function onActionCheckOutEntity(asset) {
		   var displayName = asset.displayName, nodeRef = new Alfresco.util.NodeRef(asset.nodeRef);

		   Alfresco.util.PopupManager.displayMessage({
		      displayTime : 0,
		      effect : null,
		      text : this.msg("message.checkout-entity.inprogress", displayName)
		   });

		   this.modules.actions.genericAction({
		      success : {
			      callback : {
			         fn : function DocumentActions_oAEO_success(data) {
				         this.recordData.jsNode.setNodeRef(data.json.results[0].nodeRef);
				         window.location.href = this.getActionUrls(this.recordData).documentDetailsUrl.replace("document-details","entity-details");
			         },
			         scope : this
			      }
		      },
		      failure : {
			      message : this.msg("message.checkout-entity.failure", displayName)
		      },
		      webscript : {
		         method : Alfresco.util.Ajax.POST,
		         name : "checkout/node/{nodeRef}",
		         params : {
			         nodeRef : nodeRef.uri
		         }
		      }
		   });
	   }
	});

	YAHOO.Bubbling
	      .fire(
	            "registerAction",
	            {
	               actionName : "onActionCheckInEntity",
	               fn : function onActionCheckInEntity(asset) {
		               var displayName = asset.displayName, nodeRef = new Alfresco.util.NodeRef(asset.nodeRef), version = asset.version;

		               if (asset.workingCopy && asset.workingCopy.workingCopyVersion) {
			               version = asset.workingCopy.workingCopyVersion;
		               }

		               if (!this.newEntityVersion) {
			               this.newEntityVersion = Alfresco.module.getNewEntityVersionInstance();
		               }

		               this.newEntityVersion.show({
		                  filename : displayName,
		                  nodeRef : nodeRef,
		                  version : version,
		                  onNewEntityVersionComplete : {
		                     fn : function EntityActions_oACI_success(data) {
			                     this.recordData.jsNode.setNodeRef(data.successful[0].nodeRef);
			                     window.location.href = this.getActionUrls(this.recordData).documentDetailsUrl.replace("document-details","entity-details");
		                     },
		                     scope : this
		                  }
		               });

	               }
	            });

	YAHOO.Bubbling.fire("registerAction", {
		   actionName : "onActionCancelCheckOutEntity",
		   fn : function onActionCancelCheckOutEntity(asset) {
			   var displayName = asset.displayName, nodeRef = new Alfresco.util.NodeRef(asset.nodeRef);

			   this.modules.actions.genericAction(
		         {
		            success:
		            {
		               callback:
		               {
		                  fn: function DocumentActions_oACE_success(data)
		                  {
		                      var oldNodeRef = this.recordData.jsNode.nodeRef.nodeRef,
		                      newNodeRef = data.json.results[0].nodeRef;
		                      this.recordData.jsNode.setNodeRef(newNodeRef);
		                      window.location = this.getActionUrls(this.recordData).documentDetailsUrl.replace("document-details","entity-details") + "#editCancelled";
		                      // ALF-16598 fix, page is not refreshed if only hash was changed, force page reload for cancel online editing
		                      if (oldNodeRef == newNodeRef)
		                      {
		                          window.location.reload();
		                      }
		                  },
		                  scope: this
		               }
		            },
		            failure:
		            {
		               message: this.msg("message.edit-cancel.failure", displayName)
		            },
		            webscript:
		            {
		               method: Alfresco.util.Ajax.POST,
		               name: "cancel-checkout/node/{nodeRef}",
		               params:
		               {
		                  nodeRef: nodeRef.uri
		               }
		            }
		         });			   
		   }
		});	

	YAHOO.Bubbling.fire("registerAction", {
	   actionName : "onActionRefreshReport",
	   fn : function onActionRefreshReport(asset) {
		   Alfresco.util.PopupManager.displayMessage({
			   text : this.msg("message.generate-reports.please-wait")
		   });

		   Alfresco.util.Ajax.request({
		      method : Alfresco.util.Ajax.GET,
		      url : Alfresco.constants.PROXY_URI + "becpg/entity/generate-report/node/" + asset.nodeRef.replace(":/", "")
		            + "/force",
		      successCallback : {
		         fn : function EntityDataListToolbar_onFinish_success(response) {
			         this.recordData.jsNode.setNodeRef(asset.nodeRef);
			         window.location.href = this.getActionUrls(this.recordData).documentDetailsUrl.replace("document-details","entity-details");

		         },
		         scope : this
		      },
		      failureCallback : {
		         fn : function EntityDataListToolbar_onFinish_failure(response) {
			         Alfresco.util.PopupManager.displayMessage({
				         text : this.msg("message.generate-reports.failure")
			         });
		         },
		         scope : this
		      }
		   });
	   }
	});

	YAHOO.Bubbling.fire("registerAction", {
	   actionName : "onActionShowCharact",
	   fn : function onActionShowCharact(p_record) {

	   	var recordSiteName = $isValueSet(p_record.location.site) ? p_record.location.site.name : null;
	   
	   	
	   	window.location.href = beCPG.util.entityCharactURL(recordSiteName, p_record.nodeRef, p_record.node.type);
	   	
	   	
	   }
	});
	
	
	   YAHOO.Bubbling.fire("registerAction", {
	       actionName : "onActionCompareEntity",
	       fn : function onActionShowCharact(p_record) {
	            var actionUrl = Alfresco.constants.PROXY_URI + 'becpg/entity/compare/' + p_record.nodeRef.replace(":/", "") + "/compare.pdf";

	            // Always create a new instance
	            this.modules.entityCompare = new Alfresco.module.SimpleDialog(this.id + "-entityCompare").setOptions({
	               width : "33em",
	               templateUrl : Alfresco.constants.URL_SERVICECONTEXT + "modules/entity-compare/entity-compare",
	               actionUrl : actionUrl,
	               validateOnSubmit : false,
	               firstFocus : this.id + "-entityCompare-entities-field",
	               doBeforeFormSubmit : {
	                  fn : function(form) {
	                     this.modules.entityCompare.form.setAJAXSubmit(false);
	                     this.modules.entityCompare.hide();
	                     var reportSelect = YAHOO.util.Dom.get(this.id + "-entityCompare-reportTemplate");
	                     window.location.href=actionUrl+"?entities="+YAHOO.util.Dom.get(this.id + "-entityCompare-entities-added").value
	                     +"&tplNodeRef="+reportSelect.value;
	                  },
	                  scope : this
	               },
	            });
	            
	            
	            this.modules.entityCompare.show();
	       }
	    });
	
	
	YAHOO.Bubbling.fire("registerAction", {
       actionName : "onActionShowDocs",
       fn : function onActionShowCharact(p_record) {

        var recordSiteName = $isValueSet(p_record.location.site) ? p_record.location.site.name : null;
       
        window.location.href = beCPG.util.entityDocumentsURL(recordSiteName, p_record.location.path, p_record.location.file,true);
        
        
       }
    });
	
	YAHOO.Bubbling.fire("registerAction", {
	   actionName : "onActionReportTplRefresh",
	   fn : function onActionReportTplRefresh(asset) {
		   Alfresco.util.PopupManager.displayMessage({
			   text : this.msg("message.generate-reports.please-wait")
		   });

		   Alfresco.util.Ajax.request({
		      method : Alfresco.util.Ajax.GET,
		      url : Alfresco.constants.PROXY_URI + "becpg/report/reportTpl/" + asset.nodeRef.replace(":/", "")
		            + "/refresh",
		      successCallback : {
		         fn : function EntityDataListToolbar_onReportTplRefreshReports_success(response) {
		         	Alfresco.util.PopupManager.displayMessage({
				         text : this.msg("message.generate-reports.success")
			         });
		         },
		         scope : this
		      },
		      failureCallback : {
		         fn : function EntityDataListToolbar_onReportTplRefreshReports_failure(response) {
			         Alfresco.util.PopupManager.displayMessage({
				         text : this.msg("message.generate-reports.failure")
			         });
		         },
		         scope : this
		      }
		   });
	   }
	});
	
	YAHOO.Bubbling.fire("registerAction", {
	   actionName : "onActionReportTplUpdatePermissions",
	   fn : function onActionReportTplUpdatePermissions(asset) {
		   Alfresco.util.PopupManager.displayMessage({
			   text : this.msg("message.update-permissions.please-wait")
		   });

		   Alfresco.util.Ajax.request({
		      method : Alfresco.util.Ajax.GET,
		      url : Alfresco.constants.PROXY_URI + "becpg/report/reportTpl/" + asset.nodeRef.replace(":/", "")
		            + "/updatePermissions",
		      successCallback : {
		         fn : function EntityDataListToolbar_onReportTplRefreshReports_success(response) {
		         	Alfresco.util.PopupManager.displayMessage({
				         text : this.msg("message.update-permissions.success")
			         });
		         },
		         scope : this
		      },
		      failureCallback : {
		         fn : function EntityDataListToolbar_onReportTplRefreshReports_failure(response) {
			         Alfresco.util.PopupManager.displayMessage({
				         text : this.msg("message.update-permissions.failure")
			         });
		         },
		         scope : this
		      }
		   });
	   }
	});
	
	YAHOO.Bubbling.fire("registerAction", {
	   actionName : "onActionEntityTplSynchronizeEntities",
	   fn : function onActionEntityTplSynchronizeEntities(asset) {
		   Alfresco.util.PopupManager.displayMessage({
			   text : this.msg("message.synchronize-entities.please-wait")
		   });

		   Alfresco.util.Ajax.request({
		      method : Alfresco.util.Ajax.GET,
		      url : Alfresco.constants.PROXY_URI + "becpg/entity/entityTpl/" + asset.nodeRef.replace(":/", "")
		            + "/synchronizeEntities",
		      successCallback : {
		         fn : function EntityDataListToolbar_onActionEntityTplSynchronizeEntities_success(response) {
		         	Alfresco.util.PopupManager.displayMessage({
				         text : this.msg("message.synchronize-entities.success")
			         });
		         },
		         scope : this
		      },
		      failureCallback : {
		         fn : function EntityDataListToolbar_onActionEntityTplSynchronizeEntities_failure(response) {
			         Alfresco.util.PopupManager.displayMessage({
				         text : this.msg("message.synchronize-entities.failure")
			         });
		         },
		         scope : this
		      }
		   });
	   }
	});
	
	YAHOO.Bubbling.fire("registerAction", {
	   actionName : "onActionEntityTplFormulateEntities",
	   fn : function onActionEntityTplFormulateEntities(asset) {
		   Alfresco.util.PopupManager.displayMessage({
			   text : this.msg("message.formulate-entities.please-wait")
		   });

		   Alfresco.util.Ajax.request({
		      method : Alfresco.util.Ajax.GET,
		      url : Alfresco.constants.PROXY_URI + "becpg/entity/entityTpl/" + asset.nodeRef.replace(":/", "")
		            + "/formulateEntities",
		      successCallback : {
		         fn : function EntityDataListToolbar_onActionEntityTplFormulateEntities_success(response) {
		         	Alfresco.util.PopupManager.displayMessage({
				         text : this.msg("message.formulate-entities.success")
			         });
		         },
		         scope : this
		      },
		      failureCallback : {
		         fn : function EntityDataListToolbar_onActionEntityTplFormulateEntities_failure(response) {
			         Alfresco.util.PopupManager.displayMessage({
				         text : this.msg("message.formulate-entities.failure")
			         });
		         },
		         scope : this
		      }
		   });
	   }
	});
	

})();
