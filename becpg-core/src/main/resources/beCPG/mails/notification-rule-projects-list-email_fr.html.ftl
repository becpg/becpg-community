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
                                             <p>Bonjour,</p>
                                             
                                             <p>
                                             	Veuillez trouver ci-dessous les objets concernés: 
                                              </p>
                                            <#if args.entities?size != 0>
                                             <table cellpadding="0" cellspacing="0" style="border:solid 1px black;padding: 0px;" >
                                               <tr style="background-color: #004254;color:white">
                                                 <#--  <th>Folder</th>  -->
                                                 <th>Nom</th>
                                                 <th class="becpg_cellBorderLeft" style=" border-left: solid 1px black;border-collapse:collapse;border-spacing:1px;padding: 2px;">Famille</th>
                                                 <th class="becpg_cellBorderLeft" style=" border-left: solid 1px black;border-collapse:collapse;border-spacing:1px;padding: 2px;">Priorité</th>
                                                 <th class="becpg_cellBorderLeft" style=" border-left: solid 1px black;border-collapse:collapse;border-spacing:1px;padding: 2px;">Etat du projet</th>
                                                 <th class="becpg_cellBorderLeft" style=" border-left: solid 1px black;border-collapse:collapse;border-spacing:1px;padding: 2px;">Chef de projet</th>
                                                 <th class="becpg_cellBorderLeft" style=" border-left: solid 1px black;border-collapse:collapse;border-spacing:1px;padding: 2px;">Description</th>
                                                 <th class="becpg_cellBorderLeft" style=" border-left: solid 1px black;border-collapse:collapse;border-spacing:1px;padding: 2px;">Date de début</th>
                                                 <th class="becpg_cellBorderLeft" style=" border-left: solid 1px black;border-collapse:collapse;border-spacing:1px;padding: 2px;">Date d'achèvement</th>
                                                 <th class="becpg_cellBorderLeft" style=" border-left: solid 1px black;border-collapse:collapse;border-spacing:1px;padding: 2px;">Durée(j)</th>
                                                 <th class="becpg_cellBorderLeft" style=" border-left: solid 1px black;border-collapse:collapse;border-spacing:1px;padding: 2px;">Retard(j)</th>
                                                 <th class="becpg_cellBorderLeft" style=" border-left: solid 1px black;border-collapse:collapse;border-spacing:1px;padding: 2px;">Note(%)</th>
                                                 <th class="becpg_cellBorderLeft" style=" border-left: solid 1px black;border-collapse:collapse;border-spacing:1px;padding: 2px;">Réalisé(%)</th>
                                                 
                                                 <#if args.versions??>
                                                 <th class="becpg_cellBorderLeft" style=" border-left: solid 1px black;border-collapse:collapse;border-spacing:1px;padding: 2px;">Date de création</th>
                                                 <th class="becpg_cellBorderLeft" style=" border-left: solid 1px black;border-collapse:collapse;border-spacing:1px;padding: 2px;">Version</th>
                                                 <th class="becpg_cellBorderLeft" style=" border-left: solid 1px black;border-collapse:collapse;border-spacing:1px;padding: 2px;">Créateur</th>
                                                 <th class="becpg_cellBorderLeft" style=" border-left: solid 1px black;border-collapse:collapse;border-spacing:1px;padding: 2px;">Commentaire</th>
                                                 <#--  <#else>
                                                 <th class="becpg_cellBorderLeft">${args.dateField}</th>  -->
                                                 
                                                 </#if>
                                               </tr>
                                               
                                               <#list args.entities as item> 
                                               		<#assign node=item.node/>
                                               		<#if args.versions??>
	                                               		<#list args.versions[node.nodeRef]?keys as key >
	                                               			<#assign version=args.versions[node.nodeRef][key]>
															<tr> 
																<td class="becpg_rowBorderTop" style="border-top: solid 1px black;padding: 1px 5px 1px 5px;"> ${node.parent.name} </td>
																<td class="becpg_rowBorderTopLeftRight" style="border-top: solid 1px black;border-left: solid 1px black;padding: 1px 5px 1px 5px;">
																	<a href="${shareUrl}/page/<#if node.siteShortName??>site/${node.siteShortName}/</#if><#if item.isEntityV2SubType>entity-data-lists<#else>document-details</#if>?nodeRef=${node.nodeRef}">${(item.displayName)!node.name}</a>
																</td>
																<td class="becpg_rowBorderTopLeftRight" style="border-top: solid 1px black;border-left: solid 1px black;padding: 1px 5px 1px 5px;">${version.properties["cm:created"]?date}</td>
																<td class="becpg_rowBorderTopLeftRight" style="border-top: solid 1px black;border-left: solid 1px black;padding: 1px 5px 1px 5px;">${version.properties["cm:versionLabel"]}</td>
																<td class="becpg_rowBorderTopLeftRight" style="border-top: solid 1px black;border-left: solid 1px black;padding: 1px 5px 1px 5px;">${version.properties["cm:creator"]}</td>
																<td class="becpg_rowBorderTopLeftRight" style="border-top: solid 1px black;border-left: solid 1px black;padding: 1px 5px 1px 5px;">${key?split("|")[1]}</td>
															</tr>
														</#list>
													<#else>
														<tr> 
															<#--  <td class="becpg_rowBorderTop"> ${node.parent.name} </td>  -->
															<td class="becpg_rowBorderTop" style="border-top: solid 1px black;padding: 1px 5px 1px 5px;">
																<a href="${shareUrl}/page/<#if node.siteShortName??>site/${node.siteShortName}/</#if><#if item.isEntityV2SubType>entity-data-lists<#else>document-details</#if>?nodeRef=${node.nodeRef}">${(item.displayName)!node.name}</a>
															</td>
                                             <td class="becpg_rowBorderTopLeftRight" style="border-top: solid 1px black;border-left: solid 1px black;padding: 1px 5px 1px 5px;">
                                                <#if (node.properties["pjt:projectHierarchy1"])??>
                                                <#assign famille = node.properties["pjt:projectHierarchy1"]>
                                                   ${famille.properties["bcpg:lkvValue"]}
                                                </#if>
                                             </td>
                                             <td class="becpg_rowBorderTopLeftRight" style="border-top: solid 1px black;border-left: solid 1px black;padding: 1px 5px 1px 5px;">
                                                <#if (node.properties["pjt:projectPriority"])??>
                                                   <#if node.properties["pjt:projectPriority"] == 3>
                                                      Basse
                                                   <#elseif node.properties["pjt:projectPriority"] == 2>
                                                      Moyenne
                                                   <#else>
                                                      Elevée
                                                   </#if>
                                                </#if>
                                             </td>
                                             <td class="becpg_rowBorderTopLeftRight" style="border-top: solid 1px black;border-left: solid 1px black;padding: 1px 5px 1px 5px;">
                                                <#if (node.properties["pjt:projectState"])??>
                                                      ${node.properties["pjt:projectState"]}
                                                </#if>
                                             </td>
                                             <td class="becpg_rowBorderTopLeftRight" style="border-top: solid 1px black;border-left: solid 1px black;padding: 1px 5px 1px 5px;">
                                                <#if (node.properties["cm:creator"])??>
                                                   ${node.properties["cm:creator"]}
                                                </#if>
                                             </td>
                                             <td class="becpg_rowBorderTopLeftRight" style="border-top: solid 1px black;border-left: solid 1px black;padding: 1px 5px 1px 5px;">
                                                <#if node.properties["cm:description"]??>
                                                   ${node.properties["cm:description"]}
                                                </#if>
                                             </td>
                                             <td class="becpg_rowBorderTopLeftRight" style="border-top: solid 1px black;border-left: solid 1px black;padding: 1px 5px 1px 5px;">
                                                <#if (node.properties["pjt:projectStartDate"])??>
                                                   ${node.properties["pjt:projectStartDate"]?date?string.short}
                                                </#if>
                                             </td>
                                             <td class="becpg_rowBorderTopLeftRight" style="border-top: solid 1px black;border-left: solid 1px black;padding: 1px 5px 1px 5px;">
                                                <#if (node.properties["pjt:projectCompletionDate"])??>
                                                   ${node.properties["pjt:projectCompletionDate"]?date?string.short}
                                                </#if>
                                             </td>
                                             <td class="becpg_rowBorderTopLeftRight" style="border-top: solid 1px black;border-left: solid 1px black;padding: 1px 5px 1px 5px;">
                                                <#if node.properties["pjt:projectCompletionDate"]?? && (node.properties["pjt:projectStartDate"])??>
                                                   <#assign num1 = node.properties["pjt:projectStartDate"]?date?long>
                                                   <#assign num2 = node.properties["pjt:projectCompletionDate"]?date?long>
                                                   ${((num2 - num1)/(1000 * 60 * 60 * 24))?round}
                                                </#if>
                                             </td>
                                                <#if (node.properties["pjt:projectOverdue"])??>
                                                   <#assign overdue = node.properties["pjt:projectOverdue"]>
                                                   <#if overdue <= 0>
                                                         <#assign color = "#2ECC71">
                                                   <#else>
                                                         <#assign color = "#E74C3C">
                                                   </#if>
                                                   <td class="becpg_rowBorderTopLeftRight" style="background-color: ${color};border-top: solid 1px black;border-left: solid 1px black;padding: 1px 5px 1px 5px;">${overdue}</td>
                                                </#if>
                                             <td class="becpg_rowBorderTopLeftRight" style="border-top: solid 1px black;border-left: solid 1px black;padding: 1px 5px 1px 5px;">
                                                <#if (node.properties["pjt:projectScore"])??>
                                                   ${node.properties["pjt:projectScore"]}
                                                </#if>
                                             </td>
                                             <td class="becpg_rowBorderTopLeftRight" style="border-top: solid 1px black;border-left: solid 1px black;padding: 1px 5px 1px 5px;">
                                                <#if (node.properties["pjt:completionPercent"])??>
                                                      ${node.properties["pjt:completionPercent"]}
                                                </#if>
                                             </td>
	                                          <#--  <td class="becpg_rowBorderTopLeftRight">${node.properties[dateField]?date}</td>   -->
                                                     
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