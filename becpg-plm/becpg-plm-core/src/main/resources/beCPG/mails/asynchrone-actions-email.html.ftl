
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
                                             <p>Bonjour,</p>

                                             <p>
                                             	<#if args.actionType == 'Simulate'>
                                             		<#assign viewType = "folder-details?">
                                             		La simulation des entités 
                                             	</#if>
                                             	<#if args.actionType == 'Reports'>
                                             		La génération des rapports 
                                             	</#if>
                                             	<#if args.actionType == 'OM'>
                                             		
                                             		<#if args.apply>L'application de l'ordre de modification 
                                             			<#assign viewType = "entity-data-lists?list=changeUnitList&">
                                             		<#else>
                                             			<#assign viewType = "entity-data-lists?list=calculatedCharactList&">
                                             			La simulation de l'ordre de modification </#if> 
                                             	</#if>  
												s'est terminée avec :
                                             	<b><#if args.actionState> Succès<#else> Erreur</#if></b>, après <b>${args.runTime}</b> secondes.
                                             </p>
                                             <#if args.destination??>
	                                           	 <table cellpadding="2" cellspacing="0" border="0">
	                                                 <tr>
	                                                    <td></td>
	                                                 </tr>
	                                                 <tr>
	                                                    <td>Cliquez sur le lien pour accéder au résultat:</td>
	                                                 </tr>
                                                 	 <#if args.path??>
	                                                 	<tr>
	                                                       <a href="${shareUrl}/page/repository#filter=path|${args.path}">
	                                                       ${shareUrl}/page/repository#filter=path|${args.path}</a>
	                                                    </tr>
                                                     <#else>
		                                                 <tr>	
	                                                       	<a href="${shareUrl}/page/${viewType}nodeRef=${args.destination.nodeRef}">
	                                                       	${shareUrl}/page/folder-details?nodeRef=${args.destination.nodeRef}</a>
		                                                 </tr>
	                                                 </#if>                                                                         
												 </table>
											 </#if>
                                                                  
                                             <p>Cordialement,<br/>
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