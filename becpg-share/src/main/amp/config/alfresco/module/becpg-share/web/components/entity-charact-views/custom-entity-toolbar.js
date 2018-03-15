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
(function() {
   if (beCPG.component.EntityDataListToolbar) {


	   YAHOO.Bubbling
	      .fire(
	            "registerToolbarButtonAction",
	            {
	               actionName : "columns-conf",
	               right : true,
	               evaluate : function(asset, entity) {
	                  return asset.name !== null && !asset.name.indexOf("View-properties") == 0 && !asset.name.indexOf("View-reports") == 0
	                  && !asset.name.indexOf("View-documents") == 0 && !asset.name.indexOf("activityList") == 0 && !asset.name.indexOf("taskList") == 0;
	               },
	               fn : function(instance) {
	            	   this.services.preferences = new Alfresco.service.Preferences();
	            	   
	            	   var me = this;
	            	   var popupKind = "columns-conf";
	            	   var html = '<div class="hd">' + this.msg("header." + popupKind + ".picker") + '</div>';
		       			html += '<div class="bd">';
		       			html += '<form  class="form-container">';
		       			html += '<div class="form-fields '+popupKind+'">';
		       			html += '   <div class="set">';
		       			html += '        <div class="form-field">';
		       			html += '			<div  id="'+this.id+'-columns-list" />'		
		       			html += '          </div>';
		       			html += '       </div>';
		       			html += '    </div>';
		       			html += '<div id="'+this.id+'-'+popupKind+'-ft" class="bdft">';
		       			html += '</div>';
		       			html += '</form></div>';
		       			   
	       			    var containerDiv = document.createElement("div");
	       				containerDiv.innerHTML = html;
		       			
	       				this.widgets.columnsListPanel = Alfresco.util.createYUIPanel(containerDiv, {
	       					draggable : true,
	       					width : "33em"
	       				});
	       				
	       				var hiddenColumnsInPopup = ["bcpg_startEffectivity", "bcpg_endEffectivity", "bcpg_depthLevel"];
	       				
	       				var itemType =  this.options.itemType != null ? this.options.itemType : this.datalistMeta.itemType;
	       				var containerEl = Dom.get(this.id+'-columns-list').parentNode, html = "";
	       				var colCount = 0;
	       				
	       				Alfresco.util.Ajax.jsonGet({
    						url : Alfresco.constants.URL_SERVICECONTEXT + "module/entity-datagrid/config/columns?mode=datagrid-prefs&itemType=" + encodeURIComponent(itemType) + "&clearCache=true",
    						successCallback : {
    							fn : function (response) {
    								var prefs = "fr.becpg.formulation.dashlet.custom.datagrid-prefs"+"."+itemType.replace(":","_");

    								for (var i = 0; i < response.json.columns.length; i++) {
    									var column = response.json.columns[i];
    									var propLabel = column.label;
    									var value = column.name.replace(":", "_");
    									var checked = column.checked ? "checked" : "";
    									
    									if (propLabel!="hidden" && hiddenColumnsInPopup.indexOf(value) < 0) {
    										html += '<li class=""><input id="propSelected-' + i + '" type="checkbox" name="propChecked" value="'+ value +'" '+ checked + '/>' 
    												+ '<label for="propSelected-' + i + '" >' + propLabel + '</label></li>';
    									}
    								}

    								 html = "<span>"+this.msg("label.select-columns.title")
    								 	+"</span><br/><br/><ul style=\"width:" + ((colCount + 1) * 20) + "em;\">" + html + "</ul>";		    

    								containerEl.innerHTML = html;
    								 
    								var divEl = Dom.get(this.id+'-columns-conf-ft');
    								
    								divEl.innerHTML = '<input id="'+this.id+'-bulk-edit-ok" type="button" value="'+this.msg("button.ok")+'" />';
    								
    					            this.widgets.okBkButton = Alfresco.util.createYUIButton(this, "bulk-edit-ok", function (){
	
    					            	var selectedFields = Selector.query('input[type="checkbox"]', containerEl);

    					            	for ( var i in selectedFields) {
	       					 				var fieldId = selectedFields[i].value;
	       					 				var prfs = prefs + "."+fieldId
	       					 				me.services.preferences.set(prfs, {checked : selectedFields[i].checked});
	       					 			}
    					            	
    					            	this.widgets.columnsListPanel.hide();
    					            	
    					            	setTimeout(function(){
    					            		YAHOO.Bubbling.fire("scopedActiveDataListChanged", 
    					            	    		{extraDataParams : "&clearCache=true"}
    					            	    );
    					            	}, 1000);
    					            	
    					            });
    							},
    							scope : this
    						}
    					});
	       				
	       				this.widgets.columnsListPanel.show();
	               },
	               
	            });

	   
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

                        var PAGE_SIZE = 5000;
                        
                        document.location.href = dt._getDataUrl(PAGE_SIZE) + "&format=xlsx&metadata=" + encodeURIComponent(YAHOO.lang.JSON
                              .stringify(dt._buildDataGridParams({"filter":dt.currentFilter})));

                     }
                  });

   }
   
   YAHOO.Bubbling.fire("registerToolbarButtonAction", {
       actionName : "entity-edit-metadata",
       evaluate : function(asset, entity) {
           return asset.name !== null && asset.name.indexOf("View-properties") == 0 && entity.userAccess.edit;
       },
       fn : function(instance) {

          window.location.href = Alfresco.util.siteURL("edit-metadata?nodeRef="+this.entity.nodeRef);

       }

    });
   
   
   YAHOO.Bubbling.fire("registerToolbarButtonAction", {
       actionName : "entity-add-aspect",
       evaluate : function(asset, entity) {
           return asset.name !== null && asset.name.indexOf("View-properties") == 0 && entity.userAccess.edit;
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
           return asset.name !== null && asset.name.indexOf("View-reports") == 0 ;
       },
       fn : function(instance) {
           Alfresco.util.PopupManager.displayMessage({
               text : this.msg("message.generate-reports.please-wait")
           });

           Alfresco.util.Ajax.request({
              method : Alfresco.util.Ajax.GET,
              url : Alfresco.constants.PROXY_URI + "becpg/entity/generate-report/node/" + this.entity.nodeRef.replace(":/", "")
                    + "/force",
              successCallback : {
                 fn : function EntityDataListToolbar_onFinish_success(response) {
                	 	//#2147 YAHOO.Bubbling.fire("previewChangedEvent");
                	 	 window.location.reload();
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
   
   

})();
