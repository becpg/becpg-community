/**
 * Copyright (C) 2005-2010 Alfresco Software Limited.
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
 * TaskListToolbar component.
 *
 * @namespace Alfresco
 * @class Alfresco.component.TaskListToolbar
 */
(function()
{
   /**
    * YUI Library aliases
    */
   var Dom = YAHOO.util.Dom,
      Selector = YAHOO.util.Selector,
      Event = YAHOO.util.Event;

   /**
    * TaskListToolbar constructor.
    *
    * @param {String} htmlId The HTML id of the parent element
    * @return {Alfresco.component.TaskListToolbar} The new TaskListToolbar instance
    * @constructor
    */
   Alfresco.component.TaskListToolbar = function TDH_constructor(htmlId)
   {
      Alfresco.component.TaskListToolbar.superclass.constructor.call(this, "Alfresco.component.TaskListToolbar", htmlId, ["button"]);
      return this;
   };

   YAHOO.extend(Alfresco.component.TaskListToolbar, Alfresco.component.Base,
   {
      /**
       * Fired by YUI when parent element is available for scripting.
       * Component initialisation, including instantiation of YUI widgets and event listener binding.
       *
       * @method onReady
       */
      onReady: function WLT_onReady()
      {
          this.configureSearch();
          this.widgets.searchButton = Alfresco.util.createYUIButton(this, "search-button", this.onSearchButtonClick, {additionalClass: "alf-primary-button"});
    	  this.widgets.startWorkflowButton = Alfresco.util.createYUIButton(this, "startWorkflow-button", this.onStartWorkflowButtonClick);
         Dom.removeClass(Selector.query(".hidden", this.id + "-body", true), "hidden");
      },

      /**
       * Start workflow button click handler
       *
       * @method onNewFolder
       * @param e {object} DomEvent
       * @param p_obj {object} Object passed back from addListener method
       */
      onStartWorkflowButtonClick: function WLT_onNewFolder(e, p_obj)
      {
         document.location.href = Alfresco.util.siteURL("start-workflow?referrer=tasks&myTasksLinkBack=true");
      },
      

      /**
       * Search Handlers
       */

      /**
       * Configure search area
       * 
       * @method configureSearch
       */
      configureSearch : function WLT_configureSearch() {
         this.widgets.searchBox = Dom.get(this.id + "-searchText");
         this.defaultSearchText = this.msg("header.search.default");

         Event.addListener(this.widgets.searchBox, "focus", this.onSearchFocus, null, this);
         Event.addListener(this.widgets.searchBox, "blur", this.onSearchBlur, null, this);

         this.setDefaultSearchText();
         
         var me = this;
         
         this.widgets.searchEnterListener = new YAHOO.util.KeyListener(this.widgets.searchBox,
         {
            keys: YAHOO.util.KeyListener.KEY.ENTER
         }, 
         {
            fn: me.onSearchButtonClick,
            scope: this,
            correctScope: true
         }, "keydown").enable();

      },

      /**
       * Update image class when search box has focus.
       * 
       * @method onSearchFocus
       */
      onSearchFocus : function WLT_onSearchFocus() {
         if (this.widgets.searchBox.value == this.defaultSearchText) {
            Dom.removeClass(this.widgets.searchBox, "faded");
            this.widgets.searchBox.value = "";
         } else {
            this.widgets.searchBox.select();
         }
      },

      /**
       * Set default search text when box loses focus and is empty.
       * 
       * @method onSearchBlur
       */
      onSearchBlur : function WLT_onSearchBlur() {
         var searchText = YAHOO.lang.trim(this.widgets.searchBox.value);
         if (searchText.length === 0) {
            /**
             * Since the blur event occurs before the KeyListener gets the enter we give the enter listener
             * a chance of testing against "" instead of the help text.
             */
            YAHOO.lang.later(100, this, this.setDefaultSearchText, []);
         }
      },

      /**
       * Set default search text for search box.
       * 
       * @method setDefaultSearchText
       */
      setDefaultSearchText : function WLT_setDefaultSearchText() {
         Dom.addClass(this.widgets.searchBox, "faded");
         this.widgets.searchBox.value = this.defaultSearchText;
      },

      /**
       * Get current search text from search box.
       * 
       * @method getSearchText
       */
      getSearchText : function WLT_getSearchText() {

         var ret = YAHOO.lang.trim(this.widgets.searchBox.value);
         if (ret != this.defaultSearchText) {
            return ret;
         }
         return "";
      },
      

      _cleanSearchText : function WLT__cleanSearchText() {
         var searchText = this.getSearchText();
         if (searchText.indexOf("*") > 0 && searchText.replace(/\*/g, "").length < 3) {
            this.searchTerm = null;
         } else {
            this.searchTerm = searchText;
         }

      },
      


      /**
       * Will trigger a search
       * 
       * @method onSearchButtonClick
       */
      onSearchButtonClick : function WLT_onSearchButtonClick() {

         this._cleanSearchText();
        //this.setDefaultSearchText();
         
         YAHOO.Bubbling.fire("filterSearch",
                 {
                     filterOwner : this.id,
                     filterId : "search",
                     filterData : encodeURIComponent(this.searchTerm)
                 });
         
      }
   });

})();
