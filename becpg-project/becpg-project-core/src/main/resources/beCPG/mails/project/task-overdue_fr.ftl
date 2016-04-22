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
                                                <img src="${shareUrl}/res/components/images/page-64.png" alt="" width="64" height="64" border="0" style="padding-right: 20px; float: left" />
                                                   <div style="font-size: 22px; padding-bottom: 4px; white-space: nowrap;">
                                                      Relance t&acirc;che "${args.task}"
                                                   </div>
                                                   <div style="font-size: 13px; white-space: nowrap;">
                                                      ${args.date?datetime?string.full}
                                                   </div>
                                                </td>
                                             </tr>
                                          </table>
                                          <div style="font-size: 14px; margin: 12px 0px 24px 0px; padding-top: 10px; border-top: 1px solid #aaaaaa;">
                                             <p>Bonjour,</p>
                                             
                                             	<p>Ceci est une relance automatique pour la t&acirc;che	"<b>${args.task}</b>" du projet "${args.project}".

												<br />Cette t&acirc;che doit &ecirc;tre effectu&eacute;e pour le <b>${args.dueDate?date}</b>.
												</p>												
                                             
                                              	<p>Vous pouvez la compl&eacute;ter depuis le dashlet "Mes t&acirc;ches", sur la page d'accueil
	                                              <#if args.taskId??>
	                                              <br/>ou bien en cliquant <a href="${shareUrl}/page/task-edit?taskId=${args.taskId}">sur ce lien</a>
	                                              </#if>
												</p>

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
                                 <img src="${shareUrl}/themes/default/images/app-logo.png" alt="" width="117" height="48" border="0" />
                              </td>
                           </tr>
                           <tr>
                              <td>
                                 <div style="border-bottom: 1px solid #aaaaaa;">&nbsp;</div>
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
