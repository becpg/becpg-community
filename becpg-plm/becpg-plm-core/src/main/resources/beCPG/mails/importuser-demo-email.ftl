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
                                                      Trial account for beCPG
                                                   </div>
                                                   <div style="font-size: 13px;">
                                                      ${date?datetime?string.full}
                                                   </div>
                                                </td>
                                             </tr>
                                          </table>
                                          <div style="font-size: 14px; margin: 12px 0px 24px 0px; padding-top: 10px; border-top: 1px solid #aaaaaa;">
                                             <p>Hi ${person.properties["cm:firstName"]!""},</p>
                                             
                                             <p>Here is your account to try beCPG:
															<br />- Url : <a href="${shareUrl}">${shareUrl}</a>
															<br />- Account : ${username}
															<br />- Password (I advice you to change it) : ${password}</p>
															
															<p>I advice you to use Firefox or Chrome browser.<br />
															At this page, you will find a food product: <a href="https://inst2.becpg.fr/share/page/site/rd/entity-data-lists?list=View-properties&nodeRef=workspace://SpacesStore/34fed2e5-393a-4c19-b304-1188283083df">Custom Quiche 400g</a>, <a href="https://inst2.becpg.fr/share/page/site/rd/entity-data-lists?nodeRef=workspace://SpacesStore/34fed2e5-393a-4c19-b304-1188283083df&list=compoList">Composition (recipe)</a><br />
															At this page, you will find a sample project: <a href="https://inst2.becpg.fr/share/page/site/projects/entity-data-lists?list=View-properties&nodeRef=workspace://SpacesStore/c4926fd4-8082-4171-b75c-f666be77c2fc">Project Quiche</a><br />
															</p>
															<p>
															Visit this page to get help: <a href="https://www.becpg.fr/redmine/projects/becpg-community/wiki/User-doc">documentation</a><br />
															We answer questions on forums: <a href="https://www.becpg.fr/redmine/projects/becpg-community/boards">forums</a><br />
															</p>
															
															<p>Feel free to come back to Philippe (philippe.quere@becpg.fr) for any question / comment or if you want a web demo of beCPG.</p>
                                             
                                             <p>Sincerely,<br />
                                             beCPG</p>
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
                                 To find out more about beCPG visit <a href="http://www.becpg.net">http://www.becpg.net</a>
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