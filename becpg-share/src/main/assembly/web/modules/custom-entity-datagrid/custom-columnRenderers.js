/*******************************************************************************
 *  Copyright (C) 2010-2021 beCPG. 
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

						if(scope.options  && scope.options.itemType == "pjt:project"){
							return "";
						}

						if (oColumn.hidden) {
							oColumn.showAfterRender = true;
							oColumn.showAfterRenderSize = 32;
						}

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
			if(data.title || activityType == "Datalist"  || activityType == "DatalistCopy"){
				var title = "";
				var className = data.className!=null ? data.className : "entity";
				var charactType = data.charactType!=null ? data.charactType : className;
				title = "<span class=\""+charactType+"\">"+Alfresco.util.encodeHTML(data.title)+"</span>";
				if(activityType == "State"){
					title = scope.msg("entity.activity.state.change", title, scope.msg("data.state." +data.beforeState.toLowerCase()), scope.msg("data.state."+data.afterState.toLowerCase()));
				} else if (activityType == "Datalist" ){
					if (data.title == null || data.title.indexOf(className)>0){
						title  = scope.msg("entity.activity.datalist.simple", scope.msg("data.list."+className));
					} else if (className == "lvValue" || className == "lkvValue") {
						title  = scope.msg("entity.activity.datalist."+data.activityEvent.toLowerCase(), title, scope.msg("data.list."+ data.parentName.toLowerCase()));
					} else{
						title  = scope.msg("entity.activity.datalist."+data.activityEvent.toLowerCase(), title, scope.msg("data.list."+className) );
					}

				} else if (activityType == "DatalistCopy" ){
					title = "<span class=\""+data.entityType+"\">"+Alfresco.util.encodeHTML(data.title)+"</span>";
					title  = scope.msg("entity.activity.datalist.copy." + data.activityEvent.toLowerCase(), scope.msg("data.list."+className), title);
				} else if(activityType == "Entity"|| activityType == "Formulation" || activityType == "Report"){
					title  = scope.msg("entity.activity."+activityType.toLowerCase(), title);
				} else if(activityType == "Export"){
					title  = scope.msg("entity.activity.export", scope.msg("data.list."+className), data.title);
				} else if(activityType == "ChangeOrder"){
					title = '<span class="entity ecm-changeOrder"><a  href="' +  beCPG.util.entityURL(oRecord.getData("siteId"),data.entityNodeRef) + 
						'">'+Alfresco.util.encodeHTML(data.title)+'</a></span>';
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

				if (data.properties) {
				    html += '      <div class="before-after-prop">';
				    html += '      <table><thead><tr>';
				    html += '      <th class="prop-col">' + scope.msg("entity.activity.property") + '</th>';
				    html += '      <th class="before-after-col">' + scope.msg("entity.activity.before") + '</th>';
				    html += '      <th class="before-after-col">' + scope.msg("entity.activity.after") + '</th></tr></thead><tbody>';
				
				    var count = 0;
				    data.properties.forEach(function(prop) {
				        var before = prop.before;
				        var after = prop.after;
				        var locale = "";
				
				        var isBeforeArray = Array.isArray(before);
				        var isAfterArray = Array.isArray(after);
				
				        var hasBefore = before != null && (isBeforeArray ? before.length > 0 && before[0] != null && typeof before[0] === 'object' : typeof before === 'object');
				        var hasAfter = after != null && (isAfterArray ? after.length > 0 && after[0] != null && typeof after[0] === 'object' : typeof after === 'object');
				
				        if (hasBefore) {
				            var beforeObj = isBeforeArray ? before[0] : before;
				            var afterObj = isAfterArray ? (after.length > 0 ? after[0] : {}) : after;
				
				            Object.keys(beforeObj).forEach(function(key) {
				                var beforeValue = beforeObj[key] ? beforeObj[key] : "";
				                var afterValue = afterObj[key] ? afterObj[key] : "";
				                if (beforeValue != afterValue) {
				                    locale = key;
				                    if (key.indexOf("_") > -1) {
				                        locale = key.substring(3,5).toLowerCase();
				                    }
				                    html += '      <tr '+(count%2 == 0 ? '' : 'class="grey"')+'><td>' + prop.title +
				                    ' <img class="icon16_11" src="' + Alfresco.constants.URL_RESCONTEXT + 'components/images/flags/' + locale + '.png" /></td>';
				                    html += '      <td>' + beforeValue + '</td>';
				                    html += '      <td>' + afterValue + '</td></tr>';
				                    count++;
				                }
				            });
				
				            if (hasAfter) {
				                Object.keys(afterObj).forEach(function(key) {
				                    if (!beforeObj.hasOwnProperty(key)) {
				                        var afterValue = afterObj[key] ? afterObj[key] : "";
				                        locale = key;
				                        if (key.indexOf("_") > -1) {
				                            locale = key.substring(3,5).toLowerCase();
				                        }
				                        html += '      <tr '+(count%2 == 0 ? '' : 'class="grey"')+'><td>' + prop.title +
				                        ' <img class="icon16_11" src="' + Alfresco.constants.URL_RESCONTEXT + 'components/images/flags/' + locale + '.png" /></td>';
				                        html += '      <td> </td>';
				                        html += '      <td>' + afterValue + '</td></tr>';
				                        count++;
				                    }
				                });
				            }
				        } else if (hasAfter) {
				            var afterObj = isAfterArray ? (after.length > 0 ? after[0] : {}) : after;
				            html += '      <td> </td>';
				            Object.keys(afterObj).forEach(function(key) {
				                if (afterObj[key]) {
				                    locale = key;
				                    if (key.indexOf("_") > -1) {
				                        locale = key.substring(3,5).toLowerCase();
				                    }
				                    html += '      <tr '+(count%2 == 0 ? '' : 'class="grey"')+'><td>' + prop.title +
				                    ' <img class="icon16_11" src="' + Alfresco.constants.URL_RESCONTEXT + 'components/images/flags/' + locale + '.png" /></td>';
				                    html += '      <td> </td>';
				                    html += '      <td>' + afterObj[key] + '</td></tr>';
				                    count++;
				                }
				            });
				        } else {
				            html += '      <tr '+(count%2 == 0 ? '' : 'class="grey"')+'><td>' + prop.title + '</td>';
				            html += '      <td>' + (before != null ? before : ' ') + '</td>';
				            html += '      <td>' + (after != null ? after : ' ') + '</td></tr>';
				            count++;
				        }
				    });
				    html += '    </tbody></table></div>';
				}

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
