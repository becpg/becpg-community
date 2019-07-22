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
      
         
      ul {
       list-style-type: none;
       margin:0px;
       padding-left:0px;
      }
      
      ul li div.delivrable {
			 border: 1px solid rgb(204, 204, 204);
			 font-size: 108%;
		     margin: 5px;
		     width: 95%;
		     min-height: 40px;
		     height: 40px;
		     display: table !important;
		     padding:0 !important;
		     box-shadow: 0.33px 2px 8px rgba(0, 0, 0, 0.1);
		}
		
		ul li div.delivrable-status {
			width: 5px;
			display: table-cell !important;
		    padding:0 !important;
		}
		
		div.delivrable-container {
			 display: table-cell !important;
		     padding: 10px !important;
		}
		
		
		span.doc-file {
			display: inline-table;
		}
		
		div.delivrable-status.delivrable-status-Completed {
			background-color : rgb(123, 209, 72);
		}
		
		div.delivrable-status-Closed   {
			text-decoration:  line-through ;
		}
		
		
		 div.delivrable-status.delivrable-status-Closed {
		    background-color : grey;
		 }
		 
		 div.delivrable-status.delivrable-status-Planned, div.delivrable-status.delivrable-status-InProgress {
		    background-color :rgb(73, 134, 231);
		 }

 
      
      --></style>
   </head>
   
    <#assign projectModifier = people.getPerson(args.project.properties["cm:modifier"])>

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
                                                  <h2>Projet <a href="${shareUrl}/page/entity-data-lists?list=taskList&nodeRef=${args.project.nodeRef}">${args.project.name}</a> has been updated by ${projectModifier.properties["cm:firstName"]!""} ${projectModifier.properties["cm:lastName"]!""}.</h2>
                                                </td>                                              
                                             </tr>
                                          </table>
                                          <div style="font-size: 14px; margin: 12px 0px 24px 0px; padding-top: 10px; border-top: 1px solid #aaaaaa;">

                                             	<#if args.activityType == 'State'>
                                             		<p>The state of the task has been changed from <b>${args.beforeState}</b> to <b>${args.afterState}</b>.</p>
                                             		
                                             		<#if  args.taskComment??>
                                             		    <p> Comment: </p>
                                             			<table width="100%" cellpadding="0" callspacing="0" border="0" bgcolor="#eeeeee" style="padding:10px; border: 1px solid #aaaaaa;">
		                                                   <tr>
		                                                      <td>
		                                                        ${args.taskComment}
		                                                      </td>
		                                                   </tr>
		                                                </table>
		                                          
                                             		</#if>

									      	
									      	<#if args.task?? &&  args.task.sourceAssociations["pjt:dlTask"]??>
									      	  		
									      	<p >Deliverables:</p>
									      	
									      	<ul>
									      	<#list args.task.sourceAssociations["pjt:dlTask"] as deliverable>
												<#if deliverable?? && deliverable.hasPermission("Read")  && (!deliverable.properties["pjt:dlScriptExecOrder"]?? || 
														deliverable.properties["pjt:dlScriptExecOrder"] == "None" ) && (!deliverable.properties["pjt:dlUrl"]?? || !deliverable.properties["pjt:dlUrl"]?contains("wizard") )>
													<li>
														<div class="delivrable delivrable-status-${deliverable.properties["pjt:dlState"]!"InProgress"}">
										      			<div class="delivrable-status delivrable-status-${deliverable.properties["pjt:dlState"]!"InProgress"}"></div>
										      				<div class="delivrable-container">
										      						<#if deliverable.properties["pjt:dlUrl"]?? && deliverable.properties["pjt:dlUrl"]!="">
										      							<span class="doc-url"><a title="Suivre le lien" href="${deliverable.properties["pjt:dlUrl"]}">
										      						   		 <img src="${shareUrl}/res/components/images/link-16.png" /><span >&nbsp;${deliverable.properties["pjt:dlDescription"]!""}</span></a>
										      							</span>
										      						<#elseif deliverable.assocs["pjt:dlContent"]?exists>
															   			<#list deliverable.assocs["pjt:dlContent"] as content>
															                  <#if content.hasPermission("Read")>
																                  <#if content.isContainer>
																	                   <span class="doc-file"><a title="Ouvrir le dossier" href="${shareUrl}/page/site/${content.getSiteShortName()!"valid"}/folder-details?nodeRef=${content.nodeRef}">
																		                	<img src="${shareUrl}/res/components/images/filetypes/generic-folder-16.png" />&nbsp;<span >${deliverable.properties["pjt:dlDescription"]!""}</span></a>
																		               </span>
																                  <#else>
																                  	 <span class="doc-file"><a title="Ouvrir le document" href="${shareUrl}/page/site/${content.getSiteShortName()!"valid"}/document-details?nodeRef=${content.nodeRef}">
																	                	<img src="${shareUrl}/res/components/images/filetypes/generic-file-16.png" />&nbsp;<span >${deliverable.properties["pjt:dlDescription"]!""}</span></a>
																	               </span>
																                  </#if>											
																			</#if>
															   			</#list>
														  			<#else>
														  				<span >${deliverable.properties["pjt:dlDescription"]!""}</span>
													   				</#if>
										      			</div>
											         	</div>						
											      	</li>
															                  
																                  
												</#if>
		
									      		
										     </ul>
									      	</#list>
									      	
									  </#if>    	
									      	
									     
                                             		
                                             	<#elseif args.activityType == 'Comment'>
                                             		<p>A comment has been <#if args.activityEvent == 'Create'>created<#elseif args.activityEvent == 'Update'>updated<#else>deleted</#if> on <#if (args.deliverableDescription)??>deliverable <b>"${args.deliverableDescription}"</b> <#elseif (args.taskTitle)??>task <b>"${args.taskTitle}"</b> <#else>project</#if>: </p>                                             		                                             		 
                                             			<#if  args.comment?? && args.comment.content??> 
	                                             		<table width="100%" cellpadding="0" callspacing="0" border="0" bgcolor="#eeeeee" style="padding:10px; border: 1px solid #aaaaaa;">
			                                                   <tr>
			                                                      <td>
			                                                        ${args.comment.content}
			                                                      </td>
			                                                   </tr>
			                                             </table>
		                                             	</#if>
                                             	</#if>                                             	
                                             
                                           
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
                                 <img src="${shareUrl}/themes/default/images/app-logo.png" alt="" width="117"  border="0" />
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