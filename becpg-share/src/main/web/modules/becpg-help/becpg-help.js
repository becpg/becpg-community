/**
 * Dashboard BecpgHelp component.
 * 
 * @namespace beCPG
 * @class beCPG.component.BecpgHelp
 */
(function() {
	/**
	 * YUI Library aliases
	 */
	var Dom = YAHOO.util.Dom;

	/**
	 * Dashboard BecpgHelp constructor.
	 * 
	 * @param {String}
	 *           htmlId The HTML id of the parent element
	 * @return {beCPG.component.BecpgHelp} The new component instance
	 * @constructor
	 */
	beCPG.component.BecpgHelp = function BecpgHelp_constructor(htmlId) {
		return beCPG.component.BecpgHelp.superclass.constructor.call(this, "beCPG.component.BecpgHelp", htmlId, [ "button",
		      "container", "datasource", "datatable", "animation" ]);
	};

	YAHOO
	      .extend(
	            beCPG.component.BecpgHelp,
	            Alfresco.component.Base,
	            {


	               /**
						 * Fired by YUI when parent element is available for scripting
						 * 
						 * @method onReady
						 */
	               onReady : function BecpgHelp_onReady() {

		               
		               this.widgets.helpButton = Alfresco.util.createYUIButton(me, "help-button",this.onHelpButton);
		              

	               },

	       
	               onHelpButton: function BecpgHelp__onHelpButton(sType, aArgs, p_obj){
	               	 // Load the gui from the server and let the templateLoaded() method handle the rest.
	                  Alfresco.util.Ajax.request(
	                  {
	                     url: Alfresco.constants.URL_SERVICECONTEXT + "modules/becpg-help",
	                     dataObj:
	                     {
	                        htmlid: this.id
	                     },
	                     successCallback:
	                     {
	                        fn: this.onTemplateLoaded,
	                        scope: this
	                     },
	                     execScripts: true,
	                     failureMessage: "Could not load Help template"
	                  });
	          		 },
	          		 /**
	                 * Called when the AboutShare html template has been returned from the server.
	                 * Creates the YUI gui objects such as buttons and a panel and shows it.
	                 * 
	                 * @method onTemplateLoaded
	                 * @param response {object} a Alfresco.util.Ajax.request response object 
	                 */
	                onTemplateLoaded: function AS_onTemplateLoaded(response)
	                {
	                   // Inject the template from the XHR request into a new DIV element
	                   var containerDiv = document.createElement("div");
	                   containerDiv.innerHTML = response.serverResponse.responseText;
	                   
	                   // The panel is created from the HTML returned in the XHR request, not the container
	                   var panelDiv = Dom.getFirstChild(containerDiv);
	                   this.widgets.panel = Alfresco.util.createYUIPanel(panelDiv, { draggable: false });

	                   this.widgets.panel.show();
	                }
	            });
})();
