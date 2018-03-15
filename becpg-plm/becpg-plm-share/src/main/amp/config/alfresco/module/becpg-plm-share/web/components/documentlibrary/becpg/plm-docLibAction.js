/*******************************************************************************
 *  Copyright (C) 2010-2018 beCPG. 
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

	var $isValueSet = Alfresco.util.isValueSet;

	YAHOO.Bubbling
    .fire(
          "registerAction",
          {
             actionName : "onActionMergeEntity",
             fn : function onActionMergeEntity(asset) {
                 var displayName = asset.displayName, nodeRef = new Alfresco.util.NodeRef(asset.nodeRef), version = asset.version;

                 if (!this.newEntityVersion) {
                     this.newEntityVersion = Alfresco.module.getNewEntityVersionInstance();
                 }

                 this.newEntityVersion.show({
                    filename : displayName,
                    nodeRef : nodeRef,
                    version : version,
                    merge : true,
                    onNewEntityVersionComplete : {
                       fn : function EntityActions_oACI_success(data) {
                           this.recordData.jsNode.setNodeRef(data.successful[0].nodeRef);
                           window.location.href = this.getActionUrls(this.recordData).documentDetailsUrl.replace("document-details?","entity-data-lists?list=View-properties&");
                       },
                       scope : this
                    }
                 });

             }
          });
	
	YAHOO.Bubbling
    .fire(
          "registerAction",
          {
             actionName : "onActionBranchEntity",
             fn : function onActionBranchEntity(p_record) {
                 var  nodeRef = new Alfresco.util.NodeRef(p_record.nodeRef), recordSiteName = $isValueSet(p_record.location.site) ? p_record.location.site.name : null,
                		 displayName = p_record.displayName;
                 
                 Alfresco.util.PopupManager.displayMessage({
                	 text : this.msg("message.branch-entity.inprogress", displayName)
       		    	});
                 
                 
                 Alfresco.util.Ajax
                 .request({
                    method : Alfresco.util.Ajax.POST,
                    url : Alfresco.constants.PROXY_URI + "becpg/entity/simulation/create?entityNodeRef=" + nodeRef,
                    successCallback : {
                       fn : function(resp) {
                          if (resp.json) {
                             window.location.href = beCPG.util.entityURL(recordSiteName,
                                   resp.json.persistedObject, p_record.node.type);
                          }
                       },
                       scope : this
                    } ,
                    failureCallback : {
                        fn : function(response) {
                           if (response.json && response.json.message) {
                              Alfresco.util.PopupManager.displayPrompt({
                                 title : this.msg("message.branch-entity.failure"),
                                 text : response.json.message
                              });
                           } else {
                              Alfresco.util.PopupManager.displayMessage({
                                 text : this.msg("message.branch-entity.failure")
                              });
                           }
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
				   text : this.msg("message.synchronize-entities.please-wait-mail"),
				   displayTime : 5
			   });

			   Alfresco.util.Ajax.request({
			      method : Alfresco.util.Ajax.GET,
			      url : Alfresco.constants.PROXY_URI + "becpg/entity/entityTpl/" + asset.nodeRef.replace(":/", "")
			            + "/synchronizeEntities"
			   });
		   }
		});
		
		YAHOO.Bubbling.fire("registerAction", {
		   actionName : "onActionEntityTplFormulateEntities",
		   fn : function onActionEntityTplFormulateEntities(asset) {
			   Alfresco.util.PopupManager.displayMessage({
				   text : this.msg("message.formulate-entities.please-wait-mail"),
				   displayTime : 5
			   });

			   Alfresco.util.Ajax.request({
			      method : Alfresco.util.Ajax.GET,
			      url : Alfresco.constants.PROXY_URI + "becpg/entity/entityTpl/" + asset.nodeRef.replace(":/", "")
			            + "/formulateEntities"
			   });
		   }
		});
	

	   YAHOO.Bubbling.fire("registerAction", {
	       actionName : "onActionCompareEntity",
	       fn : function onActionCompareEntity(p_record) {
	            var actionUrl = Alfresco.constants.PROXY_URI + 'becpg/entity/compare/' + p_record.nodeRef.replace(":/", "") + "/";

	            // Always create a new instance
	            this.modules.entityCompare = new Alfresco.module.SimpleDialog(this.id + "-entityCompare").setOptions({
	               width : "33em",
	               templateUrl : Alfresco.constants.URL_SERVICECONTEXT + "modules/entity-compare/entity-compare?entityNodeRef="+p_record.nodeRef,
	               actionUrl : actionUrl,
	               validateOnSubmit : false,
	               firstFocus : this.id + "-entityCompare-entities-field",
	               doBeforeFormSubmit : {
	                  fn : function(form) {
	                     this.modules.entityCompare.form.setAJAXSubmit(false);
	                     this.modules.entityCompare.hide();
	                     var reportSelect = YAHOO.util.Dom.get(this.id + "-entityCompare-reportTemplate");
	                     var fileName = reportSelect.options[reportSelect.selectedIndex].getAttribute("fileName");
	                     window.location.href=actionUrl+fileName+"?entities="+YAHOO.util.Dom.get(this.id + "-entityCompare-entities-added").value
	                     +"&tplNodeRef="+reportSelect.value;
	                  },
	                  scope : this
	               }
	            });
	            
	            this.modules.entityCompare.show();
	       }
	    });
	   
	   
	   YAHOO.Bubbling.fire("registerAction", {
	       actionName : "onActionSendToSupplier",
	       fn : function onActionSendToSupplier(p_record) {
	    	    var  nodeRef = new Alfresco.util.NodeRef(p_record.nodeRef), recordSiteName = $isValueSet(p_record.location.site) ? p_record.location.site.name : null,
              		 displayName = p_record.displayName;
	    	   
	            var actionUrl = Alfresco.constants.PROXY_URI + 'becpg/supplier/send-to-supplier?entityNodeRef=' + p_record.nodeRef;

	            this.modules.sendToSupplier = new Alfresco.module.SimpleDialog(this.id + "-sendToSupplier").setOptions({
	            	  width : "33em",
		              templateUrl : Alfresco.constants.URL_SERVICECONTEXT + "modules/supplier/send-to-supplier",
		              actionUrl : actionUrl,
		              validateOnSubmit : false,
		              destroyOnHide: true,
					  firstFocus : this.id + "-sendToSupplier-projectTpl-field",
					  doBeforeFormSubmit : {
						  fn : function onActionSendToSupplier_doBeforeFormSubmit(form) {
							  Alfresco.util.PopupManager.displayMessage({
				                	 text : this.msg("message.send-to-supplier.inprogress")
				       		    	});
						  },
						  scope : this
					  },
					  onSuccess : {
						  fn : function onActionSendToSupplier_success(response) {
							  if (response.json) {
								   window.location.href = beCPG.util.entityURL(recordSiteName,
			                             response.json.persistedObject, p_record.node.type);
							  }
						  },
						  scope : this
					  },
					  onFailure : {
						  fn : function onActionSendToSupplier_failure(response) {
							  if(response.json && response.json.message){
								  Alfresco.util.PopupManager.displayMessage({
									  text : response.json.message
								  });  
							  } else {
								  Alfresco.util.PopupManager.displayMessage({
									  text : this.msg("message.import.failure")
								  });
							  }
						  },
						  scope : this
					  }
				  });
	            
	            this.modules.sendToSupplier.show();				 
	            	            
	       }
	    });
	
	
})();
	
	   
	   
	
