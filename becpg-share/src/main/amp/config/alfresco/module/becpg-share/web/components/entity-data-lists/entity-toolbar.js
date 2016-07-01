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
 * Entity Data Lists: EntityDataListToolbar component. Displays a list of EntityDataListToolbar
 * 
 * @namespace beCPG
 * @class beCPG.component.EntityDataListToolbar
 */
(function() {
   /**
    * YUI Library aliases
    */
   var Dom = YAHOO.util.Dom, Bubbling = YAHOO.Bubbling,Event = YAHOO.util.Event;
   
   /**
    * Alfresco Slingshot aliases
    */
   var $siteURL = Alfresco.util.siteURL;


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
      
      // Decoupled event listeners
//      YAHOO.Bubbling.on("filesPermissionsUpdated", this.doRefresh, this);
//      YAHOO.Bubbling.on("metadataRefresh", this.doRefresh, this);
      
      if(Alfresco.doclib && Alfresco.doclib.Actions){
          YAHOO.Bubbling.on("registerAction", this.onRegisterAction, this);
      }
      
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
    * Augment prototype with Actions module
    */
   if(Alfresco.doclib && Alfresco.doclib.Actions){
       YAHOO.lang.augmentProto(beCPG.component.EntityDataListToolbar, Alfresco.doclib.Actions);
   }

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
      options:
      {
         /**
          * Reference to the current document
          *
          * @property nodeRef
          * @type string
          */
         nodeRef: null,
         
         /**
          * Entity nodeRef 
          */
         entityNodeRef : null,

         /**
          * Current siteId, if any.
          *
          * @property siteId
          * @type string
          */
         siteId: null,

         /**
          * ContainerId representing root container
          *
          * @property containerId
          * @type string
          * @default "documentLibrary"
          */
         containerId: "documentLibrary",

         /**
          * Valid inline edit mimetypes
          * Currently allowed are plain text, HTML and XML only
          *
          * @property inlineEditMimetypes
          * @type object
          */
         inlineEditMimetypes:
         {
            "text/plain": true,
            "text/html": true,
            "text/xml": true
         },

         /**
          * Root node
          *
          * @property rootNode
          * @type string
          */
         rootNode: "alfresco://company/home",

         /**
          * Replication URL Mapping details
          *
          * @property replicationUrlMapping
          * @type object
          */
         replicationUrlMapping: {},

         /**
          * JSON representation of document details
          *
          * @property documentDetails
          * @type object
          */
         documentDetails: null,

         /**
          * Whether the Repo Browser is in use or not
          *
          * @property repositoryBrowsing
          * @type boolean
          */
         repositoryBrowsing: true
      },

      /**
       * The data for the document
       *
       * @property recordData
       * @type object
       */
      recordData: null,

      /**
       * Metadata returned by doclist data webscript
       *
       * @property doclistMetadata
       * @type object
       * @default null
       */
      doclistMetadata: null,

      /**
       * Path of asset being viewed - used to scope some actions (e.g. copy to, move to)
       *
       * @property currentPath
       * @type string
       */
      currentPath: null,

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
         
         //Reset
         containerRight.innerHTML = "";
         containerLeft.innerHTML = "";
         this.widgets.actionButtons = {};

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
         
         if(this.options.nodeRef!=null){
         
            // Asset data
             this.recordData = this.options.documentDetails.item;
             this.doclistMetadata = this.options.documentDetails.metadata;
             this.currentPath = this.recordData.location.path;
    
             // Populate convenience property
             this.recordData.jsNode = new Alfresco.util.Node(this.recordData.node);
    
             // Retrieve the actionSet for this record
             var record = this.recordData,
                actions = record.actions,
                actionsEl = Dom.get(this.id + "-actionSet"),
                actionHTML = "";
    
             record.actionParams = {};
             for (var i = 0, ii = actions.length; i < ii; i++)
             {
                actionHTML += this.renderAction(actions[i], record).replace(/div/g,"li");
             }
    
             // Token replacement (actionUrls is re-used further down)
             var actionUrls = this.getActionUrls(record);
             actionsEl.innerHTML = YAHOO.lang.substitute(actionHTML, actionUrls);
    
             // DocLib Actions module
             this.modules.actions = new Alfresco.module.DoclibActions();
             
             this.widgets.actionsMenu = Alfresco.util.createYUIButton(this, "action-set-button",
                     this.onActionMenu,
                     {
                         type : "menu",
                         menu : "action-set-menu",
                         lazyloadmenu : false,
                         disabled : false
                     });
         }
        
         // Finally show the component body here to prevent UI artifacts on YUI
         // button decoration
         Dom.setStyle(this.id + "-body", "visibility", "visible");
      },
      
      onActionMenu :  function EntityDataGrid_onSelectedItems(sType, aArgs, p_obj)
      {
          
          var domEvent = aArgs[0], eventTarget = aArgs[1];

            
                if (typeof this[eventTarget.id] === "function")
                {
                   try
                   {
                      this[eventTarget.id].call(this, this.recordData, eventTarget.element);
                      Event.preventDefault(domEvent);
                   }
                   catch (e)
                   {
                     alert(e);
                   }
                }
             
             return true;
          
          
      },
      getAction: function dlA_getAction(record, owner, resolve)
      {
         var actionId = owner.className.split(" ")[0],
            action = Alfresco.util.findInArray(record.actions, actionId, "id") || {};

         if (resolve === false)
         {
            // Return action without resolved parameters
            return action;
         }
         else
         {
            // Resolve action's parameters before returning them
            action = Alfresco.util.deepCopy(action);
            var params = action.params || {};
            for (var key in params)
            {
               params[key] = YAHOO.lang.substitute(params[key], record, function getActionParams_substitute(p_key, p_value, p_meta)
               {
                  return Alfresco.util.findValueByDotNotation(record, p_key);
               });
            }
            return action;
         }
      },
      
      /**
       * Delete Asset confirmed.
       *
       * @override
       * @method _onActionDeleteConfirm
       * @param asset {object} Object literal representing file or folder to be actioned
       * @private
       */
      _onActionDeleteConfirm: function DocumentActions__onActionDeleteConfirm(asset)
      {
         var path = asset.location.path;
         
         // Update the path for My Files and Shared Files...
         if (Alfresco.constants.PAGECONTEXT == "mine" || Alfresco.constants.PAGECONTEXT == "shared")
         {
            // Get rid of the first "/"
            var tmpPath = path.substring(1); 
            if (Alfresco.constants.PAGECONTEXT == "mine")
            {
               tmpPath = tmpPath.substring(tmpPath.indexOf("/") + 1);
            }
            var slashIndex = tmpPath.indexOf("/");
            if (slashIndex != -1)
            {
               path = tmpPath.substring(slashIndex);
            }
            else
            {
               path = "";
            }
         }
         
         var fileName = asset.fileName,
            displayName = asset.displayName,
            nodeRef = new Alfresco.util.NodeRef(asset.nodeRef),
            callbackUrl = "",
            encodedPath = path.length > 1 ? "?path=" + encodeURIComponent(path) : "";

         // Work out the correct Document Library to return to...
         if (Alfresco.constants.PAGECONTEXT == "mine")
         {
            callbackUrl = "myfiles";
         }
         else if (Alfresco.constants.PAGECONTEXT == "shared")
         {
            callbackUrl = "sharedfiles";
         }
         else
         {
            callbackUrl = Alfresco.util.isValueSet(this.options.siteId) ? "documentlibrary" : "repository";
         }
           
         this.modules.actions.genericAction(
         {
            success:
            {
               activity:
               {
                  siteId: this.options.siteId,
                  activityType: "file-deleted",
                  page: "documentlibrary",
                  activityData:
                  {
                     fileName: fileName,
                     path: path,
                     nodeRef: nodeRef.toString()
                  }
               },
               callback:
               {
                  fn: function DocumentActions_oADC_success(data)
                  {
                     window.location = $siteURL(callbackUrl + encodedPath);
                  }
               }
            },
            failure:
            {
               message: this.msg("message.delete.failure", displayName)
            },
            webscript:
            {
               method: Alfresco.util.Ajax.DELETE,
               name: "file/node/{nodeRef}",
               params:
               {
                  nodeRef: nodeRef.uri
               }
            }
         });
      }

   }, true);
   
   
   
})();
