/**
 * Entity Logo Upload method
 * 
 * @method POST
 * @param filedata {file}
 */

function main()
{
   try
   {
      var filename = null;
      var content = null;
      var nodeRef = null;
      
      // locate file attributes
      for each (field in formdata.fields)
      {
         if (field.name == "filedata" && field.isFile)
         {
            filename = field.filename;
            content = field.content;
           
         }else if (field.name == "updateNodeRef")
         {
        	 nodeRef = field.value;
          }
      }
      
      // ensure all mandatory attributes have been located
      if (filename == undefined || content == undefined)
      {
         status.code = 400;
         status.message = "Uploaded file cannot be located in request";
         status.redirect = true;
         return;
      }
      var entityNode = search.findNode(nodeRef);
      // create the new image node
      logoNode = bThumbnail.getOrCreateImageNode(entityNode);
      logoNode.properties.content.write(content);
      logoNode.properties.content.guessMimetype(filename);
      logoNode.save();
      
      // save ref to be returned
      model.logo = logoNode;
      model.name = filename;
   }
   catch (e)
   {
      var x = e;
      status.code = 500;
      status.message = "Unexpected error occured during upload of new content.";
      if (x.message && x.message.indexOf("org.alfresco.service.cmr.usage.ContentQuotaException") == 0)
      {
         status.code = 413;
         status.message = x.message;
      }
      status.redirect = true;
      return;
   }
}

main();
