
/**
 * AutoCompletePicker component.
 * 
 * @namespace beCPG
 * @class beCPG.component.AutoCompletePicker
 */
(function()
{
   /**
    * YUI Library aliases
    */
   var Dom = YAHOO.util.Dom,
      Event = YAHOO.util.Event,
      KeyListener = YAHOO.util.KeyListener;

   /**
    * Alfresco Slingshot aliases
    */
   var $html = Alfresco.util.encodeHTML,
      $hasEventInterest = Alfresco.util.hasEventInterest,
      $combine = Alfresco.util.combinePaths;
   
   /**
    * AutoCompletePicker constructor.
    * 
    * @param {String} htmlId The HTML id of the parent element
    * @param {String} currentValueHtmlId The HTML id of the parent element
    * @return {beCPG.component.AutoCompletePicker} The new AutoCompletePicker instance
    * @constructor
    */
   beCPG.component.AutoCompletePicker = function(htmlId, currentValueHtmlId)
   {
      beCPG.component.AutoCompletePicker.superclass.constructor.call(this, "beCPG.component.AutoCompletePicker", htmlId, ["button", "menu", "container", "resize", "datasource", "datatable"]);
      this.currentValueHtmlId = currentValueHtmlId;

      /**
       * Decoupled event listeners
       */
      this.eventGroup = htmlId;
      YAHOO.Bubbling.on("renderCurrentValue", this.onRenderCurrentValue, this);
      
		this.currentValueMeta = [];

		this.options.objectRenderer = new Alfresco.ObjectRenderer(this);

      return this;
   };
   
   YAHOO.extend(beCPG.component.AutoCompletePicker, Alfresco.component.Base,
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
          * Instance of an ObjectRenderer class
          *
          * @property objectRenderer
          * @type object
          */
         objectRenderer: null,

         /**
          * The selected value to be displayed (but not yet persisted)
          *
          * @property selectedValue
          * @type string
          * @default null
          */
         selectedValue: null,

         /**
          * The current value
          *
          * @property currentValue
          * @type string
          */
         currentValue: "",
         
         /**
          * The mode (view/edit)
          *
          * @property mode
          * @type string
          */
         mode: "view",

			/**
          * Multiple Select mode flag
          * 
          * @property multipleSelectMode
          * @type boolean
          * @default false
          */
         multipleSelectMode: true,
			
			/**
          * Template string or function to use for link to target nodes, must
          * be supplied when showLinkToTarget property is
          * set to true
          *
          * @property targetLinkTemplate If of type string it will be used as a template, if of type function an
          * item object will be passed as argument and link is expected to be returned by the function
          * @type (string|function)
          */
         targetLinkTemplate: null
    	},     
         

      /**
       * Set multiple initialization options at once.
       *
       * @override
       * @method setOptions
       * @param obj {object} Object literal specifying a set of options
       * @return {beCPG.component.AutoCompletePicker} returns 'this' for method chaining
       */
      setOptions: function AutoCompletePicker_setOptions(obj)
      {
			this.options = YAHOO.lang.merge(this.options, obj);

         return this;
      },

      /**
       * Fired by YUI when parent element is available for scripting.
       * Component initialisation, including instantiation of YUI widgets and event listener binding.
       *
       * @method onReady
       */
      onReady: function AutoCompletePicker_onReady()
      {
         this._loadSelectedItems();
                           
      },

		/**
       * BUBBLING LIBRARY EVENT HANDLERS FOR PAGE EVENTS
       * Disconnected event handlers for inter-component event notification
       */

      /**
       * Renders current value in reponse to an event
       *
       * @method onRenderCurrentValue
       * @param layer {object} Event fired (unused)
       * @param args {array} Event parameters
       */
      onRenderCurrentValue: function ObjectFinder_onRenderCurrentValue(layer, args)
      {
         // Check the event is directed towards this instance
    	  if ($hasEventInterest(this, args))
    	  {
    		  var items = this.selectedItems,
               	displayValue = "", link;

    		  if (items === null)
    		  {
    			  displayValue = "<span class=\"error\">" + this.msg("form.control.object-picker.current.failure") + "</span>";            
    		  }
    		  else
    		  {                   			  
					// multiple selection					
					if(this.options.multipleSelectMode)
					{
						if(this.options.mode == "view")
						{
							for (var key in items)
				 			{
								item = items[key];
								if(displayValue != "")
								{
									displayValue += ", ";
								}

								link = null;
                        if (YAHOO.lang.isFunction(this.options.targetLinkTemplate))
                        {
                           link = this.options.targetLinkTemplate.call(this, item);
                        }
                        else if (YAHOO.lang.isString(this.options.targetLinkTemplate))
                        {
                           link = YAHOO.lang.substitute(this.options.targetLinkTemplate, item);
                        }

								displayValue += this.options.objectRenderer.renderItem(item, 16,
                                 "<div>{icon} <a href='" + link + "'>{name}</a></div>");
				 			}
							var htmlInput = Dom.get(this.currentValueHtmlId + "-values");
							htmlInput.innerHTML = displayValue;
						}
						else
						{
							for (var key in items)
				 			{
								item = items[key];
								if(displayValue != "")
								{
									displayValue += "<br/>";
								}
								displayValue += item.name							
				 			}
							var basketInput = Dom.get(this.currentValueHtmlId + "-cntrl-basket");				
							basketInput.innerHTML = displayValue;
						}						
					}
					else
					{
						
						
						if(this.options.mode == "view")
						{
							for (var key in items)
				 			{
								item = items[key];
								if(displayValue != "")
								{
									displayValue += ", ";
								}
								
								link = null;
                        if (YAHOO.lang.isFunction(this.options.targetLinkTemplate))
                        {
                           link = this.options.targetLinkTemplate.call(this, item);
                        }
                        else if (YAHOO.lang.isString(this.options.targetLinkTemplate))
                        {
                           link = YAHOO.lang.substitute(this.options.targetLinkTemplate, item);
                        }

								displayValue += this.options.objectRenderer.renderItem(item, 16,
                                 "<div>{icon} <a href='" + link + "'>{name}</a></div>");
								
				 			}

							var htmlInput = Dom.get(this.currentValueHtmlId + "-values");
							htmlInput.innerHTML = displayValue;
						}
						else
						{
							for (var key in items)
				 			{
								item = items[key];
								if(displayValue != "")
								{
									displayValue += "<br/>";
								}
								displayValue += item.name							
				 			}
							
							var htmlInput = Dom.get(this.currentValueHtmlId);
							htmlInput.value = displayValue;
						}
					}
										
    		  }
         }
      },		

		/**
       * PRIVATE FUNCTIONS
       */

      /**
       * Gets selected or current value's metadata from the repository
       *
       * @method _loadSelectedItems
       * @private
       */
      _loadSelectedItems: function AutoCompletePicker__loadSelectedItems(useOptions)
      {
         var arrItems = "";
         if (this.options.selectedValue)
         {
            arrItems = this.options.selectedValue;
         }
         else
         {
            arrItems = this.options.currentValue;
         }

         var onSuccess = function AutoCompletePicker__loadSelectedItems_onSuccess(response)
         {
            var items = response.json.data.items,
               item;
            this.selectedItems = {};

            for (var i = 0, il = items.length; i < il; i++)
            {
               item = items[i];
               this.selectedItems[item.nodeRef] = item;
            }

            YAHOO.Bubbling.fire("renderCurrentValue",
            {
               eventGroup: this
            });
         };
         
         var onFailure = function AutoCompletePicker__loadSelectedItems_onFailure(response)
         {
            this.selectedItems = null;
         };

         if (arrItems != "")
         {
            Alfresco.util.Ajax.jsonRequest(
            {
               url: Alfresco.constants.PROXY_URI + "api/forms/picker/items",
               method: "POST",
               dataObj:
               {
                  items: arrItems.split(","),
                  itemValueType: this.options.valueType
               },
               successCallback:
               {
                  fn: onSuccess,
                  scope: this
               },
               failureCallback:
               {
                  fn: onFailure,
                  scope: this
               }
            });
         }
      }

   });
})();

/**
 * Helper function to clear the selection of the control
 * represented by the given id.
 * 
 * @method removeAutoCompleteSelection
 * @param fieldHtmlId The id of the autocomplete input field
 * @static
 */
beCPG.util.removeAutoCompleteSelection = function(autoCompleteFieldHtmlId)
{   
   var basketInput = YUIDom.get(autoCompleteFieldHtmlId + "-cntrl-basket");
	var inputOrig = YUIDom.get(autoCompleteFieldHtmlId + "-cntrl-orig");
	var inputRemoved = YUIDom.get(autoCompleteFieldHtmlId + "-cntrl-removed");

	basketInput.innerHTML = '';
	inputRemoved.value = inputOrig.value;
};