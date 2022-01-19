
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
                                          <table width="100%" cellpadding="0" cellspacing="0" border="0">
                                             <tr>
                                                <td class ="flex">
                                                   <img class="img" src="${shareUrl}/res/components/site-finder/images/project-email-logo.png" alt="" height="64" border="0" />
                                                  <p class="title" style="font-size: 20px; color: #004254; font-weight: bold;">Аккаунт beCPG</p>
   
                                                </td>
                                             </tr>
                                          </table>
                                          <div style="font-size: 14px; margin: 12px 0px 24px 0px; padding-top: 10px; border-top: 1px solid #aaaaaa;">
                                             <p style="color:#004254; margin-bottom:0px; font-size:18px;font-weight:bold">Привет ${person.properties["cm:firstName"]!""},</p>
                                             
                                             <p style="margin-top:0px">
															<br /><a href="${shareUrl}""><button ><b>Войдите в свой аккаунт</b></button></a>
															<br /><b>Счет : </b>${username}
															<br /><b>Пароль : </b>${password}</p>
															
															<p>Советую использовать браузер Firefox или Chrome.<br />															
															</p>
															
															<p>					
															Ознакомьтесь с нашей онлайн-документацией, чтобы начать использовать beCPG:<br />
															- <a href="https://docs.becpg.fr/en/tutorial/tutorial.html">Учебник по созданию продукта и его состава</a><br />
															- <a href="https://docs.becpg.fr/en/utilization/navigation-becpg.html">Документация по использованию beCPG</a> <br />
															- <a href="https://docs.becpg.fr/en/utilization/project-management.html">Документация по управлению проектами</a>
															</p>
                                          
                                            <p>сердечно,<br />
                                             beCPG</p>
                                          </div>
                                       </td> 
                                    </tr>
                                 </table>
                              </td>
                           </tr>
                           <tr>
                              <td>
                                 <div style="border-top: 1px solid #aaaaaa;">&nbsp;</div>
                              </td>
                           </tr>
                           <tr>
                              <td style="padding: 0px 30px; font-size: 13px;">
                                 Чтобы узнать больше о beCPG, посетите <a style="text-decoration:none; color:#EF6236" href="https://www.becpg.net">https://www.becpg.net</a>
                              </td>
                           </tr>
                           <tr>
                              <td>
                                 <div style="border-bottom: 1px solid #aaaaaa;">&nbsp;</div>
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