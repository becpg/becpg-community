function main()
{
    // Extract template args
    var itemKind = url.templateArgs["item_kind"];
    var itemId = url.templateArgs["item_id"];
    var nodeRefs = (args.nodeRefs !== null) ? args.nodeRefs : null;

    if (logger.isLoggingEnabled())
    {
        logger.log("json form submission for item:");
        logger.log("\tkind = " + itemKind);
        logger.log("\tid = " + itemId);
    }
   
    if (typeof json !== "undefined")
    {
        // At this point the field names are e.g. prop_cm_name
        // and there are some extra values - hidden fields? These are fields from YUI's datepicker(s)
        // e.g. "template_x002e_form-ui_x002e_form-test_prop_my_date-entry":"2/19/2009"
    }
    else
    {
        if (logger.isWarnLoggingEnabled())
        {
            logger.warn("json object was undefined.");
        }
        
        status.setCode(501, message);
        return;
    }
   
    var repoFormData = new Packages.org.alfresco.repo.forms.FormData();
    var assocToRemoves = [];
    var jsonKeys = json.keys();
    for ( ; jsonKeys.hasNext(); )
    {
        var nextKey = jsonKeys.next();
        
        if (nextKey == "alf_redirect")
        {
           // store redirect url in model
           model.redirect = json.get(nextKey);
           
           if (logger.isLoggingEnabled())
           {
               logger.log("found redirect: " + model.redirect);
           }
        }
        else
        {
           // add field to form data
           repoFormData.addFieldData(nextKey, json.get(nextKey));
           if(nextKey.indexOf("assoc_")>-1 && nextKey.indexOf("_added")>-1){
        	   assocToRemoves.push(nextKey.substring(nextKey.indexOf("assoc_")+6,nextKey.indexOf("_added") ));
           }
           
        }
    }

    var persistedObject = null;
    try
    {
    	
    	var splitted = null;
    	
    	if(args.allPages && args.allPages=="true" && args.queryExecutionId!=null){
    		splitted = bcpg.getSearchResults(args.queryExecutionId);
    	} else if(nodeRefs!=null){
    		splitted =  nodeRefs.split(",")
    	}
    	
    	
    	if(splitted!=null){
	    	for(var i in splitted){
	    		var nodeRef = splitted[i];
	    		if(nodeRef.indexOf("workspace")!=0){
	    			 nodeRef = "workspace://SpacesStore/"+nodeRef;
	    		}
	    		
    			var node = search.findNode(nodeRef);
				
				if (node && node.hasPermission("Write")) {
		    		for(var j in assocToRemoves){
		    			var assocToRemove = assocToRemoves[j].replace("_",":");
		    			var assocs = node.assocs[assocToRemove];
		    			for(z in assocs){
		    				node.removeAssociation(assocs[z],assocToRemove);
		    			}
		    		}
		    		persistedObject = formService.saveForm("node", nodeRef.replace(":/",""), repoFormData);
				}
				
	    	}
    	} //new node 
    	else {	
    		persistedObject = formService.saveForm(itemKind, itemId, repoFormData);
    	}
    }
    catch (error)
    {
        var msg = error.message;
       
        if (logger.isLoggingEnabled())
            logger.log(msg);
       
        // determine if the exception was a FormNotFoundException, if so return
        // 404 status code otherwise return 500
        if (msg.indexOf("FormNotFoundException") != -1)
        {
            status.setCode(404, msg);
          
            if (logger.isLoggingEnabled())
                logger.log("Returning 404 status code");
        }
        else
        {
            status.setCode(500, msg);
          
            if (logger.isLoggingEnabled())
                logger.log("Returning 500 status code");
        }
       
        return;
    }
    
    model.persistedObject = persistedObject.toString();
    model.message = "Successfully persisted form for item [" + itemKind + "]" + itemId;
}

main();
