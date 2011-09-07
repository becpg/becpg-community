/**
 * beCPG document actions
 * 
 * @namespace Alfresco
 * @class DocumentActions
 */
(function()
{
	/**
     * Check out entity
     *
     * @override
     * @method onActionCheckOutEntity
     * @param asset {object} Object literal representing entity to be actioned
     */
	Alfresco.DocumentActions.prototype.onActionCheckOutEntity = function DocumentActions_onActionCheckOutEntity(asset)
	{
		var displayName = asset.displayName,
	       nodeRef = new Alfresco.util.NodeRef(asset.nodeRef);				
		    
	    Alfresco.util.Ajax.request(
	    {
	       method: Alfresco.util.Ajax.POST,
	       url: Alfresco.constants.PROXY_URI + "becpg/entity/checkout/node/" + nodeRef.uri.replace(":/", ""),
	       successCallback:
	       {
	          fn: function DocumentActions_oACOP_success(response)
	          {	        	
	        	  this.assetData.nodeRef = response.json.results[0].nodeRef;
	              window.location = this.getActionUrls(this.assetData).documentDetailsUrl;
	          },               
	          scope: this
	       },
	       failureCallback:
	       {
	          fn: function DocumentActions_oACOP_failure(response)
	          {
	             Alfresco.util.PopupManager.displayMessage(
	             {
	                text: this.msg("message.checkout-entity.failure", displayName)
	             });	        	  
	          },
	          scope: this
	       }
	    });
	};
	
	
	
	/**
     * Cancel check out entity
     *
     * @override
     * @method onActionCancelCheckOutEntity
     * @param asset {object} Object literal representing entity to be actioned
     */
	Alfresco.DocumentActions.prototype.onActionCancelCheckOutEntity = function DocumentActions_onActionCancelCheckOutEntity(asset)
    {
       var displayName = asset.displayName,
          nodeRef = new Alfresco.util.NodeRef(asset.nodeRef);

       Alfresco.util.Ajax.request(
	    {
	       method: Alfresco.util.Ajax.POST,
	       url: Alfresco.constants.PROXY_URI + "becpg/entity/cancel-checkout/node/" + nodeRef.uri.replace(":/", ""),
	       successCallback:
	       {
	          fn: function DocumentActions_oACCOP_success(response)
	          {	        	
	        	  this.assetData.nodeRef = response.json.results[0].nodeRef;
	        	  window.location = this.getActionUrls(this.assetData).documentDetailsUrl;
	          },               
	          scope: this
	       },
	       failureCallback:
	       {
	          fn: function DocumentActions_oACCOP_failure(response)
	          {
	             Alfresco.util.PopupManager.displayMessage(
	             {
	                text: this.msg("message.cancel-checkout-entity.failure", displayName)
	             });	        	  
	          },
	          scope: this
	       }
	    });   
    };
    
    /**
     * Check in entity
     *
     * @override
     * @method onActionCheckInEntity
     * @param asset {object} Object literal representing entity to be actioned
     */
	Alfresco.DocumentActions.prototype.onActionCheckInEntity = function DocumentActions_onActionCheckInEntity(asset)
    {
       var displayName = asset.displayName,
          nodeRef = new Alfresco.util.NodeRef(asset.nodeRef),
          version = asset.version;
       
       if (asset.custom && asset.custom.workingCopyVersion)
       {
          version = asset.custom.workingCopyVersion;
       }

       if (!this.newEntityVersion)
       {
          this.newEntityVersion = Alfresco.module.getNewEntityVersionInstance();
       }
		
		this.newEntityVersion.show(
       {
          filename: displayName,
          nodeRef: nodeRef,        
          version: version,
          onNewEntityVersionComplete:
          {
             fn: Alfresco.DocumentActions.prototype.onEntityCheckedIn,
             scope: this
          }
       });	
    };
    
    /**
     * Called from the new version component after a the new version has been created.
     *
     * @method onEntityCheckedIn
     * @param complete {object} Object literal containing details of successful and failed uploads
     */
    Alfresco.DocumentActions.prototype.onEntityCheckedIn = function DocumentActions_onEntityCheckedIn(complete)
    {
    	//TODO : faut-il faire une activity spéciale ?
       // Call the normal callback to post the activity data
       //this.onNewVersionUploadComplete.call(this, complete);
       this.assetData.nodeRef = complete.successful[0].nodeRef;
       // Delay page reloading to allow time for async requests to be transmitted
       YAHOO.lang.later(0, this, function()
       {
          window.location = this.getActionUrls(this.assetData).documentDetailsUrl;
       });
    }
   
   
})();
