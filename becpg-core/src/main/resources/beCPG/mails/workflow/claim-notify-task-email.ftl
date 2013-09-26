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
                                                   <img src="${shareUrl}/res/components/images/task-64.png" alt="" width="64" height="64" border="0" style="padding-right: 20px;" />
                                                </td>
                                                <td>
                                                   <div style="font-size: 22px; padding-bottom: 4px;">
                                                      Pour information
                                                   </div>
                                                   <div style="font-size: 13px;">
                                                      ${date?datetime?string.full}
                                                   </div>
                                                </td>
                                             </tr>
                                          </table>
                                          <div style="font-size: 14px; margin: 12px 0px 24px 0px; padding-top: 10px; border-top: 1px solid #aaaaaa;">
                                             <p>Bonjour,</p>
           
                                             <p><b>${args.workflowTitle}</b></p>
                                             
                                             <#if (args.workflowDescription)??>                                             
                                             	<p>${args.workflowDescription}</p>                                             
                                             </#if>
                                             
                                             <p>
                                                <#if (args.workflowDueDate)??>Due:&nbsp;&nbsp;<b>${args.workflowDueDate?date?string.full}</b><br></#if>
                                                <#if (args.workflowPriority)??>
                                                   Priorité :&nbsp;&nbsp;
                                                   <b>
                                                   <#if args.workflowPriority == 3>
                                                      Basse
                                                   <#elseif args.workflowPriority == 2>
                                                      Moyenne
                                                   <#else>
                                                      Élevée
                                                   </#if>
                                                   </b>
                                                </#if>
                                             </p>
                                             
                                            <p>Cliquez sur ce lien pour voir la tâche :</p>
                                            <p><a href="${shareUrl}/page/task-details?taskId=${args.workflowId}">${shareUrl}/page/task-details?taskId=${args.workflowId}</a>
                                             
                                             
                                             <p>Cordialement,<br />
                                            beCPG</p>
                                          </div>
                                       </td>
                                    </tr>
                                 </table>
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