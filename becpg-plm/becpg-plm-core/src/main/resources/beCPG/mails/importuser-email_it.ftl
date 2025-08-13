<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
    <title>Conto beCPG</title>
  </head>
  <body bgcolor="#dddddd" style="margin:0; padding:0; font-family: Arial, sans-serif; font-size: 14px; color: #4c4c4c;">
    <table width="100%" cellpadding="20" cellspacing="0" border="0" bgcolor="#dddddd">
      <tr>
        <td align="center">
          <table width="600" cellpadding="0" cellspacing="0" border="0" bgcolor="white" style="border: 1px solid #cccccc; border-radius: 15px;">
            <tr>
              <td style="padding: 20px;">
                <!-- Header -->
                <table width="100%" cellpadding="0" cellspacing="0" border="0">
                  <tr>
                    <td valign="top" width="80">
                      <img src="${shareUrl}/res/components/site-finder/images/project-email-logo.png" alt="" height="64" style="display: block;" />
                    </td>
                    <td valign="middle" style="padding-left: 10px;">
                      <p style="font-size: 20px; color: #004254; font-weight: bold; margin: 0;">Account beCPG</p>
                    </td>
                  </tr>
                </table>

                <!-- Divider -->
                <div style="border-top: 1px solid #aaaaaa; margin: 20px 0;"></div>

                <!-- Greeting -->
                <p style="color:#004254; font-size:18px; font-weight:bold; margin: 0 0 10px 0;">
                  Ciao ${person.properties["cm:firstName"]!""},
                </p>

                <!-- Button CTA -->
                <table cellpadding="0" cellspacing="0" border="0" style="margin: 20px 0;">
                  <tr>
                    <td align="center" bgcolor="#ffffff" style="border: 1px solid #ff642d; border-radius: 5px;">
                      <a href="${shareUrl}" style="display: inline-block; padding: 10px 22px; font-size: 13px; font-weight: bold; color: #ff642d; text-decoration: none;">
                        Accedi al tuo account
                      </a>
                    </td>
                  </tr>
                </table>

                <!-- Account Info -->
                <p style="margin: 10px 0;"><b>Account:</b> ${username}</p>
                <p style="margin: 10px 0;"><b>Password:</b> ${password}</p>

                <!-- Browser advice -->
                <p style="margin: 10px 0;">
                  Ti consigliamo di utilizzare il browser Firefox o Chrome.
                </p>

                <!-- Documentation Links -->
                <p style="margin: 10px 0;">
                  Consulta la nostra documentazione online per iniziare a usare beCPG:
                </p>
                <ul style="padding-left: 20px; margin: 10px 0;">
                  <li><a href="https://docs.becpg.fr/en/tutorial/tutorial.html">Il tutorial per creare un prodotto e la sua composizione</a></li>
                  <li><a href="https://docs.becpg.fr/en/utilization/navigation-becpg.html">La documentazione per usare beCPG</a></li>
                  <li><a href="https://docs.becpg.fr/en/utilization/project-management.html">La documentazione per la gestione dei progetti</a></li>
                </ul>

                <!-- Closing -->
                <p style="margin: 10px 0;">
                  Cordiali saluti,<br />
                  beCPG
                </p>

                <!-- Divider -->
                <div style="border-top: 1px solid #aaaaaa; margin: 20px 0;"></div>

                <!-- Website link -->
                <p style="font-size: 13px; margin: 10px 0;">
                  Per saperne di più su beCPG, visita 
                  <a style="text-decoration: none; color: #EF6236;" href="https://www.becpg.net">https://www.becpg.net</a>
                </p>

                <!-- Bottom divider -->
                <div style="border-bottom: 1px solid #aaaaaa; margin: 20px 0;"></div>

                <!-- Footer Logo -->
                <img src="${mailLogoUrl}" alt="Logo beCPG" style="display: block; padding: 10px 0;" />
              </td>
            </tr>
          </table>
        </td>
      </tr>
    </table>
  </body>
</html>
