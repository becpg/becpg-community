
<#assign el=args.htmlid?html>
<script type="text/javascript">//<![CDATA[
   new beCPG.component.beCPGAdminConsole("${el}").setMessages(${messages});
//]]></script>
</script>

<#assign el=args.htmlid?html>

<div id="${el}-body" class="becpg-admin-console">
	<div id="${el}-form">
		<div class="header-bar">
         <div class="title">${msg("label.characts")}</div>
      </div>      
      <div class="section">
      <#list systemEntities as item>
	     		 <div class="action">				
	     		 	<a href="${page.url.context}/page/entity-data-lists?nodeRef=${item.nodeRef}">,${item.name}</a>
		    	</div>
		</#list>
		</div>
     	<div class="header-bar">
         <div class="title">${msg("label.repository")}</div>
      </div>      
      <div class="section">
			<div class="action">				
				<button type="button" name="${el}-reload-model-button" id="${el}-reload-model-button">${msg("button.reload-model")}</button>
	         <label for="${el}-reload-model-button">${msg("label.reload-model")}</label>               
	    	</div>
	    	<div class="action">				
				<button type="button" name="${el}-reload-config-button" id="${el}-reload-config-button">${msg("button.reload-config")}</button>
	         <label for="${el}-reload-config-button">${msg("label.reload-config")}</label>               
	    	</div>
	    	<div class="action">
	    		<button type="button" name="${el}-init-repo-button" id="${el}-init-repo-button">${msg("button.init-repo")}</button>
	    		<label for="${el}-init-repo-button">${msg("label.init-repo")}</label>    		
	    	</div>
	    	<div class="action">
	    		<button type="button" name="${el}-init-acl-button" id="${el}-init-acl-button">${msg("button.init-acl")}</button>
	    		<label for="${el}-init-acl-button">${msg("label.init-acl")}</label>    		
	    	</div>
	    	<div class="action">
	    		<button type="button" name="${el}-empty-cache" id="${el}-empty-cache-button">${msg("button.empty-cache")}</button>
	    		<label for="${el}-empty-cache-button">${msg("label.empty-cache")}</label>    		
	    	</div>
		</div>
    	
	</div>
</div>