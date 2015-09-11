(function()
{

	/**
	 * YUI Library aliases
	 */
	var Dom = YAHOO.util.Dom;

	/**
	 * Separator constructor.
	 *
	 * @return {beCPG.widget.separator} The designer separator instance
	 * @constructor
	 */
	beCPG.widget.separator = function Separator_constructor(p_name)
	{
	   // Load YUI Components
	   Alfresco.util.YUILoaderHelper.require(["resize"], this.onComponentsLoaded, this);

	   this.name = p_name;

	   // Initialise prototype properties
	   this.widgets = {};

	   return this;
	};

	beCPG.widget.separator.prototype =
	{
	  
	   /**
	    * Object container for storing YUI widget instances.
	    *
	    * @property widgets
	    * @type object
	    */
	   widgets: null,

	  
	   /**
	    * Fired by YUILoaderHelper when required component script files have
	    * been loaded into the browser.
	    *
	    * @method onComponentsLoaded
	    */
	   onComponentsLoaded: function Separator_onComponentsLoaded()
	   {
	      YAHOO.util.Event.onDOMReady(this.onReady, this, true);
	   },


		/**
		 * Fired by YUI when parent element is available for scripting.
		 * Template initialisation, including instantiation of YUI widgets and event listener binding.
		 *
		 * @method onReady
		 */
		onReady: function Separator_onReady() {
		  
			 this.onResize();
			
			// Recalculate the vertical size on a browser window resize event
		    YAHOO.util.Event.on(window, "resize", function(e)
		    {
		       this.onResize();
		    }, this, true);
		
		    // Monitor the document height for ajax updates
		    this.documentHeight = Dom.getXY("alf-ft")[1];
		
		    YAHOO.lang.later(1000, this, function()
		    {
		       var h = Dom.getXY("alf-ft")[1];
		       if (Math.abs(this.documentHeight - h) > 4)
		       {
		          this.documentHeight = h;
		          this.onResize();
		       }
		    }, null, true);
		  }
		 ,
	 
		 /**
		  * Fired by via resize event listener.
		  *
		  * @method onResize
		  */
		 onResize: function Separator_onResize()
		 {
		
		        var h = Dom.getXY("alf-ft")[1] - Dom.getXY("alf-hd")[1] - Dom.get("alf-hd").offsetHeight;
		
		        if (YAHOO.env.ua.ie === 6)
		        {
		           var hd = Dom.get("alf-hd"), tmpHeight = 0;
		           for (var i = 0, il = hd.childNodes.length; i < il; i++)
		           {
		              tmpHeight += hd.childNodes[i].offsetHeight;
		           }
		           h = Dom.get("alf-ft").parentNode.offsetTop - tmpHeight;
		        }
		        if (h < 200)
		        {
		           h = 200;
		        }
		        Dom.setStyle( this.name, "height", h + "px");
		 }
	};
	
	
})();
