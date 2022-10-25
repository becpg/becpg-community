
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
      .comment {
         background-color: #F2F2F2;
         padding:10px;
         border-radius: 15px;
         width:auto;
      }
      .state {
         border-radius:50px;
         margin-left:5px;
         color:white;
         padding:1px 5px;
         font-size:10px;
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
         }
       .img {
            padding-right:0;
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
                                          <table cellpadding="0" cellspacing="0" border="0">
                                             <tr>
                                                <td class="flex">
                                                   <img class= "img" src="${shareUrl}/res/components/images/project-email-logo.png" alt="" height="64" border="0"/>
                                                     <#if args.workflowPooled == true>
                                                            <p class="title" style="font-size: 20px; color: #0f515f; font-weight: bold;">Ny delad uppgift</p>
                                                         <#else>
                                                            <p class="title" style="font-size: 20px; color: #0f515f; font-weight: bold;">Du har tilldelats en uppgift</p>
                                                      </#if>
                                                </td>
                                             </tr>
                                          </table>
                                          <div style="font-size: 14px; margin: 12px 0px 0px 0px; padding-top: 10px; border-top: 1px solid  #a1a8aa;">
                                             <#if (args.workflowDescription)??>                                             
                                             	<p>Uppgift :&nbsp;&nbsp;<b>${args.workflowDescription}</b></p>
                                                </#if>
                                                                                        
                                                <p style="padding-top: 10px">
                                                <#if (args.workflowDueDate)??>Förfallodatum :&nbsp;&nbsp;<b><red>${args.workflowDueDate?date?string.full}</red></b><br></#if>
                                                <#if (args.workflowPriority)??>
                                                   Prioritet :&nbsp;&nbsp;
                                                   <b>
                                                   <#if args.workflowPriority == 3>
                                                      Låg
                                                   <#elseif args.workflowPriority == 2>
                                                      Medel
                                                   <#else>
                                                      Hög
                                                   </#if>
                                                   </b>
                                                </#if>
                                                <br>
                                                   <#if (args.fromPersonFullName)??>                                             
		                                               Tilldelas av :&nbsp;&nbsp;
	                                                   <b>
	                                                   	${args.fromPersonFullName}
	                                                   </b>
	                                             </#if>
                                             </p>  

                                             <a href="${shareUrl}/page/task-edit?taskId=${args.workflowId}"><button ><b>Visa</b></button></a>  
                                             
                                             <div>
                                                <#if (args.projectTask.sourceAssocs["pjt:tlRefusedTaskRef"])??>
		                                          <#list args.projectTask.sourceAssocs["pjt:tlRefusedTaskRef"] as item>
			                                          <#if item.properties["pjt:tlState"]=="Refused">
			                                          	<#assign hasRefused = true>
			                                          	<#break>
			                                          </#if>
		                                          </#list>
                                              </#if>
                                              
                                                <#if hasRefused??>
                                                   <h3 style="color:#EC6535">Nekade uppgifter och kommentarer</h3>
                                                      <#list args.projectTask.sourceAssocs["pjt:tlRefusedTaskRef"] as item>
                                                      <p style="margin-bottom:8px">
                                                      <b>${item.properties["pjt:tlTaskName"]}</b>
                                                         <#if item.properties["pjt:tlState"]=="Refused">
                                                         <span class="state" style="background-color:#EC6535">vägrade</span>
                                                         <#elseif item.properties["pjt:tlState"]=="Cancelled">
                                                         <span class="state" style="background-color:#FF0000">inställt</span>
                                                         <#elseif item.properties["pjt:tlState"]=="Completed">
                                                         <span class="state" style="background-color:#0F4454">avslutad</span>
                                                          <#elseif item.properties["pjt:tlState"]=="Planned">
                                                         <span class="state" style="background-color:#708090">planerad</span>
                                                         <#elseif item.properties["pjt:tlState"]=="OnHold">
                                                         <span class="state" style="background-color:#0000CD">placerad i kö</span>
                                                         </p>
                                                         </#if>
                                                         
                                                            <#if item.properties["pjt:tlTaskComment"]??>
                                                            <div class="comment">
                                                               <#assign personModifier = item.properties["cm:modifier"]>
                                                               <#if item.assocs["pjt:tlResources"]??>
                                                               <#list item.assocs["pjt:tlResources"] as person>
                                                               <#assign firstName = person.properties["cm:firstName"]>
                                                               <#assign lastName = person.properties["cm:lastName"]>
                                                               <#assign userNameRessouces = person.properties["cm:userName"]>
                                                               <#if personModifier == userNameRessouces>
                                                               <p style="margin:0px;font-size:12px;color:grey"><b>${firstName} ${lastName}</b></p>
                                                               </#if>
                                                               </#list>
                                                               <p style="margin:0px; font-style:italic">"${item.properties["pjt:tlTaskComment"]}"</p>
                                                               </#if>
                                                            </div>
                                                               <br/>
                                                </#if>
                                                      </#list>
                                                </#if>
                                             </div>

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
