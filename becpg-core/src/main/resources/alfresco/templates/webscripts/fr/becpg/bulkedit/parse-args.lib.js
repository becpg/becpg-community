
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
      
      var fields =  [];
      // Extract fields (if given)
      
      if (args.fields){
    	 var  fieldsTmp = args.fields.split('$');
    	  
          for (count = 0; count < fieldsTmp.length; count++)
          {
             fields.push(fieldsTmp[count].replace("_", ":"));
          }
    	  
      }  else if (typeof json !== "undefined" && json.has("fields"))
      {
         // Convert the JSONArray object into a native JavaScript array
         var jsonFields = json.get("fields"),
            numFields = jsonFields.length();
         
         for (count = 0; count < numFields; count++)
         {
            fields.push(jsonFields.get(count).replaceFirst("_", ":"));
         }
      }

      

      var objRet =
      {
         itemType: (args.itemType !== null) ? args.itemType : null,
         nodeRef: (args.nodeRef !== null) ? args.nodeRef : null,
         filter: filter,
         fields : fields,
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
