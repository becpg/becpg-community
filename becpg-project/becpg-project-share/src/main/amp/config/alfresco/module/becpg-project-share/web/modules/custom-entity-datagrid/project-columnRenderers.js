/*******************************************************************************
 *  Copyright (C) 2010-2017 beCPG. 
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
	      propertyName : "pjt:slScreening",
	      renderer : function(oRecord, data, label, scope) {

	         return '<div class="scoreList-screening">' + data.displayValue + '</div>';
	      }
	   });
   
   YAHOO.Bubbling.fire("registerDataGridRenderer", {
      propertyName : "pjt:slScore",
      renderer : function(oRecord, data, label, scope) {

      	var className="";
      	if(data.value != null){
      		if(data.value < 25){
      			className="score-red";
      		}
      		else if(data.value < 50){
      			className="score-orange";
      		}
      		else if(data.value < 75){
      			className="score-blue";
      		}
      		else{
      			className="score-green";
      		}
      	}
      	return '<span class="' + className + '" >' + Alfresco.util.encodeHTML(data.displayValue) + '</span>';         
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
   
}
