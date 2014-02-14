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