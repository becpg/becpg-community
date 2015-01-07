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
/**
 * Entity Data Lists: EntityDataListToolbar component. Displays a list of EntityDataListToolbar
 * 
 * @namespace beCPG
 * @class beCPG.component.EntityDataListToolbar
 */
(function() {
   /**
    * YUI Library aliases
    */
   var Dom = YAHOO.util.Dom, Bubbling = YAHOO.Bubbling;

   /**
    * EntityDataListToolbar constructor.
    * 
    * @param htmlId
    *            {String} The HTML id of the parent element
    * @return {beCPG.component.EntityDataListToolbar} The new EntityDataListToolbar instance
    * @constructor
    */
   beCPG.component.EntityDataListToolbar = function(htmlId) {

      beCPG.component.EntityDataListToolbar.superclass.constructor.call(this, "beCPG.component.EntityDataListToolbar",
            htmlId, [ "button", "container" ]);

      // Initialise prototype properties
      this.toolbarButtonActions = {};
      
      this.widgets.actionButtons = {};

      // Renderers
      Bubbling.on("registerToolbarButtonAction", this.onRegisterToolbarButtonAction, this);

      Bubbling.on("activeDataListChanged", this.onActiveDataListChanged, this);

      /* Deferred list population until DOM ready */
      this.deferredToolbarPopulation = new Alfresco.util.Deferred([ "onReady", "onActiveDataListChanged" ], {
         fn : this.populateToolbar,
         scope : this
      });

      return this;
   };

   /**
    * Extend from Alfresco.component.Base
    */
   YAHOO.extend(beCPG.component.EntityDataListToolbar, Alfresco.component.Base);

   /**
    * Augment prototype with main class implementation, ensuring overwrite is enabled
    */
   YAHOO.lang.augmentObject(beCPG.component.EntityDataListToolbar.prototype, {

      /**
       * Current entity
       */
      entity : null,
      /**
       * Object container for initialization options
       * 
       * @property options
       * @type object
       */
      options : {
         /**
          * Current siteId.
          * 
          * @property siteId
          * @type string
          * @default ""
          */
         siteId : "",

         /**
          * Current entityNodeRef.
          * 
          * @property entityNodeRef
          * @type string
          * @default ""
          */
         entityNodeRef : ""
      },

      /**
       * Register a toolbar button action via Bubbling event
       */
      onRegisterToolbarButtonAction : function EntityDataListToolbar_onRegisterToolbarButtonAction(layer, args) {
         var obj = args[1];
         if (obj && obj.actionName) {
            this.toolbarButtonActions[obj.actionName] = obj;
         }
      },

      /**
       * Append toolbar buttons
       */
      onActiveDataListChanged : function EntityDataListToolbar_onRegisterToolbarButtonAction(layer, args) {
         var obj = args[1];
         if ((obj !== null) && (obj.dataList !== null)) {
            this.datalistMeta = obj.dataList;
            this.entity = obj.entity;

            if (obj.list !== null && (!this.options.list || this.options.list === null || this.options.list.length < 1)) {
               this.options.list = obj.list;
            }

            // Could happen more than once, so check return value of
            // fulfil()
            if (!this.deferredToolbarPopulation.fulfil("onActiveDataListChanged")) {
               this.populateToolbar();
            }
         }
      },

      populateToolbar : function EntityDataGrid_populateDataGrid() {

         //

         if (!YAHOO.lang.isObject(this.datalistMeta)) {
            return;
         }
         var containerRight = Dom.get(this.id + "-toolbar-buttons-right"), containerLeft = Dom
               .get(this.id + "-toolbar-buttons-left"), template = Dom.get(this.id + "-toolBar-template-button");

         for (var actionName in this.toolbarButtonActions) {
            var action = this.toolbarButtonActions[actionName];
            if (this.widgets.actionButtons[actionName] == null 
                  && ( action.evaluate === null || action.evaluate(this.datalistMeta, this.entity))) {
               
               if(action.createWidget){
                  if (action.right !== null && action.right === true) {
                     this.widgets.actionButtons[actionName] = action.createWidget(containerRight, this); 
                  } else {
                     this.widgets.actionButtons[actionName] = action.createWidget(containerLeft, this); 
                  }
                 
               } else {
                  
                  var templateInstance = template.cloneNode(true);
                  
                  Dom.setAttribute(templateInstance,"id", this.id + "-" + actionName + "ContainerDiv");

                  Dom.addClass(templateInstance, actionName);

                  if (action.right !== null && action.right === true) {
                     containerRight.appendChild(templateInstance);
                  } else {
                     containerLeft.appendChild(templateInstance);
                  }

                  var spanEl = Dom.getFirstChild(templateInstance);
                  
               
                  Dom.setAttribute(spanEl, "id", this.id + "-" + actionName + "Button");
   
                  this.widgets.actionButtons[actionName] = Alfresco.util.createYUIButton(this, actionName + "Button",
                        action.fn);
   
                  this.widgets.actionButtons[actionName].set("label", this.msg("button." + actionName));
                  this.widgets.actionButtons[actionName].set("title", this.msg("button." + actionName + ".description"));
                  

                  Dom.removeClass(templateInstance, "hidden");
               }

            }
         }
      },

      /**
       * Fired by YUI when parent element is available for scripting.
       * 
       * @method onReady
       */
      onReady : function EntityDataListToolbar_onReady() {

         this.deferredToolbarPopulation.fulfil("onReady");

        
         // Finally show the component body here to prevent UI artifacts on YUI
         // button decoration
         Dom.setStyle(this.id + "-body", "visibility", "visible");
      }

   }, true);
})();
