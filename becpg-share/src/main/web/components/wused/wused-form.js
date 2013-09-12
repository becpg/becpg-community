(function() {
   /**
    * YUI Library aliases
    */
   var Dom = YAHOO.util.Dom;
   /**
    * WUsedForm constructor.
    * 
    * @param htmlId
    *            {String} The HTML id of the parent element
    * @return {beCPG.component.WUsedForm} The new WUsedForm instance
    * @constructor
    */
   beCPG.component.WUsedForm = function(htmlId) {

      beCPG.component.WUsedForm.superclass.constructor.call(this, "beCPG.component.WUsedForm", htmlId, [ "button",
            "container" ]);

      return this;
   };

   /**
    * Extend from Alfresco.component.Base
    */
   YAHOO.extend(beCPG.component.WUsedForm, Alfresco.component.Base);

   /**
    * Augment prototype with main class implementation, ensuring overwrite is enabled
    */
   YAHOO.lang.augmentObject(beCPG.component.WUsedForm.prototype, {
      /**
       * Object container for initialization options
       * 
       * @property options
       * @type object
       */
      options : {
         type : null,
         itemType : null,
         assocType : null,
         nodeRefs : null
      },

      /**
       * Fired by YUI when parent element is available for scripting.
       * 
       * @method onReady
       */
      onReady : function WUsedForm_onReady() {

         var me = this;

         this.widgets.typeSelect = Alfresco.util.createYUIButton(this, "itemTypeSelect-button", this.onTypeSelect, {
            type : "menu",
            menu : "itemTypeSelect-menu",
            lazyloadmenu : false
         });

         this.widgets.typeSelect.getMenu().subscribe("click", function(p_sType, p_aArgs) {
            var menuItem = p_aArgs[1];
            if (menuItem) {
               me.widgets.typeSelect.set("label", menuItem.cfg.getProperty("text"));
            }
         });

         var dt = Alfresco.util.ComponentManager.find({
            name : "beCPG.module.EntityDataGrid"
         })[0], oldFunc = dt.onDatalistColumns;

         var onShow = function() {

            dt.options.entityNodeRef = me._getNodeRefs();

            dt.onDatalistColumns = function(response) {
               var rename = true, columnId = "assoc_" + me.options.assocType.replace(":", "_");
               if (response.json.columns.length < 1) {
                  response.json.columns.push({
                     "type" : "association",
                     "name" : me.options.assocType,
                     "formsName" : columnId,
                     "label" : me.msg("column.wused"),
                     "dataType" : me.options.itemType

                  });
                  rename = false;
               }

               oldFunc.call(this, response);

               if (rename) {
                  YAHOO.Bubbling.fire("columnRenamed", {
                     columnId : columnId,
                     label : me.msg("column.wused")
                  });
               }
            };

            YAHOO.Bubbling.fire("registerDataGridRenderer", {
               propertyName : [ me.options.itemType + "_" + me.options.assocType ],
               renderer : function(oRecord, data, label, scope) {
                  var url = beCPG.util.entityCharactURL(data.siteId, data.value);

                  return '<span class="' + data.metadata + '" ><a href="' + url + '">' + Alfresco.util
                        .encodeHTML(data.displayValue) + '</a></span>';
               }

            });

            YAHOO.Bubbling.fire("activeDataListChanged", {
               dataList : {
                  name : "WUsed|" + me.options.assocType,
                  itemType : me.options.itemType
               }
            });

         };

         this.widgets.showButton = Alfresco.util.createYUIButton(this, "show-button", onShow, {
            disabled : false
         });

         this.widgets.entitiesPicker = new beCPG.component.AutoCompletePicker(this.id + '-entities',
               this.id + '-entities-field', true).setOptions({
            mode : "edit",
            currentValue : this.options.nodeRefs,
            multipleSelectMode : true,
            dsStr : "/becpg/autocomplete/targetassoc/associations/" + this.options.type
         });

         // select first

         var items = this.widgets.typeSelect.getMenu().getItems();

         for ( var i in items) {
            var typeSelected = items[i];
            if (typeSelected) {
               me.widgets.typeSelect.set("label", typeSelected.cfg.getProperty("text"));
               var className = typeSelected._oAnchor.children[0].attributes[0].nodeValue;
               this._extractValues(className);
               if (className.contains("selected")) {
                  onShow();
                  break;
               }
            }
         }

      },

      onTypeSelect : function WUsedForm_onItemTypeSelect(sType, aArgs, p_obj) {
         var eventTarget = aArgs[1];

         var className = Alfresco.util.findEventClass(eventTarget);
         this._extractValues(className);
      },

      _extractValues : function WUsedForm__extractValues(className) {
         this.options.itemType = className.split("#")[0];
         this.options.assocType = className.split("#")[1];
      },
      _getNodeRefs : function WUsedForm__getNodeRefs() {
         return this.widgets.entitiesPicker.getValues();
      }

   }, true);

})();
