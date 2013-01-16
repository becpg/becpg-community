// Declare namespace...
(function()
{
	
	/**
    * YUI Library aliases
    */
   var Dom = YAHOO.util.Dom;

   /**
    * Alfresco Slingshot aliases
    */
   var $html = Alfresco.util.encodeHTML,
      $combine = Alfresco.util.combinePaths,
      $siteURL = Alfresco.util.siteURL,
      $isValueSet = Alfresco.util.isValueSet;

	
	
  // Define constructor...
  beCPG.custom.DocumentList = function CustomDocumentList_constructor(htmlId)
  {
    beCPG.custom.DocumentList.superclass.constructor.call(this, htmlId);
    
 	 YAHOO.Bubbling.on("doclistMetadata", this.onDoclistMetadata,this);
    
    return this;
  };

  // Extend default DocumentList...
  YAHOO.extend(beCPG.custom.DocumentList, Alfresco.DocumentList,
  {
	  
	  onDoclistMetadata : function (layer, args){
		

		  var metadata = args[1].metadata, me = this;
		  //beCPG 
        if (metadata!=null 
         		&& metadata.parent!=null 
         		&& metadata.parent.type == "bcpg:entityFolder")
         {
      	  
            var div = Dom.get(me.id + "-becpg-entityFolder-instructions"),
            		instructionKey,
            		entityFolderClassName = metadata.parent.properties["bcpg:entityFolderClassName"], 
            		instructions;
            
                if(entityFolderClassName!=null){
                	entityFolderType = entityFolderClassName.split("}")[1];
                } else {
                	entityFolderType = "finishedProduct";
                }
  	        	   //same message for every product
  	        	   if(entityFolderType == "rawMaterial" || entityFolderType == "finishedProduct" || 
  	        			   entityFolderType == "semiFinishedProduct" || entityFolderType == "localSemiFinishedProduct" ||
  	        			   entityFolderType == "packagingKit" || entityFolderType == "packagingMaterial" ||
  	        			   entityFolderType == "resourceProduct"){
  	        		   instructionKey = "product";
  	        	   }
  	        	   else{
  	        		   instructionKey = entityFolderType;
  	        	   }
           
            
            instructions = "<img  src='/share/res/components/images/filetypes/generic-" + entityFolderType + "-32.png'>";
            instructions += "<span >" + Alfresco.util.message("page.documentlibrary.instructions." + instructionKey) + "</span>";

            
            div.innerHTML = instructions;
            Dom.removeClass(div, "hidden");
            
         } else {
             Dom.addClass(me.id + "-becpg-entityFolder-instructions", "hidden");
         }
        //End beCPG
	  }
	  
	
  });
  


  /**
   * Generate URL for a file- or folder-link that may be located within a different Site
   *
   * @method generateFileFolderLinkMarkup
   * @param record {object} Item record
   * @return {string} Mark-up for use in node attribute
   * <pre>
   *       Folders: Navigate into the folder (ajax)
   *       Documents: Navigate to the details page (page)
   *    Links: Same site (or Repository mode)
   *       Links to folders: Navigate into the folder (ajax)
   *       Links to documents: Navigate to the details page (page)
   *    Links: Different site
   *       Links to folders: Navigate into the site & folder (page)
   *       Links to documents: Navigate to the details page within the site (page)
   * </pre>
   */
  Alfresco.DocumentList.generateFileFolderLinkMarkup = function DL_generateFileFolderLinkMarkup(scope, record)
  {
     var jsNode = record.jsNode,
        html;

     if (jsNode.isLink && $isValueSet(scope.options.siteId) && record.location.site && record.location.site.name !== scope.options.siteId)
     {
        if (jsNode.isContainer)
        {
           html = $siteURL("documentlibrary?path=" + encodeURIComponent(record.location.path),
           {
              site: record.location.site.name
           });
        }
        else
        {
           html = scope.getActionUrls(record, record.location.site.name).documentDetailsUrl;
        }
     }
     else
     {
        if (jsNode.isContainer)
        {
        	
        	
        	if(jsNode.aspects.indexOf("bcpg:entityListsAspect") > 0){
        		html = scope.getActionUrls(record).documentDetailsUrl.replace("document-details","entity-details");
  	     	} else {
  	         if (record.parent.isContainer)
  	         {
  	            // handle folder parent node
  	            html = '#" class="filter-change" rel="' + Alfresco.DocumentList.generatePathMarkup(record.location);
  	         }
  	         else if (record.location.path === "/")
  	         {
  	            // handle Repository root parent node (special store_root type - not a folder)
  	            html = '#" class="filter-change" rel="' + Alfresco.DocumentList.generateFilterMarkup(
  	               {
  	                  filterId: "path",
  	                  filterData: $combine(record.location.path, "")
  	               });
  	         }
  	         else
  	         {
  	            // handle unknown parent node types
  	            html = '#';
  	         }
  	     	}
        }
        else
        {
           var actionUrls = scope.getActionUrls(record);
           if (jsNode.isLink && jsNode.linkedNode.isContainer)
           {
              html = actionUrls.folderDetailsUrl;
           }
           else
           {
              html = actionUrls.documentDetailsUrl;
           }
        }
     }

     return '<a href="' + html + '">';
  };



  /**
   * Override Alfresco.DocumentListViewRenderer.renderCellThumbnail with a simple icon and preview
   */
  Alfresco.DocumentListSimpleViewRenderer.prototype.renderCellThumbnail = function DL_SVR_renderCellThumbnail(scope, elCell, oRecord, oColumn, oData)
  {
     var record = oRecord.getData(),
        node = record.jsNode,
        properties = node.properties,
        name = record.displayName,
        isContainer = node.isContainer,
        isLink = node.isLink,
        extn = name.substring(name.lastIndexOf(".")),
        imgId = node.nodeRef.nodeRef; // DD added
     
     var containerTarget; // This will only get set if thumbnail represents a container
     
     oColumn.width = 40;
     Dom.setStyle(elCell, "width", oColumn.width + "px");
     Dom.setStyle(elCell.parentNode, "width", oColumn.width + "px");

     if (isContainer)
     {
        elCell.innerHTML = '<span class="folder-small">' + (isLink ? '<span class="link"></span>' : '') + (scope.dragAndDropEnabled ? '<span class="droppable"></span>' : '') + Alfresco.DocumentList.generateFileFolderLinkMarkup(scope, record) + '<img id="' + imgId + '" src="' +  beCPG.util.getFileIcon(name,record,isContainer,true) +'" /></a>';
        containerTarget = new YAHOO.util.DDTarget(imgId); // Make the folder a target
     }
     else
     {
        var id = scope.id + '-preview-' + oRecord.getId();
        elCell.innerHTML = '<span id="' + id + '" class="icon32">' + (isLink ? '<span class="link"></span>' : '') + Alfresco.DocumentList.generateFileFolderLinkMarkup(scope, record) + '<img id="' + imgId + '" src="' + beCPG.util.getFileIcon(name,record,isContainer,true)  + '" alt="' + extn + '" title="' + $html(name) + '" /></a></span>';

        // Preview tooltip
        scope.previewTooltips.push(id);
     }
     var dnd = new Alfresco.DnD(imgId, scope);
  };

	  
	
  
})();





