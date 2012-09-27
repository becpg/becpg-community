if (beCPG.module.EntityDataGridRenderers) {

	YAHOO.Bubbling.fire("registerDataGridRenderer", {
	   propertyName : ["bcpg:product","bcpg:supplier", "bcpg:client", "bcpg:entity",
	                   "bcpg:resourceProduct","cm:content_bcpg:costDetailsListSource",
	                   "bcpg:product_bcpg:packagingListProduct" ],
	   renderer : function(oRecord, data, label, scope) {
		   var url = scope._buildCellUrl(data);
		   return '<span class="' + data.metadata + '" ><a href="' + url + '">'
		         + Alfresco.util.encodeHTML(data.displayValue) + '</a></span>';
	   }

	});

	YAHOO.Bubbling.fire("registerDataGridRenderer", {
	   propertyName : "bcpg:product_bcpg:compoListProduct",
	   renderer : function(oRecord, data, label, scope) {
		   var url = scope._buildCellUrl(data);
		   var padding = (oRecord.getData("itemData")["prop_bcpg_depthLevel"].value - 1) * 15;
		   return '<span class="' + data.metadata + '" style="margin-left:' + padding + 'px;"><a href="' + url + '">'
		         + Alfresco.util.encodeHTML(data.displayValue) + '</a></span>';
	   }

	});
	
	YAHOO.Bubbling.fire("registerDataGridRenderer", {
	   propertyName : "text_bcpg:lkvValue",
	   renderer : function(oRecord, data, label, scope) {
		   if(oRecord.getData("itemData")["prop_bcpg_depthLevel"]!=null){
		      var padding = (oRecord.getData("itemData")["prop_bcpg_depthLevel"].value - 1) * 15;
			   return '<span class="' + data.metadata + '" style="margin-left:' + padding + 'px;">'+ Alfresco.util.encodeHTML(data.displayValue) + '</span>';
		   }
		   return Alfresco.util.encodeHTML(data.displayValue);
		
	   }

	});


	YAHOO.Bubbling.fire("registerDataGridRenderer", {
	   propertyName : ["boolean_bcpg:allergenListVoluntary","boolean_bcpg:allergenListInVoluntary"],
	   renderer : function(oRecord, data, label, scope) {
		   var booleanValueTrue = scope.msg("data.boolean.true");
		   var booleanValueFalse = scope.msg("data.boolean.false");
		   if (data.value) {
			   return '<span class="presentAllergen">'
			         + Alfresco.util.encodeHTML(data.value == true ? booleanValueTrue : booleanValueFalse) + '</span>';
		   } else {
			   return Alfresco.util.encodeHTML(data.value == true ? booleanValueTrue : booleanValueFalse);
		   }
	   }

	});


	YAHOO.Bubbling.fire("registerDataGridRenderer", {
	   propertyName : [ "bcpg:rclReqType", "bcpg:filReqType", "ecm:culReqType" ],
	   renderer : function(oRecord, data, label, scope) {
		   var reqTypeForbidden = scope.msg("data.reqtype.forbidden");
		   var reqTypeTolerated = scope.msg("data.reqtype.tolerated");
		   var reqTypeInfo = scope.msg("data.reqtype.info");
		   if (data.displayValue == "Forbidden") {
			   return '<span class="reqTypeForbidden">' + Alfresco.util.encodeHTML(reqTypeForbidden) + '</span>';
		   } else if (data.displayValue == "Tolerated") {
			   return '<span class="reqTypeTolerated">' + Alfresco.util.encodeHTML(reqTypeTolerated) + '</span>';
		   } else if (data.displayValue == "Info") {
			   return '<span class="reqTypeInfo">' + Alfresco.util.encodeHTML(reqTypeInfo) + '</span>';
		   } else {
			   return Alfresco.util.encodeHTML(data.displayValue);
		   }

	   }

	});

	YAHOO.Bubbling.fire("registerDataGridRenderer", {
	   propertyName : [ "ecm:rlRevisionType", "ecm:culRevision" ],
	   renderer : function(oRecord, data, label, scope) {

		   if (data.displayValue != null) {
			   return scope.msg("data.revisiontype." + data.displayValue.toLowerCase());
		   } else {
			   return Alfresco.util.encodeHTML(data.displayValue);
		   }

	   }

	});

	YAHOO.Bubbling.fire("registerDataGridRenderer", {
	   propertyName : [ "qa:sdlControlPoint", "qa:slControlPoint", "qa:clCharacts" ],
	   renderer : function(oRecord, data, label, scope) {
		   var url = scope._buildCellUrl(data);
		   return '<span class="sample"><a href="' + url + '">' + Alfresco.util.encodeHTML(data.displayValue)
		         + '</a></span>';

	   }

	});

	YAHOO.Bubbling.fire("registerDataGridRenderer", {
	   propertyName : [ "bcpg:cost", "bcpg:allergen", "bcpg:nut", 
	                    "bcpg:ing", "bcpg:geoOrigin", "bcpg:bioOrigin",
	                    "bcpg:geo", "bcpg:microbio", "bcpg:physicoChem",
	                    "bcpg:organo"],
	   renderer : function(oRecord, data, label, scope) {
		   return '<span class="' + data.metadata + '" >' + Alfresco.util.encodeHTML(data.displayValue) + '</span>';
	   }

	});
	
	
	
	YAHOO.Bubbling.fire("registerDataGridRenderer", {
	   propertyName : "bcpg:dynamicCharactValue",
	   renderer : function(oRecord, data, label, scope) {
	   	 var color =   oRecord.getData("itemData")["prop_bcpg_dynamicCharactGroupColor"].value ;
	   	 if(!color){
	   		color = "000000"; 
	   	 }
	   	return '<span style="color:#' +  color + ';">'+ Alfresco.util.encodeHTML(data.displayValue) + '</span>';
	   	 
	   }

	});
	
	
	YAHOO.Bubbling.fire("registerDataGridRenderer", {
	   propertyName : "bcpg:dynamicCharactGroupColor",
	   renderer : function(oRecord, data, label, scope) {
	   	 var color =   oRecord.getData("itemData")["prop_bcpg_dynamicCharactGroupColor"].value ;
	   	 if(!color){
	   		color = "000000"; 
	   	 }
	   	return '<div style="background-color:#' +  color + ';width:15px;height:15px;border: 1px solid; border-radius: 5px;margin-left:15px;"></div></div>';
	   }
	});

}
