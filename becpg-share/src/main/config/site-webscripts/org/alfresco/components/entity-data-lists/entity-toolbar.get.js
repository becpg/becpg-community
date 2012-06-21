
/**
 * Main entrypoint for component webscript logic
 *
 * @method main
 */
function main()
{
  
   var entityNodeRef = page.url.args.nodeRef;
   
   model.showECO = showECO(entityNodeRef);   
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