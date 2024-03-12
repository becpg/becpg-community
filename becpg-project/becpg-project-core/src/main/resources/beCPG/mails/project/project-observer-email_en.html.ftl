<html>
   <head>
      <style type="text/css">
      body
      {
         font-family: Arial, sans-serif;
         font-size: 14px;
         color: #4c4c4c;
      }
      
      a
      {
        color: #0f515f;
		text-decoration: none;
		display:flex;
		flex-direction:row;		
      }
      
         
      ul {
       list-style-type: none;
       margin:0px;
       padding-left:0px;
	   list-style-type:none;
	   
      }
      
		
		ul li div.delivrable-status {
			width: 5px;
			display: table-cell !important;
		    padding:0 !important;
		}

	
		 ul li div.delivrable {
			 border: 1px solid rgb(204, 204, 204);
			 font-size: 108%;
		     margin: 5px;
		     width: 90%;
		     min-height: 20px;
		     height: 20px;
		     display: table !important;
		     padding:0 !important;
		     box-shadow: 0.33px 2px 8px rgba(0, 0, 0, 0.1);
			 list-style-type:none !important;

		}
		
		div.delivrable-container {
			 display: table-cell !important;
		     padding: 10px !important;
		}
		
		
		span.doc-file {
			display: inline-table;
		}
		
		div.delivrable-status.delivrable-status-Completed {
			background-color : #0f515f;
		}
		
		div.delivrable-status-Closed   {
			text-decoration:  line-through ;
		}
		
		
		 div.delivrable-status.delivrable-status-Closed, div.delivrable-status.delivrable-status-Planned {
		    background-color : #ff642d;
		 }
		 
		div.delivrable-status.delivrable-status-InProgress {
		    background-color :#0f515f;
		 }
		 button 
        {
        background-color: white ;
         border-radius: 5px;
         border : solid 1px #ff642d;
         color:#ff642d;
         padding: 5px 20px;
         text-align: center;
         text-decoration: none;
         font-size: 13px;
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

	@media (min-width: 660px) {
       
         .table {
            width:70%;
         }
		 .img {
			 padding-right:20px;
		 }
		 p.Stitle {
			 font-size:12px;
		 }
		 p.title {
			 font-size:20px;
      	}
	}

    @media (max-width: 660px) {

        .table {
            width:100%;
         }
		 .img {
			 padding:0px;
			 margin-top:5px
		 }
		 .grid {
			 display:grid;
			 text-align:center;
		 }
		 p.Stitle {
			 font-size:10px;
		 }
		 p.title {
			 margin:0px;
			 padding:0px;
			font-size:18px;
		 }
		 button {
			 width:100%;
		 }
		 .center {
			 text-align:center;
			 margin:auto !important;
		 }
		 
    }


 
      
      </style>
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
                                          <table class="center" cellpadding="0" cellspacing="0" border="0">
                                             <tr class="grid">
												<td >
                                                   <img class="img" src="${shareUrl}/res/components/images/project-email-logo.png" alt="" height="64" border="0"/>
													</td>
													<td>
													<p class="title" style="color: #0f515f; font-weight: bold; margin-bottom:0px;" >${args.project.name}</p>
													<#if (args.project.properties["cm:modifier"])?? && people.getPerson(args.project.properties["cm:modifier"])??>
													<#assign projectModifier = people.getPerson(args.project.properties["cm:modifier"])>
												  	<p class="Stitle" style="color: #ff642d; font-weight: bold; margin-top:1px;">has been updated by ${projectModifier.properties["cm:firstName"]!""} ${projectModifier.properties["cm:lastName"]!""}</p>
													  <#else> 
														<p class="Stitle" style="color: #ff642d; font-weight: bold; margin-top:1px;">has been updated</p>

													</#if>
                                                	<a title="Open the project" href="${shareUrl}/page/entity-data-lists?list=taskList&nodeRef=${args.project.nodeRef}"><button ><b>Open the project</b></button></a>
												</td>                                              
                                             </tr>
                                          </table>
                                          <div style="font-size: 14px; margin: 12px 0px 24px 0px; padding-top: 10px; border-top: 1px solid #aaaaaa;">
					                            <#if (args.project.properties["bcpg:modifiedCatalog1"])??>
		                                            <#assign modifiedCatalog1 = args.project.properties["bcpg:modifiedCatalog1"]>
		                                            <#assign cmModified = args.project.properties["cm:modified"]>
                                                <#if modifiedCatalog1?datetime?iso("UTC") = cmModified?datetime?iso("UTC")>
                                                    <p>Some of the project properties have been changed</p>
                                               </#if>
                                                </#if>
                                             	<#if args.activityType == 'State'>
												 <p>The status of the task changed from <b>${args.beforeState}</b> to 
													<#if args.afterState == 'Refusé'><b style="color:#ff642d">${args.afterState}</b>
														<#elseif (args.afterState == 'En cours' || args.afterState == 'Terminé')><b style="color:#0f515f">${args.afterState}</b>
																<#else><b>${args.afterState}</b>
													</#if>
                                             	 </p>
                                             	<ul>
                                             	 <#if (args.taskTitle)??>                                             
	                                             	<li>Task : <b>${args.taskTitle}</b></li>                                       
	                                             </#if> 
	                                             <#if (args.taskDescription)?? && args.taskDescription != "">                                             
	                                             	<li>Description : ${args.taskDescription}</li>                                       
	                                             </#if> 
	                                             </ul>         
													<#if args.taskComment??>
		                                              <p style="color: #ff642d; font-weight: bold;">Comment :</p>
	                                                        <div class="comment">
	                                                           <p style="margin:0px;font-size:12px;color:grey"><b>${projectModifier.properties["cm:firstName"]!""} ${projectModifier.properties["cm:lastName"]!""}</b></p>
	                                                           <p style="margin:0px; font-style:italic">"${args.taskComment}"</p>
	                                                        </div>
	                                                           <br/>
		                                            </#if>
									      	
									<#if args.task?? &&  args.task.sourceAssociations["pjt:dlTask"]??>
									 										
									    <#assign livrable = "none">

									    <ul>
									      	<#list args.task.sourceAssociations["pjt:dlTask"] as deliverable>
												<#if deliverable?? && deliverable.hasPermission("Read")  && (!deliverable.properties["pjt:dlScriptExecOrder"]?? ||  deliverable.properties["pjt:dlScriptExecOrder"] == "None" )>
														<#if livrable == "none">
														 <#assign livrable = "livrable">
															<p>Deliverables :</p>
														</#if>
													<li>
														<div class="delivrable delivrable-status-${deliverable.properties["pjt:dlState"]!"InProgress"}">
										      			<div class="delivrable-status delivrable-status-${deliverable.properties["pjt:dlState"]!"InProgress"}"></div>
										      				<div class="delivrable-container">
										      						<#if deliverable.properties["pjt:dlUrl"]?? && deliverable.properties["pjt:dlUrl"]!="">
										      							<span class="doc-url"><a title="Follow the link" href="${deliverable.properties["pjt:dlUrl"]}">
										      						   		 <img style="padding:4px" src="${shareUrl}/res/components/images/link-16.png" /><span style="color:#0f515f;margin-left: 2px">&nbsp;${deliverable.properties["pjt:dlDescription"]!""}</span></a>
										      							</span>
										      						<#elseif deliverable.assocs["pjt:dlContent"]?exists>
															   			<#list deliverable.assocs["pjt:dlContent"] as content>
															                  <#if content.hasPermission("Read")>
																                  <#if content.isContainer>
																	                   <span class="doc-file"><a title="Open the document" href="${shareUrl}/page/site/${content.getSiteShortName()!"valid"}/folder-details?nodeRef=${content.nodeRef}">
																		                	<img style="padding:4px" src="${shareUrl}/res/components/images/filetypes/generic-folder-16.png" />&nbsp;<span style="color:#0f515f;margin-left: 2px">${deliverable.properties["pjt:dlDescription"]!""}</span></a>
																		               </span>
																                  <#else>
																                  	 <span class="doc-file"><a title="Open the document" href="${shareUrl}/page/site/${content.getSiteShortName()!"valid"}/document-details?nodeRef=${content.nodeRef}">
																	                	<img style="padding:4px" src="${shareUrl}/res/components/images/filetypes/generic-file-16.png" />&nbsp;<span style="color:#0f515f;margin-left: 2px">${deliverable.properties["pjt:dlDescription"]!""}</span></a>
																	               </span>
																                  </#if>											
																			</#if>
															   			</#list>
														  			<#else>
														  				<span style="color:#0f515f;margin-left: 2px" >${deliverable.properties["pjt:dlDescription"]!""}</span>
													   				</#if>
										      				</div>
											         	</div>						
											      	</li>
												</#if>
									      	</#list>
									  	</ul>  	
									 </#if>     	
                                             	<#elseif args.activityType == 'Comment'>
                                             		<p> A comment has been  <#if args.activityEvent == 'Create'>created<#elseif args.activityEvent == 'Update'>updated<#else>deleted</#if> on <#if args.deliverableDescription??>the deliverable <b>"${args.deliverableDescription}"</b> <#elseif args.taskTitle??>the task <b>"${args.taskTitle}"</b> <#else>the project</#if>: </p>                                             		                                             		         
                                             			<#if  args.comment?? && args.comment.content??> 
			                                                       <div class="comment">${args.comment.content}</div>
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
                                 <div style="border-bottom: 1px solid #a1a8aa; height:5px">&nbsp;</div>
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