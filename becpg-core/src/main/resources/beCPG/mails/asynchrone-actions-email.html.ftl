
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
                                          <div style="font-size: 14px; margin: 12px 0px 24px 0px; padding-top: 10px; border-top: 1px solid #aaaaaa;">
                                             <p>Hello,</p>

                                             <p>
                                             	<#if args.mailBody??>
                                             		${args.mailBody} terminated with :
                                             	</#if>
                                             	<b>
                                             		<#if args.actionState> Success<#else> Errors</#if>
                                             	</b>, after <b>${args.runTime}</b> seconds.
                                             </p>
                                             
                                             <#if args.url??>
	                                           	 <table cellpadding="2" cellspacing="0" border="0">
	                                                 <tr>
	                                                    <td></td>
	                                                 </tr>
	                                                 <tr>
	                                                    <td>Click on the link to show the result:</td>
	                                                 </tr>
	                                                 <tr>
	                                                 	<a href="${shareUrl}/${args.url}">${shareUrl}/${args.url}</a>
	                                                 </tr>                                                                
												 </table>
											 </#if>
                                                                  
                                             <p>Regardless,<br/>
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