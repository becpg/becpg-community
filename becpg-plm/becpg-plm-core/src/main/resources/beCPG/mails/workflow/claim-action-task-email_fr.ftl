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
            align-items:center;
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
                                                   <img class= "img" src="${shareUrl}/res/components/images/project-email-logo.png" alt=""  height="64" border="0"/>
                                               <div>
                                                      <#if args.workflowPooled == true>
                                                         <p class="title" style="margin-bottom:0px;margin-top:5px;font-size: 20px; color: #0f515f; font-weight: bold;">Nouvelle tâche partagée</p>
                                                      <#else>
                                                            <p class="title" style="margin:0px;font-size: 20px; color: #0f515f; font-weight: bold;">Une tâche vous a été assignée</p>
                                                      </#if>
                                                 
                                                   <p style="margin-top:0px;font-size: 13px;">
                                                      ${date?datetime?string.full}
                                                   </p>
                                                   </div>
                                                </td>
                                             </tr>
                                          </table>
                                          <div style="font-size: 14px; margin: 12px 0px 0px 0px; padding-top: 10px; border-top: 1px solid #aaaaaa;">
                                             <p>Bonjour,</p>

                                             <p>
                                                <#if args.workflowPooled == true>
                                                   La tâche partagée suivante peut être récupérée :
                                                <#else>
                                                    La tâche suivante vous a été assignée :
                                                </#if>
                                             </p>
                                             
                                             <#if args.workflowTitle??>
	                                           	 <p><b>"${args.workflowTitle}"</b></p>
	                                         </#if>
                                             
                                             <#if (args.workflowDescription)??>                                             
                                             	<p>${args.workflowDescription}</p>                                             
                                             </#if>
                                            
                                             <p>
                                                <#if (args.workflowDueDate)??>Due:&nbsp;&nbsp;<b>${args.workflowDueDate?date?string.full}</b><br></#if>               
                                             </p>
	                                                                                          
                                             <a href="${shareUrl}/page/task-edit?taskId=${args.workflowId}"><button ><b>Afficher</b></button></a>                       

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
                                 <img style="padding :10px 0px" src="${shareUrl}/res/components/images/becpg-footer-logo.png" />
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