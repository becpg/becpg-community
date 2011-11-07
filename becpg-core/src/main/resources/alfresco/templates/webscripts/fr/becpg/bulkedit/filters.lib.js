/**
 * Copyright (C) 2005-2010 Alfresco Software Limited.
 *
 * This file is part of Alfresco
 *
 * Alfresco is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Alfresco is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Alfresco. If not, see <http://www.gnu.org/licenses/>.
 */

var Filters =
{
   /**
    * Create filter parameters based on input parameters
    *
    * @method getFilterParams
    * @param filter {string} Required filter
    * @param parsedArgs {object} Parsed arguments object literal
    * @return {object} Object literal containing parameters to be used in Lucene search
    */
   getFilterParams: function Filter_getFilterParams(filter, parsedArgs)
   {
	  
	   
	   var searchQuery = parsedArgs.searchQuery;
	   if(searchQuery==null || searchQuery.length <1){
		   searchQuery="";
	   }
	   
	   if(parsedArgs.itemType!=null){
		   searchQuery+=" +TYPE:\""+parsedArgs.itemType+"\"";
	   }
	   
	   // Common types and aspects to filter from the UI
	   searchQuery +=
	         " -TYPE:\"cm:systemfolder\"" +
	         " -@cm\\:lockType:READ_ONLY_LOCK" +
	         " -ASPECT:\"bcpg:compositeVersion\"";
	   
	  var nodeRef = parsedArgs.nodeRef;
	  if(nodeRef!=null && nodeRef.length >0){
		  var node = search.findNode(nodeRef);
		  if(node!=null){
			  searchQuery += " +PATH:\"" + node.qnamePath + "//*\"";
          }
	  }
	   
	   
      var filterParams =
      {
         query: searchQuery,
         limitResults: null,
         sort: [
         {
            column: "@cm:name",
            ascending: true
         }],
         language: "lucene",
         templates: null
      };

      // Max returned results specified?
      var argMax = args.max;
      if ((argMax !== null) && !isNaN(argMax))
      {
         filterParams.limitResults = argMax;
      }


      return filterParams;
   }
};
