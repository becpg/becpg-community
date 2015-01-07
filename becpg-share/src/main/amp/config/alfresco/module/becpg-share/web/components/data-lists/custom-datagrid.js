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
    * Alfresco Slingshot aliases
    */
   var $html = Alfresco.util.encodeHTML,
      $links = Alfresco.util.activateLinks,
      $userProfile = Alfresco.util.userProfileLink;

   var LIKE_EVENTCLASS = Alfresco.util.generateDomId(null, "like");
   
   // Define constructor...
   beCPG.component.DataGrid = function CustomDataGrid_constructor(htmlId)
   {
	   beCPG.component.DataGrid.superclass.constructor.call(this, htmlId);
	     
	   this.services.likes = new Alfresco.service.Ratings(Alfresco.service.Ratings.LIKES);

	   return this;
   };
   
   /**
    * Extend from Alfresco.component.Base
    */
   YAHOO.extend(beCPG.component.DataGrid, Alfresco.component.DataGrid);

   /**
    * Augment prototype with Common Actions module
    */
   YAHOO.lang.augmentProto(beCPG.component.DataGrid, Alfresco.service.DataListActions);

   /**
    * Augment prototype with main class implementation, ensuring overwrite is enabled
    */
   YAHOO.lang.augmentObject(beCPG.component.DataGrid.prototype,
   {
	   
	   /**
	   * Like/Unlike event handler
	   *
	   * @method onLikes
	   * @param row {HTMLElement} DOM reference to a TR element (or child thereof)
	   */
	  onLikes: function CustomDataGrid_onLikes(row)
	  {
	     var file = this.widgets.dataTable.getRecord(row).getData(),
	        nodeRef = new Alfresco.util.NodeRef(file.nodeRef),
	        likes = file.likes;
	
	     likes.isLiked = !likes.isLiked;
	     likes.totalLikes += (likes.isLiked ? 1 : -1);
	
	     var responseConfig =
	     {
	        successCallback:
	        {
	           fn: function CustomDataGrid_onLikes_success(event, p_nodeRef)
	           {
	              var data = event.json.data;
	              if (data)
	              {                	
	                 // Update the record with the server's value
	                 var record = this._findRecordByParameter(p_nodeRef, "nodeRef"),
	                    file = record.getData(),
	                    likes = file.likes;
	
	                 likes.totalLikes = data.ratingsCount;
	                 this.widgets.dataTable.updateRow(record, file);
	                 
	                 // Post to the Activities Service on the "Like" action
                     if (likes.isLiked)
                     {
                        var activityData =
                        {
                           fileName: record.fileName,
                           nodeRef: file.nodeRef
                        },
                        fileName = (file.itemData.prop_cm_title != null && file.itemData.prop_cm_title.displayValue) ? file.itemData.prop_cm_title.displayValue : "";
                                                
                        //Alfresco.Share.postActivity(this.options.siteId, "org.alfresco.datalists.list-item-liked", fileName, "data-lists?list=" + this.datalistMeta.name, activityData);
                        Alfresco.Share.postActivity(this.options.siteId, "org.alfresco.documentlibrary.file-liked", fileName, "data-lists?list=" + this.datalistMeta.name, activityData);                        
                     }
	              }
	           },
	           scope: this,
	           obj: nodeRef.toString()
	        },
	        failureCallback:
	        {
	           fn: function CustomDataGrid_onLikes_failure(event, p_nodeRef)
	           {
	              // Reset the flag to it's previous state
	              var record = this._findRecordByParameter(p_nodeRef, "nodeRef"),
	                 file = record.getData(),
	                 likes = file.likes;
	
	              likes.isLiked = !likes.isLiked;
	              likes.totalLikes += (likes.isLiked ? 1 : -1);
	              this.widgets.dataTable.updateRow(record, file);
	              Alfresco.util.PopupManager.displayPrompt(
	              {
	                 text: this.msg("message.save.failure", p_nodeRef)
	              });
	           },
	           scope: this,
	           obj: nodeRef.toString()
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
	     var record = this._findRecordByParameter(nodeRef, "nodeRef");
	     this.widgets.dataTable.updateRow(record, file);
	  },
	  
	  /**
       * Fired by YUI when parent element is available for scripting
       *
       * @method onReady
       */
      onReady: function CustomDataGrid_onReady()
      {
         me = this;
         beCPG.component.DataGrid.superclass.onReady.call(this);

         // Hook like/unlike events
         var fnLikesHandler = function CustomDataGrid_fnLikesHandler(layer, args)
         {
            var owner = YAHOO.Bubbling.getOwnerByTagName(args[1].anchor, "div");
            if (owner !== null)
            {
               me.onLikes.call(me, args[1].target.offsetParent, owner);
            }
            return true;
         };
         YAHOO.Bubbling.addDefaultAction(LIKE_EVENTCLASS, fnLikesHandler);
      },
      
      /**
       * Return data type-specific formatter
       *
       * @method getCellFormatter
       * @return {function} Function to render read-only value
       */
      getCellFormatter: function CustomDataGrid_getCellFormatter()
      {
         var scope = this;
         
         /**
          * Data Type custom formatter
          *
          * @method renderCellDataType
          * @param elCell {object}
          * @param oRecord {object}
          * @param oColumn {object}
          * @param oData {object|string}
          */
         return function CustomDataGrid_renderCellDataType(elCell, oRecord, oColumn, oData)
         {
            var html = "";

            // Populate potentially missing parameters
            if (!oRecord)
            {
               oRecord = this.getRecord(elCell);
            }
            if (!oColumn)
            {
               oColumn = this.getColumn(elCell.parentNode.cellIndex);
            }

            if (oRecord && oColumn)
            {
               if (!oData)
               {
                  oData = oRecord.getData("itemData")[oColumn.field];
               }
            
               var datalistColumn = scope.datalistColumns[oColumn.key];
               if (datalistColumn)
               {
            	   if(datalistColumn.name=="cm:likesRatingSchemeCount"){
   					
                 	  var likes = oRecord.getData().likes;
						  html += '<div class="detail detail-social"><span class="item item-social">';
		
					      if (likes.isLiked)
					      {
				    	  	html += '<a class="like-action ' + LIKE_EVENTCLASS + ' enabled" title="' + scope.msg("like.document.remove.tip") + '" tabindex="0"></a>';
					      }
					      else
					      {
					    	html += '<a class="like-action ' + LIKE_EVENTCLASS + '" title="' + scope.msg("like.document.add.tip") + '" tabindex="0">' + scope.msg("like.document.add.label") + '</a>';
					      }
	
					      html += '<span class="likes-count">' + $html(likes.totalLikes) + '</span></span></div>';
                   }
            	   else{
            		   
            		   if (oData)
    	               {
                      
                         oData = YAHOO.lang.isArray(oData) ? oData : [oData];
                         for (var i = 0, ii = oData.length, data; i < ii; i++)
                         {
                            data = oData[i];

                            switch (datalistColumn.dataType.toLowerCase())
                            {
                               case "cm:person":
                                  html += '<span class="person">' + $userProfile(data.metadata, data.displayValue) + '</span>';
                                  break;
                            
                               case "datetime":
                                  html += Alfresco.util.formatDate(Alfresco.util.fromISO8601(data.value), scope.msg("date-format.default"));
                                  break;
                         
                               case "date":
                                  html += Alfresco.util.formatDate(Alfresco.util.fromISO8601(data.value), scope.msg("date-format.defaultDateOnly"));
                                  break;
                         
                               case "text":
                                  html += $links($html(data.displayValue));
                                  break;

                               default:
                                  if (datalistColumn.type == "association")
                                  {
                                     html += '<a href="' + Alfresco.util.siteURL((data.metadata == "container" ? 'folder' : 'document') + '-details?nodeRef=' + data.value) + '">';
                                     html += '<img src="' + Alfresco.constants.URL_RESCONTEXT + 'components/images/filetypes/' + Alfresco.util.getFileIcon(data.displayValue, (data.metadata == "container" ? 'cm:folder' : null), 16) + '" width="16" alt="' + $html(data.displayValue) + '" title="' + $html(data.displayValue) + '" />';
                                     html += ' ' + $html(data.displayValue) + '</a>';
                                  }
                                  else
                                  {
                                     html += $links($html(data.displayValue));
                                  }
                                  break;
                            }

                            if (i < ii - 1)
                            {
                               html += "<br />";
                            }
                         }
                      }
            	   }            	   
               }
            }

            elCell.innerHTML = html;
         };
      }
   }, true);
})();		   
