/**
 * User Profile Component - User Notifications GET method
 */

function main()
{
   // Call the repo to retrieve user properties
   var emailSiteActivitiesDisabled = false;
   var emailTaskResourceDisabled = false;
   var emailTaskObserverDisabled = false;
   var emailProjectNotificationDisabled = false;
   var emailAdminNotificationDisabled = false;
   var result = remote.call("/api/people/" + encodeURIComponent(user.id));
   if (result.status == 200)
   {
      var person = JSON.parse(result);
      // we are interested in the "cm:emailFeedDisabled" property
      emailSiteActivitiesDisabled = person.emailFeedDisabled;
      emailTaskResourceDisabled = person.emailTaskResourceDisabled;
      emailTaskObserverDisabled = person.emailTaskObserverDisabled;
      emailProjectNotificationDisabled = person.emailProjectNotificationDisabled;
      emailAdminNotificationDisabled = person.emailAdminNotificationDisabled;
   }
   model.emailSiteActivitiesDisabled = emailSiteActivitiesDisabled;
   model.emailTaskResourceDisabled = emailTaskResourceDisabled;
   model.emailTaskObserverDisabled = emailTaskObserverDisabled;
   model.emailProjectNotificationDisabled = emailProjectNotificationDisabled;
   model.emailAdminNotificationDisabled = emailAdminNotificationDisabled;
   
   // Widget instantiation metadata...
   var userNotification = {
      id : "UserNotifications", 
      name : "Alfresco.UserNotifications"
   };
   model.widgets = [userNotification];
}

main();

