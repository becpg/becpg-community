<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">

function main(){
	
	 model.showAdditionalDownloadLinks = false;
	 model.isReport = false;
  	   
	   var nodeDetails = AlfrescoUtil.getNodeDetails(model.nodeRef, model.site);
	   
	   if(nodeDetails){

		   model.isReport =  nodeDetails.item.node.type == "rep:report";
		   if(model.isReport) {
			   var showAdditionalDownloadLinks = config.scoped["reports"]["showAdditionalDownloadLinks"].value;
			   model.showAdditionalDownloadLinks = showAdditionalDownloadLinks!=null && showAdditionalDownloadLinks == "true";
	
			   for (var i=0; i<model.widgets.length; i++)
			   {
			     if (model.widgets[i].id == "WebPreview")
			     {
			       model.widgets[i].options.avoidCachedThumbnail = true;
			     }
			   }
			   
		   }
	   
	   }
	   
	
}

// Start the webscript
main();

