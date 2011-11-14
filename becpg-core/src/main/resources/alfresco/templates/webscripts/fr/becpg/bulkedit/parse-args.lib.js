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

var Common =
{
   /**
    * Cache for person objects
    */
   PeopleCache: {},

   /**
    * Gets / caches a person object
    *
    * @method getPerson
    * @param username {string} User name
    */
   getPerson: function Common_getPerson(username)
   {
      if (username == null || username == "")
      {
         return null;
      }

      if (typeof Common.PeopleCache[username] != "object")
      {
         var person = people.getPerson(username);
         if (person == null)
         {
            if (username == "System" || username.match("^System@") == "System@")
            {
               // special case for the System users
               person =
               {
                  properties:
                  {
                     userName: "System",
                     firstName: "System",
                     lastName: "User"
                  },
                  assocs: {}
               };
            }
            else
            {
               // missing person - may have been deleted from the database
               person =
               {
                  properties:
                  {
                     userName: username,
                     firstName: "",
                     lastName: ""
                  },
                  assocs: {}
               };
            }
         }
         Common.PeopleCache[username] =
         {
            userName: person.properties.userName,
            firstName: person.properties.firstName,
            lastName: person.properties.lastName,
            displayName: (person.properties.firstName + " " + person.properties.lastName).replace(/^\s+|\s+$/g, "")
         };
         if (person.assocs["cm:avatar"] != null)
         {
            Common.PeopleCache[username].avatar = person.assocs["cm:avatar"][0];
         }
      }
      return Common.PeopleCache[username];
   }
};

var ParseArgs =
{
   /**
    * Get and parse arguments
    *
    * @method getParsedArgs
    * @param containerType {string} Optional: Node Type of container to create if it doesn't exist, defaults to "cm:folder"
    * @return {array|null} Array containing the validated input parameters
    */
   getParsedArgs: function ParseArgs_getParsedArgs(containerType)
   {
     
      // Filter
      var filter = null;
      if (args.filter)
      {
         filter =
         {
            filterId: args.filter
         };
      }
      else if (typeof json !== "undefined" && json.has("filter"))
      {
         filter = jsonUtils.toObject(json.get("filter"));
         if (filter == null)
         {
            filter =
            {
               filterId: ""
            }
         }
      }

      var objRet =
      {
         itemType: (args.itemType !== null) ? args.itemType : null,
         nodeRef: (args.nodeRef !== null) ? args.nodeRef : null,
         filter: filter,
         searchParams : {
		      siteId: (args.site !== null) ? args.site : null,
		      containerId: (args.container !== null) ? args.container : null,
		      repo: (args.repo !== null) ? (args.repo == "true") : false,
		      term: (args.term !== null) ? args.term : null,
		      tag: (args.tag !== null) ? args.tag : null,
		      query: (args.query !== null) ? args.query : null,
		      sort: (args.sort !== null) ? args.sort : null		  
		   }
      };
      
      
      

      return objRet;
   }
};
