<#--
 FTL variables you can use to customize template:
  args.workflowTitle
  args.workflowPooled
  args.workflowDueDate
  args.workflowPriority
  args.workflowDocuments
 
Since 3.2.1:

 args.project or args.entity  
 args.projectTask
 
Project entities should be access by iterating  args.workflowDocuments

Example 1 show beCPG Code of entities:

   <#if (args.workflowDocuments)??>
     <#list args.workflowDocuments as doc>
            <#if doc.hasAspect("bcpg:entityListsAspect")>
${doc.properties["bcpg:code"]!"No code"}
   </#if>
</#list>
</#if>

Example 2 show last project comments:

                        <#if (args.project)??>
                        <#if args.project.assocs["pjt:projectCurrentComments"]?exists>
                        <#list args.project.assocs["pjt:projectCurrentComments"] as activity>
                        <#if activity.properties["pjt:alData"]??>
                        <#assign commentNodeRef = (activity.properties["pjt:alData"]?eval).commentNodeRef>
                        <#if commentNodeRef?? >
                        <#assign comment = activity.nodeByReference[commentNodeRef]>
                        <#if comment?? >
                          ${activity.properties["pjt:alUserId"]!"Vide"} : ${comment.content}
                          </#if>
                        </#if>
                        </#if>
                        </#list>
                        </#if>
                        </#if>

<#if (args.projectTask.properties["pjt:tlIsRefused"])??>
   <p class="title" style="font-size: 20px; color: #ff642d; font-weight: bold;">Une tâche a été refusée</p>
       <#else>
         <p class="title" style="font-size: 20px; color: #0f515f; font-weight: bold;">Une tâche vous a été assignée</p>
            </#if>
-->

<html>
   <head>
      <style type="text/css"><!--
     body
      {
         font-family: Arial, sans-serif;
         font-size: 14px;
         color: #4c4c4c;
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
      
       
      --></style>
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
                                                   <img class= "img" src="${shareUrl}/res/components/images/project-email-logo.png" alt="" height="64" border="0"/>
                                                         <p class="title" style="font-size: 20px; color: #004254; font-weight: bold;"> Tarea Adhoc</p>
                                                </td>
                                             </tr>
                                          </table>
                                          <div style="font-size: 14px; margin: 12px 0px 0px 0px; padding-top: 10px; border-top: 1px solid  #a1a8aa;";">
                                             	 <p> Buenos dias,<br />
                                          <b style="color:#ff642d">${person.properties["cm:firstName"]!""} ${person.properties["cm:lastName"]!""}</b> te ha enviado una tarea Adhoc. 
                                          </p>

                                         <p>Atentamente,<br />
                                          beCPG</p>                    
                                            
                                          </div>
                                       </td>
                                    </tr>
                                 </table>
                              </td>
                           </tr>
                           <tr>
                              <td>
                                 <div style = "border-bottom: 1px solid  #a1a8aa;">&nbsp;</div>
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