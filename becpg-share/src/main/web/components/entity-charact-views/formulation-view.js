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

		   // Finally show the component body here to prevent UI artifacts on YUI
		   // button decoration
		   Dom.setStyle(this.id + "-body", "visibility", "visible");
	   }

	}, true);

})();