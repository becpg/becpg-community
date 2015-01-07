/***********************************************************************************************************************
 * Copyright (C) 2010-2014 beCPG. This file is part of beCPG beCPG is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as published by the Free Software Foundation,
 * either version 3 of the License, or (at your option) any later version. beCPG is distributed in the hope that it will
 * be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR
 * PURPOSE. See the GNU Lesser General Public License for more details. You should have received a copy of the GNU
 * Lesser General Public License along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 **********************************************************************************************************************/
// Declare namespace...
(function() {

   /**
    * YUI Library aliases
    */
   var Dom = YAHOO.util.Dom;

   /**
    * Alfresco Slingshot aliases
    */
   var $html = Alfresco.util.encodeHTML, $combine = Alfresco.util.combinePaths, $siteURL = Alfresco.util.siteURL, $isValueSet = Alfresco.util.isValueSet;

   // Define constructor...
   beCPG.custom.DocumentList = function CustomDocumentList_constructor(htmlId) {
      beCPG.custom.DocumentList.superclass.constructor.call(this, htmlId);

      YAHOO.Bubbling.on("doclistMetadata", this.onDoclistMetadata, this);

      return this;
   };

   // Extend default DocumentList...
   YAHOO
         .extend(
               beCPG.custom.DocumentList,
               Alfresco.DocumentList,
               {

                  onDoclistMetadata : function(layer, args) {

                     var metadata = args[1].metadata, me = this;
                     // beCPG

                     if (metadata !== null && beCPG.util.isEntity(metadata.parent)) {
                        var div = Dom.get(me.id + "-becpg-entityFolder-message"), entityClassName = metadata.parent.type
                              .split(":")[1], instructions;

                        var instructionKey = "product";

                        // same message for every product
                        if (entityClassName == "rawMaterial" || entityClassName == "finishedProduct" || entityClassName == "semiFinishedProduct" || entityClassName == "localSemiFinishedProduct" || entityClassName == "packagingKit" || entityClassName == "packagingMaterial" || entityClassName == "resourceProduct") {
                           instructionKey = "product";
                        } else if(entityClassName =="projet" || entityClassName =="systemEntity" ||  entityClassName =="aclGroup" ||  entityClassName =="entityTplFolder"
                        		){
                           instructionKey = entityClassName;
                        } else {
                        	instructionKey = "entity";
                        }
                        
                        
                        instructions = "<img  src='"+Alfresco.constants.PROXY_URI +
                        		"/api/node/" + metadata.parent.nodeRef.replace(':/','') + "/content/thumbnails/doclib?c=queue&ph=true' class='node-thumbnail' width='48'>";
                        instructions += "<span >" + Alfresco.util
                              .message("page.documentlibrary.instructions." + instructionKey) + "</span>";

                        div.innerHTML = instructions;

                        this.widgets.viewEntityDetails = Alfresco.util.createYUIButton(me, "viewEntityDetails-button",
                              function(sType, aArgs, p_obj) {

                                 window.location.href = beCPG.util.entityDetailsURL(me.options.siteId,
                                       me.doclistMetadata.parent.nodeRef, me.doclistMetadata.parent.type);

                              });

                        this.widgets.viewEntityLists = Alfresco.util.createYUIButton(me, "viewEntityLists-button",
                              function(sType, aArgs, p_obj) {

                                 window.location.href = beCPG.util.entityCharactURL(me.options.siteId,
                                       me.doclistMetadata.parent.nodeRef, me.doclistMetadata.parent.type);

                              });

                        Dom.removeClass(me.id + "-becpg-entityFolder-buttons", "hidden");

                        Dom.removeClass(me.id + "-becpg-entityFolder-instructions", "hidden");

                     } else {
                        Dom.addClass(me.id + "-becpg-entityFolder-instructions", "hidden");
                     }
                     // End beCPG
                  },

                  /**
                   * Like/Unlike event handler
                   * 
                   * @method onLikes
                   * @param row
                   *            {HTMLElement} DOM reference to a TR element (or child thereof)
                   */
                  onLikes : function DL_onLikes(row) {
                     var elIdentifier = row;
                     if (typeof this.viewRenderers[this.options.viewRendererName] === "object") {
                        elIdentifier = this.viewRenderers[this.options.viewRendererName]
                              .getDataTableRecordIdFromRowElement(this, row);
                     }
                     var oRecord = this.widgets.dataTable.getRecord(elIdentifier), record = oRecord.getData(), nodeRef = record.jsNode.nodeRef, likes = record.likes;

                     likes.isLiked = !likes.isLiked;
                     likes.totalLikes += (likes.isLiked ? 1 : -1);

                     var responseConfig = {
                        successCallback : {
                           fn : function DL_onLikes_success(event, p_nodeRef) {
                              var data = event.json.data;
                              if (data) {
                                 // Update the record with the server's value
                                 var oRecord = this._findRecordByParameter(p_nodeRef, "nodeRef"), record = oRecord
                                       .getData(), node = record.node, likes = record.likes;

                                 likes.totalLikes = data.ratingsCount;
                                 this.widgets.dataTable.updateRow(oRecord, record);

                                 // Post to the Activities Service on the "Like" action
                                 if (likes.isLiked) {
                                    var activityData = {
                                       fileName : record.fileName,
                                       nodeRef : node.nodeRef
                                    };
                                    if (beCPG.util.isEntity(record)) {
                                       this.modules.actions.postActivity(this.options.siteId, "entity-liked",
                                             "entity-details", activityData);
                                    } else if (node.isContainer) {
                                       this.modules.actions.postActivity(this.options.siteId, "folder-liked",
                                             "folder-details", activityData);
                                    } else {
                                       this.modules.actions.postActivity(this.options.siteId, "file-liked",
                                             "document-details", activityData);
                                    }
                                 }
                              }
                           },
                           scope : this,
                           obj : nodeRef.toString()
                        },
                        failureCallback : {
                           fn : function DL_onLikes_failure(event, p_nodeRef) {
                              // Reset the flag to it's previous state
                              var oRecord = this._findRecordByParameter(p_nodeRef, "nodeRef"), record = oRecord
                                    .getData(), likes = record.likes;

                              likes.isLiked = !likes.isLiked;
                              likes.totalLikes += (likes.isLiked ? 1 : -1);
                              this.widgets.dataTable.updateRow(oRecord, record);
                              Alfresco.util.PopupManager.displayPrompt({
                                 text : this.msg("message.save.failure", record.displayName)
                              });
                           },
                           scope : this,
                           obj : nodeRef.toString()
                        }
                     };

                     if (likes.isLiked) {
                        this.services.likes.set(nodeRef, 1, responseConfig);
                     } else {
                        this.services.likes.remove(nodeRef, responseConfig);
                     }
                     this.widgets.dataTable.updateRow(oRecord, record);
                  }

               });

   /**
    * Generate URL for a file- or folder-link that may be located within a different Site
    * 
    * @method generateFileFolderLinkMarkup
    * @param record
    *            {object} Item record
    * @return {string} Mark-up for use in node attribute
    * 
    * <pre>
    *       Folders: Navigate into the folder (ajax)
    *       Documents: Navigate to the details page (page)
    *    Links: Same site (or Repository mode)
    *       Links to folders: Navigate into the folder (ajax)
    *       Links to documents: Navigate to the details page (page)
    *    Links: Different site
    *       Links to folders: Navigate into the site &amp; folder (page)
    *       Links to documents: Navigate to the details page within the site (page)
    * </pre>
    */
   Alfresco.DocumentList.generateFileFolderLinkMarkup = function DL_generateFileFolderLinkMarkup(scope, record) {
      var jsNode = record.jsNode, html;

      if (jsNode.isLink && $isValueSet(scope.options.siteId) && record.location.site && record.location.site.name !== scope.options.siteId) {
         if (jsNode.isContainer) {
            html = $siteURL("documentlibrary?path=" + encodeURIComponent(record.location.path), {
               site : record.location.site.name
            });
         } else {
            html = scope.getActionUrls(record, record.location.site.name).documentDetailsUrl;
         }
      } else {
         if (jsNode.isContainer) {

            if (beCPG.util.isEntity(record)) {
               html = scope.getActionUrls(record).documentDetailsUrl.replace("document-details", "entity-details");
            } else {
               if (record.parent.isContainer) {
                  // handle folder parent node
                  html = '#" class="filter-change" rel="' + Alfresco.DocumentList.generatePathMarkup(record.location);
               } else if (record.location.path === "/") {
                  // handle Repository root parent node (special store_root type - not a folder)
                  html = '#" class="filter-change" rel="' + Alfresco.DocumentList.generateFilterMarkup({
                     filterId : "path",
                     filterData : $combine(record.location.path, "")
                  });
               } else {
                  // handle unknown parent node types
                  html = '#';
               }
            }
         } else {
            var actionUrls = scope.getActionUrls(record);
            if (jsNode.isLink && jsNode.linkedNode.isContainer) {
               html = actionUrls.folderDetailsUrl;
            } else {
               html = actionUrls.documentDetailsUrl;
            }
         }
      }

      return '<a href="' + html + '">';
   };

   /**
    * Override Alfresco.DocumentListViewRenderer.renderCellThumbnail with a simple icon and preview
    */
   Alfresco.DocumentListSimpleViewRenderer.prototype.renderCellThumbnail = function DL_SVR_renderCellThumbnail(scope,
                                                                                                               elCell,
                                                                                                               oRecord,
                                                                                                               oColumn,
                                                                                                               oData) {

      var record = oRecord.getData(), node = record.jsNode, name = record.displayName, isContainer = node.isContainer, isLink = node.isLink, extn = name
            .substring(name.lastIndexOf(".")), isEntity = beCPG.util.isEntity(record), imgId = node.nodeRef.nodeRef;

      if (isEntity) {
         extn = node.type.substring(node.type.lastIndexOf(":"));
      }

      oColumn.width = 40;
      Dom.setStyle(elCell, "width", oColumn.width + "px");
      Dom.setStyle(elCell.parentNode, "width", oColumn.width + "px");

      if (isContainer && !isEntity) {
         elCell.innerHTML = '<span class="folder-small">' + (isLink ? '<span class="link"></span>' : '') + (scope.dragAndDropEnabled ? '<span class="droppable"></span>'
               : '') + Alfresco.DocumentList.generateFileFolderLinkMarkup(scope, record) + '<img id="' + imgId + '" src="' + Alfresco.constants.URL_RESCONTEXT + 'components/documentlibrary/images/folder-32.png" /></a>';
         new YAHOO.util.DDTarget(imgId); // Make the folder a target
      } else {
         var id = scope.id + '-preview-' + oRecord.getId();
         elCell.innerHTML = '<span id="' + id + '" class="icon32">' + (isLink ? '<span class="link"></span>' : '') + Alfresco.DocumentList
               .generateFileFolderLinkMarkup(scope, record) + '<img id="' + imgId + '" src="' + Alfresco.constants.URL_RESCONTEXT + 'components/images/filetypes/' + Alfresco.util
               .getFileIcon(name,node.type) + '" alt="' + extn + '" title="' + $html(name) + '" /></a></span>';

         // Preview tooltip
         scope.previewTooltips.push(id);
      }
      new Alfresco.DnD(imgId, scope);
   };

   /**
    * Render the thumbnail cell
    * 
    * @method renderCellThumbnail
    * @param scope
    *            {object} The DocumentList object
    * @param elCell
    *            {object}
    * @param oRecord
    *            {object}
    * @param oColumn
    *            {object}
    * @param oData
    *            {object|string}
    */
   Alfresco.DocumentListViewRenderer.prototype.renderCellThumbnail = function DL_VR_renderCellThumbnail(scope, elCell,
                                                                                                        oRecord,
                                                                                                        oColumn, oData) {

      var record = oRecord.getData(), node = record.jsNode, name = record.displayName, isContainer = node.isContainer, isLink = node.isLink, extn = name
            .substring(name.lastIndexOf(".")), isEntity = beCPG.util.isEntity(record), imgId = node.nodeRef.nodeRef; // DD
                                                                                                                     // added

      oColumn.width = this.thumbnailColumnWidth;
      Dom.setStyle(elCell, "width", oColumn.width + "px");
      Dom.setStyle(elCell.parentNode, "width", oColumn.width + "px");

      if ((isContainer || (isLink && node.linkedNode.isContainer)) && !isEntity) {
         elCell.innerHTML = '<span class="folder">' + (isLink ? '<span class="link"></span>' : '') + (scope.dragAndDropEnabled ? '<span class="droppable"></span>'
               : '') + Alfresco.DocumentList.generateFileFolderLinkMarkup(scope, record) + '<img id="' + imgId + '" src="' + Alfresco.constants.URL_RESCONTEXT + 'components/documentlibrary/images/folder-64.png" /></a>';
         new YAHOO.util.DDTarget(imgId); // Make the folder a target
      } else {
         elCell.innerHTML = '<span class="thumbnail">' + (isLink ? '<span class="link"></span>' : '') + Alfresco.DocumentList
               .generateFileFolderLinkMarkup(scope, record) + '<img id="' + imgId + '" src="' + Alfresco.DocumentList
               .generateThumbnailUrl(record) + '" alt="' + extn + '" title="' + $html(name) + '" /></a></span>';
      }
      new Alfresco.DnD(imgId, scope);

   };

   /**
    * Generate "Likes" UI
    * 
    * @method generateLikes
    * @param scope
    *            {object} DocumentLibrary instance
    * @param record
    *            {object} File record
    * @return {string} HTML mark-up for Likes UI
    */
   Alfresco.DocumentList.generateLikes = function DL_generateLikes(scope, record) {
      var node = record.node, likes = record.likes, i18n = "like." + (node.isContainer && !beCPG.util.isEntity(record) ? "folder."
            : "document."), html = "";

      if (likes.isLiked) {
         html = '<a class="like-action enabled" title="' + scope.msg(i18n + "remove.tip") + '" tabindex="0"></a>';
      } else {
         html = '<a class="like-action" title="' + scope.msg(i18n + "add.tip") + '" tabindex="0">' + scope
               .msg(i18n + "add.label") + '</a>';
      }

      html += '<span class="likes-count">' + $html(likes.totalLikes) + '</span>';

      return html;
   };

   /**
    * Generate "Comments" UI
    * 
    * @method generateComments
    * @param scope
    *            {object} DocumentLibrary instance
    * @param record
    *            {object} File record
    * @return {string} HTML mark-up for Comments UI
    */
   Alfresco.DocumentList.generateComments = function DL_generateComments(scope, record) {

      var node = record.node, actionUrls = scope.getActionUrls(record), url = actionUrls[node.isContainer ? "folderDetailsUrl"
            : "documentDetailsUrl"] + "#comment", i18n = "comment." + (node.isContainer && !beCPG.util.isEntity(record) ? "folder."
            : "document.");

      // beCPG
      if (beCPG.util.isEntity(record)) {
         url = url.replace("folder-details", "entity-details");
      }

      var hasComments = (node.properties["fm:commentCount"] !== undefined);

      var html = '<a href="' + url + '" class="comment' + (hasComments ? " hasComments" : "") + '" title="' + scope
            .msg(i18n + "tip") + '" tabindex="0">' + scope.msg(i18n + "label") + '</a>';
      if (hasComments) {
         html += '<span class="comment-count">' + $html(node.properties["fm:commentCount"]) + '</span>';
      }
      return html;
   };

})();
