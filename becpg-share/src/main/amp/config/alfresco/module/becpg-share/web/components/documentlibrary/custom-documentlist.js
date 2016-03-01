/*******************************************************************************
 *  Copyright (C) 2010-2016 beCPG. 
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
    var $html = Alfresco.util.encodeHTML, $combine = Alfresco.util.combinePaths, $siteURL = Alfresco.util.siteURL, $isValueSet = Alfresco.util.isValueSet;

    // Define constructor...
    beCPG.custom.DocumentList = function CustomDocumentList_constructor(htmlId)
    {
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

                        onDoclistMetadata : function(layer, args)
                        {

                            var metadata = args[1].metadata, me = this;
                            // beCPG

                            if (metadata !== null && beCPG.util.isEntity(metadata.parent))
                            {
                                var div = Dom.get(me.id + "-becpg-entityFolder-message"), entityClassName = metadata.parent.type
                                        .split(":")[1], instructions;

                                var instructionKey = "product";

                                // same message for every product
                                if (entityClassName == "rawMaterial" || entityClassName == "finishedProduct" || entityClassName == "semiFinishedProduct" || entityClassName == "localSemiFinishedProduct" || entityClassName == "packagingKit" || entityClassName == "packagingMaterial" || entityClassName == "resourceProduct")
                                {
                                    instructionKey = "product";
                                }
                                else if (entityClassName == "projet" || entityClassName == "systemEntity" || entityClassName == "aclGroup" || entityClassName == "entityTplFolder")
                                {
                                    instructionKey = entityClassName;
                                }
                                else
                                {
                                    instructionKey = "entity";
                                }

                                instructions = "<img  src='" + Alfresco.constants.PROXY_URI + "/api/node/" + metadata.parent.nodeRef
                                        .replace(':/', '') + "/content/thumbnails/doclib?c=queue&ph=true' class='node-thumbnail' width='48'>";
                                instructions += "<span >" + Alfresco.util
                                        .message("page.documentlibrary.instructions." + instructionKey) + "</span>";

                                div.innerHTML = instructions;

                                this.widgets.viewEntityLists = Alfresco.util.createYUIButton(me,
                                        "viewEntityLists-button", function(sType, aArgs, p_obj)
                                        {

                                            window.location.href = beCPG.util.entityURL(me.options.siteId,
                                                    me.doclistMetadata.parent.nodeRef, me.doclistMetadata.parent.type);

                                        });

                                Dom.removeClass(me.id + "-becpg-entityFolder-buttons", "hidden");

                                Dom.removeClass(me.id + "-becpg-entityFolder-instructions", "hidden");

                            }
                            else
                            {
                                Dom.addClass(me.id + "-becpg-entityFolder-instructions", "hidden");
                            }
                            // End beCPG
                        },
                        
                        /**
                         * Build URI parameter string for doclist JSON data webscript
                         *
                         * @method _buildDocListParams
                         * @param p_obj.page {string} Page number
                         * @param p_obj.pageSize {string} Number of items per page
                         * @param p_obj.path {string} Path to query
                         * @param p_obj.type {string} Filetype to filter: "all", "documents", "folders"
                         * @param p_obj.site {string} Current site
                         * @param p_obj.container {string} Current container
                         * @param p_obj.filter {string} Current filter
                         */
                        _buildDocListParams: function DL__buildDocListParams(p_obj)
                        {
                           // Essential defaults
                           var siteMode = $isValueSet(this.options.siteId),
                              obj =
                              {
                                 path: this.currentPath,
                                 type: this.options.showFolders ? "all" : "documents",
                                 site: this.options.siteId,
                                 container: this.options.containerId,
                                 filter: this.currentFilter
                              };
                           
                           if(this.options.disableSiteMode){
                        	   siteMode = false;
                           }
                           

                           // Pagination in use?
                           if (this.options.usePagination)
                           {
                              obj.page = this.widgets.paginator.getCurrentPage() || this.currentPage;
                              obj.pageSize = this.widgets.paginator.getRowsPerPage();
                           }

                           // Passed-in overrides
                           if (typeof p_obj === "object")
                           {
                              obj = YAHOO.lang.merge(obj, p_obj);
                           }

                           // Build the URI stem
                           var isSharedFiles = ("alfresco://company/shared" == this.options.rootNode);
                           var uriPart = siteMode ? "site/{site}/{container}" : isSharedFiles ? "node/alfresco/company/shared" : "node/alfresco/user/home",
                              params = YAHOO.lang.substitute("{type}/" + uriPart + (obj.filter.filterId === "path" ? "{path}" : ""),
                              {
                                 type: encodeURIComponent(obj.type),
                                 site: encodeURIComponent(obj.site),
                                 container: encodeURIComponent(obj.container),
                                 path: $combine("/", Alfresco.util.encodeURIPath(obj.path))
                              });

                           // Filter parameters
                           params += "?filter=" + encodeURIComponent(obj.filter.filterId);
                           if (obj.filter.filterData && obj.filter.filterId !== "path")
                           {
                              params += "&filterData=" + encodeURIComponent(obj.filter.filterData);
                           }

                           // Paging parameters
                           if (this.options.usePagination)
                           {
                              params += "&size=" + obj.pageSize  + "&pos=" + obj.page;
                           }

                           // Sort parameters
                           params += "&sortAsc=" + this.options.sortAscending + "&sortField=" + encodeURIComponent(this.options.sortField);

                           if (!siteMode)
                           {
                              // Repository mode (don't resolve Site-based folders)
                              params += "&libraryRoot=" + encodeURIComponent(this.options.rootNode.toString());
                           }

                           // View mode and No-cache
                           params += "&view=" + this.actionsView + "&noCache=" + new Date().getTime();

                           return params;
                        },
                        /**
                         * Fired when an object is dropped onto the DocumentList DOM element.
                         * Checks that files are present for upload, determines the target (either the current document list or
                         * a specific folder rendered in the document list and then calls on the DNDUpload singleton component
                         * to perform the upload.
                         *
                         * @param e {object} HTML5 drag and drop event
                         * @method onDocumentListDrop
                         */
                        onDocumentListDrop: function DL_onDocumentListDrop(e)
                        {
                           try
                           {
                              // Only perform a file upload if the user has *actually* dropped some files!
                              if (e.dataTransfer.files !== undefined && e.dataTransfer.files !== null && e.dataTransfer.files.length > 0)
                              {
                                 // We need to get the upload progress dialog widget so that we can display it.
                                 // The function called has been added to file-upload.js and ensures the dialog is a singleton.
                                 var progressDialog = Alfresco.getDNDUploadProgressInstance();

                                 var continueWithUpload = false;

                                 // Check that at least one file with some data has been dropped...
                                 var zeroByteFiles = "", i, j;

                                 j = e.dataTransfer.files.length;
                                 for (i = 0; i < j; i++)
                                 {
                                    if (e.dataTransfer.files[i].size > 0)
                                    {
                                       continueWithUpload = true;
                                    }
                                    else
                                    {
                                       zeroByteFiles += '"' + e.dataTransfer.files[i].fileName + '", ';
                                    }
                                 }

                                 if (!continueWithUpload)
                                 {
                                    zeroByteFiles = zeroByteFiles.substring(0, zeroByteFiles.lastIndexOf(", "));
                                    Alfresco.util.PopupManager.displayMessage(
                                    {
                                       text: progressDialog.msg("message.zeroByteFiles", zeroByteFiles)
                                    });
                                 }

                                 // Perform some checks on based on the browser and selected files to ensure that we will
                                 // support the upload request.
                                 if (continueWithUpload && progressDialog.uploadMethod === progressDialog.INMEMORY_UPLOAD)
                                 {
                                    // Add up the total size of all selected files to see if they exceed the maximum allowed.
                                    // If the user has requested to upload too large a file or too many files in one operation
                                    // then generate an error dialog and abort the upload...
                                    var totalRequestedUploadSize = 0;

                                    j = e.dataTransfer.files.length;
                                    for (i = 0; i < j; i++)
                                    {
                                       totalRequestedUploadSize += e.dataTransfer.files[i].size;
                                    }
                                    if (totalRequestedUploadSize > progressDialog.getInMemoryLimit())
                                    {
                                       continueWithUpload = false;
                                       Alfresco.util.PopupManager.displayPrompt(
                                       {
                                           text: progressDialog.msg("inmemory.uploadsize.exceeded", Alfresco.util.formatFileSize(progressDialog.getInMemoryLimit()))
                                       });
                                    }
                                 }

                                 // If all tests are passed...
                                 if (continueWithUpload)
                                 {
                                    // Initialise the target directory as the current path represented by the current rendering of the DocumentList.
                                    // If we determine that the user has actually dropped some files onto the a folder icon (which we're about to check
                                    // for) then we'll change this value to be that of the folder targeted...
                                    var directory = this.currentPath,
                                       directoryName = directory.substring(directory.lastIndexOf("/") + 1),
                                       destination = this.doclistMetadata.parent ? this.doclistMetadata.parent.nodeRef : null;

                                    // Providing that the drop target a <TR> (or a child node of a <TR>) in the DocumentList data table then a record
                                    // will be returned from this call. If nothing is returned then we cannot proceed with the file upload operation.
                                    var oRecord = this.widgets.dataTable.getRecord(e.target);
                                    if (oRecord !== null)
                                    {
                                       // Dropped onto a folder icon?
                                       var oColumn = this.widgets.dataTable.getColumn(e.target),
                                          record = oRecord.getData();

                                       if (e.target.tagName == "IMG" || e.target.className == "droppable")
                                       {
                                          var location = record.location;
                                          directoryName = location.file;
                                          // Site mode
                                          directory = $combine(location.path, location.file);
                                          // Repository mode
                                          destination = record.nodeRef;
                                       }
                                       // else: The file(s) were not not dropped onto a folder icon, so we will just upload to the current path
                                    }
                                    // else: If a record is not returned, then it means that we dropped into an empty folder.

                                    // Remove all the highlighting
                                    Dom.removeClass(this.widgets.dataTable.getTrEl(e.target), "dndFolderHighlight");
                                    Dom.removeClass(this.widgets.dataTable.getContainerEl(), "dndDocListHighlight");

                                    // Show uploader for multiple files
                                    var multiUploadConfig =
                                    {
                                       files: e.dataTransfer.files,
                                       uploadDirectoryName: directoryName,
                                       filter: [],
                                       mode: progressDialog.MODE_MULTI_UPLOAD,
                                       thumbnails: "doclib",
                                       onFileUploadComplete:
                                       {
                                          fn: this.onFileUploadComplete,
                                          scope: this
                                       }
                                    };

                                    // Extra parameters depending on current mode
                                    if ($isValueSet(this.options.siteId) && !this.options.disableSiteMode)
                                    {
                                       multiUploadConfig.siteId = this.options.siteId;
                                       multiUploadConfig.containerId = this.options.containerId;
                                       multiUploadConfig.uploadDirectory = directory;
                                    }
                                    else
                                    {
                                       multiUploadConfig.destination = destination;
                                    }

                                    progressDialog.show(multiUploadConfig);
                                 }
                              }
                              else
                              {
                                 Alfresco.logger.debug("DL_onDocumentListDrop: A drop event was detected, but no files were present for upload: ", e.dataTransfer);
                              }
                           }
                           catch(exception)
                           {
                              Alfresco.logger.error("DL_onDocumentListDrop: The following error occurred when files were dropped onto the Document List: ", exception);
                           }
                           e.stopPropagation();
                           e.preventDefault();
                        },

                        /**
                         * Like/Unlike event handler
                         * 
                         * @method onLikes
                         * @param row
                         *            {HTMLElement} DOM reference to a TR
                         *            element (or child thereof)
                         */
                        onLikes : function DL_onLikes(row)
                        {
                            var elIdentifier = row;
                            if (typeof this.viewRenderers[this.options.viewRendererName] === "object")
                            {
                                elIdentifier = this.viewRenderers[this.options.viewRendererName]
                                        .getDataTableRecordIdFromRowElement(this, row);
                            }
                            var oRecord = this.widgets.dataTable.getRecord(elIdentifier), record = oRecord.getData(), nodeRef = record.jsNode.nodeRef, likes = record.likes;

                            likes.isLiked = !likes.isLiked;
                            likes.totalLikes += (likes.isLiked ? 1 : -1);

                            var responseConfig =
                            {
                                successCallback :
                                {
                                    fn : function DL_onLikes_success(event, p_nodeRef)
                                    {
                                        var data = event.json.data;
                                        if (data)
                                        {
                                            // Update the record with the
                                            // server's value
                                            var oRecord = this._findRecordByParameter(p_nodeRef, "nodeRef"), record = oRecord
                                                    .getData(), node = record.node, likes = record.likes;

                                            likes.totalLikes = data.ratingsCount;
                                            this.widgets.dataTable.updateRow(oRecord, record);

                                            // Post to the Activities Service on
                                            // the "Like" action
                                            if (likes.isLiked)
                                            {
                                                var activityData =
                                                {
                                                    fileName : record.fileName,
                                                    nodeRef : node.nodeRef
                                                };
                                                if (beCPG.util.isEntity(record))
                                                {
                                                    this.modules.actions.postActivity(this.options.siteId,
                                                            "entity-liked", "entity-data-lists", activityData);
                                                }
                                                else if (node.isContainer)
                                                {
                                                    this.modules.actions.postActivity(this.options.siteId,
                                                            "folder-liked", "folder-details", activityData);
                                                }
                                                else
                                                {
                                                    this.modules.actions.postActivity(this.options.siteId,
                                                            "file-liked", "document-details", activityData);
                                                }
                                            }
                                        }
                                    },
                                    scope : this,
                                    obj : nodeRef.toString()
                                },
                                failureCallback :
                                {
                                    fn : function DL_onLikes_failure(event, p_nodeRef)
                                    {
                                        // Reset the flag to it's previous state
                                        var oRecord = this._findRecordByParameter(p_nodeRef, "nodeRef"), record = oRecord
                                                .getData(), likes = record.likes;

                                        likes.isLiked = !likes.isLiked;
                                        likes.totalLikes += (likes.isLiked ? 1 : -1);
                                        this.widgets.dataTable.updateRow(oRecord, record);
                                        Alfresco.util.PopupManager.displayPrompt(
                                        {
                                            text : this.msg("message.save.failure", record.displayName)
                                        });
                                    },
                                    scope : this,
                                    obj : nodeRef.toString()
                                }
                            };

                            if (likes.isLiked)
                            {
                                this.services.likes.set(nodeRef, 1, responseConfig);
                            }
                            else
                            {
                                this.services.likes.remove(nodeRef, responseConfig);
                            }
                            this.widgets.dataTable.updateRow(oRecord, record);
                        }

                    });

    /**
     * Generate URL for a file- or folder-link that may be located within a
     * different Site
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
    Alfresco.DocumentList.generateFileFolderLinkMarkup = function DL_generateFileFolderLinkMarkup(scope, record)
    {
        var jsNode = record.jsNode, html;

        if (jsNode.isLink && $isValueSet(scope.options.siteId) && record.location.site && record.location.site.name !== scope.options.siteId)
        {
            if (jsNode.isContainer)
            {
                html = $siteURL("documentlibrary?path=" + encodeURIComponent(record.location.path),
                {
                    site : record.location.site.name
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

                if (beCPG.util.isEntity(record))
                {
                    html = scope.getActionUrls(record).documentDetailsUrl.replace("document-details?","entity-data-lists?list=View-properties&");
                }
                else
                {
                    if (record.parent.isContainer)
                    {
                        // handle folder parent node
                        html = '#" class="filter-change" rel="' + Alfresco.DocumentList
                                .generatePathMarkup(record.location);
                    }
                    else if (record.location.path === "/")
                    {
                        // handle Repository root parent node (special
                        // store_root
                        // type - not a folder)
                        html = '#" class="filter-change" rel="' + Alfresco.DocumentList.generateFilterMarkup(
                        {
                            filterId : "path",
                            filterData : $combine(record.location.path, "")
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
     * Override Alfresco.DocumentListViewRenderer.renderCellThumbnail with a
     * simple icon and preview
     */
    Alfresco.DocumentListSimpleViewRenderer.prototype.renderCellThumbnail = function DL_SVR_renderCellThumbnail(scope,
            elCell, oRecord, oColumn, oData)
    {

        var record = oRecord.getData(),
        node = record.jsNode,
        properties = node.properties,
        name = record.displayName,
        isContainer = node.isContainer,
        isLink = node.isLink,
        extn = name.substring(name.lastIndexOf(".")),
        imgId = node.nodeRef.nodeRef // DD added
        , isEntity = beCPG.util.isEntity(record);
        
        if (isEntity)
        {
            extn = node.type.substring(node.type.lastIndexOf(":"));
        }
     
	     var containerTarget; // This will only get set if thumbnail represents a container
	     
	     oColumn.width = 40;
	     Dom.setStyle(elCell, "width", oColumn.width + "px");
	     Dom.setStyle(elCell.parentNode, "width", oColumn.width + "px");
	
	     if (isContainer && !isEntity)
	     {
	        elCell.innerHTML = '<span class="folder-small">' + (isLink ? '<span class="link"></span>' : '') + (scope.dragAndDropEnabled ? '<span class="droppable"></span>' : '') + Alfresco.DocumentList.generateFileFolderLinkMarkup(scope, record) + '<img id="' + imgId + '" src="' + this.getFolderIcon(record.node) + '" /></a>';
	        containerTarget = new YAHOO.util.DDTarget(imgId); // Make the folder a target
	     }
	     else
	     {
	        var id = scope.id + '-preview-' + oRecord.getId();
	        var fileIcon = Alfresco.util.getFileIcon(name,node.type);
	        if (fileIcon == "generic-file-32.png")
	        {
	           fileIcon = Alfresco.util.getFileIconByMimetype(node.mimetype);
	        }
	        elCell.innerHTML = '<span id="' + id + '" class="icon32">' + (isLink ? '<span class="link"></span>' : '') + Alfresco.DocumentList.generateFileFolderLinkMarkup(scope, record) + '<img id="' + imgId + '" src="' + Alfresco.constants.URL_RESCONTEXT + 'components/images/filetypes/' + fileIcon + '" alt="' + extn + '" title="' + $html(name) + '" /></a></span>';
	        // Preview tooltip
	        scope.previewTooltips.push(id);
	     }
	     var dnd = new Alfresco.DnD(imgId, scope);
    
    
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
            oRecord, oColumn, oData)
    {

        
        var record = oRecord.getData(),
        node = record.jsNode,
        properties = node.properties,
        name = record.displayName,
        isContainer = node.isContainer, 
        isEntity = beCPG.util.isEntity(record),
        isLink = node.isLink,
        extn = name.substring(name.lastIndexOf(".")),
        imgId = node.nodeRef.nodeRef; // DD added
     
	     var containerTarget; // This will only get set if thumbnail represents a container
	     
	     if (window.location.href.search(/\/sharedfiles/) != -1 && record.location.path.search("/Shared") == 0)
	     {
	        record.location.path = record.location.path.substring(7);
	     }
	     
	     oColumn.width = this.thumbnailColumnWidth;
	     Dom.setStyle(elCell, "width", oColumn.width + "px");
	     Dom.setStyle(elCell.parentNode, "width", oColumn.width + "px");
	  
	     if ((isContainer || (isLink && node.linkedNode.isContainer)) && !isEntity)
	     {
	        elCell.innerHTML = '<span class="folder">' + (isLink ? '<span class="link"></span>' : '') + (scope.dragAndDropEnabled ? '<span class="droppable"></span>' : '') + Alfresco.DocumentList.generateFileFolderLinkMarkup(scope, record) + '<img id="' + imgId + '" src="' + this.getFolderIcon(record.node) + '" /></a>';
	        containerTarget = new YAHOO.util.DDTarget(imgId); // Make the folder a target
	     }
	     else
	     {
	        elCell.innerHTML = '<span class="thumbnail">' + (isLink ? '<span class="link"></span>' : '') + Alfresco.DocumentList.generateFileFolderLinkMarkup(scope, record) + '<img id="' + imgId + '" src="' + Alfresco.DocumentList.generateThumbnailUrl(record) + '" alt="' + extn + '" title="' + $html(name) + '" /></a></span>';
	     }
	     var dnd = new Alfresco.DnD(imgId, scope);
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
    Alfresco.DocumentList.generateLikes = function DL_generateLikes(scope, record)
    {
        var node = record.node, likes = record.likes, i18n = "like." + (node.isContainer && !beCPG.util
                .isEntity(record) ? "folder." : "document."), html = "";

        if (likes.isLiked)
        {
            html = '<a class="like-action enabled" title="' + scope.msg(i18n + "remove.tip") + '" tabindex="0"></a>';
        }
        else
        {
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
    Alfresco.DocumentList.generateComments = function DL_generateComments(scope, record)
    {

        var node = record.node, actionUrls = scope.getActionUrls(record), url = actionUrls[node.isContainer ? "folderDetailsUrl"
                : "documentDetailsUrl"] + "#comment", i18n = "comment." + (node.isContainer && !beCPG.util
                .isEntity(record) ? "folder." : "document.");

        // beCPG
        if (beCPG.util.isEntity(record))
        {
            url = url.replace("folder-details?","entity-data-lists?list=View-properties&");
        }

        var hasComments = (node.properties["fm:commentCount"] !== undefined);

        var html = '<a href="' + url + '" class="comment' + (hasComments ? " hasComments" : "") + '" title="' + scope
                .msg(i18n + "tip") + '" tabindex="0">' + scope.msg(i18n + "label") + '</a>';
        if (hasComments)
        {
            html += '<span class="comment-count">' + $html(node.properties["fm:commentCount"]) + '</span>';
        }
        return html;
    };

})();
