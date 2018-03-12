
/**
 * @module becpg/header/BeCPGRecentMenu
 * @extends module:alfresco/menus/AlfMenuBarPopup
 * @mixes module:alfresco/core/CoreXhr
 * @author Matthieu Laborie
 */
define([ "dojo/_base/declare", "alfresco/core/CoreXhr", "dojo/_base/lang", "dojo/_base/array", "dojo/aspect", "dijit/registry",
		"alfresco/menus/AlfMenuGroup", "alfresco/header/AlfMenuItem", "alfresco/header/AlfCascadingMenu", "dojo/dom-style" ], function(declare,
		AlfXhr, lang, array, aspect, registry, AlfMenuGroup, AlfMenuItem, AlfCascadingMenu, domStyle) {

	/**
	 * This extends "alfresco/header/AlfMenuGroup" to add additional
	 * subscriptions
	 */
	return declare([ AlfMenuGroup, AlfXhr ], {

		/**
		 * This should be set with the current nodeRef
		 * 
		 * @instance
		 * @type {string}
		 * @default null
		 */
		entityNodeRef : null,

		pageUri : "",
		
		list : null,
		
		/**
		 * Extend the default postCreate function to setup handlers for adding
		 * the 'Useful' group once all the other menu groups and items have been
		 * processed.
		 * 
		 * @instance
		 */
		postCreate : function alf_menus_header_BeCPGRecentMenu__postCreate() {
			this.inherited(arguments);
			this.alfLog("log", "Loading recents");

			var nodeRef = this.entityNodeRef, url = Alfresco.constants.PROXY_URI + "becpg/dockbar";
	        if (nodeRef !== null && nodeRef.length > 0) {
	            url += "?entityNodeRef=" + nodeRef.replace(/\\/g,"") + "&list="+this.list;
	         } 
	         
			this.serviceXhr({
				url : url,
				method : "GET",
				successCallback : this._menuDataLoaded,
				callbackScope : this
			});

		},

		_menuDataLoaded : function alfresco_header_BeCPGRecentMenu___menuDataLoaded(response, originalRequestConfig) {
			this.alfLog("log", "Menu data loaded successfully", response);

			// Add recent groups if there are some...
			if (response.items && response.items.length > 0) {

				for (var i = 0; i < response.items.length; i++) {
					var item = response.items[i];
					
					var targetUrl = "entity-data-lists?list=View-properties&nodeRef=" + item.nodeRef;
					if (this.pageUri.indexOf("entity-data-lists")) {
						targetUrl = "entity-data-lists?nodeRef=" + item.nodeRef;
						// TODO Should be split or better
						// page.url.args.list
						if (item.list != null && item.list != "null") {
							targetUrl += "&list="+item.list;
						} else {
	                         targetUrl += "&list=View-properties";
	                     }

					}

					if (item.site) {
						targetUrl = "site/" + item.site.shortName + "/" + targetUrl;
					}
					
					
					this.addChild(new AlfMenuItem({
						label : item.displayName,
						iconClass : "entity " + item.itemType.split(":")[1],
						targetUrl : targetUrl
					}));

				}

			}

		}

	});
});