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
/**
 * Asset location helper class.
 * 
 * @namespace Alfresco
 * @class Alfresco.Location
 */
(function() {

   /**
    * Alfresco Slingshot aliases
    */
   var $siteURL = Alfresco.util.siteURL;

   beCPG.util.entityCharactURL = function(siteId, pNodeRef, type) {

      var nodeRef = new Alfresco.util.NodeRef(pNodeRef);

      var redirect = $siteURL("entity-data-lists?nodeRef=" + nodeRef.toString(), {
         site : siteId
      });

      if (type == "bcpg:finishedProduct" || type == "bcpg:semiFinishedProduct") {
         redirect += "&list=compoList";
      } else if (type == "bcpg:packagingKit") {
         redirect += "&list=packagingList";
      }

      return redirect;

   };

   beCPG.util.entityDocumentsURL = function(siteId, path, name, isFullPath) {
      var url = null;

      if (siteId) {
         if (isFullPath) {
            url = 'documentlibrary?path=' + encodeURIComponent(path + '/' + name);
         } else {
            url = 'documentlibrary?path=' + encodeURIComponent('/' + path + '/' + name);
         }
      } else {

         if (path) {
            if (isFullPath) {
               url = 'repository?path=' + encodeURIComponent(path + '/' + name);
            } else {
               url = 'repository?path=' + encodeURIComponent('/' + path.split('/').slice(2).join('/') + '/' + name);

            }
         }
      }
      if (url !== null) {
         url = $siteURL(url, {
            site : siteId
         });
      }
      return url;

   };
   

   beCPG.util.entityDetailsURL = function(siteId, pNodeRef, type) {
      var nodeRef = new Alfresco.util.NodeRef(pNodeRef);
      
      var containerType = "entity";
      if(type=="document" || type =="folder"){
         containerType=type;
      }
      
      return $siteURL(containerType+"-details?nodeRef=" + nodeRef.toString(), {
         site : siteId
      });

   };

   beCPG.util.isEntity = function(record) {
      if (record && record.jsNode && beCPG.util.contains(record.jsNode.aspects, "bcpg:entityListsAspect")) {
         return true;
      }

      if (record && record.aspects !== null && beCPG.util.contains(record.aspects, "bcpg:entityListsAspect")) {
         return true;
      }
      return false;

   };

   beCPG.util.postActivity = function(siteId, activityType, title, page, data, callback) {
      // Mandatory parameter check
      if (!YAHOO.lang.isString(siteId) || siteId.length === 0 || !YAHOO.lang.isString(activityType) || activityType.length === 0 || !YAHOO.lang
            .isString(title) || title.length === 0 || !YAHOO.lang.isObject(data) === null || !(YAHOO.lang
            .isString(data.nodeRef) || YAHOO.lang.isString(data.parentNodeRef))) {
         return;
      }

      var config = {
         method : "POST",
         url : Alfresco.constants.PROXY_URI + "slingshot/activity/create",
         successCallback : {
            fn : callback,
            scope : this
         },
         failureCallback : {
            fn : callback,
            scope : this
         },
         dataObj : YAHOO.lang.merge({
            site : siteId,
            type : activityType,
            title : title,
            page : page
         }, data)
      };

      Alfresco.util.Ajax.jsonRequest(config);

   };
   
   
   
   beCPG.util.getFileIcon = function(p_fileName, p_record, p_isContainer, p_isSimpleView) {

      var node = p_record.jsNode, type = node.type;
      
      var folders = {
            "{http://www.alfresco.org/model/content/1.0}folder" : "folder",
            "cm:folder" : "folder",
            "{http://www.alfresco.org/model/site/1.0}sites" : "site",
            "st:sites" : "site",
            "{http://www.alfresco.org/model/site/1.0}site" : "site",
            "st:site" : "site"
     };

      var iconSize = p_isSimpleView ? 32 : 64;

      if (p_isContainer  && (!Alfresco.util.getFileIcon.types[type] 
              || folders[type]) ) {
          return Alfresco.constants.URL_RESCONTEXT + 'components/documentlibrary/images/folder-' + iconSize + '.png';
      }

      if (p_isSimpleView) {
          return Alfresco.constants.URL_RESCONTEXT + 'components/images/filetypes/'
                + Alfresco.util.getFileIcon(p_fileName, type, iconSize);
      }

      return Alfresco.DocumentList.generateThumbnailUrl(p_record);

  };

})();
