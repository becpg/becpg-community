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
                                                   <p class="title" style="font-size: 20px; color: #0f515f; font-weight: bold;">Vous avez été invité à rejoindre le site ${args["siteName"]}</p>
                                                </td>
                                             </tr>
                                          </table>
                                          <div style="font-size: 14px; margin: 12px 0px 0px 0px; padding-top: 10px; border-top: 1px solid #a1a8aa;">
                                             <p>Bonjour ${inviteePerson.properties["cm:firstName"]!""},</p>
                                             <p style="margin-bottom:5px">${inviterPerson.properties["cm:firstName"]!""} ${inviterPerson.properties["cm:lastName"]!""} vous invite à rejoindre le site ${args["siteName"]} avec un rôle de ${args["inviteeSiteRole"]}.</p>
                                             <a href="${args["acceptLink"]}"><button style="margin-bottom:15px"><b>Cliquez sur ce lien pour accepter l'invitation</b></button></a>
                                             

                                           <#if args["inviteeGenPassword"]?exists>
                                             <p style="margin-bottom:8px">Un compte a été créé pour vous et vos informations de connexion sont les suivantes :<br />
                                             <br />Nom d'utilisateur : ${args["inviteeUserName"]}
                                             <br />Mot de passe : ${args["inviteeGenPassword"]}
                                             </p>
                                             <p style="font-size:12px;color:#696969;font-style:italic;margin-bottom:20px">Nous vous conseillons vivement de modifier votre mot de passe lors de votre première connexion.
                                             <br />Pour ce faire, sélectionnez <b>Mon profil</b>, puis <b>Changer de mot de passe</b>.</p>
                                            </#if>
                                             <div style="margin-bottom:20px"><a href="${args["rejectLink"]}"><button><b>Cliquez sur ce lien pour décliner l'invitation</b></button></a></div>
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
                                  Pour en savoir plus sur beCPG, visitez <a href="http://www.becpg.net">http://www.becpg.net</a>
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

