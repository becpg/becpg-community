<html>
   <head>
   <style type="text/css">
      <!--
      body {
         font-family: Arial, sans-serif;
         font-size: 14px;
         color: #4c4c4c;
      }

      a,
      a:visited {
         color: #004254;
		 text-decoration: none;
		 font-weight: bolder;
      }
      
      
      button 
     {
        background-color: white ;
         border-radius: 5px;
         border : solid 1px #ff642d;
         color:#ff642d;
         padding: 15px 32px;
         text-align: center;
         text-decoration: none;
         font-size: 16px;
         cursor : pointer;
         margin-bottom: 5px;
      }
      button:hover {
         background-color : #ff642d;
         border : solid 1px #ff642d;
         color: white;
      }
      button:focus {
         outline:none;
      }
      .img {
            padding-right:20px;
      }
      .line {
         height:4px;
         width:30px;
         background-color:#ff642d;
         margin:4px 0px 14px 0px;
         border-radius:5px;
      }
	  
      @media (min-width: 660px) {
         td .flex {
            display:flex;
         }
         .img {
            padding-right:20px;
         }
         .table {
            width:70%;
         }
      }
      @media (max-width: 660px) {
         td .flex {
            text-align:center;
         }
         td .title {
            margin-top : 5px;
margin-bottom : 0px;
         }
       .img {
            padding-right:0;
margin-top:3px;
         }
         .table {
            width:100%;
         }
      }
      -->
   </style>
</head>
   
   <body bgcolor="#dddddd">
   <table width="100%" cellpadding="20" cellspacing="0" border="0" bgcolor="#dddddd">
      <tr>
         <td width="100%" align="center">
            <table class="table" cellpadding="0" cellspacing="0" bgcolor="white"
               style="background-color: white; border: 1px solid #cccccc; border-radius: 15px;">
               <tr>
                  <td width="100%">
                     <table width="100%" cellpadding="0" cellspacing="0" border="0">
                        <tr>
                           <td style="padding: 10px 30px 0px;">
                              <table width="100%" cellpadding="0" cellspacing="0" border="0">
                                 <tr>
                                    <td>
                                         <table width="100%" cellpadding="0" cellspacing="0" border="0">
                                          <tr>
                                             <td class="flex" flex="wrap">
                                                <img class="img" src="${shareUrl}/res/components/images/project-email-logo.png"alt=""  height="64" border="0" />
                                                <p class="title" style="font-size: 20px; color: #004254; font-weight: bold;">Son Aktiviteler</p>
                                             </td>
                                          </tr>
                                       </table>
                                          <div style="font-size: 14px; margin: 18px 0px 24px 0px; padding-top: 18px; border-top: 1px solid #aaaaaa;">
                                             <#if activities?exists && activities?size &gt; 0>
                                             <#list activities as activity>
                                                <#if activity.siteNetwork??>
                                                <#assign userLink="<a href=\"${shareUrl}/page/user/${activity.postUserId?html}/profile\">${activity.activitySummary.firstName?html!\"\"} ${activity.activitySummary.lastName?html!\"\"}</a>">
                                                <#assign secondUserLink="">
                                                <#-- The activity feed stores the '@@NULL@@' placeholder when the activity is not bound to a site -->
                                                <#assign hasSite=activity.siteNetwork?has_content && activity.siteNetwork != "@@NULL@@">
                                                <#if hasSite>
                                                <#assign itemLink="<a href=\"${shareUrl}/page/site/${activity.siteNetwork?html}/${activity.activitySummary.page!\"\"}\">${(activity.activitySummary.title!\"\")?html}</a>">
                                                <#assign siteLink="<a href=\"${shareUrl}/page/site/${activity.siteNetwork?html}/dashboard\">${(siteTitles[activity.siteNetwork]?html)!activity.siteNetwork?html}</a>">
                                                <#else>
                                                <#assign itemLink="<a href=\"${shareUrl}/page/${activity.activitySummary.page!\"\"}\">${(activity.activitySummary.title!\"\")?html}</a>">
                                                <#assign siteLink="">
                                                </#if>
                                                
                                                <#assign suppressSite = !hasSite>
                                                
                                                <#switch activity.activityType>
                                                   <#case "org.alfresco.site.user-joined">
                                                   <#case "org.alfresco.site.user-left">
                                                      <#assign suppressSite=true>
                                                   <#case "org.alfresco.site.user-role-changed">
                                                      <#assign custom0=message("role."+activity.activitySummary.role)!"">
                                                      <#assign userLink="<a href=\"${shareUrl}/page/user/${activity.activitySummary.memberUserName?html}/profile\">${activity.activitySummary.memberFirstName?html!\"\"} ${activity.activitySummary.memberLastName?html!\"\"}</a>">
                                                      <#break>
                                                   <#case "org.alfresco.site.group-added">
                                                   <#case "org.alfresco.site.group-removed">
                                                      <#assign suppressSite=true>
                                                   <#case "org.alfresco.site.group-role-changed">
                                                      <#assign custom0=message("role."+activity.activitySummary.role)!"">
                                                      <#assign userLink=activity.activitySummary.groupName?replace("GROUP_", "")>
                                                      <#break>
                                                   <#case "org.alfresco.subscriptions.followed">
                                                      <#assign userLink="<a href=\"${shareUrl}/page/user/${activity.activitySummary.followerUserName?html}/profile\">${activity.activitySummary.followerFirstName?html!\"\"} ${activity.activitySummary.followerLastName?html!\"\"}</a>">
                                                      <#assign secondUserLink="<a href=\"${shareUrl}/page/user/${activity.activitySummary.userUserName?html}/profile\">${activity.activitySummary.userFirstName?html!\"\"} ${activity.activitySummary.userLastName?html!\"\"}</a>">                                                   
                                                      <#assign suppressSite=true>
                                                      <#break>
                                                   <#case "org.alfresco.subscriptions.subscribed">
                                                      <#assign userLink="<a href=\"${shareUrl}/page/user/${activity.activitySummary.subscriberUserName?html}/profile\">${activity.activitySummary.subscriberFirstName?html!\"\"} ${activity.activitySummary.subscriberLastName?html!\"\"}</a>">
                                                      <#assign custom0=(activity.activitySummary.node!"")?html>
                                                      <#assign suppressSite=true>
                                                      <#break>                                                   
                                                   <#case "org.alfresco.profile.status-changed">
                                                      <#assign custom0=(activity.activitySummary.status!"")?html>
                                                      <#assign suppressSite=true>
                                                      <#break>      
                                                   <#case "fr.becpg.entity.state-changed">
                                                      <#if hasSite>
                                                         <#assign itemLink="<a href=\"${shareUrl}/page/site/${activity.siteNetwork?html}/entity-data-lists?list=View-properties&nodeRef=${activity.activitySummary.entityNodeRef!\"\"}\">${(activity.activitySummary.title!\"\")?html}</a>">
                                                      <#else>
                                                         <#assign itemLink="<a href=\"${shareUrl}/page/entity-data-lists?list=View-properties&nodeRef=${activity.activitySummary.entityNodeRef!\"\"}\">${(activity.activitySummary.title!\"\")?html}</a>">
                                                      </#if>
                                                      <#assign custom0=message("state."+ activity.activitySummary.beforeState!"Unknow")>
                                                      <#assign custom1=message("state."+ activity.activitySummary.afterState!"Unknow")>
                                                      <#break> 
                                                   <#case "fr.becpg.project.project-state">
                                                         <#if hasSite>
                                                            <#assign itemLink="<a href=\"${shareUrl}/page/site/${activity.siteNetwork?html}/entity-details?nodeRef=${activity.activitySummary.nodeRef}\">${(activity.activitySummary.title!\"\")?html}</a>">
                                                         <#else>
                                                            <#assign itemLink="<a href=\"${shareUrl}/page/entity-details?nodeRef=${activity.activitySummary.nodeRef}\">${(activity.activitySummary.title!\"\")?html}</a>">
                                                            <#assign suppressSite=true>
                                                         </#if>
                                                         <#assign custom0=message("state."+ activity.activitySummary.beforeState!"Unknow")>
                                                         <#assign custom1=message("state."+ activity.activitySummary.afterState!"Unknow")>
                                                     <#break>
                                                     <#case "fr.becpg.project.task-state">
                                                          <#if hasSite>
                                                            <#assign itemLink="<a href=\"${shareUrl}/page/site/${activity.siteNetwork?html}/entity-data-lists?list=taskList&nodeRef=${activity.activitySummary.entityNodeRef}\">${(activity.activitySummary.title!\"\")?html} [${(activity.activitySummary.entityTitle!\"\")?html}]</a>">
                                                         <#else>
                                                            <#assign itemLink="<a href=\"${shareUrl}/page/entity-data-lists?list=taskList&nodeRef=${activity.activitySummary.entityNodeRef}\">${(activity.activitySummary.title!\"\")?html} [${(activity.activitySummary.entityTitle!\"\")?html}]</a>">
                                                            <#assign suppressSite=true>
                                                         </#if>
                                                         <#assign custom0=message("state."+ activity.activitySummary.beforeState!"Unknow")>
                                                         <#assign custom1=message("state."+ activity.activitySummary.afterState!"Unknow")>
                                                      <#break>
                                                      <#case "fr.becpg.project.deliverable-state">
                                                           <#if hasSite>
                                                            <#assign itemLink="<a href=\"${shareUrl}/page/site/${activity.siteNetwork?html}/entity-data-lists?list=deliverableList&nodeRef=${activity.activitySummary.entityNodeRef}\">${(activity.activitySummary.title!\"\")?html} [${(activity.activitySummary.entityTitle!\"\")?html}]</a>">
                                                         <#else>
                                                            <#assign itemLink="<a href=\"${shareUrl}/page/entity-data-lists?list=taskList&nodeRef=${activity.activitySummary.entityNodeRef}\">${(activity.activitySummary.title!\"\")?html} [${(activity.activitySummary.entityTitle!\"\")?html}]</a>">
                                                            <#assign suppressSite=true>
                                                         </#if>
                                                         <#assign custom0=message("state."+ activity.activitySummary.beforeState!"Unknow")>
                                                         <#assign custom1=message("state."+ activity.activitySummary.afterState!"Unknow")>
                                                      <#break>
                                                   <#case "fr.becpg.export">
                                                      <#assign custom0=(activity.activitySummary.title!"")?html>
                                                      <#assign suppressSite=true>
                                                      <#break>

                                                   <#default>

                                                </#switch>
                                                
                                                <#assign detail=message(activity.activityType?html, itemLink, userLink, custom0, custom1, siteLink, secondUserLink)!"">
                                                
                                                <#-- An unknown activity type resolves to no message at all: skip it rather than sending an empty line -->
                                                <#if detail?has_content>
                                                <div class="activity">
                                                   <#if suppressSite>${detail}<#else>${message("in.site", detail, siteLink)!""}</#if>
                                                </div>
                                                <div style="font-size: 11px; padding: 2px 0px 12px 0px; color:#ff642d">
                                                   ${activity.postDate?datetime?string.medium}
                                                </div>
                                                </#if>
                                                </#if>
                                             </#list>
                                             </#if>
                                          </div>
                                       </td>
                                    </tr>
                                 </table>
                              </td>
                           </tr>
                           <tr>
                              <td>
                                 <div style="border-top: 1px solid #aaaaaa;">&nbsp;</div>
                              </td>
                           </tr>
                           <tr>
                              <td style="padding: 0px 30px; font-size: 13px;">
                                 <br /><a href="${shareUrl}/page/user/${personProps["cm:userName"]}/user-notifications"><button ><b>Bildirimleri devre dışı bırak</b></button></a>
                              </td>
                           </tr>
                           <tr>
                              <td>
                                 <div style="border-bottom: 1px solid #a1a8aa;">&nbsp;</div>
                           </td>
                        </tr>
                        <tr>
                           <td style="padding: 10px 30px;">
                              <img style="padding :10px 0px" src="${mailLogoUrl}" />
                           </td>
                        </tr>
                     </table>
                  </td>
               </tr>
            </table>
         </td>
      </tr>
   </table>
</body>

</html>
