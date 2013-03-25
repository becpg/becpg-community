<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">

function main()
{
   AlfrescoUtil.param("nodeRef");
   AlfrescoUtil.param("site", null);
   AlfrescoUtil.param("rootPage", "documentlibrary");
   AlfrescoUtil.param("rootLabelId", "path.documents");
   AlfrescoUtil.param("showFavourite", "true");
   AlfrescoUtil.param("pathMode","false");
   AlfrescoUtil.param("showLikes", "true");
   AlfrescoUtil.param("showComments", "true");
   AlfrescoUtil.param("showQuickShare", "true");
   AlfrescoUtil.param("showDownload", "true");
   AlfrescoUtil.param("showPath", "true");
   var nodeDetails = AlfrescoUtil.getNodeDetails(model.nodeRef, model.site);
   if (nodeDetails)
   {
      model.item = nodeDetails.item;
      model.node = nodeDetails.item.node;
      model.isContainer = nodeDetails.item.node.isContainer;
      model.paths = AlfrescoUtil.getPaths(nodeDetails, model.rootPage, model.rootLabelId);
      model.showQuickShare = (!model.isContainer && model.showQuickShare && config.scoped["Social"]["quickshare"].getChildValue("url") != null).toString();
      model.showComments = ((nodeDetails.item.node.permissions.user["CreateChildren"] || false) && model.showComments).toString();
      model.showDownload = (!model.pathMode  && !model.isContainer && model.showDownload ).toString();
      
      var count = nodeDetails.item.node.properties["fm:commentCount"];
      model.commentCount = (count != undefined ? count : null);
      model.defaultReport = null;
      if(!model.pathMode && nodeDetails.item.node.associations &&  nodeDetails.item.node.associations["rep:reports"]){
      
      	model.reports = nodeDetails.item.node.associations["rep:reports"];
      	
      	
      	for(var j in model.reports){
      		var report = model.reports[j];
      		if(model.defaultReport==null){
      			//Set First as default
      			model.defaultReport = report;
      		}
				if(report.isSelected || report.isDefault){
					//Override with default
					model.defaultReport = report;
					if(report.isSelected){
						//If selected here we are
						break;
					}
				} 
      	}
      	
      }
      
      // Widget instantiation metadata...
      var likes = {};
      if (model.item.likes != null)
      {
         likes.isLiked = model.item.likes.isLiked || false;
         likes.totalLikes = model.item.likes.totalLikes || 0;
      }

      var nodeHeader = {
         id : "NodeHeader",
         name : "beCPG.custom.NodeHeader",
         options : {
            nodeRef : model.nodeRef,
            siteId : model.site,
            rootPage : model.rootPage,
            rootLabelId : model.rootLabelId,
            pathMode : (model.pathMode == "true"),
            showFavourite : (model.showFavourite == "true"),
            showLikes : (model.showLikes == "true"),
            showComments : (model.showComments == "true"),
            showDownload : (model.showDownload == "true"),
            showPath : (model.showPath == "true"),
            displayName : (model.item.displayName != null) ? model.item.displayName : model.item.fileName,
            likes : likes,
            isFavourite : (model.item.isFavourite || false),
            isContainer : model.isContainer,
            sharedId: model.item.node.properties["qshare:sharedId"] || null,
            sharedBy: model.item.node.properties["qshare:sharedBy"] || null,
            itemType : model.item.node.type,
            path :  model.item.location.path,
            itemName : model.item.fileName,
            report : model.defaultReport
         }
      };
      
      if(nodeDetails.item.workingCopy != null && nodeDetails.item.workingCopy.isWorkingCopy)
      {
         nodeHeader.options.showFavourite = false;
         nodeHeader.options.showLikes = false;
         model.showQuickShare = "false";
         model.showComments = "false";
      }
      
      model.widgets = [nodeHeader];
   }
}

main();
