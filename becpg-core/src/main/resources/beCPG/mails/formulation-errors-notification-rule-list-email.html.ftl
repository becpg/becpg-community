<html>

<head>
   <style type="text/css"> 
      .becpg_cellBorderLeft {
		  border-left: solid 1px black;
		  border-collapse:collapse;
		  border-spacing:1px;
		  padding: 2px;
		}
		.becpg_rowBorderTopLeftRight {
		   border-top: solid 1px black;
		   border-left: solid 1px black;
		   padding: 1px 5px 1px 5px;
		}
		.becpg_rowBorderTop {
		   border-top: solid 1px black;
		   padding: 1px 5px 1px 5px;
		}
      body {
         font-family: Arial, sans-serif;
         font-size: 14px;
         color: #4c4c4c;
      }

      a, a:visited {
         color: #0072cf;
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
         
      
   </style>
</head>

<body bgcolor="#dddddd">
   <table width="100%" cellpadding="20" cellspacing="0" border="0" bgcolor="#dddddd">
      <tr>
         <td width="100%" align="center">
            <table class="table" cellpadding="0" cellspacing="0" bgcolor="white"
               style="background-color: white; border: 1px solid #cccccc; border-radius: 15px;">
               <tr>
                  <td width="100%">
                     <table width="100%" cellpadding="0" cellspacing="0" border="0">
                        <tr>
                           <td style="padding: 10px 30px 0px;">
                              <table cellpadding="0" cellspacing="0" border="0">
                                            <tr>
                                       <td>
                                       <#assign notification=args.notification>
                                       <#assign dateField=notification.properties["bcpg:nrDateField"]>
                                          <table width="100%" cellpadding="0" cellspacing="0" border="0">
                                             <tr>
                                                <td class="flex">
                                                   <img class="img" src="${shareUrl}/res/components/images/project-email-logo.png" alt="" height="64" border="0" />
                                                   <p class="title" style="font-size: 20px; color: #004254; font-weight: bold;">${notification.properties["bcpg:nrSubject"]}</p>
                                                </td>
                                             </tr>
                                          </table>
                                          <div style="font-size: 14px; margin: 12px 0px 24px 0px; padding-top: 10px; border-top: 1px solid #aaaaaa;">
                                             <p>Hello,</p>
                                             
                                             <p>
                                             	Please find the affected objects below: 
                                              </p>
                                            <#if args.entities?size != 0>
                                             <table cellpadding="0" cellspacing="0" style="border:solid 1px black;padding: 0px;" >
                                               <tr style="background-color: #004254;color:white">
                                                 <th>Object</th>
                                                 <th class="becpg_cellBorderLeft">Error message</th>
                                                 <th class="becpg_cellBorderLeft">Date</th>
                                               </tr>
                                               
                                               <#list args.entities as item> 
                                               		<#assign node=item.node/>
                                               		<#assign entityNode=node.parent.parent.parent/>
													<tr> 
														<td class="becpg_rowBorderTop">
															<a href="${shareUrl}/page/<#if entityNode.siteShortName??>site/${entityNode.siteShortName}/</#if>entity-data-lists?nodeRef=${entityNode.nodeRef}">${entityNode.name}</a>
														</td>
														<td class="becpg_rowBorderTopLeftRight"><#if node.properties["bcpg:rclReqMessage"]??>${node.properties["bcpg:rclReqMessage"]}</#if></td>
														<td class="becpg_rowBorderTopLeftRight"><#if node.properties[dateField]??>${node.properties[dateField]?date}</#if></td>
                                               		</tr>	
												</#list>
                                             </table>
                                             <#else>
                                             <p><b> No element found<b></p>
                                             </#if>
                                             
                                             <p>Regards,<br/>
                                             beCPG</p>
                                             
                                          </div>
                                       </td>
                                    </tr>
                                 </table>
                           </td>
                        </tr>
                        <tr>
                           <td>
                              <div style="border-bottom: 1px solid #a1a8aa;">&nbsp;</div>
                           </td>
                        </tr>
                        <tr>
                           <td style="padding: 10px 30px;">
                              <img style="padding :10px 0px"
                                 src="${mailLogoUrl}" />
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