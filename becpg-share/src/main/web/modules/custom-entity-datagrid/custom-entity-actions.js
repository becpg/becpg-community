/**
 * Entity Data Grid Custom Actions module
 * 
 * @namespace beCPG.module
 * @class beCPG.module.CustomEntityDataGridActions
 */
(function() {

	/**
	 * beCPG.module.CustomEntityDataGridActions implementation
	 */
	beCPG.module.CustomEntityDataGridActions = {};
	beCPG.module.CustomEntityDataGridActions.prototype = {
	   /**
		 * ACTIONS WHICH ARE LOCAL TO THE DATAGRID COMPONENT
		 */
	 
	   /**
		 * @method onActionShowDetails
		 * @param items
		 *           {Object | Array} Object literal representing the Data Item to
		 *           be actioned, or an Array thereof
		 */
	   onActionShowDetails : function EntityDataGrid_onActionShowDetails(p_items) {
		   var items = YAHOO.lang.isArray(p_items) ? p_items : [ p_items ], nodeRefs = [];

		   for ( var i = 0, ii = items.length; i < ii; i++) {
			   nodeRefs.push(items[i].nodeRef);
		   }

		   var url = Alfresco.constants.URL_SERVICECONTEXT + "modules/entity-charact-details/entity-charact-details"
		         + "?entityNodeRef=" + this.options.entityNodeRef + "&itemType="
		         + encodeURIComponent(this.options.itemType != null ? this.options.itemType : this.datalistMeta.itemType)
		         + "&dataListName=" + encodeURIComponent(this.datalistMeta.name) + "&dataListItems=" + nodeRefs.join(",");

		   Alfresco.util.Ajax.request({
		      url : url,
		      dataObj : {
			      htmlid : this.id
		      },
		      successCallback : {
		         fn : function(response) {
			         // Inject the template from the XHR request into a new DIV
						// element
			         var containerDiv = document.createElement("div");
			         containerDiv.innerHTML = response.serverResponse.responseText;

			         // The panel is created from the HTML returned in the XHR
						// request, not the container
			         var panelDiv = Dom.getFirstChild(containerDiv);
			         this.widgets.panel = Alfresco.util.createYUIPanel(panelDiv, {
			            draggable : true,
			            width : "50em"
			         });

			         this.widgets.panel.show();

		         },
		         scope : this
		      },
		      failureMessage : "Could not load dialog template from '" + url + "'.",
		      scope : this,
		      execScripts : true
		   });

	   },
	   
	   onActionBulkEdit : function EntityDataGrid_onActionShowDetails(p_items) {
		   var items = YAHOO.lang.isArray(p_items) ? p_items : [ p_items ];

		   var query = "";
		   for ( var i = 0, ii = items.length; i < ii; i++) {
			   if(query.length>0){
			   	query+=" OR ";
			   }
			   if(items[i].itemData["assoc_bcpg_compoListProduct"]!=null){
			   	//TODO
			   	query+="ID:\""+items[i].itemData["assoc_bcpg_compoListProduct"][0].value+"\"";
			   }
		   }
		   
		   window.location = Alfresco.constants.URL_PAGECONTEXT + "bulk-edit?t=" + encodeURIComponent(query)+"&a=true&r=true";
	   }

	};
	

 /**
  * Augment prototype with Common Actions module
  */
 YAHOO.lang.augmentProto(beCPG.module.EntityDataGridActions, beCPG.module.CustomEntityDataGridActions);
	
})();