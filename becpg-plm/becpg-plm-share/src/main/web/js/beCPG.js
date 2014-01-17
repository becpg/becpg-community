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
}
