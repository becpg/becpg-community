/*******************************************************************************
 * Copyright (C) 2010-2021 beCPG.
 * 
 * This file is part of beCPG
 * 
 * beCPG is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * beCPG is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with beCPG. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
(function() {

	/**
	 * EntityProcess constructor.
	 * 
	 * @param htmlId
	 *            {String} The HTML id of the parent element
	 * @return {beCPG.component.EntityProcess} The new EntityProcess
	 *         instance
	 * @constructor
	 */
	beCPG.component.EntityProcess = function(htmlId) {

		beCPG.component.EntityProcess.superclass.constructor.call(this, "beCPG.component.EntityProcess", htmlId, [ "button", "container" ]);
		var me = this;
		
          
		var fnActionHandler = function EntityDataGrid_fnActionHandler(layer, args){ 
            var owner = YAHOO.Bubbling.getOwnerByTagName(args[1].el, "select");
            if (owner !== null) {
            	var fieldId  = owner.id;
            	var value = args[1].value;
            	me.onChangeProcessFilter(fieldId, value);
            }
            

            YAHOO.util.Event.preventDefault(args[0]);
            return false;
        };
        
        YAHOO.Bubbling.addDefaultAction("process-list-filter-action", fnActionHandler);

		return this;
	};

	/**
	 * Extend from Alfresco.component.Base
	 */
	YAHOO.extend(beCPG.component.EntityProcess, Alfresco.component.Base);

	/**
	 * Augment prototype with main class implementation, ensuring overwrite is
	 * enabled
	 */
	YAHOO.lang.augmentObject(beCPG.component.EntityProcess.prototype, {
		/**
		 * Object container for initialization options
		 * 
		 * @property options
		 * @type object
		 */
		options : {
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
		 * Filter Result based on clicked element 
		 * Fired by YUI when element is clicked
		 * 
		 * @property fieldId 
		 * @type string
		 * 
		 */
	     onChangeProcessFilter : function(fieldId, value){
	    	var FILTER_ALL = "all",
	    	processFilter  = Dom.getElementsByClassName(fieldId)[0],
	    	valueFilter = processFilter.options[processFilter.selectedIndex].value,
	    	className = value + "-" + valueFilter;
	    	
	    	if(valueFilter == FILTER_ALL){
	    		className = value;
	    	} else if (value == FILTER_ALL){
	    		className = valueFilter;
	    	}
	    	
	    	var filteredDivEls = Dom.getElementsByClassName("process-element");
	    	
	    	for(var i in  filteredDivEls){
	    		Dom.removeClass(filteredDivEls[i], "hidden");
	    		if(!Dom.hasClass(filteredDivEls[i], className) && className != FILTER_ALL){
	    			Dom.addClass(filteredDivEls[i], "hidden");
	    		}
	    	}
	   
	    }
	    

	});

})();
