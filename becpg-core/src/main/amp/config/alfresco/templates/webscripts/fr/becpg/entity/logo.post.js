/**
 * Entity Logo Upload method
 * 
 * @method POST
 * @param filedata
 *            {file}
 */

function main()
{
   try
   {
      var filename = null;
      var content = null;
      var updateNodeRef = null;
      
      var fnFieldValue = function(p_field)
      {
         return p_field.value.length() > 0 && p_field.value != "null" ? p_field.value : null;
      };

      
      // locate file attributes
      for each (field in formdata.fields)
      {
          switch (String(field.name).toLowerCase())
          {
             
              case "filename":
                  filename = fnFieldValue(field);
                  break;

               case "filedata":
                  if (field.isFile)
                  {
                     filename = filename ? filename : field.filename;
                     content = field.content;
                     mimetype = field.mimetype;
                  }
                  break;
               case "updatenoderef":
                   updateNodeRef = fnFieldValue(field);
                   break;
          }
          
      }
      
      // ensure all mandatory attributes have been located
      if (filename == undefined || content == undefined || updateNodeRef ==  undefined)
      {
         status.code = 400;
         status.message = "Uploaded file cannot be located in request";
         status.redirect = true;
         return;
      }
      var entityNode = search.findNode(updateNodeRef);
      // create the new image node
      logoNode = bThumbnail.getOrCreateImageNode(entityNode);
      logoNode.properties.content.write(content);
      logoNode.properties.content.guessMimetype(filename);
      var extension = filename.substring(filename.lastIndexOf(".")+1, filename.length());
      var name = logoNode.properties["cm:name"];
      if(name.indexOf(".")>0){
    	  logoNode.properties["cm:name"] = name.substring(0,name.lastIndexOf("."))+"."+extension;
      } else {
    	  logoNode.properties["cm:name"] = name+"."+extension;
      }
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
      } else {
          logger.warn(e);
      }
      status.redirect = true;
      return;
   }
}

main();
