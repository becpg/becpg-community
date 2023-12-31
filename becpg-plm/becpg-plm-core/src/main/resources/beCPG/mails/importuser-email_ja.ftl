
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
                                                  <p class="title" style="font-size: 20px; color: #004254; font-weight: bold;">アカウント beCPG</p>
   
                                                </td>
                                             </tr>
                                          </table>
                                          <div style="font-size: 14px; margin: 12px 0px 24px 0px; padding-top: 10px; border-top: 1px solid #aaaaaa;">
                                             <p style="color:#004254; margin-bottom:0px; font-size:18px;font-weight:bold">こんにちは ${person.properties["cm:firstName"]!""},</p>
                                             
                                             <p style="margin-top:0px">
															<br /><a href="${shareUrl}""><button ><b>お客様のアカウントへのアクセス</b></button></a>
															<br /><b>アカウント : </b>${username}
															<br /><b>パスワード : </b>${password}</p>
															
															<p>FirefoxまたはChromeのブラウザを使用することをお勧めします。<br />															
															</p>
															
															<p>					
															beCPGの使用を開始するには、オンラインドキュメントを参照してください。<br />
															- <a href="https://docs.becpg.fr/en/tutorial/tutorial.html">製品とその構成を作成するためのチュートリアル</a><br />
															- <a href="https://docs.becpg.fr/en/utilization/navigation-becpg.html">beCPGを使用するためのドキュメント</a> <br />
															- <a href="https://docs.becpg.fr/en/utilization/project-management.html">プロジェクト管理のドキュメント</a>
															</p>
															
                                          
                                            <p>敬具<br />
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
                                 beCPGの詳細については、以下をご覧ください。 <a style="text-decoration:none; color:#EF6236" href="https://www.becpg.net">https://www.becpg.net</a>
                              </td>
                           </tr>
                           <tr>
                              <td>
                                 <div style="border-bottom: 1px solid #aaaaaa;">&nbsp;</div>
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