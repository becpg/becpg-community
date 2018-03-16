
function main()
{
   model.success = false;
   
   // Call the repo to create the st:site folder structure
   var conn = remote.connect("alfresco");
   var repoResponse = conn.get("/becpg/admin/repository/init-repo");
   if (repoResponse.status == 401)
   {
      status.setCode(repoResponse.status, "error.loggedOut");
   }
   else
   {
      var repoJSON = JSON.parse(repoResponse);
      // Check if we got a positive result from create site
      if (repoJSON.sites)
      {
    	  
    	  for (var i= 0; i< repoJSON.sites.length; i++) {
		    	  var site = repoJSON.sites[i];
		         // Yes we did, now create the Surf objects in the web-tier and the associated configuration elements
		         // Retry a number of times until success - remove the site on total failure
		         for (var r=0; r<3 && !model.success; r++)
		         {
		            var tokens = [];
		            tokens["siteid"] = site.shortName;
		            model.success = sitedata.newPreset(site.sitePreset, tokens);
		         }
		         // if we get here - it was a total failure to create the site config - even after retries
		         if (!model.success)
		         {
		            // Delete the st:site folder structure and set error handler
		            conn.del("/api/sites/" + encodeURIComponent(repoJSON.shortName));
		            status.setCode(status.STATUS_INTERNAL_SERVER_ERROR, "error.create");
		         }
		         
		         model.success = false;
         
    	  }
      }
      else if (repoJSON.status.code)
      {
         // Default error handler to report failure to create st:site folder
         status.setCode(repoJSON.status.code, repoJSON.message);
      }
   }
}

main();