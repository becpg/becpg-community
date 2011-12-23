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

const SITES_SPACE_QNAME_PATH = "/app:company_home/st:sites/";

var Evaluator =
{
   /**
    * Cache for cm:person objects
    */
   PeopleObjectCache: {},

   /**
    * Gets / caches a person object
    *
    * @method getPersonObject
    * @param nodeRef {string} NodeRef of a cm:person object
    */
   getPersonObject: function Evaluator_getPersonObject(nodeRef)
   {
      if (nodeRef == null || nodeRef == "")
      {
         return null;
      }

      if (typeof Evaluator.PeopleObjectCache[nodeRef] == "undefined")
      {
         var person = search.findNode(nodeRef);
         Evaluator.PeopleObjectCache[nodeRef] =
         {
            userName: person.properties.userName,
            firstName: person.properties.firstName,
            lastName: person.properties.lastName,
            displayName: (person.properties.firstName + " " + person.properties.lastName).replace(/^\s+|\s+$/g, "")
         };
         if (person.assocs["cm:avatar"] != null)
         {
            Evaluator.PeopleObjectCache[nodeRef].avatar = person.assocs["cm:avatar"][0];
         }
      }
      return Evaluator.PeopleObjectCache[nodeRef];
   },

   /**
    * Cache for nodes that are subtypes of cm:cmobject
    */
   ContentObjectCache: {},

   /**
    * Gets / caches a content object
    *
    * @method getContentObject
    * @param nodeRef {string} NodeRef
    */
   getContentObject: function Evaluator_getContentObject(nodeRef)
   {
      if (nodeRef == null || nodeRef == "")
      {
         return null;
      }

      if (typeof Evaluator.ContentObjectCache[nodeRef] == "undefined")
      {
         var node = search.findNode(nodeRef);
         try
         {
            Evaluator.ContentObjectCache[nodeRef] = node;
         }
         catch(e)
         {
            // Possibly a stale indexed node
            return null;
         }
      }
      return Evaluator.ContentObjectCache[nodeRef];
   },

   /**
    * Generate displayValue and any extra metadata for this field
    *
    * @method decorateFieldData
    * @param objData {object} Object literal containing this field's data
    * @param node {ScriptNode} The list item node for this field
    * @return {Boolean} false to prevent this field being added to the output stream.
    */
   decorateFieldData: function Evaluator_decorateFieldData(objData, node)
   {
      var value = objData.value,
         type = objData.type,
         obj, parts = [];
      
      if (type == "cm:person")
      {
         obj = Evaluator.getPersonObject(value);
         if (obj == null)
         {
            return false;
         }
         objData.displayValue = obj.displayName;
         objData.metadata = obj.userName;
      }
      else if (type == "cm:folder")
      {
         obj = Evaluator.getContentObject(value);
         if (obj == null)
         {
            return false;
         }
         parts =  Evaluator.splitQNamePath(obj);
         objData.siteId = parts[0];
         objData.displayValue = obj.displayPath.substring(companyhome.name.length() + 1);
         objData.metadata = "container";
      }
      else if (type == "cm:cmobject" || type == "cm:content")
      {
         obj = Evaluator.getContentObject(value);
         if (obj == null)
         {
            return false;
         }
         parts =  Evaluator.splitQNamePath(obj);
         objData.siteId = parts[0];
         objData.displayValue = obj.properties["cm:name"];
         objData.metadata = obj.isContainer ? "container" : "document";
      }
      else if(type == "bcpg:product")
	  {
		 obj = Evaluator.getContentObject(value);
         if (obj == null)
         {
            return false;
         }
         parts =  Evaluator.splitQNamePath(obj);
         objData.siteId = parts[0];
         objData.displayValue = obj.properties["cm:name"];
         // the namespace may be different due to inheritance (ie: {http://www.bcpg.fr/model/clientName/1.0}
         //objData.metadata = obj.type.replace('{http://www.bcpg.fr/model/becpg/1.0}', '');
         objData.metadata = obj.type.split("}")[1];
	  }
	  else if(type == "bcpg:ing" || type == "bcpg:nut" || type == "bcpg:allergen" || type == "bcpg:organo" || type == "bcpg:listValue" || type == "bcpg:cost" || type == "bcpg:physicoChem"  || type == "bcpg:microbio" || type == "bcpg:bioOrigin" || type == "bcpg:geoOrigin" ||  type == "bcpg:entity" || type == "bcpg:charact" || type == "qa:controlPoint" || type == "qa:controlStep"  || type == "qa:controlMethod" || type == "bcpg:entity" || type == "eco:changeUnit" || type == "bcpg:supplier" || type == "bcpg:client" || type == "bcpg:resourceProduct" || type == "mpm:processResource" || type == "mpm:processStep" || type == "var:charact") // don't forget to modify the file entity-datagrid.js otherwise, we cannont navigate to the object
	  {
		 obj = Evaluator.getContentObject(value);
         if (obj == null)
         {
            return false;
         }
         parts =  Evaluator.splitQNamePath(obj);
         objData.siteId = parts[0];
         objData.displayValue = obj.properties["cm:name"];
         objData.metadata = "document";
	  }
	  else if(type== "cm:authorityContainer")
	  {
		 obj = Evaluator.getContentObject(value);
         if (obj == null)
         {
            return false;
         }
         objData.displayValue = obj.properties["cm:authorityDisplayName"];
         objData.metadata = "document";
	  }	  
	  else if(type == "qname")
	  {
		  //TODO generic way
		  if(value == "{http://www.bcpg.fr/model/becpg/1.0}compoList"){
			  objData.displayValue = Packages.org.springframework.extensions.surf.util.I18NUtil.getMessage("bcpg_bcpgmodel.type.bcpg_compoList.title");
		  }
		  else if(value == "{http://www.bcpg.fr/model/becpg/1.0}packagingList")
		  {
			  objData.displayValue = Packages.org.springframework.extensions.surf.util.I18NUtil.getMessage("bcpg_bcpgmodel.type.bcpg_packaging.title");
		  }
	  }
      return true;
   },
   
   /**
    * Splits the qname path to a node.
    * 
    * Returns an array with:
    * [0] = site
    * [1] = container or null if the node does not match
    * [2] = remaining part of the cm:name based path to the object - as an array
    */
    splitQNamePath : function(node)
   {
      var path = node.qnamePath;
      var displayPath = node.displayPath.split("/");
      var parts = null;
      
      if (path.match("^"+SITES_SPACE_QNAME_PATH) == SITES_SPACE_QNAME_PATH)
      {
         var tmp = path.substring(SITES_SPACE_QNAME_PATH.length);
         var pos = tmp.indexOf('/');
         if (pos >= 1)
         {
            // site id is the cm:name for the site - we cannot use the encoded QName version
            var siteId = displayPath[3];
            tmp = tmp.substring(pos + 1);
            pos = tmp.indexOf('/');
            if (pos >= 1)
            {
               // strip container id from the path
               var containerId = tmp.substring(0, pos);
               containerId = containerId.substring(containerId.indexOf(":") + 1);
               
               parts = [ siteId, containerId, displayPath.slice(5, displayPath.length) ];
            }
         }
      }
      
      return (parts != null ? parts : [ null, null, displayPath ]);
   },

   
   /**
    * Node Evaluator - main entrypoint
    */
   run: function Evaluator_run(node, fields, hasWriteAccess)
   {
      var permissions = {},
         actionSet = "",
         actionLabels = {},
         createdBy = Common.getPerson(node.properties["cm:creator"]),
         modifiedBy = Common.getPerson(node.properties["cm:modifier"]),
         nodeData = {};
 
      /**
       * PERMISSIONS
       */
      permissions =
      {
         "create": hasWriteAccess && node.hasPermission("CreateChildren"),
         "edit": hasWriteAccess && node.hasPermission("Write"),
         "delete": hasWriteAccess && node.hasPermission("Delete")
      };

      // Use the form service to parse the required properties
      scriptObj = formService.getForm("node", node.nodeRef, fields, fields);

      // Make sure we can quickly look-up the Field Definition within the formData loop...
      var objDefinitions = {};
      for each (formDef in scriptObj.fieldDefinitions)
      {
         objDefinitions[formDef.dataKeyName] = formDef;
      }

      // Populate the data model
      var formData = scriptObj.formData.data;
      for (var k in formData)
      {
         var isAssoc = k.indexOf("assoc") == 0,
            value = formData[k].value,
            values,
            type = isAssoc ? objDefinitions[k].endpointType : objDefinitions[k].dataType,
            endpointMany = isAssoc ? objDefinitions[k].endpointMany : false,
            objData =
            {
               type: type
            };

         if (value instanceof java.util.Date)
         {
            objData.value = utils.toISO8601(value);
            objData.displayValue = objData.value;
            nodeData[k] = objData;
         }
         else if (endpointMany)
         {
            if (value.length() > 0)
            {
               values = value.split(",");
               nodeData[k] = [];
               for each (value in values)
               {
                  var objLoop =
                  {
                     type: objData.type,
                     value: value,
                     displayValue: value
                  };

                  if (Evaluator.decorateFieldData(objLoop, node))
                  {
                     nodeData[k].push(objLoop);
                  }
               }
            }
         }
         else
         {
            objData.value = value;
            objData.displayValue = objData.value;

            if (Evaluator.decorateFieldData(objData, node))
            {
               nodeData[k] = objData;
            }
         }
      }

      return(
      {
         node: node,
         nodeData: nodeData,
         actionSet: actionSet,
         actionPermissions: permissions,
         createdBy: createdBy,
         modifiedBy: modifiedBy,
         tags: node.tags,
         actionLabels: actionLabels
      });
   }
};
