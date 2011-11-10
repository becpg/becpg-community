/**
 * BulkEdit component GET method
 */

const SITES_SPACE_QNAME_PATH = "/app:company_home/st:sites/";
const DISCUSSION_QNAMEPATH = "/fm:discussion";
const COMMENT_QNAMEPATH = DISCUSSION_QNAMEPATH + "/cm:Comments/";


/**
 * Helper to escape the QName string so it is valid inside an alfresco query.
 * The language supports the SQL92 identifier standard.
 * 
 * @param qname   The QName string to escape
 * @return escaped string
 */
function escapeQName(qname)
{
   var separator = qname.indexOf(':'),
       namespace = qname.substring(0, separator),
       localname = qname.substring(separator + 1);

   return escapeString(namespace) + '\\:' + escapeString(localname);
}

function escapeString(value)
{
   var result = "";

   for (var i=0,c; i<value.length; i++)
   {
      c = value.charAt(i);
      if (i == 0)
      {
         if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_'))
         {
            result += '\\';
         }
      }
      else
      {
         if (!((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || c == '_' || c == '$' || c == '#'))
         {
            result += '\\';
         }
      }
      result += c;
   }
   return result;
}


// Compute search query
function getSearchQuery(params)
{
   var ftsQuery = "",
      term = params.term,
      tag = params.tag,
      nodeRef = params.nodeRef,
      formData = params.query;
   
   // Simple keyword search and tag specific search
   if (term !== null && term.length !== 0)
   {
      // TAG is now part of the default macro
      ftsQuery = term + " ";
   }
   else if (tag !== null && tag.length !== 0)
   {
      // Just look for tag
      ftsQuery = "TAG:" + tag +" ";
   }
  
   
   // Advanced search form data search.
   // Supplied as json in the standard Alfresco Forms data structure:
   //    prop_<name>:value|assoc_<name>:value
   //    name = namespace_propertyname|pseudopropertyname
   //    value = string value - comma separated for multi-value, no escaping yet!
   // - underscore represents colon character in name
   // - pseudo property is one of any cm:content url property: mimetype|encoding|size
   // - always string values - interogate DD for type data
   if (formData !== null && formData.length !== 0)
   {
      var formQuery = "",
          formJson = jsonUtils.toObject(formData);
      
      // extract form data and generate search query
      var first = true;
      var useSubCats = false;
      for (var p in formJson)
      {
         // retrieve value and check there is someting to search for
         // currently all values are returned as strings
         var propValue = formJson[p];
         if (propValue.length !== 0)
         {
            if (p.indexOf("prop_") === 0)
            {
               // found a property - is it namespace_propertyname or pseudo property format?
               var propName = p.substr(5);
               if (propName.indexOf("_") !== -1)
               {
                  // property name - convert to DD property name format
                  propName = propName.replace("_", ":");
                  
                  // special case for range packed properties
                  if (propName.match("-range$") == "-range")
                  {
                     // currently support text based ranges (usually numbers) or date ranges
                     // range value is packed with a | character separator
                     
                     // if neither value is specified then there is no need to add the term
                     if (propValue.length > 1)
                     {
                        var from, to, sepindex = propValue.indexOf("|");
                        if (propName.match("-date-range$") == "-date-range")
                        {
                           // date range found
                           propName = propName.substr(0, propName.length - "-date-range".length)
                           
                           // work out if "from" and/or "to" are specified - use MIN and MAX otherwise;
                           // we only want the "YYYY-MM-DD" part of the ISO date value - so crop the strings
                           from = (sepindex === 0 ? "MIN" : propValue.substr(0, 10));
                           to = (sepindex === propValue.length - 1 ? "MAX" : propValue.substr(sepindex + 1, sepindex + 10));
                        }
                        else
                        {
                           // simple range found
                           propName = propName.substr(0, propName.length - "-range".length);
                           
                           // work out if "min" and/or "max" are specified - use MIN and MAX otherwise
                           from = (sepindex === 0 ? "MIN" : propValue.substr(0, sepindex));
                           to = (sepindex === propValue.length - 1 ? "MAX" : propValue.substr(sepindex + 1));
                        }
                        formQuery += (first ? '' : ' AND ') + escapeQName(propName) + ':"' + from + '".."' + to + '"';
                     }
                  }
                  else if (propName.indexOf("cm:categories") != -1) 
                  {
                     // determines if the checkbox use sub categories was clicked
                     if (propName.indexOf("usesubcats") == -1)
                     {
                        if (formJson["prop_cm_categories_usesubcats"] == "true")
                        {
                           useSubCats = true;
                        }
                     }
                     else 
                     { 
                        // ignore the 'usesubcats' property
                        continue; 
                     }

                     // build list of category terms to search for
                     var firstCat = true;
                     var catQuery = "";
                     var cats = propValue.split(',');
                     for (var i = 0; i < cats.length; i++) 
                     {
                        var cat = cats[i];
                        var catNode = search.findNode(cat);
                        if (catNode) 
                        {
                           catQuery += (firstCat ? '' : ' OR ') + "PATH:\"" + catNode.qnamePath + (useSubCats ? "//*\"" : "/member\"" );
                           firstCat = false;
                        }
                     }
                     
                     if (catQuery.length !== 0)
                     {
                        // surround category terms with brackets if appropriate
                        formQuery += (first ? '' : ' AND ') + "(" + catQuery + ")";
                     }
                     else
                     {
                        // ignore categories, continue loop so we don't set the 'first' flag
                        continue;
                     }
                  }
                  else
                  {
                     formQuery += (first ? '' : ' AND ') + escapeQName(propName) + ':"' + propValue + '"';
                  }
               }
               else
               {
                  // pseudo cm:content property - e.g. mimetype, size or encoding
                  formQuery += (first ? '' : ' AND ') + 'cm:content.' + propName + ':"' + propValue + '"';
               }
               first = false;
            }
         }
      }
      
      if (formQuery.length !== 0 || ftsQuery.length !== 0)
      {
         // extract data type for this search - advanced search query is type specific
         ftsQuery = 'TYPE:"' + formJson.datatype + '"' +
                    (formQuery.length !== 0 ? ' AND (' + formQuery + ')' : '') +
                    (ftsQuery.length !== 0 ? ' AND (' + ftsQuery + ')' : '');
      }
   }
   
   if (ftsQuery.length !== 0)
   {
      // we processed the search terms, so suffix the PATH query
      var path = null;
      if (!params.repo)
      {
         path = SITES_SPACE_QNAME_PATH;
         if (params.siteId !== null && params.siteId.length > 0)
         {
            path += "cm:" + search.ISO9075Encode(params.siteId) + "/";
         }
         else
         {
            path += "*/";
         }
         if (params.containerId !== null && params.containerId.length > 0)
         {
            path += "cm:" + search.ISO9075Encode(params.containerId) + "/";
         }
         else
         {
            path += "*/";
         }
      }
      
      if (path != null)
      {
         ftsQuery = 'PATH:"' + path + '/*" AND (' + ftsQuery + ') ';
      }
      ftsQuery = '(' + ftsQuery + ') AND -TYPE:"cm:thumbnail"';
      
      // sort field - expecting field to in one of the following formats:
      //  - short QName form such as: cm:name
      //  - pseudo cm:content field starting with "." such as: .size
      //  - any other directly supported search field such as: TYPE
      var sortColumns = [];
      var sort = params.sort;
      if (sort != null && sort.length != 0)
      {
         var asc = true;
         var separator = sort.indexOf("|");
         if (separator != -1)
         {
            sort = sort.substring(0, separator);
            asc = (sort.substring(separator + 1) == "true");
         }
         var column;
         if (sort.charAt(0) == '.')
         {
            // handle pseudo cm:content fields
            column = "@{http://www.alfresco.org/model/content/1.0}content" + sort;
         }
         else if (sort.indexOf(":") != -1)
         {
            // handle attribute field sort
            column = "@" + utils.longQName(sort);
         }
         else
         {
            // other sort types e.g. TYPE
            column = sort;
         }
         sortColumns.push(
         {
            column: column,
            ascending: asc
         });
      }
   }
   if (logger.isLoggingEnabled())
     logger.log("Search query: " + ftsQuery);
   
   return ftsQuery;
}


function main()
{
	
   
   // get the bulk-edit types from the config
   var bulkEditables = config.scoped["bulk-edit"]["itemTypes"].childrenMap["itemType"];
   var itemTypes = [];
   for (var i = 0, itemType, label; i < bulkEditables.size(); i++)
   {
	   itemType = bulkEditables.get(i);
      
      // resolve label text
      label = itemType.attributes["label"];
      if (label == null)
      {
         label = "type."+itemType.attributes["name"].replace(":","_");
         if (label != null)
         {
            label = msg.get(label);
         }
      }
      
      // create the model object to represent the sort field definition
      itemTypes.push(
      {
         name: itemType.attributes["name"],
         label: label ? label : itemType.attributes["name"],
         formId : itemType.attributes["formId"] ? itemType.attributes["formId"] : "bulk-edit"
      });
   }
   
   
 var params =
   {
      siteId: (page.url.args["site"] !== null) ? page.url.args["site"] : null,
      containerId: (page.url.args["container"] !== null) ? page.url.args["container"] : null,
      repo: (page.url.args["repo"] !== null) ? (page.url.args["repo"] == "true") : false,
      term: (page.url.args["term"] !== null) ? page.url.args["term"] : null,
      tag: (page.url.args["tag"] !== null) ? page.url.args["tag"] : null,
      query: (page.url.args["query"] !== null) ? page.url.args["query"] : null,
      sort: (page.url.args["sort"] !== null) ? page.url.args["sort"] : null,
      nodeRef :  (page.url.args["nodeRef"] !== null) ? page.url.args["nodeRef"] : null
   };
   
 
 model.searchQuery = getSearchQuery(params); 
  
 model.nodeRef = params.nodeRef;
 
 model.itemTypes = itemTypes;
    
//  TODO filter type
// model.itemTypes = [];
//   
//   var connector = remote.connect("alfresco");
//   // Call the repo to search results
//   result = connector.get("/slingshot/search?term="+encodeURIComponent(model.searchQuery));
//   
//   model.result = result;
//   
//   
//   if (result.status == 200)
//   {
//      var nodes = eval('(' + result + ')');
//
//      for(var i in nodes){
//   	   for(var j in itemTypes){
//   		   if(nodes[i].typeShort == itemTypes[j].name){
//   			   model.itemTypes.push(itemTypes[j]);
//   			   break;
//   		   }
//   	   }   
//      }
//   }
//   
   
   
}



main();