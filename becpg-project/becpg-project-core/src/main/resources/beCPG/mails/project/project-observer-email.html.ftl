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
                                             </tr>
                                          </table>
                                          <div style="font-size: 14px; margin: 12px 0px 24px 0px; padding-top: 10px; border-top: 1px solid #aaaaaa;">
                                             <p>Hi,</p>

                                             <p>                                             	
                                             	<#if args.activityType == 'State'>
                                             		The state of the task has been changed from <b>${args.beforeState}</b> to <b>${args.afterState}</b>.
                                             	<#elseif args.activityType == 'Comment'>                                             		
                                             		A comment has been <#if args.activityEvent == 'Create'>created<#elseif args.activityEvent == 'Update'>updated<#else>deleted</#if> on <#if (args.deliverableDescription)??>le livrable <b>"${args.deliverableDescription}"</b> <#elseif (args.taskTitle)??>la tâche <b>"${args.taskTitle}"</b> <#else>le projet</#if> :                                              		
                                             		<i>${args.comment.content}</i>
                                             	</#if>                                             	
                                             </p>
                                             
                                             <p><b>"${args.taskTitle}"</b></p>
                                             
                                             <#if (args.taskDescription)??>                                             
                                             	<p>${args.taskDescription}</p>                                             
                                             </#if>                                             
                                             
                                             <#if (args.project)??>
                                                <table cellpadding="0" callspacing="0" border="0" bgcolor="#eeeeee" style="padding:10px; border: 1px solid #aaaaaa;">
                                                   <tr>
                                                      <td>
                                                         <table cellpadding="0" cellspacing="0" border="0">
                                                            <tr>
                                                               <td valign="top">
                                                                  <img src="${shareUrl}/res/components/images/generic-file.png" alt="" width="64" height="64" border="0" style="padding-right: 10px;" />
                                                               </td>
                                                               <td>
                                                                  <table cellpadding="2" cellspacing="0" border="0">
                                                                     <tr>
                                                                        <td><b>${args.project.name}</b></td>
                                                                     </tr>
                                                                    <tr>
                                                                        <td>Click on this link to access the project:</td>
                                                                     </tr>
                                                                     <tr>
                                                                        <td>
                                                                           <a href="${shareUrl}/page/entity-data-lists?list=taskList&nodeRef=${args.project.nodeRef}">
                                                                           ${shareUrl}/page/entity-data-lists?list=taskList&nodeRef=${args.project.nodeRef}</a>
                                                                        </td>
                                                                     </tr>                                                                         
                                                                  </table>
                                                               </td>
                                                            </tr>
                                                         </table>
                                                      </td>
                                                   </tr>
                                                </table>
                                             </#if>
                                             
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