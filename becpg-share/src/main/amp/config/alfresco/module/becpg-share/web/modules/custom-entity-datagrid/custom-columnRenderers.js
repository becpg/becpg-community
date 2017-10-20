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
if (beCPG.module.EntityDataGridRenderers) {

	   

   YAHOO.Bubbling.fire("registerDataGridRenderer", {
      propertyName : "mltext_bcpg:lkvValue",
      renderer : function(oRecord, data, label, scope) {
         if (oRecord.getData("itemData")["prop_bcpg_depthLevel"] != null) {
            var padding = (oRecord.getData("itemData")["prop_bcpg_depthLevel"].value - 1) * 15;
            return '<span class="' + data.metadata + '" style="margin-left:' + padding + 'px;">' + Alfresco.util
                  .encodeHTML(data.displayValue) + '</span>';
         }
         return Alfresco.util.encodeHTML(data.displayValue);
      }
   });
   
   
   YAHOO.Bubbling
         .fire(
               "registerDataGridRenderer",
               {
                  propertyName : "fm:commentCount",
                  renderer : function(oRecord, data, label, scope, i, ii, elCell, oColumn) {

                     if (data.value != null && data.value != "" && data.value != "0") {

                        if (oColumn.hidden) {
                           scope.widgets.dataTable.showColumn(oColumn);
                           Dom.removeClass(elCell.parentNode, "yui-dt-hidden");
                        }
                        Dom.setStyle(elCell, "width", "32px");
                        Dom.setStyle(elCell.parentNode, "width", "32px");
                        return '<div class="onActionShowComments"><a class="' + scope.id + '-action-link action-link" title="' + scope
                              .msg("actions.comment") + '" href="" rel="edit"><span>' + data.displayValue + '</span></a></div>';
                     }
                     return "";
                  }

               });

   
   YAHOO.Bubbling.fire("registerDataGridRenderer", {
	      propertyName : [ "cm:created","pjt:rlDueDate" ],
	      renderer : function(oRecord, data, label, scope) {
	    	  if (data.value != null){
	    		  return Alfresco.util.formatDate(data.value,"dd/mm/yyyy").toLowerCase();
	    	  }
	         return "";
	      }

	   });


   var LIKE_EVENTCLASS = Alfresco.util.generateDomId(null, "like");


   YAHOO.Bubbling
   .fire(
		   "registerDataGridRenderer",
		   {
			   propertyName : "cm:likesRatingSchemeCount",
			   renderer : function(oRecord, data, label, scope, i, ii, elCell, oColumn) {

				   if(!scope.onLikes){

					   scope.likeServices = new Alfresco.service.Ratings(Alfresco.service.Ratings.LIKES);

					   scope.onLikes = function EntityDataGrid_onLikes(row)
					   {
						   var file = this.widgets.dataTable.getRecord(row).getData(),
						   nodeRef = new Alfresco.util.NodeRef(file.nodeRef),
						   likes = file.likes;

						   if(!file.likes){
							   file.likes = {isLiked : false ,totalLikes : 0 };
						   }

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
							   this.likeServices.set(nodeRef, 1, responseConfig);
						   }
						   else
						   {
							   this.likeServices.remove(nodeRef, responseConfig);
						   }
						   var record = this._findRecordByParameter(nodeRef, "nodeRef");
						   this.widgets.dataTable.updateRow(record, file);

					   };

					   // Hook like/unlike events
					   var fnLikesHandler = function CustomDataGrid_fnLikesHandler(layer, args)
					   {
						   var owner = YAHOO.Bubbling.getOwnerByTagName(args[1].anchor, "div");
						   if (owner !== null)
						   {
							   scope.onLikes.call(scope, args[1].target.offsetParent, owner);           		     
						   }
						   return true;
					   };
					   YAHOO.Bubbling.addDefaultAction(LIKE_EVENTCLASS, fnLikesHandler);

				   }


				   var likes = oRecord.getData().likes, html = "";
				   if(likes){
					   html += '<div class="detail detail-social"><span class="item item-social">';

					   if (likes.isLiked)
					   {
						   html += '<a class="like-action ' + LIKE_EVENTCLASS + ' enabled" title="' + scope.msg("like.document.remove.tip") + '" tabindex="0"></a>';
					   }
					   else
					   {
						   html += '<a class="like-action ' + LIKE_EVENTCLASS + '" title="' + scope.msg("like.document.add.tip") + '" tabindex="0">' + scope.msg("like.document.add.label") + '</a>';
					   }

					   html += '<span class="likes-count">' + Alfresco.util.encodeHTML(likes.totalLikes) + '</span></span></div>';

				   }
				   return html;
			   }

		   });

   
  

}
