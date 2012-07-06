/**
 * beCPG document actions
 * 
 * @namespace Alfresco
 * @class DocumentActions
 */
(function() {
	
	  var $siteURL = Alfresco.util.siteURL,
     		$isValueSet = Alfresco.util.isValueSet;

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
				         window.location = this.getActionUrls(this.recordData).documentDetailsUrl;
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
			                     window.location = this.getActionUrls(this.recordData).documentDetailsUrl;
		                     },
		                     scope : this
		                  }
		               });

	               }
	            });


	YAHOO.Bubbling.fire("registerAction", {
	   actionName : "onActionRefreshReport",
	   fn : function onActionRefreshReport(asset) {
		   Alfresco.util.PopupManager.displayMessage({
			   text : this.msg("message.generate-report.please-wait")
		   });

		   Alfresco.util.Ajax.request({
		      method : Alfresco.util.Ajax.GET,
		      url : Alfresco.constants.PROXY_URI + "becpg/entity/generate-report/node/" + asset.nodeRef.replace(":/", "")
		            + "/force",
		      successCallback : {
		         fn : function EntityDataListToolbar_onFinish_success(response) {
			         this.recordData.jsNode.setNodeRef(asset.nodeRef);
			         window.location = this.getActionUrls(this.recordData).documentDetailsUrl;

		         },
		         scope : this
		      },
		      failureCallback : {
		         fn : function EntityDataListToolbar_onFinish_failure(response) {
			         Alfresco.util.PopupManager.displayMessage({
				         text : this.msg("message.generate-report.failure")
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
	   	var redirect  = $siteURL("entity-data-lists?nodeRef="+p_record.nodeRef,
         {
            site: recordSiteName
         });
	   	
	   	if(p_record.node.aspects.indexOf("bcpg:transformationAspect")>0){
	   		redirect+="&list=compoList";
	   	}
	   	
	   	window.location.href = redirect;
	   }
	});

})();