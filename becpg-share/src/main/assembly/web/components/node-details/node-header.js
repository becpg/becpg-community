/**
 * Copyright (C) 2005-2011 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

/**
 * Document and Folder header component.
 *
 * @namespace Alfresco
 * @class Alfresco.component.NodeHeader
 */
(function()
{
   /**
    * NodeHeader constructor.
    *
    * @param {String} htmlId The HTML id of the parent element
    * @return {Alfresco.component.NodeHeader} The new NodeHeader instance
    * @constructor
    */
   Alfresco.component.NodeHeader = function NodeHeader_constructor(htmlId)
   {
      Alfresco.component.NodeHeader.superclass.constructor.call(this, "Alfresco.component.NodeHeader", htmlId);

      YAHOO.Bubbling.on("metadataRefresh", this.doRefresh, this);

      this.nodeType = null;

      return this;
   };

   YAHOO.extend(Alfresco.component.NodeHeader, Alfresco.component.Base,
   {
      /**
       * Object container for initialization options
       *
       * @property options
       * @type object
       */
      options:
      {
         /**
          * Current page context, if any (e.g. "mine", "shared" - as extracted from URI template)
          * 
          * @property pagecontext
          * @type string
          * @default null
          */
         pagecontext: null,
         
         /**
          * 
          * @property libraryRoot
          * @type string
          * @default null
          */
         libraryRoot: null,
         
         /**
          * Reference to the current document
          *
          * @property nodeRef
          * @type string
          */
         nodeRef: null,

         /**
          * Requested siteId, if any.
          *
          * @property siteId
          * @type string
          */
         siteId: "",

         /**
          * Actual siteId, if any.
          *
          * @property actualSiteId
          * @type string
          */
         actualSiteId: "",

         /**
          * Root page to create links to.
          *
          * @property rootPage
          * @type string
          * @default "documentlibrary"
          */
         rootPage: "documentlibrary",

         /**
          * Root label ID. The I18N property of the document library root container.
          *
          * @property rootLabelId
          * @type string
          * @default "path.documents"
          */
         rootLabelId: "path.documents",

         /**
          * Show only the location. Overrides the other option settings.
          * 
          * @property showOnlyLocation
          * @type boolean
          * @default false
          */
         showOnlyLocation: false,
         
         /**
          * Flag indicating whether or not to show favourite
          *
          * @property showFavourite
          * @type boolean
          * @default: true
          */
         showFavourite: true,

         /**
          * Flag indicating whether or not to show likes
          *
          * @property showLikes
          * @type boolean
          * @default: true
          */
         showLikes: true,

         /**
          * Flag indicating whether or not to show comments
          *
          * @property showComments
          * @type boolean
          * @default: true
          */
         showComments: true,

         /**
          * Flag indicating whether or not to show show quickshare
          *
          * @property showQuickShare
          * @type boolean
          * @default: true
          */
         showQuickShare: true,

		 /**
          * Flag indicating whether or not to show show basket
          *
          * @property showBasket
          * @type boolean
          * @default: true
          */
         showBasket: false,

         /**
          * Flag indicating whether or not to show AI suggestion
          *
          * @property showAiSuggestion
          * @type boolean
          * @default: false
          */
         showAiSuggestion: false,

         /**
          * Flag indicating whether or not to show download button
          *
          * @property showDownload
          * @type boolean
          * @default: true
          */
         showDownload: true,

         /**
          * Flag indicating whether or not to show path
          *
          * @property showPath
          * @type boolean
          * @default: true
          */
         showPath: true,

         /**
          * Object describing if the nodeRef is liked by the current user and other
          *
          * @property likes
          * @type {Object}
          */
         likes: {},

         /**
          * Whether the node we're currently viewing is a container or not
          *
          * @property isContainer
          * @type boolean
          */
         isContainer: false,

         /**
          * Shared id if the node has been shared (only possible for documents at them moment)
          *
          * @property sharedId
          * @type string
          * @default null
          */
         sharedId: null,

         /**
          * The user that shared the node
          *
          * @property sharedBy
          * @type string
          * @default null
          */
         sharedBy: null,

         /**
          * Flag indicating whether or not to show item modifier
          * 
          * @property showItemModifier
          * @type boolean
          * @default: true
          */
         showItemModifier: true
      },

      /**
       * Fired by YUI when parent element is available for scripting.
       * Initial History Manager event registration
       *
       * @method onReady
       */
      onReady: function NodeHeader_onReady()
      {
	      var me = this;
          // MNT-9081 fix, redirect user to the correct location, if requested site is not the actual site where document is located
          if (this.options.siteId != this.options.actualSiteId)
          {
             // Moved to a site...
             if (this.options.actualSiteId != null)
             {
                var inRepository = this.options.actualSiteId === null,
                    correctUrl = window.location.protocol + "//" + window.location.host + Alfresco.constants.URL_PAGECONTEXT + 
                          (inRepository ? "" : "site/" + this.options.actualSiteId + "/") + "document-details" + window.location.search;
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
                var correctUrl = "/share/page/document-details?nodeRef=" + this.options.nodeRef;
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

         // Add breadcrumbs display functionality
         try {
            var pathBreadCrumbs = {};
            if(sessionStorage.pathBreadCrumbs) {
              pathBreadCrumbs = JSON.parse(sessionStorage.pathBreadCrumbs);
            }
            
            var printBreadCumbsPath = function(path) {
               YAHOO.util.Dom.removeClass(me.id+"-bcpath", "hidden");
               var html = '<ul class="bcpath">';
               for(var i = 0; i < path.length; i++) {
                  var type = path[i].type;
                  if(type == "pjt:taskList") {
                     var url = Alfresco.constants.URL_PAGECONTEXT+"task-edit?taskId="+ path[i].nodeRef;
                  } else {
                     var url = beCPG.util.entityURL(path[i].siteId, path[i].nodeRef, type, null, path[i].listId)+"&bcPath=true";
                  }
                  html += '<li style="z-index:'+(20-i)+'"><span class="' + type.split(':')[1] + '" ><a href="' + url + '">' 
                     + Alfresco.util.encodeHTML(path[i].name) + '</a></li>';
               }
               html += "</ul>";
               YAHOO.util.Dom.get(me.id+"-bcpath").innerHTML = html;
            };
            
            if(this.options.showPath) {
               if(pathBreadCrumbs.currentNode != null && pathBreadCrumbs.path && pathBreadCrumbs.path.length > 0) {
                  printBreadCumbsPath(pathBreadCrumbs.path);
               }
            }
         } catch(e) {
            // Silent error handling
            if (console && console.error) {
               console.error("Error displaying breadcrumbs:", e);
            }
         }
         
         this.nodeType = this.options.isContainer ? "folder" : "document";

         if (this.options.showLikes && !this.options.showOnlyLocation && this.options.lock != "READ_ONLY_LOCK")
         {
            // Create like widget
            new Alfresco.Like(this.id + '-like').setOptions(
            {
               nodeRef: this.options.nodeRef,
               siteId: this.options.siteId,
               type: this.nodeType,
               displayName: this.options.displayName
            }).display(this.options.likes.isLiked, this.options.likes.totalLikes);
         }

         if (this.options.showFavourite && !this.options.showOnlyLocation)
         {
            // Create favourite widget
            new Alfresco.Favourite(this.id + '-favourite').setOptions(
            {
               nodeRef: this.options.nodeRef,
               type: this.nodeType
            }).display(this.options.isFavourite);
         }

         if (this.options.showQuickShare && !this.options.showOnlyLocation)
         {
            // Create favourite widget
            new Alfresco.QuickShare(this.id + '-quickshare').setOptions(
                  {
                     nodeRef: this.options.nodeRef,
                     displayName: this.options.displayName
                  }).display(this.options.sharedId, this.options.sharedBy);
         }

		if (this.options.showBasket && !this.options.showOnlyLocation)
         {
	
					function renderBasket() {
						var record = {
							nodeRef: me.options.nodeRef,
							siteId: me.options.siteId,
							type: me.nodeType,
							displayName: me.options.displayName,
							jsNode: {
								isContainer : false
							}
						};

						var html = "", basketService = new beCPG.service.Basket();

						if (basketService.isInBasket(record)) {
							html = '<a class="basket-action enabled" title="' + me.msg("basket.remove.tip") + '" tabindex="0"></a>';
						} else {
							html = '<a class="basket-action" title="' + me.msg("basket.add.tip") + '" tabindex="0">' + me.msg("basket.add.label") + '</a>';
						}
						var spanEl = Dom.get(me.id + '-basket');
						if(spanEl!=null){
							spanEl.innerHTML = html;
	
							Alfresco.util.useAsButton(Selector.query("a", spanEl, true), function(e) {
								basketService.toggle(record);
								renderBasket();
								YAHOO.util.Event.preventDefault(e);
								
							}, null, me);
						}

					}

					renderBasket();

         }
         
         if (beCPG.constants.AI_ENABLED && this.options.showAiSuggestion && !this.options.showOnlyLocation)
          {
              function initAiSuggestion() {
                 
                  var record = {
                      nodeRef: me.options.nodeRef,
                      siteId: me.options.siteId,
                      type: me.nodeType,
                      displayName: me.options.displayName,
                      jsNode: {
                          isContainer: false,
                          properties: me.options.nodeProperties || {}
                      }
                  };

                  var html = "", aiSuggestionService = new beCPG.service.AiSuggestion();

                  if (aiSuggestionService.isEnabled(record)) {
							html = '<a class="ai-suggestion-action enabled" title="' + me.msg("aisuggestion.show.tip") + '" tabindex="0"></a>';
						} else {
							html = '<a class="ai-suggestion-action" title="' + me.msg("aisuggestion.show.tip") + '" tabindex="0">' + me.msg("aisuggestion.show.label") + '</a>';
						}
                  var spanEl = Dom.get(me.id + '-aisuggestion');
                  if(spanEl != null) {
                      spanEl.innerHTML = html;
                      
                      YAHOO.util.Event.addListener(spanEl, "click", function(e)
                      {
                          aiSuggestionService.showSuggestions(record, e);
                          YAHOO.util.Event.preventDefault(e);
                      }, null, me);
                  }
              }
              
              initAiSuggestion();
          }

         // Parse the date
         if (this.options.showItemModifier && !this.options.showOnlyLocation)
         {
            var dateEl = Dom.get(this.id + '-modifyDate');
            dateEl.innerHTML = Alfresco.util.formatDate(Alfresco.util.fromISO8601(dateEl.innerHTML), Alfresco.util.message("date-format.default"));
         }
         else
         {
            var nodeHeader = YAHOO.util.Dom.getElementsByClassName("node-header")[0];
         }
      },

      /**
       * Refresh component in response to metadataRefresh event
       *
       * @method doRefresh
       */
      doRefresh: function NodeHeader_doRefresh()
      {
         YAHOO.Bubbling.unsubscribe("metadataRefresh", this.doRefresh, this);

         var url = 'components/node-details/node-header?nodeRef={nodeRef}&rootPage={rootPage}' +
            '&rootLabelId={rootLabelId}&showFavourite={showFavourite}&showLikes={showLikes}' +
            '&showComments={showComments}&showQuickShare={showQuickShare}&showDownload={showDownload}&showPath={showPath}&showItemModifier={showItemModifier}' +
            '&showAiSuggestion={showAiSuggestion}' +
            (this.options.pagecontext ? '&pagecontext={pagecontext}' :  '') + 
            (this.options.libraryRoot ? '&libraryRoot={libraryRoot}' :  '') +
            (this.options.siteId ? '&site={siteId}' :  '');

         this.refresh(url);
      }
   });
})();
