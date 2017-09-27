/*******************************************************************************
 *  Copyright (C) 2010-2017 beCPG. 
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
	      propertyName : [ "bcpg:alData" ],
	      renderer : function(oRecord, data, label, scope) {
	    	  var activityType = oRecord.getData("itemData")["prop_bcpg_alType"].value;
	    	  var user = oRecord.getData("itemData")["prop_bcpg_alUserId"];
	    	  var dateCreated = oRecord.getData("itemData")["prop_cm_created"];
	    	  var html = "";
              if(data.title || activityType == "Datalist" ){
            	  var title = "";
            	  var className = data.className!=null ? data.className : "entity"; 	  
            	  title = "<span class=\""+className+"\">"+Alfresco.util.encodeHTML(data.title)+"</span>";
            	  if(activityType == "State"){
            		  title = scope.msg("entity.activity.state.change", title, scope.msg("data.state." +data.beforeState.toLowerCase()), scope.msg("data.state."+data.afterState.toLowerCase()));
            	  } else if (activityType == "Datalist" ){
            		  if (data.title == null || data.title.indexOf(className)>0){
            			  title  = scope.msg("entity.activity.datalist.simple", scope.msg("data.list."+className));
            		  } else{
            			  title  = scope.msg("entity.activity.datalist."+data.activityEvent.toLowerCase(), title, scope.msg("data.list."+className) );
            		  }
            		  
            	  } else if(activityType == "Entity"|| activityType == "Formulation" || activityType == "Report"){
            		  title  = scope.msg("entity.activity."+activityType.toLowerCase(), title);
            	  } else if(activityType == "Comment"){
            		 title  = scope.msg("entity.activity.comment."+data.activityEvent.toLowerCase(), title);
            	  } else if(activityType == "Content"){
            		  if(data.activityEvent == "Delete"){
            			  title = '<span class="doc-file"><img src="' + Alfresco.constants.URL_RESCONTEXT + 'components/images/filetypes/' + Alfresco.util
 	                     .getFileIcon(data.title, "cm:content", 16) + '" />'+Alfresco.util.encodeHTML(data.title)+'</span>';
            		  } else {
	            		 title = '<span class="doc-file"><a  href="' +  beCPG.util.entityURL(oRecord.getData("siteId"),data.contentNodeRef, "document") + 
	            		 '"><img src="' + Alfresco.constants.URL_RESCONTEXT + 'components/images/filetypes/' + Alfresco.util
	                     .getFileIcon(data.title, "cm:content", 16) + '" />'+Alfresco.util.encodeHTML(data.title)+'</a></span>';
            		  }
            		 title  = scope.msg("entity.activity.content."+data.activityEvent.toLowerCase(), title);
            	  }  else if(activityType == "Merge"){
            		  title  = scope.msg("entity.activity.merge", title, data.branchTitle );
            	  } else if(activityType == "Version"){
            		  title  = scope.msg("entity.activity.version", title, data.versionLabel, data.versionNodeRef );
            	  }
            	  
            	  html += '<div class="entity-activity-details">';
    	          html += '   <div class="icon">' + Alfresco.Share.userAvatar(user.value,32) + '</div>';
    	          html += '   <div class="details">';
    	          html += '      <span class="user-info">';
    	          html += Alfresco.util.userProfileLink(user.value, user.displayValue, 'class="theme-color-1"') + ' ';
    	          html += '      </span>';
    	          html += '      <span class="date-info">';
    	          html += Alfresco.util.relativeTime(Alfresco.util.fromISO8601(dateCreated.value)) +' ('+  Alfresco.util.formatDate(dateCreated.value	, Alfresco.util.message(scope.msg("date.format"))) + ') <br/>';
    	          html += '      </span>';
    	          html += '      <div class="activity-title">' + title + '</div>';
    	          if(data.content){
    	         	html += '      <div class="activity-content">' + (data.content) + '</div>';
    	          }
    	          html += '   </div>';
    	          html += '   <div class="clear"></div>';
    	          html += '</div>';
            	  
              }
	    	  return html;

	      }

	   });
   
   YAHOO.Bubbling.fire("registerDataGridRenderer", {
	      propertyName : "sec:groupsAssignee",
	      renderer : function(oRecord, data, label, scope) {
	         if (data.displayValue != null) {
	            return Alfresco.util.encodeHTML(data.displayValue);
	         }
	         return Alfresco.util.encodeHTML(data.metadata);

	      }

	   });

 YAHOO.Bubbling.fire("registerDataGridRenderer", {
	      propertyName : [ "cm:created" ],
	      renderer : function(oRecord, data, label, scope) {
	    	  if (data.value != null){
	    		  return Alfresco.util.formatDate(data.value,"ddd. d mmm. yyyy").toLowerCase();
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
