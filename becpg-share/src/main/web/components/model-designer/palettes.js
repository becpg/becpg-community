/**
 * @namespace beCPG
 * @class beCPG.component.DesignerPalettes
 */
(function()
{
   /**
    * YUI Library aliases
    */
   var Dom = YAHOO.util.Dom,
      Event = YAHOO.util.Event,
      Selector = YAHOO.util.Selector,
      Bubbling = YAHOO.Bubbling;

   /**
    * Alfresco Slingshot aliases
    */
   var $html = Alfresco.util.encodeHTML,
      $combine = Alfresco.util.combinePaths;

   /**
    * DesignerPalettes constructor.
    * 
    * @param htmlId {String} The HTML id of the parent element
    * @return {beCPG.component.DesignerPalettes} The new DesignerPalettes instance
    * @constructor
    */
   beCPG.component.DesignerPalettes = function(htmlId)
   {
	  this.id = htmlId;
      beCPG.component.DesignerPalettes.superclass.constructor.call(this, "beCPG.component.DesignerPalettes", htmlId, ["button", "container"]);
    
      
      return this;
   };
   
   /**
    * Extend from Alfresco.component.Base
    */
   YAHOO.extend(beCPG.component.DesignerPalettes, Alfresco.component.Base,
   {
  
      /**
     
      /**
       * Fired by YUI when parent element is available for scripting.
       *
       * @method onReady
       */
      onReady: function DesignerPalettes_onReady()
      {
    	  
    	  headers = YUISelector.query("h2", this.id);
    	      
  	      if (YAHOO.lang.isArray(headers))
  	      {
  	    	 for(var i in headers){
  	         // Create twister from the first H2 tag found by the query
  	         Alfresco.util.createTwister(headers[i], this.filterName);
  	    	 }
  	      }
    	
         this.renderDesignerControls();
         
     
      },
      
      
      /**
       * Renders the controls palette
       *
       * @method renderDesignerControls
       */
      renderDesignerControls: function DesignerPalettes_renderDesignerControls()
      {
         var me = this;
         

       // Prepare the XHR callback object
       var callback =
            {
               success: function DesignerPaletteslND_success(oResponse)
               {
                  var results = YAHOO.lang.JSON.parse(oResponse.responseText);

                  if (results)
                  {
                      // Build the controls widget
                      this._buildControls(results);
                  }     
               },
               // If the XHR call is not successful, fire the controls callback anyway
               failure: function DesignerPaletteslND_failure(oResponse)
               {
                  if (oResponse.status == 401)
                  {
                     // Our session has likely timed-out, so refresh to offer the login page
                     window.location.reload();
                  }
                  alert("Unexpected error");
               },
               
               // Callback function scope
               scope: me
            };
       
         
        var uri = Alfresco.constants.PROXY_URI + "becpg/designer/controls";

            // Make the XHR call using Connection Manager's asyncRequest method  
         YAHOO.util.Connect.asyncRequest('GET', uri, callback);
         
         
         
      },
       
      
      
      /**
       * PRIVATE FUNCTIONS
       */

      /**
       * Creates the controls palette
       * @method _buildControls
       * @private
       */
      _buildControls : function DesignerPalettes_buildControls(results){
    	  var controls = Dom.get(this.id + "-form-controls"),
    	  	  sets = Dom.get(this.id + "-form-sets"),
    	  	  liTag ;
    	  
    	  var id, description,fragment;
    	  for(var i in results.controls){
    		  id =  results.controls[i].id;
    		  liTag = document.createElement('li');
    		  liTag.setAttribute('id', 'formControls_'+ id );
    		  liTag.setAttribute('class', 'form-control-'+ id );
    		  liTag.innerHTML = id;
    		  controls.appendChild(liTag);
    		  new beCPG.DnD('formControls_'+ id, this,"control");
    	  } 	 
    	  for(var i in results.sets){
    		  id =  results.sets[i].id;
    		  liTag = document.createElement('li');
    		  liTag.setAttribute('id', 'formSets_'+ id );
    		  liTag.setAttribute('class', 'form-set-'+ id );
    		  liTag.innerHTML = id;
    		  sets.appendChild(liTag);
    		  new beCPG.DnD('formSets_'+ id, this,"set");
    	  } 	 

      }

   });
   
   
   
})();