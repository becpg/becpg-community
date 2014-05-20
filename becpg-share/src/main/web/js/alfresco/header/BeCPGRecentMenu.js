/**
 * Copyright (C) 2005-2013 Alfresco Software Limited.
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
 * @module alfresco/header/BeCPGRecentMenu
 * @extends module:alfresco/menus/AlfMenuBarPopup
 * @mixes module:alfresco/core/CoreXhr
 * @author Dave Draper
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
				url += "?entityNodeRef=" + nodeRef.replace(/\\/g, "");
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
					
					var targetUrl = "entity-details?nodeRef=" + item.nodeRef;
					if (this.pageUri.indexOf("entity-data-lists")) {
						targetUrl = "entity-data-lists?nodeRef=" + item.nodeRef;
						// TODO Should be split or better
						// page.url.args.list
						if (item.itemType == "bcpg:finishedProduct" || item.itemType == "bcpg:semiFinishedProduct") {
							targetUrl += "&list=compoList";
						} else if (item.itemType == "bcpg:packagingKit") {
							targetUrl += "&list=packagingList";
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