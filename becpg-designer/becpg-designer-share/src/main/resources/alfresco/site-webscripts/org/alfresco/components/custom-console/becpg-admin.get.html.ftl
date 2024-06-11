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
		<button type="button" name="${el}-clean-config-button" id="${el}-clean-config-button">${msg("button.clean-config")}</button>
 		<label for="${el}-clean-config-button">${msg("label.clean-config")}</label>              
	</div>
</@>