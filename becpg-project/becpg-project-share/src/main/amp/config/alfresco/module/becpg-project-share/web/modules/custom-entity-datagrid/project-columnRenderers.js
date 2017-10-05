/*******************************************************************************
 *  Copyright (C) 2010-2016 beCPG. 
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
if (beCPG.module.EntityDataGridRenderers) {

    YAHOO.Bubbling.on("dirtyDataTable",function(event,args) {
        if (args && args.length >1) {
            var field = args[1].column.field;
            if(field == "prop_pjt_tlState" || field == "prop_pjt_tlDuration" || field == "prop_pjt_tlWork" || field == "prop_pjt_blBudgetedExpense" || field == "prop_pjt_blBudgetedInvoice" || field == "prop_pjt_ltlTime") {
                YAHOO.Bubbling.fire("refreshDataGrids", {updateOnly : true});
            }
        }    
    }, this);


   YAHOO.Bubbling.fire("registerDataGridRenderer", {
      propertyName : [ "pjt:tlTaskName" ],
      renderer : function(oRecord, data, label, scope) {
      	
      	var padding = 0, className = oRecord.getData("itemData")["prop_pjt_tlIsMilestone"].value ? "task-milestone" : "task";
      	if (oRecord.getData("itemData")["prop_bcpg_depthLevel"] && oRecord.getData("itemData")["prop_bcpg_depthLevel"].value) {
				padding = (oRecord.getData("itemData")["prop_bcpg_depthLevel"].value - 1) * 25;
			}      	
         
         return '<span class="' + className + '" style="margin-left:' + padding + 'px;" >' + Alfresco.util.encodeHTML(data.displayValue) + '</span>';
      }

   });

 YAHOO.Bubbling.fire("registerDataGridRenderer", {
      propertyName : [ "pjt:tlState", "pjt:dlState" ],
      renderer : function(oRecord, data, label, scope) {
         return '<span class="' + "task-" + data.value.toLowerCase() + '" title="' + data.displayValue + '" />';
      }

   });
   
  
   
   YAHOO.Bubbling.fire("registerDataGridRenderer", {
	      propertyName : [ "pjt:alData" ],
	      renderer : function(oRecord, data, label, scope) {
	    	  var activityType = oRecord.getData("itemData")["prop_pjt_alType"].value;
	    	  var user = oRecord.getData("itemData")["prop_pjt_alUserId"];
	    	  var dateCreated = oRecord.getData("itemData")["prop_cm_created"];
	    	  var html = "";
              if(data.title){
            	  var title = "";
            	  var className = oRecord.getData("itemData")["prop_pjt_alDeliverableId"].value!=null ? "deliverable" : 
            		  oRecord.getData("itemData")["prop_pjt_alTaskId"].value!=null ? "task" : "project"; 
            	  title = "<span class=\""+className+"\">"+Alfresco.util.encodeHTML(data.title)+"</span>";
            	  if(activityType == "State"){
            		  title = scope.msg("project.activity.state.change", title, scope.msg("data."+className+"state." +data.beforeState.toLowerCase()), scope.msg("data."+className+"state."+data.afterState.toLowerCase()));
            	  } else if(activityType == "Comment"){
            		 title  = scope.msg("project.activity.comment."+data.activityEvent.toLowerCase(), title);
            	  } else if(activityType == "Content"){
            		  if(data.activityEvent == "Delete"){
            			  title = '<span class="doc-file"><img src="' + Alfresco.constants.URL_RESCONTEXT + 'components/images/filetypes/' + Alfresco.util
 	                     .getFileIcon(data.title, "cm:content", 16) + '" />'+Alfresco.util.encodeHTML(data.title)+'</span>';
            		  } else {
	            		 title = '<span class="doc-file"><a  href="' +  beCPG.util.entityURL(oRecord.getData("siteId"),data.contentNodeRef, "document") + 
	            		 '"><img src="' + Alfresco.constants.URL_RESCONTEXT + 'components/images/filetypes/' + Alfresco.util
	                     .getFileIcon(data.title, "cm:content", 16) + '" />'+Alfresco.util.encodeHTML(data.title)+'</a></span>';
            		  }
            		 title  = scope.msg("project.activity.content."+data.activityEvent.toLowerCase(), title);
            	  }
            	  html += '<div class="project-activity-details">';
    	          html += '   <div class="icon">' + Alfresco.Share.userAvatar(user.value,32) + '</div>';
    	          html += '   <div class="details">';
    	          html += '      <span class="user-info">';
    	          html += Alfresco.util.userProfileLink(user.value, user.displayValue, 'class="theme-color-1"') + ' ';
    	          html += '      </span>';
    	          html += '      <span class="date-info">';
    	          html += Alfresco.util.relativeTime(Alfresco.util.fromISO8601(dateCreated.value)) + '<br/>';
    	          html += '      </span>';
    	          html += '      <div class="activity-title">' + title + '</div>';
    	          if(data.content){
    	         	html += '      <div class="activity-content">' + (data.content) + '</div>';
    	          }
    	          html += '   </div>';
    	          html += '   <div class="clear"></div>';
    	          html += '</div>';
            	  
              }
	    	  return html;

	      }

	   });
   
   
   YAHOO.Bubbling.fire("registerDataGridRenderer", {
	      propertyName : "pjt:slScreening",
	      renderer : function(oRecord, data, label, scope) {

	         return '<div class="scoreList-screening">' + data.displayValue + '</div>';
	      }
	   });
   
   
   YAHOO.Bubbling.fire("registerDataGridRenderer", {
	      propertyName : [ "pjt:blItem" ],
	      renderer : function(oRecord, data, label, scope) {
	      	
	      	var padding = 0;
	      	if (oRecord.getData("itemData")["prop_bcpg_depthLevel"] && oRecord.getData("itemData")["prop_bcpg_depthLevel"].value) {
				padding = (oRecord.getData("itemData")["prop_bcpg_depthLevel"].value - 1) * 25;
			}      	
	         
	         return '<span style="margin-left:' + padding + 'px;" >' + Alfresco.util.encodeHTML(data.displayValue) + '</span>';
	      }

	   });
   
   YAHOO.Bubbling.fire("registerDataGridRenderer", {
      propertyName : [ "pjt:tlRealDuration" ],
      renderer : function(oRecord, data, label, scope) {
      	
      	var className = "";
      	if (data.value && oRecord.getData("itemData")["prop_pjt_tlDuration"].value && data.value > oRecord.getData("itemData")["prop_pjt_tlDuration"].value) {
      		className = "red";
		}      	
         return '<span class="' + className + '" >' + Alfresco.util.encodeHTML(data.displayValue) + '</span>';
      }

   });
   
   YAHOO.Bubbling.fire("registerDataGridRenderer", {
      propertyName : [ "pjt:tlLoggedTime" ],
      renderer : function(oRecord, data, label, scope) {
      	
      	var className = "";
      	if (data.value && oRecord.getData("itemData")["prop_pjt_tlWork"].value && data.value > oRecord.getData("itemData")["prop_pjt_tlWork"].value) {
      		className = "red";
			}      	
         
         return '<span class="' + className + '" >' + Alfresco.util.encodeHTML(data.displayValue) + '</span>';
      }

   });
   
   YAHOO.Bubbling.fire("registerDataGridRenderer", {
      propertyName : [ "pjt:tlCapacity" ],
      renderer : function(oRecord, data, label, scope) {
      	
      	var className = "";
      	if (data.value && data.value > 100) {
      		className = "red";
			}      	
         
         return '<span class="' + className + '" >' + Alfresco.util.encodeHTML(data.displayValue) + '</span>';
      }

   });
   
   /* Align cost to the right
   YAHOO.Bubbling.fire("registerDataGridRenderer", {
	      propertyName : [ "pjt:invoice","pjt:expense","pjt:blBudgetedExpense", "pjt:blBudgetedInvoice",, "pjt:blProfit" ,"pjt:projectBudgetedCost","pjt:tlFixedCost","pjt:resourceCostValue" ],
	      renderer : function(oRecord, data, label, scope, i, ii, elCell, oColumn) {
	      	
	    	  if(data.value!=null){
	    		  Dom.setStyle(elCell, "text-align", "right");  
	    		  return  (new Intl.NumberFormat(Alfresco.constants.JS_LOCALE.replace("_","-") ,{minimumFractionDigits : 2, maximumFractionDigits : 2 })).format( data.value);
	    	  }
	    	  return "";
	    	  
	      }

	 });
	*/
   
}
