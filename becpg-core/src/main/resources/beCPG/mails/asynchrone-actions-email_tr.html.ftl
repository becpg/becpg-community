<html>

<head>
   <style type="text/css"> 
      
      body {
         font-family: Arial, sans-serif;
         font-size: 14px;
         color: #4c4c4c;
      }

      a, a:visited {
         color: #0072cf;
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
      .img {
            padding-right:20px;
      }
      .line {
         height:4px;
         width:30px;
         background-color:#ff642d;
         margin:4px 0px 14px 0px;
         border-radius:5px;
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
         button {
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
                              <table width="100%" cellpadding="0" cellspacing="0" border="0">
                                <p>Merhaba,</p>

                                             <p>
                                             	<#if args.mailBody??>
                                             		${args.mailBody} ile bitti :
                                             	</#if>
                                             	<b>
                                             		<#if args.actionState> başarı<#else> hata</#if>
                                             	</b>, sonra <#if args.runTime < 60 ><b>${args.runTime?round}</b> saniye.
                                             				<#else><b>${(args.runTime/60)?round}</b> dakika.</#if>
                                             </p>
                                             
                                             <#if args.url??>
	                                           	 <table cellpadding="2" cellspacing="0" border="0">
	                                                 <tr>
	                                                    <td></td>
	                                                 </tr>
	                                                 <tr>
	                                                    <td>Sonuca erişmek için bağlantıya tıklayın:</td>
	                                                 </tr>
	                                                 <tr>
	                                                 	<a href="${shareUrl}/${args.url}">${shareUrl}/${args.url}</a>
	                                                 </tr>                                                                
												 </table>
											 </#if>
                                                                  
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
                                 src="${shareUrl}/res/components/images/becpg-footer-logo.png" />
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