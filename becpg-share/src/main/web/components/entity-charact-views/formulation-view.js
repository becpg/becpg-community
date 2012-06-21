(function() {
	/**
	 * YUI Library aliases
	 */
	var Dom = YAHOO.util.Dom;

	/**
	 * FormulationView constructor.
	 * 
	 * @param htmlId
	 *           {String} The HTML id of the parent element
	 * @return {beCPG.component.FormulationView} The new FormulationView instance
	 * @constructor
	 */
	beCPG.component.FormulationView = function(htmlId) {

		beCPG.component.FormulationView.superclass.constructor.call(this, "beCPG.component.FormulationView", htmlId, [
		      "button", "container" ]);

		var dataGridModuleCount = 1;

		YAHOO.Bubbling.on("dataGridReady", function(layer, args) {
			if (dataGridModuleCount == 3) {
				// Initialize the browser history management library
				try {
					YAHOO.util.History.initialize("yui-history-field", "yui-history-iframe");
				} catch (e2) {
					/*
					 * The only exception that gets thrown here is when the browser
					 * is not supported (Opera, or not A-grade)
					 */
					Alfresco.logger.error(this.name + ": Couldn't initialize HistoryManager.", e2);
					var obj = args[1];
					if ((obj !== null) && (obj.entityDataGridModule !== null)) {
						obj.entityDataGridModule.onHistoryManagerReady();
					}
				}
			}
			dataGridModuleCount++;
		});

		return this;
	};

	/**
	 * Extend from Alfresco.component.Base
	 */
	YAHOO.extend(beCPG.component.FormulationView, Alfresco.component.Base);

	/**
	 * Augment prototype with main class implementation, ensuring overwrite is
	 * enabled
	 */
	YAHOO.lang.augmentObject(beCPG.component.FormulationView.prototype, {
	   /**
		 * Object container for initialization options
		 * 
		 * @property options
		 * @type object
		 */
	   options : {
	      /**
			 * Current siteId.
			 * 
			 * @property siteId
			 * @type string
			 * @default ""
			 */
	      siteId : "",

	      /**
			 * Current entityNodeRef.
			 * 
			 * @property entityNodeRef
			 * @type string
			 * @default ""
			 */
	      entityNodeRef : ""

	   },

	   /**
		 * Fired by YUI when parent element is available for scripting.
		 * 
		 * @method onReady
		 */
	   onReady : function FormulationView_onReady() {

		   this.widgets.formulateButton = Alfresco.util.createYUIButton(this, "formulateButton", this.onFormulate, {
			   disabled : false
		   });
		   
		  //Toolbar contribs
			var controls = Dom.getChildren("toolbar-contribs-"+this.id);
		   for(var el in controls){
		   	(new  YAHOO.util.Element("toolbar-contribs")).appendChild(controls[el]);
		   }

		   // Finally show the component body here to prevent UI artifacts on YUI
		   // button decoration
		   Dom.setStyle(this.id + "-body", "visibility", "visible");
	   },

	   /**
		 * Formulate button click handler
		 * 
		 * @method onFormulate
		 * @param e
		 *           {object} DomEvent
		 * @param p_obj
		 *           {object} Object passed back from addListener method
		 */
	   onFormulate : function FormulationView_onFormulate(e, p_obj) {
		   Alfresco.util.PopupManager.displayMessage({
			   text : this.msg("message.formulate.please-wait")
		   });

		   Alfresco.util.Ajax.request({
		      method : Alfresco.util.Ajax.GET,
		      url : Alfresco.constants.PROXY_URI + "becpg/product/formulate/node/"
		            + this.options.entityNodeRef.replace(":/", ""),
		      successCallback : {
		         fn : function FormulationView_onFormulate_success(response) {
			         Alfresco.util.PopupManager.displayMessage({
				         text : this.msg("message.formulate.success")
			         });

			         YAHOO.Bubbling.fire("refreshDataGrids");

		         },
		         scope : this
		      },
		      failureCallback : {
		         fn : function FormulationView_onFormulate_failure(response) {
			         if (response.message != null) {
				         Alfresco.util.PopupManager.displayPrompt({
					         text : response.message
				         });
			         } else {
				         Alfresco.util.PopupManager.displayMessage({
					         text : this.msg("message.formulate.failure")
				         });
			         }
		         },
		         scope : this
		      }
		   });
	   }

	}, true);
})();