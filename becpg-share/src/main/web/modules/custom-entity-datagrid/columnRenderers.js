if (beCPG.module.EntityDataGrid) {

	YAHOO.Bubbling.fire("registerDataGridRenderer", {
	   propertyName : "bcpg:product",
	   renderer : function(oRecord,data, label, scope) {
		   var url = scope._buildCellUrl(data);
		   return '<a href="' + url + '">' + Alfresco.util.encodeHTML(data.displayValue) + '</a>';
	   }

	});

	YAHOO.Bubbling.fire("registerDataGridRenderer", {
	   propertyName : "bcpg:product_bcpg:compoListProduct",
	   renderer : function(oRecord,data, label, scope) {
		   var url = scope._buildCellUrl(data);
		   var padding = 5 + oRecord.getData("itemData")["prop_bcpg_depthLevel"].value * 15;
		   return '<span class="' + data.metadata + '" style="padding-left:' + padding + 'px;"><a href="' + url + '">'
		         + Alfresco.util.encodeHTML(data.displayValue) + '</a></span>';
	   }

	});

	YAHOO.Bubbling.fire("registerDataGridRenderer",
	      {
	         propertyName : "bcpg:product_bcpg:packagingListProduct",
	         renderer : function(oRecord,data, label, scope) {
		         var url = scope._buildCellUrl(data);
		         return '<span class="' + data.metadata + '"><a href="' + url + '">' + Alfresco.util.encodeHTML(data.displayValue)
		               + '</a></span>';
	         }

	      });

	YAHOO.Bubbling.fire("registerDataGridRenderer", {
	   propertyName : "boolean_bcpg:allergenListVoluntary",
	   renderer : function(oRecord,data, label, scope) {
		   var booleanValueTrue = scope.msg("data.boolean.true");
		   var booleanValueFalse = scope.msg("data.boolean.false");
		   if (data.displayValue) {
			   return '<span class="presentAllergen">'
			         + Alfresco.util.encodeHTML(data.displayValue == true ? booleanValueTrue : booleanValueFalse) + '</span>';
		   } else {
			   return Alfresco.util.encodeHTML(data.displayValue == true ? booleanValueTrue : booleanValueFalse);
		   }
	   }

	});

	YAHOO.Bubbling.fire("registerDataGridRenderer", {
	   propertyName : "boolean_bcpg:allergenListInVoluntary",
	   renderer : function(oRecord,data, label, scope) {
		   var booleanValueTrue = scope.msg("data.boolean.true");
		   var booleanValueFalse = scope.msg("data.boolean.false");
		   if (data.displayValue) {
			   return '<span class="presentAllergen">'
			         + Alfresco.util.encodeHTML(data.displayValue == true ? booleanValueTrue : booleanValueFalse) + '</span>';
		   } else {
			   return Alfresco.util.encodeHTML(data.displayValue == true ? booleanValueTrue : booleanValueFalse);
		   }
	   }

	});

	YAHOO.Bubbling.fire("registerDataGridRenderer", {
	   propertyName : [ "bcpg:supplier", "bcpg:client", "bcpg:entity", "bcpg:resourceProduct" ],
	   renderer : function(oRecord,data, label, scope) {
		   var url = scope._buildCellUrl(data);
		   var html = '<a href="' + url + '">';
		   html += '<img src="' + Alfresco.constants.URL_RESCONTEXT + 'components/images/filetypes/'
		         + Alfresco.util.getFileIcon(data.displayValue, (data.metadata == "container" ? 'cm:folder' : null), 16)
		         + '" width="16" alt="' + Alfresco.util.encodeHTML(data.displayValue) + '" title="' + Alfresco.util.encodeHTML(data.displayValue) + '" />';
		   html += ' ' + Alfresco.util.encodeHTML(data.displayValue) + '</a>';
		   return html;
	   }

	});

	YAHOO.Bubbling.fire("registerDataGridRenderer", {
	   propertyName : [ "bcpg:rclReqType", "bcpg:filReqType", "ecm:culReqType" ],
	   renderer : function(oRecord,data, label, scope) {
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
	   renderer : function(oRecord,data, label, scope) {

		   if (data.displayValue != null) {
			   return scope.msg("data.revisiontype." + data.displayValue.toLowerCase());
		   } else {
			   return Alfresco.util.encodeHTML(data.displayValue);
		   }

	   }

	});

	YAHOO.Bubbling.fire("registerDataGridRenderer", {
	   propertyName : [ "qa:sdlControlPoint", "qa:slControlPoint", "qa:clCharacts" ],
	   renderer : function(oRecord,data, label, scope) {
		   var url = scope._buildCellUrl(data);
		   return '<span class="sample"><a href="' + url + '">' + Alfresco.util.encodeHTML(data.displayValue) + '</a></span>';

	   }

	});

}
