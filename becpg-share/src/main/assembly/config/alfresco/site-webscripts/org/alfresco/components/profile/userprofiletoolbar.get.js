<import resource="classpath:/alfresco/site-webscripts/org/alfresco/becpg/menu/imports/share-header.lib.js">
/**
 * User Profile - Toolbar Component GET method
 */
function main()
{
   var userId = page.url.templateArgs["userid"];
   if (userId == null)
   {
      userId = user.name;
   }
   model.activeUserProfile = (userId == user.name);
   model.activePage = (page.url.templateArgs.pageid || "");

   model.following = -1;
   model.followers = -1;

   var following = remote.call("/api/subscriptions/" + encodeURIComponent(userId) + "/following/count");
   if (following.status == 200)
   {
      model.following = JSON.parse(following).count;

      if (model.activeUserProfile)
      {
         var followers = remote.call("/api/subscriptions/" + encodeURIComponent(userId) + "/followers/count");
         if(followers.status == 200)
         {
            model.followers = JSON.parse(followers).count;
         }
      }
   }


   model.links = [];

   // Add Profile link
   addLink("profile-link", "profile", "link.info");
   //Add delegations
   if (model.activeUserProfile)
   {
	   if(!isExternalUser(user))
	   {	   
		   //TODO Move that to project or add delegation to core ?
		   addLink("user-delegation-link", "user-delegation?nodeRef="+getPersonNodeRef(user), "link.delegation");
	   }
	   if(isLanguageMgr(user)){
		   addLink("user-language-link", "user-language?nodeRef="+getPersonNodeRef(user), "link.language");
	   }
   }
   // Add User Sites link
   addLink("user-sites-link", "user-sites", "link.sites");
   // Add User Content link
   addLink("user-content-link", "user-content", "link.content");

   if (model.activeUserProfile)
   {
      if (model.following != -1)
      {
         // Add Following link
         addLink("following-link", "following", "link.following", [model.following]);
      }

      if (model.followers != -1)
      {
         // Add Followers link
         addLink("followers-link", "followers", "link.followers", [model.followers]);
      }

      if (user.capabilities.isMutable)
      {
         // Add Change Password link
         addLink("change-password-link", "change-password", "link.changepassword");
      }

      // Add Notifications links
      addLink("user-notifications-link", "user-notifications", "link.notifications");

      
      // Add Trashcan link
      addLink("user-trashcan-link", "user-trashcan", "link.trashcan");
   }
   else
   {
      if (model.following != -1)
      {
         // Add Following link
         addLink("otherfollowing-link", "following", "link.otherfollowing", [model.following]);
      }
   }
}

function addLink(id, href, msgId, msgArgs)
{
	
   model.links.push(
   {
      id: id,
      href: href,
      cssClass: (model.activePage == href) ? "theme-color-4" : null,
      label: msg.get(msgId, msgArgs ? msgArgs : null)
   });
}

function getPersonNodeRef(user){
    for(var capability  in user.capabilities){
       if(capability.indexOf("personNodeRef_") == 0){
           return capability.substring(14);
       }
    }
    return null;
}

main();