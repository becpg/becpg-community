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
   var Dom = YAHOO.util.Dom, Event = YAHOO.util.Event;

   /**
    * Alfresco Slingshot aliases
    */
   var $html = Alfresco.util.encodeHTML, $userProfileLink = Alfresco.util.userProfileLink, $userAvatar = Alfresco.Share.userAvatar;

   // Define constructor...
   beCPG.custom.DocumentVersions = function CustomDocumentVersions_constructor(htmlId) {
      beCPG.custom.DocumentVersions.superclass.constructor.call(this, htmlId);
      return this;
   };

   // Extend default DocumentVersions...
   YAHOO
         .extend(
               beCPG.custom.DocumentVersions,
               Alfresco.DocumentVersions,
               {
                  /**
                   * Object container for initialization options
                   * 
                   * @property options
                   * @type {object} object literal
                   */
                  options : {
                     /**
                      * Reference to the current document
                      * 
                      * @property nodeRef
                      * @type string
                      */
                     nodeRef : null,

                     /**
                      * Current siteId, if any.
                      * 
                      * @property siteId
                      * @type string
                      */
                     siteId : "",

                     /**
                      * The name of container that the node lives in, will be used when uploading new versions.
                      * 
                      * @property containerId
                      * @type string
                      */
                     containerId : null,

                     /**
                      * The version of the working copy (if it is a working copy), will be used during upload.
                      * 
                      * @property workingCopyVersion
                      * @type string
                      */
                     workingCopyVersion : null,

                     /**
                      * Tells if the user may upload a new version or revert the document.
                      * 
                      * @property allowNewVersionUpload
                      * @type string
                      */
                     allowNewVersionUpload : false,

                     /**
                      * Tells if the user may compare the version and the document.
                      * 
                      * @property isEntity
                      * @type string
                      */
                     isEntity : false
                  },

                  /**
                   * Fired by YUI when parent element is available for scripting
                   * 
                   * @method onReady
                   */
                  onReady : function DocumentVersions_onReady() {
                  	 var containerElement = Dom.get(this.id + "-olderVersions");
                     if (!containerElement) {
                        return;
                     }
                  
                     this.widgets.alfrescoDataTable = new Alfresco.util.DataTable(
                           {
                              dataSource : {
                                 url : this.options.isEntity ? Alfresco.constants.PROXY_URI + "becpg/api/entity-version?nodeRef=" + this.options.nodeRef
                                       : Alfresco.constants.PROXY_URI + "api/version?nodeRef=" + this.options.nodeRef,
                                 doBeforeParseData : this.bind(function(oRequest, oFullResponse) {
                                    // Versions are returned in an array but must be placed in an object to be able to
                                    // be parse by yui
                                    // Also skip the first version since that is the current version
                                    this.latestVersion = oFullResponse.splice(0, 1)[0];
                                    Dom.get(this.id + "-latestVersion").innerHTML = this
                                          .getDocumentVersionMarkup(this.latestVersion);

                                    // Cache the version data for other components (e.g. HistoricPropertiesViewer)
                                    this.versionCache = oFullResponse;

                                    return ({
                                       "data" : oFullResponse
                                    });
                                 })
                              },
                              dataTable : {
                                 container : this.id + "-olderVersions",
                                 columnDefinitions : [ {
                                    key : "version",
                                    sortable : false,
                                    formatter : this.bind(this.renderCellVersion)
                                 } ],
                                 config : {
                                    MSG_EMPTY : this.msg("message.noVersions")
                                 }
                              }
                           });

                     // Resize event handler - adjusts the filename container DIV to a size relative to the container
                     // width
                     Event.addListener(window, "resize", function() {
                        var width = (Dom.getViewportWidth() * 0.25) + "px", nodes = YAHOO.util.Selector.query(
                              'h3.thin', this.id + "-body");
                        for (var i = 0; i < nodes.length; i++) {
                           nodes[i].style.width = width;
                        }
                     }, this, true);
                  },

                  /**
                   * Triggers the archiving and download of a single folders contents
                   * 
                   * @method onActionFolderDownload
                   * @param record
                   *            {object} Object literal representing the folder to be actioned
                   */
                  onActionEntityDownload : function DocumentVersions_onActionEntityDownload(nodeRef) {

                     var downloadDialog = Alfresco.getArchiveAndDownloadInstance(), config = {
                        nodesToArchive : [ {
                           "nodeRef" : nodeRef
                        } ],
                        archiveName : encodeURIComponent(this.latestVersion.name)
                     };
                     downloadDialog.show(config);
                  },

                  /**
                   * Builds and returns the markup for a version.
                   * 
                   * @method getDocumentVersionMarkup
                   * @param doc
                   *            {Object} The details for the document
                   */
                  getDocumentVersionMarkup : function DocumentVersions_getDocumentVersionMarkup(doc) {
                     var downloadURL = Alfresco.constants.PROXY_URI + 'api/node/content/' + doc.nodeRef.replace(":/",
                           "") + '/' + encodeURIComponent(doc.name) + '?a=true', compareURL = Alfresco.constants.PROXY_URI + 'becpg/entity/compare/' + this.options.nodeRef
                           .replace(":/", "") + '/' + encodeURIComponent(doc.label) + '/' + encodeURIComponent(doc.name) + ".pdf", html = '';

                     html += '<div class="version-panel-left">';
                     html += '   <span class="document-version">' + $html(doc.label) + '</span>';
                     html += '</div>';
                     html += '<div class="version-panel-right">';
                     html += '   <h3 class="thin dark" style="width:' + (Dom.getViewportWidth() * 0.25) + 'px;">' + $html(doc.name) + '</h3>';
                     html += '   <span class="actions">';
                     if (this.options.allowNewVersionUpload) {
                        html += '   <a href="#" name=".onRevertVersionClick" rel="' + doc.label + '" class="' + this.id + ' revert" title="' + this
                              .msg("label.revert") + '">&nbsp;</a>';
                     }
                     if (this.options.isEntity == true) {
                        html += '   <a href="#" name=".onActionEntityDownload" rel="' + doc.nodeRef + '" class="' + this.id + ' download" title="' + this
                              .msg("label.download") + '">&nbsp;</a>';
                     } else {
                        html += '      <a href="' + downloadURL + '" class="download" title="' + this
                              .msg("label.download") + '">&nbsp;</a>';
                     }
                     html += '		<a href="#" name=".onViewHistoricPropertiesClick" rel="' + doc.nodeRef + '" class="' + this.id + ' historicProperties" title="' + this
                           .msg("label.historicProperties") + '">&nbsp;</a>';
                     if (this.options.isEntity == true) {
                        html += '      <a href="' + compareURL + '" class="compare" title="' + this
                              .msg("label.compare") + '">&nbsp;</a>';
                     }
                     html += '   </span>';
                     html += '   <div class="clear"></div>';
                     html += '   <div class="version-details">';
                     html += '      <div class="version-details-left">';
                     html += $userAvatar(doc.creator.userName, 32);
                     html += '      </div>';
                     html += '      <div class="version-details-right">';
                     html += $userProfileLink(doc.creator.userName, doc.creator.firstName + ' ' + doc.creator.lastName,
                           'class="theme-color-1"') + ' ';
                     html += Alfresco.util.relativeTime(Alfresco.util.fromISO8601(doc.createdDateISO)) + '<br />';
                     html += ((doc.description || "").length > 0) ? $html(doc.description, true)
                           : '<span class="faded">(' + this.msg("label.noComment") + ')</span>';
                     html += '      </div>';
                     html += '   </div>';
                     html += '</div>';

                     html += '<div class="clear"></div>';
                     return html;
                  }

               });
})();
