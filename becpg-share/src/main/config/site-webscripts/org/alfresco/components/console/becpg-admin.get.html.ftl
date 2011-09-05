
<#assign el=args.htmlid?html>
<script type="text/javascript">//<![CDATA[
   new beCPG.component.beCPGAdminConsole("${el}").setMessages(${messages});
//]]></script>
</script>

<#assign el=args.htmlid?html>

<div id="${el}-body" class="becpg-admin-console">
	<div id="${el}-form">
     	<div class="header-bar">
         <div class="title"><label for="${el}-repository">${msg("label.repository")}</label></div>
      </div>      
      <div class="section">
			<div class="action">				
				<button type="button" name="${el}-reload-model-button" id="${el}-reload-model-button">${msg("button.reload-model")}</button>
	         <label for="${el}-reload-model">${msg("label.reload-model")}</label>               
	    	</div>
	    	<div class="action">
	    		<button type="button" name="${el}-init-repo-button" id="${el}-init-repo-button">${msg("button.init-repo")}</button>
	    		<label for="${el}-sendmail">${msg("label.init-repo")}</label>    		
	    	</div>
		</div>
    	
	</div>
</div>