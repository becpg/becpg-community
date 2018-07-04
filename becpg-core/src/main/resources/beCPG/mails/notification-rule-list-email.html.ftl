<html>
	<head>      
		<style type="text/css">
			<!--      body      {         font-family: Arial, sans-serif;         font-size: 14px;         color: #4c4c4c;      }      a, a:visited      {         color: #0072cf;      }      -->
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
		</style>   
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
                                       <#assign notification=args.notification>
                                       <#assign dateField=notification.properties["bcpg:nrDateField"]>
                                          <table cellpadding="0" cellspacing="0" border="0">
                                             <tr>
                                                <td>
                                                   <img src="${shareUrl}/res/components/images/page-64.png" alt="" width="64" height="64" border="0" style="padding-right: 20px;" />
                                                </td>
                                                <td>
                                                   <div style="color: #000000; font-size: 22px; padding-bottom: 4px;">
                                                   			${notification.properties["bcpg:nrSubject"]}
                                                   </div>
                                                   <div style="font-size: 13px;">
                                                   </div>
                                                </td>
                                             </tr>
                                          </table>
                                          <div style="font-size: 14px; margin: 12px 0px 24px 0px; padding-top: 10px; border-top: 1px solid #aaaaaa;">
                                             <p>Bonjour,</p>
                                             
                                             <p>
                                             	Veuillez trouver ci-dessous les objets concernés: 
                                              </p>
                                            <#if args.entities?size != 0>
                                             <table cellpadding="0" cellspacing="0" style="border:solid 1px black;padding: 0px;" >
                                               <tr style="background-color: #009dcc;">
                                                 <th>Dossier</th>
                                                 <th class="becpg_cellBorderLeft">Objet</th>
                                                 <#if args.versions??>
                                                 <th class="becpg_cellBorderLeft">Date de création</th>
                                                 <th class="becpg_cellBorderLeft">Verion</th>
                                                 <th class="becpg_cellBorderLeft">Créateur</th>
                                                 <th class="becpg_cellBorderLeft">Commentaire</th>
                                                 <#else>
                                                 <th class="becpg_cellBorderLeft">${args.dateField}</th>
                                                 </#if>
                                               </tr>
                                               
                                               <#list args.entities as item> 
                                               		<#assign node=item.node/>
                                               		<#if args.versions??>
	                                               		<#list args.versions[node.nodeRef]?keys as key >
	                                               			<#assign version=args.versions[node.nodeRef][key]>
															<tr> 
																<td class="becpg_rowBorderTop"> ${node.parent.name} </td>
																<td class="becpg_rowBorderTopLeftRight">
																	<a href="${shareUrl}/page/<#if node.siteShortName??>site/${node.siteShortName}/</#if><#if item.isEntityV2SubType>entity-data-lists<#else>document-details</#if>?nodeRef=${node.nodeRef}">${node.name}</a>
																</td>
																<td class="becpg_rowBorderTopLeftRight">${version.properties["cm:created"]?date}</td>
																<td class="becpg_rowBorderTopLeftRight">${version.properties["cm:versionLabel"]}</td>
																<td class="becpg_rowBorderTopLeftRight">${version.properties["cm:creator"]}</td>
																<td class="becpg_rowBorderTopLeftRight">${key?split("|")[1]}</td>
															</tr>
														</#list>
													<#else>
														<tr> 
															<td class="becpg_rowBorderTop"> ${node.parent.name} </td>
															<td class="becpg_rowBorderTopLeftRight">
																<a href="${shareUrl}/page/<#if node.siteShortName??>site/${node.siteShortName}/</#if><#if item.isEntityV2SubType>entity-data-lists<#else>document-details</#if>?nodeRef=${node.nodeRef}">${node.name}</a>
															</td>
	                                               			<td class="becpg_rowBorderTopLeftRight">${node.properties[dateField]?date}</td>
	                                               		</tr>	
													</#if>
														
                                               		
												</#list>
                                             </table>
                                             <#else>
                                             <p><b> Aucun élément trouvé<b></p>
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