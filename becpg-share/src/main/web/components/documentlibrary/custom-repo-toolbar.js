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
(function()
{

   /**
    * RepositoryDocListToolbar constructor.
    * 
    * @param {String} htmlId The HTML id of the parent element
    * @return {beCPG.custom.RepositoryDocListToolbar} The new DocListToolbar instance
    * @constructor
    */
   beCPG.custom.RepositoryDocListToolbar = function(htmlId)
   {
      return beCPG.custom.RepositoryDocListToolbar.superclass.constructor.call(this, htmlId);
   };

   /**
    * Extend Alfresco.DocListToolbar
    */
   YAHOO.extend(beCPG.custom.RepositoryDocListToolbar, beCPG.custom.DocListToolbar);

   /**
    * Augment prototype with main class implementation, ensuring overwrite is enabled
    */
   YAHOO.lang.augmentObject(beCPG.custom.RepositoryDocListToolbar.prototype,
   {
      /**
       * YUI WIDGET EVENT HANDLERS
       * Handlers for standard events fired from YUI widgets, e.g. "click"
       */

      /**
       * File Upload button click handler
       *
       * @method onFileUpload
       * @param e {object} DomEvent
       * @param p_obj {object} Object passed back from addListener method
       */
      onFileUpload: function RDLTB_onFileUpload(e, p_obj)
      {
         if (this.fileUpload === null)
         {
            this.fileUpload = Alfresco.getFileUploadInstance(); 
         }
         
         // Show uploader for multiple files
         var multiUploadConfig =
         {
            destination: this.modules.docList.doclistMetadata.parent.nodeRef,
            filter: [],
            mode: this.fileUpload.MODE_MULTI_UPLOAD,
            thumbnails: "doclib",
            onFileUploadComplete:
            {
               fn: this.onFileUploadComplete,
               scope: this
            }
         };
         this.fileUpload.show(multiUploadConfig);
      },
      
      /**
       * File Upload complete event handler
       *
       * @method onFileUploadComplete
       * @param complete {object} Object literal containing details of successful and failed uploads
       */
      onFileUploadComplete: function RDLTB_onFileUploadComplete(complete)
      {
         // Overridden so activity doesn't get posted
      },

      /**
       * @method _getRssFeedUrl
       * @private
       */
      _getRssFeedUrl: function DLTB__getRssFeedUrl()
      {
         var params = YAHOO.lang.substitute("{type}/node/alfresco/company/home{path}",
         {
            type: this.modules.docList.options.showFolders ? "all" : "documents",
            path: Alfresco.util.encodeURIPath(this.currentPath)
         });

         params += "?filter=" + encodeURIComponent(this.currentFilter.filterId);
         if (this.currentFilter.filterData)
         {
            params += "&filterData=" + encodeURIComponent(this.currentFilter.filterData);
         }
         params += "&format=rss";
         
         return Alfresco.constants.URL_FEEDSERVICECONTEXT + "components/documentlibrary/feed/" + params;
      }
   }, true);
})();
