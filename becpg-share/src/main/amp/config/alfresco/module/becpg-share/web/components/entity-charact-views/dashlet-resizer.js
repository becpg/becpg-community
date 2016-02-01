/*******************************************************************************
 *  Copyright (C) 2010-2015 beCPG. 
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
(function() {


   var PREF = "fr.becpg.formulation.dashlet";
   
/**
    * FormulationView constructor.
    * 
    * @param htmlId
    *            {String} The HTML id of the parent element
    * @return {beCPG.component.FormulationView} The new FormulationView instance
    * @constructor
    */
   beCPG.widget.DashletResizer = function(htmlId, dashletId) {

      beCPG.widget.DashletResizer.superclass.constructor.call(this, "beCPG.widget.DashletResizer", htmlId, dashletId, [
            "button", "container" ]);

      this.id = htmlId;
      this.dashletId = dashletId;
      
      this.preferencesService = new Alfresco.service.Preferences();
      
      return this;
   };

   /**
    * Extend from Alfresco.component.Base
    */
   YAHOO.extend(beCPG.widget.DashletResizer, Alfresco.widget.DashletResizer);

   /**
    * Augment prototype with main class implementation, ensuring overwrite is enabled
    */
   YAHOO.lang.augmentObject(beCPG.widget.DashletResizer.prototype, {
	      /**
	       * Fired by YUI when parent element is available for scripting.
	       * Template initialisation, including instantiation of YUI widgets and event listener binding.
	       *
	       * @method onReady
	       */
	      onReady: function DashletResizer_onReady()
	      {
	         // Have permission to resize?
	    	 // beCPG always allowed to resize 
//	         if (!Alfresco.constants.DASHLET_RESIZE)
//	         {
//	            return;
//	         }

	         // Find dashlet div
	         this.dashlet = Selector.query("div.dashlet", Dom.get(this.id), true);
	         if (!this.dashlet)
	         {
	            return;
	         }
	         Dom.addClass(this.dashlet, "resizable");

	         // Find dashlet body div?
	         this.dashletBody = Selector.query("div.body", this.dashlet, true);
	         if (!this.dashletBody)
	         {
	            return;
	         }

	         // Difference in height between dashlet and dashletBody for resize events
	         var origHeight = Dom.getStyle(this.dashlet, "height");
	         if (origHeight == "auto")
	         {
	            origHeight = this.dashlet.offsetHeight - parseInt(Dom.getStyle(this.dashlet, "padding-bottom"), 10);
	         }
	         else
	         {
	            origHeight = parseInt(origHeight, 10);
	         }
	         this.heightDelta = origHeight - parseInt(Dom.getStyle(this.dashletBody, "height"), 10);

	         // Create and attach Vertical Resizer
	         this.widgets.resizer = new YAHOO.util.Resize(this.dashlet,
	         {
	            handles: ["b"],
	            minHeight: this.options.minDashletHeight,
	            maxHeight: this.options.maxDashletHeight
	         });

	         // During resize event handler
	         this.widgets.resizer.on("resize", function()
	         {
	            this.onResize();
	         }, this, true);

	         // End resize event handler
	         this.widgets.resizer.on("endResize", function(eventTarget)
	         {
	            this.onEndResize(eventTarget.height);
	         }, this, true);

	         // Clear the fixed-pixel width the dashlet has been given
	         Dom.setStyle(this.dashlet, "width", "");
	      },
      /**
       * Fired by end resize event listener.
       *
       * @method onResize
       * @param h Height - not used
       */
      onEndResize: function DashletResizer_onEndResize(h)
      {
         // Clear the fixed-pixel width the dashlet has been given
         Dom.setStyle(this.dashlet, "width", "");

         // Make any iFrames that were hidden at the start of the resize operation
         // visible again.
         for (var i = 0; i<this._hiddenOnResize.length; i++)
         {
            Dom.setStyle(this._hiddenOnResize[i], "visibility", "visible");
         }
         this._hiddenOnResize = {};
         
         var height = parseInt(Dom.getStyle(this.dashlet, "height"), 10) - this.heightDelta;
         
         
         this.preferencesService.set(PREF+"."+this.dashletId+".height", height, {
            successCallback :  function(){}});
        
         
         // Fire a Bubbling event to notify any listeners on the dashlet, e.g. to refresh maps
         YAHOO.Bubbling.fire("dashletResizeEnd", {
            eventGroup: this.dashletId,
            dashletId: this.dashletId,
            htmlId: this.id,
            height: height,
            heightDelta: this.heightDelta
         });
      }
    
   }, true);

   

})();