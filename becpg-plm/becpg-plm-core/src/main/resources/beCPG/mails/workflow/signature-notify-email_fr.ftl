
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
                                                    <p class="title" style="font-size: 20px; color: #0f515f; font-weight: bold;">Signature du document terminée</p>
                                                </td>
                                             </tr>
                                          </table>
                                          <div style="font-size: 14px; margin: 12px 0px 0px 0px; padding-top: 10px; border-top: 1px solid  #a1a8aa;">
                                             <#if (args.documentName)??>                                             
                                             	<p>Document signé :&nbsp;&nbsp;<b>${args.documentName}</b></p>
                                                </#if>
                                                                                        
                                                <p style="padding-top: 10px">
                                                <br>
                                             </p>  

                                             <a href="${shareUrl}/s/${args.shareId}"><button ><b>Afficher</b></button></a>  
                                             
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
