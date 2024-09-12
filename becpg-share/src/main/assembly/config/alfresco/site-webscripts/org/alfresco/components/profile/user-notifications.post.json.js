/**
 * User Notifications Update method
 * 
 * @method POST
 */
 
function main()
{
   // make remote call to update user notification setting on person object
   var emailSiteActivitiesDisabled = true;
   if (json.has("email-site-activities"))
   {
      emailSiteActivitiesDisabled = !(json.get("email-site-activities") == "on");
   }
   
   var emailTaskResourceDisabled = true;
   if (json.has("email-task-resource"))
   {
      emailTaskResourceDisabled = !(json.get("email-task-resource") == "on");
   }
   
   var emailTaskObserverDisabled = true;
   if (json.has("email-task-observer"))
   {
      emailTaskObserverDisabled = !(json.get("email-task-observer") == "on");
   }
   
   var emailProjectNotificationDisabled = true;
   if (json.has("email-project-notification"))
   {
      emailProjectNotificationDisabled = !(json.get("email-project-notification") == "on");
   }
   
   var emailAdminNotificationDisabled = true;
   if (json.has("email-admin-notification"))
   {
      emailAdminNotificationDisabled = !(json.get("email-admin-notification") == "on");
   }
   
   var conn = remote.connect("alfresco");
   var result = conn.post(
      "/slingshot/profile/userprofile",
      jsonUtils.toJSONString(
         {
            "username": user.id,
            "properties":
            {
               "cm:emailFeedDisabled": emailSiteActivitiesDisabled,
               "bcpg:emailTaskResourceDisabled": emailTaskResourceDisabled,
               "bcpg:emailTaskObserverDisabled": emailTaskObserverDisabled,
               "bcpg:emailProjectNotificationDisabled": emailProjectNotificationDisabled,
               "bcpg:emailAdminNotificationDisabled": emailAdminNotificationDisabled,
            }
         }),
      "application/json");
   if (result.status == 200)
   {
      model.success = true;
   }
   else
   {
      model.success = false;
      status.code = result.status;
   }
}

main();