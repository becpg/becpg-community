function extractMetadata(file)
{
   // Extract metadata - via repository action for now.
   // This should use the MetadataExtracter API to fetch properties, allowing for possible failures.
   var emAction = actions.create("extract-metadata");
   if (emAction != null)
   {
      // Call using readOnly = false, newTransaction = false
      emAction.execute(file, false, false);
   }
}

function exitUpload(statusCode, statusMsg)
{
   status.code = statusCode;
   status.message = statusMsg;
   status.redirect = true;
   formdata.cleanup();
}

function main()
{
   try
   {
      var filename = null,
         content = null,
         mimetype = null,
         siteId = null, site = null,
         containerId = null, container = null,
         destination = null,
         destNode = null,
         thumbnailNames = null,
         i;

      // Upload specific
      var uploadDirectory = null,
         contentType = null,
         aspects = [],
         overwrite = true; // If a filename clashes for a versionable file

      // Update specific
      var updateNodeRef = null,
         majorVersion = false,
         description = "";
      
      // Prevents Flash- and IE8-sourced "null" values being set for those parameters where they are invalid.
      // Note: DON'T use a "!==" comparison for "null" here.
      var fnFieldValue = function(p_field)
      {
         return field.value.length() > 0 && field.value != "null" ? field.value : null;
      };

      // allow the locale to be set via an argument
      if (args["lang"] != null)
      {
         utils.setLocale(args["lang"]);
      }

      // Parse file attributes
      for each (field in formdata.fields)
      {
         switch (String(field.name).toLowerCase())
         {
            case "filedata":
               if (field.isFile)
               {
                  filename = field.filename;
                  content = field.content;
                  mimetype = field.mimetype;
               }
               break;

            case "siteid":
               siteId = fnFieldValue(field);
               break;

            case "containerid":
               containerId = fnFieldValue(field);
               break;

            case "destination":
               destination = fnFieldValue(field);
               break;

            case "uploaddirectory":
               uploadDirectory = fnFieldValue(field);
               if (uploadDirectory !== null)
               {
                  // Remove any leading "/" from the uploadDirectory
                  if (uploadDirectory.substr(0, 1) == "/")
                  {
                     uploadDirectory = uploadDirectory.substr(1);
                  }
                  // Ensure uploadDirectory ends with "/" if not the root folder
                  if ((uploadDirectory.length > 0) && (uploadDirectory.substring(uploadDirectory.length - 1) != "/"))
                  {
                     uploadDirectory = uploadDirectory + "/";
                  }
               }
               break;

            case "updatenoderef":
               updateNodeRef = fnFieldValue(field);
               break;

            case "description":
               description = field.value;
               break;

            case "contenttype":
               contentType = field.value;
               break;

            case "aspects":
               aspects = field.value != "-" ? field.value.split(",") : [];
               break;

            case "majorversion":
               majorVersion = field.value == "true";
               break;

            case "overwrite":
               overwrite = field.value == "true";
               break;

            case "thumbnails":
               thumbnailNames = field.value;
               break;
         }
      }

      // Ensure mandatory file attributes have been located. Need either destination, or site + container or updateNodeRef
      if ((filename === null || content === null) || (destination === null && (siteId === null || containerId === null) && updateNodeRef === null))
      {
         exitUpload(400, "Required parameters are missing");
         return;
      }

      /**
       * Site or Non-site?
       */
      if (siteId !== null && siteId.length() > 0)
      {
         /**
          * Site mode.
          * Need valid site and container. Try to create container if it doesn't exist.
          */
         site = siteService.getSite(siteId);
         if (site === null)
         {
            exitUpload(404, "Site (" + siteId + ") not found.");
            return;
         }

         container = site.getContainer(containerId);
         if (container === null)
         {
            try
            {
               // Create container since it didn't exist
               container = site.createContainer(containerId);
            }
            catch(e)
            {
               // Error could be that it already exists (was created exactly after our previous check) but also something else
               container = site.getContainer(containerId);
               if (container === null)
               {
                  // Container still doesn't exist, then re-throw error
                  throw e;
               }
               // Since the container now exists we can proceed as usual
            }
         }

         if (container === null)
         {
            exitUpload(404, "Component container (" + containerId + ") not found.");
            return;
         }
         
         destNode = container;
      }
      else if (destination !== null)
      {
         /**
          * Non-Site mode.
          * Need valid destination nodeRef.
          */
         destNode = search.findNode(destination);
         if (destNode === null)
         {
            exitUpload(404, "Destination (" + destination + ") not found.");
            return;
         }
      }

      /**
       * Update existing or Upload new?
       */
      if (updateNodeRef !== null)
      {
         /**
          * Update existing file specified in updateNodeRef
          */
         var updateNode = search.findNode(updateNodeRef);
         if (updateNode === null)
         {
            exitUpload(404, "Node specified by updateNodeRef (" + updateNodeRef + ") not found.");
            return;
         }
         
         if (updateNode.isLocked)
         {
            // We cannot update a locked document
            exitUpload(404, "Cannot update locked document '" + updateNodeRef + "', supply a reference to its working copy instead.");
            return;
         }

         if (!updateNode.hasAspect("cm:workingcopy"))
         {
            // Ensure the file is versionable (autoVersion = true, autoVersionProps = false)
            updateNode.ensureVersioningEnabled(true, false);

            // It's not a working copy, do a check out to get the actual working copy
            updateNode = updateNode.checkoutForUpload();
         }

         // Update the working copy content
         updateNode.properties.content.write(content);
         // Reset working copy mimetype and encoding
         updateNode.properties.content.guessMimetype(filename);
         updateNode.properties.content.guessEncoding();
         // check it in again, with supplied version history note
         updateNode = updateNode.checkin(description, majorVersion);

         // Extract the metadata
         // (The overwrite policy controls which if any parts of
         //  the document's properties are updated from this)
         extractMetadata(updateNode);

         // Record the file details ready for generating the response
         model.document = updateNode;
      }
      else
      {
         /**
          * Upload new file to destNode (calculated earlier) + optional subdirectory
          */
         if (uploadDirectory !== null && uploadDirectory.length > 0)
         {
            destNode = destNode.childByNamePath(uploadDirectory);
            if (destNode === null)
            {
               exitUpload(404, "Cannot upload file since upload directory '" + uploadDirectory + "' does not exist.");
               return;
            }
         }

         /**
          * Existing file handling.
          */
         var existingFile = destNode.childByNamePath(filename);
         if (existingFile !== null)
         {
            // File already exists, decide what to do
            if (existingFile.hasAspect("cm:versionable") && overwrite)
            {
               // Upload component was configured to overwrite files if name clashes
               existingFile.properties.content.write(content);

               // Reapply mimetype as upload may have been via Flash - which always sends binary mimetype
               existingFile.properties.content.guessMimetype(filename);
               existingFile.properties.content.guessEncoding();
               existingFile.save();

               // Extract the metadata
               // (The overwrite policy controls which if any parts of
               //  the document's properties are updated from this)
               extractMetadata(existingFile);

               // Record the file details ready for generating the response
               model.document = existingFile;

               // We're finished - bail out here
               formdata.cleanup();
               return;
            }
            else
            {
               // Upload component was configured to find a new unique name for clashing filenames
               var counter = 1,
                  tmpFilename,
                  dotIndex;

               while (existingFile !== null)
               {
                  dotIndex = filename.lastIndexOf(".");
                  if (dotIndex == 0)
                  {
                     // File didn't have a proper 'name' instead it had just a suffix and started with a ".", create "1.txt"
                     tmpFilename = counter + filename;
                  }
                  else if (dotIndex > 0)
                  {
                     // Filename contained ".", create "filename-1.txt"
                     tmpFilename = filename.substring(0, dotIndex) + "-" + counter + filename.substring(dotIndex);
                  }
                  else
                  {
                     // Filename didn't contain a dot at all, create "filename-1"
                     tmpFilename = filename + "-" + counter;
                  }
                  existingFile = destNode.childByNamePath(tmpFilename);
                  counter++;
               }
               filename = tmpFilename;
            }
         }

         /**
          * Create a new file.
          */
         var newFile = destNode.createFile(filename);
         if (contentType !== null)
         {
            newFile.specializeType(contentType);
         }
         // Use a the appropriate write() method so that the mimetype already guessed from the original filename is
         // maintained - as upload may have been via Flash - which always sends binary mimetype and would overwrite it.
         // Also perform the encoding guess step in the write() method to save an additional Writer operation.
         newFile.properties.content.write(content, false, true);
         newFile.save();

         // Create thumbnail?
         if (thumbnailNames != null)
         {
            var thumbnails = thumbnailNames.split(","),
               thumbnailName = "";

            for (i = 0; i < thumbnails.length; i++)
            {
               thumbnailName = thumbnails[i];
               if (thumbnailName != "" && thumbnailService.isThumbnailNameRegistered(thumbnailName))
               {
                  newFile.createThumbnail(thumbnailName, true);
               }
            }
         }

         // Additional aspects?
         if (aspects.length > 0)
         {
            for (i = 0; i < aspects.length; i++)
            {
               newFile.addAspect(aspects[i]);
            }
         }

         // Extract the metadata
         extractMetadata(newFile);

         // Record the file details ready for generating the response
         model.document = newFile;
      }
      
      // final cleanup of temporary resources created during request processing
 	 	formdata.cleanup();
   }
   catch (e)
   {
      try
      {
         formdata.cleanup();
      }
      catch (ce)
      {
         // NOTE: ignore
      }

      // capture exception, annotate it accordingly and re-throw
      if (e.message && e.message.indexOf("org.alfresco.service.cmr.usage.ContentQuotaException") == 0)
      {
         e.code = 413;
      }
      else
      {
         e.code = 500;
         e.message = "Unexpected error occurred during upload of new content.";      
      }
      throw e;
   }
}

main();
