<html>
   <#assign inviterPersonRef=args["inviterPersonRef"]/>
   <#assign inviterPerson=companyhome.nodeByReference[inviterPersonRef]/>
   <#assign inviteePersonRef=args["inviteePersonRef"]/>
   <#assign inviteePerson=companyhome.nodeByReference[inviteePersonRef]/>

   <head>
      <style type="text/css">
     body {
         font-family: Arial, sans-serif;
         font-size: 14px;
         color: #4c4c4c;
      }

      a,
      a:visited {
         color: #0072cf;
      }
      
      
      button 
     {
        background-color: white ;
         border-radius: 5px;
         border : solid 1px #ff642d;
         color:#ff642d;
         padding: 10px 25px;
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
            align-items:center;
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
         button {
            width:100%;
         }
      }
      </style>
   </head>
   
   <body bgcolor="#dddddd">
      <table width="100%" cellpadding="20" cellspacing="0" border="0" bgcolor="#dddddd">
         <tr>
            <td width="100%" align="center">
                <table class="table" cellpadding="0" cellspacing="0" bgcolor="white" style="background-color: white; border: 1px solid #cccccc; border-radius: 15px;">
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
                                                <td class="flex">
                                                   <img class="img" src="${shareUrl}/res/components/images/project-email-logo.png"alt="" height="64" border="0" />
                                                   <p class="title" style="font-size: 20px; color: #0f515f; font-weight: bold;">${message("templates.invite-email-add-direct.html.header", args["siteName"])}</p>
                                                </td>
                                             </tr>
                                          </table>
                                          <div style="font-size: 14px; margin: 12px 0px 0px 0px; padding-top: 10px; border-top: 1px solid #a1a8aa;">
                                             <p>${message("templates.invite-email-add-direct.html.salutation", inviteePerson.properties["cm:firstName"]!"")}</p>
      
                                             <p>${message("templates.invite-email-add-direct.html.detail", inviterPerson.properties["cm:firstName"]!"", inviterPerson.properties["cm:lastName"]!"", args["siteName"], args["inviteeSiteRole"])}</p>
                                             
                                             <a href="${args["siteDashboardLink"]}"><button><b>${message("templates.invite-email-add-direct.html.navigate", args["siteName"])}</b></button></a>
                                             
                                             <p>${message("templates.invite-email-add-direct.html.closing", productName!"")}</p>
                                             
                                             <div class="alf-leave-site" style="margin-bottom:20px">
                                                ${message("templates.invite-email-add-direct.html.leave", args["siteLeaveLink"])}
                                             </div>
                                          </div>
                                       </td>
                                    </tr>
                                 </table>
                              </td>
                           </tr>
                           <tr>
                              <td>
                                 <div style="border-top: 1px solid #a1a8aa;">&nbsp;</div>
                              </td>
                           </tr>
                           <tr>
                              <td style="padding: 0px 30px; font-size: 13px;">
                                 Pour en savoir plus sur beCPG, visitez notre site Web <a href="http://www.becpg.fr">http://www.becpg.fr</a>
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