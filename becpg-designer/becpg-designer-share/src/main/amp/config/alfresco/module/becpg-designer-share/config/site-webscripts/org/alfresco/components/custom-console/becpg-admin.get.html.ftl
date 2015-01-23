<#assign el=args.htmlid?html>


<@markup id="custom-js" target="js"  action="after">
	<@script type="text/javascript" src="${url.context}/res/components/console/becpg-designer-admin.js" group="becpg-admin" ></@script>
</@>

<@markup id="custom-actions" target="actions"  action="before">
	<div class="action">
		<button type="button" name="${el}-reload-model-button" id="${el}-reload-model-button">${msg("button.reload-model")}</button>
 		<label for="${el}-reload-model-button">${msg("label.reload-model")}</label>              
	</div>
	<div class="action">
		<button type="button" name="${el}-reload-config-button" id="${el}-reload-config-button">${msg("button.reload-config")}</button>
    	<label for="${el}-reload-config-button">${msg("label.reload-config")}</label>             
	</div>
</@>