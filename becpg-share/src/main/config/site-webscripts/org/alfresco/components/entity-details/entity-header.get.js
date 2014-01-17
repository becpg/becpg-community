/*******************************************************************************
 *  Copyright (C) 2010-2014 beCPG. 
 *   
 *  This file is part of beCPG 
 *   
 *  beCPG is free software: you can redistribute it and/or modify 
 *  it under the terms of the GNU Lesser General Public License as published by 
 *  the Free Software Foundation, either version 3 of the License, or 
 *  (at your option) any later version. 
 *   
 *  beCPG is distributed in the hope that it will be useful, 
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of 
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the 
 *  GNU Lesser General Public License for more details. 
 *   
 *  You should have received a copy of the GNU Lesser General Public License along with beCPG.
 *   If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
<import resource="classpath:/alfresco/templates/org/alfresco/import/alfresco-util.js">

function main()
{
   AlfrescoUtil.param("nodeRef");
   AlfrescoUtil.param("site", null);
   AlfrescoUtil.param("rootPage", "documentlibrary");
   AlfrescoUtil.param("rootLabelId", "path.documents");
   AlfrescoUtil.param("showOnlyLocation", "false");
   AlfrescoUtil.param("showFavourite", "true");
   AlfrescoUtil.param("showLikes", "true");
   AlfrescoUtil.param("showComments", "true");
   AlfrescoUtil.param("showQuickShare", "true");
   AlfrescoUtil.param("showDownload", "true");
   AlfrescoUtil.param("showPath", "true");
   AlfrescoUtil.param("libraryRoot", null);
   AlfrescoUtil.param("pagecontext", null);
   var nodeDetails = AlfrescoUtil.getNodeDetails(model.nodeRef, model.site, null, model.libraryRoot);
   if (nodeDetails)
   {
      model.item = nodeDetails.item;
      model.node = nodeDetails.item.node;
      model.isContainer = nodeDetails.item.node.isContainer;
      model.paths = AlfrescoUtil.getPaths(nodeDetails, model.rootPage, model.rootLabelId);
      model.showQuickShare = (!model.isContainer && model.showQuickShare && config.scoped["Social"]["quickshare"].getChildValue("url") != null).toString();
      model.isWorkingCopy = (model.item && model.item.workingCopy && model.item.workingCopy.isWorkingCopy) ? true : false;
      model.showFavourite = (model.isWorkingCopy ? false : model.showFavourite).toString();
      model.showLikes = (model.isWorkingCopy ? false : model.showLikes).toString();
      model.showComments = (model.isWorkingCopy ? false : ((nodeDetails.item.node.permissions.user["CreateChildren"] || false) && model.showComments)).toString();
      model.showDownload = (!model.isContainer && model.showDownload).toString();
      model.showOnlyLocation = model.showOnlyLocation.toString();
      var count = nodeDetails.item.node.properties["fm:commentCount"];
      model.commentCount = (count != undefined ? count : null);
      model.defaultReport = null;
      if(model.pathMode != "true" && nodeDetails.item.node.associations &&  nodeDetails.item.node.associations["rep:reports"]){
      
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
            actualSiteId: model.item.location.site != null ? model.item.location.site.name : null,
            rootPage : model.rootPage,
            rootLabelId : model.rootLabelId,
            showOnlyLocation: (model.showOnlyLocation == "true"),
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
            pagecontext: model.pagecontext,
            libraryRoot: model.libraryRoot,
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
