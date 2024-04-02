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
(function() {
   if (beCPG.component.EntityDataListToolbar) {


	   
	   YAHOO.Bubbling
            .fire(
                  "registerToolbarButtonAction",
                  {
                     actionName : "export-csv",
                     right : true,
                     evaluate : function(asset, entity) {
                        return asset.name !== null && !asset.name.indexOf("View-properties") == 0 && !asset.name.indexOf("View-reports") == 0
                        && !asset.name.indexOf("View-documents") == 0 ;
                     },
                     fn : function(instance) {

                        var dt = Alfresco.util.ComponentManager.find({
                           name : "beCPG.module.EntityDataGrid"
                        })[0];
                        
                        
	                     Alfresco.util.Ajax
	                     .jsonGet({
	                        url : dt._getColumnUrl("export"),
	                        successCallback : {
	                           fn : function(response) {
	
	                              var requestParams = {
	                                 fields : [],
	                                 labels : [],
	                                 filter : dt.currentFilter,
	                                 page : 1,
	                                 extraParams : dt.options.extraParams
	                              };
	                              
	                              requestParams.filter.filterParams = dt._createFilterURLParameters(dt.currentFilter, dt.options.filterParameters);
	
	                              for ( var i = 0, ii = response.json.columns.length; i < ii; i++) {
		  							 var column = response.json.columns[i], columnName = column.name.replace(":", "_"), columnLabel = (column.label!="hidden"? column.label :"");
	                                 if (Object.keys(column).includes("label") && ["datasource"].indexOf(column.label) < 0) {
		
		                                 if (column.dataType == "nested" && column.columns) {
		                                    for ( var j = 0; j < column.columns.length; j++) { 
										    var col = column.columns[j];                             
			 								if (Object.keys(col).includes("label") && ["datasource"].indexOf(col.label) < 0) {                                            
			                                       columnName += "|" + col.name.replace(":", "_");
			                               		   columnLabel += "|" + (col.label!="hidden"?col.label:"");    
		                                       }                                        
		                                    }
		                                 }
		                                 requestParams.fields.push({"id":columnName, "label": columnLabel});
	                                 }
	                              }
	
	                              var MAX_RESULTS_UNLIMITED = -1;
	                              
	                              var name = "export";
	                              if(dt.datalistMeta){
									if(dt.datalistMeta.entityName){
										name  += " - "+dt.datalistMeta.entityName;
									}
									
									if( dt.datalistMeta.title){
									  	name+= " - "+dt.datalistMeta.title;
									} else if( dt.datalistMeta.name){
										name+= " - "+dt.datalistMeta.name;
									}
									
								  }
								  name +=".xlsx"
								
	 							 beCPG.util.launchAsyncDownload(name, name, dt._getDataUrl(MAX_RESULTS_UNLIMITED) + "&format=xlsx" , requestParams);  
	                 
	
	                           },
	                           scope : this
	                        }
	                     });
                   

                     }
                  });

   }
   
   YAHOO.Bubbling.fire("registerToolbarButtonAction", {
       actionName : "entity-edit-metadata",
       evaluate : function(asset, entity) {
           return asset.name !== null && asset.name.indexOf("View-properties") == 0 && entity.userAccess.edit;
       },
       fn : function(instance) {

          window.location.href = Alfresco.util.siteURL("edit-metadata?nodeRef="+this.entity.nodeRef+ (this.options.siteId ? "&siteId=" + this.options.siteId : ""));

       }

    });
   
   
   YAHOO.Bubbling.fire("registerToolbarButtonAction", {
       actionName : "entity-add-aspect",
       evaluate : function(asset, entity) {
           return asset.name !== null && asset.name.indexOf("View-properties") == 0 && (entity.userAccess.edit || entity.aspects.includes("bcpg:archivedEntityAspect"));
       },
       fn : function(instance) {
           
           this["onActionManageAspects"].call(this, this.recordData);
           
       }

    });
   
   YAHOO.Bubbling.fire("registerToolbarButtonAction", {
       actionName : "entity-print-metadata",
       evaluate : function(asset, entity) {
           return asset.name !== null && asset.name.indexOf("View-properties") == 0;
       },
       fn : function(instance) {
           var wnd = window.open(Alfresco.constants.URL_PAGECONTEXT+"print-details?nodeRef="+this.entity.nodeRef);
           setTimeout(function() {
               wnd.print();
           }, 3000);
       }

    });
   
  
   
   
   YAHOO.Bubbling.fire("registerToolbarButtonAction", {
       actionName : "entity-refresh-reports",
       evaluate : function(asset, entity) {
           return asset.name !== null && asset.name.indexOf("View-reports") == 0 && !entity.aspects.includes("bcpg:archivedEntityAspect");
       },
       fn : function(instance) {
    	   
    	   var refreshReportButton = YAHOO.util.Selector.query('div.entity-refresh-reports'), me = this;

			Dom.addClass(refreshReportButton, "loading");
			
           var msgPopup = Alfresco.util.PopupManager.displayMessage({
               text : this.msg("message.generate-reports.please-wait"),
               spanClass : "wait",
               displayTime : 0
           });

           Alfresco.util.Ajax.request({
              method : Alfresco.util.Ajax.GET,
              url : Alfresco.constants.PROXY_URI + "becpg/entity/generate-report/node/" + this.entity.nodeRef.replace(":/", "")
                    + "/force",
              successCallback : {
                 fn : function EntityDataListToolbar_onFinish_success(response) {
                	 	//#2147 YAHOO.Bubbling.fire("previewChangedEvent");
                	 Dom.removeClass(refreshReportButton, "loading");
                	 window.location.reload();
                 },
                 scope : this
              },
              failureCallback : {
                 fn : function EntityDataListToolbar_onFinish_failure(response) {
                	 msgPopup.destroy();
                	 Dom.removeClass(refreshReportButton, "loading");
                     Alfresco.util.PopupManager.displayMessage({
                         text : this.msg("message.generate-reports.failure")
                     });
                 },
                 scope : this
              }
           });
       }

    });
   
   YAHOO.Bubbling.fire("registerToolbarButtonAction", {
       actionName : "entity-reports-parameters",
       evaluate : function(asset, entity) {
           return asset.name !== null && asset.name.indexOf("View-reports") == 0  && entity.userAccess.edit;
       },
       fn : function(instance) {
    	   var templateUrl = YAHOO.lang
           .substitute(
                 Alfresco.constants.URL_SERVICECONTEXT + "components/form?popup=true&formId=reports-parameters&itemKind=node&itemId={itemId}&mode=edit&submitType=json&showCancelButton=true",
                 {
                    itemId : this.options.entityNodeRef
                 });

	     var editProductMetadata = new Alfresco.module.SimpleDialog(this.id + "-editReportMetadata");
	
	     editProductMetadata.setOptions(
	           {
	              width : "33em",
	              successMessage : this.msg("message.details.success"),
	              failureMessage : this.msg("message.details.failure"),
	              templateUrl : templateUrl,
	              destroyOnHide : true,
	              doBeforeDialogShow : {
	                 fn : function(p_form, p_dialog) {
	                    Alfresco.util.populateHTML([ p_dialog.id + "-dialogTitle",
	                          this.msg("label.entity-reports-parameters.title") ]);
	                 },
	                 scope : this
	              }
	
	           }).show();
	       }

    });
    
    

		YAHOO.Bubbling
			.fire(
				"registerToolbarButtonAction",
				{
					actionName: "formulate",
					hideLabel: true,
					evaluate: function(asset, entity) {
						return asset.name != null &&
							(asset.name != "View-documents" && asset.name != "View-reports" && asset.name != "activityList" && asset.name != "WUsed")
							&& entity != null && (beCPG.util.contains(entity.aspects,
								"bcpg:productAspect") || entity.type == "bcpg:productSpecification" || entity.type == "qa:batch" || entity.type == "pjt:project" || entity.type == "bcpg:productCollection") 
								&& entity.userAccess.edit && !entity.aspects.includes("bcpg:archivedEntityAspect");
					},
					fn: function(instance) {


						Alfresco.util.PopupManager.displayMessage({
							text: this.msg("message.formulate.please-wait")
						});

						var formulateButton = YAHOO.util.Selector.query('div.formulate');

						Dom.addClass(formulateButton, "loading");

						Alfresco.util.Ajax
							.request({
								method: Alfresco.util.Ajax.GET,
								responseContentType: Alfresco.util.Ajax.JSON,
								url: Alfresco.constants.PROXY_URI + "becpg/remote/formulate?nodeRef=" + this.options.entityNodeRef+"&format=json",
								successCallback: {
									fn: function(response) {
										Alfresco.util.PopupManager.displayMessage({
											text: this.msg("message.formulate.success")
										});

										YAHOO.Bubbling.fire("refreshDataGrids",{ clearCache :true,
			            	    		  cacheTimeStamp : (new Date()).getTime() });
										Dom.removeClass(formulateButton, "loading");
									},
									scope: this
								},
								failureCallback: {
									fn: function(response) {
										if (response.json && response.json.message) {
											Alfresco.util.PopupManager.displayPrompt({
												title: this.msg("message.formulate.failure"),
												text: response.json.message
											});
										} else {
											Alfresco.util.PopupManager.displayMessage({
												text: this.msg("message.formulate.failure")
											});
										}
										Dom.removeClass(formulateButton, "loading");
									},
									scope: this
								}

							});

					}
				});

    
   
   

})();
