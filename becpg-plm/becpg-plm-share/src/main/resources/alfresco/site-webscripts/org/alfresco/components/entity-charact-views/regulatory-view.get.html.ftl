<@markup id="css" >
	<@link href="${url.context}/res/components/entity-charact-views/regulatory-view.css" group="entity-datalists" />
</@>

<@markup id="js">
	<#-- Product toolbar actions, among which the AI suggestion button -->
	<@script src="${url.context}/res/modules/custom-entity-datagrid/product-entity-toolbar.js" group="entity-datalists"/>
	<@script src="${url.context}/res/components/entity-charact-views/regulatory-view.js" group="entity-datalists"/>
</@>

<@markup id="widgets">
	<@createWidgets group="entity-datalists"/>
</@>

<@markup id="html">
	<@uniqueIdDiv>
		<#assign el = args.htmlid?html>
		<div id="${el}-body" class="regulatory-view">
			<#if enabled>
				<div id="${el}-message" class="regulatory-view-message">${msg("message.loading")?html}</div>
				<div id="${el}-frame" class="regulatory-view-frame"></div>
			<#else>
				<div class="regulatory-view-message">${msg("message.not-configured")?html}</div>
			</#if>
		</div>
	</@>
</@>
