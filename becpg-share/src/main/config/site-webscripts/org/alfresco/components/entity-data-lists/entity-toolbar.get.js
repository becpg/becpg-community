
/**
 * Main entrypoint for component webscript logic
 *
 * @method main
 */
function main()
{
  
   var entityNodeRef = page.url.args.nodeRef;
   
   model.showFormulate = showFormulate(entityNodeRef);
   model.showECO = showECO(entityNodeRef);   
}

/**
 * Call backend to retrieve aspect and see if it should show the formulate button
 * @param nodeRef
 * @returns
 */
function showFormulate(nodeRef)
{
   var result = remote.call("/slingshot/doclib/aspects/node/"+nodeRef.replace(":/",""));
   
   if (result.status == 200)
   {
      var aspects = eval('(' + result + ')'),
      aspect;
      
      for (var i = 0, ii = aspects.current.length; i < ii; i++)
      {
         aspect = aspects.current[i];
         if (aspect == "bcpg:transformationAspect")
         {
            return true;
         }
      }
   }

   return false;
}

/**
 * Call backend to retrieve type and see if it should show the ECO buttons
 * @param nodeRef
 * @returns
 */
function showECO(nodeRef)
{
   var result = remote.call("/slingshot/doclib/node/"+nodeRef.replace(":/",""));
   
   if (result.status == 200)
   {
      var node = eval('(' + result + ')');
      
      if(node.item.nodeType == "ecm:changeOrder"){
    	  return true;
      }      
   }

   return false;
}


main();