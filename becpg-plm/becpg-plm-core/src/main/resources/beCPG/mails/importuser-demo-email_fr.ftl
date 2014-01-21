<html>

   <head>
      <style type="text/css"><!--
      body
      {
         font-family: Arial, sans-serif;
         font-size: 14px;
         color: #4c4c4c;
      }
      
      a, a:visited
      {
         color: #0072cf;
      }
      --></style>
   </head>
   
   <body bgcolor="#dddddd">
      <table width="100%" cellpadding="20" cellspacing="0" border="0" bgcolor="#dddddd">
         <tr>
            <td width="100%" align="center">
               <table width="70%" cellpadding="0" cellspacing="0" bgcolor="white" style="background-color: white; border: 1px solid #aaaaaa;">
                  <tr>
                     <td width="100%">
                        <table width="100%" cellpadding="0" cellspacing="0" border="0">
                           <tr>
                              <td style="padding: 10px 30px 0px;">
                                 <table width="100%" cellpadding="0" cellspacing="0" border="0">
                                    <tr>
                                       <td>
                                          <table cellpadding="0" cellspacing="0" border="0">
                                             <tr>
                                                <td>
                                                   <img src="${shareUrl}/res/components/site-finder/images/site-64.png" alt="" width="64" height="64" border="0" style="padding-right: 20px;" />
                                                </td>
                                                <td>
                                                   <div style="font-size: 22px; padding-bottom: 4px;">
                                                      Compte d'essai pour beCPG
                                                   </div>
                                                   <div style="font-size: 13px;">
                                                      ${date?datetime?string.full}
                                                   </div>
                                                </td>
                                             </tr>
                                          </table>
                                          <div style="font-size: 14px; margin: 12px 0px 24px 0px; padding-top: 10px; border-top: 1px solid #aaaaaa;">
                                             <p>Bonjour ${person.properties["cm:firstName"]!""},</p>
                                             
                                             <p>Voici votre compte pour accéder au système de démo :
															<br />- Url : <a href="${shareUrl}">${shareUrl}</a>
															<br />- Compte : ${username}
															<br />- Mot de passe (modifiez le lors de votre première connexion) : ${password}                                             
															</p>
															
															<p>Je vous conseille d'utiliser Firefox ou Chrome.
															<br />Dans le site "Projets", vous trouverez des exemples de projets.
															<br />Dans le site "Produits", vous trouverez des exemples de produits.
															<br />Vous pouvez créer de nouveaux projets ou produits dans les sites existants ou vous pouvez créer un nouveau site.
															<br />Vous trouverez des tutoriels à cette page : <a href="http://www.becpg.fr/documentation/starting-kit">http://www.becpg.fr/documentation/starting-kit</a></p>
															
															<p>N'hésitez pas à revenir vers moi (philippe.quere@becpg.fr) pour toute question / remarque ou si vous souhaitez une web démo de 30 minutes pour avoir une vue d'ensemble de beCPG.</p>
                                             
                                             <p>Cordialement,
                                             <br />beCPG</p>
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
                                 Pour avoir plus d'informations sur beCPG, visitez <a href="http://www.becpg.fr">http://www.becpg.fr</a>
                              </td>
                           </tr>
                           <tr>
                              <td>
                                 <div style="border-bottom: 1px solid #aaaaaa;">&nbsp;</div>
                              </td>
                           </tr>
                           <tr>
                              <td style="padding: 10px 30px;">
                                 <img src="${shareUrl}/themes/default/images/app-logo.png" alt="" width="117" height="48" border="0" />
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