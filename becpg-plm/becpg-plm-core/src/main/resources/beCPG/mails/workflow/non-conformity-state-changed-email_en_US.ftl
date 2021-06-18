

<html>
   <head>
      <style type="text/css">
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
         padding: 10px 22px;
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
                                   
                                     <p> Hello,<br />
                                      Non-compliance <b>${nc.properties["bcpg:code"]!""}</b> has been updated by <span style="color:#ff642d; font-weigth:bold">${person.properties["cm:firstName"]!""} ${person.properties["cm:lastName"]!""}</span>.</p>
                                        

                                       <p><b>Description :</b> ${nc.properties["cm:description"]!""}<br />
                                       <b>State :</b> ${nc.properties["qa:ncState"]!""}<br />
                                       <b>Comment :</b> ${ncComment!""}
                                       </p>

                                       <p>Regards,<br />
                                       beCPG</p>
                                         
                                          
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