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
/**
 * Document and Folder header component.
 * 
 * @namespace beCPG
 * @class beCPG.custom.NodeHeader
 */
(function() {

   /**
    * NodeHeader constructor.
    * 
    * @param {String}
    *            htmlId The HTML id of the parent element
    * @return {beCPG.custom.NodeHeader} The new NodeHeader instance
    * @constructor
    */
   beCPG.custom.NodeHeader = function NodeHeader_constructor(htmlId) {
      beCPG.custom.NodeHeader.superclass.constructor.call(this, htmlId);

      YAHOO.Bubbling.on("folderCopied", this.onEntityCopied , this);
      
      YAHOO.Bubbling.on("dirtyDataTable", this.entityUpdated , this);
      
      return this;
   };

   YAHOO
         .extend(
               beCPG.custom.NodeHeader,
               Alfresco.component.NodeHeader,
               {
                    
            	  ws : null,
                  /**
                   * Fired by YUI when parent element is available for scripting. Initial History Manager event
                   * registration
                   * 
                   * @method onReady
                   */
                  onReady : function NodeHeader_onReady() {
                	  
                   var me = this;
                	  
                   
                   // MNT-9081 fix, redirect user to the correct location, if requested site is not the actual site where document is located
                   if (this.options.siteId != this.options.actualSiteId)
                   {
                      // Moved to a site...
                      if (this.options.actualSiteId != null)
                      {
                         var inRepository = this.options.actualSiteId === null,
                             correctUrl = window.location.protocol + "//" + window.location.host + Alfresco.constants.URL_PAGECONTEXT + 
                                   (inRepository ? "" : "site/" + this.options.actualSiteId + "/") + "entity-data-lists" + window.location.search;
                         Alfresco.util.PopupManager.displayPrompt(
                         {
                            text: (inRepository ? this.msg("message.document.moved.repo") : this.msg("message.document.moved", this.options.actualSiteId)),
                            buttons: [
                            {
                               text: this.msg("button.ok"),
                               handler: function()
                               {
                                  window.location = correctUrl;
                               },
                               isDefault: true
                            }]
                         });
                         YAHOO.lang.later(10000, this, function()
                         {
                            window.location = correctUrl;
                         });
                         return;
                      }
                      else
                      {
                         // Moved elsewhere in repository...
                         var correctUrl = "/share/page/entity-data-lists?list=View-properties&nodeRef=" + this.options.nodeRef;;
                         Alfresco.util.PopupManager.displayPrompt(
                         {
                            text: this.msg("message.document.movedToRepo"),
                            buttons: [
                            {
                               text: this.msg("button.ok"),
                               handler: function()
                               {
                                  window.location = correctUrl;
                               },
                               isDefault: true
                            }]
                         });
                         YAHOO.lang.later(10000, this, function()
                         {
                            window.location = correctUrl;
                         });
                         return;
                      }
                  }
                   
                	  
                	try {
                		var pathBreadCrumbs = null;
	                	if(sessionStorage.pathBreadCrumbs!=null){
	                		pathBreadCrumbs = JSON.parse(sessionStorage.pathBreadCrumbs);
	                	}
	          			if(pathBreadCrumbs == null){
	          				pathBreadCrumbs = { currentNode : null, path : []};
	          			} 
	          			
	          			var printBreadCumbsPath = function(path){

         		    		 YAHOO.util.Dom.removeClass(me.id+"-bcpath","hidden");
         		    		 var html = '<ul class="bcpath">';
         		    		 for(var i = 0 ; i<path.length;i++){		
         		    			var type = path[i].type,  url = beCPG.util.entityURL(path[i].siteId, path[i].nodeRef, type, null, path[i].listId)+"&bcPath=true";
         		    			 html += '<li style="z-index:'+(20-i)+'"><span class="' +type.split(':')[1] + '" ><a href="' + url + '">'
	    						+ Alfresco.util.encodeHTML(path[i].name) + '</a></li>';
         		    		 }
         		    		 html += "</ul>";
         		    		 YAHOO.util.Dom.get(me.id+"-bcpath").innerHTML = html;
	          			};
	          			
	          		    if(this.options.showRelativePath){
	          		    	if(pathBreadCrumbs.currentNode != null && pathBreadCrumbs.currentNode.nodeRef != this.options.nodeRef) {
	          		    		 var isInPath = false; 
	          		    		 for(var i = 0 ; i<pathBreadCrumbs.path.length;i++){
	          		    			 if(pathBreadCrumbs.path[i].nodeRef == this.options.nodeRef){
	          		    				pathBreadCrumbs.path = pathBreadCrumbs.path.slice(0,i);
	          		    				isInPath = true;
	          		    				break;
	          		    			 }
	          		    			 
	          		    		 }
	          		    		if(!isInPath){
	          		    			if(this.options.listFrom!=null){
	          		    				pathBreadCrumbs.currentNode.listId = this.options.listFrom;
	          		    			}
	          		    			pathBreadCrumbs.path.push( pathBreadCrumbs.currentNode);
	          		    		}	
	          		    		pathBreadCrumbs.currentNode = {siteId : this.options.siteId,
	          		    				nodeRef : this.options.nodeRef,
	          		    				name:this.options.itemName,
	          		    				type:this.options.itemType,
	          		    				listId : this.options.listId};
	          					
	          		    		printBreadCumbsPath(pathBreadCrumbs.path);
	          		    		 
	          		    		 
	          		    	} else if(pathBreadCrumbs.currentNode!=null){
	          		    		printBreadCumbsPath(pathBreadCrumbs.path);
	          		    	}
	          			} else if(pathBreadCrumbs.currentNode==null ||  pathBreadCrumbs.currentNode.nodeRef != this.options.nodeRef){
	          				//Reset
	          				pathBreadCrumbs = { currentNode :{siteId : this.options.siteId,
	          					nodeRef : this.options.nodeRef,
	          					name:this.options.itemName,
	          					type:this.options.itemType,
      		    				listId : this.options.listId}, path : []};
	          			} else if(pathBreadCrumbs.path!=null && pathBreadCrumbs.path.length>0){
	          				printBreadCumbsPath(pathBreadCrumbs.path);
	          			}
	          		  sessionStorage.pathBreadCrumbs = JSON.stringify(pathBreadCrumbs);
	          		    
                	} catch(e){
                	  delete sessionStorage.pathBreadCrumbs;
                	}
                	
                	//Websocket

                	this.registerWebSocket();
                
                    this.nodeType = "entity";

                  },
                  entityUpdated: function NodeHeader_entityUpdated(layer, args){
                	  if(this.ws!=null){
                		  var message = {
                     			 type : "UPDATE",
                     			 user : Alfresco.constants.USERNAME	 
                     	 };
                		  this.ws.send( YAHOO.lang.JSON.stringify(message) );
                	  }
                	  
                  },
                  
                  registerWebSocket : function NodeHeader_entityUpdated(){
                	  if ("WebSocket" in window && this.ws==null)
                      {
                         var protocolPrefix = (window.location.protocol === 'https:') ? 'wss:' : 'ws:', me = this;
                         
                         me.ws = new WebSocket(protocolPrefix + '//' + location.host + "/share/becpgws/"+this.options.nodeRef.replace(":/","")+"/"+Alfresco.constants.USERNAME);
          		
                         me.ws.onmessage = function (evt) 
                         { 
                        	 
                            var message = YAHOO.lang.JSON.parse(evt.data);
                            if(message.type && message.type == "JOINING" && message.user!=Alfresco.constants.USERNAME){
                            	
                            	if(YAHOO.util.Dom.get(me.id+"-chat-user-"+message.user)==null){
                            		 var ulEl = YAHOO.util.Dom.get(me.id+"-node-users");
	                            	 var html = ulEl.innerHTML;
	                            	 
	                            	 html += '<span id="'+me.id+'-chat-user-'+message.user+'" class="avatar" title="' + message.user + '">';
	                            	 html += Alfresco.Share.userAvatar(message.user, 32);
	                            	 html += "</span>";
	                            	 
	                            	 ulEl.innerHTML = html;
                            	}
                            	
                            } else if(message.type == "LEAVING" && message.user!=Alfresco.constants.USERNAME){
                            	 var child = document.getElementById(me.id+"-chat-user-"+message.user);
                            	 if(child!=null){
                            		 child.parentNode.removeChild(child);
                            	 }
                            	
                            } else if(message.type == "UPDATE" && message.user!=Alfresco.constants.USERNAME){
                            	YAHOO.util.Dom.addClass(me.id+"-chat-user-"+message.user,"user-activiti");
                            	setTimeout(function(){
                            	   YAHOO.Bubbling.fire("refreshDataGrids");
                            	   YAHOO.util.Dom.removeClass(me.id+"-chat-user-"+message.user,"user-activiti");
                            	},3000);
                            }
                            
                         };
          				
                      }
                  },

                  /**
                   * Generic file action event handler
                   *
                   * @method onFileAction
                   * @param layer {object} Event fired
                   * @param args {array} Event parameters (depends on event type)
                   */
                  onEntityCopied: function DL_onEntityCopied(layer, args)
                  {
                     var obj = args[1];
                     if (obj)
                     {
                    	 var siteId = null;
                    	 if(obj.destination!=null && obj.destination.indexOf("Sites")>0
                    			 && obj.destination.split('/').length>1){
                    		 siteId = obj.destination.split('/')[2];
                    	 }
                    	 
                    	 window.location = "/share/page/"+ (siteId ? 'site/'+siteId+"/": '')
                    	 + "entity-data-lists?list="
                    	 +(this.options.listId? this.options.listId:"View-properties")
                    	 +"&nodeRef=" + obj.nodeRef;
                     }
                  },
                 
                  /**
                   * Refresh component in response to metadataRefresh event
                   * 
                   * @method doRefresh
                   */
                  doRefresh : function NodeHeader_doRefresh() {
                     YAHOO.Bubbling.unsubscribe("metadataRefresh", this.doRefresh, this);

                     var url = 'components/entity-data-lists/entity-header?nodeRef={nodeRef}&rootPage={rootPage}' 
                             + '&rootLabelId={rootLabelId}&showPath={showPath}' 
                             + (this.options.pagecontext ? '&pagecontext={pagecontext}': '')
                             + (this.options.libraryRoot ? '&libraryRoot={libraryRoot}' : '') 
                             + (this.options.siteId ? '&site={siteId}': '')
                             + (this.options.showOnlyLocation ? '&showOnlyLocation={showOnlyLocation}':'');

                     this.refresh(url);
                  }
                 

               });
})();
