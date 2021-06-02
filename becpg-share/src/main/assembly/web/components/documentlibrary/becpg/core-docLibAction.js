/*******************************************************************************
 *  Copyright (C) 2010-2021 beCPG. 
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
                 
                var msgPopup = Alfresco.util.PopupManager.displayMessage({
                	 text : this.msg("message.branch-entity.inprogress", displayName),
                	 displayTime: 0
       		    	});
                 
                 
                 Alfresco.util.Ajax
                 .request({
                    method : Alfresco.util.Ajax.POST,
                    url : Alfresco.constants.PROXY_URI + "becpg/entity/simulation/create?entityNodeRef=" + nodeRef,
                    responseContentType : Alfresco.util.Ajax.JSON,
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
                        	msgPopup.destroy();
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
	   actionName : "onActionRefreshReport",
	   fn : function onActionRefreshReport(asset) {
		   var msgPopup = Alfresco.util.PopupManager.displayMessage({
			   text : this.msg("message.generate-reports.please-wait"),
			   spanClass : "wait",
			   displayTime : 0
		   });

		   Alfresco.util.Ajax.request({
		      method : Alfresco.util.Ajax.GET,
		      url : Alfresco.constants.PROXY_URI + "becpg/entity/generate-report/node/" + asset.nodeRef.replace(":/", "")
		            + "/force",
		      successCallback : {
		         fn : function EntityDataListToolbar_onFinish_success(response) {
			         this.recordData.jsNode.setNodeRef(asset.nodeRef);
			         window.location.href = this.getActionUrls(this.recordData).documentDetailsUrl.replace("document-details?","entity-data-lists?list=View-properties&");

		         },
		         scope : this
		      },
		      failureCallback : {
		         fn : function EntityDataListToolbar_onFinish_failure(response) {
		        	 msgPopup.destroy();
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
	   
	   	
	   	window.location.href = beCPG.util.entityURL(recordSiteName, p_record.nodeRef, p_record.node.type);
	   	
	   	
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
	       actionName : "onActionCopyDataListTo",
       fn : function onActionCopyDataListTo(p_record) {
    	   var dt = Alfresco.util.ComponentManager.find({
				name : "beCPG.module.EntityDataGrid"
			});
    	   
    	   if(dt!=null && dt.length >0 && dt[0].datalistMeta.nodeRef!=null){

    		    var actionUrl = Alfresco.constants.PROXY_URI + 'becpg/entity/datalists/copy/node/' + dt[0].datalistMeta.nodeRef.replace(":/", "")+"?destination=to";

	            this.modules.entityPicker = new Alfresco.module.SimpleDialog(this.id + "-entityPicker").setOptions({
	               width : "33em",
	               templateUrl : Alfresco.constants.URL_SERVICECONTEXT + "modules/entity-picker/entity-picker?title="
	               							+encodeURIComponent(this.msg("message.copy-datalist-to",dt[0].datalistMeta.title))
	               							+"&entityNodeRef="+p_record.nodeRef+"&showActions=true",
	               actionUrl : actionUrl,
	               validateOnSubmit : false,
	               firstFocus : this.id + "-entityPicker-entity-field",
	               onSuccess:
	               {
	                  fn: function onActionCopyDataListTo_success(response)
	                  {
	                	  if(response.json) {
	                	    var dest = response.json[0];
	                	    var recordSiteName = $isValueSet(p_record.location.site) ? p_record.location.site.name : null;
	                	    window.location.href =  beCPG.util.entityURL(recordSiteName, dest, null, null , dt[0].datalistMeta.name);
	                	  }
	                  },
	                  scope: this
	               }
	            });
	            
	            this.modules.entityPicker.show();
    		   
    	   } else {
    		   Alfresco.util.PopupManager.displayMessage({
    			   text : this.msg("message.copy-datalist-to.notselected")
    		   });  
    	   }
       }
    });


	
	YAHOO.Bubbling.fire("registerAction", {
	       actionName : "onActionImportDataListFrom",
       fn : function onActionImportDataListFrom(p_record) {
    	   var dt = Alfresco.util.ComponentManager.find({
				name : "beCPG.module.EntityDataGrid"
			});
    	   
    	   if(dt!=null && dt.length >0 && dt[0].datalistMeta.nodeRef!=null){

    		    var actionUrl = Alfresco.constants.PROXY_URI + 'becpg/entity/datalists/copy/node/' + dt[0].datalistMeta.nodeRef.replace(":/", "")+"?destination=from";

	            this.modules.entityPicker = new Alfresco.module.SimpleDialog(this.id + "-entityPicker").setOptions({
	               width : "33em",
	               templateUrl : Alfresco.constants.URL_SERVICECONTEXT + "modules/entity-picker/entity-picker?title="
	               							+encodeURIComponent(this.msg("message.import-datalist-from",dt[0].datalistMeta.title))
	               							+"&entityNodeRef="+p_record.nodeRef+"&showActions=true",
	               actionUrl : actionUrl,
	               validateOnSubmit : false,
	               firstFocus : this.id + "-entityPicker-entity-field",
	               onSuccess:
	               {
	                  fn: function onActionImportDataListFrom_success(response)
	                  {
	                	  if(response.json) {            	

							 Alfresco.util.PopupManager.displayMessage({
						    			   text : this.msg("message.import-datalist-from.success")
						      }); 
							  document.location.reload();
	                	  }
	                  },
	                  scope: this
	               }
	            });
	            
	            this.modules.entityPicker.show();
    		   
    	   } else {
    		   Alfresco.util.PopupManager.displayMessage({
    			   text : this.msg("message.import-datalist-from.notselected")
    		   });  
    	   }
       }
    });
	
	YAHOO.Bubbling.fire("registerAction", {
	   actionName : "onActionReportTplRefresh",
	   fn : function onActionReportTplRefresh(asset) {
		   Alfresco.util.PopupManager.displayMessage({
			   text : this.msg("message.generate-reports.please-wait-mail"),
			   displayTime : 5
		   });

		   Alfresco.util.Ajax.request({
		      method : Alfresco.util.Ajax.GET,
		      url : Alfresco.constants.PROXY_URI + "becpg/report/reportTpl/" + asset.nodeRef.replace(":/", "")
		            + "/refresh"
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
		   actionName : "onActionShowAssociatedProcess",
		   fn : function onActionShowAssociatedProcess(asset) {
				   
				   Alfresco.util.Ajax.request({
					    method : Alfresco.util.Ajax.GET,
						url : Alfresco.constants.URL_SERVICECONTEXT + "modules/entity-process/entity-process?nodeRef="+asset.nodeRef+"&htmlid="+this.id ,
						successCallback : {
							fn : function(resp){
								var containerDiv = document.createElement("div");     
	                            containerDiv.innerHTML = resp.serverResponse.responseText;
	                            var panelDiv = Dom.getFirstChild(containerDiv);
	                            this.widgets.projects = Alfresco.util.createYUIPanel(panelDiv, {
									draggable : true,
									width : "40em"
								});
								this.widgets.projects.show();
							},
							scope : this
						},
						execScripts: true
				   });
				   
		   }
		});

})();
