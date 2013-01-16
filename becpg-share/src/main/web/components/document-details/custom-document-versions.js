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
      $userProfileLink = Alfresco.util.userProfileLink,
      $userAvatar = Alfresco.Share.userAvatar;
	
  // Define constructor...
  beCPG.custom.DocumentVersions = function CustomDocumentVersions_constructor(htmlId)
  {
    beCPG.custom.DocumentVersions.superclass.constructor.call(this, htmlId);
    return this;
  };

  // Extend default DocumentVersions...
  YAHOO.extend(beCPG.custom.DocumentVersions, Alfresco.DocumentVersions,
  {
	  /**
      * Object container for initialization options
      *
      * @property options
      * @type {object} object literal
      */
     options:
     {
        /**
         * Reference to the current document
         *
         * @property nodeRef
         * @type string
         */
        nodeRef: null,

        /**
         * Current siteId, if any.
         *
         * @property siteId
         * @type string
         */
        siteId: "",

        /**
         * The name of container that the node lives in, will be used when uploading new versions.
         *
         * @property containerId
         * @type string
         */
        containerId: null,

        /**
         * The version of the working copy (if it is a working copy), will be used during upload.
         *
         * @property workingCopyVersion
         * @type string
         */
        workingCopyVersion: null,

        /**
         * Tells if the user may upload a new version or revert the document.
         *
         * @property allowNewVersionUpload
         * @type string
         */
        allowNewVersionUpload: false,

        /**
         * Tells if the user may compare the version and the document.
         *
         * @property allowComparison
         * @type string
         */
        allowComparison: false
     },
     /**
      * Builds and returns the markup for a version.
      *
      * @method getDocumentVersionMarkup
      * @param doc {Object} The details for the document
      */
     getDocumentVersionMarkup: function DocumentVersions_getDocumentVersionMarkup(doc)
     {
        var downloadURL = Alfresco.constants.PROXY_URI + '/api/node/content/' + doc.nodeRef.replace(":/", "") + '/' + doc.name + '?a=true',
        compareURL = Alfresco.constants.PROXY_URI + '/becpg/entity/compare/' + this.options.nodeRef.replace(":/", "") + '/' + doc.label + '/' + doc.name + ".pdf",
           html = '';

        html += '<div class="version-panel-left">';
        html += '   <span class="document-version">' + $html(doc.label) + '</span>';
        html += '</div>';
        html += '<div class="version-panel-right">';
        html += '   <h3 class="thin dark" style="width:' + (Dom.getViewportWidth() * 0.25) + 'px;">' + $html(doc.name) +  '</h3>';
        html += '   <span class="actions">';
        if (this.options.allowNewVersionUpload)
        {
           html += '   <a href="#" name=".onRevertVersionClick" rel="' + doc.label + '" class="' + this.id + ' revert" title="' + this.msg("label.revert") + '">&nbsp;</a>';
        }
        html += '      <a href="' + downloadURL + '" class="download" title="' + this.msg("label.download") + '">&nbsp;</a>';
        html += '		<a href="#" name=".onViewHistoricPropertiesClick" rel="' + doc.nodeRef + '" class="' + this.id + ' historicProperties" title="' + this.msg("label.historicProperties") + '">&nbsp;</a>';
        if (this.options.allowComparison == true)
        {
           html += '      <a href="' + compareURL + '" class="compare" title="' + this.msg("label.compare") + '">&nbsp;</a>';
        }
        html += '   </span>';
        html += '   <div class="clear"></div>';
        html += '   <div class="version-details">';
        html += '      <div class="version-details-left">';
        html += $userAvatar(doc.creator.userName, 32);
        html += '      </div>';
        html += '      <div class="version-details-right">';
        html += $userProfileLink(doc.creator.userName, doc.creator.firstName + ' ' + doc.creator.lastName, 'class="theme-color-1"') + ' ';
        html += Alfresco.util.relativeTime(Alfresco.util.fromISO8601(doc.createdDateISO)) + '<br />';
        html += ((doc.description || "").length > 0) ? $html(doc.description, true) : '<span class="faded">(' + this.msg("label.noComment") + ')</span>';
        html += '      </div>';
        html += '   </div>';
        html += '</div>';

        html += '<div class="clear"></div>';
        return html;
     }
    
  });
})();