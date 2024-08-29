<@markup id="css" >
   <#-- CSS Dependencies -->
   <@link href="${url.context}/res/components/profile/profile.css" group="profile"/>
   <@link href="${url.context}/res/components/profile/usernotifications.css" group="profile"/>
</@>

<@markup id="js">
   <#-- JavaScript Dependencies -->
   <@script src="${url.context}/res/components/profile/usernotifications.js" group="profile"/>
</@>

<@markup id="widgets">
   <@createWidgets group="profile"/>
</@>

<@markup id="html">
   <@uniqueIdDiv>
      <#assign el=args.htmlid?html>
      <div id="${el}-body" class="notifications profile">
         <form id="${el}-form" action="${url.context}/service/components/profile/user-notifications" method="post">
            <div class="header-bar">${msg("label.notifications")}</div>
            <div class="row">
               <span class="label"><label for="email-site-activities">${msg("label.email-site-activities")}:</label></span>
               <span><input type="checkbox" id="email-site-activities" <#if !emailSiteActivitiesDisabled>checked</#if>/></span>
            </div>
            <div class="row">
               <span class="label"><label for="email-task-resource">${msg("label.email-task-resource")}:</label></span>
               <span><input type="checkbox" id="email-task-resource" <#if !emailTaskResourceDisabled>checked</#if>/></span>
            </div>
            <div class="row">
               <span class="label"><label for="email-task-observer">${msg("label.email-task-observer")}:</label></span>
               <span><input type="checkbox" id="email-task-observer" <#if !emailTaskObserverDisabled>checked</#if>/></span>
            </div>
            <div class="row">
               <span class="label"><label for="email-project-notification">${msg("label.email-project-notification")}:</label></span>
               <span><input type="checkbox" id="email-project-notification" <#if !emailProjectNotificationDisabled>checked</#if>/></span>
            </div>
            <div class="row">
               <span class="label"><label for="email-admin-notification">${msg("label.email-admin-notification")}:</label></span>
               <span><input type="checkbox" id="email-admin-notification" <#if !emailAdminNotificationDisabled>checked</#if>/></span>
            </div>
            <hr/>
            <div class="buttons">
               <button id="${el}-button-ok" name="save">${msg("button.ok")}</button>
               <button id="${el}-button-cancel" name="cancel">${msg("button.cancel")}</button>
            </div>
         </form>
      </div>
   </@>
</@>