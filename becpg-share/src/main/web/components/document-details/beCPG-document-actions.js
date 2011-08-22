/**
 * beCPG document actions
 * 
 * @namespace Alfresco
 * @class DocumentActions
 */
(function()
{
	/**
     * Check out product
     *
     * @override
     * @method onActionCheckOutProduct
     * @param asset {object} Object literal representing product to be actioned
     */
	Alfresco.DocumentActions.prototype.onActionCheckOutProduct = function DocumentActions_onActionCheckOutProduct(asset)
	{
		var displayName = asset.displayName,
	       nodeRef = new Alfresco.util.NodeRef(asset.nodeRef);				
		    
	    Alfresco.util.Ajax.request(
	    {
	       method: Alfresco.util.Ajax.POST,
	       url: Alfresco.constants.PROXY_URI + "becpg/product/checkout/node/" + nodeRef.uri.replace(":/", ""),
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
	                text: this.msg("message.checkout-product.failure", displayName)
	             });	        	  
	          },
	          scope: this
	       }
	    });
	};
	
	
	
	/**
     * Cancel check out product
     *
     * @override
     * @method onActionCancelCheckOutProduct
     * @param asset {object} Object literal representing product to be actioned
     */
	Alfresco.DocumentActions.prototype.onActionCancelCheckOutProduct = function DocumentActions_onActionCancelCheckOutProduct(asset)
    {
       var displayName = asset.displayName,
          nodeRef = new Alfresco.util.NodeRef(asset.nodeRef);

       Alfresco.util.Ajax.request(
	    {
	       method: Alfresco.util.Ajax.POST,
	       url: Alfresco.constants.PROXY_URI + "becpg/product/cancel-checkout/node/" + nodeRef.uri.replace(":/", ""),
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
	                text: this.msg("message.cancel-checkout-product.failure", displayName)
	             });	        	  
	          },
	          scope: this
	       }
	    });   
    };
    
    /**
     * Check in product
     *
     * @override
     * @method onActionCheckInProduct
     * @param asset {object} Object literal representing product to be actioned
     */
	Alfresco.DocumentActions.prototype.onActionCheckInProduct = function DocumentActions_onActionCheckInProduct(asset)
    {
       var displayName = asset.displayName,
          nodeRef = new Alfresco.util.NodeRef(asset.nodeRef),
          version = asset.version;
       
       if (asset.custom && asset.custom.workingCopyVersion)
       {
          version = asset.custom.workingCopyVersion;
       }

       if (!this.newProductVersion)
       {
          this.newProductVersion = Alfresco.module.getNewProductVersionInstance();
       }
		
		this.newProductVersion.show(
       {
          filename: displayName,
          nodeRef: nodeRef,        
          version: version,
          onNewProductVersionComplete:
          {
             fn: Alfresco.DocumentActions.prototype.onProductCheckedIn,
             scope: this
          }
       });	
    };
    
    /**
     * Called from the new version component after a the new version has been created.
     *
     * @method onProductCheckedIn
     * @param complete {object} Object literal containing details of successful and failed uploads
     */
    Alfresco.DocumentActions.prototype.onProductCheckedIn = function DocumentActions_onProductCheckedIn(complete)
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
