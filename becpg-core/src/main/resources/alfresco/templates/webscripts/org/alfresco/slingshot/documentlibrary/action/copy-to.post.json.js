<import resource="classpath:/alfresco/templates/webscripts/org/alfresco/slingshot/documentlibrary/action/action.lib.js">

/**
 * Copy multiple files action
 * @method POST
 */

/**
 * Entrypoint required by action.lib.js
 *
 * @method runAction
 * @param p_params {object} Object literal containing files array
 * @return {object|null} object representation of action results
 */
function runAction(p_params)
{
   var results = [],
      destNode = p_params.destNode,
      files = p_params.files,
      file, fileNode, result, nodeRef;

   // Must have array of files
   if (!files || files.length === 0)
   {
      status.setCode(status.STATUS_BAD_REQUEST, "No files.");
      return;
   }
   
   for (file in files)
   {
      nodeRef = files[file];
      result =
      {
         nodeRef: nodeRef,
         action: "copyFile",
         success: false
      };
      
      try
      {
         fileNode = search.findNode(nodeRef);
         if (fileNode === null)
         {
            result.id = file;
            result.nodeRef = nodeRef;
            result.success = false;
         }
         else
         {
            result.id = fileNode.name;
            result.type = fileNode.isContainer ? "folder" : "document";
            // copy the node (deep copy for containers)
				//beCPG : if node has aspect bcpg:productList
            if (fileNode.isContainer || fileNode.hasAspect("bcpg:entityListsAspect"))
            {
               result.nodeRef = fileNode.copy(destNode, true).nodeRef.toString();
            }
            else
            {					
               result.nodeRef = fileNode.copy(destNode).nodeRef.toString();
            }
            result.success = (result.nodeRef !== null);
         }
      }
      catch (e)
      {
         result.id = file;
         result.nodeRef = nodeRef;
         result.success = false;
      }
      
      results.push(result);
   }

   return results;
}

/* Bootstrap action script */
main();
