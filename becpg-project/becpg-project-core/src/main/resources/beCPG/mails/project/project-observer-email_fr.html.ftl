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
       padding-left:10px;
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
   
    <#assign currentPerson = people.getPerson(args.project.properties["cm:modifier"])>

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
                                                  <h2>Le projet <a href="${shareUrl}/page/entity-data-lists?list=taskList&nodeRef=${args.project.nodeRef}">${args.project.name}</a> a été mis à jour par ${currentPerson.properties["cm:firstName"]!""} ${currentPerson.properties["cm:lastName"]!""}.</h2>
                                                </td>                                              
                                             </tr>
                                          </table>
                                          <div style="font-size: 14px; margin: 12px 0px 24px 0px; padding-top: 10px; border-top: 1px solid #aaaaaa;">

                                             	<#if args.activityType == 'State'>
                                             		<p>L'état de la tâche a été changé de <b>${args.beforeState}</b> à <b>${args.afterState}</b>.</p>
                                             		
                                             		<#if  args.taskComment??>
                                             		    <p> Commentaire: </p>
                                             			<table width="100%" cellpadding="0" callspacing="0" border="0" bgcolor="#eeeeee" style="padding:10px; border: 1px solid #aaaaaa;">
		                                                   <tr>
		                                                      <td>
		                                                        ${args.taskComment}
		                                                      </td>
		                                                   </tr>
		                                                </table>
		                                          
                                             		</#if>
                                             		
                                             		
									      	<p >Livrables:</p>
									      	
									      	<ul><li>
									      		<div class="delivrable delivrable-status-InProgress">
										      			<div class="delivrable-status delivrable-status-InProgress"></div>
										      				<div class="delivrable-container">
										      						<span >Livrable 1</span>
										      			</div>
										      	</div>						
										      	</li>
										     </ul>
				      						
                                             		
                                             		
                                             	<#elseif args.activityType == 'Comment'>
                                             		<p> Un commentaire a été  <#if args.activityEvent == 'Create'>créé<#elseif args.activityEvent == 'Update'>mis à jour<#else>supprimé</#if> sur <#if args.deliverableDescription??>le livrable <b>"${args.deliverableDescription}"</b> <#elseif args.taskTitle??>la tâche <b>"${args.taskTitle}"</b> <#else>le projet</#if> : </p>                                             		                                             		 
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