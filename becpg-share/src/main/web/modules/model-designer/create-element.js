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
      $links = Alfresco.util.activateLinks,
      $combine = Alfresco.util.combinePaths,
      $userProfile = Alfresco.util.userProfileLink;

   
   /**
    * CreateDesignerElement constructor.
    * 
    * @param htmlId {String} The HTML id of the parent element
    * @return {beCPG.component.EntityDataGrid} The new EntityDataGrid instance
    * @constructor
    */
   beCPG.module.CreateDesignerElement = function(htmlId)
   {
      beCPG.module.CreateDesignerElement.superclass.constructor.call(this, "beCPG.module.CreateDesignerElement", htmlId, ["button", "container"]);

      return this;
   };

   /**
    * Extend from Alfresco.component.Base
    */
   YAHOO.extend(beCPG.module.CreateDesignerElement, Alfresco.component.Base);

   /**
    * Augment prototype with main class implementation, ensuring overwrite is enabled
    */
   YAHOO.lang.augmentObject(beCPG.module.CreateDesignerElement.prototype,
   {
      /**
       * Object container for initialization options
       *
       * @property options
       * @type object
       */
      options:
      {
    	  
      },
   
	   /**
	    * Fired by YUI when parent element is available for scripting
	    *
	    * @method onReady
	    */
	   onReady: function CreateDesignerElement_onReady()
	   {
	      var me = this;
	      
	      // Item Select menu button
	      this.initLinkedSelect(Dom.get(this.id+"-assocType"),Dom.get(this.id+"-type"),Dom.get(this.id+"-model"));
	      
			
		},
		
		initLinkedSelect : function(sel1,sel2,sel3 ) {
			
			var me = this;
	       
			var sel2Options = new Array();
			var sel3Options = new Array();
			for (var i=0; i < sel2.options.length; i++) {
				sel2Options[i] = new Array(sel2.options[i].text,sel2.options[i].value);
			}
			for (var i=0; i < sel3.options.length; i++) {
				sel3Options[i] = new Array(sel3.options[i].text,sel3.options[i].value);
			}
			sel2.options.length = 0;
			sel2.options[0] = new Option(sel2Options[0][0],sel2Options[0][1]);
			sel3.options.length = 0;
			sel3.options[0] = new Option(sel3Options[0][0],sel3Options[0][1]);
			
			Event.addListener(sel1, "change", function() {
				var fromCode = sel1.options[sel1.selectedIndex].value;
				sel2.options.length = 0;
				sel3.options.length = 0;
				sel3.options[0] = new Option(sel3Options[0][0],sel3Options[0][1]);
				for (i = 0; i < sel2Options.length; i++) {
					if (sel2Options[i][1].indexOf(fromCode) == 0 || sel2Options[i][1]=="-" ) {
						sel2.options[sel2.options.length] = new Option(sel2Options[i][0],sel2Options[i][1].split("-")[1]);
					}
				}
				sel2.options[0].selected = true;
				if(sel2.options.length>1){
					Dom.get(me.id+"-type-container").style.display = "block";
				} else {
					Dom.get(me.id+"-type-container").style.display = "none";
				}
			});
			
			Event.addListener(sel2, "change", function() {
				var fromCode = sel2.options[sel2.selectedIndex].value;
				sel3.options.length = 0;
				for (i = 0; i < sel3Options.length; i++) {
					if (sel3Options[i][1].indexOf(fromCode) == 0 || sel3Options[i][1]=="-") {
						sel3.options[sel3.options.length] = new Option(sel3Options[i][0],sel3Options[i][1].split("-")[2]);
					}
				}
				sel3.options[0].selected = true;
				if(sel3.options.length>1){
					Dom.get(me.id+"-model-container").style.display = "block";
				} else {
					Dom.get(me.id+"-model-container").style.display = "none";
				}
			});
			
			sel1.options[0].selected = true;
			if(sel1.options.length>1){
				Dom.get(me.id+"-assocType-container").style.display = "block";
			} else {
				Dom.get(me.id+"-assocType-container").style.display = "none";
			}
			
		}
   }

, true);
})();
