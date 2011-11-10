

/**
 * Data Lists: DesignerTree component.
 * 
 * Displays a list of DesignerTree
 * 
 * @namespace beCPG
 * @class beCPG.component.DesignerTree
 */
(function()
{
   /**
    * YUI Library aliases
    */
   var Dom = YAHOO.util.Dom,
      Event = YAHOO.util.Event,
      Selector = YAHOO.util.Selector,
      Bubbling = YAHOO.Bubbling;

   /**
    * Alfresco Slingshot aliases
    */
   var $html = Alfresco.util.encodeHTML,
      $combine = Alfresco.util.combinePaths;

   /**
    * DesignerTree constructor.
    * 
    * @param htmlId {String} The HTML id of the parent element
    * @return {beCPG.component.DesignerTree} The new DesignerTree instance
    * @constructor
    */
   beCPG.component.DesignerTree = function(htmlId)
   {
      beCPG.component.DesignerTree.superclass.constructor.call(this, "beCPG.component.DesignerTree", htmlId, ["button", "container"]);

  
      
      return this;
   };
   
   /**
    * Extend from Alfresco.component.Base
    */
   YAHOO.extend(beCPG.component.DesignerTree, Alfresco.component.Base,
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
           * Current modelNodeRef.
           * 
           * @property modelNodeRef
           * @type string
           * @default ""
           */
    	  modelNodeRef: ""

      },

      /**
     
      /**
       * Fired by YUI when parent element is available for scripting.
       *
       * @method onReady
       */
      onReady: function DesignerTree_onReady()
      {
         this.widgets.newModel = Alfresco.util.createYUIButton(this, "newModelButton", this.onNewModel,
         {
            disabled: true
         });
     
         
         this.widgets.modelSelect =  Alfresco.util.createYUIButton(this, "modelSelect-button", this.onTypeSelect,
                 {
                            type: "menu",
                            menu: "modelSelect-menu",
                            lazyloadmenu : false
                  });

                 
         this.widgets.modelSelect.getMenu().subscribe("click", function (p_sType, p_aArgs)
                         {
                              var menuItem = p_aArgs[1];
                              if (menuItem)
                              {
                                  me.widgets.typeSelect.set("label", menuItem.cfg.getProperty("text"));
                              }
                          });
                 
                 
         //select first
         var modelSelected =  this.widgets.modelSelect.getMenu().getItem(0);
         if(modelSelected){
              me.widgets.typeSelect.set("label", typeSelected.cfg.getProperty("text"));
           //	  this.options.itemType = typeSelected._oAnchor.children[0].attributes[0].nodeValue;
                	 
         }
         
         
         this.renderDesignerTree();
      },
      
    
      /**
       * Renders the Data Lists into the DOM
       *
       * @method renderDesignerTree
       */
      renderDesignerTree: function DesignerTree_renderDesignerTree()
      {
         var me = this;
         
         var tree = new YAHOO.widget.TreeView(this.id + "-tree",[ 
                                                                {
                                                                    type: "text",
                                                                    label: "List 0",
                                                                    expanded: true,
                                                                        children: [
                                                                                {
                                                                                    type: "text",
                                                                                    label: "List 0-0",
                                                                                    expanded: true,
                                                                                    children: [
                                                                                        "item 0-0-0",
                                                                                        {
                                                                                            type: "text",
                                                                                            target: "_new",
                                                                                            href: "www.elsewhere.com",
                                                                                            title: "go elsewhere",
                                                                                            label: "elsewhere"
                                                                                        }
                                                                                     ]
                                                                                }
                                                                        ]
                                                                },
                                                                {
                                                                    type: "text",
                                                                    label: "List 1",
                                                                    children: [
                                                                        {
                                                                            type: "text", 
                                                                            label: "List 1-0", 
                                                                            children: [
                                                                                {
                                                                                    type: "DateNode",
                                                                                    label: "02/01/2009",
                                                                                    "editable": true
                                                                                },
                                                                                {
                                                                                    type: "text",
                                                                                    label: "item <strong>1-1-0</strong>"
                                                                                }
                                                                            ]
                                                                        }
                                                                    ]}
                              
                                                            ]);
                                                            tree.render();
         
         
      },
       
      


      /**
       * YUI WIDGET EVENT HANDLERS
       * Handlers for standard events fired from YUI widgets, e.g. "click"
       */

      /**
       * New List button click handler
       *
       * @method onNewList
       * @param e {object} DomEvent
       * @param p_obj {object} Object passed back from addListener method
       */
      onNewModel: function DesignerTree_onNewModel(e, p_obj)
      {
        
      }
   });
})();