
   
   YAHOO.Bubbling.fire("registerAction",
   {
	   actionName: "onActionCheckInEntity",
   		  fn: function onActionCheckInEntity(asset)
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
   			
   			
   		  }
   });
   
   YAHOO.Bubbling.fire("registerAction",
   {   
   	      actionName: "onActionCheckOutEntity",
   	      fn: function onActionCheckOutEntity(record)
   	      {     
   	   	   
   	   	   var displayName = record.displayName;

  		       this.modules.actions.genericAction(
  		       {
  		         success:
  		         {
  		            event:
  		            {
  		              name: "metadataRefresh"
  		            },
  		            message: this.msg("message.checkout-entity.success", displayName)
  		         },
  		         failure:
  		         {
  		            message: this.msg("message.checkout-entity.failure", displayName)
  		         },
  		         webscript:
  		         {
  		            method: Alfresco.util.Ajax.POST,
  		            name: "becpg/entity/checkout/node/{nodeRef}",
  		            params:
  		            {
  		              nodeRef: record.jsNode.nodeRef.uri
  		            }
  		         }
  		       });
   	   	   
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
   	   	}
   });
   
   
   YAHOO.Bubbling.fire("registerAction",
   {   
   	   		  actionName: "onActionCancelCheckOutEntity",
   	   		  fn: function onActionCancelCheckOutEntity(record)
   	   		  {     
   	   			  
   	   			  var displayName = record.displayName;

   	   		       this.modules.actions.genericAction(
   	   		       {
   	   		         success:
   	   		         {
   	   		            event:
   	   		            {
   	   		              name: "metadataRefresh"
   	   		            },
   	   		            message: this.msg("message.cancel-checkout-entity.success", displayName)
   	   		         },
   	   		         failure:
   	   		         {
   	   		            message: this.msg("message.cancel-checkout-entity.failure", displayName)
   	   		         },
   	   		         webscript:
   	   		         {
   	   		            method: Alfresco.util.Ajax.POST,
   	   		            name: "becpg/entity/cancel-checkout/node/{nodeRef}",
   	   		            params:
   	   		            {
   	   		              nodeRef: record.jsNode.nodeRef.uri
   	   		            }
   	   		         }
   	   		       });
   	   			  
   	   		  }
    });
   
//   /**
//    * Called from the new version component after a the new version has been created.
//    *
//    * @method onEntityCheckedIn
//    * @param complete {object} Object literal containing details of successful and failed uploads
//    */
//   Alfresco.DocumentActions.prototype.onEntityCheckedIn = function DocumentActions_onEntityCheckedIn(complete)
//   {
//   	//TODO : faut-il faire une activity spéciale ?
//      // Call the normal callback to post the activity data
//      //this.onNewVersionUploadComplete.call(this, complete);
//      this.assetData.nodeRef = complete.successful[0].nodeRef;
//      // Delay page reloading to allow time for async requests to be transmitted
//      YAHOO.lang.later(0, this, function()
//      {
//        window.location = this.getActionUrls(this.assetData).documentDetailsUrl;
//      });
//   };
//   
   