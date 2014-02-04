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
 * beCPG root namespace.
 * 
 * @namespace beCPG
 */
// Ensure beCPG root object exists
if (typeof beCPG == "undefined" || !beCPG)
{
   var beCPG = {};
}

/**
 * beCPG top-level component namespace.
 * 
 * @namespace beCPG
 * @class beCPG.component
 */
beCPG.component = beCPG.component || {};

/**
 * beCPG top-level module namespace.
 * 
 * @namespace beCPG
 * @class beCPG.module
 */
beCPG.module = beCPG.module || {};

/**
 * beCPG top-level widget namespace.
 * 
 * @namespace beCPG
 * @class beCPG.widget
 */
beCPG.widget = beCPG.widget || {};

/**
 * beCPG top-level dashlet namespace.
 * 
 * @namespace beCPG
 * @class beCPG.dashlet
 */
beCPG.dashlet = beCPG.dashlet || {};

/**
 * beCPG top-level util namespace.
 * 
 * @namespace beCPG
 * @class beCPG.util
 */
beCPG.util = beCPG.util || {};
/**
 * beCPG top-level util namespace.
 * 
 * @namespace beCPG
 * @class beCPG.custom
 */
beCPG.custom = beCPG.custom || {};

/**
 * Utility method for arrays
 */
beCPG.util.contains = function(array, obj) {
  if(!array || array === null ){
	  return false;
  }
  
	var i = array.length;

   while (i--) {
       if (array[i] === obj) {
           return true;
       }
   }
   return false;
};
