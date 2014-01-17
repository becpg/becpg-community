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
 * This extends the alfresco/header/AlfSitesMenu widget to asynchronously retrieve the page
 * data for each site so that they can be accessed immediately.
 * 
 * @module blogs/BlogsSitesMenu
 * @extends module:alfresco/header/AlfSitesMenu
 * @mixes module:alfresco/core/CoreXhr
 * @author Dave Draper
 */
define(["dojo/_base/declare",
        "alfresco/header/AlfSitesMenu",
        "alfresco/core/CoreXhr",
        "dojo/_base/lang",
        "dojo/_base/array",
        "dojo/aspect",
        "dijit/registry",
        "alfresco/menus/AlfMenuGroup",
        "alfresco/header/AlfMenuItem",
        "alfresco/header/AlfCascadingMenu",
        "dojo/dom-style",
        "dijit/popup"], 
        function(declare, AlfSitesMenu, AlfXhr, lang, array, aspect, registry, AlfMenuGroup, AlfMenuItem, AlfCascadingMenu, domStyle, popup) {
   
   return declare([AlfSitesMenu, AlfXhr], {
      
      /**
       * Adds an individual menu item.
       * 
       * @instance
       * @param {object} group The group to add the menu item to
       * @param {object} widget The menu item to add
       * @param {integer} index The index to add the menu item at.
       */
      _addMenuItem: function blogs_BlogSitesMenu___addMenuItem(group, widget, index) {
         if (group == this.recentGroup)
         {
            // Create a basic group for holding the favourites...
             var sitePageList = new AlfMenuGroup({
               widgets: [{
                  name: "alfresco/header/AlfMenuItem",
                  config: {
                     label: "Loading..."
                  }
               }]
            });
            
            // Create the cascading menu item to popout the favourites list...
            var siteCascade = new AlfCascadingMenu(widget.config);
            
            // Set up the sites cascading popup to asynchronously load the pages upon request...
            siteCascade.popup.onOpen = dojo.hitch(this, "loadSitePages", widget.config.siteShortName, sitePageList);
            
            // Add the list into the cascading menu...
            siteCascade.popup.addChild(sitePageList);

            // Add the default menu items...
            group.addChild(siteCascade);
         }
         else
         {
            // If we're not adding a Recent Sites menu item then just default to the normal action
            this.inherited(arguments);
         }
      },
      
      /**
       * This variable will be used to keep track of which sites pages have been loaded. It is initialised
       * to null and populated as page data is loaded.
       * 
       * @instance
       * @type {object}
       * @default null
       */
      _sitePagesLoaded: null,
      
      /**
       * This function is hitched to the each sites cascading menu so that when it is clicked a XHR request is made
       * to retrieve the pages for the site.
       * 
       * @instance
       * @param {string} siteShortName The short name of the site to load the pages for
       * @param {object} sitePageList A reference to the alfresco/menus/AlfMenuGroup widget that the site pages should be added to
       */
      loadSitePages: function blogs_BlogSitesMenu__loadSitePages(siteShortName, sitePageList) {
         if (this._sitePagesLoaded != null && this._sitePagesLoaded[siteShortName] == true)
         {
            this.alfLog("log", "Site pages already loaded for: " + siteShortName);
         }
         else
         {
            this.alfLog("log", "Loading pages for site: " + siteShortName);
            this.serviceXhr({url : Alfresco.constants.URL_SERVICECONTEXT + "blogs/site/" + siteShortName,
                             method: "GET",
                             siteShortName: siteShortName, // Including the site short name will make it available in the "sitePagesLoaded" callback "originalRequestConfig"
                             sitePageList: sitePageList,   // ...as will the sitePageList menu group
                             successCallback: this.sitePagesLoaded,
                             callbackScope: this});
         }
      },
      
      /**
       * This function is "hitched" from the serviceXhr call in the "loadSitePages" function and handles the response
       * from the asynchronous request to get site pages. It clears the original "Loading..." menu item and adds in
       * each of the site page links.
       * 
       * @instance
       * @param {object} response The response from the request
       * @param {object} originalRequestConfig The configuration passed on the original request
       */
      sitePagesLoaded: function blogs_BlogSitesMenu__loadSitePages(response, originalRequestConfig) {
         this.alfLog("log", "Site pages data loaded successfully", response);
         
         // Initialise the object that keeps track of which pages have been loaded if it has not
         // previously been initialised...
         if (this._sitePagesLoaded == null)
         {
            this._sitePagesLoaded = {};
         }
         
         // Record that the site pages have been loaded to prevent them from being loaded again...
         this._sitePagesLoaded[originalRequestConfig.siteShortName] = true;
         
         // Check for keyboard access by seeing if the first child is focused...
         var focusFirstChild = (originalRequestConfig.sitePageList && originalRequestConfig.sitePageList.getChildren().length > 0 && originalRequestConfig.sitePageList.getChildren()[0].focused);
         
         // Remove the loading item...
         array.forEach(originalRequestConfig.sitePageList.getChildren(), function(widget, index) {
            originalRequestConfig.sitePageList.removeChild(widget);
         });
         
         // Add the site pages...
         if (response.sitePages && response.sitePages.length > 0)
         {
            array.forEach(response.sitePages, function(sitePage, index) {
               this.alfLog("log", "Adding site page menu item", sitePage);
               var item = new AlfMenuItem(sitePage);
               originalRequestConfig.sitePageList.addChild(item);
            }, this);
         }
         else
         {
            // TODO: Should add some error handling here - but has been left out as Example for Blog post only covers "golden path"
         }
         
         if (focusFirstChild)
         {
            // Focus the first site page...
            originalRequestConfig.sitePageList.focusFirstChild();
         }
      }
   });
});